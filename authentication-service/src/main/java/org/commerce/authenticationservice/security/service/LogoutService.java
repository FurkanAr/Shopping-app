package org.commerce.authenticationservice.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commerce.authenticationservice.constants.Constant;
import org.commerce.authenticationservice.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    Logger logger = LoggerFactory.getLogger(getClass());

    public LogoutService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logger.info("logout method started");
        final String header = request.getHeader("Authorization");
        final String jwt;
        if (header == null || !header.startsWith("Bearer ")) {
            logger.warn("Header is missing");
            return;
        }
        jwt = header.substring(7);
        logger.info("Authorization request in header");
        var storedToken = tokenRepository.findByToken(jwt).orElse(null);
        if (storedToken != null) {
            logger.info("Token found with jwt: {}", storedToken.getId());
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            logger.info("Token saved: {}", storedToken.getId());
            try {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), Constant.Authentication.LOGOUT_MESSAGE);
                logger.info("User logged out");
            } catch (IOException e) {
                logger.warn("IOException");
                throw new RuntimeException(e);
            }

        }
        logger.info("logout method successfully worked");
    }
}
