package com.eatcloud.adminservice.domain.globalCategory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "p_categories")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class StoreCategory extends BaseCategory {
}
