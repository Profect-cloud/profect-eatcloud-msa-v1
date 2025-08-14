package com.eatcloud.storeservice.domain.store.entity;

import com.eatcloud.storeservice.global.timeData.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_store_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(DailyStoreSalesId.class)
public class DailyStoreSales extends BaseTimeEntity {

    @Id
    @Column(name = "sale_date")
    private LocalDate saleDate;

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
