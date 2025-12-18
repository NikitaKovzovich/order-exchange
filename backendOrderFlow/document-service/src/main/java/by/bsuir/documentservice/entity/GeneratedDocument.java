package by.bsuir.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сгенерированный документ (Read Model для CQRS)
 * PDF документы, сгенерированные системой (счета, УПД, ТТН)
 */
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

	/**
	 * Тип шаблона
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "template_type", nullable = false)
	private TemplateType templateType;

	/**
	 * ID заказа
	 */
	@Column(name = "order_id", nullable = false)
	private Long orderId;

	/**
	 * Ключ файла в S3/MinIO
	 */
	@Column(name = "file_key", nullable = false, length = 1024)
	private String fileKey;

	/**
	 * Время генерации
	 */
	@Column(name = "generated_at")
	@Builder.Default
	private LocalDateTime generatedAt = LocalDateTime.now();

	/**
	 * ID пользователя или системы, сгенерировавшей документ
	 */
	@Column(name = "generated_by", nullable = false)
	private Long generatedBy;

	/**
	 * Номер документа
	 */
	@Column(name = "document_number", nullable = false, length = 100)
	private String documentNumber;

	/**
	 * Дата документа
	 */
	@Column(name = "document_date", nullable = false)
	private LocalDate documentDate;

	/**
	 * Типы шаблонов документов
	 */
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
	}

	// ========== Бизнес-методы ==========

	/**
	 * Проверить, является ли счетом
	 */
	public boolean isInvoice() {
		return templateType == TemplateType.INVOICE;
	}

	/**
	 * Проверить, является ли отгрузочным документом
	 */
	public boolean isShippingDocument() {
		return templateType == TemplateType.UPD || templateType == TemplateType.TTN;
	}

	/**
	 * Сгенерировать номер документа
	 */
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
