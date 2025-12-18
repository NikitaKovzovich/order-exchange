package by.bsuir.apigateway.filter;

import by.bsuir.apigateway.service.JwtProvider;
import lombok.RequiredArgsConstructor;
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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        if ("OPTIONS".equals(method)) {
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Credentials", "true");
            exchange.getResponse().getHeaders().add("Access-Control-Max-Age", "3600");
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }

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

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Email", email)
                            .header("X-User-Role", role)
                            .build())
                    .build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        if (path == null) return false;

        String cleanPath = path.replaceAll("\\?.*", "");

        return cleanPath.equals("/api/auth/login") ||
               cleanPath.equals("/api/auth/register") ||
               cleanPath.equals("/api/auth/validate") ||
               cleanPath.startsWith("/api/auth/company") ||
               cleanPath.startsWith("/api/addresses/company") ||
               cleanPath.startsWith("/actuator") ||
               cleanPath.startsWith("/eureka") ||
               cleanPath.startsWith("/swagger-ui") ||
               cleanPath.startsWith("/api-docs") ||
               cleanPath.startsWith("/v3/api-docs") ||
               cleanPath.contains("/api-docs");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

