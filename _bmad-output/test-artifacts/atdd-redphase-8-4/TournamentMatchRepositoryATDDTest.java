package com.tictactore.repository;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TournamentMatchRepository ATDD Tests (Story 8.4: 2v2 Partners & Stubs)")
class TournamentMatchRepositoryATDDTest {

    @Autowired
    private TournamentMatchRepository tournamentMatchRepository;

    @Autowired
    private TournamentRegistrationRepository registrationRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg1Partner;
    private TournamentRegistration reg2;
    private TournamentRegistration reg2Partner;

    @BeforeEach
    void setUp() {
        User p1 = userRepository.save(User.builder().email("p1@example.com").nickname("PlayerOne").build());
        User p1p = userRepository.save(User.builder().email("p1p@example.com").nickname("PartnerOne").build());
        User p2 = userRepository.save(User.builder().email("p2@example.com").nickname("PlayerTwo").build());
        User p2p = userRepository.save(User.builder().email("p2p@example.com").nickname("PartnerTwo").build());

        tournament = tournamentRepository.save(Tournament.builder()
                .name("2v2 Random Championship")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                .minParticipants(4)
                .maxParticipants(16)
                .registrationDeadline(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(TournamentStatus.IN_PROGRESS)
                .build());

        reg1 = registrationRepository.save(TournamentRegistration.builder().tournament(tournament).user(p1).build());
        reg1Partner = registrationRepository.save(TournamentRegistration.builder().tournament(tournament).user(p1p).build());
        reg2 = registrationRepository.save(TournamentRegistration.builder().tournament(tournament).user(p2).build());
        reg2Partner = registrationRepository.save(TournamentRegistration.builder().tournament(tournament).user(p2p).build());
    }

    @Test
    void shouldPersistFourParticipantsAndStubFlags() {
        TournamentMatch match = TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant1Partner(reg1Partner)
                .participant2(reg2)
                .participant2Partner(reg2Partner)
                .isParticipant1Stub(true)
                .isParticipant2Stub(false)
                .status(TournamentMatchStatus.READY)
                .build();

        TournamentMatch saved = tournamentMatchRepository.save(match);
        entityManager.flush();
        entityManager.clear();

        TournamentMatch found = tournamentMatchRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getParticipant1().getId()).isEqualTo(reg1.getId());
        assertThat(found.getParticipant1Partner().getId()).isEqualTo(reg1Partner.getId());
        assertThat(found.getParticipant2().getId()).isEqualTo(reg2.getId());
        assertThat(found.getParticipant2Partner().getId()).isEqualTo(reg2Partner.getId());
        assertThat(found.isParticipant1Stub()).isTrue();
        assertThat(found.isParticipant2Stub()).isFalse();
    }

    @Test
    void shouldFindByAnyParticipantRegistrationIdIncludingPartners() {
        TournamentMatch match = TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant1Partner(reg1Partner)
                .participant2(reg2)
                .participant2Partner(reg2Partner)
                .status(TournamentMatchStatus.READY)
                .build();
        tournamentMatchRepository.save(match);
        entityManager.flush();
        entityManager.clear();

        List<TournamentMatch> foundByPart1 = tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), reg1.getId());
        List<TournamentMatch> foundByPart1Partner = tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), reg1Partner.getId());
        List<TournamentMatch> foundByPart2 = tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), reg2.getId());
        List<TournamentMatch> foundByPart2Partner = tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), reg2Partner.getId());

        assertThat(foundByPart1).hasSize(1);
        assertThat(foundByPart1Partner).hasSize(1);
        assertThat(foundByPart2).hasSize(1);
        assertThat(foundByPart2Partner).hasSize(1);
    }
}
