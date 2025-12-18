package by.bsuir.chatservice.repository;

import by.bsuir.chatservice.entity.TicketMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

	Page<TicketMessage> findByTicketIdOrderBySentAtAsc(Long ticketId, Pageable pageable);

	List<TicketMessage> findByTicketIdOrderBySentAtAsc(Long ticketId);

	@Query("SELECT m FROM TicketMessage m WHERE m.ticket.id = :ticketId AND m.isInternalNote = false ORDER BY m.sentAt ASC")
	List<TicketMessage> findPublicMessagesByTicketId(Long ticketId);

	long countByTicketId(Long ticketId);
}
