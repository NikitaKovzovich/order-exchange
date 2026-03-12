package by.bsuir.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@Service
@Slf4j
public class FileStorageService {

	@Value("${services.document-service.url:http://localhost:8085}")
	private String documentServiceUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	
	public String storeFile(MultipartFile file, String subfolder) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			});
			body.add("folder", subfolder);
			body.add("serviceSource", "auth-service");

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			ResponseEntity<Map> response = restTemplate.exchange(
					documentServiceUrl + "/api/documents/upload",
					HttpMethod.POST,
					requestEntity,
					Map.class
			);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				String objectKey = (String) response.getBody().get("objectKey");
				log.info("File uploaded via document-service: {}", objectKey);
				return objectKey;
			}

			throw new RuntimeException("Document service returned: " + response.getStatusCode());
		} catch (Exception e) {
			log.warn("Document-service unavailable, using placeholder path: {}", e.getMessage());
			String placeholder = subfolder + "/" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
			return placeholder;
		}
	}

	
	public String getPresignedUrl(String objectKey) {
		if (objectKey == null || objectKey.isEmpty()) return null;
		try {
			ResponseEntity<Map> response = restTemplate.getForEntity(
					documentServiceUrl + "/api/documents/url?objectKey=" + objectKey,
					Map.class
			);
			if (response.getBody() != null) {
				return (String) response.getBody().get("url");
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to get presigned URL: {}", e.getMessage());
			return null;
		}
	}

	
	public void deleteFile(String objectKey) {
		if (objectKey == null || objectKey.isEmpty()) return;
		try {
			restTemplate.delete(documentServiceUrl + "/api/documents?objectKey=" + objectKey);
			log.info("Deleted file via document-service: {}", objectKey);
		} catch (Exception e) {
			log.error("Failed to delete file: {}", e.getMessage());
		}
	}
}
