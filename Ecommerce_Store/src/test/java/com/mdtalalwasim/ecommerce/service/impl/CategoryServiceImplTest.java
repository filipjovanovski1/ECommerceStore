package com.mdtalalwasim.ecommerce.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.mdtalalwasim.ecommerce.entity.Category;
import com.mdtalalwasim.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void existCategoryDelegatesToRepository() {
        when(categoryRepository.existsByCategoryName("Shoes")).thenReturn(true);

        boolean exists = categoryService.existCategory("Shoes");

        assertThat(exists).isTrue();
        verify(categoryRepository).existsByCategoryName("Shoes");
    }

    @Test
    void findAllActiveCategoryReturnsRepositoryResults() {
        List<Category> categories = List.of(new Category());
        when(categoryRepository.findByIsActiveTrue()).thenReturn(categories);

        List<Category> result = categoryService.findAllActiveCategory();

        assertThat(result).isSameAs(categories);
        verify(categoryRepository).findByIsActiveTrue();
    }
}