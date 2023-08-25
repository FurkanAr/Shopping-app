package org.commerce.authenticationservice.service;

import org.commerce.authenticationservice.constants.Constant;
import org.commerce.authenticationservice.converter.UserConverter;
import org.commerce.authenticationservice.exception.messages.Messages;
import org.commerce.authenticationservice.exception.role.RoleCannotFoundException;
import org.commerce.authenticationservice.exception.user.UserEmailAlreadyInUseException;
import org.commerce.authenticationservice.exception.user.UserNameAlreadyInUseException;
import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.repository.RoleRepository;
import org.commerce.authenticationservice.repository.UserRepository;
import org.commerce.authenticationservice.request.LoginRequest;
import org.commerce.authenticationservice.request.UserRequest;
import org.commerce.authenticationservice.security.jwt.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    Logger logger = LoggerFactory.getLogger(getClass());

    public AuthenticationService(UserRepository userRepository, UserConverter userConverter, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    public String register(UserRequest userRequest) {
        logger.info("register method started");
        logger.info("UserRequest: {}", userRequest);

        checkRequestEmailInUse(userRequest.getEmail());
        checkRequestUserNameInUse(userRequest.getUserName());

        Set<Role> roles = new HashSet<>();
        userRequest.getRoles().forEach(role-> roles.add(roleRepository.findByName(role)
                .orElseThrow(() -> new RoleCannotFoundException(Messages.Role.NOT_EXISTS + role))));

        User user = userRepository.save(userConverter.convert(userRequest, roles));

        // TODO sent user information to feign client
        // TODO sent mail to notification service

        logger.info("User created: {}", user.getId());
        logger.info("register method successfully worked");
        return Constant.Authentication.REGISTRATION_MESSAGE;
    }

    public String login(LoginRequest loginRequest) {
        logger.info("login method started");
        User user = userRepository.findByUserName(loginRequest.getUserName()).orElseThrow(() ->
                new UsernameNotFoundException(Messages.User.NOT_EXISTS + loginRequest.getUserName()));

        logger.info("Found user: {}", user.getId());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUserName(), loginRequest.getPassword());

        authenticationManager.authenticate(authToken);
        logger.info("User authenticated user: {}", user.getId());

        var token = jwtTokenService.generateToken(user);
        logger.info("User {}, token created ", user.getId());

        logger.info("login method successfully worked");
        return token;
    }

    private void checkRequestUserNameInUse(String username) {
        logger.info("checkRequestUserNameInUse method started");
        Optional<User> user = userRepository.findByUserName(username);
        if (user.isPresent()) {
            logger.warn("Username is not available: {}", username);
            throw new UserNameAlreadyInUseException(Messages.User.NAME_EXIST + username);
        }
        logger.info("Username can be use");
        logger.info("checkRequestUserNameInUse method successfully worked");
    }

    private void checkRequestEmailInUse(String email) {
        logger.info("checkRequestUserNameInUse method started");
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            logger.warn("User already has account by given email: {}", email);
            throw new UserEmailAlreadyInUseException(Messages.User.EMAIL_EXIST + email);
        }
        logger.info("Email can be use");
        logger.info("checkRequestEmailInUse method successfully worked");
    }

}
