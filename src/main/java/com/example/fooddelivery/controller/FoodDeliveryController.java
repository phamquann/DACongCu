package com.example.fooddelivery.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fooddelivery.service.FoodDeliveryService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class FoodDeliveryController {

    private final FoodDeliveryService service;

    public FoodDeliveryController(FoodDeliveryService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public String health() {
        return "food-delivery-backend-ready";
    }

    @PostMapping("/auth/register")
    public FoodDeliveryService.AuthResponse register(@RequestBody FoodDeliveryService.RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/auth/login")
    public FoodDeliveryService.AuthResponse login(@RequestBody FoodDeliveryService.LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/users/{userId}/profile")
    public FoodDeliveryService.UserProfileResponse getProfile(@PathVariable String userId) {
        return service.getProfile(userId);
    }

    @PutMapping("/users/{userId}/profile")
    public FoodDeliveryService.UserProfileResponse updateProfile(
            @PathVariable String userId,
            @RequestBody FoodDeliveryService.UpdateProfileRequest request) {
        return service.updateProfile(userId, request);
    }

    @GetMapping("/users/{userId}/notifications")
    public List<FoodDeliveryService.NotificationResponse> getNotifications(@PathVariable String userId) {
        return service.getNotifications(userId);
    }

    @GetMapping("/categories")
    public List<FoodDeliveryService.CategoryResponse> listCategories() {
        return service.listCategories();
    }

    @GetMapping("/menu/items")
    public List<FoodDeliveryService.MenuItemResponse> listMenuItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryCode) {
        return service.listMenuItems(keyword, categoryCode);
    }

    @GetMapping("/menu/items/{itemId}")
    public FoodDeliveryService.MenuItemResponse getMenuItem(@PathVariable String itemId) {
        return service.getMenuItem(itemId);
    }

    @GetMapping("/vouchers")
    public List<FoodDeliveryService.VoucherResponse> listVouchers(
            @RequestParam(required = false) String categoryCode) {
        return service.listVouchers(categoryCode);
    }

    @PostMapping("/vouchers/validate")
    public FoodDeliveryService.VoucherValidationResponse validateVoucher(
            @RequestBody FoodDeliveryService.VoucherValidationRequest request) {
        return service.validateVoucher(request);
    }

    @GetMapping("/carts/{userId}")
    public FoodDeliveryService.CartResponse getCart(@PathVariable String userId) {
        return service.getCart(userId);
    }

    @PostMapping("/carts/{userId}/items")
    public FoodDeliveryService.CartResponse addCartItem(
            @PathVariable String userId,
            @RequestBody FoodDeliveryService.AddCartItemRequest request) {
        return service.addCartItem(userId, request);
    }

    @PatchMapping("/carts/{userId}/items/{cartItemId}")
    public FoodDeliveryService.CartResponse updateCartItemQuantity(
            @PathVariable String userId,
            @PathVariable String cartItemId,
            @RequestBody FoodDeliveryService.UpdateCartItemQuantityRequest request) {
        return service.updateCartItemQuantity(userId, cartItemId, request);
    }

    @DeleteMapping("/carts/{userId}/items/{cartItemId}")
    public FoodDeliveryService.CartResponse removeCartItem(
            @PathVariable String userId,
            @PathVariable String cartItemId) {
        return service.removeCartItem(userId, cartItemId);
    }

    @PostMapping("/carts/{userId}/apply-voucher")
    public FoodDeliveryService.CartResponse applyVoucher(
            @PathVariable String userId,
            @RequestBody(required = false) FoodDeliveryService.ApplyVoucherRequest request) {
        return service.applyVoucher(userId, request);
    }

    @DeleteMapping("/carts/{userId}/voucher")
    public FoodDeliveryService.CartResponse clearVoucher(@PathVariable String userId) {
        return service.clearVoucher(userId);
    }

    @PostMapping("/orders/checkout")
    public FoodDeliveryService.OrderResponse checkout(@RequestBody FoodDeliveryService.CheckoutRequest request) {
        return service.checkout(request);
    }

    @GetMapping("/orders")
    public List<FoodDeliveryService.OrderResponse> getOrdersByUser(@RequestParam String userId) {
        return service.getOrdersByUser(userId);
    }

    @GetMapping("/orders/{orderId}")
    public FoodDeliveryService.OrderResponse getOrder(@PathVariable String orderId) {
        return service.getOrder(orderId);
    }

    @PatchMapping("/orders/{orderId}/status")
    public FoodDeliveryService.OrderResponse updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody FoodDeliveryService.UpdateOrderStatusRequest request) {
        return service.updateOrderStatus(orderId, request);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public FoodDeliveryService.OrderResponse cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) FoodDeliveryService.CancelOrderRequest request) {
        return service.cancelOrder(orderId, request);
    }

    @PostMapping("/orders/{orderId}/review")
    public FoodDeliveryService.ReviewResponse submitReview(
            @PathVariable String orderId,
            @RequestBody FoodDeliveryService.ReviewRequest request) {
        return service.submitReview(orderId, request);
    }
}
