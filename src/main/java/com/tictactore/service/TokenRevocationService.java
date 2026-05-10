package com.tictactore.service;

public interface TokenRevocationService {
    void revoke(String token);
    boolean isRevoked(String token);
}
