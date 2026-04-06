package com.example.controller;

import com.example.model.LoginRequest;
import com.example.model.LoginResponse;
import com.example.service.AuthService;
// import com.example.model.GoogleLoginRequest;
import com.example.model.GoogleIdTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import com.example.model.ForgotPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        boolean authenticated = authService.authenticate(request.getUsername(), request.getPassword());
        if (authenticated) {
            return new LoginResponse(true, "Đăng nhập thành công!");
        } else {
            return new LoginResponse(false, "Sai tài khoản hoặc mật khẩu!");
        }
    }

    // Đăng nhập Google thật
    @PostMapping("/google")
    public LoginResponse googleLogin(@RequestBody GoogleIdTokenRequest request) {
        try {
            System.out.println("[DEBUG] idToken nhận được từ FE: " + request.getIdToken());
            if (request.getIdToken() == null || request.getIdToken().isEmpty()) {
                return new LoginResponse(false, "Không nhận được idToken từ FE!");
            }
            String CLIENT_ID = "80672838908-g0vdba3n0l1ibo5abn00am3baihronjj.apps.googleusercontent.com";
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();
            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                // Ở đây bạn có thể lưu user vào DB nếu muốn
                return new LoginResponse(true, "Đăng nhập Google thành công! Xin chào: " + name + " (" + email + ")");
            } else {
                return new LoginResponse(false, "ID Token không hợp lệ!");
            }
        } catch (Exception e) {
            return new LoginResponse(false, "Lỗi xác thực Google: " + e.getMessage());
        }
    }

    // Quên mật khẩu (giả lập gửi mail)
    @PostMapping("/forgot-password")
    public LoginResponse forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Giả lập: luôn trả về thành công nếu có email
        if (request.getEmail() != null && request.getEmail().contains("@")) {
            return new LoginResponse(true, "Đã gửi hướng dẫn đặt lại mật khẩu tới email!");
        } else {
            return new LoginResponse(false, "Email không hợp lệ!");
        }
    }
}
