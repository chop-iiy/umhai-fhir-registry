package ca.uhn.fhir.jpa.starter.config;

import ca.uhn.fhir.jpa.starter.entity.UmhaiHistoryEntity;
import ca.uhn.fhir.jpa.starter.entity.UmhaiInstanceEntity;
import ca.uhn.fhir.jpa.starter.repository.UmhaiHistoryRepository;
import ca.uhn.fhir.jpa.starter.repository.UmhaiInstanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * One-time migration: imports records from the legacy umhai-data.json file
 * into the UMHAI_INSTANCES / UMHAI_HISTORY database tables via JPA.
 *
 * Disabled by default. Enable for a single run with:
 *   mvn spring-boot:run -Dspring-boot.run.arguments=--umhai.migrate.enabled=true
 *
 * Safe to run more than once — instance rows are upserted by instanceUuid
 * (the primary key), so re-running will not create duplicates. History rows
 * are only inserted if not already present for a given instance+eventType+timestamp.
 */
@Component
@ConditionalOnProperty(name = "umhai.migrate.enabled", havingValue = "true")
public class UmhaiDataMigrationRunner implements CommandLineRunner {

	private static final Logger ourLog = LoggerFactory.getLogger(UmhaiDataMigrationRunner.class);
	private static final String DATA_FILE = "umhai-data.json";

	private final UmhaiInstanceRepository instanceRepository;
	private final UmhaiHistoryRepository historyRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public UmhaiDataMigrationRunner(
			UmhaiInstanceRepository instanceRepository, UmhaiHistoryRepository historyRepository) {
		this.instanceRepository = instanceRepository;
		this.historyRepository = historyRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		File file = new File(DATA_FILE);
		if (!file.exists()) {
			ourLog.warn(
					"[UMHAI MIGRATION] No {} file found at {} — nothing to migrate.",
					DATA_FILE,
					file.getAbsolutePath());
			return;
		}

		ourLog.info("[UMHAI MIGRATION] Starting import from {}", file.getAbsolutePath());

		String content = new String(Files.readAllBytes(Paths.get(DATA_FILE)));
		@SuppressWarnings("unchecked")
		Map<String, Object> data = objectMapper.readValue(content, Map.class);

		int instanceCount = migrateInstances(data);
		int historyCount = migrateHistory(data);

		ourLog.info(
				"[UMHAI MIGRATION] Complete. Imported {} instance(s) and {} history event(s).",
				instanceCount,
				historyCount);
	}

	@SuppressWarnings("unchecked")
	private int migrateInstances(Map<String, Object> data) {
		List<Map<String, Object>> instances = (List<Map<String, Object>>) data.get("instances");
		if (instances == null || instances.isEmpty()) {
			ourLog.info("[UMHAI MIGRATION] No 'instances' array found in JSON file.");
			return 0;
		}

		int count = 0;
		int skipped = 0;
		java.util.Set<String> seenHardwareAppPairs = new java.util.HashSet<>();

		for (Map<String, Object> row : instances) {
			String instanceUuid = (String) row.get("instanceUuid");
			if (instanceUuid == null) {
				ourLog.warn("[UMHAI MIGRATION] Skipping instance row with no instanceUuid: {}", row);
				skipped++;
				continue;
			}

			String hardwareSerial = (String) row.get("hardwareSerial");
			String appId = (String) row.get("appId");
			String pairKey = hardwareSerial + "|" + appId;

			// The (hardware_serial, app_id) pair is unique in the schema. Older JSON data
			// may contain multiple instanceUuids for the same pair (e.g. from a previous
			// UUID-generation scheme). Keep only the first occurrence per pair.
			if (!seenHardwareAppPairs.add(pairKey)) {
				ourLog.warn(
						"[UMHAI MIGRATION] Skipping duplicate hardwareSerial+appId pair '{}' "
								+ "(instanceUuid={}) — a row for this pair was already imported.",
						pairKey,
						instanceUuid);
				skipped++;
				continue;
			}

			UmhaiInstanceEntity entity =
					instanceRepository.findById(instanceUuid).orElse(new UmhaiInstanceEntity());

			entity.setInstanceUuid(instanceUuid);
			entity.setHardwareSerial(hardwareSerial);
			entity.setPlatform((String) row.get("platform"));
			entity.setModel((String) row.get("model"));
			entity.setAppId(appId);
			entity.setAppVersion((String) row.get("appVersion"));
			entity.setBuildNumber((String) row.get("buildNumber"));
			entity.setStatus(row.get("status") != null ? (String) row.get("status") : "Active");
			entity.setFirstLaunch((Boolean) row.get("firstLaunch"));
			entity.setRegisteredAt(toDate(row.get("registeredAt")));
			entity.setLastUpdated(toDate(row.get("lastUpdated")));
			entity.setPreviousInstanceUuid((String) row.get("previousInstanceUuid"));
			entity.setRegisteredBy((String) row.get("registeredBy"));
			entity.setDecommissionedAt(toDate(row.get("decommissionedAt")));
			entity.setDecommissionedReason((String) row.get("decommissionedReason"));

			// registeredAt is NOT NULL in the schema — fall back to now if missing from old data
			if (entity.getRegisteredAt() == null) {
				entity.setRegisteredAt(new Date());
			}

			try {
				// saveAndFlush + per-row try/catch means one bad row can't abort the whole
				// startup batch or leave the persistence context in a broken state.
				instanceRepository.saveAndFlush(entity);
				count++;
			} catch (Exception e) {
				ourLog.error(
						"[UMHAI MIGRATION] Failed to import instance {} (hardwareSerial={}, appId={}): {}",
						instanceUuid,
						hardwareSerial,
						appId,
						e.getMessage());
				skipped++;
			}
		}

		if (skipped > 0) {
			ourLog.warn(
					"[UMHAI MIGRATION] {} instance row(s) skipped — review the JSON file for "
							+ "duplicate hardwareSerial+appId pairs or bad data.",
					skipped);
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	private int migrateHistory(Map<String, Object> data) {
		List<Map<String, Object>> history = (List<Map<String, Object>>) data.get("history");
		if (history == null || history.isEmpty()) {
			ourLog.info("[UMHAI MIGRATION] No 'history' array found in JSON file.");
			return 0;
		}

		int count = 0;
		for (Map<String, Object> row : history) {
			String instanceUuid = (String) row.get("instanceUuid");
			String eventType = (String) row.get("eventType");
			Date timestamp = toDate(row.get("timestamp"));

			if (instanceUuid == null || eventType == null || timestamp == null) {
				ourLog.warn("[UMHAI MIGRATION] Skipping incomplete history row: {}", row);
				continue;
			}

			boolean alreadyExists = historyRepository.findByInstanceAndEventType(instanceUuid, eventType).stream()
					.anyMatch(existing -> existing.getTimestamp() != null
							&& existing.getTimestamp().getTime() == timestamp.getTime());

			if (alreadyExists) {
				continue;
			}

			UmhaiHistoryEntity entity = new UmhaiHistoryEntity();
			entity.setInstanceUuid(instanceUuid);
			entity.setEventType(eventType);
			entity.setTimestamp(timestamp);
			entity.setDescription((String) row.get("description"));

			try {
				historyRepository.saveAndFlush(entity);
				count++;
			} catch (Exception e) {
				ourLog.error(
						"[UMHAI MIGRATION] Failed to import history event for instance {} ({}): {}",
						instanceUuid,
						eventType,
						e.getMessage());
			}
		}
		return count;
	}

	/**
	 * The legacy saveData() wrote dates as epoch millis (Long).
	 * Handles Long, Integer, or already-ISO-string forms defensively.
	 */
	private Date toDate(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return new Date(((Number) value).longValue());
		}
		if (value instanceof String) {
			try {
				return new Date(Long.parseLong((String) value));
			} catch (NumberFormatException e) {
				ourLog.warn("[UMHAI MIGRATION] Could not parse date value '{}', skipping field.", value);
				return null;
			}
		}
		return null;
	}
}
