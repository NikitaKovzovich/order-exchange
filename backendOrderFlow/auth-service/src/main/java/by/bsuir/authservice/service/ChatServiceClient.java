package by.bsuir.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

	@Value("${gateway.auth.internal-secret:${GATEWAY_AUTH_INTERNAL_SECRET:}}")
	private String gatewayInternalSecret;

	private final RestTemplate restTemplate = new RestTemplate();


	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getRecentTickets(int limit) {
		try {
			HttpHeaders headers = new HttpHeaders();
			if (gatewayInternalSecret != null && !gatewayInternalSecret.isBlank()) {
				headers.set("X-Gateway-Auth", gatewayInternalSecret);
			}

			ResponseEntity<Map> rawResponse = restTemplate.exchange(
					chatServiceUrl + "/api/support/tickets/admin?page=0&size=" + limit,
					HttpMethod.GET,
					new HttpEntity<>(headers),
					Map.class
			);
			ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) rawResponse;
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
