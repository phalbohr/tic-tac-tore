package com.tictactore.controller;

import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * MatchController
 *
 * <p>Responsibility: REST Controller for managing 2v2 foosball matches.
 * Handles match creation and the confirmation workflow (approve/reject).</p>
 *
 * <p>Usage:
 * <ul>
 *   <li>POST /api/v1/matches - Record a new match</li>
 *   <li>PUT /api/v1/matches/{id}/approve - Opponent confirmation</li>
 * </ul>
 * </p>
 *
 * <p>Dependencies:
 * <ul>
 *   <li>MatchService - Business logic for match operations</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Matches", description = "API for recording and managing foosball matches")
public class MatchController {

    private final MatchService matchService;

    /**
     * Records a new 2v2 match.
     * @param request The match details.
     * @return The created match record.
     */
    @Operation(summary = "Record a new 2v2 match", description = "Creators can record a match with scores and positions. Initial state is PENDING_APPROVAL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Match created successfully",
                     content = @Content(schema = @Schema(implementation = MatchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
        @ApiResponse(responseCode = "404", description = "One or more participants not found")
    })
    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchRequest request) {
        log.info("Received request to create a new match. Participants: {}, {}, {}, {}", 
            request.teamAAttackerId(), request.teamADefenderId(), 
            request.teamBAttackerId(), request.teamBDefenderId());
        
        MatchResponse createdMatch = matchService.createMatch(request);
        
        log.info("Successfully created match with ID: {}", createdMatch.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
    }

    /**
     * Approves a pending match.
     * Only an opponent can approve a match in PENDING_APPROVAL status.
     * @param id The ID of the match to approve.
     * @return 204 No Content if successful.
     */
    @Operation(summary = "Approve a pending match", description = "Allows an opponent to confirm the results of a match. Changes status from PENDING_APPROVAL to CONFIRMED.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Match approved successfully"),
        @ApiResponse(responseCode = "400", description = "Only an opponent can approve this match or invalid match status"),
        @ApiResponse(responseCode = "404", description = "Match not found")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approveMatch(
            @Parameter(description = "ID of the match to be approved") @PathVariable UUID id) {
        log.info("Received request to approve match with ID: {}", id);
        matchService.approveMatch(id);
        log.info("Successfully processed approval for match with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
