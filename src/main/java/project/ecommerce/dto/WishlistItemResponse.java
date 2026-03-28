package project.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WishlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private String imageUrl;
    private Boolean productActive;
    private Boolean inStock;
    private LocalDateTime addedAt;
}