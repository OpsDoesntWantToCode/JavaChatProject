# ĐẶC TẢ GIAO DIỆN & TRẢI NGHIỆM NGƯỜI DÙNG — PHÂN HỆ USER (USER UI SPECIFICATION)

> **Dự án:** Java Chat System (Đồ án Lập trình ứng dụng Java)  
> **Phụ trách:** Thành viên B — Trần Nguyễn Tuấn Minh (MSSV: 24127084)  
> **Nền tảng công nghệ:** Java 21 LTS, JavaFX 21 + CSS (`discord-theme.css`)  
> **Mô hình kiến trúc UI:** Tree Layer Model (Primitive ➔ Semantic ➔ Component Tokens)  
> **Thư viện Icon & Animation:** Line Icons (Lucide / Feather / Tabler via Ikonli & native `SVGPath`), GPU-accelerated Transitions & Micro-animations  
> **Tài liệu tham chiếu chuẩn:** [final-project.pdf](file:///d:/JavaChatProject/docs/final-project.pdf), [AGENTS.md](file:///d:/JavaChatProject/AGENTS.md), [tech-stack.md](file:///d:/JavaChatProject/docs/tech-stack.md), [plan.md](file:///d:/JavaChatProject/docs/plan.md)  
> **Phong cách chủ đạo:** Modern Discord Dark kết hợp Telegram Desktop, Zalo PC, Notion AI và Facebook Messenger E2EE.

---

## 1. Mô hình Kiến trúc UI Ba Tầng (Tree Layer Model / Design Tokens)

Hệ thống giao diện được tổ chức theo chuẩn **Three-Layer Token Architecture** chuẩn ngành nhằm bảo đảm tính kế thừa, dễ bảo trì và phân lập ranh giới rõ ràng giữa giá trị thô và ngữ nghĩa hiển thị:

```text
┌────────────────────────────────────────────────────────────────────────┐
│  TẦNG 1: PRIMITIVE TOKENS (Giá trị thô: Bảng màu gốc, Thước đo, Lưới)   │
│  • Hex Colors (#2B2D31, #5865F2...) • Spacing (4px, 8px, 16px, 24px)   │
│  • Typography Scale (11px, 13px, 16px, 22px) • Timing (120ms, 200ms)  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  TẦNG 2: SEMANTIC TOKENS (Ý nghĩa ngữ nghĩa & Vai trò trong hệ thống)   │
│  • --bg-canvas, --bg-card, --bg-input • --accent-primary               │
│  • --status-online, --status-danger • --text-normal, --text-muted      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  TẦNG 3: COMPONENT-LEVEL TOKENS & STATES (Áp dụng cho từng thành phần) │
│  • .btn-primary:hover, .btn-primary:pressed • .input-error:focus       │
│  • .chat-bubble-sent, .chat-bubble-received • .ai-card-ephemeral       │
└────────────────────────────────────────────────────────────────────────┘
```

### 1.1 Tầng 1 — Primitive Tokens (Thô)
*   **Color Primitives:**
    *   Dark Slates: `#18191C`, `#1E1F22`, `#23252B`, `#2B2D31`, `#313338`, `#383A40`, `#3E4047`.
    *   Discord Blurples: `#5865F2`, `#4752C4`, `#3C45A5`.
    *   Meta AI Gradients: `#00C6FF`, `#7F00FF`, `#E100FF`, `#8B5CF6`.
    *   Semantic Hues: Emerald Green `#23A55A`, Ruby Red `#FA777C` / `#DA373C`, Amber `#F0B232`, Cyan Link `#00A8FC`.
    *   Neutrals: Pure White `#FFFFFF`, Soft White `#F2F3F5`, Muted Gray `#949BA4`, Dark Border `#3E4047`.
*   **Spacing Primitives (8-Point Grid):** `space-xs: 4px`, `space-sm: 8px`, `space-md: 12px`, `space-lg: 16px`, `space-xl: 24px`, `space-2xl: 32px`.
*   **Typography Scale:** `font-xs: 10px`, `font-sm: 11px`, `font-base: 13px`, `font-md: 14px`, `font-lg: 16px`, `font-xl: 22px`.
*   **Motion Timing Primitives:** `duration-instant: 80ms`, `duration-fast: 120ms`, `duration-normal: 200ms`, `duration-smooth: 300ms`.

### 1.2 Tầng 2 — Semantic Tokens (Ngữ nghĩa)
*   `--bg-canvas`: Nền canvas tổng thể của ứng dụng (`#2B2D31`).
*   `--bg-card`: Bề mặt các thẻ Card nổi, Drawer, Dialog (`#313338`).
*   `--bg-input`: Bề mặt các trường nhập liệu (`#1E1F22`).
*   `--bg-nav-rail`: Thanh điều hướng mỏng 60px ngoài cùng bên trái (`#1E1F22`).
*   `--accent-primary`: Màu chủ đạo (`#5865F2`).
*   `--accent-hover`: Trạng thái hover của màu chủ đạo (`#4752C4`).
*   `--status-online`: Màu báo trực tuyến và mật khẩu an toàn (`#23A55A`).
*   `--status-offline`: Màu báo ngoại tuyến (`#80848E`).
*   `--status-danger`: Màu cảnh báo, nút Chặn, nút Xóa, viền lỗi (`#FA777C` / `#DA373C`).
*   `--text-normal`: Chữ chính độ tương phản cao (> 7:1 đạt WCAG AAA) (`#F2F3F5`).
*   `--text-muted`: Chữ phụ, ghi chú, placeholder (`#949BA4`).
*   `--text-link`: Liên kết có thể click (`#00A8FC`).
*   `--bubble-sent`: Nền bong bóng tin nhắn người gửi (`#5865F2`).
*   `--bubble-received`: Nền bong bóng tin nhắn người nhận (`#383A40`).

### 1.3 Tầng 3 — Component-Level Tokens & Trạng thái (Component & States)
*   `.btn-primary`: Nền `--accent-primary`, bo góc `6px`, chữ đậm. Khi `:hover` nâng sáng độ phủ `8%` trong `120ms`, khi `:pressed` thụt nhẹ `0.95x`.
*   `.text-field`, `.password-field`: Nền `--bg-input`, viền `1px solid transparent`. Khi `:focused` hiện viền phát sáng `--accent-primary` kèm drop shadow mượt.
*   `.input-error`: Viền đỏ `--status-danger`, phát sáng đỏ `rgba(250, 119, 124, 0.35)`.
*   `.chat-input-box.ai-active`: Khung soạn thảo viền chuyển sắc Gradient Meta AI kèm đổ bóng Gaussian phát sáng tím.

---

## 2. Hệ thống Line Icon & Micro-animations Tương tác (JavaFX Hardware Accelerated)

Ứng dụng tận dụng tối đa thế mạnh đồ họa của **JavaFX 21** (`SVGPath`, GPU-accelerated `Timeline`, `Transition`, Prism Pipeline) kết hợp bộ **Line Icon chuyên dụng (Lucide / Feather / Tabler)**.

### 2.1 Ba Kỹ thuật Chuyển động Icon (Micro-animation Techniques)

1.  **Kỹ thuật 1: Line Icon (Ikonli FontIcon / SVGPath) + JavaFX Transitions:**
    *   *Áp dụng cho 90% nút bấm thông thường.*
    *   **Nút Xóa dòng chat / Nút Thùng rác (Mục 11.e):**
        *   Khi hover: Kích hoạt `RotateTransition` lắc nhẹ luân phiên góc `-15°` sang `+15°` (Tada effect trong `200ms`), đồng thời icon chuyển từ màu xám `--text-muted` sang màu đỏ `--status-danger`.
    *   **Nút Báo cáo Spam (Mục 10):**
        *   Khi hover: Icon hình khiên/cờ cắm rung lắc nhẹ `TranslateTransition` theo trục X kèm hiệu ứng phát sáng màu cam cảnh báo.
    *   **Nút Gửi tin nhắn / Kết bạn (Mục 6.b, Mục 11.c):**
        *   Khi hover: Dùng `ScaleTransition` phóng to nhẹ `1.15x` trong `120ms`.
        *   Khi `mousePressed`: Thu nhỏ về `0.90x` (tạo cảm giác phản hồi xúc giác cơ học - Elastic physics).

2.  **Kỹ thuật 2: "Vẽ nét chạy" (Stroke-Dash Offset Animation) với `SVGPath`:**
    *   *Chuẩn phong cách UseAnimations / Lordicon sắc nét.*
    *   Sử dụng chuỗi vector `SVGPath` từ **Lucide Icons** (độ dày nét `stroke-width: 1.8`, `stroke-linecap: round`).
    *   **Hiệu ứng Draw-on-hover:** Thiết lập `getStrokeDashArray().setAll(50d, 50d)`. Khi người dùng rê chuột vào (`setOnMouseEntered`), `Timeline` chạy thuộc tính `strokeDashOffsetProperty()` từ `50` về `0` trong `300ms`. Nét vẽ icon sẽ chạy uốn lượn uốn quanh từ đầu đến cuối cực kỳ tinh xảo.

3.  **Kỹ thuật 3: Vòng tròn Chuyển sắc & Hiệu ứng Đang suy nghĩ của Meta AI (Mục 11.h):**
    *   **Biểu tượng Vòng tròn Meta AI:** Sử dụng dải màu chuyển sắc Gradient 3D:
        ```css
        linear-gradient(to right, #00C6FF, #7F00FF, #E100FF)
        ```
    *   Khi AI đang xử lý sinh câu trả lời: Kích hoạt `RotateTransition` xoay tròn 360 độ vô hạn kèm hiệu ứng nhấp nháy phát sáng (`Pulse`). Khi sinh xong, trở về trạng thái tĩnh.

---

## 3. Kiến trúc Cấu trúc Không gian & 6 Cụm Màn hình Nghiệp vụ

---

### 3.1 Cụm 1: Màn hình Xác thực & Tài khoản (Mục 1, 2, 3, 4, 5)

*   **Ứng dụng tham khảo:** Discord & Linear Desktop.
*   **Cửa sổ Auth Container:** Khung kích thước `900x600` (hoặc card căn giữa `560x680`), nền `ambient-canvas`. Sử dụng `StackPane` kết hợp hiệu ứng `FadeTransition` (100ms) và `TranslateTransition` (120ms) để chuyển cảnh mượt mà giữa 3 view:

#### A. Màn hình Đăng nhập (Mục 5)
*   **Bố cục:** Thẻ Card trung tâm rộng `440px`.
*   **Các trường & thành phần:**
    1. Tiêu đề: *"Chào mừng trở lại!"*, Phụ đề: *"Rất vui được gặp lại bạn một lần nữa!"*.
    2. Ô Tên đăng nhập hoặc Email (`TextField`).
    3. Ô Mật khẩu (`PasswordFieldWrapper`): Có nút con mắt Line Icon 👁 ẩn/hiện mật khẩu tức thì.
    4. Nút bấm *"Đăng nhập"* (màu Blurple, có hiệu ứng loading khi submit).
    5. Link *"Quên mật khẩu?"* ➔ chuyển sang view Khởi tạo lại mật khẩu.
    6. Link *"Cần một tài khoản? Đăng ký"* ➔ chuyển sang view Đăng ký.
*   **Dữ liệu mẫu điền sẵn:** `tuanminh_2026`, `Password@123`.

```text
┌─────────────────────────────────────────────────────────────┐
│                    CỬA SỔ AUTH CONTAINER                    │
│                                                             │
│         ┌─────────────────────────────────────────┐         │
│         │           Chào mừng trở lại!            │         │
│         │   Rất vui được gặp lại bạn một lần nữa! │         │
│         │                                         │         │
│         │  TÊN ĐĂNG NHẬP HOẶC EMAIL *             │         │
│         │  ┌───────────────────────────────────┐  │         │
│         │  │ tuanminh_2026                     │  │         │
│         │  └───────────────────────────────────┘  │         │
│         │                                         │         │
│         │  MẬT KHẨU *                             │         │
│         │  ┌───────────────────────────────┬───┐  │         │
│         │  │ ••••••••••••••                │ 👁│  │         │
│         │  └───────────────────────────────┴───┘  │         │
│         │  Quên mật khẩu?                         │         │
│         │                                         │         │
│         │  ┌───────────────────────────────────┐  │         │
│         │  │             ĐĂNG NHẬP             │  │         │
│         │  └───────────────────────────────────┘  │         │
│         │                                         │         │
│         │  Cần một tài khoản? Đăng ký             │         │
│         └─────────────────────────────────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

#### B. Màn hình Đăng ký (Mục 1)
*   **Bố cục:** Thẻ Card rộng `540px`, chia **GridPane 2 Cột đối xứng (50% - 50%)** giúp form không bị tràn trang:
    *   **Cột 1 (Thông tin tài khoản & cư trú):**
        *   Tên đăng nhập (`TextField`)
        *   Email (`TextField`)
        *   Địa chỉ (`TextField`)
        *   Mật khẩu (`PasswordFieldWrapper` có nút con mắt 👁)
        *   Thanh đo độ mạnh mật khẩu (Strength ProgressBar: Đỏ / Vàng / Xanh lá kèm nhãn *"Độ mạnh: Rất an toàn ✓"*)
    *   **Cột 2 (Thông tin cá nhân & xác thực):**
        *   Họ và tên (`TextField`)
        *   Ngày sinh (JavaFX `DatePicker` chọn lịch trực quan)
        *   Giới tính (`ComboBox<String>`: Nam, Nữ, Khác)
        *   Xác nhận mật khẩu (`PasswordFieldWrapper` có nút con mắt 👁)
*   Nút CTA: *"Đăng ký tài khoản"* (full-width).
*   Footer: *"Đã có tài khoản? Đăng nhập"*.

#### C. Màn hình Khởi tạo lại mật khẩu (Mục 3)
*   **Bố cục:** Form tối giản trong card `440px`.
*   **Thành phần:**
    *   Tiêu đề: *"Khởi tạo lại mật khẩu"*, Phụ đề: *"Nhập email hoặc tên đăng nhập để nhận mật khẩu mới qua thư điện tử."*.
    *   Ô nhập Email hoặc Tên đăng nhập.
    *   Thanh tiến trình `ProgressBar` (Indeterminate mode) ẩn ban đầu; khi bấm gửi sẽ hiện lên mô phỏng quá trình kết nối Jakarta Mail API để giao diện không bị giật/đơ.
    *   Nút *"Gửi mật khẩu mới qua Email"*.
    *   Link *"← Quay lại Đăng nhập"*.

#### D. Hộp thoại Cập nhật thông tin & Đổi mật khẩu (Mục 2, 4)
*   **Vị trí:** Không làm màn hình rời. Được mở bằng popup/dialog khi bấm vào **Avatar người dùng ở góc dưới Cột 1 (Navigation Rail)**.
*   **Cấu trúc Dialog (User Settings — 2 Tabs):**
    *   **Tab 1 — Hồ sơ cá nhân (Mục 2):** Cập nhật Họ và tên, Địa chỉ, Ngày sinh (`DatePicker`), Giới tính (`ComboBox`). Nút *"Lưu thay đổi"*.
    *   **Tab 2 — Bảo mật & Đổi mật khẩu (Mục 4):** Nhập Mật khẩu hiện tại, Mật khẩu mới, Xác nhận mật khẩu mới. Nút *"Cập nhật mật khẩu"*.

---

### 3.2 Cụm 2: Bố cục Khung chính sau khi Đăng nhập (Main Workspace)

*   **Ứng dụng tham khảo:** Zalo PC kết hợp Telegram Desktop.
*   **Cấu trúc 4 Cột chuẩn từ trái sang phải:**

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

1.  **Cột 1 — Navigation Rail (60px):**
    *   Góc trên cùng: Avatar người dùng tròn kèm chấm xanh lá báo Online. Click mở **User Settings Dialog** (Mục 2, 4).
    *   Ở giữa: 2 nút Icon lớn chuyển đổi chế độ:
        *   💬 **Tin nhắn (Chat):** Mở danh sách hội thoại và khung chat.
        *   👥 **Danh bạ (Friends):** Mở trung tâm quản lý bạn bè.
    *   Dưới cùng: Nút ⚙️ Cài đặt và 🚪 Đăng xuất.
2.  **Cột 2 — Sub-Sidebar (280px – 320px):**
    *   Nội dung thay đổi linh hoạt theo Cột 1:
        *   *Khi ở tab Tin nhắn:* Hiển thị thanh tìm kiếm hội thoại, danh sách chat 1-1 và nhóm chat có badge unread.
        *   *Khi ở tab Danh bạ:* Hiển thị cây danh mục: Bạn bè, Danh sách nhóm, Lời mời kết bạn.
3.  **Cột 3 — Vùng chính (Flexible — co giãn tự do):**
    *   Hiển thị Khung Chat tương tác (khi ở tab Tin nhắn) hoặc Bảng Quản lý Bạn bè (khi ở tab Danh bạ).
4.  **Cột 4 — Ngăn kéo Thông tin / Right Drawer (280px — trượt đóng/mở):**
    *   Bình thường có thể thu gọn. Khi bấm nút ℹ️ trên Header thì trượt ra hiển thị chi tiết hội thoại, danh sách thành viên nhóm, đổi tên nhóm, gán Admin, báo cáo spam, xóa lịch sử.

---

### 3.3 Cụm 3: Màn hình Quản lý Bạn bè & Tìm kiếm Người dùng (Mục 6, 7, 8, 9)

*   **Ứng dụng tham khảo:** Tab "Friends" của Discord. Toàn bộ 4 yêu cầu 6, 7, 8, 9 được quy hoạch khoa học vào một màn hình duy nhất tại Cột 3.
*   **Thanh Header ngang (Pill / Tab Navigation):**

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ 👥 BẠN BÈ  |  [ Đang Online (5) ]  [ Tất cả (12) ]  [ Lời mời (2) ]  [ + Thêm bạn ] │
├─────────────────────────────────────────────────────────────────────────────┤
│ 🔍 Lọc theo họ tên hoặc @username...                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ DANH SÁCH BẠN BÈ HIỆN TẠI:                                                  │
│                                                                             │
│ [Avatar] Trần Tuấn Minh  @tuanminh_2026   [● Online]     [💬 Nhắn tin] [ ⋮ ]│
│ [Avatar] Lê Hoàng Nam    @nam_le          [● Online]     [💬 Nhắn tin] [ ⋮ ]│
│ [Avatar] Nguyễn Lan Anh  @lananh          [○ Offline]    [💬 Nhắn tin] [ ⋮ ]│
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Chi tiết 4 Tabs:
1.  **Tab 1 — Đang Online (Mục 8):**
    *   Bộ lọc tìm kiếm theo tên ở trên cùng.
    *   Chỉ hiển thị những bạn bè có trạng thái Online (chấm xanh lá).
    *   Mỗi dòng có: Avatar, Họ tên, Username, và **2 nút hành động nhanh**:
        *   Nút *"💬 Nhắn tin"*: Nhảy thẳng sang Cột 3 mở cuộc trò chuyện với người đó.
        *   Nút *"➕ Tạo nhóm nhanh"*: Mở popup tạo nhóm có điền sẵn thành viên này.
2.  **Tab 2 — Tất cả bạn bè (Mục 6):**
    *   Hiển thị toàn bộ bạn bè, phân biệt rõ chấm Online (xanh) / Offline (xám).
    *   Bên phải mỗi dòng có nút *"Nhắn tin"* và nút menu ngữ cảnh `⋮` chứa:
        *   *"Hủy kết bạn"* (Mục 6.c)
        *   *"Chặn người dùng (Block)"* (Mục 6.d — chữ màu đỏ cảnh báo, khi block sẽ không thể tìm kiếm hay kết bạn lại).
3.  **Tab 3 — Lời mời kết bạn (Mục 7):**
    *   Thanh lọc theo tên người gửi.
    *   Mỗi dòng yêu cầu kết bạn đến có 2 nút thao tác rõ ràng:
        *   Nút *"Đồng ý"* (Icon tích xanh ✓).
        *   Nút *"Từ chối"* (Icon dấu nhân đỏ ✕).
4.  **Tab 4 — Tìm người lạ / Thêm bạn (Mục 6.b & Mục 9):**
    *   Ô tìm kiếm lớn cho phép nhập Họ tên hoặc Tên đăng nhập (Backend truy vấn tự động loại trừ những người đã block mình).
    *   Kết quả trả về dưới dạng danh sách thẻ (Card list), mỗi thẻ có đủ **3 nút bấm độc lập**:
        *   [ + Kết bạn ] (Mục 6.b)
        *   [ 💬 Nhắn tin ngay ] (Mục 9)
        *   [ 👥 Tạo Group ] (Mục 9)

---

### 3.4 Cụm 4: Khung Chat, Danh sách Tối ưu & Thao tác Lịch sử (Mục 10, 11.a ➔ 11.g)

*   **Ứng dụng tham khảo:** Telegram Desktop kết hợp Zalo PC.
*   **Tối ưu hóa Hiệu năng (Virtualization Engine):**
    *   Sử dụng **`ListView<Message>`** (cơ chế VirtualFlow của JavaFX) thay cho việc gắn hàng trăm node vào `ScrollPane`. Chỉ có các tin nhắn đang hiển thị trên màn hình mới chiếm bộ nhớ RAM, giúp ứng dụng cuộn mượt mà 60fps kể cả khi có 10.000 dòng chat.

#### A. Tìm kiếm Toàn cục & Tự động Cuộn Highlight (Mục 11.f, 11.g)
*   Khi gõ từ khóa ở Cột 2, kết quả hiển thị 2 nhóm: *Liên hệ/Nhóm* và *Tin nhắn trong lịch sử*.
*   Khi click vào 1 kết quả tin nhắn:
    *   Khung chat gọi lệnh: `messageListView.scrollTo(targetIndex)`.
    *   Kích hoạt `Timeline` nháy màu nền của dòng `ListCell` đó từ vàng (`rgba(240, 178, 50, 0.35)`) về trong suốt trong `1.5 giây` giúp người dùng nhận diện ngay vị trí câu chat.

#### B. Header Khung Chat (Cột 3 — Trên cùng)
*   Hiển thị Avatar, Tên, trạng thái Online/Offline theo thời gian thực (Mục 11.a).
*   Góc phải chứa 3 nút công cụ:
    *   🔍 **Tìm trong cuộc trò chuyện (Mục 11.f):** Mở thanh tìm kiếm nhỏ kèm nút Next/Prev để nhảy giữa các câu chat khớp từ khóa.
    *   ⚠️ **Báo cáo Spam (Mục 10):** Icon cờ cảnh báo có micro-animation rung lắc. Click mở hộp thoại xác nhận báo cáo spam + ô nhập lý do gửi về Admin.
    *   ℹ️ **Thông tin hội thoại:** Đóng/mở Cột 4 (Drawer), nơi đặt nút đỏ *"Xóa toàn bộ lịch sử chat"* (Mục 11.d).

#### C. Chọn Xóa Một hoặc Nhiều Dòng Tin Nhắn (Mục 11.e)
*   **Cơ chế Chọn Xóa Nhiều Dòng:**
    *   Trong `ListCell`, đặt một `CheckBox` ở bên trái được bind thuộc tính:
        ```java
        checkBox.visibleProperty().bind(multiSelectModeProperty);
        checkBox.managedProperty().bind(multiSelectModeProperty);
        ```
    *   Khi biến `BooleanProperty multiSelectMode` bật lên `true`, toàn bộ các dòng chat sẽ đồng loạt trượt nhẹ sang phải nhường chỗ cho `CheckBox` hiện ra.
    *   Dưới đáy khung chat xuất hiện thanh công cụ nổi: *"Đã chọn [X] tin nhắn — [ Xóa tin đã chọn (Đỏ) ] [ Hủy ]"*.

---

### 3.5 Cụm 5: Trợ lý AI như "Một Thành viên trong Nhóm" (Mục 11.h)

*   **Quan điểm thiết kế quan trọng:**
    *   **AI được đối xử tự nhiên như một thành viên trong nhóm / danh bạ bạn bè**. Không cần mở cửa sổ kỳ lạ hay popup phức tạp.
    *   Tin nhắn của AI xuất hiện ngay trong luồng chat thông thường, kèm **Indicator nhận diện rõ ràng**:
        *   Avatar riêng: Biểu tượng Robot 🤖 hoặc Vòng tròn chuyển sắc Meta AI.
        *   Tên hiển thị: `Trợ lý AI` kèm huy hiệu `[AI BOT]` màu tím gradient.
        *   Bong bóng tin nhắn bot có viền trái chuyển sắc (`border-left: 4px solid #8B5CF6`) và nền tối sang trọng (`#2B2738`).

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ 💬 KHUNG HỘI THOẠI CÓ SỰ THAM GIA CỦA AI                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ [Avatar Minh] Trần Tuấn Minh  14:30                                         │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ Thầy yêu cầu phần mã hóa nhóm phải tham khảo Messenger kìa các bạn!     │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ [Avatar AI] 🤖 Trợ lý AI  [AI BOT]  14:31                                   │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ ┌─ Đang phản hồi tin nhắn của @Trần Tuấn Minh                           │ │
│ │ └───────────────────────────────────────────────────────────────────────┘ │
│ │ 💡 Theo yêu cầu của thầy, nhóm nên vẽ thêm icon ổ khóa 🔒 trên Header,  │ │
│ │    banner hệ thống thông báo mã hóa E2EE và trang xem Khóa bảo mật nhé!│ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ THANH NHẬP LIỆU META AI:                                                    │
│ [ Dải Smart Reply Chips ]:                                                  │
│   ( ✨ Tóm tắt đoạn chat )  ( 💬 Gợi ý trả lời nhanh )  ( ❓ Hỏi đáp tự do ) │
│ ┌───────────────────────────────────────────────────────────┬─────────────┐ │
│ │ [💬 Context: Cả đoạn chat ▼] Gõ /ai hoặc nhập tin nhắn... │ ✨ AI   Gửi │ │
│ └───────────────────────────────────────────────────────────┴─────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Các Chi tiết Tương tác AI Nâng cao:
1.  **3 Chế độ Tương tác Context-Aware:**
    *   *Chế độ 1 (Full-Context):* Đọc 5–10 tin nhắn gần nhất để tóm tắt hoặc gợi ý phương án cho cả nhóm.
    *   *Chế độ 2 (Single-Message Context):* Click chuột phải vào 1 tin ➔ Chọn *"✨ Hỏi AI về tin này"*, AI trích dẫn tin đó và phản hồi.
    *   *Chế độ 3 (Zero-Context):* Hỏi đáp kiến thức tổng quát độc lập.
2.  **Khung Nhập liệu Kích hoạt AI (`.ai-active`):**
    *   Khi người dùng gõ `/ai` hoặc click nút ✨ AI, khung nhập liệu áp dụng class CSS:
        ```css
        .chat-input-box.ai-active {
            -fx-border-color: linear-gradient(to right, #00C6FF, #7F00FF, #E100FF);
            -fx-border-width: 1.5px;
            -fx-border-radius: 20px;
            -fx-effect: dropshadow(gaussian, rgba(127, 0, 255, 0.35), 12, 0.2, 0, 0);
        }
        ```
3.  **Slash Command Menu (`/goi-y`, `/sua-loi`, `/dich`):**
    *   Dùng `PopupControl` không viền xuất hiện ngay phía trên ô gõ kèm hiệu ứng `FadeTransition` + `TranslateTransition` trượt nhẹ từ dưới lên `10px`.
4.  **Thẻ Gợi ý Ẩn của Bot (Ephemeral AI Card):**
    *   Thẻ hiển thị các `Button` gợi ý bo tròn (`-fx-background-radius: 999px;`) xếp trong `FlowPane` để người dùng click nhanh điền câu trả lời vào ô chat.

---

### 3.6 Cụm 6: Nhóm Chat & Mã hóa Đầu cuối kiểu Facebook Messenger (Mục 12.a ➔ 12.g)

*   **Ứng dụng tham khảo:** Facebook Messenger (theo đúng chỉ đạo của đề bài).

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ 💬 HEADER: 🔒 Nhóm Đồ Án Java (3 thành viên) - [Được bảo vệ mã hóa]         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│         ┌─────────────────────────────────────────────────────────┐         │
│         │ 🔒 Tin nhắn trong nhóm này được bảo vệ bằng mã hóa nhóm.│         │
│         │    Chỉ thành viên trong nhóm mới có khóa giải mã.       │         │
│         └─────────────────────────────────────────────────────────┘         │
│                                                                             │
│ [Avatar Hải] Lưu Chí Hải  10:15                                             │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ Script database.sql đã chạy hoàn tất 13 bảng trên PostgreSQL rồi nhé!   │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Chi tiết Tính năng & Ràng buộc Quyền hạn:
1.  **Hộp thoại Tạo nhóm (Mục 12.a):**
    *   Ô nhập Tên nhóm.
    *   Công tắc bật/tắt (ToggleSwitch): *"Bật mã hóa nhóm (E2EE Messenger Style)"*.
    *   Danh sách bạn bè kèm `CheckBox`. Nút *"Tạo nhóm"* chỉ kích hoạt khi đã chọn tối thiểu 1 người bạn.
2.  **Quản trị Phân quyền Admin ở Cột 4 (Mục 12.b, 12.c, 12.d, 12.e):**
    *   Tên nhóm kèm icon bút ✏️ để **Đổi tên nhóm** (12.b).
    *   Nút **"+ Thêm thành viên"** (12.c) mở danh sách thêm bạn bè.
    *   **Danh sách thành viên:**
        *   Mỗi dòng có nhãn **Admin** màu vàng nếu là quản trị viên.
        *   **Ràng buộc quyền hạn tuyệt đối (Binding):**
            ```java
            deleteBtn.visibleProperty().bind(isCurrentUserAdmin);
            deleteBtn.managedProperty().bind(isCurrentUserAdmin);
            ```
            Thành viên thường sẽ hoàn toàn không nhìn thấy hoặc không click được nút Xóa thành viên (12.e).
3.  **UI Mã hóa nhóm kiểu Facebook Messenger (Mục 12.g):**
    *   Biểu tượng **ổ khóa 🔒** cạnh tên nhóm ở Cột 2 và Header Cột 3. Khi bật E2EE, `ScaleTransition` chuyển icon sang màu xanh lá `#23A55A`.
    *   Banner hệ thống căn giữa ở đầu đoạn chat: *"🔒 Tin nhắn trong nhóm này được bảo vệ bằng mã hóa nhóm."*
    *   Mục *"Quyền riêng tư & Mã hóa"* trong Cột 4: Khi bấm vào sẽ mở popup hiển thị **Khóa bảo mật / Fingerprint** (dãy mã Hex nhận diện thiết bị các thành viên) giống hệt màn hình xác minh bảo mật của Messenger.

---

## 4. Ma trận Dữ liệu Mẫu (Dummy Data Matrix cho Báo cáo 20%)

Để phục vụ chụp ảnh toàn diện cho Báo cáo PDF mốc 20%, hệ thống sẽ nạp sẵn bộ dữ liệu mẫu sống động:

| Thực thể | Dữ liệu Giả lập Đổ sẵn trên UI |
| :--- | :--- |
| **Tài khoản đang đăng nhập** | `tuanminh_2026` — Trần Nguyễn Tuấn Minh (Online ●) |
| **Bạn bè Online** | • `chihai_2026` — Lưu Chí Hải (Online ●)<br>• `lananh_it` — Nguyễn Lan Anh (Online ●)<br>• `nam_le` — Lê Hoàng Nam (Online ●) |
| **Bạn bè Offline** | • `duc_huy` — Phạm Đức Huy (Offline ○)<br>• `thu_ha` — Vũ Thu Hà (Offline ○) |
| **Lời mời kết bạn** | • `quoc_bao` — Đặng Quốc Bảo (Chờ duyệt)<br>• `my_duyen` — Trần Mỹ Duyên (Chờ duyệt) |
| **Nhóm chat thường** | • 👥 Nhóm Thể Thao Cuối Tuần (4 thành viên) |
| **Nhóm chat có mã hóa 🔒** | • 🔒 Nhóm Đồ Án Java 2026 (Minh [Admin], Hải [Admin], Lan Anh) |
| **Trợ lý AI** | • 🤖 Trợ lý AI `[AI BOT]` — Trả lời tóm tắt & gợi ý tin nhắn trong hội thoại |

---

## 5. Danh mục File Mã nguồn Phân hệ User (`src/user/`)

Cấu trúc các lớp JavaFX sẽ được triển khai theo đúng chuẩn:

```text
src/user/
├── discord-theme.css          # Toàn bộ stylesheet CSS (Tree Layer Tokens, DatePicker, Meta AI glow, Bubbles)
├── JavaFXTheme.java           # Lớp tiện ích: Toggle mật khẩu con mắt, Animation Shake, Độ mạnh mật khẩu, Micro-animations
├── UserLoginApp.java          # Entry point JavaFX Application, điều phối chuyển cảnh Auth
├── UserLoginView.java         # Màn hình Đăng nhập (Mục 5)
├── UserRegisterView.java      # Màn hình Đăng ký 7 trường 2 cột (Mục 1)
├── UserForgotPasswordView.java# Màn hình Khởi tạo mật khẩu qua Email (Mục 3)
├── UserMainWorkspace.java     # Khung chính 4 Cột: Rail, Sub-Sidebar, Main View, Right Drawer
├── FriendsManagementView.java # Trung tâm Bạn bè 4 tabs chuẩn Discord (Mục 6, 7, 8, 9)
├── ChatConversationView.java  # Khung chat 1-1 & nhóm ListView tối ưu, tin nhắn AI bot, highlight vàng (Mục 10, 11, 12)
├── GroupManagementDrawer.java # Ngăn kéo Cột 4 quản lý thành viên, đổi tên, mã hóa E2EE (Mục 12)
└── UserSettingsDialog.java    # Modal 2 tab: Hồ sơ cá nhân & Đổi mật khẩu (Mục 2, 4)
```

---

## 6. Kết luận & Trạng thái

Tài liệu này là **Nguồn Chân lý Duy nhất (Single Source of Truth)** cho toàn bộ thiết kế giao diện phân hệ User của Thành viên B. Tất cả các màn hình sẽ tuân thủ nghiêm ngặt 100% các quy chuẩn, Tree Layer Model, Line Icon và Micro-animations trong tài liệu này trước khi bước vào lập trình logic.
