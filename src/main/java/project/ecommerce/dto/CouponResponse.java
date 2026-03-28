package project.ecommerce.dto;

import project.ecommerce.entity.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderValue;
    private Integer maxUsages;
    private Integer currentUsages;
    private LocalDateTime expiresAt;
    private Boolean active;
}
