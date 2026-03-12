package by.bsuir.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ChatServiceClient {

	@Value("${services.chat-service.url:http://localhost:8084}")
	private String chatServiceUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getRecentTickets(int limit) {
		try {
			ResponseEntity<Map> response = restTemplate.getForEntity(
					chatServiceUrl + "/api/support/tickets/admin/all?page=0&size=" + limit,
					Map.class
			);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				Map<String, Object> body = response.getBody();
				if (body.containsKey("data") && body.get("data") instanceof Map) {
					Map<String, Object> data = (Map<String, Object>) body.get("data");
					if (data.containsKey("content") && data.get("content") instanceof List) {
						return (List<Map<String, Object>>) data.get("content");
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to fetch recent tickets from chat-service: {}", e.getMessage());
		}
		return Collections.emptyList();
	}
}

