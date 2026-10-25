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
                // Store Inventory Service (Products, Categories, Stores, Stock)
                .route("inventory-products", r -> r.path("/api/products/**").uri("lb://store-inventory-service"))
                .route("inventory-categories", r -> r.path("/api/categories/**").uri("lb://store-inventory-service"))
                .route("inventory-stores", r -> r.path("/api/stores/**").uri("lb://store-inventory-service"))
                .route("inventory-stock", r -> r.path("/api/inventory/**").uri("lb://store-inventory-service"))
                // Order Service (Orders)
                .route("order-service", r -> r.path("/api/orders/**").uri("lb://order-service"))
                .build();
    }
}
