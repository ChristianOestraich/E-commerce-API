package project.ecommerce.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductResponse {
    private Long productId;
    private String productName;
    private Long totalSold;
    private java.math.BigDecimal totalRevenue;
}
