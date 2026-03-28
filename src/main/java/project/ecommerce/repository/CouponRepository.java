package project.ecommerce.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.ecommerce.dto.report.TopCouponResponse;
import project.ecommerce.entity.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);

    List<Coupon> findByActiveTrue();

    @Query("SELECT new project.ecommerce.dto.report.TopCouponResponse(" +
            "c.code, c.currentUsages, " +
            "COALESCE(SUM(o.discount), 0.0)) " +
            "FROM Coupon c LEFT JOIN Order o ON o.coupon = c " +
            "GROUP BY c.code, c.currentUsages " +
            "ORDER BY c.currentUsages DESC")
    List<TopCouponResponse> findTopCoupons(Pageable pageable);
}