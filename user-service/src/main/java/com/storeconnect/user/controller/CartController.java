package com.storeconnect.user.controller;

import com.storeconnect.user.dto.AddToCartRequest;
import com.storeconnect.user.dto.CartDTO;
import com.storeconnect.user.dto.UpdateCartItemRequest;
import com.storeconnect.user.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shopping Cart", description = "Shopping cart operations for authenticated customers")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Get current customer's shopping cart")
    @GetMapping
    public ResponseEntity<CartDTO> getCart(
            @RequestHeader(value = "X-User-Id", required = true) String customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @Operation(summary = "Add an item to the shopping cart")
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(
            @RequestHeader(value = "X-User-Id", required = true) String customerId,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(customerId, request));
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            @RequestHeader(value = "X-User-Id", required = true) String customerId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(customerId, itemId, request.quantity()));
    }

    @Operation(summary = "Remove an item from cart")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeCartItem(
            @RequestHeader(value = "X-User-Id", required = true) String customerId,
            @PathVariable String itemId) {
        return ResponseEntity.ok(cartService.removeItem(customerId, itemId));
    }

    @Operation(summary = "Clear the entire shopping cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-User-Id", required = true) String customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
