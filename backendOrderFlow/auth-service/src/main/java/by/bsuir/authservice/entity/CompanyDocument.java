package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDocument {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(name = "document_type", nullable = false)
	private DocumentType documentType;

	@Column(name = "file_key", nullable = false)
	private String fileKey;

	@Column(name = "file_path", nullable = false)
	private String filePath;

	@Column(name = "original_filename")
	private String originalFilename;

	public enum DocumentType {
		LOGO,
		REGISTRATION_CERTIFICATE,
		CHARTER,
		EDS_FILE,
		SEAL_IMAGE
	}
}
