// MenuCategory.java
package com.eatcloud.adminservice.domain.category.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "p_menu_categories")
@SQLRestriction("deleted_at is null")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MenuCategory extends BaseCategory {

    // denormalized: 빠른 필터링용(상위 카테고리 바로 조건 가능)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_category_id", nullable = false)
    private StoreCategory storeCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mid_category_id", nullable = false)
    private MidCategory midCategory;

    /**
     * (선택) 무결성 보조:
     * 저장 전에 midCategory의 상위가 현재 storeCategory와 다르면 맞춰주는 훅
     * 필요 없으면 지워도 됨(서비스/DB 레벨에서 관리 가능)
     */
    @PrePersist @PreUpdate
    private void alignStoreCategory() {
        if (midCategory != null && midCategory.getStoreCategory() != null) {
            this.storeCategory = midCategory.getStoreCategory();
        }
    }
}
