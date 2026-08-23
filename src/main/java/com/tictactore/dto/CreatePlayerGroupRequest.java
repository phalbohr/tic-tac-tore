package com.tictactore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePlayerGroupRequest(
        @NotBlank(message = "Group name is required")
        @Size(max = 50, message = "Group name cannot exceed 50 characters")
        String name,
        @Size(max = 12, message = "Group cannot have more than 12 members")
        List<UUID> memberIds,
        Boolean isFavorite
) {
    public CreatePlayerGroupRequest(String name, List<UUID> memberIds, Boolean isFavorite) {
        this.name = name != null ? name.trim() : null;
        this.memberIds = memberIds != null ? memberIds : List.of();
        this.isFavorite = Boolean.TRUE.equals(isFavorite);
    }
}
