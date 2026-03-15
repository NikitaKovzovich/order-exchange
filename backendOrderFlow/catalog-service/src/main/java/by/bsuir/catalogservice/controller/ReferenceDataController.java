package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.ApiResponse;
import by.bsuir.catalogservice.dto.UnitRequest;
import by.bsuir.catalogservice.dto.VatRateRequest;
import by.bsuir.catalogservice.entity.UnitOfMeasure;
import by.bsuir.catalogservice.entity.VatRate;
import by.bsuir.catalogservice.repository.ProductRepository;
import by.bsuir.catalogservice.repository.UnitOfMeasureRepository;
import by.bsuir.catalogservice.repository.VatRateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reference Data", description = "Units of measure and VAT rates")
public class ReferenceDataController {

	private final UnitOfMeasureRepository unitOfMeasureRepository;
	private final VatRateRepository vatRateRepository;
	private final ProductRepository productRepository;



	@GetMapping("/units")
	@Operation(summary = "Get all units of measure")
	public ResponseEntity<ApiResponse<List<UnitOfMeasure>>> getAllUnits() {
		List<UnitOfMeasure> units = unitOfMeasureRepository.findAll();
		return ResponseEntity.ok(ApiResponse.success(units, "Units retrieved successfully"));
	}

	@GetMapping("/units/{id}")
	@Operation(summary = "Get unit by ID")
	public ResponseEntity<ApiResponse<UnitOfMeasure>> getUnitById(@PathVariable Long id) {
		return unitOfMeasureRepository.findById(id)
				.map(unit -> ResponseEntity.ok(ApiResponse.success(unit, "Unit found")))
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/units")
	@Operation(summary = "Create unit of measure (#17)")
	public ResponseEntity<ApiResponse<UnitOfMeasure>> createUnit(@Valid @RequestBody UnitRequest request) {
		if (unitOfMeasureRepository.existsByName(request.name())) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Unit with this name already exists"));
		}
		UnitOfMeasure unit = UnitOfMeasure.builder().name(request.name()).build();
		unit = unitOfMeasureRepository.save(unit);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(unit, "Unit created"));
	}

	@PutMapping("/units/{id}")
	@Operation(summary = "Update unit of measure (#17)")
	public ResponseEntity<ApiResponse<UnitOfMeasure>> updateUnit(@PathVariable Long id, @Valid @RequestBody UnitRequest request) {
		return unitOfMeasureRepository.findById(id)
				.map(unit -> {
					unit.setName(request.name());
					unit = unitOfMeasureRepository.save(unit);
					return ResponseEntity.ok(ApiResponse.success(unit, "Unit updated"));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/units/{id}")
	@Operation(summary = "Delete unit of measure (#17)")
	public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable Long id) {
		if (!unitOfMeasureRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		if (productRepository.existsByUnitId(id)) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error("Единица измерения используется в товарах и не может быть удалена"));
		}
		unitOfMeasureRepository.deleteById(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Unit deleted"));
	}



	@GetMapping("/vat-rates")
	@Operation(summary = "Get all VAT rates")
	public ResponseEntity<ApiResponse<List<VatRate>>> getAllVatRates() {
		List<VatRate> vatRates = vatRateRepository.findAll();
		return ResponseEntity.ok(ApiResponse.success(vatRates, "VAT rates retrieved successfully"));
	}

	@GetMapping("/vat-rates/{id}")
	@Operation(summary = "Get VAT rate by ID")
	public ResponseEntity<ApiResponse<VatRate>> getVatRateById(@PathVariable Long id) {
		return vatRateRepository.findById(id)
				.map(vatRate -> ResponseEntity.ok(ApiResponse.success(vatRate, "VAT rate found")))
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/vat-rates")
	@Operation(summary = "Create VAT rate (#17)")
	public ResponseEntity<ApiResponse<VatRate>> createVatRate(@Valid @RequestBody VatRateRequest request) {
		if (vatRateRepository.existsByDescription(request.description())) {
			return ResponseEntity.badRequest().body(ApiResponse.error("VAT rate with this description already exists"));
		}
		VatRate vatRate = VatRate.builder()
				.ratePercentage(request.ratePercentage())
				.description(request.description())
				.build();
		vatRate = vatRateRepository.save(vatRate);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(vatRate, "VAT rate created"));
	}

	@PutMapping("/vat-rates/{id}")
	@Operation(summary = "Update VAT rate (#17)")
	public ResponseEntity<ApiResponse<VatRate>> updateVatRate(@PathVariable Long id, @Valid @RequestBody VatRateRequest request) {
		return vatRateRepository.findById(id)
				.map(vatRate -> {
					vatRate.setRatePercentage(request.ratePercentage());
					vatRate.setDescription(request.description());
					vatRate = vatRateRepository.save(vatRate);
					return ResponseEntity.ok(ApiResponse.success(vatRate, "VAT rate updated"));
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/vat-rates/{id}")
	@Operation(summary = "Delete VAT rate (#17)")
	public ResponseEntity<ApiResponse<Void>> deleteVatRate(@PathVariable Long id) {
		if (!vatRateRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		if (productRepository.existsByVatRateId(id)) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.error("Ставка НДС используется в товарах и не может быть удалена"));
		}
		vatRateRepository.deleteById(id);
		return ResponseEntity.ok(ApiResponse.success(null, "VAT rate deleted"));
	}
}
