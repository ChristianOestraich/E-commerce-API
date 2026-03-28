package project.ecommerce.service;

import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import project.ecommerce.dto.PaymentResponse;
import project.ecommerce.entity.Order;
import project.ecommerce.entity.Payment;
import project.ecommerce.entity.enums.OrderStatus;
import project.ecommerce.entity.enums.PaymentStatus;
import project.ecommerce.repository.OrderRepository;
import project.ecommerce.repository.PaymentRepository;
import project.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse createPreference(String email, Long orderId) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Pedido não pertence ao usuário.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Pedido cancelado não pode ser pago.");
        }

        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.APPROVED) {
                throw new RuntimeException("Pedido já foi pago.");
            }
        });

        try {
            // Monta os itens da preferência
            List<PreferenceItemRequest> items = order.getItems().stream()
                    .map(item -> PreferenceItemRequest.builder()
                            .title(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .currencyId("BRL")
                            .build())
                    .toList();

            // URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:8080/api/payments/success")
                    .failure("http://localhost:8080/api/payments/failure")
                    .pending("http://localhost:8080/api/payments/pending")
                    .build();

            // Monta a preferência
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(String.valueOf(order.getId()))
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Salva o pagamento
            Payment payment = Payment.builder()
                    .order(order)
                    .status(PaymentStatus.PENDING)
                    .amount(order.getTotal())
                    .preferenceId(preference.getId())
                    .initPoint(preference.getSandboxInitPoint())
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);
            return toResponse(payment);

        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Erro ao criar preferência no MercadoPago: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse handleSuccess(String preferenceId,
                                         String status,
                                         String paymentId) {
        Payment payment = paymentRepository.findByPreferenceId(preferenceId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado."));

        if ("approved".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setMercadoPagoPaymentId(paymentId);
            payment.setPaidAt(LocalDateTime.now());

            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }

        paymentRepository.save(payment);
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse handleFailure(String preferenceId) {
        Payment payment = paymentRepository.findByPreferenceId(preferenceId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado."));

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse handlePending(String preferenceId) {
        Payment payment = paymentRepository.findByPreferenceId(preferenceId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado."));

        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
        return toResponse(payment);
    }

    public PaymentResponse findByOrderId(String email, Long orderId) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado."));

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setInitPoint(payment.getInitPoint());
        response.setPreferenceId(payment.getPreferenceId());
        response.setMercadoPagoPaymentId(payment.getMercadoPagoPaymentId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setPaidAt(payment.getPaidAt());
        return response;
    }
}
