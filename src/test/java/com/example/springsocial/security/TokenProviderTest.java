package com.example.springsocial.security;

import com.example.springsocial.config.AppProperties;
import com.example.springsocial.model.AuthProvider;
import com.example.springsocial.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TokenProviderTest {

    private TokenProvider tokenProvider;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Auth authProperties;

    // Generate a cryptographically secure key for HS512 algorithm
    // Keys.secretKeyFor() ensures the key meets minimum size requirements
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    
    // Base64 encoded version of the secret key for storage/configuration
    private final String SECRET_KEY_STRING = Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded());
    
    // Token expiration time in milliseconds (10 seconds for testing)
    private final long EXPIRATION_TIME = 10000;

    @BeforeEach
    void setUp() {
        // Initialize mock objects
        MockitoAnnotations.openMocks(this);
        
        // Configure mock behavior:
        // When appProperties.getAuth() is called, return authProperties mock
        when(appProperties.getAuth()).thenReturn(authProperties);
        
        // When authProperties.getTokenSecret() is called, return our Base64 encoded key
        when(authProperties.getTokenSecret()).thenReturn(SECRET_KEY_STRING);
        
        // When authProperties.getTokenExpirationMsec() is called, return our test expiration time
        when(authProperties.getTokenExpirationMsec()).thenReturn(EXPIRATION_TIME);
        
        // Create the TokenProvider instance with our mock AppProperties
        tokenProvider = new TokenProvider(appProperties);
    }

    @Test
    void testCreateAndValidateToken() {
        // Create a test user
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setEmailVerified(true);
        user.setPassword("password");
        user.setProvider(AuthProvider.local);
        user.setProviderId("123");

        // Create UserPrincipal and Authentication objects
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, null);
        
        // Create JWT token
        String token = tokenProvider.createToken(authentication);

        // Verify the token is not null, valid, and contains correct user ID
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(1L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // Create an expired token (issued and expired in the past)
        String expiredToken = Jwts.builder()
                .setSubject("1")  // User ID
                .setIssuedAt(new Date(System.currentTimeMillis() - EXPIRATION_TIME * 2))
                .setExpiration(new Date(System.currentTimeMillis() - EXPIRATION_TIME))
                .signWith(SECRET_KEY)  // Sign with our secure key
                .compact();

        // Verify token validation fails for expired token
        assertFalse(tokenProvider.validateToken(expiredToken));
    }

    @Test
    void testValidateToken_InvalidSignature() {
        // Generate a different key to test invalid signatures
        SecretKey wrongKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        
        // Create token signed with wrong key
        String invalidToken = Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(wrongKey)
                .compact();

        // Verify token validation fails for token with invalid signature
        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_MalformedToken() {
        // Test with a clearly invalid token string
        String malformedToken = "invalid.jwt.token.string";
        assertFalse(tokenProvider.validateToken(malformedToken));
    }

    @Test
    void testValidateToken_EmptyToken() {
        // Test with empty token string
        assertFalse(tokenProvider.validateToken(""));
    }

    @Test
    void testValidateToken_NullToken() {
        // Test with null token
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void testGetUserIdFromToken_InvalidToken() {
        // Test that invalid token throws expected exception
        String invalidToken = "invalid.jwt.token.string";
        assertThrows(JwtException.class, () -> tokenProvider.getUserIdFromToken(invalidToken));
    }

    @Test
    void testValidateToken_EmptySecretKey() {
        // Configure mock to return empty secret key
        when(authProperties.getTokenSecret()).thenReturn("");
        
        // Verify validation fails with empty secret key
        assertFalse(tokenProvider.validateToken("ValidTokenString"));
    }

    @Test
    void testValidateToken_NullSecretKey() {
        // Configure mock to return null secret key
        when(authProperties.getTokenSecret()).thenReturn(null);
        
        // Verify validation fails with null secret key
        assertFalse(tokenProvider.validateToken("ValidTokenString"));
    }

    @Test
    void testCreateToken_NullUserPrincipal() {
        // Create authentication with null principal
        Authentication authentication = new UsernamePasswordAuthenticationToken(null, null, null);
        
        // Verify token creation throws NPE with null principal
        assertThrows(NullPointerException.class, () -> tokenProvider.createToken(authentication));
    }

    @Test
    void testGetUserIdFromToken_ValidToken() {
        // Create a valid test token
        String token = Jwts.builder()
                .setSubject("123")  // User ID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();

        // Verify we can correctly extract user ID from valid token
        Long userId = tokenProvider.getUserIdFromToken(token);
        assertEquals(123L, userId);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Create a valid test token
        String token = Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();

        // Verify validation succeeds for valid token
        assertTrue(tokenProvider.validateToken(token));
    }
}