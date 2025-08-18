// BaseCategory.java
package com.eatcloud.adminservice.domain.category.entity;

import com.eatcloud.adminservice.global.timeData.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BaseCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Integer id;

    // 상위: 50, 중간/메뉴: 100을 쓰고 싶다면 하위 엔티티에서 length 재정의 가능
    @Column(name = "code", nullable = false, unique = true, length = 100)
    protected String code;

    @Column(name = "name", nullable = false, length = 100)
    protected String name;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    protected Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    protected Boolean isActive = true;

    @Column(name = "total_store_amount", nullable = false)
    @Builder.Default
    protected Integer totalStoreAmount = 0;
}
