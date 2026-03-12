package by.bsuir.authservice.controller;

import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.entity.VerificationDocument;
import by.bsuir.authservice.entity.VerificationRequest;
import by.bsuir.authservice.repository.*;
import by.bsuir.authservice.service.EventPublisher;
import by.bsuir.authservice.service.FileStorageService;
import by.bsuir.authservice.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class VerificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private VerificationService verificationService;

	@MockBean
	private VerificationRequestRepository verificationRequestRepository;

	@MockBean
	private AddressRepository addressRepository;

	@MockBean
	private BankAccountRepository bankAccountRepository;

	@MockBean
	private ResponsiblePersonRepository responsiblePersonRepository;

	@MockBean
	private CompanyDocumentRepository companyDocumentRepository;

	@MockBean
	private SupplierSettingsRepository supplierSettingsRepository;

	@MockBean
	private EventPublisher eventPublisher;

	@MockBean
	private FileStorageService fileStorageService;

	private VerificationRequest testRequest;
	private Company testCompany;
	private User testUser;

	@BeforeEach
	void setUp() {
		testCompany = Company.builder()
				.id(1L)
				.name("Test Company")
				.legalName("ООО Test Company")
				.legalForm(Company.LegalForm.LLC)
				.taxId("1234567890")
				.status(Company.CompanyStatus.PENDING_VERIFICATION)
				.build();

		testUser = User.builder()
				.id(1L)
				.email("test@example.com")
				.role(User.Role.SUPPLIER)
				.company(testCompany)
				.build();

		testRequest = VerificationRequest.builder()
				.id(1L)
				.company(testCompany)
				.user(testUser)
				.status(VerificationRequest.VerificationStatus.PENDING)
				.createdAt(LocalDateTime.now())
				.requestedAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Get Verification Requests Tests")
	class GetVerificationRequestsTests {

		@Test
		@DisplayName("Should return paginated verification requests")
		void shouldReturnPaginatedRequests() throws Exception {
			when(verificationRequestRepository.findAll(any(Pageable.class)))
					.thenReturn(new PageImpl<>(List.of(testRequest)));

			mockMvc.perform(get("/api/admin/verification"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").isArray())
					.andExpect(jsonPath("$.content.length()").value(1))
					.andExpect(jsonPath("$.content[0].id").value(1))
					.andExpect(jsonPath("$.content[0].companyName").value("ООО Test Company"))
					.andExpect(jsonPath("$.content[0].status").value("PENDING"));
		}

		@Test
		@DisplayName("Should filter by status")
		void shouldFilterByStatus() throws Exception {
			when(verificationRequestRepository.findByStatus(
					eq(VerificationRequest.VerificationStatus.PENDING), any(Pageable.class)))
					.thenReturn(new PageImpl<>(List.of(testRequest)));

			mockMvc.perform(get("/api/admin/verification")
							.param("status", "PENDING"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").isArray())
					.andExpect(jsonPath("$.content.length()").value(1));
		}

		@Test
		@DisplayName("Should return empty page")
		void shouldReturnEmptyPage() throws Exception {
			when(verificationRequestRepository.findAll(any(Pageable.class)))
					.thenReturn(new PageImpl<>(List.of()));

			mockMvc.perform(get("/api/admin/verification"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").isArray())
					.andExpect(jsonPath("$.content.length()").value(0));
		}
	}

	@Nested
	@DisplayName("Get Pending Requests Tests")
	class GetPendingRequestsTests {

		@Test
		@DisplayName("Should return pending requests")
		void shouldReturnPendingRequests() throws Exception {
			when(verificationService.getPendingRequests()).thenReturn(List.of(testRequest));

			mockMvc.perform(get("/api/admin/verification/pending"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(1));
		}
	}

	@Nested
	@DisplayName("Get Verification Request Detail Tests")
	class GetVerificationRequestDetailTests {

		@Test
		@DisplayName("Should return full verification request detail")
		void shouldReturnFullDetail() throws Exception {
			when(verificationService.getVerificationRequest(1L)).thenReturn(testRequest);
			when(addressRepository.findByCompanyId(1L)).thenReturn(Collections.emptyList());
			when(responsiblePersonRepository.findByCompanyId(1L)).thenReturn(Collections.emptyList());
			when(bankAccountRepository.findByCompanyId(1L)).thenReturn(java.util.Optional.empty());
			when(companyDocumentRepository.findByCompanyId(1L)).thenReturn(Collections.emptyList());

			mockMvc.perform(get("/api/admin/verification/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.status").value("PENDING"))
					.andExpect(jsonPath("$.role").value("SUPPLIER"))
					.andExpect(jsonPath("$.company.legalName").value("ООО Test Company"))
					.andExpect(jsonPath("$.company.taxId").value("1234567890"))
					.andExpect(jsonPath("$.documents").isArray());
		}
	}

	@Nested
	@DisplayName("Get Documents Tests")
	class GetDocumentsTests {

		@Test
		@DisplayName("Should return documents for request")
		void shouldReturnDocumentsForRequest() throws Exception {
			VerificationDocument document = VerificationDocument.builder()
					.id(1L)
					.verificationRequest(testRequest)
					.documentType("REGISTRATION_CERTIFICATE")
					.documentPath("/path/to/document")
					.documentName("certificate.pdf")
					.uploadedAt(LocalDateTime.now())
					.build();

			when(verificationService.getVerificationRequest(1L)).thenReturn(testRequest);
			when(companyDocumentRepository.findByCompanyId(1L)).thenReturn(Collections.emptyList());
			when(verificationService.getVerificationDocuments(1L)).thenReturn(List.of(document));
			when(fileStorageService.getPresignedUrl(anyString())).thenReturn("http://minio/presigned-url");

			mockMvc.perform(get("/api/admin/verification/1/documents"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(1))
					.andExpect(jsonPath("$[0].originalFilename").value("certificate.pdf"));
		}

		@Test
		@DisplayName("Should return empty list when no documents")
		void shouldReturnEmptyListWhenNoDocuments() throws Exception {
			when(verificationService.getVerificationRequest(1L)).thenReturn(testRequest);
			when(companyDocumentRepository.findByCompanyId(1L)).thenReturn(Collections.emptyList());
			when(verificationService.getVerificationDocuments(1L)).thenReturn(List.of());

			mockMvc.perform(get("/api/admin/verification/1/documents"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(0));
		}
	}

	@Nested
	@DisplayName("Approve Verification Tests")
	class ApproveVerificationTests {

		@Test
		@DisplayName("Should approve verification")
		void shouldApproveVerification() throws Exception {
			testCompany.setStatus(Company.CompanyStatus.ACTIVE);
			when(verificationService.getVerificationRequest(1L)).thenReturn(testRequest);

			mockMvc.perform(post("/api/admin/verification/1/approve")
							.header("X-User-Id", "2"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Verification approved"));

			verify(verificationService).approveVerification(1L, 2L);
			verify(eventPublisher, times(2)).publish(anyString(), anyString(), anyString(), anyMap());
		}
	}

	@Nested
	@DisplayName("Reject Verification Tests")
	class RejectVerificationTests {

		@Test
		@DisplayName("Should reject verification with reason")
		void shouldRejectVerification() throws Exception {
			testCompany.setStatus(Company.CompanyStatus.REJECTED);
			when(verificationService.getVerificationRequest(1L)).thenReturn(testRequest);

			Map<String, String> body = Map.of("reason", "Documents not readable");

			mockMvc.perform(post("/api/admin/verification/1/reject")
							.header("X-User-Id", "2")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Verification rejected"));

			verify(verificationService).rejectVerification(1L, 2L, "Documents not readable");
		}

		@Test
		@DisplayName("Should reject when reason is empty")
		void shouldRejectWhenReasonIsEmpty() throws Exception {
			Map<String, String> body = Map.of("reason", "");

			mockMvc.perform(post("/api/admin/verification/1/reject")
							.header("X-User-Id", "2")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error").value("Rejection reason is required"));
		}
	}
}
