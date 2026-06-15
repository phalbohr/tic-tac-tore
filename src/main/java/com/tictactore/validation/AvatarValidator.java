package com.tictactore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class AvatarValidator implements ConstraintValidator<ValidAvatar, String> {

    public static final Set<String> ALLOWED_AVATARS = Set.of(
            "ball-classic", "ball-cork", "player-red-1", "player-red-2",
            "player-blue-1", "player-blue-2", "table-classic", "table-top",
            "beer-mug", "beer-bottle", "trophy-gold", "trophy-silver",
            "glove-red", "glove-blue", "whistle-gold", "foosball-rod",
            "handle-wood", "handle-rubber", "score-counter", "snack-pretzel",
            "snack-pizza", "jersey-red", "jersey-blue", "crown"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return ALLOWED_AVATARS.contains(value);
    }
}
