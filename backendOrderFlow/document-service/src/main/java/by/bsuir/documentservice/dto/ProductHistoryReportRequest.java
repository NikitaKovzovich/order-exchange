package by.bsuir.documentservice.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ProductHistoryReportRequest(
	@NotNull String productName,
	String productSku,
	String periodFrom,
	String periodTo,
	@NotNull List<Row> rows,
	BigDecimal minPrice,
	BigDecimal maxPrice
) {
	public record Row(
		String date,
		String supplierName,
		BigDecimal quantity,
		BigDecimal unitPrice,
		BigDecimal totalPrice
	) {}
}
