package ca.uhn.fhir.jpa.starter.repository;

import ca.uhn.fhir.jpa.starter.entity.UmhaiInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UmhaiInstanceRepository extends JpaRepository<UmhaiInstanceEntity, String> {

    Optional<UmhaiInstanceEntity> findByInstanceUuid(String instanceUuid);

    List<UmhaiInstanceEntity> findByStatus(String status);

    @Query("SELECT u FROM UmhaiInstanceEntity u WHERE u.hardwareSerial = :hardwareSerial AND u.status = 'Active'")
    List<UmhaiInstanceEntity> findActiveByHardwareSerial(@Param("hardwareSerial") String hardwareSerial);

    @Query("SELECT u FROM UmhaiInstanceEntity u WHERE u.hardwareSerial = :hardwareSerial AND u.appId = :appId AND u.status = 'Active'")
    Optional<UmhaiInstanceEntity> findActiveByHardwareAndApp(@Param("hardwareSerial") String hardwareSerial, 
                                                              @Param("appId") String appId);

    @Query("SELECT u FROM UmhaiInstanceEntity u WHERE u.appId = :appId AND u.status = 'Active'")
    List<UmhaiInstanceEntity> findActiveByAppId(@Param("appId") String appId);

    @Query("SELECT COUNT(u) FROM UmhaiInstanceEntity u WHERE u.status = 'Active'")
    long countActiveInstances();

    boolean existsByInstanceUuidAndStatus(String instanceUuid, String status);
}