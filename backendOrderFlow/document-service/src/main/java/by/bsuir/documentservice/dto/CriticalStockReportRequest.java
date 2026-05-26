package by.bsuir.documentservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriticalStockReportRequest(
	@NotNull String supplierName,
	@NotNull List<Item> items
) {
	public record Item(
		String sku,
		String name,
		Integer currentStock,
		Integer minThreshold,
		Integer deficit
	) {}
}
