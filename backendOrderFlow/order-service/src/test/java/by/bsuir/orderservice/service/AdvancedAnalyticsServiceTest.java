package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.RetailAnalyticsResponse;
import by.bsuir.orderservice.dto.SupplierAnalyticsResponse;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderItem;
import by.bsuir.orderservice.entity.OrderStatus;
import by.bsuir.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvancedAnalyticsServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private AdvancedAnalyticsService analyticsService;

	private Order paidOrder;
	private Order pendingOrder;
	private OrderStatus paidStatus;
	private OrderStatus pendingStatus;

	@BeforeEach
	void setUp() {
		paidStatus = OrderStatus.builder()
				.id(1L)
				.code(OrderStatus.Codes.PAID)
				.name("Paid")
				.build();

		pendingStatus = OrderStatus.builder()
				.id(2L)
				.code(OrderStatus.Codes.PENDING_CONFIRMATION)
				.name("Pending")
				.build();

		OrderItem item1 = OrderItem.builder()
				.id(1L)
				.productId(1L)
				.productName("Product A")
				.productSku("SKU-001")
				.quantity(10)
				.unitPrice(new BigDecimal("100.00"))
				.lineTotal(new BigDecimal("1000.00"))
				.build();

		OrderItem item2 = OrderItem.builder()
				.id(2L)
				.productId(2L)
				.productName("Product B")
				.productSku("SKU-002")
				.quantity(5)
				.unitPrice(new BigDecimal("200.00"))
				.lineTotal(new BigDecimal("1000.00"))
				.build();

		paidOrder = Order.builder()
				.id(1L)
				.orderNumber("ORD-001")
				.supplierId(1L)
				.customerId(2L)
				.status(paidStatus)
				.totalAmount(new BigDecimal("2000.00"))
				.vatAmount(new BigDecimal("400.00"))
				.createdAt(LocalDateTime.now().minusDays(5))
				.items(new ArrayList<>(List.of(item1, item2)))
				.build();

		pendingOrder = Order.builder()
				.id(2L)
				.orderNumber("ORD-002")
				.supplierId(1L)
				.customerId(3L)
				.status(pendingStatus)
				.totalAmount(new BigDecimal("500.00"))
				.vatAmount(new BigDecimal("100.00"))
				.createdAt(LocalDateTime.now().minusDays(2))
				.items(new ArrayList<>())
				.build();
	}

	@Nested
	@DisplayName("Supplier Analytics Tests")
	class SupplierAnalyticsTests {

		@Test
		@DisplayName("Should calculate supplier KPI for month")
		void shouldCalculateSupplierKpi() {
			when(orderRepository.findAllBySupplierId(1L))
					.thenReturn(List.of(paidOrder, pendingOrder));

			SupplierAnalyticsResponse result = analyticsService.getSupplierAnalytics(1L, "month");

			assertThat(result).isNotNull();
			assertThat(result.kpi()).isNotNull();
			assertThat(result.kpi().orderCount()).isEqualTo(2);
			assertThat(result.kpi().revenue()).isEqualByComparingTo(new BigDecimal("2000.00"));
		}

		@Test
		@DisplayName("Should calculate order funnel")
		void shouldCalculateFunnel() {
			when(orderRepository.findAllBySupplierId(1L))
					.thenReturn(List.of(paidOrder, pendingOrder));

			SupplierAnalyticsResponse result = analyticsService.getSupplierAnalytics(1L, "month");

			assertThat(result.funnel()).isNotNull();
			assertThat(result.funnel().paid()).isEqualTo(1);
			assertThat(result.funnel().pendingConfirmation()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should calculate sales dynamics")
		void shouldCalculateSalesDynamics() {
			when(orderRepository.findAllBySupplierId(1L))
					.thenReturn(List.of(paidOrder));

			SupplierAnalyticsResponse result = analyticsService.getSupplierAnalytics(1L, "week");

			assertThat(result.salesDynamics()).isNotEmpty();
		}

		@Test
		@DisplayName("Should calculate product analytics with ABC")
		void shouldCalculateProductAnalytics() {
			when(orderRepository.findAllBySupplierId(1L))
					.thenReturn(List.of(paidOrder));

			SupplierAnalyticsResponse result = analyticsService.getSupplierAnalytics(1L, "month");

			assertThat(result.productAnalytics()).isNotNull();
			assertThat(result.productAnalytics().topByRevenue()).hasSize(2);
			assertThat(result.productAnalytics().abcAnalysis()).isNotEmpty();
		}

		@Test
		@DisplayName("Should calculate customer analytics")
		void shouldCalculateCustomerAnalytics() {
			when(orderRepository.findAllBySupplierId(1L))
					.thenReturn(List.of(paidOrder, pendingOrder));

			SupplierAnalyticsResponse result = analyticsService.getSupplierAnalytics(1L, "month");

			assertThat(result.customerAnalytics()).hasSize(2);
		}
	}

	@Nested
	@DisplayName("Retail Analytics Tests")
	class RetailAnalyticsTests {

		@Test
		@DisplayName("Should calculate retail KPI")
		void shouldCalculateRetailKpi() {
			when(orderRepository.findAllByCustomerId(2L))
					.thenReturn(List.of(paidOrder));

			RetailAnalyticsResponse result = analyticsService.getRetailAnalytics(2L, "month");

			assertThat(result).isNotNull();
			assertThat(result.kpi()).isNotNull();
			assertThat(result.kpi().totalExpenses()).isEqualByComparingTo(new BigDecimal("2000.00"));
			assertThat(result.kpi().orderCount()).isEqualTo(1);
			assertThat(result.kpi().supplierCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should calculate expenses dynamics")
		void shouldCalculateExpensesDynamics() {
			when(orderRepository.findAllByCustomerId(2L))
					.thenReturn(List.of(paidOrder));

			RetailAnalyticsResponse result = analyticsService.getRetailAnalytics(2L, "week");

			assertThat(result.expensesDynamics()).isNotEmpty();
		}

		@Test
		@DisplayName("Should calculate expense structure by supplier")
		void shouldCalculateExpenseStructure() {
			when(orderRepository.findAllByCustomerId(2L))
					.thenReturn(List.of(paidOrder));

			RetailAnalyticsResponse result = analyticsService.getRetailAnalytics(2L, "month");

			assertThat(result.expenseStructure()).isNotNull();
			assertThat(result.expenseStructure().bySupplier()).hasSize(1);
		}

		@Test
		@DisplayName("Should calculate supplier analytics")
		void shouldCalculateSupplierAnalytics() {
			when(orderRepository.findAllByCustomerId(2L))
					.thenReturn(List.of(paidOrder));

			RetailAnalyticsResponse result = analyticsService.getRetailAnalytics(2L, "month");

			assertThat(result.supplierAnalytics()).hasSize(1);
			assertThat(result.supplierAnalytics().get(0).totalAmount())
					.isEqualByComparingTo(new BigDecimal("2000.00"));
		}
	}

	@Nested
	@DisplayName("Product Purchase History Tests")
	class ProductPurchaseHistoryTests {

		@Test
		@DisplayName("Should get product purchase history")
		void shouldGetProductPurchaseHistory() {
			when(orderRepository.findAllByCustomerId(2L))
					.thenReturn(List.of(paidOrder));

			var result = analyticsService.getProductPurchaseHistory(2L, null);

			assertThat(result).hasSize(2);
		}

		@Test
		@DisplayName("Should filter by product ID")
		void shouldFilterByProductId() {
			when(orderRepository.findAllByCustomerId(2L))
					.thenReturn(List.of(paidOrder));

			var result = analyticsService.getProductPurchaseHistory(2L, 1L);

			assertThat(result).hasSize(1);
		}
	}

	@Nested
	@DisplayName("Period Filter Tests")
	class PeriodFilterTests {

		@Test
		@DisplayName("Should filter by today")
		void shouldFilterByToday() {
			Order todayOrder = Order.builder()
					.id(3L)
					.supplierId(1L)
					.status(paidStatus)
					.totalAmount(new BigDecimal("100.00"))
					.createdAt(LocalDateTime.now())
					.items(new ArrayList<>())
					.build();

			when(orderRepository.findAllBySupplierId(1L))
					.thenReturn(List.of(paidOrder, todayOrder));

			SupplierAnalyticsResponse result = analyticsService.getSupplierAnalytics(1L, "today");

			assertThat(result.kpi().orderCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should filter by quarter")
		void shouldFilterByQuarter() {
			when(orderRepository.findAllBySupplierId(1L))
					.thenReturn(List.of(paidOrder, pendingOrder));

			SupplierAnalyticsResponse result = analyticsService.getSupplierAnalytics(1L, "quarter");

			assertThat(result.kpi().orderCount()).isEqualTo(2);
		}
	}
}
