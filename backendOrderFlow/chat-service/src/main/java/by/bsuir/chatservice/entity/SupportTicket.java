package by.bsuir.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_ticket", indexes = {
	@Index(name = "idx_requester_company_id", columnList = "requester_company_id"),
	@Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "requester_company_id", nullable = false)
	private Long requesterCompanyId;

	@Column(name = "requester_user_id", nullable = false)
	private Long requesterUserId;

	@Column(nullable = false)
	private String subject;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private TicketStatus status = TicketStatus.NEW;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private TicketPriority priority = TicketPriority.NORMAL;

	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private TicketCategory category;

	@Column(name = "assigned_admin_id")
	private Long assignedAdminId;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<TicketMessage> messages = new ArrayList<>();

	public enum TicketStatus {
		NEW,
		IN_PROGRESS,
		WAITING_FOR_CUSTOMER,
		RESOLVED,
		CLOSED
	}

	public enum TicketPriority {
		LOW,
		NORMAL,
		HIGH,
		URGENT
	}

	public enum TicketCategory {
		TECHNICAL_ISSUE,
		PAYMENT_ISSUE,
		ORDER_ISSUE,
		ACCOUNT_ISSUE,
		VERIFICATION_ISSUE,
		OTHER
	}

	public void assignTo(Long adminId) {
		this.assignedAdminId = adminId;
		this.status = TicketStatus.IN_PROGRESS;
		this.updatedAt = LocalDateTime.now();
	}

	public void waitForCustomer() {
		this.status = TicketStatus.WAITING_FOR_CUSTOMER;
		this.updatedAt = LocalDateTime.now();
	}

	public void resolve() {
		this.status = TicketStatus.RESOLVED;
		this.resolvedAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public void close() {
		this.status = TicketStatus.CLOSED;
		this.updatedAt = LocalDateTime.now();
	}

	public void reopen() {
		this.status = TicketStatus.IN_PROGRESS;
		this.resolvedAt = null;
		this.updatedAt = LocalDateTime.now();
	}

	public boolean isOpen() {
		return status != TicketStatus.RESOLVED && status != TicketStatus.CLOSED;
	}
}
