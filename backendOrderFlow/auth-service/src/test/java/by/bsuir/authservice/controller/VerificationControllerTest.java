package by.bsuir.authservice.controller;

import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.entity.VerificationDocument;
import by.bsuir.authservice.entity.VerificationRequest;
import by.bsuir.authservice.repository.CompanyRepository;
import by.bsuir.authservice.repository.VerificationRequestRepository;
import by.bsuir.authservice.service.EventPublisher;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	private CompanyRepository companyRepository;

	@MockBean
	private EventPublisher eventPublisher;

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
	@DisplayName("Get All Verification Requests Tests")
	class GetAllVerificationRequestsTests {

		@Test
		@DisplayName("Should return all verification requests")
		void shouldReturnAllVerificationRequests() throws Exception {
			when(verificationRequestRepository.findAll()).thenReturn(List.of(testRequest));

			mockMvc.perform(get("/api/admin/verification"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(1))
					.andExpect(jsonPath("$[0].id").value(1))
					.andExpect(jsonPath("$[0].companyName").value("ООО Test Company"))
					.andExpect(jsonPath("$[0].status").value("PENDING"));
		}

		@Test
		@DisplayName("Should filter by status")
		void shouldFilterByStatus() throws Exception {
			when(verificationRequestRepository.findByStatus(VerificationRequest.VerificationStatus.PENDING))
					.thenReturn(List.of(testRequest));

			mockMvc.perform(get("/api/admin/verification")
							.param("status", "PENDING"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(1));
		}

		@Test
		@DisplayName("Should return empty list")
		void shouldReturnEmptyList() throws Exception {
			when(verificationRequestRepository.findAll()).thenReturn(List.of());

			mockMvc.perform(get("/api/admin/verification"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(0));
		}
	}

	@Nested
	@DisplayName("Get Pending Requests Tests")
	class GetPendingRequestsTests {

		@Test
		@DisplayName("Should return pending requests")
		void shouldReturnPendingRequests() throws Exception {
			when(verificationService.getPendingRequests()).thenReturn(List.of(testRequest));

			mockMvc.perform(get("/api/admin/verification/pending")
							.header("X-User-Email", "admin@example.com"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(1));
		}
	}

	@Nested
	@DisplayName("Get Verification Request By Id Tests")
	class GetVerificationRequestByIdTests {

		@Test
		@DisplayName("Should return verification request by id")
		void shouldReturnVerificationRequestById() throws Exception {
			when(verificationService.getVerificationRequest(1L)).thenReturn(testRequest);

			mockMvc.perform(get("/api/admin/verification/1")
							.header("X-User-Email", "admin@example.com"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.companyId").value(1))
					.andExpect(jsonPath("$.companyName").value("ООО Test Company"))
					.andExpect(jsonPath("$.status").value("PENDING"));
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

			when(verificationService.getVerificationDocuments(1L)).thenReturn(List.of(document));

			mockMvc.perform(get("/api/admin/verification/1/documents")
							.header("X-User-Email", "admin@example.com"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(1));
		}

		@Test
		@DisplayName("Should return empty list when no documents")
		void shouldReturnEmptyListWhenNoDocuments() throws Exception {
			when(verificationService.getVerificationDocuments(1L)).thenReturn(List.of());

			mockMvc.perform(get("/api/admin/verification/1/documents")
							.header("X-User-Email", "admin@example.com"))
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
			when(verificationRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
			when(companyRepository.save(any(Company.class))).thenReturn(testCompany);
			when(verificationRequestRepository.save(any(VerificationRequest.class))).thenReturn(testRequest);

			mockMvc.perform(post("/api/admin/verification/1/approve")
							.header("X-User-Id", "2"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Верификация одобрена"));

			verify(verificationService).approveVerification(1L, 2L);
			verify(eventPublisher, times(2)).publish(anyString(), anyString(), anyString(), anyMap());
		}

		@Test
		@DisplayName("Should handle approve when request not found")
		void shouldHandleApproveWhenRequestNotFound() throws Exception {
			when(verificationRequestRepository.findById(999L)).thenReturn(Optional.empty());

			mockMvc.perform(post("/api/admin/verification/999/approve")
							.header("X-User-Id", "2"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Верификация одобрена"));

			verify(verificationService).approveVerification(999L, 2L);
		}
	}

	@Nested
	@DisplayName("Reject Verification Tests")
	class RejectVerificationTests {

		@Test
		@DisplayName("Should reject verification")
		void shouldRejectVerification() throws Exception {
			when(verificationRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
			when(verificationRequestRepository.save(any(VerificationRequest.class))).thenReturn(testRequest);

			Map<String, String> body = Map.of("reason", "Документы не соответствуют требованиям");

			mockMvc.perform(post("/api/admin/verification/1/reject")
							.header("X-User-Id", "2")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Верификация отклонена"));

			verify(verificationService).rejectVerification(1L, 2L, "Документы не соответствуют требованиям");
		}
	}
}
