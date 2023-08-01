package org.commerce.authenticationservice.controller.advice;

import org.commerce.authenticationservice.exception.response.ExceptionResponse;
import org.commerce.authenticationservice.exception.response.ExceptionValidatorResponse;
import org.commerce.authenticationservice.exception.user.UserEmailAlreadyInUseException;
import org.commerce.authenticationservice.exception.user.UserNameAlreadyInUseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionValidatorResponse> handle(MethodArgumentNotValidException exception, HttpServletRequest request){
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError:: getDefaultMessage).collect(Collectors.toList());
        return ResponseEntity
                .ok(new ExceptionValidatorResponse(
                        "Validation Failed",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getServletPath(),
                        errors));
    }

    @ExceptionHandler(UserEmailAlreadyInUseException.class)
    public ResponseEntity<ExceptionResponse> handle(UserEmailAlreadyInUseException exception, HttpServletRequest request) {
        return ResponseEntity
                .ok(new ExceptionResponse(exception.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getServletPath()));
    }

    @ExceptionHandler(UserNameAlreadyInUseException.class)
    public ResponseEntity<ExceptionResponse> handle(UserNameAlreadyInUseException exception, HttpServletRequest request) {
        return ResponseEntity
                .ok(new ExceptionResponse(exception.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getServletPath()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handle(UsernameNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity
                .ok(new ExceptionResponse(exception.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getServletPath()));
    }
}
