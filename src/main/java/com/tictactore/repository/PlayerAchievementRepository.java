package com.tictactore.repository;

import com.tictactore.model.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement, UUID> {

    @Query("SELECT pa FROM PlayerAchievement pa JOIN FETCH pa.achievement WHERE pa.user.id = :userId ORDER BY pa.unlockedAt DESC")
    List<PlayerAchievement> findByUserIdOrderByUnlockedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(pa) > 0 THEN TRUE ELSE FALSE END FROM PlayerAchievement pa WHERE pa.user.id = :userId AND pa.achievement.id = :achievementId")
    boolean existsByUserIdAndAchievementId(@Param("userId") UUID userId, @Param("achievementId") UUID achievementId);

    @Query("SELECT pa FROM PlayerAchievement pa WHERE pa.user.id = :userId AND pa.achievement.id = :achievementId")
    Optional<PlayerAchievement> findByUserIdAndAchievementId(@Param("userId") UUID userId, @Param("achievementId") UUID achievementId);
}
