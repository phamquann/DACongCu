package com.example.service;

import com.example.model.Order;
import com.example.model.Order.OrderStatus;
import com.example.model.User;
import com.example.repository.OrderRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public Order createOrder(Long userId, BigDecimal totalAmount, String deliveryAddress, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tìm thấy"));
        
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(deliveryAddress);
        order.setNotes(notes);
        order.setStatus(OrderStatus.PENDING);
        
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tìm thấy"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tìm thấy"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
