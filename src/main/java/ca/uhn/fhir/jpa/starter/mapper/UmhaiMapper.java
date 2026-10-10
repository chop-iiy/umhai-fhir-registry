package ca.uhn.fhir.jpa.starter.mapper;

import ca.uhn.fhir.jpa.starter.custom.UmhaiInstance;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.jpa.starter.entity.UmhaiCertificationEmbeddable;
import ca.uhn.fhir.jpa.starter.entity.UmhaiInstanceEntity;
import ca.uhn.fhir.jpa.starter.entity.UmhaiHistoryEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        // --- Extended (FHIR R6 incubator) elements ---
        entity.setAppName(str(instance.getAppName()));
        entity.setPublisher(str(instance.getPublisher()));
        entity.setPublisherReference(str(instance.getPublisherReference()));
        entity.setSupportEmail(str(instance.getSupportEmail()));
        entity.setSupportUrl(str(instance.getSupportUrl()));
        entity.setPrivacyPolicyUrl(str(instance.getPrivacyPolicyUrl()));
        entity.setClassification(str(instance.getClassification()));
        entity.setPackageIdentifier(str(instance.getPackageIdentifier()));
        entity.setStoreUrl(str(instance.getStoreUrl()));
        entity.setReleaseDate(instance.getReleaseDate() != null ? instance.getReleaseDate().getValue() : null);
        entity.setChecksumAlgorithm(str(instance.getChecksumAlgorithm()));
        entity.setChecksumValue(str(instance.getChecksumValue()));

        List<UmhaiCertificationEmbeddable> certs = new ArrayList<>();
        if (instance.hasCertification()) {
            for (UmhaiInstance.CertificationComponent c : instance.getCertification()) {
                certs.add(new UmhaiCertificationEmbeddable(
                    str(c.getType()),
                    str(c.getStatus()),
                    c.getIssued() != null ? c.getIssued().getValue() : null,
                    c.getExpires() != null ? c.getExpires().getValue() : null,
                    str(c.getValidator())));
            }
        }
        entity.setCertifications(certs);

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

        // --- Extended (FHIR R6 incubator) elements ---
        if (entity.getAppName() != null) instance.setAppName(entity.getAppName());
        if (entity.getPublisher() != null) instance.setPublisher(entity.getPublisher());
        if (entity.getPublisherReference() != null) instance.setPublisherReference(entity.getPublisherReference());
        if (entity.getSupportEmail() != null) instance.setSupportEmail(entity.getSupportEmail());
        if (entity.getSupportUrl() != null) instance.setSupportUrl(entity.getSupportUrl());
        if (entity.getPrivacyPolicyUrl() != null) instance.setPrivacyPolicyUrl(entity.getPrivacyPolicyUrl());
        if (entity.getClassification() != null) instance.setClassification(entity.getClassification());
        if (entity.getPackageIdentifier() != null) instance.setPackageIdentifier(entity.getPackageIdentifier());
        if (entity.getStoreUrl() != null) instance.setStoreUrl(entity.getStoreUrl());
        if (entity.getReleaseDate() != null) instance.setReleaseDate(entity.getReleaseDate());
        if (entity.getChecksumAlgorithm() != null) instance.setChecksumAlgorithm(entity.getChecksumAlgorithm());
        if (entity.getChecksumValue() != null) instance.setChecksumValue(entity.getChecksumValue());

        for (UmhaiCertificationEmbeddable c : entity.getCertifications()) {
            UmhaiInstance.CertificationComponent component = instance.addCertification();
            component.setType(c.getType());
            component.setStatus(c.getStatus());
            if (c.getIssued() != null) component.setIssued(c.getIssued());
            if (c.getExpires() != null) component.setExpires(c.getExpires());
            if (c.getValidator() != null) component.setValidator(c.getValidator());
        }

        return instance;
    }

    private static String str(StringDt value) {
        return value != null ? value.getValue() : null;
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