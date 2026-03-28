package project.ecommerce.service;

import project.ecommerce.dto.CartItemRequest;
import project.ecommerce.dto.CartItemResponse;
import project.ecommerce.dto.CartResponse;
import project.ecommerce.entity.*;
import project.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;

    // Busca ou cria o carrinho do usuário autenticado
    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return toResponse(cart);
    }

    public CartResponse addItem(String email, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findByIdAndActiveTrue(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado ou inativo."));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Quantidade solicitada indisponível em estoque.");
        }

        // Se o produto já está no carrinho, apenas soma a quantidade
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(0)
                        .build());

        int novaQuantidade = item.getQuantity() + request.getQuantity();

        if (product.getStockQuantity() < novaQuantidade) {
            throw new RuntimeException("Quantidade total excede o estoque disponível.");
        }

        item.setQuantity(novaQuantidade);
        cartItemRepository.save(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    public CartResponse updateItem(String email, Long itemId, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado."));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item não pertence ao carrinho do usuário.");
        }

        if (item.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Quantidade solicitada indisponível em estoque.");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    public CartResponse removeItem(String email, Long itemId) {
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado."));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item não pertence ao carrinho do usuário.");
        }

        cartItemRepository.delete(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public CartResponse applyCoupon(String email, String code) {
        Cart cart = getOrCreateCart(email);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Adicione itens ao carrinho antes de aplicar um cupom.");
        }

        BigDecimal subtotal = calculateSubtotal(cart);
        Coupon coupon = couponService.validateCoupon(code, subtotal);

        cart.setCoupon(coupon);
        cartRepository.save(cart);

        return toResponse(cart);
    }

    public CartResponse removeCoupon(String email) {
        Cart cart = getOrCreateCart(email);
        cart.setCoupon(null);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Mapeamento
    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = couponService.calculateDiscount(cart.getCoupon(), subtotal);
        BigDecimal total = subtotal.subtract(discount);

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setItems(itemResponses);
        response.setSubtotal(subtotal);
        response.setDiscount(discount);
        response.setTotal(total);
        response.setCouponCode(cart.getCoupon() != null ? cart.getCoupon().getCode() : null);
        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setImageUrl(item.getProduct().getImageUrl());
        response.setUnitPrice(item.getProduct().getPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(
                item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
        );
        return response;
    }
}
