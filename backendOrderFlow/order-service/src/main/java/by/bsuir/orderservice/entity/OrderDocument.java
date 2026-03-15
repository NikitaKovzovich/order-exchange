package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;





@Entity
@Table(name = "order_document", indexes = {
	@Index(name = "idx_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(name = "document_type", nullable = false)
	private DocumentType documentType;




	@Column(name = "file_key", nullable = false, length = 1024)
	private String fileKey;




	@Column(name = "original_filename")
	private String originalFilename;




	@Column(name = "uploaded_by")
	private Long uploadedBy;

	@Column(name = "uploaded_at")
	@Builder.Default
	private LocalDateTime uploadedAt = LocalDateTime.now();




	public enum DocumentType {
		INVOICE("Счет на оплату"),
		PAYMENT_PROOF("Платежное поручение"),
		UPD("Универсальный передаточный документ"),
		SIGNED_UPD("Подписанный УПД"),
		TTN("Товарно-транспортная накладная"),
		CORRECTION_TTN("Корректировочная ТТН"),
		DISCREPANCY_ACT("Акт о расхождении");

		private final String displayName;

		DocumentType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}






	public boolean isInvoice() {
		return documentType == DocumentType.INVOICE;
	}




	public boolean isPaymentProof() {
		return documentType == DocumentType.PAYMENT_PROOF;
	}




	public boolean isShippingDocument() {
		return documentType == DocumentType.UPD ||
			documentType == DocumentType.TTN ||
			documentType == DocumentType.SIGNED_UPD;
	}
}
