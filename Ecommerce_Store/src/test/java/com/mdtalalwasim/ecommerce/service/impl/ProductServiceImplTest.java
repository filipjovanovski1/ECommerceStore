package com.mdtalalwasim.ecommerce.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findAllActiveProductsTrimsInputsAndFallsBackToActiveList() {
        when(productRepository.findByIsActiveTrue()).thenReturn(List.of());

        productService.findAllActiveProducts("  ", "   ");

        verify(productRepository).findByIsActiveTrue();
    }

    @Test
    void findAllActiveProductsUsesCategoryAndSearchTogether() {
        when(productRepository.findByIsActiveTrueAndProductCategoryAndProductTitleContainingIgnoreCase(
                eq("Shoes"), eq("Boots")))
                .thenReturn(List.of());

        productService.findAllActiveProducts("Shoes", " Boots ");

        verify(productRepository)
                .findByIsActiveTrueAndProductCategoryAndProductTitleContainingIgnoreCase("Shoes", "Boots");
    }

    @Test
    void updateProductByIdRecalculatesDiscountAndKeepsExistingImageWhenFileEmpty() {
        Product existing = new Product();
        existing.setId(5L);
        existing.setProductImage("old.png");

        Product incoming = new Product();
        incoming.setId(5L);
        incoming.setProductTitle("New Title");
        incoming.setProductDescription("New Desc");
        incoming.setProductCategory("Cat");
        incoming.setProductPrice(200.0);
        incoming.setProductStock(10);
        incoming.setCreatedAt(existing.getCreatedAt());
        incoming.setIsActive(true);
        incoming.setDiscount(10);

        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);


        Product updated = productService.updateProductById(incoming, file);

        assertThat(updated).isNotNull();
        assertThat(updated.getProductImage()).isEqualTo("old.png");
        assertThat(updated.getProductTitle()).isEqualTo("New Title");
        assertThat(updated.getProductDescription()).isEqualTo("New Desc");
        assertThat(updated.getProductCategory()).isEqualTo("Cat");
        assertThat(updated.getProductPrice()).isEqualTo(200.0);
        assertThat(updated.getProductStock()).isEqualTo(10);
        assertThat(updated.getDiscount()).isEqualTo(10);
        assertThat(updated.getDiscountPrice()).isEqualTo(180.0);
    }
}