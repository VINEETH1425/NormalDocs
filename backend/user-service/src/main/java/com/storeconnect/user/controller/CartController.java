package com.storeconnect.user.controller;

import com.storeconnect.user.dto.AddToCartRequest;
import com.storeconnect.user.dto.CartDTO;
import com.storeconnect.user.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCart(
            @RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(customerId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeCartItem(
            @RequestHeader("X-User-Id") String customerId,
            @PathVariable("itemId") String itemId) {
        return ResponseEntity.ok(cartService.removeItem(customerId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") String customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
