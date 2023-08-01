package org.commerce.authenticationservice.service;

import org.commerce.authenticationservice.constants.Constant;
import org.commerce.authenticationservice.converter.UserConverter;
import org.commerce.authenticationservice.exception.messages.Messages;
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

        checkRequestEmailInUse(userRequest);
        checkRequestUserNameInUse(userRequest);

        Set<Role> roles = new HashSet<>();
        userRequest.getRoles().forEach(r -> roles.add(roleRepository.findByName(r)));

        User user = userRepository.save(userConverter.convert(userRequest, roles));

        // TODO sent user information to feign client
        // TODO sent mail to notification service

        logger.info("User created: {}", user.getId());

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

    private void checkRequestUserNameInUse(UserRequest userRequest) {
        Optional<User> user = userRepository.findByUserName(userRequest.getUserName());
        if (user.isPresent()) {
            logger.warn("Username is not available: {}", userRequest.getUserName());
            throw new UserNameAlreadyInUseException(Messages.User.NAME_EXIST + userRequest.getUserName());
        }
        logger.info("Username can be use");
    }

    private void checkRequestEmailInUse(UserRequest userRequest) {
        Optional<User> user = userRepository.findByEmail(userRequest.getEmail());
        if (user.isPresent()) {
            logger.warn("User already has account by given email: {}", userRequest.getEmail());
            throw new UserEmailAlreadyInUseException(Messages.User.EMAIL_EXIST + userRequest.getEmail());
        }
        logger.info("Email can be use");
    }

}
