package com.example.springsocial.exception;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import static org.junit.jupiter.api.Assertions.*;

class OAuth2AuthenticationProcessingExceptionTest {

    // --- CORE FAILURE SCENARIOS ---
    @Test
    void shouldHandleInvalidCredentialsFailure() {
        String errorMessage = "Invalid OAuth2 credentials";
        OAuth2AuthenticationProcessingException exception = 
            new OAuth2AuthenticationProcessingException(errorMessage);
        
        assertAll(
            () -> assertEquals(errorMessage, exception.getMessage()),
            () -> assertNull(exception.getCause()),
            () -> assertTrue(exception instanceof AuthenticationException)
        );
    }

    @Test
    void shouldHandleExpiredSessionFailure() {
        Throwable cause = new IllegalStateException("Session expired");
        OAuth2AuthenticationProcessingException exception = 
            new OAuth2AuthenticationProcessingException("OAuth2 session expired", cause);
        
        assertAll(
            () -> assertEquals("OAuth2 session expired", exception.getMessage()),
            () -> assertEquals(cause, exception.getCause())
        );
    }

    // --- ERROR PROPAGATION TESTS ---
    @Test
    void shouldPreserveOriginalErrorDetails() {
        Throwable rootCause = new RuntimeException("Token validation failed");
        OAuth2AuthenticationProcessingException exception = 
            new OAuth2AuthenticationProcessingException("OAuth2 error", rootCause);
        
        assertEquals(rootCause, exception.getCause());
    }

    // --- EDGE CASES ---
    @Test
    void shouldHandleNullErrorMessage() {
        assertDoesNotThrow(() -> 
            new OAuth2AuthenticationProcessingException(null)
        );
    }

    @Test
    void shouldHandleEmptyErrorMessage() {
        OAuth2AuthenticationProcessingException exception = 
            new OAuth2AuthenticationProcessingException("");
        assertEquals("", exception.getMessage());
    }

    // --- SECURITY AWARENESS ---
    @Test
    void shouldWarnAboutTokenInErrorMessage() {
        String jwt = "eyJhbGciOi...";
        String message = "Invalid token: " + jwt;
        OAuth2AuthenticationProcessingException exception = 
            new OAuth2AuthenticationProcessingException(message);
        
        if (exception.getMessage().contains(jwt)) {
            System.err.println("SECURITY RECOMMENDATION: Redact tokens in OAuth2 error messages");
        }
    }
}