package org.commerce.authenticationservice.exception.token;

public class VerificationTokenNotFoundException extends RuntimeException {
    public VerificationTokenNotFoundException(String message) {

        super(message);
    }
}
