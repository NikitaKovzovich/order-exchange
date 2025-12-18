package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AnalyticsResponse(
		OrderStats orderStats,
		RevenueStats revenueStats,
		List<TopProduct> topProducts,
		List<TopSupplier> topSuppliers,
		Map<String, Long> ordersByStatus,
		List<DailyStats> dailyStats
) {
	public record OrderStats(
			long totalOrders,
			long pendingOrders,
			long completedOrders,
			long cancelledOrders,
			double completionRate
	) {}

	public record RevenueStats(
			BigDecimal totalRevenue,
			BigDecimal averageOrderValue,
			BigDecimal totalVat,
			BigDecimal revenueThisMonth,
			double growthPercent
	) {}

	public record TopProduct(
			Long productId,
			String productName,
			long orderCount,
			int totalQuantity,
			BigDecimal totalRevenue
	) {}

	public record TopSupplier(
			Long supplierId,
			long orderCount,
			BigDecimal totalRevenue
	) {}

	public record DailyStats(
			String date,
			long orderCount,
			BigDecimal revenue
	) {}
}
