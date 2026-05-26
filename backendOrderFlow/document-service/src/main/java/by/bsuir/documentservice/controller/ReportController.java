package by.bsuir.documentservice.controller;

import by.bsuir.documentservice.dto.CriticalStockReportRequest;
import by.bsuir.documentservice.dto.ProductHistoryReportRequest;
import by.bsuir.documentservice.dto.SupplierSummaryReportRequest;
import by.bsuir.documentservice.service.ThymeleafPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

@RestController
@RequestMapping("/api/documents/reports")
@RequiredArgsConstructor
@Tag(name = "PDF Reports", description = "Аналитические PDF-отчёты на лету (Thymeleaf → PDF)")
public class ReportController {

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	private final ThymeleafPdfService pdfService;

	@PostMapping("/critical-stock")
	@Operation(summary = "Отчёт по товарным позициям с критическим остатком (Поставщик)")
	public ResponseEntity<byte[]> generateCriticalStockReport(@Valid @RequestBody CriticalStockReportRequest request) {
		List<CriticalStockReportRequest.Item> sorted = request.items().stream()
				.sorted(Comparator.comparingInt(item -> item.currentStock() != null ? item.currentStock() : Integer.MAX_VALUE))
				.toList();

		Map<String, Object> ctx = new HashMap<>();
		ctx.put("supplierName", request.supplierName());
		ctx.put("generatedAt", LocalDateTime.now().format(TIMESTAMP_FORMAT));
		ctx.put("items", sorted);

		byte[] pdf = pdfService.renderToPdf("reports/critical-stock-report", ctx);

		String fileName = URLEncoder.encode("critical-stock-report.pdf", StandardCharsets.UTF_8);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
				.body(pdf);
	}

	@PostMapping("/supplier-summary")
	@Operation(summary = "Аналитическая сводка по взаимодействию с поставщиками (Торговая сеть)")
	public ResponseEntity<byte[]> generateSupplierSummary(@Valid @RequestBody SupplierSummaryReportRequest request) {
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("customerName", request.customerName());
		ctx.put("periodFrom", request.periodFrom());
		ctx.put("periodTo", request.periodTo());
		ctx.put("rows", request.rows());
		ctx.put("totalAmount", request.totalAmount());
		ctx.put("overallAverageCheck", request.overallAverageCheck());
		ctx.put("generatedAt", LocalDateTime.now().format(TIMESTAMP_FORMAT));

		byte[] pdf = pdfService.renderToPdf("reports/supplier-summary-report", ctx);

		String fileName = URLEncoder.encode("supplier-summary-report.pdf", StandardCharsets.UTF_8);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
				.body(pdf);
	}

	@PostMapping("/product-purchase-history")
	@Operation(summary = "История закупок товарной позиции (Торговая сеть)")
	public ResponseEntity<byte[]> generateProductHistory(@Valid @RequestBody ProductHistoryReportRequest request) {
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("productName", request.productName());
		ctx.put("productSku", request.productSku());
		ctx.put("periodFrom", request.periodFrom());
		ctx.put("periodTo", request.periodTo());
		ctx.put("rows", request.rows());
		ctx.put("minPrice", request.minPrice());
		ctx.put("maxPrice", request.maxPrice());
		ctx.put("generatedAt", LocalDateTime.now().format(TIMESTAMP_FORMAT));

		byte[] pdf = pdfService.renderToPdf("reports/product-purchase-history-report", ctx);

		String fileName = URLEncoder.encode("product-purchase-history-report.pdf", StandardCharsets.UTF_8);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
				.body(pdf);
	}
}
