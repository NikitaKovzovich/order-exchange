package by.bsuir.documentservice.repository;

import by.bsuir.documentservice.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {

	Optional<DocumentType> findByCode(String code);

	boolean existsByCode(String code);
}
