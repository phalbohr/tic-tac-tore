package com.tictactore.api;

import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Matches", description = "API for recording and managing foosball matches")
public interface MatchApi {

    @Operation(summary = "Record a new 2v2 match", description = "Creators can record a match with scores and positions. Initial state is PENDING_APPROVAL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Match created successfully",
                     content = @Content(schema = @Schema(implementation = MatchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
        @ApiResponse(responseCode = "404", description = "One or more participants not found")
    })
    ResponseEntity<MatchResponse> createMatch(MatchRequest request);

    @Operation(summary = "Get matches pending approval by the current user", 
               description = "Returns a list of matches where the current user is an opponent and must approve or reject the results.")
    @ApiResponse(responseCode = "200", description = "List of pending matches retrieved successfully",
                 content = @Content(schema = @Schema(implementation = MatchResponse.class)))
    ResponseEntity<List<MatchResponse>> getPendingMatches();

    @Operation(summary = "Approve a pending match", description = "Allows an opponent to confirm the results of a match. Changes status from PENDING_APPROVAL to CONFIRMED.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Match approved successfully"),
        @ApiResponse(responseCode = "400", description = "Only an opponent can approve this match or invalid match status"),
        @ApiResponse(responseCode = "404", description = "Match not found")
    })
    ResponseEntity<Void> approveMatch(
            @Parameter(description = "ID of the match to be approved") UUID id);

    @Operation(summary = "Reject a pending match", description = "Allows an opponent to dispute the results of a match. Changes status from PENDING_APPROVAL back to DRAFT for corrections.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Match rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Only an opponent can reject this match or invalid match status"),
        @ApiResponse(responseCode = "404", description = "Match not found")
    })
    ResponseEntity<Void> rejectMatch(
            @Parameter(description = "ID of the match to be rejected") UUID id);
}
