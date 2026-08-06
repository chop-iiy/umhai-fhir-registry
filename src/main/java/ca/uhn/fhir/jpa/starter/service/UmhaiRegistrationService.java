package ca.uhn.fhir.jpa.starter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.jpa.starter.custom.UmhaiInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class UmhaiRegistrationService {
    private static final Logger ourLog = LoggerFactory.getLogger(UmhaiRegistrationService.class);

    private static final String NAMESPACE_DNS = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
    private static final String DATA_FILE = "umhai-data.json";

    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern HARDWARE_SERIAL_PATTERN = Pattern.compile(
        "^[A-Z0-9]{2,}(-[A-Z0-9]{2,}){0,3}$"
    );
    
    private static final Pattern APP_ID_PATTERN = Pattern.compile(
        "^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$"
    );
    
    private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile(
        "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    );
    
    private static final Pattern BUILD_NUMBER_PATTERN = Pattern.compile(
        "^[0-9]{1,10}$"
    );

    private static final Set<String> VALID_PLATFORMS = new HashSet<>(Arrays.asList(
        "ios", "android", "web", "windows", "macos", "linux", "other"
    ));

    private static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList(
        "Active", "Pending", "Decommissioned", "Transferred", "Suspended"
    ));

    private final Map<String, UmhaiInstance> instanceStore = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> historyLog = new ArrayList<>();
    private final ObjectMapper objectMapper;
    private final List<String> validationErrors = new ArrayList<>();

    public UmhaiRegistrationService() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        loadData();
    }

    public String generateUuidV5(String hardwareSerial, String appId) {
        try {
            String input = NAMESPACE_DNS + ":" + hardwareSerial + ":" + appId;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (hash[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (hash[i] & 0xff);
            }
            
            msb &= 0xffffffffffff0fffL;
            msb |= 0x0000000000005000L;
            
            lsb &= 0x3fffffffffffffffL;
            lsb |= 0x8000000000000000L;
            
            return String.format("%08x-%04x-%04x-%04x-%012x",
                (msb >> 32) & 0xffffffffL,
                (msb >> 16) & 0xffffL,
                msb & 0xffffL,
                (lsb >> 48) & 0xffffL,
                lsb & 0xffffffffffffL
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }

    public boolean validateHardwareSerial(String serial) {
        if (serial == null || serial.isEmpty()) {
            addValidationError("hardwareSerial is required");
            return false;
        }
        
        if (!HARDWARE_SERIAL_PATTERN.matcher(serial).matches()) {
            addValidationError("hardwareSerial must be alphanumeric with optional dashes");
            return false;
        }
        
        for (UmhaiInstance instance : instanceStore.values()) {
            if (instance.isActive() && 
                instance.getHardwareSerial().getValue().equals(serial)) {
                addValidationError("hardwareSerial '" + serial + "' is already registered and active");
                return false;
            }
        }
        
        return true;
    }

    public boolean validatePlatform(String platform) {
        if (platform == null || platform.isEmpty()) {
            addValidationError("platform is required");
            return false;
        }
        
        if (!VALID_PLATFORMS.contains(platform.toLowerCase())) {
            addValidationError("platform must be one of: " + String.join(", ", VALID_PLATFORMS));
            return false;
        }
        
        return true;
    }

    public boolean validateAppId(String appId) {
        if (appId == null || appId.isEmpty()) {
            addValidationError("appId is required");
            return false;
        }
        
        if (!APP_ID_PATTERN.matcher(appId).matches()) {
            addValidationError("appId must be in reverse domain notation (e.g., com.healthcare.provider)");
            return false;
        }
        
        if (appId.length() > 100) {
            addValidationError("appId must not exceed 100 characters");
            return false;
        }
        
        return true;
    }

    public boolean validateAppVersion(String version) {
        if (version == null || version.isEmpty()) {
            addValidationError("appVersion is required");
            return false;
        }
        
        if (!SEMANTIC_VERSION_PATTERN.matcher(version).matches()) {
            addValidationError("appVersion must follow semantic versioning (e.g., 2.4.0, 1.0.0-alpha.1)");
            return false;
        }
        
        return true;
    }

    public boolean validateBuildNumber(String buildNumber) {
        if (buildNumber == null || buildNumber.isEmpty()) {
            return true;
        }
        
        if (!BUILD_NUMBER_PATTERN.matcher(buildNumber).matches()) {
            addValidationError("buildNumber must be numeric (e.g., 1056)");
            return false;
        }
        
        return true;
    }

    public boolean validateStatus(String status) {
        if (status == null || status.isEmpty()) {
            addValidationError("status is required");
            return false;
        }
        
        if (!VALID_STATUSES.contains(status)) {
            addValidationError("status must be one of: " + String.join(", ", VALID_STATUSES));
            return false;
        }
        
        return true;
    }

    public boolean validateUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            addValidationError("instanceUuid is required");
            return false;
        }
        
        if (!UUID_PATTERN.matcher(uuid).matches()) {
            addValidationError("instanceUuid must be a valid UUID format");
            return false;
        }
        
        return true;
    }

    public boolean validateModel(String model) {
        if (model != null && model.length() > 100) {
            addValidationError("model must not exceed 100 characters");
            return false;
        }
        return true;
    }

    private void addValidationError(String error) {
        validationErrors.add(error);
    }

    private void clearValidationErrors() {
        validationErrors.clear();
    }

    public synchronized UmhaiInstance registerInstance(Map<String, Object> registrationData) {
        clearValidationErrors();
        String hardwareSerial = (String) registrationData.get("hardwareSerial");
        String appId = (String) registrationData.get("appId");
        String platform = (String) registrationData.get("platform");
        String model = (String) registrationData.get("model");
        String appVersion = (String) registrationData.get("appVersion");
        String buildNumber = (String) registrationData.get("buildNumber");
        Boolean isFirstLaunch = (Boolean) registrationData.getOrDefault("isFirstLaunch", true);
        String registeredBy = (String) registrationData.getOrDefault("registeredBy", "system");

        boolean isValid = true;
        isValid &= validateHardwareSerial(hardwareSerial);
        isValid &= validateAppId(appId);
        isValid &= validatePlatform(platform);
        isValid &= validateModel(model);
        isValid &= validateAppVersion(appVersion);
        isValid &= validateBuildNumber(buildNumber);
        isValid &= validateStatus("Active");
        
        if (!isValid) {
            throw new IllegalArgumentException("Validation failed: " + String.join("; ", validationErrors));
        }

        String instanceUuid = generateUuidV5(hardwareSerial, appId);
        UmhaiInstance existingInstance = instanceStore.get(instanceUuid);
        
        if (existingInstance != null && existingInstance.isActive()) {
            ourLog.info("[UMHAI SERVICE] Match found. Tracking existing active registration instance: {}", instanceUuid);
            existingInstance.setAppVersion(appVersion);
            if (buildNumber != null) {
                existingInstance.setBuildNumber(buildNumber);
            }
            existingInstance.setLastUpdated(new Date());
            existingInstance.setStatus("Active");
            logHistory(instanceUuid, "UPDATE", new Date(), "Instance updated");
            saveData();
            return existingInstance;
        } else {
            ourLog.info("[UMHAI SERVICE] No match found. Registering a brand new instance context: {}", instanceUuid);
            UmhaiInstance newInstance = new UmhaiInstance();
            newInstance.setInstanceUuid(instanceUuid);
            newInstance.setHardwareSerial(hardwareSerial);
            newInstance.setPlatform(platform);
            if (model != null) {
                newInstance.setModel(model);
            }
            newInstance.setAppId(appId);
            newInstance.setAppVersion(appVersion);
            if (buildNumber != null) {
                newInstance.setBuildNumber(buildNumber);
            }
            newInstance.setStatus("Active");
            newInstance.setFirstLaunch(isFirstLaunch);
            newInstance.setRegisteredAt(new Date());
            newInstance.setLastUpdated(new Date());
            newInstance.setRegisteredBy(registeredBy);

            instanceStore.put(instanceUuid, newInstance);
            logHistory(instanceUuid, "INSTALL", new Date(), "New instance registered");
            saveData();
            return newInstance;
        }
    }

    public UmhaiInstance findByUuid(String instanceUuid) {
        if (!validateUuid(instanceUuid)) {
            throw new IllegalArgumentException("Invalid UUID format: " + instanceUuid);
        }
        return instanceStore.get(instanceUuid);
    }

    public List<UmhaiInstance> getAllActiveInstances() {
        List<UmhaiInstance> active = new ArrayList<>();
        for (UmhaiInstance instance : instanceStore.values()) {
            if (instance.isActive()) {
                active.add(instance);
            }
        }
        return active;
    }

    public boolean validateInstance(String instanceUuid) {
        if (!validateUuid(instanceUuid)) {
            return false;
        }
        UmhaiInstance instance = instanceStore.get(instanceUuid);
        return instance != null && instance.isActive();
    }

    public void deleteInstance(String instanceUuid) {
        if (!validateUuid(instanceUuid)) {
            throw new IllegalArgumentException("Invalid UUID format: " + instanceUuid);
        }
        instanceStore.remove(instanceUuid);
        historyLog.removeIf(entry -> entry.get("instanceUuid").equals(instanceUuid));
        saveData();
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActiveInstances", instanceStore.values().stream().filter(UmhaiInstance::isActive).count());
        stats.put("totalInstances", instanceStore.size());
        stats.put("totalHistoryEvents", historyLog.size());
        stats.put("validPlatforms", VALID_PLATFORMS);
        stats.put("validStatuses", VALID_STATUSES);
        stats.put("status", "UP");
        return stats;
    }

    public Map<String, Object> getValidationRules() {
        Map<String, Object> rules = new HashMap<>();
        rules.put("hardwareSerialPattern", HARDWARE_SERIAL_PATTERN.pattern());
        rules.put("hardwareSerialExamples", Arrays.asList("SN99-23XJ-01", "SN-1234-5678", "ABC-DEF-GHI", "SN12345"));
        rules.put("appIdPattern", APP_ID_PATTERN.pattern());
        rules.put("semanticVersionPattern", SEMANTIC_VERSION_PATTERN.pattern());
        rules.put("buildNumberPattern", BUILD_NUMBER_PATTERN.pattern());
        rules.put("validPlatforms", VALID_PLATFORMS);
        rules.put("validStatuses", VALID_STATUSES);
        rules.put("maxModelLength", 100);
        return rules;
    }

    public synchronized UmhaiInstance handleHardwareTransfer(String hardwareSerial, String appId, Map<String, Object> newData) {
        String oldUuid = generateUuidV5(hardwareSerial, appId);
        UmhaiInstance oldInstance = instanceStore.get(oldUuid);
        
        if (oldInstance == null) {
            return registerInstance(newData);
        }

        oldInstance.setStatus("Decommissioned");
        oldInstance.setLastUpdated(new Date());
        logHistory(oldUuid, "DECOMMISSIONED", new Date(), "Hardware transferred");

        String transferId = UUID.randomUUID().toString();
        String newUuid = generateUuidV5(hardwareSerial + "_" + transferId, appId);
        
        UmhaiInstance newInstance = new UmhaiInstance();
        newInstance.setInstanceUuid(newUuid);
        newInstance.setHardwareSerial(hardwareSerial);
        newInstance.setPlatform((String) newData.get("platform"));
        newInstance.setModel((String) newData.get("model"));
        newInstance.setAppId(appId);
        newInstance.setAppVersion((String) newData.get("appVersion"));
        newInstance.setBuildNumber((String) newData.get("buildNumber"));
        newInstance.setStatus("Active");
        newInstance.setFirstLaunch(true);
        newInstance.setPreviousInstanceUuid(oldUuid);
        newInstance.setRegisteredAt(new Date());
        newInstance.setLastUpdated(new Date());
        newInstance.setRegisteredBy((String) newData.getOrDefault("registeredBy", "system"));

        instanceStore.put(newUuid, newInstance);
        logHistory(newUuid, "TRANSFER", new Date(), "New instance after transfer");
        saveData();
        return newInstance;
    }

    private void loadData() {
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(DATA_FILE)));
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(content, Map.class);
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> instances = (List<Map<String, Object>>) data.get("instances");
                if (instances != null) {
                    for (Map<String, Object> instanceData : instances) {
                        UmhaiInstance instance = new UmhaiInstance();
                        instance.setInstanceUuid((String) instanceData.get("instanceUuid"));
                        instance.setHardwareSerial((String) instanceData.get("hardwareSerial"));
                        instance.setPlatform((String) instanceData.get("platform"));
                        instance.setModel((String) instanceData.get("model"));
                        instance.setAppId((String) instanceData.get("appId"));
                        instance.setAppVersion((String) instanceData.get("appVersion"));
                        instance.setBuildNumber((String) instanceData.get("buildNumber"));
                        instance.setStatus((String) instanceData.get("status"));
                        instance.setFirstLaunch((Boolean) instanceData.get("firstLaunch"));
                        if (instanceData.get("registeredAt") != null) {
                            instance.setRegisteredAt(new Date((Long) instanceData.get("registeredAt")));
                        }
                        if (instanceData.get("lastUpdated") != null) {
                            instance.setLastUpdated(new Date((Long) instanceData.get("lastUpdated")));
                        }
                        if (instanceData.get("previousInstanceUuid") != null) {
                            instance.setPreviousInstanceUuid((String) instanceData.get("previousInstanceUuid"));
                        }
                        if (instanceData.get("registeredBy") != null) {
                            instance.setRegisteredBy((String) instanceData.get("registeredBy"));
                        }
                        instanceStore.put(instance.getInstanceUuid().getValue(), instance);
                    }
                    System.out.println("Loaded " + instanceStore.size() + " instances from file");
                }
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> history = (List<Map<String, Object>>) data.get("history");
                if (history != null) {
                    historyLog.addAll(history);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load data from file: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            Map<String, Object> data = new HashMap<>();
            List<Map<String, Object>> instances = new ArrayList<>();
            for (UmhaiInstance instance : instanceStore.values()) {
                Map<String, Object> instanceData = new HashMap<>();
                instanceData.put("instanceUuid", instance.getInstanceUuid().getValue());
                instanceData.put("hardwareSerial", instance.getHardwareSerial().getValue());
                instanceData.put("platform", instance.getPlatform().getValue());
                instanceData.put("model", instance.getModel() != null ? instance.getModel().getValue() : null);
                instanceData.put("appId", instance.getAppId().getValue());
                instanceData.put("appVersion", instance.getAppVersion().getValue());
                instanceData.put("buildNumber", instance.getBuildNumber() != null ? instance.getBuildNumber().getValue() : null);
                instanceData.put("status", instance.getStatus().getValue());
                instanceData.put("firstLaunch", instance.getFirstLaunch() != null ? instance.getFirstLaunch().getValue() : false);
                instanceData.put("registeredAt", instance.getRegisteredAt().getValue().getTime());
                instanceData.put("lastUpdated", instance.getLastUpdated() != null ? instance.getLastUpdated().getValue().getTime() : null);
                instanceData.put("previousInstanceUuid", instance.getPreviousInstanceUuid() != null ? instance.getPreviousInstanceUuid().getValue() : null);
                instanceData.put("registeredBy", instance.getRegisteredBy() != null ? instance.getRegisteredBy().getValue() : null);
                instances.add(instanceData);
            }
            data.put("instances", instances);
            data.put("history", historyLog);
            data.put("lastSaved", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(data);
            Files.write(Paths.get(DATA_FILE), json.getBytes(StandardCharsets.UTF_8));
            System.out.println("Saved " + instanceStore.size() + " instances to file");
        } catch (IOException e) {
            System.err.println("Could not save data to file: " + e.getMessage());
        }
    }

    private void logHistory(String instanceUuid, String eventType, Date timestamp, String description) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("eventId", UUID.randomUUID().toString());
        logEntry.put("instanceUuid", instanceUuid);
        logEntry.put("eventType", eventType);
        logEntry.put("timestamp", timestamp.getTime());
        logEntry.put("description", description);
        historyLog.add(logEntry);
        ourLog.info("[UMHAI AUDIT] Logged History Event -> Instance: {} | Action: {}", instanceUuid, eventType);
    }
}