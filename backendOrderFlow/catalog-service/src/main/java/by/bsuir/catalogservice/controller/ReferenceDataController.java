package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.ApiResponse;
import by.bsuir.catalogservice.entity.UnitOfMeasure;
import by.bsuir.catalogservice.entity.VatRate;
import by.bsuir.catalogservice.repository.UnitOfMeasureRepository;
import by.bsuir.catalogservice.repository.VatRateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
}

