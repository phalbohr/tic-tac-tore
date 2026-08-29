package com.tictactore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user profile")
public class UpdateProfileRequest {

    @Schema(description = "New nickname to set", example = "newnickname")
    @jakarta.validation.constraints.Size(max = 255, message = "Nickname must be at most 255 characters")
    private String nickname;

    @Schema(description = "New interface language (EN/DE)", example = "EN")
    @Pattern(regexp = "^(?i)(EN|DE)$", message = "Language must be EN or DE")
    private String language;

    @Schema(description = "New avatar name from preset grid", example = "ball-classic")
    @com.tictactore.validation.ValidAvatar
    private String avatar;

    @Schema(description = "Whether the user has completed the onboarding tutorial", example = "true", defaultValue = "false")
    private Boolean tutorialCompleted;

    @Schema(description = "Default player group ID to set", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    private UUID defaultGroupId;

    @Schema(description = "Default rule configuration ID to set", example = "50f4a8e2-888e-4f10-9173-67c8cbcf8f3a")
    private UUID defaultRuleConfigurationId;

    @Schema(description = "Whether to clear default player group", example = "false")
    private Boolean clearDefaultGroup;

    @Schema(description = "Whether to clear default rule configuration", example = "false")
    private Boolean clearDefaultRuleConfiguration;

    @Schema(description = "Whether to receive push notifications for newly created matchmaking pools", example = "true")
    private Boolean poolNotificationsEnabled;
}
