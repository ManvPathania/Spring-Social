package com.example.springsocial.security.oauth2.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class GoogleOAuth2UserInfoTest {

    @Test
    public void testGetId() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);
        assertEquals("123456789", userInfo.getId());
    }

    @Test
    public void testGetName() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John Doe");
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);
        assertEquals("John Doe", userInfo.getName());
    }

    @Test
    public void testGetEmail() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "johndoe@example.com");
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);
        assertEquals("johndoe@example.com", userInfo.getEmail());
    }

    @Test
    public void testGetImageUrl() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("picture", "http://example.com/image.jpg");
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);
        assertEquals("http://example.com/image.jpg", userInfo.getImageUrl());
    }

    @Test
    public void testGetImageUrlWhenNoPicture() {
        Map<String, Object> attributes = new HashMap<>();
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);
        assertNull(userInfo.getImageUrl());
    }
}