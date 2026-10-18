# Review ERD Java Chat System

Ngày review: 16/10/2026. Phạm vi: tài liệu và ERD của thành viên A; chưa triển khai SQL hoặc Java. Sơ đồ giữ nguyên **13 bảng, 64 cột và 21 khóa ngoại** trong đề xuất DOCX của B. Baseline đã được đồng ý về cấu trúc; chi tiết P1 chưa được nghiệm thu.

## 1. Nguồn và mức độ xác nhận

Đã đọc đầy đủ [AGENTS.md](../../../AGENTS.md), [Final Project.pdf](../../Final%20Project.pdf) (5 trang), [Tech_Stack.md](../../Tech_Stack.md), [Project_Context.md](../../Project_Context.md) và [Database_Schema_Draft.docx](Database_Schema_Draft.docx). DOCX có 123 paragraph, không có bảng Word, hình, tracked changes hay phần comment/footnote/header/footer riêng. Nội dung được trích trực tiếp từ OOXML; không suy đoán phần chưa đọc. Đề bài PDF được trích văn bản và kiểm tra các trang render.

Thứ tự ưu tiên: đề bài chính thức → quyết định A/B đã xác nhận → hai tài liệu context/stack → đề xuất chưa duyệt. `Feedback 2 ben` được context nhắc đến nhưng không có trong bộ nguồn được cung cấp; review này không tự gán thêm quyết định từ tài liệu đó.

| Mức | Nội dung | Cách sử dụng trong bộ ERD |
|---|---|---|
| Yêu cầu chính thức | Các chức năng Admin/User, nộp script tạo DB và thiết kế DB trong báo cáo; PDF tr. 2–5. | Dùng để đánh giá độ bao phủ chức năng; đề không ấn định 13 bảng, PostgreSQL, AES-GCM hoặc E2EE. |
| Đã thống nhất A/B | PostgreSQL local; ID tự tăng phù hợp bằng `BIGINT GENERATED ALWAYS AS IDENTITY`; `TIMESTAMPTZ`; BCrypt; soft delete; danh sách 13 bảng; cặp bạn bè chuẩn hóa; block có hướng; xóa lịch sử một phía; OPEN_APP theo phiên thật; thống kê theo tin chủ động gửi; Server kiểm tra quyền và transaction. | Giữ nguyên định hướng trong `Tech_Stack.md` §5 và `Project_Context.md` §3.1. |
| Có trong đề xuất B, chưa duyệt cuối | Các khai báo cột/NULL/default, CHECK, từng ON DELETE, 5 index, cách đếm session, điều kiện xóa lịch sử theo timestamp, hai cột khóa mã hóa và `iv`. | Chép nguyên vào Markdown; biểu diễn hiện trạng trong Mermaid. Không coi tiêu đề “chuẩn hóa (P1)” trong DOCX là bằng chứng đã nghiệm thu. |
| Còn mở đã được context ghi nhận | E2EE hay phía Server; vòng đời khóa; `key_version`; xử lý xóa lịch sử đồng thời; quy tắc hoa/thường; index thực tế; trạng thái giao tin. | Chỉ nêu để A/B chốt. Không thêm cột/bảng vào ERD. |
| Nhận xét/đề xuất mới của Codex | Các phân tích thiếu bảo đảm và phương án tại §6–8, được ghi nhãn rõ. | Chưa có hiệu lực thiết kế, chưa được thực thi. |

## 2. Danh sách 13 bảng và khóa chính

| Bảng | Số cột | PK theo DOCX | Vai trò |
|---|---:|---|---|
| `users` | 11 | `id` | Hồ sơ, thông tin đăng nhập BCrypt, trạng thái ACTIVE/LOCKED/DELETED, thời điểm tạo và last seen. |
| `login_history` | 3 | `id` | Lưu mỗi lần đăng nhập để Admin xem riêng user hoặc toàn hệ thống. |
| `activity_logs` | 5 | `id` | Sự kiện OPEN_APP và session phục vụ thống kê hoạt động. |
| `friend_requests` | 5 | `id` | Lời mời có hướng và trạng thái PENDING/ACCEPTED/REJECTED. |
| `friendships` | 3 | `(user_id_1, user_id_2)` | Quan hệ bạn bè vô hướng, một dòng với ID nhỏ đứng trước. |
| `user_blocks` | 3 | `(blocker_id, blocked_id)` | Quan hệ block có hướng; không được chuẩn hóa bằng min/max. |
| `direct_messages` | 6 | `id` | Tin cá nhân, người gửi/người nhận, nội dung, cờ đã đọc và thời gian. |
| `message_hidden` | 3 | `(message_id, user_id)` | Đánh dấu một tin bị ẩn riêng đối với một người. |
| `conversation_clears` | 3 | `(user_id, partner_id)` | Mốc xóa toàn bộ lịch sử với đối tác theo từng phía. |
| `chat_groups` | 6 | `id` | Tên, người tạo, trạng thái mã hóa và wrapped key dự kiến. |
| `group_members` | 5 | `(group_id, user_id)` | Bảng trung gian user–group, quyền Admin nhóm và encrypted key dự kiến cho thành viên. |
| `group_messages` | 6 | `id` | Tin nhóm; nội dung có thể là ciphertext, `iv` Base64 theo DOCX. |
| `spam_reports` | 5 | `id` | Người báo cáo, người bị báo cáo, lý do và thời gian. |

Tám PK đơn `id` đều là `BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`. Năm PK ghép chỉ bảo đảm duy nhất **cả tổ hợp**, không làm từng thành phần trở thành UNIQUE riêng. `group_members.is_admin` là quyền Admin **nhóm**, không phải quyền Admin hệ thống.

## 3. Toàn bộ 21 FK, cardinality và ON DELETE

Tất cả FK đều `BIGINT NOT NULL`, trỏ tới PK `id`. Với từng vai trò FK, mỗi dòng con gắn đúng **1** dòng cha; một dòng cha có **0..N** dòng con. Đây là cardinality hiện được các khai báo bảo đảm, không hàm ý một user luôn phải có tin nhắn hay một nhóm đã có thành viên. Mermaid ghi riêng tên cột và ON DELETE trên từng đường nối để phân biệt các vai trò cùng trỏ về `users`.

| # | FK ở bảng con | Tham chiếu | Cha → con | ON DELETE | FK nằm trong PK con |
|---:|---|---|---|---|---|
| 1 | `login_history.user_id` | `users.id` | 1 → 0..N | RESTRICT | Không |
| 2 | `activity_logs.user_id` | `users.id` | 1 → 0..N | RESTRICT | Không |
| 3 | `friend_requests.sender_id` | `users.id` | 1 → 0..N | CASCADE | Không |
| 4 | `friend_requests.receiver_id` | `users.id` | 1 → 0..N | CASCADE | Không |
| 5 | `friendships.user_id_1` | `users.id` | 1 → 0..N | CASCADE | Có |
| 6 | `friendships.user_id_2` | `users.id` | 1 → 0..N | CASCADE | Có |
| 7 | `user_blocks.blocker_id` | `users.id` | 1 → 0..N | CASCADE | Có |
| 8 | `user_blocks.blocked_id` | `users.id` | 1 → 0..N | CASCADE | Có |
| 9 | `direct_messages.sender_id` | `users.id` | 1 → 0..N | RESTRICT | Không |
| 10 | `direct_messages.receiver_id` | `users.id` | 1 → 0..N | RESTRICT | Không |
| 11 | `message_hidden.message_id` | `direct_messages.id` | 1 → 0..N | CASCADE | Có |
| 12 | `message_hidden.user_id` | `users.id` | 1 → 0..N | CASCADE | Có |
| 13 | `conversation_clears.user_id` | `users.id` | 1 → 0..N | CASCADE | Có |
| 14 | `conversation_clears.partner_id` | `users.id` | 1 → 0..N | CASCADE | Có |
| 15 | `chat_groups.created_by` | `users.id` | 1 → 0..N | RESTRICT | Không |
| 16 | `group_members.group_id` | `chat_groups.id` | 1 → 0..N | CASCADE | Có |
| 17 | `group_members.user_id` | `users.id` | 1 → 0..N | RESTRICT | Có |
| 18 | `group_messages.group_id` | `chat_groups.id` | 1 → 0..N | CASCADE | Không |
| 19 | `group_messages.sender_id` | `users.id` | 1 → 0..N | RESTRICT | Không |
| 20 | `spam_reports.reporter_id` | `users.id` | 1 → 0..N | RESTRICT | Không |
| 21 | `spam_reports.reported_id` | `users.id` | 1 → 0..N | RESTRICT | Không |

Có **9 RESTRICT và 12 CASCADE**. Soft delete cập nhật `users.status`, không kích hoạt ON DELETE. RESTRICT ngăn xóa vật lý cha khi còn dòng con liên quan; CASCADE chỉ áp dụng khi có DELETE vật lý. Xóa vật lý nhóm sẽ xóa thành viên và tin nhóm theo đề xuất; cần A/B xác nhận chính sách lưu lịch sử, không tự đổi ở đây. Nguồn chưa khai báo ON UPDATE hoặc DEFERRABLE; ERD không tự bổ sung.

**Không có quan hệ FK 1–1 trong schema này:** không FK nào UNIQUE độc lập. “Chat 1–1” là loại hội thoại giữa hai người; các FK của `direct_messages` vẫn là 1–N. Một dòng mốc xóa trên một cặp `(user_id, partner_id)` cũng không biến `users` → `conversation_clears` thành 1–1.

Các quan hệ nhiều–nhiều đã được tách bằng bảng trung gian:

- User ↔ user qua `friendships`: vô hướng, chuẩn hóa ID; qua `friend_requests`: có hướng, nhiều dòng lịch sử được phép ngoài trường hợp PENDING bị chặn trùng; qua `user_blocks`: có hướng.
- User ↔ group qua `group_members`: một user tham gia nhiều nhóm, một nhóm có nhiều user; PK ghép chặn tham gia trùng.
- User ↔ tin cá nhân qua `message_hidden`: một bản đánh dấu ẩn cho một cặp; giới hạn chỉ hai người trong hội thoại là nghiệp vụ chưa được FK bảo đảm.
- User ↔ đối tác qua `conversation_clears`: mỗi cặp có hướng có tối đa một mốc xóa. `(A,B)` và `(B,A)` độc lập.

Không vẽ quan hệ FK từ `group_messages` tới `group_members`, từ `spam_reports` tới `direct_messages`, hoặc từ `friend_requests` tới `friendships` vì DOCX không khai báo các FK đó. Những liên hệ nghiệp vụ này cần Server xử lý.

## 4. NULL, UNIQUE, CHECK và DEFAULT theo từng bảng

Trong bảng dưới, “bắt buộc” gồm cả NOT NULL tường minh và NOT NULL do PRIMARY KEY. Có **55 cột bắt buộc và 9 cột cho phép NULL**. Kiểu/độ dài nguyên bản của toàn bộ 64 cột nằm trong [bản chuyển đổi](Database_Schema_Draft.md) và [Mermaid](ERD_2026-10-16.mmd).

| Bảng | Cột bắt buộc | Cột cho phép NULL | UNIQUE/CHECK theo đề xuất |
|---|---|---|---|
| `users` | `id, username, password_hash, full_name, email, status, created_at` | `address, dob, gender, last_seen_at` | UNIQUE `username`; UNIQUE `email`; `gender IN ('MALE','FEMALE','OTHER')`; `status IN ('ACTIVE','LOCKED','DELETED')`. |
| `login_history` | `id, user_id, login_time` | Không | Không có CHECK/UNIQUE ngoài PK. |
| `activity_logs` | `id, user_id, action_type, created_at` | `session_id` | `action_type IN ('OPEN_APP')`; không có UNIQUE session. |
| `friend_requests` | `id, sender_id, receiver_id, status, created_at` | Không | `status IN ('PENDING','ACCEPTED','REJECTED')`; `chk_fr_no_self`: `sender_id <> receiver_id`; partial unique expression index tại §5. |
| `friendships` | `user_id_1, user_id_2, created_at` | Không | `chk_friendship_order`: `user_id_1 < user_id_2`; duy nhất cặp nhờ PK. |
| `user_blocks` | `blocker_id, blocked_id, created_at` | Không | `chk_block_no_self`: `blocker_id <> blocked_id`; duy nhất cặp có hướng nhờ PK. |
| `direct_messages` | `id, sender_id, receiver_id, content, is_read, created_at` | Không | `chk_dm_no_self`: `sender_id <> receiver_id`. |
| `message_hidden` | `message_id, user_id, hidden_at` | Không | Chỉ PK ghép; không có CHECK xác nhận user là sender/receiver. |
| `conversation_clears` | `user_id, partner_id, cleared_at` | Không | `chk_clear_no_self`: `user_id <> partner_id`; duy nhất cặp có hướng nhờ PK. |
| `chat_groups` | `id, group_name, created_by, is_encrypted, created_at` | `wrapped_encryption_key` | Không có CHECK quan hệ flag–key hoặc số thành viên. |
| `group_members` | `group_id, user_id, is_admin, joined_at` | `encrypted_group_key` | Chỉ PK ghép; không có CHECK bảo đảm nhóm còn Admin. |
| `group_messages` | `id, group_id, sender_id, content, created_at` | `iv` | Không có CHECK về nội dung mã hóa, IV hoặc membership. |
| `spam_reports` | `id, reporter_id, reported_id, created_at` | `reason` | `chk_spam_no_self`: `reporter_id <> reported_id`; không có UNIQUE cặp hoặc FK tới tin đã chat. |

Có **10 biểu thức CHECK**: 4 CHECK miền giá trị và 6 CHECK ngăn tự liên hệ/chuẩn hóa cặp. CHECK của `gender` vẫn cho phép NULL theo khai báo gốc. Không tự thêm tên cho các CHECK miền giá trị chưa được đặt tên trong nguồn.

Các DEFAULT được giữ nguyên:

- `NOW()`: `users.created_at`, `login_history.login_time`, `activity_logs.created_at`, `friend_requests.created_at`, `friendships.created_at`, `user_blocks.created_at`, `direct_messages.created_at`, `message_hidden.hidden_at`, `chat_groups.created_at`, `group_members.joined_at`, `group_messages.created_at`, `spam_reports.created_at`.
- `'ACTIVE'`: `users.status`; `'PENDING'`: `friend_requests.status`; `'OPEN_APP'`: `activity_logs.action_type`.
- `FALSE`: `direct_messages.is_read`, `chat_groups.is_encrypted`, `group_members.is_admin`.
- `conversation_clears.cleared_at` không có DEFAULT; `last_seen_at` và các cột nullable khác cũng không có DEFAULT được khai báo.

`NOT NULL` không tự cấm chuỗi rỗng, không xác nhận định dạng email và không kiểm tra nội dung hash BCrypt; đây là nhận xét review, chưa phải constraint bổ sung. PK/UNIQUE có chỉ mục hỗ trợ do PostgreSQL tạo; FK không tự tạo chỉ mục trên cột bảng con. Xem [tài liệu PostgreSQL về constraints](https://www.postgresql.org/docs/current/ddl-constraints.html).

## 5. Toàn bộ index được ghi trong DOCX

Có **5 khai báo CREATE INDEX**, gồm 4 index thường và 1 unique index có điều kiện trên biểu thức. Đây là nội dung của đề xuất B, chưa xác nhận đã tồn tại trong database.

| Tên | Bảng/khóa theo đúng thứ tự nguồn | Đặc tính và mục đích |
|---|---|---|
| `idx_login_history_time` | `login_history (login_time DESC)` | Xem lịch sử đăng nhập chung theo thời gian. |
| `uq_pending_friend_pair` | `friend_requests (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) WHERE status = 'PENDING'` | UNIQUE có điều kiện; chặn hai PENDING cùng cặp, kể cả đảo chiều. Không làm `sender_id` hay `receiver_id` UNIQUE riêng. |
| `idx_dm_pair_time` | `direct_messages (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), created_at)` | Lịch sử chat hai chiều theo cặp và thời gian. Không phải index tìm chuỗi nội dung. |
| `idx_group_messages_time` | `group_messages (group_id, created_at)` | Lịch sử chat nhóm theo thời gian. |
| `idx_spam_reports_time` | `spam_reports (created_at DESC)` | Danh sách spam theo thời gian. |

Ngoài 5 index tường minh, 13 PK và 2 UNIQUE đơn sẽ có index hỗ trợ nếu triển khai đúng khai báo PostgreSQL; không tự đặt tên các index tự sinh trong ERD. Không có index `(receiver_id,status)`, `(user_id,created_at)`, `friendships(user_id_2)`, chỉ mục tìm chuỗi hoặc index thống kê người gửi được khai báo trong DOCX. `Project_Context.md` §3.3 mới nêu một số hướng bổ sung ở mức P2; không được trình bày chúng là index đã có.

## 6. Các điểm P1 cần chốt trước database.sql

### 6.1 Tình trạng P1 so với context

**Nhận xét Codex:** DOCX mới đã có NOT NULL cho các trường bắt buộc được liệt kê, CHECK miền giá trị/ngăn tự liên hệ, partial unique PENDING hai chiều, chính sách ON DELETE từng FK và 5 index cụ thể. Như vậy một phần yêu cầu review trong context đã được đưa vào bản đề xuất; không nên báo rằng chúng vẫn hoàn toàn chưa có. Tuy nhiên context vẫn nói chi tiết P1 chưa nghiệm thu, nên cần A/B duyệt các khai báo thực tế này và sau đó mới kiểm thử SQL trên DB rỗng.

### 6.2 OPEN_APP và định nghĩa chỉ số

**Nguồn:** quyết định A/B yêu cầu đúng một bản ghi mỗi phiên thật, không phát sinh bản ghi mới vì reconnect. DOCX cho `session_id NULL`, giữ cùng ID khi reconnect và dùng `COUNT(DISTINCT session_id)` hoặc `COUNT(DISTINCT user_id)`.

**Vấn đề:** giữ cùng ID và đếm DISTINCT có thể che các dòng log bị ghi lặp; schema không ngăn ghi trùng, và session NULL không được COUNT(DISTINCT session_id) tính vào. Hai COUNT không thay thế nhau: số phiên dùng để thống kê lượt mở app theo user; DISTINCT user theo tháng dùng cho biểu đồ số người hoạt động. Không nên dùng “hoặc” mà không phân biệt chỉ số.

**Cần A/B xác nhận:** ai cấp session, phạm vi duy nhất của ID, lúc nào bắt đầu/kết thúc phiên thật, nhiều thiết bị/đăng nhập đồng thời, cơ chế idempotency khi reconnect và phiên qua ranh giới tháng. **Đề xuất mới Codex:** cân nhắc bắt buộc session và duy nhất trên `(user_id, session_id, action_type)` hoặc một cơ chế Server có bảo đảm tương đương; chỉ xem xét sau khi chốt semantics. Chưa đổi NULL hay thêm index này vào schema.

### 6.3 Xóa lịch sử một phía và đồng thời

**Nguồn:** hiện trạng là `created_at > cleared_at` và loại `message_hidden` của đúng user. Context đã nêu timestamp trùng/tin gửi trễ là P1.

**Vấn đề:** chưa nêu hành vi khi chưa có dòng clear, khi timestamp bằng nhau, transaction gửi đã bắt đầu trước thao tác clear nhưng commit sau, hay hai thao tác clear chạy đồng thời. PostgreSQL `NOW()` theo thời điểm bắt đầu transaction có thể làm thời gian dữ liệu khác thứ tự commit; xem [hàm thời gian PostgreSQL](https://www.postgresql.org/docs/current/functions-datetime.html#FUNCTIONS-DATETIME-CURRENT). Điều kiện tìm kiếm, lịch sử và tải tin offline phải cùng áp dụng quyền nhìn thấy; kết quả tìm kiếm không được trả lại tin đã ẩn/clear.

**Cần A/B xác nhận:** clear dựa trên thời điểm Server nhận lệnh, thứ tự được lưu hay tập tin đã nhìn thấy; khi chưa clear thì xem toàn bộ tin không bị ẩn; clear mới không được lùi mốc cũ. Mốc ID là hướng đã được context nhắc tới, chưa được duyệt; ID tự tăng cũng không tự bảo đảm thứ tự commit. Không thêm cột mốc ID trong ERD này.

### 6.4 Quyền và transaction không được FK bảo đảm

| Luồng | Đã có trong nguồn | Điểm phải chốt/bảo đảm trước triển khai |
|---|---|---|
| Chấp nhận lời mời | Trạng thái + PK cặp bạn bè + unique PENDING | Xác nhận đúng người nhận, lời mời còn PENDING, không block; cập nhật trạng thái và tạo friendship nguyên tử; xử lý hai thao tác đồng thời. |
| Block | Transaction thêm block, xóa friendship, hủy lời mời | “Hủy mọi lời mời” là DELETE hay chuyển REJECTED, chỉ PENDING hay cả lịch sử? Không có trạng thái CANCELLED trong nguồn. Chốt thứ tự khóa để accept/send request không lọt qua block. |
| Tìm người/kết bạn | Block có hướng | Người bị block không thấy người block mình và không được gửi lời mời lại theo PDF. Việc chặn cả hai hướng gửi lời mời/chat hoặc chặn thêm thành viên nhóm chưa được quy định đầy đủ; không tự suy diễn. |
| Ẩn tin | PK `(message_id,user_id)` | Server phải xác nhận user là sender hoặc receiver; FK hiện cho phép một user bất kỳ đánh dấu tin không thuộc mình nếu không kiểm tra quyền. |
| Tạo/quản lý nhóm | `created_by`, membership, cờ Admin nhóm | Tạo nhóm có creator và ít nhất một username khác theo PDF; thêm creator làm Admin trong transaction theo context. FK không bảo đảm creator nằm trong nhóm, số thành viên tối thiểu hay luôn có Admin. Chốt xóa Admin cuối, tự rời nhóm và chuyển quyền. |
| Gửi tin nhóm | FK group và sender riêng | Xác nhận sender là thành viên hợp lệ tại thời điểm gửi; xử lý tranh chấp với remove member. Không tự thêm composite FK membership: cần cân nhắc giữ lịch sử sau khi rời nhóm. |
| Báo cáo spam | Hai FK user và CHECK không tự báo cáo | PDF mô tả người bị báo cáo đã chat với người báo cáo. Chốt có cần tin từ `reported_id` gửi tới `reporter_id`, chat nhóm có tính không, và lịch sử ẩn/clear có còn làm bằng chứng không. Không có FK tới tin bằng chứng trong baseline. |
| Quyền Admin hệ thống | Có phân hệ Admin theo PDF | `users` không có cột role; 13 bảng không có bảng tài khoản Admin riêng. Nguồn chưa chỉ rõ cách xác thực/phân quyền Admin hệ thống ở Server. A/B phải chốt cơ chế; `is_admin` nhóm không giải quyết quyền này. Đây là phát hiện mới của Codex, không phải yêu cầu tự thêm bảng. |

Server cũng cần kiểm tra ACTIVE/LOCKED/DELETED trước các thao tác được phép, và chốt xử lý session đang mở khi khóa tài khoản. Không dùng CHECK truy vấn bảng khác để thay thế toàn bộ quyền nghiệp vụ; PostgreSQL không hỗ trợ CHECK bảo đảm dữ liệu bảng khác theo cách đó. Xem [giới hạn CHECK](https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-CHECK-CONSTRAINTS).

### 6.5 Danh tính và chính sách xóa

**Cần duyệt từ nguồn hiện tại:** từng RESTRICT/CASCADE ở §3; soft delete không xóa tin; có được tái sử dụng username/email của user DELETED hay không. UNIQUE hiện vẫn bao phủ user DELETED. Khuyến nghị `LOWER(username)` khi tìm kiếm trong DOCX không phải constraint duy nhất không phân biệt hoa/thường.

**Quyết định còn mở trong context (P2, nên thống nhất trước DDL nếu ảnh hưởng UNIQUE):** chính sách hoa/thường của username/email. Chưa tự thêm `citext`, functional unique index hoặc đổi kiểu cột. Các kiểm tra định dạng email, tên rỗng, ngày sinh và độ dài nội dung cần quy tắc nhập liệu; chưa coi là constraint đã duyệt.

## 7. Mã hóa nhóm còn chờ A/B và giảng viên xác nhận

**Đã thống nhất:** dùng hướng AES-GCM với IV/nonce và metadata giải mã; ciphertext gồm authentication tag theo cách xử lý Java; không lưu AES key plaintext; master/wrapping key và secret phải ở ngoài repo/client. **Chưa được xác nhận:** mã hóa phía Server hay E2EE. PDF tr. 3 §12g chỉ yêu cầu mã hóa nhóm và dẫn tham khảo tính năng Facebook; không thể kết luận chỉ mã hóa tại Server đã đáp ứng cách hiểu của giảng viên.

| Vấn đề | Hiện trạng giữ nguyên trong ERD | Quyết định cần xác nhận |
|---|---|---|
| Mô hình tin cậy | `chat_groups.wrapped_encryption_key TEXT NULL`; `group_members.encrypted_group_key TEXT NULL` | Cột nào dùng trong mô hình đã chọn, ai giải mã được, ai có khóa bảo vệ, cách phân phối khóa/định danh người nhận nếu E2EE. Có cả hai cột không chứng minh E2EE hoàn chỉnh. |
| Payload mã hóa | `group_messages.content TEXT NOT NULL`; `iv VARCHAR(64) NULL` | Định dạng ciphertext/tag/Base64, kích thước IV, quy tắc IV không lặp với cùng khóa; mã hóa/giải mã tại đâu; lỗi xác thực tag xử lý thế nào. |
| Phân biệt tin cũ/mới | Chỉ có cờ `chat_groups.is_encrypted` ở cấp nhóm | Có được bật/tắt mã hóa khi đã có lịch sử không? Cờ hiện tại không tự nói từng tin cũ là plaintext hay ciphertext. `iv NULL` chưa được nguồn định nghĩa chính thức là discriminator. |
| Đổi khóa | Chưa có `key_version` hoặc lịch sử khóa | Có rotate khi thêm/xóa thành viên không, thành viên mới được đọc lịch sử nào, người rời nhóm giữ quyền giải mã tin cũ hay không; cách đọc lại tin dùng khóa cũ. `key_version` là đề xuất đã có trong context, chưa phải cột được duyệt. |
| Nhất quán dữ liệu | Chưa có CHECK flag–key–IV; FK không kiểm tra membership | Cần quy tắc nào ở DB, quy tắc nào ở Server; các tổ hợp NULL nào hợp lệ đối với mô hình chọn. |
| Khôi phục tài khoản | Email reset ngẫu nhiên và BCrypt được yêu cầu/chọn | Nếu khóa thành viên phụ thuộc bí mật người dùng, đổi/reset mật khẩu có ảnh hưởng khả năng giải mã không? Nguồn chưa nêu quy trình khôi phục khóa. Đây là câu hỏi review mới của Codex. |

**Đề xuất mới Codex, chưa duyệt:** trước DDL hãy lập một đặc tả ngắn về payload, vòng đời khóa và quyền đọc lịch sử; quyết định có cho phép chuyển chế độ/rotate trong phạm vi đồ án không. Nếu kết quả cần thay schema thì phải A/B review riêng. Không lén thêm `key_version`, public key, key history hoặc bảng thứ 14 vào bộ ERD hiện tại.

## 8. Đối chiếu yêu cầu Final Project.pdf

### 8.1 Chín nhóm Admin ở trang 2

| Yêu cầu | Dữ liệu hiện có | Đánh giá và điều kiện còn lại |
|---|---|---|
| 1. Quản lý user, lọc/sắp xếp, CRUD, khóa/mở, mật khẩu, lịch sử, bạn bè | `users`, `login_history`, `friendships` | Đủ trường hồ sơ; soft delete là quyết định A/B cho hành vi xóa. Cần xác thực Admin hệ thống và xác nhận quy tắc lọc status. |
| 2. Danh sách login theo thời gian, username, họ tên | `login_history` JOIN `users`; `idx_login_history_time` | Biểu diễn được. Hồ sơ hiện tại được JOIN; không có snapshot tên tại lần login, đề không bắt buộc snapshot. |
| 3. Nhóm, thành viên, Admin nhóm | `chat_groups`, `group_members`, `users` | Biểu diễn được; không nhầm creator với toàn bộ Admin nhóm. |
| 4. Báo cáo spam và khóa tài khoản | `spam_reports`, `users`; `idx_spam_reports_time` | Biểu diễn danh sách/lọc được; cần quyền và điều kiện đã chat ở §6.4. |
| 5. Đăng ký mới theo khoảng ngày | `users.created_at`, `full_name` | Biểu diễn được; chốt có tính user DELETED hay không. |
| 6. Biểu đồ đăng ký 12 tháng theo năm | `users.created_at` | Biểu diễn được; truy vấn phải bổ sung tháng không có dữ liệu bằng 0, chốt múi giờ thống kê. |
| 7. Bạn trực tiếp và cột “tính luôn số lượng bạn của bạn” | `friendships` hai chiều | Cần làm rõ cách đếm; xem §8.3. Cần DISTINCT, loại chính user và tránh đếm nhiều đường trùng. |
| 8. Hoạt động trong khoảng thời gian | `activity_logs`, `direct_messages`, `group_messages`, `users` | Theo quyết định A/B: lượt mở app theo phiên; đối tác = DISTINCT receiver của tin do user gửi; nhóm = DISTINCT group của tin do user gửi. Chưa đủ bảo đảm session chống trùng/NULL. |
| 9. Biểu đồ người mở app mỗi tháng | `activity_logs.user_id`, `created_at` | DISTINCT user có OPEN_APP theo từng tháng; không dùng số dòng log hoặc số phiên thay số người. |

### 8.2 Mười hai nhóm User ở trang 3

| Yêu cầu | Dữ liệu hiện có | Đánh giá và điều kiện còn lại |
|---|---|---|
| 1–5. Đăng ký, sửa thông tin, reset, đổi mật khẩu, login | `users`, `login_history` | Biểu diễn được; mật khẩu lưu hash. Đề mô tả random password gửi email, không bắt buộc reset-token table. Gửi email và thay hash cần thiết kế xử lý lỗi; không tự thêm bảng. |
| 6–7. Bạn bè, yêu cầu kết bạn, accept/reject, hủy bạn, block | `friendships`, `friend_requests`, `user_blocks` | Có cấu trúc và partial unique PENDING; cần transaction, kiểm tra chiều block và quy tắc hủy request. Online/offline thuộc Server session đã được A/B chọn. |
| 8–9. Bạn online, tìm user, chat/tạo group, ẩn người block mình | Các bảng quan hệ + session Server + `users` | Không cần tự thêm `users.is_online`. Kiểm tra block trước trả kết quả tìm kiếm và thực hiện hành động. |
| 10. Báo cáo người đã chat với mình | `spam_reports`, `direct_messages` | Có dữ liệu để kiểm tra; FK đơn chưa bảo đảm điều kiện đã chat. |
| 11a–c. Realtime, offline, lịch sử | `direct_messages`, `created_at`, `is_read` | Lưu được tin offline/lịch sử; Socket/reconnect/đẩy tin là trách nhiệm Server. `is_read` không phải trạng thái đã giao. |
| 11d–e. Xóa toàn bộ, xóa một/vài dòng | `conversation_clears`, `message_hidden` | Khớp quyết định A/B về xóa riêng từng phía; phải chốt đồng thời ở §6.3. |
| 11f–g. Tìm chuỗi một hoặc mọi hội thoại, nhảy đến tin | `direct_messages.content`, `id`, sender/receiver; hai bảng xóa một phía | Có thể truy vấn nội dung và định vị bằng ID, lọc quyền nhìn thấy. Chưa có index tìm chuỗi trong DOCX; chưa chứng minh hiệu năng trên dữ liệu lớn. |
| 11h. LLM chỉnh/gợi ý chat | Không bắt buộc thêm bảng | Có thể xử lý qua Server/HttpClient theo stack; đề không yêu cầu lưu lịch sử gọi LLM. |
| 12a–f. Tạo nhóm + ít nhất một username, đổi tên, thêm, gán Admin, xóa thành viên, chat | `chat_groups`, `group_members`, `group_messages` | Biểu diễn được nhưng chưa có constraint bảo đảm số thành viên/creator/membership/quyền; cần Server + transaction. |
| 12g. Mã hóa nhóm | Các cột flag/key/content/iv hiện tại | Chưa đủ kết luận đáp ứng trước khi thầy xác nhận mô hình và A/B chốt vòng đời khóa. |

### 8.3 Khác biệt và thiếu rõ ràng phải báo cáo

1. **Bạn của bạn:** PDF tr. 2 §7 viết “1 cột bạn bè trực tiếp, 1 cột tính luôn số lượng bạn của bạn”. DOCX §3 định nghĩa tập bạn cấp hai **loại bạn trực tiếp và chính user**. Hai cách trình bày chưa chắc cho cùng giá trị ở cột thứ hai. Ví dụ U–A, U–B, A–C, B–C: số trực tiếp = 2; số chỉ cấp hai = 1 (C); số trực tiếp cộng cấp hai = 3. A/B cần xác nhận cách hiểu đề, hỏi thầy nếu cần. ERD không đổi; Markdown giữ nguyên định nghĩa DOCX.
2. **Hoạt động:** từ “hoặc” giữa DISTINCT session và DISTINCT user trong DOCX phải được tách theo hai chỉ số đã thống nhất. Giữ cùng session khi reconnect không tự bảo đảm đúng một dòng OPEN_APP. Đây là khoảng trống giữa mục tiêu A/B và ràng buộc đề xuất, không phải lý do tự sửa schema.
3. **Mã hóa:** PDF dẫn tính năng Facebook nhưng không chỉ định E2EE; context đã yêu cầu hỏi thầy. Không chọn ngầm mã hóa Server hay E2EE.
4. **Soft delete:** đề nói xóa tài khoản nhưng không bắt buộc xóa vật lý. A/B đã chọn DELETED để giữ lịch sử; chưa thấy mâu thuẫn trực tiếp với đề. Cần mô tả rõ hành vi này trong báo cáo cuối.

### 8.4 Sản phẩm database phải nộp

PDF tr. 4 yêu cầu script tạo DB ở cả mốc 20% và 80%, đồng thời có thiết kế DB trong báo cáo (tr. 4–5). Bộ tài liệu/ERD này hỗ trợ phần thiết kế, **chưa thay thế `script/database.sql`** và chưa chứng minh schema chạy được. Cấu hình kết nối/JAR ở mốc 80% là phần triển khai sau; không phát tán credential DB đặc quyền trong client theo quyết định stack.

## 9. Quy ước Mermaid và giới hạn biểu diễn

`ERD.mmd` sử dụng `erDiagram`, kiểu PostgreSQL nguyên dạng một token (`BIGINT`, `VARCHAR(n)`, `TEXT`, `DATE`, `TIMESTAMPTZ`, `BOOLEAN`), `PK`, `FK`, `UK` và comment. `NN` nghĩa là NOT NULL; `NULL` nghĩa là cho phép NULL; `IDENTITY` trong comment rút gọn chính xác `GENERATED ALWAYS AS IDENTITY`, không phải đổi sang SERIAL. Cột có nhiều vai trò ghi `PK, FK`. Hai cột UNIQUE đơn là `users.username` và `users.email`.

Theo [cú pháp Mermaid ER chính thức](https://mermaid.js.org/syntax/entityRelationshipDiagram.html), quan hệ dùng crow's foot; `||` = đúng một, `o{` = không hoặc nhiều. Đường liền được dùng khi FK thuộc PK bảng con; đường đứt khi FK không thuộc PK. Kiểu đường không thể hiện CASCADE/RESTRICT; nhãn đường mới ghi ON DELETE cùng tên vai trò FK.

Các thông tin SQL không được phép suy ra sai từ hình:

- Mermaid không thay DDL, không thực thi constraint, không chứng minh quyền nghiệp vụ hoặc transaction đúng. Không dùng UK trên từng cột của PK ghép hay của unique index biểu thức.
- Partial unique index `uq_pending_friend_pair`, biểu thức LEAST/GREATEST, CHECK và tên constraint nằm đầy đủ trong Markdown/§4–5; không thể hiện như UNIQUE đơn trong hình.
- Identity, default và NULL được ghi bằng comment hỗ trợ đọc; Mermaid không kiểm tra ngữ nghĩa PostgreSQL. Các ghi chú ciphertext/Base64/BCrypt và quy tắc nghiệp vụ đầy đủ vẫn ở bản chuyển đổi.
- Cardinality là theo FK hiện tại, nên không vẽ nhóm có tối thiểu hai thành viên hoặc chỉ hai người có quyền ẩn tin như thể SQL đã cưỡng chế.
- Hình tổng quan 13 bảng cần xem ở kích thước lớn/phóng to. Nên chèn SVG vào báo cáo để giữ chữ sắc; nếu thu toàn bộ hình vào một trang A4, có thể cần trang ngang hoặc các hình phụ theo nhóm. Chưa tạo thêm bảng/cột hay quan hệ giả chỉ để điều chỉnh bố cục.

## 10. Kiểm tra và việc tiếp theo

Đã kiểm tra thành công:

| Kiểm tra | Kết quả |
|---|---|
| Bảo toàn nội dung DOCX | Đối chiếu lại trực tiếp OOXML: giữ đủ 115 paragraph nội dung không rỗng; paragraph nhãn ngôn ngữ `sql` được chuyển thành nhãn code fence. Không thiếu định nghĩa cột, constraint, index, quy tắc hoặc truy vấn minh họa. Tiêu đề nguồn bắt đầu từ mục 2 được giữ nguyên. |
| Số lượng và tên | Đúng 13 bảng, 64 cột, đúng tên và thứ tự cột trong từng bảng; đối chiếu kiểu PostgreSQL và độ dài `VARCHAR(n)` với DOCX. |
| PK/FK/UNIQUE/NULL/default | Đủ 8 PK identity đơn, 5 PK ghép, 2 UNIQUE đơn; từng dấu PK/FK/UK, NULL/NN, default và identity trong mô hình đã parse khớp nguồn. Có 55 cột bắt buộc, 9 nullable. |
| Quan hệ | Đủ 21 FK, đúng bảng cha/con và tên vai trò; 9 RESTRICT, 12 CASCADE. Cardinality 1 → 0..N; không gán nhầm 1–1 hoặc thêm FK membership chưa tồn tại. |
| Constraint/index ngoài hình | Bản Markdown giữ đủ 10 CHECK, 5 CREATE INDEX, toàn bộ PK/FK/default và quy tắc nghiệp vụ; partial unique index vẫn giữ biểu thức và điều kiện PENDING. |
| Cú pháp Mermaid | Chạy thành công `mermaid.parse`, `mermaidAPI.getDiagramFromText` và `mermaid.render` bằng bundle Mermaid có sẵn của VS Code; mô hình parse xác nhận 13 entity, 64 attribute, 21 relationship. Không chỉ kiểm tra bằng regex. |
| SVG/PNG | SVG độc lập hợp lệ XML, kích thước 2609 × 2656; PNG 5218 × 5312 pixel (render 2×), giải mã ảnh thành công. Đã xem toàn cảnh và chi tiết: nền sáng, font Arial, bảng/cột không cắt chữ, nhãn vai trò FK đọc được. SVG dùng phần tử text, không phụ thuộc HTML `foreignObject` hay tải tài nguyên mạng. |
| Bảo toàn nguồn/phạm vi | SHA-256 của DOCX, PDF, AGENTS, Tech_Stack và Project_Context không đổi so với đầu tác vụ. Chỉ tạo 5 đầu ra yêu cầu trong `docs/database/`; không cài package, sửa Java/SQL, commit hoặc push Git. |

Render dùng Node/Playwright/Chromium đã có và các module Mermaid đóng gói cùng VS Code tại `markdown-language-features/markdown-editor-out/mermaid.core-TWSVWPNT.js` (bản cài đặt VS Code thư mục `6d62b1d5da`). Chỉ phục vụ module có sẵn qua localhost trong lúc render; không tải công cụ hoặc đưa nội dung schema lên dịch vụ bên ngoài. Màu/font/khoảng cách và tắt HTML labels được khai báo trong directive đầu `ERD.mmd`. Có thể dùng bản Mermaid tương thích trong công cụ preview/export sẵn có để xuất lại; bố cục có thể khác giữa các phiên bản renderer.

Hạn chế còn lại: sơ đồ tổng quan vẫn dày khi thu nhỏ vào một trang báo cáo; nên dùng SVG/phóng to hoặc tách hình phụ theo nhóm khi dàn báo cáo. Chưa tạo SQL, chưa chạy PostgreSQL/JDBC/Maven, chưa đo hiệu năng truy vấn, chưa kiểm tra trực tiếp khả năng nhập SVG vào Word và chưa kiểm thử nghiệp vụ thực tế. Không coi kết quả parse/render là nghiệm thu P1 hay kết quả test database.

Trước khi A viết `database.sql`, hai thành viên cần duyệt: 13 bảng và từng khai báo P1 hiện tại; 21 ON DELETE; OPEN_APP/session/idempotency; clear đồng thời; cách hủy request và tranh chấp block/accept; quyền Admin hệ thống và quyền nhóm; điều kiện báo cáo spam; cách đếm bạn cấp hai; mô hình mã hóa với giảng viên và vòng đời khóa. Quy tắc hoa/thường, múi giờ/khoảng thống kê và bộ index thực tế cũng cần ghi thành quyết định chung.

**Đề xuất mới Codex cho bước SQL sau khi duyệt:** chạy script trên database rỗng; kiểm thử username/email trùng, NULL/trạng thái sai, mọi CHECK và FK, PENDING trùng hai chiều kể cả đồng thời, friendship đảo ID, xóa vật lý theo từng FK và soft delete không làm mất lịch sử. Kiểm thử riêng quyền/transaction, reconnect không tăng OPEN_APP, clear với timestamp trùng/tin commit muộn, tìm kiếm không lộ tin ẩn và đọc lại tin mã hóa sau thay đổi thành viên/khóa. Các bước này chưa được chạy trong phạm vi tài liệu hiện tại.
