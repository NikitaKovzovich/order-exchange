package by.bsuir.catalogservice.dto;

public record ProductImageResponse(
	Long id,
	Long productId,
	String fileName,
	String mimeType,
	long sizeKb,
	boolean isPrimary,
	String url
) {}
