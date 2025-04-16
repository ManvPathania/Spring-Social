package com.example.springsocial.security.oauth2.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class FacebookOAuth2UserInfoTest {

    @Test
    public void testGetId() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "123456789");
        FacebookOAuth2UserInfo userInfo = new FacebookOAuth2UserInfo(attributes);
        assertEquals("123456789", userInfo.getId());
    }

    @Test
    public void testGetName() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John Doe");
        FacebookOAuth2UserInfo userInfo = new FacebookOAuth2UserInfo(attributes);
        assertEquals("John Doe", userInfo.getName());
    }

    @Test
    public void testGetEmail() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "johndoe@example.com");
        FacebookOAuth2UserInfo userInfo = new FacebookOAuth2UserInfo(attributes);
        assertEquals("johndoe@example.com", userInfo.getEmail());
    }

    @Test
    public void testGetImageUrl() {
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> pictureData = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("url", "http://example.com/image.jpg");
        pictureData.put("data", data);
        attributes.put("picture", pictureData);
        FacebookOAuth2UserInfo userInfo = new FacebookOAuth2UserInfo(attributes);
        assertEquals("http://example.com/image.jpg", userInfo.getImageUrl());
    }

    @Test
    public void testGetImageUrlWhenNoPicture() {
        Map<String, Object> attributes = new HashMap<>();
        FacebookOAuth2UserInfo userInfo = new FacebookOAuth2UserInfo(attributes);
        assertNull(userInfo.getImageUrl());
    }
}