package com.example.springsocial.security.oauth2.user;

import com.example.springsocial.exception.OAuth2AuthenticationProcessingException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OAuth2UserInfoFactoryTest {

    @Test
    public void testGetOAuth2UserInfo_Google() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "John Doe");
        attributes.put("email", "john.doe@example.com");
        attributes.put("picture", "http://example.com/john.jpg");

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

        assertTrue(userInfo instanceof GoogleOAuth2UserInfo);
        assertEquals("123456789", userInfo.getId());
        assertEquals("John Doe", userInfo.getName());
        assertEquals("john.doe@example.com", userInfo.getEmail());
        assertEquals("http://example.com/john.jpg", userInfo.getImageUrl());
    }

    @Test
    public void testGetOAuth2UserInfo_Facebook() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "987654321");
        attributes.put("name", "Jane Doe");
        attributes.put("email", "jane.doe@example.com");
        Map<String, Object> pictureData = new HashMap<>();
        pictureData.put("url", "http://example.com/jane.jpg");
        Map<String, Object> picture = new HashMap<>();
        picture.put("data", pictureData);
        attributes.put("picture", picture);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("facebook", attributes);

        assertTrue(userInfo instanceof FacebookOAuth2UserInfo);
        assertEquals("987654321", userInfo.getId());
        assertEquals("Jane Doe", userInfo.getName());
        assertEquals("jane.doe@example.com", userInfo.getEmail());
        assertEquals("http://example.com/jane.jpg", userInfo.getImageUrl());
    }

    @Test
    public void testGetOAuth2UserInfo_Github() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 11223344);
        attributes.put("name", "Jim Beam");
        attributes.put("email", "jim.beam@example.com");
        attributes.put("avatar_url", "http://example.com/jim.jpg");

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("github", attributes);

        assertTrue(userInfo instanceof GithubOAuth2UserInfo);
        assertEquals("11223344", userInfo.getId());
        assertEquals("Jim Beam", userInfo.getName());
        assertEquals("jim.beam@example.com", userInfo.getEmail());
        assertEquals("http://example.com/jim.jpg", userInfo.getImageUrl());
    }

    @Test
    public void testGetOAuth2UserInfo_UnsupportedProvider() {
        Map<String, Object> attributes = new HashMap<>();

        Exception exception = assertThrows(OAuth2AuthenticationProcessingException.class, () ->
                OAuth2UserInfoFactory.getOAuth2UserInfo("linkedin", attributes));

        String expectedMessage = "Sorry! Login with linkedin is not supported yet.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}