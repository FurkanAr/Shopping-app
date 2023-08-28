package org.commerce.authenticationservice.security.jwt;

import org.commerce.authenticationservice.repository.TokenRepository;
import org.commerce.authenticationservice.security.service.CustomUserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtTokenService jwtService;
    private final CustomUserDetailService customUserDetailService;
    private final TokenRepository tokenRepository;

    Logger logger = LoggerFactory.getLogger(getClass());

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, JwtTokenService jwtService,
                                   CustomUserDetailService customUserDetailService, TokenRepository tokenRepository) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.customUserDetailService = customUserDetailService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        logger.info("doFilterInternal method started");

        final String header = request.getHeader("Authorization");
        final String jwt;
        final String username;
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                logger.warn("Header is missing");
                filterChain.doFilter(request, response);
                return;
            }
            jwt = header.substring(7);
            logger.info("Authorization request in header");
            username = jwtService.findUserName(jwt);
            logger.info("User found with jwt: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(token -> !token.getExpired() && !token.getRevoked())
                        .orElse(false);

                if (jwtService.tokenControl(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.info("User: {}, authenticated", userDetails.getUsername());
                }
            }
            logger.info("doFilterInternal method successfully worked");
            filterChain.doFilter(request, response);
        }catch (Exception exception){
            handlerExceptionResolver.resolveException(request, response , null, exception);
        }
    }

}

