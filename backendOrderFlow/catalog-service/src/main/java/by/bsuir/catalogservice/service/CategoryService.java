package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.CategoryRequest;
import by.bsuir.catalogservice.dto.CategoryResponse;
import by.bsuir.catalogservice.entity.Category;
import by.bsuir.catalogservice.exception.DuplicateResourceException;
import by.bsuir.catalogservice.exception.InvalidOperationException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final EventPublisher eventPublisher;

	public List<CategoryResponse> getAllCategories() {
		return categoryRepository.findAll().stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<CategoryResponse> getRootCategories() {
		return categoryRepository.findByParentIsNull().stream()
				.map(this::mapToResponseWithChildren)
				.collect(Collectors.toList());
	}

	public CategoryResponse getCategoryById(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
		return mapToResponseWithChildren(category);
	}

	public List<CategoryResponse> getSubcategories(Long parentId) {
		categoryRepository.findById(parentId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));
		return categoryRepository.findByParentId(parentId).stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public CategoryResponse createCategory(CategoryRequest request) {
		if (categoryRepository.existsByName(request.name())) {
			throw new DuplicateResourceException("Category", "name", request.name());
		}

		Category category = Category.builder()
				.name(request.name())
				.build();

		if (request.parentId() != null) {
			Category parent = categoryRepository.findById(request.parentId())
					.orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.parentId()));
			category.setParent(parent);
		}

		category = categoryRepository.save(category);
		eventPublisher.publishCategoryCreated(category);
		return mapToResponse(category);
	}

	@Transactional
	public CategoryResponse updateCategory(Long id, CategoryRequest request) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

		if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
			throw new DuplicateResourceException("Category", "name", request.name());
		}

		category.setName(request.name());

		if (request.parentId() != null) {
			if (request.parentId().equals(id)) {
				throw new IllegalArgumentException("Category cannot be its own parent");
			}
			Category parent = categoryRepository.findById(request.parentId())
					.orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.parentId()));
			category.setParent(parent);
		} else {
			category.setParent(null);
		}

		category = categoryRepository.save(category);
		eventPublisher.publishCategoryUpdated(category);
		return mapToResponse(category);
	}

	@Transactional
	public void deleteCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

		if (!category.getProducts().isEmpty()) {
			throw new InvalidOperationException("delete", "Cannot delete category with products");
		}
		if (!category.getChildren().isEmpty()) {
			throw new InvalidOperationException("delete", "Cannot delete category with subcategories");
		}

		categoryRepository.delete(category);
		eventPublisher.publishCategoryDeleted(id);
	}

	private CategoryResponse mapToResponse(Category category) {
		int productCount = 0;
		if (category.getProducts() != null) {
			productCount = category.getProducts().size();
		}
		return new CategoryResponse(
				category.getId(),
				category.getName(),
				category.getParent() != null ? category.getParent().getId() : null,
				category.getParent() != null ? category.getParent().getName() : null,
				productCount
		);
	}

	private CategoryResponse mapToResponseWithChildren(Category category) {
		List<CategoryResponse> children = null;
		if (category.getChildren() != null && !category.getChildren().isEmpty()) {
			children = category.getChildren().stream()
					.map(this::mapToResponseWithChildren)
					.collect(Collectors.toList());
		}
		int productCount = 0;
		if (category.getProducts() != null) {
			productCount = category.getProducts().size();
		}
		return new CategoryResponse(
				category.getId(),
				category.getName(),
				category.getParent() != null ? category.getParent().getId() : null,
				category.getParent() != null ? category.getParent().getName() : null,
				children,
				productCount
		);
	}
}
