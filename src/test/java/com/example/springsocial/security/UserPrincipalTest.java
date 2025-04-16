package com.example.springsocial.security;

import com.example.springsocial.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserPrincipalTest {

    private User user;
    private Map<String, Object> attributes;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");

        attributes = new HashMap<>();
        attributes.put("key", "value");
    }

    @Test
    public void testCreateUserPrincipal() {
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        assertNotNull(userPrincipal);
        assertEquals(user.getId(), userPrincipal.getId());
        assertEquals(user.getEmail(), userPrincipal.getEmail());
        assertEquals(user.getPassword(), userPrincipal.getPassword());

        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    public void testCreateUserPrincipalWithAttributes() {
        UserPrincipal userPrincipal = UserPrincipal.create(user, attributes);

        assertNotNull(userPrincipal);
        assertEquals(user.getId(), userPrincipal.getId());
        assertEquals(user.getEmail(), userPrincipal.getEmail());
        assertEquals(user.getPassword(), userPrincipal.getPassword());
        assertEquals(attributes, userPrincipal.getAttributes());
    }

    @Test
    public void testUserPrincipalMethods() {
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        assertTrue(userPrincipal.isAccountNonExpired());
        assertTrue(userPrincipal.isAccountNonLocked());
        assertTrue(userPrincipal.isCredentialsNonExpired());
        assertTrue(userPrincipal.isEnabled());
        assertEquals(user.getEmail(), userPrincipal.getUsername());
        assertEquals(String.valueOf(user.getId()), userPrincipal.getName());
    }
}