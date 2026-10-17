package com.storeconnect.order.feign;

import com.storeconnect.order.dto.feign.StockReservationRequest;
import com.storeconnect.order.dto.feign.StockValidateRequest;
import com.storeconnect.order.dto.feign.StockValidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "store-inventory-service")
public interface InventoryFeignClient {

    @PostMapping("/internal/inventory/validate-stock")
    StockValidateResponse validateStock(@RequestBody StockValidateRequest request);

    @PostMapping("/internal/inventory/reserve")
    void reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/internal/inventory/release")
    void releaseStock(@RequestBody StockReservationRequest request);
}
