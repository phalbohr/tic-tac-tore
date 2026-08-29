package com.tictactore.repository;

import com.tictactore.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    @Query("SELECT u.nickname FROM User u WHERE u.nickname IN :nicknames")
    List<String> findExistingNicknames(@Param("nicknames") List<String> nicknames);

    @Query("SELECT u FROM User u WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :query, '%')) AND u.email NOT LIKE 'deleted-%' AND u.nickname NOT LIKE 'ex-player-%'")
    List<User> searchActiveUsers(@Param("query") String query, Pageable pageable);

    List<User> findByPoolNotificationsEnabledTrueAndIdNot(UUID excludedUserId);

    Slice<User> findByPoolNotificationsEnabledTrueAndIdNot(UUID excludedUserId, Pageable pageable);
}
