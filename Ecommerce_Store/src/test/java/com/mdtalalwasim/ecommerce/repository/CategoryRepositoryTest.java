package com.mdtalalwasim.ecommerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mdtalalwasim.ecommerce.entity.Category;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void existsByCategoryNameReturnsTrueForSavedCategory() {
        Category category = new Category();
        category.setCategoryName("Dresses");
        category.setCategoryImage("dresses.png");
        category.setIsActive(true);

        categoryRepository.save(category);

        assertThat(categoryRepository.existsByCategoryName("Dresses")).isTrue();
    }

    @Test
    void findByIsActiveTrueReturnsOnlyActiveCategories() {
        Category active = new Category();
        active.setCategoryName("Hats");
        active.setCategoryImage("hats.png");
        active.setIsActive(true);

        Category inactive = new Category();
        inactive.setCategoryName("Shorts");
        inactive.setCategoryImage("shorts.png");
        inactive.setIsActive(false);

        categoryRepository.saveAll(List.of(active, inactive));

        List<Category> results = categoryRepository.findByIsActiveTrue();

        assertThat(results)
                .extracting(Category::getCategoryName)
                .containsExactly("Hats");
    }
}