// com.eatcloud.storeservice.internal.dto.CloseStoreCommand
package com.eatcloud.storeservice.internal.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CloseStoreCommand {
    private String reason; // 선택
}
