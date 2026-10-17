package com.storeconnect.user.service;

import com.storeconnect.user.dto.*;
import com.storeconnect.user.entity.Cart;
import com.storeconnect.user.entity.CartItem;
import com.storeconnect.user.exception.CartItemNotFoundException;
import com.storeconnect.user.exception.InvalidQuantityException;
import com.storeconnect.user.feign.InventoryFeignClient;
import com.storeconnect.user.repository.CartItemRepository;
import com.storeconnect.user.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryFeignClient inventoryFeignClient;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       InventoryFeignClient inventoryFeignClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryFeignClient = inventoryFeignClient;
    }

    @Transactional
    public Cart getOrCreateCartEntity(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .id("CART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                            .customerId(customerId)
                            .items(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional(readOnly = true)
    public CartDTO getCart(String customerId) {
        Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerId);
        if (cartOpt.isEmpty()) {
            return new CartDTO(null, customerId, null, List.of(), BigDecimal.ZERO, 0, LocalDateTime.now());
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalCount = 0;

        for (CartItem item : items) {
            ProductDTO prod = null;
            try {
                prod = inventoryFeignClient.getProductById(item.getProductId());
            } catch (Exception e) {
                log.warn("Could not fetch product details from inventory service for {}: {}", item.getProductId(), e.getMessage());
            }

            String prodName = prod != null ? prod.name() : "Product (" + item.getProductId() + ")";
            String prodSku = prod != null ? prod.sku() : "SKU-N/A";
            BigDecimal unitPrice = prod != null && prod.price() != null ? prod.price() : BigDecimal.ZERO;
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            itemDTOs.add(new CartItemDTO(
                    item.getId(),
                    item.getProductId(),
                    prodName,
                    prodSku,
                    unitPrice,
                    item.getQuantity(),
                    subtotal
            ));

            total = total.add(subtotal);
            totalCount += item.getQuantity();
        }

        return new CartDTO(
                cart.getId(),
                cart.getCustomerId(),
                cart.getStoreId(),
                itemDTOs,
                total,
                totalCount,
                cart.getUpdatedAt()
        );
    }

    @Transactional
    public CartDTO addToCart(String customerId, AddToCartRequest request) {
        if (request.quantity() <= 0) {
            throw new InvalidQuantityException(request.quantity());
        }

        Cart cart = getOrCreateCartEntity(customerId);
        if (request.storeId() != null && !request.storeId().isBlank()) {
            cart.setStoreId(request.storeId().trim());
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId());
        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            existing.setQuantity(existing.getQuantity() + request.quantity());
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = CartItem.builder()
                    .id("ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .cart(cart)
                    .productId(request.productId().trim())
                    .quantity(request.quantity())
                    .createdAt(LocalDateTime.now())
                    .build();
            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Added product {} (qty: {}) to cart for customer {}", request.productId(), request.quantity(), customerId);
        return getCart(customerId);
    }

    @Transactional
    public CartDTO updateItemQuantity(String customerId, String itemId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }

        Cart cart = getOrCreateCartEntity(customerId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new CartItemNotFoundException(itemId);
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCart(customerId);
    }

    @Transactional
    public CartDTO removeItem(String customerId, String itemId) {
        Cart cart = getOrCreateCartEntity(customerId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new CartItemNotFoundException(itemId);
        }

        cartItemRepository.delete(item);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Removed cart item {} for customer {}", itemId, customerId);
        return getCart(customerId);
    }

    @Transactional
    public void clearCart(String customerId) {
        cartRepository.findByCustomerId(customerId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            log.info("Cleared cart for customer {}", customerId);
        });
    }
}
