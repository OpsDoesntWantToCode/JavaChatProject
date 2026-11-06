# ĐẶC TẢ GIAO DIỆN CỤM 1: XÁC THỰC & TÀI KHOẢN (AUTHENTICATION SPECIFICATION)

> **Module:** Cụm 1 — Xác thực & Thiết lập Tài khoản (Mục 1, 2, 3, 4, 5)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)  
> **Lớp triển khai JavaFX:** `src/user/UserLoginView.java`, `src/user/UserRegisterView.java`, `src/user/UserForgotPasswordView.java`, `src/user/UserHeroSection.java`, `src/user/UserSettingsDialog.java`, `src/user/UserLoginApp.java`

---

## 1. Cấu trúc Giải phẫu & Kiến trúc Thích ứng (Anatomy & Adaptive Layout)

Module Xác thực hỗ trợ hai chế độ hiển thị thích ứng theo độ rộng khung nhìn:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        CHẾ ĐỘ MỞ RỘNG (SPLIT-SCREEN ≥ 1000PX)                          │
│                                                                                        │
│   [CỘT TRÁI: HERO SHOWCASE (45%)]                        [CỘT PHẢI: FORM CARD (55%)]   │
│                                                                                        │
│   💬 DESKTOP CLIENT • JAVA 21 & POSTGRESQL (11.5px)      (Bề mặt Gunmetal nổi bật)     │
│                                                          ┌───────────────────────────┐ │
│   Java Chat System (26px Bold)                           │ Viền: -border-subtle (1px)│ │
│   Giao tiếp thời gian thực bảo mật cao cho desktop.      │ Bo góc: radius.2xl (14px) │ │
│                                                          │ Đệm: space.9 space.10     │ │
│   ┌──────────────────────────────────────────────────┐   │                           │ │
│   │ [🔒] Mã hóa AES-GCM (14.5px Semi-Bold, #FFFFFF)  │   │     Chào mừng trở lại!    │ │
│   │      Khóa riêng từng phiên chat, an toàn dữ liệu.│   │     (26px Bold)           │ │
│   ├──────────────────────────────────────────────────┤   │                           │ │
│   │ [⚡] TCP Socket thuần (14.5px Semi-Bold, #FFFFFF) │   │ TÊN ĐĂNG NHẬP HOẶC...     │ │
│   │      Độ trễ thấp, kết nối liên tục không đứt đoạn│   │ [ tuanminh_2026 (44px)  ] │ │
│   ├──────────────────────────────────────────────────┤   │                           │ │
│   │ [👥] Quản lý hội thoại (14.5px Semi-Bold, #FFFFFF│   │ MẬT KHẨU *                │ │
│   │      Nhắn tin trực tiếp, nhóm chat & bạn bè.     │   │ [ •••••••••••• (44px)  👁]│ │
│   └──────────────────────────────────────────────────┘   │                           │ │
│                                                          │ [    ĐĂNG NHẬP (44px)   ] │ │
│   Đồ án Lập trình Ứng dụng Java — Phân hệ User (12px)    └───────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

1.  **Chế độ Thu gọn (Compact Mode — Viewport < 1000px):**
    *   Kích thước cửa sổ: `540 x 600` (Đăng nhập / Quên mật khẩu), `820 x 750` (Đăng ký).
    *   Thẻ form căn giữa tuyệt đối, ôm sát nội dung (`setMaxHeight(USE_PREF_SIZE)`).
2.  **Chế độ Mở rộng (Expanded Mode — Viewport ≥ 1000px):**
    *   Tự động kích hoạt bố cục chia đôi (Split-Screen): Cột trái Hero Showcase chiếm 45%, Cột phải Form Container chiếm 55%.
    *   Thẻ Form Card nổi bật tự nhiên thông qua sự tương phản giữa bề mặt `-bg-card: #1B1D22` và nền canvas `-bg-canvas: #141619` kết hợp viền mỏng `-border-subtle: #2A2D35`.

---

## 2. Đặc tả Thành phần Giao diện (Component Specifications)

### 2.1 Màn hình Đăng nhập (Mục 5 — `UserLoginView.java`)

#### Bảng Ánh xạ Design Tokens
| Thuộc tính UI | Token Kế thừa từ `base-ui-spec.md` | Giá trị Thực thi |
| :--- | :--- | :--- |
| **Chiều rộng Thẻ** | Cố định Fitts' Law | `460px` (`setMaxWidth(460)`) |
| **Bề mặt Thẻ** | `-bg-card` | `#1B1D22` |
| **Viền Thẻ** | `-border-subtle` | `1px solid #2A2D35` |
| **Bo góc Thẻ** | `radius.2xl` | `14px` |
| **Khoảng đệm Thẻ** | `space.9 space.10` | `36px 40px` |
| **Độ sâu Bề mặt** | Layered Elevation Level 2 | Nổi nhẹ trên `-bg-canvas`, không bóng mờ ảo |
| **Khoảng cách Trường**| `space.4` | `16px` |

#### Cấu trúc Phần tử (Anatomy)
1.  **Khối Tiêu đề (Header):**
    *   Tiêu đề chính: *"Chào mừng trở lại!"* (`font-size: 26px`, Bold, `-text-normal: #EDEDED`).
    *   Phụ đề: *"Rất vui được gặp lại bạn một lần nữa!"* (`font-size: 14px`, `-text-muted: #8E929B`).
2.  **Trường Tên đăng nhập / Email:**
    *   Nhãn: `TÊN ĐĂNG NHẬP HOẶC EMAIL *` (Bold `12.5px`, sao đỏ `-status-danger: #FA777C`).
    *   Ô nhập `txtUsername`: Chiều cao `44px`, nền `-bg-input` (`#0F1013`), bo góc `radius.lg` (`9px`), cỡ chữ `15px`.
3.  **Trường Mật khẩu (`JavaFXTheme.PasswordFieldWrapper`):**
    *   Nhãn: `MẬT KHẨU *`.
    *   Ô nhập: Chiều cao `44px`, bo góc `9px`, tích hợp nút con mắt `👁` cho phép ẩn/hiện mật khẩu tức thời (đồng bộ 2 chiều dữ liệu).
    *   Liên kết *"Quên mật khẩu?"*: Cỡ `13.5px`, màu `-text-link` (`#4E6BF2`).
4.  **Nút bấm Đăng nhập (`.btn-primary`):**
    *   Nền `-accent-primary` (`#4E6BF2`), chiều cao `44px`, bo góc `radius.md` (`8px`), chữ đậm `15px` màu trắng.
    *   Trạng thái Loading: Vô hiệu hóa và hiển thị *"Đang đăng nhập..."*.
5.  **Chân trang:** Liên kết chuyển sang Đăng ký: *"Cần một tài khoản? Đăng ký"*.

---

### 2.2 Màn hình Đăng ký (Mục 1 — `UserRegisterView.java`)

#### Bảng Ánh xạ Design Tokens
| Thuộc tính UI | Token Kế thừa | Giá trị Thực thi |
| :--- | :--- | :--- |
| **Chiều rộng Thẻ** | Cố định 2 Cột | `680px` (mỗi cột ~300px) |
| **Bố cục Lưới** | GridPane 2 Cột | `hgap: 20px (space.5)`, `vgap: 14px` |
| **Kích thước Cửa sổ** | Viewport Chuẩn | `820 x 750` |

#### Phân bố 7 Trường Nhập Liệu
*   **Cột 1 (Thông tin Tài khoản & Bảo mật):**
    1.  Tên đăng nhập (`TextField`, cao `44px`, bo góc `9px`).
    2.  Email (`TextField`, cao `44px`, bo góc `9px`).
    3.  Địa chỉ (`TextField`, cao `44px`, bo góc `9px`).
    4.  Mật khẩu (`PasswordFieldWrapper`, cao `44px`, bo góc `9px`, có nút mắt `👁`).
    5.  **Thanh đo độ mạnh mật khẩu (4-Segment Meter):**
        *   Gồm 4 phân đoạn cao `5px`, bo góc `2.5px` (`radius.xs`), cách nhau `4px` (`space.1`).
        *   0%: 4 phân đoạn mang màu track `-bg-chip` (`#1E2026`).
        *   Yếu: 1 phân đoạn đỏ `-status-danger` (`#FA777C`).
        *   Trung bình: 2 phân đoạn vàng `-status-warning` (`#F0B232`).
        *   Khá: 3 phân đoạn xanh `-accent-primary` (`#4E6BF2`).
        *   Rất an toàn: 4 phân đoạn xanh lá `-status-online` (`#23A55A`) kèm nhãn *"Độ mạnh: Rất an toàn ✓"*.
*   **Cột 2 (Thông tin Cá nhân):**
    1.  Họ và tên (`TextField`, cao `44px`, bo góc `9px`).
    2.  Ngày sinh (JavaFX `DatePicker`, nền `-bg-input`, bo góc `9px`, cao `44px`).
    3.  Giới tính (JavaFX `ComboBox<String>`: Nam, Nữ, Khác):
        *   Nền `-bg-input` (`#0F1013`), bo góc `9px`.
        *   Custom ListCell: Bắt sự kiện `hoverProperty()` (nền `#262932`) và `selectedProperty()` (nền `#4E6BF2`), triệt tiêu xung đột màu.
        *   Popup animation: Fade in + Trượt nhẹ `8px` trong `150ms`.
    4.  Xác nhận mật khẩu (`PasswordFieldWrapper`, cao `44px`, bo góc `9px`).
*   **Nút bấm Đăng ký:** Full-width, cao `44px`, bo góc `8px`, chữ đậm `15px`.

---

### 2.3 Màn hình Khởi tạo lại Mật khẩu (Mục 3 — `UserForgotPasswordView.java`)
*   Thẻ Card: Chiều rộng `460px`, padding `36px 40px`, bo góc `14px`, viền `1px solid #2A2D35`.
*   Trường nhập: Email hoặc Tên đăng nhập (cao `44px`, bo góc `9px`).
*   Thanh tiến trình: `ProgressBar` indeterminate hiển thị hiệu ứng gradient trong thời gian gửi mail mô phỏng.
*   Nút CTA: *"Gửi mật khẩu mới qua Email"* (cao `44px`, bo góc `8px`).

---

### 2.4 Cột Hero Branding Showcase (`UserHeroSection.java`)
*   **Vị trí:** Cột trái 45% khi ở chế độ Fullscreen hoặc chiều rộng cửa sổ ≥ 1000px.
*   **Tiêu đề & Nhãn:** Tiêu đề `26px` Bold, Phụ đề `14px`, nhãn tag hệ thống `11.5px` Bold.
*   **3 Thẻ tính năng công cụ (`.feature-chip`):**
    *   Bố cục hình chữ nhật phẳng bo góc `10px` (`radius.xl`), nền `-bg-chip` (`#1E2026`), viền `1px solid -border-subtle`, đệm `12px 16px`.
    *   Hộp chứa Icon Badge (`iconBadge`): `38x38px`, bo góc `8px` (`radius.md`), nền `rgba(255, 255, 255, 0.08)`, viền `1px solid rgba(255, 255, 255, 0.12)`.
    *   Icon Vector Lucide đơn sắc trắng:
        *   `Mã hóa AES-GCM`: Icon Khóa vector trắng `#FFFFFF`.
        *   `TCP Socket thuần`: Icon Tia sét vector trắng `#FFFFFF`.
        *   `Quản lý hội thoại`: Icon Người dùng vector trắng `#FFFFFF`.

---

### 2.5 Hộp thoại Hồ sơ & Đổi Mật khẩu (Mục 2, 4 — `UserSettingsDialog.java`)
*   **Cơ chế mở:** Nhấp chuột vào Avatar người dùng trên Navigation Rail (Cột 1).
*   **Định dạng:** Modal Dialog căn giữa, bo góc `14px`, nền `-bg-card` (`#1B1D22`).
*   **Cấu trúc 2 Tabs:**
    *   **Tab 1 — Hồ sơ cá nhân (Mục 2):** Cập nhật Họ và tên, Địa chỉ, Ngày sinh, Giới tính.
    *   **Tab 2 — Bảo mật & Đổi mật khẩu (Mục 4):** Nhập Mật khẩu hiện tại, Mật khẩu mới, Xác nhận mật khẩu mới.
