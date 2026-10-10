package ca.uhn.fhir.jpa.starter.service;

import ca.uhn.fhir.jpa.starter.custom.UmhaiInstance;
import ca.uhn.fhir.jpa.starter.entity.UmhaiCertificationEmbeddable;
import ca.uhn.fhir.jpa.starter.entity.UmhaiHistoryEntity;
import ca.uhn.fhir.jpa.starter.entity.UmhaiInstanceEntity;
import ca.uhn.fhir.jpa.starter.mapper.UmhaiMapper;
import ca.uhn.fhir.jpa.starter.repository.UmhaiHistoryRepository;
import ca.uhn.fhir.jpa.starter.repository.UmhaiInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import java.util.*;

@Service
public class UmhaiRegistrationService {
	private static final Logger ourLog = LoggerFactory.getLogger(UmhaiRegistrationService.class);

	private static final String NAMESPACE_DNS = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";

	private static final Pattern UUID_PATTERN =
			Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

	private static final Pattern HARDWARE_SERIAL_PATTERN = Pattern.compile("^[A-Z0-9]{2,}(-[A-Z0-9]{2,}){0,3}$");

	private static final Pattern APP_ID_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$");

	private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile(
			"^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

	private static final Pattern BUILD_NUMBER_PATTERN = Pattern.compile("^[0-9]{1,10}$");

	private static final Set<String> VALID_PLATFORMS =
			new HashSet<>(Arrays.asList("ios", "android", "web", "windows", "macos", "linux", "other"));

	private static final Set<String> VALID_STATUSES =
			new HashSet<>(Arrays.asList("Active", "Pending", "Decommissioned", "Transferred", "Suspended"));

	// ---- Extended (FHIR R6 incubator) metadata rules ----

	private static final Set<String> VALID_CLASSIFICATIONS =
			new LinkedHashSet<>(Arrays.asList("wellness", "samd", "digital-therapeutic", "ehr-companion", "other"));

	/** Hash algorithm -> expected hex length of the digest. */
	private static final Map<String, Integer> CHECKSUM_HEX_LENGTHS = new LinkedHashMap<>();

	static {
		CHECKSUM_HEX_LENGTHS.put("sha-256", 64);
		CHECKSUM_HEX_LENGTHS.put("sha-384", 96);
		CHECKSUM_HEX_LENGTHS.put("sha-512", 128);
	}

	private static final Set<String> VALID_CERTIFICATION_STATUSES =
			new LinkedHashSet<>(Arrays.asList("active", "expired", "revoked"));

	private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private static final Pattern URL_PATTERN = Pattern.compile("^https?://[^\\s]+$", Pattern.CASE_INSENSITIVE);

	/** iOS bundle IDs and Android package names: dotted segments, letters/digits/_/-. */
	private static final Pattern PACKAGE_ID_PATTERN =
			Pattern.compile("^[A-Za-z][A-Za-z0-9_-]*(\\.[A-Za-z][A-Za-z0-9_-]*)+$");

	private static final Pattern PUBLISHER_REFERENCE_PATTERN = Pattern.compile("^Organization/[A-Za-z0-9\\-.]{1,64}$");

	private final UmhaiInstanceRepository instanceRepository;
	private final UmhaiHistoryRepository historyRepository;
	private final UmhaiMapper mapper;
	private final List<String> validationErrors = new ArrayList<>();

	public UmhaiRegistrationService(
			UmhaiInstanceRepository instanceRepository, UmhaiHistoryRepository historyRepository, UmhaiMapper mapper) {
		this.instanceRepository = instanceRepository;
		this.historyRepository = historyRepository;
		this.mapper = mapper;
	}

	/**
	 * Instance identity is derived from hardware serial + app ID + app version + build number.
	 * Any change to app version or build number must produce a different UUID — this is how
	 * "issue a new UMHAI ID when the running app version/build changes" is implemented.
	 */
	public String generateUuidV5(String hardwareSerial, String appId, String appVersion, String buildNumber) {
		try {
			String input = NAMESPACE_DNS + ":" + hardwareSerial + ":" + appId
					+ ":" + (appVersion != null ? appVersion : "")
					+ ":" + (buildNumber != null ? buildNumber : "");
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

			return String.format(
					"%08x-%04x-%04x-%04x-%012x",
					(msb >> 32) & 0xffffffffL,
					(msb >> 16) & 0xffffL,
					msb & 0xffffL,
					(lsb >> 48) & 0xffffL,
					lsb & 0xffffffffffffL);
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

		// Note: whether this hardware serial already has an active registration is no longer
		// a validation error here. registerInstance() looks that up itself and decides whether
		// to treat the request as "no change", "version/build update" (issue new UMHAI ID), or
		// "brand new registration" — see registerInstance() for the actual business rule.
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

	// ------------------------------------------------------------------
	// Extended metadata: validation
	// ------------------------------------------------------------------

	private boolean validateOptionalLength(String field, String value, int max) {
		if (value != null && value.length() > max) {
			addValidationError(field + " must not exceed " + max + " characters");
			return false;
		}
		return true;
	}

	private boolean validateOptionalPattern(String field, String value, Pattern pattern, String hint) {
		if (value != null && !value.isEmpty() && !pattern.matcher(value).matches()) {
			addValidationError(field + " " + hint);
			return false;
		}
		return true;
	}

	/**
	 * Validates the optional app-identity / packaging / integrity / certification fields.
	 * Every field is optional; only supplied values are checked.
	 */
	@SuppressWarnings("unchecked")
	public boolean validateMetadata(Map<String, Object> data) {
		boolean ok = true;

		ok &= validateOptionalLength("appName", (String) data.get("appName"), 200);
		ok &= validateOptionalLength("publisher", (String) data.get("publisher"), 200);

		String publisherReference = (String) data.get("publisherReference");
		ok &= validateOptionalPattern(
				"publisherReference",
				publisherReference,
				PUBLISHER_REFERENCE_PATTERN,
				"must be of the form Organization/<id>");

		String supportEmail = (String) data.get("supportEmail");
		ok &= validateOptionalLength("supportEmail", supportEmail, 254);
		ok &= validateOptionalPattern("supportEmail", supportEmail, EMAIL_PATTERN, "must be a valid e-mail address");

		for (String urlField : new String[] {"supportUrl", "privacyPolicyUrl", "storeUrl"}) {
			String url = (String) data.get(urlField);
			ok &= validateOptionalLength(urlField, url, 500);
			ok &= validateOptionalPattern(urlField, url, URL_PATTERN, "must be an http(s) URL");
		}

		String classification = (String) data.get("classification");
		if (classification != null && !VALID_CLASSIFICATIONS.contains(classification)) {
			addValidationError("classification must be one of: " + String.join(", ", VALID_CLASSIFICATIONS));
			ok = false;
		}

		String packageIdentifier = (String) data.get("packageIdentifier");
		ok &= validateOptionalLength("packageIdentifier", packageIdentifier, 150);
		ok &= validateOptionalPattern(
				"packageIdentifier",
				packageIdentifier,
				PACKAGE_ID_PATTERN,
				"must be a dotted package/bundle identifier (e.g., com.organization.app)");

		if (data.get("releaseDate") != null && parseDate(data.get("releaseDate")) == null) {
			addValidationError(
					"releaseDate must be an ISO-8601 date or date-time (e.g., 2026-03-15 or 2026-03-15T09:00:00Z)");
			ok = false;
		}

		ok &= validateChecksum((String) data.get("checksumAlgorithm"), (String) data.get("checksumValue"));

		Object certs = data.get("certifications");
		if (certs != null) {
			if (!(certs instanceof List)) {
				addValidationError("certifications must be an array");
				ok = false;
			} else {
				int index = 0;
				for (Object item : (List<Object>) certs) {
					ok &= validateCertification(index++, item);
				}
			}
		}
		return ok;
	}

	private boolean validateChecksum(String algorithm, String value) {
		if (algorithm == null && value == null) {
			return true;
		}
		if (algorithm == null || value == null) {
			addValidationError("checksumAlgorithm and checksumValue must be supplied together");
			return false;
		}
		Integer expected = CHECKSUM_HEX_LENGTHS.get(algorithm.toLowerCase());
		if (expected == null) {
			addValidationError("checksumAlgorithm must be one of: " + String.join(", ", CHECKSUM_HEX_LENGTHS.keySet()));
			return false;
		}
		if (!HEX_PATTERN.matcher(value).matches() || value.length() != expected) {
			addValidationError(
					"checksumValue must be a " + expected + "-character hex string for " + algorithm.toLowerCase());
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean validateCertification(int index, Object item) {
		String prefix = "certifications[" + index + "].";
		if (!(item instanceof Map)) {
			addValidationError("certifications[" + index + "] must be an object");
			return false;
		}
		Map<String, Object> cert = (Map<String, Object>) item;
		boolean ok = true;

		String type = (String) cert.get("type");
		if (type == null || type.isEmpty()) {
			addValidationError(prefix + "type is required");
			ok = false;
		} else {
			ok &= validateOptionalLength(prefix + "type", type, 50);
		}

		String status = (String) cert.get("status");
		if (status == null || !VALID_CERTIFICATION_STATUSES.contains(status)) {
			addValidationError(prefix + "status must be one of: " + String.join(", ", VALID_CERTIFICATION_STATUSES));
			ok = false;
		}

		Date issued = null;
		Date expires = null;
		if (cert.get("issued") != null) {
			issued = parseDate(cert.get("issued"));
			if (issued == null) {
				addValidationError(prefix + "issued must be an ISO-8601 date or date-time");
				ok = false;
			}
		}
		if (cert.get("expires") != null) {
			expires = parseDate(cert.get("expires"));
			if (expires == null) {
				addValidationError(prefix + "expires must be an ISO-8601 date or date-time");
				ok = false;
			}
		}
		if (issued != null && expires != null && expires.before(issued)) {
			addValidationError(prefix + "expires must not be before issued");
			ok = false;
		}
		ok &= validateOptionalLength(prefix + "validator", (String) cert.get("validator"), 200);
		return ok;
	}

	/** Accepts ISO-8601 date-time (with offset) or plain date; returns null if unparseable. */
	private Date parseDate(Object value) {
		if (value instanceof Number) {
			return new Date(((Number) value).longValue());
		}
		if (!(value instanceof String)) {
			return null;
		}
		String text = ((String) value).trim();
		try {
			return Date.from(Instant.parse(text));
		} catch (DateTimeParseException ignored) {
			// fall through to plain date
		}
		try {
			return Date.from(LocalDate.parse(text).atStartOfDay().toInstant(ZoneOffset.UTC));
		} catch (DateTimeParseException ignored) {
			return null;
		}
	}

	// ------------------------------------------------------------------
	// Extended metadata: persistence
	// ------------------------------------------------------------------

	/**
	 * Copies any supplied extended metadata onto the entity. Keys that are absent are left
	 * untouched, so a plain re-registration never wipes previously stored metadata.
	 * Must be called after validateMetadata().
	 */
	@SuppressWarnings("unchecked")
	private void applyMetadata(UmhaiInstanceEntity entity, Map<String, Object> data) {
		if (data == null) {
			return;
		}
		if (data.containsKey("appName")) entity.setAppName((String) data.get("appName"));
		if (data.containsKey("publisher")) entity.setPublisher((String) data.get("publisher"));
		if (data.containsKey("publisherReference"))
			entity.setPublisherReference((String) data.get("publisherReference"));
		if (data.containsKey("supportEmail")) entity.setSupportEmail((String) data.get("supportEmail"));
		if (data.containsKey("supportUrl")) entity.setSupportUrl((String) data.get("supportUrl"));
		if (data.containsKey("privacyPolicyUrl")) entity.setPrivacyPolicyUrl((String) data.get("privacyPolicyUrl"));
		if (data.containsKey("classification")) entity.setClassification((String) data.get("classification"));
		if (data.containsKey("packageIdentifier")) entity.setPackageIdentifier((String) data.get("packageIdentifier"));
		if (data.containsKey("storeUrl")) entity.setStoreUrl((String) data.get("storeUrl"));
		if (data.containsKey("releaseDate")) entity.setReleaseDate(parseDate(data.get("releaseDate")));
		if (data.containsKey("checksumAlgorithm")) {
			String algorithm = (String) data.get("checksumAlgorithm");
			entity.setChecksumAlgorithm(algorithm != null ? algorithm.toLowerCase() : null);
		}
		if (data.containsKey("checksumValue")) {
			String value = (String) data.get("checksumValue");
			entity.setChecksumValue(value != null ? value.toLowerCase() : null);
		}
		if (data.get("certifications") instanceof List) {
			List<UmhaiCertificationEmbeddable> certs = new ArrayList<>();
			for (Object item : (List<Object>) data.get("certifications")) {
				Map<String, Object> c = (Map<String, Object>) item;
				certs.add(new UmhaiCertificationEmbeddable(
						(String) c.get("type"),
						(String) c.get("status"),
						parseDate(c.get("issued")),
						parseDate(c.get("expires")),
						(String) c.get("validator")));
			}
			entity.getCertifications().clear();
			entity.getCertifications().addAll(certs);
		}
	}

	private void addValidationError(String error) {
		validationErrors.add(error);
	}

	private void clearValidationErrors() {
		validationErrors.clear();
	}

	@Transactional
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
		isValid &= validateMetadata(registrationData);

		if (!isValid) {
			throw new IllegalArgumentException("Validation failed: " + String.join("; ", validationErrors));
		}

		String instanceUuid = generateUuidV5(hardwareSerial, appId, appVersion, buildNumber);

		// Is there already an active instance for this hardware+app, at ANY version/build?
		Optional<UmhaiInstanceEntity> currentActive =
				instanceRepository.findByHardwareSerialAndAppIdAndStatus(hardwareSerial, appId, "Active");

		if (currentActive.isPresent() && currentActive.get().getInstanceUuid().equals(instanceUuid)) {
			// Case 1: identical hardware + app + version + build already active — this is the
			// same running instance checking in again. No new ID, just a heartbeat/touch.
			ourLog.info("[UMHAI SERVICE] Unchanged instance checking in: {}", instanceUuid);
			UmhaiInstanceEntity entity = currentActive.get();
			entity.setLastUpdated(new Date());
			applyMetadata(entity, registrationData);
			UmhaiInstanceEntity saved = instanceRepository.save(entity);
			logHistory(instanceUuid, "CHECKIN", "Instance re-checked in, no version/build change");
			return mapper.toModel(saved);
		}

		if (currentActive.isPresent()) {
			// Case 2: same hardware + app, but version and/or build changed — supersede the old
			// instance with a newly-issued UMHAI ID, chained via previousInstanceUuid.
			UmhaiInstanceEntity oldEntity = currentActive.get();
			String oldUuid = oldEntity.getInstanceUuid();
			ourLog.info(
					"[UMHAI SERVICE] Version/build changed for {} ({} {} -> {} {}). "
							+ "Decommissioning {} and issuing new UMHAI ID {}.",
					hardwareSerial,
					oldEntity.getAppVersion(),
					oldEntity.getBuildNumber(),
					appVersion,
					buildNumber,
					oldUuid,
					instanceUuid);

			oldEntity.setStatus("Decommissioned");
			oldEntity.setLastUpdated(new Date());
			oldEntity.setDecommissionedAt(new Date());
			oldEntity.setDecommissionedReason("Superseded by version/build update ("
					+ oldEntity.getAppVersion() + "/" + oldEntity.getBuildNumber()
					+ " -> " + appVersion + "/" + buildNumber + ")");
			instanceRepository.save(oldEntity);
			logHistory(oldUuid, "DECOMMISSIONED", "Superseded by new UMHAI ID " + instanceUuid);

			UmhaiInstanceEntity newEntity =
					instanceRepository.findById(instanceUuid).orElse(new UmhaiInstanceEntity());
			newEntity.setInstanceUuid(instanceUuid);
			newEntity.setHardwareSerial(hardwareSerial);
			newEntity.setPlatform(platform);
			if (model != null) {
				newEntity.setModel(model);
			}
			newEntity.setAppId(appId);
			newEntity.setAppVersion(appVersion);
			if (buildNumber != null) {
				newEntity.setBuildNumber(buildNumber);
			}
			newEntity.setStatus("Active");
			newEntity.setFirstLaunch(isFirstLaunch);
			newEntity.setPreviousInstanceUuid(oldUuid);
			newEntity.setRegisteredAt(new Date());
			newEntity.setLastUpdated(new Date());
			newEntity.setRegisteredBy(registeredBy);
			carryOverMetadata(oldEntity, newEntity);
			applyMetadata(newEntity, registrationData);

			UmhaiInstanceEntity saved = instanceRepository.save(newEntity);
			logHistory(instanceUuid, "VERSION_UPDATE", "New UMHAI ID issued, supersedes " + oldUuid);
			return mapper.toModel(saved);
		}

		// Case 3: no active instance exists for this hardware+app at all — brand new registration.
		// (If this exact hardware+app+version+build combo was previously decommissioned, this
		// reactivates that same row rather than creating a duplicate — same deterministic UUID.)
		ourLog.info("[UMHAI SERVICE] No active instance found. Registering: {}", instanceUuid);
		UmhaiInstanceEntity entity = instanceRepository.findById(instanceUuid).orElse(new UmhaiInstanceEntity());
		entity.setInstanceUuid(instanceUuid);
		entity.setHardwareSerial(hardwareSerial);
		entity.setPlatform(platform);
		if (model != null) {
			entity.setModel(model);
		}
		entity.setAppId(appId);
		entity.setAppVersion(appVersion);
		if (buildNumber != null) {
			entity.setBuildNumber(buildNumber);
		}
		entity.setStatus("Active");
		entity.setFirstLaunch(isFirstLaunch);
		entity.setRegisteredAt(new Date());
		entity.setLastUpdated(new Date());
		entity.setRegisteredBy(registeredBy);
		entity.setDecommissionedAt(null);
		entity.setDecommissionedReason(null);
		applyMetadata(entity, registrationData);

		UmhaiInstanceEntity saved = instanceRepository.save(entity);
		logHistory(instanceUuid, "INSTALL", "New instance registered");
		return mapper.toModel(saved);
	}

	public UmhaiInstance findByUuid(String instanceUuid) {
		if (!validateUuid(instanceUuid)) {
			throw new IllegalArgumentException("Invalid UUID format: " + instanceUuid);
		}
		return instanceRepository.findById(instanceUuid).map(mapper::toModel).orElse(null);
	}

	public List<UmhaiInstance> getAllActiveInstances() {
		List<UmhaiInstanceEntity> activeEntities = instanceRepository.findByStatus("Active");
		List<UmhaiInstance> active = new ArrayList<>();
		for (UmhaiInstanceEntity entity : activeEntities) {
			active.add(mapper.toModel(entity));
		}
		return active;
	}

	public boolean validateInstance(String instanceUuid) {
		if (!validateUuid(instanceUuid)) {
			return false;
		}
		return instanceRepository
				.findById(instanceUuid)
				.map(entity -> "Active".equals(entity.getStatus()))
				.orElse(false);
	}

	@Transactional
	public void deleteInstance(String instanceUuid) {
		if (!validateUuid(instanceUuid)) {
			throw new IllegalArgumentException("Invalid UUID format: " + instanceUuid);
		}
		instanceRepository.deleteById(instanceUuid);
		historyRepository.deleteByInstanceUuid(instanceUuid);
	}

	public Map<String, Object> getStatistics() {
		Map<String, Object> stats = new HashMap<>();
		stats.put("totalActiveInstances", instanceRepository.countByStatus("Active"));
		stats.put("totalInstances", instanceRepository.count());
		stats.put("totalHistoryEvents", historyRepository.count());
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
		rules.put("validClassifications", VALID_CLASSIFICATIONS);
		rules.put("checksumAlgorithms", CHECKSUM_HEX_LENGTHS);
		rules.put("validCertificationStatuses", VALID_CERTIFICATION_STATUSES);
		rules.put("packageIdentifierPattern", PACKAGE_ID_PATTERN.pattern());
		rules.put("publisherReferencePattern", PUBLISHER_REFERENCE_PATTERN.pattern());
		return rules;
	}

	@Transactional
	public synchronized UmhaiInstance handleHardwareTransfer(
			String hardwareSerial, String appId, Map<String, Object> newData) {
		Optional<UmhaiInstanceEntity> oldEntityOpt =
				instanceRepository.findByHardwareSerialAndAppIdAndStatus(hardwareSerial, appId, "Active");

		if (oldEntityOpt.isEmpty()) {
			return registerInstance(newData);
		}

		clearValidationErrors();
		if (!validateMetadata(newData)) {
			throw new IllegalArgumentException("Validation failed: " + String.join("; ", validationErrors));
		}

		UmhaiInstanceEntity oldEntity = oldEntityOpt.get();
		String oldUuid = oldEntity.getInstanceUuid();
		oldEntity.setStatus("Decommissioned");
		oldEntity.setLastUpdated(new Date());
		oldEntity.setDecommissionedAt(new Date());
		oldEntity.setDecommissionedReason("Hardware transferred");
		instanceRepository.save(oldEntity);
		logHistory(oldUuid, "DECOMMISSIONED", "Hardware transferred");

		String newAppVersion = (String) newData.get("appVersion");
		String newBuildNumber = (String) newData.get("buildNumber");
		String newUuid = generateUuidV5(hardwareSerial, appId, newAppVersion, newBuildNumber);

		UmhaiInstanceEntity newEntity = instanceRepository.findById(newUuid).orElse(new UmhaiInstanceEntity());
		newEntity.setInstanceUuid(newUuid);
		newEntity.setHardwareSerial(hardwareSerial);
		newEntity.setPlatform((String) newData.get("platform"));
		newEntity.setModel((String) newData.get("model"));
		newEntity.setAppId(appId);
		newEntity.setAppVersion(newAppVersion);
		newEntity.setBuildNumber(newBuildNumber);
		newEntity.setStatus("Active");
		newEntity.setFirstLaunch(true);
		newEntity.setPreviousInstanceUuid(oldUuid);
		newEntity.setRegisteredAt(new Date());
		newEntity.setLastUpdated(new Date());
		newEntity.setRegisteredBy((String) newData.getOrDefault("registeredBy", "system"));
		newEntity.setDecommissionedAt(null);
		newEntity.setDecommissionedReason(null);
		carryOverMetadata(oldEntity, newEntity);
		applyMetadata(newEntity, newData);

		UmhaiInstanceEntity savedNew = instanceRepository.save(newEntity);
		logHistory(newUuid, "TRANSFER", "New instance after transfer");
		return mapper.toModel(savedNew);
	}

	/**
	 * A new UMHAI ID is issued whenever version/build changes, but the app's identity,
	 * publisher, packaging and certification metadata normally still applies — carry it
	 * forward so the superseding instance is not left blank. Per-release fields
	 * (release date, checksum) are NOT carried over because they describe a specific binary.
	 */
	private void carryOverMetadata(UmhaiInstanceEntity from, UmhaiInstanceEntity to) {
		to.setAppName(from.getAppName());
		to.setPublisher(from.getPublisher());
		to.setPublisherReference(from.getPublisherReference());
		to.setSupportEmail(from.getSupportEmail());
		to.setSupportUrl(from.getSupportUrl());
		to.setPrivacyPolicyUrl(from.getPrivacyPolicyUrl());
		to.setClassification(from.getClassification());
		to.setPackageIdentifier(from.getPackageIdentifier());
		to.setStoreUrl(from.getStoreUrl());
		to.getCertifications().clear();
		for (UmhaiCertificationEmbeddable c : from.getCertifications()) {
			to.getCertifications()
					.add(new UmhaiCertificationEmbeddable(
							c.getType(), c.getStatus(), c.getIssued(), c.getExpires(), c.getValidator()));
		}
	}

	private void logHistory(String instanceUuid, String eventType, String description) {
		UmhaiHistoryEntity entity = mapper.toHistoryEntity(instanceUuid, eventType, description);
		historyRepository.save(entity);
		ourLog.info("[UMHAI AUDIT] Logged History Event -> Instance: {} | Action: {}", instanceUuid, eventType);
	}
}
