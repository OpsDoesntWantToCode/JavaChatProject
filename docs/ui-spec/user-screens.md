# ĐẶC TẢ CÁC MÀN HÌNH PHÂN HỆ USER (USER SCREENS SPECIFICATION)

> **Phân hệ phụ trách:** Thành viên B — Trần Nguyễn Tuấn Minh (`feat/user-ui`)  
> **Nền tảng:** JavaFX 21 LTS + CSS (`src/user/theme.css`)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md) (4 Trụ cột Design System & Heuristics, Three-Tier Tokens, Quy chuẩn Chuyển động Fluent 2, Lưới 8px, Single Border Source)  
> **Trạng thái:** Toàn bộ 14 tệp JavaFX đã được triển khai hoàn chỉnh tại `src/user/`, tích hợp đầy đủ hệ thống phản hồi xúc giác vi mô (Tactile Micro-Interactions) cho các nút bấm qua `JavaFXTheme`.

---

## 1. Cụm 1: Xác thực & Quản lý Tài khoản (Authentication)

* **Tệp mã nguồn:** `UserLoginView.java`, `UserRegisterView.java`, `UserForgotPasswordView.java`, `UserHeroSection.java`, `UserSettingsDialog.java`, `UserLoginApp.java`.
* **Cơ chế Layout Thích ứng:**
  * **Viewport < 1000px (Compact Mode):** Card căn giữa cửa sổ, ôm sát nội dung (`540x600` cho Login/Forgot, `820x750` cho Register).
  * **Viewport ≥ 1000px (Split-Screen):** Cột trái là Hero Branding Showcase (`45%`), Cột phải chứa Card form (`55%`).

### 1.1 Màn hình Đăng nhập (`UserLoginView.java`)
* **Kích thước Thẻ:** Rộng `460px`, bo góc `14px`, padding `36px 40px`, nền `-bg-card: #1B1D22`, viền `1px solid -border-subtle: #2A2D35`.
* **Thành phần:**
  * Header: Tiêu đề *"Chào mừng trở lại!"* (`26px`, Bold, `-text-normal: #EDEDED`), subtitle (`14px`, `-text-muted: #8E929B`).
  * `txtUsername`: Cao `44px`, bo góc `9px`, nền `-bg-input: #0F1013`.
  * `txtPassword`: Bọc qua `JavaFXTheme.PasswordFieldWrapper`, tích hợp nút con mắt `👁` ẩn/hiện mật khẩu tức thời (tích hợp `JavaFXTheme.applyIconButton` phóng nhẹ `1.08x` khi hover). Link *"Quên mật khẩu?"* (`-text-link: #4E6BF2`).
  * `btnLogin`: Cao `44px`, bo góc `8px`, chữ đậm trắng, hover đổi màu sapphire, tích hợp `JavaFXTheme.applyTactileButton` (co nhẹ `0.975x` trong 70ms khi nhấn, phục hồi trong 80ms), hỗ trợ trạng thái loading (*"Đang đăng nhập..."*).
  * Chân trang: Link chuyển sang Đăng ký (*"Cần một tài khoản? Đăng ký"*).

### 1.2 Màn hình Đăng ký (`UserRegisterView.java`)
* **Kích thước Thẻ:** Rộng `680px`, bo góc `14px`, bố cục `GridPane` 2 cột đối xứng (`hgap: 20px`, `vgap: 14px`).
* **7 Trường Nhập liệu Chuẩn Đề bài:**
  * *Cột 1 (Tài khoản & Bảo mật):* (1) Tên đăng nhập, (2) Email, (3) Địa chỉ, (4) Mật khẩu (có nút mắt `👁`), (5) Thanh đo độ mạnh mật khẩu 4 phân đoạn (Yếu: đỏ, Trung bình: vàng, Khá: xanh lam, Rất an toàn: xanh lá kèm nhãn trạng thái).
  * *Cột 2 (Thông tin cá nhân):* (6) Họ và tên, (7) Ngày sinh (`DatePicker`), Giới tính (`ComboBox<String>`: Nam, Nữ, Khác - Matte Dark), Mật khẩu xác nhận.
* **Nút bấm:** `btnRegister` rộng 100%, cao `44px`, bo góc `8px`, tích hợp `JavaFXTheme.applyTactileButton`. Link quay lại Đăng nhập.

### 1.3 Màn hình Khởi tạo lại Mật khẩu (`UserForgotPasswordView.java`)
* **Thẻ Card:** Rộng `460px`, bo góc `14px`, viền hairline `1px solid -border-subtle`.
* **Trường nhập:** Email hoặc Username; `ProgressBar` indeterminate giả lập quá trình gửi mail SMTP; nút *"Gửi mật khẩu mới qua Email"* (`JavaFXTheme.applyTactileButton`).

### 1.4 Cột Hero Branding Showcase (`UserHeroSection.java`)
* **Bố cục:** Rộng `45%` khi mở rộng. Tiêu đề thương hiệu + 3 thẻ tính năng chữ nhật bo góc `10px`, nền `-bg-chip: #1E2026`:
  1. *Mã hóa AES-GCM*: Icon Khóa vector trắng.
  2. *TCP Socket thuần*: Icon Tia sét vector trắng.
  3. *Quản lý hội thoại*: Icon Người dùng vector trắng.
  * Hộp chứa Icon Badge: `38x38px`, bo góc `8px`, icon vector SVG Lucide màu trắng thuần túy (`#FFFFFF`).

### 1.5 Hộp thoại Hồ sơ & Đổi Mật khẩu (`UserSettingsDialog.java`)
* **Cơ chế:** Modal Dialog căn giữa mở khi nhấp chuột vào Avatar tại Navigation Rail (Cột 1).
  * **Hiệu ứng Xuất hiện (`applyModalEntrance`):** Phóng nhẹ `0.96x ➔ 1.0x` kèm `Fade-in` trong `180ms` (Fluent 2 Decelerate).
  * **Hiệu ứng Đóng (`closeModalWithAnimation`):** Co nhẹ `0.97x` kèm `Fade-out` trong `120ms` (Fluent 2 Accelerate, đóng nhanh hơn mở).
  * Nút đóng ✕ tích hợp `applyIconButton`, nút *"Đóng"* tích hợp `applyTactileButton` kích hoạt `closeModalWithAnimation`.
* **2 Tabs & Chuyển đổi Mượt mà (`applyTabContentTransition`):**
  * Khi người dùng bấm chuyển giữa 2 Tab, nội dung trượt nhẹ `8px` kèm `Fade-in` trong `150ms`.
  * *Tab 1 — Hồ sơ cá nhân:* Cập nhật Họ và tên, Địa chỉ, Ngày sinh, Giới tính. Nút *Đổi ảnh đại diện* và *Lưu thay đổi hồ sơ* (`applyTactileButton`).
  * *Tab 2 — Bảo mật:* Đổi mật khẩu (Mật khẩu cũ, Mật khẩu mới, Xác nhận mật khẩu mới). Nút *Cập nhật mật khẩu* (`applyTactileButton`).

---

## 2. Cụm 2: Khung Không gian Làm việc Chính (Workspace Shell)

* **Tệp mã nguồn:** `UserMainWorkspace.java`.
* **Bố cục 4 Cột & Quy tắc Single Border Source:**

| Cột | Tên Thành phần | Kích thước | Nền (CSS) | Quy tắc Đường viền | Chức năng chính & Hoạt ảnh |
| :---: | :--- | :---: | :--- | :--- | :--- |
| **1** | **Navigation Rail** | `60px` cố định | `-bg-nav-rail: #0F1013` | Chỉ vẽ viền phải: `border-right: 1px solid -border-subtle` | Avatar `40px` (click mở Settings), các nút icon 💬 Chat, 👥 Bạn bè, ⚙️ Cài đặt, 🚪 Đăng xuất (đều tích hợp `applyIconButton` zoom 1.08x hover / 0.94x click). |
| **2** | **Sub-Sidebar** | `280px – 320px` | `-bg-card: #1B1D22` | Chỉ vẽ viền phải: `border-right: 1px solid -border-subtle` | Nút ➕ tạo nhóm (`applyIconButton`), thanh tìm kiếm hội thoại, danh sách chat (avatar, tên, tin nhắn cuối, icon 🔒 nhóm mã hóa, unread badge đỏ). |
| **3** | **Main View** | Co giãn (`Priority.ALWAYS`) | `-bg-canvas: #141619` | Không viền ngoài (`border: none`). Header chỉ viền đáy, Input bar chỉ viền đỉnh. | Nạp động: Khung chat `ChatConversationView` hoặc Trung tâm bạn bè `FriendsManagementView` (chuyển đổi mượt mà với `applyTabContentTransition` 150ms). |
| **4** | **Right Drawer** | `280px` cố định | `-bg-card: #1B1D22` | Chỉ vẽ viền trái: `border-left: 1px solid -border-subtle` | Trượt mở với `JavaFXTheme.animateDrawerOpen` (220ms, TranslateX 280 ➔ 0, Decelerate) và trượt đóng với `animateDrawerClose` (160ms, TranslateX 0 ➔ 280, Accelerate). Chứa `GroupManagementDrawer`. |

---

## 3. Cụm 3: Quản lý Bạn bè & Danh bạ (Friends & Directory)

* **Tệp mã nguồn:** `FriendsManagementView.java`.
* **Bố cục:** Nạp tại Cột 3 (Main View) khi bấm icon 👥 Bạn bè trên Navigation Rail. Gồm thanh 4 Tab phía trên, ô lọc nhanh ở giữa và danh sách thẻ người dùng ở dưới. Chuyển đổi qua lại giữa 4 Tab được mượt mà hóa với `JavaFXTheme.applyTabContentTransition` (150ms, Slide 8px + Fade).
* **4 Tabs Nghiệp vụ:**
  1. **Tab 1 — Đang Online:** Lọc danh sách bạn bè trực tuyến (chấm xanh `-status-online: #23A55A`). Nút thao tác: `💬 Nhắn tin` (`applyTactileButton`) và `➕ Tạo nhóm` (`applyTactileButton`). Nút tùy chọn `•••` (`applyIconButton`).
  2. **Tab 2 — Tất cả Bạn bè:** Hiển thị toàn bộ bạn bè kèm trạng thái Online (chấm xanh) / Offline (chấm xám). Menu ngữ cảnh (`•••` - `applyIconButton`): *Hủy kết bạn* và *Chặn người dùng (Block)* (chữ đỏ cảnh báo).
  3. **Tab 3 — Lời mời Kết bạn:** Danh sách yêu cầu kết bạn đến đang chờ (`PENDING`). Hai nút tương tác: `Đồng ý ✓` (`.btn-primary`, `applyTactileButton`) và `Từ chối ✕` (`.btn-secondary`, `applyTactileButton`).
  4. **Tab 4 — Thêm bạn / Tìm kiếm Người lạ:** Ô tìm kiếm theo username / họ tên (tự động loại trừ người đã chặn). Kết quả hiển thị kèm 3 nút độc lập tích hợp `applyTactileButton`: `+ Kết bạn (Mục 6.b)`, `💬 Nhắn tin ngay (Mục 9)` và `👥 Tạo Group (Mục 9)`.

---

## 4. Cụm 4: Khung Chat & Lịch sử Tin nhắn (Chat Engine)

* **Tệp mã nguồn:** `ChatConversationView.java`, `SpamReportDialog.java`.
* **Kiến trúc Ảo hóa Danh sách:** BẮT BUỘC dùng `ListView<Message>` (VirtualFlow) thay cho ScrollPane để tối ưu bộ nhớ và duy trì 60fps khi lịch sử chat dài. Hỗ trợ cuộn lên đỉnh để tải trang cũ (Lazy Pagination).
* **Cấu trúc Khung Chat:**
  * **Header (Top Bar):** Avatar `40px` kèm chấm online/offline, Họ tên / Tên nhóm, icon ổ khóa 🔒 xanh lá nếu nhóm bật AES-GCM. Cụm nút công cụ tích hợp `JavaFXTheme.applyIconButton`: 🔍 Tìm kiếm tin nhắn, ⚠️ Báo cáo Spam (mở `SpamReportDialog`), ☑️ Chọn nhiều xóa tin, ℹ️ Đóng/mở Right Drawer.
  * **Luồng Bong bóng Tin nhắn:**
    * *Tin gửi đi (Sent):* Căn phải, nền `-bubble-sent: #4E6BF2`, chữ trắng, bo góc `12px` (góc dưới phải `3px`).
    * *Tin nhận được (Received):* Căn trái, nền `-bubble-received: #1E2128`, chữ `-text-normal: #EDEDED`, bo góc `12px` (góc dưới trái `3px`).
    * *Tin Trợ lý AI (Bot):* Căn trái, nền `-bg-ai-bubble: #2B2738`, viền trái tím `3px solid #8B5CF6`, huy hiệu `[AI BOT]`.
  * **Thanh Soạn thảo Tin nhắn (Bottom Bar):** Ô nhập liệu, nút đính kèm 📎 (`applyIconButton`), nút `✨ Gợi ý AI` và nút `Gửi 🚀` (`applyTactileButton`).
  * **Tìm kiếm trong Hội thoại:** Thanh tìm kiếm nội bộ có nút Lên/Xuống để duyệt tin nhắn; tự động cuộn và highlight nền vàng nhạt `1.5s` vào bong bóng tìm thấy.
  * **Xóa Tin nhắn Linh hoạt:** Menu chuột phải hoặc thanh Multi-Select cho phép chọn một hoặc nhiều tin nhắn để *"Xóa tin nhắn phía tôi"*.

---

## 5. Cụm 5: Trợ lý AI trong Hội thoại (AI Assistant)

* **Tệp mã nguồn:** Tích hợp trực tiếp trong `ChatConversationView.java`.
* **Triết lý:** AI xuất hiện bình đẳng như một thành viên trong luồng chat thông thường, sử dụng phong cách tím nhận diện (`-accent-ai: #8B5CF6`).
* **Thành phần & Hành vi:**
  * **Bong bóng AI Bot:** Avatar 🤖, tên hiển thị `Trợ lý AI` kèm badge `[AI BOT]` tím. Hiệu ứng gõ chữ từng từ (Streaming Text) qua `Platform.runLater`.
  * **3 Chế độ Ngữ cảnh (Context Modes):**
    1. *Full-Context:* Đọc ngầm 5-10 tin nhắn gần nhất để tóm tắt hoặc trả lời.
    2. *Single-Message Context:* Chuột phải vào một tin nhắn ➔ *"✨ Hỏi AI về tin này"*.
    3. *Zero-Context:* Tương tác hỏi đáp độc lập.
  * **Smart Reply Chips:** Dải chip phẳng bo góc `8px` ngay trên ô nhập: `✨ Tóm tắt đoạn chat`, `💬 Gợi ý trả lời`, `❓ Hỏi đáp tự do` (tất cả đều tích hợp `JavaFXTheme.applyTactileButton`).
  * **Khung nhập kích hoạt AI (`.ai-active`):** Khi gõ `/ai` hoặc bấm icon ✨, viền ô nhập chuyển sang sắc tím `#8B5CF6`.

---

## 6. Cụm 6: Quản trị Nhóm Chat & Mã hóa Đầu cuối (Group & E2EE)

* **Tệp mã nguồn:** `GroupManagementDrawer.java`, `GroupCreateDialog.java`, `SpamReportDialog.java`.
* **Hộp thoại Tạo Nhóm (`GroupCreateDialog.java`):**
  * Modal Dialog bo góc `14px`, hiệu ứng mở `applyModalEntrance` (180ms), hiệu ứng đóng `closeModalWithAnimation` (120ms), ô nhập Tên nhóm, công tắc toggle *"Bật mã hóa nhóm (E2EE Messenger Style)"* chuẩn phong cách phẳng Obsidian Dark.
  * Danh sách chọn bạn bè kèm `CheckBox` tùy biến (nền phẳng `-bg-input`, khi tick chuyển sắc xanh Sapphire `#4E6BF2` kèm dấu kiểm trắng sắc nét, loại bỏ hoàn toàn ô 3D dập nổi của Modena).
  * Thanh cuộn ẩn rãnh chuẩn Desktop Minimalist 8px với con lăn pill bo tròn `#2E323D`, tương thích hoàn hảo với nền tối.
  * Hỗ trợ nhấp chuột chọn trực tiếp cả dòng bạn bè (Row Click Toggle) kèm hiệu ứng hover `#1E2129`.
  * Nút đóng ✕ (`applyIconButton`), nút *"Hủy"* (`btn-secondary`) và nút *"Tạo nhóm 🚀"* (`btn-primary` kèm `applyTactileButton`, chỉ kích hoạt khi hợp lệ).
* **Hộp thoại Báo cáo Vi phạm (`SpamReportDialog.java`):**
  * Hiệu ứng mở `applyModalEntrance` (180ms), hiệu ứng đóng `closeModalWithAnimation` (120ms).
  * Bộ nút lựa chọn lý do vi phạm (`RadioButton`) tùy biến phẳng Obsidian Dark (chấm tròn xanh Sapphire rực rỡ khi được chọn).
  * Khung nhập mô tả (`TextArea`) khắc phục triệt để lỗi nền trắng Modena bằng cách đồng bộ viewport `.content` với nền tối `-bg-input: #0F1013`, viền hairline `1px solid #2A2D35` bo góc mềm `9px`.
  * Nút đóng ✕ (`applyIconButton`), nút *"Hủy bỏ"* (`btn-secondary`) và nút *"Gửi báo cáo vi phạm ⚠️"* (`.btn-danger-solid` kèm `applyTactileButton`).
* **Ngăn kéo Quản trị Nhóm Cột 4 (`GroupManagementDrawer.java`):**
  * Đổi tên nhóm (icon bút ✏️ - `applyIconButton`).
  * Thêm thành viên mới (nút *"+ Thêm"* - `applyTactileButton` mở danh bạ bạn bè chưa vào nhóm).
  * Danh sách thành viên hiển thị huy hiệu `[Admin]` màu vàng `-status-warning: #F0B232`. Thao tác quản trị viên `•••` (`applyIconButton`).
  * **Ràng buộc Quyền Admin:** Nút thao tác quản trị viên chỉ hiển thị cho Quản trị viên (`btnMore.visibleProperty().bind(isCurrentUserAdmin)`).
  * Nút *"Xóa lịch sử trò chuyện"* và *"Rời khỏi nhóm"* (`applyTactileButton`).
  * Rời nhóm & Giải tán nhóm: Cảnh báo chuyển giao Admin nếu là Quản trị viên cuối cùng.
  * **Banner Mã hóa E2EE:** Icon khiên bảo mật xanh lá, hiển thị thông điệp xác thực khóa đối xứng AES-GCM kiểu Messenger.
