package ca.uhn.fhir.jpa.starter.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "umhai_history")
public class UmhaiHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Long eventId;

	@Column(name = "instance_uuid", length = 36, nullable = false)
	private String instanceUuid;

	@Column(name = "event_type", length = 50, nullable = false)
	private String eventType;

	@Column(name = "timestamp", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Column(name = "description", length = 500)
	private String description;

	@Column(name = "event_data", columnDefinition = "CLOB")
	private String eventData;

	// Getters and Setters
	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getInstanceUuid() {
		return instanceUuid;
	}

	public void setInstanceUuid(String instanceUuid) {
		this.instanceUuid = instanceUuid;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEventData() {
		return eventData;
	}

	public void setEventData(String eventData) {
		this.eventData = eventData;
	}
}
