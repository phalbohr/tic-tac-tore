package com.tictactore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User profile information")
public class ProfileDto {

    @Schema(description = "User's unique nickname", example = "johndoe")
    private String nickname;

    @Schema(description = "URL to the user's avatar", example = "https://api.dicebear.com/7.x/identicon/svg?seed=...")
    private String avatar;

    @Schema(description = "User's preferred language", example = "en")
    private String language;

    @Schema(description = "Whether the user has completed the onboarding tutorial", example = "true")
    private Boolean tutorialCompleted;

    @Schema(description = "User's default player group ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    private UUID defaultGroupId;

    @Schema(description = "User's default rule template configuration ID", example = "50f4a8e2-888e-4f10-9173-67c8cbcf8f3a")
    private UUID defaultRuleConfigurationId;

    @Schema(description = "Version for optimistic locking", example = "1")
    private Long version;
}
