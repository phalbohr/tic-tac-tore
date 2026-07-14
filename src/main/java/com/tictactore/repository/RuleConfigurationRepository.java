package com.tictactore.repository;

import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleConfigurationRepository extends JpaRepository<RuleConfiguration, UUID> {
    List<RuleConfiguration> findByType(RuleConfigurationType type);
}
