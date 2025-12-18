package by.bsuir.authservice.controller;

import by.bsuir.authservice.entity.VerificationDocument;
import by.bsuir.authservice.entity.VerificationRequest;
import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.service.EventPublisher;
import by.bsuir.authservice.service.VerificationService;
import by.bsuir.authservice.repository.VerificationRequestRepository;
import by.bsuir.authservice.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/verification")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Verification", description = "Company verification management API")
public class VerificationController {
	private final VerificationService verificationService;
	private final VerificationRequestRepository verificationRequestRepository;
	private final CompanyRepository companyRepository;
	private final EventPublisher eventPublisher;


	@GetMapping
	@Operation(summary = "Get all verification requests")
	public ResponseEntity<List<Map<String, Object>>> getAllVerificationRequests(
			@RequestParam(required = false) String status) {
		List<VerificationRequest> requests;

		if (status != null && !status.isEmpty()) {
			requests = verificationRequestRepository.findByStatus(VerificationRequest.VerificationStatus.valueOf(status));
		} else {
			requests = verificationRequestRepository.findAll();
		}

		List<Map<String, Object>> response = requests.stream().map(req -> {
			Map<String, Object> map = new HashMap<>();
			map.put("id", req.getId());
			map.put("companyId", req.getCompany().getId());
			map.put("companyName", req.getCompany().getLegalName());
			map.put("taxId", req.getCompany().getTaxId());
			map.put("status", req.getStatus().name());
			map.put("submittedAt", req.getCreatedAt());
			map.put("documents", req.getDocuments() != null ? req.getDocuments() : new ArrayList<>());
			return map;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(response);
	}


	@GetMapping("/pending")
	@Operation(summary = "Get pending verification requests")
	public ResponseEntity<List<VerificationRequest>> getPendingRequests(
			@RequestHeader("X-User-Email") String email) {
		List<VerificationRequest> requests = verificationService.getPendingRequests();
		return ResponseEntity.ok(requests);
	}


	@GetMapping("/{requestId}")
	@Operation(summary = "Get verification request by ID")
	public ResponseEntity<Map<String, Object>> getVerificationRequest(
			@PathVariable Long requestId,
			@RequestHeader("X-User-Email") String email) {
		VerificationRequest request = verificationService.getVerificationRequest(requestId);

		Map<String, Object> response = new HashMap<>();
		response.put("id", request.getId());
		response.put("companyId", request.getCompany().getId());
		response.put("companyName", request.getCompany().getLegalName());
		response.put("taxId", request.getCompany().getTaxId());
		response.put("status", request.getStatus().name());
		response.put("submittedAt", request.getCreatedAt());
		response.put("company", request.getCompany());
		response.put("documents", request.getDocuments() != null ? request.getDocuments() : new ArrayList<>());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{requestId}/documents")
	@Operation(summary = "Get documents for verification request")
	public ResponseEntity<List<VerificationDocument>> getDocuments(
			@PathVariable Long requestId,
			@RequestHeader("X-User-Email") String email) {
		List<VerificationDocument> documents = verificationService.getVerificationDocuments(requestId);
		return ResponseEntity.ok(documents);
	}

	@PostMapping("/{requestId}/approve")
	@Operation(summary = "Approve verification request")
	public ResponseEntity<Map<String, String>> approveVerification(
			@PathVariable Long requestId,
			@RequestHeader("X-User-Id") Long reviewerId) {
		verificationService.approveVerification(requestId, reviewerId);

		Optional<VerificationRequest> request = verificationRequestRepository.findById(requestId);
		if (request.isPresent()) {
			VerificationRequest req = request.get();
			req.setStatus(VerificationRequest.VerificationStatus.APPROVED);

			Company company = req.getCompany();
			company.setStatus(Company.CompanyStatus.ACTIVE);
			company.setVerified(true);
			companyRepository.save(company);

			verificationRequestRepository.save(req);

			eventPublisher.publish("VerificationRequest", req.getId().toString(),
				"VerificationRequestApproved",
				Map.of(
					"verificationRequestId", req.getId(),
					"companyId", company.getId(),
					"companyName", company.getLegalName(),
					"reviewerId", reviewerId,
					"newCompanyStatus", company.getStatus().name()
				));

			eventPublisher.publish("Company", company.getId().toString(),
				"CompanyVerified",
				Map.of(
					"companyId", company.getId(),
					"companyName", company.getLegalName(),
					"verifiedBy", reviewerId
				));
		}

		return ResponseEntity.ok(Collections.singletonMap("message", "Верификация одобрена"));
	}

	@PostMapping("/{requestId}/reject")
	@Operation(summary = "Reject verification request")
	public ResponseEntity<Map<String, String>> rejectVerification(
			@PathVariable Long requestId,
			@RequestBody Map<String, String> body,
			@RequestHeader("X-User-Id") Long reviewerId) {
		String reason = body.get("reason");
		verificationService.rejectVerification(requestId, reviewerId, reason);

		Optional<VerificationRequest> request = verificationRequestRepository.findById(requestId);
		if (request.isPresent()) {
			VerificationRequest req = request.get();
			req.setStatus(VerificationRequest.VerificationStatus.REJECTED);
			req.setRejectionReason(reason);

			Company company = req.getCompany();
			company.setStatus(Company.CompanyStatus.REJECTED);
			companyRepository.save(company);

			verificationRequestRepository.save(req);

			eventPublisher.publish("VerificationRequest", req.getId().toString(),
				"VerificationRequestRejected",
				Map.of(
					"verificationRequestId", req.getId(),
					"companyId", company.getId(),
					"companyName", company.getLegalName(),
					"reviewerId", reviewerId,
					"rejectionReason", reason,
					"newCompanyStatus", company.getStatus().name()
				));

			eventPublisher.publish("Company", company.getId().toString(),
				"CompanyRejected",
				Map.of(
					"companyId", company.getId(),
					"companyName", company.getLegalName(),
					"rejectedBy", reviewerId,
					"reason", reason
				));
		}

		return ResponseEntity.ok(Collections.singletonMap("message", "Верификация отклонена"));
	}
}
