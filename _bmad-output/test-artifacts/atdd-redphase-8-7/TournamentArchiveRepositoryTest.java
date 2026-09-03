package com.tictactore.repository;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.geode.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("ATDD red phase: Story 8.7 - Tournament archive paginated repository queries")
@org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
@DisplayName("TournamentRepository Archive Pagination Tests")
class TournamentArchiveRepositoryTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should query completed tournaments paginated and ordered by updated_at descending")
    void shouldQueryCompletedTournamentsPaginated() {
        var t1 = Tournament.builder()
                .title("Completed Tournament 1")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.ONE_VS_ONE)
                .status(TournamentStatus.COMPLETED)
                .registrationDeadline(Instant.now().minusSeconds(3600))
                .build();

        var t2 = Tournament.builder()
                .title("Active Tournament 2")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE)
                .status(TournamentStatus.IN_PROGRESS)
                .registrationDeadline(Instant.now().minusSeconds(1800))
                .build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.flush();

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        var completedPage = tournamentRepository.findByStatus(TournamentStatus.COMPLETED, pageable);

        assertThat(completedPage.getContent()).hasSize(1);
        assertThat(completedPage.getContent().get(0).getTitle()).isEqualTo("Completed Tournament 1");
    }
}
