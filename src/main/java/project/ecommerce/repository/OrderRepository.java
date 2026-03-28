package project.ecommerce.repository;

import project.ecommerce.entity.Order;
import project.ecommerce.entity.User;
import project.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserAndActiveTrue(User user, Pageable pageable);

    Page<Order> findByActiveTrue(Pageable pageable);

    Optional<Order> findByIdAndActiveTrue(Long id);

    Page<Order> findByUserAndStatusAndActiveTrue(User user, OrderStatus status, Pageable pageable);

}
