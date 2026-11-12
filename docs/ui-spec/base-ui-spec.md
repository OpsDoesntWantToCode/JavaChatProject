# QUY CHUẨN NỀN TẢNG GIAO DIỆN PHÂN HỆ USER (GLOBAL UI BASE SPECIFICATION)

> **Cấp bậc tài liệu:** TÀI LIỆU CHA NỀN TẢNG (PARENT SPECIFICATION)  
> **Kiến trúc quản trị:** Three-Tier Design Tokens Architecture (W3C Design Tokens Community Group Standard)  
> **Phạm vi áp dụng:** Toàn bộ thành phần giao diện JavaFX và stylesheet CSS thuộc phân hệ User (`src/user/`)  
> **Mục tiêu:** Định hình một ngôn ngữ thiết kế nhất quán, tinh gọn, hiệu năng cao và có khả năng mở rộng theo chuẩn công nghệ máy tính để bàn hiện đại.

---

## 1. Mô hình Kiến trúc Token Ba Tầng (Three-Tier Token Architecture)

Hệ thống giao diện tuân thủ nghiêm ngặt mô hình phân tầng chuẩn công nghiệp quốc tế:

```text
┌────────────────────────────────────────────────────────────────────────┐
│ TẦNG 1: PRIMITIVE TOKENS (Giá trị thô / Global Reference Tokens)       │
│ • Bảng màu gốc (Slate, Sapphire, Status) • Thang kích thước khoảng cách │
│ • Thang đo Typography (Segoe UI) • Bán kính bo góc • Thời lượng motion │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ (Ánh xạ có ngữ nghĩa)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ TẦNG 2: SEMANTIC TOKENS (Token Ngữ nghĩa / System Alias Tokens)        │
│ • bg.canvas, bg.surface, bg.input, bg.hover • action.primary           │
│ • text.primary, text.secondary, text.link • border.subtle              │
│ • status.success, status.danger, status.warning, status.neutral        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ (Kế thừa vào thành phần)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ TẦNG 3: COMPONENT TOKENS (Token Thành phần / Scoped Component Tokens)  │
│ • auth-card.*, text-field.*, btn-primary.*, feature-chip.*             │
│ • chat-bubble.sent.*, chat-bubble.received.*, strength-meter.*         │
│ • elevation-surface.*, icon-badge.*                                    │
└────────────────────────────────────────────────────────────────────────┘
```

Mọi tài liệu đặc tả màn hình (như [user-screens.md](user-screens.md)) đều kế thừa trực tiếp từ hệ thống Token này; không tự ý tạo ra các thông số biến thể nằm ngoài danh mục.

---

## 2. Hệ Thống Token Hợp Nhất (Design Tokens Map)

Bảng ánh xạ trực tiếp từ Primitive Token (Giá trị thô) sang Semantic Token (CSS trong `theme.css`):

| Nhóm | Biến CSS (`theme.css`) | Primitive Token | Giá trị Hex | Ứng dụng công thái học |
| :--- | :--- | :--- | :---: | :--- |
| **Bề mặt** | `-bg-canvas` | `color.slate.900` | `#141619` | Nền tĩnh ngoài cùng của cửa sổ |
| | `-bg-card` | `color.slate.850` | `#1B1D22` | Bề mặt thẻ Card, Modal, Sidebar, Drawer |
| | `-bg-input` / `-bg-nav-rail` | `color.slate.950` | `#0F1013` | Nền chìm cho ô nhập liệu và Nav Rail 60px |
| | `-bg-chip` | `color.slate.800` | `#1E2026` | Bề mặt nút Smart Chip, thẻ tính năng Hero |
| | `-bg-item-hover` | `color.slate.700` | `#262932` | Trạng thái hover cell danh sách |
| **Hành động** | `-accent-primary` / `-text-link` | `color.sapphire.500`| `#4E6BF2` | Sắc xanh chủ đạo, nút CTA, link điều hướng |
| | `-accent-hover` | `color.sapphire.600`| `#3D5AE0` | Trạng thái hover nút CTA |
| | `-accent-active` | `color.sapphire.700`| `#2E48C7` | Trạng thái nhấn giữ nút CTA |
| | `-accent-ai` | `color.violet.500` | `#8B5CF6` | Điểm nhấn Trợ lý AI và huy hiệu [AI BOT] |
| **Trạng thái** | `-status-online` | `color.emerald.500` | `#23A55A` | Trực tuyến, thành công, an toàn E2EE |
| | `-status-danger` | `color.ruby.500` | `#FA777C` | Báo lỗi, nút xóa/rời nhóm, sao đỏ bắt buộc |
| | `-status-warning`| `color.amber.500` | `#F0B232` | Cảnh báo, huy hiệu [Admin] nhóm |
| | `-status-offline`| `color.neutral.500`| `#80848E` | Ngoại tuyến, trạng thái trung tính |
| **Typography** | `-text-normal` | `color.neutral.100`| `#EDEDED` | Chữ chính đạt tương phản cao WCAG AAA |
| | `-text-muted` | `color.neutral.400`| `#8E929B` | Chữ phụ trợ, subtitle, placeholder |
| | `-text-white` | `color.neutral.000`| `#FFFFFF` | Trắng tinh khiết (Icon Lucide, text CTA) |
| **Đường viền** | `-border-subtle` | `color.slate.600` | `#2A2D35` | Đường viền hairline tiêu chuẩn 1px |
| | `-border-hover` | `color.slate.500` | `#383D48` | Đường viền tương tác hover/focus |
| **Chat Bubbles**| `-bubble-sent` | `color.sapphire.500`| `#4E6BF2` | Bong bóng tin gửi đi (căn phải) |
| | `-bubble-received` | `color.slate.750` | `#1E2128` | Bong bóng tin nhận được (căn trái) |
| | `-bg-ai-bubble` | `color.slate.820` | `#2B2738` | Bong bóng tin Trợ lý AI (viền trái tím) |

---

## 3. Tầng 3: Component-Level Tokens & Tiêu chuẩn Thành phần

### 3.1 Thẻ Card Chính (`.auth-card`) & Phân tầng Bề mặt (Layered Surface Elevation)
*   **Tokens:** `background: -bg-card`, `border: 1px solid -border-subtle`, `radius: radius.2xl (14px)`, `padding: space.9 space.10 (36px 40px)`.
*   **Nguyên tắc Phân tầng Độ sâu Bề mặt (Layered Surface Elevation):**
    *   Thay thế hoàn toàn các vầng sáng mờ ảo hoặc viền neon trang trí (vốn là biểu hiện của AI slop không có giá trị công năng) bằng **hệ thống phân tầng bề mặt tinh tế** thông qua các bước sáng tự nhiên kết hợp đường viền hairline:
        *   **Tầng 0 (Background Canvas):** `-bg-canvas: #141619` — Nền tĩnh của cửa sổ.
        *   **Tầng 1 (Sub-Panel / Inputs):** `-bg-input: #0F1013` — Bề mặt chìm nhẹ, tạo độ tập trung khi gõ.
        *   **Tầng 2 (Surface Card / Modal):** `-bg-card: #1B1D22` — Bề mặt nổi nhẹ, phân tách bằng đường viền mỏng `1px solid -border-subtle (#2A2D35)`.
        *   **Tầng 3 (Floating Popups / Menus):** `background: #22252C`, bo góc `8px`, bóng đổ độ sâu nhẹ nhàng `dropshadow(three-pass-box, rgba(0, 0, 0, 0.45), 14, 0, 0, 6)`.
    *   **Bảo vệ độ sắc nét font chữ (Zero Rasterization):** Tuyệt đối không gắn `-fx-effect` trực tiếp lên thẻ card hoặc bất kỳ container chứa chữ. Giữ nguyên công nghệ DirectWrite Subpixel LCD ClearType nguyên bản.

### 3.2 Trường Nhập Liệu (`.text-field`, `.password-field`, `.combo-box`, `.date-picker`)
*   **Tokens:** `height: 44px`, `background: -bg-input`, `border: 1px solid -border-subtle`, `radius: radius.lg (9px)`, `padding: space.2 space.3 (8px 14px)`, `font-size: 15px`.
*   **Trạng thái Focus:** `border: 1px solid -accent-primary`, `effect: dropshadow(three-pass-box, rgba(78, 107, 242, 0.35), 8, 0, 0, 0)`.
*   **Trạng thái Lỗi (`.input-error`):** `border: 1px solid -status-danger`, `effect: dropshadow(three-pass-box, rgba(250, 119, 124, 0.35), 8, 0, 0, 0)`.

### 3.3 Nút Bấm Chính (`.btn-primary`)
*   **Tokens:** `height: 44px`, `background: -accent-primary`, `radius: radius.md (8px)`, `font-size: 15px`, `font-weight: bold`, `text-fill: -text-white`.
*   **Tương tác:** Hover sang `-accent-hover` (`120ms`), Pressed sang `-accent-active` (`80ms`).

### 3.4 Icon Vector Chuẩn Hóa
*   **Hình thức:** Vector `SVGPath` lấy từ hệ thống thư viện **Lucide / Feather**.
*   **Màu sắc:** Cố định màu trắng thuần túy (`#FFFFFF`), áp dụng quy tắc cưỡng bức CSS:
    ```css
    .feature-chip-icon, .feature-chip SVGPath, .hero-logo-box SVGPath, .hero-logo-icon {
        -fx-fill: #FFFFFF !important;
    }
    ```
*   **Hộp chứa Icon Badge (`iconBadge`):** Kích thước cố định `38x38px`, bo góc `8px`, nền `rgba(255, 255, 255, 0.08)`, viền `1px solid rgba(255, 255, 255, 0.12)`.

---

## 4. Quy Chuẩn Không Gian & Khung Màn Hình (Layout & Responsive Standards)

Ứng dụng desktop hỗ trợ 2 chế độ hiển thị thích ứng mượt mà:

1.  **Chế độ Thu gọn / Hộp thoại Tập trung (Compact Mode — Viewport < 1000px):**
    *   Kích thước cửa sổ chuẩn Fitts' Law: `540 x 600` (Đăng nhập / Quên mật khẩu), `820 x 750` (Đăng ký 2 cột).
    *   Thẻ xác thực căn giữa tuyệt đối, ôm sát nội dung (`setMaxHeight(USE_PREF_SIZE)`).
2.  **Chế độ Mở rộng / Toàn màn hình (Expanded Mode — Viewport ≥ 1000px):**
    *   Kích hoạt bố cục phân chia 2 cột đối trọng:
        *   **Cột Trái (Hero Branding Showcase — 45%):** Logo, tiêu đề thương hiệu, 3 thẻ tính năng chữ nhật bo góc `10px` với icon vector trắng.
        *   **Cột Phải (Form Container — 55%):** Thẻ xác thực căn giữa, bề mặt Gunmetal `-bg-card` nổi bật tự nhiên trên nền canvas.
3.  **Khung Không Gian Làm Việc Chính (Main Workspace Shell — 4 Cột):**
    *   **Cột 1 (Navigation Rail):** Cố định `60px` (Chỉ vẽ viền phải: `-fx-border-color: transparent #2A2D35 transparent transparent;`).
    *   **Cột 2 (Sub-Sidebar):** Linh hoạt `280px – 320px` (Chỉ vẽ viền phải: `-fx-border-color: transparent #2A2D35 transparent transparent;`).
    *   **Cột 3 (Main Content View):** Co giãn tự do (`Priority.ALWAYS`), không vẽ viền ngoài. Header chỉ vẽ viền đáy, Thanh nhập liệu dưới đáy chỉ vẽ viền đỉnh.
    *   **Cột 4 (Right Drawer):** Cố định `280px` (Chỉ vẽ viền trái: `-fx-border-color: transparent transparent transparent #2A2D35;`).
    *   **Quy tắc Single Border Source:** Tuyệt đối không để hai component liền kề cùng vẽ đường viền tiếp giáp nhau (tránh viền kép 2px thô kệch).

---

## 5. Sáu Nguyên Tắc Kỹ Thuật Thực Tiễn Chống AI Slop (Anti-Slop Engineering Principles)

Nhằm đảm bảo sản phẩm mang bản sắc công cụ máy tính để bàn chuyên nghiệp, đáng tin cậy và không mang dáng dấp "ý tưởng đồ họa AI hời hợt":

1.  **Phân Tầng Bề Mặt & Viền Hairline (Layered Surface Elevation & Hairline Borders):**
    *   *Vấn đề AI slop:* Dùng vầng sáng neon hoặc gradient phát quang lung linh che lấp bố cục yếu kém.
    *   *Chuẩn kỹ thuật:* Xây dựng độ sâu không gian bằng sự chênh lệch độ sáng tinh tế giữa các lớp bề mặt (`#0F1013` ➔ `#141619` ➔ `#1B1D22` ➔ `#22252C`) kết hợp đường viền hairline sắc nét `1px solid #2A2D35`.
2.  **Vòng Tiêu Điểm Bàn Phím Công Thái Học (Focus-Visible Keyboard Rings — WCAG 2.2):**
    *   *Vấn đề AI slop:* Bỏ quên thao tác bàn phím, xóa bỏ focus ring khiến người dùng phím bị lạc lối.
    *   *Chuẩn kỹ thuật:* Khi chuyển tab bàn phím, ô nhập và nút bấm hiển thị viền tiêu điểm sắc nét `1px solid -accent-primary` kèm vòng ngoài `dropshadow(three-pass-box, rgba(78, 107, 242, 0.35), 8, 0, 0, 0)`.
3.  **Trạng Thái Dữ Liệu Rỗng Có Ý Nghĩa (Meaningful Empty States & Contextual Cues):**
    *   *Vấn đề AI slop:* Màn hình trắng trơn hoặc danh sách trống trơ trọi khi chưa có tin nhắn / bạn bè.
    *   *Chuẩn kỹ thuật:* Mọi danh sách rỗng đều có icon vector tối giản, tiêu đề hướng dẫn rõ ràng và nút hành động gợi ý tiếp theo (ví dụ: *"Chưa có tin nhắn nào trong cuộc trò chuyện này. Hãy gửi lời chào đầu tiên!"*).
4.  **Mật Độ Thông Tin Công Thái Học (Ergonomic Information Density & Hierarchy):**
    *   *Vấn đề AI slop:* Khoảng cách co dãn tùy tiện, cỡ chữ loạn xạ hoặc dùng chữ xám trên nền xám.
    *   *Chuẩn kỹ thuật:* Phân cấp nghiêm ngặt giữa Thông tin chính (`-text-normal: #EDEDED` cỡ `15px`, tương phản > 7:1) và Siêu dữ liệu phụ trợ (`-text-muted: #8E929B` cỡ `11px - 13px`), mục tiêu nhấp chuột đạt chuẩn Fitts' Law `44px`.
5.  **Phản Hồi Xúc Giác Đầy Đủ 4 Trạng Thái & Tiến Trình Minh Bạch (Tactile States & Progressive Feedback):**
    *   *Vấn đề AI slop:* Nút bấm chỉ đổi độ mờ (opacity) hời hợt, không có trạng thái loading dẫn đến người dùng bấm liên tục.
    *   *Chuẩn kỹ thuật:* Mọi nút bấm có đủ 4 trạng thái: Default, Hover (phóng nhẹ 1.02x), Pressed (thụt nhẹ 0.98x), Disabled (vô hiệu hóa, con trỏ wait). Khi xử lý tác vụ mạng (Đăng nhập, Tải tin), nút lập tức hiển thị nhãn tiến trình cụ thể (*"Đang đăng nhập..."*, *"Đang gửi..."*).
6.  **Báo Lỗi Trực Quan Ngay Tại Nguồn (Inline Field Validation & Micro-Shake Feedback):**
    *   *Vấn đề AI slop:* Bật hộp thoại modal alert ngắt ngang trải nghiệm làm việc.
    *   *Chuẩn kỹ thuật:* Lỗi được gắn trực tiếp dưới trường nhập liệu với viền đỏ `-status-danger`, kết hợp hoạt ảnh rung lắc vi mô (`TranslateTransition ±8px` trong `320ms`) và tự động focus con trỏ vào trường lỗi đầu tiên.

---

## 6. Quy Chuẩn Vòng Lặp Phát Triển Nguyên Tử (Spec-First ➔ Anti-Slop Code ➔ Atomic Commit)

Mọi hoạt động bổ sung hoặc sửa đổi giao diện phân hệ User BẮT BUỘC tuân thủ chu trình 3 bước khép kín:

1. **Bước 1 — Spec-First & Spec Synchronization (Đặc tả đi trước mã nguồn):**
   - BẮT BUỘC rà soát và chỉnh sửa tệp đặc tả chi tiết trong `docs/ui-spec/user-screens.md` TRƯỚC KHI viết code.
   - Đảm bảo tên lớp (`Class`), phương thức (`Method`), cấu trúc phân cấp Node, class CSS và hành vi UI khớp 100% với hiện thực mã nguồn.

2. **Bước 2 — Anti-AI-Slop Code Implementation (Triển khai mã chuẩn mực):**
   - Triển khai mã JavaFX 21 + CSS tuân thủ nghiêm ngặt 6 nguyên tắc Anti-Slop tại Mục 5 và Quy tắc Single Border Source tại Mục 4.3.
   - Sử dụng 100% token từ `src/user/theme.css`.

3. **Bước 3 — Atomic Verification & Conventional Commit (Kiểm thử & Lưu vết Git):**
   - Kiểm tra `git diff` sạch sẽ, chạy thử giao diện qua `UserLauncher`.
   - Thực hiện commit Git theo chuẩn Conventional Commits: `type(user-ui): concise message`.

4. **Kỷ Luật Ngữ Cảnh Zero-Noise (Context Hygiene):**
   - Mỗi chu trình chỉ nạp đúng 1 file spec liên quan và file code mục tiêu.
   - Khuyến nghị thực hiện `/compact` sau mỗi vòng lặp nguyên tử để giải phóng bộ nhớ context trước khi chuyển sang task mới.
