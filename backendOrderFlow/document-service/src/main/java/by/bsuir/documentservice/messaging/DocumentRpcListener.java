package by.bsuir.documentservice.messaging;

import by.bsuir.documentservice.config.RabbitMQConfig;
import by.bsuir.documentservice.dto.*;
import by.bsuir.documentservice.service.PdfGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;






@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentRpcListener {

	private final PdfGenerationService pdfGenerationService;
	private final ObjectMapper objectMapper;

	@RabbitListener(queues = RabbitMQConfig.RPC_GENERATE_INVOICE)
	public Map<String, Object> handleGenerateInvoice(Map<String, Object> request) {
		try {
			log.info("RPC: generateInvoice for orderId={}", request.get("orderId"));
			Long userId = toLong(request.get("userId"));
			InvoiceGenerationRequest invoiceRequest = objectMapper.convertValue(
					request.get("payload"), InvoiceGenerationRequest.class);

			GeneratedDocumentResponse response = pdfGenerationService.generateInvoice(invoiceRequest, userId);
			return Map.of(
					"success", true,
					"id", response.id(),
					"fileKey", response.fileKey() != null ? response.fileKey() : "",
					"documentNumber", response.documentNumber() != null ? response.documentNumber() : ""
			);
		} catch (Exception e) {
			log.error("RPC: generateInvoice failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}

	@RabbitListener(queues = RabbitMQConfig.RPC_GENERATE_TTN)
	public Map<String, Object> handleGenerateTtn(Map<String, Object> request) {
		try {
			log.info("RPC: generateTtn for orderId={}", request.get("orderId"));
			Long userId = toLong(request.get("userId"));
			TtnGenerationRequest ttnRequest = objectMapper.convertValue(
					request.get("payload"), TtnGenerationRequest.class);

			GeneratedDocumentResponse response = pdfGenerationService.generateTTN(ttnRequest, userId);
			return Map.of(
					"success", true,
					"id", response.id(),
					"fileKey", response.fileKey() != null ? response.fileKey() : "",
					"documentNumber", response.documentNumber() != null ? response.documentNumber() : ""
			);
		} catch (Exception e) {
			log.error("RPC: generateTtn failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}

	@RabbitListener(queues = RabbitMQConfig.RPC_GENERATE_DISCREPANCY_ACT)
	public Map<String, Object> handleGenerateDiscrepancyAct(Map<String, Object> request) {
		try {
			log.info("RPC: generateDiscrepancyAct for orderId={}", request.get("orderId"));
			Long userId = toLong(request.get("userId"));
			DiscrepancyActRequest actRequest = objectMapper.convertValue(
					request.get("payload"), DiscrepancyActRequest.class);

			GeneratedDocumentResponse response = pdfGenerationService.generateDiscrepancyAct(actRequest, userId);
			return Map.of(
					"success", true,
					"id", response.id(),
					"fileKey", response.fileKey() != null ? response.fileKey() : "",
					"documentNumber", response.documentNumber() != null ? response.documentNumber() : ""
			);
		} catch (Exception e) {
			log.error("RPC: generateDiscrepancyAct failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}

	private Long toLong(Object value) {
		if (value == null) return 0L;
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(value.toString());
	}
}
