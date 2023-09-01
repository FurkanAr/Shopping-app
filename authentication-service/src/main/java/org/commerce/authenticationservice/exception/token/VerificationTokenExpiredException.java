package org.commerce.authenticationservice.exception.token;

public class VerificationTokenExpiredException extends RuntimeException {
    public VerificationTokenExpiredException(String message) {
        super(message);
    }
}
