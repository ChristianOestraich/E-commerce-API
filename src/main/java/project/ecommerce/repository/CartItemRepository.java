package project.ecommerce.repository;

import project.ecommerce.entity.Cart;
import project.ecommerce.entity.CartItem;
import project.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
