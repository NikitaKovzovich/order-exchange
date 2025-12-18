package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record SupplierAnalyticsResponse(
		KpiStats kpi,
		FunnelStats funnel,
		List<DailyStats> salesDynamics,
		ProductAnalytics productAnalytics,
		List<CustomerStats> customerAnalytics
) {
	public record KpiStats(
			BigDecimal revenue,
			long orderCount,
			BigDecimal averageCheck,
			long shippedUnits,
			String period
	) {}

	public record FunnelStats(
			long pendingConfirmation,
			long confirmed,
			long rejected,
			long awaitingPayment,
			long pendingPaymentVerification,
			long paid,
			long paymentProblem,
			long awaitingShipment,
			long shipped,
			long delivered,
			long closed
	) {}

	public record DailyStats(
			String date,
			BigDecimal revenue,
			long orderCount
	) {}

	public record ProductAnalytics(
			List<TopProductByRevenue> topByRevenue,
			List<TopProductByQuantity> topByQuantity,
			List<AbcProduct> abcAnalysis,
			List<LowStockProduct> lowStock
	) {}

	public record TopProductByRevenue(
			Long productId,
			String productName,
			String productSku,
			BigDecimal revenue,
			int quantity
	) {}

	public record TopProductByQuantity(
			Long productId,
			String productName,
			String productSku,
			int quantity,
			BigDecimal revenue
	) {}

	public record AbcProduct(
			Long productId,
			String productName,
			String category,
			BigDecimal revenue,
			double revenuePercent,
			double cumulativePercent
	) {}

	public record LowStockProduct(
			Long productId,
			String productName,
			String productSku,
			int currentStock,
			int minStock,
			int avgDailySales
	) {}

	public record CustomerStats(
			Long customerId,
			String customerName,
			long orderCount,
			BigDecimal totalRevenue,
			BigDecimal averageCheck,
			String lastOrderDate
	) {}
}
