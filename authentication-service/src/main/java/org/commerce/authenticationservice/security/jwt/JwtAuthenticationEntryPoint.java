package org.commerce.authenticationservice.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commerce.authenticationservice.exception.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                authException.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getServletPath());

        PrintWriter writer = response.getWriter();
        writer.println(new ObjectMapper().writeValueAsString(exceptionResponse));

    }
}
