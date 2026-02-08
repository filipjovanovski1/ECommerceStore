package com.mdtalalwasim.ecommerce.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mdtalalwasim.ecommerce.entity.Cart;
import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.repository.CartRepository;
import com.mdtalalwasim.ecommerce.repository.ProductRepository;
import com.mdtalalwasim.ecommerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void saveCartCreatesNewCartWithDiscountPriceTotal() {
        User user = new User();
        user.setId(5L);

        Product product = new Product();
        product.setId(7L);
        product.setProductStock(10);
        product.setDiscountPrice(45.0);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(cartRepository.findByProductIdAndUserId(7L, 5L)).thenReturn(null);

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.saveCart(7L, 5L, 2);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart persisted = cartCaptor.getValue();

        assertThat(result).isNotNull();
        assertThat(persisted.getQuantity()).isEqualTo(2);
        assertThat(persisted.getTotalPrice()).isEqualTo(90.0);
        assertThat(persisted.getProduct()).isEqualTo(product);
        assertThat(persisted.getUser()).isEqualTo(user);
    }

    @Test
    void saveCartUpdatesExistingCartQuantityWhenStockAllows() {
        User user = new User();
        user.setId(2L);

        Product product = new Product();
        product.setId(3L);
        product.setProductStock(5);
        product.setDiscountPrice(30.0);

        Cart existing = new Cart();
        existing.setId(1L);
        existing.setUser(user);
        existing.setProduct(product);
        existing.setQuantity(1);
        existing.setTotalPrice(30.0);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(cartRepository.findByProductIdAndUserId(3L, 2L)).thenReturn(existing);
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        cartService.saveCart(3L, 2L, 2);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        Cart persisted = cartCaptor.getValue();

        assertThat(persisted.getQuantity()).isEqualTo(3);
        assertThat(persisted.getTotalPrice()).isEqualTo(90.0);
    }

    @Test
    void checkoutCartUpdatesStockAndClearsCart() {
        User user = new User();
        user.setId(9L);

        Product product = new Product();
        product.setId(11L);
        product.setProductStock(4);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(2);

        when(cartRepository.findByUserId(9L)).thenReturn(List.of(cart));

        boolean result = cartService.checkoutCart(9L);

        assertThat(result).isTrue();
        assertThat(product.getProductStock()).isEqualTo(2);
        verify(productRepository).save(product);
        verify(cartRepository).deleteByUserId(9L);
    }
}