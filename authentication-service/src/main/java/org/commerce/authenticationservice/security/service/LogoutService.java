package org.commerce.authenticationservice.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commerce.authenticationservice.constants.Constant;
import org.commerce.authenticationservice.repository.TokenRepository;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    public LogoutService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String header = request.getHeader("Authorization");
        final String jwt;
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }
        jwt = header.substring(7);
        var storedToken = tokenRepository.findByToken(jwt).orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            try {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), Constant.Authentication.LOGOUT_MESSAGE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
