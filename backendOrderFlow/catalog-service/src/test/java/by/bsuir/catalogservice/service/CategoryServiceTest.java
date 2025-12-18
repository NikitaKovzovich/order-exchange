package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.CategoryRequest;
import by.bsuir.catalogservice.dto.CategoryResponse;
import by.bsuir.catalogservice.entity.Category;
import by.bsuir.catalogservice.entity.Product;
import by.bsuir.catalogservice.exception.InvalidOperationException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private CategoryService categoryService;

	private Category rootCategory;
	private Category childCategory;

	@BeforeEach
	void setUp() {
		rootCategory = Category.builder()
				.id(1L)
				.name("Electronics")
				.parent(null)
				.children(new ArrayList<>())
				.products(new ArrayList<>())
				.build();

		childCategory = Category.builder()
				.id(2L)
				.name("Smartphones")
				.parent(rootCategory)
				.children(new ArrayList<>())
				.products(new ArrayList<>())
				.build();

		rootCategory.getChildren().add(childCategory);
	}

	@Nested
	@DisplayName("Get Category Tests")
	class GetCategoryTests {

		@Test
		@DisplayName("Should return all categories")
		void shouldReturnAllCategories() {
			when(categoryRepository.findAll()).thenReturn(List.of(rootCategory, childCategory));

			List<CategoryResponse> categories = categoryService.getAllCategories();

			assertThat(categories).hasSize(2);
		}

		@Test
		@DisplayName("Should return category by ID")
		void shouldReturnCategoryById() {
			when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));

			CategoryResponse response = categoryService.getCategoryById(1L);

			assertThat(response).isNotNull();
			assertThat(response.name()).isEqualTo("Electronics");
		}

		@Test
		@DisplayName("Should throw exception when category not found")
		void shouldThrowExceptionWhenCategoryNotFound() {
			when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> categoryService.getCategoryById(999L))
					.isInstanceOf(ResourceNotFoundException.class)
					.hasMessageContaining("Category");
		}

		@Test
		@DisplayName("Should return root categories")
		void shouldReturnRootCategories() {
			when(categoryRepository.findByParentIsNull()).thenReturn(List.of(rootCategory));

			List<CategoryResponse> roots = categoryService.getRootCategories();

			assertThat(roots).hasSize(1);
			assertThat(roots.getFirst().name()).isEqualTo("Electronics");
		}

		@Test
		@DisplayName("Should return subcategories")
		void shouldReturnSubcategories() {
			when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
			when(categoryRepository.findByParentId(1L)).thenReturn(List.of(childCategory));

			List<CategoryResponse> subcategories = categoryService.getSubcategories(1L);

			assertThat(subcategories).hasSize(1);
			assertThat(subcategories.getFirst().name()).isEqualTo("Smartphones");
		}
	}

	@Nested
	@DisplayName("Create Category Tests")
	class CreateCategoryTests {

		@Test
		@DisplayName("Should create root category")
		void shouldCreateRootCategory() {
			CategoryRequest request = new CategoryRequest("New Category", null);
			Category newCategory = Category.builder()
					.id(3L)
					.name("New Category")
					.build();

			when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

			CategoryResponse response = categoryService.createCategory(request);

			assertThat(response).isNotNull();
			assertThat(response.name()).isEqualTo("New Category");
			verify(categoryRepository).save(any(Category.class));
		}

		@Test
		@DisplayName("Should create child category")
		void shouldCreateChildCategory() {
			CategoryRequest request = new CategoryRequest("Child", 1L);
			Category newChild = Category.builder()
					.id(3L)
					.name("Child")
					.parent(rootCategory)
					.build();

			when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
			when(categoryRepository.save(any(Category.class))).thenReturn(newChild);

			CategoryResponse response = categoryService.createCategory(request);

			assertThat(response).isNotNull();
			assertThat(response.name()).isEqualTo("Child");
		}

		@Test
		@DisplayName("Should throw exception when parent not found")
		void shouldThrowExceptionWhenParentNotFound() {
			CategoryRequest request = new CategoryRequest("Child", 999L);
			when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> categoryService.createCategory(request))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("Update Category Tests")
	class UpdateCategoryTests {

		@Test
		@DisplayName("Should update category successfully")
		void shouldUpdateCategorySuccessfully() {
			CategoryRequest request = new CategoryRequest("Updated Name", null);
			when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
			when(categoryRepository.existsByName("Updated Name")).thenReturn(false);
			when(categoryRepository.save(any(Category.class))).thenReturn(rootCategory);

			CategoryResponse response = categoryService.updateCategory(1L, request);

			assertThat(response).isNotNull();
			verify(categoryRepository).save(any(Category.class));
		}
	}

	@Nested
	@DisplayName("Delete Category Tests")
	class DeleteCategoryTests {

		@Test
		@DisplayName("Should delete category without products")
		void shouldDeleteCategoryWithoutProducts() {
			Category emptyCategory = Category.builder()
					.id(3L)
					.name("Empty")
					.children(new ArrayList<>())
					.products(new ArrayList<>())
					.build();

			when(categoryRepository.findById(3L)).thenReturn(Optional.of(emptyCategory));

			categoryService.deleteCategory(3L);

			verify(categoryRepository).delete(emptyCategory);
		}

		@Test
		@DisplayName("Should throw exception when category has products")
		void shouldThrowExceptionWhenCategoryHasProducts() {
			Category categoryWithProducts = Category.builder()
					.id(3L)
					.name("HasProducts")
					.children(new ArrayList<>())
					.products(new ArrayList<>())
					.build();
			categoryWithProducts.getProducts().add(Product.builder().id(1L).build());

			when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryWithProducts));

			assertThatThrownBy(() -> categoryService.deleteCategory(3L))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("products");
		}

		@Test
		@DisplayName("Should throw exception when category has subcategories")
		void shouldThrowExceptionWhenCategoryHasSubcategories() {
			when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));

			assertThatThrownBy(() -> categoryService.deleteCategory(1L))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("subcategories");
		}
	}
}
