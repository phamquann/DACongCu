package com.example.repository;

import com.example.model.Order;
import com.example.model.Order.OrderStatus;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
