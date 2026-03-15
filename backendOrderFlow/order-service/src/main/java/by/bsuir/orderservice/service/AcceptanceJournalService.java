package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.AcceptanceDetailRecord;
import by.bsuir.orderservice.dto.AcceptanceJournalResponse;
import by.bsuir.orderservice.dto.AcceptanceSummaryRecord;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderItem;
import by.bsuir.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;






@Service
@RequiredArgsConstructor
public class AcceptanceJournalService {

	private final OrderRepository orderRepository;
	private final by.bsuir.orderservice.client.AuthServiceClient authServiceClient;

	public AcceptanceJournalResponse getJournal(Long customerId, Long supplierId, LocalDate dateFrom, LocalDate dateTo) {
		List<Order> orders = orderRepository.findDeliveredOrClosedByCustomerId(customerId);


		if (supplierId != null) {
			orders = orders.stream()
					.filter(o -> o.getSupplierId().equals(supplierId))
					.toList();
		}


		LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : LocalDateTime.MIN;
		LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay() : LocalDateTime.MAX;

		orders = orders.stream()
				.filter(o -> o.getUpdatedAt() != null &&
						o.getUpdatedAt().isAfter(from) &&
						o.getUpdatedAt().isBefore(to))
				.toList();


		List<AcceptanceDetailRecord> details = new ArrayList<>();
		for (Order order : orders) {
			for (OrderItem item : order.getItems()) {
				int acceptedQty = item.getReceivedQuantity() != null ? item.getReceivedQuantity() : item.getQuantity();
				BigDecimal totalPrice = item.getUnitPrice() != null
						? item.getUnitPrice().multiply(BigDecimal.valueOf(acceptedQty))
						: BigDecimal.ZERO;

				details.add(new AcceptanceDetailRecord(
						order.getUpdatedAt(),
						item.getProductId(),
						item.getProductName() != null ? item.getProductName() : "Товар #" + item.getProductId(),
						item.getProductSku(),
						order.getSupplierId(),
						authServiceClient.getCompanyName(order.getSupplierId()),
						acceptedQty,
						item.getUnitPrice(),
						totalPrice,
						order.getId(),
						order.getOrderNumber()
				));
			}
		}


		details.sort(Comparator.comparing(AcceptanceDetailRecord::acceptanceDate).reversed());


		Map<Long, ProductAgg> aggMap = new LinkedHashMap<>();
		for (AcceptanceDetailRecord d : details) {
			ProductAgg agg = aggMap.computeIfAbsent(d.productId(), k -> new ProductAgg(d.productName(), d.productSku()));
			agg.totalQuantity += d.quantity();
			agg.totalAmount = agg.totalAmount.add(d.totalPrice());
			agg.suppliers.add(d.supplierId());
		}

		List<AcceptanceSummaryRecord> summary = aggMap.entrySet().stream()
				.map(e -> new AcceptanceSummaryRecord(
						e.getKey(),
						e.getValue().name,
						e.getValue().sku,
						e.getValue().totalQuantity,
						e.getValue().totalAmount,
						e.getValue().suppliers.size()
				))
				.toList();


		BigDecimal grandTotalQty = BigDecimal.valueOf(summary.stream().mapToInt(AcceptanceSummaryRecord::totalQuantity).sum());
		BigDecimal grandTotalAmount = summary.stream()
				.map(AcceptanceSummaryRecord::totalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new AcceptanceJournalResponse(details, summary, grandTotalQty, grandTotalAmount);
	}

	private static class ProductAgg {
		String name;
		String sku;
		int totalQuantity = 0;
		BigDecimal totalAmount = BigDecimal.ZERO;
		Set<Long> suppliers = new HashSet<>();

		ProductAgg(String name, String sku) {
			this.name = name;
			this.sku = sku;
		}
	}
}
