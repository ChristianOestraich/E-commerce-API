package project.ecommerce.repository;

import project.ecommerce.entity.Order;
import project.ecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByPreferenceId(String preferenceId);

    Optional<Payment> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);

    Optional<Payment> findByOrder(Order order);
}
