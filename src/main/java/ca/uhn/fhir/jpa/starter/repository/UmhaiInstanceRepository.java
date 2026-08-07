package ca.uhn.fhir.jpa.starter.repository;

import ca.uhn.fhir.jpa.starter.entity.UmhaiInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UmhaiInstanceRepository extends JpaRepository<UmhaiInstanceEntity, String> {

    List<UmhaiInstanceEntity> findByStatus(String status);

    Optional<UmhaiInstanceEntity> findByHardwareSerialAndStatus(String hardwareSerial, String status);

    // Finds the current active instance for a given hardware+app pair, regardless of
    // app version/build number — used to detect whether a registration represents the
    // same running instance (no change), or a version/build change that should issue
    // a new UMHAI instance ID.
    Optional<UmhaiInstanceEntity> findByHardwareSerialAndAppIdAndStatus(
        String hardwareSerial, String appId, String status);

    long countByStatus(String status);
}
