package com.tictactore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
