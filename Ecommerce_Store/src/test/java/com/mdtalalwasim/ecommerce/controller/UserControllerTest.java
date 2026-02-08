package com.mdtalalwasim.ecommerce.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import com.mdtalalwasim.ecommerce.entity.Cart;
import com.mdtalalwasim.ecommerce.entity.Category;
import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.service.CartService;
import com.mdtalalwasim.ecommerce.service.CategoryService;
import com.mdtalalwasim.ecommerce.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @MockBean
    private CartService cartService;

    @Test
    void addToCartRedirectsToSigninWhenUnauthenticated() throws Exception {
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());

        mockMvc.perform(get("/user/add-to-cart")
                        .param("productId", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"))
                .andExpect(request().sessionAttribute("errorMsg", "Please sign in to add items to your cart."));

        verify(cartService, never()).saveCart(anyLong(), anyLong(), anyInt());
    }

    @Test
    void addToCartRejectsNonPositiveQuantity() throws Exception {
        User user = new User();
        user.setId(5L);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(userService.getUserByEmail("user@shop.test")).thenReturn(user);

        mockMvc.perform(get("/user/add-to-cart")
                        .principal(() -> "user@shop.test")
                        .param("productId", "8")
                        .param("quantity", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/8"))
                .andExpect(request().sessionAttribute("errorMsg", "Quantity must be at least 1."));

        verify(cartService, never()).saveCart(anyLong(), anyLong(), anyInt());
    }

    @Test
    void addToCartSetsSuccessMessageOnSave() throws Exception {
        User user = new User();
        user.setId(11L);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(userService.getUserByEmail("user@shop.test")).thenReturn(user);
        when(cartService.saveCart(12L, 11L, 2)).thenReturn(new Cart());

        mockMvc.perform(get("/user/add-to-cart")
                        .principal(() -> "user@shop.test")
                        .param("productId", "12")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/12"))
                .andExpect(request().sessionAttribute("successMsg", "Successfully, Product added to Cart"));
    }

    @Test
    void addToCartSetsErrorMessageWhenInventoryUnavailable() throws Exception {
        User user = new User();
        user.setId(10L);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(userService.getUserByEmail("user@shop.test")).thenReturn(user);
        when(cartService.saveCart(15L, 10L, 1)).thenReturn(null);

        mockMvc.perform(get("/user/add-to-cart")
                        .principal(() -> "user@shop.test")
                        .param("productId", "15")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/15"))
                .andExpect(request().sessionAttribute("errorMsg", "Requested quantity is unavailable."));
    }

    @Test
    void modelAttributePopulatesUserAndCartDetails() {
        User user = new User();
        user.setId(3L);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of(new Category()));
        when(userService.getUserByEmail("user@shop.test")).thenReturn(user);
        when(cartService.getCounterCart(3L)).thenReturn(4L);

        ExtendedModelMap model = new ExtendedModelMap();
        userController.getUserDetails(() -> "user@shop.test", model);

        assertThat(model.getAttribute("allActiveCategory")).isNotNull();
        assertThat(model.getAttribute("currentLoggedInUserDetails")).isEqualTo(user);
        assertThat(model.getAttribute("countCartForUser")).isEqualTo(4L);
    }

    @Test
    void orderPageReturnsToCartWhenCheckoutFails() throws Exception {
        User user = new User();
        user.setId(19L);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(userService.getUserByEmail("user@shop.test")).thenReturn(user);
        when(cartService.getCounterCart(19L)).thenReturn(0L);
        when(cartService.checkoutCart(19L)).thenReturn(false);

        mockMvc.perform(get("/user/orders")
                        .principal(() -> "user@shop.test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/cart"))
                .andExpect(request().sessionAttribute("errorMsg", "Your cart is empty."));
        verify(cartService).checkoutCart(19L);
    }

    @Test
    void updateCartQuantitySetsErrorWhenStockUnavailable() throws Exception {
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(cartService.updateCartQuantity("increase", 55L)).thenReturn(false);

        mockMvc.perform(get("/user/cart-quantity-update")
                        .param("symbol", "increase")
                        .param("cartId", "55"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/cart"))
                .andExpect(request().sessionAttribute("errorMsg", "Requested quantity is unavailable."));
    }

    @Test
    void updateCartQuantityIncreaseSuccessDoesNotSetError() throws Exception {
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(cartService.updateCartQuantity("increase", 7L)).thenReturn(true);

        mockMvc.perform(get("/user/cart-quantity-update")
                        .param("symbol", "increase")
                        .param("cartId", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/cart"))
                .andExpect(request().sessionAttributeDoesNotExist("errorMsg"));
    }

    @Test
    void updateCartQuantityDecreaseDoesNotSetError() throws Exception {
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(cartService.updateCartQuantity("decrease", 9L)).thenReturn(false);

        mockMvc.perform(get("/user/cart-quantity-update")
                        .param("symbol", "decrease")
                        .param("cartId", "9"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/cart"))
                .andExpect(request().sessionAttributeDoesNotExist("errorMsg"));
    }

    @Test
    void orderPageRedirectsToSuccessWhenCheckoutSucceeds() throws Exception {
        User user = new User();
        user.setId(22L);
        when(categoryService.findAllActiveCategory()).thenReturn(List.of());
        when(userService.getUserByEmail("user@shop.test")).thenReturn(user);
        when(cartService.getCounterCart(22L)).thenReturn(2L);
        when(cartService.checkoutCart(22L)).thenReturn(true);

        mockMvc.perform(get("/user/orders")
                        .principal(() -> "user@shop.test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/order-success"))
                .andExpect(request().sessionAttribute("successMsg", "Order placed successfully."));
    }
}
