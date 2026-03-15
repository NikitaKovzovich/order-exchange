package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.*;
import by.bsuir.catalogservice.entity.*;
import by.bsuir.catalogservice.exception.*;
import by.bsuir.catalogservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final UnitOfMeasureRepository unitRepository;
	private final VatRateRepository vatRateRepository;
	private final InventoryRepository inventoryRepository;
	private final PartnershipRepository partnershipRepository;
	private final EventPublisher eventPublisher;

	public PageResponse<ProductResponse> getSupplierProducts(Long supplierId, String search, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
		String normalizedSearch = search != null && !search.trim().isEmpty() ? search.trim() : null;
		Page<Product> products = normalizedSearch == null
				? productRepository.findBySupplierId(supplierId, pageable)
				: productRepository.searchSupplierProducts(supplierId, normalizedSearch, pageable);
		return toPageResponse(products);
	}


	public PageResponse<ProductResponse> searchProducts(ProductSearchRequest request) {
		return searchProducts(request, null);
	}


	public PageResponse<ProductResponse> searchProducts(ProductSearchRequest request, Long customerCompanyId) {
		Sort sort = request.sortDir().equalsIgnoreCase("desc")
				? Sort.by(request.sortBy()).descending()
				: Sort.by(request.sortBy()).ascending();
		Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

		Page<Product> products;

		if (customerCompanyId != null) {
			List<Long> activeSupplierIds = partnershipRepository.findActiveSupplierIdsByCustomerId(customerCompanyId);
			if (activeSupplierIds.isEmpty()) {

				return new PageResponse<>(List.of(), 0, request.size(), 0, 0, true, true);
			}
			products = productRepository.searchProductsByPartners(
					activeSupplierIds,
					request.categoryId(),
					request.supplierId(),
					request.minPrice(),
					request.maxPrice(),
					request.search(),
					pageable);
		} else {
			products = productRepository.searchProducts(
					request.categoryId(),
					request.supplierId(),
					request.minPrice(),
					request.maxPrice(),
					request.search(),
					pageable);
		}

		return toPageResponse(products);
	}

	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
		return mapToResponse(product);
	}

	@Transactional
	public ProductResponse createProduct(Long supplierId, ProductRequest request) {
		if (productRepository.existsBySupplierIdAndSku(supplierId, request.sku())) {
			throw new DuplicateResourceException("Product", "sku", request.sku());
		}

		Category category = categoryRepository.findById(request.categoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
		UnitOfMeasure unit = unitRepository.findById(request.unitId())
				.orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.unitId()));
		VatRate vatRate = vatRateRepository.findById(request.vatRateId())
				.orElseThrow(() -> new ResourceNotFoundException("VatRate", "id", request.vatRateId()));

		Product product = Product.builder()
				.supplierId(supplierId)
				.sku(request.sku())
				.name(request.name())
				.description(request.description())
				.category(category)
				.pricePerUnit(request.pricePerUnit())
				.unit(unit)
				.vatRate(vatRate)
				.weight(request.weight())
				.packageDimensions(request.packageDimensions())
				.countryOfOrigin(request.countryOfOrigin())
				.productionDate(request.productionDate())
				.expiryDate(request.expiryDate())
				.status(Product.ProductStatus.DRAFT)
				.build();

		product = productRepository.save(product);

		Inventory inventory = Inventory.builder()
				.product(product)
				.quantityAvailable(request.initialQuantity() != null ? request.initialQuantity() : 0)
				.reservedQuantity(0)
				.build();
		inventoryRepository.save(inventory);

		eventPublisher.publishProductCreated(product);
		return mapToResponse(product);
	}

	@Transactional
	public ProductResponse updateProduct(Long id, Long supplierId, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("update", "Product does not belong to this supplier");
		}

		if (!product.getSku().equals(request.sku()) &&
				productRepository.existsBySupplierIdAndSku(supplierId, request.sku())) {
			throw new DuplicateResourceException("Product", "sku", request.sku());
		}

		Category category = categoryRepository.findById(request.categoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
		UnitOfMeasure unit = unitRepository.findById(request.unitId())
				.orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", "id", request.unitId()));
		VatRate vatRate = vatRateRepository.findById(request.vatRateId())
				.orElseThrow(() -> new ResourceNotFoundException("VatRate", "id", request.vatRateId()));

		product.setSku(request.sku());
		product.setName(request.name());
		product.setDescription(request.description());
		product.setCategory(category);
		product.setPricePerUnit(request.pricePerUnit());
		product.setUnit(unit);
		product.setVatRate(vatRate);
		product.setWeight(request.weight());
		product.setPackageDimensions(request.packageDimensions());
		product.setCountryOfOrigin(request.countryOfOrigin());
		product.setProductionDate(request.productionDate());
		product.setExpiryDate(request.expiryDate());

		product = productRepository.save(product);
		eventPublisher.publishProductUpdated(product);
		return mapToResponse(product);
	}

	@Transactional
	public ProductResponse publishProduct(Long id, Long supplierId) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("publish", "Product does not belong to this supplier");
		}

		product.publish();
		product = productRepository.save(product);
		eventPublisher.publishProductPublished(product);
		return mapToResponse(product);
	}

	@Transactional
	public ProductResponse archiveProduct(Long id, Long supplierId) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("archive", "Product does not belong to this supplier");
		}

		product.archive();
		product = productRepository.save(product);
		eventPublisher.publishProductArchived(product);
		return mapToResponse(product);
	}

	@Transactional
	public ProductResponse toDraft(Long id, Long supplierId) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("toDraft", "Product does not belong to this supplier");
		}

		product.toDraft();
		product = productRepository.save(product);
		return mapToResponse(product);
	}

	@Transactional
	public void deleteProduct(Long id, Long supplierId) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("delete", "Product does not belong to this supplier");
		}

		productRepository.delete(product);
		eventPublisher.publishProductDeleted(product);
	}


	@Transactional
	public ProductResponse adminHideProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
		product.archive();
		product = productRepository.save(product);
		return mapToResponse(product);
	}


	@Transactional
	public ProductResponse adminShowProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
		product.showByAdmin();
		product = productRepository.save(product);
		return mapToResponse(product);
	}


	@Transactional
	public void adminDeleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
		productRepository.delete(product);
	}


	@Transactional
	public int publishCatalog(Long supplierId) {
		List<Product> drafts = productRepository.findBySupplierIdAndStatus(supplierId, Product.ProductStatus.DRAFT);
		for (Product product : drafts) {
			product.publish();
		}
		productRepository.saveAll(drafts);
		return drafts.size();
	}


	@Transactional
	public int updateCatalog(Long supplierId) {
		return publishCatalog(supplierId);
	}

	private ProductResponse mapToResponse(Product product) {
		int availableQty = 0;
		if (product.getInventory() != null) {
			availableQty = product.getInventory().getActualAvailable();
		}

		CategoryResponse categoryResponse = new CategoryResponse(
				product.getCategory().getId(),
				product.getCategory().getName(),
				null, null, 0
		);

		return new ProductResponse(
				product.getId(),
				product.getSupplierId(),
				product.getSku(),
				product.getName(),
				product.getDescription(),
				categoryResponse,
				product.getPricePerUnit(),
				product.getPriceWithVat(),
				product.getUnit().getName(),
				product.getVatRate().getDescription(),
				product.getVatRate().getRatePercentage(),
				product.getWeight(),
				product.getPackageDimensions(),
				product.getCountryOfOrigin(),
				product.getProductionDate(),
				product.getExpiryDate(),
				product.getStatus().name(),
				availableQty,
				availableQty > 0,
				null
		);
	}

	private PageResponse<ProductResponse> toPageResponse(Page<Product> page) {
		List<ProductResponse> content = page.getContent().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());

		return new PageResponse<>(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast()
		);
	}


	public PageResponse<ProductResponse> getAdminProducts(Long supplierId, Long categoryId, String status, String search, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
		Product.ProductStatus statusEnum = null;
		if (status != null && !status.isBlank()) {
			try {
				statusEnum = Product.ProductStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {

			}
		}
		String searchTerm = (search != null && !search.isBlank()) ? search : null;
		Page<Product> products = productRepository.findAllForAdmin(supplierId, categoryId, statusEnum, searchTerm, pageable);
		return toPageResponse(products);
	}
}
