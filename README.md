# Food Delivery Dashboard - React + Tailwind CSS

Một ứng dụng quản lý và đặt đồ ăn nhanh hoàn chỉnh với giao diện Admin, User Booking, và Chat Support.

## 📋 Các tính năng

### 1. **Admin Dashboard** 🛠️

- Sidebar điều hướng với icon (Home, Quản lý người dùng, Đơn hàng)
- Header với tìm kiếm, thông báo và avatar
- **4 thẻ thống kê nhanh** với màu sắc đặc trưng:
  - 🟨 Đơn chờ xác nhận (Vàng)
  - 🔵 Đơn đang chuẩn bị (Xanh dương)
  - 🟢 Đơn đã hoàn tất (Xanh lá)
  - ⚫ Lịch đặt trước (Xám)
- **Bảng dữ liệu** hiển thị danh sách lịch đặt trước với các cột:
  - Mã đơn
  - Tên khách hàng
  - Thời gian
  - Món ăn
  - Trạng thái

### 2. **User Booking Page** 📅

- **Chọn ngày & giờ**: Nút chọn ngày (Ngày, 1-5) và dropdown giờ/phút
- **Tìm kiếm món ăn**: Input tìm kiếm với bộ lọc real-time
- **Danh sách menu**: Hiển thị các món ăn phổ biến với đánh giá sao
- **Sidebar phải**:
  - Danh sách điểm nổi bật (Phở, Cơm tấm, Bún chả, Gà rán)
  - Hiển thị thông tin lịch đặt trước hiện tại

### 3. **Chat Support** 💬

- **Danh sách chat**: Hiển thị các cuộc hội thoại (Shipper, Hỗ trợ, Nhà hàng)
- **Khu vực tin nhắn**: Bong bóng chat riêng cho người dùng và Shipper
- **Input bar**: Gồm nút đính kèm, chụp ảnh, và gửi tin nhắn
- **Header chat**: Hiển thị tên và trạng thái online

## 🎨 Thiết kế

- **Màu sắc chủ đạo**: Cam (#f97316) cho các nút và accent
- **Kiểu góc bo tròn**: `rounded-lg` cho tất cả component
- **Responsive**: Tối ưu cho cả mobile và desktop
- **Icons**: Sử dụng `lucide-react` cho các icon đẹp mắt

## 🚀 Hướng dẫn cài đặt & chạy

### Yêu cầu

- **Node.js** >= 16.x
- **npm** hoặc **yarn**

### 1. Cài đặt Dependencies

```bash
npm install
# hoặc
yarn install
```

### 2. Chạy Development Server

```bash
npm run dev
# hoặc
yarn dev
```

Server sẽ chạy trên `http://localhost:3000`

### 3. Build cho Production

```bash
npm run build
# hoặc
yarn build
```

## 📁 Cấu trúc dự án

```
.
├── App.jsx                    # Main App component với navigation
├── components/
│   ├── AdminDashboard.jsx    # Dashboard quản lý
│   ├── UserBooking.jsx       # Trang đặt chỗ người dùng
│   └── ChatSupport.jsx       # Trang chat hỗ trợ
├── index.html                # HTML entry point
├── main.jsx                  # React entry point
├── index.css                 # Global styles + Tailwind
├── package.json              # Dependencies
├── tailwind.config.js        # Tailwind configuration
├── postcss.config.js         # PostCSS configuration
└── vite.config.js            # Vite configuration
```

## 🛠️ Technology Stack

| Công nghệ        | Mục đích                |
| ---------------- | ----------------------- |
| **React**        | Frontend framework      |
| **Tailwind CSS** | Utility-first CSS       |
| **Vite**         | Build tool & dev server |
| **Lucide React** | Icon library            |

## 📱 Responsive Design

Tất cả các component được thiết kế responsive với các breakpoint:

- `md:` (768px) - Tablet
- `lg:` (1024px) - Desktop

## 🎯 Các tính năng interactif

### AdminDashboard

- ✅ Sidebar với icon chuyển page
- ✅ Tìm kiếm trong header
- ✅ Thông báo
- ✅ Hover effects trên bảng dữ liệu

### UserBooking

- ✅ Chọn ngày & giờ động
- ✅ Tìm kiếm & lọc món ăn real-time
- ✅ Nút thêm vào giỏ
- ✅ Hiển thị đánh giá sao

### ChatSupport

- ✅ Chuyển đổi giữa các cuộc chat
- ✅ Gửi tin nhắn with real-time display
- ✅ Phân biệt tin nhắn user/shipper
- ✅ Responsive chat layout

## 🎨 Tùy chỉnh màu sắc

Để thay đổi màu sắc cam chủ đạo, chỉnh sửa `tailwind.config.js`:

```javascript
theme: {
  extend: {
    colors: {
      orange: {
        500: '#YOUR_COLOR_HERE', // Thay đổi màu cam
      },
    },
  },
}
```

## 📝 Ghi chú

- Component sử dụng **functional components** với React Hooks
- CSS được quản lý hoàn toàn qua **Tailwind CSS** (không có file CSS riêng)
- Sử dụng **lucide-react** cho icons (có thể thay bằng icon library khác)

## 👨‍💻 Phát triển thêm

Để mở rộng ứng dụng:

1. **Thêm routing** - Sử dụng `react-router-dom`
2. **State management** - Thêm `Redux` hoặc `Zustand`
3. **API integration** - Kết nối backend API
4. **Database** - Setup database để lưu dữ liệu thực

Ví dụ:

```bash
npm install react-router-dom
npm install zustand
```

## 📞 Hỗ trợ

Nếu gặp vấn đề, kiểm tra:

- Node.js version: `node --version`
- npm version: `npm --version`
- Clear cache và reinstall: `rm -rf node_modules && npm install`

---

**Được tạo với ❤️ sử dụng React + Tailwind CSS**
