package by.bsuir.documentservice.service;

import by.bsuir.documentservice.dto.*;
import by.bsuir.documentservice.entity.GeneratedDocument;
import by.bsuir.documentservice.repository.GeneratedDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

	@Mock
	private MinioService minioService;

	@Mock
	private GeneratedDocumentRepository generatedDocumentRepository;

	@InjectMocks
	private PdfGenerationService pdfGenerationService;

	private TtnGenerationRequest ttnRequest;
	private DiscrepancyActRequest discrepancyRequest;
	private GeneratedDocument generatedDocument;

	@BeforeEach
	void setUp() {
		CompanyInfoDto shipper = new CompanyInfoDto(
				"ООО Поставщик", "123456789", "г. Минск, ул. Примерная, 1",
				null, null, "Иванов И.И.", null
		);

		CompanyInfoDto consignee = new CompanyInfoDto(
				"ООО Покупатель", "987654321", "г. Минск, ул. Тестовая, 2",
				null, null, null, null
		);

		ProductItemDto item = new ProductItemDto(
				1, "Товар тестовый", "SKU-001", "шт", 10,
				new BigDecimal("100.00"), new BigDecimal("120.00"),
				new BigDecimal("20"), new BigDecimal("200.00"),
				new BigDecimal("1000.00"), new BigDecimal("1200.00"),
				null, null
		);

		ttnRequest = new TtnGenerationRequest(
				1L, "ORD-001", LocalDate.now(), null,
				shipper, consignee, null,
				"г. Минск, склад 1", "г. Минск, склад 2",
				null, List.of(item),
				new BigDecimal("1000.00"), new BigDecimal("200.00"), new BigDecimal("1200.00"),
				null, null, null, null
		);

		DiscrepancyItemDto discrepancyItem = new DiscrepancyItemDto(
				1, "Товар тестовый", "SKU-001", "шт",
				10, 8, -2,
				new BigDecimal("100.00"), new BigDecimal("200.00"),
				DiscrepancyItemDto.DiscrepancyType.SHORTAGE, null
		);

		discrepancyRequest = new DiscrepancyActRequest(
				1L, "ORD-001", "TTN-001", LocalDate.now(), LocalDate.now(),
				shipper, consignee, List.of(discrepancyItem),
				new BigDecimal("200.00"),
				List.of("Петров П.П.", "Сидоров С.С."),
				"Выявлена недостача", null, null
		);

		generatedDocument = GeneratedDocument.builder()
				.id(1L)
				.templateType(GeneratedDocument.TemplateType.TTN)
				.orderId(1L)
				.fileKey("generated/1/ttn_TTN-12345678.pdf")
				.generatedBy(1L)
				.documentNumber("TTN-12345678")
				.documentDate(LocalDate.now())
				.generatedAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Generate TTN Tests")
	class GenerateTtnTests {

		@Test
		@DisplayName("Should generate TTN successfully")
		void shouldGenerateTtnSuccessfully() throws Exception {
			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenReturn("generated/1/ttn_TTN-12345678.pdf");
			when(generatedDocumentRepository.save(any(GeneratedDocument.class)))
					.thenReturn(generatedDocument);

			GeneratedDocumentResponse response = pdfGenerationService.generateTTN(ttnRequest, 1L);

			assertThat(response).isNotNull();
			assertThat(response.orderId()).isEqualTo(1L);
			assertThat(response.templateType()).isEqualTo("TTN");
			verify(minioService).uploadPdfBytes(any(byte[].class), eq("generated/1"), anyString());
			verify(generatedDocumentRepository).save(any(GeneratedDocument.class));
		}

		@Test
		@DisplayName("Should throw when MinIO upload fails")
		void shouldThrowWhenMinioUploadFails() throws Exception {
			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenThrow(new RuntimeException("MinIO unavailable"));

			assertThatThrownBy(() -> pdfGenerationService.generateTTN(ttnRequest, 1L))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Failed to generate TTN");
		}
	}

	@Nested
	@DisplayName("Generate Discrepancy Act Tests")
	class GenerateDiscrepancyActTests {

		@Test
		@DisplayName("Should generate Discrepancy Act successfully")
		void shouldGenerateDiscrepancyActSuccessfully() throws Exception {
			GeneratedDocument discrepancyDoc = GeneratedDocument.builder()
					.id(2L)
					.templateType(GeneratedDocument.TemplateType.DISCREPANCY_ACT)
					.orderId(1L)
					.fileKey("generated/1/discrepancy-act_DA-12345678.pdf")
					.generatedBy(1L)
					.documentNumber("DA-12345678")
					.documentDate(LocalDate.now())
					.generatedAt(LocalDateTime.now())
					.build();

			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenReturn("generated/1/discrepancy-act_DA-12345678.pdf");
			when(generatedDocumentRepository.save(any(GeneratedDocument.class)))
					.thenReturn(discrepancyDoc);

			GeneratedDocumentResponse response = pdfGenerationService.generateDiscrepancyAct(discrepancyRequest, 1L);

			assertThat(response).isNotNull();
			assertThat(response.orderId()).isEqualTo(1L);
			assertThat(response.templateType()).isEqualTo("DISCREPANCY_ACT");
			verify(minioService).uploadPdfBytes(any(byte[].class), eq("generated/1"), anyString());
		}

		@Test
		@DisplayName("Should throw when MinIO upload fails for Discrepancy Act")
		void shouldThrowWhenMinioUploadFailsForDiscrepancyAct() throws Exception {
			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenThrow(new RuntimeException("Storage error"));

			assertThatThrownBy(() -> pdfGenerationService.generateDiscrepancyAct(discrepancyRequest, 1L))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Failed to generate Discrepancy Act");
		}
	}

	@Nested
	@DisplayName("Get Documents Tests")
	class GetDocumentsTests {

		@Test
		@DisplayName("Should get document by ID")
		void shouldGetDocumentById() {
			when(generatedDocumentRepository.findById(1L)).thenReturn(Optional.of(generatedDocument));

			GeneratedDocumentResponse response = pdfGenerationService.getDocument(1L);

			assertThat(response).isNotNull();
			assertThat(response.id()).isEqualTo(1L);
			assertThat(response.templateType()).isEqualTo("TTN");
		}

		@Test
		@DisplayName("Should throw when document not found")
		void shouldThrowWhenDocumentNotFound() {
			when(generatedDocumentRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> pdfGenerationService.getDocument(999L))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("not found");
		}

		@Test
		@DisplayName("Should get documents by order")
		void shouldGetDocumentsByOrder() {
			when(generatedDocumentRepository.findByOrderId(1L))
					.thenReturn(List.of(generatedDocument));

			List<GeneratedDocumentResponse> responses = pdfGenerationService.getDocumentsByOrder(1L);

			assertThat(responses).hasSize(1);
			assertThat(responses.get(0).orderId()).isEqualTo(1L);
		}

		@Test
		@DisplayName("Should return empty list when no documents for order")
		void shouldReturnEmptyListWhenNoDocuments() {
			when(generatedDocumentRepository.findByOrderId(999L)).thenReturn(List.of());

			List<GeneratedDocumentResponse> responses = pdfGenerationService.getDocumentsByOrder(999L);

			assertThat(responses).isEmpty();
		}
	}

	@Nested
	@DisplayName("Download Document Tests")
	class DownloadDocumentTests {

		@Test
		@DisplayName("Should download generated document")
		void shouldDownloadGeneratedDocument() throws Exception {
			when(generatedDocumentRepository.findById(1L)).thenReturn(Optional.of(generatedDocument));
			when(minioService.downloadFile(anyString()))
					.thenReturn(new ByteArrayInputStream("PDF content".getBytes()));

			InputStream result = pdfGenerationService.downloadGeneratedDocument(1L);

			assertThat(result).isNotNull();
			verify(minioService).downloadFile("generated/1/ttn_TTN-12345678.pdf");
		}

		@Test
		@DisplayName("Should throw when download fails")
		void shouldThrowWhenDownloadFails() throws Exception {
			when(generatedDocumentRepository.findById(1L)).thenReturn(Optional.of(generatedDocument));
			when(minioService.downloadFile(anyString()))
					.thenThrow(new RuntimeException("Download error"));

			assertThatThrownBy(() -> pdfGenerationService.downloadGeneratedDocument(1L))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Failed to download");
		}
	}

	@Nested
	@DisplayName("TTN Generation with Optional Fields Tests")
	class TtnOptionalFieldsTests {

		@Test
		@DisplayName("Should generate TTN with transport info")
		void shouldGenerateTtnWithTransportInfo() throws Exception {
			TransportInfoDto transport = new TransportInfoDto(
					"МАЗ", "1234 AB-7", "Водитель В.В.", null, null, null
			);

			TtnGenerationRequest requestWithTransport = new TtnGenerationRequest(
					ttnRequest.orderId(), ttnRequest.orderNumber(), ttnRequest.documentDate(),
					ttnRequest.series(), ttnRequest.shipper(), ttnRequest.consignee(),
					ttnRequest.payer(), ttnRequest.loadingPoint(), ttnRequest.unloadingPoint(),
					transport, ttnRequest.items(),
					ttnRequest.totalWithoutVat(), ttnRequest.totalVat(), ttnRequest.totalWithVat(),
					ttnRequest.totalWeight(), ttnRequest.totalPackages(),
					ttnRequest.releaseReason(), ttnRequest.notes()
			);

			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenReturn("generated/1/ttn.pdf");
			when(generatedDocumentRepository.save(any(GeneratedDocument.class)))
					.thenReturn(generatedDocument);

			GeneratedDocumentResponse response = pdfGenerationService.generateTTN(requestWithTransport, 1L);

			assertThat(response).isNotNull();
		}

		@Test
		@DisplayName("Should generate TTN with payer different from consignee")
		void shouldGenerateTtnWithSeparatePayer() throws Exception {
			CompanyInfoDto payer = new CompanyInfoDto(
					"ООО Плательщик", "111222333", null, null, null, null, null
			);

			TtnGenerationRequest requestWithPayer = new TtnGenerationRequest(
					ttnRequest.orderId(), ttnRequest.orderNumber(), ttnRequest.documentDate(),
					ttnRequest.series(), ttnRequest.shipper(), ttnRequest.consignee(),
					payer, ttnRequest.loadingPoint(), ttnRequest.unloadingPoint(),
					ttnRequest.transport(), ttnRequest.items(),
					ttnRequest.totalWithoutVat(), ttnRequest.totalVat(), ttnRequest.totalWithVat(),
					ttnRequest.totalWeight(), ttnRequest.totalPackages(),
					ttnRequest.releaseReason(), ttnRequest.notes()
			);

			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenReturn("generated/1/ttn.pdf");
			when(generatedDocumentRepository.save(any(GeneratedDocument.class)))
					.thenReturn(generatedDocument);

			GeneratedDocumentResponse response = pdfGenerationService.generateTTN(requestWithPayer, 1L);

			assertThat(response).isNotNull();
		}

		@Test
		@DisplayName("Should generate TTN with notes")
		void shouldGenerateTtnWithNotes() throws Exception {
			TtnGenerationRequest requestWithNotes = new TtnGenerationRequest(
					ttnRequest.orderId(), ttnRequest.orderNumber(), ttnRequest.documentDate(),
					ttnRequest.series(), ttnRequest.shipper(), ttnRequest.consignee(),
					ttnRequest.payer(), ttnRequest.loadingPoint(), ttnRequest.unloadingPoint(),
					ttnRequest.transport(), ttnRequest.items(),
					ttnRequest.totalWithoutVat(), ttnRequest.totalVat(), ttnRequest.totalWithVat(),
					ttnRequest.totalWeight(), ttnRequest.totalPackages(),
					"Договор №123 от 01.01.2025", "Хрупкий груз"
			);

			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenReturn("generated/1/ttn.pdf");
			when(generatedDocumentRepository.save(any(GeneratedDocument.class)))
					.thenReturn(generatedDocument);

			GeneratedDocumentResponse response = pdfGenerationService.generateTTN(requestWithNotes, 1L);

			assertThat(response).isNotNull();
		}
	}

	@Nested
	@DisplayName("Discrepancy Act Generation with Optional Fields Tests")
	class DiscrepancyOptionalFieldsTests {

		@Test
		@DisplayName("Should generate act with resolution proposal")
		void shouldGenerateActWithResolutionProposal() throws Exception {
			DiscrepancyActRequest requestWithProposal = new DiscrepancyActRequest(
					discrepancyRequest.orderId(), discrepancyRequest.orderNumber(),
					discrepancyRequest.ttnNumber(), discrepancyRequest.ttnDate(),
					discrepancyRequest.actDate(), discrepancyRequest.supplier(),
					discrepancyRequest.buyer(), discrepancyRequest.items(),
					discrepancyRequest.totalDiscrepancyAmount(), discrepancyRequest.commissionMembers(),
					discrepancyRequest.conclusion(),
					"Выставить претензию поставщику",
					"Акт составлен в присутствии представителя поставщика"
			);

			GeneratedDocument discrepancyDoc = GeneratedDocument.builder()
					.id(2L)
					.templateType(GeneratedDocument.TemplateType.DISCREPANCY_ACT)
					.orderId(1L)
					.fileKey("generated/1/act.pdf")
					.generatedBy(1L)
					.documentNumber("DA-12345678")
					.documentDate(LocalDate.now())
					.build();

			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenReturn("generated/1/act.pdf");
			when(generatedDocumentRepository.save(any(GeneratedDocument.class)))
					.thenReturn(discrepancyDoc);

			GeneratedDocumentResponse response = pdfGenerationService.generateDiscrepancyAct(requestWithProposal, 1L);

			assertThat(response).isNotNull();
		}

		@Test
		@DisplayName("Should generate act without commission members")
		void shouldGenerateActWithoutCommissionMembers() throws Exception {
			DiscrepancyActRequest requestWithoutCommission = new DiscrepancyActRequest(
					discrepancyRequest.orderId(), discrepancyRequest.orderNumber(),
					discrepancyRequest.ttnNumber(), discrepancyRequest.ttnDate(),
					discrepancyRequest.actDate(), discrepancyRequest.supplier(),
					discrepancyRequest.buyer(), discrepancyRequest.items(),
					discrepancyRequest.totalDiscrepancyAmount(), null,
					discrepancyRequest.conclusion(), null, null
			);

			GeneratedDocument discrepancyDoc = GeneratedDocument.builder()
					.id(2L)
					.templateType(GeneratedDocument.TemplateType.DISCREPANCY_ACT)
					.orderId(1L)
					.fileKey("generated/1/act.pdf")
					.generatedBy(1L)
					.documentNumber("DA-12345678")
					.documentDate(LocalDate.now())
					.build();

			when(minioService.uploadPdfBytes(any(byte[].class), anyString(), anyString()))
					.thenReturn("generated/1/act.pdf");
			when(generatedDocumentRepository.save(any(GeneratedDocument.class)))
					.thenReturn(discrepancyDoc);

			GeneratedDocumentResponse response = pdfGenerationService.generateDiscrepancyAct(requestWithoutCommission, 1L);

			assertThat(response).isNotNull();
		}
	}
}
