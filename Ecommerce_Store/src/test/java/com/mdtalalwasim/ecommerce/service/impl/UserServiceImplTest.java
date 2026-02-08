package com.mdtalalwasim.ecommerce.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void saveUserSetsDefaultsAndEncodesPassword() {
        User user = new User();
        user.setEmail("customer@example.com");
        user.setPassword("plain-password");

        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.saveUser(user);

        assertThat(saved.getRole()).isEqualTo("ROLE_USER");
        assertThat(saved.getIsEnable()).isTrue();
        assertThat(saved.getAccountStatusNonLocked()).isTrue();
        assertThat(saved.getAccountfailedAttemptCount()).isEqualTo(0);
        assertThat(saved.getAccountLockTime()).isNull();
        assertThat(saved.getPassword()).isEqualTo("encoded-password");

        verify(userRepository).save(user);
    }

    @Test
    void updateUserResetTokenForSendingEmailStoresToken() {
        User user = new User();
        user.setEmail("customer@example.com");

        when(userRepository.findByEmail("customer@example.com")).thenReturn(user);

        userService.updateUserResetTokenForSendingEmail("customer@example.com", "reset-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getResetTokens()).isEqualTo("reset-token");
    }
}