package by.bsuir.orderservice.repository;

import by.bsuir.orderservice.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

	List<OrderHistory> findByOrderIdOrderByTimestampDesc(Long orderId);

	List<OrderHistory> findByOrderIdOrderByTimestampAsc(Long orderId);
}
