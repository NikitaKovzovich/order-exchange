package by.bsuir.orderservice.repository;

import by.bsuir.orderservice.entity.OrderDiscrepancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDiscrepancyRepository extends JpaRepository<OrderDiscrepancy, Long> {

	List<OrderDiscrepancy> findByOrderId(Long orderId);

	Optional<OrderDiscrepancy> findByOrderIdAndStatus(Long orderId, OrderDiscrepancy.DiscrepancyStatus status);

	List<OrderDiscrepancy> findByStatus(OrderDiscrepancy.DiscrepancyStatus status);
}
