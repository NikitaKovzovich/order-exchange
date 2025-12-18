package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "discrepancy_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscrepancyItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discrepancy_id", nullable = false)
	private OrderDiscrepancy discrepancy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_item_id", nullable = false)
	private OrderItem orderItem;

	@Column(name = "expected_quantity", nullable = false)
	private Integer expectedQuantity;

	@Column(name = "actual_quantity", nullable = false)
	private Integer actualQuantity;

	@Column(name = "discrepancy_quantity", nullable = false)
	private Integer discrepancyQuantity;

	@Column(name = "unit_price", precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "discrepancy_amount", precision = 12, scale = 2)
	private BigDecimal discrepancyAmount;

	@Column
	private String reason;

	public void calculate() {
		this.discrepancyQuantity = this.expectedQuantity - this.actualQuantity;
		if (this.unitPrice != null) {
			this.discrepancyAmount = this.unitPrice.multiply(BigDecimal.valueOf(Math.abs(this.discrepancyQuantity)));
		}
	}
}
