package project.ecommerce.service;

import project.ecommerce.dto.ProductRatingResponse;
import project.ecommerce.dto.ReviewRequest;
import project.ecommerce.dto.ReviewResponse;
import project.ecommerce.entity.Product;
import project.ecommerce.entity.ProductReview;
import project.ecommerce.entity.User;
import project.ecommerce.repository.OrderRepository;
import project.ecommerce.repository.ProductRepository;
import project.ecommerce.repository.ProductReviewRepository;
import project.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse create(String email, Long productId, ReviewRequest request) {
        User user = getUser(email);
        Product product = getProduct(productId);

        // Verifica se o usuario comprou o produto com pedido entregue
        if (!orderRepository.userHasPurchasedProduct(user, product)) {
            throw new RuntimeException(
                    "Voce so pode avaliar produtos de pedidos ja entregues.");
        }

        // Verifica se ja avaliou
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Voce ja avaliou este produto.");
        }

        ProductReview review = ProductReview.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse update(String email, Long reviewId, ReviewRequest request) {
        User user = getUser(email);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avaliacao nao encontrada."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Avaliacao nao pertence ao usuario.");
        }

        if (!review.getActive()) {
            throw new RuntimeException("Avaliacao inativa nao pode ser editada.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(String email, Long reviewId) {
        User user = getUser(email);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avaliacao nao encontrada."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Avaliacao nao pertence ao usuario.");
        }

        review.setActive(false);
        reviewRepository.save(review);
    }

    // ADMIN pode desativar qualquer avaliacao
    @Transactional
    public void adminDelete(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avaliacao nao encontrada."));
        review.setActive(false);
        reviewRepository.save(review);
    }

    public Page<ReviewResponse> findByProduct(Long productId, Pageable pageable) {
        Product product = getProduct(productId);
        return reviewRepository.findByProductAndActiveTrue(product, pageable)
                .map(this::toResponse);
    }

    public ProductRatingResponse getRating(Long productId) {
        Product product = getProduct(productId);
        Double average = reviewRepository.findAverageRatingByProduct(product);
        Long total = reviewRepository.countByProduct(product);

        return new ProductRatingResponse(
                product.getId(),
                product.getName(),
                average != null ? Math.round(average * 10.0) / 10.0 : 0.0,
                total
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));
    }

    private Product getProduct(Long productId) {
        return productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Produto nao encontrado."));
    }

    private ReviewResponse toResponse(ProductReview review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserName(review.getUser().getName());
        response.setProductId(review.getProduct().getId());
        response.setProductName(review.getProduct().getName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setActive(review.getActive());
        return response;
    }
}
