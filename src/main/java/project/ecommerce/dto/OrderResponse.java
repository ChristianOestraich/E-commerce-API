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
    private BigDecimal total;
    private LocalDateTime createdAt;
    private Boolean active;
    private List<OrderItemResponse> items;
}