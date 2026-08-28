package com.tictactore.controller;

import com.tictactore.dto.CreatePoolRequest;
import com.tictactore.dto.PoolResponse;
import com.tictactore.model.User;
import com.tictactore.service.PoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pools")
@RequiredArgsConstructor
public class PoolController {

    private final PoolService poolService;

    @PostMapping
    public ResponseEntity<PoolResponse> createPool(
            @Valid @RequestBody CreatePoolRequest request,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PoolResponse response = poolService.createPool(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoolResponse> getPoolById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PoolResponse response = poolService.getPoolById(id);
        return ResponseEntity.ok(response);
    }
}
