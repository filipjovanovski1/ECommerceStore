package com.mdtalalwasim.ecommerce.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.mdtalalwasim.ecommerce.controller.AdminViewController;
import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.repository.UserRepository;
import com.mdtalalwasim.ecommerce.service.CartService;
import com.mdtalalwasim.ecommerce.service.CategoryService;
import com.mdtalalwasim.ecommerce.service.ProductService;
import com.mdtalalwasim.ecommerce.service.UserService;

import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@WebMvcTest(AdminViewController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService userService;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @MockBean
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Test
    @WithMockUser(roles = "USER")
    void adminRoutesAreForbiddenForStandardUsers() throws Exception {
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());

        mockMvc.perform(get("/admin/"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@shop.test", roles = "ADMIN")
    void adminRoutesAllowAdminUsers() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@shop.test");

        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(userService.getUserByEmail("admin@shop.test")).thenReturn(admin);
        when(cartService.getCounterCart(1L)).thenReturn(0L);

        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin-dashboard"));
    }
}