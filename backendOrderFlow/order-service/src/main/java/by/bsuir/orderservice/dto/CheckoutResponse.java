package by.bsuir.orderservice.dto;

import java.util.List;

/**
 * Ответ после оформления заказа (checkout)
 * Содержит список созданных заказов (по одному на каждого поставщика)
 */
public record CheckoutResponse(
	List<OrderResponse> orders,
	int orderCount,
	String message
) {}
