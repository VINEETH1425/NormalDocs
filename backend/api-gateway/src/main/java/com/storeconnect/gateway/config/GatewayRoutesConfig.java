package com.storeconnect.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service (Auth, Cart)
                .route("user-auth", r -> r.path("/api/auth/**").uri("lb://user-service"))
                .route("user-cart", r -> r.path("/api/cart/**").uri("lb://user-service"))
                // Store Inventory Service (Python FastAPI on port 8082)
                .route("inventory-products", r -> r.path("/api/products/**").uri("http://localhost:8082"))
                .route("inventory-categories", r -> r.path("/api/categories/**").uri("http://localhost:8082"))
                .route("inventory-stores", r -> r.path("/api/stores/**").uri("http://localhost:8082"))
                .route("inventory-stock", r -> r.path("/api/inventory/**").uri("http://localhost:8082"))
                // Order Service (Orders)
                .route("order-service", r -> r.path("/api/orders/**").uri("lb://order-service"))
                // Swagger / OpenAPI documentation routes
                .route("user-docs", r -> r.path("/v3/api-docs/user").filters(f -> f.setPath("/v3/api-docs")).uri("lb://user-service"))
                .route("inventory-docs", r -> r.path("/v3/api-docs/inventory").filters(f -> f.setPath("/v3/api-docs")).uri("http://localhost:8082"))
                .route("order-docs", r -> r.path("/v3/api-docs/order").filters(f -> f.setPath("/v3/api-docs")).uri("lb://order-service"))
                .build();
    }
}
