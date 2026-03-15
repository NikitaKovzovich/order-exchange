package by.bsuir.orderservice.dto;

import java.util.List;





public record CheckoutResponse(
	List<OrderResponse> orders,
	int orderCount,
	String message
) {}
