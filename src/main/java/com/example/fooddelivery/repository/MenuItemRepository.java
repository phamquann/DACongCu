package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.MenuItemEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, String> {
}
