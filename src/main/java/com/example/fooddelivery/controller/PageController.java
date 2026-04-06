package com.example.fooddelivery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({ "/", "/home" })
    public String home() {
        return "forward:/trang-chu.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/figma-bill/login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/registration.html";
    }

    @GetMapping("/booking")
    public String booking() {
        return "forward:/figma-bill/UserBooking.html";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "forward:/checkout.html";
    }

    @GetMapping("/product")
    public String productDetail() {
        return "forward:/product-detail.html";
    }

    @GetMapping("/orders/history")
    public String orderHistory() {
        return "forward:/order-management.html";
    }

    @GetMapping("/orders/tracking")
    public String tracking() {
        return "forward:/thong-bao.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "forward:/ho-so.html";
    }

    @GetMapping("/vouchers")
    public String vouchers() {
        return "forward:/figma-bill/voucher.html";
    }

    @GetMapping("/chat")
    public String chat() {
        return "forward:/figma-bill/ChatSupport.html";
    }

    @GetMapping("/bill/success")
    public String billSuccess() {
        return "forward:/figma-bill/dat-hang-thanh-cong.html";
    }

    @GetMapping("/bill")
    public String billRoot() {
        return "forward:/figma-bill/dat-hang-thanh-cong.html";
    }

    @GetMapping("/bill/suggestions")
    public String billSuggestions() {
        return "forward:/figma-bill/goi-y-mon-an.html";
    }

    @GetMapping("/bill/invoice")
    public String billInvoice() {
        return "forward:/figma-bill/hoa-don-dien-tu.html";
    }

    @GetMapping("/bill/actions")
    public String billActions() {
        return "forward:/figma-bill/thao-tac-hoa-don.html";
    }

    @GetMapping("/bill/review")
    public String billReview() {
        return "forward:/figma-bill/danh-gia-don-hang.html";
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        return "forward:/figma-bill/AdminDashboard.html";
    }

    @GetMapping("/admin/reviews")
    public String adminReviews() {
        return "forward:/danh-gia.html";
    }

    @GetMapping("/admin/vouchers")
    public String adminVouchers() {
        return "forward:/figma-bill/admin-voucher.html";
    }
}
