package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.InventoryResponse;
import by.bsuir.catalogservice.dto.InventoryUpdateRequest;
import by.bsuir.catalogservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private InventoryService inventoryService;

	@Test
	@DisplayName("GET /api/inventory/{productId} - should return inventory")
	void shouldReturnInventory() throws Exception {
		InventoryResponse response = new InventoryResponse(
				1L, "Test Product", "SKU-001", 100, 10, 90, false, false
		);
		when(inventoryService.getInventory(1L)).thenReturn(response);

		mockMvc.perform(get("/api/inventory/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.quantityAvailable").value(100));
	}

	@Test
	@DisplayName("PUT /api/inventory/{productId} - should update inventory")
	void shouldUpdateInventory() throws Exception {
		InventoryUpdateRequest request = new InventoryUpdateRequest(150, "Restocking");
		InventoryResponse response = new InventoryResponse(
				1L, "Test Product", "SKU-001", 150, 0, 150, false, false
		);
		when(inventoryService.updateQuantity(eq(1L), anyLong(), any(InventoryUpdateRequest.class)))
				.thenReturn(response);

		mockMvc.perform(put("/api/inventory/1")
						.header("X-User-Company-Id", "100")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.quantityAvailable").value(150));
	}

	@Test
	@DisplayName("POST /api/inventory/{productId}/add - should add stock")
	void shouldAddStock() throws Exception {
		InventoryResponse response = new InventoryResponse(
				1L, "Test Product", "SKU-001", 150, 0, 150, false, false
		);
		when(inventoryService.addStock(eq(1L), anyLong(), eq(50), eq("New delivery")))
				.thenReturn(response);

		mockMvc.perform(post("/api/inventory/1/add")
						.header("X-User-Company-Id", "100")
						.param("quantity", "50")
						.param("reason", "New delivery"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("GET /api/inventory/low-stock - should return low stock products")
	void shouldReturnLowStockProducts() throws Exception {
		InventoryResponse response = new InventoryResponse(
				1L, "Low Stock Product", "SKU-001", 5, 0, 5, true, false
		);
		when(inventoryService.getLowStockProducts(10)).thenReturn(List.of(response));

		mockMvc.perform(get("/api/inventory/low-stock?threshold=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].lowStock").value(true));
	}

	@Test
	@DisplayName("GET /api/inventory/out-of-stock - should return out of stock products")
	void shouldReturnOutOfStockProducts() throws Exception {
		InventoryResponse response = new InventoryResponse(
				1L, "Out of Stock Product", "SKU-001", 0, 0, 0, true, true
		);
		when(inventoryService.getOutOfStockProducts()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/inventory/out-of-stock"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].outOfStock").value(true));
	}
}
