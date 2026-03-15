package by.bsuir.orderservice.messaging;

import by.bsuir.orderservice.dto.AnalyticsResponse;
import by.bsuir.orderservice.service.AdvancedAnalyticsService;
import by.bsuir.orderservice.service.AnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;




@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRpcListener {

	public static final String RPC_OVERALL_ANALYTICS = "rpc.order.getOverallAnalytics";
	public static final String RPC_COMPANY_ORDER_STATS = "rpc.order.getCompanyOrderStats";

	private final AnalyticsService analyticsService;
	private final AdvancedAnalyticsService advancedAnalyticsService;
	private final ObjectMapper objectMapper;






	@RabbitListener(queues = RPC_OVERALL_ANALYTICS)
	public Map<String, Object> handleGetOverallAnalytics(Map<String, Object> request) {
		try {
			String period = (String) request.getOrDefault("period", "all");
			log.info("RPC: getOverallAnalytics, period={}", period);

			AnalyticsResponse analytics = analyticsService.getOverallAnalytics(period);

			@SuppressWarnings("unchecked")
			Map<String, Object> data = objectMapper.convertValue(analytics, Map.class);

			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			result.put("data", data);
			return result;
		} catch (Exception e) {
			log.error("RPC: getOverallAnalytics failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}






	@RabbitListener(queues = RPC_COMPANY_ORDER_STATS)
	public Map<String, Object> handleGetCompanyOrderStats(Map<String, Object> request) {
		try {
			Long companyId = toLong(request.get("companyId"));
			String role = (String) request.getOrDefault("role", "SUPPLIER");
			log.info("RPC: getCompanyOrderStats, companyId={}, role={}", companyId, role);

			AnalyticsResponse analytics;
			if ("SUPPLIER".equalsIgnoreCase(role)) {
				analytics = analyticsService.getSupplierAnalytics(companyId);
			} else {
				analytics = analyticsService.getCustomerAnalytics(companyId);
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> data = objectMapper.convertValue(analytics, Map.class);

			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			result.put("data", data);
			return result;
		} catch (Exception e) {
			log.error("RPC: getCompanyOrderStats failed: {}", e.getMessage(), e);
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(value.toString());
	}
}
