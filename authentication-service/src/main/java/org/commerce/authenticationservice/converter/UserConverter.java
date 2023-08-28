package org.commerce.authenticationservice.converter;

import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.request.RegisterRequest;
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

    public User convert(RegisterRequest registerRequest, Set<Role> roles) {
        logger.info("convert to User method started");
        User user = new User();
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setRoles(roles);
        user.setFirstName(registerRequest.getFirstName());
        user.setSurName(registerRequest.getSurName());
        logger.info("convert to User method successfully worked");
        return user;
    }

}
