package com.example.fooddelivery.repository;

import java.util.Optional;

import com.example.fooddelivery.entity.CategoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, String> {

    Optional<CategoryEntity> findByCodeIgnoreCase(String code);
}
