package com.storeconnect.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutesConfig.class);

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info(">>> Initializing Spring Cloud Gateway routes via Java DSL RouteLocator...");

        return builder.routes()
                // 1. User Service routes (Auth, Customer, Cart)
                .route("user-auth", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                .route("user-customers", r -> r
                        .path("/api/customers/**")
                        .uri("lb://user-service"))
                .route("user-cart", r -> r
                        .path("/api/cart/**")
                        .uri("lb://user-service"))

                // 2. Store-Inventory Service routes
                .route("store-inventory-products", r -> r
                        .path("/api/products/**")
                        .uri("lb://store-inventory-service"))
                .route("store-inventory-categories", r -> r
                        .path("/api/categories/**")
                        .uri("lb://store-inventory-service"))
                .route("store-inventory-stores", r -> r
                        .path("/api/stores/**")
                        .uri("lb://store-inventory-service"))
                .route("store-inventory-stock", r -> r
                        .path("/api/inventory/**")
                        .uri("lb://store-inventory-service"))

                // 3. Order Service routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("lb://order-service"))

                // 4. Payment Service routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://payment-service"))
                .build();
    }
}
