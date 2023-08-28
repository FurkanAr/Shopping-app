package org.commerce.authenticationservice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.commerce.authenticationservice.constants.Constant;
import org.commerce.authenticationservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenService {

    Logger logger = LoggerFactory.getLogger(getClass());

    public String findUserName(String token) {
        logger.info("findUserName method started");
        String username = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build().parseClaimsJws(token).getBody().get("username").toString();
        logger.info("User found with token: {}", username);
        logger.info("findUserName method successfully worked");
        return username;
    }

    private <T> T exportToken(String token, Function<Claims, T> claimFunction){
        logger.info("exportToken method started");
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build().parseClaimsJws(token).getBody();
        logger.info("claims: {}", claims);
        logger.info("claims: {}", claims.get("username"));
        logger.info("exportToken method successfully worked");
        return claimFunction.apply(claims);
    }

    private Key getKey() {
        logger.info("getKey method started");
        byte[] key = Decoders.BASE64.decode(Constant.Jwt.SECRET_KEY);
        logger.info("getKey method successfully worked");
        return Keys.hmacShaKeyFor(key);
    }

    public boolean tokenControl(String jwt, UserDetails userDetails) {
        logger.info("tokenControl method started");
        final String userName = findUserName(jwt);
        boolean isSame = (userName.equals(userDetails.getUsername()) && !exportToken(jwt, Claims::getExpiration).before(new Date()));
        logger.info("Username is same: {}", isSame);
        logger.info("tokenControl method successfully worked");
        return isSame;
    }

    public String generateToken(UserDetails user, Map<String, Object> extraClaims) {
        logger.info("generateToken method started");
        String token = buildToken(user, extraClaims, Constant.Jwt.ACCESS_TOKEN_EXPIRES_IN);
        logger.info("generateToken method successfully worked");
        return token;
    }

    public String generateRefreshToken(UserDetails user,  Map<String, Object> extraClaims) {
        logger.info("generateRefreshToken method started");
        String token = buildToken(user, extraClaims, Constant.RefreshToken.REFRESH_TOKEN_EXPIRES_IN);
        logger.info("generateRefreshToken method successfully worked");
        return token;
    }

    private String buildToken(UserDetails user, Map<String, Object> extraClaims, Long expiration){
        logger.info("buildToken method started");
        String token = Jwts.builder()
                .setIssuer("Shopping app")
                .setClaims(extraClaims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
        logger.info("buildToken method successfully worked");
        return token;
    }


}
