package com.example.springsocial.security.oauth2;

import com.example.springsocial.exception.OAuth2AuthenticationProcessingException;
import com.example.springsocial.model.AuthProvider;
import com.example.springsocial.model.User;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.UserPrincipal;
import com.example.springsocial.security.oauth2.user.OAuth2UserInfo;
import com.example.springsocial.security.oauth2.user.OAuth2UserInfoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2UserRequest oAuth2UserRequest;
    private Map<String, Object> attributes;
    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        // Setup test OAuth2UserRequest using proper builder pattern
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("redirect-uri")
                .scope("scope")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("client-name")
                .build();

        // Create a mock access token
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        // Setup test attributes
        attributes = new HashMap<>();
        attributes.put("sub", "12345");
        attributes.put("name", "Test User");
        attributes.put("email", "test@example.com");
        attributes.put("picture", "http://example.com/pic.jpg");

        // Setup test OAuth2User
        oAuth2User = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                Collections.singleton(new OAuth2UserAuthority(attributes)),
                attributes,
                "sub"
        );
    }

    @Test
    void processOAuth2User_ShouldThrowException_WhenEmailMissing() {
        // Arrange
        attributes.remove("email");
        oAuth2User = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                Collections.singleton(new OAuth2UserAuthority(attributes)),
                attributes,
                "sub"
        );

        // Act & Assert
        assertThrows(OAuth2AuthenticationProcessingException.class, () -> {
            customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);
        });
    }

    @Test
    void processOAuth2User_ShouldRegisterNewUser_WhenUserNotExists() {
        // Arrange
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setProvider(AuthProvider.google);
        when(userRepository.save(any())).thenReturn(savedUser);

        // Act
        OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any());
        assertEquals("test@example.com", ((UserPrincipal) result).getEmail());
    }

    @Test
    void processOAuth2User_ShouldUpdateExistingUser_WhenUserExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setProvider(AuthProvider.google);
        existingUser.setName("Old Name");
        existingUser.setImageUrl("http://example.com/old-pic.jpg");

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);

        // Act
        OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(existingUser);
        assertEquals("Test User", existingUser.getName());
        assertEquals("http://example.com/pic.jpg", existingUser.getImageUrl());
    }

    @Test
    void processOAuth2User_ShouldThrowException_WhenProviderMismatch() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setProvider(AuthProvider.facebook); // Different from request

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        OAuth2AuthenticationProcessingException exception = assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User)
        );

        assertTrue(exception.getMessage().contains("Looks like you're signed up with facebook account"));
    }

    @Test
    void processOAuth2User_ShouldThrowException_WhenUnsupportedProvider() {
        // Arrange
        ClientRegistration unsupportedClientRegistration = ClientRegistration.withRegistrationId("unsupported")
                .clientId("client-id")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("redirect-uri")
                .scope("scope")
                .authorizationUri("https://unsupported.com/auth")
                .tokenUri("https://unsupported.com/token")
                .userInfoUri("https://unsupported.com/userinfo")
                .userNameAttributeName("id")
                .clientName("client-name")
                .build();

        // Create a mock access token for the unsupported request
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        OAuth2UserRequest unsupportedRequest = new OAuth2UserRequest(unsupportedClientRegistration, accessToken);

        // Act & Assert
        OAuth2AuthenticationProcessingException exception = assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> customOAuth2UserService.processOAuth2User(unsupportedRequest, oAuth2User)
        );

        assertTrue(exception.getMessage().contains("Login with unsupported is not supported yet"));
    }

    @Test
    void registerNewUser_ShouldCreateNewUserWithCorrectDetails() {
        // Arrange
        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setProvider(AuthProvider.google);
        when(userRepository.save(any())).thenReturn(savedUser);

        // Create a test OAuth2UserInfo using the attributes map
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

        // Act
        User result = customOAuth2UserService.registerNewUser(oAuth2UserRequest, userInfo);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(AuthProvider.google, result.getProvider());
        verify(userRepository).save(any());
    }

    @Test
    void updateExistingUser_ShouldUpdateNameAndImageUrl() {
        // Arrange
        User existingUser = new User();
        existingUser.setName("Old Name");
        existingUser.setImageUrl("http://example.com/old-pic.jpg");

        when(userRepository.save(any())).thenReturn(existingUser);

        // Create a test OAuth2UserInfo using the attributes map
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

        // Act
        User result = customOAuth2UserService.updateExistingUser(existingUser, userInfo);

        // Assert
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("http://example.com/pic.jpg", result.getImageUrl());
        verify(userRepository).save(existingUser);
    }
}