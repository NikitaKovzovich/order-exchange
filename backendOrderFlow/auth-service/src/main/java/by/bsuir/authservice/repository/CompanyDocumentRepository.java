package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.CompanyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, Long> {
	List<CompanyDocument> findByCompanyId(Long companyId);
	Optional<CompanyDocument> findByCompanyIdAndDocumentType(Long companyId, CompanyDocument.DocumentType documentType);
}
