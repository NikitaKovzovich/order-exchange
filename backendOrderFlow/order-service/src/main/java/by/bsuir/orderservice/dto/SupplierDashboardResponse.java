package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;





public record SupplierDashboardResponse(
		BigDecimal revenueThisMonth,
		long newOrdersToday,
		BigDecimal averageCheck,
		long ordersInTransit,
		long pendingConfirmationCount,
		List<OrderResponse> pendingConfirmationOrders,
		List<SupplierAnalyticsResponse.DailyStats> salesDynamics7Days
) {}
