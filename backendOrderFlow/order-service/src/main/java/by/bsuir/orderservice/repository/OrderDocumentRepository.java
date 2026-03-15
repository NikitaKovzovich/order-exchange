package by.bsuir.orderservice.repository;

import by.bsuir.orderservice.entity.OrderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDocumentRepository extends JpaRepository<OrderDocument, Long> {

	List<OrderDocument> findByOrderIdOrderByUploadedAtDesc(Long orderId);

	List<OrderDocument> findByOrderIdAndDocumentType(Long orderId, OrderDocument.DocumentType documentType);
}
