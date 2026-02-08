package com.mdtalalwasim.ecommerce.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;

import java.util.Date;

import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.repository.UserRepository;
import com.mdtalalwasim.ecommerce.utils.AppConstant;

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
        user.setPassword("plain");

        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.saveUser(user);

        assertThat(saved.getRole()).isEqualTo("ROLE_USER");
        assertThat(saved.getIsEnable()).isTrue();
        assertThat(saved.getAccountStatusNonLocked()).isTrue();
        assertThat(saved.getAccountfailedAttemptCount()).isEqualTo(0);
        assertThat(saved.getAccountLockTime()).isNull();
        assertThat(saved.getPassword()).isEqualTo("encoded");
    }

    @Test
    void userAccountLockMarksUserLocked() {
        User user = new User();
        user.setAccountStatusNonLocked(true);

        userService.userAccountLock(user);

        assertThat(user.getAccountStatusNonLocked()).isFalse();
        assertThat(user.getAccountLockTime()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void userFailedAttemptIncreaseIncrementsCounter() {
        User user = new User();
        user.setAccountfailedAttemptCount(1);

        userService.userFailedAttemptIncrease(user);

        assertThat(user.getAccountfailedAttemptCount()).isEqualTo(2);
        verify(userRepository).save(user);
    }

    @Test
    void isUnlockAccountTimeExpiredUnlocksAndResetsWhenTimePassed() {
        User user = new User();

        user.setAccountStatusNonLocked(false);
        user.setAccountfailedAttemptCount(2);
        user.setAccountLockTime(new Date(System.currentTimeMillis() - AppConstant.UNLOCK_DURATION_TIME - 1000));

        boolean result = userService.isUnlockAccountTimeExpired(user);

        assertThat(result).isTrue();
        assertThat(user.getAccountStatusNonLocked()).isTrue();
        assertThat(user.getAccountfailedAttemptCount()).isEqualTo(0);
        assertThat(user.getAccountLockTime()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserResetTokenForSendingEmailStoresToken() {
        User user = new User();
        user.setEmail("user@shop.test");
        when(userRepository.findByEmail("user@shop.test")).thenReturn(user);

        userService.updateUserResetTokenForSendingEmail("user@shop.test", "reset-token");

        assertThat(user.getResetTokens()).isEqualTo("reset-token");
        verify(userRepository).save(user);
    }

    @Test
    void getUserByresetTokensDelegatesToRepository() {
        User user = new User();
        user.setResetTokens("token-123");
        when(userRepository.findByResetTokens("token-123")).thenReturn(user);

        User result = userService.getUserByresetTokens("token-123");

        assertThat(result).isEqualTo(user);
        verify(userRepository).findByResetTokens("token-123");
    }
}
