package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationDocument {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "verification_request_id", nullable = false)
	private VerificationRequest verificationRequest;

	@Column(name = "document_name", nullable = false)
	private String documentName;

	@Column(name = "document_path", nullable = false)
	private String documentPath;

	@Column(name = "document_type", nullable = false)
	private String documentType;

	@Column(name = "uploaded_at")
	private LocalDateTime uploadedAt;
}
