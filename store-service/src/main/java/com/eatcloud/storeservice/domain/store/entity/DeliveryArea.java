package com.eatcloud.storeservice.domain.store.entity;

import com.eatcloud.storeservice.global.timeData.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


import java.util.UUID;

@Entity
@Table(name = "delivery_areas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryArea extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "area_id")
	private UUID areaId;

	@Column(name = "area_name", nullable = false, length = 100)
	private String areaName;
}