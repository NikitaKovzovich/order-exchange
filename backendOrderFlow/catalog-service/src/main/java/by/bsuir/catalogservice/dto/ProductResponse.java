package by.bsuir.catalogservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponse(
	Long id,
	Long supplierId,
	String sku,
	String name,
	String description,
	CategoryResponse category,
	BigDecimal pricePerUnit,
	BigDecimal priceWithVat,
	String unitName,
	String vatRateDescription,
	BigDecimal vatPercentage,
	BigDecimal weight,
	String countryOfOrigin,
	LocalDate productionDate,
	LocalDate expiryDate,
	String status,
	int availableQuantity,
	boolean inStock,
	String primaryImageUrl
) {}
