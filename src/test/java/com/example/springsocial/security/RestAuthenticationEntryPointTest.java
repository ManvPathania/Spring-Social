package com.example.springsocial.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private RestAuthenticationEntryPoint entryPoint;

    @Test
    void commence_ShouldSend401WithLocalizedMessage() throws Exception {
        // Given
        String expectedMessage = "Unauthorized access";
        when(authException.getLocalizedMessage()).thenReturn(expectedMessage);

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, expectedMessage);
    }

    @Test
    void commence_ShouldHandleNullLocalizedMessage() throws Exception {
        // Given
        when(authException.getLocalizedMessage()).thenReturn(null);

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, null);
    }

    @Test
    void commence_ShouldLogError() throws Exception {
        // Given
        String errorMessage = "Authentication failed";
        when(authException.getLocalizedMessage()).thenReturn(errorMessage);

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(authException).getMessage(); // Verify logger called getMessage()
    }

    @Test
    void commence_ShouldPropagateIOExceptions() throws Exception {
        // Given
        String errorMessage = "Access denied";
        when(authException.getLocalizedMessage()).thenReturn(errorMessage);
        doThrow(new IOException("Network error"))
            .when(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);

        // When/Then
        assertThrows(IOException.class, () -> 
            entryPoint.commence(request, response, authException)
        );
    }

    @Test
    void commence_ShouldCallSendErrorExactlyOnce() throws Exception {
        // Given
        String errorMessage = "Invalid credentials";
        when(authException.getLocalizedMessage()).thenReturn(errorMessage);

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(response, times(1)).sendError(anyInt(), anyString());
        verifyNoMoreInteractions(response);
    }
}