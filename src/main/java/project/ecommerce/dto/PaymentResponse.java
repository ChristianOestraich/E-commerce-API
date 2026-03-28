package project.ecommerce.dto;

import project.ecommerce.entity.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String initPoint;
    private String preferenceId;
    private String mercadoPagoPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}