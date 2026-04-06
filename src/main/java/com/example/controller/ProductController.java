package com.example.controller;

import com.example.dto.ProductRequest;
import com.example.dto.ProductResponse;
import com.example.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tạo sản phẩm thành công");
        response.put("data", product);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy danh sách sản phẩm thành công");
        response.put("total", products.size());
        response.put("data", products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableProducts() {
        List<ProductResponse> products = productService.getAvailableProducts();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy danh sách sản phẩm có sẵn thành công");
        response.put("total", products.size());
        response.put("data", products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getProductsByCategory(categoryId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy sản phẩm theo danh mục thành công");
        response.put("total", products.size());
        response.put("data", products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy sản phẩm thành công");
        response.put("data", product);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String name) {
        List<ProductResponse> products = productService.searchProducts(name);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tìm kiếm sản phẩm thành công");
        response.put("total", products.size());
        response.put("data", products);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật sản phẩm thành công");
        response.put("data", product);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa sản phẩm thành công");
        return ResponseEntity.ok(response);
    }
}
