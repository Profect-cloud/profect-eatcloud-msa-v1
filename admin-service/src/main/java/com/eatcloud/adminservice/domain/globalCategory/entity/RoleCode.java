package com.eatcloud.adminservice.domain.globalCategory.entity;

import com.eatcloud.adminservice.global.timeData.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "role_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCode extends BaseTimeEntity {
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