package project.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.ecommerce.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActiveTrue();

    Optional<User> findByIdAndActiveTrue(Long id);
}
