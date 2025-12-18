package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Товар (Read Model для CQRS)
 * Основная сущность каталога
 *
 * Жизненный цикл статусов:
 * DRAFT -> PUBLISHED -> ARCHIVED
 * PUBLISHED -> DRAFT (при редактировании)
 */
@Entity
@Table(name = "product", indexes = {
	@Index(name = "idx_supplier_id", columnList = "supplier_id"),
	@Index(name = "idx_name", columnList = "name"),
	@Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "supplier_id", nullable = false)
	private Long supplierId;

	@Column(nullable = false, length = 100)
	private String sku;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
	private BigDecimal pricePerUnit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "unit_id", nullable = false)
	private UnitOfMeasure unit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vat_rate_id", nullable = false)
	private VatRate vatRate;

	@Column(precision = 10, scale = 3)
	private BigDecimal weight;

	@Column(name = "package_dimensions", columnDefinition = "JSON")
	private String packageDimensions;

	@Column(name = "production_date")
	private LocalDate productionDate;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;

	@Column(name = "country_of_origin", length = 100)
	private String countryOfOrigin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ProductStatus status = ProductStatus.DRAFT;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@Builder.Default
	private List<ProductImage> images = new ArrayList<>();

	@OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Inventory inventory;

	/**
	 * Статусы товара
	 */
	public enum ProductStatus {
		DRAFT,      // Черновик - виден только поставщику
		PUBLISHED,  // Опубликован - виден в каталоге
		ARCHIVED    // Архивирован - снят с продажи
	}

	// ========== Бизнес-методы для CQRS ==========

	/**
	 * Опубликовать товар
	 * @throws IllegalStateException если товар уже опубликован или архивирован
	 */
	public void publish() {
		if (status == ProductStatus.ARCHIVED) {
			throw new IllegalStateException("Cannot publish archived product");
		}
		this.status = ProductStatus.PUBLISHED;
	}

	/**
	 * Перевести в черновик (для редактирования)
	 */
	public void toDraft() {
		if (status == ProductStatus.ARCHIVED) {
			throw new IllegalStateException("Cannot move archived product to draft");
		}
		this.status = ProductStatus.DRAFT;
	}

	/**
	 * Архивировать товар
	 */
	public void archive() {
		this.status = ProductStatus.ARCHIVED;
	}

	/**
	 * Проверить доступность для заказа
	 */
	public boolean isAvailable() {
		return status == ProductStatus.PUBLISHED &&
			inventory != null &&
			inventory.getQuantityAvailable() > 0;
	}

	/**
	 * Получить доступное количество
	 */
	public int getAvailableQuantity() {
		return inventory != null ? inventory.getQuantityAvailable() : 0;
	}

	/**
	 * Рассчитать цену с НДС
	 */
	public BigDecimal getPriceWithVat() {
		if (vatRate == null) return pricePerUnit;
		return vatRate.calculatePriceWithVat(pricePerUnit);
	}

	/**
	 * Проверить срок годности
	 */
	public boolean isExpired() {
		return expiryDate != null && expiryDate.isBefore(LocalDate.now());
	}

	/**
	 * Проверить, истекает ли срок годности скоро (в течение 30 дней)
	 */
	public boolean isExpiringSoon() {
		if (expiryDate == null) return false;
		LocalDate warningDate = LocalDate.now().plusDays(30);
		return expiryDate.isBefore(warningDate) && !isExpired();
	}

	/**
	 * Получить основное изображение
	 */
	public ProductImage getPrimaryImage() {
		return images.stream()
				.filter(ProductImage::getIsPrimary)
				.findFirst()
				.orElse(images.isEmpty() ? null : images.get(0));
	}
}
