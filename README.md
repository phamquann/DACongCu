# FastFood - Hệ Thống Đặt Món Ăn Nhanh Online

## 📋 Mô Tả Dự Án
Đây là một ứng dụng web đầy đủ cho phép quản lý và bán hàng thực phẩm nhanh (FastFood) trực tuyến. Hệ thống bao gồm:

- **Frontend (UI)**: Giao diện đặt hàng cho khách hàng + Dashboard quản lý cho admin
- **Backend (API)**: RESTful API built with Spring Boot 4.0.5
- **Database**: MySQL 8.4
- **Features**:
  - Quản lý sản phẩm, danh mục, đơn hàng
  - Hệ thống thông báo
  - Giỏ hàng trực tuyến
  - Đặt hàng online
  - Admin dashboard cho quản lý

## 🛠️ Công Nghệ Sử Dụng

**Backend:**
- Java 17
- Spring Boot 4.0.5
- Spring Data JPA
- MySQL 8.4
- Maven

**Frontend:**
- HTML5
- CSS3
- JavaScript (Vanilla)
- Thymeleaf (Template Engine)

## 📁 Cấu Trúc Dự Án

```
src/main/
├── java/com/example/
│   ├── controller/          # REST Controllers & Home Controller
│   ├── model/              # JPA Entities (User, Product, Order, etc.)
│   ├── repository/         # Spring Data JPA Repositories
│   ├── service/            # Business Logic Services
│   ├── dto/                # Data Transfer Objects
│   ├── exception/          # Exception Handlers
│   ├── config/             # Configuration Classes
│   └── Application.java    # Main Application Class
└── resources/
    ├── templates/          # HTML Templates
    │   ├── index.html      # Customer Shopping Page
    │   └── admin-dashboard.html  # Admin Dashboard
    ├── static/             # Static Files (CSS, JS)
    └── application.properties  # Configuration
```

## 🚀 Cách Chạy Ứng Dụng

### 1. Chuẩn Bị Môi Trường

**Yêu cầu:**
- Java 17+
- MySQL 8.4 (running)
- Maven 3.6+

### 2. Cấu Hình Database

**Tạo Database:**
```sql
CREATE DATABASE da_cong_cu;
USE da_cong_cu;
```

**Cập nhật Connection String** trong `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/da_cong_cu
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Chạy Ứng Dụng

**Sử dụng Maven:**
```bash
cd d:\DACongCu
mvn clean install
mvn spring-boot:run
```

**Hoặc build và run jar:**
```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 4. Truy Cập Ứng Dụng

- **Customer Shopping**: http://localhost:8080/
- **Admin Dashboard**: http://localhost:8080/admin
- **API Documentation**: http://localhost:8080/api

## 📚 API Endpoints

### 🧑 User Endpoints
```
POST   /api/users/register      - Đăng ký người dùng mới
GET    /api/users              - Lấy tất cả người dùng
GET    /api/users/{id}         - Lấy thông tin người dùng
PUT    /api/users/{id}         - Cập nhật thông tin người dùng
DELETE /api/users/{id}         - Xóa người dùng
```

### 🍔 Product Endpoints
```
POST   /api/products           - Tạo sản phẩm mới
GET    /api/products           - Lấy tất cả sản phẩm
GET    /api/products/available - Lấy sản phẩm có sẵn
GET    /api/products/{id}      - Lấy chi tiết sản phẩm
GET    /api/products/category/{categoryId} - Lấy sản phẩm theo danh mục
GET    /api/products/search?name=keyword  - Tìm kiếm sản phẩm
PUT    /api/products/{id}      - Cập nhật sản phẩm
DELETE /api/products/{id}      - Xóa sản phẩm
```

### 📂 Category Endpoints
```
POST   /api/categories         - Tạo danh mục mới
GET    /api/categories         - Lấy tất cả danh mục
GET    /api/categories/{id}    - Lấy chi tiết danh mục
PUT    /api/categories/{id}    - Cập nhật danh mục
DELETE /api/categories/{id}    - Xóa danh mục
```

### 📦 Order Endpoints
```
POST   /api/orders             - Tạo đơn hàng mới
GET    /api/orders             - Lấy tất cả đơn hàng
GET    /api/orders/{id}        - Lấy chi tiết đơn hàng
GET    /api/orders/user/{userId} - Lấy đơn hàng của người dùng
PUT    /api/orders/{id}        - Cập nhật đơn hàng
DELETE /api/orders/{id}        - Xóa đơn hàng
```

### 🔔 Notification Endpoints
```
POST   /api/notifications      - Tạo thông báo
GET    /api/notifications      - Lấy tất cả thông báo
GET    /api/notifications/unread - Lấy thông báo chưa đọc
PUT    /api/notifications/{id}/read - Đánh dấu đã đọc
GET    /api/notifications/unread-count - Lấy số thông báo chưa đọc
DELETE /api/notifications/{id} - Xóa thông báo
```

## 📝 Ví Dụ Request/Response

### Tạo Sản Phẩm Mới

**Request:**
```bash
POST /api/products
Content-Type: application/json

{
  "name": "Burger Bò",
  "description": "Burger thơm ngon với thịt bò tươi",
  "price": 50000,
  "quantity": 25,
  "categoryId": 1,
  "imageUrl": "https://example.com/burger.jpg"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo sản phẩm thành công",
  "data": {
    "id": 1,
    "name": "Burger Bò",
    "description": "Burger thơm ngon với thịt bò tươi",
    "price": 50000,
    "quantity": 25,
    "categoryId": 1,
    "categoryName": "Burger",
    "imageUrl": "https://example.com/burger.jpg",
    "isAvailable": true,
    "createdAt": "2026-04-03T10:30:00",
    "updatedAt": "2026-04-03T10:30:00"
  }
}
```

### Đăng Ký Người Dùng

**Request:**
```bash
POST /api/users/register
Content-Type: application/json

{
  "name": "Phạm Văn B",
  "email": "pham.van.b@example.com",
  "phone": "0912345678",
  "address": "123 Đường Lê Lợi, Q.1, TP.HCM"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "name": "Phạm Văn B",
    "email": "pham.van.b@example.com",
    "phone": "0912345678",
    "address": "123 Đường Lê Lợi, Q.1, TP.HCM",
    "createdAt": "2026-04-03T10:30:00"
  }
}
```

## 🎯 Các Tính Năng Chính

### 👥 Quản Lý Người Dùng
- Đăng ký tài khoản mới
- Xem thông tin cá nhân
- Cập nhật hồ sơ

### 🍕 Quản Lý Sản Phẩm (Admin)
- Thêm/Sửa/Xóa sản phẩm
- Quản lý danh mục sản phẩm
- Kiểm tra tồn kho
- Tìm kiếm sản phẩm

### 🛒 Hệ Thống Giỏ Hàng
- Thêm sản phẩm vào giỏ
- Cập nhật số lượng
- Tính giá tự động (gồm phí giao hàng)
- Lưu giỏ hàng vào localStorage

### 📦 Quản Lý Đơn Hàng
- Tạo đơn hàng mới
- Theo dõi trạng thái đơn hàng
- Quản lý từng trạng thái: Chờ xử lý, Xác nhận, Đang chuẩn bị, Sẵn sàng, Đã giao, Đã hủy

### 🔔 Hệ Thống Thông Báo
- Tạo và gửi thông báo
- Xem thông báo chưa đọc
- Đánh dấu thông báo đã đọc
- Phân loại thông báo: Thông tin, Cảnh báo, Lỗi, Thành công

### 📊 Admin Dashboard
- Tổng quan thống kê (tổng đơn, doanh thu, sản phẩm có sẵn, tổng khách)
- Quản lý đơn hàng
- Quản lý sản phẩm
- Quản lý danh mục
- Quản lý thông báo
- Cài đặt hệ thống

## 🔐 Bảo Mật

Current features:
- CORS enabled cho development
- Validation trên request parameters
- Hibernate validation annotations

Recommended untuk production:
- Thêm Spring Security authentication
- Implement JWT tokens
- Mã hóa password
- HTTPS
- Rate limiting

## 🧪 Testing

**Cách kiểm tra API:**

Sử dụng Postman hoặc curl:

```bash
# Get all products
curl http://localhost:8080/api/products

# Create a category
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Burger","description":"Các loại burger"}'

# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Burger Bò",
    "description":"Burger ngon",
    "price":50000,
    "quantity":25,
    "categoryId":1
  }'
```

## 📱 Responsive Design

- ✅ Desktop (1200px+)
- ✅ Tablet (768px - 1199px)
- ✅ Mobile (320px - 767px)

## 🐛 Troubleshooting

**Problem:** Connection refused to MySQL
- **Solution:** Chạy MySQL server: `mysql -u root -p`

**Problem:** Port 8080 already in use
- **Solution:** Đổi port trong `application.properties`: `server.port=8081`

**Problem:** Images not loading
- **Solution:** Cập nhật imageUrl hoặc sử dụng placeholder URLs

## 🚀 Production Deployment

1. Build jar file: `mvn clean package`
2. Upload jar to server
3. Set environment variables
4. Run: `java -jar demo-0.0.1-SNAPSHOT.jar`

Hoặc sử dụng Docker:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 📞 Support & Contact

- 📧 Email: admin@fastfood.vn
- 📱 Phone: 0812345678
- 🏠 Address: 123 Đường Lê Lợi, Q.1, TP.HCM

## 📄 License

MIT License - Sử dụng tự do cho mục đích cá nhân và thương mại

---

**Phiên bản:** 1.0.0  
**Ngày cập nhật:** 04/04/2026  
**Trạng thái:** Production Ready ✅
