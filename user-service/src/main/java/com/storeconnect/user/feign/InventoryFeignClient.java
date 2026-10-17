package com.storeconnect.user.feign;

import com.storeconnect.user.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "store-inventory-service")
public interface InventoryFeignClient {

    @GetMapping("/internal/inventory/product/{id}")
    ProductDTO getProductById(@PathVariable("id") String id);
}
