package by.bsuir.orderservice.dto;

import java.time.LocalDateTime;

public record OrderDocumentResponse(
		Long id,
		String documentType,
		String documentTypeName,
		String fileKey,
		String originalFilename,
		Long uploadedBy,
		LocalDateTime uploadedAt
) {}
