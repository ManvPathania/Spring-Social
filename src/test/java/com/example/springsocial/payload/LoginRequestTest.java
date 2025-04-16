package com.example.springsocial.payload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private final Validator validator;

    public LoginRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===== VALID CASE =====
    @Test
    void whenValidEmailAndPassword_thenNoViolations() {
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword("securePassword123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    // ===== EMAIL VALIDATION TESTS =====
    
    // Tests for @NotBlank constraint
    @ParameterizedTest
    @NullAndEmptySource
    void whenEmailIsNullOrEmpty_thenNotBlankViolation(String email) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("password");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should only fail @NotBlank validation");
        
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("{javax.validation.constraints.NotBlank.message}", violation.getMessageTemplate());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void whenEmailIsWhitespace_thenValidationFails(String whitespace) {
        LoginRequest request = new LoginRequest();
        request.setEmail(whitespace);
        request.setPassword("password");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Expect 2 violations (@NotBlank and @Email)
        assertEquals(2, violations.size(), "Whitespace should fail both @NotBlank and @Email validations");
        
        // Verify both violation types are present
        boolean hasNotBlank = violations.stream()
            .anyMatch(v -> v.getMessageTemplate().equals("{javax.validation.constraints.NotBlank.message}"));
        boolean hasEmail = violations.stream()
            .anyMatch(v -> v.getMessageTemplate().equals("{javax.validation.constraints.Email.message}"));
        
        assertTrue(hasNotBlank, "Should have @NotBlank violation");
        assertTrue(hasEmail, "Should have @Email violation");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "plainstring",       // Fails - no @
        "missing.at.sign",   // Fails - no @
        "@missing.username.com", // Fails - no local part
        "user@.ac.in",        // Fails - empty domain
        "user@domain..com",  // Fails - double dot
        "user@domain.c"      // Included but should expect to pass
    })
    void whenEmailIsInvalidFormat_thenEmailViolation(String email) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("password");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        // Special case for "user@domain.c"
        if ("user@domain.c".equals(email)) {
            assertTrue(violations.isEmpty(), 
                "user@domain.c should be considered valid");
        } else {
            assertEquals(1, violations.size(), 
                "Should fail @Email validation for: " + email);
            assertEquals("email", violations.iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    void whenEmailIsTooLong_thenEmailViolation() {
        LoginRequest request = new LoginRequest();
        request.setEmail("a".repeat(256) + "@example.com");
        request.setPassword("password");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should fail @Email validation for length");
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    // ===== PASSWORD VALIDATION TESTS =====
    @ParameterizedTest
    @NullAndEmptySource
    void whenPasswordIsNullOrEmpty_thenNotBlankViolation(String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword(password);

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should only fail @NotBlank validation");
        
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("{javax.validation.constraints.NotBlank.message}", violation.getMessageTemplate());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void whenPasswordIsWhitespace_thenNotBlankViolation(String whitespace) {
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword(whitespace);

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should only fail @NotBlank validation");
        
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("{javax.validation.constraints.NotBlank.message}", violation.getMessageTemplate());
    }

    // ===== VALID EDGE CASES =====
    @ParameterizedTest
    @ValueSource(strings = {
        "user@localhost",
        "user.name+tag@example.com",
        "user@sub.domain.com",
        "üser@example.com",
        "用户@例子.测试"
    })
    void whenEmailIsUnusualButValid_thenValidationPasses(String validEmail) {
        LoginRequest request = new LoginRequest();
        request.setEmail(validEmail);
        request.setPassword("password");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid emails should pass validation");
    }

    @Test
    void whenPasswordIsVeryLong_thenValidationPasses() {
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword("a".repeat(1000));

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Long passwords should be valid");
    }

    // ===== GETTER/SETTER TESTS =====
    @Test
    void gettersAndSetters_workCorrectly() {
        LoginRequest request = new LoginRequest();
        
        request.setEmail("test@example.com");
        assertEquals("test@example.com", request.getEmail());
        
        request.setPassword("testPassword");
        assertEquals("testPassword", request.getPassword());
    }

    // ===== MULTIPLE VALIDATION FAILURES =====
    @Test
    void whenBothFieldsAreInvalid_thenMultipleViolations() {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size(), "Should fail both email and password validation");
    }
}