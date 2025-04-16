package com.example.springsocial.security.oauth2;

import com.example.springsocial.config.AppProperties;
import com.example.springsocial.exception.BadRequestException;
import com.example.springsocial.security.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;
import java.util.List;

import static com.example.springsocial.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.OAuth2 oauth2;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(appProperties.getOauth2()).thenReturn(oauth2);
        successHandler = new OAuth2AuthenticationSuccessHandler(tokenProvider, appProperties, authRequestRepository);
    }

    @Test
    void testDetermineTargetUrl_withValidRedirectUri() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String redirectUri = "http://localhost:8080/oauth2/redirect";
        Cookie cookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
        request.setCookies(cookie);

        when(oauth2.getAuthorizedRedirectUris()).thenReturn(List.of("http://localhost:8080"));
        when(tokenProvider.createToken(authentication)).thenReturn("mocked-token");

        String targetUrl = successHandler.determineTargetUrl(request, response, authentication);

        assertTrue(targetUrl.startsWith(redirectUri));
        assertTrue(targetUrl.contains("token=mocked-token"));
    }

    @Test
    void testDetermineTargetUrl_withInvalidRedirectUri_throwsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Cookie cookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, "http://malicious.com");
        request.setCookies(cookie);

        when(oauth2.getAuthorizedRedirectUris()).thenReturn(List.of("http://localhost:8080"));

        assertThrows(BadRequestException.class, () ->
                successHandler.determineTargetUrl(request, response, authentication));
    }

    @Test
    void testIsAuthorizedRedirectUri_valid() {
        when(oauth2.getAuthorizedRedirectUris()).thenReturn(List.of("http://localhost:8080"));

        boolean result = successHandler.isAuthorizedRedirectUri("http://localhost:8080/path");

        assertTrue(result);
    }

    @Test
    void testIsAuthorizedRedirectUri_invalid() {
        when(oauth2.getAuthorizedRedirectUris()).thenReturn(List.of("http://localhost:8080"));

        boolean result = successHandler.isAuthorizedRedirectUri("http://malicious.com");

        assertFalse(result);
    }

    @Test
    void testClearAuthenticationAttributes_callsRemoveCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.clearAuthenticationAttributes(request, response);

        verify(authRequestRepository, times(1)).removeAuthorizationRequestCookies(request, response);
    }
}
