package com.mdtalalwasim.ecommerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import com.mdtalalwasim.ecommerce.entity.Category;
import com.mdtalalwasim.ecommerce.service.CartService;
import com.mdtalalwasim.ecommerce.service.CategoryService;
import com.mdtalalwasim.ecommerce.service.ProductService;
import com.mdtalalwasim.ecommerce.service.UserService;

@WebMvcTest(AdminViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("e2e")
class AdminViewControllerTest {

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

    @Test
    void saveCategoryRejectsDuplicateName() throws Exception {
        when(categoryService.existCategory("Shoes")).thenReturn(true);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());

        MockMultipartFile file = buildImageFile();

        mockMvc.perform(multipart("/admin/save-category")
                        .file(file)
                        .param("categoryName", "Shoes")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category"))
                .andExpect(request().sessionAttribute("errorMsg", "Category Name already Exists"));
    }

    @Test
    void saveCategoryStoresSuccessMessage() throws Exception {
        when(categoryService.existCategory("Hats")).thenReturn(false);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());

        Category saved = new Category();
        saved.setId(4L);
        saved.setCategoryName("Hats");
        when(categoryService.saveCategory(org.mockito.ArgumentMatchers.any(Category.class))).thenReturn(saved);

        MockMultipartFile file = buildImageFile();

        mockMvc.perform(multipart("/admin/save-category")
                        .file(file)
                        .param("categoryName", "Hats")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category"))
                .andExpect(request().sessionAttribute("successMsg", "Category Save Successfully."));
    }

    @Test
    void updateCategorySetsSuccessMessage() throws Exception {
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());

        Category existing = new Category();
        existing.setId(7L);
        existing.setCategoryName("Bags");
        existing.setCategoryImage("old.png");
        when(categoryService.findById(7L)).thenReturn(Optional.of(existing));

        Category updated = new Category();
        updated.setId(7L);
        updated.setCategoryName("Bags");
        updated.setCategoryImage("sample-upload.png");
        when(categoryService.saveCategory(org.mockito.ArgumentMatchers.any(Category.class))).thenReturn(updated);

        MockMultipartFile file = buildImageFile();

        mockMvc.perform(multipart("/admin/update-category")
                        .file(file)
                        .param("id", "7")
                        .param("categoryName", "Bags")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category"))
                .andExpect(request().sessionAttribute("successMsg", "Category Updated Successfully"));
    }

    @Test
    void categoryListLoadsAllCategories() throws Exception {
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        Category category = new Category();
        category.setCreatedAt(LocalDateTime.now());
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/admin/category"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("allCategoryList"));
    }

    private MockMultipartFile buildImageFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("images/sample-upload.png");
        return new MockMultipartFile(
                "file",
                "sample-upload.png",
                "image/png",
                resource.getInputStream().readAllBytes());
    }
}