package com.storeconnect.user.service;

import com.storeconnect.user.dto.AddToCartRequest;
import com.storeconnect.user.dto.CartDTO;
import com.storeconnect.user.dto.CartItemDTO;
import com.storeconnect.user.entity.Cart;
import com.storeconnect.user.entity.CartItem;
import com.storeconnect.user.repository.CartItemRepository;
import com.storeconnect.user.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public Cart getOrCreateCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart cart = new Cart("CART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), customerId);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartDTO getCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        return toDTO(cart);
    }

    @Transactional
    public CartDTO addToCart(String customerId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(customerId);

        if (request.getStoreId() != null && !request.getStoreId().isBlank()) {
            cart.setStoreId(request.getStoreId());
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem(
                    "ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    cart,
                    request.getProductId(),
                    request.getQuantity()
            );
            cart.getItems().add(newItem);
        }

        Cart saved = cartRepository.save(cart);
        return toDTO(saved);
    }

    @Transactional
    public CartDTO removeItem(String customerId, String itemId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        Cart saved = cartRepository.save(cart);
        return toDTO(saved);
    }

    @Transactional
    public void clearCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public CartDTO toDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalCount = 0;

        for (CartItem item : cart.getItems()) {
            // Note: unitPrice and product details can be fetched or set simply
            BigDecimal price = new BigDecimal("100.00"); // placeholder default, frontend calculates accurately
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            total = total.add(subtotal);
            totalCount += item.getQuantity();

            itemDTOs.add(new CartItemDTO(
                    item.getId(),
                    item.getProductId(),
                    item.getProductId(),
                    "",
                    price,
                    item.getQuantity(),
                    subtotal
            ));
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
}
