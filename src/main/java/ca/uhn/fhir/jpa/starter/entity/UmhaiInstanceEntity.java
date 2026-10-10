package ca.uhn.fhir.jpa.starter.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "umhai_instances")
public class UmhaiInstanceEntity {

	@Id
	@Column(name = "instance_uuid", length = 36, nullable = false)
	private String instanceUuid;

	@Column(name = "hardware_serial", length = 100, nullable = false)
	private String hardwareSerial;

	@Column(name = "platform", length = 20, nullable = false)
	private String platform;

	@Column(name = "model", length = 100)
	private String model;

	@Column(name = "app_id", length = 100, nullable = false)
	private String appId;

	@Column(name = "app_version", length = 20, nullable = false)
	private String appVersion;

	@Column(name = "build_number", length = 20)
	private String buildNumber;

	@Column(name = "status", length = 20, nullable = false)
	private String status;

	@Column(name = "first_launch")
	private Boolean firstLaunch;

	@Column(name = "registered_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date registeredAt;

	@Column(name = "last_updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdated;

	@Column(name = "previous_instance_uuid", length = 36)
	private String previousInstanceUuid;

	@Column(name = "registered_by", length = 100)
	private String registeredBy;

	@Column(name = "decommissioned_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date decommissionedAt;

	@Column(name = "decommissioned_reason", length = 255)
	private String decommissionedReason;

	// --- App identity & publisher (FHIR R6 incubator extension) ---

	@Column(name = "app_name", length = 200)
	private String appName;

	@Column(name = "publisher", length = 200)
	private String publisher;

	@Column(name = "publisher_reference", length = 200)
	private String publisherReference;

	@Column(name = "support_email", length = 254)
	private String supportEmail;

	@Column(name = "support_url", length = 500)
	private String supportUrl;

	@Column(name = "privacy_policy_url", length = 500)
	private String privacyPolicyUrl;

	// --- Classification, packaging & distribution ---

	@Column(name = "classification", length = 30)
	private String classification;

	@Column(name = "package_identifier", length = 150)
	private String packageIdentifier;

	@Column(name = "store_url", length = 500)
	private String storeUrl;

	@Column(name = "release_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date releaseDate;

	// --- Integrity ---

	@Column(name = "checksum_algorithm", length = 10)
	private String checksumAlgorithm;

	@Column(name = "checksum_value", length = 128)
	private String checksumValue;

	// --- Certifications ---

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "umhai_instance_certifications", joinColumns = @JoinColumn(name = "instance_uuid"))
	private List<UmhaiCertificationEmbeddable> certifications = new ArrayList<>();

	// Getters and Setters
	public String getInstanceUuid() {
		return instanceUuid;
	}

	public void setInstanceUuid(String instanceUuid) {
		this.instanceUuid = instanceUuid;
	}

	public String getHardwareSerial() {
		return hardwareSerial;
	}

	public void setHardwareSerial(String hardwareSerial) {
		this.hardwareSerial = hardwareSerial;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getFirstLaunch() {
		return firstLaunch;
	}

	public void setFirstLaunch(Boolean firstLaunch) {
		this.firstLaunch = firstLaunch;
	}

	public Date getRegisteredAt() {
		return registeredAt;
	}

	public void setRegisteredAt(Date registeredAt) {
		this.registeredAt = registeredAt;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getPreviousInstanceUuid() {
		return previousInstanceUuid;
	}

	public void setPreviousInstanceUuid(String previousInstanceUuid) {
		this.previousInstanceUuid = previousInstanceUuid;
	}

	public String getRegisteredBy() {
		return registeredBy;
	}

	public void setRegisteredBy(String registeredBy) {
		this.registeredBy = registeredBy;
	}

	public Date getDecommissionedAt() {
		return decommissionedAt;
	}

	public void setDecommissionedAt(Date decommissionedAt) {
		this.decommissionedAt = decommissionedAt;
	}

	public String getDecommissionedReason() {
		return decommissionedReason;
	}

	public void setDecommissionedReason(String decommissionedReason) {
		this.decommissionedReason = decommissionedReason;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublisherReference() {
		return publisherReference;
	}

	public void setPublisherReference(String publisherReference) {
		this.publisherReference = publisherReference;
	}

	public String getSupportEmail() {
		return supportEmail;
	}

	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}

	public String getSupportUrl() {
		return supportUrl;
	}

	public void setSupportUrl(String supportUrl) {
		this.supportUrl = supportUrl;
	}

	public String getPrivacyPolicyUrl() {
		return privacyPolicyUrl;
	}

	public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
		this.privacyPolicyUrl = privacyPolicyUrl;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getPackageIdentifier() {
		return packageIdentifier;
	}

	public void setPackageIdentifier(String packageIdentifier) {
		this.packageIdentifier = packageIdentifier;
	}

	public String getStoreUrl() {
		return storeUrl;
	}

	public void setStoreUrl(String storeUrl) {
		this.storeUrl = storeUrl;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getChecksumAlgorithm() {
		return checksumAlgorithm;
	}

	public void setChecksumAlgorithm(String checksumAlgorithm) {
		this.checksumAlgorithm = checksumAlgorithm;
	}

	public String getChecksumValue() {
		return checksumValue;
	}

	public void setChecksumValue(String checksumValue) {
		this.checksumValue = checksumValue;
	}

	public List<UmhaiCertificationEmbeddable> getCertifications() {
		if (certifications == null) {
			certifications = new ArrayList<>();
		}
		return certifications;
	}

	public void setCertifications(List<UmhaiCertificationEmbeddable> certifications) {
		this.certifications = certifications;
	}
}
