package com.example.springsocial.security.oauth2;

import com.example.springsocial.util.CookieUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.Cookie;

import java.util.Optional;

import static com.example.springsocial.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationFailureHandler failureHandler;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository;

    @Mock
    private AuthenticationException exception;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        failureHandler.httpCookieOAuth2AuthorizationRequestRepository = authRequestRepository;
    }

    @Test
    void testOnAuthenticationFailure_withRedirectCookie() throws Exception {
        String redirectUri = "http://localhost:3000/login";
        Cookie cookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
        request.setCookies(cookie);

        try (MockedStatic<CookieUtils> mockedCookieUtils = mockStatic(CookieUtils.class)) {
            mockedCookieUtils.when(() -> CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME))
                    .thenReturn(Optional.of(cookie));

            when(exception.getLocalizedMessage()).thenReturn("InvalidUser");

            RedirectStrategy mockRedirectStrategy = spy(new DefaultRedirectStrategy());
            failureHandler.setRedirectStrategy(mockRedirectStrategy);

            failureHandler.onAuthenticationFailure(request, response, exception);

            verify(authRequestRepository).removeAuthorizationRequestCookies(request, response);
            verify(mockRedirectStrategy).sendRedirect(eq(request), eq(response), contains("http://localhost:3000/login?error=InvalidUser"));
        }
    }

    @Test
    void testOnAuthenticationFailure_withoutRedirectCookie() throws Exception {
        try (MockedStatic<CookieUtils> mockedCookieUtils = mockStatic(CookieUtils.class)) {
            mockedCookieUtils.when(() -> CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME))
                    .thenReturn(Optional.empty());

            when(exception.getLocalizedMessage()).thenReturn("Failure");

            RedirectStrategy mockRedirectStrategy = spy(new DefaultRedirectStrategy());
            failureHandler.setRedirectStrategy(mockRedirectStrategy);

            failureHandler.onAuthenticationFailure(request, response, exception);

            verify(authRequestRepository).removeAuthorizationRequestCookies(request, response);
            verify(mockRedirectStrategy).sendRedirect(eq(request), eq(response), contains("/?error=Failure"));
        }
    }
}
