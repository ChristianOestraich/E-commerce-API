package project.ecommerce.repository;

import project.ecommerce.dto.report.TopProductResponse;
import project.ecommerce.entity.CartItem;
import project.ecommerce.entity.Cart;
import project.ecommerce.entity.OrderItem;
import project.ecommerce.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT new project.ecommerce.dto.report.TopProductResponse(" +
            "i.product.id, i.product.name, SUM(i.quantity), SUM(i.subtotal)) " +
            "FROM OrderItem i " +
            "WHERE i.order.status IN ('CONFIRMED', 'DELIVERED') " +
            "AND i.order.active = true " +
            "GROUP BY i.product.id, i.product.name " +
            "ORDER BY SUM(i.quantity) DESC")
    List<TopProductResponse> findTopProducts(Pageable pageable);
}