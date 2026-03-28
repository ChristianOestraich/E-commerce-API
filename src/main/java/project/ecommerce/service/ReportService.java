package project.ecommerce.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import project.ecommerce.dto.report.*;
import project.ecommerce.entity.enums.OrderStatus;
import project.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    // Resumo geral do dashboard
    @Cacheable(value = "dashboard")
    public DashboardSummaryResponse getDashboardSummary() {
        Long totalUsers = userRepository.count();
        Long totalOrders = orderRepository.count();
        Long totalProducts = productRepository.count();
        BigDecimal totalRevenue = orderRepository.findTotalRevenue();
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        Long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);

        return new DashboardSummaryResponse(
                totalUsers,
                totalOrders,
                totalProducts,
                totalRevenue,
                pendingOrders,
                cancelledOrders,
                deliveredOrders
        );
    }

    // Produtos mais vendidos
    @Cacheable(value = "topProducts", key = "#limit")
    public List<TopProductResponse> getTopProducts(int limit) {
        return orderItemRepository.findTopProducts(PageRequest.of(0, limit));
    }

    // Vendas por periodo
    public List<SalesByPeriodResponse> getSalesByPeriod(LocalDateTime start,
                                                        LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new RuntimeException("Data inicial nao pode ser maior que a data final.");
        }
        return orderRepository.findSalesByPeriod(start, end);
    }

    // Receita por periodo
    public BigDecimal getRevenueByPeriod(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new RuntimeException("Data inicial nao pode ser maior que a data final.");
        }
        return orderRepository.findRevenueByPeriod(start, end);
    }

    public List<TopCouponResponse> getTopCoupons(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return couponRepository.findTopCoupons(pageable);
    }

    // Produtos com estoque baixo
    @Cacheable(value = "lowStock", key = "#threshold")
    public List<LowStockResponse> getLowStockProducts(int threshold) {
        return productRepository.findLowStock(threshold)
                .stream()
                .map(p -> new LowStockResponse(
                        p.getId(),
                        p.getName(),
                        p.getCategory().getName(),
                        p.getStockQuantity()
                ))
                .collect(Collectors.toList());
    }
}
