package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.*;
import by.bsuir.catalogservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductService productService;

	private ProductResponse createTestProductResponse() {
		CategoryResponse category = new CategoryResponse(1L, "Electronics", null, null, 5);
		return new ProductResponse(
				1L,
				100L,
				"SKU-001",
				"Test Product",
				"Description",
				category,
				new BigDecimal("99.99"),
				new BigDecimal("119.99"),
				"шт",
				"20%",
				new BigDecimal("20.00"),
				new BigDecimal("0.5"),
				"Belarus",
				null,
				null,
				"DRAFT",
				50,
				true,
				null
		);
	}

	@Test
	@DisplayName("GET /api/products/{id} - should return product by ID")
	void shouldReturnProductById() throws Exception {
		ProductResponse product = createTestProductResponse();
		when(productService.getProductById(1L)).thenReturn(product);

		mockMvc.perform(get("/api/products/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.name").value("Test Product"));
	}

	@Test
	@DisplayName("GET /api/products/search - should search products")
	void shouldSearchProducts() throws Exception {
		ProductResponse product = createTestProductResponse();
		PageResponse<ProductResponse> pageResponse = new PageResponse<>(
				List.of(product), 0, 20, 1, 1, true, true
		);
		when(productService.searchProducts(any(ProductSearchRequest.class))).thenReturn(pageResponse);

		mockMvc.perform(get("/api/products/search")
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
	}

	@Test
	@DisplayName("GET /api/products/supplier - should return supplier products")
	void shouldReturnSupplierProducts() throws Exception {
		ProductResponse product = createTestProductResponse();
		PageResponse<ProductResponse> pageResponse = new PageResponse<>(
				List.of(product), 0, 20, 1, 1, true, true
		);
		when(productService.getSupplierProducts(eq(100L), anyInt(), anyInt())).thenReturn(pageResponse);

		mockMvc.perform(get("/api/products/supplier")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content").isArray());
	}

	@Test
	@DisplayName("POST /api/products - should create product")
	void shouldCreateProduct() throws Exception {
		ProductRequest request = new ProductRequest(
				"SKU-001", "Test Product", "Description",
				1L, new BigDecimal("99.99"), 1L, 1L,
				new BigDecimal("0.5"), "Belarus", null, null, 100
		);
		ProductResponse response = createTestProductResponse();
		when(productService.createProduct(eq(100L), any(ProductRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/products")
						.header("X-User-Company-Id", "100")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value("Test Product"));
	}

	@Test
	@DisplayName("POST /api/products - should return 400 for invalid request")
	void shouldReturn400ForInvalidRequest() throws Exception {
		ProductRequest invalidRequest = new ProductRequest(
				"", "", null, null, null, null, null, null, null, null, null, null
		);

		mockMvc.perform(post("/api/products")
						.header("X-User-Company-Id", "100")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /api/products/{id} - should update product")
	void shouldUpdateProduct() throws Exception {
		ProductRequest request = new ProductRequest(
				"SKU-001", "Updated Product", "Description",
				1L, new BigDecimal("149.99"), 1L, 1L,
				new BigDecimal("0.5"), "Belarus", null, null, null
		);
		ProductResponse response = createTestProductResponse();
		when(productService.updateProduct(eq(1L), eq(100L), any(ProductRequest.class))).thenReturn(response);

		mockMvc.perform(put("/api/products/1")
						.header("X-User-Company-Id", "100")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("POST /api/products/{id}/publish - should publish product")
	void shouldPublishProduct() throws Exception {
		ProductResponse response = createTestProductResponse();
		when(productService.publishProduct(1L, 100L)).thenReturn(response);

		mockMvc.perform(post("/api/products/1/publish")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Product published successfully"));
	}

	@Test
	@DisplayName("POST /api/products/{id}/archive - should archive product")
	void shouldArchiveProduct() throws Exception {
		ProductResponse response = createTestProductResponse();
		when(productService.archiveProduct(1L, 100L)).thenReturn(response);

		mockMvc.perform(post("/api/products/1/archive")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Product archived successfully"));
	}

	@Test
	@DisplayName("POST /api/products/{id}/draft - should move product to draft")
	void shouldMoveProductToDraft() throws Exception {
		ProductResponse response = createTestProductResponse();
		when(productService.toDraft(1L, 100L)).thenReturn(response);

		mockMvc.perform(post("/api/products/1/draft")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}
