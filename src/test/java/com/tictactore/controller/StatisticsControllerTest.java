package com.tictactore.controller;

import com.tictactore.dto.PagedResponse;
import com.tictactore.dto.TeamPairStatsResponse;
import com.tictactore.dto.TimePeriod;
import com.tictactore.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsController Unit Tests")
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;

    @Test
    @DisplayName("Should delegate to service and return 200 OK with PagedResponse")
    void shouldDelegateToService_whenGetTeamPairsCalled() {
        UUID playerId = UUID.randomUUID();
        UUID ruleConfigId = UUID.randomUUID();
        PagedResponse<TeamPairStatsResponse> expectedResponse = new PagedResponse<>(
                List.of(), 0, 10, 0L, 0
        );

        when(statisticsService.getTeamPairStats(playerId, TimePeriod.LAST_MONTH, ruleConfigId, 0, 10, 3))
                .thenReturn(expectedResponse);

        ResponseEntity<PagedResponse<TeamPairStatsResponse>> response = statisticsController.getTeamPairStats(
                playerId, TimePeriod.LAST_MONTH, ruleConfigId, 0, 10, 3
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(statisticsService).getTeamPairStats(playerId, TimePeriod.LAST_MONTH, ruleConfigId, 0, 10, 3);
    }
}
