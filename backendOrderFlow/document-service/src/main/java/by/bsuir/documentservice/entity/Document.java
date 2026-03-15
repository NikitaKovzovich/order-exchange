package by.bsuir.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;





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




	@Column(name = "entity_type", nullable = false, length = 50)
	private String entityType;




	@Column(name = "entity_id", nullable = false)
	private Long entityId;




	@Column(name = "file_name", nullable = false)
	private String fileName;




	@Column(name = "file_key", nullable = false, length = 1024)
	private String fileKey;




	@Column(name = "file_size", nullable = false)
	private Long fileSize;




	@Column(name = "mime_type", nullable = false, length = 100)
	private String mimeType;




	@Column(name = "uploaded_by", nullable = false)
	private Long uploadedBy;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();




	public static class EntityTypes {
		public static final String ORDER = "order";
		public static final String COMPANY = "company";
		public static final String VERIFICATION = "verification";
	}






	public long getFileSizeKb() {
		return fileSize / 1024;
	}




	public double getFileSizeMb() {
		return fileSize / (1024.0 * 1024.0);
	}




	public boolean isImage() {
		return mimeType != null && mimeType.startsWith("image/");
	}




	public boolean isPdf() {
		return "application/pdf".equals(mimeType);
	}




	public String getDownloadUrl(String baseUrl) {
		return baseUrl + "/api/documents/" + id + "/download";
	}
}
