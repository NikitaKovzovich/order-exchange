package by.bsuir.documentservice.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;




@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnProperty(name = "gateway.auth.enabled", havingValue = "true", matchIfMissing = false)
public class GatewayAuthFilter implements Filter {

	private static final Set<String> PUBLIC_PREFIXES = Set.of(
			"/actuator", "/api-docs", "/swagger-ui", "/v3/api-docs"
	);

	@Value("${gateway.auth.internal-secret:}")
	private String expectedGatewaySecret;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String path = httpRequest.getRequestURI();

		if (isPublicPath(path) || "OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
			chain.doFilter(request, response);
			return;
		}

		String gatewaySecret = httpRequest.getHeader("X-Gateway-Auth");
		if (expectedGatewaySecret != null && !expectedGatewaySecret.isBlank()) {
			if (!expectedGatewaySecret.equals(gatewaySecret)) {
				log.warn("Direct access attempt blocked: {} {} (invalid gateway secret)",
						httpRequest.getMethod(), path);
				httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
				httpResponse.setContentType("application/json");
				httpResponse.getWriter().write(
						"{\"success\":false,\"message\":\"Access denied. Invalid gateway signature.\"}");
				return;
			}
			chain.doFilter(request, response);
			return;
		}

		String userId = httpRequest.getHeader("X-User-Id");
		String userRole = httpRequest.getHeader("X-User-Role");
		String companyId = httpRequest.getHeader("X-User-Company-Id");

		if (userId == null && userRole == null && companyId == null) {
			log.warn("Direct access attempt blocked: {} {} (no gateway headers)",
					httpRequest.getMethod(), path);
			httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
			httpResponse.setContentType("application/json");
			httpResponse.getWriter().write(
					"{\"success\":false,\"message\":\"Access denied. Requests must go through API Gateway.\"}");
			return;
		}

		chain.doFilter(request, response);
	}

	private boolean isPublicPath(String path) {
		return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
	}
}
