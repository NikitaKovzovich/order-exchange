package by.bsuir.documentservice.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record SupplierSummaryReportRequest(
	@NotNull String customerName,
	String periodFrom,
	String periodTo,
	@NotNull List<Row> rows,
	BigDecimal totalAmount,
	BigDecimal overallAverageCheck
) {
	public record Row(
		String supplierName,
		Integer orderCount,
		BigDecimal totalAmount,
		BigDecimal averageCheck,
		String lastOrderDate
	) {}
}
