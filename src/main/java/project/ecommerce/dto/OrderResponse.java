package project.ecommerce.dto;

import project.ecommerce.entity.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private String couponCode;
    private LocalDateTime createdAt;
    private Boolean active;
    private AddressResponse deliveryAddress;
    private List<OrderItemResponse> items;
}