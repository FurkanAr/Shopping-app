package org.commerce.authenticationservice.exception.user;

public class UserNameAlreadyInUseException extends RuntimeException {
    public UserNameAlreadyInUseException(String message) {
        super(message);
    }
}
