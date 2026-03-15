package by.bsuir.authservice.controller;

import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.AddressRepository;
import by.bsuir.authservice.repository.BankAccountRepository;
import by.bsuir.authservice.repository.CompanyDocumentRepository;
import by.bsuir.authservice.repository.ResponsiblePersonRepository;
import by.bsuir.authservice.repository.SupplierSettingsRepository;
import by.bsuir.authservice.repository.VerificationRequestRepository;
import by.bsuir.authservice.service.EventPublisher;
import by.bsuir.authservice.service.FileStorageService;
import by.bsuir.authservice.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	private final AddressRepository addressRepository;
	private final BankAccountRepository bankAccountRepository;
	private final ResponsiblePersonRepository responsiblePersonRepository;
	private final CompanyDocumentRepository companyDocumentRepository;
	private final SupplierSettingsRepository supplierSettingsRepository;
	private final EventPublisher eventPublisher;
	private final FileStorageService fileStorageService;

	@GetMapping
	@Operation(summary = "Get verification requests with pagination, status filter and search")
	public ResponseEntity<Map<String, Object>> getVerificationRequests(
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "role", required = false) String role,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		VerificationRequest.VerificationStatus statusFilter = null;
		if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
			statusFilter = VerificationRequest.VerificationStatus.valueOf(status.toUpperCase());
		}

		User.Role roleFilter = null;
		if (role != null && !role.isEmpty() && !"ALL".equalsIgnoreCase(role)) {
			roleFilter = User.Role.valueOf(role.toUpperCase());
		}

		boolean hasSearch = search != null && !search.trim().isEmpty();
		Page<VerificationRequest> requestsPage;

		if (hasSearch && statusFilter != null && roleFilter != null) {
			requestsPage = verificationRequestRepository.searchByUserRoleAndStatusAndCompanyNameOrTaxId(
					roleFilter, statusFilter, search.trim(), pageable);
		} else if (hasSearch && roleFilter != null) {
			requestsPage = verificationRequestRepository.searchByUserRoleAndCompanyNameOrTaxId(
					roleFilter, search.trim(), pageable);
		} else if (hasSearch && statusFilter != null) {
			requestsPage = verificationRequestRepository.searchByStatusAndCompanyNameOrTaxId(
					statusFilter, search.trim(), pageable);
		} else if (hasSearch) {
			requestsPage = verificationRequestRepository.searchByCompanyNameOrTaxId(search.trim(), pageable);
		} else if (statusFilter != null && roleFilter != null) {
			requestsPage = verificationRequestRepository.findByUserRoleAndStatus(roleFilter, statusFilter, pageable);
		} else if (roleFilter != null) {
			requestsPage = verificationRequestRepository.findByUserRole(roleFilter, pageable);
		} else if (statusFilter != null) {
			requestsPage = verificationRequestRepository.findByStatus(statusFilter, pageable);
		} else {
			requestsPage = verificationRequestRepository.findAll(pageable);
		}

		List<Map<String, Object>> content = requestsPage.getContent().stream()
				.map(this::mapVerificationRequestToResponse)
				.collect(Collectors.toList());

		Map<String, Object> pageData = new LinkedHashMap<>();
		pageData.put("content", content);
		pageData.put("page", requestsPage.getNumber());
		pageData.put("size", requestsPage.getSize());
		pageData.put("totalElements", requestsPage.getTotalElements());
		pageData.put("totalPages", requestsPage.getTotalPages());

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("data", pageData);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/pending")
	@Operation(summary = "Get pending verification requests")
	public ResponseEntity<List<Map<String, Object>>> getPendingRequests() {
		List<VerificationRequest> requests = verificationService.getPendingRequests();
		List<Map<String, Object>> result = requests.stream()
				.map(this::mapVerificationRequestToResponse)
				.collect(Collectors.toList());
		return ResponseEntity.ok(result);
	}

	@GetMapping("/{requestId}")
	@Operation(summary = "Get full verification request detail with company profile and documents")
	public ResponseEntity<Map<String, Object>> getVerificationRequestDetail(@PathVariable Long requestId) {
		VerificationRequest request = verificationService.getVerificationRequest(requestId);
		Company company = request.getCompany();
		Long companyId = company.getId();

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("id", request.getId());
		response.put("status", request.getStatus().name());
		response.put("submittedAt", request.getCreatedAt());
		response.put("reviewedAt", request.getReviewedAt());
		response.put("rejectionReason", request.getRejectionReason());
		response.put("role", request.getUser().getRole().name());
		Map<String, Object> companyInfo = new LinkedHashMap<>();
		companyInfo.put("id", company.getId());
		companyInfo.put("legalName", company.getLegalName());
		companyInfo.put("name", company.getName());
		companyInfo.put("legalForm", company.getLegalForm().name());
		companyInfo.put("legalFormText", company.getLegalFormText());
		companyInfo.put("taxId", company.getTaxId());
		companyInfo.put("registrationDate", company.getRegistrationDate());
		companyInfo.put("contactPhone", company.getContactPhone());
		companyInfo.put("contactEmail", request.getUser().getEmail());
		companyInfo.put("status", company.getStatus().name());
		response.put("company", companyInfo);
		List<Address> addresses = addressRepository.findByCompanyId(companyId);
		Map<String, String> addressMap = new LinkedHashMap<>();
		for (Address addr : addresses) {
			addressMap.put(addr.getAddressType().name(), addr.getFullAddress());
		}
		response.put("addresses", addressMap);
		Map<String, Object> requisites = new LinkedHashMap<>();
		List<ResponsiblePerson> persons = responsiblePersonRepository.findByCompanyId(companyId);
		for (ResponsiblePerson p : persons) {
			if (p.getPosition() == ResponsiblePerson.Position.director) {
				requisites.put("directorName", p.getFullName());
			} else if (p.getPosition() == ResponsiblePerson.Position.chief_accountant) {
				requisites.put("chiefAccountantName", p.getFullName());
			}
		}

		bankAccountRepository.findByCompanyId(companyId).ifPresent(bank -> {
			requisites.put("bankName", bank.getBankName());
			requisites.put("bic", bank.getBic());
			requisites.put("accountNumber", bank.getAccountNumber());
		});
		response.put("requisites", requisites);
		List<Map<String, Object>> documents = new ArrayList<>();

		List<CompanyDocument> companyDocs = companyDocumentRepository.findByCompanyId(companyId);
		for (CompanyDocument doc : companyDocs) {
			Map<String, Object> d = new LinkedHashMap<>();
			d.put("id", doc.getId());
			d.put("type", doc.getDocumentType().name());
			d.put("originalFilename", doc.getOriginalFilename());
			d.put("downloadUrl", fileStorageService.getPresignedUrl(doc.getFilePath()));
			d.put("isImage", isImageFile(doc.getOriginalFilename()));
			documents.add(d);
		}
		List<VerificationDocument> verDocs = request.getDocuments();
		if (verDocs != null) {
			for (VerificationDocument doc : verDocs) {
				Map<String, Object> d = new LinkedHashMap<>();
				d.put("id", doc.getId());
				d.put("type", doc.getDocumentType());
				d.put("originalFilename", doc.getDocumentName());
				d.put("downloadUrl", fileStorageService.getPresignedUrl(doc.getDocumentPath()));
				d.put("isImage", isImageFile(doc.getDocumentName()));
				documents.add(d);
			}
		}
		response.put("documents", documents);
		if (request.getUser().getRole() == User.Role.SUPPLIER) {
			supplierSettingsRepository.findById(companyId).ifPresent(settings -> {
				response.put("paymentTerms", settings.getPaymentTerms().name());
			});
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{requestId}/documents")
	@Operation(summary = "Get documents for verification request")
	public ResponseEntity<List<Map<String, Object>>> getDocuments(@PathVariable Long requestId) {
		VerificationRequest request = verificationService.getVerificationRequest(requestId);
		Long companyId = request.getCompany().getId();

		List<Map<String, Object>> documents = new ArrayList<>();

		companyDocumentRepository.findByCompanyId(companyId).forEach(doc -> {
			Map<String, Object> d = new LinkedHashMap<>();
			d.put("id", doc.getId());
			d.put("type", doc.getDocumentType().name());
			d.put("originalFilename", doc.getOriginalFilename());
			d.put("downloadUrl", fileStorageService.getPresignedUrl(doc.getFilePath()));
			documents.add(d);
		});

		List<VerificationDocument> verDocs = verificationService.getVerificationDocuments(requestId);
		verDocs.forEach(doc -> {
			Map<String, Object> d = new LinkedHashMap<>();
			d.put("id", doc.getId());
			d.put("type", doc.getDocumentType());
			d.put("originalFilename", doc.getDocumentName());
			d.put("downloadUrl", fileStorageService.getPresignedUrl(doc.getDocumentPath()));
			documents.add(d);
		});

		return ResponseEntity.ok(documents);
	}

	@PostMapping("/{requestId}/approve")
	@Operation(summary = "Approve verification request")
	public ResponseEntity<Map<String, String>> approveVerification(
			@PathVariable Long requestId,
			@RequestHeader("X-User-Id") Long reviewerId) {

		verificationService.approveVerification(requestId, reviewerId);

		VerificationRequest req = verificationService.getVerificationRequest(requestId);
		Company company = req.getCompany();

		eventPublisher.publish("VerificationRequest", req.getId().toString(),
				"VerificationRequestApproved",
				Map.of(
						"verificationRequestId", req.getId(),
						"companyId", company.getId(),
						"companyName", company.getLegalName() != null ? company.getLegalName() : "",
						"reviewerId", reviewerId,
						"newCompanyStatus", company.getStatus().name()
				));

		eventPublisher.publish("Company", company.getId().toString(),
				"CompanyVerified",
				Map.of(
						"companyId", company.getId(),
						"companyName", company.getLegalName() != null ? company.getLegalName() : "",
						"verifiedBy", reviewerId
				));

		return ResponseEntity.ok(Map.of("message", "Verification approved"));
	}

	@PostMapping("/{requestId}/reject")
	@Operation(summary = "Reject verification request with reason")
	public ResponseEntity<Map<String, String>> rejectVerification(
			@PathVariable Long requestId,
			@RequestBody Map<String, String> body,
			@RequestHeader("X-User-Id") Long reviewerId) {

		String reason = body.get("reason");
		if (reason == null || reason.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Rejection reason is required"));
		}

		verificationService.rejectVerification(requestId, reviewerId, reason);

		VerificationRequest req = verificationService.getVerificationRequest(requestId);
		Company company = req.getCompany();

		eventPublisher.publish("VerificationRequest", req.getId().toString(),
				"VerificationRequestRejected",
				Map.of(
						"verificationRequestId", req.getId(),
						"companyId", company.getId(),
						"companyName", company.getLegalName() != null ? company.getLegalName() : "",
						"reviewerId", reviewerId,
						"rejectionReason", reason,
						"newCompanyStatus", company.getStatus().name()
				));

		return ResponseEntity.ok(Map.of("message", "Verification rejected"));
	}

	private Map<String, Object> mapVerificationRequestToResponse(VerificationRequest req) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", req.getId());
		map.put("companyId", req.getCompany().getId());
		map.put("companyName", req.getCompany().getLegalName());
		map.put("taxId", req.getCompany().getTaxId());
		map.put("role", req.getUser().getRole().name());
		map.put("status", req.getStatus().name());
		map.put("submittedAt", req.getCreatedAt());
		map.put("rejectionReason", req.getRejectionReason());
		return map;
	}

	private boolean isImageFile(String filename) {
		if (filename == null) return false;
		String lower = filename.toLowerCase();
		return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
				|| lower.endsWith(".png") || lower.endsWith(".gif")
				|| lower.endsWith(".webp");
	}
}
