package com.example.springsocial.security.oauth2;

import com.example.springsocial.util.CookieUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.Cookie;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @InjectMocks
    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private OAuth2AuthorizationRequest authorizationRequest;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .clientId("client123")
                .authorizationUri("https://example.com/oauth/authorize")
                .redirectUri("https://app.example.com/callback")
                .scopes(java.util.Collections.singleton("read"))
                .state("state123")
                .build();
    }

    @Test
    void loadAuthorizationRequest_WhenNoCookiePresent_ReturnsNull() {
        try (MockedStatic<CookieUtils> cookieUtils = mockStatic(CookieUtils.class)) {
            cookieUtils.when(() -> CookieUtils.getCookie(any(), anyString()))
                    .thenReturn(Optional.empty());

            assertNull(repository.loadAuthorizationRequest(request));
        }
    }

    @Test
    void loadAuthorizationRequest_WhenValidCookie_ReturnsRequest() {
        try (MockedStatic<CookieUtils> cookieUtils = mockStatic(CookieUtils.class)) {
            Cookie mockCookie = new Cookie("test", "value");
            cookieUtils.when(() -> CookieUtils.getCookie(eq(request), 
                    eq(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)))
                    .thenReturn(Optional.of(mockCookie));
            cookieUtils.when(() -> CookieUtils.deserialize(eq(mockCookie), eq(OAuth2AuthorizationRequest.class)))
                    .thenReturn(authorizationRequest);

            assertEquals(authorizationRequest, repository.loadAuthorizationRequest(request));
        }
    }

    @Test
    void saveAuthorizationRequest_WhenNullRequest_DeletesCookies() {
        try (MockedStatic<CookieUtils> cookieUtils = mockStatic(CookieUtils.class)) {
            repository.saveAuthorizationRequest(null, request, response);

            cookieUtils.verify(() -> CookieUtils.deleteCookie(
                    eq(request), 
                    eq(response), 
                    eq(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)));
            
            cookieUtils.verify(() -> CookieUtils.deleteCookie(
                    eq(request), 
                    eq(response), 
                    eq(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)));
        }
    }

    @Test
    void saveAuthorizationRequest_WhenValidRequest_SavesAuthCookie() {
        try (MockedStatic<CookieUtils> cookieUtils = mockStatic(CookieUtils.class)) {
            String serialized = "serialized-request";
            cookieUtils.when(() -> CookieUtils.serialize(any(OAuth2AuthorizationRequest.class)))
                    .thenReturn(serialized);

            repository.saveAuthorizationRequest(authorizationRequest, request, response);

            cookieUtils.verify(() -> CookieUtils.addCookie(
                    eq(response),
                    eq(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME),
                    eq(serialized),
                    eq(180)));
        }
    }

    @Test
    void saveAuthorizationRequest_WhenRedirectUriParam_SavesRedirectCookie() {
        try (MockedStatic<CookieUtils> cookieUtils = mockStatic(CookieUtils.class)) {
            String redirectUri = "https://custom-redirect.com";
            request.setParameter(
                    HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME, 
                    redirectUri);
            
            String serialized = "serialized-request";
            cookieUtils.when(() -> CookieUtils.serialize(any(OAuth2AuthorizationRequest.class)))
                    .thenReturn(serialized);

            repository.saveAuthorizationRequest(authorizationRequest, request, response);

            cookieUtils.verify(() -> CookieUtils.addCookie(
                    eq(response),
                    eq(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME),
                    eq(redirectUri),
                    eq(180)));
        }
    }

    @Test
    void removeAuthorizationRequest_ReturnsLoadedRequest() {
        try (MockedStatic<CookieUtils> cookieUtils = mockStatic(CookieUtils.class)) {
            Cookie mockCookie = new Cookie("test", "value");
            cookieUtils.when(() -> CookieUtils.getCookie(eq(request), anyString()))
                    .thenReturn(Optional.of(mockCookie));
            cookieUtils.when(() -> CookieUtils.deserialize(any(), any()))
                    .thenReturn(authorizationRequest);

            assertEquals(authorizationRequest, repository.removeAuthorizationRequest(request));
        }
    }

    @Test
    void removeAuthorizationRequestCookies_DeletesBothCookies() {
        try (MockedStatic<CookieUtils> cookieUtils = mockStatic(CookieUtils.class)) {
            repository.removeAuthorizationRequestCookies(request, response);

            cookieUtils.verify(() -> CookieUtils.deleteCookie(
                    eq(request), 
                    eq(response), 
                    eq(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)));
            
            cookieUtils.verify(() -> CookieUtils.deleteCookie(
                    eq(request), 
                    eq(response), 
                    eq(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)));
        }
    }
}