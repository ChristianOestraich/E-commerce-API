package project.ecommerce.dto;

import lombok.Data;
import java.util.List;

@Data
public class WishlistResponse {
    private Long totalItems;
    private List<WishlistItemResponse> items;
}