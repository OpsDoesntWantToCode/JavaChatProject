# ĐẶC TẢ GIAO DIỆN CỤM 4: KHUNG TRÒ CHUYỆN & LỊCH SỬ TIN NHẮN (CHAT ENGINE SPECIFICATION)

> **Module:** Cụm 4 — Khung Chat Trực tiếp & Quản lý Lịch sử Tin nhắn (Mục 10, 11.a ➔ 11.g)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)  
> **Lớp triển khai JavaFX:** `src/user/ChatConversationView.java`

---

## 1. Kiến trúc Ảo hóa Danh sách & Hiệu năng (VirtualFlow Engine)

Nhằm tối ưu hóa hiệu năng render trên các cuộc trò chuyện có khối lượng tin nhắn lớn:

*   **VirtualFlow `ListView<Message>`:** BẮT BUỘC sử dụng `ListView<Message>` thay cho việc gắn trực tiếp Node vào `ScrollPane`. Chỉ các bong bóng chat nằm trong tầm nhìn (viewport) mới được cấp phát bộ nhớ RAM, duy trì tốc độ khung hình 60fps.
*   **Phân trang Tải dữ liệu (Lazy Pagination):** Khi người dùng cuộn lên đỉnh danh sách (`scrollPosition <= 0`), hệ thống kích hoạt truy vấn tải ngầm 30 tin nhắn cũ tiếp theo từ cơ sở dữ liệu.

---

## 2. Đặc tả Thành phần Khung Chat

### 2.1 Header Khung Chat (Top Bar)
*   **Định danh Đối phương (Mục 11.a):**
    *   Avatar `40px` kèm chấm màu trạng thái thời gian thực: Xanh lá (`-status-online: #23A55A`) khi Online, Xám (`-status-offline: #80848E`) khi Offline.
    *   Họ tên đối phương hoặc Tên nhóm chat (`font-size: 16px`, Bold, `-text-normal: #EDEDED`).
    *   Icon ổ khóa `🔒` xanh lá nếu phiên trò chuyện thuộc nhóm có mã hóa AES-GCM.
*   **Khối Công cụ Thao tác Nhanh:**
    1.  🔍 **Tìm kiếm trong hội thoại (Mục 11.f):** Mở thanh tìm kiếm nội bộ kèm 2 nút Lên/Xuống để duyệt tin nhắn khớp từ khóa.
    2.  ⚠️ **Báo cáo Spam (Mục 10):** Icon cờ cảnh báo kèm micro-animation rung lắc khi hover. Click mở hộp thoại gửi nội dung tố cáo về Admin.
    3.  ℹ️ **Thông tin hội thoại:** Đóng/mở Ngăn kéo Cột 4 (Right Drawer).

---

### 2.2 Luồng Bong Bóng Tin Nhắn (Chat Bubbles)

| Thuộc tính UI | Tin nhắn của Mình (Sent) | Tin nhắn Đối phương (Received) |
| :--- | :--- | :--- |
| **Căn lề** | Căn phải (`Pos.CENTER_RIGHT`) | Căn trái (`Pos.CENTER_LEFT`) |
| **Nền (Background)** | `-bubble-sent: #4E6BF2` | `-bubble-received: #252830` |
| **Màu chữ** | `-text-white: #FFFFFF` | `-text-normal: #EDEDED` |
| **Bo góc** | `12px` (Góc dưới phải: `3px`) | `12px` (Góc dưới trái: `3px`) |
| **Khoảng đệm** | `10px 14px` (`space.2 space.3`) | `10px 14px` (`space.2 space.3`) |
| **Timestamp** | `11px`, màu `rgba(255, 255, 255, 0.7)` | `11px`, màu `-text-muted: #8E929B` |

---

### 2.3 Tìm kiếm & Tự động Cuộn Highlight (Mục 11.f, 11.g)
*   Khi người dùng click vào một kết quả tìm kiếm tin nhắn:
    1.  Khung chat thực thi lệnh: `messageListView.scrollTo(targetIndex)`.
    2.  Kích hoạt `Timeline` chớp màu nền của ô `ListCell` từ màu vàng hổ phách (`rgba(240, 178, 50, 0.35)`) mờ dần về trong suốt trong `1500ms` (`motion.highlight`).

---

### 2.4 Cơ chế Chọn Xóa Một hoặc Nhiều Dòng Tin Nhắn (Mục 11.e)
*   **Chế độ Đa chọn (Multi-Select Mode):**
    *   Mỗi dòng tin nhắn chứa một `CheckBox` ẩn ban đầu.
    *   Khi bật cờ `multiSelectModeProperty = true`: Toàn bộ các dòng tin nhắn trượt nhẹ sang phải `24px` nhường chỗ cho `CheckBox` xuất hiện.
    *   Dưới đáy khung chat xuất hiện thanh công cụ nổi (Floating Action Bar):
        ```text
        [ Đã chọn X tin nhắn ] ──── [ 🗑 Xóa tin đã chọn (Đỏ) ] [ Hủy chọn ]
        ```
*   **Xóa toàn bộ lịch sử (Mục 11.d):** Nút đỏ đặt trong Ngăn kéo Cột 4 kèm modal xác nhận.
