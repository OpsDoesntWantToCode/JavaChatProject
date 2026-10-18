# PROJECT CONTEXT — JAVA CHAT SYSTEM (DÙNG CHUNG CHO A VÀ B)

> **Phiên bản tài liệu:** 2.0, 16/10/2026. **Dành chung cho cả hai thành viên và mọi trợ lý AI**, không chỉ Codex.  
> **Nguồn:** Yêu cầu gốc `Final Project.pdf`, trao đổi/chốt quyết định A–B, các phiên bản `Feedback 2 ben`.  
> **Mục tiêu:** Lưu yêu cầu đề bài, quyết định chung, trách nhiệm từng người, các đề xuất chưa duyệt và tình trạng dự án. Không trộn lẫn các mức xác nhận.

## 0. Thông tin cốt lõi

- **Dự án:** Java Chat System — đồ án cuối kỳ môn Lập trình ứng dụng Java.
- **Quy mô:** nhóm 2 người; 2 phân hệ **Admin** và **User**; một hệ thống database/chức năng liên thông.
- **Thành viên A:** **Admin UI + ERD + PostgreSQL + `script/database.sql`**; có thể dùng Codex CLI trong VS Code như công cụ cá nhân.
- **Thành viên B:** **User UI + khởi tạo Git repository/Maven + khung và tổng hợp báo cáo**; dùng trợ lý AI riêng, không bắt buộc sử dụng Codex.
- **Công việc làm chung:** duyệt ERD, thiết kế giao thức Server/DTO, phần `server/` và `shared/`, tích hợp và kiểm thử.
- **Mốc 20%:** giao diện 2 phân hệ + DB SQL script + Git evidence + báo cáo; giao diện **chưa cần lập trình sự kiện**.
- **Mốc 80%:** toàn bộ hệ thống chạy được; ít nhất 2 JAR Admin/User + cấu hình DB, mã nguồn, script, Git evidence, báo cáo, hướng dẫn có hình, video demo, link GitHub, tự đánh giá.
- **Stack chính thức:** xem `Tech_Stack.md`. Không tự thay đổi.

## 1. Các yêu cầu chức năng theo `Final Project.pdf`

### 1.1 Admin — 9 nhóm yêu cầu

1. **Quản lý user:** xem và lọc theo tên/username/trạng thái; sắp xếp tên/ngày tạo; thêm/sửa/xóa; khóa/mở khóa; cập nhật mật khẩu; xem lịch sử đăng nhập và danh sách bạn bè của user.
2. **Lịch sử đăng nhập chung:** hiển thị theo thời gian với thời gian, username, họ tên.
3. **Danh sách nhóm chat:** sắp xếp tên/thời gian tạo, lọc tên, xem thành viên và danh sách Admin nhóm.
4. **Báo cáo spam:** danh sách, sắp xếp thời gian/username, lọc thời gian/username, khóa tài khoản.
5. **Người đăng ký mới:** lọc theo khoảng thời gian, sắp xếp tên/ngày tạo, lọc tên.
6. **Biểu đồ đăng ký mới theo năm:** chọn năm, trục X = 12 tháng, Y = số user mới.
7. **Thống kê số lượng bạn bè:** số bạn trực tiếp và số bạn của bạn; lọc/sắp xếp theo yêu cầu, lọc số bạn trực tiếp bằng/nhỏ hơn/lớn hơn.
8. **Người dùng hoạt động trong khoảng thời gian:** lượt mở app, đã chat với bao nhiêu người, đã chat bao nhiêu nhóm; có bộ lọc/sắp xếp.
9. **Biểu đồ người hoạt động theo năm:** mỗi tháng có bao nhiêu user đã mở ứng dụng.

### 1.2 User — 12 nhóm yêu cầu

1. Đăng ký tài khoản.
2. Cập nhật thông tin tài khoản.
3. Khởi tạo lại mật khẩu: đề bài mô tả tạo mật khẩu ngẫu nhiên gửi email.
4. Đổi mật khẩu.
5. Đăng nhập.
6. Danh sách bạn bè: lọc theo tên, online/offline, tìm/gửi lời mời, hủy bạn, block (sau block không thể gửi kết bạn lại).
7. Yêu cầu kết bạn: lọc tên, chấp nhận/từ chối.
8. Bạn bè online: lọc tên, chat, tạo group.
9. Tìm người theo username/tên, không thấy người đã block mình; có thể chat/tạo group.
10. Báo cáo spam khi đã chat với người bị báo cáo.
11. **Chat cá nhân:** realtime khi online; gửi để xem sau khi offline; lịch sử; xóa toàn bộ; xóa một/vài dòng; tìm chuỗi trong hội thoại một người và nhảy đến đúng đoạn; tìm trong tất cả hội thoại và nhảy đến đoạn; gọi LLM chỉnh/gợi ý chat.
12. **Nhóm chat:** tạo nhóm và thêm tối thiểu 1 username; đổi tên; thêm thành viên; gán Admin nhóm; chỉ Admin nhóm xóa thành viên; chat nhóm; mã hóa nhóm (đề dẫn tính năng nhóm mã hóa Facebook).

**Chú ý:** Đề không quy định cụ thể DBMS, GUI framework, thư viện mạng hay kiểu mã hóa/E2EE. Các chi tiết này do nhóm lựa chọn hoặc cần hỏi giảng viên.

## 2. Quyết định A/B về kiến trúc và cách làm

- **Phân công theo phân hệ (Cách 1)**: A phụ trách Admin UI, ERD, PostgreSQL và `script/database.sql`; B phụ trách User UI, Maven/repository và báo cáo. ERD cùng duyệt.
- **Một GitHub repository**; quy ước branch: `main` tích hợp chung; `feat/admin-db` dành cho A (Admin UI, ERD, PostgreSQL và `script/database.sql`); `feat/user-ui` dành cho B (User UI, Maven/repository và báo cáo).
- **Maven gốc `pom.xml`**; Java package `com.chatapp.admin`, `com.chatapp.user`, `com.chatapp.server`, `com.chatapp.shared` tương ứng.
- **`shared` và `server`** là phần cùng sở hữu; không tự sửa phá giao thức mà không phối hợp.
- **Server trung gian** kết nối PostgreSQL, xử lý nghiệp vụ, phân quyền, phiên online, Socket; không đặt credential DB có quyền cao hoặc API key trên client.
- **Local DB**: A và B có thể chạy mỗi người một PostgreSQL local để phát triển, dùng cùng schema; khi tích hợp, cùng một Server và một nguồn dữ liệu.
- **20%**: UI demo bằng dữ liệu giả và SQL script tái tạo DB từ đầu; **80%** mới có logic thực, realtime, thống kê, email, LLM, mã hóa, đóng gói JAR.

## 3. Database chính thức của cả nhóm: 13 bảng

A/B đã chấp thuận thiết kế và chuẩn hóa sau khi SQL PASS trên PostgreSQL 18.6: **13 bảng, 65 cột, 21 FK**. Nguồn hiện hành: [Database_Schema](database/Database_Schema.md), [Database_Decisions](database/Database_Decisions.md), [ERD](database/ERD.mmd), [SQL](../script/database.sql), [test results](database/TEST_RESULTS.md). Nguồn draft của B và ERD cũ được giữ tại [archive](database/archive/README.md).

Các bảng: users, login_history, activity_logs, friend_requests, friendships, user_blocks, direct_messages, message_hidden, conversation_clears, chat_groups, group_members, group_messages, spam_reports. Giữ baseline 13 bảng; thêm users.role, watermark vào conversation_clears, UUID NOT NULL UNIQUE cho session; bỏ encrypted_group_key dự phòng ở group_members.

### 3.1 Quy tắc đã chốt

- BCrypt/password_hash, soft delete ACTIVE/LOCKED/DELETED; không có cột online cố định.
- Friendship một cặp theo ID tăng, block có hướng; PENDING duy nhất hai chiều. Chấp nhận/block nguyên tử theo khóa cặp.
- Xóa chat riêng từng phía bằng message_hidden và watermark; cleared_at chỉ audit. Gửi/clear phải khóa cặp trước cấp identity, không giả định ID là commit order.
- OPEN_APP một lần/phiên thật; reconnect giữ UUID, không tăng lượt; UUID không phải token xác thực.
- Thống kê đếm tin user chủ động gửi; biểu đồ hoạt động DISTINCT user/tháng, khoảng [from,to), giờ Việt Nam. Bạn cấp hai hiển thị tổng DISTINCT người cách tối đa hai cạnh, loại chính mình.
- users.role là quyền hệ thống, group_members.is_admin là quyền nhóm; không suy quyền từ JAR hoặc payload.
- Một reporter chỉ báo cáo một reported user một lần; Server kiểm đã nhận DM từ người đó.
- Mã hóa mặc định **Server-side AES-GCM PROVISIONAL**, một DEK/nhóm, mode bất biến, KEK ngoài DB/repo/client; không E2EE, chưa được giảng viên xác nhận.

### 3.2 Phạm vi đã kiểm chứng và phần phải làm ở Server

Suite SQL lần cuối **202 PASS, 0 FAIL**: cấu trúc, PK/FK/UNIQUE/CHECK/NOT NULL, index, trigger mode–IV, soft delete không mất lịch sử. Không xem đây là kết quả test Java, concurrency hoặc AEAD. Server còn phải triển khai cổng quyền/khóa, visibility, membership, Admin cuối/bootstrapping/kế nhiệm, spam evidence, sessions, BCrypt, TLS và quản lý khóa. Xem [ERD_Review](database/ERD_Review.md).

## 4. Chức năng liên kết UI ↔ Database

- **Admin** xem thống kê/lịch sử từ `users`, `login_history`, `activity_logs`, `friendships`, `chat_groups`, `group_members`, `spam_reports`, `direct_messages`, `group_messages`.
- **User** tạo dữ liệu từ đăng ký/login, friend requests, friendships, blocks, direct/group messages và spam reports.
- **Truy vấn nhóm/bạn bè phải nhất quán** vì hai phân hệ sử dụng chung một schema.
- **Phiên bản 1**: giao diện chỉ cần hiển thị đúng chức năng với dữ liệu mẫu; database script phải tạo các bảng/ràng buộc có thể dùng ở phiên bản 2.

## 5. Phạm vi công việc của thành viên A — triển khai song song với B

1. Nhận từ B repository và xác nhận branch làm việc `feat/admin-db` (không tự thay tên branch khi repo đã tồn tại).
2. Cùng B review ERD cuối: tất cả 13 bảng, kiểu PostgreSQL, NULL/NOT NULL, PK/FK/UNIQUE/CHECK, chỉ mục và chính sách ON DELETE.
3. Viết `script/database.sql` sao cho có thể chạy lại từ database rỗng; kiểm thử trường hợp sai (username trùng, trạng thái sai, FK vô hiệu, lời mời PENDING trùng/đảo chiều, cặp bạn bè đảo ID...); xuất ERD.
4. Xây dựng khung Admin Swing + FlatLaf, 9 nhóm màn hình Admin; màn hình có thể dùng dummy data ở mốc 20%.
5. Chụp minh chứng GUI và commit của A; gửi B phần giải thích DB/ERD + màn hình Admin để tổng hợp báo cáo.
6. A tránh tự ý sửa `com.chatapp.user`; B tránh tự ý sửa `com.chatapp.admin` và `script/database.sql`. Thay đổi `server`, `shared`, `pom.xml` hoặc API/DTO dùng chung phải phối hợp và review chéo.

## 6. Các điểm chưa được quyết định hoàn toàn

- Xác nhận từ giảng viên liệu Server-side AES-GCM đã chọn có đáp ứng yêu cầu hay phải E2EE; hiện vẫn PROVISIONAL.
- Watermark và khóa đã chốt trong Database_Decisions; còn kiểm thử integration gửi/clear đồng thời tại Server.
- SQL schema/index/trigger đã PASS PostgreSQL 18.6; quyền service, concurrency và hiệu năng vẫn cần kiểm thử khi có Server.
- LLM nhà cung cấp/model/API cụ thể, cấu hình email/SMTP, Maven build nhiều file JAR và cách phân phối Server.
- Phong cách giao diện và màn hình chuẩn chưa có bộ quy định cuối cùng.

## 7. Các điều không nên làm

- Không thay đổi tech stack đã thống nhất chỉ vì thói quen hoặc đề xuất mặc định của bất kỳ AI assistant nào (không JavaFX/Spring Boot/Supabase/Jackson khi chưa được hai bên duyệt).
- Không viết lại kiến trúc hoặc sinh toàn bộ dự án chỉ từ một prompt khổng lồ mà không có review và test.
- Không khẳng định 13 bảng đã hoàn chỉnh chỉ vì đã có tên/cột.
- Không lưu plaintext password, khóa AES gốc, database credentials hoặc LLM API keys trong source/client.
- Không tự ý sửa phần code do thành viên còn lại sở hữu. Không để AI tự push/merge/commit, ghi đè thay đổi chưa commit hoặc sửa cấu hình chung nếu chưa có yêu cầu/phê duyệt của người đang thực hiện và quy ước nhóm.

## 8. Danh mục tài liệu nguồn cần giữ trong project

- `Final Project.pdf` — yêu cầu đề bài gốc. Đây là nguồn có ưu tiên cao nhất khi xác định tiêu chí cần nộp.
- `Feedback 2 ben` — các vòng phản hồi A/B và bản 13 bảng của B, cần đối chiếu phiên bản hiện hành.
- `Tech_Stack.md` — bảng công nghệ quyết định, ưu tiên dùng để ngăn tự ý đổi stack.
- `AGENTS.md` (nếu sử dụng) — quy tắc hướng dẫn cho AI coding assistants; Codex có thể tự đọc, AI khác cần được nhắc đọc nếu công cụ không tự nhận diện.

**Quy tắc ưu tiên khi có xung đột:** yêu cầu thầy trong đề gốc → quyết định A/B mới nhất được xác nhận → `Tech_Stack.md`/tài liệu context được cập nhật → đề xuất kỹ thuật chưa được duyệt. Nếu phát hiện xung đột, báo rõ thay vì âm thầm thay đổi.


## 9. Quy trình dùng NHIỀU trợ lý AI trong cùng một repository

- **Nguồn sự thật chung:** `Final Project.pdf` (yêu cầu chính thức) → quyết định A–B mới nhất đã được xác nhận → `docs/Tech_Stack.md` + `docs/Project_Context.md` được cập nhật → đề xuất chưa duyệt. Nếu tài liệu mâu thuẫn, báo lại để hai người chốt.
- **Không có AI độc quyền sở hữu tài liệu:** A có thể dùng Codex CLI, B có thể dùng Gemini, ChatGPT hoặc công cụ khác. Hai tài liệu này không được chứa hướng dẫn chỉ áp dụng cho một AI.
- **Phạm vi code:** A xử lý Admin UI/ERD/PostgreSQL/`script/database.sql`; B xử lý User UI/Maven/repository/báo cáo; sửa `server`, `shared`, `pom.xml`, DTO/protocol phải được review vì ảnh hưởng cả hai.
- **Trước khi giao việc cho AI:** nêu vai trò A hay B, mục tiêu hiện tại là bản 20% hay 80%, chỉ rõ file/phạm vi được phép sửa, yêu cầu đọc các tài liệu chung.
- **Sau khi AI thay đổi code:** người thực hiện xem `git diff`, chạy test/build có thể chạy, kiểm tra dữ liệu nhạy cảm, rồi chủ động commit. Không coi AI nói "đã test" là bằng chứng nếu không có kết quả thực tế.
- **Đồng bộ quyết định mới:** khi cả hai chốt thay đổi kiến trúc/schema/tech stack, cập nhật các file chung trong một commit/PR và thông báo người còn lại trước khi tiếp tục code.

## 10. Prompt khởi đầu trung lập cho bất kỳ AI nào (thay vai trò khi dùng)

```text
Tôi là thành viên [A/B] của nhóm Java Chat System.
Hãy đọc Final Project.pdf (nếu có), docs/Tech_Stack.md và docs/Project_Context.md;
nếu có AGENTS.md và công cụ hỗ trợ thì đọc thêm.
Dự án đang ở mốc [20%/80%]. Phần tôi phụ trách là [ghi cụ thể].
Hãy phân biệt yêu cầu đề bài, quyết định A/B đã chốt và đề xuất chưa duyệt.
Trước mắt chỉ kiểm tra hiện trạng và tóm tắt phạm vi; chưa sửa code khi tôi chưa yêu cầu.
Không thay đổi tech stack hoặc các file dùng chung khi chưa thống nhất với thành viên còn lại.
```
