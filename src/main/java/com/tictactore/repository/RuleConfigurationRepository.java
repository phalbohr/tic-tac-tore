package com.tictactore.repository;

import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleConfigurationRepository extends JpaRepository<RuleConfiguration, UUID> {

    List<RuleConfiguration> findByType(RuleConfigurationType type);

    List<RuleConfiguration> findByTypeOrderByCreatedAtDesc(RuleConfigurationType type);

    @Query("SELECT r FROM RuleConfiguration r WHERE r.type = :type OR r.createdBy = :createdBy ORDER BY r.createdAt DESC")
    List<RuleConfiguration> findByTypeOrCreatedByOrderByCreatedAtDesc(@Param("type") RuleConfigurationType type, @Param("createdBy") UUID createdBy);

    List<RuleConfiguration> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);

    @Query("SELECT r FROM RuleConfiguration r WHERE r.id = :id AND (r.createdBy = :createdBy OR r.type = :type)")
    Optional<RuleConfiguration> findByIdAndCreatedByOrType(@Param("id") UUID id, @Param("createdBy") UUID createdBy, @Param("type") RuleConfigurationType type);

    long countByCreatedBy(UUID createdBy);

    boolean existsByCreatedByAndNameIgnoreCase(UUID createdBy, String name);
}
