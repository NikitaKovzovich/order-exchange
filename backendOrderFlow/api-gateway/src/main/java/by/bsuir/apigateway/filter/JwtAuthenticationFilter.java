package by.bsuir.apigateway.filter;

import by.bsuir.apigateway.service.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GatewayFilter {
    private final JwtProvider jwtProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isAuthEndpoint(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Authorization header is missing or invalid", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtProvider.validateToken(token)) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }

            String email = jwtProvider.getEmailFromToken(token);
            String role = jwtProvider.getRoleFromToken(token);
            Long userId = jwtProvider.getUserIdFromToken(token);
            Long companyId = jwtProvider.getCompanyIdFromToken(token);

            ServerHttpRequest.Builder requestBuilder = request.mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role);

            if (userId != null) {
                requestBuilder.header("X-User-Id", userId.toString());
            }
            if (companyId != null) {
                requestBuilder.header("X-User-Company-Id", companyId.toString());
            }

            ServerHttpRequest mutatedRequest = requestBuilder.build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isAuthEndpoint(ServerHttpRequest request) {
        String path = request.getPath().value();
        return path.startsWith("/api/auth/");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(err.getBytes()))
        );
    }
}

