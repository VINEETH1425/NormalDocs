package com.storeconnect.user.controller;

import com.storeconnect.user.dto.CartDTO;
import com.storeconnect.user.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/cart")
public class InternalCartController {

    private final CartService cartService;

    public InternalCartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable("customerId") String customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable("customerId") String customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
