package com.eatcloud.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import com.eatcloud.orderservice.common.BaseTimeEntity;

@Entity
@Table(name = "order_status_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusCode extends BaseTimeEntity {
    @Id
    @Column(name = "code", length = 30)
    private String code;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
