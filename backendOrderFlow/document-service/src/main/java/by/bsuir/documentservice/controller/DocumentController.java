package by.bsuir.documentservice.controller;

import by.bsuir.documentservice.dto.ApiResponse;
import by.bsuir.documentservice.dto.DocumentResponse;
import by.bsuir.documentservice.dto.UploadDocumentRequest;
import by.bsuir.documentservice.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "API для управления документами")
public class DocumentController {

	private final DocumentService documentService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Загрузить документ")
	public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
			@RequestParam("file") MultipartFile file,
			@RequestParam("documentTypeCode") String documentTypeCode,
			@RequestParam("entityType") String entityType,
			@RequestParam("entityId") Long entityId,
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId) {

		UploadDocumentRequest request = new UploadDocumentRequest(documentTypeCode, entityType, entityId);
		DocumentResponse response = documentService.uploadDocument(file, request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Получить информацию о документе")
	public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(@PathVariable Long id) {
		DocumentResponse response = documentService.getDocument(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/entity/{entityType}/{entityId}")
	@Operation(summary = "Получить документы по сущности")
	public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByEntity(
			@PathVariable String entityType,
			@PathVariable Long entityId) {
		List<DocumentResponse> response = documentService.getDocumentsByEntity(entityType, entityId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{id}/download")
	@Operation(summary = "Скачать документ")
	public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long id) {
		DocumentResponse doc = documentService.getDocument(id);
		InputStream inputStream = documentService.downloadDocument(id);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.fileName() + "\"")
				.contentType(MediaType.parseMediaType(doc.mimeType()))
				.body(new InputStreamResource(inputStream));
	}

	@GetMapping("/{id}/url")
	@Operation(summary = "Получить ссылку на скачивание")
	public ResponseEntity<ApiResponse<String>> getDownloadUrl(@PathVariable Long id) {
		String url = documentService.getDownloadUrl(id);
		return ResponseEntity.ok(ApiResponse.success(url));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Удалить документ")
	public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
		documentService.deleteDocument(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Document deleted"));
	}
}
