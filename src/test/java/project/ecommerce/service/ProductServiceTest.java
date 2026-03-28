package project.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import project.ecommerce.dto.ProductRequest;
import project.ecommerce.dto.ProductResponse;
import project.ecommerce.entity.Category;
import project.ecommerce.entity.Product;
import project.ecommerce.repository.CategoryRepository;
import project.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Eletronicos")
                .active(true)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Notebook")
                .description("Notebook gamer")
                .price(new BigDecimal("3500.00"))
                .stockQuantity(10)
                .active(true)
                .category(category)
                .build();
    }

    @Test
    void create_ShouldReturnProductResponse_WhenDataIsValid() {
        ProductRequest request = new ProductRequest();
        request.setName("Notebook");
        request.setDescription("Notebook gamer");
        request.setPrice(new BigDecimal("3500.00"));
        request.setStockQuantity(10);
        request.setCategoryId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals("Notebook", response.getName());
        assertEquals(new BigDecimal("3500.00"), response.getPrice());
        assertEquals("Eletronicos", response.getCategoryName());
    }

    @Test
    void create_ShouldThrowException_WhenCategoryNotFound() {
        ProductRequest request = new ProductRequest();
        request.setCategoryId(99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.create(request));

        assertEquals("Categoria não encontrada.", ex.getMessage());
    }

    @Test
    void findAll_ShouldReturnPageOfProducts() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByActiveTrue(any())).thenReturn(page);

        Page<ProductResponse> result = productService.findAll(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Notebook", result.getContent().get(0).getName());
    }

    @Test
    void findById_ShouldReturnProduct_WhenExists() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(1L);

        assertNotNull(response);
        assertEquals("Notebook", response.getName());
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.findById(99L));

        assertEquals("Produto não encontrado.", ex.getMessage());
    }

    @Test
    void deactivate_ShouldSetActiveFalse() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        productService.deactivate(1L);

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }
}