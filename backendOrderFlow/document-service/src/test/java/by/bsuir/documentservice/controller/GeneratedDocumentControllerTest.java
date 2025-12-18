package by.bsuir.documentservice.controller;

import by.bsuir.documentservice.dto.*;
import by.bsuir.documentservice.service.PdfGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeneratedDocumentController.class)
class GeneratedDocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PdfGenerationService pdfGenerationService;

	private ObjectMapper objectMapper;
	private GeneratedDocumentResponse testResponse;
	private TtnGenerationRequest ttnRequest;
	private DiscrepancyActRequest discrepancyRequest;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		testResponse = new GeneratedDocumentResponse(
				1L, "TTN", "Товарно-транспортная накладная", 1L,
				"TTN-12345678", LocalDate.now(), "generated/1/ttn.pdf",
				LocalDateTime.now(), 1L
		);

		CompanyInfoDto shipper = new CompanyInfoDto(
				"ООО Поставщик", "123456789", "г. Минск",
				null, null, null, null
		);

		CompanyInfoDto consignee = new CompanyInfoDto(
				"ООО Покупатель", "987654321", "г. Минск",
				null, null, null, null
		);

		ProductItemDto item = new ProductItemDto(
				1, "Товар", null, "шт", 10,
				new BigDecimal("100.00"), new BigDecimal("120.00"),
				new BigDecimal("20"), new BigDecimal("200.00"),
				new BigDecimal("1000.00"), new BigDecimal("1200.00"),
				null, null
		);

		ttnRequest = new TtnGenerationRequest(
				1L, "ORD-001", LocalDate.now(), null,
				shipper, consignee, null,
				"Склад 1", "Склад 2",
				null, List.of(item),
				null, null, null, null, null, null, null
		);

		DiscrepancyItemDto discrepancyItem = new DiscrepancyItemDto(
				1, "Товар", null, "шт",
				10, 8, -2,
				new BigDecimal("100.00"), new BigDecimal("200.00"),
				DiscrepancyItemDto.DiscrepancyType.SHORTAGE, null
		);

		discrepancyRequest = new DiscrepancyActRequest(
				1L, "ORD-001", "TTN-001", LocalDate.now(), LocalDate.now(),
				shipper, consignee, List.of(discrepancyItem),
				null, null, null, null, null
		);
	}

	@Test
	@DisplayName("Should generate TTN")
	void shouldGenerateTtn() throws Exception {
		when(pdfGenerationService.generateTTN(any(TtnGenerationRequest.class), eq(1L)))
				.thenReturn(testResponse);

		mockMvc.perform(post("/api/generated-documents/ttn")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(ttnRequest))
						.header("X-User-Id", "1"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.templateType").value("TTN"))
				.andExpect(jsonPath("$.data.orderId").value(1));

		verify(pdfGenerationService).generateTTN(any(TtnGenerationRequest.class), eq(1L));
	}

	@Test
	@DisplayName("Should generate Discrepancy Act")
	void shouldGenerateDiscrepancyAct() throws Exception {
		GeneratedDocumentResponse actResponse = new GeneratedDocumentResponse(
				2L, "DISCREPANCY_ACT", "Акт о расхождении", 1L,
				"DA-12345678", LocalDate.now(), "generated/1/act.pdf",
				LocalDateTime.now(), 1L
		);

		when(pdfGenerationService.generateDiscrepancyAct(any(DiscrepancyActRequest.class), eq(1L)))
				.thenReturn(actResponse);

		mockMvc.perform(post("/api/generated-documents/discrepancy-act")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(discrepancyRequest))
						.header("X-User-Id", "1"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.templateType").value("DISCREPANCY_ACT"));

		verify(pdfGenerationService).generateDiscrepancyAct(any(DiscrepancyActRequest.class), eq(1L));
	}

	@Test
	@DisplayName("Should get document by ID")
	void shouldGetDocumentById() throws Exception {
		when(pdfGenerationService.getDocument(1L)).thenReturn(testResponse);

		mockMvc.perform(get("/api/generated-documents/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.templateType").value("TTN"));
	}

	@Test
	@DisplayName("Should download document")
	void shouldDownloadDocument() throws Exception {
		when(pdfGenerationService.getDocument(1L)).thenReturn(testResponse);
		when(pdfGenerationService.downloadGeneratedDocument(1L))
				.thenReturn(new ByteArrayInputStream("PDF content".getBytes()));

		mockMvc.perform(get("/api/generated-documents/1/download"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
	}

	@Test
	@DisplayName("Should get documents by order")
	void shouldGetDocumentsByOrder() throws Exception {
		when(pdfGenerationService.getDocumentsByOrder(1L))
				.thenReturn(List.of(testResponse));

		mockMvc.perform(get("/api/generated-documents/order/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].orderId").value(1));
	}

	@Test
	@DisplayName("Should get download URL")
	void shouldGetDownloadUrl() throws Exception {
		when(pdfGenerationService.getDocument(1L)).thenReturn(testResponse);

		mockMvc.perform(get("/api/generated-documents/1/url"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").value("/api/generated-documents/1/download"));
	}

	@Test
	@DisplayName("Should return 400 for invalid TTN request")
	void shouldReturn400ForInvalidTtnRequest() throws Exception {
		TtnGenerationRequest invalidRequest = new TtnGenerationRequest(
				null, null, null, null, null, null, null,
				null, null, null, null, null, null, null,
				null, null, null, null
		);

		mockMvc.perform(post("/api/generated-documents/ttn")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest))
						.header("X-User-Id", "1"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should return 400 for invalid Discrepancy request")
	void shouldReturn400ForInvalidDiscrepancyRequest() throws Exception {
		DiscrepancyActRequest invalidRequest = new DiscrepancyActRequest(
				null, null, null, null, null, null, null,
				null, null, null, null, null, null
		);

		mockMvc.perform(post("/api/generated-documents/discrepancy-act")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest))
						.header("X-User-Id", "1"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should return empty list when no documents for order")
	void shouldReturnEmptyListWhenNoDocuments() throws Exception {
		when(pdfGenerationService.getDocumentsByOrder(999L)).thenReturn(List.of());

		mockMvc.perform(get("/api/generated-documents/order/999"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data").isEmpty());
	}
}
