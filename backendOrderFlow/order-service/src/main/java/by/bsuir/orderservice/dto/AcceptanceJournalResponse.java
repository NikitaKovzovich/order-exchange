package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;




public record AcceptanceJournalResponse(
		List<AcceptanceDetailRecord> details,
		List<AcceptanceSummaryRecord> summary,
		BigDecimal grandTotalQuantity,
		BigDecimal grandTotalAmount
) {}
