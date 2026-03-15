package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.dto.RetailAnalyticsResponse.*;
import by.bsuir.orderservice.dto.SupplierAnalyticsResponse.*;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderItem;
import by.bsuir.orderservice.entity.OrderStatus;
import by.bsuir.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedAnalyticsService {

	private final OrderRepository orderRepository;
	private final by.bsuir.orderservice.client.AuthServiceClient authServiceClient;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public SupplierAnalyticsResponse getSupplierAnalytics(Long supplierId, String period) {
		LocalDateTime startDate = getStartDate(period);
		List<Order> allOrders = orderRepository.findAllBySupplierId(supplierId);
		List<Order> periodOrders = filterByPeriod(allOrders, startDate);

		SupplierAnalyticsResponse.KpiStats kpi = calculateSupplierKpi(periodOrders, period);
		FunnelStats funnel = calculateFunnel(allOrders);
		List<SupplierAnalyticsResponse.DailyStats> salesDynamics = calculateSalesDynamics(periodOrders, startDate);
		ProductAnalytics productAnalytics = calculateProductAnalytics(periodOrders, allOrders);
		List<CustomerStats> customerAnalytics = calculateCustomerAnalytics(periodOrders);

		return new SupplierAnalyticsResponse(kpi, funnel, salesDynamics, productAnalytics, customerAnalytics);
	}

	public RetailAnalyticsResponse getRetailAnalytics(Long customerId, String period) {
		LocalDateTime startDate = getStartDate(period);
		List<Order> allOrders = orderRepository.findAllByCustomerId(customerId);
		List<Order> periodOrders = filterByPeriod(allOrders, startDate);

		RetailAnalyticsResponse.KpiStats kpi = calculateRetailKpi(periodOrders, period);
		List<DailyExpenses> expensesDynamics = calculateExpensesDynamics(periodOrders, startDate);
		ExpenseStructure expenseStructure = calculateExpenseStructure(periodOrders);
		List<RetailAnalyticsResponse.SupplierStats> supplierAnalytics = calculateSupplierAnalytics(periodOrders);

		return new RetailAnalyticsResponse(kpi, expensesDynamics, expenseStructure, supplierAnalytics, List.of());
	}

	public List<ProductPurchaseHistory> getProductPurchaseHistory(Long customerId, Long productId) {
		List<Order> orders = orderRepository.findAllByCustomerId(customerId);

		Map<Long, List<PurchaseRecord>> productPurchases = new HashMap<>();

		for (Order order : orders) {
			if (!isPaid(order.getStatus().getCode())) continue;

			for (OrderItem item : order.getItems()) {
				if (productId != null && !item.getProductId().equals(productId)) continue;

				PurchaseRecord record = new PurchaseRecord(
						order.getCreatedAt().format(DATE_FORMAT),
						order.getSupplierId(),
						authServiceClient.getCompanyName(order.getSupplierId()),
						item.getQuantity(),
						item.getUnitPrice(),
						item.getLineTotal()
				);

				productPurchases.computeIfAbsent(item.getProductId(), k -> new ArrayList<>()).add(record);
			}
		}

		return productPurchases.entrySet().stream()
				.map(e -> {
					List<PurchaseRecord> records = e.getValue();
					String productName = records.isEmpty() ? "Unknown" : "Product #" + e.getKey();
					return new ProductPurchaseHistory(e.getKey(), productName, "SKU-" + e.getKey(), records);
				})
				.toList();
	}

	private LocalDateTime getStartDate(String period) {
		LocalDate today = LocalDate.now();
		return switch (period.toLowerCase()) {
			case "today" -> today.atStartOfDay();
			case "week" -> today.minusWeeks(1).atStartOfDay();
			case "month" -> today.minusMonths(1).atStartOfDay();
			case "quarter" -> today.minusMonths(3).atStartOfDay();
			case "year" -> today.minusYears(1).atStartOfDay();
			default -> today.minusMonths(1).atStartOfDay();
		};
	}

	private List<Order> filterByPeriod(List<Order> orders, LocalDateTime startDate) {
		return orders.stream()
				.filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startDate))
				.toList();
	}

	private SupplierAnalyticsResponse.KpiStats calculateSupplierKpi(List<Order> orders, String period) {
		List<Order> paidOrders = orders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.toList();

		BigDecimal revenue = paidOrders.stream()
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		long orderCount = orders.size();

		BigDecimal averageCheck = orderCount > 0
				? revenue.divide(BigDecimal.valueOf(paidOrders.isEmpty() ? 1 : paidOrders.size()), 2, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;

		long shippedUnits = orders.stream()
				.filter(o -> isShipped(o.getStatus().getCode()))
				.flatMap(o -> o.getItems().stream())
				.mapToLong(OrderItem::getQuantity)
				.sum();

		return new SupplierAnalyticsResponse.KpiStats(revenue, orderCount, averageCheck, shippedUnits, period);
	}

	private RetailAnalyticsResponse.KpiStats calculateRetailKpi(List<Order> orders, String period) {
		List<Order> paidOrders = orders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.toList();

		BigDecimal totalExpenses = paidOrders.stream()
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		long orderCount = orders.size();

		long supplierCount = orders.stream()
				.map(Order::getSupplierId)
				.distinct()
				.count();

		BigDecimal averageCheck = orderCount > 0
				? totalExpenses.divide(BigDecimal.valueOf(paidOrders.isEmpty() ? 1 : paidOrders.size()), 2, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;

		return new RetailAnalyticsResponse.KpiStats(totalExpenses, orderCount, supplierCount, averageCheck, period);
	}

	private FunnelStats calculateFunnel(List<Order> orders) {
		Map<String, Long> statusCounts = orders.stream()
				.collect(Collectors.groupingBy(o -> o.getStatus().getCode(), Collectors.counting()));

		return new FunnelStats(
				statusCounts.getOrDefault(OrderStatus.Codes.PENDING_CONFIRMATION, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.CONFIRMED, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.REJECTED, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.AWAITING_PAYMENT, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.PAID, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.PAYMENT_PROBLEM, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.AWAITING_SHIPMENT, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.SHIPPED, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.AWAITING_CORRECTION, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.DELIVERED, 0L),
				statusCounts.getOrDefault(OrderStatus.Codes.CLOSED, 0L)
		);
	}

	private List<SupplierAnalyticsResponse.DailyStats> calculateSalesDynamics(List<Order> orders, LocalDateTime startDate) {
		Map<String, DailyAggregation> dailyMap = new LinkedHashMap<>();

		long daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), LocalDate.now()) + 1;
		for (int i = 0; i < daysBetween; i++) {
			String date = startDate.toLocalDate().plusDays(i).format(DATE_FORMAT);
			dailyMap.put(date, new DailyAggregation());
		}

		for (Order order : orders) {
			if (order.getCreatedAt() == null) continue;
			String date = order.getCreatedAt().toLocalDate().format(DATE_FORMAT);
			DailyAggregation agg = dailyMap.get(date);
			if (agg != null) {
				agg.orderCount++;
				if (isPaid(order.getStatus().getCode())) {
					agg.revenue = agg.revenue.add(order.getTotalAmount());
				}
			}
		}

		return dailyMap.entrySet().stream()
				.map(e -> new SupplierAnalyticsResponse.DailyStats(e.getKey(), e.getValue().revenue, e.getValue().orderCount))
				.toList();
	}

	private List<DailyExpenses> calculateExpensesDynamics(List<Order> orders, LocalDateTime startDate) {
		Map<String, DailyAggregation> dailyMap = new LinkedHashMap<>();

		long daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), LocalDate.now()) + 1;
		for (int i = 0; i < daysBetween; i++) {
			String date = startDate.toLocalDate().plusDays(i).format(DATE_FORMAT);
			dailyMap.put(date, new DailyAggregation());
		}

		for (Order order : orders) {
			if (order.getCreatedAt() == null) continue;
			String date = order.getCreatedAt().toLocalDate().format(DATE_FORMAT);
			DailyAggregation agg = dailyMap.get(date);
			if (agg != null) {
				agg.orderCount++;
				if (isPaid(order.getStatus().getCode())) {
					agg.revenue = agg.revenue.add(order.getTotalAmount());
				}
			}
		}

		return dailyMap.entrySet().stream()
				.map(e -> new DailyExpenses(e.getKey(), e.getValue().revenue, e.getValue().orderCount))
				.toList();
	}

	private ProductAnalytics calculateProductAnalytics(List<Order> periodOrders, List<Order> allOrders) {
		List<Order> paidOrders = periodOrders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.toList();

		Map<Long, ProductAgg> productMap = new HashMap<>();

		for (Order order : paidOrders) {
			for (OrderItem item : order.getItems()) {
				ProductAgg agg = productMap.computeIfAbsent(item.getProductId(),
						k -> new ProductAgg(item.getProductName(), item.getProductSku()));
				agg.quantity += item.getQuantity();
				agg.revenue = agg.revenue.add(item.getLineTotal());
				agg.orderCount++;
			}
		}

		List<TopProductByRevenue> topByRevenue = productMap.entrySet().stream()
				.sorted((a, b) -> b.getValue().revenue.compareTo(a.getValue().revenue))
				.limit(5)
				.map(e -> new TopProductByRevenue(e.getKey(), e.getValue().name, e.getValue().sku,
						e.getValue().revenue, e.getValue().quantity))
				.toList();

		List<TopProductByQuantity> topByQuantity = productMap.entrySet().stream()
				.sorted((a, b) -> Integer.compare(b.getValue().quantity, a.getValue().quantity))
				.limit(5)
				.map(e -> new TopProductByQuantity(e.getKey(), e.getValue().name, e.getValue().sku,
						e.getValue().quantity, e.getValue().revenue))
				.toList();

		List<AbcProduct> abcAnalysis = calculateAbcAnalysis(productMap);

		return new ProductAnalytics(topByRevenue, topByQuantity, abcAnalysis, List.of());
	}

	private List<AbcProduct> calculateAbcAnalysis(Map<Long, ProductAgg> productMap) {
		BigDecimal totalRevenue = productMap.values().stream()
				.map(p -> p.revenue)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) return List.of();

		List<Map.Entry<Long, ProductAgg>> sorted = productMap.entrySet().stream()
				.sorted((a, b) -> b.getValue().revenue.compareTo(a.getValue().revenue))
				.toList();

		List<AbcProduct> result = new ArrayList<>();
		double cumulative = 0;

		for (Map.Entry<Long, ProductAgg> entry : sorted) {
			double percent = entry.getValue().revenue.doubleValue() / totalRevenue.doubleValue() * 100;
			cumulative += percent;

			String category;
			if (cumulative <= 80) category = "A";
			else if (cumulative <= 95) category = "B";
			else category = "C";

			result.add(new AbcProduct(
					entry.getKey(),
					entry.getValue().name,
					category,
					entry.getValue().revenue,
					Math.round(percent * 100.0) / 100.0,
					Math.round(cumulative * 100.0) / 100.0
			));
		}

		return result;
	}

	private List<CustomerStats> calculateCustomerAnalytics(List<Order> orders) {
		Map<Long, CustomerAgg> customerMap = new HashMap<>();

		for (Order order : orders) {
			CustomerAgg agg = customerMap.computeIfAbsent(order.getCustomerId(), k -> new CustomerAgg());
			agg.orderCount++;
			if (isPaid(order.getStatus().getCode())) {
				agg.totalRevenue = agg.totalRevenue.add(order.getTotalAmount());
			}
			if (agg.lastOrderDate == null || order.getCreatedAt().isAfter(agg.lastOrderDate)) {
				agg.lastOrderDate = order.getCreatedAt();
			}
		}

		return customerMap.entrySet().stream()
				.sorted((a, b) -> b.getValue().totalRevenue.compareTo(a.getValue().totalRevenue))
				.map(e -> {
					BigDecimal avg = e.getValue().orderCount > 0
							? e.getValue().totalRevenue.divide(BigDecimal.valueOf(e.getValue().orderCount), 2, RoundingMode.HALF_UP)
							: BigDecimal.ZERO;
					return new CustomerStats(
							e.getKey(),
							authServiceClient.getCompanyName(e.getKey()),
							e.getValue().orderCount,
							e.getValue().totalRevenue,
							avg,
							e.getValue().lastOrderDate != null ? e.getValue().lastOrderDate.format(DATE_FORMAT) : null
					);
				})
				.toList();
	}

	private ExpenseStructure calculateExpenseStructure(List<Order> orders) {
		List<Order> paidOrders = orders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.toList();

		BigDecimal total = paidOrders.stream()
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		Map<Long, BigDecimal> supplierExpenses = new HashMap<>();
		for (Order order : paidOrders) {
			supplierExpenses.merge(order.getSupplierId(), order.getTotalAmount(), BigDecimal::add);
		}

		List<SupplierExpense> bySupplier = supplierExpenses.entrySet().stream()
				.sorted((a, b) -> b.getValue().compareTo(a.getValue()))
				.map(e -> {
					double percent = total.compareTo(BigDecimal.ZERO) > 0
							? e.getValue().doubleValue() / total.doubleValue() * 100
							: 0;
					return new SupplierExpense(e.getKey(), authServiceClient.getCompanyName(e.getKey()), e.getValue(),
							Math.round(percent * 100.0) / 100.0);
				})
				.toList();

		return new ExpenseStructure(List.of(), bySupplier);
	}

	private List<RetailAnalyticsResponse.SupplierStats> calculateSupplierAnalytics(List<Order> orders) {
		Map<Long, SupplierAgg> supplierMap = new HashMap<>();

		for (Order order : orders) {
			SupplierAgg agg = supplierMap.computeIfAbsent(order.getSupplierId(), k -> new SupplierAgg());
			agg.orderCount++;
			if (isPaid(order.getStatus().getCode())) {
				agg.totalAmount = agg.totalAmount.add(order.getTotalAmount());
			}
			if (agg.lastOrderDate == null || order.getCreatedAt().isAfter(agg.lastOrderDate)) {
				agg.lastOrderDate = order.getCreatedAt();
			}
		}

		return supplierMap.entrySet().stream()
				.sorted((a, b) -> b.getValue().totalAmount.compareTo(a.getValue().totalAmount))
				.map(e -> {
					BigDecimal avg = e.getValue().orderCount > 0
							? e.getValue().totalAmount.divide(BigDecimal.valueOf(e.getValue().orderCount), 2, RoundingMode.HALF_UP)
							: BigDecimal.ZERO;
					return new RetailAnalyticsResponse.SupplierStats(
							e.getKey(),
							authServiceClient.getCompanyName(e.getKey()),
							e.getValue().orderCount,
							e.getValue().totalAmount,
							avg,
							e.getValue().lastOrderDate != null ? e.getValue().lastOrderDate.format(DATE_FORMAT) : null
					);
				})
				.toList();
	}

	private boolean isPaid(String statusCode) {
		return statusCode.equals(OrderStatus.Codes.PAID) ||
				statusCode.equals(OrderStatus.Codes.AWAITING_SHIPMENT) ||
				statusCode.equals(OrderStatus.Codes.SHIPPED) ||
				statusCode.equals(OrderStatus.Codes.AWAITING_CORRECTION) ||
				statusCode.equals(OrderStatus.Codes.DELIVERED) ||
				statusCode.equals(OrderStatus.Codes.CLOSED);
	}

	private boolean isShipped(String statusCode) {
		return statusCode.equals(OrderStatus.Codes.SHIPPED) ||
				statusCode.equals(OrderStatus.Codes.AWAITING_CORRECTION) ||
				statusCode.equals(OrderStatus.Codes.DELIVERED) ||
				statusCode.equals(OrderStatus.Codes.CLOSED);
	}

	private static class DailyAggregation {
		long orderCount = 0;
		BigDecimal revenue = BigDecimal.ZERO;
	}

	private static class ProductAgg {
		String name;
		String sku;
		int quantity = 0;
		BigDecimal revenue = BigDecimal.ZERO;
		long orderCount = 0;

		ProductAgg(String name, String sku) {
			this.name = name != null ? name : "Unknown";
			this.sku = sku != null ? sku : "";
		}
	}

	private static class CustomerAgg {
		long orderCount = 0;
		BigDecimal totalRevenue = BigDecimal.ZERO;
		LocalDateTime lastOrderDate = null;
	}

	private static class SupplierAgg {
		long orderCount = 0;
		BigDecimal totalAmount = BigDecimal.ZERO;
		LocalDateTime lastOrderDate = null;
	}







	public SupplierDashboardResponse getSupplierDashboard(Long supplierId) {
		List<Order> allOrders = orderRepository.findAllBySupplierId(supplierId);
		LocalDateTime weekAgo = getStartDate("week");
		LocalDateTime monthAgo = getStartDate("month");
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();

		List<Order> monthOrders = filterByPeriod(allOrders, monthAgo);
		List<Order> weekOrders = filterByPeriod(allOrders, weekAgo);


		BigDecimal revenueThisMonth = monthOrders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);


		long newOrdersToday = allOrders.stream()
				.filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart))
				.count();


		BigDecimal averageCheck = BigDecimal.ZERO;
		List<Order> paidMonthOrders = monthOrders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.toList();
		if (!paidMonthOrders.isEmpty()) {
			averageCheck = revenueThisMonth.divide(BigDecimal.valueOf(paidMonthOrders.size()), 2, java.math.RoundingMode.HALF_UP);
		}


		long ordersInTransit = allOrders.stream()
				.filter(o -> "SHIPPED".equals(o.getStatus().getCode()))
				.count();


		long pendingCount = allOrders.stream()
				.filter(o -> OrderStatus.Codes.PENDING_CONFIRMATION.equals(o.getStatus().getCode()))
				.count();


		List<SupplierAnalyticsResponse.DailyStats> salesDynamics = calculateSalesDynamics(weekOrders, weekAgo);

		return new SupplierDashboardResponse(
				revenueThisMonth,
				newOrdersToday,
				averageCheck,
				ordersInTransit,
				pendingCount,
				List.of(),
				salesDynamics
		);
	}




	public CustomerDashboardResponse getCustomerDashboard(Long customerId) {
		List<Order> allOrders = orderRepository.findAllByCustomerId(customerId);
		LocalDateTime weekAgo = getStartDate("week");
		LocalDateTime monthAgo = getStartDate("month");

		List<Order> monthOrders = filterByPeriod(allOrders, monthAgo);
		List<Order> weekOrders = filterByPeriod(allOrders, weekAgo);


		BigDecimal expensesThisMonth = monthOrders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);


		long orderCount = monthOrders.size();


		long activeContracts = allOrders.stream()
				.filter(o -> !"CANCELLED".equals(o.getStatus().getCode()) && !"REJECTED".equals(o.getStatus().getCode()))
				.map(Order::getSupplierId)
				.distinct()
				.count();


		BigDecimal averageCheck = BigDecimal.ZERO;
		List<Order> paidMonthOrders = monthOrders.stream()
				.filter(o -> isPaid(o.getStatus().getCode()))
				.toList();
		if (!paidMonthOrders.isEmpty()) {
			averageCheck = expensesThisMonth.divide(BigDecimal.valueOf(paidMonthOrders.size()), 2, java.math.RoundingMode.HALF_UP);
		}


		ExpenseStructure expenseStructure = calculateExpenseStructure(monthOrders);
		List<DailyExpenses> expensesDynamics = calculateExpensesDynamics(weekOrders, weekAgo);

		return new CustomerDashboardResponse(
				expensesThisMonth,
				orderCount,
				activeContracts,
				averageCheck,
				List.of(),
				expenseStructure,
				expensesDynamics
		);
	}
}
