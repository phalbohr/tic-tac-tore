package com.tictactore.controller;

import com.tictactore.dto.CreatePlayerGroupRequest;
import com.tictactore.dto.PlayerGroupResponse;
import com.tictactore.dto.UpdatePlayerGroupRequest;
import com.tictactore.model.User;
import com.tictactore.service.PlayerGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/player-groups")
@RequiredArgsConstructor
public class PlayerGroupController {

    private final PlayerGroupService playerGroupService;

    @GetMapping
    public ResponseEntity<List<PlayerGroupResponse>> getGroups(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(playerGroupService.getGroups(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerGroupResponse> getGroupById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(playerGroupService.getGroupById(principal.getId(), id));
    }

    @PostMapping
    public ResponseEntity<PlayerGroupResponse> createGroup(
            @Valid @RequestBody CreatePlayerGroupRequest request,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var created = playerGroupService.createGroup(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerGroupResponse> updateGroup(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdatePlayerGroupRequest request,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var updated = playerGroupService.updateGroup(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        playerGroupService.deleteGroup(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
