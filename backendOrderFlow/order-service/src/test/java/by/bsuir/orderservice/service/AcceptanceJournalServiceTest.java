package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.AcceptanceJournalResponse;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcceptanceJournalService Tests")
class AcceptanceJournalServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private by.bsuir.orderservice.client.AuthServiceClient authServiceClient;

	@InjectMocks
	private AcceptanceJournalService journalService;

	private Order deliveredOrder1;
	private Order deliveredOrder2;

	@BeforeEach
	void setUp() {
		OrderStatus deliveredStatus = OrderStatus.builder()
				.id(10L).code(OrderStatus.Codes.DELIVERED).name("Доставлен").build();
		OrderStatus closedStatus = OrderStatus.builder()
				.id(12L).code(OrderStatus.Codes.CLOSED).name("Закрыт").build();

		deliveredOrder1 = Order.builder()
				.id(1L)
				.orderNumber("ORD-001")
				.supplierId(100L)
				.customerId(200L)
				.status(deliveredStatus)
				.deliveryAddress("Address 1")
				.totalAmount(new BigDecimal("500.00"))
				.vatAmount(new BigDecimal("100.00"))
				.updatedAt(LocalDateTime.of(2026, 3, 10, 12, 0))
				.build();

		OrderItem item1 = OrderItem.builder()
				.id(1L).order(deliveredOrder1)
				.productId(10L).productName("Молоко 3.2%").productSku("MLK-001")
				.quantity(50).unitPrice(new BigDecimal("3.00"))
				.lineTotal(new BigDecimal("150.00")).lineVat(new BigDecimal("30.00"))
				.build();
		OrderItem item2 = OrderItem.builder()
				.id(2L).order(deliveredOrder1)
				.productId(20L).productName("Хлеб белый").productSku("BRD-001")
				.quantity(100).unitPrice(new BigDecimal("2.00"))
				.lineTotal(new BigDecimal("200.00")).lineVat(new BigDecimal("40.00"))
				.build();
		deliveredOrder1.setItems(new ArrayList<>(List.of(item1, item2)));

		deliveredOrder2 = Order.builder()
				.id(2L)
				.orderNumber("ORD-002")
				.supplierId(300L)
				.customerId(200L)
				.status(closedStatus)
				.deliveryAddress("Address 2")
				.totalAmount(new BigDecimal("1000.00"))
				.vatAmount(new BigDecimal("200.00"))
				.updatedAt(LocalDateTime.of(2026, 3, 12, 15, 0))
				.build();

		OrderItem item3 = OrderItem.builder()
				.id(3L).order(deliveredOrder2)
				.productId(10L).productName("Молоко 3.2%").productSku("MLK-001")
				.quantity(30).unitPrice(new BigDecimal("3.00"))
				.lineTotal(new BigDecimal("90.00")).lineVat(new BigDecimal("18.00"))
				.build();
		deliveredOrder2.setItems(new ArrayList<>(List.of(item3)));
	}

	@Nested
	@DisplayName("getJournal — без фильтров")
	class GetJournalNoFilters {

		@Test
		@DisplayName("Должен вернуть пустой журнал если нет заказов")
		void shouldReturnEmptyJournalWhenNoOrders() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L)).thenReturn(List.of());

			AcceptanceJournalResponse journal = journalService.getJournal(200L, null, null, null);

			assertThat(journal.details()).isEmpty();
			assertThat(journal.summary()).isEmpty();
			assertThat(journal.grandTotalQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
			assertThat(journal.grandTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		}

		@Test
		@DisplayName("Должен вернуть детализацию из нескольких заказов")
		void shouldReturnDetailsFromMultipleOrders() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1, deliveredOrder2));

			AcceptanceJournalResponse journal = journalService.getJournal(200L, null, null, null);

			assertThat(journal.details()).hasSize(3);
			assertThat(journal.details()).extracting("orderNumber")
					.containsExactlyInAnyOrder("ORD-001", "ORD-001", "ORD-002");
		}

		@Test
		@DisplayName("Должен правильно агрегировать по товарам (summary)")
		void shouldAggregateByProduct() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1, deliveredOrder2));

			AcceptanceJournalResponse journal = journalService.getJournal(200L, null, null, null);

			assertThat(journal.summary()).hasSize(2);


			var milkSummary = journal.summary().stream()
					.filter(s -> s.productId().equals(10L)).findFirst().orElseThrow();
			assertThat(milkSummary.totalQuantity()).isEqualTo(80);
			assertThat(milkSummary.totalAmount()).isEqualByComparingTo(new BigDecimal("240.00"));
			assertThat(milkSummary.uniqueSuppliers()).isEqualTo(2);


			var breadSummary = journal.summary().stream()
					.filter(s -> s.productId().equals(20L)).findFirst().orElseThrow();
			assertThat(breadSummary.totalQuantity()).isEqualTo(100);
			assertThat(breadSummary.totalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
			assertThat(breadSummary.uniqueSuppliers()).isEqualTo(1);
		}

		@Test
		@DisplayName("Должен посчитать grand total")
		void shouldCalculateGrandTotal() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1, deliveredOrder2));

			AcceptanceJournalResponse journal = journalService.getJournal(200L, null, null, null);


			assertThat(journal.grandTotalQuantity()).isEqualByComparingTo(new BigDecimal("180"));
			assertThat(journal.grandTotalAmount()).isEqualByComparingTo(new BigDecimal("440.00"));
		}
	}

	@Nested
	@DisplayName("getJournal — фильтр по поставщику")
	class GetJournalFilterBySupplier {

		@Test
		@DisplayName("Должен отфильтровать по конкретному поставщику")
		void shouldFilterBySupplierId() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1, deliveredOrder2));

			AcceptanceJournalResponse journal = journalService.getJournal(200L, 100L, null, null);


			assertThat(journal.details()).hasSize(2);
			assertThat(journal.details()).allMatch(d -> d.supplierId().equals(100L));
		}

		@Test
		@DisplayName("Должен вернуть пустой результат если поставщик не найден")
		void shouldReturnEmptyForNonExistentSupplier() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1, deliveredOrder2));

			AcceptanceJournalResponse journal = journalService.getJournal(200L, 999L, null, null);

			assertThat(journal.details()).isEmpty();
			assertThat(journal.summary()).isEmpty();
		}
	}

	@Nested
	@DisplayName("getJournal — фильтр по периоду")
	class GetJournalFilterByPeriod {

		@Test
		@DisplayName("Должен отфильтровать по дате")
		void shouldFilterByDateRange() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1, deliveredOrder2));


			LocalDate dateFrom = LocalDate.of(2026, 3, 11);
			LocalDate dateTo = LocalDate.of(2026, 3, 13);

			AcceptanceJournalResponse journal = journalService.getJournal(200L, null, dateFrom, dateTo);

			assertThat(journal.details()).hasSize(1);
			assertThat(journal.details().get(0).orderNumber()).isEqualTo("ORD-002");
		}

		@Test
		@DisplayName("Должен корректно применять оба фильтра вместе")
		void shouldApplyBothFilters() {
			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1, deliveredOrder2));


			LocalDate dateFrom = LocalDate.of(2026, 3, 11);
			LocalDate dateTo = LocalDate.of(2026, 3, 13);

			AcceptanceJournalResponse journal = journalService.getJournal(200L, 300L, dateFrom, dateTo);

			assertThat(journal.details()).hasSize(1);
			assertThat(journal.details().get(0).supplierId()).isEqualTo(300L);
		}
	}

	@Nested
	@DisplayName("getJournal — receivedQuantity")
	class GetJournalReceivedQuantity {

		@Test
		@DisplayName("Должен использовать receivedQuantity если задано")
		void shouldUseReceivedQuantityWhenPresent() {
			OrderItem itemWithReceived = deliveredOrder1.getItems().get(0);
			itemWithReceived.setReceivedQuantity(40);

			when(orderRepository.findDeliveredOrClosedByCustomerId(200L))
					.thenReturn(List.of(deliveredOrder1));

			AcceptanceJournalResponse journal = journalService.getJournal(200L, null, null, null);

			var milkDetail = journal.details().stream()
					.filter(d -> d.productId().equals(10L)).findFirst().orElseThrow();
			assertThat(milkDetail.quantity()).isEqualTo(40);
			assertThat(milkDetail.totalPrice()).isEqualByComparingTo(new BigDecimal("120.00"));
		}
	}
}
