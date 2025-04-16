package com.example.springsocial.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CookieUtilsTest {

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void testGetCookie() {
        Cookie cookie = new Cookie("testCookie", "testValue");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<Cookie> result = CookieUtils.getCookie(request, "testCookie");

        assertTrue(result.isPresent());
        assertEquals("testValue", result.get().getValue());
    }

    @Test
    public void testGetCookie_NotFound() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        Optional<Cookie> result = CookieUtils.getCookie(request, "nonExistentCookie");

        assertFalse(result.isPresent());
    }

    @Test
    public void testAddCookie() {
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        CookieUtils.addCookie(response, "newCookie", "newValue", 3600);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie addedCookie = cookieCaptor.getValue();

        assertEquals("newCookie", addedCookie.getName());
        assertEquals("newValue", addedCookie.getValue());
        assertEquals(3600, addedCookie.getMaxAge());
        assertTrue(addedCookie.isHttpOnly());
    }

    @Test
    public void testDeleteCookie() {
        Cookie cookie = new Cookie("deleteCookie", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        CookieUtils.deleteCookie(request, response, "deleteCookie");

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie deletedCookie = cookieCaptor.getValue();

        assertEquals("deleteCookie", deletedCookie.getName());
        assertEquals("", deletedCookie.getValue());
        assertEquals(0, deletedCookie.getMaxAge());
    }

    @Test
    public void testSerializeAndDeserialize() {
        String testString = "test";
        String serialized = CookieUtils.serialize(testString);
        String deserialized = CookieUtils.deserialize(new Cookie("test", serialized), String.class);

        assertEquals(testString, deserialized);
    }
}