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

	@Column(name = "manager_name", length = 20, nullable = false)
	private String managerName;

	@Column(name = "manager_email", length = 255, nullable = false)
	private String managerEmail;

	@Column(name = "manager_password", length = 255, nullable = false)
	private String managerPassword;

	@Column(name = "manager_phone_number", length = 18)
	private String managerPhoneNumber;

	@Column(name = "store_name", length = 200, nullable = false)
	private String storeName;

	@Column(name = "store_address", length = 300)
	private String storeAddress;

	@Column(name = "store_phone_number", length = 18)
	private String storePhoneNumber;

	@Column(name = "store_category_id")
	private Integer categoryId;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "status", length = 20, nullable = false)
	private String status;

	@Column(name = "reviewer_admin_id")
	private UUID reviewerAdminId;

	@Column(name = "review_comment", columnDefinition = "TEXT")
	private String reviewComment;
}
