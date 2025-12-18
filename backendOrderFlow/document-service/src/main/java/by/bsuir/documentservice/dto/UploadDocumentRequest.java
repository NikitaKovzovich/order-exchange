package by.bsuir.documentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadDocumentRequest(
		@NotBlank(message = "Document type code is required")
		String documentTypeCode,

		@NotBlank(message = "Entity type is required")
		String entityType,

		@NotNull(message = "Entity ID is required")
		Long entityId
) {}
