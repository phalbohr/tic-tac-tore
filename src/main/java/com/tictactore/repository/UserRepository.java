package com.tictactore.repository;

import com.tictactore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u.nickname FROM User u WHERE u.nickname IN :nicknames")
    java.util.List<String> findExistingNicknames(@Param("nicknames") java.util.Collection<String> nicknames);
}
