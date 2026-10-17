package com.storeconnect.user.controller;

import com.storeconnect.user.dto.CustomerDTO;
import com.storeconnect.user.dto.CustomerUpdateRequest;
import com.storeconnect.user.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Customer Profile", description = "Current authenticated customer profile management")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Get current authenticated customer profile")
    @GetMapping("/me")
    public ResponseEntity<CustomerDTO> getCurrentCustomer(
            @RequestHeader(value = "X-User-Id", required = true) String customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @Operation(summary = "Update current customer profile")
    @PutMapping("/me")
    public ResponseEntity<CustomerDTO> updateCurrentCustomer(
            @RequestHeader(value = "X-User-Id", required = true) String customerId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }
}
