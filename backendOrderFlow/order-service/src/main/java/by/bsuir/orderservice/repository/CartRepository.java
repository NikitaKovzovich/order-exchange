package by.bsuir.orderservice.repository;

import by.bsuir.orderservice.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

	Optional<Cart> findByCustomerId(Long customerId);

	boolean existsByCustomerId(Long customerId);

	@Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId")
	Optional<Cart> findByCustomerIdWithItems(Long customerId);

	void deleteByCustomerId(Long customerId);
}
