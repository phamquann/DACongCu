package com.example.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "Họ và tên không được bỏ trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    private String name;

    @NotBlank(message = "Email không được bỏ trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được bỏ trống")
    @Pattern(regexp = "^(0[1-9]|\\+84[1-9])[0-9]{8,9}$", 
            message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Địa chỉ không được bỏ trống")
    @Size(min = 5, max = 255, message = "Địa chỉ phải từ 5 đến 255 ký tự")
    private String address;
}
