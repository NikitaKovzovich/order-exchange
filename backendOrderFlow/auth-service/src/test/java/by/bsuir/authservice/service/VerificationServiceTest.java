package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

	@Mock
	private VerificationRequestRepository verificationRequestRepository;

	@Mock
	private VerificationDocumentRepository verificationDocumentRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CompanyRepository companyRepository;

	@InjectMocks
	private VerificationService verificationService;

	private VerificationRequest testRequest;
	private User testUser;
	private User reviewer;
	private Company testCompany;

	@BeforeEach
	void setUp() {
		testCompany = Company.builder()
				.id(1L)
				.name("Test Company")
				.status(Company.CompanyStatus.PENDING_VERIFICATION)
				.build();

		testUser = User.builder()
				.id(1L)
				.email("test@example.com")
				.isActive(false)
				.company(testCompany)
				.build();

		reviewer = User.builder()
				.id(2L)
				.email("admin@example.com")
				.role(User.Role.ADMIN)
				.isActive(true)
				.build();

		testRequest = VerificationRequest.builder()
				.id(1L)
				.user(testUser)
				.company(testCompany)
				.status(VerificationRequest.VerificationStatus.PENDING)
				.requestedAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Get Pending Requests Tests")
	class GetPendingRequestsTests {

		@Test
		@DisplayName("Should return pending requests")
		void shouldReturnPendingRequests() {
			when(verificationRequestRepository.findByStatus(VerificationRequest.VerificationStatus.PENDING))
					.thenReturn(List.of(testRequest));

			List<VerificationRequest> result = verificationService.getPendingRequests();

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getStatus()).isEqualTo(VerificationRequest.VerificationStatus.PENDING);
		}

		@Test
		@DisplayName("Should return empty list when no pending requests")
		void shouldReturnEmptyListWhenNoPendingRequests() {
			when(verificationRequestRepository.findByStatus(VerificationRequest.VerificationStatus.PENDING))
					.thenReturn(List.of());

			List<VerificationRequest> result = verificationService.getPendingRequests();

			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("Approve Verification Tests")
	class ApproveVerificationTests {

		@Test
		@DisplayName("Should approve verification successfully")
		void shouldApproveVerificationSuccessfully() {
			when(verificationRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
			when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
			when(verificationRequestRepository.save(any())).thenReturn(testRequest);
			when(userRepository.save(any())).thenReturn(testUser);
			when(companyRepository.save(any())).thenReturn(testCompany);

			verificationService.approveVerification(1L, 2L);

			verify(verificationRequestRepository).save(argThat(req ->
					req.getStatus() == VerificationRequest.VerificationStatus.APPROVED));
			verify(userRepository).save(argThat(user -> user.getIsActive()));
			verify(companyRepository).save(argThat(company ->
					company.getStatus() == Company.CompanyStatus.ACTIVE));
		}

		@Test
		@DisplayName("Should throw when request not found")
		void shouldThrowWhenRequestNotFound() {
			when(verificationRequestRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> verificationService.approveVerification(999L, 2L))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Verification request not found");
		}

		@Test
		@DisplayName("Should throw when reviewer not found")
		void shouldThrowWhenReviewerNotFound() {
			when(verificationRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
			when(userRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> verificationService.approveVerification(1L, 999L))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Reviewer not found");
		}
	}

	@Nested
	@DisplayName("Reject Verification Tests")
	class RejectVerificationTests {

		@Test
		@DisplayName("Should reject verification successfully")
		void shouldRejectVerificationSuccessfully() {
			when(verificationRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
			when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
			when(verificationRequestRepository.save(any())).thenReturn(testRequest);
			when(companyRepository.save(any())).thenReturn(testCompany);

			verificationService.rejectVerification(1L, 2L, "Invalid documents");

			verify(verificationRequestRepository).save(argThat(req ->
					req.getStatus() == VerificationRequest.VerificationStatus.REJECTED &&
							"Invalid documents".equals(req.getRejectionReason())));
			verify(companyRepository).save(argThat(company ->
					company.getStatus() == Company.CompanyStatus.REJECTED));
		}
	}

	@Nested
	@DisplayName("Get Verification Documents Tests")
	class GetVerificationDocumentsTests {

		@Test
		@DisplayName("Should return documents for request")
		void shouldReturnDocumentsForRequest() {
			VerificationDocument doc = VerificationDocument.builder()
					.id(1L)
					.documentName("certificate.pdf")
					.documentType("REGISTRATION_CERTIFICATE")
					.build();

			when(verificationDocumentRepository.findByVerificationRequestId(1L))
					.thenReturn(List.of(doc));

			List<VerificationDocument> result = verificationService.getVerificationDocuments(1L);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getDocumentName()).isEqualTo("certificate.pdf");
		}
	}

	@Nested
	@DisplayName("Add Document Tests")
	class AddDocumentTests {

		@Test
		@DisplayName("Should add document successfully")
		void shouldAddDocumentSuccessfully() {
			when(verificationRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
			when(verificationDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			verificationService.addDocument(1L, "certificate.pdf", "/docs/cert.pdf", "REGISTRATION_CERTIFICATE");

			verify(verificationDocumentRepository).save(argThat(doc ->
					"certificate.pdf".equals(doc.getDocumentName()) &&
							"/docs/cert.pdf".equals(doc.getDocumentPath())));
		}
	}
}
