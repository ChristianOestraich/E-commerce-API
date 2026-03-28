package project.ecommerce.controller;

import project.ecommerce.dto.ProductRatingResponse;
import project.ecommerce.dto.ReviewRequest;
import project.ecommerce.dto.ReviewResponse;
import project.ecommerce.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    // Lista avaliacoes de um produto
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> findByProduct(
            @PathVariable Long productId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.findByProduct(productId, pageable));
    }

    // Rating medio do produto
    @GetMapping("/{productId}/rating")
    public ResponseEntity<ProductRatingResponse> getRating(
            @PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getRating(productId));
    }

    // Cria avaliacao
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ReviewResponse> create(
            Authentication auth,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.create(auth.getName(), productId, request));
    }

    // Edita propria avaliacao
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> update(
            Authentication auth,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.update(auth.getName(), reviewId, request));
    }

    // Soft delete da propria avaliacao
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> delete(
            Authentication auth,
            @PathVariable Long reviewId) {
        reviewService.delete(auth.getName(), reviewId);
        return ResponseEntity.noContent().build();
    }

    // ADMIN desativa qualquer avaliacao
    @DeleteMapping("/reviews/{reviewId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDelete(@PathVariable Long reviewId) {
        reviewService.adminDelete(reviewId);
        return ResponseEntity.noContent().build();
    }
}
