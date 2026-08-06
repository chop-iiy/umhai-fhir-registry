// Part 1: Imports and Class Definition
package ca.uhn.fhir.jpa.starter.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.jpa.starter.custom.UmhaiInstance;
import ca.uhn.fhir.jpa.starter.service.UmhaiRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/api/umhai")
public class UmhaiRegistrationEndpoint {
    private static final Logger ourLog = LoggerFactory.getLogger(UmhaiRegistrationEndpoint.class);
    private final UmhaiRegistrationService registrationService;

    public UmhaiRegistrationEndpoint(UmhaiRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        ourLog.info("[UMHAI REST] Incoming Health Check invocation.");
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("service", "UMHAI Central Registry");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        ourLog.info("[UMHAI REST] Fetching Registry aggregation metrics.");
        return ResponseEntity.ok(registrationService.getStatistics());
    }

    @GetMapping("/validation-rules")
    public ResponseEntity<Map<String, Object>> getValidationRules() {
        ourLog.info("[UMHAI REST] Fetching compliance verification criteria.");
        return ResponseEntity.ok(registrationService.getValidationRules());
    }
// Part 2: Register Method
    @SuppressWarnings("unchecked")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerInstance(@RequestBody Map<String, Object> request,
                                                                  HttpServletRequest servletRequest,
                                                                  HttpServletResponse response) {
        ourLog.info("[UMHAI REST] Processing Instance Registration. Payload: {}", request);
        try {
            Map<String, Object> payload = (Map<String, Object>) request.get("payload");
            if (payload == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Missing 'payload'"));
            }
            Map<String, Object> hardware = (Map<String, Object>) payload.get("hardware");
            Map<String, Object> application = (Map<String, Object>) payload.get("application");
            Map<String, Object> context = (Map<String, Object>) payload.get("context");

            Map<String, Object> registrationData = new HashMap<>();
            registrationData.put("hardwareSerial", hardware.get("serial_number"));
            registrationData.put("platform", hardware.get("platform"));
            registrationData.put("model", hardware.get("model"));
            registrationData.put("appId", application.get("app_id"));
            registrationData.put("appVersion", application.get("version"));
            registrationData.put("buildNumber", application.get("build_number"));
            registrationData.put("isFirstLaunch", context != null ? context.get("is_first_launch") : true);
            registrationData.put("registeredBy", servletRequest.getRemoteAddr());

            UmhaiInstance instance = registrationService.registerInstance(registrationData);
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("status", "success");
            responseData.put("instanceUuid", instance.getInstanceUuid().getValue());
            responseData.put("hardwareSerial", instance.getHardwareSerial().getValue());
            responseData.put("appId", instance.getAppId().getValue());
            responseData.put("status", instance.getStatus().getValue());
            responseData.put("registeredAt", instance.getRegisteredAt().getValueAsString());
            responseData.put("message", "Registered successfully");

            response.setHeader("X-App-Instance-ID", instance.getInstanceUuid().getValue());
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(e.getLocalizedMessage()));
        }
    }
// Part 3: Other Methods and Closing
    @GetMapping("/validate/{instanceUuid}")
    public ResponseEntity<Map<String, Object>> validateInstance(@PathVariable("instanceUuid") String instanceUuid) {
        ourLog.info("[UMHAI REST] Verifying token status for UMHAI UUID: {}", instanceUuid);
        try {
            boolean isValid = registrationService.validateInstance(instanceUuid);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("valid", isValid);
            response.put("instanceUuid", instanceUuid);
            if (isValid) {
                UmhaiInstance instance = registrationService.findByUuid(instanceUuid);
                if (instance != null) {
                    response.put("hardwareSerial", instance.getHardwareSerial().getValue());
                    response.put("appId", instance.getAppId().getValue());
                    response.put("status", instance.getStatus().getValue());
                }
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/instance/{instanceUuid}")
    public ResponseEntity<Map<String, Object>> getInstance(@PathVariable("instanceUuid") String instanceUuid) {
        ourLog.info("[UMHAI REST] Retrieving metadata for profile index: {}", instanceUuid);
        try {
            UmhaiInstance instance = registrationService.findByUuid(instanceUuid);
            if (instance == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Missing instance"));
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("instanceUuid", instance.getInstanceUuid().getValue());
            response.put("hardwareSerial", instance.getHardwareSerial().getValue());
            response.put("platform", instance.getPlatform().getValue());
            response.put("appId", instance.getAppId().getValue());
            response.put("status", instance.getStatus().getValue());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/instances")
    public ResponseEntity<Map<String, Object>> getAllInstances() {
        ourLog.info("[UMHAI REST] Querying comprehensive registry records dataset.");
        try {
            List<UmhaiInstance> instances = registrationService.getAllActiveInstances();
            List<Map<String, Object>> instanceList = new ArrayList<>();
            for (UmhaiInstance instance : instances) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("instanceUuid", instance.getInstanceUuid().getValue());
                item.put("hardwareSerial", instance.getHardwareSerial().getValue());
                item.put("appId", instance.getAppId().getValue());
                instanceList.add(item);
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("instances", instanceList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transferHardware(@RequestBody Map<String, Object> request) {
        ourLog.info("[UMHAI REST] Intercepting Hardware Migrations request.");
        try {
            String hardwareSerial = (String) request.get("hardwareSerial");
            String appId = (String) request.get("appId");
            Map<String, Object> newData = (Map<String, Object>) request.get("newData");
            UmhaiInstance instance = registrationService.handleHardwareTransfer(hardwareSerial, appId, newData);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "transferred");
            response.put("instanceUuid", instance.getInstanceUuid().getValue());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/instance/{instanceUuid}")
    public ResponseEntity<Map<String, Object>> deleteInstance(@PathVariable("instanceUuid") String instanceUuid) {
        ourLog.info("[UMHAI REST] Evicting Instance Reference index entry: {}", instanceUuid);
        try {
            registrationService.deleteInstance(instanceUuid);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", "error");
        error.put("message", message);
        return error;
    }
}
