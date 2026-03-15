package by.bsuir.documentservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;




public record GeneratedDocumentResponse(
	Long id,
	String templateType,
	String templateDisplayName,
	Long orderId,
	String documentNumber,
	LocalDate documentDate,
	String fileKey,
	LocalDateTime generatedAt,
	Long generatedBy
) {}
