package by.bsuir.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;





@Entity
@Table(name = "generated_document", indexes = {
	@Index(name = "idx_order_id", columnList = "order_id"),
	@Index(name = "idx_template_type", columnList = "template_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;




	@Convert(converter = TemplateTypeConverter.class)
	@Column(name = "template_type", nullable = false)
	private TemplateType templateType;




	@Column(name = "order_id", nullable = false)
	private Long orderId;




	@Column(name = "file_key", nullable = false, length = 1024)
	private String fileKey;




	@Column(name = "generated_at")
	@Builder.Default
	private LocalDateTime generatedAt = LocalDateTime.now();




	@Column(name = "generated_by", nullable = false)
	private Long generatedBy;




	@Column(name = "document_number", nullable = false, length = 100)
	private String documentNumber;




	@Column(name = "document_date", nullable = false)
	private LocalDate documentDate;




	public enum TemplateType {
		INVOICE("Счет на оплату", "invoice"),
		UPD("Универсальный передаточный документ", "upd"),
		TTN("Товарно-транспортная накладная", "ttn"),
		DISCREPANCY_ACT("Акт о расхождении", "discrepancy_act");

		private final String displayName;
		private final String templateName;

		TemplateType(String displayName, String templateName) {
			this.displayName = displayName;
			this.templateName = templateName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getTemplateName() {
			return templateName;
		}

		public static TemplateType fromPersistenceValue(String value) {
			if (value == null || value.isBlank()) {
				return null;
			}

			for (TemplateType type : values()) {
				if (type.name().equalsIgnoreCase(value) || type.templateName.equalsIgnoreCase(value)) {
					return type;
				}
			}

			throw new IllegalArgumentException("Unknown template type: " + value);
		}
	}






	public boolean isInvoice() {
		return templateType == TemplateType.INVOICE;
	}




	public boolean isShippingDocument() {
		return templateType == TemplateType.UPD || templateType == TemplateType.TTN;
	}




	public static String generateDocumentNumber(TemplateType type, Long orderId) {
		String prefix = switch (type) {
			case INVOICE -> "INV";
			case UPD -> "UPD";
			case TTN -> "TTN";
			case DISCREPANCY_ACT -> "ACT";
		};
		return prefix + "-" + orderId + "-" + System.currentTimeMillis() % 10000;
	}
}
