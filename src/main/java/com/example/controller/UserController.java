package com.example.controller;

import com.example.dto.UserRegistrationRequest;
import com.example.dto.UserResponse;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    // ========== POST ==========
    // Đăng ký người dùng mới
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.registerUser(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đăng ký thành công");
        response.put("data", user);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ========== GET ==========
    // Lấy tất cả người dùng
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy danh sách người dùng thành công");
        response.put("total", users.size());
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }

    // Lấy người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy thông tin người dùng thành công");
        response.put("data", user);
        
        return ResponseEntity.ok(response);
    }

    // Lấy người dùng theo Email
    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy thông tin người dùng thành công");
        response.put("data", user);
        
        return ResponseEntity.ok(response);
    }

    // Lấy người dùng theo Số điện thoại
    @GetMapping("/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getUserByPhone(@PathVariable String phone) {
        UserResponse user = userService.getUserByPhone(phone);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy thông tin người dùng thành công");
        response.put("data", user);
        
        return ResponseEntity.ok(response);
    }

    // Tìm kiếm người dùng theo tên
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam String name) {
        List<UserResponse> users = userService.searchUsersByName(name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tìm kiếm người dùng thành công");
        response.put("total", users.size());
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }

    // ========== PUT ==========
    // Cập nhật thông tin người dùng
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.updateUser(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật thông tin người dùng thành công");
        response.put("data", user);
        
        return ResponseEntity.ok(response);
    }

    // ========== DELETE ==========
    // Xóa người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa người dùng thành công");
        
        return ResponseEntity.ok(response);
    }

    // API Health Check
    @GetMapping("/health/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API is running");
        response.put("status", "UP");
        
        return ResponseEntity.ok(response);
    }
}
