package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.*;
import by.bsuir.catalogservice.service.PartnershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;






@RestController
@RequestMapping("/api/partnerships")
@RequiredArgsConstructor
@Tag(name = "Partnerships", description = "API управления партнёрствами и договорами")
public class PartnershipController {

	private final PartnershipService partnershipService;



	@PostMapping
	@Operation(summary = "Отправить запрос на сотрудничество (торговая сеть)")
	public ResponseEntity<ApiResponse<PartnershipResponse>> createPartnershipRequest(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@Valid @RequestBody PartnershipRequest request) {
		PartnershipResponse response = partnershipService.createPartnershipRequest(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response, "Partnership request sent"));
	}

	@GetMapping("/customer")
	@Operation(summary = "Получить список партнёрств торговой сети (база поставщиков)")
	public ResponseEntity<ApiResponse<List<PartnershipResponse>>> getCustomerPartnerships(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId) {
		List<PartnershipResponse> partnerships = partnershipService.getCustomerPartnerships(customerId);
		return ResponseEntity.ok(ApiResponse.success(partnerships));
	}



	@GetMapping("/supplier")
	@Operation(summary = "Получить все партнёрства поставщика")
	public ResponseEntity<ApiResponse<List<PartnershipResponse>>> getSupplierPartnerships(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		List<PartnershipResponse> partnerships = partnershipService.getSupplierPartnerships(supplierId);
		return ResponseEntity.ok(ApiResponse.success(partnerships));
	}

	@GetMapping("/supplier/pending")
	@Operation(summary = "Вкладка «Новые заявки» (поставщик)")
	public ResponseEntity<ApiResponse<List<PartnershipResponse>>> getSupplierPendingRequests(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		List<PartnershipResponse> requests = partnershipService.getSupplierPendingRequests(supplierId);
		return ResponseEntity.ok(ApiResponse.success(requests));
	}

	@GetMapping("/supplier/active")
	@Operation(summary = "Вкладка «Активные договоры» (поставщик)")
	public ResponseEntity<ApiResponse<List<PartnershipResponse>>> getSupplierActivePartners(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		List<PartnershipResponse> partners = partnershipService.getSupplierActivePartners(supplierId);
		return ResponseEntity.ok(ApiResponse.success(partners));
	}

	@PostMapping("/{id}/accept")
	@Operation(summary = "Подтвердить заявку на сотрудничество (поставщик)")
	public ResponseEntity<ApiResponse<PartnershipResponse>> acceptPartnership(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		PartnershipResponse response = partnershipService.acceptPartnership(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(response, "Partnership accepted"));
	}

	@PostMapping("/{id}/reject")
	@Operation(summary = "Отклонить заявку на сотрудничество (поставщик)")
	public ResponseEntity<ApiResponse<PartnershipResponse>> rejectPartnership(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		PartnershipResponse response = partnershipService.rejectPartnership(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(response, "Partnership rejected"));
	}

	@PutMapping("/{id}/contract")
	@Operation(summary = "Обновить данные договора (поставщик: «Исправить» / «Изменить договор»)")
	public ResponseEntity<ApiResponse<PartnershipResponse>> updateContract(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@Valid @RequestBody ContractUpdateRequest request) {
		PartnershipResponse response = partnershipService.updateContract(id, supplierId, request);
		return ResponseEntity.ok(ApiResponse.success(response, "Contract updated"));
	}



	@GetMapping("/suppliers")
	@Operation(summary = "Список всех поставщиков с статусом партнёрства (ТЗ стр. 20)",
			description = "3 состояния карточки: NEW, PENDING, ACTIVE")
	public ResponseEntity<ApiResponse<java.util.List<SupplierWithPartnershipResponse>>> getAllSuppliersWithStatus(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@RequestParam(required = false) @Parameter(description = "Search by name or taxId") String search) {
		java.util.List<SupplierWithPartnershipResponse> suppliers =
				partnershipService.getAllSuppliersWithPartnershipStatus(customerId, search);
		return ResponseEntity.ok(ApiResponse.success(suppliers));
	}
}
