package org.commerce.authenticationservice.security.jwt;

import org.commerce.authenticationservice.security.service.CustomUserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtService;
    private final CustomUserDetailService customUserDetailService;

    Logger logger = LoggerFactory.getLogger(getClass());

    public JwtAuthenticationFilter(JwtTokenService jwtService, CustomUserDetailService customUserDetailService) {
        this.jwtService = jwtService;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("doFilterInternal method started");

        final String header = request.getHeader("Authorization");
        final String jwt;
        final String userName;

        if (header == null || !header.startsWith("Bearer ")) {
            logger.warn("Header is missing");
            filterChain.doFilter(request, response);
            return;
        }
        jwt = header.substring(7);
        logger.info("Authorization request in header");
        userName = jwtService.findUserName(jwt);

        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailService.loadUserByUsername(userName);
            if (jwtService.tokenControl(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                logger.info("User: {}, authenticated", userDetails.getUsername());
            }
        }
        logger.info("doFilterInternal method successfully worked");
        filterChain.doFilter(request, response);
    }

}

