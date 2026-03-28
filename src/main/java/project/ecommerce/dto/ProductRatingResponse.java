package project.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductRatingResponse {
    private Long productId;
    private String productName;
    private Double averageRating;
    private Long totalReviews;
}
