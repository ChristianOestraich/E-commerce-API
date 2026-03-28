package project.ecommerce.controller;

import project.ecommerce.dto.OrderResponse;
import project.ecommerce.entity.enums.OrderStatus;
import project.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Checkout — gera pedido a partir do carrinho
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            Authentication auth,
            @RequestParam(required = false) Long addressId) {
        return ResponseEntity.ok(orderService.checkout(auth.getName(), addressId));
    }

    // Cancela pedido (soft delete + devolve estoque)
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(Authentication auth,
                                                @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancel(auth.getName(), orderId));
    }

    // Lista pedidos do usuário autenticado
    @GetMapping("/my")
    public ResponseEntity<Page<OrderResponse>> findMyOrders(Authentication auth,
                                                            Pageable pageable) {
        return ResponseEntity.ok(orderService.findMyOrders(auth.getName(), pageable));
    }

    // Busca pedido específico do usuário
    @GetMapping("/my/{orderId}")
    public ResponseEntity<OrderResponse> findMyOrderById(Authentication auth,
                                                         @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.findMyOrderById(auth.getName(), orderId));
    }

    // ADMIN — lista todos os pedidos ativos
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    // ADMIN — lista todos incluindo cancelados
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> findAllIncludingInactive(Pageable pageable) {
        return ResponseEntity.ok(orderService.findAllIncludingInactive(pageable));
    }

    // ADMIN — atualiza status do pedido
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long orderId,
                                                      @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, status));
    }
}