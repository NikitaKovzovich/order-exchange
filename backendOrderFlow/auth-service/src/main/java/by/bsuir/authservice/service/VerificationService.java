package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationService {
	private final VerificationRequestRepository verificationRequestRepository;
	private final VerificationDocumentRepository verificationDocumentRepository;
	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;

	public List<VerificationRequest> getPendingRequests() {
		return verificationRequestRepository.findByStatus(VerificationRequest.VerificationStatus.PENDING);
	}

	public VerificationRequest getVerificationRequest(Long requestId) {
		return verificationRequestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Verification request not found"));
	}

	@Transactional
	public void approveVerification(Long requestId, Long reviewerId) {
		VerificationRequest request = getVerificationRequest(requestId);
		User reviewer = userRepository.findById(reviewerId)
				.orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

		request.setStatus(VerificationRequest.VerificationStatus.APPROVED);
		request.setReviewedAt(LocalDateTime.now());
		request.setReviewer(reviewer);
		verificationRequestRepository.save(request);

		User supplierUser = request.getUser();
		supplierUser.setIsActive(true);
		userRepository.save(supplierUser);

		Company company = request.getCompany();
		company.setStatus(Company.CompanyStatus.ACTIVE);
		companyRepository.save(company);
	}

	@Transactional
	public void rejectVerification(Long requestId, Long reviewerId, String rejectionReason) {
		VerificationRequest request = getVerificationRequest(requestId);
		User reviewer = userRepository.findById(reviewerId)
				.orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

		request.setStatus(VerificationRequest.VerificationStatus.REJECTED);
		request.setReviewedAt(LocalDateTime.now());
		request.setReviewer(reviewer);
		request.setRejectionReason(rejectionReason);
		verificationRequestRepository.save(request);

		Company company = request.getCompany();
		company.setStatus(Company.CompanyStatus.REJECTED);
		companyRepository.save(company);
	}

	public List<VerificationDocument> getVerificationDocuments(Long requestId) {
		return verificationDocumentRepository.findByVerificationRequestId(requestId);
	}

	@Transactional
	public void addDocument(Long requestId, String documentName, String documentPath, String documentType) {
		VerificationRequest request = getVerificationRequest(requestId);

		VerificationDocument document = VerificationDocument.builder()
				.verificationRequest(request)
				.documentName(documentName)
				.documentPath(documentPath)
				.documentType(documentType)
				.uploadedAt(LocalDateTime.now())
				.build();

		verificationDocumentRepository.save(document);
	}
}
