package org.commerce.authenticationservice.controller;

import org.commerce.authenticationservice.request.LoginRequest;
import org.commerce.authenticationservice.request.UserRequest;
import org.commerce.authenticationservice.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest) {
        logger.info("login method started");
        String response = authenticationService.login(loginRequest);
        logger.info("login successfully worked, username: {}", loginRequest.getUserName());
        return ResponseEntity.ok(response);
    }

}
