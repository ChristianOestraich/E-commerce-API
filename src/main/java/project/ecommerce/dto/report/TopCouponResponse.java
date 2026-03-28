package project.ecommerce.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopCouponResponse {
    private String code;
    private Integer totalUsages;
    private BigDecimal totalDiscount;
}