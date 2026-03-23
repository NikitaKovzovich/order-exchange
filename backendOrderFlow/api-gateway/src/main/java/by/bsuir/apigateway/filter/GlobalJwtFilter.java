package by.bsuir.apigateway.filter;

import by.bsuir.apigateway.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GlobalJwtFilter implements GlobalFilter, Ordered {
    private final JwtProvider jwtProvider;

    @Value("${gateway.auth.internal-secret:}")
    private String gatewayInternalSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtProvider.validateToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String email = jwtProvider.getEmailFromToken(token);
            String role = jwtProvider.getRoleFromToken(token);
            Long userId = jwtProvider.getUserIdFromToken(token);
            Long companyId = jwtProvider.getCompanyIdFromToken(token);

            var requestBuilder = exchange.getRequest().mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role);

            if (gatewayInternalSecret != null && !gatewayInternalSecret.isBlank()) {
                requestBuilder.header("X-Gateway-Auth", gatewayInternalSecret);
            }

            if (userId != null) {
                requestBuilder.header("X-User-Id", userId.toString());
            }
            if (companyId != null) {
                requestBuilder.header("X-User-Company-Id", companyId.toString());
            }

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(requestBuilder.build())
                    .build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        if (path == null) return false;
        String p = path;
        // normalize trailing slash
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p.startsWith("/api/auth/login") ||
               p.startsWith("/api/auth/register") ||
               p.startsWith("/api/auth/validate") ||
               p.startsWith("/api/auth/company") ||
               p.startsWith("/api/addresses/company") ||
               p.startsWith("/actuator") ||
               p.startsWith("/eureka") ||
               p.startsWith("/swagger-ui") ||
               p.startsWith("/api-docs") ||
               p.startsWith("/v3/api-docs") ||
               p.contains("/api-docs");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

