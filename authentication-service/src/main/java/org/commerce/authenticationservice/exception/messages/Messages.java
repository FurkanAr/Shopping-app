package org.commerce.authenticationservice.exception.messages;

public class Messages {


    public static class VerificationToken{

        public static final String NOT_EXISTS = "Token not found";

        public static final String ALREADY_CONFIRMED = "Email already confirmed";
        public static final String EXPIRED = "Token expired";
    }

    public static class Role{
        public static final String NOT_EXISTS = "Role cannot find with given name: ";
    }

    public static class User {
        public static final String EXIST = "User already has account by given email: ";
        public static final String NOT_EXISTS = "User cannot find with given username: ";
        public static final String EMAIL_NOT_EXISTS = "User cannot find with given email: ";
        public static final String ID_NOT_EXISTS = "User cannot find with given id: ";
        public static final String NAME_EXIST = "Username is used : ";
        public static final String EMAIL_EXIST = "Email is used: ";
        public static final String INCORRECT_PASSWORD = "Your password is incorrect given username: ";


    }
}
