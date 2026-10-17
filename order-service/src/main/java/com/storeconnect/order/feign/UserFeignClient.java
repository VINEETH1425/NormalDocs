package com.storeconnect.order.feign;

import com.storeconnect.order.dto.feign.CartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/internal/cart/{customerId}")
    CartDTO getCart(@PathVariable("customerId") String customerId);

    @DeleteMapping("/internal/cart/{customerId}/clear")
    void clearCart(@PathVariable("customerId") String customerId);
}
