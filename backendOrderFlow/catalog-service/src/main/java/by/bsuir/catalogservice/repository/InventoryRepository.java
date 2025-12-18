package by.bsuir.catalogservice.repository;

import by.bsuir.catalogservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	@Query("SELECT i FROM Inventory i WHERE i.quantityAvailable - i.reservedQuantity < :threshold")
	List<Inventory> findLowStock(@Param("threshold") int threshold);

	@Query("SELECT i FROM Inventory i WHERE i.quantityAvailable - i.reservedQuantity <= 0")
	List<Inventory> findOutOfStock();

	@Modifying
	@Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :quantity WHERE i.productId = :productId AND i.quantityAvailable - i.reservedQuantity >= :quantity")
	int reserveStock(@Param("productId") Long productId, @Param("quantity") int quantity);

	@Modifying
	@Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity WHERE i.productId = :productId AND i.reservedQuantity >= :quantity")
	int releaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
