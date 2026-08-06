package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.Date;

@ResourceDef(name = "UmhaiInstance", profile = "http://your-org/fhir/StructureDefinition/umhai-instance")
public class UmhaiInstance extends DomainResource implements IBaseResource {

    private static final long serialVersionUID = 1L;

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