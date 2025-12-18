package by.bsuir.documentservice.dto;

import java.math.BigDecimal;

public record ProductItemDto(
		Integer lineNumber,
		String name,
		String sku,
		String unitOfMeasure,
		Integer quantity,
		BigDecimal priceWithoutVat,
		BigDecimal priceWithVat,
		BigDecimal vatRate,
		BigDecimal vatAmount,
		BigDecimal totalWithoutVat,
		BigDecimal totalWithVat,
		BigDecimal grossWeight,
		Integer packageCount
) {}
