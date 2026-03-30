package project.ecommerce.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import project.ecommerce.dto.AddressResponse;
import project.ecommerce.dto.OrderItemResponse;
import project.ecommerce.dto.OrderResponse;
import project.ecommerce.entity.*;
import project.ecommerce.entity.enums.OrderStatus;
import project.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CouponService couponService;
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;

    @Caching(evict = {
            @CacheEvict(value = "dashboard", allEntries = true),
            @CacheEvict(value = "topProducts", allEntries = true)
    })
    @Transactional
    public OrderResponse checkout(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Carrinho nao encontrado."));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Carrinho esta vazio.");
        }

        Address deliveryAddress = null;
        if (addressId != null) {
            deliveryAddress = addressRepository.findByIdAndUser(addressId, user)
                    .orElseThrow(() -> new RuntimeException("Endereco nao encontrado."));
        } else {
            deliveryAddress = addressRepository.findByUserAndMainTrue(user)
                    .orElse(null);
        }

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (!product.getActive()) {
                throw new RuntimeException("Produto '" + product.getName() + "' nao esta mais disponivel.");
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .subtotal(cartItem.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Coupon coupon = cart.getCoupon();
        BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
        BigDecimal total = subtotal.subtract(discount);

        if (coupon != null) {
            couponService.incrementUsage(coupon);
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(total)
                .discount(discount)
                .coupon(coupon)
                .createdAt(LocalDateTime.now())
                .active(true)
                .deliveryAddress(deliveryAddress)
                .build();

        order = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);
        orderRepository.save(order);

        emailService.sendOrderConfirmationEmail(
                user.getEmail(),
                user.getName(),
                order.getId(),
                order.getTotal().toPlainString()
        );

        cart.getItems().clear();
        cart.setCoupon(null);
        cartRepository.save(cart);

        return toResponse(order);
    }

    @Caching(evict = {
            @CacheEvict(value = "dashboard", allEntries = true),
            @CacheEvict(value = "topProducts", allEntries = true)
    })
    @Transactional
    public OrderResponse cancel(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Pedido nao pertence ao usuario.");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Pedido ja entregue nao pode ser cancelado.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Pedido ja esta cancelado.");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setActive(false);
        orderRepository.save(order);

        emailService.sendOrderCancelledEmail(
                user.getEmail(),
                user.getName(),
                order.getId()
        );

        return toResponse(order);
    }

    public Page<OrderResponse> findMyOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));
        return orderRepository.findByUserAndActiveTrue(user, pageable)
                .map(this::toResponse);
    }

    public OrderResponse findMyOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Pedido nao pertence ao usuario.");
        }

        return toResponse(order);
    }

    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    public Page<OrderResponse> findAllIncludingInactive(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado."));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Pedido cancelado nao pode ser atualizado.");
        }

        order.setStatus(status);
        orderRepository.save(order);

        emailService.sendOrderStatusUpdatedEmail(
                order.getUser().getEmail(),
                order.getUser().getName(),
                order.getId(),
                status.name()
        );

        return toResponse(order);
    }

    public Page<OrderResponse> findAllAdmin(int page, int size, OrderStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null) {
            return orderRepository.findByStatus(status, pageable)
                    .map(this::toResponse);
        }
        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    OrderItemResponse r = new OrderItemResponse();
                    r.setId(item.getId());
                    r.setProductId(item.getProduct().getId());
                    r.setProductName(item.getProduct().getName());
                    r.setQuantity(item.getQuantity());
                    r.setUnitPrice(item.getUnitPrice());
                    r.setSubtotal(item.getSubtotal());
                    return r;
                })
                .collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(OrderItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AddressResponse addressResponse = null;
        if (order.getDeliveryAddress() != null) {
            addressResponse = new AddressResponse();
            addressResponse.setId(order.getDeliveryAddress().getId());
            addressResponse.setStreet(order.getDeliveryAddress().getStreet());
            addressResponse.setNumber(order.getDeliveryAddress().getNumber());
            addressResponse.setComplement(order.getDeliveryAddress().getComplement());
            addressResponse.setNeighborhood(order.getDeliveryAddress().getNeighborhood());
            addressResponse.setCity(order.getDeliveryAddress().getCity());
            addressResponse.setState(order.getDeliveryAddress().getState());
            addressResponse.setZipCode(order.getDeliveryAddress().getZipCode());
            addressResponse.setMain(order.getDeliveryAddress().getMain());
        }

        // Busca status do pagamento via repository (evita OneToMany no Order)
        String paymentStatus = paymentRepository.findByOrder(order)
                .map(payment -> payment.getStatus().name())
                .orElse("PENDING");

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setSubtotal(subtotal);
        response.setDiscount(order.getDiscount());
        response.setTotal(order.getTotal());
        response.setUserName(order.getUser().getName());
        response.setUserEmail(order.getUser().getEmail());
        response.setPaymentStatus(paymentStatus);
        response.setCouponCode(order.getCoupon() != null ? order.getCoupon().getCode() : null);
        response.setCreatedAt(order.getCreatedAt());
        response.setActive(order.getActive());
        response.setDeliveryAddress(addressResponse);
        response.setItems(itemResponses);
        return response;
    }
}