package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.CategoryRequest;
import by.bsuir.catalogservice.dto.CategoryResponse;
import by.bsuir.catalogservice.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CategoryService categoryService;

	@Test
	@DisplayName("GET /api/categories - should return all categories")
	void shouldReturnAllCategories() throws Exception {
		CategoryResponse category = new CategoryResponse(1L, "Electronics", null, null, 5);
		when(categoryService.getAllCategories()).thenReturn(List.of(category));

		mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].name").value("Electronics"));
	}

	@Test
	@DisplayName("GET /api/categories/{id} - should return category by ID")
	void shouldReturnCategoryById() throws Exception {
		CategoryResponse category = new CategoryResponse(1L, "Electronics", null, null, 5);
		when(categoryService.getCategoryById(1L)).thenReturn(category);

		mockMvc.perform(get("/api/categories/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.name").value("Electronics"));
	}

	@Test
	@DisplayName("GET /api/categories/tree - should return category tree")
	void shouldReturnCategoryTree() throws Exception {
		CategoryResponse category = new CategoryResponse(1L, "Electronics", null, null, 5);
		when(categoryService.getRootCategories()).thenReturn(List.of(category));

		mockMvc.perform(get("/api/categories/tree"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@DisplayName("POST /api/categories - should create category")
	void shouldCreateCategory() throws Exception {
		CategoryRequest request = new CategoryRequest("New Category", null);
		CategoryResponse response = new CategoryResponse(1L, "New Category", null, null, 0);
		when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value("New Category"));
	}

	@Test
	@DisplayName("POST /api/categories - should return 400 for invalid request")
	void shouldReturn400ForInvalidRequest() throws Exception {
		CategoryRequest request = new CategoryRequest("", null);

		mockMvc.perform(post("/api/categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("PUT /api/categories/{id} - should update category")
	void shouldUpdateCategory() throws Exception {
		CategoryRequest request = new CategoryRequest("Updated", null);
		CategoryResponse response = new CategoryResponse(1L, "Updated", null, null, 0);
		when(categoryService.updateCategory(any(), any(CategoryRequest.class))).thenReturn(response);

		mockMvc.perform(put("/api/categories/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value("Updated"));
	}

	@Test
	@DisplayName("DELETE /api/categories/{id} - should delete category")
	void shouldDeleteCategory() throws Exception {
		mockMvc.perform(delete("/api/categories/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}
