package org.commerce.authenticationservice.service;

import org.commerce.authenticationservice.converter.TokenConverter;
import org.commerce.authenticationservice.model.Token;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final TokenConverter tokenConverter;
    Logger logger = LoggerFactory.getLogger(getClass());


    public TokenService(TokenRepository tokenRepository, TokenConverter tokenConverter) {
        this.tokenRepository = tokenRepository;
        this.tokenConverter = tokenConverter;
    }


    public void saveToken(String jwtToken, User user) {
        logger.info("saveToken method started");
        Token token = tokenConverter.convert(jwtToken, user);
        tokenRepository.save(token);
        logger.info("saveToken method successfully worked");

    }

    public List<Token> findAllValidTokenByUser(Long id) {
        logger.info("findAllValidTokenByUser method started");
        var validUserTokens = tokenRepository.findAllValidTokenByUser(id);
        logger.info("findAllValidTokenByUser method successfully worked");
        return validUserTokens;
    }

    public void saveAllTokens(List<Token> validUserTokens) {
        logger.info("saveAllTokens method started");
        tokenRepository.saveAll(validUserTokens);
        logger.info("saveAllTokens method successfully worked");
    }
}
