package com.tictactore.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record PlayerSummaryDto(
        UUID id,
        String nickname,
        @JsonProperty("avatar")
        @JsonAlias({"avatarUrl", "avatar"})
        String avatar
) {
    public String avatarUrl() {
        return avatar;
    }
}
