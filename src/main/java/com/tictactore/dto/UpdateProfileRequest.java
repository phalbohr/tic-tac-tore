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
    @Pattern(regexp = "^(ball-classic|ball-cork|player-red-1|player-red-2|player-blue-1|player-blue-2|table-classic|table-top|beer-mug|beer-bottle|trophy-gold|trophy-silver|glove-red|glove-blue|whistle-gold|foosball-rod|handle-wood|handle-rubber|score-counter|snack-pretzel|snack-pizza|jersey-red|jersey-blue|crown|)?$", message = "Invalid avatar selection")
    private String avatar;
}
