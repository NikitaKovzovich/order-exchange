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

	boolean existsByOrderId(Long orderId);
}
