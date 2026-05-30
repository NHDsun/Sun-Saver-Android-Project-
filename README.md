# ☀️ Sun Saver - Ứng Dụng Quản Lý Chi Tiêu Cá Nhân

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Language">
  <img src="https://img.shields.io/badge/Database-Firestore-blue.svg" alt="Database">
  <img src="https://img.shields.io/badge/Auth-Firebase-yellow.svg" alt="Auth">
</div>

---

## 📝 Giới thiệu
**Sun Saver** là giải pháp quản lý tài chính cá nhân toàn diện trên nền tảng Android. Ứng dụng đã được nâng cấp lên hệ thống lưu trữ đám mây **Firebase Firestore**, đảm bảo dữ liệu của bạn luôn được đồng bộ và an toàn trên mọi thiết bị.

## 🚀 Tính năng chính

### 🔐 Bảo mật & Đồng bộ đám mây
*   **Firebase Authentication:** Đăng nhập và đăng ký an toàn qua Firebase.
*   **Đồng bộ thời gian thực:** Dữ liệu giao dịch được cập nhật ngay lập tức giữa các thiết bị thông qua Firestore.
*   **Cá nhân hóa:** Mỗi người dùng sở hữu một kho dữ liệu riêng biệt dựa trên UID duy nhất.

### 💰 Quản lý giao dịch (CRUD)
*   **Ghi chép nhanh:** Thêm mới giao dịch Thu nhập (Income) hoặc Chi tiêu (Expense).
*   **Phân loại thông minh:** Hỗ trợ nhiều danh mục như Ăn uống, Học tập, Giải trí, Di chuyển...
*   **Lịch sử giao dịch:** Xem, tìm kiếm, chỉnh sửa hoặc xóa bỏ giao dịch trực tiếp trên đám mây.

### 📊 Thống kê & Phân tích
*   **Tổng quan tài chính:** Tự động tính toán Tổng Thu, Tổng Chi và Số dư hiện tại từ Firestore.
*   **Báo cáo chi tiết:** Theo dõi thói quen chi tiêu một cách trực quan.

## 🛠 Công nghệ sử dụng
*   **Ngôn ngữ:** Java (Android)
*   **Cơ sở dữ liệu:** Google Firebase Firestore
*   **Xác thực:** Firebase Authentication
*   **Giao diện:** XML Layouts, Material Design Components

## 📂 Cấu trúc dự án
```text
app/src/main/java/com/example/
├── activities/    # Điều khiển luồng giao diện (Login, Main, Statistics...)
├── adapters/      # Cầu nối dữ liệu hiển thị lên ListView/RecyclerView
├── database/      # FirestoreHelper - Xử lý truy vấn dữ liệu đám mây
└── models/        # Transaction - Lớp đối tượng định nghĩa dữ liệu
```

## ⚙️ Cấu hình Firebase
Để ứng dụng hoạt động, bạn cần:
1.  Tạo project trên [Firebase Console](https://console.firebase.google.com/).
2.  Bật **Authentication** (Email/Password) và **Firestore Database**.
3.  Tải file `google-services.json` và đặt vào thư mục `app/`.

---
**Sun Saver** - *Dữ liệu của bạn, luôn bên bạn!* ☀️
