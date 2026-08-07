package ca.uhn.fhir.jpa.starter.entity;

import jakarta.persistence.*;
import java.util.Date;

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

    // Getters and Setters
    public String getInstanceUuid() { return instanceUuid; }
    public void setInstanceUuid(String instanceUuid) { this.instanceUuid = instanceUuid; }

    public String getHardwareSerial() { return hardwareSerial; }
    public void setHardwareSerial(String hardwareSerial) { this.hardwareSerial = hardwareSerial; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public String getBuildNumber() { return buildNumber; }
    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getFirstLaunch() { return firstLaunch; }
    public void setFirstLaunch(Boolean firstLaunch) { this.firstLaunch = firstLaunch; }

    public Date getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Date registeredAt) { this.registeredAt = registeredAt; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getPreviousInstanceUuid() { return previousInstanceUuid; }
    public void setPreviousInstanceUuid(String previousInstanceUuid) { this.previousInstanceUuid = previousInstanceUuid; }

    public String getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(String registeredBy) { this.registeredBy = registeredBy; }

    public Date getDecommissionedAt() { return decommissionedAt; }
    public void setDecommissionedAt(Date decommissionedAt) { this.decommissionedAt = decommissionedAt; }

    public String getDecommissionedReason() { return decommissionedReason; }
    public void setDecommissionedReason(String decommissionedReason) { this.decommissionedReason = decommissionedReason; }
}