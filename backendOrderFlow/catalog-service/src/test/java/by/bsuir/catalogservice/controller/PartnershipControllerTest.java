package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.ContractUpdateRequest;
import by.bsuir.catalogservice.dto.PartnershipRequest;
import by.bsuir.catalogservice.dto.PartnershipResponse;
import by.bsuir.catalogservice.service.PartnershipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartnershipController.class)
@DisplayName("PartnershipController Tests")
class PartnershipControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PartnershipService partnershipService;

	private PartnershipResponse createTestResponse(String status) {
		return new PartnershipResponse(
				1L, 100L, "Поставщик Б", 200L, "Торговая сеть А", "123456789",
				status, "C-001", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
				LocalDateTime.now(), null
		);
	}

	@Test
	@DisplayName("POST /api/partnerships — создание заявки на партнёрство")
	void shouldCreatePartnershipRequest() throws Exception {
		PartnershipRequest request = new PartnershipRequest(
				100L, "C-001", LocalDate.of(2026, 1, 1),
				LocalDate.of(2027, 1, 1), "Торговая сеть А", "123456789"
		);

		when(partnershipService.createPartnershipRequest(eq(200L), any(PartnershipRequest.class)))
				.thenReturn(createTestResponse("PENDING"));

		mockMvc.perform(post("/api/partnerships")
						.header("X-User-Company-Id", "200")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.supplierId").value(100))
				.andExpect(jsonPath("$.data.customerId").value(200))
				.andExpect(jsonPath("$.data.status").value("PENDING"))
				.andExpect(jsonPath("$.data.contractNumber").value("C-001"));
	}

	@Test
	@DisplayName("GET /api/partnerships/customer — партнёрства торговой сети")
	void shouldGetCustomerPartnerships() throws Exception {
		when(partnershipService.getCustomerPartnerships(200L))
				.thenReturn(List.of(createTestResponse("ACTIVE")));

		mockMvc.perform(get("/api/partnerships/customer")
						.header("X-User-Company-Id", "200"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
	}

	@Test
	@DisplayName("GET /api/partnerships/supplier — все партнёрства поставщика")
	void shouldGetSupplierPartnerships() throws Exception {
		when(partnershipService.getSupplierPartnerships(100L))
				.thenReturn(List.of(createTestResponse("PENDING")));

		mockMvc.perform(get("/api/partnerships/supplier")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].supplierCompanyName").value("Поставщик Б"));
	}

	@Test
	@DisplayName("GET /api/partnerships/supplier/pending — новые заявки")
	void shouldGetPendingRequests() throws Exception {
		when(partnershipService.getSupplierPendingRequests(100L))
				.thenReturn(List.of(createTestResponse("PENDING")));

		mockMvc.perform(get("/api/partnerships/supplier/pending")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].status").value("PENDING"));
	}

	@Test
	@DisplayName("GET /api/partnerships/supplier/active — активные договоры")
	void shouldGetActivePartners() throws Exception {
		when(partnershipService.getSupplierActivePartners(100L))
				.thenReturn(List.of(createTestResponse("ACTIVE")));

		mockMvc.perform(get("/api/partnerships/supplier/active")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
	}

	@Test
	@DisplayName("POST /api/partnerships/{id}/accept — подтверждение заявки")
	void shouldAcceptPartnership() throws Exception {
		when(partnershipService.acceptPartnership(1L, 100L))
				.thenReturn(createTestResponse("ACTIVE"));

		mockMvc.perform(post("/api/partnerships/1/accept")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("ACTIVE"))
				.andExpect(jsonPath("$.message").value("Partnership accepted"));
	}

	@Test
	@DisplayName("POST /api/partnerships/{id}/reject — отклонение заявки")
	void shouldRejectPartnership() throws Exception {
		when(partnershipService.rejectPartnership(1L, 100L))
				.thenReturn(createTestResponse("REJECTED"));

		mockMvc.perform(post("/api/partnerships/1/reject")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("REJECTED"));
	}

	@Test
	@DisplayName("PUT /api/partnerships/{id}/contract — обновление договора")
	void shouldUpdateContract() throws Exception {
		ContractUpdateRequest request = new ContractUpdateRequest(
				"C-002", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 6, 1)
		);

		PartnershipResponse updated = new PartnershipResponse(
				1L, 100L, "Поставщик Б", 200L, "Торговая сеть А", "123456789",
				"ACTIVE", "C-002", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 6, 1),
				LocalDateTime.now(), LocalDateTime.now()
		);

		when(partnershipService.updateContract(eq(1L), eq(100L), any(ContractUpdateRequest.class)))
				.thenReturn(updated);

		mockMvc.perform(put("/api/partnerships/1/contract")
						.header("X-User-Company-Id", "100")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.contractNumber").value("C-002"));
	}

	@Test
	@DisplayName("GET /api/partnerships/customer — пустой список")
	void shouldReturnEmptyList() throws Exception {
		when(partnershipService.getCustomerPartnerships(200L)).thenReturn(List.of());

		mockMvc.perform(get("/api/partnerships/customer")
						.header("X-User-Company-Id", "200"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isEmpty());
	}
}
