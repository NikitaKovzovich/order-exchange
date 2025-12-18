package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierSettings {
	@Id
	@Column(name = "company_id")
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "company_id")
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_terms", nullable = false)
	private PaymentTerms paymentTerms;

	public enum PaymentTerms {
		prepayment_100,
		payment_on_shipment
	}

	public static PaymentTerms fromString(String value) {
		if (value == null) {
			return null;
		}
		return switch (value.toLowerCase()) {
			case "prepaid", "prepayment", "prepayment_100" -> PaymentTerms.prepayment_100;
			case "postpaid", "payment_on_shipment" -> PaymentTerms.payment_on_shipment;
			default -> throw new IllegalArgumentException("Unknown payment terms: " + value);
		};
	}
}
