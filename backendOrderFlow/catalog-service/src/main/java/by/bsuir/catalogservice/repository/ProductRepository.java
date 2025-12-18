package by.bsuir.catalogservice.repository;

import by.bsuir.catalogservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findBySupplierId(Long supplierId);
	Page<Product> findBySupplierId(Long supplierId, Pageable pageable);

	List<Product> findBySupplierIdAndStatus(Long supplierId, Product.ProductStatus status);
	Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

	boolean existsBySupplierIdAndSku(Long supplierId, String sku);
	Optional<Product> findBySupplierIdAndSku(Long supplierId, String sku);

	@Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' AND p.category.id = :categoryId")
	Page<Product> findPublishedByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' " +
		"AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
		"AND (:supplierId IS NULL OR p.supplierId = :supplierId) " +
		"AND (:minPrice IS NULL OR p.pricePerUnit >= :minPrice) " +
		"AND (:maxPrice IS NULL OR p.pricePerUnit <= :maxPrice) " +
		"AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
	Page<Product> searchProducts(
			@Param("categoryId") Long categoryId,
			@Param("supplierId") Long supplierId,
			@Param("minPrice") BigDecimal minPrice,
			@Param("maxPrice") BigDecimal maxPrice,
			@Param("search") String search,
			Pageable pageable);

	long countBySupplierId(Long supplierId);
	long countBySupplierIdAndStatus(Long supplierId, Product.ProductStatus status);
	long countByCategoryId(Long categoryId);
	boolean existsByCategoryId(Long categoryId);
}
