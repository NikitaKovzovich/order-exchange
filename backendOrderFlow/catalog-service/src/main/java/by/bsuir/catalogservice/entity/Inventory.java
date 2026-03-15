package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;





@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

	@Id
	@Column(name = "product_id")
	private Long productId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(name = "quantity_available", nullable = false)
	@Builder.Default
	private Integer quantityAvailable = 0;

	@Column(name = "reserved_quantity")
	@Builder.Default
	private Integer reservedQuantity = 0;






	public void addStock(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("Quantity must be positive");
		}
		this.quantityAvailable += quantity;
	}





	public void reserve(int quantity) {
		if (quantity > getActualAvailable()) {
			throw new IllegalStateException("Insufficient stock. Available: " + getActualAvailable() + ", requested: " + quantity);
		}
		this.reservedQuantity += quantity;
	}




	public void cancelReservation(int quantity) {
		this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
	}




	public void shipReserved(int quantity) {
		if (quantity > reservedQuantity) {
			throw new IllegalStateException("Cannot ship more than reserved");
		}
		this.reservedQuantity -= quantity;
		this.quantityAvailable -= quantity;
	}




	public int getActualAvailable() {
		return quantityAvailable - reservedQuantity;
	}




	public boolean hasEnough(int quantity) {
		return getActualAvailable() >= quantity;
	}




	public boolean isLowStock() {
		return getActualAvailable() < 10;
	}




	public boolean isOutOfStock() {
		return getActualAvailable() <= 0;
	}
}
