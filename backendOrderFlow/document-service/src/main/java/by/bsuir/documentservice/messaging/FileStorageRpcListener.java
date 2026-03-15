package by.bsuir.documentservice.messaging;

import by.bsuir.documentservice.config.RabbitMQConfig;
import by.bsuir.documentservice.service.DocumentService;
import by.bsuir.documentservice.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;





@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageRpcListener {

	private final DocumentService documentService;
	private final MinioService minioService;






	@RabbitListener(queues = RabbitMQConfig.RPC_FILE_UPLOAD)
	public Map<String, Object> handleUploadFile(Map<String, Object> request) {
		try {
			String fileBase64 = (String) request.get("fileBase64");
			String originalFilename = (String) request.getOrDefault("originalFilename", "file");
			String contentType = (String) request.getOrDefault("contentType", "application/octet-stream");
			String folder = (String) request.getOrDefault("folder", "general");
			String serviceSource = (String) request.getOrDefault("serviceSource", "unknown");
			Long ownerId = request.get("ownerId") != null ? toLong(request.get("ownerId")) : null;
			String ownerType = (String) request.get("ownerType");

			byte[] fileBytes = Base64.getDecoder().decode(fileBase64);

			ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
					"file", originalFilename, contentType, fileBytes);

			String objectKey = documentService.uploadFileSimple(
					multipartFile, folder, serviceSource, ownerId, ownerType);

			log.info("RPC: uploadFile success, objectKey={}", objectKey);
			return Map.of(
					"success", true,
					"objectKey", objectKey,
					"originalFilename", originalFilename,
					"fileSize", fileBytes.length,
					"contentType", contentType
			);
		} catch (Exception e) {
			log.error("RPC: uploadFile failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}






	@RabbitListener(queues = RabbitMQConfig.RPC_FILE_URL)
	public Map<String, Object> handleGetPresignedUrl(Map<String, Object> request) {
		try {
			String objectKey = (String) request.get("objectKey");
			String url = documentService.getPresignedUrlByKey(objectKey);
			log.debug("RPC: getPresignedUrl for key={}", objectKey);
			return Map.of("success", true, "url", url, "objectKey", objectKey);
		} catch (Exception e) {
			log.error("RPC: getPresignedUrl failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}






	@RabbitListener(queues = RabbitMQConfig.RPC_FILE_DELETE)
	public Map<String, Object> handleDeleteFile(Map<String, Object> request) {
		try {
			String objectKey = (String) request.get("objectKey");
			documentService.deleteByFileKey(objectKey);
			log.info("RPC: deleteFile success, key={}", objectKey);
			return Map.of("success", true);
		} catch (Exception e) {
			log.error("RPC: deleteFile failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(value.toString());
	}
}
