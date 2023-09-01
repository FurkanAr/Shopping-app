package org.commerce.authenticationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.commerce.authenticationservice.config.RabbitMQMailConfiguration;
import org.commerce.authenticationservice.constants.Constant;
import org.commerce.authenticationservice.converter.MailConverter;
import org.commerce.authenticationservice.converter.TokenConverter;
import org.commerce.authenticationservice.converter.UserConverter;
import org.commerce.authenticationservice.exception.messages.Messages;
import org.commerce.authenticationservice.exception.token.EmailAlreadyConfirmedException;
import org.commerce.authenticationservice.exception.token.VerificationTokenExpiredException;
import org.commerce.authenticationservice.exception.token.VerificationTokenNotFoundException;
import org.commerce.authenticationservice.exception.user.UserEmailAlreadyInUseException;
import org.commerce.authenticationservice.exception.user.UserNameAlreadyInUseException;
import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.model.Token;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.model.VerificationToken;
import org.commerce.authenticationservice.repository.TokenRepository;
import org.commerce.authenticationservice.repository.UserRepository;
import org.commerce.authenticationservice.request.LoginRequest;
import org.commerce.authenticationservice.request.MailRequest;
import org.commerce.authenticationservice.request.RegisterRequest;
import org.commerce.authenticationservice.response.AuthenticationResponse;
import org.commerce.authenticationservice.security.jwt.JwtTokenService;
import org.commerce.authenticationservice.security.service.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Map.entry;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQMailConfiguration rabbitMQMailConfiguration;
    private final MailConverter mailConverter;
    private final VerificationTokenService verificationTokenService;
    private final RoleService roleService;
    private final TokenService tokenService;
    Logger logger = LoggerFactory.getLogger(getClass());

    public AuthenticationService(UserRepository userRepository, UserConverter userConverter,
                                  AuthenticationManager authenticationManager, JwtTokenService jwtTokenService,
                                 RabbitTemplate rabbitTemplate, RabbitMQMailConfiguration rabbitMQMailConfiguration,
                                 MailConverter mailConverter, VerificationTokenService verificationTokenService,
                                 RoleService roleService, TokenService tokenService) {

        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQMailConfiguration = rabbitMQMailConfiguration;
        this.mailConverter = mailConverter;
        this.verificationTokenService = verificationTokenService;
        this.roleService = roleService;
        this.tokenService = tokenService;
    }

    @Transactional
    public String register(RegisterRequest registerRequest) {
        logger.info("register method started");
        logger.info("RegisterRequest: {}", registerRequest);

        checkRequestEmailInUse(registerRequest.getEmail());
        checkRequestUserNameInUse(registerRequest.getUsername());

        Set<Role> roles = roleService.getRoles(registerRequest.getRoles());
        User user = userRepository.save(userConverter.convert(registerRequest, roles));

        logger.info("User created: {}", user.getId());

        // TODO sent user information to feign client
        // TODO sent mail to notification service

        UserDetails userDetails = CustomUserDetails.create(user);
        String verificationToken = jwtTokenService.generateVerificationToken(userDetails);
        verificationTokenService.createVerificationToken(verificationToken, user);
        String url = "http://localhost:9091/api/auth/verifyEmail?token="+verificationToken;

        MailRequest mailRequest = mailConverter
                .convert(user.getEmail(), Constant.Authentication.REGISTRATION_SUBJECT,
                                        Constant.Authentication.REGISTRATION_MAIL_MESSAGE + url);

        rabbitTemplate.convertAndSend(rabbitMQMailConfiguration.getQueueName(), mailRequest);
        logger.info("MailRequest: {}, sent to : {}", mailRequest, rabbitMQMailConfiguration.getQueueName());

        logger.info("register method successfully worked");
        return "Success! Please, check your email for to complete your registration";
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

        tokenService.saveToken(jwtToken, user);
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
                tokenService.saveToken(accessToken, user);
                logger.info("User {}, token saved ", user.getId());

                var authResponse = new AuthenticationResponse(accessToken, refreshToken);
                logger.info("access and refresh token created user: {}", user.getId());

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
        logger.info("refreshToken method successfully worked");
    }

    @Transactional
    public String verifyEmail(String token) {
        logger.info("verifyEmail method started");
        VerificationToken verificationToken = verificationTokenService.getToken(token);

        if (verificationToken.getConfirmedAt() != null){
            logger.warn("Token already confirmed, user {}", verificationToken.getUser().getId());
            throw new EmailAlreadyConfirmedException(Messages.VerificationToken.ALREADY_CONFIRMED);
        }

        LocalDateTime expiredAt = verificationToken.getExpiredAt();

        if (expiredAt.isBefore(LocalDateTime.now())){
            logger.warn("Token expired");
            verificationTokenService.deleteToken(token);
            throw new VerificationTokenExpiredException(Messages.VerificationToken.EXPIRED);
        }

        verificationTokenService.setEnableToken(token);

        User user = verificationToken.getUser();
        user.setEnabled(Boolean.TRUE);
        userRepository.save(user);

        logger.info("User: {}, account enabled", user.getId());
        logger.info("verifyEmail method successfully worked");

        return "Email verified successfully. Now you can login to your account";
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
        List<Token> validUserTokens = tokenService.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            logger.warn("User {} has not valid tokens: ", user.getId());
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenService.saveAllTokens(validUserTokens);
        logger.info("User {}, valid tokens saved ", user.getId());
        logger.info("revokeAllUserTokens method successfully worked");
    }



}
