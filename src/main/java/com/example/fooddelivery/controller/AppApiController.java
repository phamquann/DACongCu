package com.example.fooddelivery.controller;

import java.util.List;

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

import com.example.fooddelivery.service.AppDataService;

@RestController
@RequestMapping("/api/v1")
public class AppApiController {

    private final AppDataService appDataService;

    public AppApiController(AppDataService appDataService) {
        this.appDataService = appDataService;
    }

    @GetMapping("/categories")
    public List<AppDataService.CategoryDto> categories() {
        return appDataService.getCategories();
    }

    @GetMapping("/menu/items")
    public List<AppDataService.MenuItemDto> menuItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryCode) {
        return appDataService.getMenuItems(keyword, categoryCode);
    }

    @GetMapping("/carts/{userId}")
    public AppDataService.CartResponse cart(@PathVariable String userId) {
        return appDataService.getCart(userId);
    }

    @PostMapping("/carts/{userId}/items")
    public AppDataService.CartResponse addCartItem(
            @PathVariable String userId,
            @RequestBody AddCartItemRequest request) {
        return appDataService.addCartItem(userId, request.menuItemId(), request.quantity());
    }

    @PatchMapping("/carts/{userId}/items/{cartItemId}")
    public AppDataService.CartResponse updateCartItem(
            @PathVariable String userId,
            @PathVariable String cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        return appDataService.updateCartItem(userId, cartItemId, request.quantity());
    }

    @DeleteMapping("/carts/{userId}/items/{cartItemId}")
    public AppDataService.CartResponse removeCartItem(
            @PathVariable String userId,
            @PathVariable String cartItemId) {
        return appDataService.removeCartItem(userId, cartItemId);
    }

    @PostMapping("/carts/{userId}/apply-voucher")
    public AppDataService.CartResponse applyVoucher(
            @PathVariable String userId,
            @RequestBody ApplyVoucherRequest request) {
        return appDataService.applyVoucher(userId, request.code());
    }

    @GetMapping("/users/{userId}/profile")
    public AppDataService.UserProfileDto profile(@PathVariable String userId) {
        return appDataService.getProfile(userId);
    }

    @PutMapping("/users/{userId}/profile")
    public AppDataService.UserProfileDto updateProfile(
            @PathVariable String userId,
            @RequestBody UpdateProfileRequest request) {
        AppDataService.UserProfileUpdateRequest dto = new AppDataService.UserProfileUpdateRequest(
                request.fullName(),
                request.email(),
                request.phone(),
                request.address(),
                request.dateOfBirth());

        return appDataService.updateProfile(userId, dto);
    }

    @GetMapping("/vouchers")
    public List<AppDataService.VoucherDto> vouchers() {
        return appDataService.getPublicVouchers();
    }

    @PostMapping("/orders/checkout")
    public AppDataService.OrderDto checkout(@RequestBody CheckoutRequest request) {
        AppDataService.CheckoutRequest dto = new AppDataService.CheckoutRequest(
                request.userId(),
                request.deliveryAddress(),
                request.note(),
                request.paymentMethod(),
                request.scheduledAt());

        return appDataService.checkout(dto);
    }

    @GetMapping("/orders")
    public List<AppDataService.OrderDto> orders(@RequestParam String userId) {
        return appDataService.getOrdersByUser(userId);
    }

    @GetMapping("/orders/{orderId}")
    public AppDataService.OrderDto orderById(@PathVariable String orderId) {
        return appDataService.getOrderById(orderId);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public AppDataService.OrderDto cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) CancelOrderRequest ignored) {
        return appDataService.cancelOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/review")
    public AppDataService.OrderDto reviewOrder(
            @PathVariable String orderId,
            @RequestBody ReviewRequest request) {
        return appDataService.reviewOrder(orderId, request.rating(), request.comment());
    }

    @GetMapping("/support/conversations")
    public List<AppDataService.ConversationSummaryDto> conversations(@RequestParam String userId) {
        return appDataService.getConversationSummaries(userId);
    }

    @GetMapping("/support/conversations/{conversationId}")
    public AppDataService.ConversationDetailDto conversationDetail(
            @PathVariable String conversationId,
            @RequestParam String userId) {
        return appDataService.getConversationDetail(userId, conversationId);
    }

    @PostMapping("/support/conversations/{conversationId}/messages")
    public AppDataService.ConversationDetailDto sendMessage(
            @PathVariable String conversationId,
            @RequestParam String userId,
            @RequestBody SendMessageRequest request) {
        return appDataService.sendConversationMessage(userId, conversationId, request.message());
    }

    @GetMapping("/admin/dashboard")
    public AppDataService.AdminDashboardDto adminDashboard(
            @RequestParam(required = false) String keyword) {
        return appDataService.getAdminDashboard(keyword);
    }

    @GetMapping("/admin/reviews")
    public AppDataService.AdminReviewsDto adminReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean responded) {
        return appDataService.getAdminReviews(keyword, rating, responded);
    }

    @PostMapping("/admin/reviews/{orderId}/reply")
    public void adminReplyReview(
            @PathVariable String orderId,
            @RequestBody ReplyReviewRequest request) {
        appDataService.replyToReview(orderId, request.message());
    }

    @GetMapping("/admin/vouchers")
    public List<AppDataService.VoucherDto> adminVouchers() {
        return appDataService.getAdminVouchers();
    }

    @PostMapping("/admin/vouchers")
    public AppDataService.VoucherDto adminCreateVoucher(@RequestBody AdminVoucherRequest request) {
        AppDataService.AdminVoucherRequest dto = toAdminVoucherRequest(request);
        return appDataService.createVoucher(dto);
    }

    @PutMapping("/admin/vouchers/{code}")
    public AppDataService.VoucherDto adminUpdateVoucher(
            @PathVariable String code,
            @RequestBody AdminVoucherRequest request) {
        AppDataService.AdminVoucherRequest dto = toAdminVoucherRequest(request);
        return appDataService.updateVoucher(code, dto);
    }

    @DeleteMapping("/admin/vouchers/{code}")
    public void adminDeleteVoucher(@PathVariable String code) {
        appDataService.deleteVoucher(code);
    }

    private AppDataService.AdminVoucherRequest toAdminVoucherRequest(AdminVoucherRequest request) {
        return new AppDataService.AdminVoucherRequest(
                request.code(),
                request.title(),
                request.description(),
                request.discountType(),
                request.discountValue(),
                request.minimumOrder(),
                request.expiryDate(),
                request.categoryCodes(),
                request.active());
    }

    public record AddCartItemRequest(String menuItemId, Integer quantity) {
    }

    public record UpdateCartItemRequest(Integer quantity) {
    }

    public record ApplyVoucherRequest(String code) {
    }

    public record UpdateProfileRequest(
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth) {
    }

    public record CheckoutRequest(
            String userId,
            String deliveryAddress,
            String note,
            String paymentMethod,
            String scheduledAt) {
    }

    public record CancelOrderRequest(String reason) {
    }

    public record ReviewRequest(Integer rating, String comment) {
    }

    public record SendMessageRequest(String message) {
    }

    public record ReplyReviewRequest(String message) {
    }

    public record AdminVoucherRequest(
            String code,
            String title,
            String description,
            String discountType,
            Number discountValue,
            Number minimumOrder,
            String expiryDate,
            List<String> categoryCodes,
            Boolean active) {
    }
}
