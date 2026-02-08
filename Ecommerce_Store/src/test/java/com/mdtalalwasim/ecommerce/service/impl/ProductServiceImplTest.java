package com.mdtalalwasim.ecommerce.service.impl;

import com.mdtalalwasim.ecommerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.repository.ProductRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product activeProduct;

    @BeforeEach
    void setUp() {
        activeProduct = new Product();
        activeProduct.setId(1L);
        activeProduct.setProductTitle("Brown Chinos");
        activeProduct.setProductCategory("trousers");
        activeProduct.setIsActive(true);
    }

    @Test
    void findAllActiveProductsWithBlankSearchUsesCategoryLookup() {
        when(productRepository.findByProductCategory("trousers")).thenReturn(List.of(activeProduct));

        List<Product> result = productService.findAllActiveProducts(" trousers ", "   ");

        assertThat(result).containsExactly(activeProduct);
        verify(productRepository).findByProductCategory("trousers");
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void findAllActiveProductsWithSearchAndNoCategoryUsesTitleSearch() {
        when(productRepository.findByIsActiveTrueAndProductTitleContainingIgnoreCase("chinos"))
                .thenReturn(List.of(activeProduct));

        List<Product> result = productService.findAllActiveProducts(" ", "chinos");

        assertThat(result).containsExactly(activeProduct);
        verify(productRepository).findByIsActiveTrueAndProductTitleContainingIgnoreCase("chinos");
        verifyNoMoreInteractions(productRepository);
    }
}