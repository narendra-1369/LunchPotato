package com.lunchpotato.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // Handle duplicate accounts and invalid requests
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, String>> handleIllegalArgument(
                        IllegalArgumentException exception) {

                Map<String, String> response = new HashMap<>();
                response.put("error", exception.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Handle validation errors
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidationErrors(
                        MethodArgumentNotValidException exception) {

                Map<String, String> response = new HashMap<>();

                exception.getBindingResult().getFieldErrors()
                                .forEach(error -> response.put(error.getField(), error.getDefaultMessage()));

                return ResponseEntity.badRequest().body(response);
        }

        // Handle incorrect login credentials
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Map<String, String>> handleBadCredentials(
                        BadCredentialsException exception) {

                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid email or password");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Handle unauthorized resource ownership
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Map<String, String>> handleAccessDenied(
                        AccessDeniedException exception) {

                Map<String, String> response = new HashMap<>();
                response.put("error", "You are not authorized to access this resource");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
}