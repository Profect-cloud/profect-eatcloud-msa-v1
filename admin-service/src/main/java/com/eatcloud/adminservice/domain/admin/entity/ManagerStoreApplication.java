package com.eatcloud.adminservice.domain.admin.entity;

import com.eatcloud.autotime.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;


import java.util.UUID;

@Entity
@Table(name = "p_manager_store_applications")
@SQLRestriction("deleted_at is null")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerStoreApplication extends BaseTimeEntity {

	@Id
	@Column(name = "application_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID applicationId;

	// Manager 신청 정보
	@Column(name = "manager_name", length = 20, nullable = false)
	private String managerName;

	@Column(name = "manager_email", length = 255, nullable = false)
	private String managerEmail;

	@Column(name = "manager_password", length = 255, nullable = false)
	private String managerPassword;

	@Column(name = "manager_phone_number", length = 18)
	private String managerPhoneNumber;

	// Store 신청 정보
	@Column(name = "store_name", length = 200, nullable = false)
	private String storeName;

	@Column(name = "store_address", length = 300)
	private String storeAddress;

	@Column(name = "store_phone_number", length = 18)
	private String storePhoneNumber;

	@Column(name = "store_category_id")   // ★ 컬럼명만 정확히 매핑
	private Integer categoryId;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	// 심사 상태
	@Column(name = "status", length = 20, nullable = false)
	private String status;

	@Column(name = "reviewer_admin_id")
	private UUID reviewerAdminId;

	@Column(name = "review_comment", columnDefinition = "TEXT")
	private String reviewComment;
}
