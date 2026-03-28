package project.ecommerce.service;

import project.ecommerce.dto.CouponRequest;
import project.ecommerce.dto.CouponResponse;
import project.ecommerce.entity.Coupon;
import project.ecommerce.entity.enums.DiscountType;
import project.ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponResponse create(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new RuntimeException("Cupom ja existe com esse codigo.");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minimumOrderValue(request.getMinimumOrderValue())
                .maxUsages(request.getMaxUsages())
                .currentUsages(0)
                .expiresAt(request.getExpiresAt())
                .active(true)
                .build();

        return toResponse(couponRepository.save(coupon));
    }

    public List<CouponResponse> findAll() {
        return couponRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CouponResponse> findAllIncludingInactive() {
        return couponRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CouponResponse deactivate(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cupom nao encontrado."));
        coupon.setActive(false);
        return toResponse(couponRepository.save(coupon));
    }

    // Valida e retorna o cupom — usado pelo CartService
    public Coupon validateCoupon(String code, BigDecimal orderTotal) {
        Coupon coupon = couponRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Cupom invalido ou inativo."));

        if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cupom expirado.");
        }

        if (coupon.getMaxUsages() != null &&
                coupon.getCurrentUsages() >= coupon.getMaxUsages()) {
            throw new RuntimeException("Cupom esgotado.");
        }

        if (orderTotal.compareTo(coupon.getMinimumOrderValue()) < 0) {
            throw new RuntimeException("Valor minimo para usar este cupom e R$ "
                    + coupon.getMinimumOrderValue());
        }

        return coupon;
    }

    // Calcula o desconto — usado pelo CartService e OrderService
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null) return BigDecimal.ZERO;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // FIXED — desconto nao pode ser maior que o subtotal
            return coupon.getDiscountValue().min(subtotal);
        }
    }

    // Incrementa uso do cupom — chamado no checkout
    public void incrementUsage(Coupon coupon) {
        coupon.setCurrentUsages(coupon.getCurrentUsages() + 1);
        couponRepository.save(coupon);
    }

    public CouponResponse toResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setDiscountType(coupon.getDiscountType());
        response.setDiscountValue(coupon.getDiscountValue());
        response.setMinimumOrderValue(coupon.getMinimumOrderValue());
        response.setMaxUsages(coupon.getMaxUsages());
        response.setCurrentUsages(coupon.getCurrentUsages());
        response.setExpiresAt(coupon.getExpiresAt());
        response.setActive(coupon.getActive());
        return response;
    }
}
