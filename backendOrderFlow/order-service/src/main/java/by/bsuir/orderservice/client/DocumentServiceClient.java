package by.bsuir.orderservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DocumentServiceClient {

	private static final String RPC_EXCHANGE = "rpc.exchange";
	private static final String RPC_GENERATE_INVOICE = "rpc.document.generateInvoice";
	private static final String RPC_GENERATE_TTN = "rpc.document.generateTtn";
	private static final String RPC_GENERATE_DISCREPANCY_ACT = "rpc.document.generateDiscrepancyAct";
	private static final String RPC_FILE_UPLOAD = "rpc.document.uploadFile";

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	public DocumentServiceClient(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
	}

	public GeneratedDocResult generateInvoice(InvoiceRequest request, Long userId) {
		return callGenerate(RPC_GENERATE_INVOICE, request, userId, "Invoice");
	}

	public GeneratedDocResult generateTtn(TtnRequest request, Long userId) {
		return callGenerate(RPC_GENERATE_TTN, request, userId, "TTN");
	}

	public GeneratedDocResult generateDiscrepancyAct(DiscrepancyActReq request, Long userId) {
		return callGenerate(RPC_GENERATE_DISCREPANCY_ACT, request, userId, "DiscrepancyAct");
	}

	@SuppressWarnings("unchecked")
	public String uploadFile(MultipartFile file, String folder, String serviceSource, Long ownerId, String ownerType) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		try {
			Map<String, Object> request = new HashMap<>();
			request.put("fileBase64", Base64.getEncoder().encodeToString(file.getBytes()));
			request.put("originalFilename", file.getOriginalFilename());
			request.put("contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream");
			request.put("folder", folder);
			request.put("serviceSource", serviceSource);
			request.put("ownerId", ownerId);
			request.put("ownerType", ownerType);

			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_FILE_UPLOAD, request);
			if (response instanceof Map<?, ?> map) {
				Boolean success = (Boolean) map.get("success");
				if (Boolean.TRUE.equals(success)) {
					return (String) map.get("objectKey");
				}
				throw new RuntimeException("Document service RPC returned error: " + map.get("error"));
			}
			throw new RuntimeException("Document service RPC returned unexpected response");
		} catch (Exception e) {
			log.warn("Failed to upload file via document-service RPC, using placeholder key: {}", e.getMessage());
			return folder + "/" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
		}
	}

	@SuppressWarnings("unchecked")
	private GeneratedDocResult callGenerate(String routingKey, Object request, Long userId, String docType) {
		try {
			Map<String, Object> message = new HashMap<>();
			message.put("userId", userId);
			message.put("orderId", extractOrderId(request));
			message.put("payload", objectMapper.convertValue(request, Map.class));

			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, routingKey, message);

			if (response instanceof Map<?, ?> map) {
				Boolean success = (Boolean) map.get("success");
				if (Boolean.TRUE.equals(success)) {
					Long id = map.get("id") != null ? toLong(map.get("id")) : null;
					String fileKey = map.get("fileKey") != null ? map.get("fileKey").toString() : null;
					String documentNumber = map.get("documentNumber") != null ? map.get("documentNumber").toString() : null;
					return new GeneratedDocResult(id, fileKey, documentNumber);
				} else {
					log.error("RPC {} returned error: {}", docType, map.get("error"));
				}
			}
		} catch (Exception e) {
			log.error("Failed to generate {} via RabbitMQ RPC: {}", docType, e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Object extractOrderId(Object request) {
		try {
			Map<String, Object> map = objectMapper.convertValue(request, Map.class);
			return map.get("orderId");
		} catch (Exception e) {
			return null;
		}
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(value.toString());
	}

	public record GeneratedDocResult(Long id, String fileKey, String documentNumber) {}

	public record InvoiceRequest(
			Long orderId,
			String orderNumber,
			LocalDate documentDate,
			CompanyInfo seller,
			CompanyInfo buyer,
			List<InvoiceItem> items,
			BigDecimal totalWithoutVat,
			BigDecimal totalVat,
			BigDecimal totalWithVat,
			String paymentTerms,
			String bankDetails
	) {}

	public record TtnRequest(
			Long orderId,
			String orderNumber,
			LocalDate documentDate,
			String series,
			CompanyInfo shipper,
			CompanyInfo consignee,
			CompanyInfo payer,
			String loadingPoint,
			String unloadingPoint,
			Object transport,
			List<TtnItem> items,
			BigDecimal totalWithoutVat,
			BigDecimal totalVat,
			BigDecimal totalWithVat,
			BigDecimal totalWeight,
			Integer totalPackages,
			String releaseReason,
			String notes
	) {}

	public record DiscrepancyActReq(
			Long orderId,
			String orderNumber,
			LocalDate actDate,
			CompanyInfo supplier,
			CompanyInfo customer,
			List<DiscrepancyLine> items,
			BigDecimal totalDiscrepancyAmount,
			String notes
	) {}

	public record CompanyInfo(
			String name,
			String taxId,
			String legalAddress,
			String bankDetails,
			String phone,
			String directorName,
			String accountantName
	) {
		public static CompanyInfo of(String name) {
			return new CompanyInfo(name, null, null, null, null, null, null);
		}
	}

	public record InvoiceItem(
			String name,
			String sku,
			String unit,
			int quantity,
			BigDecimal pricePerUnit,
			BigDecimal vatRate,
			BigDecimal totalWithoutVat,
			BigDecimal vatAmount,
			BigDecimal totalWithVat
	) {}

	public record TtnItem(
			String name,
			String unit,
			int quantity,
			BigDecimal pricePerUnit,
			BigDecimal vatRate,
			BigDecimal totalWithoutVat,
			BigDecimal vatAmount,
			BigDecimal totalWithVat,
			BigDecimal weight
	) {}

	public record DiscrepancyLine(
			String productName,
			String productSku,
			int expectedQuantity,
			int actualQuantity,
			int discrepancyQuantity,
			BigDecimal unitPrice,
			BigDecimal discrepancyAmount,
			String reason
	) {}
}
