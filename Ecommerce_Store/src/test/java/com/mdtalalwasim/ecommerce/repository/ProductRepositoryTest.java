package com.mdtalalwasim.ecommerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mdtalalwasim.ecommerce.entity.Product;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByIsActiveTrueAndProductCategoryAndProductTitleContainingIgnoreCaseMatchesCombination() {
        Product match = buildProduct("Running Shoes", "Shoes", true);
        Product differentTitle = buildProduct("Trail Shoes", "Shoes", true);
        Product inactive = buildProduct("Running Shoes", "Shoes", false);
        Product differentCategory = buildProduct("Running Hats", "Hats", true);

        productRepository.saveAll(List.of(match, differentTitle, inactive, differentCategory));

        List<Product> results = productRepository
                .findByIsActiveTrueAndProductCategoryAndProductTitleContainingIgnoreCase("Shoes", "running");

        assertThat(results)
                .extracting(Product::getProductTitle)
                .containsExactly("Running Shoes");
    }

    @Test
    void findByIsActiveTrueAndProductCategoryReturnsOnlyActiveCategoryMatches() {
        Product activeShoes = buildProduct("Street Shoes", "Shoes", true);
        Product inactiveShoes = buildProduct("Classic Shoes", "Shoes", false);
        Product activeHats = buildProduct("Classic Hat", "Hats", true);

        productRepository.saveAll(List.of(activeShoes, inactiveShoes, activeHats));

        List<Product> results = productRepository.findByIsActiveTrueAndProductCategory("Shoes");

        assertThat(results)
                .extracting(Product::getProductTitle)
                .containsExactly("Street Shoes");
    }

    @Test
    void findByIsActiveTrueAndProductTitleContainingIgnoreCaseSearchesAcrossCategories() {
        Product activeMatch = buildProduct("Sport Backpack", "Bags", true);
        Product inactiveMatch = buildProduct("Sport Backpack", "Bags", false);
        Product activeOther = buildProduct("Formal Briefcase", "Bags", true);

        productRepository.saveAll(List.of(activeMatch, inactiveMatch, activeOther));

        List<Product> results = productRepository
                .findByIsActiveTrueAndProductTitleContainingIgnoreCase("sport");

        assertThat(results)
                .extracting(Product::getProductTitle)
                .containsExactly("Sport Backpack");
    }

    private Product buildProduct(String title, String category, boolean active) {
        Product product = new Product();
        product.setProductTitle(title);
        product.setProductCategory(category);
        product.setProductDescription("desc");
        product.setProductPrice(25.0);
        product.setProductStock(10);
        product.setProductImage("image.png");
        product.setDiscount(0);
        product.setDiscountPrice(25.0);
        product.setIsActive(active);
        return product;
    }
}