# KẾ HOẠCH TRIỂN KHAI DỰ ÁN — JAVA CHAT SYSTEM (MỐC 20%)

> **Dự án:** Java Chat System (Đồ án Lập trình ứng dụng Java)  
> **Phân vai chính thức:**
> - **Thành viên A:** Lưu Chí Hải (MSSV: 24127030) — Phụ trách Admin UI (9 nhóm chức năng) + Script CSDL PostgreSQL + Phần Thiết kế CSDL & Màn hình Admin trong báo cáo.
> - **Thành viên B:** Trần Nguyễn Tuấn Minh (MSSV: 24127084) — Phụ trách User UI (12 nhóm chức năng) + Khởi tạo Repo Git/Maven + Dựng khung Báo cáo & Phần Màn hình User trong báo cáo.
>
> **Mốc thời gian điều chỉnh (Chiến lược về đích sớm):**
> - *Hạn chót chính thức mốc 20% của môn học:* **22/11/2026**
> - *Mục tiêu nội bộ nhóm hoàn thành mốc 20%:* **05/11/2026** *(Rút ngắn 17 ngày để dồn toàn lực cho mốc 80% — Hạn chót toàn đồ án: 12/12/2026)*.

**Đối chiếu ngày 02/11/2026:** đã có `pom.xml` (Java 21, JavaFX 21.0.5, nguồn `src/`), chưa có class Java hoặc giao diện chạy trong working tree. Các ngày giao việc dưới đây là kế hoạch, không phải bằng chứng hoàn thành. Tài liệu đặc tả và dữ liệu mẫu Admin đang được chuẩn bị trên nhánh `feat/admin-db`, chưa tích hợp vào `main`; các nhiệm vụ UI vẫn chưa đánh dấu hoàn thành.

---

## 1. Yêu cầu Cốt lõi & Chuẩn hóa Cấu trúc Nộp bài 20%

- **Quy tắc đóng gói (Tuyệt đối không sai cấu trúc để tránh bị 0đ):**
  ```text
  NopBai_NhomXX_20pct.zip
  ├── src/
  │   ├── admin/      # Mã nguồn các màn hình Admin (Hải)
  │   └── user/       # Mã nguồn các màn hình User (Minh)
  ├── script/         # Chứa duy nhất file database.sql tạo CSDL
  ├── git/            # Ảnh chụp màn hình git log / commit của Hải và Minh
  └── BaoCao.pdf      # Báo cáo nhóm đầy đủ 5 phần bắt buộc
  ```
- **Quy chuẩn UI (JavaFX 21 + CSS):**
  - Kích thước cửa sổ chính: Chuẩn 1000x700 px (hoặc tỷ lệ tương đương, có khả năng co giãn responsive).
  - Look & Feel: JavaFX CSS (hỗ trợ Light / Dark theme), font chữ hệ thống rõ ràng (`Segoe UI` / `Inter`).
  - Dữ liệu mẫu: Bắt buộc đổ sẵn dữ liệu vào `TableView`, danh sách bạn bè `ListView` và vùng bong bóng chat trong `ScrollPane` để phục vụ chụp hình báo cáo.

---

## 2. Tiến độ Thực hiện Tổng quan (Lộ trình Rút gọn)

- **Chặng 1 (22/10 – 24/10):** Khởi tạo, thống nhất ERD, làm sạch repo, chốt giao thức và công nghệ.
- **Chặng 2 (25/10 – 02/11):** Triển khai giao diện tĩnh độc lập song song (Hải làm Admin, Minh làm User).
- **Chặng 3 (03/11 – 05/11):** Ghép mã nguồn vào `main`, chụp minh chứng Git, hoàn thiện Báo cáo PDF và đóng gói nộp mốc 20%.
- **Chặng 4 (06/11 – 12/12):** Dồn toàn lực cho mốc 80% (TCP Socket Server, pgJDBC, Transaction, AES-GCM, Mail, LLM, đóng gói JAR).

---

## 3. Danh mục Nhiệm vụ & Hạn chót Từng Task (Task Matrix)

### 3.1 Khởi tạo & Cơ sở Dữ liệu (Dùng chung)
- [x] **Setup Repository & Cấu trúc thư mục** *(Hoàn thành: 22/10/2026)*: Khởi tạo Git repo, tạo các thư mục chuẩn `src/admin`, `src/user`, `script`, `git`, `docs`.
- [x] **Thống nhất Công nghệ & Quy tắc AI** *(Hoàn thành: 22/10/2026, cập nhật 29/10/2026)*: Chốt Java 21, chuyển sang JavaFX 21 + CSS, PostgreSQL local, Gson, `AGENTS.md`, `docs/tech-stack.md`.
- [x] **Thiết kế & Hoàn thiện CSDL 13 bảng** *(Hoàn thành: 23/10/2026)*: Hoàn tất `docs/database/schema.md` (tích hợp 13 bảng, sơ đồ Mermaid ERD và cẩm nang vận hành CSDL: UUID phiên, Watermark xóa tin, mã hóa nhóm).
- [x] **Viết Script Tạo CSDL Chính thức** *(Hoàn thành: 23/10/2026)*: Hoàn tất `script/database.sql` (chạy độc lập trên database rỗng, 100% ràng buộc và triggers).
- [x] **Tích hợp Sơ đồ ERD & Cẩm nang vào Schema** *(Hoàn thành: 24/10/2026)*: Nhúng sơ đồ ERD Mermaid trực tiếp vào `docs/database/schema.md`, xóa bỏ các file ảnh và context trùng lặp để tài liệu CSDL đạt chuẩn nguồn chân lý duy nhất.
- [x] **Dọn dẹp Kho lưu trữ** *(Hoàn thành: 24/10/2026)*: Xóa bỏ các tệp nháp rác, archive các tài liệu giải trình cũ, đưa `script/` về trạng thái tinh gọn chỉ chứa 1 file SQL.

---

### 3.2 Phân hệ Quản trị (Admin UI) — Thành viên A: Hải (`src/admin/`)

- [ ] **Task A1 — Khung giao diện chính Admin (`AdminApplication`, `AdminMainView`)** *(Hạn chót: 26/10/2026)*:
  - *Mô tả:* `Stage`/`Scene` kích thước 1000x700; `BorderPane` với sidebar `VBox` gồm 9 mục và vùng nội dung thay đổi (có thể dùng `StackPane`). Header hiển thị danh tính Admin mẫu và nút Đăng xuất; mốc 20% không tuyên bố có xác thực thật.
- [ ] **Task A2 — Quản lý danh sách người dùng (`UserManagementView`)** *(Hạn chót: 27/10/2026)*:
  - *Mô tả:* `TableView` hiển thị ID, username, họ tên, email, trạng thái, ngày tạo; hồ sơ chi tiết đủ địa chỉ, ngày sinh, giới tính trong `UserDetailDialog`. Lọc tên/username/trạng thái `ACTIVE`/`LOCKED`/`DELETED`, sắp xếp tên/ngày tạo. Nút Thêm, Cập nhật, Xóa mềm, Khóa/Mở khóa, Cập nhật mật khẩu; dùng control JavaFX và `Alert` xác nhận.
- [ ] **Task A3 — Hộp thoại chi tiết người dùng (`UserDetailDialog`)** *(Hạn chót: 28/10/2026)*:
  - *Mô tả:* `UserDetailDialog` xem/thêm/sửa hồ sơ. Tách `UserLoginHistoryDialog` và `UserFriendsDialog` để xem lịch sử/bạn bè của đúng user được chọn; `PasswordUpdateDialog` có hai `PasswordField`, không hiển thị hash hoặc lưu mật khẩu mẫu. Mọi hộp thoại có owner và nút đóng/hủy.
- [ ] **Task A4 — Lịch sử đăng nhập toàn hệ thống (`GlobalLoginHistoryView`)** *(Hạn chót: 29/10/2026)*:
  - *Mô tả:* Bảng hiển thị danh sách đăng nhập theo thứ tự thời gian mới nhất (Thời gian đăng nhập, Tên đăng nhập, Họ tên). Có ô lọc theo username hoặc khoảng ngày.
- [ ] **Task A5 — Quản lý nhóm chat (`GroupManagementView` & `GroupDetailDialog`)** *(Hạn chót: 30/10/2026)*:
  - *Mô tả:* Bảng danh sách các nhóm chat (Tên nhóm, Người tạo, Ngày tạo, Cờ mã hóa). Bộ lọc theo tên nhóm, sắp xếp theo tên/ngày tạo. Dialog xem chi tiết: hiển thị danh sách thành viên trong nhóm và danh sách các Admin nhóm.
- [ ] **Task A6 — Quản lý báo cáo Spam (`SpamReportView`)** *(Hạn chót: 31/10/2026)*:
  - *Mô tả:* Bảng danh sách báo cáo vi phạm (Thời gian, Người báo cáo, Người bị báo cáo, Lý do spam). Bộ lọc/sắp xếp theo thời gian hoặc username bị báo cáo. Nút bấm thao tác nhanh: "Khóa tài khoản người bị báo cáo".
- [ ] **Task A7 — Thống kê người dùng đăng ký mới (`NewUsersView`)** *(Hạn chót: 01/11/2026)*:
  - *Mô tả:* Bộ chọn khoảng thời gian (Từ ngày - Đến ngày), ô lọc tên, ComboBox sắp xếp tên/ngày tạo. Bảng hiển thị danh sách người dùng đăng ký mới trong khoảng thời gian đã chọn.
- [ ] **Task A8 — Thống kê số lượng bạn bè & FOF (`FriendStatisticsView`)** *(Hạn chót: 01/11/2026)*:
  - *Mô tả:* Bảng hiển thị danh sách user gồm 2 cột số liệu quan trọng: "Số bạn trực tiếp" và "Số bạn của bạn (Friend-of-Friends)". Bộ lọc nâng cao: lọc theo tên, ComboBox toán tử (`=`, `<`, `>`) kèm ô nhập số lượng bạn bè trực tiếp để lọc.
- [ ] **Task A9 — Thống kê người dùng hoạt động (`ActiveUsersView`)** *(Hạn chót: 02/11/2026)*:
  - *Mô tả:* Bộ chọn khoảng thời gian (Từ ngày - Đến ngày). Bảng kết quả hiển thị danh sách user kèm 3 cột số liệu: Lượt mở app, Số người đã chat, Số nhóm đã chat. Có bộ lọc tên và bộ lọc số lượng hoạt động (`=`, `<`, `>`).
- [ ] **Task A10 — Biểu đồ Đăng ký mới theo năm (`RegistrationChartView`)** *(Hạn chót: 03/11/2026)*:
  - *Mô tả:* `ComboBox` chọn năm; JavaFX Charts `BarChart<String, Number>` với `CategoryAxis` gồm đủ 12 tháng và `NumberAxis` là số người đăng ký mới, có tháng giá trị 0; dùng dữ liệu mẫu.
- [ ] **Task A11 — Biểu đồ Người dùng hoạt động theo năm (`ActivityChartView`)** *(Hạn chót: 03/11/2026)*:
  - *Mô tả:* `ComboBox` chọn năm, JavaFX `BarChart` với đủ 12 tháng; trục tung đếm DISTINCT người mở ứng dụng mỗi tháng, không đếm số phiên mở app.

---

### 3.3 Phân hệ Người dùng (User UI) — Thành viên B: Minh (`src/user/`)

- [ ] **Task U1 — Đăng nhập & Đăng ký tài khoản (`UserLoginApp`, `UserRegisterView`)** *(Hạn chót: 26/10/2026)*:
  - *Mô tả:* Giao diện đăng nhập hiện đại (username, password, nút Đăng nhập, link Quên mật khẩu, link Đăng ký). Giao diện Đăng ký tài khoản đầy đủ các trường theo đề bài: tên đăng nhập, họ tên, địa chỉ, ngày sinh (date picker/text), giới tính (Radio/ComboBox), email, mật khẩu.
- [ ] **Task U2 — Quên mật khẩu & Hồ sơ cá nhân (`ForgotPasswordDialog`, `UserProfileDialog`)** *(Hạn chót: 27/10/2026)*:
  - *Mô tả:* Dialog quên mật khẩu: nhập email để nhận mật khẩu ngẫu nhiên. Dialog thông tin cá nhân: cập nhật họ tên, địa chỉ, ngày sinh, giới tính; tab hoặc vùng riêng để thực hiện Đổi mật khẩu (mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới).
- [ ] **Task U3 — Khung giao diện chat chính (`UserMainChatView`)** *(Hạn chót: 28/10/2026)*:
  - *Mô tả:* Cửa sổ chính 1000x700 chia layout 3 vùng: Cột trái (Thanh tìm kiếm, Tabs chuyển đổi Bạn bè / Nhóm chat), Vùng giữa (Khung chat hội thoại), Vùng phải (Thông tin người đang chat / danh sách thành viên nhóm).
- [ ] **Task U4 — Danh sách bạn bè & Quản lý quan hệ (`FriendListView`)** *(Hạn chót: 29/10/2026)*:
  - *Mô tả:* Danh sách hiển thị bạn bè có avatar/tên, chấm màu trạng thái Online (xanh) / Offline (xám). Ô lọc bạn bè theo tên. Menu ngữ cảnh (chuột phải) hoặc nút thao tác: "Hủy kết bạn", "Chặn người dùng (Block)" (kèm thông báo sau khi block sẽ không thể tìm thấy hoặc gửi kết bạn lại).
- [ ] **Task U5 — Danh sách yêu cầu kết bạn (`FriendRequestDialog`)** *(Hạn chót: 29/10/2026)*:
  - *Mô tả:* Danh sách các lời mời kết bạn đang chờ (`PENDING`), có ô lọc theo tên người gửi. Mỗi dòng có nút "Đồng ý" (Accept) và nút "Từ chối" (Reject).
- [ ] **Task U6 — Tab Bạn bè đang Online (`OnlineFriendsView`)** *(Hạn chót: 30/10/2026)*:
  - *Mô tả:* Danh sách lọc riêng những bạn bè đang có trạng thái Online. Có ô lọc tên, nút "Chat ngay" để mở nhanh khung chat, nút "Tạo nhóm" với người đó.
- [ ] **Task U7 — Tìm kiếm người dùng hệ thống (`UserSearchDialog`)** *(Hạn chót: 30/10/2026)*:
  - *Mô tả:* Ô tìm kiếm theo tên đăng nhập hoặc họ tên. Bảng hiển thị kết quả (tự động ẩn những người đã chặn mình). Trên từng kết quả có nút: "Gửi yêu cầu kết bạn", "Chat nhanh", hoặc "Tạo group".
- [ ] **Task U8 — Khung chat 1-1 & Lịch sử tin nhắn (`DirectChatView`)** *(Hạn chót: 31/10/2026)*:
  - *Mô tả:* Vùng hiển thị tin nhắn dạng bong bóng (chat bubbles: tin mình gửi bên phải màu xanh, tin đối phương bên trái màu xám). Thanh tiêu đề trên cùng gồm: Tên/Avatar, trạng thái online/offline, nút "Báo cáo Spam", nút "Xóa toàn bộ lịch sử chat", ô tìm kiếm tin nhắn trong hội thoại. Phía dưới là ô nhập tin nhắn, nút "Gửi", nút "Gợi ý AI (LLM)".
- [ ] **Task U9 — Tìm kiếm tin nhắn trong hội thoại & toàn cục (`ChatSearchDialog`)** *(Hạn chót: 01/11/2026)*:
  - *Mô tả:* (1) Thanh tìm kiếm trong cuộc trò chuyện 1 người có nút Lên / Xuống (Next/Prev) để cuộn màn hình nhảy đến đúng đoạn chat. (2) Hộp thoại tìm kiếm toàn cục: gõ từ khóa tìm kiếm xuyên suốt tất cả các cuộc hội thoại, click vào kết quả sẽ mở đúng hội thoại và nhảy tới vị trí tin nhắn đó.
- [ ] **Task U10 — Xóa tin nhắn linh hoạt (`MessageContextMenu`)** *(Hạn chót: 01/11/2026)*:
  - *Mô tả:* Cho phép click chuột phải lên từng tin nhắn hoặc chọn nhiều tin để thực hiện: "Xóa tin nhắn phía tôi" (xóa lẻ 1 hoặc vài dòng lịch sử).
- [ ] **Task U11 — Trợ lý gợi ý tin nhắn LLM (`LLMAssistPopup`)** *(Hạn chót: 02/11/2026)*:
  - *Mô tả:* Nút icon AI cạnh ô gõ tin nhắn. Khi bấm sẽ hiện popup/dialog gợi ý: chọn phong cách (Chỉnh sửa lịch sự hơn, Rút gọn, Mở rộng câu văn), hiển thị kết quả gợi ý từ AI kèm nút "Áp dụng vào ô chat".
- [ ] **Task U12 — Tạo nhóm & Quản lý nhóm chat (`GroupChatView`, `GroupCreateDialog`)** *(Hạn chót: 02/11/2026)*:
  - *Mô tả:* Dialog tạo nhóm: nhập tên nhóm, chọn/nhập tối thiểu 1 username thành viên, checkbox "Bật mã hóa nhóm". Khung chat nhóm: Vùng hội thoại nhóm; Sidebar bên phải hiển thị tên nhóm (cho phép đổi tên), nút "Thêm thành viên", danh sách thành viên có huy hiệu "Admin nhóm". Nút "Gán Admin" cho thành viên và nút "Xóa khỏi nhóm" (chỉ hiển thị quyền xóa cho Admin nhóm).
- [ ] **Task U13 — Hộp thoại Báo cáo Spam (`SpamReportDialog`)** *(Hạn chót: 02/11/2026)*:
  - *Mô tả:* Dialog bật lên từ thanh chat: hiển thị tên người bị báo cáo, ô nhập lý do spam/quấy rối, nút "Gửi báo cáo".

---

### 3.4 Tích hợp, Báo cáo & Đóng gói Nghiệm thu 20% (Làm chung)
- [ ] **Task C1 — Khung Báo cáo Đồ án (`bao-cao-draft.docx` / LaTeX)** *(Hạn chót: 03/11/2026)*: Minh dựng khung báo cáo đầy đủ 5 mục bắt buộc: (1) Thông tin nhóm, (2) Bảng phân công, (3) Bảng đóng góp tổng 100% theo thực tế (50-50 mới là dự kiến), (4) Thiết kế CSDL (Hải viết và dán ERD), (5) Danh sách màn hình (chụp ảnh Admin và User).
- [ ] **Task C2 — Ghép mã nguồn & Kiểm thử biên dịch** *(Hạn chót: 03/11/2026)*: Tạo PR gộp `feat/admin-db` và `feat/user-ui` vào nhánh `main`. Kiểm tra biên dịch không có bất kỳ lỗi cú pháp nào.
- [ ] **Task C3 — Chụp minh chứng Git (`git/`)** *(Hạn chót: 04/11/2026)*: Chụp lịch sử commit của Hải lưu vào `git/commits_Hai.png`, chụp commit của Minh lưu vào `git/commits_Minh.png`.
- [ ] **Task C4 — Xuất Báo cáo PDF & Đóng gói ZIP** *(Hạn chót: 05/11/2026)*: Xuất file `BaoCao.pdf`, kiểm tra nén file zip gồm đúng 3 thư mục `src/`, `script/`, `git/` và `BaoCao.pdf`.

---

## 4. Định hướng Tiếp nối cho Mốc 80% (06/11 – 12/12/2026)

Ngay sau khi nghiệm thu mốc 20%, nhóm sẽ chuyển sang giai đoạn lập trình logic thực tế:
- [ ] **Socket TCP Server** *(Tuần 1: 06/11 – 13/11/2026)*: Xây dựng Server đa luồng, quản lý danh sách socket kết nối online, định tuyến tin nhắn realtime và lưu tin nhắn offline.
- [ ] **Tầng Dữ liệu pgJDBC** *(Tuần 2: 14/11 – 21/11/2026)*: Server kết nối PostgreSQL qua pgJDBC, triển khai DAO với PreparedStatement, transaction và advisory lock theo `docs/database/schema.md`. Nếu cần connection pool, phải chọn phương án theo stack được duyệt, không tự thêm framework.
- [ ] **Tin nhắn Cá nhân & Nghiệp vụ Bạn bè** *(Tuần 3: 22/11 – 29/11/2026)*: Chat 1-1 realtime/offline, Watermark xóa tin, Quản lý bạn bè, Block, Báo cáo Spam.
- [ ] **Mã hóa nhóm AES-GCM & Dịch vụ Ngoài** *(Tuần 4: 30/11 – 06/12/2026)*: Chat nhóm mã hóa Server-side AES-GCM, Jakarta Mail reset pass, Java 21 `HttpClient` gọi LLM API.
- [ ] **Thống kê JavaFX Charts & Đóng gói JAR** *(Tuần 5: 07/12 – 12/12/2026)*: Tích hợp số liệu thật vào `BarChart` Admin (UI dữ liệu mẫu đã thuộc mốc 20%); đóng gói `Admin.jar`, `User.jar` kèm cấu hình môi trường, quay video demo và hoàn tất báo cáo cuối kỳ. Thông tin DB chỉ dùng phía Server, không phát credential DB cho client.
