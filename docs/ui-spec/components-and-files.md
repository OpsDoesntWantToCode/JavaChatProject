# DANH MỤC THÀNH PHẦN & TỆP MÃ NGUỒN PHÂN HỆ USER (COMPONENTS & FILES SPECIFICATION)

> **Module:** Cấu trúc Triển khai Mã nguồn Giao diện (`src/user/`)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)

---

## 1. Cấu trúc Cây Thư mục Phân hệ User (`src/user/`)

```text
src/user/
├── theme.css                   # Stylesheet trung tâm (Tokens 3 tầng, Deep Obsidian, Layered Elevation, Selective Radius, Icons)
├── JavaFXTheme.java            # Lớp tiện ích: PasswordFieldWrapper, Shake, FieldLabel, Password Strength Meter
├── UserLoginApp.java           # Entry point Application, điều phối chuyển cảnh và bố cục Split-Screen
├── UserHeroSection.java        # Cột giới thiệu Hero Branding khi Fullscreen (Icon vector Lucide trắng thuần túy)
├── UserLoginView.java          # Màn hình Đăng nhập (Mục 5)
├── UserRegisterView.java       # Màn hình Đăng ký 7 trường 2 cột (Mục 1)
├── UserForgotPasswordView.java # Màn hình Khởi tạo lại mật khẩu qua Email (Mục 3)
├── UserMainWorkspace.java      # Khung chính 4 Cột: Rail, Sub-Sidebar, Main View, Right Drawer
├── FriendsManagementView.java  # Trung tâm Bạn bè 4 tabs hiện đại (Mục 6, 7, 8, 9)
├── ChatConversationView.java   # Khung chat 1-1 & nhóm ListView tối ưu, tin nhắn AI bot, highlight vàng (Mục 10, 11)
├── GroupManagementDrawer.java  # Ngăn kéo Cột 4 quản lý thành viên, đổi tên, mã hóa Messenger E2EE (Mục 12)
└── UserSettingsDialog.java     # Hộp thoại Modal 2 tab: Hồ sơ cá nhân & Đổi mật khẩu (Mục 2, 4)
```

---

## 2. Mô tả Trách nhiệm & Trạng thái Triển khai

| Tệp mã nguồn | Vai trò & Trách nhiệm chính | Trạng thái kỹ thuật |
| :--- | :--- | :---: |
| `src/user/theme.css` | Quản lý toàn bộ Design Tokens 3 tầng, định nghĩa bảng màu Deep Obsidian, độ nổi phân tầng Layered Elevation, bo góc có chọn lọc (8px, 9px, 10px, 14px), và màu trắng `#FFFFFF` cho SVG icons. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/JavaFXTheme.java` | Tiện ích UI: `PasswordFieldWrapper` có nút con mắt 👁 đồng bộ 2 chiều, `shakeNode()` báo rung lắc lỗi viền đỏ, thanh đo độ mạnh mật khẩu 4 phân đoạn trực quan. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/UserLoginApp.java` | Lớp khởi chạy ứng dụng JavaFX, quản lý Stage, lắng nghe chiều rộng cửa sổ (≥ 1000px) để kích hoạt Split-Screen Hero Section, chuyển cảnh Fade & Slide mượt mà 60fps. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/UserHeroSection.java` | Hiển thị cột Hero 45% khi Fullscreen, bố cục 3 thẻ tính năng chữ nhật bo góc `10px`, icon vector Lucide thuần trắng trong hộp badge `38x38px`. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/UserLoginView.java` | Giao diện Đăng nhập, thẻ Card `460px` bo góc `14px`, độ nổi phân tầng hairline border sắc nét, hỗ trợ nút con mắt ẩn/hiện mật khẩu, link chuyển cảnh Đăng ký và Quên mật khẩu. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/UserRegisterView.java` | Giao diện Đăng ký 7 trường chia GridPane 2 cột đối xứng, DatePicker và ComboBox Giới tính chuẩn Matte Dark có popup animation, thanh đo độ mạnh mật khẩu 4 khúc. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/UserForgotPasswordView.java` | Giao diện Khởi tạo lại mật khẩu, thẻ Card `460px` bo góc `14px`, thanh tiến trình `ProgressBar` giả lập kết nối SMTP. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/UserMainWorkspace.java` | Khung giao diện chính sau đăng nhập: Navigation Rail `60px`, Sub-Sidebar `280-320px`, Vùng chính co giãn, và Right Drawer `280px`. | **Đã hoàn thiện & Kiểm thử** |
| `src/user/FriendsManagementView.java` | Trung tâm Bạn bè tích hợp 4 Tabs: Đang Online, Tất cả bạn bè, Lời mời kết bạn, và Thêm bạn/Tìm người lạ với đầy đủ 3 nút thao tác độc lập. | **Sẵn sàng triển khai** |
| `src/user/ChatConversationView.java` | Khung chat trực tiếp tối ưu hóa VirtualFlow `ListView<Message>`, hỗ trợ tìm kiếm tin nhắn tự động cuộn highlight vàng, chọn xóa nhiều tin nhắn, và hiển thị bot AI. | **Sẵn sàng triển khai** |
| `src/user/GroupManagementDrawer.java` | Ngăn kéo Cột 4 quản lý nhóm chat, phân quyền Admin (chỉ Admin mới có nút xóa thành viên), đổi tên nhóm, và hiển thị Khóa bảo mật E2EE. | **Sẵn sàng triển khai** |
| `src/user/UserSettingsDialog.java` | Hộp thoại Modal nổi mở từ Avatar người dùng, chia 2 Tab: Cập nhật hồ sơ cá nhân (Mục 2) và Đổi mật khẩu (Mục 4). | **Đã hoàn thiện & Kiểm thử** |
