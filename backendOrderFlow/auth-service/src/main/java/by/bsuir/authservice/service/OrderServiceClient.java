package by.bsuir.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class OrderServiceClient {

	private static final String RPC_EXCHANGE = "rpc.exchange";
	private static final String RPC_OVERALL_ANALYTICS = "rpc.order.getOverallAnalytics";
	private static final String RPC_COMPANY_ORDER_STATS = "rpc.order.getCompanyOrderStats";

	private final RabbitTemplate rabbitTemplate;

	public OrderServiceClient(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getOverallAnalytics() {
		try {
			Map<String, Object> request = Map.of("period", "all");
			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_OVERALL_ANALYTICS, request);

			if (response instanceof Map<?, ?> map) {
				Boolean success = (Boolean) map.get("success");
				if (Boolean.TRUE.equals(success) && map.get("data") instanceof Map) {
					return (Map<String, Object>) map.get("data");
				}
			}
		} catch (Exception e) {
			log.warn("Failed to fetch analytics from order-service via RabbitMQ RPC: {}", e.getMessage());
		}
		return Collections.emptyMap();
	}


	@SuppressWarnings("unchecked")
	public Map<String, Object> getCompanyOrderStats(Long companyId, String role) {
		try {
			Map<String, Object> request = new HashMap<>();
			request.put("companyId", companyId);
			request.put("role", role);

			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_COMPANY_ORDER_STATS, request);

			if (response instanceof Map<?, ?> map) {
				Boolean success = (Boolean) map.get("success");
				if (Boolean.TRUE.equals(success) && map.get("data") instanceof Map) {
					return (Map<String, Object>) map.get("data");
				}
			}
		} catch (Exception e) {
			log.warn("Failed to fetch company stats from order-service via RabbitMQ RPC for company {}: {}",
					companyId, e.getMessage());
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
