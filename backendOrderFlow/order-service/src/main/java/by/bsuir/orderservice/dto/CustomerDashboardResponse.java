package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;





public record CustomerDashboardResponse(
		BigDecimal expensesThisMonth,
		long orderCount,
		long activeContractsCount,
		BigDecimal averageCheck,
		List<OrderResponse> recentOrders,
		RetailAnalyticsResponse.ExpenseStructure expenseStructure,
		List<RetailAnalyticsResponse.DailyExpenses> expensesDynamics7Days
) {}
