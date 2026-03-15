package by.bsuir.authservice.messaging;

import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.repository.UserRepository;
import by.bsuir.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;




@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRpcListener {

	public static final String RPC_GET_COMPANY_NAME = "rpc.auth.getCompanyName";
	public static final String RPC_GET_ALL_SUPPLIERS = "rpc.auth.getAllSupplierCompanies";

	private final AuthService authService;
	private final UserRepository userRepository;






	@RabbitListener(queues = RPC_GET_COMPANY_NAME)
	public Map<String, Object> handleGetCompanyName(Map<String, Object> request) {
		try {
			Long companyId = toLong(request.get("companyId"));
			log.debug("RPC: getCompanyName for companyId={}", companyId);

			Company company = authService.getCompanyById(companyId);

			return Map.of(
					"success", true,
					"name", company.getName() != null ? company.getName() : "",
					"legalName", company.getLegalName() != null ? company.getLegalName() : ""
			);
		} catch (Exception e) {
			log.warn("RPC: getCompanyName failed: {}", e.getMessage());
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}






	@RabbitListener(queues = RPC_GET_ALL_SUPPLIERS)
	public Map<String, Object> handleGetAllSupplierCompanies(Map<String, Object> request) {
		try {
			log.debug("RPC: getAllSupplierCompanies");
			List<User> suppliers = userRepository.findAllActiveSupplierCompanies();

			List<Map<String, Object>> supplierList = suppliers.stream()
					.filter(u -> u.getCompany() != null)
					.map(u -> {
						Company c = u.getCompany();
						Map<String, Object> item = new HashMap<>();
						item.put("companyId", c.getId());
						item.put("legalName", c.getLegalName() != null ? c.getLegalName() : "");
						item.put("name", c.getName() != null ? c.getName() : "");
						item.put("taxId", c.getTaxId() != null ? c.getTaxId() : "");
						item.put("contactPhone", c.getContactPhone() != null ? c.getContactPhone() : "");
						return item;
					})
					.toList();

			return Map.of("success", true, "suppliers", supplierList);
		} catch (Exception e) {
			log.warn("RPC: getAllSupplierCompanies failed: {}", e.getMessage());
			return Map.of("success", false, "error", e.getMessage() != null ? e.getMessage() : "Unknown error");
		}
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(value.toString());
	}
}
