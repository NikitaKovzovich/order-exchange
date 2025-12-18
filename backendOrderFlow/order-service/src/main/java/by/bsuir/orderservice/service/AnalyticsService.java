package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.AnalyticsResponse;
import by.bsuir.orderservice.dto.AnalyticsResponse.*;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderItem;
import by.bsuir.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

	private final OrderRepository orderRepository;

	public AnalyticsResponse getOverallAnalytics() {
		List<Order> allOrders = orderRepository.findAll();

		OrderStats orderStats = calculateOrderStats(allOrders);
		RevenueStats revenueStats = calculateRevenueStats(allOrders);
		List<TopProduct> topProducts = calculateTopProducts(allOrders);
		List<TopSupplier> topSuppliers = calculateTopSuppliers(allOrders);
		Map<String, Long> ordersByStatus = calculateOrdersByStatus(allOrders);
		List<DailyStats> dailyStats = calculateDailyStats(allOrders);

		return new AnalyticsResponse(
				orderStats,
				revenueStats,
				topProducts,
				topSuppliers,
				ordersByStatus,
				dailyStats
		);
	}

	public AnalyticsResponse getSupplierAnalytics(Long supplierId) {
		List<Order> orders = orderRepository.findAllBySupplierId(supplierId);

		OrderStats orderStats = calculateOrderStats(orders);
		RevenueStats revenueStats = calculateRevenueStats(orders);
		List<TopProduct> topProducts = calculateTopProducts(orders);
		Map<String, Long> ordersByStatus = calculateOrdersByStatus(orders);
		List<DailyStats> dailyStats = calculateDailyStats(orders);

		return new AnalyticsResponse(
				orderStats,
				revenueStats,
				topProducts,
				List.of(),
				ordersByStatus,
				dailyStats
		);
	}

	public AnalyticsResponse getCustomerAnalytics(Long customerId) {
		List<Order> orders = orderRepository.findAllByCustomerId(customerId);

		OrderStats orderStats = calculateOrderStats(orders);
		RevenueStats revenueStats = calculateRevenueStats(orders);
		List<TopSupplier> topSuppliers = calculateTopSuppliers(orders);
		Map<String, Long> ordersByStatus = calculateOrdersByStatus(orders);
		List<DailyStats> dailyStats = calculateDailyStats(orders);

		return new AnalyticsResponse(
				orderStats,
				revenueStats,
				List.of(),
				topSuppliers,
				ordersByStatus,
				dailyStats
		);
	}

	private OrderStats calculateOrderStats(List<Order> orders) {
		long total = orders.size();
		long pending = orders.stream()
				.filter(o -> isPending(o.getStatus().getCode()))
				.count();
		long completed = orders.stream()
				.filter(o -> isCompleted(o.getStatus().getCode()))
				.count();
		long cancelled = orders.stream()
				.filter(o -> isCancelled(o.getStatus().getCode()))
				.count();

		double completionRate = total > 0 ? (double) completed / total * 100 : 0;

		return new OrderStats(total, pending, completed, cancelled, completionRate);
	}

	private RevenueStats calculateRevenueStats(List<Order> orders) {
		BigDecimal totalRevenue = orders.stream()
				.filter(o -> isCompleted(o.getStatus().getCode()))
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalVat = orders.stream()
				.filter(o -> isCompleted(o.getStatus().getCode()))
				.map(Order::getVatAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		long completedCount = orders.stream()
				.filter(o -> isCompleted(o.getStatus().getCode()))
				.count();

		BigDecimal avgOrderValue = completedCount > 0
				? totalRevenue.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;

		LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
		BigDecimal revenueThisMonth = orders.stream()
				.filter(o -> isCompleted(o.getStatus().getCode()))
				.filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new RevenueStats(totalRevenue, avgOrderValue, totalVat, revenueThisMonth, 0.0);
	}

	private List<TopProduct> calculateTopProducts(List<Order> orders) {
		Map<Long, ProductAggregation> productStats = new HashMap<>();

		for (Order order : orders) {
			if (!isCompleted(order.getStatus().getCode())) continue;

			for (OrderItem item : order.getItems()) {
				ProductAggregation agg = productStats.computeIfAbsent(
						item.getProductId(),
						k -> new ProductAggregation(item.getProductName())
				);
				agg.orderCount++;
				agg.totalQuantity += item.getQuantity();
				agg.totalRevenue = agg.totalRevenue.add(item.getLineTotal());
			}
		}

		return productStats.entrySet().stream()
				.sorted((a, b) -> b.getValue().totalRevenue.compareTo(a.getValue().totalRevenue))
				.limit(10)
				.map(e -> new TopProduct(
						e.getKey(),
						e.getValue().productName,
						e.getValue().orderCount,
						e.getValue().totalQuantity,
						e.getValue().totalRevenue
				))
				.toList();
	}

	private List<TopSupplier> calculateTopSuppliers(List<Order> orders) {
		Map<Long, SupplierAggregation> supplierStats = new HashMap<>();

		for (Order order : orders) {
			if (!isCompleted(order.getStatus().getCode())) continue;

			SupplierAggregation agg = supplierStats.computeIfAbsent(
					order.getSupplierId(),
					k -> new SupplierAggregation()
			);
			agg.orderCount++;
			agg.totalRevenue = agg.totalRevenue.add(order.getTotalAmount());
		}

		return supplierStats.entrySet().stream()
				.sorted((a, b) -> b.getValue().totalRevenue.compareTo(a.getValue().totalRevenue))
				.limit(10)
				.map(e -> new TopSupplier(
						e.getKey(),
						e.getValue().orderCount,
						e.getValue().totalRevenue
				))
				.toList();
	}

	private Map<String, Long> calculateOrdersByStatus(List<Order> orders) {
		return orders.stream()
				.collect(Collectors.groupingBy(
						o -> o.getStatus().getCode(),
						Collectors.counting()
				));
	}

	private List<DailyStats> calculateDailyStats(List<Order> orders) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

		Map<String, DailyAggregation> dailyMap = new LinkedHashMap<>();

		for (int i = 0; i <= 30; i++) {
			String date = thirtyDaysAgo.plusDays(i).format(formatter);
			dailyMap.put(date, new DailyAggregation());
		}

		for (Order order : orders) {
			if (order.getCreatedAt() == null) continue;

			String date = order.getCreatedAt().toLocalDate().format(formatter);
			DailyAggregation agg = dailyMap.get(date);
			if (agg != null) {
				agg.orderCount++;
				if (isCompleted(order.getStatus().getCode())) {
					agg.revenue = agg.revenue.add(order.getTotalAmount());
				}
			}
		}

		return dailyMap.entrySet().stream()
				.map(e -> new DailyStats(e.getKey(), e.getValue().orderCount, e.getValue().revenue))
				.toList();
	}

	private boolean isPending(String statusCode) {
		return statusCode.equals("PENDING_CONFIRMATION") ||
				statusCode.equals("CONFIRMED") ||
				statusCode.equals("AWAITING_PAYMENT") ||
				statusCode.equals("PENDING_PAYMENT_VERIFICATION") ||
				statusCode.equals("PAID") ||
				statusCode.equals("AWAITING_SHIPMENT") ||
				statusCode.equals("SHIPPED");
	}

	private boolean isCompleted(String statusCode) {
		return statusCode.equals("DELIVERED") || statusCode.equals("CLOSED");
	}

	private boolean isCancelled(String statusCode) {
		return statusCode.equals("REJECTED") || statusCode.equals("CANCELLED");
	}

	private static class ProductAggregation {
		String productName;
		long orderCount = 0;
		int totalQuantity = 0;
		BigDecimal totalRevenue = BigDecimal.ZERO;

		ProductAggregation(String name) {
			this.productName = name;
		}
	}

	private static class SupplierAggregation {
		long orderCount = 0;
		BigDecimal totalRevenue = BigDecimal.ZERO;
	}

	private static class DailyAggregation {
		long orderCount = 0;
		BigDecimal revenue = BigDecimal.ZERO;
	}
}
