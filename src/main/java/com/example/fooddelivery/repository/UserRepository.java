package com.example.fooddelivery.repository;

import java.util.Optional;

import com.example.fooddelivery.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, String id);

    boolean existsByPhoneAndIdNot(String phone, String id);
}
