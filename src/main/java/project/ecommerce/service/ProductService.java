package project.ecommerce.service;

import project.ecommerce.dto.ProductRequest;
import project.ecommerce.dto.ProductResponse;
import project.ecommerce.entity.Category;
import project.ecommerce.entity.Product;
import project.ecommerce.repository.CategoryRepository;
import project.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.ecommerce.repository.WishlistRepository;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WishlistRepository wishlistRepository;
    private final EmailService emailService;

    public Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable).map(this::toResponse);
    }

    public Page<ProductResponse> search(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable).map(this::toResponse);
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.getActive());
        response.setCategoryName(product.getCategory().getName());
        return response;
    }

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> findAllIncludingInactive(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public ProductResponse findById(Long id) {
        return toResponse(productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado.")));
    }

    public ProductResponse create(ProductRequest request) {
        // metodo existente sem alteracao no corpo
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada."));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .active(true)
                .category(category)
                .build();

        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada."));

        boolean priceDropped = request.getPrice().compareTo(product.getPrice()) < 0;

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        ProductResponse response = toResponse(productRepository.save(product));

        // Notifica usuarios da wishlist se o preco baixou
        if (priceDropped) {
            wishlistRepository.findByProduct(product)
                    .forEach(item -> emailService.sendPriceDropEmail(
                            item.getUser().getEmail(),
                            item.getUser().getName(),
                            product.getName(),
                            request.getPrice()
                    ));
        }

        return response;
    }

    public void deactivate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
        product.setActive(false);
        productRepository.save(product);
    }
}
