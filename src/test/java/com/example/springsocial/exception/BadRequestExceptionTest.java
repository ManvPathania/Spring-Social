package com.example.springsocial.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    // Test constructor with message only
    @Test
    void whenCreatedWithMessage_thenHasCorrectMessageAndStatus() {
        String errorMessage = "Invalid request parameters";
        BadRequestException exception = new BadRequestException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
        
        // Verify response status annotation
        ResponseStatus annotation = exception.getClass().getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.BAD_REQUEST, annotation.value());
    }

    // Test constructor with empty message
    @Test
    void whenCreatedWithEmptyMessage_thenHasEmptyMessage() {
        BadRequestException exception = new BadRequestException("");
        
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    // Test constructor with null message
    @Test
    void whenCreatedWithNullMessage_thenHasNullMessage() {
        BadRequestException exception = new BadRequestException(null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    // Test constructor with message and cause
    @Test
    void whenCreatedWithMessageAndCause_thenHasBoth() {
        String errorMessage = "Validation failed";
        Throwable cause = new IllegalArgumentException("Invalid input");
        BadRequestException exception = new BadRequestException(errorMessage, cause);
        
        assertEquals(errorMessage, exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    // Test constructor with null cause
    @Test
    void whenCreatedWithMessageAndNullCause_thenHasMessageOnly() {
        String errorMessage = "Missing required field";
        BadRequestException exception = new BadRequestException(errorMessage, null);
        
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    // Test constructor with null message and null cause
    @Test
    void whenCreatedWithNullMessageAndNullCause_thenHasNulls() {
        BadRequestException exception = new BadRequestException(null, null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    // Test with very long message
    @Test
    void whenCreatedWithLongMessage_thenHandlesCorrectly() {
        String longMessage = "a".repeat(10000);
        BadRequestException exception = new BadRequestException(longMessage);
        
        assertEquals(longMessage, exception.getMessage());
    }

    // Test with special characters in message
    @Test
    void whenCreatedWithSpecialCharacters_thenPreservesMessage() {
        String message = "Error: 无效请求 (Invalid request)";
        BadRequestException exception = new BadRequestException(message);
        
        assertEquals(message, exception.getMessage());
    }

    // Test exception chaining
    @Test
    void whenChainingExceptions_thenProperlyNested() {
        NullPointerException rootCause = new NullPointerException();
        IllegalArgumentException intermediate = new IllegalArgumentException("Bad input", rootCause);
        BadRequestException topLevel = new BadRequestException("Request failed", intermediate);
        
        assertEquals("Request failed", topLevel.getMessage());
        assertSame(intermediate, topLevel.getCause());
        assertSame(rootCause, topLevel.getCause().getCause());
    }

    // Test ResponseStatus annotation
    @Test
    void whenExceptionThrown_thenHasBadRequestStatus() {
        ResponseStatus annotation = BadRequestException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.BAD_REQUEST, annotation.value());
        assertEquals("", annotation.reason());
        assertEquals("500 INTERNAL_SERVER_ERROR", annotation.code().toString());
    }
}