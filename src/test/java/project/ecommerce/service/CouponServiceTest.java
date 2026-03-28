package project.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.ecommerce.dto.CouponRequest;
import project.ecommerce.dto.CouponResponse;
import project.ecommerce.entity.Coupon;
import project.ecommerce.entity.enums.DiscountType;
import project.ecommerce.repository.CouponRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private CouponRepository couponRepository;
    @InjectMocks private CouponService couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .id(1L)
                .code("DESCONTO10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .minimumOrderValue(new BigDecimal("50.00"))
                .currentUsages(0)
                .maxUsages(100)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
    }

    @Test
    void create_ShouldReturnCoupon_WhenCodeNotExists() {
        CouponRequest request = new CouponRequest();
        request.setCode("DESCONTO10");
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("10"));
        request.setMinimumOrderValue(new BigDecimal("50.00"));
        request.setMaxUsages(100);
        request.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(couponRepository.existsByCode(anyString())).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        CouponResponse response = couponService.create(request);

        assertNotNull(response);
        assertEquals("DESCONTO10", response.getCode());
        assertEquals(DiscountType.PERCENTAGE, response.getDiscountType());
    }

    @Test
    void create_ShouldThrowException_WhenCodeAlreadyExists() {
        CouponRequest request = new CouponRequest();
        request.setCode("DESCONTO10");

        when(couponRepository.existsByCode(anyString())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> couponService.create(request));

        assertEquals("Cupom ja existe com esse codigo.", ex.getMessage());
    }

    @Test
    void validateCoupon_ShouldReturnCoupon_WhenValid() {
        when(couponRepository.findByCodeAndActiveTrue("DESCONTO10"))
                .thenReturn(Optional.of(coupon));

        Coupon result = couponService.validateCoupon("DESCONTO10",
                new BigDecimal("100.00"));

        assertNotNull(result);
        assertEquals("DESCONTO10", result.getCode());
    }

    @Test
    void validateCoupon_ShouldThrowException_WhenExpired() {
        coupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeAndActiveTrue("DESCONTO10"))
                .thenReturn(Optional.of(coupon));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> couponService.validateCoupon("DESCONTO10",
                        new BigDecimal("100.00")));

        assertEquals("Cupom expirado.", ex.getMessage());
    }

    @Test
    void validateCoupon_ShouldThrowException_WhenBelowMinimum() {
        when(couponRepository.findByCodeAndActiveTrue("DESCONTO10"))
                .thenReturn(Optional.of(coupon));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> couponService.validateCoupon("DESCONTO10",
                        new BigDecimal("30.00")));

        assertTrue(ex.getMessage().contains("Valor minimo"));
    }

    @Test
    void calculateDiscount_ShouldReturnPercentageDiscount() {
        BigDecimal subtotal = new BigDecimal("200.00");
        BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
        assertEquals(new BigDecimal("20.00"), discount);
    }

    @Test
    void calculateDiscount_ShouldReturnFixedDiscount() {
        coupon.setDiscountType(DiscountType.FIXED);
        coupon.setDiscountValue(new BigDecimal("30.00"));

        BigDecimal subtotal = new BigDecimal("200.00");
        BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);

        assertEquals(new BigDecimal("30.00"), discount);
    }

    @Test
    void calculateDiscount_ShouldNotExceedSubtotal_WhenFixedDiscount() {
        coupon.setDiscountType(DiscountType.FIXED);
        coupon.setDiscountValue(new BigDecimal("500.00"));

        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);

        assertEquals(new BigDecimal("100.00"), discount);
    }

    @Test
    void calculateDiscount_ShouldReturnZero_WhenCouponIsNull() {
        BigDecimal discount = couponService.calculateDiscount(null,
                new BigDecimal("200.00"));
        assertEquals(BigDecimal.ZERO, discount);
    }
}