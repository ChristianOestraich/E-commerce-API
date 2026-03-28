package project.ecommerce.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.ecommerce.dto.report.SalesByPeriodResponse;
import project.ecommerce.entity.Order;
import project.ecommerce.entity.Product;
import project.ecommerce.entity.User;
import project.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserAndActiveTrue(User user, Pageable pageable);

    Page<Order> findByActiveTrue(Pageable pageable);

    Optional<Order> findByIdAndActiveTrue(Long id);

    Page<Order> findByUserAndStatusAndActiveTrue(User user, OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) > 0 FROM Order o " +
            "JOIN o.items i " +
            "WHERE o.user = :user " +
            "AND i.product = :product " +
            "AND o.status = 'DELIVERED' " +
            "AND o.active = true")
    boolean userHasPurchasedProduct(@Param("user") User user,
                                    @Param("product") Product product);

    // Total de receita de pedidos confirmados/entregues
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
            "WHERE o.status IN ('CONFIRMED', 'DELIVERED') AND o.active = true")
    BigDecimal findTotalRevenue();

    // Contagem por status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.active = true")
    Long countByStatus(@Param("status") OrderStatus status);

    // Vendas agrupadas por dia
    @Query("SELECT new project.ecommerce.dto.report.SalesByPeriodResponse(" +
            "CAST(o.createdAt AS string), COUNT(o), SUM(o.total)) " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "AND o.status IN ('CONFIRMED', 'DELIVERED') " +
            "AND o.active = true " +
            "GROUP BY CAST(o.createdAt AS string) " +
            "ORDER BY CAST(o.createdAt AS string) ASC")
    List<SalesByPeriodResponse> findSalesByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Receita por periodo
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end " +
            "AND o.status IN ('CONFIRMED', 'DELIVERED') " +
            "AND o.active = true")
    BigDecimal findRevenueByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
