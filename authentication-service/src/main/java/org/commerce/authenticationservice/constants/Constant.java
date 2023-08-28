package org.commerce.authenticationservice.constants;

public class Constant {


    public static class Jwt {
        public static final long ACCESS_TOKEN_EXPIRES_IN = 86400000;
        public static final String SECRET_KEY = "9ln6jZ1h5BuP28k5RmlOaeL5rise7xe9czd4yA8pZdfGA36zbmRt";

    }

    public static class RefreshToken{
        public static final long REFRESH_TOKEN_EXPIRES_IN = 604800000;
    }

    public static class Authentication {
        public static final String LOGIN_MESSAGE = "User successfully login!!";
        public static final String REGISTRATION_MAIL_MESSAGE = "Welcome to the blog app!!";
        public static final String REGISTRATION_MESSAGE = "User successfully registered!!";

        public static final String LOGOUT_MESSAGE = "User successfully logout!!";
    }

}
