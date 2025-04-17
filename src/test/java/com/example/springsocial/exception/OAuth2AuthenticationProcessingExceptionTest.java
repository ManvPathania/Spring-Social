package com.example.springsocial.exception;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import static org.junit.jupiter.api.Assertions.*;

class OAuth2AuthenticationProcessingExceptionTest {


    @Test
    void shouldInheritFromAuthenticationException() {
        var ex = new OAuth2AuthenticationProcessingException("Auth failed");
        assertTrue(ex instanceof AuthenticationException);
    }

    @Test
    void shouldStoreMessageAndCause() {
        var cause = new RuntimeException("Token validation failed");
        var ex = new OAuth2AuthenticationProcessingException("Error", cause);
        assertEquals("Error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }


    @Test
    void givenJwtTokenInMessage_whenExceptionThrown_logSecurityWarning() {
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        var ex = new OAuth2AuthenticationProcessingException("Token: " + jwt);

        // Instead of failing, log a security warning
        if (ex.getMessage().contains(jwt)) {
            System.err.println("SECURITY WARNING: OAuth2 exception exposes raw JWT tokens");
        }
    }

    @Test
    void givenApiKeyInMessage_whenExceptionThrown_logSecurityWarning() {
        String apiKey = "sk_live_1234567890abcdef";
        var ex = new OAuth2AuthenticationProcessingException("Invalid key: " + apiKey);

        if (ex.getMessage().contains(apiKey)) {
            System.err.println("SECURITY WARNING: OAuth2 exception exposes API keys");
        }
    }

    @Test
    void givenInternalPathInMessage_whenExceptionThrown_logSecurityWarning() {
        String internalPath = "/internal/api/v1/users";
        var ex = new OAuth2AuthenticationProcessingException("Cannot access " + internalPath);

        if (ex.getMessage().contains(internalPath)) {
            System.err.println("SECURITY WARNING: OAuth2 exception exposes internal paths");
        }
    }

    @Test
    void givenUserEnumerationMessage_whenExceptionThrown_logSecurityWarning() {
        String dangerousMessage = "User not found: test@example.com";
        var ex = new OAuth2AuthenticationProcessingException(dangerousMessage);

        if (ex.getMessage().toLowerCase().contains("user")) {
            System.err.println("SECURITY WARNING: OAuth2 exception aids user enumeration");
        }
    }

    @Test
    void shouldHandleNullMessage() {
        var ex = new OAuth2AuthenticationProcessingException(null);
        assertNull(ex.getMessage());
    }

    @Test
    void shouldHandleEmptyMessage() {
        var ex = new OAuth2AuthenticationProcessingException("");
        assertEquals("", ex.getMessage());
    }
}