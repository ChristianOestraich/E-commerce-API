package project.ecommerce.controller;

import project.ecommerce.dto.PaymentResponse;
import project.ecommerce.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final MercadoPagoService mercadoPagoService;

    // Inicia o pagamento — retorna o initPoint (URL do checkout MercadoPago)
    @PostMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> createPayment(Authentication auth,
                                                         @PathVariable Long orderId) {
        return ResponseEntity.ok(mercadoPagoService.createPreference(auth.getName(), orderId));
    }

    // Callback de sucesso
    @GetMapping("/success")
    public ResponseEntity<PaymentResponse> success(
            @RequestParam("preference_id") String preferenceId,
            @RequestParam("status") String status,
            @RequestParam("payment_id") String paymentId) {
        return ResponseEntity.ok(mercadoPagoService.handleSuccess(preferenceId, status, paymentId));
    }

    // Callback de falha
    @GetMapping("/failure")
    public ResponseEntity<PaymentResponse> failure(
            @RequestParam("preference_id") String preferenceId) {
        return ResponseEntity.ok(mercadoPagoService.handleFailure(preferenceId));
    }

    // Callback de pendente
    @GetMapping("/pending")
    public ResponseEntity<PaymentResponse> pending(
            @RequestParam("preference_id") String preferenceId) {
        return ResponseEntity.ok(mercadoPagoService.handlePending(preferenceId));
    }

    // Consulta pagamento de um pedido
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> findByOrderId(Authentication auth,
                                                         @PathVariable Long orderId) {
        return ResponseEntity.ok(mercadoPagoService.findByOrderId(auth.getName(), orderId));
    }
}