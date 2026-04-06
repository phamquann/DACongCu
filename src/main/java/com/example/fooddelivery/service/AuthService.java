package com.example.fooddelivery.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AtomicLong userSequence = new AtomicLong(1000);
    private final Map<String, UserAccount> usersById = new ConcurrentHashMap<>();
    private final Map<String, String> userIdByEmail = new ConcurrentHashMap<>();
    private final Map<String, String> userIdByPhone = new ConcurrentHashMap<>();

    public AuthService() {
        seedDefaultUsers();
    }

    public AuthResponse register(RegisterRequest request) {
        require(request != null, "Body đăng ký không hợp lệ.");

        String fullName = normalizeRequired(request.fullName(), "Vui lòng nhập họ tên.");
        String email = normalizeEmail(request.email());
        String phone = normalizePhone(request.phone());
        String password = normalizeRequired(request.password(), "Vui lòng nhập mật khẩu.");
        String address = trimToNull(request.address());
        String dateOfBirth = normalizeDate(request.dateOfBirth());

        if (userIdByEmail.containsKey(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng.");
        }
        if (userIdByPhone.containsKey(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng.");
        }

        String userId = "USR-" + userSequence.incrementAndGet();
        UserAccount account = new UserAccount(userId, fullName, email, phone, address, dateOfBirth, password);

        usersById.put(userId, account);
        userIdByEmail.put(email, userId);
        userIdByPhone.put(phone, userId);

        return toAuthResponse(account);
    }

    public AuthResponse login(LoginRequest request) {
        require(request != null, "Body đăng nhập không hợp lệ.");

        String credential = normalizeRequired(request.credential(), "Vui lòng nhập email hoặc số điện thoại.");
        String password = normalizeRequired(request.password(), "Vui lòng nhập mật khẩu.");

        String userId = userIdByEmail.get(credential.toLowerCase());
        if (userId == null) {
            userId = userIdByPhone.get(credential);
        }

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu.");
        }

        UserAccount account = usersById.get(userId);
        if (account == null || !account.password().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu.");
        }

        return toAuthResponse(account);
    }

    private void seedDefaultUsers() {
        UserAccount demoUser = new UserAccount(
                "USR-1000",
                "Demo User",
                "user@example.com",
                "0911111111",
                "123 Le Loi, Q1, TP.HCM",
                "2000-01-01",
                "password123");

        UserAccount legacyUser = new UserAccount(
                "USR-1001",
                "Tran Thi Thanh Mai",
                "mai@example.com",
                "0947689615",
                "Duong 5, Thu Duc, Ho Chi Minh",
                "2000-01-01",
                "123456");

        usersById.put(demoUser.id(), demoUser);
        usersById.put(legacyUser.id(), legacyUser);

        userIdByEmail.put(demoUser.email(), demoUser.id());
        userIdByEmail.put(legacyUser.email(), legacyUser.id());

        userIdByPhone.put(demoUser.phone(), demoUser.id());
        userIdByPhone.put(legacyUser.phone(), legacyUser.id());

        userSequence.set(1001);
    }

    private AuthResponse toAuthResponse(UserAccount account) {
        UserProfileResponse user = new UserProfileResponse(
                account.id(),
                account.fullName(),
                account.email(),
                account.phone(),
                account.address(),
                account.dateOfBirth());

        return new AuthResponse("dev-token-" + account.id(), user);
    }

    private String normalizeRequired(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeRequired(email, "Vui lòng nhập email.").toLowerCase();
        if (!normalized.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không hợp lệ.");
        }
        return normalized;
    }

    private String normalizePhone(String phone) {
        String normalized = normalizeRequired(phone, "Vui lòng nhập số điện thoại.");
        if (normalized.length() < 9) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ.");
        }
        return normalized;
    }

    private String normalizeDate(String date) {
        String normalized = trimToNull(date);
        if (normalized == null) {
            return null;
        }

        try {
            LocalDate.parse(normalized);
            return normalized;
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày sinh không hợp lệ (yyyy-MM-dd).");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    public record RegisterRequest(
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth,
            String password) {
    }

    public record LoginRequest(String credential, String password) {
    }

    public record AuthResponse(String token, UserProfileResponse user) {
    }

    public record UserProfileResponse(
            String id,
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth) {
    }

    private record UserAccount(
            String id,
            String fullName,
            String email,
            String phone,
            String address,
            String dateOfBirth,
            String password) {
    }
}
