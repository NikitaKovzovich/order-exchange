package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(name = "address_type", nullable = false)
	private AddressType addressType;

	@Lob
	@Column(name = "full_address", nullable = false)
	private String fullAddress;

	@Builder.Default
	@Column(name = "is_default")
	private Boolean isDefault = false;

	public enum AddressType {
		legal,
		postal,
		shipping,
		delivery
	}
}
