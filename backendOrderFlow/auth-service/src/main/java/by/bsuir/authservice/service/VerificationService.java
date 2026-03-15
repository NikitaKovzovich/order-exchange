package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationService {
	private final VerificationRequestRepository verificationRequestRepository;
	private final VerificationDocumentRepository verificationDocumentRepository;
	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final NotificationService notificationService;
	private final RabbitEventPublisher rabbitEventPublisher;
	private final EmailService emailService;

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
		supplierUser.setStatus("ACTIVE");
		userRepository.save(supplierUser);

		Company company = request.getCompany();
		company.setStatus(Company.CompanyStatus.ACTIVE);
		company.setVerified(true);
		companyRepository.save(company);
		notificationService.createNotification(supplierUser.getId(),
				"Регистрация одобрена",
				"Ваша заявка на регистрацию одобрена. Добро пожаловать на платформу!",
				Notification.NotificationType.VERIFICATION_APPROVED,
				"VerificationRequest", requestId);


		emailService.sendVerificationApprovedEmail(
				supplierUser.getEmail(),
				company.getLegalName() != null ? company.getLegalName() : "компания");

		rabbitEventPublisher.publish("verificationapproved", Map.of(
				"userId", supplierUser.getId(),
				"companyId", company.getId(),
				"companyName", company.getLegalName() != null ? company.getLegalName() : "",
				"reviewerId", reviewerId
		));
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

		User user = request.getUser();
		notificationService.createNotification(user.getId(),
				"Регистрация отклонена",
				"К сожалению, ваша заявка на регистрацию была отклонена. Причина: " + rejectionReason,
				Notification.NotificationType.VERIFICATION_REJECTED,
				"VerificationRequest", requestId);


		emailService.sendVerificationRejectedEmail(
				user.getEmail(),
				company.getLegalName() != null ? company.getLegalName() : "компания",
				rejectionReason);

		rabbitEventPublisher.publish("verificationrejected", Map.of(
				"userId", user.getId(),
				"companyId", company.getId(),
				"companyName", company.getLegalName() != null ? company.getLegalName() : "",
				"reason", rejectionReason
		));
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
