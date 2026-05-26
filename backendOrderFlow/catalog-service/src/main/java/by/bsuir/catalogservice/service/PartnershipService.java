package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.ContractUpdateRequest;
import by.bsuir.catalogservice.dto.PartnershipRequest;
import by.bsuir.catalogservice.dto.PartnershipResponse;
import by.bsuir.catalogservice.dto.SupplierWithPartnershipResponse;
import by.bsuir.catalogservice.entity.Partnership;
import by.bsuir.catalogservice.exception.DuplicateResourceException;
import by.bsuir.catalogservice.exception.InvalidOperationException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.PartnershipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;




@Slf4j
@Service
public class PartnershipService {

	private static final String NOTIFICATION_EXCHANGE = "order.events";
	private static final String NOTIFICATION_ROUTING_KEY = "partnership.notification";
	private static final String RPC_EXCHANGE = "rpc.exchange";
	private static final String RPC_GET_COMPANY_NAME = "rpc.auth.getCompanyName";

	private final PartnershipRepository partnershipRepository;
	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public PartnershipService(PartnershipRepository partnershipRepository,
							@Autowired(required = false) RabbitTemplate rabbitTemplate) {
		this.partnershipRepository = partnershipRepository;
		this.rabbitTemplate = rabbitTemplate;
	}




	@Transactional
	public PartnershipResponse createPartnershipRequest(Long customerId, PartnershipRequest request) {
		if (partnershipRepository.existsBySupplierIdAndCustomerId(request.supplierId(), customerId)) {
			throw new DuplicateResourceException("Partnership", "supplier+customer",
					request.supplierId() + "+" + customerId);
		}

		Partnership partnership = Partnership.builder()
				.supplierId(request.supplierId())
				.customerId(customerId)
				.contractNumber(request.contractNumber())
				.contractDate(request.contractDate())
				.contractEndDate(request.contractEndDate())
				.customerCompanyName(request.customerCompanyName())
				.customerUnp(request.customerUnp())
				.build();

		partnership = partnershipRepository.save(partnership);
		log.info("Partnership request created: supplier={}, customer={}", request.supplierId(), customerId);

		String customerName = request.customerCompanyName() != null && !request.customerCompanyName().isBlank()
				? request.customerCompanyName()
				: resolveCompanyName(customerId, "Торговая сеть");
		publishNotification(request.supplierId(), "PARTNERSHIP_REQUEST", "Запрос на сотрудничество",
				"Торговая сеть \"" + customerName + "\" отправила запрос на сотрудничество и указала данные договора. "
						+ "Пожалуйста, проверьте информацию и подтвердите заявку для начала работы.");

		return toResponse(partnership);
	}




	@Transactional
	public PartnershipResponse acceptPartnership(Long partnershipId, Long supplierId) {
		Partnership partnership = getPartnershipForSupplier(partnershipId, supplierId);
		partnership.accept();
		partnership = partnershipRepository.save(partnership);

		log.info("Partnership {} accepted by supplier {}", partnershipId, supplierId);

		String supplierName = partnership.getSupplierCompanyName() != null && !partnership.getSupplierCompanyName().isBlank()
				? partnership.getSupplierCompanyName()
				: resolveCompanyName(supplierId, "Поставщик");
		publishNotification(partnership.getCustomerId(), "CONTRACT_CONFIRMED", "Подтверждение договора",
				"Поставщик \"" + supplierName + "\" подтвердил ваш запрос на сотрудничество. "
						+ "Теперь вам доступен каталог товаров этого поставщика.");

		return toResponse(partnership);
	}




	@Transactional
	public PartnershipResponse rejectPartnership(Long partnershipId, Long supplierId) {
		Partnership partnership = getPartnershipForSupplier(partnershipId, supplierId);
		partnership.reject();
		partnership = partnershipRepository.save(partnership);

		log.info("Partnership {} rejected by supplier {}", partnershipId, supplierId);
		return toResponse(partnership);
	}




	@Transactional
	public PartnershipResponse updateContract(Long partnershipId, Long supplierId, ContractUpdateRequest request) {
		Partnership partnership = getPartnershipForSupplier(partnershipId, supplierId);
		partnership.updateContract(request.contractNumber(), request.contractDate(), request.contractEndDate());
		partnership = partnershipRepository.save(partnership);

		log.info("Contract updated for partnership {}", partnershipId);
		return toResponse(partnership);
	}




	public List<PartnershipResponse> getSupplierPendingRequests(Long supplierId) {
		return partnershipRepository.findBySupplierIdAndStatus(supplierId, Partnership.PartnershipStatus.PENDING)
				.stream().map(this::toResponse).toList();
	}




	public List<PartnershipResponse> getSupplierActivePartners(Long supplierId) {
		return partnershipRepository.findBySupplierIdAndStatus(supplierId, Partnership.PartnershipStatus.ACTIVE)
				.stream().map(this::toResponse).toList();
	}




	public List<PartnershipResponse> getSupplierPartnerships(Long supplierId) {
		return partnershipRepository.findBySupplierId(supplierId)
				.stream().map(this::toResponse).toList();
	}




	public List<PartnershipResponse> getCustomerPartnerships(Long customerId) {
		return partnershipRepository.findByCustomerId(customerId)
				.stream().map(this::toResponse).toList();
	}




	public List<Long> getActiveSupplierIds(Long customerId) {
		return partnershipRepository.findActiveSupplierIdsByCustomerId(customerId);
	}







	@SuppressWarnings("unchecked")
	public List<SupplierWithPartnershipResponse> getAllSuppliersWithPartnershipStatus(Long customerId, String search) {

		List<Map<String, Object>> suppliers = new ArrayList<>();
		if (rabbitTemplate != null) {
			try {
				Object response = rabbitTemplate.convertSendAndReceive("rpc.exchange",
						"rpc.auth.getAllSupplierCompanies", Map.of("request", "all"));
				if (response instanceof Map<?, ?> map) {
					Boolean success = (Boolean) map.get("success");
					if (Boolean.TRUE.equals(success) && map.get("suppliers") instanceof List<?> list) {
						for (Object item : list) {
							if (item instanceof Map<?, ?> m) {
								suppliers.add((Map<String, Object>) m);
							}
						}
					}
				}
			} catch (Exception e) {
				log.warn("Failed to fetch suppliers from auth-service: {}", e.getMessage());
			}
		}


		List<Partnership> partnerships = partnershipRepository.findByCustomerId(customerId);
		Map<Long, Partnership> partnershipBySupplier = new HashMap<>();
		for (Partnership p : partnerships) {
			partnershipBySupplier.put(p.getSupplierId(), p);
		}


		List<SupplierWithPartnershipResponse> result = new ArrayList<>();
		for (Map<String, Object> supplier : suppliers) {
			Long companyId = toLong(supplier.get("companyId"));
			String name = supplier.get("legalName") != null ? supplier.get("legalName").toString() : "";
			String taxId = supplier.get("taxId") != null ? supplier.get("taxId").toString() : "";
			String phone = supplier.get("contactPhone") != null ? supplier.get("contactPhone").toString() : "";


			if (search != null && !search.isBlank()) {
				String searchLower = search.toLowerCase();
				if (!name.toLowerCase().contains(searchLower) && !taxId.contains(search)) {
					continue;
				}
			}

			Partnership partnership = partnershipBySupplier.get(companyId);
			String status = "NEW";
			Long partnershipId = null;
			if (partnership != null) {
				status = partnership.getStatus().name();
				partnershipId = partnership.getId();
			}

			result.add(new SupplierWithPartnershipResponse(companyId, name, taxId, phone, status, partnershipId));
		}

		return result;
	}

	private void publishNotification(Long recipientId, String type, String title, String message) {
		if (rabbitTemplate == null || recipientId == null) {
			return;
		}
		try {
			Map<String, Object> payload = Map.of(
					"recipientId", recipientId,
					"type", type,
					"title", title,
					"message", message,
					"orderId", 0L,
					"orderNumber", ""
			);
			rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, payload);
			log.debug("Published partnership notification: type={}, recipient={}", type, recipientId);
		} catch (Exception e) {
			log.warn("Failed to publish partnership notification: {}", e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private String resolveCompanyName(Long companyId, String fallback) {
		if (rabbitTemplate == null || companyId == null) {
			return fallback;
		}
		try {
			Object response = rabbitTemplate.convertSendAndReceive(RPC_EXCHANGE, RPC_GET_COMPANY_NAME,
					Map.of("companyId", companyId));
			if (response instanceof Map<?, ?> map && Boolean.TRUE.equals(map.get("success"))) {
				Object legalName = map.get("legalName");
				if (legalName != null && !legalName.toString().isEmpty()) {
					return legalName.toString();
				}
				Object name = map.get("name");
				if (name != null && !name.toString().isEmpty()) {
					return name.toString();
				}
			}
		} catch (Exception e) {
			log.warn("Failed to resolve company name for {}: {}", companyId, e.getMessage());
		}
		return fallback;
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Number n) return n.longValue();
		try {
			return Long.parseLong(value.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}



	private Partnership getPartnershipForSupplier(Long partnershipId, Long supplierId) {
		Partnership partnership = partnershipRepository.findById(partnershipId)
				.orElseThrow(() -> new ResourceNotFoundException("Partnership", "id", partnershipId));
		if (!partnership.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("partnership", "Partnership does not belong to this supplier");
		}
		return partnership;
	}

	private PartnershipResponse toResponse(Partnership p) {
		return new PartnershipResponse(
				p.getId(),
				p.getSupplierId(),
				p.getSupplierCompanyName(),
				p.getCustomerId(),
				p.getCustomerCompanyName(),
				p.getCustomerUnp(),
				p.getStatus().name(),
				p.getContractNumber(),
				p.getContractDate(),
				p.getContractEndDate(),
				p.getCreatedAt(),
				p.getUpdatedAt()
		);
	}
}
