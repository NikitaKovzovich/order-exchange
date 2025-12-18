package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart", indexes = {
	@Index(name = "idx_cart_customer", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@Builder.Default
	private List<CartItem> items = new ArrayList<>();

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public void addItem(CartItem item) {
		CartItem existing = findItemByProductId(item.getProductId());
		if (existing != null) {
			existing.setQuantity(existing.getQuantity() + item.getQuantity());
			existing.calculateTotal();
		} else {
			items.add(item);
			item.setCart(this);
		}
		this.updatedAt = LocalDateTime.now();
	}

	public void removeItem(Long productId) {
		items.removeIf(item -> item.getProductId().equals(productId));
		this.updatedAt = LocalDateTime.now();
	}

	public void updateItemQuantity(Long productId, Integer quantity) {
		CartItem item = findItemByProductId(productId);
		if (item != null) {
			if (quantity <= 0) {
				removeItem(productId);
			} else {
				item.setQuantity(quantity);
				item.calculateTotal();
			}
		}
		this.updatedAt = LocalDateTime.now();
	}

	public CartItem findItemByProductId(Long productId) {
		return items.stream()
				.filter(item -> item.getProductId().equals(productId))
				.findFirst()
				.orElse(null);
	}

	public void clear() {
		items.clear();
		this.updatedAt = LocalDateTime.now();
	}

	public BigDecimal getTotalAmount() {
		return items.stream()
				.map(CartItem::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getTotalVat() {
		return items.stream()
				.map(CartItem::getVatAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public int getItemCount() {
		return items.size();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public List<Long> getSupplierIds() {
		return items.stream()
				.map(CartItem::getSupplierId)
				.distinct()
				.toList();
	}

	public List<CartItem> getItemsBySupplierId(Long supplierId) {
		return items.stream()
				.filter(item -> item.getSupplierId().equals(supplierId))
				.toList();
	}
}
