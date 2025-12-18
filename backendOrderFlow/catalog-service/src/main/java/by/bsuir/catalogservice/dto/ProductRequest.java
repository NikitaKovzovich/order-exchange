package by.bsuir.catalogservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductRequest(
	@NotBlank(message = "SKU is required")
	@Size(max = 100, message = "SKU must not exceed 100 characters")
	String sku,

	@NotBlank(message = "Product name is required")
	@Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
	String name,

	String description,

	@NotNull(message = "Category is required")
	Long categoryId,

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.01", message = "Price must be greater than 0")
	BigDecimal pricePerUnit,

	@NotNull(message = "Unit of measure is required")
	Long unitId,

	@NotNull(message = "VAT rate is required")
	Long vatRateId,

	@DecimalMin(value = "0", message = "Weight must be non-negative")
	BigDecimal weight,

	String countryOfOrigin,
	LocalDate productionDate,
	LocalDate expiryDate,

	@Min(value = 0, message = "Initial quantity must be non-negative")
	Integer initialQuantity
) {}
