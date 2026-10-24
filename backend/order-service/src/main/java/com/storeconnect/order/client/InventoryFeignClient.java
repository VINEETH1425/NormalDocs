package com.storeconnect.order.client;

import com.storeconnect.order.dto.StockReservationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "store-inventory-service")
public interface InventoryFeignClient {

    @PostMapping("/internal/inventory/reserve")
    void reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/internal/inventory/release")
    void releaseStock(@RequestBody StockReservationRequest request);
}
