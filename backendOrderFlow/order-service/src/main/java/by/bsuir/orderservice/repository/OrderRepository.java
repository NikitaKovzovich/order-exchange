package by.bsuir.orderservice.repository;

import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findByOrderNumber(String orderNumber);

	Page<Order> findBySupplierId(Long supplierId, Pageable pageable);

	Page<Order> findByCustomerId(Long customerId, Pageable pageable);

	Page<Order> findBySupplierIdAndStatus(Long supplierId, OrderStatus status, Pageable pageable);

	Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

	@Query("SELECT o FROM Order o WHERE o.supplierId = :supplierId AND o.status.code IN :statusCodes")
	Page<Order> findBySupplierIdAndStatusCodes(
			@Param("supplierId") Long supplierId,
			@Param("statusCodes") Iterable<String> statusCodes,
			Pageable pageable);

	@Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status.code IN :statusCodes")
	Page<Order> findByCustomerIdAndStatusCodes(
			@Param("customerId") Long customerId,
			@Param("statusCodes") Iterable<String> statusCodes,
			Pageable pageable);

	boolean existsByOrderNumber(String orderNumber);

	List<Order> findAllBySupplierId(Long supplierId);

	List<Order> findAllByCustomerId(Long customerId);
}
