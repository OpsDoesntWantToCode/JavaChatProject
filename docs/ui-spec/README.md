# HỆ THỐNG ĐẶC TẢ GIAO DIỆN PHÂN HỆ USER (USER UI SPECIFICATION SUITE)

> **Dự án:** Java Chat System (Ứng dụng Chat Desktop JavaFX 21 + CSS)  
> **Phân hệ phụ trách:** Thành viên B — Phân hệ Người dùng (User UI)  
> **Kiến trúc quản trị:** Three-Tier Design Tokens Architecture (W3C DTCG Standard)

---

## 1. Giới thiệu Tổng quan

Bộ tài liệu `docs/ui-spec/` được xây dựng nhằm chuẩn hóa toàn diện giao diện và trải nghiệm người dùng cho phân hệ User. Hệ thống tài liệu được tổ chức theo cấu trúc phân tầng công nghiệp: **01 Tài liệu Nền tảng Toàn cục (Parent Specification)** điều phối và các **Tài liệu Chức năng Con (Child Specifications)** chuyên trách.

Mô hình này đảm bảo tính kế thừa nhất quán về màu sắc, khoảng cách, font chữ và tương tác phản hồi, loại bỏ hoàn toàn sự phân mảnh giao diện (UI Drift).

---

## 2. Bản đồ Danh mục Tài liệu Đặc tả

```text
docs/ui-spec/
├── base-ui-spec.md             # [PARENT] Quy chuẩn Nền tảng Toàn cục (Tokens 3 tầng, Lưới 8-point, Bo góc, Motion)
├── auth.md                     # [Cụm 1] Xác thực & Tài khoản (Đăng nhập, Đăng ký 7 trường, Quên mật khẩu, Hero Section)
├── workspace-shell.md          # [Cụm 2] Bố cục Không gian Làm việc Chính (Rail 60px, Sub-Sidebar, Main View, Right Drawer)
├── friends-search.md           # [Cụm 3] Trung tâm Quản lý Bạn bè & Danh bạ (Tabs Online, Tất cả, Lời mời, Thêm bạn)
├── chat-messaging.md           # [Cụm 4] Khung Chat Trực tiếp & Lịch sử Tin nhắn (VirtualFlow ListView, Highlight cuộn, Xóa tin)
├── ai-assistant.md             # [Cụm 5] Trợ lý Trí tuệ Nhân tạo trong Hội thoại (Bot Stream, 3 chế độ Context, Smart Reply)
├── group-chat-encryption.md    # [Cụm 6] Quản trị Nhóm Chat & Mã hóa Đầu cuối Messenger Style (Tạo nhóm, Phân quyền Admin, E2EE)
├── dummy-data-matrix.md        # Ma trận Dữ liệu Giả lập Sống động cho Báo cáo Mốc 20% & Chụp ảnh giao diện
└── components-and-files.md     # Danh mục phân công và trách nhiệm các tệp mã nguồn JavaFX (src/user/)
```

---

## 3. Bảng Ma trận Kế thừa Chức năng

| Mã module | Tệp đặc tả | Vai trò & Phân hệ Chức năng | Tài liệu Kế thừa |
| :---: | :--- | :--- | :--- |
| **SYS** | **[base-ui-spec.md](base-ui-spec.md)** | **TÀI LIỆU CHA NỀN TẢNG:** Định nghĩa Three-Tier Tokens, Lưới 8-point, Bảng màu Obsidian, Font Segoe UI LCD ClearType, Icon vector trắng | *Gốc quản trị* |
| **U-01** | **[auth.md](auth.md)** | Cụm Xác thực: Đăng nhập (Mục 5), Đăng ký 2 cột (Mục 1), Quên mật khẩu (Mục 3), Hero Branding Section | `base-ui-spec.md` |
| **U-02** | **[workspace-shell.md](workspace-shell.md)** | Khung Không gian làm việc 4 cột: Navigation Rail, Sub-Sidebar, Main View, Right Drawer | `base-ui-spec.md` |
| **U-03** | **[friends-search.md](friends-search.md)** | Danh bạ Bạn bè: Lọc bạn online (Mục 8), Tất cả bạn bè (Mục 6), Lời mời kết bạn (Mục 7), Tìm kiếm người lạ (Mục 9) | `base-ui-spec.md` |
| **U-04** | **[chat-messaging.md](chat-messaging.md)** | Khung Trò chuyện: Ảo hóa VirtualFlow ListView, Tìm kiếm tin nhắn cuộn highlight (Mục 11.f, 11.g), Xóa tin nhắn (Mục 11.e) | `base-ui-spec.md` |
| **U-05** | **[ai-assistant.md](ai-assistant.md)** | Trợ lý AI: Tích hợp trong luồng chat (Mục 11.h), Khung nhập kích hoạt Meta AI, Smart Reply Chips, Gợi ý ngữ cảnh | `base-ui-spec.md` |
| **U-06** | **[group-chat-encryption.md](group-chat-encryption.md)** | Nhóm Chat & Bảo mật: Tạo nhóm (Mục 12.a), Phân quyền Admin (Mục 12.d, 12.e), Xác thực mã hóa Messenger Style (Mục 12.g) | `base-ui-spec.md` |
| **U-07** | **[dummy-data-matrix.md](dummy-data-matrix.md)** | Dữ liệu Kiểm thử: Ma trận dữ liệu mẫu phục vụ chụp ảnh nộp Báo cáo PDF Mốc 20% | `base-ui-spec.md` |
| **U-08** | **[components-and-files.md](components-and-files.md)** | Cấu trúc Mã nguồn: Danh mục tệp triển khai thực tế tại `src/user/` | `base-ui-spec.md` |

---

## 4. Hướng dẫn Dành cho Lập trình viên

1. **Tra cứu quy chuẩn:** Trước khi xây dựng hoặc tinh chỉnh bất kỳ component nào, lập trình viên tra cứu các giá trị Token tại [base-ui-spec.md](base-ui-spec.md).
2. **Kế thừa CSS:** Mọi thuộc tính màu sắc, font chữ và bo góc phải sử dụng các biến CSS semantic đã định nghĩa trong `src/user/theme.css` (`-bg-card`, `-accent-primary`, `-border-subtle`, `-text-normal`).
3. **Liên kết tài liệu:** Toàn bộ liên kết giữa các tài liệu trong dự án sử dụng đường dẫn tương đối chuẩn Git.
