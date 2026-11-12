# QUY CHUẨN NỀN TẢNG GIAO DIỆN & TRẢI NGHIỆM NGƯỜI DÙNG (GLOBAL UI/UX BASE SPECIFICATION)

> **Cấp bậc tài liệu:** TÀI LIỆU CHA NỀN TẢNG (PARENT SPECIFICATION)  
> **Kiến trúc quản trị:** Three-Tier Design Tokens Architecture (W3C Design Tokens Community Group Standard)  
> **Tiêu chuẩn thẩm định:** Microsoft Fluent 2, NN/g Usability Heuristics, Refactoring UI, Linear-Style Desktop Tooling  
> **Phạm vi áp dụng:** Toàn bộ thành phần giao diện JavaFX và stylesheet CSS thuộc hệ thống Desktop Chat (`src/user/`, `src/admin/`)  
> **Mục tiêu:** Định hình một sản phẩm máy tính để bàn chuẩn mực công nghiệp, thân thiện tối đa với người dùng (User-Friendly), loại bỏ hoàn toàn các biểu hiện "AI slop", vận hành mượt mà 60fps – 120fps trên Windows.

---

## 1. Bốn Trụ Cột Nền Tảng Thiết Kế Hệ Thống & Tâm Lý Học Nhận Thức

Toàn bộ quy chuẩn giao diện và tương tác của dự án được xây dựng dựa trên 4 nguồn chuẩn mực thiết kế hệ thống và tâm lý học nhận thức chính thống:

### 1.1 Chuẩn Thiết Kế Hệ Điều Hành Microsoft (Windows App Standards)
* **Fluent Design System & WinUI 3 Guidelines (Microsoft Learn):**
  * **Typography chuẩn hệ thống:** Sử dụng họ font `Segoe UI Variable` / `Segoe UI` với công nghệ dựng chữ sắc nét DirectWrite Subpixel LCD ClearType (`-fx-font-smoothing-type: lcd;`).
  * **Ma trận bo góc chuẩn mực:** Bo góc siêu vi `4px` cho các phần tử điều khiển nhỏ/nội bộ (Input, Tag, Badge) và `8px – 14px` cho các khung chứa lớn, Card, Modal.
  * **Phân lớp độ sâu nền (Layering & Elevation):** Khai thác phân lớp thị giác tương đương Mica/Acrylic/Smoke để phân tách không gian, giúp mắt người dùng dễ phân biệt nội dung chính và phụ.
  * **Vùng tương tác công thái học (Fitts' Law):** Vùng tương tác tối thiểu `44x44px` cho các nút chính và `46x32px` cho các nút điều hướng caption. Hỗ trợ hành vi cửa sổ tự nhiên trên Windows (Aero Snap, co giãn linh hoạt).

### 1.2 Các Định Luật Tâm Lý Học Nhận Thức & Khả Năng Sử Dụng (Usability Heuristics)
* **Jakob's Law (Định luật Jakob) – Jakob Nielsen (Nielsen Norman Group):**
  * Người dùng dành phần lớn thời gian trên các ứng dụng máy tính quen thuộc khác (VS Code, Discord, Telegram, File Explorer). Do đó, bố cục phải tuân thủ các mô thức quen thuộc (Navigation Rail cột trái, danh sách hội thoại ở giữa, khung chat ở trung tâm, ngăn kéo thông tin ở cột phải). Tuyệt đối không đảo lộn vị trí điều khiển khiến người dùng bối rối.
* **10 Usability Heuristics (Jakob Nielsen):**
  * **Nguyên tắc 1 (*Visibility of system status*):** Trạng thái hệ thống luôn hiển thị minh bạch tức thời: Trạng thái Online (chấm xanh ●) / Offline (chấm xám ○), thanh tiến trình gửi mail, nhãn *"Đang đăng nhập..."* khi click.
  * **Nguyên tắc 4 (*Consistency and standards*):** Mọi nút bấm và ô nhập liệu BẮT BUỘC có đủ **5 trạng thái tương tác**: Mặc định (`Default`), Di chuột (`Hover`), Nhấn giữ (`Active/Armed`), Tiêu điểm bàn phím (`Focus-Visible`), và Vô hiệu hóa (`Disabled`).
  * **Hỗ trợ điều hướng bàn phím đầy đủ:** Phản hồi chuẩn mực phím tắt: `Tab` chuyển trường, `Enter` kích hoạt/gửi, `Shift+Enter` xuống dòng, `Esc` đóng tìm kiếm / modal dialog.

### 1.3 Sách Chuyên Khảo & Hệ Thống Lưới Thực Chiến (Visual & Spatial Systems)
* **Sách *"Refactoring UI"* (Adam Wathan & Steve Schoger – Tailwind CSS Founders):**
  * **Lưới toán học bội số `4px / 8px`:** Mọi thông số padding, margin, gap đều chia hết cho 4 hoặc 8 (`4px`, `8px`, `12px`, `16px`, `24px`, `32px`, `40px`).
  * **Đường viền Hairline thay cho đổ bóng rườm rà:** Dùng đường viền mảnh `1px solid rgba(255, 255, 255, 0.08)` hoặc `1px solid #2A2D35` để phân tách bề mặt thay vì dùng bóng mờ dày đặc.
  * **Phân cấp 3 lớp độ sáng nền:** Dark Canvas (`#141619`) ➔ Sub-Panel/Input (`#0F1013`) ➔ Surface Card (`#1B1D22`) ➔ Floating Menu (`#22252C`).
  * **Tiết chế màu nhấn (Accent Restraint):** Màu nhấn xanh Sapphire (`#4E6BF2`) và tím AI (`#8B5CF6`) chỉ dùng cho hành động có chủ đích; không rải rác màu mè gây nhiễu loạn thị giác.
* **Google Material Design 3 & Apple HIG:**
  * Độ tương phản chữ đạt chuẩn WCAG AAA (> 7:1 cho text chính `#EDEDED` và > 4.5:1 cho text phụ `#8E929B`). Bố cục Master-Detail đa cột tối ưu cho màn hình desktop rộng.

### 1.4 Chuẩn Mực Desktop App Hiện Đại ("Linear-Style" & Developer Tooling)
* **Phong cách Linear, Raycast, VS Code, Discord:** 
  * Bản sắc công cụ chuyên nghiệp, tinh tế, đĩnh đạc, độ nảy tức thì và hiệu năng phản hồi cao.
  * **Loại bỏ hoàn toàn AI Slop:** Không dùng gradient phát quang neon, không viền cầu vồng, không bo góc quá đà (pill-shape tùy tiện), không lãng phí khoảng trắng.

---

## 2. Mô hình Kiến trúc Token Ba Tầng (Three-Tier Token Architecture)

Hệ thống giao diện tuân thủ nghiêm ngặt mô hình phân tầng chuẩn W3C Design Tokens Community Group:

```text
┌────────────────────────────────────────────────────────────────────────┐
│ TẦNG 1: PRIMITIVE TOKENS (Giá trị thô / Global Reference Tokens)       │
│ • Bảng màu gốc (Slate, Sapphire, Status) • Thang khoảng cách 4px/8px   │
│ • Typography (Segoe UI) • Bán kính bo góc • Thời lượng chuyển động      │
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

Mọi tài liệu đặc tả màn hình con (như [user-screens.md](user-screens.md)) đều kế thừa trực tiếp từ hệ thống Token này; không tự ý tạo ra các thông số biến thể nằm ngoài danh mục.

---

## 3. Hệ Thống Token Hợp Nhất (Design Tokens Map)

Bảng ánh xạ trực tiếp từ Primitive Token sang Semantic Token (CSS trong `theme.css`):

| Nhóm | Biến CSS (`theme.css`) | Primitive Token | Giá trị Hex | Ứng dụng công thái học |
| :--- | :--- | :--- | :---: | :--- |
| **Bề mặt** | `-bg-canvas` | `color.slate.900` | `#141619` | Nền tĩnh ngoài cùng của cửa sổ (Tầng 0) |
| | `-bg-card` | `color.slate.850` | `#1B1D22` | Bề mặt thẻ Card, Modal, Sidebar, Drawer (Tầng 2) |
| | `-bg-input` / `-bg-nav-rail` | `color.slate.950` | `#0F1013` | Nền chìm cho ô nhập liệu và Nav Rail 60px (Tầng 1) |
| | `-bg-chip` | `color.slate.800` | `#1E2026` | Bề mặt nút Smart Chip, thẻ tính năng Hero |
| | `-bg-item-hover` | `color.slate.700` | `#262932` | Trạng thái hover cell danh sách |
| **Hành động** | `-accent-primary` / `-text-link` | `color.sapphire.500`| `#4E6BF2` | Sắc xanh chủ đạo, nút CTA, link điều hướng |
| | `-accent-hover` | `color.sapphire.600`| `#3D5AE0` | Trạng thái hover nút CTA |
| | `-accent-active` | `color.sapphire.700`| `#2E48C7` | Trạng thái nhấn giữ nút CTA |
| | `-accent-ai` | `color.violet.500` | `#8B5CF6` | Điểm nhấn Trợ lý AI và huy hiệu `[AI BOT]` |
| **Trạng thái** | `-status-online` | `color.emerald.500` | `#23A55A` | Trực tuyến, thành công, an toàn E2EE |
| | `-status-danger` | `color.ruby.500` | `#FA777C` | Báo lỗi, nút xóa/rời nhóm, sao đỏ bắt buộc |
| | `-status-warning`| `color.amber.500` | `#F0B232` | Cảnh báo, huy hiệu `[Admin]` nhóm |
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

## 4. Quy Chuẩn Thành Phần Giao Diện (Component Standards)

### 4.1 Thẻ Card Chính (`.auth-card`) & Phân tầng Bề mặt (Layered Surface Elevation)
* **Tokens:** `background: -bg-card`, `border: 1px solid -border-subtle`, `radius: radius.2xl (14px)`, `padding: space.9 space.10 (36px 40px)`.
* **Phân tầng độ sâu:** Không dùng shadow phát sáng lòe loẹt. Dùng sự tương phản giữa `-bg-canvas` (`#141619`) và `-bg-card` (`#1B1D22`) kết hợp viền mỏng hairline `1px solid #2A2D35`.

### 4.2 Trường Nhập Liệu (`.text-field`, `.password-field`, `.combo-box`, `.date-picker`)
* **Tokens:** `height: 44px`, `background: -bg-input`, `border: 1px solid -border-subtle`, `radius: radius.lg (9px)`, `padding: space.2 space.3 (8px 14px)`, `font-size: 15px`.
* **Trạng thái Focus:** `border: 1px solid -accent-primary`, `effect: dropshadow(three-pass-box, rgba(78, 107, 242, 0.25), 6, 0, 0, 0)`.
* **Trạng thái Lỗi (`.input-error`):** `border: 1px solid -status-danger`, kết hợp hoạt ảnh lắc nhẹ `JavaFXTheme.shakeNode()`.

### 4.3 Quy Chuẩn Nút Bấm & Phản Hồi Xúc Giác (Buttons & Tactile Micro-Interactions)
* **Nút Bấm Chính (`.btn-primary`, `.btn-secondary`):**
  * **Tokens:** `height: 44px`, `background: -accent-primary`, `radius: radius.md (8px)`, `font-size: 15px`, `font-weight: bold`, `text-fill: -text-white`.
  * **Phản hồi xúc giác Native (`JavaFXTheme.applyTactileButton`):**
    * `MOUSE_PRESSED`: `ScaleTransition` thu nhỏ `0.975x` trong `70ms` (`Interpolator.EASE_OUT`).
    * `MOUSE_RELEASED`: `ScaleTransition` phục hồi `1.0x` trong `80ms`.
    * `MOUSE_EXITED`: Tự động khôi phục `1.0x` nếu chuột rời vùng bấm khi đang nhấn.
    * *Bảo vệ độ nét chữ:* Tuyệt đối không scale nút có chữ khi hover để tránh làm nhòe font DirectWrite ClearType.
* **Nút Icon Đơn Lẻ (`.btn-icon-subtle`, `.btn-eye-toggle`, `.dialog-close-btn`):**
  * **Phản hồi vi mô Native (`JavaFXTheme.applyIconButton`):**
    * `MOUSE_ENTERED`: Phóng nhẹ `1.08x` trong `100ms`.
    * `MOUSE_EXITED`: Khôi phục `1.0x` trong `100ms`.
    * `MOUSE_PRESSED`: Co nhẹ `0.94x` trong `60ms`.

### 4.4 Icon Vector Chuẩn Hóa
* **Hình thức:** Vector `SVGPath` lấy từ hệ thống thư viện **Lucide / Feather**.
* **Màu sắc:** Cố định màu trắng thuần túy (`#FFFFFF`) trên khung badge trung tính `38x38px` bo góc `8px`.
  ```css
  .feature-chip-icon, .feature-chip SVGPath, .hero-logo-box SVGPath, .hero-logo-icon {
      -fx-fill: #FFFFFF !important;
  }
  ```

### 4.5 Ô Nhập Văn Bản Nhiều Dòng (`.text-area`) & Khử Nền Trắng Modena
* **Vấn đề triệt tiêu:** JavaFX Modena mặc định gán `-fx-control-inner-background: #ffffff` lên vùng viewport `.content` của `TextArea`, gây hiện tượng chói mắt "nền trắng bóc" trong Dark Mode.
* **Quy chuẩn sửa đổi:** Bắt buộc đồng bộ `-fx-control-inner-background: -bg-input` và style trực tiếp `.text-area .content` với `-fx-background-color: -bg-input; -fx-text-fill: -text-normal; -fx-prompt-text-fill: #686C75;`. Bo góc vỏ ngoài mềm mại `9px`, viền hairline `1px solid -border-subtle`.

### 4.6 Các Phần Tử Chọn Lựa (`.radio-button`, `.check-box`) & Điểm Nhấn Sapphire
* **Triệt tiêu 3D Bevel cũ kỹ:** Thay thế toàn bộ hình thái nổi khối chuyển sắc xám của Modena bằng thiết kế phẳng Obsidian Dark.
* **RadioButton:** Vỏ tròn phẳng `-bg-input` viền `1.5px solid -border-subtle`. Khi được chọn (`:selected`), viền chuyển sang Sapphire (`#4E6BF2`) và điểm chấm tròn trung tâm (`.dot`) phủ đặc màu Sapphire rực rỡ.
* **CheckBox:** Hộp vuông phẳng bo góc mềm `4px`, viền `1.5px solid -border-subtle`. Khi được chọn (`:selected`), hộp phủ đầy màu Sapphire (`#4E6BF2`) và hiển thị dấu kiểm vector sắc nét màu trắng (`#FFFFFF`).

### 4.7 Thanh Cuộn Tối Giản (`.scroll-bar`) & Danh Sách (`.list-view`)
* **Thanh cuộn Minimalist 8px:** Chiều rộng chỉ `8px`, toàn bộ rãnh cuộn (`.track`) và hai nút mũi tên tăng giảm (`.increment-button`, `.decrement-button`) được làm trong suốt hoàn toàn (`transparent`). Con lăn (`.thumb`) là dải pill bo góc `4px` màu xám chìm `#2E323D`, hover `#424756`, pressed `#4E6BF2`.
* **Khung danh sách (`.list-view`):** Triệt tiêu hoàn toàn dải sáng nền trắng của Modena, gán `-fx-control-inner-background: -bg-input`, hỗ trợ hiệu ứng lướt chuột (`hover: #1E2129`) mượt mà trên từng hàng.

### 4.8 Menu Ngữ Cảnh Chuột Phải (`.context-menu`, `.menu-item`)
* **Thẻ lơ lửng Obsidian Dark (Transient Elevation Surface):** Nền `-bg-card: #1B1D22`, viền `1px solid -border-subtle: #2A2D35`, bo góc `8px`, đệm trong `6px`, đổ bóng sâu `dropshadow(three-pass-box, rgba(0, 0, 0, 0.65), 18, 0, 0, 8)`.
* **Trạng thái Item:** Mặc định nền trong suốt, chữ `-text-normal: #EDEDED`. Khi hover/focus: nền `#262A34`, chữ sáng `#FFFFFF`. Đường phân cách (`SeparatorMenuItem`) dùng viền hairline mảnh.
* **Tích hợp JavaFX:** Tự động nạp stylesheet `theme.css` vào `PopupScene` thông qua tiện ích `JavaFXTheme.applyContextMenuTheme(cm)`.

### 4.9 Hộp Thoại Hệ Thống & Cảnh Báo (`.dialog-pane`, Alerts, Prompts)
* **Triệt tiêu cửa sổ trắng và icon chấm hỏi `(?)` thô kệch của Modena:** Gán lớp vỏ `.dialog-pane` nền `-bg-card`, viền `1px solid -border-subtle`, bo góc `12px`.
* **Thanh tiêu đề Header:** Nền chìm `-bg-input: #0F1013` phân tách bằng viền đáy hairline, chữ tiêu đề đậm `-text-normal`.
* **Cụm nút bấm:** Nút OK/Thực hiện mang màu Sapphire `-accent-primary` (`:default`), nút Hủy mang màu trung tính `#252832` (`btn-secondary`).
* **Tiện ích thống nhất qua `JavaFXTheme`:** Cung cấp các phương thức tĩnh `applyDialogTheme(dialog)`, `showInputDialog(...)`, `showConfirmDialog(...)`, và `showInfoAlert(...)`.

### 4.10 Chú Thích Công Cụ (`.tooltip`)
* **Pill nổi Obsidian Dark:** Nền `#242730`, viền `1px solid #2A2D35`, bo góc `6px`, chữ `-text-normal: #EDEDED`, cỡ chữ `12px`, padding `5px 9px`, đổ bóng mềm `rgba(0,0,0,0.45)`. Loại bỏ hoàn toàn hộp màu vàng nhạt mặc định của JavaFX Modena.

---

## 5. Quy Chuẩn Chuyển Động & Hiệu Ứng Thị Giác Chuẩn Công Nghiệp (Motion & Visual Effects)

Để thêm Animation và Effect cho ứng dụng Windows mà vẫn giữ được vẻ đĩnh đạc, sắc nét của một phần mềm chuyên nghiệp (thoát hẳn cảm giác màu mè, giật cục của "AI slop"), hệ thống tuân thủ nghiêm ngặt 5 quy tắc vật lý từ **Microsoft Fluent 2 Motion**, **Material Design 3**, **NN/g** và **W3C WCAG**:

### 5.1 Quy Tắc Thời Lượng (Duration) — Nhanh Hơn Web và Mobile (30% – 50%)
Theo nghiên cứu của **Nielsen Norman Group (Jakob Nielsen)** về giới hạn thời gian phản hồi (*Response Time Limits*), mọi phản hồi dưới `100ms` tạo cảm giác tức thì. Trên môi trường Desktop dùng chuột tốc độ cao:
* **Micro-interactions (Hover nút, đổi màu viền, toggle icon):** **`80ms – 150ms`**. (Nếu để `300ms – 500ms` như AI mặc định sinh ra, ứng dụng sẽ mang lại cảm giác lề mề, trễ nhịp tay chuột).
* **Mở/Đóng thành phần nhỏ (Dropdown, Context Menu, Tooltip, Flyout):**
  * **Lúc mở (Enter):** **`150ms – 200ms`**.
  * **Lúc đóng (Exit):** **`100ms – 150ms`**. *Quy chuẩn Fluent 2 & Material 3 bắt buộc hiệu ứng đóng phải nhanh hơn hiệu ứng mở vì người dùng đã xong việc và muốn phần tử biến mất ngay để làm việc tiếp theo.*
* **Thay đổi bố cục lớn (Đóng/mở Sidebar, mở Modal Dialog, chuyển tab View):** **`200ms – 250ms`** (Tối đa tuyệt đối không vượt quá `300ms` trên công cụ làm việc).

### 5.2 Quy Tắc Đường Cong Gia Tốc (Easing Curves) — Bí Quyết Của Độ "Mượt"
Tuyệt đối không dùng chuyển động đều (`linear`) hoặc `ease-in-out` chậm chạp ở cả 2 đầu, không lạm dụng nảy lò xo (`elastic` / `bounce` lố bịch). Áp dụng 3 đường cong chuẩn mực:
* **Khi xuất hiện trên màn hình (`Decelerate` / `Ease-Out`):** Dùng `cubic-bezier(0, 0, 0, 1)` hoặc `Interpolator.EASE_OUT`. Phần tử phóng ra cực nhanh ở những mili-giây đầu tiên để mắt người dùng thấy phản hồi tức thì, sau đó phanh êm lại trước khi dừng hẳn.
* **Khi biến mất khỏi màn hình (`Accelerate` / `Ease-In`):** Dùng `cubic-bezier(0.7, 0, 1, 0.5)` hoặc `Interpolator.EASE_IN`. Phần tử bắt đầu rút đi và tăng tốc dần rồi biến mất.
* **Khi di chuyển vị trí (`Standard` / `Point-to-Point`):** Dùng cho vạch chỉ thị tab (Active Pill) trượt hoặc khi trượt đóng/mở Right Drawer: `cubic-bezier(0.8, 0, 0.2, 1)` (bắt đầu nhanh, lướt mượt và dừng êm).

### 5.3 Quy Tắc Dịch Chuyển Không Gian (Spatial Choreography) — Ít Nhưng Tinh Tế
Chuyển động chỉ dùng để dẫn hướng mắt người dùng hiểu phần tử đó xuất phát từ đâu, không phải để biểu diễn đồ họa:
* **Khoảng cách trượt cực ngắn (Short-distance Slide + Fade):** Khi mở Context Menu, Dropdown hoặc chuyển Tab, kết hợp **Fade-in (`Opacity 0 ➔ 1`)** cùng cú trượt vi mô chỉ **`4px – 12px`** theo hướng mở ra.
* **Tỷ lệ phóng to/thu nhỏ của Modal Dialog (Subtle Scale):** Tuyệt đối không phóng từ `0%` lên `100%` (gây chóng mặt). Chuẩn mực Windows 11 là phóng từ **`96%` (hoặc `95%`) lên `100%`** kết hợp `Opacity 0 ➔ 1` trong **`180ms`**.
* **Chuyển động của Icon (Animated Line Icons):** Chỉ kích hoạt khi người dùng trực tiếp tương tác (Hover hoặc Click, ví dụ: con mắt mật khẩu nháy nhẹ, icon AI zoom 1.08x). Tuyệt đối không để icon tự động lặp chuyển động vô tận (infinite loop) khi người dùng đang đọc nội dung.

### 5.4 Quy Tắc Hiệu Ứng Thị Giác & Chất Liệu (Materials & Effects)
* **Phân biệt rõ Kính mờ (Mica vs. Acrylic — Chuẩn Microsoft Fluent 2):**
  * **Mica (Nền cửa sổ dài hạn):** Màu nền sâu tĩnh (`#141619`) cố định cho lớp nền dưới cùng (Base Layer / Sidebar). Không tốn GPU khi cuộn.
  * **Acrylic / Backdrop Blur (Kính mờ trong suốt):** Chỉ áp dụng cho **các bề mặt tạm thời (Transient Surfaces)** như Context Menu, Popup, Flyout hoặc Tooltip. Cấm lạm dụng Glassmorphism cho tất cả thẻ card tĩnh vì gây mờ chữ và rối mắt.
* **Đổ bóng theo Chức năng (Functional Elevation Shadows):**
  * **Phần tử nằm phẳng (Button, Input, Card tĩnh, Sidebar):** Độ cao `Z = 0` ➔ **Không đổ bóng** (chỉ dùng viền hairline `1px solid -border-subtle: #2A2D35`).
  * **Menu nổi / Tooltip / Popover:** Độ cao trung bình ➔ Bóng mềm, độ mờ thấp: `0 4px 12px rgba(0, 0, 0, 0.14)`.
  * **Modal Dialog / Cửa sổ con:** Độ cao lớn nhất ➔ Bóng rộng: `0 16px 32px rgba(0, 0, 0, 0.28)` kết hợp **Lớp phủ làm tối nền sau (`Smoke Layer`: `rgba(0, 0, 0, 0.4)`)** để tập trung thị giác, không làm mờ nhòe nền sau.
* **Cấm Tuyệt Đối Hiệu Ứng Phát Sáng (Glow / Neon Prohibition):** Cấm hoàn toàn `DropShadow` có màu rực rỡ (nút xanh bóng xanh, viền phát quang neon Dribbble). Khi Focus, chỉ dùng viền ngoài `1px` sắc nét kèm vòng mờ Focus Ring độ trong suốt nhẹ `20%` màu Accent.

### 5.5 Quy Tắc Hiệu Năng & Khả Năng Tiếp Cận (Performance & Accessibility — WCAG 2.1)
* **Đảm bảo 60fps – 120fps (GPU Acceleration):** Chỉ animate 2 thuộc tính không gây tính toán lại bố cục (Reflow/Repaint): **Vị trí/Kích thước (`Transform`: Translate, Scale, Rotate)** và **Độ trong suốt (`Opacity`)**. Hạn chế tối đa việc animate trực tiếp `width`, `height`, `margin` theo từng frame trên danh sách.
* **Trạng thái Tải dữ liệu (Skeleton Shimmer thay vì Spinner):** Với vùng nội dung mất hơn `300ms` để tải, không dùng spinner quay tít giữa màn hình. Dùng khung xương xám (**Skeleton Shimmer**) có kích thước giống hệt bố cục thật với ánh sáng lướt nhẹ (`1.5s/chu kỳ`) để giữ ổn định cấu trúc mắt nhìn.
* **Tôn trọng cài đặt Hệ điều hành (`Reduce Motion` — WCAG 2.3.3):** Khi người dùng kích hoạt cờ *Animation Effects = Off* trong Windows Settings hoặc chạy chế độ tiết kiệm pin, mọi hiệu ứng trượt/scale tự động chuyển thành đổi độ mờ tức thì (`Cross-fade` dưới `50ms`) hoặc hiển thị trực tiếp.

---

## 6. Quy Chuẩn Không Gian & Khung Màn Hình (Layout & Responsive Standards)

### 6.1 Khung Không Gian Làm Việc Chính (Main Workspace Shell — 4 Cột)
* **Cột 1 (Navigation Rail):** Cố định `60px` (Chỉ vẽ viền phải: `-fx-border-color: transparent #2A2D35 transparent transparent;`).
* **Cột 2 (Sub-Sidebar):** Linh hoạt `280px – 320px` (Chỉ vẽ viền phải: `-fx-border-color: transparent #2A2D35 transparent transparent;`).
* **Cột 3 (Main Content View):** Co giãn tự do (`Priority.ALWAYS`), không vẽ viền ngoài. Header chỉ vẽ viền đáy, Thanh nhập liệu chỉ vẽ viền đỉnh.
* **Cột 4 (Right Drawer):** Cố định `280px` (Chỉ vẽ viền trái: `-fx-border-color: transparent transparent transparent #2A2D35;`). Trượt mở trong `200ms`.
* **Quy tắc Single Border Source:** Tuyệt đối không để hai component liền kề cùng vẽ đường viền tiếp giáp nhau (tránh viền kép `2px` thô kệch).

### 6.2 Hai Chế Độ Màn Hình Xác Thực Thích Ứng (Adaptive Layout)
* **Compact Mode (Viewport < 1000px):** Form Card căn giữa chuẩn Fitts' Law `540x600` (Login/Forgot) và `820x750` (Register 2 cột).
* **Expanded Mode (Viewport ≥ 1000px):** Bố cục chia đôi: Cột Trái Hero Branding `45%`, Cột Phải Form Card `55%`.

---

## 7. Sáu Nguyên Tắc Kỹ Thuật Thực Tiễn Chống AI Slop (Anti-Slop Engineering Principles)

Nhằm đảm bảo sản phẩm mang bản sắc công cụ máy tính để bàn chuyên nghiệp, đáng tin cậy:

1. **Phân Tầng Bề Mặt & Viền Hairline (Layered Surface Elevation & Hairline Borders):** Thay thế bóng mờ neon bằng các tầng sắc thái tối giản (`#0F1013` ➔ `#141619` ➔ `#1B1D22` ➔ `#22252C`) kết hợp đường viền hairline sắc nét `1px solid #2A2D35`.
2. **Vòng Tiêu Điểm Bàn Phím Công Thái Học (Focus-Visible Keyboard Rings — WCAG 2.2):** Giữ nguyên focus ring sắc nét `1px solid -accent-primary` kèm bóng ngoài `rgba(78, 107, 242, 0.25)` khi người dùng chuyển Tab bàn phím.
3. **Trạng Thái Dữ Liệu Rỗng Có Ý Nghĩa (Meaningful Empty States & Contextual Cues):** Mọi danh sách rỗng đều có icon vector tối giản, tiêu đề hướng dẫn rõ ràng và nút hành động gợi ý tiếp theo.
4. **Mật Độ Thông Tin Công Thái Học (Ergonomic Information Density & Hierarchy):** Phân cấp nghiêm ngặt giữa Thông tin chính (`-text-normal: #EDEDED` cỡ `15px`) và Siêu dữ liệu phụ trợ (`-text-muted: #8E929B` cỡ `11px - 13px`), mục tiêu nhấp chuột đạt chuẩn Fitts' Law `44px`.
5. **Phản Hồi Xúc Giác Đầy Đủ 5 Trạng Thái & Tiến Trình Minh Bạch:** Đủ 5 trạng thái: Default, Hover, Pressed (`JavaFXTheme.applyTactileButton`), Focus, Disabled. Khi tác vụ mạng diễn ra, nút hiển thị nhãn tiến trình cụ thể (*"Đang đăng nhập..."*, *"Đang gửi..."*).
6. **Báo Lỗi Trực Quan Ngay Tại Nguồn (Inline Field Validation & Micro-Shake Feedback):** Lỗi gắn trực tiếp dưới trường nhập liệu với viền đỏ `-status-danger`, kết hợp hoạt ảnh rung lắc vi mô `JavaFXTheme.shakeNode()` (`±8px` trong `200ms`) và tự động focus con trỏ.

---

## 8. Quy Chuẩn Vòng Lặp Phát Triển Nguyên Tử (Spec-First ➔ Anti-Slop Code ➔ Atomic Commit)

Mọi hoạt động bổ sung hoặc sửa đổi giao diện phân hệ User BẮT BUỘC tuân thủ chu trình 3 bước khép kín:

1. **Bước 1 — Spec-First & Spec Synchronization (Đặc tả đi trước mã nguồn):**
   - Rà soát và cập nhật tệp đặc tả chi tiết trong `docs/ui-spec/user-screens.md` TRƯỚC KHI viết code.
   - Đảm bảo tên lớp (`Class`), phương thức (`Method`), cấu trúc Node, class CSS và hành vi UI khớp 100% với hiện thực mã nguồn.

2. **Bước 2 — Anti-AI-Slop Code Implementation (Triển khai mã chuẩn mực):**
   - Triển khai mã JavaFX 21 + CSS tuân thủ nghiêm ngặt các nguyên tắc Motion tại Mục 5, Single Border Source tại Mục 6.1 và Anti-Slop tại Mục 7.
   - Sử dụng 100% token từ `src/user/theme.css` và tiện ích từ `JavaFXTheme.java`.

3. **Bước 3 — Atomic Verification & Conventional Commit (Kiểm thử & Lưu vết Git):**
   - Kiểm tra `git diff` sạch sẽ, chạy thử giao diện qua `UserLauncher`.
   - Thực hiện commit Git theo chuẩn Conventional Commits: `type(user-ui): concise message`.

4. **Kỷ Luật Ngữ Cảnh Zero-Noise (Context Hygiene):**
   - Mỗi chu trình chỉ nạp đúng 1 file spec liên quan và file code mục tiêu.
   - Khuyến nghị thực hiện `/compact` sau mỗi vòng lặp nguyên tử để giải phóng bộ nhớ context trước khi chuyển sang task mới.
