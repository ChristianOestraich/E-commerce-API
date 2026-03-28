package project.ecommerce.dto;

import project.ecommerce.entity.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    @NotBlank
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal discountValue;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minimumOrderValue;

    private Integer maxUsages;

    @NotNull
    private LocalDateTime expiresAt;
}