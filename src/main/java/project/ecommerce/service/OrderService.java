package project.ecommerce.service;

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

    @Transactional
    public OrderResponse checkout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Carrinho não encontrado."));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Carrinho está vazio.");
        }

        // Valida estoque e deduz quantidade de cada produto
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (!product.getActive()) {
                throw new RuntimeException("Produto '" + product.getName() + "' não está mais disponível.");
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

        BigDecimal total = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cria o pedido
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(total)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        order = orderRepository.save(order);

        // Vincula os itens ao pedido
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);
        orderRepository.save(order);

        // Limpa o carrinho após o checkout
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

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
        return toResponse(orderRepository.save(order));
    }

    // Mapeamento
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

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setTotal(order.getTotal());
        response.setCreatedAt(order.getCreatedAt());
        response.setActive(order.getActive());
        response.setItems(itemResponses);
        return response;
    }
}
