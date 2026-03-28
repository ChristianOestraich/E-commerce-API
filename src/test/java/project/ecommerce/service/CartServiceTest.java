package project.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.ecommerce.dto.CartItemRequest;
import project.ecommerce.dto.CartResponse;
import project.ecommerce.entity.*;
import project.ecommerce.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CouponService couponService;
    @InjectMocks private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("christian@email.com")
                .active(true)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>())
                .build();

        product = Product.builder()
                .id(1L)
                .name("Notebook")
                .price(new BigDecimal("3500.00"))
                .stockQuantity(10)
                .active(true)
                .build();
    }

    @Test
    void getCart_ShouldReturnCart_WhenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(couponService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);

        CartResponse response = cartService.getCart("christian@email.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void addItem_ShouldAddItemToCart_WhenProductAvailable() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(couponService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);

        CartResponse response = cartService.addItem("christian@email.com", request);

        assertNotNull(response);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_ShouldThrowException_WhenStockInsufficient() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(20);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addItem("christian@email.com", request));

        assertEquals("Quantidade solicitada indisponível em estoque.", ex.getMessage());
    }

    @Test
    void clearCart_ShouldRemoveAllItems() {
        CartItem item = CartItem.builder()
                .id(1L).cart(cart).product(product).quantity(1).build();
        cart.getItems().add(item);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart("christian@email.com");

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }
}