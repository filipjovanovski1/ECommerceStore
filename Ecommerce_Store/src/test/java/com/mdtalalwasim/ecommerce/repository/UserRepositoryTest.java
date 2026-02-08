package com.mdtalalwasim.ecommerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mdtalalwasim.ecommerce.entity.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByRoleReturnsMatchingUsers() {
        User admin = new User();
        admin.setEmail("admin@shop.test");
        admin.setRole("ROLE_ADMIN");

        User user = new User();
        user.setEmail("user@shop.test");
        user.setRole("ROLE_USER");

        userRepository.saveAll(List.of(admin, user));

        List<User> results = userRepository.findByRole("ROLE_USER");

        assertThat(results)
                .extracting(User::getEmail)
                .containsExactly("user@shop.test");
    }

    @Test
    void findByResetTokensReturnsUserWithToken() {
        User user = new User();
        user.setEmail("reset@shop.test");
        user.setResetTokens("token-abc");

        userRepository.save(user);

        User result = userRepository.findByResetTokens("token-abc");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("reset@shop.test");
    }
}