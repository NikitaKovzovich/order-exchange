package by.bsuir.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AuthServiceClient {

	private static final String RPC_EXCHANGE = "rpc.exchange";
	private static final String RPC_GET_COMPANY_NAME = "rpc.auth.getCompanyName";

	private final RabbitTemplate rabbitTemplate;
	private final Map<Long, String> companyNameCache = new ConcurrentHashMap<>();

	public AuthServiceClient(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@SuppressWarnings("unchecked")
	public String getCompanyName(Long companyId) {
		if (companyId == null) return "Неизвестная компания";

		return companyNameCache.computeIfAbsent(companyId, id -> {
			try {
				Map<String, Object> request = Map.of("companyId", id);
				Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_GET_COMPANY_NAME, request);

				if (response instanceof Map<?, ?> map) {
					Boolean success = (Boolean) map.get("success");
					if (Boolean.TRUE.equals(success)) {
						String legalName = (String) map.get("legalName");
						if (legalName != null && !legalName.isEmpty()) {
							return legalName;
						}
						String name = (String) map.get("name");
						if (name != null && !name.isEmpty()) {
							return name;
						}
					}
				}
			} catch (Exception e) {
				log.warn("Failed to get company name via RabbitMQ RPC for companyId={}: {}", id, e.getMessage());
			}
			return "Компания #" + id;
		});
	}

	public void evictCache(Long companyId) {
		companyNameCache.remove(companyId);
	}

	public void clearCache() {
		companyNameCache.clear();
	}
}
