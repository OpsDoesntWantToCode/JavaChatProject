# PROJECT CONTEXT — JAVA CHAT SYSTEM

> **Đọc trước khi Codex bắt đầu làm việc.**  
> **Tổng hợp từ cuộc trao đổi dài giữa thành viên A, thành viên B và phản hồi về đề `Final Project.pdf`, tính đến 16/10/2026.**  
> Tài liệu này ghi lại **yêu cầu đề bài**, **quyết định A/B**, **review kỹ thuật chưa được duyệt hoàn toàn**, và **công việc tiếp theo**. Không trộn lẫn các mức độ xác nhận.

## 0. Thông tin cốt lõi

- **Dự án:** Java Chat System — đồ án cuối kỳ môn Lập trình ứng dụng Java.
- **Quy mô:** nhóm 2 người; 2 phân hệ **Admin** và **User**; một hệ thống database/chức năng liên thông.
- **Người dùng phiên Codex này:** **Thành viên A** — phụ trách **Admin UI + Database**.
- **Thành viên B:** **User UI + khởi tạo Git repository/Maven + khung và tổng hợp báo cáo**.
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

- **Phân công theo phân hệ (Cách 1)**: A làm toàn bộ Admin UI và SQL; B làm toàn bộ User UI, repo, Maven và báo cáo tổng hợp. ERD cùng duyệt.
- **Một GitHub repository**; nhánh được chốt gần nhất `main`, `feat/admin`, `feat/user`.
- **Maven gốc `pom.xml`**; Java package `com.chatapp.admin`, `com.chatapp.user`, `com.chatapp.server`, `com.chatapp.shared` tương ứng.
- **`shared` và `server`** là phần cùng sở hữu; không tự sửa phá giao thức mà không phối hợp.
- **Server trung gian** kết nối PostgreSQL, xử lý nghiệp vụ, phân quyền, phiên online, Socket; không đặt credential DB có quyền cao hoặc API key trên client.
- **Local DB**: A và B có thể chạy mỗi người một PostgreSQL local để phát triển, dùng cùng schema; khi tích hợp, cùng một Server và một nguồn dữ liệu.
- **20%**: UI demo bằng dữ liệu giả và SQL script tái tạo DB từ đầu; **80%** mới có logic thực, realtime, thống kê, email, LLM, mã hóa, đóng gói JAR.

## 3. Baseline thiết kế database: 13 bảng

Đây là **danh sách 13 bảng mới nhất do B đưa ra**, đã được A chấp thuận **về mặt cấu trúc**, nhưng **chi tiết constraints P1 vẫn cần chỉnh và review**.

| Bảng | Cột chính theo dự thảo của B | Mục đích |
|---|---|---|
| `users` | `id`, `username`, `password_hash`, `full_name`, `address`, `dob`, `gender`, `email`, `status`, `last_seen_at`, `created_at` | Tài khoản, soft delete. |
| `login_history` | `id`, `user_id`, `login_time` | Lịch sử đăng nhập. |
| `activity_logs` | `id`, `user_id`, `action_type`, `session_id`, `created_at` | Mỗi phiên một `OPEN_APP`. |
| `friend_requests` | `id`, `sender_id`, `receiver_id`, `status`, `created_at` | Lời mời PENDING/ACCEPTED/REJECTED. |
| `friendships` | `user_id_1`, `user_id_2`, `created_at` | Bạn bè hai chiều lưu một dòng/cặp. |
| `user_blocks` | `blocker_id`, `blocked_id`, `created_at` | Chặn người dùng. |
| `direct_messages` | `id`, `sender_id`, `receiver_id`, `content`, `is_read`, `created_at` | Chat 1–1 và lịch sử. |
| `message_hidden` | `message_id`, `user_id`, `hidden_at` | Ẩn tin lẻ chỉ cho người xóa. |
| `conversation_clears` | `user_id`, `partner_id`, `cleared_at` | Mốc xóa toàn bộ lịch sử chat 1–1 cho từng người. |
| `chat_groups` | `id`, `group_name`, `created_by`, `is_encrypted`, `wrapped_encryption_key`, `created_at` | Nhóm và trạng thái/khóa mã hóa được bảo vệ. |
| `group_members` | `group_id`, `user_id`, `is_admin`, `encrypted_group_key`, `joined_at` | Thành viên và quyền Admin nhóm; key dự phòng nếu E2EE. |
| `group_messages` | `id`, `group_id`, `sender_id`, `content`, `iv`, `created_at` | Tin nhóm có thể mã hóa. |
| `spam_reports` | `id`, `reporter_id`, `reported_id`, `reason`, `created_at` | Báo cáo spam. |

### 3.1 Quy tắc đã thống nhất

1. **`users.status`** = `ACTIVE`, `LOCKED`, `DELETED` (soft delete); `password_hash` lưu bcrypt, không có `is_online` tĩnh. Khuyến nghị thêm `deleted_at` nếu cần nhưng chưa bắt buộc.
2. **`friendships`**: `PRIMARY KEY (user_id_1,user_id_2)` và `CHECK (user_id_1 < user_id_2)` để chống trùng/đảo chiều.
3. **`user_blocks`**: block có hướng; block phải hủy quan hệ bạn bè, ngăn lời mời và tìm kiếm theo đúng chiều nghiệp vụ.
4. **`message_hidden`**: xóa một/vài tin bên người dùng đó; đối phương không mất tin.
5. **`conversation_clears`**: mốc xóa toàn bộ lịch sử một phía, `(user_id, partner_id)` duy nhất; các tin mới sau khi xóa phải hiển thị.
6. **`activity_logs`**: ghi `OPEN_APP` một lần khi bắt đầu phiên sử dụng thực, không tăng vì Socket reconnect.
7. **Admin stats**: số đối tác chat = `COUNT(DISTINCT receiver_id)` của tin do user chủ động gửi; số nhóm chat = `COUNT(DISTINCT group_id)` của tin nhóm do user gửi; biểu đồ hoạt động = số `DISTINCT user_id` có `OPEN_APP` mỗi tháng.
8. **`ON DELETE`**: giữ lịch sử quan trọng bằng RESTRICT/NO ACTION; chỉ CASCADE dữ liệu thực sự phụ thuộc (ví dụ liên kết thành viên theo nhóm, dòng ẩn tin theo tin gốc khi xóa vật lý). Soft Delete user không kích hoạt CASCADE.
9. **Mã hóa**: AES-GCM, IV/nonce riêng, ciphertext gồm tag theo cách xử lý của Java; không lưu khóa thô. `wrapped_encryption_key` chỉ là dự kiến dùng khi mã hóa phía Server, phải bảo vệ khóa wrapping/master bên ngoài DB.

### 3.2 Review P1 — CẦN XỬ LÝ trước khi chốt `database.sql`

- Thêm rõ `NOT NULL` cho trường bắt buộc; `CHECK` cho `users.status`, `friend_requests.status`, `activity_logs.action_type`; cân nhắc `CHECK sender_id <> receiver_id`, `CHECK blocker_id <> blocked_id`, `CHECK user_id <> partner_id`.
- `friend_requests`: chống nhiều PENDING cùng cặp cả khi đảo chiều. **Đề xuất** partial unique index PostgreSQL:

```sql
CREATE UNIQUE INDEX uq_pending_friend_pair
ON friend_requests (
  LEAST(sender_id, receiver_id),
  GREATEST(sender_id, receiver_id)
)
WHERE status = 'PENDING';
```

- **Không tùy tiện hard-delete tài khoản**, vì lịch sử tin nhắn, login và spam có liên quan. Chính sách từng FK phải ghi rõ.
- **Transaction + kiểm tra quyền**: chấp nhận kết bạn, block, tạo nhóm/thêm creator làm Admin, gửi chat nhóm, xóa tin, báo cáo spam, chỉ Admin nhóm xóa người.
- **`conversation_clears`**: chỉ so sánh `created_at > cleared_at` có thể thiếu ổn định nếu nhiều tin có timestamp trùng hoặc transaction gửi trễ. Chốt semantics và cách xử lý đồng thời.
- **`group_messages`**: cần biểu diễn tin plaintext/encrypted và phiên bản khóa nếu có đổi khóa; `key_version` là đề xuất của A, **chưa phải cột đã chốt**. Xác nhận phạm vi mã hóa với thầy trước khi hoàn thiện schema khóa.

### 3.3 Review P2 — cải tiến trong quá trình thực hiện

- Index đúng truy vấn: lịch sử chat hai chiều và chat nhóm, `(receiver_id,status)` yêu cầu kết bạn, `(user_id,created_at)` hoạt động, thời gian login và báo cáo spam; không tự tạo quá nhiều index.
- `is_read` không biểu diễn trạng thái đã giao tin; cân nhắc xử lý khi reconnect/multi-session.
- Quy tắc chữ hoa/thường của `username` và `email` cần thống nhất.
- Không xem hai FK hợp lệ là đủ để chứng minh user có quyền thao tác: Server phải kiểm tra quyền trong ngữ cảnh.

## 4. Chức năng liên kết UI ↔ Database

- **Admin** xem thống kê/lịch sử từ `users`, `login_history`, `activity_logs`, `friendships`, `chat_groups`, `group_members`, `spam_reports`, `direct_messages`, `group_messages`.
- **User** tạo dữ liệu từ đăng ký/login, friend requests, friendships, blocks, direct/group messages và spam reports.
- **Truy vấn nhóm/bạn bè phải nhất quán** vì hai phân hệ sử dụng chung một schema.
- **Phiên bản 1**: giao diện chỉ cần hiển thị đúng chức năng với dữ liệu mẫu; database script phải tạo các bảng/ràng buộc có thể dùng ở phiên bản 2.

## 5. Công việc cụ thể của thành viên A — triển khai ngay

1. Nhận từ B repository và xác nhận branch mới nhất `feat/admin` (không tự thay tên branch khi repo đã tồn tại).
2. Cùng B review ERD cuối: tất cả 13 bảng, kiểu PostgreSQL, NULL/NOT NULL, PK/FK/UNIQUE/CHECK, chỉ mục và chính sách ON DELETE.
3. Viết `script/database.sql` sao cho có thể chạy lại từ database rỗng; kiểm thử trường hợp sai (username trùng, trạng thái sai, FK vô hiệu, lời mời PENDING trùng/đảo chiều, cặp bạn bè đảo ID...); xuất ERD.
4. Xây dựng khung Admin Swing + FlatLaf, 9 nhóm màn hình Admin; màn hình có thể dùng dummy data ở mốc 20%.
5. Chụp minh chứng GUI và commit của A; gửi B phần giải thích DB/ERD + màn hình Admin để tổng hợp báo cáo.
6. Không sửa `com.chatapp.user` nếu không được yêu cầu; thay đổi `server`, `shared`, `pom.xml` phải phối hợp với B để không làm vỡ phần B đang triển khai.

## 6. Các điểm chưa được quyết định hoàn toàn

- E2EE **hay** mã hóa tại Server cho nhóm; chính sách khóa và phân phối khóa; thay đổi mã hóa khi nhóm đã có lịch sử.
- Quy tắc chính xác khi timestamp xóa lịch sử trùng hoặc có tin đến trễ; đề xuất mốc tin nhắn ID cần cân nhắc trước khi code.
- SQL schema P1 và index thực tế **chưa nghiệm thu**, dù danh sách 13 bảng đã được đồng ý về định hướng.
- LLM nhà cung cấp/model/API cụ thể, cấu hình email/SMTP, Maven build nhiều file JAR và cách phân phối Server.
- Phong cách giao diện và màn hình chuẩn chưa có bộ quy định cuối cùng.

## 7. Các điều không nên làm

- Không thay đổi tech stack đã thống nhất vì thói quen của AI (không JavaFX/Spring Boot/Supabase/Jackson khi chưa được duyệt).
- Không viết lại kiến trúc hoặc sinh toàn bộ dự án chỉ từ một prompt khổng lồ mà không có review và test.
- Không khẳng định 13 bảng đã hoàn chỉnh chỉ vì đã có tên/cột.
- Không lưu plaintext password, khóa AES gốc, database credentials hoặc LLM API keys trong source/client.
- Không tự ý sửa code User do B phụ trách, không tự push/merge/commit trừ khi A trực tiếp yêu cầu.

## 8. Danh mục tài liệu nguồn cần giữ trong project

- `Final Project.pdf` — yêu cầu đề bài gốc. Đây là nguồn có ưu tiên cao nhất khi xác định tiêu chí cần nộp.
- `Feedback 2 ben` — các vòng phản hồi A/B và bản 13 bảng của B, cần đối chiếu phiên bản hiện hành.
- `Tech_Stack.md` — bảng công nghệ quyết định, ưu tiên dùng để ngăn tự ý đổi stack.
- `AGENTS.md` — quy tắc cho Codex trong repository.

**Quy tắc ưu tiên khi có xung đột:** yêu cầu thầy trong đề gốc → quyết định A/B mới nhất được xác nhận → `Tech_Stack.md`/tài liệu context được cập nhật → đề xuất kỹ thuật chưa được duyệt. Nếu phát hiện xung đột, báo rõ thay vì âm thầm thay đổi.
