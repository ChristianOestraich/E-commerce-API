package project.ecommerce.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SalesByPeriodResponse {
    private String period;
    private Long totalOrders;
    private BigDecimal totalRevenue;
}