package org.commerce.authenticationservice.converter;

import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.request.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserConverter {

    private final PasswordEncoder passwordEncoder;
    Logger logger = LoggerFactory.getLogger(getClass());

    public UserConverter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User convert(UserRequest userRequest, Set<Role> roles) {
        logger.info("convert to User method started");
        User user = new User();
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setUserName(userRequest.getUserName());
        user.setEmail(userRequest.getEmail());
        user.setRoles(roles);
        user.setFullName(userRequest.getFullName());
        logger.info("convert to User method successfully worked");
        return user;
    }

}
