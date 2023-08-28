package org.commerce.authenticationservice.converter;

import org.commerce.authenticationservice.model.Token;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.model.enums.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TokenConverter {

    Logger logger = LoggerFactory.getLogger(getClass());

    public Token convert(String jwtToken, User user) {
        logger.info("convert to token method started");
        Token token = new Token();
        token.setToken(jwtToken);
        token.setTokenType(TokenType.BEARER);
        token.setExpired(false);
        token.setRevoked(false);
        token.setUser(user);
        logger.info("convert to token method successfully worked");
        return token;
    }

}
