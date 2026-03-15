package by.bsuir.orderservice.dto;

import java.util.Map;

public record OrderSummaryResponse(
		long totalOrders,
		Map<String, Long> countByStatus,

		long pendingConfirmation,
		long awaitingShipment,
		long inTransit,
		long paymentProblems,

		long awaitingDelivery,
		long requirePayment,
		long rejected
) {

	public OrderSummaryResponse(long totalOrders, Map<String, Long> countByStatus) {
		this(totalOrders, countByStatus,
				countByStatus.getOrDefault("PENDING_CONFIRMATION", 0L),
				countByStatus.getOrDefault("AWAITING_SHIPMENT", 0L),
				countByStatus.getOrDefault("SHIPPED", 0L),
				countByStatus.getOrDefault("PAYMENT_PROBLEM", 0L),
				countByStatus.getOrDefault("SHIPPED", 0L),
				countByStatus.getOrDefault("AWAITING_PAYMENT", 0L),
				countByStatus.getOrDefault("REJECTED", 0L) + countByStatus.getOrDefault("PAYMENT_PROBLEM", 0L)
		);
	}
}
