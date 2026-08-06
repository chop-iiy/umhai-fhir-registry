package ca.uhn.fhir.jpa.starter.mapper;

import ca.uhn.fhir.jpa.starter.custom.UmhaiInstance;
import ca.uhn.fhir.jpa.starter.entity.UmhaiInstanceEntity;
import ca.uhn.fhir.jpa.starter.entity.UmhaiHistoryEntity;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UmhaiMapper {

    public UmhaiInstanceEntity toEntity(UmhaiInstance instance) {
        if (instance == null) return null;

        UmhaiInstanceEntity entity = new UmhaiInstanceEntity();
        entity.setInstanceUuid(instance.getInstanceUuid().getValue());
        entity.setHardwareSerial(instance.getHardwareSerial().getValue());
        entity.setPlatform(instance.getPlatform().getValue());
        entity.setModel(instance.getModel() != null ? instance.getModel().getValue() : null);
        entity.setAppId(instance.getAppId().getValue());
        entity.setAppVersion(instance.getAppVersion().getValue());
        entity.setBuildNumber(instance.getBuildNumber() != null ? instance.getBuildNumber().getValue() : null);
        entity.setStatus(instance.getStatus().getValue());
        entity.setFirstLaunch(instance.getFirstLaunch() != null ? instance.getFirstLaunch().getValue() : null);
        entity.setRegisteredAt(instance.getRegisteredAt().getValue());
        entity.setLastUpdated(instance.getLastUpdated() != null ? instance.getLastUpdated().getValue() : null);
        entity.setPreviousInstanceUuid(instance.getPreviousInstanceUuid() != null ? 
            instance.getPreviousInstanceUuid().getValue() : null);
        entity.setRegisteredBy(instance.getRegisteredBy() != null ? 
            instance.getRegisteredBy().getValue() : null);
        entity.setDecommissionedAt(instance.getDecommissionedAt() != null ? 
            instance.getDecommissionedAt().getValue() : null);
        
        return entity;
    }

    public UmhaiInstance toModel(UmhaiInstanceEntity entity) {
        if (entity == null) return null;

        UmhaiInstance instance = new UmhaiInstance();
        instance.setInstanceUuid(entity.getInstanceUuid());
        instance.setHardwareSerial(entity.getHardwareSerial());
        instance.setPlatform(entity.getPlatform());
        if (entity.getModel() != null) {
            instance.setModel(entity.getModel());
        }
        instance.setAppId(entity.getAppId());
        instance.setAppVersion(entity.getAppVersion());
        if (entity.getBuildNumber() != null) {
            instance.setBuildNumber(entity.getBuildNumber());
        }
        instance.setStatus(entity.getStatus());
        if (entity.getFirstLaunch() != null) {
            instance.setFirstLaunch(entity.getFirstLaunch());
        }
        instance.setRegisteredAt(entity.getRegisteredAt());
        instance.setLastUpdated(entity.getLastUpdated());
        if (entity.getPreviousInstanceUuid() != null) {
            instance.setPreviousInstanceUuid(entity.getPreviousInstanceUuid());
        }
        if (entity.getRegisteredBy() != null) {
            instance.setRegisteredBy(entity.getRegisteredBy());
        }
        if (entity.getDecommissionedAt() != null) {
            instance.setDecommissionedAt(entity.getDecommissionedAt());
        }
        
        return instance;
    }

    public UmhaiHistoryEntity toHistoryEntity(String instanceUuid, String eventType, String description) {
        UmhaiHistoryEntity entity = new UmhaiHistoryEntity();
        entity.setInstanceUuid(instanceUuid);
        entity.setEventType(eventType);
        entity.setTimestamp(new Date());
        entity.setDescription(description);
        return entity;
    }
}