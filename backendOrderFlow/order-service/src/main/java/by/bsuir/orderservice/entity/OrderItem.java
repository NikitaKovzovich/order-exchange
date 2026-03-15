package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;




@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "product_id", nullable = false)
	private Long productId;




	@Column(name = "product_name")
	private String productName;




	@Column(name = "product_sku")
	private String productSku;




	@Column(name = "unit_price", precision = 10, scale = 2)
	private BigDecimal unitPrice;




	@Column(name = "vat_rate", precision = 5, scale = 2)
	@Builder.Default
	private BigDecimal vatRate = BigDecimal.ZERO;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "total_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalPrice;

	@Column(name = "line_total", precision = 12, scale = 2)
	private BigDecimal lineTotal;

	@Column(name = "line_vat", precision = 12, scale = 2)
	private BigDecimal lineVat;




	@Column(name = "received_quantity")
	private Integer receivedQuantity;






	public void calculateTotals() {
		if (unitPrice != null && quantity != null) {
			this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
			this.totalPrice = this.lineTotal;
			if (vatRate != null && vatRate.compareTo(BigDecimal.ZERO) > 0) {
				this.lineVat = this.lineTotal.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
			} else {
				this.lineVat = BigDecimal.ZERO;
			}
		}
	}




	public void calculateTotalPrice() {
		if (unitPrice != null && quantity != null) {
			this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
		}
	}




	public void setQuantityAndRecalculate(int quantity) {
		this.quantity = quantity;
		calculateTotalPrice();
	}




	public boolean hasDiscrepancy() {
		return receivedQuantity != null && !receivedQuantity.equals(quantity);
	}




	public int getQuantityDifference() {
		if (receivedQuantity == null) return 0;
		return quantity - receivedQuantity;
	}




	public void confirmFullReceipt() {
		this.receivedQuantity = this.quantity;
	}




	public void confirmPartialReceipt(int actualQuantity) {
		if (actualQuantity < 0 || actualQuantity > quantity) {
			throw new IllegalArgumentException("Invalid received quantity");
		}
		this.receivedQuantity = actualQuantity;
	}
}
