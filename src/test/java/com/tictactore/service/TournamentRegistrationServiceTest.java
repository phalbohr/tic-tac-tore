package com.tictactore.service;

import com.tictactore.dto.RegisterTournamentRequest;
import com.tictactore.event.TournamentInviteAcceptedEvent;
import com.tictactore.event.TournamentInviteCreatedEvent;
import com.tictactore.event.TournamentInviteDeclinedEvent;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentRegistrationService Unit Tests")
class TournamentRegistrationServiceTest {

    @Mock
    private TournamentRegistrationRepository registrationRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TournamentRegistrationServiceImpl service;

    private final UUID tournamentId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();
    private final UUID partnerId = UUID.randomUUID();
    private final UUID strangerId = UUID.randomUUID();
    private final UUID registrationId = UUID.randomUUID();

    private Tournament tournament1v1;
    private Tournament tournament2v2;
    private User player;
    private User partner;

    @BeforeEach
    void setUp() {
        player = User.builder().id(playerId).nickname("Player").avatar("https://avatar.com/p.png").build();
        partner = User.builder().id(partnerId).nickname("Partner").avatar("https://avatar.com/partner.png").build();

        tournament1v1 = Tournament.builder()
                .id(tournamentId)
                .name("Solo Cup")
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .status(TournamentStatus.REGISTRATION_OPEN)
                .registrationDeadline(Instant.now().plus(2, ChronoUnit.DAYS))
                .maxParticipants(8)
                .build();

        tournament2v2 = Tournament.builder()
                .id(tournamentId)
                .name("Duo Championship")
                .mode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS)
                .status(TournamentStatus.REGISTRATION_OPEN)
                .registrationDeadline(Instant.now().plus(2, ChronoUnit.DAYS))
                .maxParticipants(8)
                .build();
    }

    @Nested
    @DisplayName("Registration Scenarios")
    class RegisterTests {

        @Test
        void shouldRegisterSoloSuccessfully_when1v1Mode() {
            var request = new RegisterTournamentRequest(null);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament1v1));
            when(registrationRepository.findActiveUserRegistration(eq(tournamentId), eq(playerId), any()))
                    .thenReturn(Optional.empty());
            when(registrationRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                    .thenReturn(0L);
            when(userRepository.findById(playerId)).thenReturn(Optional.of(player));
            when(registrationRepository.save(any(TournamentRegistration.class)))
                    .thenAnswer(invocation -> {
                        TournamentRegistration r = invocation.getArgument(0);
                        r.setId(registrationId);
                        return r;
                    });

            var response = service.register(tournamentId, playerId, request);

            assertThat(response.id()).isEqualTo(registrationId);
            assertThat(response.status()).isEqualTo(RegistrationStatus.CONFIRMED);
            assertThat(response.playerId()).isEqualTo(playerId);
            assertThat(response.partnerId()).isNull();
        }

        @Test
        void shouldRegisterWithPartner_when2v2FixedTeamsMode() {
            var request = new RegisterTournamentRequest(partnerId);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament2v2));
            when(registrationRepository.findActiveUserRegistration(eq(tournamentId), eq(playerId), any()))
                    .thenReturn(Optional.empty());
            when(registrationRepository.findActiveUserRegistration(eq(tournamentId), eq(partnerId), any()))
                    .thenReturn(Optional.empty());
            when(registrationRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                    .thenReturn(0L);
            when(userRepository.findById(playerId)).thenReturn(Optional.of(player));
            when(userRepository.findById(partnerId)).thenReturn(Optional.of(partner));
            when(registrationRepository.save(any(TournamentRegistration.class)))
                    .thenAnswer(invocation -> {
                        TournamentRegistration r = invocation.getArgument(0);
                        r.setId(registrationId);
                        return r;
                    });

            var response = service.register(tournamentId, playerId, request);

            assertThat(response.id()).isEqualTo(registrationId);
            assertThat(response.status()).isEqualTo(RegistrationStatus.PENDING_CONFIRMATION);
            assertThat(response.partnerId()).isEqualTo(partnerId);
            assertThat(response.partnerNickname()).isEqualTo("Partner");
            verify(eventPublisher).publishEvent(any(TournamentInviteCreatedEvent.class));
        }

        @Test
        void shouldThrowResourceNotFoundException_whenTournamentNotFound() {
            var request = new RegisterTournamentRequest(null);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.register(tournamentId, playerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void shouldThrowIllegalStateException_whenTournamentNotOpen() {
            tournament1v1.setStatus(TournamentStatus.IN_PROGRESS);
            var request = new RegisterTournamentRequest(null);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament1v1));

            assertThatThrownBy(() -> service.register(tournamentId, playerId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tournament is not open for registration");
        }

        @Test
        void shouldThrowIllegalArgumentException_whenDeadlinePassed() {
            tournament1v1.setRegistrationDeadline(Instant.now().minus(1, ChronoUnit.HOURS));
            var request = new RegisterTournamentRequest(null);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament1v1));

            assertThatThrownBy(() -> service.register(tournamentId, playerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("deadline has passed");
        }

        @Test
        void shouldThrowIllegalArgumentException_whenPartnerMissingIn2v2Fixed() {
            var request = new RegisterTournamentRequest(null);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament2v2));

            assertThatThrownBy(() -> service.register(tournamentId, playerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Partner ID is required");
        }

        @Test
        void shouldThrowIllegalArgumentException_whenPartnerProvidedIn1v1() {
            var request = new RegisterTournamentRequest(partnerId);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament1v1));

            assertThatThrownBy(() -> service.register(tournamentId, playerId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Partner cannot be specified");
        }

        @Test
        void shouldThrowIllegalStateException_whenCapacityReached() {
            var request = new RegisterTournamentRequest(null);
            when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament1v1));
            when(registrationRepository.findActiveUserRegistration(eq(tournamentId), eq(playerId), any()))
                    .thenReturn(Optional.empty());
            when(registrationRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                    .thenReturn(8L);

            assertThatThrownBy(() -> service.register(tournamentId, playerId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("maximum participant capacity");
        }
    }

    @Nested
    @DisplayName("Invitation Response Scenarios")
    class InvitationTests {

        @Test
        void shouldAcceptInvitationSuccessfully_whenPartnerAccepts() {
            var registration = TournamentRegistration.builder()
                    .id(registrationId)
                    .tournament(tournament2v2)
                    .player(player)
                    .partner(partner)
                    .status(RegistrationStatus.PENDING_CONFIRMATION)
                    .build();
            when(registrationRepository.findByIdWithDetails(registrationId)).thenReturn(Optional.of(registration));
            when(registrationRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED)).thenReturn(2L);
            when(registrationRepository.save(any(TournamentRegistration.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.acceptInvitation(tournamentId, registrationId, partnerId);

            assertThat(response.status()).isEqualTo(RegistrationStatus.CONFIRMED);
            verify(eventPublisher).publishEvent(any(TournamentInviteAcceptedEvent.class));
        }

        @Test
        void shouldThrowAccessDeniedException_whenNonPartnerAccepts() {
            var registration = TournamentRegistration.builder()
                    .id(registrationId)
                    .tournament(tournament2v2)
                    .player(player)
                    .partner(partner)
                    .status(RegistrationStatus.PENDING_CONFIRMATION)
                    .build();
            when(registrationRepository.findByIdWithDetails(registrationId)).thenReturn(Optional.of(registration));
            when(registrationRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED)).thenReturn(0L);

            assertThatThrownBy(() -> service.acceptInvitation(tournamentId, registrationId, strangerId))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void shouldDeclineInvitationSuccessfully_whenPartnerDeclines() {
            var registration = TournamentRegistration.builder()
                    .id(registrationId)
                    .tournament(tournament2v2)
                    .player(player)
                    .partner(partner)
                    .status(RegistrationStatus.PENDING_CONFIRMATION)
                    .build();
            when(registrationRepository.findByIdWithDetails(registrationId)).thenReturn(Optional.of(registration));
            when(registrationRepository.save(any(TournamentRegistration.class))).thenAnswer(i -> i.getArgument(0));

            var response = service.declineInvitation(tournamentId, registrationId, partnerId);

            assertThat(response.status()).isEqualTo(RegistrationStatus.DECLINED);
            verify(eventPublisher).publishEvent(any(TournamentInviteDeclinedEvent.class));
        }
    }

    @Nested
    @DisplayName("Cancellation & Queries")
    class CancelAndQueryTests {

        @Test
        void shouldCancelRegistration_whenPlayerCancels() {
            var registration = TournamentRegistration.builder()
                    .id(registrationId)
                    .tournament(tournament1v1)
                    .player(player)
                    .partner(null)
                    .status(RegistrationStatus.CONFIRMED)
                    .build();
            when(registrationRepository.findByIdWithDetails(registrationId)).thenReturn(Optional.of(registration));
            when(registrationRepository.save(any(TournamentRegistration.class))).thenAnswer(i -> i.getArgument(0));

            service.cancelRegistration(tournamentId, registrationId, playerId);

            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        }

        @Test
        void shouldThrowAccessDeniedException_whenStrangerCancels() {
            var registration = TournamentRegistration.builder()
                    .id(registrationId)
                    .tournament(tournament1v1)
                    .player(player)
                    .partner(null)
                    .status(RegistrationStatus.CONFIRMED)
                    .build();
            when(registrationRepository.findByIdWithDetails(registrationId)).thenReturn(Optional.of(registration));

            assertThatThrownBy(() -> service.cancelRegistration(tournamentId, registrationId, strangerId))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void shouldReturnMyRegistrationStatus_whenRegistered() {
            var registration = TournamentRegistration.builder()
                    .id(registrationId)
                    .tournament(tournament2v2)
                    .player(player)
                    .partner(partner)
                    .status(RegistrationStatus.PENDING_CONFIRMATION)
                    .build();
            when(tournamentRepository.existsById(tournamentId)).thenReturn(true);
            when(registrationRepository.findActiveUserRegistration(eq(tournamentId), eq(partnerId), any()))
                    .thenReturn(Optional.of(registration));

            var status = service.getMyRegistrationStatus(tournamentId, partnerId);

            assertThat(status.registered()).isTrue();
            assertThat(status.isPendingInvite()).isTrue();
            assertThat(status.registration().id()).isEqualTo(registrationId);
        }

        @Test
        void shouldReturnPendingInvitations() {
            var registration = TournamentRegistration.builder()
                    .id(registrationId)
                    .tournament(tournament2v2)
                    .player(player)
                    .partner(partner)
                    .status(RegistrationStatus.PENDING_CONFIRMATION)
                    .build();
            when(registrationRepository.findPendingInvitationsForUser(partnerId))
                    .thenReturn(List.of(registration));

            var list = service.getPendingInvitations(partnerId);

            assertThat(list).hasSize(1);
            assertThat(list.get(0).id()).isEqualTo(registrationId);
        }
    }
}
