package project.ecommerce.repository;

import project.ecommerce.entity.Address;
import project.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);

    Optional<Address> findByUserAndMainTrue(User user);

    Optional<Address> findByIdAndUser(Long id, User user);
}
