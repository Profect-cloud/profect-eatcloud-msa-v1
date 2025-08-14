package com.eatcloud.adminservice.domain.globalCategory.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import profect.eatcloud.global.timeData.BaseTimeEntity;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseCategory extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;
}
