package by.bsuir.chatservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final RabbitTemplate rabbitTemplate;

	public FileStorageService(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@SuppressWarnings("unchecked")
	public String storeFile(MultipartFile file, String folder, Long ownerId, String ownerType) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		try {
			if (rabbitTemplate == null) {
				throw new IllegalStateException("RabbitTemplate is not configured");
			}

			Map<String, Object> request = new HashMap<>();
			request.put("fileBase64", Base64.getEncoder().encodeToString(file.getBytes()));
			request.put("originalFilename", file.getOriginalFilename());
			request.put("contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream");
			request.put("folder", folder);
			request.put("serviceSource", "chat-service");
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
			log.warn("Failed to upload attachment via document-service RPC, using placeholder key: {}", e.getMessage());
			return folder + "/" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
		}
	}
}
