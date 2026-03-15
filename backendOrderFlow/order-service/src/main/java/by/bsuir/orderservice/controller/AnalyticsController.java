package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.dto.RetailAnalyticsResponse.ProductPurchaseHistory;
import by.bsuir.orderservice.service.AdvancedAnalyticsService;
import by.bsuir.orderservice.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting API")
public class AnalyticsController {

	private final AnalyticsService analyticsService;
	private final AdvancedAnalyticsService advancedAnalyticsService;

	@GetMapping
	@Operation(summary = "Get overall analytics (admin) with optional period filter")
	public ResponseEntity<ApiResponse<AnalyticsResponse>> getOverallAnalytics(
			@RequestParam(defaultValue = "all") @Parameter(description = "Period: today, week, month, quarter, year, all") String period) {
		AnalyticsResponse analytics = analyticsService.getOverallAnalytics(period);
		return ResponseEntity.ok(ApiResponse.success(analytics));
	}

	@GetMapping("/dashboard")
	@Operation(summary = "Get dashboard data: orders today, pending confirmation, payment problems")
	public ResponseEntity<ApiResponse<OrderSummaryResponse>> getDashboardData(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long companyId) {

		OrderSummaryResponse summary = analyticsService.getDashboardSummary(companyId);
		return ResponseEntity.ok(ApiResponse.success(summary));
	}

	@GetMapping("/supplier")
	@Operation(summary = "Get supplier KPI, funnel, sales dynamics, product and customer analytics")
	public ResponseEntity<ApiResponse<SupplierAnalyticsResponse>> getSupplierAnalytics(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@RequestParam(defaultValue = "month") @Parameter(description = "Period: today, week, month, quarter, year") String period) {
		SupplierAnalyticsResponse analytics = advancedAnalyticsService.getSupplierAnalytics(supplierId, period);
		return ResponseEntity.ok(ApiResponse.success(analytics));
	}

	@GetMapping("/customer")
	@Operation(summary = "Get retail chain KPI, expenses dynamics, structure and supplier analytics")
	public ResponseEntity<ApiResponse<RetailAnalyticsResponse>> getRetailAnalytics(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@RequestParam(defaultValue = "month") @Parameter(description = "Period: today, week, month, quarter, year") String period) {
		RetailAnalyticsResponse analytics = advancedAnalyticsService.getRetailAnalytics(customerId, period);
		return ResponseEntity.ok(ApiResponse.success(analytics));
	}

	@GetMapping("/customer/product-history")
	@Operation(summary = "Get product purchase history for retail chain")
	public ResponseEntity<ApiResponse<List<ProductPurchaseHistory>>> getProductPurchaseHistory(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@RequestParam(required = false) @Parameter(description = "Filter by product ID") Long productId) {
		List<ProductPurchaseHistory> history = advancedAnalyticsService.getProductPurchaseHistory(customerId, productId);
		return ResponseEntity.ok(ApiResponse.success(history));
	}

	@GetMapping("/dashboard/supplier")
	@Operation(summary = "Aggregated supplier dashboard (ТЗ стр. 2)",
			description = "Returns all data for supplier home page in a single request")
	public ResponseEntity<ApiResponse<SupplierDashboardResponse>> getSupplierDashboard(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		SupplierDashboardResponse dashboard = advancedAnalyticsService.getSupplierDashboard(supplierId);
		return ResponseEntity.ok(ApiResponse.success(dashboard));
	}

	@GetMapping("/dashboard/customer")
	@Operation(summary = "Aggregated customer dashboard (ТЗ стр. 19)",
			description = "Returns all data for retail chain home page in a single request")
	public ResponseEntity<ApiResponse<CustomerDashboardResponse>> getCustomerDashboard(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId) {
		CustomerDashboardResponse dashboard = advancedAnalyticsService.getCustomerDashboard(customerId);
		return ResponseEntity.ok(ApiResponse.success(dashboard));
	}
}
