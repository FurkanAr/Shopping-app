package org.commerce.authenticationservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.commerce.authenticationservice.constants.Constant;
import org.commerce.authenticationservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtTokenService {

    Logger logger = LoggerFactory.getLogger(getClass());

    public String findUserName(String token) {
        logger.info("findUserName method started");

        String userName = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        logger.info("User found with token: {}", userName);
        logger.info("findUserName method successfully worked");
        return userName;
    }

    private Key getKey() {
        logger.info("getKey method started");
        byte[] key = Decoders.BASE64.decode(Constant.Jwt.SECRET_KEY);
        logger.info("getKey method successfully worked");
        return Keys.hmacShaKeyFor(key);
    }

    public String generateToken(User user) {
        logger.info("generateToken method started");
        String token = Jwts.builder()
                .setSubject(user.getUserName())
                .setIssuer("Blog.app")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + Constant.Jwt.EXPIRES_IN))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
        logger.info("generateToken method successfully worked");
        return token;
    }

    public 	boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | IllegalArgumentException |
                 UnsupportedJwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody().getExpiration();
        return expiration.before(new Date());
    }
}
