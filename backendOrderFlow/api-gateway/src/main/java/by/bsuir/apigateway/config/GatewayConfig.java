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
                // Auth Service Routes
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

                // Catalog Service Routes
                .route("catalog-service-products", r -> r
                        .path("/api/products/**")
                        .uri("lb://catalog-service"))
                .route("catalog-service-categories", r -> r
                        .path("/api/categories/**")
                        .uri("lb://catalog-service"))
                .route("catalog-service-inventory", r -> r
                        .path("/api/inventory/**")
                        .uri("lb://catalog-service"))

                // Order Service Routes
                .route("order-service-orders", r -> r
                        .path("/api/orders/**")
                        .uri("lb://order-service"))
                .route("order-service-cart", r -> r
                        .path("/api/cart/**")
                        .uri("lb://order-service"))

                // Chat Service Routes
                .route("chat-service-chats", r -> r
                        .path("/api/chats/**")
                        .uri("lb://chat-service"))
                .route("chat-service-support", r -> r
                        .path("/api/support/**")
                        .uri("lb://chat-service"))
                .route("chat-service-websocket", r -> r
                        .path("/ws/**")
                        .uri("lb://chat-service"))

                // Document Service Routes
                .route("document-service", r -> r
                        .path("/api/documents/**")
                        .uri("lb://document-service"))
                .route("document-service-invoices", r -> r
                        .path("/api/invoices/**")
                        .uri("lb://document-service"))

                // Eureka Dashboard
                .route("eureka", r -> r
                        .path("/eureka/**")
                        .uri("http://localhost:8761"))
                .build();
    }
}

