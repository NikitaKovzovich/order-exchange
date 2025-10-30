package by.bsuir.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://auth-service"))
                .route("address-service", r -> r
                        .path("/api/addresses/**")
                        .uri("lb://auth-service"))
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .uri("lb://auth-service"))
                .route("verification-service", r -> r
                        .path("/api/verification/**")
                        .uri("lb://auth-service"))
                .route("eureka", r -> r
                        .path("/eureka/**")
                        .uri("http://localhost:8761"))
                .build();
    }
}

