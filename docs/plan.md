# KẾ HOẠCH TRIỂN KHAI DỰ ÁN — JAVA CHAT SYSTEM (MỐC 20%)

> **Dự án:** Java Chat System (Đồ án Lập trình ứng dụng Java)  
> **Phân vai chính thức:**
> - **Thành viên A:** Lưu Chí Hải (MSSV: 24127030) — Phụ trách Admin UI (9 nhóm chức năng) + Script CSDL PostgreSQL + Phần Thiết kế CSDL & Màn hình Admin trong báo cáo.
> - **Thành viên B:** Trần Nguyễn Tuấn Minh (MSSV: 24127084) — Phụ trách User UI (12 nhóm chức năng) + Khởi tạo Repo Git/Maven + Dựng khung Báo cáo & Phần Màn hình User trong báo cáo.
>
> **Mốc thời gian điều chỉnh (Chiến lược về đích sớm):**
> - *Hạn chót chính thức mốc 20% của môn học:* **22/11/2026**
> - *Mục tiêu nội bộ nhóm hoàn thành mốc 20%:* **05/11/2026** *(Rút ngắn 17 ngày để dồn toàn lực cho mốc 80% — Hạn chót toàn đồ án: 12/12/2026)*.

**Đối chiếu ngày 16/11/2026:** Đã hoàn thành toàn bộ mã nguồn giao diện Desktop JavaFX 21 + CSS cho cả hai phân hệ Admin (`src/admin/`) và User (`src/user/`) cùng cấu hình Maven `pom.xml`. CSDL PostgreSQL 13 bảng hoàn chỉnh tại `script/database.sql`. Tiến hành tích hợp (merge) hai nhánh `feat/admin-db` và `feat/user-ui`, chuẩn bị minh chứng Git và xuất Báo cáo PDF mốc 20%.

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
- **Tiêu chí nghiệm thu mốc 20%:**
  1. File `BaoCao.pdf` đủ 5 phần theo đề bài (Thông tin nhóm, Bảng phân công, Bảng đóng góp tổng 100%, Thiết kế CSDL, Danh sách màn hình).
  2. Mã nguồn mở lên là chạy được (không phụ thuộc IDE, không lỗi package/đường dẫn tuyệt đối cá nhân).
  3. Đổ sẵn dữ liệu mẫu (mock/dummy data) để chụp hình giao diện vào báo cáo.
  4. Script SQL tạo CSDL sạch sẽ, chạy 1 lần là tạo đủ bảng và quan hệ trên database rỗng.
  5. Minh chứng Git rõ ràng, thể hiện sự cộng tác liên tục của cả 2 thành viên.

---

## 2. Phân công Công việc Cụ thể (Ma trận Trách nhiệm)

| Hạng mục công việc | Người phụ trách chính | Người phối hợp | Sản phẩm đầu ra | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **Setup Project, Git, Maven** | Minh (Thành viên B) | Hải | `pom.xml`, cấu trúc `src/`, `.gitignore` | **Hoàn thành** |
| **Thiết kế CSDL (13 bảng) & SQL Script** | Hải (Thành viên A) | Minh | `script/database.sql`, `docs/database/schema.md` | **Hoàn thành** |
| **Màn hình Admin (9 nhóm chức năng)** | Hải (Thành viên A) | — | Thư mục `src/admin/` (JavaFX 21 + CSS) | **Hoàn thành** |
| **Màn hình User (12 nhóm chức năng)** | Minh (Thành viên B) | — | Thư mục `src/user/` (JavaFX 21 + CSS) | **Hoàn thành** |
| **Viết Báo cáo mốc 20% (Khung + User)**| Minh (Thành viên B) | — | File `BaoCao.pdf` (Tổng hợp chung) | **Đang hoàn thiện** |
| **Viết Báo cáo mốc 20% (DB + Admin)**  | Hải (Thành viên A) | — | Nội dung mục 4 & 5 trong Báo cáo | **Đang hoàn thiện** |
| **Chụp ảnh Git commit 2 thành viên** | Cả hai | Cả hai | `git/commits_Hai.png`, `git/commits_Minh.png` | **Hoàn thành** |
| **Đóng gói file nộp bài ZIP** | Minh (Thành viên B) | Hải review | File zip nộp môn học | **Chờ nộp** |

---

## 3. Danh mục Công việc Chi tiết (Checklist Từng Task)

### 3.1 Khởi tạo & Cơ sở Dữ liệu (Dùng chung)
- [x] **Setup Repository & Cấu trúc thư mục** *(Hoàn thành: 22/10/2026)*: Khởi tạo Git repo, tạo các thư mục chuẩn `src/admin`, `src/user`, `script`, `git`, `docs`.
- [x] **Thống nhất Công nghệ & Quy tắc AI** *(Hoàn thành: 22/10/2026, cập nhật 29/10/2026)*: Chốt Java 21, chuyển sang JavaFX 21 + CSS, PostgreSQL local, Gson, `AGENTS.md`, `docs/tech-stack.md`.
- [x] **Thiết kế & Hoàn thiện CSDL 13 bảng** *(Hoàn thành: 23/10/2026)*: Hoàn tất `docs/database/schema.md` (tích hợp 13 bảng, sơ đồ Mermaid ERD và cẩm nang vận hành CSDL: UUID phiên, Watermark xóa tin, mã hóa nhóm).
- [x] **Viết Script Tạo CSDL Chính thức** *(Hoàn thành: 23/10/2026)*: Hoàn tất `script/database.sql` (chạy độc lập trên database rỗng, 100% ràng buộc và triggers).
- [x] **Tích hợp Sơ đồ ERD & Cẩm nang vào Schema** *(Hoàn thành: 24/10/2026)*: Nhúng sơ đồ ERD Mermaid trực tiếp vào `docs/database/schema.md`, xóa bỏ các file ảnh và context trùng lặp để tài liệu CSDL đạt chuẩn nguồn chân lý duy nhất.
- [x] **Dọn dẹp Kho lưu trữ** *(Hoàn thành: 24/10/2026)*: Xóa bỏ các tệp nháp rác, archive các tài liệu giải trình cũ, đưa `script/` về trạng thái tinh gọn chỉ chứa 1 file SQL.

---

### 3.2 Phân hệ Quản trị (Admin UI) — Thành viên A: Hải (`src/admin/`)

- [x] **Task A1 — Khung giao diện chính Admin (`AdminApplication`, `AdminMainView`)** *(Hoàn thành: 26/10/2026)*:
  - *Mô tả:* `Stage`/`Scene` kích thước 1000x700; `BorderPane` với sidebar `VBox` gồm 9 mục và vùng nội dung thay đổi (`StackPane`). Header hiển thị danh tính Admin mẫu và nút Đăng xuất.
- [x] **Task A2 — Quản lý danh sách người dùng (`UserManagementView`)** *(Hoàn thành: 27/10/2026)*:
  - *Mô tả:* `TableView` hiển thị ID, username, họ tên, email, trạng thái, ngày tạo; hồ sơ chi tiết đủ địa chỉ, ngày sinh, giới tính trong `UserDetailDialog`. Lọc tên/username/trạng thái `ACTIVE`/`LOCKED`/`DELETED`, sắp xếp tên/ngày tạo. Nút Thêm, Cập nhật, Xóa mềm, Khóa/Mở khóa, Cập nhật mật khẩu; dùng control JavaFX và `Alert` xác nhận.
- [x] **Task A3 — Hộp thoại chi tiết người dùng (`UserDetailDialog`)** *(Hoàn thành: 28/10/2026)*:
  - *Mô tả:* `UserDetailDialog` xem/thêm/sửa hồ sơ. Tách `UserLoginHistoryDialog` và `UserFriendsDialog` để xem lịch sử/bạn bè của đúng user được chọn; `PasswordUpdateDialog` có hai `PasswordField`, không hiển thị hash hoặc lưu mật khẩu mẫu. Mọi hộp thoại có owner và nút đóng/hủy.
- [x] **Task A4 — Lịch sử đăng nhập toàn hệ thống (`GlobalLoginHistoryView`)** *(Hoàn thành: 29/10/2026)*:
  - *Mô tả:* Bảng hiển thị danh sách đăng nhập theo thứ tự thời gian mới nhất (Thời gian đăng nhập, Tên đăng nhập, Họ tên). Có ô lọc theo username hoặc khoảng ngày.
- [x] **Task A5 — Quản lý nhóm chat (`GroupManagementView` & `GroupDetailDialog`)** *(Hoàn thành: 30/10/2026)*:
  - *Mô tả:* Bảng danh sách các nhóm chat (Tên nhóm, Người tạo, Ngày tạo, Cờ mã hóa). Bộ lọc theo tên nhóm, sắp xếp theo tên/ngày tạo. Dialog xem chi tiết: hiển thị danh sách thành viên trong nhóm và danh sách các Admin nhóm.
- [x] **Task A6 — Quản lý báo cáo Spam (`SpamReportView`)** *(Hoàn thành: 31/10/2026)*:
  - *Mô tả:* Bảng danh sách báo cáo vi phạm (Thời gian, Người báo cáo, Người bị báo cáo, Lý do spam). Bộ lọc/sắp xếp theo thời gian hoặc username bị báo cáo. Nút bấm thao tác nhanh: "Khóa tài khoản người bị báo cáo".
- [x] **Task A7 — Thống kê người dùng đăng ký mới (`NewUsersView`)** *(Hoàn thành: 01/11/2026)*:
  - *Mô tả:* Bộ chọn khoảng thời gian (Từ ngày - Đến ngày), ô lọc tên, ComboBox sắp xếp tên/ngày tạo. Bảng hiển thị danh sách người dùng đăng ký mới trong khoảng thời gian đã chọn.
- [x] **Task A8 — Thống kê số lượng bạn bè & FOF (`FriendStatisticsView`)** *(Hoàn thành: 01/11/2026)*:
  - *Mô tả:* Bảng hiển thị danh sách user gồm 2 cột số liệu quan trọng: "Số bạn trực tiếp" và "Số bạn của bạn (Friend-of-Friends)". Bộ lọc nâng cao: lọc theo tên, ComboBox toán tử (`=`, `<`, `>`) kèm ô nhập số lượng bạn bè trực tiếp để lọc.
- [x] **Task A9 — Thống kê người dùng hoạt động (`ActiveUsersView`)** *(Hoàn thành: 02/11/2026)*:
  - *Mô tả:* Bộ chọn khoảng thời gian (Từ ngày - Đến ngày). Bảng kết quả hiển thị danh sách user kèm 3 cột số liệu: Lượt mở app, Số người đã chat, Số nhóm đã chat. Có bộ lọc tên và bộ lọc số lượng hoạt động (`=`, `<`, `>`).
- [x] **Task A10 — Biểu đồ Đăng ký mới theo năm (`RegistrationChartView`)** *(Hoàn thành: 03/11/2026)*:
  - *Mô tả:* `ComboBox` chọn năm; JavaFX Charts `BarChart<String, Number>` với `CategoryAxis` gồm đủ 12 tháng và `NumberAxis` là số người đăng ký mới, có tháng giá trị 0; dùng dữ liệu mẫu.
- [x] **Task A11 — Biểu đồ Người dùng hoạt động theo năm (`ActivityChartView`)** *(Hoàn thành: 03/11/2026)*:
  - *Mô tả:* `ComboBox` chọn năm, JavaFX `BarChart` với đủ 12 tháng; trục tung đếm DISTINCT người mở ứng dụng mỗi tháng, không đếm số phiên mở app.

---

### 3.3 Phân hệ Người dùng (User UI) — Thành viên B: Minh (`src/user/`)

- [x] **Task U1 — Đăng nhập & Đăng ký tài khoản (`UserLoginApp`, `UserRegisterView`)** *(Hoàn thành: 26/10/2026)*:
  - *Mô tả:* Giao diện đăng nhập hiện đại (username, password, nút Đăng nhập, link Quên mật khẩu, link Đăng ký). Giao diện Đăng ký tài khoản đầy đủ các trường theo đề bài: tên đăng nhập, họ tên, địa chỉ, ngày sinh (DatePicker), giới tính (ComboBox), email, mật khẩu.
- [x] **Task U2 — Quên mật khẩu & Hồ sơ cá nhân (`UserForgotPasswordView`, `UserSettingsDialog`)** *(Hoàn thành: 27/10/2026)*:
  - *Mô tả:* Dialog quên mật khẩu: nhập email để nhận mật khẩu ngẫu nhiên. Dialog thông tin cá nhân: cập nhật họ tên, địa chỉ, ngày sinh, giới tính; tab hoặc vùng riêng để thực hiện Đổi mật khẩu (mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới).
- [x] **Task U3 — Khung không gian làm việc chính (`UserMainWorkspace`)** *(Hoàn thành: 28/10/2026)*:
  - *Mô tả:* Cửa sổ chính 1000x700 chia layout 3 vùng: Cột trái (Thanh tìm kiếm, Tabs chuyển đổi Bạn bè / Nhóm chat), Vùng giữa (Khung chat hội thoại), Vùng phải (Drawer thông tin người đang chat / danh sách thành viên nhóm).
- [x] **Task U4 — Danh sách bạn bè & Quản lý quan hệ (`FriendsManagementView`)** *(Hoàn thành: 29/10/2026)*:
  - *Mô tả:* Danh sách hiển thị bạn bè có avatar/tên, chấm màu trạng thái Online (xanh) / Offline (xám). Ô lọc bạn bè theo tên. Menu ngữ cảnh (chuột phải) hoặc nút thao tác: "Hủy kết bạn", "Chặn người dùng (Block)" (kèm thông báo sau khi block sẽ không thể tìm thấy hoặc gửi kết bạn lại).
- [x] **Task U5 — Danh sách yêu cầu kết bạn (`FriendsManagementView` Tab 3)** *(Hoàn thành: 29/10/2026)*:
  - *Mô tả:* Danh sách các lời mời kết bạn đang chờ (`PENDING`), có ô lọc theo tên người gửi. Mỗi dòng có nút "Đồng ý" (Accept) và nút "Từ chối" (Reject).
- [x] **Task U6 — Tab Bạn bè đang Online (`FriendsManagementView` Tab 1)** *(Hoàn thành: 30/10/2026)*:
  - *Mô tả:* Danh sách lọc riêng những bạn bè đang có trạng thái Online. Có ô lọc tên, nút "Chat ngay" để mở nhanh khung chat, nút "Tạo nhóm" với người đó.
- [x] **Task U7 — Tìm kiếm người dùng hệ thống (`FriendsManagementView` Tab 4)** *(Hoàn thành: 30/10/2026)*:
  - *Mô tả:* Ô tìm kiếm theo tên đăng nhập hoặc họ tên. Bảng hiển thị kết quả (tự động ẩn những người đã chặn mình). Trên từng kết quả có nút: "Gửi yêu cầu kết bạn", "Chat nhanh", hoặc "Tạo group".
- [x] **Task U8 — Khung chat 1-1 & Lịch sử tin nhắn (`ChatConversationView`, `UserDirectDrawer`)** *(Hoàn thành: 31/10/2026)*:
  - *Mô tả:* Vùng hiển thị tin nhắn dạng bong bóng (chat bubbles: tin mình gửi bên phải màu xanh, tin đối phương bên trái màu xám). Thanh tiêu đề trên cùng gồm: Tên/Avatar, trạng thái online/offline, nút "Báo cáo Spam", nút "Xóa toàn bộ lịch sử chat", ô tìm kiếm tin nhắn trong hội thoại, drawer hồ sơ người dùng 1-1. Phía dưới là ô nhập tin nhắn, nút "Gửi", nút "Gợi ý AI (LLM)".
- [x] **Task U9 — Tìm kiếm tin nhắn trong hội thoại & Highlight (`ChatConversationView`, `GlobalSearchDialog`)** *(Hoàn thành: 01/11/2026)*:
  - *Mô tả:* (1) Thanh tìm kiếm trong cuộc trò chuyện 1 người có nút Lên / Xuống (Next/Prev) để cuộn màn hình nhảy đến đúng đoạn chat. (2) Hộp thoại tìm kiếm toàn cục: gõ từ khóa tìm kiếm xuyên suốt tất cả các cuộc hội thoại, click vào kết quả sẽ mở đúng hội thoại và nhảy tới vị trí tin nhắn đó.
- [x] **Task U10 — Xóa tin nhắn linh hoạt (`ChatConversationView` / Multi-Select)** *(Hoàn thành: 01/11/2026)*:
  - *Mô tả:* Cho phép click chuột phải lên từng tin nhắn hoặc chọn nhiều tin để thực hiện: "Xóa tin nhắn phía tôi" (xóa lẻ 1 hoặc vài dòng lịch sử).
- [x] **Task U11 — Trợ lý gợi ý tin nhắn LLM (`ChatConversationView` / Smart Reply)** *(Hoàn thành: 02/11/2026)*:
  - *Mô tả:* Nút icon AI cạnh ô gõ tin nhắn. Khi bấm sẽ hiện popup/dialog gợi ý: chọn phong cách (Chỉnh sửa lịch sự hơn, Rút gọn, Mở rộng câu văn), hiển thị kết quả gợi ý từ AI kèm nút "Áp dụng vào ô chat".
- [x] **Task U12 — Tạo nhóm & Quản lý nhóm chat (`GroupManagementDrawer`, `GroupCreateDialog`, `MemberAddDialog`)** *(Hoàn thành: 02/11/2026)*:
  - *Mô tả:* Dialog tạo nhóm: nhập tên nhóm, chọn/nhập tối thiểu 1 username thành viên, checkbox "Bật mã hóa nhóm". Khung chat nhóm: Vùng hội thoại nhóm; Sidebar bên phải hiển thị tên nhóm (cho phép đổi tên), nút "Thêm thành viên", danh sách thành viên có huy hiệu "Admin nhóm". Nút "Gán Admin" cho thành viên và nút "Xóa khỏi nhóm" (chỉ hiển thị quyền xóa cho Admin nhóm).
- [x] **Task U13 — Hộp thoại Báo cáo Spam (`SpamReportDialog`)** *(Hoàn thành: 02/11/2026)*:
  - *Mô tả:* Dialog bật lên từ thanh chat: hiển thị tên người bị báo cáo, ô nhập lý do spam/quấy rối, nút "Gửi báo cáo".

---

### 3.4 Tích hợp, Báo cáo & Đóng gói Nghiệm thu 20% (Làm chung)
- [x] **Task C1 — Khung Báo cáo Đồ án (`BaoCao_Draft.docx` / LaTeX)** *(Hoàn thành: 03/11/2026)*: Minh dựng khung báo cáo đầy đủ 5 mục bắt buộc: (1) Thông tin nhóm, (2) Bảng phân công, (3) Bảng đóng góp tổng 100% theo thực tế (50-50), (4) Thiết kế CSDL (Hải viết và dán ERD), (5) Danh sách màn hình (chụp ảnh Admin và User).
- [x] **Task C2 — Ghép mã nguồn & Kiểm thử biên dịch** *(Hoàn thành: 16/11/2026)*: Hợp nhất `feat/admin-db` và `feat/user-ui` vào nhánh làm việc. Kiểm tra biên dịch JavaFX 21 không có bất kỳ lỗi cú pháp nào.
- [x] **Task C3 — Chụp minh chứng Git (`git/`) & Ảnh giao diện User** *(Hoàn thành: 16/11/2026)*: Lưu `git/commits_Hai.png`, xuất `git/commits_Minh.png` (1920x1080), và chụp đủ 18 ảnh màn hình User (phủ kín 100% các task U1 - U13) lưu vào `docs/ui-spec/evidence/` kèm manifest.json.
- [ ] **Task C4 — Xuất Báo cáo PDF & Đóng gói ZIP** *(Hạn chót: 20/11/2026)*: Xuất file `BaoCao.pdf`, kiểm tra nén file zip gồm đúng 3 thư mục `src/`, `script/`, `git/` và `BaoCao.pdf`.

---

## 4. Định hướng Tiếp nối cho Mốc 80% (17/11 – 12/12/2026)

Ngay sau khi nghiệm thu mốc 20%, nhóm sẽ chuyển sang giai đoạn lập trình logic thực tế:
- [ ] **Socket TCP Server** *(Tuần 1)*: Xây dựng Server đa luồng, quản lý danh sách socket kết nối online, định tuyến tin nhắn realtime và lưu tin nhắn offline.
- [ ] **Tầng Dữ liệu pgJDBC** *(Tuần 2)*: Server kết nối PostgreSQL qua pgJDBC, triển khai DAO với PreparedStatement, transaction và advisory lock theo `docs/database/schema.md`. Nếu cần connection pool, phải chọn phương án theo stack được duyệt, không tự thêm framework.
- [ ] **Tin nhắn Cá nhân & Nghiệp vụ Bạn bè** *(Tuần 3)*: Chat 1-1 realtime/offline, Watermark xóa tin, Quản lý bạn bè, Block, Báo cáo Spam.
- [ ] **Mã hóa nhóm AES-GCM & Dịch vụ Ngoài** *(Tuần 4)*: Chat nhóm mã hóa Server-side AES-GCM, Jakarta Mail reset pass, Java 21 `HttpClient` gọi LLM API.
- [ ] **Thống kê JavaFX Charts & Đóng gói JAR** *(Tuần 5)*: Tích hợp số liệu thật vào `BarChart` Admin (UI dữ liệu mẫu đã thuộc mốc 20%); đóng gói `Admin.jar`, `User.jar` kèm cấu hình môi trường, quay video demo và hoàn tất báo cáo cuối kỳ. Thông tin DB chỉ dùng phía Server, không phát credential DB cho client.
