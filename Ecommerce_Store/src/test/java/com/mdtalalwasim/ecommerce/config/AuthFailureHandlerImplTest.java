package com.mdtalalwasim.ecommerce.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.repository.UserRepository;
import com.mdtalalwasim.ecommerce.service.UserService;
import com.mdtalalwasim.ecommerce.utils.AppConstant;

@ExtendWith(MockitoExtension.class)
class AuthFailureHandlerImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthFailureHandlerImpl authFailureHandler;

    @Test
    void incrementsFailedAttemptsForActiveUsers() throws Exception {
        User user = new User();
        user.setIsEnable(true);
        user.setAccountStatusNonLocked(true);
        user.setAccountfailedAttemptCount((int) AppConstant.ATTEMPT_COUNT - 1);

        when(userRepository.findByEmail("user@shop.test")).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "user@shop.test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad"));

        verify(userService).userFailedAttemptIncrease(user);
        assertThat(response.getRedirectedUrl()).isEqualTo("/signin?error");
    }

    @Test
    void locksAccountWhenAttemptsExceeded() throws Exception {
        User user = new User();
        user.setIsEnable(true);
        user.setAccountStatusNonLocked(true);
        user.setAccountfailedAttemptCount((int) AppConstant.ATTEMPT_COUNT);

        when(userRepository.findByEmail("user@shop.test")).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "user@shop.test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad"));

        verify(userService).userAccountLock(user);
        assertThat(response.getRedirectedUrl()).isEqualTo("/signin?error");
    }

    @Test
    void handlesUnknownUserWithoutServiceCalls() throws Exception {
        when(userRepository.findByEmail("missing@shop.test")).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "missing@shop.test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad"));

        verifyNoInteractions(userService);
        assertThat(response.getRedirectedUrl()).isEqualTo("/signin?error");
    }

    @Test
    void doesNotIncrementWhenAccountInactive() throws Exception {
        User user = new User();
        user.setIsEnable(false);
        user.setAccountStatusNonLocked(true);

        when(userRepository.findByEmail("inactive@shop.test")).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "inactive@shop.test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("bad"));

        verify(userService, never()).userFailedAttemptIncrease(user);
        verify(userService, never()).userAccountLock(user);
        assertThat(response.getRedirectedUrl()).isEqualTo("/signin?error");
    }
}