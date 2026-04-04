package com.example.service;

import com.example.dto.UserRegistrationRequest;
import com.example.dto.UserResponse;
import com.example.exception.DuplicateException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Chuyển đổi Entity → Response DTO
    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // Chuyển đổi Request DTO → Entity
    private User convertToEntity(UserRegistrationRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        return user;
    }

    // Đăng ký người dùng mới
    public UserResponse registerUser(UserRegistrationRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Email '" + request.getEmail() + "' đã được đăng ký");
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateException("Số điện thoại '" + request.getPhone() + "' đã được đăng ký");
        }

        User user = convertToEntity(request);
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    // Lấy tất cả người dùng
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Lấy người dùng theo ID
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return convertToResponse(user);
    }

    // Lấy người dùng theo Email
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));
        return convertToResponse(user);
    }

    // Lấy người dùng theo Số điện thoại
    public UserResponse getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phone));
        return convertToResponse(user);
    }

    // Tìm kiếm người dùng theo tên
    public List<UserResponse> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Cập nhật thông tin người dùng
    public UserResponse updateUser(Long id, UserRegistrationRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // Kiểm tra email trùng (nếu thay đổi)
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        // Kiểm tra số điện thoại trùng (nếu thay đổi)
        if (!user.getPhone().equals(request.getPhone()) && 
            userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateException("Số điện thoại '" + request.getPhone() + "' đã được sử dụng");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    // Xóa người dùng
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        userRepository.delete(user);
    }
}
