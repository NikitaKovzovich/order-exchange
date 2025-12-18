package by.bsuir.catalogservice.dto;

public record InventoryResponse(
	Long productId,
	String productName,
	String productSku,
	int quantityAvailable,
	int reservedQuantity,
	int actualAvailable,
	boolean lowStock,
	boolean outOfStock
) {}
