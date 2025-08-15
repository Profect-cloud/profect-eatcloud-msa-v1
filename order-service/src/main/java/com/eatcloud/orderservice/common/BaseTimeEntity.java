package com.eatcloud.orderservice.common;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@EntityListeners(TimeDataListener.class)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseTimeEntity {
	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, optional = true)
	@JoinColumn(name = "p_time_id", nullable = false)
	private TimeData timeData;
	@PrePersist
	public void prePersist() {
		if (this.timeData == null) {
			this.timeData = TimeData.builder()
				.pTimeId(UUID.randomUUID())
				.createdAt(LocalDateTime.now())
				.createdBy("system")
				.updatedAt(LocalDateTime.now())
				.updatedBy("system")
				.build();
		}
	}

	@PreUpdate
	public void preUpdate() {
		if (this.timeData != null) {
			this.timeData.setUpdatedAt(LocalDateTime.now());
			this.timeData.setUpdatedBy("system");
		}
	}
}
