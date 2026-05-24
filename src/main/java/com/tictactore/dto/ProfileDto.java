package com.tictactore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "User profile information")
public class ProfileDto {

    @Schema(description = "User's unique nickname", example = "johndoe")
    private String nickname;

    @Schema(description = "URL to the user's avatar", example = "https://api.dicebear.com/7.x/identicon/svg?seed=...")
    private String avatar;

    @Schema(description = "User's preferred language", example = "en")
    private String language;
}
