package project.ecommerce.entity;

import project.ecommerce.entity.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    // Valor minimo do pedido para usar o cupom
    @Column(nullable = false)
    private BigDecimal minimumOrderValue;

    // Limite de usos (null = ilimitado)
    private Integer maxUsages;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentUsages = 0;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
