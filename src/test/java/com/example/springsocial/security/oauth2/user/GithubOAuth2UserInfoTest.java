package com.example.springsocial.security.oauth2.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GithubOAuth2UserInfoTest {

    private GithubOAuth2UserInfo githubOAuth2UserInfo;
    private Map<String, Object> attributes;

    @BeforeEach
    public void setUp() {
        attributes = new HashMap<>();
        attributes.put("id", 123456);
        attributes.put("name", "John Doe");
        attributes.put("email", "john.doe@example.com");
        attributes.put("avatar_url", "http://example.com/avatar.jpg");

        githubOAuth2UserInfo = new GithubOAuth2UserInfo(attributes);
    }

    @Test
    public void testGetId() {
        assertEquals("123456", githubOAuth2UserInfo.getId());
    }

    @Test
    public void testGetName() {
        assertEquals("John Doe", githubOAuth2UserInfo.getName());
    }

    @Test
    public void testGetEmail() {
        assertEquals("john.doe@example.com", githubOAuth2UserInfo.getEmail());
    }

    @Test
    public void testGetImageUrl() {
        assertEquals("http://example.com/avatar.jpg", githubOAuth2UserInfo.getImageUrl());
    }
}