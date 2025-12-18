package by.bsuir.documentservice.dto;

import java.math.BigDecimal;

public record DiscrepancyItemDto(
		Integer lineNumber,
		String productName,
		String productSku,
		String unitOfMeasure,
		Integer expectedQuantity,
		Integer actualQuantity,
		Integer discrepancy,
		BigDecimal unitPrice,
		BigDecimal discrepancyAmount,
		DiscrepancyType discrepancyType,
		String note
) {
	public enum DiscrepancyType {
		SHORTAGE,
		SURPLUS,
		DAMAGE,
		QUALITY,
		OTHER
	}
}
