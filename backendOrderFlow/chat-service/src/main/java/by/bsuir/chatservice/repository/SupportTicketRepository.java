package by.bsuir.chatservice.repository;

import by.bsuir.chatservice.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

	Page<SupportTicket> findByRequesterCompanyId(Long companyId, Pageable pageable);

	Page<SupportTicket> findByRequesterUserId(Long userId, Pageable pageable);

	Page<SupportTicket> findByStatus(SupportTicket.TicketStatus status, Pageable pageable);

	Page<SupportTicket> findByAssignedAdminId(Long adminId, Pageable pageable);

	@Query("SELECT t FROM SupportTicket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED')")
	Page<SupportTicket> findAllOpen(Pageable pageable);

	@Query("SELECT t FROM SupportTicket t WHERE t.assignedAdminId IS NULL AND t.status = 'NEW'")
	List<SupportTicket> findUnassigned();

	long countByStatus(SupportTicket.TicketStatus status);

	long countByAssignedAdminIdAndStatus(Long adminId, SupportTicket.TicketStatus status);
}
