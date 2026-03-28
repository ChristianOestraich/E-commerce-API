package project.ecommerce.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowStockResponse {
    private Long productId;
    private String productName;
    private String categoryName;
    private Integer stockQuantity;
}

