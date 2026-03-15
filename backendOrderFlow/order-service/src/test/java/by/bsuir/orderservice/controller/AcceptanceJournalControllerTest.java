package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.AcceptanceDetailRecord;
import by.bsuir.orderservice.dto.AcceptanceJournalResponse;
import by.bsuir.orderservice.dto.AcceptanceSummaryRecord;
import by.bsuir.orderservice.service.AcceptanceJournalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AcceptanceJournalController.class)
@DisplayName("AcceptanceJournalController Tests")
class AcceptanceJournalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AcceptanceJournalService journalService;

	@Test
	@DisplayName("GET /api/acceptance-journal — должен вернуть журнал приёмки")
	void shouldReturnAcceptanceJournal() throws Exception {
		AcceptanceDetailRecord detail = new AcceptanceDetailRecord(
				LocalDateTime.of(2026, 3, 10, 12, 0),
				10L, "Молоко 3.2%", "MLK-001",
				100L, "Компания #100",
				50, new BigDecimal("3.00"), new BigDecimal("150.00"),
				1L, "ORD-001"
		);

		AcceptanceSummaryRecord summary = new AcceptanceSummaryRecord(
				10L, "Молоко 3.2%", "MLK-001",
				50, new BigDecimal("150.00"), 1
		);

		AcceptanceJournalResponse response = new AcceptanceJournalResponse(
				List.of(detail), List.of(summary),
				new BigDecimal("50"), new BigDecimal("150.00")
		);

		when(journalService.getJournal(eq(200L), isNull(), isNull(), isNull()))
				.thenReturn(response);

		mockMvc.perform(get("/api/acceptance-journal")
						.header("X-User-Company-Id", "200"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.details").isArray())
				.andExpect(jsonPath("$.data.details[0].productName").value("Молоко 3.2%"))
				.andExpect(jsonPath("$.data.details[0].quantity").value(50))
				.andExpect(jsonPath("$.data.summary").isArray())
				.andExpect(jsonPath("$.data.summary[0].totalQuantity").value(50))
				.andExpect(jsonPath("$.data.grandTotalQuantity").value(50))
				.andExpect(jsonPath("$.data.grandTotalAmount").value(150.00));
	}

	@Test
	@DisplayName("GET /api/acceptance-journal с фильтрами — должен передать параметры в сервис")
	void shouldPassFiltersToService() throws Exception {
		AcceptanceJournalResponse emptyResponse = new AcceptanceJournalResponse(
				List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO
		);

		when(journalService.getJournal(anyLong(), anyLong(), any(), any()))
				.thenReturn(emptyResponse);

		mockMvc.perform(get("/api/acceptance-journal")
						.header("X-User-Company-Id", "200")
						.param("supplierId", "100")
						.param("dateFrom", "2026-03-01")
						.param("dateTo", "2026-03-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.details").isEmpty());
	}

	@Test
	@DisplayName("GET /api/acceptance-journal — пустой журнал")
	void shouldReturnEmptyJournal() throws Exception {
		AcceptanceJournalResponse emptyResponse = new AcceptanceJournalResponse(
				List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO
		);

		when(journalService.getJournal(eq(200L), isNull(), isNull(), isNull()))
				.thenReturn(emptyResponse);

		mockMvc.perform(get("/api/acceptance-journal")
						.header("X-User-Company-Id", "200"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.details").isEmpty())
				.andExpect(jsonPath("$.data.summary").isEmpty())
				.andExpect(jsonPath("$.data.grandTotalQuantity").value(0))
				.andExpect(jsonPath("$.data.grandTotalAmount").value(0));
	}
}
