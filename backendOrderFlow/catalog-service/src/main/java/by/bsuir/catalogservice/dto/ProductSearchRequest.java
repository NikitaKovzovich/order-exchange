package by.bsuir.catalogservice.dto;

import java.math.BigDecimal;

public record ProductSearchRequest(
	Long categoryId,
	Long supplierId,
	BigDecimal minPrice,
	BigDecimal maxPrice,
	String search,
	int page,
	int size,
	String sortBy,
	String sortDir
) {
	public ProductSearchRequest {
		if (page < 0) page = 0;
		if (size <= 0) size = 20;
		if (sortBy == null || sortBy.isBlank()) sortBy = "name";
		if (sortDir == null || sortDir.isBlank()) sortDir = "asc";
	}
}
