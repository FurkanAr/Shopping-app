package org.commerce.authenticationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.commerce.authenticationservice.converter.TokenConverter;
import org.commerce.authenticationservice.converter.UserConverter;
import org.commerce.authenticationservice.exception.messages.Messages;
import org.commerce.authenticationservice.exception.role.RoleCannotFoundException;
import org.commerce.authenticationservice.exception.user.UserEmailAlreadyInUseException;
import org.commerce.authenticationservice.exception.user.UserNameAlreadyInUseException;
import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.repository.RoleRepository;
import org.commerce.authenticationservice.repository.TokenRepository;
import org.commerce.authenticationservice.repository.UserRepository;
import org.commerce.authenticationservice.request.LoginRequest;
import org.commerce.authenticationservice.request.RegisterRequest;
import org.commerce.authenticationservice.response.AuthenticationResponse;
import org.commerce.authenticationservice.security.jwt.JwtTokenService;
import org.commerce.authenticationservice.security.service.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Map.entry;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final TokenConverter tokenConverter;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    Logger logger = LoggerFactory.getLogger(getClass());

    public AuthenticationService(UserRepository userRepository, UserConverter userConverter, RoleRepository roleRepository, TokenRepository tokenRepository, TokenConverter tokenConverter, AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
        this.tokenConverter = tokenConverter;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        logger.info("register method started");
        logger.info("RegisterRequest: {}", registerRequest);

        checkRequestEmailInUse(registerRequest.getEmail());
        checkRequestUserNameInUse(registerRequest.getUsername());

        Set<Role> roles = new HashSet<>();
        registerRequest.getRoles().forEach(role -> roles.add(roleRepository.findByName(role)
                .orElseThrow(() -> new RoleCannotFoundException(Messages.Role.NOT_EXISTS + role))));

        User user = userRepository.save(userConverter.convert(registerRequest, roles));

        logger.info("User created: {}", user.getId());

        // TODO sent user information to feign client
        // TODO sent mail to notification service

        Map<String, Object> claims = getUserJwtClaims(user);
        UserDetails userDetails = CustomUserDetails.create(user);

        var jwtToken = jwtTokenService.generateToken(userDetails, claims);
        logger.info("User {}, token created ", user.getId());

        tokenRepository.save(tokenConverter.convert(jwtToken, user));
        logger.info("User {}, token saved ", user.getId());

        var refreshToken = jwtTokenService.generateRefreshToken(userDetails, getUserJwtClaims(user));
        logger.info("User {}, refresh token created ", user.getId());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtToken, refreshToken);
        logger.info("Access and refresh token created user: {}", user.getId());

        logger.info("register method successfully worked");
        return authenticationResponse;
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest loginRequest) {
        logger.info("login method started");
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() ->
                new UsernameNotFoundException(Messages.User.NOT_EXISTS + loginRequest.getUsername()));

        logger.info("Found user: {}", user.getId());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword());

        authenticationManager.authenticate(authToken);
        logger.info("User authenticated user: {}", user.getId());

        Map<String, Object> claims = getUserJwtClaims(user);
        UserDetails userDetails = CustomUserDetails.create(user);

        var jwtToken = jwtTokenService.generateToken(userDetails, claims);
        logger.info("User {}, token created ", user.getId());

        revokeAllUserTokens(user);

        tokenRepository.save(tokenConverter.convert(jwtToken, user));
        logger.info("User {}, token saved ", user.getId());

        var refreshToken = jwtTokenService.generateRefreshToken(userDetails, getUserJwtClaims(user));
        logger.info("User {}, refresh token created ", user.getId());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtToken, refreshToken);
        logger.info("Access and refresh token created user: {}", user.getId());

        logger.info("login method successfully worked");
        return authenticationResponse;
    }

    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("refreshToken method started");
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String username;

        if (header == null || !header.startsWith("Bearer ")) {
            logger.warn("Header is missing");
            return;
        }
        refreshToken = header.substring(7);
        logger.info("Authorization request in header");
        username = jwtTokenService.findUserName(refreshToken);
        logger.info("User found with refresh token: {}", username);

        if (username != null) {
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(Messages.User.NOT_EXISTS + username));
            UserDetails userDetails = CustomUserDetails.create(user);
            logger.info("User: {}, userDetails", user.getUsername());

            if (jwtTokenService.tokenControl(refreshToken, userDetails)) {
                var accessToken = jwtTokenService.generateToken(userDetails, getUserJwtClaims(user));
                logger.info("User {}, token created ", user.getId());

                revokeAllUserTokens(user);
                tokenRepository.save(tokenConverter.convert(accessToken, user));
                logger.info("User {}, token saved ", user.getId());

                var authResponse = new AuthenticationResponse(accessToken, refreshToken);
                logger.info("access and refresh token created user: {}", user.getId());

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
        logger.info("refreshToken method successfully worked");
    }

    private void checkRequestUserNameInUse(String username) {
        logger.info("checkRequestUserNameInUse method started");
        Optional<User> user = userRepository.findByUsername(username);
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
        logger.info("Email {}, can be use: ", email);
        logger.info("checkRequestEmailInUse method successfully worked");
    }

    private Map<String, Object> getUserJwtClaims(User user) {
        logger.info("getUserJwtClaims method started");

        Map<String, Object> claims = Map.ofEntries(entry("userId", user.getId()),
                entry("username", user.getUsername()),
                entry("role", user.getRoles()));
        logger.info("User {}, jwt token claims created ", user.getId());
        logger.info("getUserJwtClaims method successfully worked");
        return claims;
    }

    private void revokeAllUserTokens(User user) {
        logger.info("revokeAllUserTokens method started");
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            logger.warn("User {} has not valid tokens: ", user.getId());
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
        logger.info("User {}, valid tokens saved ", user.getId());
        logger.info("revokeAllUserTokens method successfully worked");
    }


}
