package by.bsuir.orderservice.repository;

import by.bsuir.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

	Optional<OrderStatus> findByCode(String code);

	boolean existsByCode(String code);
}
