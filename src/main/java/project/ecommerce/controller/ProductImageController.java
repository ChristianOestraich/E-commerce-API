package project.ecommerce.controller;

import project.ecommerce.dto.ProductResponse;
import project.ecommerce.entity.Product;
import project.ecommerce.repository.ProductRepository;
import project.ecommerce.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final FileStorageService fileStorageService;
    private final ProductRepository productRepository;

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> uploadImage(@PathVariable Long id,
                                                       @RequestParam("file") MultipartFile file) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        // Remove imagem antiga se existir
        if (product.getImageUrl() != null) {
            fileStorageService.delete(product.getImageUrl());
        }

        String imageUrl = fileStorageService.save(file);
        product.setImageUrl(imageUrl);
        productRepository.save(product);

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.getActive());
        response.setCategoryName(product.getCategory().getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        if (product.getImageUrl() == null) {
            throw new RuntimeException("Produto não possui imagem.");
        }

        fileStorageService.delete(product.getImageUrl());
        product.setImageUrl(null);
        productRepository.save(product);

        return ResponseEntity.noContent().build();
    }
}