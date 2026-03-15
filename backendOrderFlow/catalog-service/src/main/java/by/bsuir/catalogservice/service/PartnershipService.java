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

		return toResponse(partnership);
	}




	@Transactional
	public PartnershipResponse acceptPartnership(Long partnershipId, Long supplierId) {
		Partnership partnership = getPartnershipForSupplier(partnershipId, supplierId);
		partnership.accept();
		partnership = partnershipRepository.save(partnership);

		log.info("Partnership {} accepted by supplier {}", partnershipId, supplierId);
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
