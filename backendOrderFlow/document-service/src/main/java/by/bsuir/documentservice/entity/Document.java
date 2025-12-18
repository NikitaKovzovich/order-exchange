package by.bsuir.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Документ (Read Model для CQRS)
 * Метаданные файлов, хранящихся в S3/MinIO
 */
@Entity
@Table(name = "document", indexes = {
	@Index(name = "idx_entity", columnList = "entity_type, entity_id"),
	@Index(name = "idx_uploaded_by", columnList = "uploaded_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_type_id", nullable = false)
	private DocumentType documentType;

	/**
	 * Тип сущности, к которой привязан документ (order, company, verification)
	 */
	@Column(name = "entity_type", nullable = false, length = 50)
	private String entityType;

	/**
	 * ID сущности
	 */
	@Column(name = "entity_id", nullable = false)
	private Long entityId;

	/**
	 * Оригинальное имя файла
	 */
	@Column(name = "file_name", nullable = false)
	private String fileName;

	/**
	 * Ключ в S3/MinIO
	 */
	@Column(name = "file_key", nullable = false, length = 1024)
	private String fileKey;

	/**
	 * Размер файла в байтах
	 */
	@Column(name = "file_size", nullable = false)
	private Long fileSize;

	/**
	 * MIME тип файла
	 */
	@Column(name = "mime_type", nullable = false, length = 100)
	private String mimeType;

	/**
	 * ID пользователя, загрузившего документ
	 */
	@Column(name = "uploaded_by", nullable = false)
	private Long uploadedBy;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	/**
	 * Типы сущностей
	 */
	public static class EntityTypes {
		public static final String ORDER = "order";
		public static final String COMPANY = "company";
		public static final String VERIFICATION = "verification";
	}

	// ========== Бизнес-методы ==========

	/**
	 * Получить размер в KB
	 */
	public long getFileSizeKb() {
		return fileSize / 1024;
	}

	/**
	 * Получить размер в MB
	 */
	public double getFileSizeMb() {
		return fileSize / (1024.0 * 1024.0);
	}

	/**
	 * Проверить, является ли документ изображением
	 */
	public boolean isImage() {
		return mimeType != null && mimeType.startsWith("image/");
	}

	/**
	 * Проверить, является ли документ PDF
	 */
	public boolean isPdf() {
		return "application/pdf".equals(mimeType);
	}

	/**
	 * Сгенерировать URL для скачивания
	 */
	public String getDownloadUrl(String baseUrl) {
		return baseUrl + "/api/documents/" + id + "/download";
	}
}
