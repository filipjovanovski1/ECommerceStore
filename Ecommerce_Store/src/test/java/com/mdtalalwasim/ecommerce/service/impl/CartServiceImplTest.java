package com.mdtalalwasim.ecommerce.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

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
    void saveCartAddsNewItemWhenStockAllows() {
        User user = new User();
        user.setId(42L);

        Product product = new Product();
        product.setId(10L);
        product.setProductStock(5);
        product.setDiscountPrice(25.0);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByProductIdAndUserId(10L, 42L)).thenReturn(null);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        when(cartRepository.save(cartCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Cart saved = cartService.saveCart(10L, 42L, 2);

        assertThat(saved).isNotNull();
        Cart persisted = cartCaptor.getValue();

        assertThat(persisted.getProduct()).isEqualTo(product);
        assertThat(persisted.getQuantity()).isEqualTo(2);
        assertThat(persisted.getTotalPrice()).isEqualTo(50.0);
        assertThat(persisted.getUser()).isEqualTo(user);
    }

    @Test
    void saveCartUpdatesExistingItemQuantity() {
        User user = new User();
        user.setId(7L);

        Product product = new Product();
        product.setId(5L);
        product.setProductStock(10);
        product.setDiscountPrice(30.0);

        Cart existing = new Cart();
        existing.setQuantity(2);
        existing.setProduct(product);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartRepository.findByProductIdAndUserId(5L, 7L)).thenReturn(existing);
        when(cartRepository.save(existing)).thenReturn(existing);

        Cart saved = cartService.saveCart(5L, 7L, 3);

        assertThat(saved).isEqualTo(existing);
        assertThat(existing.getQuantity()).isEqualTo(5);
        assertThat(existing.getTotalPrice()).isEqualTo(150.0);
    }

    @Test
    void saveCartReturnsNullWhenStockIsInsufficient() {
        User user = new User();
        user.setId(7L);

        Product product = new Product();
        product.setId(5L);
        product.setProductStock(5);
        product.setDiscountPrice(30.0);

        Cart existing = new Cart();
        existing.setQuantity(4);
        existing.setProduct(product);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartRepository.findByProductIdAndUserId(5L, 7L)).thenReturn(existing);

        Cart saved = cartService.saveCart(5L, 7L, 2);

        assertThat(saved).isNull();
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartQuantityDeletesWhenQuantityDropsToZero() {
        Cart cart = new Cart();
        cart.setId(11L);
        cart.setQuantity(1);

        when(cartRepository.findById(11L)).thenReturn(Optional.of(cart));

        boolean result = cartService.updateCartQuantity("decrease", 11L);

        assertThat(result).isTrue();
        verify(cartRepository).deleteById(11L);
    }

    @Test
    void updateCartQuantityDecreasePersistsWhenAboveZero() {
        Cart cart = new Cart();
        cart.setId(10L);
        cart.setQuantity(3);

        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));

        boolean result = cartService.updateCartQuantity("decrease", 10L);

        assertThat(result).isTrue();
        assertThat(cart.getQuantity()).isEqualTo(2);
        verify(cartRepository).save(cart);
    }

    @Test
    void updateCartQuantityBlocksIncreaseBeyondStock() {
    Product product = new Product();
        product.setProductStock(2);
        Cart cart = new Cart();
        cart.setId(12L);
        cart.setQuantity(2);
        cart.setProduct(product);

        when(cartRepository.findById(12L)).thenReturn(Optional.of(cart));

        boolean result = cartService.updateCartQuantity("increase", 12L);

        assertThat(result).isFalse();
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartQuantityIncreaseSavesWhenStockAvailable() {
        Product product = new Product();
        product.setProductStock(4);
        Cart cart = new Cart();
        cart.setId(12L);
        cart.setQuantity(2);
        cart.setProduct(product);

        when(cartRepository.findById(12L)).thenReturn(Optional.of(cart));

        boolean result = cartService.updateCartQuantity("increase", 12L);

        assertThat(result).isTrue();
        assertThat(cart.getQuantity()).isEqualTo(3);
        verify(cartRepository).save(cart);
    }

    @Test
    void updateCartQuantityReturnsFalseWhenMissingCart() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = cartService.updateCartQuantity("increase", 99L);

        assertThat(result).isFalse();
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void checkoutCartDeductsStockAndClearsCart() {
        Product product = new Product();
        product.setId(4L);
        product.setProductStock(3);
        Cart cart = new Cart();
        cart.setId(90L);
        cart.setProduct(product);
        cart.setQuantity(5);

        when(cartRepository.findByUserId(99L)).thenReturn(List.of(cart));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));


        boolean result = cartService.checkoutCart(99L);

        assertThat(result).isTrue();
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getProductStock()).isEqualTo(0);
        verify(cartRepository).deleteByUserId(99L);
    }

    @Test
    void checkoutCartReturnsFalseWhenCartEmpty() {
        when(cartRepository.findByUserId(3L)).thenReturn(List.of());

        boolean result = cartService.checkoutCart(3L);

        assertThat(result).isFalse();
        verify(cartRepository, never()).deleteByUserId(3L);
    }
}