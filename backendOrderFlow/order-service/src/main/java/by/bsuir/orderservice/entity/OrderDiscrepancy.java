package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_discrepancy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDiscrepancy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@OneToMany(mappedBy = "discrepancy", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<DiscrepancyItem> items = new ArrayList<>();

	@Column(name = "total_discrepancy_amount", precision = 12, scale = 2)
	private BigDecimal totalDiscrepancyAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private DiscrepancyStatus status = DiscrepancyStatus.PENDING;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	@Column(name = "resolved_by")
	private Long resolvedBy;

	public enum DiscrepancyStatus {
		PENDING,
		ACCEPTED,
		REJECTED,
		RESOLVED
	}

	public void addItem(DiscrepancyItem item) {
		items.add(item);
		item.setDiscrepancy(this);
	}

	public void calculateTotal() {
		this.totalDiscrepancyAmount = items.stream()
				.map(DiscrepancyItem::getDiscrepancyAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
