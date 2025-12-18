package by.bsuir.documentservice.repository;

import by.bsuir.documentservice.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

	List<Document> findByEntityTypeAndEntityId(String entityType, Long entityId);

	Optional<Document> findByFileKey(String fileKey);

	boolean existsByFileKey(String fileKey);

	void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
