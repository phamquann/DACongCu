package com.example.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được bỏ trống")
    private String name;

    @NotBlank(message = "Mô tả sản phẩm không được bỏ trống")
    private String description;

    @NotNull(message = "Giá không được bỏ trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng không được bỏ trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;

    @NotNull(message = "Danh mục không được bỏ trống")
    private Long categoryId;

    private String imageUrl;
}
