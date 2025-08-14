package com.eatcloud.adminservice.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(name = "CategoryDto", description = "공통 카테고리 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
	@Schema(description = "카테고리 ID", example = "1")
	private Integer id;

	@Schema(description = "카테고리 코드", example = "Store/Menu/ etc등등")
	private String code;

	@Schema(description = "카테고리 이름", example = "한식/치킨/카카오페이 etc등등")
	private String displayName;

	@Schema(description = "정렬 순서", example = "1")
	private Integer sortOrder;

	@Schema(description = "활성 여부", example = "true")
	@Builder.Default
	private Boolean isActive = true;
}
