# ĐẶC TẢ GIAO DIỆN & TRẢI NGHIỆM NGƯỜI DÙNG — PHÂN HỆ USER (USER UI SPECIFICATION)

> **Dự án:** Java Chat System (Đồ án Lập trình ứng dụng Java)  
> **Phụ trách:** Thành viên B — Trần Nguyễn Tuấn Minh (MSSV: 24127084)  
> **Nền tảng công nghệ:** Java 21 LTS, JavaFX 21 + CSS (`theme.css`)  
> **Mô hình kiến trúc UI:** Tree Layer Model (Primitive ➔ Semantic ➔ Component Tokens)  
> **Thư viện Icon & Animation:** Line Icons (Lucide / Feather / Tabler via Ikonli & native `SVGPath`), GPU-accelerated Transitions & Micro-animations  
> **Tài liệu tham chiếu chuẩn:** [final-project.pdf](file:///d:/JavaChatProject/docs/final-project.pdf), [AGENTS.md](file:///d:/JavaChatProject/AGENTS.md), [tech-stack.md](file:///d:/JavaChatProject/docs/tech-stack.md), [plan.md](file:///d:/JavaChatProject/docs/plan.md)  
> **Phong cách chủ đạo:** Modern Obsidian Dark Surface (kết hợp Antigravity / Developer Tooling UI với bo góc có chọn lọc, Telegram Desktop, Notion AI và Messenger E2EE — Bản sắc thiết kế độc lập, không phụ thuộc hay sao chép Discord).

---

## 1. Mô hình Kiến trúc UI Ba Tầng (Tree Layer Model / Design Tokens)

Hệ thống giao diện được tổ chức theo chuẩn **Three-Layer Token Architecture** chuẩn ngành nhằm bảo đảm tính kế thừa, dễ bảo trì và phân lập ranh giới rõ ràng giữa giá trị thô và ngữ nghĩa hiển thị:

```text
┌────────────────────────────────────────────────────────────────────────┐
│  TẦNG 1: PRIMITIVE TOKENS (Giá trị thô: Bảng màu gốc, Thước đo, Lưới)   │
│  • Hex Colors (#141619, #0F1013, #4E6BF2...) • Spacing (4px, 8px...)   │
│  • Typography Scale (11px, 13px, 16px, 26px) • Timing (120ms, 200ms)  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  TẦNG 2: SEMANTIC TOKENS (Ý nghĩa ngữ nghĩa & Vai trò trong hệ thống)   │
│  • -bg-canvas, -bg-card, -bg-input • -accent-primary                   │
│  • -status-online, -status-danger • -text-normal, -text-muted          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│  TẦNG 3: COMPONENT-LEVEL TOKENS & STATES (Áp dụng cho từng thành phần) │
│  • .btn-primary:hover, .btn-primary:pressed • .input-error:focus       │
│  • .auth-card (14px), .text-field (9px), .feature-chip (10px)          │
└────────────────────────────────────────────────────────────────────────┘
```

### 1.1 Tầng 1 — Primitive Tokens (Thô)
*   **Color Primitives (Deep Obsidian & Tech Slate):**
    *   Dark Slates: `#0F1013`, `#141619`, `#1B1D22`, `#1E2026`, `#252830`, `#2A2D35`, `#333742`.
    *   Tech Sapphires: `#4E6BF2`, `#3D5AE0`, `#2E48C7`.
    *   Meta AI Gradients: `#00C6FF`, `#7F00FF`, `#E100FF`, `#8B5CF6`.
    *   Semantic Hues: Emerald Green `#23A55A`, Ruby Red `#FA777C` / `#DA373C`, Amber `#F0B232`, Action Blue `#4E6BF2`.
    *   Neutrals: Pure White `#FFFFFF`, High-Contrast White `#EDEDED`, Muted Slate `#8E929B`, Dark Border `#2A2D35`.
*   **Spacing Primitives (8-Point Grid):** `space-xs: 4px`, `space-sm: 8px`, `space-md: 12px`, `space-lg: 16px`, `space-xl: 24px`, `space-2xl: 36px`.
*   **Typography Scale (Chuẩn công thái học Modern Desktop, ClearType Subpixel LCD):**
    *   **Bộ font chuẩn hệ thống:** BẮT BUỘC dùng `-fx-font-family: "Segoe UI";` đơn nhất.
    *   **Giải pháp triệt tiêu chữ bị bể / mờ (Zero Rasterization Artifacts):**
        *   TUYỆT ĐỐI KHÔNG gắn `-fx-effect` trực tiếp lên `.auth-card` hoặc bất kỳ container cha nào chứa nhãn chữ (`Label`, `TextField`, `Button`). Trong engine Prism của JavaFX, bất kỳ node nào có `effect` sẽ bị rasterize toàn bộ node con thành bitmap texture off-screen, dẫn tới việc VÔ HIỆU HÓA công nghệ ClearType Subpixel antialiasing và làm font bị mờ, bể, răng cưa khi Windows scale DPI (125%/150%).
        *   Áp dụng CSS trực tiếp lên node Text: `.text { -fx-font-smoothing-type: lcd; }`.
        *   Aura phát sáng nhẹ nhàng được tách hoàn toàn sang node hậu cảnh (`ambientAura` hoặc canvas dưới cùng), bảo đảm thẻ Card giữ nguyên kết xuất DirectWrite nguyên bản sắc mịn 100%.
    *   `font-xs: 11px`: Huy hiệu nhỏ, credit.
    *   `font-sm: 12.5px - 13px`: Nhãn trường (`field-label`).
    *   `font-base: 14px`: Phụ đề (`header-subtitle`), mô tả tính năng Hero, liên kết (`link-action`), footer.
    *   `font-md: 14.5px - 15px`: Nội dung ô nhập (`text-field`, `password-field`, `combo-box`), tiêu đề tính năng Hero (`feature-chip-title`), chữ nút bấm (`btn-primary`).
    *   `font-xl: 26px`: Tiêu đề chính cửa sổ/card (`header-title`) và tiêu đề Hero (`hero-title`).
*   **Kích thước Chiều cao Chuẩn (Component Height Standard):**
    *   Chiều cao ô nhập & ComboBox: **`44px`** (chuẩn Fitts' Law & WCAG Touch/Click Target).
    *   Chiều cao nút bấm CTA: **`44px`**.
*   **Hệ thống Bo góc Có chọn lọc (Selective Corner Radius System):**
    *   Thẻ Card trung tâm (`.auth-card`): **`14px`** (Mềm mại, thanh lịch, tạo bản sắc riêng).
    *   Ô nhập liệu (`.text-field`, `.password-field`, `.combo-box`, `.date-picker`): **`9px`** (Bo góc mềm cao cấp theo ảnh tham chiếu thực tế).
    *   Nút bấm hành động (`.btn-primary`): **`8px`** (Phản hồi xúc giác hiện đại).
    *   Thẻ tính năng Hero (`.feature-chip`): **`10px`** (Tham chiếu sidebar item hiện đại).
    *   Phân đoạn mật khẩu (`.strength-segment`): **`2.5px`**.
    *   Menu thả xuống (`.combo-box-popup .list-view`): **`8px`**.
*   **Motion Timing Primitives:** `duration-instant: 80ms`, `duration-fast: 120ms`, `duration-normal: 200ms`, `duration-smooth: 300ms`.

### 1.2 Tầng 2 — Semantic Tokens (Ngữ nghĩa)
*   `-bg-canvas`: Nền canvas tổng thể của ứng dụng (`#141619` — Obsidian Dark).
*   `-bg-card`: Bề mặt các thẻ Card nổi, Drawer, Dialog (`#1B1D22` — Gunmetal Dark).
*   `-bg-input`: Bề mặt các trường nhập liệu (`#0F1013` — Pure Obsidian).
*   `-bg-nav-rail`: Thanh điều hướng mỏng 60px ngoài cùng bên trái (`#0F1013`).
*   `-accent-primary`: Màu chủ đạo (`#4E6BF2` — Sapphire Blue).
*   `-accent-hover`: Trạng thái hover của màu chủ đạo (`#3D5AE0`).
*   `-status-online`: Màu báo trực tuyến và mật khẩu an toàn (`#23A55A`).
*   `-status-offline`: Màu báo ngoại tuyến (`#80848E`).
*   `-status-danger`: Màu cảnh báo, nút Chặn, nút Xóa, viền lỗi (`#FA777C` / `#DA373C`).
*   `-text-normal`: Chữ chính độ tương phản cao (> 7:1 đạt WCAG AAA) (`#EDEDED`).
*   `-text-muted`: Chữ phụ, ghi chú, placeholder (`#8E929B`).
*   `-text-link`: Liên kết có thể click (`#4E6BF2`).
*   `-border-subtle`: Viền mỏng phân tách bề mặt (`#2A2D35`).
*   `-bubble-sent`: Nền bong bóng tin nhắn người gửi (`#4E6BF2`).
*   `-bubble-received`: Nền bong bóng tin nhắn người nhận (`#252830`).

### 1.3 Tầng 3 — Component-Level Tokens & Trạng thái (Bản sắc Riêng & Anti-Slop)
*   `.btn-primary`: Nền phẳng `-accent-primary` (`#4E6BF2`), bo góc **`8px`**, chữ trắng đậm **`15px`**, chiều cao **`44px`**. Khi `:hover` chuyển sang `#3D5AE0` trong `120ms`, khi `:pressed` thụt nhẹ `0.98x`.
*   `.text-field`, `.password-field`, `.combo-box`, `.date-picker`: Nền phẳng obsidian `-bg-input` (`#0F1013`), bo góc mềm **`9px`**, viền mờ `1px solid #2A2D35`, cỡ chữ **`15px`**, chiều cao **`44px`**, đệm **`10px 14px`**. Khi `:focused` hiện viền sắc nét `-accent-primary` (`#4E6BF2`).
*   `.field-label`: Cỡ chữ **`12.5px`**, chữ đậm (`bold`), màu `#A0A5B0`, dễ đọc và tương phản rõ.
*   `.header-title`: Cỡ chữ **`26px`**, chữ đậm (`bold`), màu `#EDEDED`.
*   `.header-subtitle`: Cỡ chữ **`14px`**, màu `#8E929B`, khoảng cách dòng thoáng đãng.
*   `.link-action`: Cỡ chữ **`13.5px - 14px`**, màu `#4E6BF2`, không gạch chân (hover gạch chân).
*   `.auth-card`: Thẻ Card bề mặt phẳng `-bg-card` (`#1B1D22`), bo góc tinh tế **`14px`**, viền mỏng 1px `#2A2D35`, đệm rộng rãi **`36px 40px`**. Không gán `-fx-effect` trực tiếp lên card để tránh rasterization làm bể font chữ.
*   **Hiệu ứng Vầng sáng Ambient Light (Card-Originating Aura):** Thay vì đặt một đốm sáng tĩnh ở giữa toàn cửa sổ, vầng sáng Ambient Aura (`.card-ambient-aura`) được gắn động trực tiếp phát xuất từ phía sau từng thẻ đăng nhập / đăng ký (`.auth-card`). Vầng hào quang bao phủ êm ái quanh form (`RadialGradient` dải màu `rgba(78, 107, 242, 0.16)` lan tỏa dịu mắt ra `transparent` kết hợp `GaussianBlur`), mở rộng `40px - 50px` quanh 4 mép thẻ. Nhờ gắn trực tiếp vào view, dù ở chế độ thu gọn hay toàn màn hình hai cột, ánh sáng viền luôn phát xuất chính xác từ phía sau form xác thực mà vẫn bảo toàn độ sắc nét 100% của chữ.
*   `.hero-brand-section`: Khung thông tin tối giản thanh lịch cột trái khi Fullscreen (chiều rộng cửa sổ ≥ 1000px). Bỏ hoàn toàn các box hình oval/pill (loại bỏ biểu hiện AI slop). Cỡ chữ được chuẩn hóa tương đương bên Đăng ký / Đăng nhập: Tiêu đề **`26px`** bold, Phụ đề **`14px`**, Tiêu đề tính năng **`14.5px`** bold, Nội dung tính năng **`13.5px`**. Nội dung rút gọn cô đọng, sắc nét, tương phản chuẩn WCAG AAA.
*   `.feature-chip`: Thẻ thông tin phẳng hình chữ nhật bo góc **`10px`** (chuẩn phong cách Developer Tooling), nền `#1E2026`, viền mỏng `1px solid #2D313A`, đệm `12px 16px`. Chữ trắng sáng `#EDEDED` và xám sáng `#A0A5B0` tương phản rõ ràng trên nền tối.
    *   **Thư viện Icon Vector Tối giản Đơn sắc Trắng (Monochromatic Pure White Line Icons):** Không dùng màu mè sặc sỡ, duy trì duy nhất 1 tông màu trắng sắc nét thuần túy (`#FFFFFF`), lấy từ thư viện icon chuẩn Feather/Lucide vector:
        *   Mã hóa AES-GCM: Vector Khóa bảo mật `Lock` màu trắng `#FFFFFF`.
        *   TCP Socket thuần: Vector Tia sét `Zap` màu trắng `#FFFFFF`.
        *   Quản lý hội thoại: Vector Người dùng `Users` màu trắng `#FFFFFF`.
        *   Hộp chứa Icon Badge (`38x38px`, bo góc `8px`): Sử dụng nền xám đen trung tính thanh lịch `rgba(255, 255, 255, 0.05)`, viền mảnh `1px solid rgba(255, 255, 255, 0.08)`, làm nổi bật icon trắng tinh khôi chuẩn minimalist tooling.
*   `.input-error`: Viền đỏ cảnh báo `-status-danger` (`#FA777C`), không lạm dụng hiệu ứng nhấp nháy neon.
*   `.strength-bar`: Thanh đo độ mạnh mật khẩu **4 phân đoạn (4-Segment Segmented Meter)**, mỗi phân đoạn cao **`5px`**, bo góc `2.5px`, cách nhau `4px`.
    *   Trạng thái 0%: Cả 4 phân đoạn màu track `#1E2026`.
    *   Trạng thái Yếu (< 40%): Phân đoạn 1 đỏ `#FA777C`, 3 phân đoạn còn lại `#1E2026`.
    *   Trạng thái Trung bình (40-69%): Phân đoạn 1 và 2 màu vàng hổ phách `#F0B232`, 2 phân đoạn còn lại `#1E2026`.
    *   Trạng thái Khá (70-89%): Phân đoạn 1, 2, 3 màu Sapphire Blue `#4E6BF2`, phân đoạn 4 `#1E2026`.
    *   Trạng thái Rất an toàn (≥ 90%): Cả 4 phân đoạn màu xanh lá `#23A55A`.
*   `.strength-label`: Nhãn mô tả độ mạnh cỡ **`12.5px`**, đồng bộ màu với các phân đoạn.
*   `.combo-box-popup`: Menu danh sách thả xuống Matte Dark triệt để: Nền cố định `#0F1013`, viền `1px solid #2A2D35`, bo góc **`8px`**, bóng đổ độ sâu `dropshadow(three-pass-box, rgba(0, 0, 0, 0.45), 14, 0, 0, 6)`.
    *   **Khắc phục lỗi trùng lặp khi hover:** Sử dụng cơ chế lắng nghe trạng thái `hoverProperty()` và `selectedProperty()` của từng ô `ListCell` độc lập, bảo đảm khi hover ô sẽ highlight viền hộp bo góc 6px với nền `#262932` và chữ `#FFFFFF`, khi không hover trả về trong suốt (chữ `#EDEDED`), khi selected mang nền `#4E6BF2` đậm nét; các lựa chọn phân tách rõ rệt, không bị chồng chéo hay xung đột màu.
    *   **Hoạt ảnh Dropdown chuẩn xác:** Truy xuất trực tiếp `ComboBoxListViewSkin.getPopupContent()`, kích hoạt hoạt ảnh `FadeTransition` (0.0 ➔ 1.0) kết hợp `TranslateTransition` trượt nhẹ xuống `8px` trong `150ms` mượt mà 60fps khi người dùng bấm mở menu.

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

*   **Ngôn ngữ Thiết kế:** Modern Obsidian Dark Surface (Bản sắc độc lập, phong cách công cụ chuyên nghiệp, tương phản cao, bo góc có chọn lọc).
*   **Nguyên tắc Thiết kế Cốt lõi (Strict Anti-Slop Rules):**
    *   **Loại bỏ hoàn toàn viền Neon & Gradient tràn lan:** Không dùng đường kẻ viền neon sặc sỡ, không bo tròn quá đà làm component bị "giả tạo" (AI slop).
    *   **Thẻ Card & Bề mặt phẳng trầm:** Sử dụng bề mặt matte phẳng `-bg-card` (`#1B1D22`), bo góc thanh lịch **`14px`**, viền mỏng tinh tế `1px solid #2A2D35`.
    *   **Aura Gradient Nhẹ Nhàng (Subtle Atmospheric Aura):** Ánh sáng hào quang chỉ đóng vai trò là một vầng khuếch tán cực kỳ dịu mắt ở hậu cảnh (`rgba(78, 107, 242, 0.08)`), mang lại chiều sâu không gian tĩnh tại, tuyệt đối không làm chói mắt hay tạo viền phát sáng gắt.
    *   **Khắc phục triệt để chữ bị bể (Rasterization Artifacts):** Tách biệt bóng đổ khỏi thẻ chứa văn bản, kích hoạt ClearType Subpixel DirectWrite giúp mọi chữ viết (`Label`, `TextField`, `Button`) hiển thị sắc nét 100%, không bị vỡ hạt do raster texture cache của JavaFX.
    *   **Không dùng Icon hộp thừa thãi & Box Oval:** Bỏ hoàn toàn các icon trang trí đặt trong hộp viền bo tròn ở đầu form; bỏ hoàn toàn các box hình oval/pill ở cột Hero. Tiêu đề đứng độc lập, sắc nét, tự tin.
*   **Cơ chế Thích ứng Hai Trạng Thái (Dual-State Responsive Architecture):**
    *   **Trạng thái 1 — Thu gọn / Hộp thoại tập trung (Compact Mode — Chiều rộng < 1000px):**
        *   Cửa sổ hộp thoại tập trung chuẩn (`540 x 600` cho Login/Forgot, `820 x 750` cho Register).
        *   Thẻ Card căn giữa tuyệt đối, ôm khít nội dung (`setMaxHeight(USE_PREF_SIZE)`), không có khoảng trống thừa.
    *   **Trạng thái 2 — Mở rộng / Toàn màn hình (Expanded / Fullscreen Mode — Chiều rộng ≥ 1000px):**
        *   Tự động kích hoạt **Bố cục Chia Hai Cột (Split-Screen Layout 45% - 55%)**:
            *   **Cột Trái (Hero Branding Showcase — 45%):** Khung giới thiệu tối giản, thanh lịch: Tiêu đề *"Java Chat System"* (26px bold), phụ đề cô đọng *"Giao tiếp thời gian thực bảo mật cao cho máy tính để bàn."* (14px), và 3 thẻ tính năng hình chữ nhật bo góc **`10px`** (tiêu đề 14.5px bold `#EDEDED`, mô tả 13.5px `#8E929B`) tương phản cao với nền `#1E2026`.
            *   **Cột Phải (Auth Form Container — 55%):** Thẻ Card đăng nhập / đăng ký căn giữa hoàn hảo.
            *   **Hậu cảnh Ambient:** Lớp nền tối sâu (`#141619`) kết hợp vầng sáng Aura mờ nhạt tự nhiên.

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│               CHẾ ĐỘ FULLSCREEN (SPLIT-SCREEN MODERN OBSIDIAN DARK)                    │
│                                                                                        │
│   [CỘT TRÁI: HERO SHOWCASE (BO GÓC 10PX, TƯƠNG PHẢN CAO)]│ [CỘT PHẢI: AUTH FORM CARD]  │
│                                                          │                             │
│   (Vầng Aura khuếch tán cực nhẹ ~8% ở hậu cảnh)          │ ┌─────────────────────────┐ │
│                                                          │ │ 1px subtle (#2A2D35)    │ │
│   JAVA CHAT SYSTEM (26px Bold)                           │ │ Bo góc 14px thanh lịch  │ │
│   Giao tiếp thời gian thực bảo mật cao cho desktop. (14px│ │ Đệm 36px-40px thoáng    │ │
│                                                          │ │                         │ │
│   ┌──────────────────────────────────────────────────┐   │ │    Chào mừng trở lại!   │ │
│   │ 🔒 Mã hóa AES-GCM (14.5px Bold)                  │   │ │    (26px Bold)          │ │
│   │    Khóa riêng từng phiên chat, an toàn dữ liệu.  │   │ │                         │ │
│   ├──────────────────────────────────────────────────┤   │ │ TÊN ĐĂNG NHẬP HOẶC...   │ │
│   │ ⚡ TCP Socket thuần (14.5px Bold)                 │   │ │ [ tuanminh_2026 (44px)] │ │
│   │    Độ trễ thấp, kết nối liên tục không đứt đoạn. │   │ │                         │ │
│   ├──────────────────────────────────────────────────┤   │ │ MẬT KHẨU *              │ │
│   │ 👥 Quản lý hội thoại (14.5px Bold)               │   │ │ [ •••••••••••• (44px) 👁]│ │
│   │    Nhắn tin trực tiếp, nhóm chat & bạn bè online.│   │ │                         │ │
│   └──────────────────────────────────────────────────┘   │ │ [   ĐĂNG NHẬP (44px)  ] │ │
│                                                          │ └─────────────────────────┘ │
└──────────────────────────────────────────────────────────┴─────────────────────────────┘
```

#### A. Màn hình Đăng nhập (Mục 5)
*   **Tỷ lệ & Kích thước:**
    *   Chiều rộng thẻ Card: Chuẩn **`450px`**, `setMaxHeight(USE_PREF_SIZE)`.
    *   Khoảng đệm nội bộ (`padding`): **`36px 40px`**, bo góc **`14px`**, viền mỏng **`#2A2D35` 1px**.
    *   Khoảng cách trường (`spacing`): **`16px`**, không có icon hộp thừa ở đầu form.
    *   Kích thước cửa sổ mặc định: **`540 x 600`**.
*   **Các trường & thành phần:**
    1. Header: Tiêu đề: *"Chào mừng trở lại!"* (Segoe UI, bold **`26px`**, `#EDEDED`), Phụ đề: *"Rất vui được gặp lại bạn một lần nữa!"* (`#8E929B`, **`14px`**).
    2. Ô Tên đăng nhập hoặc Email (`TextField`, nền `#0F1013`, bo góc mềm **`9px`**, cỡ chữ **`15px`**, cao **`44px`**, padding `10px 14px`).
    3. Ô Mật khẩu (`PasswordFieldWrapper`, nền `#0F1013`, bo góc mềm **`9px`**, cỡ chữ **`15px`**, cao **`44px`**, padding `10px 14px`): Có nút con mắt Line Icon 👁 ẩn/hiện tinh tế.
    4. Nút bấm *"Đăng nhập"* (màu Sapphire Blue `#4E6BF2` phẳng, bo góc **`8px`**, chữ đậm **`15px`**, cao **`44px`**).
    5. Link *"Quên mật khẩu?"* (`#4E6BF2`, cỡ **`13.5px`**, không gạch chân, hover có gạch chân).
    6. Link *"Cần một tài khoản? Đăng ký"* (`#4E6BF2`, cỡ **`13.5px`**).
*   **Dữ liệu mẫu điền sẵn:** `tuanminh_2026`, `Password@123`.

#### B. Màn hình Đăng ký (Mục 1)
*   **Tỷ lệ:** Thẻ Card rộng **`680px`** (mỗi cột rộng ~300px thoáng đãng), padding **`36px 40px`**, bo góc **`14px`**, viền `1px solid #2A2D35`, chia **GridPane 2 Cột đối xứng (50% - 50%)**:
    *   Tiêu đề: *"Tạo một tài khoản"* (bold **`26px`**), Phụ đề: *"Điền đầy đủ thông tin bên dưới để bắt đầu kết nối!"* (**`14px`**).
    *   **Cột 1 (Thông tin tài khoản & cư trú):**
        *   Tên đăng nhập (`TextField`, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px)
        *   Email (`TextField`, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px)
        *   Địa chỉ (`TextField`, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px)
        *   Mật khẩu (`PasswordFieldWrapper`, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px có nút con mắt 👁)
        *   Thanh đo độ mạnh mật khẩu (4-Segment Meter: 4 phân đoạn 5px bo tròn 2.5px, chuyển sắc từ Đỏ sang Vàng, Sapphire Blue, Xanh lá kèm nhãn *"Độ mạnh: Rất an toàn ✓"*, chữ 12.5px)
    *   **Cột 2 (Thông tin cá nhân & xác thực):**
        *   Họ và tên (`TextField`, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px)
        *   Ngày sinh (JavaFX `DatePicker`, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px)
        *   Giới tính (`ComboBox<String>`: Nam, Nữ, Khác, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px)
        *   Xác nhận mật khẩu (`PasswordFieldWrapper`, nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px có nút con mắt 👁)
*   Nút CTA: *"Đăng ký tài khoản"* (full-width, solid Sapphire Blue `#4E6BF2`, bo góc **`8px`**, cao 44px, chữ đậm 15px).
*   Footer: *"Đã có tài khoản? Đăng nhập"* (`.link-action`, cỡ 13.5px).
*   Kích thước cửa sổ mặc định: **`820 x 750`**.

#### C. Màn hình Khởi tạo lại mật khẩu (Mục 3)
*   **Tỷ lệ:** Form tối giản trong card **`450px`**, padding **`36px 40px`**, bo góc **`14px`**, viền `1px solid #2A2D35`.
*   **Thành phần:**
    *   Tiêu đề: *"Khởi tạo lại mật khẩu"* (bold **`26px`**, `#EDEDED`), Phụ đề: *"Nhập email hoặc tên đăng nhập của bạn để nhận mật khẩu mới."* (**`14px`**, `#8E929B`).
    *   Ô nhập Email hoặc Tên đăng nhập (nền `#0F1013`, bo góc **`9px`**, cao 44px, chữ 15px).
    *   Thanh tiến trình `ProgressBar` ẩn ban đầu.
    *   Nút *"Gửi mật khẩu mới qua Email"* (solid Sapphire Blue `#4E6BF2`, bo góc **`8px`**, cao 44px, chữ 15px).
    *   Link *"← Quay lại Đăng nhập"* (`.link-action`, cỡ 13.5px).
*   Kích thước cửa sổ mặc định: **`540 x 600`**.

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
