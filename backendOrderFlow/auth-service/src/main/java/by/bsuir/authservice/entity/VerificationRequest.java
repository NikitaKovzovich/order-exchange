package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "verification_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VerificationStatus status;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "requested_at")
	private LocalDateTime requestedAt;

	@Column(name = "reviewed_at")
	private LocalDateTime reviewedAt;

	@ManyToOne
	@JoinColumn(name = "reviewer_id")
	private User reviewer;

	@Column(name = "rejection_reason")
	private String rejectionReason;

	@OneToMany(mappedBy = "verificationRequest", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<VerificationDocument> documents;

	public enum VerificationStatus {
		PENDING,
		APPROVED,
		REJECTED
	}

	public List<VerificationDocument> getDocuments() {
		return documents;
	}
}
