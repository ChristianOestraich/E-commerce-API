package project.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ecommerce.dto.CartItemRequest;
import project.ecommerce.dto.CartResponse;
import project.ecommerce.dto.WishlistItemResponse;
import project.ecommerce.dto.WishlistResponse;
import project.ecommerce.entity.*;
import project.ecommerce.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    // Adiciona produto na wishlist
    @Transactional
    public WishlistResponse addItem(String email, Long productId) {
        User user = getUser(email);
        Product product = getProduct(productId);

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Produto já está na lista de desejos.");
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .addedAt(LocalDateTime.now())
                .build();

        wishlistRepository.save(item);
        return toResponse(user);
    }

    // Remove produto da wishlist
    @Transactional
    public WishlistResponse removeItem(String email, Long productId) {
        User user = getUser(email);
        Product product = getProduct(productId);

        WishlistItem item = wishlistRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new RuntimeException(
                        "Produto não encontrado na lista de desejos."));

        wishlistRepository.delete(item);
        return toResponse(user);
    }

    // Lista todos os itens da wishlist
    public WishlistResponse getWishlist(String email) {
        User user = getUser(email);
        return toResponse(user);
    }

    // Move item da wishlist para o carrinho
    @Transactional
    public CartResponse moveToCart(String email, Long productId, Integer quantity) {
        User user = getUser(email);
        Product product = getProduct(productId);

        if (!product.getActive()) {
            throw new RuntimeException("Produto indisponível para compra.");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Estoque insuficiente.");
        }

        // Adiciona ao carrinho
        CartItemRequest cartRequest = new CartItemRequest();
        cartRequest.setProductId(productId);
        cartRequest.setQuantity(quantity);
        CartResponse cartResponse = cartService.addItem(email, cartRequest);

        // Remove da wishlist
        wishlistRepository.findByUserAndProduct(user, product)
                .ifPresent(wishlistRepository::delete);

        return cartResponse;
    }

    // Verifica se produto esta na wishlist do usuario
    public Boolean isInWishlist(String email, Long productId) {
        User user = getUser(email);
        Product product = getProduct(productId);
        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    // Limpa toda a wishlist
    @Transactional
    public void clearWishlist(String email) {
        User user = getUser(email);
        List<WishlistItem> items = wishlistRepository.findByUser(user);
        wishlistRepository.deleteAll(items);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    private Product getProduct(Long productId) {
        return productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
    }

    private WishlistResponse toResponse(User user) {
        List<WishlistItemResponse> items = wishlistRepository.findByUser(user)
                .stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        WishlistResponse response = new WishlistResponse();
        response.setTotalItems((long) items.size());
        response.setItems(items);
        return response;
    }

    private WishlistItemResponse toItemResponse(WishlistItem item) {
        WishlistItemResponse response = new WishlistItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setProductDescription(item.getProduct().getDescription());
        response.setProductPrice(item.getProduct().getPrice());
        response.setImageUrl(item.getProduct().getImageUrl());
        response.setProductActive(item.getProduct().getActive());
        response.setInStock(item.getProduct().getStockQuantity() > 0);
        response.setAddedAt(item.getAddedAt());
        return response;
    }
}