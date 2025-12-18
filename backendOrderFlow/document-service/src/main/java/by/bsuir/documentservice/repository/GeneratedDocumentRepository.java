package by.bsuir.documentservice.repository;

import by.bsuir.documentservice.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {

	List<GeneratedDocument> findByOrderId(Long orderId);

	Optional<GeneratedDocument> findByOrderIdAndTemplateType(Long orderId, GeneratedDocument.TemplateType templateType);

	boolean existsByOrderIdAndTemplateType(Long orderId, GeneratedDocument.TemplateType templateType);
}
