// com.eatcloud.storeservice.internal.dto.CreateStoreCommand
package com.eatcloud.storeservice.internal.dto;

import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateStoreCommand {
    private UUID applicationId;     // 멱등키(권장)
    private UUID managerId;         // 필수
    private String storeName;
    private String storeAddress;
    private String storePhoneNumber;
    private UUID categoryId;        // null 허용 가능
    private String description;
}
