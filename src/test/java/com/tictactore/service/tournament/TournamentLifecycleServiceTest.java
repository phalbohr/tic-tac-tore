package com.tictactore.service.tournament;

import com.tictactore.dto.TournamentResponse;
import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.event.TournamentCancelledEvent;
import com.tictactore.event.TournamentStartedEvent;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.TournamentConflictException;
import com.tictactore.model.RegistrationStatus;
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
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.impl.TournamentLifecycleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentLifecycleService Tests")
class TournamentLifecycleServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentRegistrationRepository registrationRepository;

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private TournamentSeedingStrategy seedingStrategy;

    @Mock
    private BracketGenerator cupBracketGenerator;

    @Mock
    private BracketGenerator championshipBracketGenerator;

    @Mock
    private BracketGenerator randomPairingBracketGenerator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TournamentLifecycleServiceImpl lifecycleService;

    private UUID tournamentId;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        lifecycleService = new TournamentLifecycleServiceImpl(
                tournamentRepository,
                registrationRepository,
                tournamentMatchRepository,
                seedingStrategy,
                cupBracketGenerator,
                championshipBracketGenerator,
                randomPairingBracketGenerator,
                eventPublisher
        );

        tournamentId = UUID.randomUUID();
        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Autumn Open")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .status(TournamentStatus.REGISTRATION_OPEN)
                .minParticipants(4)
                .maxParticipants(8)
                .registrationDeadline(Instant.now().minusSeconds(60))
                .build();
    }

    @Test
    void shouldThrowNotFoundWhenTournamentDoesNotExist() {
        when(tournamentRepository.findByIdWithLock(tournamentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lifecycleService.startTournament(tournamentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowConflictWhenTournamentNotOpenForRegistration() {
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        when(tournamentRepository.findByIdWithLock(tournamentId)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> lifecycleService.startTournament(tournamentId))
                .isInstanceOf(TournamentConflictException.class);
    }

    @Test
    void shouldCancelTournamentWhenConfirmedRegistrationsBelowMinimum() {
        when(tournamentRepository.findByIdWithLock(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(i -> i.getArgument(0));

        User p1 = User.builder().id(UUID.randomUUID()).nickname("Player1").build();
        TournamentRegistration reg1 = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(p1)
                .tournament(tournament)
                .status(RegistrationStatus.CONFIRMED)
                .build();

        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(List.of(reg1));

        TournamentResponse response = lifecycleService.startTournament(tournamentId);

        assertThat(response.status()).isEqualTo(TournamentStatus.CANCELLED);

        var captor = ArgumentCaptor.forClass(TournamentCancelledEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().tournamentId()).isEqualTo(tournamentId);
        assertThat(captor.getValue().participantUserIds()).containsExactly(p1.getId());
    }

    @Test
    void shouldStartTournamentAndGenerateMatchesWhenCapacityMet() {
        when(tournamentRepository.findByIdWithLock(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(i -> i.getArgument(0));

        User p1 = User.builder().id(UUID.randomUUID()).nickname("Player1").build();
        User p2 = User.builder().id(UUID.randomUUID()).nickname("Player2").build();
        User p3 = User.builder().id(UUID.randomUUID()).nickname("Player3").build();
        User p4 = User.builder().id(UUID.randomUUID()).nickname("Player4").build();

        TournamentRegistration reg1 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p1).status(RegistrationStatus.CONFIRMED).build();
        TournamentRegistration reg2 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p2).status(RegistrationStatus.CONFIRMED).build();
        TournamentRegistration reg3 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p3).status(RegistrationStatus.CONFIRMED).build();
        TournamentRegistration reg4 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p4).status(RegistrationStatus.CONFIRMED).build();

        List<TournamentRegistration> confirmed = List.of(reg1, reg2, reg3, reg4);
        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(confirmed);

        List<SeededParticipant> seeded = List.of(
                new SeededParticipant(reg1, 1, 1.0),
                new SeededParticipant(reg2, 2, 0.8),
                new SeededParticipant(reg3, 3, 0.6),
                new SeededParticipant(reg4, 4, 0.4)
        );
        when(seedingStrategy.seed(tournament, confirmed)).thenReturn(seeded);

        TournamentMatch m1 = TournamentMatch.builder().round(1).matchOrder(1).status(TournamentMatchStatus.READY).build();
        when(cupBracketGenerator.generateBracket(tournament, seeded)).thenReturn(List.of(m1));

        TournamentResponse response = lifecycleService.startTournament(tournamentId);

        assertThat(response.status()).isEqualTo(TournamentStatus.IN_PROGRESS);
        verify(tournamentMatchRepository).saveAll(List.of(m1));

        var captor = ArgumentCaptor.forClass(TournamentStartedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().tournamentId()).isEqualTo(tournamentId);
        assertThat(captor.getValue().totalMatches()).isEqualTo(1);
    }

    @Test
    void shouldStartTournamentAndUseRandomPairingBracketGeneratorWhenModeIs2v2Random() {
        tournament.setMode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS);
        when(tournamentRepository.findByIdWithLock(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(i -> i.getArgument(0));

        User p1 = User.builder().id(UUID.randomUUID()).nickname("Player1").build();
        User p2 = User.builder().id(UUID.randomUUID()).nickname("Player2").build();
        User p3 = User.builder().id(UUID.randomUUID()).nickname("Player3").build();
        User p4 = User.builder().id(UUID.randomUUID()).nickname("Player4").build();

        TournamentRegistration reg1 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p1).status(RegistrationStatus.CONFIRMED).build();
        TournamentRegistration reg2 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p2).status(RegistrationStatus.CONFIRMED).build();
        TournamentRegistration reg3 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p3).status(RegistrationStatus.CONFIRMED).build();
        TournamentRegistration reg4 = TournamentRegistration.builder().id(UUID.randomUUID()).player(p4).status(RegistrationStatus.CONFIRMED).build();

        List<TournamentRegistration> confirmed = List.of(reg1, reg2, reg3, reg4);
        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(confirmed);

        List<SeededParticipant> seeded = List.of(
                new SeededParticipant(reg1, 1, 1.0),
                new SeededParticipant(reg2, 2, 0.8),
                new SeededParticipant(reg3, 3, 0.6),
                new SeededParticipant(reg4, 4, 0.4)
        );
        when(seedingStrategy.seed(tournament, confirmed)).thenReturn(seeded);

        TournamentMatch m1 = TournamentMatch.builder().round(1).matchOrder(1).status(TournamentMatchStatus.READY).build();
        when(randomPairingBracketGenerator.generateBracket(tournament, seeded)).thenReturn(List.of(m1));

        TournamentResponse response = lifecycleService.startTournament(tournamentId);

        assertThat(response.status()).isEqualTo(TournamentStatus.IN_PROGRESS);
        verify(randomPairingBracketGenerator).generateBracket(tournament, seeded);
        verify(tournamentMatchRepository).saveAll(List.of(m1));
    }
}
