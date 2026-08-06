package ca.uhn.fhir.jpa.starter.repository;

import ca.uhn.fhir.jpa.starter.entity.UmhaiHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UmhaiHistoryRepository extends JpaRepository<UmhaiHistoryEntity, Long> {

    List<UmhaiHistoryEntity> findByInstanceUuidOrderByTimestampDesc(String instanceUuid);

    @Query("SELECT h FROM UmhaiHistoryEntity h WHERE h.instanceUuid = :instanceUuid AND h.eventType = :eventType")
    List<UmhaiHistoryEntity> findByInstanceAndEventType(@Param("instanceUuid") String instanceUuid,
                                                         @Param("eventType") String eventType);

    @Query("SELECT COUNT(h) FROM UmhaiHistoryEntity h WHERE h.instanceUuid = :instanceUuid")
    long countEventsForInstance(@Param("instanceUuid") String instanceUuid);

    void deleteByInstanceUuid(String instanceUuid);
}