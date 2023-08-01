package org.commerce.authenticationservice.controller;

import org.commerce.authenticationservice.request.LoginRequest;
import org.commerce.authenticationservice.request.UserRequest;
import org.commerce.authenticationservice.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    Logger logger = LoggerFactory.getLogger(getClass());

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRequest userRequest) {
        logger.info("register method started");
        String response = authenticationService.register(userRequest);
        logger.info("register successfully worked, user email: {}", userRequest.getEmail());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest) {
        logger.info("login method started");
        String response = authenticationService.login(loginRequest);
        logger.info("login successfully worked, username: {}", loginRequest.getUserName());
        return ResponseEntity.ok(response);
    }
}
