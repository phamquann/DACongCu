package com.example.fooddelivery.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fooddelivery.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final AuthService authService;

    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthService.AuthResponse register(@RequestBody AuthService.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthService.AuthResponse login(@RequestBody AuthService.LoginRequest request) {
        return authService.login(request);
    }
}
