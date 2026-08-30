package com.tictactore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementCatalogInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeAchievements() {
        try {
            seedAchievement("a0000000-0000-0000-0000-000000000001", "FIRST_WIN", "MILESTONE",
                    "achievements.first_win.title", "achievements.first_win.description", "trophy");
            seedAchievement("a0000000-0000-0000-0000-000000000002", "MATCHES_10", "EXPERIENCE",
                    "achievements.matches_10.title", "achievements.matches_10.description", "flame");
            seedAchievement("a0000000-0000-0000-0000-000000000003", "CLEAN_SHEET", "SKILL",
                    "achievements.clean_sheet.title", "achievements.clean_sheet.description", "shield");
            seedAchievement("a0000000-0000-0000-0000-000000000004", "STRIKER_50", "OFFENSE",
                    "achievements.striker_50.title", "achievements.striker_50.description", "target");
            seedAchievement("a0000000-0000-0000-0000-000000000005", "DEFENSE_WALL", "DEFENSE",
                    "achievements.defense_wall.title", "achievements.defense_wall.description", "wall");
            log.info("Achievement system catalog presets verified/initialized");
        } catch (Exception e) {
            log.warn("Could not seed achievement catalog presets: {}", e.getMessage());
        }
    }

    private void seedAchievement(String id, String code, String category, String nameKey, String descriptionKey, String icon) {
        jdbcTemplate.update("""
            INSERT INTO achievement (id, code, category, name_key, description_key, icon, created_at)
            SELECT ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP
            WHERE NOT EXISTS (SELECT 1 FROM achievement WHERE code = ?)
        """, id, code, category, nameKey, descriptionKey, icon, code);
    }
}
