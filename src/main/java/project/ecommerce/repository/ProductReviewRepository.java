package project.ecommerce.repository;

import project.ecommerce.entity.Product;
import project.ecommerce.entity.ProductReview;
import project.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    Page<ProductReview> findByProductAndActiveTrue(Product product, Pageable pageable);

    Optional<ProductReview> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product = :product AND r.active = true")
    Double findAverageRatingByProduct(@Param("product") Product product);

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product = :product AND r.active = true")
    Long countByProduct(@Param("product") Product product);
}
