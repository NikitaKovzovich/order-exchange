package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.InventoryResponse;
import by.bsuir.catalogservice.dto.InventoryUpdateRequest;
import by.bsuir.catalogservice.entity.Inventory;
import by.bsuir.catalogservice.entity.Product;
import by.bsuir.catalogservice.exception.InsufficientStockException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.InventoryRepository;
import by.bsuir.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;
	private final EventPublisher eventPublisher;

	public InventoryResponse getInventory(Long productId) {
		Inventory inventory = inventoryRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
		return mapToResponse(inventory);
	}

	@Transactional
	public InventoryResponse updateQuantity(Long productId, Long supplierId, InventoryUpdateRequest request) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new IllegalArgumentException("Product does not belong to this supplier");
		}

		Inventory inventory = inventoryRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

		inventory.setQuantityAvailable(request.quantity());
		inventory = inventoryRepository.save(inventory);

		eventPublisher.publishInventoryUpdated(inventory, request.reason());
		return mapToResponse(inventory);
	}

	@Transactional
	public InventoryResponse addStock(Long productId, Long supplierId, int quantity, String reason) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new IllegalArgumentException("Product does not belong to this supplier");
		}

		Inventory inventory = inventoryRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

		inventory.addStock(quantity);
		inventory = inventoryRepository.save(inventory);

		eventPublisher.publishInventoryUpdated(inventory, reason != null ? reason : "stock added");
		return mapToResponse(inventory);
	}

	@Transactional
	public void reserveStock(Long productId, int quantity) {
		Inventory inventory = inventoryRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

		if (!inventory.hasEnough(quantity)) {
			throw new InsufficientStockException(productId, quantity, inventory.getActualAvailable());
		}

		inventory.reserve(quantity);
		inventoryRepository.save(inventory);
		eventPublisher.publishStockReserved(productId, quantity);
	}

	@Transactional
	public void releaseStock(Long productId, int quantity) {
		Inventory inventory = inventoryRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

		inventory.cancelReservation(quantity);
		inventoryRepository.save(inventory);
		eventPublisher.publishStockReleased(productId, quantity);
	}

	@Transactional
	public void shipStock(Long productId, int quantity) {
		Inventory inventory = inventoryRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

		inventory.shipReserved(quantity);
		inventoryRepository.save(inventory);
	}

	public List<InventoryResponse> getLowStockProducts(int threshold) {
		return inventoryRepository.findLowStock(threshold).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<InventoryResponse> getOutOfStockProducts() {
		return inventoryRepository.findOutOfStock().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	private InventoryResponse mapToResponse(Inventory inventory) {
		return new InventoryResponse(
				inventory.getProductId(),
				inventory.getProduct() != null ? inventory.getProduct().getName() : null,
				inventory.getProduct() != null ? inventory.getProduct().getSku() : null,
				inventory.getQuantityAvailable(),
				inventory.getReservedQuantity(),
				inventory.getActualAvailable(),
				inventory.isLowStock(),
				inventory.isOutOfStock()
		);
	}
}
