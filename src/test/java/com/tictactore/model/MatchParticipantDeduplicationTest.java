package com.tictactore.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Match Participant Deduplication Tests")
class MatchParticipantDeduplicationTest {

    @Test
    void shouldDeduplicateParticipants_whenDuplicatePlayerIdsPresent() {
        var playerId1 = UUID.randomUUID();
        var playerId2 = UUID.randomUUID();
        var match = Match.builder()
                .teamAAttackerId(playerId1)
                .teamADefenderId(playerId1)
                .teamBAttackerId(playerId2)
                .teamBDefenderId(playerId2)
                .build();

        var participantIds = match.getParticipantIds();

        assertThat(participantIds)
                .hasSize(2)
                .containsExactly(playerId1, playerId2);
    }

    @Test
    void shouldReturnDistinctParticipants_whenAllDistinct() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var id3 = UUID.randomUUID();
        var id4 = UUID.randomUUID();
        var match = Match.builder()
                .teamAAttackerId(id1)
                .teamADefenderId(id2)
                .teamBAttackerId(id3)
                .teamBDefenderId(id4)
                .build();

        var participantIds = match.getParticipantIds();

        assertThat(participantIds)
                .hasSize(4)
                .containsExactly(id1, id2, id3, id4);
    }
}
