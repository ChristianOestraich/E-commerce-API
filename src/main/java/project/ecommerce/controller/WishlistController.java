package project.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.ecommerce.dto.CartResponse;
import project.ecommerce.dto.WishlistResponse;
import project.ecommerce.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // Lista todos os itens da wishlist
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(Authentication auth) {
        return ResponseEntity.ok(wishlistService.getWishlist(auth.getName()));
    }

    // Adiciona produto na wishlist
    @PostMapping("/{productId}")
    public ResponseEntity<WishlistResponse> addItem(Authentication auth,
                                                    @PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.addItem(auth.getName(), productId));
    }

    // Remove produto da wishlist
    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistResponse> removeItem(Authentication auth,
                                                       @PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.removeItem(auth.getName(), productId));
    }

    // Verifica se produto esta na wishlist
    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> isInWishlist(Authentication auth,
                                                @PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isInWishlist(auth.getName(), productId));
    }

    // Move item da wishlist para o carrinho
    @PostMapping("/{productId}/move-to-cart")
    public ResponseEntity<CartResponse> moveToCart(
            Authentication auth,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(
                wishlistService.moveToCart(auth.getName(), productId, quantity));
    }

    // Limpa toda a wishlist
    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(Authentication auth) {
        wishlistService.clearWishlist(auth.getName());
        return ResponseEntity.noContent().build();
    }
}