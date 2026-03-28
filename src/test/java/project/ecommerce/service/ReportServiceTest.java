// test/java/project/ecommerce/service/ReportServiceTest.java
package project.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.ecommerce.dto.report.DashboardSummaryResponse;
import project.ecommerce.dto.report.LowStockResponse;
import project.ecommerce.dto.report.TopProductResponse;
import project.ecommerce.entity.Category;
import project.ecommerce.entity.Product;
import project.ecommerce.entity.enums.OrderStatus;
import project.ecommerce.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CouponRepository couponRepository;
    @InjectMocks private ReportService reportService;

    @Test
    void getDashboardSummary_ShouldReturnCorrectSummary() {
        when(userRepository.count()).thenReturn(10L);
        when(orderRepository.count()).thenReturn(50L);
        when(productRepository.count()).thenReturn(30L);
        when(orderRepository.findTotalRevenue()).thenReturn(new BigDecimal("15000.00"));
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(5L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(2L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(40L);

        DashboardSummaryResponse response = reportService.getDashboardSummary();

        assertNotNull(response);
        assertEquals(10L, response.getTotalUsers());
        assertEquals(50L, response.getTotalOrders());
        assertEquals(30L, response.getTotalProducts());
        assertEquals(new BigDecimal("15000.00"), response.getTotalRevenue());
        assertEquals(5L, response.getPendingOrders());
        assertEquals(2L, response.getCancelledOrders());
        assertEquals(40L, response.getDeliveredOrders());
    }

    @Test
    void getTopProducts_ShouldReturnLimitedList() {
        List<TopProductResponse> topProducts = List.of(
                new TopProductResponse(1L, "Notebook", 50L, new BigDecimal("175000.00")),
                new TopProductResponse(2L, "Mouse", 100L, new BigDecimal("15000.00"))
        );

        when(orderItemRepository.findTopProducts(any())).thenReturn(topProducts);

        List<TopProductResponse> result = reportService.getTopProducts(2);

        assertEquals(2, result.size());
        assertEquals("Notebook", result.get(0).getProductName());
    }

    @Test
    void getLowStockProducts_ShouldReturnProductsBelowThreshold() {
        Category category = Category.builder()
                .id(1L).name("Eletronicos").build();

        Product product = Product.builder()
                .id(1L)
                .name("Notebook")
                .stockQuantity(3)
                .active(true)
                .category(category)
                .build();

        when(productRepository.findLowStock(10)).thenReturn(List.of(product));

        List<LowStockResponse> result = reportService.getLowStockProducts(10);

        assertEquals(1, result.size());
        assertEquals("Notebook", result.get(0).getProductName());
        assertEquals(3, result.get(0).getStockQuantity());
        assertEquals("Eletronicos", result.get(0).getCategoryName());
    }

    @Test
    void getSalesByPeriod_ShouldThrowException_WhenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.getSalesByPeriod(start, end));

        assertEquals("Data inicial nao pode ser maior que a data final.",
                ex.getMessage());
    }

    @Test
    void getRevenueByPeriod_ShouldReturnRevenue() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        when(orderRepository.findRevenueByPeriod(start, end))
                .thenReturn(new BigDecimal("5000.00"));

        BigDecimal revenue = reportService.getRevenueByPeriod(start, end);

        assertEquals(new BigDecimal("5000.00"), revenue);
    }
}