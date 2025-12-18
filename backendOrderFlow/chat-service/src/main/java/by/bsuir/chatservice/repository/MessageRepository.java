package by.bsuir.chatservice.repository;

import by.bsuir.chatservice.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

	Page<Message> findByChannelIdOrderBySentAtDesc(Long channelId, Pageable pageable);

	List<Message> findByChannelIdOrderBySentAtAsc(Long channelId);

	@Query("SELECT COUNT(m) FROM Message m WHERE m.channel.id = :channelId AND m.senderId != :userId AND m.isRead = false")
	long countUnreadMessages(Long channelId, Long userId);

	@Modifying
	@Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.channel.id = :channelId AND m.senderId != :userId AND m.isRead = false")
	int markAsRead(Long channelId, Long userId);
}
