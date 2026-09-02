package com.tictactore.service.tournament;

import com.tictactore.event.TournamentStubPartnerAssignedEvent;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.service.tournament.impl.TournamentAccountDeletionHandlerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentAccountDeletionHandler ATDD Tests (Story 8.4)")
class TournamentAccountDeletionHandlerTest {

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    @Mock
    private StubPartnerSelector stubPartnerSelector;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TournamentAccountDeletionHandler handler;
    private Tournament tournament;
    private User deletedUser;
    private TournamentRegistration deletedReg;
    private TournamentRegistration stubReg;

    @BeforeEach
    void setUp() {
        handler = new TournamentAccountDeletionHandlerImpl(
                tournamentMatchRepository,
                tournamentRegistrationRepository,
                stubPartnerSelector,
                eventPublisher
        );

        tournament = Tournament.builder()
                .id(UUID.randomUUID())
                .name("Summer 2v2 Random Cup")
                .mode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                .format(TournamentFormat.CHAMPIONSHIP)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        deletedUser = User.builder().id(UUID.randomUUID()).nickname("Quitter").build();
        deletedReg = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .user(deletedUser)
                .strengthScore(BigDecimal.valueOf(1400))
                .build();

        stubReg = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .user(User.builder().id(UUID.randomUUID()).nickname("Substitute").build())
                .strengthScore(BigDecimal.valueOf(1410))
                .build();
    }

    @Test
    void shouldReplaceDeletedParticipantWithStubAndPublishEvent() {
        TournamentRegistration partner1 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).build();
        TournamentRegistration opponent1 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).build();
        TournamentRegistration opponent2 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).build();

        TournamentMatch match = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(deletedReg)
                .participant1Partner(partner1)
                .participant2(opponent1)
                .participant2Partner(opponent2)
                .status(TournamentMatchStatus.READY)
                .round(1)
                .build();

        when(tournamentRegistrationRepository.findByUserIdAndActiveTournament(deletedUser.getId()))
                .thenReturn(List.of(deletedReg));
        when(tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), deletedReg.getId()))
                .thenReturn(List.of(match));
        when(tournamentRegistrationRepository.findAllActiveInTournament(tournament.getId()))
                .thenReturn(List.of(deletedReg, partner1, opponent1, opponent2, stubReg));
        when(stubPartnerSelector.selectStubPartner(eq(tournament), eq(deletedReg), eq(partner1.getId()), any()))
                .thenReturn(stubReg);

        handler.handleUserDeletion(deletedUser.getId());

        assertThat(match.getParticipant1()).isEqualTo(stubReg);
        assertThat(match.isParticipant1Stub()).isTrue();
        verify(tournamentMatchRepository).save(match);

        ArgumentCaptor<TournamentStubPartnerAssignedEvent> captor = ArgumentCaptor.forClass(TournamentStubPartnerAssignedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TournamentStubPartnerAssignedEvent published = captor.getValue();
        assertThat(published.tournamentId()).isEqualTo(tournament.getId());
        assertThat(published.matchId()).isEqualTo(match.getId());
        assertThat(published.deletedUserId()).isEqualTo(deletedUser.getId());
        assertThat(published.teammateUserId()).isEqualTo(partner1.getUser().getId());
        assertThat(published.stubPartnerUserId()).isEqualTo(stubReg.getUser().getId());
    }

    @Test
    void shouldReplaceDeletedPartner2WithStubAndSetFlag() {
        TournamentRegistration team1Player = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).build();
        TournamentRegistration team1Partner = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).build();
        TournamentRegistration team2Player = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).build();

        TournamentMatch match = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(team1Player)
                .participant1Partner(team1Partner)
                .participant2(team2Player)
                .participant2Partner(deletedReg)
                .status(TournamentMatchStatus.PENDING)
                .round(2)
                .build();

        when(tournamentRegistrationRepository.findByUserIdAndActiveTournament(deletedUser.getId()))
                .thenReturn(List.of(deletedReg));
        when(tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), deletedReg.getId()))
                .thenReturn(List.of(match));
        when(tournamentRegistrationRepository.findAllActiveInTournament(tournament.getId()))
                .thenReturn(List.of(deletedReg, team1Player, team1Partner, team2Player, stubReg));
        when(stubPartnerSelector.selectStubPartner(eq(tournament), eq(deletedReg), eq(team2Player.getId()), any()))
                .thenReturn(stubReg);

        handler.handleUserDeletion(deletedUser.getId());

        assertThat(match.getParticipant2Partner()).isEqualTo(stubReg);
        assertThat(match.isParticipant2Stub()).isTrue();
        verify(tournamentMatchRepository).save(match);
    }
}
