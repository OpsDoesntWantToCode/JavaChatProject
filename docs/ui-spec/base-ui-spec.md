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

Mọi tài liệu đặc tả chức năng con (`docs/ui-spec/*.md`) đều kế thừa trực tiếp từ hệ thống Token này; không tự ý tạo ra các thông số biến thể nằm ngoài danh mục.

---

## 2. Tầng 1: Primitive Tokens (Giá trị Nền tảng)

### 2.1 Thang Màu Thô (Primitive Palette)
Bảng màu xây dựng trên nền tảng **Modern Obsidian Surface** kết hợp sắc thái **Tech Sapphire**:

| Nhóm màu | Tên Token | Giá trị Hex | Ứng dụng gốc |
| :--- | :--- | :---: | :--- |
| **Dark Slates** | `color.slate.950` | `#0F1013` | Sắc đen obsidian sâu nhất |
| | `color.slate.900` | `#141619` | Sắc canvas nền cửa sổ |
| | `color.slate.850` | `#1B1D22` | Sắc bề mặt thẻ card và modal |
| | `color.slate.800` | `#1E2026` | Sắc bề mặt khối công cụ / chip |
| | `color.slate.750` | `#22252C` | Trạng thái hover bậc nhẹ |
| | `color.slate.700` | `#262932` | Trạng thái hover danh sách / cell |
| | `color.slate.600` | `#2A2D35` | Đường viền mỏng tiêu chuẩn |
| | `color.slate.500` | `#383D48` | Đường viền tương tác / hover |
| **Tech Sapphires** | `color.sapphire.500` | `#4E6BF2` | Sắc xanh chủ đạo nhận diện |
| | `color.sapphire.600` | `#3D5AE0` | Sắc xanh tương tác hover |
| | `color.sapphire.700` | `#2E48C7` | Sắc xanh trạng thái nhấn giữ (active) |
| **Semantic Hues** | `color.emerald.500` | `#23A55A` | Trạng thái trực tuyến / thành công |
| | `color.ruby.500` | `#FA777C` | Trạng thái lỗi / nguy hiểm / cảnh báo đỏ |
| | `color.amber.500` | `#F0B232` | Trạng thái cảnh báo vàng / chờ duyệt |
| | `color.neutral.500`| `#80848E` | Trạng thái ngoại tuyến / slate trung tính |
| **Neutrals** | `color.neutral.000`| `#FFFFFF` | Trắng tinh khiết (Icon vector, CTA text) |
| | `color.neutral.100`| `#EDEDED` | Trắng độ tương phản cao (WCAG AAA) |
| | `color.neutral.400`| `#8E929B` | Xám nhạt phụ trợ (Placeholder, subtitle) |

### 2.2 Thang Đo Khoảng Cách (8-Point Spatial System)
Hệ thống khoảng cách tuân thủ nguyên tắc bội số của 4 và 8:

| Token Khoảng cách | Giá trị | Mục đích áp dụng |
| :--- | :---: | :--- |
| `space.1` | **`4px`** | Khoảng cách vi mô (nhãn sao đỏ, phân đoạn thanh đo) |
| `space.2` | **`8px`** | Khoảng cách nội bộ nhỏ, padding dọc cell, gap giữa icon và nhãn |
| `space.3` | **`12px`** | Khoảng cách giữa các phần tử nội bộ, padding ngang trường nhập |
| `space.4` | **`16px`** | Khoảng cách dọc tiêu chuẩn giữa các nhóm trường form |
| `space.5` | **`20px`** | Khoảng cách giữa các cột trong lưới đối xứng (GridPane hgap) |
| `space.6` | **`24px`** | Padding lề ngoài, padding khối Hero Section |
| `space.9` | **`36px`** | Padding dọc nội bộ của thẻ xác thực |
| `space.10` | **`40px`** | Padding ngang nội bộ của thẻ xác thực |

### 2.3 Thang Đo Typography (Type Scale)
*   **Họ Font chuẩn:** `Segoe UI` (hỗ trợ hiển thị mượt mà trên Windows Desktop).
*   **Đặc tính hiển thị:** Kích hoạt DirectWrite Subpixel LCD ClearType (`-fx-font-smoothing-type: lcd;`).
*   **Thang kích thước & Trọng số:**

| Cấp bậc | Token Kích thước | Trọng số (Weight) | Màu mặc định | Mục đích sử dụng |
| :--- | :---: | :---: | :--- | :--- |
| **Caption / Badge** | `11px` | Regular / Bold | `color.neutral.400` | Nhãn credit, timestamp tin nhắn, badge nhỏ |
| **Field Label** | `12.5px` | Bold (`700`) | `color.neutral.400` | Nhãn trường form, nhãn độ mạnh mật khẩu |
| **Body Secondary** | `13.5px` | Regular (`400`) | `color.neutral.400` | Mô tả tính năng, liên kết phụ trợ, text footer |
| **Body Primary** | `14px` | Regular (`400`) | `color.neutral.400` | Phụ đề form, nội dung tin nhắn chat đối phương |
| **Body Medium** | `14.5px` | Semi-Bold (`600`) | `color.neutral.100` | Tiêu đề tính năng thẻ công cụ, text nút bấm |
| **Input / Content** | `15px` | Regular / Bold | `color.neutral.100` | Nội dung trường nhập liệu, nút CTA chính |
| **Header Section** | `16px` | Bold (`700`) | `color.neutral.100` | Tiêu đề thanh tiêu đề chat (Header Title) |
| **Display Title** | `26px` | Bold (`700`) | `color.neutral.100` | Tiêu đề thẻ xác thực chính, tiêu đề thương hiệu |

### 2.4 Thang Bo Góc Chọn Lọc (Corner Radius Matrix)
Quy chuẩn bo góc có chọn lọc (Selective Radius) phản ánh phong cách giao diện công cụ chuyên nghiệp:

| Token Bo góc | Bán kính | Áp dụng cho thành phần |
| :--- | :---: | :--- |
| `radius.xs` | **`2.5px`** | Phân đoạn thanh đo mật khẩu |
| `radius.sm` | **`6px`** | Vùng highlight lựa chọn trong menu (ListCell) |
| `radius.md` | **`8px`** | Nút bấm hành động (`.btn-primary`), hộp icon badge (`38x38px`), popup menu |
| `radius.lg` | **`9px`** | Trường nhập liệu (`.text-field`, `.password-field`, `.combo-box`, `.date-picker`) |
| `radius.xl` | **`10px`** | Thẻ tính năng công cụ (`.feature-chip`), thẻ kết quả danh bạ |
| `radius.2xl` | **`14px`** | Thẻ xác thực chính (`.auth-card`), hộp thoại Modal |

### 2.5 Thang Thời Lượng Chuyển Động (Motion & Timing)

| Token Thời lượng | Giá trị | Ứng dụng tương tác |
| :--- | :---: | :--- |
| `motion.instant` | **`80ms`** | Phản hồi nhấn giữ chuột (:pressed state) |
| `motion.fast` | **`120ms`** | Phản hồi di chuột (:hover), nút toggle con mắt |
| `motion.normal` | **`150ms`** | Hoạt ảnh mở menu thả xuống (Fade + SlideY) |
| `motion.smooth` | **`200ms`** | Chuyển cảnh View (Fade out 90ms + Fade in 110ms), Drawer trượt |
| `motion.alert` | **`320ms`** | Hiệu ứng rung lắc báo lỗi (Shake on Error) |
| `motion.highlight`| **`1500ms`** | Hiệu ứng chớp highlight khi tìm kiếm tin nhắn |

---

## 3. Tầng 2: Semantic Tokens (Token Ngữ Nghĩa & Vai Trò)

Các token ngữ nghĩa ánh xạ trực tiếp từ Primitive Tokens, đóng vai trò làm giao diện lập trình logic cho CSS stylesheet (`src/user/theme.css`):

```css
/* Tầng 2: Semantic Mapping trong theme.css */
.root {
    /* Backgrounds */
    -bg-canvas:        #141619; /* color.slate.900 */
    -bg-card:          #1B1D22; /* color.slate.850 */
    -bg-input:         #0F1013; /* color.slate.950 */
    -bg-chip:          #1E2026; /* color.slate.800 */
    -bg-nav-rail:      #0F1013; /* color.slate.950 */
    -bg-item-hover:    #262932; /* color.slate.700 */

    /* Actions */
    -accent-primary:   #4E6BF2; /* color.sapphire.500 */
    -accent-hover:     #3D5AE0; /* color.sapphire.600 */
    -accent-active:    #2E48C7; /* color.sapphire.700 */

    /* Statuses */
    -status-online:    #23A55A; /* color.emerald.500 */
    -status-danger:    #FA777C; /* color.ruby.500 */
    -status-warning:   #F0B232; /* color.amber.500 */
    -status-offline:   #80848E; /* color.neutral.500 */

    /* Typography */
    -text-normal:      #EDEDED; /* color.neutral.100 */
    -text-muted:       #8E929B; /* color.neutral.400 */
    -text-white:       #FFFFFF; /* color.neutral.000 */
    -text-link:        #4E6BF2; /* color.sapphire.500 */

    /* Borders */
    -border-subtle:    #2A2D35; /* color.slate.600 */
    -border-hover:     #383D48; /* color.slate.500 */

    /* Chat Bubbles */
    -bubble-sent:      #4E6BF2; /* color.sapphire.500 */
    -bubble-received:  #252830; /* Tối ưu độ sâu chat */
}
```

---

## 4. Tầng 3: Component-Level Tokens & Tiêu chuẩn Thành phần

### 4.1 Thẻ Card Chính (`.auth-card`) & Phân tầng Bề mặt (Layered Surface Elevation)
*   **Tokens:** `background: -bg-card`, `border: 1px solid -border-subtle`, `radius: radius.2xl (14px)`, `padding: space.9 space.10 (36px 40px)`.
*   **Nguyên tắc Phân tầng Độ sâu Bề mặt (Layered Surface Elevation):**
    *   Thay thế hoàn toàn các vầng sáng mờ ảo hoặc viền neon trang trí (vốn là biểu hiện của AI slop không có giá trị công năng) bằng **hệ thống phân tầng bề mặt tinh tế** thông qua các bước sáng tự nhiên kết hợp đường viền hairline:
        *   **Tầng 0 (Background Canvas):** `-bg-canvas: #141619` — Nền tĩnh của cửa sổ.
        *   **Tầng 1 (Sub-Panel / Inputs):** `-bg-input: #0F1013` — Bề mặt chìm nhẹ, tạo độ tập trung khi gõ.
        *   **Tầng 2 (Surface Card / Modal):** `-bg-card: #1B1D22` — Bề mặt nổi nhẹ, phân tách bằng đường viền mỏng `1px solid -border-subtle (#2A2D35)`.
        *   **Tầng 3 (Floating Popups / Menus):** `background: #22252C`, bo góc `8px`, bóng đổ độ sâu nhẹ nhàng `dropshadow(three-pass-box, rgba(0, 0, 0, 0.45), 14, 0, 0, 6)`.
    *   **Bảo vệ độ sắc nét font chữ (Zero Rasterization):** Tuyệt đối không gắn `-fx-effect` trực tiếp lên thẻ card hoặc bất kỳ container chứa chữ. Giữ nguyên công nghệ DirectWrite Subpixel LCD ClearType nguyên bản.

### 4.2 Trường Nhập Liệu (`.text-field`, `.password-field`, `.combo-box`, `.date-picker`)
*   **Tokens:** `height: 44px`, `background: -bg-input`, `border: 1px solid -border-subtle`, `radius: radius.lg (9px)`, `padding: space.2 space.3 (8px 14px)`, `font-size: 15px`.
*   **Trạng thái Focus:** `border: 1px solid -accent-primary`, `effect: dropshadow(three-pass-box, rgba(78, 107, 242, 0.35), 8, 0, 0, 0)`.
*   **Trạng thái Lỗi (`.input-error`):** `border: 1px solid -status-danger`, `effect: dropshadow(three-pass-box, rgba(250, 119, 124, 0.35), 8, 0, 0, 0)`.

### 4.4 Nút Bấm Chính (`.btn-primary`)
*   **Tokens:** `height: 44px`, `background: -accent-primary`, `radius: radius.md (8px)`, `font-size: 15px`, `font-weight: bold`, `text-fill: -text-white`.
*   **Tương tác:** Hover sang `-accent-hover` (`120ms`), Pressed sang `-accent-active` (`80ms`).

### 4.5 Icon Vector Chuẩn Hóa
*   **Hình thức:** Vector `SVGPath` lấy từ hệ thống thư viện **Lucide / Feather**.
*   **Màu sắc:** Cố định màu trắng thuần túy (`#FFFFFF`), áp dụng quy tắc cưỡng bức CSS:
    ```css
    .feature-chip-icon, .feature-chip SVGPath, .hero-logo-box SVGPath, .hero-logo-icon {
        -fx-fill: #FFFFFF !important;
    }
    ```
*   **Hộp chứa Icon Badge (`iconBadge`):** Kích thước cố định `38x38px`, bo góc `8px`, nền `rgba(255, 255, 255, 0.08)`, viền `1px solid rgba(255, 255, 255, 0.12)`.

---

## 5. Quy Chuẩn Không Gian & Khung Màn Hình (Layout & Responsive Standards)

Ứng dụng desktop hỗ trợ 2 chế độ hiển thị thích ứng mượt mà:

1.  **Chế độ Thu gọn / Hộp thoại Tập trung (Compact Mode — Viewport < 1000px):**
    *   Kích thước cửa sổ chuẩn Fitts' Law: `540 x 600` (Đăng nhập / Quên mật khẩu), `820 x 750` (Đăng ký 2 cột).
    *   Thẻ xác thực căn giữa tuyệt đối, ôm sát nội dung (`setMaxHeight(USE_PREF_SIZE)`).
2.  **Chế độ Mở rộng / Toàn màn hình (Expanded Mode — Viewport ≥ 1000px):**
    *   Kích hoạt bố cục phân chia 2 cột đối trọng:
        *   **Cột Trái (Hero Branding Showcase — 45%):** Logo, tiêu đề thương hiệu, 3 thẻ tính năng chữ nhật bo góc `10px` với icon vector trắng.
        *   **Cột Phải (Form Container — 55%):** Thẻ xác thực căn giữa, bề mặt Gunmetal `-bg-card` nổi bật tự nhiên trên nền canvas.
3.  **Khung Không Gian Làm Việc Chính (Main Workspace Shell — 4 Cột):**
    *   **Cột 1 (Navigation Rail):** Cố định `60px`.
    *   **Cột 2 (Sub-Sidebar):** Linh hoạt `280px – 320px`.
    *   **Cột 3 (Main Content View):** Co giãn tự do (`Priority.ALWAYS`).
    *   **Cột 4 (Right Drawer):** Cố định `280px` (trượt đóng/mở mượt mà trong `200ms`).

---

## 6. Sáu Nguyên Tắc Kỹ Thuật Thực Tiễn Chống AI Slop (Anti-Slop Engineering Principles)

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

## 7. Quy Chuẩn Vòng Lặp Phát Triển Nguyên Tử (Spec-First ➔ Anti-Slop Code ➔ Atomic Commit)

Mọi hoạt động bổ sung hoặc sửa đổi giao diện phân hệ User BẮT BUỘC tuân thủ chu trình 3 bước khép kín:

1. **Bước 1 — Spec-First & Spec Synchronization (Đặc tả đi trước mã nguồn):**
   - BẮT BUỘC rà soát và chỉnh sửa tệp đặc tả chi tiết tương ứng trong `docs/ui-spec/*.md` TRƯỚC KHI viết code.
   - Đảm bảo tên lớp (`Class`), phương thức (`Method`), cấu trúc phân cấp Node, class CSS và dữ liệu mẫu khớp 100% với hiện thực mã nguồn.
   - Cập nhật đồng bộ danh mục tệp và trách nhiệm tại `components-and-files.md`.

2. **Bước 2 — Anti-AI-Slop Code Implementation (Triển khai mã chống AI Slop):**
   - **Màu sắc & Hiệu ứng:** CẤM màu neon chói lọi (`#00FFCC`, `#FF00FF`, `#00FFFF`), CẤM hiệu ứng phát quang lòe loẹt (glow/drop-shadow đa sắc). BẮT BUỘC dùng Deep Obsidian (`#0F1013`, `#141619`, `#1B1D22`), Tech Sapphire (`#4E6BF2`) và viền hairline `1px solid #2A2D35`.
   - **Bo góc chọn lọc:** CẤM bo tròn viên thuốc (pill `999px`) bừa bãi cho thẻ Card, Panel, Dialog. CHỈ dùng bo tròn 50% cho Avatar.
   - **Dữ liệu mẫu chân thực:** BẮT BUỘC sử dụng kịch bản dữ liệu mẫu từ `dummy-data-matrix.md` (Hải, Minh, Lan Anh, đồ án Java, script database.sql, AES-GCM Messenger).

3. **Bước 3 — Atomic Verification & Conventional Commit (Kiểm thử & Lưu vết Git):**
   - Kiểm tra `git diff` sạch sẽ, chạy thử giao diện qua `UserLauncher`.
   - Thực hiện commit Git theo chuẩn Conventional Commits: `type(user-ui): concise message`.

4. **Kỷ Luật Ngữ Cảnh Zero-Noise (Context Hygiene):**
   - Mỗi chu trình chỉ nạp đúng 1 file spec liên quan và file code mục tiêu.
   - Khuyến nghị thực hiện `/compact` sau mỗi vòng lặp nguyên tử để giải phóng bộ nhớ context trước khi chuyển sang task mới.
