package project.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.ecommerce.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByActiveTrue();

    Optional<Category> findByIdAndActiveTrue(Long id);
}
