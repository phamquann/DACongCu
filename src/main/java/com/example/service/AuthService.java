package com.example.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    // Giả lập tài khoản: user: admin, pass: 123456
    public boolean authenticate(String username, String password) {
        return ("admin".equals(username) && "123456".equals(password));
    }
}