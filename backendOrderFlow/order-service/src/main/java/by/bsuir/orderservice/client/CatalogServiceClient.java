package by.bsuir.orderservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
public class CatalogServiceClient {

	private static final String RPC_EXCHANGE = "rpc.exchange";
	private static final String RPC_GET_PARTNERSHIP = "rpc.catalog.getPartnership";

	private final RabbitTemplate rabbitTemplate;

	public CatalogServiceClient(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public PartnershipInfo getPartnership(Long supplierId, Long customerId) {
		if (supplierId == null || customerId == null) return null;
		try {
			Map<String, Object> request = Map.of("supplierId", supplierId, "customerId", customerId);
			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_GET_PARTNERSHIP, request);
			if (response instanceof Map<?, ?> map) {
				Boolean success = (Boolean) map.get("success");
				Boolean found = (Boolean) map.get("found");
				if (Boolean.TRUE.equals(success) && Boolean.TRUE.equals(found)) {
					String contractNumber = (String) map.get("contractNumber");
					LocalDate contractDate = parseDate((String) map.get("contractDate"));
					LocalDate contractEndDate = parseDate((String) map.get("contractEndDate"));
					return new PartnershipInfo(contractNumber, contractDate, contractEndDate);
				}
			}
		} catch (Exception e) {
			log.warn("Failed to fetch partnership via RPC for supplier={} customer={}: {}", supplierId, customerId, e.getMessage());
		}
		return null;
	}

	private LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) return null;
		try {
			return LocalDate.parse(value);
		} catch (Exception e) {
			return null;
		}
	}

	public record PartnershipInfo(String contractNumber, LocalDate contractDate, LocalDate contractEndDate) {
	}
}
