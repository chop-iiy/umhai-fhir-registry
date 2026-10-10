// Part 1: Imports and Class Definition
package ca.uhn.fhir.jpa.starter.rest;

import ca.uhn.fhir.jpa.starter.custom.UmhaiInstance;
import ca.uhn.fhir.jpa.starter.service.UmhaiRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	public ResponseEntity<Map<String, Object>> registerInstance(
			@RequestBody Map<String, Object> request, HttpServletRequest servletRequest, HttpServletResponse response) {
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
			copyApplicationMetadata(application, registrationData);

			UmhaiInstance instance = registrationService.registerInstance(registrationData);
			Map<String, Object> responseData = new LinkedHashMap<>();
			responseData.put("status", "success");
			responseData.put("instanceUuid", instance.getInstanceUuid().getValue());
			responseData.put("hardwareSerial", instance.getHardwareSerial().getValue());
			responseData.put("appId", instance.getAppId().getValue());
			responseData.put("status", instance.getStatus().getValue());
			responseData.put("registeredAt", instance.getRegisteredAt().getValueAsString());
			responseData.put("metadata", metadataToMap(instance));
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
			response.put("metadata", metadataToMap(instance));
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
				if (instance.getAppName() != null)
					item.put("appName", instance.getAppName().getValue());
				if (instance.getPublisher() != null)
					item.put("publisher", instance.getPublisher().getValue());
				if (instance.getClassification() != null)
					item.put("classification", instance.getClassification().getValue());
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

	/**
	 * Copies the optional extended application metadata from the request's
	 * payload.application block (snake_case) into the service's registration map (camelCase).
	 * Only keys actually present in the request are copied, so absent fields are never overwritten.
	 */
	@SuppressWarnings("unchecked")
	private void copyApplicationMetadata(Map<String, Object> application, Map<String, Object> target) {
		copyIfPresent(application, "app_name", target, "appName");
		copyIfPresent(application, "publisher", target, "publisher");
		copyIfPresent(application, "publisher_reference", target, "publisherReference");
		copyIfPresent(application, "support_email", target, "supportEmail");
		copyIfPresent(application, "support_url", target, "supportUrl");
		copyIfPresent(application, "privacy_policy_url", target, "privacyPolicyUrl");
		copyIfPresent(application, "classification", target, "classification");
		copyIfPresent(application, "package_identifier", target, "packageIdentifier");
		copyIfPresent(application, "store_url", target, "storeUrl");
		copyIfPresent(application, "release_date", target, "releaseDate");

		Object checksum = application.get("checksum");
		if (checksum instanceof Map) {
			Map<String, Object> c = (Map<String, Object>) checksum;
			copyIfPresent(c, "algorithm", target, "checksumAlgorithm");
			copyIfPresent(c, "value", target, "checksumValue");
		}

		Object certifications = application.get("certifications");
		if (certifications instanceof List) {
			List<Map<String, Object>> mapped = new ArrayList<>();
			for (Object item : (List<Object>) certifications) {
				if (item instanceof Map) {
					Map<String, Object> in = (Map<String, Object>) item;
					Map<String, Object> out = new LinkedHashMap<>();
					copyIfPresent(in, "type", out, "type");
					copyIfPresent(in, "status", out, "status");
					copyIfPresent(in, "issued", out, "issued");
					copyIfPresent(in, "expires", out, "expires");
					copyIfPresent(in, "validator", out, "validator");
					mapped.add(out);
				} else {
					mapped.add(null); // rejected by service validation
				}
			}
			target.put("certifications", mapped);
		}
	}

	private void copyIfPresent(Map<String, Object> from, String fromKey, Map<String, Object> to, String toKey) {
		if (from != null && from.containsKey(fromKey)) {
			to.put(toKey, from.get(fromKey));
		}
	}

	/** Renders the extended metadata of an instance for API responses (omits unset fields). */
	private Map<String, Object> metadataToMap(UmhaiInstance instance) {
		Map<String, Object> m = new LinkedHashMap<>();
		putIfSet(m, "appName", instance.getAppName());
		putIfSet(m, "publisher", instance.getPublisher());
		putIfSet(m, "publisherReference", instance.getPublisherReference());
		putIfSet(m, "supportEmail", instance.getSupportEmail());
		putIfSet(m, "supportUrl", instance.getSupportUrl());
		putIfSet(m, "privacyPolicyUrl", instance.getPrivacyPolicyUrl());
		putIfSet(m, "classification", instance.getClassification());
		putIfSet(m, "packageIdentifier", instance.getPackageIdentifier());
		putIfSet(m, "storeUrl", instance.getStoreUrl());
		if (instance.getReleaseDate() != null) {
			m.put("releaseDate", instance.getReleaseDate().getValueAsString());
		}
		if (instance.getChecksumAlgorithm() != null && instance.getChecksumValue() != null) {
			Map<String, Object> checksum = new LinkedHashMap<>();
			checksum.put("algorithm", instance.getChecksumAlgorithm().getValue());
			checksum.put("value", instance.getChecksumValue().getValue());
			m.put("checksum", checksum);
		}
		if (instance.hasCertification()) {
			List<Map<String, Object>> certs = new ArrayList<>();
			for (UmhaiInstance.CertificationComponent c : instance.getCertification()) {
				Map<String, Object> cm = new LinkedHashMap<>();
				putIfSet(cm, "type", c.getType());
				putIfSet(cm, "status", c.getStatus());
				if (c.getIssued() != null) cm.put("issued", c.getIssued().getValueAsString());
				if (c.getExpires() != null) cm.put("expires", c.getExpires().getValueAsString());
				putIfSet(cm, "validator", c.getValidator());
				certs.add(cm);
			}
			m.put("certifications", certs);
		}
		return m;
	}

	private void putIfSet(Map<String, Object> map, String key, ca.uhn.fhir.model.primitive.StringDt value) {
		if (value != null && value.getValue() != null) {
			map.put(key, value.getValue());
		}
	}

	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> error = new LinkedHashMap<>();
		error.put("status", "error");
		error.put("message", message);
		return error;
	}
}
