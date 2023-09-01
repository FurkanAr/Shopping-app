package org.commerce.authenticationservice.controller;

import org.commerce.authenticationservice.request.LoginRequest;
import org.commerce.authenticationservice.request.RegisterRequest;
import org.commerce.authenticationservice.response.AuthenticationResponse;
import org.commerce.authenticationservice.service.AuthenticationService;
import org.commerce.authenticationservice.service.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final VerificationTokenService verificationTokenService;
    Logger logger = LoggerFactory.getLogger(getClass());

    public AuthenticationController(AuthenticationService authenticationService, VerificationTokenService verificationTokenService) {
        this.authenticationService = authenticationService;
        this.verificationTokenService = verificationTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest registerRequest) {
        logger.info("register method started");
        String response = authenticationService.register(registerRequest);
        logger.info("register successfully worked, user email: {}", registerRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        logger.info("login method started");
        AuthenticationResponse authenticationResponse = authenticationService.login(loginRequest);
        logger.info("login successfully worked, username: {}", loginRequest.getUsername());
        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("refreshToken method started");
        authenticationService.refreshToken(request, response);
        logger.info("refreshToken successfully worked");
    }

    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token){
        return authenticationService.verifyEmail(token);
    }

}
