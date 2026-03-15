package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;





@Entity
@Table(name = "partnership", indexes = {
	@Index(name = "idx_partnership_supplier", columnList = "supplier_id"),
	@Index(name = "idx_partnership_customer", columnList = "customer_id")
}, uniqueConstraints = {
	@UniqueConstraint(name = "uk_supplier_customer", columnNames = {"supplier_id", "customer_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partnership {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "supplier_id", nullable = false)
	private Long supplierId;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private PartnershipStatus status = PartnershipStatus.PENDING;

	@Column(name = "contract_number", length = 100)
	private String contractNumber;

	@Column(name = "contract_date")
	private LocalDate contractDate;

	@Column(name = "contract_end_date")
	private LocalDate contractEndDate;

	@Column(name = "customer_company_name")
	private String customerCompanyName;

	@Column(name = "customer_unp", length = 20)
	private String customerUnp;

	@Column(name = "supplier_company_name")
	private String supplierCompanyName;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public enum PartnershipStatus {
		PENDING,
		ACTIVE,
		REJECTED
	}



	public void accept() {
		if (this.status != PartnershipStatus.PENDING) {
			throw new IllegalStateException("Can only accept PENDING partnership");
		}
		this.status = PartnershipStatus.ACTIVE;
		this.updatedAt = LocalDateTime.now();
	}

	public void reject() {
		if (this.status != PartnershipStatus.PENDING) {
			throw new IllegalStateException("Can only reject PENDING partnership");
		}
		this.status = PartnershipStatus.REJECTED;
		this.updatedAt = LocalDateTime.now();
	}

	public void updateContract(String contractNumber, LocalDate contractDate, LocalDate contractEndDate) {
		this.contractNumber = contractNumber;
		this.contractDate = contractDate;
		this.contractEndDate = contractEndDate;
		this.updatedAt = LocalDateTime.now();
	}

	public boolean isActive() {
		return this.status == PartnershipStatus.ACTIVE;
	}
}
