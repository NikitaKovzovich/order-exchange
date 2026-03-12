package by.bsuir.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class OrderServiceClient {

	@Value("${services.order-service.url:http://localhost:8083}")
	private String orderServiceUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getOverallAnalytics() {
		try {
			ResponseEntity<Map> response = restTemplate.getForEntity(
					orderServiceUrl + "/api/analytics",
					Map.class
			);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				Map<String, Object> body = response.getBody();
				if (body.containsKey("data") && body.get("data") instanceof Map) {
					return (Map<String, Object>) body.get("data");
				}
				return body;
			}
		} catch (Exception e) {
			log.warn("Failed to fetch analytics from order-service: {}", e.getMessage());
		}
		return Collections.emptyMap();
	}

	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getCompanyOrderStats(Long companyId, String role) {
		try {
			String endpoint;
			if ("SUPPLIER".equalsIgnoreCase(role)) {
				endpoint = orderServiceUrl + "/api/analytics/supplier?companyId=" + companyId;
			} else {
				endpoint = orderServiceUrl + "/api/analytics/customer?companyId=" + companyId;
			}

			ResponseEntity<Map> response = restTemplate.getForEntity(endpoint, Map.class);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				Map<String, Object> body = response.getBody();
				if (body.containsKey("data") && body.get("data") instanceof Map) {
					return (Map<String, Object>) body.get("data");
				}
				return body;
			}
		} catch (Exception e) {
			log.warn("Failed to fetch company stats from order-service for company {}: {}", companyId, e.getMessage());
		}
		Map<String, Object> fallback = new HashMap<>();
		fallback.put("totalOrders", 0);
		fallback.put("totalRevenue", 0);
		fallback.put("lastOrderDate", null);
		return fallback;
	}

	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSupplierStats(Long companyId) {
		return getCompanyOrderStats(companyId, "SUPPLIER");
	}

	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getRetailChainStats(Long companyId) {
		return getCompanyOrderStats(companyId, "RETAIL_CHAIN");
	}
}

