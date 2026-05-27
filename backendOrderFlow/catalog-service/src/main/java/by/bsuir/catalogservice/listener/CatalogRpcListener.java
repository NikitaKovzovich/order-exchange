package by.bsuir.catalogservice.listener;

import by.bsuir.catalogservice.config.RabbitMQConfig;
import by.bsuir.catalogservice.entity.Partnership;
import by.bsuir.catalogservice.repository.PartnershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogRpcListener {

	private final PartnershipRepository partnershipRepository;

	@RabbitListener(queues = RabbitMQConfig.RPC_GET_PARTNERSHIP)
	public Map<String, Object> handleGetPartnership(Map<String, Object> request) {
		try {
			Long supplierId = toLong(request.get("supplierId"));
			Long customerId = toLong(request.get("customerId"));

			Optional<Partnership> opt = partnershipRepository.findBySupplierIdAndCustomerId(supplierId, customerId);
			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			if (opt.isPresent()) {
				Partnership p = opt.get();
				result.put("found", true);
				result.put("status", p.getStatus());
				if (p.getContractNumber() != null) result.put("contractNumber", p.getContractNumber());
				if (p.getContractDate() != null) result.put("contractDate", p.getContractDate().toString());
				if (p.getContractEndDate() != null) result.put("contractEndDate", p.getContractEndDate().toString());
			} else {
				result.put("found", false);
			}
			return result;
		} catch (Exception e) {
			log.warn("RPC: getPartnership failed: {}", e.getMessage());
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(value.toString());
	}
}
