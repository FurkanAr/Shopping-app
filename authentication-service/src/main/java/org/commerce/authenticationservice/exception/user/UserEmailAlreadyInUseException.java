package org.commerce.authenticationservice.exception.user;

public class UserEmailAlreadyInUseException extends RuntimeException {
    public UserEmailAlreadyInUseException(String message) {
        super(message);
    }
}
