package com.eatcloud.storeservice.globalCategory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "p_menu_category")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MenuCategory extends BaseCategory {

} 