package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.model.api.annotation.Block;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BackboneElement;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ResourceDef(name = "UmhaiInstance", profile = "http://your-org/fhir/StructureDefinition/umhai-instance")
public class UmhaiInstance extends DomainResource implements IBaseResource {

    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------
    // Instance identity, hardware and lifecycle (original elements)
    // ------------------------------------------------------------------

    @Child(name = "instanceUuid", order = 0, min = 1, max = 1)
    @Description(shortDefinition = "UUID v5 of the application instance")
    private StringDt instanceUuid;

    @Child(name = "hardwareSerial", order = 1, min = 1, max = 1)
    @Description(shortDefinition = "Hardware serial number")
    private StringDt hardwareSerial;

    @Child(name = "platform", order = 2, min = 1, max = 1)
    @Description(shortDefinition = "Platform/OS")
    private StringDt platform;

    @Child(name = "model", order = 3, min = 0, max = 1)
    @Description(shortDefinition = "Device model")
    private StringDt model;

    @Child(name = "appId", order = 4, min = 1, max = 1)
    @Description(shortDefinition = "Application identifier")
    private StringDt appId;

    @Child(name = "appVersion", order = 5, min = 1, max = 1)
    @Description(shortDefinition = "Application version")
    private StringDt appVersion;

    @Child(name = "buildNumber", order = 6, min = 0, max = 1)
    @Description(shortDefinition = "Build number")
    private StringDt buildNumber;

    @Child(name = "status", order = 7, min = 1, max = 1)
    @Description(shortDefinition = "Instance status")
    private StringDt status;

    @Child(name = "firstLaunch", order = 8, min = 0, max = 1)
    @Description(shortDefinition = "Indicates if this is the first launch")
    private BooleanDt firstLaunch;

    @Child(name = "registeredAt", order = 9, min = 1, max = 1)
    @Description(shortDefinition = "Registration timestamp")
    private DateTimeDt registeredAt;

    @Child(name = "lastUpdated", order = 10, min = 0, max = 1)
    @Description(shortDefinition = "Last update timestamp")
    private DateTimeDt lastUpdated;

    @Child(name = "previousInstanceUuid", order = 11, min = 0, max = 1)
    @Description(shortDefinition = "Previous UUID in case of hardware transfer")
    private StringDt previousInstanceUuid;

    @Child(name = "registeredBy", order = 12, min = 0, max = 1)
    @Description(shortDefinition = "User or system that registered this instance")
    private StringDt registeredBy;

    @Child(name = "decommissionedAt", order = 13, min = 0, max = 1)
    @Description(shortDefinition = "Timestamp when the instance was decommissioned")
    private DateTimeDt decommissionedAt;

    // ------------------------------------------------------------------
    // App identity & publisher (FHIR R6 incubator extension)
    // ------------------------------------------------------------------

    @Child(name = "appName", order = 14, min = 0, max = 1)
    @Description(shortDefinition = "Human-readable name of the mobile health application")
    private StringDt appName;

    @Child(name = "publisher", order = 15, min = 0, max = 1)
    @Description(shortDefinition = "Name of the organization that develops or publishes the application")
    private StringDt publisher;

    @Child(name = "publisherReference", order = 16, min = 0, max = 1)
    @Description(shortDefinition = "Reference to the publishing Organization resource (e.g. Organization/123)")
    private StringDt publisherReference;

    @Child(name = "supportEmail", order = 17, min = 0, max = 1)
    @Description(shortDefinition = "Developer or help-desk contact e-mail address")
    private StringDt supportEmail;

    @Child(name = "supportUrl", order = 18, min = 0, max = 1)
    @Description(shortDefinition = "Developer portal or support URL")
    private StringDt supportUrl;

    @Child(name = "privacyPolicyUrl", order = 19, min = 0, max = 1)
    @Description(shortDefinition = "URL of the application's privacy policy")
    private StringDt privacyPolicyUrl;

    // ------------------------------------------------------------------
    // Classification, platform packaging & distribution
    // ------------------------------------------------------------------

    @Child(name = "classification", order = 20, min = 0, max = 1)
    @Description(shortDefinition = "Software category: wellness | samd | digital-therapeutic | ehr-companion | other")
    private StringDt classification;

    @Child(name = "packageIdentifier", order = 21, min = 0, max = 1)
    @Description(shortDefinition = "Platform bundle identifier or package name (e.g. com.organization.app)")
    private StringDt packageIdentifier;

    @Child(name = "storeUrl", order = 22, min = 0, max = 1)
    @Description(shortDefinition = "Official app store or distribution listing URL")
    private StringDt storeUrl;

    @Child(name = "releaseDate", order = 23, min = 0, max = 1)
    @Description(shortDefinition = "Date this application version was released")
    private DateTimeDt releaseDate;

    // ------------------------------------------------------------------
    // Integrity (cryptographic checksum of the application binary)
    // ------------------------------------------------------------------

    @Child(name = "checksumAlgorithm", order = 24, min = 0, max = 1)
    @Description(shortDefinition = "Hash algorithm used for the checksum: sha-256 | sha-384 | sha-512")
    private StringDt checksumAlgorithm;

    @Child(name = "checksumValue", order = 25, min = 0, max = 1)
    @Description(shortDefinition = "Hex-encoded cryptographic hash of the application binary")
    private StringDt checksumValue;

    // ------------------------------------------------------------------
    // Regulatory, security and clinical certifications
    // ------------------------------------------------------------------

    @Child(name = "certification", type = {}, order = 26, min = 0, max = Child.MAX_UNLIMITED)
    @Description(shortDefinition = "Regulatory, security or clinical certifications held by the application")
    private List<CertificationComponent> certification;

    @Block
    public static class CertificationComponent extends BackboneElement {

        private static final long serialVersionUID = 1L;

        @Child(name = "type", order = 1, min = 1, max = 1)
        @Description(shortDefinition = "Certification or standard (e.g. ONC, ISO-27001, HIPAA, FDA, ORCHA)")
        private StringDt type;

        @Child(name = "status", order = 2, min = 1, max = 1)
        @Description(shortDefinition = "active | expired | revoked")
        private StringDt status;

        @Child(name = "issued", order = 3, min = 0, max = 1)
        @Description(shortDefinition = "Date the certification was granted")
        private DateTimeDt issued;

        @Child(name = "expires", order = 4, min = 0, max = 1)
        @Description(shortDefinition = "Date the certification expires")
        private DateTimeDt expires;

        @Child(name = "validator", order = 5, min = 0, max = 1)
        @Description(shortDefinition = "Certifying agency or auditing body")
        private StringDt validator;

        public StringDt getType() { return type; }
        public void setType(StringDt type) { this.type = type; }
        public void setType(String type) { this.type = new StringDt(type); }

        public StringDt getStatus() { return status; }
        public void setStatus(StringDt status) { this.status = status; }
        public void setStatus(String status) { this.status = new StringDt(status); }

        public DateTimeDt getIssued() { return issued; }
        public void setIssued(DateTimeDt issued) { this.issued = issued; }
        public void setIssued(Date issued) { this.issued = new DateTimeDt(issued); }

        public DateTimeDt getExpires() { return expires; }
        public void setExpires(DateTimeDt expires) { this.expires = expires; }
        public void setExpires(Date expires) { this.expires = new DateTimeDt(expires); }

        public StringDt getValidator() { return validator; }
        public void setValidator(StringDt validator) { this.validator = validator; }
        public void setValidator(String validator) { this.validator = new StringDt(validator); }

        @Override
        public CertificationComponent copy() {
            CertificationComponent copy = new CertificationComponent();
            copy.type = this.type;
            copy.status = this.status;
            copy.issued = this.issued;
            copy.expires = this.expires;
            copy.validator = this.validator;
            return copy;
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(type, status, issued, expires, validator);
        }

        @Override
        public String fhirType() {
            return "UmhaiInstance.certification";
        }
    }

    // Getters and Setters
    public StringDt getInstanceUuid() { return instanceUuid; }
    public void setInstanceUuid(StringDt instanceUuid) { this.instanceUuid = instanceUuid; }
    public void setInstanceUuid(String instanceUuid) { this.instanceUuid = new StringDt(instanceUuid); }

    public StringDt getHardwareSerial() { return hardwareSerial; }
    public void setHardwareSerial(StringDt hardwareSerial) { this.hardwareSerial = hardwareSerial; }
    public void setHardwareSerial(String hardwareSerial) { this.hardwareSerial = new StringDt(hardwareSerial); }

    public StringDt getPlatform() { return platform; }
    public void setPlatform(StringDt platform) { this.platform = platform; }
    public void setPlatform(String platform) { this.platform = new StringDt(platform); }

    public StringDt getModel() { return model; }
    public void setModel(StringDt model) { this.model = model; }
    public void setModel(String model) { this.model = new StringDt(model); }

    public StringDt getAppId() { return appId; }
    public void setAppId(StringDt appId) { this.appId = appId; }
    public void setAppId(String appId) { this.appId = new StringDt(appId); }

    public StringDt getAppVersion() { return appVersion; }
    public void setAppVersion(StringDt appVersion) { this.appVersion = appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = new StringDt(appVersion); }

    public StringDt getBuildNumber() { return buildNumber; }
    public void setBuildNumber(StringDt buildNumber) { this.buildNumber = buildNumber; }
    public void setBuildNumber(String buildNumber) { this.buildNumber = new StringDt(buildNumber); }

    public StringDt getStatus() { return status; }
    public void setStatus(StringDt status) { this.status = status; }
    public void setStatus(String status) { this.status = new StringDt(status); }

    public BooleanDt getFirstLaunch() { return firstLaunch; }
    public void setFirstLaunch(BooleanDt firstLaunch) { this.firstLaunch = firstLaunch; }
    public void setFirstLaunch(Boolean firstLaunch) { this.firstLaunch = new BooleanDt(firstLaunch); }

    public DateTimeDt getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(DateTimeDt registeredAt) { this.registeredAt = registeredAt; }
    public void setRegisteredAt(Date registeredAt) { this.registeredAt = new DateTimeDt(registeredAt); }

    public DateTimeDt getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(DateTimeDt lastUpdated) { this.lastUpdated = lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = new DateTimeDt(lastUpdated); }

    public StringDt getPreviousInstanceUuid() { return previousInstanceUuid; }
    public void setPreviousInstanceUuid(StringDt previousInstanceUuid) { this.previousInstanceUuid = previousInstanceUuid; }
    public void setPreviousInstanceUuid(String previousInstanceUuid) { this.previousInstanceUuid = new StringDt(previousInstanceUuid); }

    public StringDt getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(StringDt registeredBy) { this.registeredBy = registeredBy; }
    public void setRegisteredBy(String registeredBy) { this.registeredBy = new StringDt(registeredBy); }

    public DateTimeDt getDecommissionedAt() { return decommissionedAt; }
    public void setDecommissionedAt(DateTimeDt decommissionedAt) { this.decommissionedAt = decommissionedAt; }
    public void setDecommissionedAt(Date decommissionedAt) { this.decommissionedAt = new DateTimeDt(decommissionedAt); }

    public StringDt getAppName() { return appName; }
    public void setAppName(StringDt appName) { this.appName = appName; }
    public void setAppName(String appName) { this.appName = new StringDt(appName); }

    public StringDt getPublisher() { return publisher; }
    public void setPublisher(StringDt publisher) { this.publisher = publisher; }
    public void setPublisher(String publisher) { this.publisher = new StringDt(publisher); }

    public StringDt getPublisherReference() { return publisherReference; }
    public void setPublisherReference(StringDt publisherReference) { this.publisherReference = publisherReference; }
    public void setPublisherReference(String publisherReference) { this.publisherReference = new StringDt(publisherReference); }

    public StringDt getSupportEmail() { return supportEmail; }
    public void setSupportEmail(StringDt supportEmail) { this.supportEmail = supportEmail; }
    public void setSupportEmail(String supportEmail) { this.supportEmail = new StringDt(supportEmail); }

    public StringDt getSupportUrl() { return supportUrl; }
    public void setSupportUrl(StringDt supportUrl) { this.supportUrl = supportUrl; }
    public void setSupportUrl(String supportUrl) { this.supportUrl = new StringDt(supportUrl); }

    public StringDt getPrivacyPolicyUrl() { return privacyPolicyUrl; }
    public void setPrivacyPolicyUrl(StringDt privacyPolicyUrl) { this.privacyPolicyUrl = privacyPolicyUrl; }
    public void setPrivacyPolicyUrl(String privacyPolicyUrl) { this.privacyPolicyUrl = new StringDt(privacyPolicyUrl); }

    public StringDt getClassification() { return classification; }
    public void setClassification(StringDt classification) { this.classification = classification; }
    public void setClassification(String classification) { this.classification = new StringDt(classification); }

    public StringDt getPackageIdentifier() { return packageIdentifier; }
    public void setPackageIdentifier(StringDt packageIdentifier) { this.packageIdentifier = packageIdentifier; }
    public void setPackageIdentifier(String packageIdentifier) { this.packageIdentifier = new StringDt(packageIdentifier); }

    public StringDt getStoreUrl() { return storeUrl; }
    public void setStoreUrl(StringDt storeUrl) { this.storeUrl = storeUrl; }
    public void setStoreUrl(String storeUrl) { this.storeUrl = new StringDt(storeUrl); }

    public DateTimeDt getReleaseDate() { return releaseDate; }
    public void setReleaseDate(DateTimeDt releaseDate) { this.releaseDate = releaseDate; }
    public void setReleaseDate(Date releaseDate) { this.releaseDate = new DateTimeDt(releaseDate); }

    public StringDt getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(StringDt checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = new StringDt(checksumAlgorithm); }

    public StringDt getChecksumValue() { return checksumValue; }
    public void setChecksumValue(StringDt checksumValue) { this.checksumValue = checksumValue; }
    public void setChecksumValue(String checksumValue) { this.checksumValue = new StringDt(checksumValue); }

    public List<CertificationComponent> getCertification() {
        if (certification == null) {
            certification = new ArrayList<>();
        }
        return certification;
    }

    public void setCertification(List<CertificationComponent> certification) {
        this.certification = certification;
    }

    public boolean hasCertification() {
        return certification != null && !certification.isEmpty();
    }

    public CertificationComponent addCertification() {
        CertificationComponent component = new CertificationComponent();
        getCertification().add(component);
        return component;
    }

    public boolean isActive() {
        return "Active".equals(status.getValue());
    }

    @Override
    public UmhaiInstance copy() {
        UmhaiInstance copy = new UmhaiInstance();
        copy.instanceUuid = this.instanceUuid;
        copy.hardwareSerial = this.hardwareSerial;
        copy.platform = this.platform;
        copy.model = this.model;
        copy.appId = this.appId;
        copy.appVersion = this.appVersion;
        copy.buildNumber = this.buildNumber;
        copy.status = this.status;
        copy.firstLaunch = this.firstLaunch;
        copy.registeredAt = this.registeredAt;
        copy.lastUpdated = this.lastUpdated;
        copy.previousInstanceUuid = this.previousInstanceUuid;
        copy.registeredBy = this.registeredBy;
        copy.decommissionedAt = this.decommissionedAt;
        copy.appName = this.appName;
        copy.publisher = this.publisher;
        copy.publisherReference = this.publisherReference;
        copy.supportEmail = this.supportEmail;
        copy.supportUrl = this.supportUrl;
        copy.privacyPolicyUrl = this.privacyPolicyUrl;
        copy.classification = this.classification;
        copy.packageIdentifier = this.packageIdentifier;
        copy.storeUrl = this.storeUrl;
        copy.releaseDate = this.releaseDate;
        copy.checksumAlgorithm = this.checksumAlgorithm;
        copy.checksumValue = this.checksumValue;
        if (this.certification != null) {
            copy.certification = new ArrayList<>();
            for (CertificationComponent component : this.certification) {
                copy.certification.add(component.copy());
            }
        }
        return copy;
    }

    @Override
    public ResourceType getResourceType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return ElementUtil.isEmpty(instanceUuid, hardwareSerial, platform, appId, appVersion);
    }

    @Override
    public String toString() {
        return "UmhaiInstance{" +
                "instanceUuid=" + instanceUuid +
                ", hardwareSerial=" + hardwareSerial +
                ", platform=" + platform +
                ", appId=" + appId +
                ", status=" + status +
                '}';
    }
}
