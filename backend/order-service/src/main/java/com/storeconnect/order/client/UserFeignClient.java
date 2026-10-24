package com.storeconnect.order.client;

import com.storeconnect.order.dto.CartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/internal/cart/{customerId}")
    CartDTO getCart(@PathVariable("customerId") String customerId);

    @DeleteMapping("/internal/cart/{customerId}")
    void clearCart(@PathVariable("customerId") String customerId);
}
