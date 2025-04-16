package com.example.springsocial.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    // ===== BASIC FUNCTIONALITY TESTS =====
    @Test
    void whenCreatedWithValidArgs_thenConstructsProperly() {
        String resource = "User";
        String field = "id";
        Object value = 123L;
        
        ResourceNotFoundException ex = new ResourceNotFoundException(resource, field, value);
        
        assertEquals(resource, ex.getResourceName());
        assertEquals(field, ex.getFieldName());
        assertEquals(value, ex.getFieldValue());
        assertEquals("User not found with id : '123'", ex.getMessage());
    }

    // ===== NULL VALUE TESTS =====
    @Test
    void whenCreatedWithNullFieldValue_thenHandlesGracefully() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order", "number", null);
        
        assertEquals("Order", ex.getResourceName());
        assertEquals("number", ex.getFieldName());
        assertNull(ex.getFieldValue());
        assertEquals("Order not found with number : 'null'", ex.getMessage());
    }

    @Test
    void whenCreatedWithNullResourceName_thenHandlesGracefully() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null, "email", "test@example.com");
        
        assertNull(ex.getResourceName());
        assertEquals("email", ex.getFieldName());
        assertEquals("test@example.com", ex.getFieldValue());
        assertEquals("null not found with email : 'test@example.com'", ex.getMessage());
    }

    @Test
    void whenCreatedWithNullFieldName_thenHandlesGracefully() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product", null, "SKU-123");
        
        assertEquals("Product", ex.getResourceName());
        assertNull(ex.getFieldName());
        assertEquals("SKU-123", ex.getFieldValue());
        assertEquals("Product not found with null : 'SKU-123'", ex.getMessage());
    }

    // ===== EMPTY STRING TESTS =====
    @Test
    void whenCreatedWithEmptyResourceName_thenHandlesGracefully() {
        ResourceNotFoundException ex = new ResourceNotFoundException("", "code", "ABC123");
        
        assertEquals("", ex.getResourceName());
        assertEquals("code", ex.getFieldName());
        assertEquals("ABC123", ex.getFieldValue());
        assertEquals(" not found with code : 'ABC123'", ex.getMessage());
    }

    @Test
    void whenCreatedWithEmptyFieldName_thenHandlesGracefully() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Account", "", "user123");
        
        assertEquals("Account", ex.getResourceName());
        assertEquals("", ex.getFieldName());
        assertEquals("user123", ex.getFieldValue());
        assertEquals("Account not found with  : 'user123'", ex.getMessage());
    }

    // ===== SPECIAL CHARACTER TESTS =====
    @Test
    void whenCreatedWithSpecialChars_thenHandlesProperly() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Üser", "émail", "测试@例子.com");
        
        assertEquals("Üser", ex.getResourceName());
        assertEquals("émail", ex.getFieldName());
        assertEquals("测试@例子.com", ex.getFieldValue());
        assertEquals("Üser not found with émail : '测试@例子.com'", ex.getMessage());
    }

    // ===== NUMERIC FIELD VALUE TESTS =====
    @Test
    void whenCreatedWithNumericFieldValue_thenHandlesProperly() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Invoice", "number", 1001);
        
        assertEquals("Invoice", ex.getResourceName());
        assertEquals("number", ex.getFieldName());
        assertEquals(1001, ex.getFieldValue());
        assertEquals("Invoice not found with number : '1001'", ex.getMessage());
    }

    // ===== OBJECT FIELD VALUE TESTS =====
    @Test
    void whenCreatedWithObjectFieldValue_thenHandlesProperly() {
        Object complexValue = new Object() {
            @Override
            public String toString() {
                return "COMPLEX_OBJ";
            }
        };
        
        ResourceNotFoundException ex = new ResourceNotFoundException("Config", "setting", complexValue);
        
        assertEquals("Config", ex.getResourceName());
        assertEquals("setting", ex.getFieldName());
        assertEquals(complexValue, ex.getFieldValue());
        assertEquals("Config not found with setting : 'COMPLEX_OBJ'", ex.getMessage());
    }

    // ===== ANNOTATION TESTS =====
    @Test
    void whenExceptionThrown_thenHasNotFoundStatus() {
        ResponseStatus annotation = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    // ===== LONG STRING TESTS =====
    @Test
    void whenCreatedWithLongStrings_thenHandlesProperly() {
        String longResource = "A".repeat(1000);
        String longField = "B".repeat(1000);
        String longValue = "C".repeat(1000);
        
        ResourceNotFoundException ex = new ResourceNotFoundException(longResource, longField, longValue);
        
        assertEquals(longResource, ex.getResourceName());
        assertEquals(longField, ex.getFieldName());
        assertEquals(longValue, ex.getFieldValue());
        assertTrue(ex.getMessage().contains(longResource));
        assertTrue(ex.getMessage().contains(longField));
        assertTrue(ex.getMessage().contains(longValue));
    }

    // ===== ALL NULLS TEST =====
    @Test
    void whenCreatedWithAllNulls_thenHandlesGracefully() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null, null, null);
        
        assertNull(ex.getResourceName());
        assertNull(ex.getFieldName());
        assertNull(ex.getFieldValue());
        assertEquals("null not found with null : 'null'", ex.getMessage());
    }
}