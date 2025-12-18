package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record RetailAnalyticsResponse(
		KpiStats kpi,
		List<DailyExpenses> expensesDynamics,
		ExpenseStructure expenseStructure,
		List<SupplierStats> supplierAnalytics,
		List<ProductPurchaseHistory> productHistory
) {
	public record KpiStats(
			BigDecimal totalExpenses,
			long orderCount,
			long supplierCount,
			BigDecimal averageCheck,
			String period
	) {}

	public record DailyExpenses(
			String date,
			BigDecimal expenses,
			long orderCount
	) {}

	public record ExpenseStructure(
			List<CategoryExpense> byCategory,
			List<SupplierExpense> bySupplier
	) {}

	public record CategoryExpense(
			String categoryName,
			BigDecimal amount,
			double percent
	) {}

	public record SupplierExpense(
			Long supplierId,
			String supplierName,
			BigDecimal amount,
			double percent
	) {}

	public record SupplierStats(
			Long supplierId,
			String supplierName,
			long orderCount,
			BigDecimal totalAmount,
			BigDecimal averageCheck,
			String lastOrderDate
	) {}

	public record ProductPurchaseHistory(
			Long productId,
			String productName,
			String productSku,
			List<PurchaseRecord> purchases
	) {}

	public record PurchaseRecord(
			String date,
			Long supplierId,
			String supplierName,
			int quantity,
			BigDecimal unitPrice,
			BigDecimal totalPrice
	) {}
}
