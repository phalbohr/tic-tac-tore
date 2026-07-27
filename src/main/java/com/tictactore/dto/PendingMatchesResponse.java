package com.tictactore.dto;

import java.util.List;

/**
 * Data transfer object representing the list of pending match confirmation requests.
 *
 * @param count total number of pending matches for the current user
 * @param matches list of match responses currently pending verification
 */
public record PendingMatchesResponse(
        int count,
        List<MatchResponse> matches
) {}
