package com.tictactore.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class E2EUserDto {
    private UUID id;
    private String email;
    private String nickname;
    private String providerId;
    private String avatar;
    private String language;
}
