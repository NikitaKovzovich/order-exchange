package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.AcceptanceJournalResponse;
import by.bsuir.orderservice.dto.ApiResponse;
import by.bsuir.orderservice.service.AcceptanceJournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;





@RestController
@RequestMapping("/api/acceptance-journal")
@RequiredArgsConstructor
@Tag(name = "Acceptance Journal", description = "Цифровой журнал приёмки товаров (Торговая сеть)")
public class AcceptanceJournalController {

	private final AcceptanceJournalService journalService;

	@GetMapping
	@Operation(summary = "Получить журнал приёмки с детализацией и агрегацией",
			description = "Фильтры: поставщик, период. Возвращает 2 таблицы + итого.")
	public ResponseEntity<ApiResponse<AcceptanceJournalResponse>> getJournal(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@RequestParam(required = false) @Parameter(description = "Filter by supplier ID") Long supplierId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

		AcceptanceJournalResponse journal = journalService.getJournal(customerId, supplierId, dateFrom, dateTo);
		return ResponseEntity.ok(ApiResponse.success(journal));
	}
}
