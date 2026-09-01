package com.tictactore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MyRegistrationStatusResponse(
        @JsonProperty("registered") boolean registered,
        TournamentRegistrationResponse registration,
        @JsonProperty("isPendingInvite") boolean isPendingInvite
) {
    public boolean isRegistered() {
        return registered;
    }
}
