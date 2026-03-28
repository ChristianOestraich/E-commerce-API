package project.ecommerce.service;

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

        // Resolve o endereco de entrega
        Address deliveryAddress = null;
        if (addressId != null) {
            deliveryAddress = addressRepository.findByIdAndUser(addressId, user)
                    .orElseThrow(() -> new RuntimeException("Endereco nao encontrado."));
        } else {
            deliveryAddress = addressRepository.findByUserAndMainTrue(user)
                    .orElse(null);
        }

        // Valida estoque e deduz quantidade
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

        // Cria os itens do pedido
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .subtotal(cartItem.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        // Calcula subtotal
        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Aplica cupom se existir no carrinho
        Coupon coupon = cart.getCoupon();
        BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
        BigDecimal total = subtotal.subtract(discount);

        // Incrementa uso do cupom
        if (coupon != null) {
            couponService.incrementUsage(coupon);
        }

        // Cria o pedido
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

        // Vincula os itens ao pedido
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

        // Limpa o carrinho incluindo o cupom
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
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Pedido não pertence ao usuário.");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Pedido já entregue não pode ser cancelado.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Pedido já está cancelado.");
        }

        // Devolve os itens ao estoque
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

    // Usuário vê apenas seus pedidos ativos
    public Page<OrderResponse> findMyOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        return orderRepository.findByUserAndActiveTrue(user, pageable)
                .map(this::toResponse);
    }

    // Usuário busca pedido por ID
    public OrderResponse findMyOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Pedido não pertence ao usuário.");
        }

        return toResponse(order);
    }

    // ADMIN vê todos os pedidos ativos
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    // ADMIN vê todos incluindo cancelados
    public Page<OrderResponse> findAllIncludingInactive(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ADMIN atualiza status do pedido
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Pedido cancelado não pode ser atualizado.");
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

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setSubtotal(subtotal);
        response.setDiscount(order.getDiscount());
        response.setTotal(order.getTotal());
        response.setCouponCode(order.getCoupon() != null ? order.getCoupon().getCode() : null);
        response.setCreatedAt(order.getCreatedAt());
        response.setActive(order.getActive());
        response.setDeliveryAddress(addressResponse);
        response.setItems(itemResponses);
        return response;
    }
}
