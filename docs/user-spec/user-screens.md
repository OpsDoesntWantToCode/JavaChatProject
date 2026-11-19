# ĐẶC TẢ CÁC MÀN HÌNH PHÂN HỆ USER (USER SCREENS SPECIFICATION)

> **Phân hệ phụ trách:** Thành viên B — Trần Nguyễn Tuấn Minh (`feat/user-ui`)  
> **Nền tảng:** JavaFX 21 LTS + CSS (`src/user/theme.css`)  
> **Nguồn chân lý cao nhất:** [final-project.pdf](../final-project.pdf) (Trang 3: Phân hệ dành cho người sử dụng, gồm 12 nhóm chức năng chuẩn)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md) (4 Trụ cột Design System & Heuristics, Three-Tier Tokens, Quy chuẩn Chuyển động Fluent 2, Lưới 8px, Single Border Source)  
> **Trạng thái:** Toàn bộ 19 tệp ảnh minh chứng evidence đã được chụp và định danh chuẩn xác 1:1 theo số thứ tự 12 chức năng trong [final-project.pdf](../final-project.pdf), lưu tại `docs/user-spec/evidence/` kèm bảng băm checksum SHA-256 trong `manifest.json`.

---

## 0. Bảng Ánh xạ Minh chứng Đối chiếu 1:1 với Đề bài (final-project.pdf)

Để giảng viên và hội đồng nghiệm thu dễ dàng chấm điểm theo thang tiêu chí mốc 20%, toàn bộ 19 ảnh minh chứng giao diện được định danh chuẩn xác theo số thứ tự 12 chức năng trong [final-project.pdf](../final-project.pdf):

| Mục Đề bài | Tên Chức năng theo Đề bài | Tệp Mã nguồn JavaFX | Tệp Ảnh Minh chứng Evidence |
| :---: | :--- | :--- | :--- |
| **1** | **Đăng ký tài khoản** | `UserRegisterView.java` | `u1-register.png` |
| **2** | **Cập nhật thông tin tài khoản** (Hiển thị & Điều chỉnh) | `UserSettingsDialog.java` | `u2-profile-view.png`<br>`u2-profile-edit.png` |
| **3** | **Khởi tạo lại mật khẩu** (Mật khẩu random gửi tới email) | `UserForgotPasswordView.java` | `u3-forgot-password.png` |
| **4** | **Cập nhật mật khẩu** | `UserSettingsDialog.java` (Tab Bảo mật) | `u4-change-password.png` |
| **5** | **Đăng nhập** | `UserLoginView.java` | `u5-login.png` |
| **6** | **Danh sách bạn bè** (Online/Offline, Hủy bạn, Block tài khoản) | `FriendsManagementView.java` (Tab 2) | `u6-friends-all.png` |
| **7** | **Danh sách yêu cầu kết bạn** (Lọc tên, Đồng ý/Từ chối) | `FriendsManagementView.java` (Tab 3) | `u7-friends-pending.png` |
| **8** | **Danh sách bạn bè đang online** (Lọc tên, Chat, Tạo group) | `FriendsManagementView.java` (Tab 1) | `u8-friends-online.png` |
| **9** | **Tìm người theo username/tên** (Ẩn người block, Chat, Group, Kết bạn) | `FriendsManagementView.java` (Tab 4) | `u9-friends-search.png` |
| **10** | **Báo cáo spam** khi người đó chat với mình | `SpamReportDialog.java` | `u10-spam-report.png` |
| **11** | **Chat cá nhân, xem/xóa lịch sử, tìm kiếm, LLM** | `ChatConversationView.java`<br>`UserDirectDrawer.java`<br>`GlobalSearchDialog.java` | `u11-workspace-direct.png`<br>`u11-direct-chat-details.png`<br>`u11-message-multiselect.png`<br>`u11-global-search.png`<br>`u11-smart-replies.png` |
| **12** | **Nhóm chat, mã hóa nhóm, quản lý thành viên & admin** | `GroupManagementDrawer.java`<br>`GroupCreateDialog.java`<br>`MemberAddDialog.java` | `u12-workspace-group.png`<br>`u12-group-create.png`<br>`u12-member-add.png` |

---

## 1. Cụm 1: Quản lý Tài khoản & Xác thực (Mục 1 – Mục 5)

* **Tệp mã nguồn:** `UserRegisterView.java`, `UserSettingsDialog.java`, `UserForgotPasswordView.java`, `UserLoginView.java`, `UserLoginApp.java`.
* **Cơ chế Layout Thích ứng (Centered Card sang trọng chuẩn Telegram Desktop / Discord):**
  * Ở mọi kích thước cửa sổ (từ mặc định `540x600` đến Fullscreen `1920x1080`), các thẻ form luôn được căn giữa hoàn hảo trên nền Ambient Canvas Obsidian sâu lắng (`radial-gradient`).
  * 100% Vector & CSS: Không phụ thuộc tài nguyên ảnh ngoài, loại bỏ hoàn toàn hiện tượng bể pixel, răng cưa viền hay vỡ hạt trên màn hình High-DPI.

### 1.1 Mục 1: Màn hình Đăng ký tài khoản (`u1-register.png`)
* **Kích thước Thẻ:** Rộng `680px`, bo góc `14px`, bố cục `GridPane` 2 cột đối xứng (`hgap: 20px`, `vgap: 14px`).
* **7 Trường Nhập liệu Chuẩn Đề bài:**
  * *Cột 1 (Tài khoản & Bảo mật):* (1) Tên đăng nhập, (2) Email, (3) Địa chỉ, (4) Mật khẩu (có nút mắt `👁`), (5) Thanh đo độ mạnh mật khẩu 4 phân đoạn (Yếu: đỏ, Trung bình: vàng, Khá: xanh lam, Rất an toàn: xanh lá kèm nhãn trạng thái).
  * *Cột 2 (Thông tin cá nhân):* (6) Họ và tên, (7) Ngày sinh (`DatePicker`), Giới tính (`ComboBox<String>`: Nam, Nữ, Khác - Matte Dark), Mật khẩu xác nhận.
* **Nút bấm:** `btnRegister` rộng 100%, cao `44px`, bo góc `8px`, tích hợp `JavaFXTheme.applyTactileButton`. Link quay lại Đăng nhập.

### 1.2 Mục 2: Cập nhật thông tin tài khoản (`u2-profile-view.png` & `u2-profile-edit.png`)
* **Màn hình Hiển thị Hồ sơ (`u2-profile-view.png`):**
  * Thẻ hồ sơ cá nhân sang trọng hiển thị Avatar xem trước, Tên hiển thị, Tên đăng nhập `@tuanminh_2026`, Tag trạng thái trực tuyến xanh lá, Hộp thư Email cố định, Ngày sinh, Giới tính, Địa chỉ cư trú.
  * Cụm nút hành động gồm nút *"Đóng"* và nút *"✏️ Chỉnh sửa thông tin hồ sơ"* nổi bật giúp người dùng dễ dàng chuyển sang chế độ cập nhật.
* **Màn hình Khi đang Thay đổi/Điều chỉnh Hồ sơ (`u2-profile-edit.png`):**
  * Banner thông báo trạng thái `ℹ️ Đang ở chế độ chỉnh sửa thông tin cá nhân`.
  * Các ô nhập liệu Họ và tên, Ngày sinh (`DatePicker`), Giới tính (`ComboBox`), Địa chỉ cư trú chuyển sang trạng thái active với viền sáng Sapphire. Tên đăng nhập và Email được giữ cố định.
  * Nút bấm: *"Hủy bỏ"* và *"💾 Lưu thay đổi hồ sơ"* (`.btn-primary`).

### 1.3 Mục 3: Khởi tạo lại mật khẩu ngẫu nhiên qua Email (`u3-forgot-password.png`)
* **Quy định đề bài:** *"Khởi tạo lại mật khẩu: mật khẩu sẽ được random và gởi tới email."*
* **Thẻ Card:** Rộng `460px`, bo góc `14px`, viền hairline `1px solid -border-subtle`.
* **Trường nhập:** Email hoặc Username.
* **Cơ chế:** Nút *"Gửi mật khẩu ngẫu nhiên qua Email"* hiển thị thông báo đã tạo mật khẩu ngẫu nhiên và gửi tới hòm thư người dùng theo đúng yêu cầu đề bài.

### 1.4 Mục 4: Cập nhật mật khẩu (`u4-change-password.png`)
* **Vị trí:** Hộp thoại Cài đặt (Tab Bảo mật & Đổi mật khẩu), dành cho người dùng đã đăng nhập vào hệ thống.
* **Các trường nhập:**
  1. *Mật khẩu hiện tại* (mật khẩu cũ cần xác thực an toàn).
  2. *Mật khẩu mới* (kèm thanh đo độ mạnh mật khẩu thời gian thực 4 phân đoạn).
  3. *Xác nhận mật khẩu mới*.
* **Nút bấm:** *"Cập nhật mật khẩu"* (`applyTactileButton`), kiểm tra trùng khớp và độ mạnh trước khi cập nhật.

### 1.5 Mục 5: Màn hình Đăng nhập (`u5-login.png`)
* **Kích thước Thẻ:** Rộng `460px`, bo góc `14px`, padding `36px 40px`, nền `-bg-card: #1B1D22`, viền `1px solid -border-subtle: #2A2D35`, căn giữa tuyệt đối trên nền Ambient Canvas (`radial-gradient`).
* **Thành phần:**
  * Header: Tiêu đề *"Chào mừng trở lại!"* (`26px`, Bold, `-text-normal: #EDEDED`), subtitle (`14px`, `-text-muted: #8E929B`).
  * `txtUsername`: Cao `44px`, bo góc `9px`, nền `-bg-input: #0F1013`.
  * `txtPassword`: Bọc qua `JavaFXTheme.PasswordFieldWrapper`, tích hợp nút con mắt `👁` ẩn/hiện mật khẩu tức thời. Link *"Quên mật khẩu?"* chuyển hướng tới Mục 3.
  * `btnLogin`: Cao `44px`, bo góc `8px`, chữ đậm trắng, hover đổi màu sapphire, tích hợp `JavaFXTheme.applyTactileButton`.
  * Chân trang: Link chuyển sang Đăng ký (*"Cần một tài khoản? Đăng ký"* - Mục 1).

---

## 2. Cụm 2: Quản lý Bạn bè & Danh bạ (Mục 6 – Mục 9)

* **Tệp mã nguồn:** `FriendsManagementView.java`.
* **Bố cục:** Nạp tại Cột 3 (Main View) khi bấm icon 👥 Bạn bè trên Navigation Rail. Gồm thanh 4 Tab phía trên, ô lọc nhanh ở giữa và danh sách thẻ người dùng ở dưới.

### 2.1 Mục 6: Danh sách bạn bè (`u6-friends-all.png`)
* **Quy định đề bài:** *"Hiển thị danh sách và cho lọc theo tên (cho thấy trạng thái online/offline), Tìm theo tên và yêu cầu kết bạn, Hủy kết bạn, Block tài khoản và hủy kết bạn (người đó sẽ không thể yêu cầu kết bạn lại được)."*
* **Minh chứng thể hiện:** Tab "Tất cả bạn bè" hiển thị toàn bộ bạn bè có chấm trạng thái Online (xanh) / Offline (xám), ô tìm kiếm lọc tên.
* **Menu ngữ cảnh trực quan (Action Menu Popup):** Đang mở tại dòng bạn bè, thể hiện rõ 2 tùy chọn độc lập:
  * `✕ Hủy kết bạn (Mục 6.c)`
  * `🚫 Chặn người dùng (Block - Mục 6.d)` kèm ghi chú rõ người bị chặn sẽ không thể tìm thấy hoặc gửi lại kết bạn.

### 2.2 Mục 7: Danh sách yêu cầu kết bạn (`u7-friends-pending.png`)
* **Quy định đề bài:** *"Hiển thị danh sách và cho lọc theo tên, Đồng ý/từ chối kết bạn."*
* **Minh chứng thể hiện:** Tab "Lời mời kết bạn", danh sách các lời mời chờ xử lý, ô lọc tên, hai nút bấm tương tác: `Đồng ý ✓` (`.btn-primary`) và `Từ chối ✕` (`.btn-secondary`).

### 2.3 Mục 8: Danh sách bạn bè đang online (`u8-friends-online.png`)
* **Quy định đề bài:** *"Cho lọc theo tên, có thể chat, tạo group."*
* **Minh chứng thể hiện:** Tab "Đang Online" lọc riêng bạn bè trực tuyến (chấm xanh lá). Từng dòng có nút `💬 Nhắn tin` để mở ngay hội thoại và nút `➕ Tạo nhóm` để bắt đầu nhóm mới với người đó.

### 2.4 Mục 9: Tìm người dựa vào tên đăng nhập hoặc tên (`u9-friends-search.png`)
* **Quy định đề bài:** *"Tìm người dựa vào tên đăng nhập hoặc tên (không thấy người block mình). Sau đó có thể chat, tạo group."*
* **Minh chứng thể hiện:** Tab "+ Thêm bạn mới / Tìm kiếm người lạ", ô tìm kiếm người dùng trong hệ thống (tự động loại trừ người đã chặn). Mỗi kết quả hiển thị kèm 3 nút độc lập: `+ Kết bạn (Mục 6.b)`, `💬 Nhắn tin ngay (Mục 9)` và `👥 Tạo Group (Mục 9)`.

---

## 3. Cụm 3: Báo cáo Vi phạm Spam (Mục 10)

* **Tệp mã nguồn:** `SpamReportDialog.java`.
* **Ảnh minh chứng:** `u10-spam-report.png`.
* **Quy định đề bài:** *"Báo cáo spam khi người đó chat với mình và mình không thích."*
* **Thành phần:**
  * Thẻ người dùng bị báo cáo (Avatar, tên hiển thị, username).
  * Bộ nút lựa chọn lý do vi phạm (`RadioButton` tùy biến phẳng Obsidian Dark): Spam quảng cáo/lừa đảo, Quấy rối/xúc phạm, Giả mạo danh tính.
  * Khung nhập mô tả chi tiết (`TextArea`).
  * Nút `Hủy bỏ` và nút `Gửi báo cáo vi phạm ⚠️` (`.btn-danger`).

---

## 4. Cụm 4: Khung Chat, Lịch sử Tin nhắn & Trợ lý AI (Mục 11)

* **Tệp mã nguồn:** `ChatConversationView.java`, `UserDirectDrawer.java`, `GlobalSearchDialog.java`.
* **Danh mục ảnh minh chứng:**
  * **`u11-workspace-direct.png`:** Không gian làm việc Chat 1-1 tổng thể (Cột 1 Navigation Rail, Cột 2 Sub-Sidebar danh sách chat, Cột 3 Khung chat chính, Cột 4 Ngăn kéo Right Drawer chi tiết người chat).
  * **`u11-direct-chat-details.png` (Mục 11.c):** Luồng bong bóng chat 1-1 chi tiết (tin mình gửi căn phải màu xanh, tin đối phương căn trái màu xám, thời gian gửi, avatar).
  * **`u11-message-multiselect.png` (Mục 11.e):** Tính năng xóa 1 hoặc vài dòng trong lịch sử chat. Kích hoạt chế độ chọn nhiều với thanh công cụ nổi (Floating Bar) hiển thị số tin nhắn đã chọn và nút *"Xóa tin nhắn phía tôi"*.
  * **`u11-global-search.png` (Mục 11.g):** Hộp thoại tìm kiếm chuỗi trong lịch sử chat với tất cả mọi người. Phân loại kết quả theo Cuộc trò chuyện và Tin nhắn, bôi vàng từ khóa tìm thấy.
  * **`u11-smart-replies.png` (Mục 11.h):** Gọi LLM để chỉnh hoặc gợi ý chat cho người dùng. Dải chip gợi ý nhanh (Smart Reply Chips: Tóm tắt đoạn chat, Gợi ý trả lời, Hỏi đáp) và bong bóng phản hồi của Trợ lý AI Bot với huy hiệu `[AI BOT]` tím.

---

## 5. Cụm 5: Nhóm Chat & Quản lý Nhóm (Mục 12)

* **Tệp mã nguồn:** `GroupManagementDrawer.java`, `GroupCreateDialog.java`, `MemberAddDialog.java`.
* **Danh mục ảnh minh chứng:**
  * **`u12-workspace-group.png` (Mục 12.f, 12.g):** Không gian làm việc Nhóm chat, hiển thị biểu tượng ổ khóa 🔒 xanh lá minh chứng nhóm bật mã hóa AES-GCM, thanh tiêu đề nhóm và ngăn kéo bên phải hiển thị danh sách thành viên có huy hiệu `[Admin]`.
  * **`u12-group-create.png` (Mục 12.a, 12.g):** Hộp thoại Tạo nhóm chat mới. Cho phép nhập tên nhóm, chọn tối thiểu 1 thành viên từ danh sách bạn bè và checkbox *"Bật mã hóa nhóm (mô phỏng)"*.
  * **`u12-member-add.png` (Mục 12.c):** Hộp thoại Thêm thành viên vào nhóm chat dành cho quản trị viên nhóm.
