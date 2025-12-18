package by.bsuir.documentservice.controller;

import by.bsuir.documentservice.dto.*;
import by.bsuir.documentservice.service.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

/**
 * Контроллер для работы со сгенерированными документами (ТТН, Акт о расхождении)
 */
@RestController
@RequestMapping("/api/generated-documents")
@RequiredArgsConstructor
@Tag(name = "Generated Documents", description = "API для генерации и управления документами (ТТН, Акты)")
public class GeneratedDocumentController {

	private final PdfGenerationService pdfGenerationService;

	@PostMapping("/ttn")
	@Operation(summary = "Сгенерировать ТТН", description = "Генерация товарно-транспортной накладной по форме ТТН-1 (РБ)")
	public ResponseEntity<ApiResponse<GeneratedDocumentResponse>> generateTTN(
			@Valid @RequestBody TtnGenerationRequest request,
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId) {

		GeneratedDocumentResponse response = pdfGenerationService.generateTTN(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PostMapping("/discrepancy-act")
	@Operation(summary = "Сгенерировать Акт о расхождении", description = "Генерация акта о расхождении при приемке товара")
	public ResponseEntity<ApiResponse<GeneratedDocumentResponse>> generateDiscrepancyAct(
			@Valid @RequestBody DiscrepancyActRequest request,
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId) {

		GeneratedDocumentResponse response = pdfGenerationService.generateDiscrepancyAct(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Получить информацию о документе")
	public ResponseEntity<ApiResponse<GeneratedDocumentResponse>> getDocument(@PathVariable Long id) {
		GeneratedDocumentResponse response = pdfGenerationService.getDocument(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{id}/download")
	@Operation(summary = "Скачать сгенерированный документ")
	public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long id) {
		GeneratedDocumentResponse doc = pdfGenerationService.getDocument(id);
		InputStream inputStream = pdfGenerationService.downloadGeneratedDocument(id);

		String fileName = doc.templateType().toLowerCase() + "_" + doc.documentNumber() + ".pdf";

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(inputStream));
	}

	@GetMapping("/order/{orderId}")
	@Operation(summary = "Получить все документы по заказу")
	public ResponseEntity<ApiResponse<List<GeneratedDocumentResponse>>> getDocumentsByOrder(
			@PathVariable Long orderId) {
		List<GeneratedDocumentResponse> response = pdfGenerationService.getDocumentsByOrder(orderId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{id}/url")
	@Operation(summary = "Получить ссылку на скачивание")
	public ResponseEntity<ApiResponse<String>> getDownloadUrl(@PathVariable Long id) {
		GeneratedDocumentResponse doc = pdfGenerationService.getDocument(id);
		// Используем сервис для получения presigned URL
		String url = "/api/generated-documents/" + id + "/download";
		return ResponseEntity.ok(ApiResponse.success(url));
	}
}
