// MidCategory.java
package com.eatcloud.adminservice.domain.category.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "p_mid_categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MidCategory extends BaseCategory {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_category_id", nullable = false)
    private StoreCategory storeCategory;
}
