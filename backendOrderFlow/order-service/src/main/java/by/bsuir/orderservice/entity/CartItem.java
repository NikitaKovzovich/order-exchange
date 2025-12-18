package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item", indexes = {
	@Index(name = "idx_cart_item_cart", columnList = "cart_id"),
	@Index(name = "idx_cart_item_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "supplier_id", nullable = false)
	private Long supplierId;

	@Column(name = "product_name", nullable = false)
	private String productName;

	@Column(name = "product_sku")
	private String productSku;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "vat_rate", precision = 5, scale = 2)
	@Builder.Default
	private BigDecimal vatRate = BigDecimal.ZERO;

	@Column(name = "total_price", precision = 12, scale = 2)
	private BigDecimal totalPrice;

	@Column(name = "vat_amount", precision = 12, scale = 2)
	private BigDecimal vatAmount;

	@Column(name = "added_at")
	@Builder.Default
	private LocalDateTime addedAt = LocalDateTime.now();

	public void calculateTotal() {
		if (unitPrice != null && quantity != null) {
			this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
			if (vatRate != null && vatRate.compareTo(BigDecimal.ZERO) > 0) {
				this.vatAmount = this.totalPrice.multiply(vatRate)
						.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
			} else {
				this.vatAmount = BigDecimal.ZERO;
			}
		}
	}

	public void updateQuantity(int newQuantity) {
		this.quantity = newQuantity;
		calculateTotal();
	}
}
