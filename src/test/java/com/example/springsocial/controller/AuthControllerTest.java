package com.example.springsocial.controller;

import com.example.springsocial.exception.BadRequestException;
import com.example.springsocial.model.User;
import com.example.springsocial.payload.ApiResponse;
import com.example.springsocial.payload.AuthResponse;
import com.example.springsocial.payload.LoginRequest;
import com.example.springsocial.payload.SignUpRequest;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateUser_success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.createToken(authentication)).thenReturn("mocked-token");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertNotNull(authResponse);
        assertEquals("mocked-token", authResponse.getAccessToken());
        assertEquals("Bearer", authResponse.getTokenType());
    }

    @Test
    void testRegisterUser_emailAlreadyExists_throwsException() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("exists@example.com");

        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authController.registerUser(signUpRequest));
    }

    @Test
    void testRegisterUser_success() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setName("New User");
        signUpRequest.setEmail("new@example.com");
        signUpRequest.setPassword("rawpassword");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawpassword")).thenReturn("encodedpassword");

        User savedUser = new User();
        savedUser.setId(100L);
        savedUser.setName("New User");
        savedUser.setEmail("new@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        RequestAttributes requestAttributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(201, response.getStatusCodeValue());

        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("User registered successfully@", body.getMessage());

        URI expectedUri = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(savedUser.getId()).toUri();

        assertEquals(expectedUri, response.getHeaders().getLocation());

        RequestContextHolder.resetRequestAttributes();
    }
}
