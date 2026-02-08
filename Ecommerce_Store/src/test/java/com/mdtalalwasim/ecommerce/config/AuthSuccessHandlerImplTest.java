package com.mdtalalwasim.ecommerce.config;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthSuccessHandlerImplTest {

    @Test
    void redirectsAdminsToAdminDashboard() throws Exception {
        AuthSuccessHandlerImpl handler = new AuthSuccessHandlerImpl();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin@shop.test",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/admin/");
    }

    @Test
    void redirectsUsersToHome() throws Exception {
        AuthSuccessHandlerImpl handler = new AuthSuccessHandlerImpl();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user@shop.test",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }
}