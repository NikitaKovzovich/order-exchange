package by.bsuir.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class FileStorageService {

	private static final String RPC_EXCHANGE = "rpc.exchange";
	private static final String RPC_FILE_UPLOAD = "rpc.document.uploadFile";
	private static final String RPC_FILE_URL = "rpc.document.getPresignedUrl";
	private static final String RPC_FILE_DELETE = "rpc.document.deleteFile";

	private final RabbitTemplate rabbitTemplate;

	public FileStorageService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@SuppressWarnings("unchecked")
	public String storeFile(MultipartFile file, String subfolder) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		try {
			byte[] fileBytes = file.getBytes();
			String fileBase64 = Base64.getEncoder().encodeToString(fileBytes);

			Map<String, Object> request = new HashMap<>();
			request.put("fileBase64", fileBase64);
			request.put("originalFilename", file.getOriginalFilename());
			request.put("contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream");
			request.put("folder", subfolder);
			request.put("serviceSource", "auth-service");

			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_FILE_UPLOAD, request);

			if (response instanceof Map<?, ?> map) {
				Boolean success = (Boolean) map.get("success");
				if (Boolean.TRUE.equals(success)) {
					String objectKey = (String) map.get("objectKey");
					log.info("File uploaded via RabbitMQ RPC: {}", objectKey);
					return objectKey;
				} else {
					throw new RuntimeException("Document service RPC returned error: " + map.get("error"));
				}
			}

			throw new RuntimeException("Document service RPC returned unexpected response");
		} catch (Exception e) {
			log.warn("Document-service RPC unavailable, using placeholder path: {}", e.getMessage());
			String placeholder = subfolder + "/" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
			return placeholder;
		}
	}


	@SuppressWarnings("unchecked")
	public String getPresignedUrl(String objectKey) {
		if (objectKey == null || objectKey.isEmpty()) return null;
		try {
			Map<String, Object> request = Map.of("objectKey", objectKey);
			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_FILE_URL, request);

			if (response instanceof Map<?, ?> map) {
				Boolean success = (Boolean) map.get("success");
				if (Boolean.TRUE.equals(success)) {
					return (String) map.get("url");
				}
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to get presigned URL via RabbitMQ RPC: {}", e.getMessage());
			return null;
		}
	}


	@SuppressWarnings("unchecked")
	public void deleteFile(String objectKey) {
		if (objectKey == null || objectKey.isEmpty()) return;
		try {
			Map<String, Object> request = Map.of("objectKey", objectKey);
			rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_FILE_DELETE, request);
			log.info("Deleted file via RabbitMQ RPC: {}", objectKey);
		} catch (Exception e) {
			log.error("Failed to delete file via RabbitMQ RPC: {}", e.getMessage());
		}
	}
}
