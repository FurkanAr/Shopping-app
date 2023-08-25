package org.commerce.authenticationservice.controller.advice;

import org.commerce.authenticationservice.exception.response.ExceptionResponse;
import org.commerce.authenticationservice.exception.role.RoleCannotFoundException;
import org.commerce.authenticationservice.exception.user.UserEmailAlreadyInUseException;
import org.commerce.authenticationservice.exception.user.UserNameAlreadyInUseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handle(MethodArgumentNotValidException exception, ServletWebRequest request){
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError:: getDefaultMessage).collect(Collectors.toList());
        return ResponseEntity
                .ok(new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        errors,
                        request.getDescription(false)));
    }

    @ExceptionHandler(UserEmailAlreadyInUseException.class)
    public ResponseEntity<ExceptionResponse> handle(UserEmailAlreadyInUseException exception, ServletWebRequest request) {
        return ResponseEntity
                .ok(new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        Collections.singletonList(exception.getMessage()),
                        request.getDescription(true)));
    }

    @ExceptionHandler(UserNameAlreadyInUseException.class)
    public ResponseEntity<ExceptionResponse> handle(UserNameAlreadyInUseException exception, ServletWebRequest request) {
        return ResponseEntity
                .ok(new ExceptionResponse(
                        HttpStatus.BAD_REQUEST,
                        Collections.singletonList(exception.getMessage()),
                        request.getDescription(true)));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(UsernameNotFoundException exception, ServletWebRequest request) {
        return ResponseEntity
                .ok(new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        Collections.singletonList(exception.getMessage()),
                        request.getDescription(true)));
    }

    @ExceptionHandler(RoleCannotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(RoleCannotFoundException exception, ServletWebRequest request) {
        return ResponseEntity
                .ok(new ExceptionResponse(
                        HttpStatus.NOT_FOUND,
                        Collections.singletonList(exception.getMessage()),
                        request.getDescription(true)));
    }

}
