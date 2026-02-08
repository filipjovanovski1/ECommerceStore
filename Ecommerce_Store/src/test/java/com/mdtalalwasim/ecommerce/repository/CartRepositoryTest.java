package com.mdtalalwasim.ecommerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mdtalalwasim.ecommerce.entity.Cart;
import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.entity.User;

@DataJpaTest
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void sumQuantityByUserIdAggregatesAcrossCartLines() {
        User user = userRepository.save(buildUser("Jordan", "jordan@example.com"));
        User otherUser = userRepository.save(buildUser("Casey", "casey@example.com"));

        Product shoes = productRepository.save(buildProduct("Training Shoes"));
        Product hat = productRepository.save(buildProduct("Training Hat"));

        cartRepository.saveAll(List.of(
                buildCart(user, shoes, 2),
                buildCart(user, hat, 3),
                buildCart(otherUser, shoes, 5)));

        assertThat(cartRepository.sumQuantityByUserId(user.getId())).isEqualTo(5L);
        assertThat(cartRepository.sumQuantityByUserId(otherUser.getId())).isEqualTo(5L);
    }

    @Test
    void findByProductIdAndUserIdAndFindByUserIdReturnExpectedCartLines() {
        User user = userRepository.save(buildUser("Taylor", "taylor@example.com"));
        Product product = productRepository.save(buildProduct("Travel Bag"));
        Product otherProduct = productRepository.save(buildProduct("Wallet"));

        Cart cart = cartRepository.save(buildCart(user, product, 1));
        cartRepository.save(buildCart(user, otherProduct, 4));

        Cart found = cartRepository.findByProductIdAndUserId(product.getId(), user.getId());
        List<Cart> userCarts = cartRepository.findByUserId(user.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(cart.getId());
        assertThat(userCarts)
                .extracting(Cart::getProduct)
                .extracting(Product::getProductTitle)
                .containsExactlyInAnyOrder("Travel Bag", "Wallet");
    }

    @Test
    void sumQuantityByUserIdDefaultsToZeroWhenNoRowsExist() {
        User user = userRepository.save(buildUser("Morgan", "morgan@example.com"));

        assertThat(cartRepository.sumQuantityByUserId(user.getId())).isZero();
    }

    private Cart buildCart(User user, Product product, int quantity) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(quantity);
        return cart;
    }

    private Product buildProduct(String title) {
        Product product = new Product();
        product.setProductTitle(title);
        product.setProductCategory("Accessories");
        product.setProductDescription("desc");
        product.setProductPrice(30.0);
        product.setProductStock(10);
        product.setProductImage("image.png");
        product.setDiscount(0);
        product.setDiscountPrice(30.0);
        product.setIsActive(true);
        return product;
    }

    private User buildUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setMobile("1234567890");
        user.setAddress("123 Lane");
        user.setCity("City");
        user.setState("State");
        user.setPinCode("00000");
        user.setPassword("password");
        user.setProfileImage("profile.png");
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountStatusNonLocked(true);
        user.setAccountfailedAttemptCount(0);
        return user;
    }
}