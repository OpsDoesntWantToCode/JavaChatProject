# AGENTS.md — NGUYÊN TẮC VẬN HÀNH & CHỈ DẪN DỰ ÁN CHO AI ASSISTANTS

> Bản hướng dẫn chỉ đạo hệ thống cho toàn bộ Trợ lý AI (Codex, Antigravity, Claude, ChatGPT) khi làm việc trên dự án Java Chat System.
> Mọi AI BẮT BUỘC tuân thủ nghiêm ngặt các từ khóa chỉ thị: BẮT BUỘC, PHẢI, NGHIÊM CẤM, TUYỆT ĐỐI KHÔNG, LUÔN LUÔN.

---

## 1. Vai trò Thành viên & Phạm vi Trách nhiệm

- **Dự án:** Java Chat System (Ứng dụng chat desktop JavaFX 21 + CSS, CSDL PostgreSQL, TCP Socket).
- **Quyết định công nghệ GUI:** Hải và Minh đã thống nhất chuyển từ Swing sang **JavaFX 21 + CSS** ngày 29/10/2026.
- **Thành viên A (Lưu Chí Hải - MSSV: 24127030):**
  - Nhánh làm việc: `feat/admin-db`
  - Phạm vi: Giao diện Admin (`src/admin/`), CSDL PostgreSQL (`script/database.sql`), sơ đồ ERD, phần Admin trong báo cáo.
- **Thành viên B (Trần Nguyễn Tuấn Minh - MSSV: 24127084):**
  - Nhánh làm việc: `feat/user-ui`
  - Phạm vi: Giao diện User (`src/user/`), cấu hình Maven (`pom.xml`), phần User và tổng hợp Báo cáo (`BaoCao.pdf`).
- **Phần sở hữu chung (BẮT BUỘC review chéo trước khi chỉnh sửa):** `server/`, `shared/` (DTO, giao thức Socket), `pom.xml`, `docs/plan.md`.
- **Các mốc tiến độ:**
  - Mốc 20% (Mục tiêu: 05/11/2026 — Hạn chót chính thức: 22/11/2026): Toàn bộ UI tĩnh JavaFX có dữ liệu mẫu, script CSDL hoàn chỉnh, minh chứng Git và Báo cáo PDF.
  - Mốc 80% (Hạn chót toàn đồ án: 12/12/2026): Toàn bộ hệ thống chạy thực tế (Socket realtime/offline, pgJDBC, AES-GCM, Mail SMTP, LLM API, đóng gói 2 file JAR: `Admin.jar` & `User.jar`).

---

## 2. Bản đồ Tài liệu Tham chiếu (Kích hoạt theo Ngữ cảnh)

- **Khi xem yêu cầu nghiệp vụ gốc và tiêu chuẩn nộp bài:**
  -> BẮT BUỘC đọc `docs/final-project.pdf` (Nguồn chân lý cao nhất).
- **Khi kiểm tra tiến độ, danh mục màn hình hoặc task cần làm:**
  -> BẮT BUỘC đọc `docs/plan.md`.
- **Khi chọn thư viện, phiên bản JDK hoặc kiến trúc Client-Server:**
  -> BẮT BUỘC tuân thủ `docs/tech-stack.md`.
- **Khi thiết kế bảng, tra cứu CSDL, xem sơ đồ ERD hoặc viết truy vấn SQL/transaction:**
  -> BẮT BUỘC đọc `docs/database/schema.md` (Tài liệu CSDL duy nhất: chứa 13 bảng, 65 cột, sơ đồ Mermaid ERD và cẩm nang vận hành backend).

---

## 3. Tiêu chuẩn Đặt tên Toàn diện (Naming Conventions)

Mọi tệp tin, thư mục và thành phần mã nguồn trong dự án BẮT BUỘC tuân thủ các quy chuẩn chuẩn ngành sau:

| Đối tượng | Quy chuẩn đặt tên | Ví dụ hợp lệ | Ví dụ vi phạm |
| :--- | :--- | :--- | :--- |
| **Tài liệu gốc tại root** | `UPPER_SNAKE_CASE.md` | `README.md`, `AGENTS.md` | `readme.md`, `Agents.md` |
| **Tài liệu trong `docs/`** | `kebab-case.md` | `plan.md`, `tech-stack.md`, `final-project.pdf` | `Tech_Stack.md`, `Final Project.pdf` |
| **Thư mục (Directories)** | `kebab-case` hoặc `lowercase` | `src/admin/`, `src/user/`, `docs/database/` | `src/AdminUI/`, `docs/Database Schema/` |
| **Java Packages** | `lowercase` liền nhau (phân cấp dấu chấm) | `com.chatapp.admin`, `com.chatapp.shared.dto` | `com.chatapp.Admin`, `com.chatapp.user_ui` |
| **Java Classes / Interfaces** | `PascalCase` (Danh từ / Cụm danh từ) | `UserManagementPanel`, `UserDAO`, `SocketServer` | `userManagementPanel`, `user_dao` |
| **Java Methods / Variables**| `camelCase` (Động từ / Cụm động từ cho hàm) | `calculateFriends()`, `sendMessage()`, `isOnline` | `CalculateFriends()`, `send_message` |
| **Java Constants (Hằng số)**| `UPPER_SNAKE_CASE` | `DEFAULT_PORT`, `MAX_RETRIES`, `SESSION_TIMEOUT`| `defaultPort`, `MaxRetries` |
| **CSDL Bảng & Cột (SQL)** | `snake_case` (chữ thường nối gạch dưới) | Bảng: `login_history`<br>Cột: `sender_id`, `created_at` | Bảng: `LoginHistory`<br>Cột: `senderId`, `CreatedAt` |
| **CSDL Ràng buộc & Index** | Tiền tố chuẩn + `snake_case` | `pk_users`, `fk_fr_sender`, `uq_activity_session`, `chk_fr_no_self`, `idx_login_time` | `primary_key_1`, `FK_1`, `IndexUser` |
| **Socket JSON Actions** | `UPPER_SNAKE_CASE` | `AUTH_LOGIN`, `CHAT_SEND_DM`, `ACCOUNT_LOCKED` | `authLogin`, `chat_send_dm` |
| **Git Branches** | `kebab-case` có tiền tố phân loại | `feat/admin-db`, `feat/user-ui`, `fix/socket-reconnect` | `Admin_DB`, `feat_user`, `MinhBranch` |
| **Git Commits** | Conventional Commits (`type(scope): msg`) | `feat(admin-ui): create user table`, `docs: update plan` | `update code`, `fix bug`, `DONE` |

---

## 4. Môi trường Biên dịch & Thực thi

- **Phiên bản Java:** Java 21 LTS (OpenJDK / Oracle JDK 21).
- **Công cụ build:** Apache Maven (`pom.xml`).
- **Cơ sở dữ liệu:** PostgreSQL 16+ chạy cục bộ (port 5432).
  - Script tạo CSDL từ database rỗng: `script/database.sql`.
- **Thư mục mã nguồn:**
  - Giao diện Admin: `src/admin/`
  - Giao diện User: `src/user/`
  - CSDL DDL: `script/database.sql`

---

## 5. Quy tắc Lập trình & Kiến trúc

- **Tiêu chuẩn Giao diện (Window Java — JavaFX 21 + CSS):**
  - BẮT BUỘC dùng **JavaFX 21** với stylesheet **CSS** (ứng dụng desktop cửa sổ trên Windows, kích thước chuẩn ~1000x700, đóng gói 2 file JAR độc lập; TUYỆT ĐỐI KHÔNG làm ứng dụng Web/HTML).
  - BẮT BUỘC đổ sẵn dữ liệu mẫu sống động vào TableView, danh sách bạn bè và bong bóng chat phục vụ chụp ảnh báo cáo mốc 20%.
- **Hệ thống Máy chủ (Backend Java):**
  - BẮT BUỘC lập trình hoàn toàn bằng Java 21 LTS thuần túy (Java Server đa luồng), quản lý các phiên kết nối online/offline, điều phối nghiệp vụ và kết nối PostgreSQL qua JDBC (`pgJDBC`).
- **Giao tiếp Mạng (TCP Socket — Không phải WebSocket):**
  - BẮT BUỘC dùng **TCP Socket thuần của Java** (`java.net.Socket`, `java.net.ServerSocket` trên tầng Transport/OS mạng).
  - TUYỆT ĐỐI KHÔNG dùng WebSocket (`ws://`, `wss://`), Socket.io hay bất kỳ giao thức Web nào.
  - Định dạng dữ liệu truyền tải đóng gói qua **Gson (JSON)** (`action`, `session_id`, `payload`, `status`, `error`).
- **Tiêu chuẩn Cơ sở dữ liệu & Truy vấn:**
  - BẮT BUỘC dùng `try-with-resources` cho toàn bộ `Connection`, `PreparedStatement` và `ResultSet`.
  - TUYỆT ĐỐI KHÔNG nối chuỗi SQL trực tiếp. LUÔN LUÔN dùng truy vấn có tham số hóa với dấu `?`.
  - LUÔN LUÔN đảm bảo tính toàn vẹn giao dịch (`conn.setAutoCommit(false)`) khi thao tác thay đổi trên nhiều bảng.
- **Bảo mật & Mạng:**
  - TUYỆT ĐỐI KHÔNG lưu mật khẩu dạng văn bản thô. LUÔN LUÔN băm bằng BCrypt (`BCrypt.hashpw`).
  - TUYỆT ĐỐI KHÔNG hardcode mật khẩu kết nối CSDL, khóa mã hóa chủ (KEK), thông tin email SMTP hoặc API key LLM trong ứng dụng Client hoặc đẩy lên Git.
  - BẮT BUỘC dùng Java Cryptography API cho mã hóa AES-GCM phía Server (mỗi nhóm 1 khóa DEK, IV ngẫu nhiên 12 byte Base64).
- **Mẫu Kiến trúc Chuẩn (Golden Patterns):**
  - Khung điều khiển (Dashboard): Dùng `CardLayout` cho thanh điều hướng sidebar.
  - Khung Chat: Tách riêng panel danh sách hội thoại, panel bong bóng chat (`JScrollPane`), và vùng nhập tin nhắn.
  - Giao thức Socket: Tuân thủ định dạng gói JSON Gson chuẩn (`action`, `session_id`, `payload`, `status`, `error`).

---

## 6. Vùng cấm Tuyệt đối (DANH SÁCH CẤM / TUYỆT ĐỐI KHÔNG)

- **TUYỆT ĐỐI KHÔNG tự ý thực thi `git commit` hoặc `git push`** trừ khi người dùng ra lệnh trực tiếp trong prompt.
- **NGHIÊM CẤM đọc hoặc tham chiếu các tệp trong `docs/database/archive/` hoặc `docs/archive/`** — đây là các bản nháp lịch sử đã lỗi thời.
- **NGHIÊM CẤM sửa chéo thư mục của thành viên khác:**
  - Khi làm việc cho Hải: CHỈ sửa mã nguồn trong `src/admin/`, `script/` và tài liệu Admin.
  - Khi làm việc cho Minh: CHỈ sửa mã nguồn trong `src/user/`, cấu hình Maven và tài liệu User.
  - TUYỆT ĐỐI KHÔNG sửa mã nguồn chung (`server/`, `shared/`) khi chưa có sự thống nhất rõ ràng giữa hai bạn.
- **NGHIÊM CẤM tự ý đưa vào các framework ngoài danh mục:**
  - TUYỆT ĐỐI KHÔNG thêm Spring Boot, Hibernate/JPA, Jackson, Lombok hoặc Supabase.
  - TUYỆT ĐỐI KHÔNG dùng WebSocket, Socket.io, HTTP Servlet hoặc công nghệ Web làm nền tảng chat; BẮT BUỘC dùng Java TCP Socket thuần và Window Java Desktop UI.
  - CHỈ sử dụng các thư viện đã được phê duyệt trong `docs/tech-stack.md` (JavaFX 21, pgJDBC, Gson, Jakarta Mail, BCrypt, HttpClient).
- **TUYỆT ĐỐI KHÔNG sử dụng đường dẫn tệp tuyệt đối của máy cá nhân:**
  - LUÔN LUÔN dùng đường dẫn tương đối lấy thư mục gốc của repository làm chuẩn (ví dụ: `docs/plan.md`, `script/database.sql`).

---

## 7. Quy tắc Cập nhật & Bảo trì AGENTS.md

Để bảo đảm `AGENTS.md` luôn là "Kim chỉ nam tối cao" tinh gọn, chính xác và không làm quá tải ngữ cảnh (context window) của AI, việc cập nhật tệp này BẮT BUỘC tuân thủ các nguyên tắc sau:

1. **Phân định Ranh giới Nội dung (Nguyên tắc Single Source of Truth):**
   - **CHỈ ghi vào `AGENTS.md`:** Các nguyên tắc chỉ đạo hệ thống bất biến, ranh giới trách nhiệm hai thành viên, tiêu chuẩn đặt tên, môi trường kỹ thuật cốt lõi và danh sách vùng cấm.
   - **TUYỆT ĐỐI KHÔNG ghi vào `AGENTS.md`:**
     - Danh sách việc cần làm (task), checklist tiến độ, deadline -> BẮT BUỘC lưu tại `docs/plan.md`.
     - Chi tiết bảng, cột, kiểu dữ liệu, sơ đồ ERD, truy vấn SQL -> BẮT BUỘC lưu tại `docs/database/schema.md`.
     - Hướng dẫn chi tiết thư viện, cấu hình build chi tiết -> BẮT BUỘC lưu tại `docs/tech-stack.md`.
     - Các đoạn mã giả, ghi chú cá nhân, hoặc nhật ký thảo luận tạm thời.

2. **Điều kiện Kích hoạt Cập nhật (When to update):**
   - CHỈ cập nhật khi: Có chỉ đạo trực tiếp từ người dùng, hoặc phát sinh quy chuẩn/vùng cấm mới cần khóa cứng sau khi cả hai thành viên đã thống nhất.
   - TUYỆT ĐỐI KHÔNG tự ý suy diễn hoặc tự tiện thêm quy tắc thử nghiệm tạm thời.

3. **Tiêu chuẩn Biên soạn & Định dạng (Formatting Standards):**
   - BẮT BUỘC giữ vững 100% tiếng Việt chuẩn mực, rõ nghĩa, không pha trộn tiếng Anh tùy tiện.
   - BẮT BUỘC dùng các từ khóa chỉ thị dứt khoát (RFC 2119: BẮT BUỘC, PHẢI, NGHIÊM CẤM, TUYỆT ĐỐI KHÔNG, LUÔN LUÔN).
   - Trình bày dạng gạch đầu dòng hoặc bảng biểu súc tích; TUYỆT ĐỐI KHÔNG viết văn xuôi dài dòng, không dùng từ ngữ cảm thán hoặc trang trí thừa.

4. **Kiểm tra Xung đột & Tinh giản (No Duplication / No Conflict):**
   - Trước khi thêm nội dung mới, BẮT BUỘC rà soát toàn bộ tệp để đảm bảo quy tắc mới KHÔNG xung đột hoặc lặp lại với các mục có sẵn.
   - Khi một quy tắc cũ bị thay thế hoặc hết giá trị, PHẢI xóa bỏ hoặc cập nhật đè trực tiếp thay vì ghi thêm lời giải thích chắp vá.
