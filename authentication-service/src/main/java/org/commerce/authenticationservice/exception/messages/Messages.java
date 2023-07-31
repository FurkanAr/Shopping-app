package org.commerce.authenticationservice.exception.messages;

public class Messages {


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
