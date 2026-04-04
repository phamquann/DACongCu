# 📖 User Guide - FastFood System

## Mục Lục
1. [Hướng Dẫn Khách Hàng](#hướng-dẫn-khách-hàng)
2. [Hướng Dẫn Quản Trị Viên](#hướng-dẫn-quản-trị-viên)
3. [Tìm Kiếm Sự Cố](#tìm-kiếm-sự-cố)

---

## 🛒 Hướng Dẫn Khách Hàng

### 1. Trang Chủ Mua Hàng

**Truy cập:** http://localhost:8080/

![Trang Chủ]
```
┌─────────────────────────────────────────────────────┐
│  🍕 FastFood     [Menu] [Về Chúng Tôi] [Liên Hệ]   │
└─────────────────────────────────────────────────────┘
│ Đặt Món Ăn Nhanh Online                             │
│ Giao hàng nhanh chóng, chất lượng tuyệt vời         │
│ [Tìm kiếm...] [Tìm Kiếm]                           │
└─────────────────────────────────────────────────────┘
│ [Tất Cả] [Burger] [Pizza] [Gà Rán] [Cơm] [Nước]    │
│                                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐             │
│  │ 🍔Burger │ │ 🍕Pizza  │ │ 🍗Gà Rán │             │
│  │Burger Bò │ │ Hải Sản  │ │1 Miếng   │             │
│  │50,000đ   │ │120,000đ  │ │35,000đ   │             │
│  │[+ Thêm]  │ │[+ Thêm]  │ │[+ Thêm]  │             │
│  └──────────┘ └──────────┘ └──────────┘             │
└─────────────────────────────────────────────────────┘
```

### 2. Tìm Kiếm Sản Phẩm

**Cách 1: Tìm kiếm theo từ khóa**
- Nhập tên sản phẩm vào ô tìm kiếm
- Ấn nút "Tìm Kiếm" hoặc Enter
- Kết quả sẽ hiển thị ngay

**Cách 2: Lọc theo danh mục**
- Click vào tab danh mục (Burger, Pizza, Gà Rán, v.v.)
- Danh sách sản phẩm sẽ cập nhật

**Ví dụ:**
```
Tìm: "Burger" → Hiển thị: Burger Bò, Burger Gà, Burger CộngHòa
Danh mục: "Pizza" → Hiển thị: Pizza Hải Sản, Pizza Thịt, Pizza Vegetarian
```

### 3. Thêm Sản Phẩm Vào Giỏ Hàng

1. Chọn sản phẩm bạn muốn
2. Click nút **"+ Thêm"**
3. Sản phẩm sẽ được thêm vào giỏ hàng
4. Số lượng trên biểu tượng 🛒 sẽ tăng lên

**Ví dụ:**
```
Bước 1: Click "Burger Bò" → "+ Thêm"
Bước 2: Giỏ hàng từ 0 → 1 (hiển thị trên icon 🛒)
```

### 4. Xem Giỏ Hàng

1. Click vào icon **🛒** ở góc trên phải
2. Sidebar giỏ hàng sẽ mở
3. Xem danh sách sản phẩm đã thêm

**Giỏ Hàng bao gồm:**
- Tên sản phẩm
- Giá từng sản phẩm
- Số lượng (có nút +/- để điều chỉnh)
- Tổng tiền (Tạm tính + Phí giao hàng)

### 5. Điều Chỉnh Số Lượng

**Cách 1: Từ giỏ hàng**
- Nhấn nút **-** để giảm số lượng
- Nhấn nút **+** để tăng số lượng
- Nhấn nút **✕** để xóa sản phẩm

**Ví dụ:**
```
Burger Bò: 1
- Ấn "+" → Burger Bò: 2
- Ấn "-" → Burger Bò: 1
- Ấn "✕" → Xóa Burger Bò khỏi giỏ
```

### 6. Đặt Hàng

1. Khi đã lựa chọn sản phẩm, click **"Giỏ Hàng"** 🛒
2. Click nút **"Đặt Hàng"** ở dưới cùng
3. Form đặt hàng sẽ hiện lên

**Thông tin cần nhập:**
```
┌─────────────────────────────────┐
│ Họ và Tên *                      │ Nguyễn Văn A
│ Email *                          │ nguyyen@example.com
│ Số Điện Thoại *                  │ 0912345678
│ Địa Chỉ Giao Hàng *              │ 123 Đường ABC
│ Ghi Chú (tùy chọn)               │ Không cay, thêm...
│ Thời Gian Giao Hàng *            │ 04/04/2026 19:00
│                                  │
│ Tổng tiền: 95,000đ               │
│                                  │
│ [Hủy] [Xác Nhận Đặt Hàng]        │
└─────────────────────────────────┘
```

**Lưu ý:**
- (*) = Bắt buộc phải điền
- "Ghi Chú" giúp cửa hàng hiểu yêu cầu của bạn
- Chọn thời gian giao hàng phù hợp

### 7. Hoàn Thành Đơn Hàng

- Click **"Xác Nhận Đặt Hàng"**
- Nếu thành công → Hiển thị ✓ "Đặt hàng thành công!"
- Cửa hàng sẽ liên hệ trong vòng 30 phút

**Sau đó:**
- Đơn hàng của bạn đã được ghi nhận
- Cửa hàng sẽ chuẩn bị
- Giao hàng vào thời gian bạn chọn

---

## 👨‍💼 Hướng Dẫn Quản Trị Viên

### 1. Đăng Nhập Admin

**Truy cập:** http://localhost:8080/admin

```
┌──────────────────────────────────────────────────┐
│  🍕 FastFood                   🔔 (3) [Cài đặt]   │
└──────────────────────────────────────────────────┘
│ ┌────────────────────────┐ ┌──────────────────────│
│ │ 📊 Tổng quan          │ │ Dashboard:           │
│ │ 📦 Đơn hàng           │ │ Tổng Đơn: 1,234      │
│ │ 🍔 Sản phẩm           │ │ Doanh Thu: 25.5M     │
│ │ 📂 Danh mục           │ │ Sản Phẩm: 45         │
│ │ 🔔 Thông báo          │ │ Khách: 567           │
│ │ ⚙️  Cài đặt           │ └──────────────────────│
└────────────────────────┘
```

### 2. Tổng Quan (Dashboard)

**Hiển thị:**
- Tổng số đơn hàng
- Doanh thu hôm nay
- Số sản phẩm có sẵn
- Tổng số khách hàng
- Danh sách đơn hàng gần đây

**Cách sử dụng:**
1. Click vào "📊 Tổng quan" (mặc định)
2. Xem các thống kê chính
3. Theo dõi trạng thái kinh doanh

### 3. Quản Lý Đơn Hàng

#### Xem Danh Sách Đơn Hàng

1. Click **"📦 Đơn hàng"** ở sidebar
2. Hiển thị bảng tất cả đơn hàng

```
┌──────────────────────────────────────────────┐
│ Quản Lý Đơn Hàng         [Tạo Đơn Mới]      │
├──────────────────────────────────────────────┤
│ Mã Đơn  │ Khách  │ Tiền  │ Trạng Thái  │ Hành│
├──────────────────────────────────────────────┤
│ #ORD001 │Phạm VB │250kđ  │ ⏳ Chờ xử lý │Sửa│
│ #ORD002 │Trần TC │320kđ  │ ✈️ Đang giao │Xóa│
│ #ORD003 │Lê VD   │180kđ  │ ✓ Đã giao   │    │
└──────────────────────────────────────────────┘
```

#### Cập Nhật Trạng Thái

1. Tìm đơn hàng cần cập nhật
2. Click nút **"Cập Nhật"**
3. Chọn trạng thái mới:
   - ⏳ Chờ xử lý → ✓ Xác nhận
   - ✓ Xác nhận → 👨‍🍳 Đang chuẩn bị
   - 👨‍🍳 Chuẩn bị → 📦 Sẵn sàng
   - 📦 Sẵn sàng → ✈️ Đang giao
   - ✈️ Đang giao → ✓ Đã giao

#### Xóa Đơn Hàng

1. Click nút **"Xóa"**
2. Xác nhận xóa
3. Đơn hàng sẽ bị xóa (hoặc chuyển sang hủy)

### 4. Quản Lý Sản Phẩm

#### Xem Danh Sách Sản Phẩm

1. Click **"🍔 Sản phẩm"** ở sidebar
2. Hiển thị lưới sản phẩm

```
┌──────────────────────────────────────────────┐
│ Quản Lý Sản Phẩm         [Thêm Sản Phẩm]    │
├──────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ 🍔       │  │ 🍕       │  │ 🍗       │   │
│  │ Burger B │  │ Pizza H  │  │ Gà Rán   │   │
│  │ 50,000đ  │  │120,000đ  │  │ 35,000đ  │   │
│  │ Tồn:25   │  │ Tồn:10   │  │ Tồn:40   │   │
│  │[Sửa][Xóa]│  │[Sửa][Xóa]│  │[Sửa][Xóa]│   │
│  └──────────┘  └──────────┘  └──────────┘   │
└──────────────────────────────────────────────┘
```

#### Thêm Sản Phẩm Mới

1. Click **"Thêm Sản Phẩm"**
2. Điền form:

```
┌─────────────────────────────┐
│ Tên Sản Phẩm *              │
│ Burger Bacon               │
│                             │
│ Mô Tả *                    │
│ Burger với thịt bacon crunchy
│                             │
│ Giá (VNĐ) *                │
│ 55000                       │
│                             │
│ Số Lượng *                  │
│ 30                          │
│                             │
│ Danh Mục *                  │
│ [Burger ▼]                 │
│                             │
│ [Lưu Sản Phẩm]             │
└─────────────────────────────┘
```

3. Click **"Lưu Sản Phẩm"**
4. Sản phẩm sẽ được tạo

#### Sửa Sản Phẩm

1. Click nút **"Sửa"** trên sản phẩm
2. Chỉnh sửa thông tin
3. Click **"Lưu Sản Phẩm"**

#### Xóa Sản Phẩm

1. Click nút **"Xóa"**
2. Xác nhận xóa
3. Sản phẩm sẽ bị xóa khỏi hệ thống

### 5. Quản Lý Danh Mục

#### Xem Danh Sách Danh Mục

1. Click **"📂 Danh mục"** ở sidebar
2. Hiển thị bảng danh mục:

```
┌─────────────────────────────────────┐
│ Quản Lý Danh Mục [Thêm Danh Mục]   │
├─────────────────────────────────────┤
│ Tên    │ Mô Tả           │ SP │ Hành│
├─────────────────────────────────────┤
│ Burger │ Các loại burger │ 5  │Sửa │
│ Pizza  │ Pizza đa dạng   │ 8  │Xóa │
│ Gà Rán │ Gà rán giòn     │ 4  │    │
└─────────────────────────────────────┘
```

#### Thêm Danh Mục Mới

1. Click **"Thêm Danh Mục"**
2. Nhập tên và mô tả
3. Click **"Lưu Danh Mục"**

#### Sửa/Xóa Danh Mục

- Click **"Sửa"** để chỉnh sửa
- Click **"Xóa"** để xóa

### 6. Quản Lý Thông Báo

#### Xem Danh Sách Thông Báo

1. Click **"🔔 Thông báo"** ở sidebar
2. Hiển thị bảng thông báo

```
┌─────────────────────────────────────────┐
│ Quản Lý Thông Báo [Tạo Thông Báo]      │
├─────────────────────────────────────────┤
│ Tiêu Đề         │ Nội Dung │ Loại │ Hành│
├─────────────────────────────────────────┤
│ Ưu đãi cuối tuần│ Giảm 30% │ℹ️ TT│ Xóa │
│ Menu mới        │ Xem mới │✓ TS│ Xóa │
└─────────────────────────────────────────┘
```

#### Tạo Thông Báo Mới

1. Click **"Tạo Thông Báo"**
2. Điền thông tin:

```
┌──────────────────────────────┐
│ Tiêu Đề                      │
│ Khuyến mãi 50% vào thứ Sáu   │
│                              │
│ Nội Dung                     │
│ Tất cả sản phẩm giảm 50%,    │
│ áp dụng vào thứ Sáu hàng tuần│
│                              │
│ Loại Thông Báo               │
│ [Thông tin ▼]                │
│                              │
│ [Gửi Thông Báo]             │
└──────────────────────────────┘
```

3. Chọn loại: ℹ️ Thông tin / ⚠️ Cảnh báo / ✓ Thành công / ❌ Lỗi
4. Click **"Gửi Thông Báo"**

### 7. Cài Đặt Hệ Thống

1. Click **"⚙️ Cài đặt"** ở sidebar
2. Cập nhật thông tin cửa hàng:

```
┌─────────────────────────────────┐
│ Cài Đặt Hệ Thống               │
├─────────────────────────────────┤
│ Tên Cửa Hàng                    │
│ FastFood Vietnam               │
│                                 │
│ Địa Chỉ                        │
│ 123 Đường Lê Lợi, Q.1, TP.HCM  │
│                                 │
│ Số Điện Thoại                   │
│ 0812345678                      │
│                                 │
│ Email                           │
│ admin@fastfood.vn              │
│                                 │
│ Giờ Mở Cửa                     │
│ 07:00                           │
│                                 │
│ Giờ Đóng Cửa                   │
│ 22:00                           │
│                                 │
│ Phí Giao Hàng (VNĐ)            │
│ 25000                           │
│                                 │
│ [Lưu Cài Đặt]                  │
└─────────────────────────────────┘
```

3. Click **"Lưu Cài Đặt"**

### 8. Xem Thông Báo

1. Click icon **🔔** ở góc trên phải
2. Xem panel thông báo
3. Thông báo chưa đọc được highlight
4. Click để đánh dấu đã đọc

---

## 🆘 Tìm Kiếm Sự Cố

### Problem 1: Không Thấy Sản Phẩm Trên Trang Khách Hàng

**Nguyên Nhân:**
- Sản phẩm không được đánh dấu là "Có sẵn"
- Database trống

**Giải pháp:**
1. Vào Admin → Sản phẩm
2. Kiểm tra các sản phẩm có trạng thái "Có sẵn"
3. Nếu không có, hãy tạo sản phẩm mới
4. Làm mới trang khách hàng (F5)

### Problem 2: Không Thể Đặt Hàng

**Nguyên Nhân:**
- Giỏ hàng trống
- Thiếu thông tin
- Form không được điền đầy đủ

**Giải pháp:**
- Kiểm tra lại giỏ hàng (có tối thiểu 1 sản phẩm)
- Điền đầy đủ trường bắt buộc (*)
- Kiểm tra số điện thoại có hợp lệ

### Problem 3: Ứng Dụng Chạy Chậm

**Nguyên Nhân:**
- Database quá lớn
- Số lượng đơn hàng quá nhiều

**Giải pháp:**
- Xóa dữ liệu cũ
- Tối ưu hóa database
- Restart ứng dụng

### Problem 4: Quên Mật Khẩu MySQL

**Giải pháp:**
```bash
# Reset password
mysqld --skip-grant-tables
mysql -u root
FLUSH PRIVILEGES;
SET PASSWORD FOR 'root'@'localhost' = PASSWORD('new_password');
EXIT;
```

### Problem 5: Lỗi CORS

**Nguyên Nhân:**
- Frontend và backend khác domain

**Giải pháp:**
- Kiểm tra `@CrossOrigin` trong controller
- Nếu cần, cập nhật allowed origins

```java
@CrossOrigin(origins = "*", maxAge = 3600)
```

---

## 📞 Liên Hệ Hỗ Trợ

**Email:** admin@fastfood.vn  
**Phone:** 0812345678  
**Address:** 123 Đường Lê Lợi, Q.1, TP.HCM

---

**Version:** 1.0.0  
**Last Updated:** 04/04/2026
