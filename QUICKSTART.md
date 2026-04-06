# 🚀 Quick Start Guide - FastFood System

## ⚡ 5 Phút Để Chạy Ứng Dụng

### Bước 1: Chuẩn Bị Database (1 phút)

**Mở MySQL Command Line hoặc MySQL Workbench:**

```sql
-- Chạy file init.sql để tạo database và dữ liệu mẫu
source d:\DACongCu\src\main\resources\init.sql;
```

**Hoặc chạy từ terminal:**
```bash
mysql -u root -p < d:\DACongCu\src\main\resources\init.sql
```

### Bước 2: Cập Nhật Connection String (1 phút)

Chỉnh sửa `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/da_cong_cu?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Bước 3: Build Ứng Dụng (2 phút)

```bash
cd d:\DACongCu
mvn clean install
```

### Bước 4: Chạy Ứng Dụng (1 phút)

```bash
mvn spring-boot:run
```

**Hoặc:**
```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Bước 5: Truy Cập Ứng Dụng

- **🛒 Mua hàng**: http://localhost:8080/
- **📊 Admin**: http://localhost:8080/admin
- **📡 API**: http://localhost:8080/api

---

## 📱 Thử Nghiệm Nhanh

### 1. Xem Danh Sách Sản Phẩm

```bash
curl http://localhost:8080/api/products
```

### 2. Xem Danh Mục

```bash
curl http://localhost:8080/api/categories
```

### 3. Tạo Sản Phẩm Mới

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mì Trộn",
    "description": "Mì trộn chua cay",
    "price": 30000,
    "quantity": 50,
    "categoryId": 4
  }'
```

### 4. Đăng Ký Người Dùng

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nguyễn Văn A",
    "email": "nguyen.van.a@example.com",
    "phone": "0912345678",
    "address": "123 Đường ABC, TP.HCM"
  }'
```

---

## 🎯 Các Tính Năng Sẵn Dùng

### 👥 Khách Hàng (Customer)

✅ Xem danh sách sản phẩm  
✅ Lọc sản phẩm theo danh mục  
✅ Tìm kiếm sản phẩm  
✅ Thêm vào giỏ hàng  
✅ Xem giỏ hàng  
✅ Đặt hàng online  
✅ Theo dõi đơn hàng (khi API được hoàn thiện)

### 👨‍💼 Quản Trị Viên (Admin)

✅ Xem thống kê tổng quan  
✅ Quản lý sản phẩm (Thêm, Sửa, Xóa)  
✅ Quản lý danh mục  
✅ Quản lý đơn hàng  
✅ Quản lý thông báo  
✅ Cài đặt hệ thống

---

## 🗂️ Các File Quan Trọng

| File | Mô Tả |
|------|-------|
| `src/main/resources/templates/index.html` | Trang mua hàng (khách hàng) |
| `src/main/resources/templates/admin-dashboard.html` | Trang quản lý (admin) |
| `src/main/resources/init.sql` | Khởi tạo database |
| `src/main/resources/application.properties` | Cài đặt Spring Boot |
| `src/main/java/com/example/controller/` | API Controllers |
| `src/main/java/com/example/model/` | Database Models |
| `src/main/java/com/example/service/` | Business Logic |

---

## ✅ Checklist Trước Khi Chạy

- [ ] MySQL server đang chạy
- [ ] Java 17+ đã cài đặt
- [ ] Maven đã cài đặt
- [ ] Database `da_cong_cu` đã được tạo
- [ ] Cập nhật connection string trong `application.properties`
- [ ] Không có ứng dụng khác chạy trên port 8080

---

## 🆘 Gặp Lỗi?

### Error: "Access denied for user 'root'@'localhost'"

**Giải pháp:**
- Kiểm tra password MySQL
- Cập nhật đúng password trong `application.properties`

```properties
spring.datasource.password=correct_password
```

### Error: "Connection refused - localhost:3306"

**Giải pháp:**
- Đảm bảo MySQL đang chạy
- Khởi động MySQL:
  ```bash
  mysql -u root -p
  ```

### Error: "Port 8080 is already in use"

**Giải pháp:**
- Đổi port trong `application.properties`:
  ```properties
  server.port=8081
  ```
- Hoặc kill process đang sử dụng port 8080:
  ```bash
  lsof -i :8080
  kill -9 <PID>
  ```

### Error: "Tables don't exist"

**Giải pháp:**
- Chạy lại `init.sql`:
  ```bash
  mysql -u root -p da_cong_cu < src/main/resources/init.sql
  ```

---

## 📊 Tuyên Bố Dữ Liệu Mẫu

Sau khi chạy `init.sql`, bạn sẽ có:

- **5 Danh mục**: Burger, Pizza, Gà Rán, Cơm, Nước Uống
- **12 Sản phẩm mẫu**: Burger Bò, Pizza Hải Sản, Gà Rán, v.v.
- **3 Người dùng mẫu**: Phạm Văn B, Trần Thị C, Lê Văn D
- **3 Thông báo mẫu**: Ưu đãi, Menu mới, Freeship

---

## 🔗 Các URL Quan Trọng

| URL | Mô Tả |
|-----|-------|
| http://localhost:8080/ | Trang chính (mua hàng) |
| http://localhost:8080/admin | Trang quản lý admin |
| http://localhost:8080/api/products | Get all products |
| http://localhost:8080/api/categories | Get all categories |
| http://localhost:8080/api/notifications | Get notifications |

---

## 📚 Tài Liệu Thêm

- Xem `README.md` để có thông tin chi tiết
- API documentation trong file README

---

## 💡 Tips & Tricks

**Để xem SQL logs:**
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Để reset database:**
```sql
DROP DATABASE da_cong_cu;
source src/main/resources/init.sql;
```

**Để test API dễ hơn, dùng Postman:**
- Import các endpoint từ file `postman_collection.json` (nếu có)

---

**Vậy là xong! Ứng dụng của bạn đã sẵn sàng để sử dụng. 🎉**

### Bước tiếp theo:
1. Thêm sản phẩm qua admin dashboard
2. Mua hàng từ trang khách hàng
3. Quản lý đơn hàng từ admin panel
4. Explore các API endpoints

---

**Cần giúp?** Tham khảo README.md hoặc liên hệ admin@fastfood.vn
