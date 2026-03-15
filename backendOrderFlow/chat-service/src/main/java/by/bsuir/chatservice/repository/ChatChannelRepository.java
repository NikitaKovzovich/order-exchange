package by.bsuir.chatservice.repository;

import by.bsuir.chatservice.entity.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

	Optional<ChatChannel> findByOrderId(Long orderId);

	@Query("SELECT c FROM ChatChannel c WHERE c.supplierUserId = :userId OR c.customerUserId = :userId ORDER BY c.createdAt DESC")
	List<ChatChannel> findByUserId(Long userId);

	@Query("SELECT c FROM ChatChannel c WHERE (c.supplierUserId = :userId OR c.customerUserId = :userId) AND c.isActive = true ORDER BY c.createdAt DESC")
	List<ChatChannel> findActiveByUserId(Long userId);

	@Query("SELECT c FROM ChatChannel c WHERE (c.supplierUserId = :userId OR c.customerUserId = :userId) " +
		"AND c.isActive = true " +
		"AND (LOWER(c.channelName) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(c.orderId AS string) LIKE CONCAT('%', :search, '%')) " +
		"ORDER BY c.createdAt DESC")
	List<ChatChannel> findActiveByUserIdAndSearch(Long userId, String search);

	@Query("SELECT DISTINCT c FROM ChatChannel c JOIN c.messages m WHERE m.senderId = :userId AND c.isActive = true ORDER BY c.createdAt DESC")
	List<ChatChannel> findActiveChannelsBySenderId(Long userId);

	boolean existsByOrderId(Long orderId);
}
