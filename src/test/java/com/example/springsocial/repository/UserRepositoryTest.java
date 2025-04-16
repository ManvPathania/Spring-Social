package com.example.springsocial.repository;

import com.example.springsocial.model.User;
import com.example.springsocial.model.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setProvider(AuthProvider.local);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Test
    public void whenFindByEmail_thenReturnUser() {
        Optional<User> found = userRepository.findByEmail(user.getEmail());
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void whenExistsByEmail_thenReturnTrue() {
        Boolean exists = userRepository.existsByEmail(user.getEmail());
        assertThat(exists).isTrue();
    }

    @Test
    public void whenEmailNotExists_thenReturnFalse() {
        Boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(exists).isFalse();
    }
}