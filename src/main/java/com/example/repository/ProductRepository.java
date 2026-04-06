package com.example.repository;

import com.example.model.Product;
import com.example.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findByIsAvailableTrue();
    List<Product> findByCategoryAndIsAvailableTrue(Category category);
    List<Product> findByNameContainingIgnoreCase(String name);
}
