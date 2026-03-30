package com.advance.auth.exception;

public class CardLimitExceededException extends RuntimeException {
    public CardLimitExceededException(Long userId, int limit) {
        super("User with id " + userId + " already has " + limit + "cards (maximum reached)");
    }
}
