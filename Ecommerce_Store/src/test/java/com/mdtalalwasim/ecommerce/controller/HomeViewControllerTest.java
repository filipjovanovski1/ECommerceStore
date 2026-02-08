package com.mdtalalwasim.ecommerce.controller;


import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.List;

import com.mdtalalwasim.ecommerce.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import com.mdtalalwasim.ecommerce.entity.Category;
import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.entity.User;

@WebMvcTest(HomeViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @MockBean
    private CartService cartService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @Test
    void homeIndexLoadsLatestCategoriesAndProducts() throws Exception {
        Category category = new Category();
        category.setId(10L);
        category.setCategoryName("Trousers");
        category.setIsActive(true);

        Product product = new Product();
        product.setId(20L);
        product.setProductTitle("Brown Chinos");
        product.setIsActive(true);

        when(categoryService.findAllActiveCategory()).thenReturn(List.of(category));
        when(productService.findAllActiveProducts("")).thenReturn(List.of(product));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index.html"))
                .andExpect(model().attributeExists("latestSixActiveCategory"))
                .andExpect(model().attributeExists("latestEightActiveProducts"))
                .andExpect(model().attributeExists("allActiveCategory"));
    }
    @Test
    void productsPageReturnsActiveCategoriesAndProducts() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Shoes");
        category.setIsActive(true);

        Product product = new Product();
        product.setId(2L);
        product.setProductTitle("Leather Boots");
        product.setIsActive(true);

        when(categoryService.findAllActiveCategory()).thenReturn(new java.util.ArrayList<>(List.of(category)));
        when(productService.findAllActiveProducts("shoes", "boot")).thenReturn(new java.util.ArrayList<>(List.of(product)));

        mockMvc.perform(get("/products")
                        .param("category", "shoes")
                        .param("search", "boot"))
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attribute("allActiveCategory", List.of(category)))
                .andExpect(model().attribute("allActiveProducts", List.of(product)))
                .andExpect(model().attribute("paramValue", "shoes"))
                .andExpect(model().attribute("searchValue", "boot"));
    }

    @Test
    void saveUserRejectsInvalidRegistrations() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image".getBytes()
        );

        mockMvc.perform(multipart("/save-user")
                        .file(file)
                        .param("name", "Invalid User")
                        .param("email", "invalid-email")
                        .param("mobile", "1234567890")
                        .param("address", "123 Main Street")
                        .param("city", "Springfield")
                        .param("state", "IL")
                        .param("pinCode", "12345")
                        .param("password", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(request().sessionAttribute("errorMsg", "Invalid registration details."));

        verifyNoInteractions(userService);
    }

    @Test
    void saveUserRejectsDuplicateRegistrations() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image".getBytes()
        );

        User existingUser = new User();
        existingUser.setId(99L);
        existingUser.setEmail("existing@example.com");

        when(userService.getUserByEmail("existing@example.com")).thenReturn(existingUser);

        mockMvc.perform(multipart("/save-user")
                        .file(file)
                        .param("name", "Existing User")
                        .param("email", "existing@example.com")
                        .param("mobile", "1234567890")
                        .param("address", "123 Main Street")
                        .param("city", "Springfield")
                        .param("state", "IL")
                        .param("pinCode", "12345")
                        .param("password", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(request().sessionAttribute("errorMsg", "Account already exists."));

        verify(userService).getUserByEmail("existing@example.com");
        verifyNoMoreInteractions(userService);
    }
}
