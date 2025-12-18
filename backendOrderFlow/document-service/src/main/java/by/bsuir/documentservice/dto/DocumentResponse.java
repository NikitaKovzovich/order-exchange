package by.bsuir.documentservice.dto;

import java.time.LocalDateTime;

public record DocumentResponse(
		Long id,
		String documentTypeCode,
		String documentTypeName,
		String entityType,
		Long entityId,
		String fileName,
		String fileKey,
		Long fileSize,
		String mimeType,
		Long uploadedBy,
		LocalDateTime createdAt
) {}
