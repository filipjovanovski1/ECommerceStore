package com.mdtalalwasim.ecommerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.mdtalalwasim.ecommerce.entity.Category;
import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.service.CartService;
import com.mdtalalwasim.ecommerce.service.CategoryService;
import com.mdtalalwasim.ecommerce.service.ProductService;
import com.mdtalalwasim.ecommerce.service.UserService;
import com.mdtalalwasim.ecommerce.utils.CommonUtils;

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
    private CommonUtils commonUtils;

    @MockBean
    private CartService cartService;

    @MockBean
    private PasswordEncoder passwordEncoder;

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
}