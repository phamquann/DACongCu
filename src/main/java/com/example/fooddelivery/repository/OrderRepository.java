package com.example.fooddelivery.repository;

import java.util.List;

import com.example.fooddelivery.entity.OrderEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Override
    @EntityGraph(attributePaths = { "items", "timeline", "review" })
    java.util.Optional<OrderEntity> findById(String id);

    @EntityGraph(attributePaths = { "items", "timeline", "review" })
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
