package com.tictactore.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController ATDD Tests")
class UserControllerTest {

    @Test
    @Disabled("TDD RED PHASE - Story 1.4 Profile Management")
    @DisplayName("PATCH /me - should update language and nickname")
    void patchMe_shouldUpdateLanguageAndNickname() {
        // Arrange
        
        // Act
        
        // Assert
        
    }

    @Test
    @Disabled("TDD RED PHASE - Story 1.4 Profile Management")
    @DisplayName("PATCH /me - should return 400 when cooldown not passed")
    void patchMe_shouldReturn400_whenCooldownNotPassed() {
        // Arrange
        
        // Act
        
        // Assert
        
    }
}
