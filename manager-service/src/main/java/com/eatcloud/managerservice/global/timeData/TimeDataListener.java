package com.eatcloud.managerservice.global.timeData;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import com.eatcloud.managerservice.SecurityUtil;

import java.time.LocalDateTime;
import java.util.UUID;

public class TimeDataListener {

	@PrePersist
	public void prePersist(BaseTimeEntity entity) {
		if (entity.getTimeData() == null) {
			String user = SecurityUtil.getCurrentUsername();
			LocalDateTime now = LocalDateTime.now();

			TimeData td = TimeData.builder()
				.pTimeId(
					UUID.randomUUID())
				.createdAt(now)
				.createdBy(user)
				.updatedAt(now)
				.updatedBy(user)
				.build();

			entity.setTimeData(td);
		}
	}

	@PreUpdate
	public void preUpdate(BaseTimeEntity entity) {
		if (entity.getTimeData() != null) {
			String user = SecurityUtil.getCurrentUsername();
			LocalDateTime now = LocalDateTime.now();

			entity.getTimeData().setUpdatedAt(now);
			entity.getTimeData().setUpdatedBy(user);
		}
	}
}