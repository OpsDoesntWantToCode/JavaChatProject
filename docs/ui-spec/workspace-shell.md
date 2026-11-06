# ĐẶC TẢ GIAO DIỆN CỤM 2: KHUNG KHÔNG GIAN LÀM VIỆC CHÍNH (WORKSPACE SHELL SPECIFICATION)

> **Module:** Cụm 2 — Khung Làm Việc Chính (Main Workspace Shell)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)  
> **Lớp triển khai JavaFX:** `src/user/UserMainWorkspace.java`

---

## 1. Cấu trúc Lưới Bốn Cột Tiêu chuẩn (Four-Column Grid Architecture)

Khung làm việc chính sau khi đăng nhập thành công được tổ chức theo cấu trúc 4 cột công thái học:

```text
┌───────┬───────────────────┬───────────────────────────────────┬──────────────┐
│ CỘT 1 │       CỘT 2       │               CỘT 3               │    CỘT 4     │
│ RAIL  │    SUB-SIDEBAR    │             MAIN VIEW             │ RIGHT DRAWER │
│ (60px)│    (280-320px)    │            (Flexible)             │   (280px)    │
├───────┼───────────────────┼───────────────────────────────────┼──────────────┤
│[User] │ 🔍 Tìm kiếm...    │ 💬 Header Chat (Tên, Online, 🔒)  │ ℹ️ Chi tiết   │
│ (●)   │ ───────────────── │───────────────────────────────────│              │
│       │ ── HỘI THOẠI ──── │ [Bong bóng chat đối phương]       │ • Đổi tên ✏️ │
│ 💬    │ • Minh Trần (●)   │        [Bong bóng chat của mình]  │ • Thành viên │
│ Chat  │ • Lan Anh (○)     │ 🔒 Tin nhắn được bảo vệ AES-GCM   │ • Gán Admin  │
│       │ • 🔒 Nhóm Đồ Án   │                                   │ • Xóa TV ⛔  │
│ 👥    │                   │───────────────────────────────────│ • Báo cáo ⚠️ │
│Friends│                   │ [✨ Smart Reply Chips]            │ • Xóa chat 🗑│
│       │                   │ [Ô soạn thảo tin nhắn]     [Gửi]  │              │
│ ⚙️ 🚪 │                   │                                   │              │
└───────┴───────────────────┴───────────────────────────────────┴──────────────┘
```

---

## 2. Bảng Phân Bổ Kích Thước & Ánh Xạ Tokens

| Cột | Tên Thành phần | Chiều rộng | Bề mặt (Background) | Đường viền (Border) | Vai trò chức năng chính |
| :---: | :--- | :---: | :--- | :--- | :--- |
| **1** | **Navigation Rail** | `60px` cố định | `-bg-nav-rail: #0F1013` | Viền phải `1px solid -border-subtle` | Avatar người dùng, nút chuyển Chat/Danh bạ, Cài đặt, Đăng xuất |
| **2** | **Sub-Sidebar** | `280px – 320px` | `-bg-card: #1B1D22` | Viền phải `1px solid -border-subtle` | Tìm kiếm nhanh, danh sách hội thoại 1-1/nhóm hoặc cây danh mục danh bạ |
| **3** | **Main View** | Tự co giãn (`Priority.ALWAYS`) | `-bg-canvas: #141619` | Không viền | Header trò chuyện, khung chat ảo hóa hoặc bảng quản lý bạn bè |
| **4** | **Right Drawer** | `280px` cố định | `-bg-card: #1B1D22` | Viền trái `1px solid -border-subtle` | Chi tiết hội thoại, thành viên nhóm, phân quyền Admin, mã hóa, xóa chat |

---

## 3. Đặc tả Chi tiết Từng Cột

### 3.1 Cột 1 — Navigation Rail (`60px`)
*   **Avatar Người dùng:** Đường kính `40px` kèm chấm xanh trực tuyến `-status-online` (`#23A55A`). Nhấp chuột kích hoạt modal `UserSettingsDialog`.
*   **Bộ Nút Chuyển Chế độ (Mode Selectors):**
    *   💬 **Tin nhắn:** Hiển thị icon chat. Khi kích hoạt: vạch chỉ báo `3px` xanh Sapphire ở cạnh trái, icon trắng `#FFFFFF` trên nền `-bg-item-hover`.
    *   👥 **Danh bạ:** Hiển thị icon bạn bè. Khi click: Cột 2 chuyển sang danh mục bạn bè và Cột 3 chuyển sang trung tâm danh bạ.
*   **Công cụ Chân trang:** Nút Cài đặt ⚙️ và nút Đăng xuất 🚪 (đóng socket, đưa về `UserLoginView`).

### 3.2 Cột 2 — Sub-Sidebar (`280px – 320px`)
*   **Thanh Tìm kiếm Nhanh:** Ô nhập cao `36px`, bo góc `8px`, nền `-bg-input` (`#0F1013`), placeholder gợi ý.
*   **Danh sách Cuộc Trò chuyện (Chat List):**
    *   Mỗi dòng hiển thị: Avatar `36px`, Tên người dùng / Tên nhóm, Đoạn trích tin nhắn cuối (`13px`, `-text-muted`), Thời gian (`11px`).
    *   Biểu tượng ổ khóa `🔒` xanh lá bên cạnh tên nhóm có mã hóa AES-GCM.
    *   Huy hiệu tin chưa đọc (Unread Badge): Hình tròn bo góc màu `-status-danger` (`#FA777C`).

### 3.3 Cột 3 — Vùng Chính (Main View)
*   **Header Trò chuyện:** Avatar, Tên, trạng thái Online/Offline thời gian thực.
*   **Bộ công cụ Header:** Nút tìm kiếm trong đoạn chat (🔍), Nút báo cáo spam (⚠️), Nút mở Drawer (ℹ️).
*   **Vùng Nội dung:** Tích hợp Khung chat `ChatConversationView` hoặc Bảng bạn bè `FriendsManagementView`.

### 3.4 Cột 4 — Ngăn kéo Thông tin Phụ (Right Drawer — `280px`)
*   **Động học Đóng/Mở:** Mặc định ẩn (`managed = false`). Khi bấm nút `ℹ️` trên Header, trượt ra từ phải sang trái bằng `TranslateTransition` trong `200ms` (`motion.smooth`).
*   **Chức năng:** Xem thông tin đối phương / nhóm chat, danh sách thành viên, gán Admin, xem Khóa bảo mật E2EE, xóa lịch sử trò chuyện.
