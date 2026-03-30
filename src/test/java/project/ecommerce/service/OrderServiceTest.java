package project.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.ecommerce.dto.OrderResponse;
import project.ecommerce.entity.*;
import project.ecommerce.entity.enums.OrderStatus;
import project.ecommerce.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private CouponService couponService;
    @Mock private EmailService emailService;
    @Mock private PaymentRepository paymentRepository;
    @InjectMocks private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;
    private Order order;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Christian")
                .email("christian@email.com")
                .active(true)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Notebook")
                .price(new BigDecimal("3500.00"))
                .stockQuantity(10)
                .active(true)
                .build();

        cartItem = CartItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();

        cartItem.setCart(cart);

        order = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .total(new BigDecimal("7000.00"))
                .discount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .active(true)
                .items(new ArrayList<>())
                .build();

        // Mock lenient — evita UnnecessaryStubbingException nos testes que lancam excecao antes do toResponse
        lenient().when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.empty());
    }

    @Test
    void checkout_ShouldCreateOrder_WhenCartHasItems() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(couponService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderResponse response = orderService.checkout("christian@email.com", null);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(productRepository, times(1)).save(any(Product.class));
        assertEquals(8, product.getStockQuantity());
    }

    @Test
    void checkout_ShouldThrowException_WhenCartIsEmpty() {
        cart.getItems().clear();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout("christian@email.com", null));

        assertEquals("Carrinho esta vazio.", ex.getMessage());
    }

    @Test
    void checkout_ShouldThrowException_WhenStockInsufficient() {
        product.setStockQuantity(1);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout("christian@email.com", null));

        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
    }

    @Test
    void cancel_ShouldCancelOrder_AndRestoreStock() {
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("3500.00"))
                .subtotal(new BigDecimal("7000.00"))
                .build();
        order.getItems().add(orderItem);
        order.setUser(user);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.cancel("christian@email.com", 1L);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertFalse(response.getActive());
        assertEquals(12, product.getStockQuantity());
    }

    @Test
    void cancel_ShouldThrowException_WhenOrderIsDelivered() {
        order.setStatus(OrderStatus.DELIVERED);
        order.setUser(user);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancel("christian@email.com", 1L));

        assertEquals("Pedido ja entregue nao pode ser cancelado.", ex.getMessage());
    }
}