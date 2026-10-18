# Database Decisions — Java Chat System

Thiết kế Database chính thức của cả nhóm A/B. Nhóm đã chấp thuận các quyết định kỹ thuật và cho phép chuẩn hóa sau khi SQL PASS trên PostgreSQL thật.

**Đã PASS trên PostgreSQL 18.6: 202 assertion SQL, 0 FAIL trong lần chạy cuối**, gồm schema/constraint/index/trigger. Xem [TEST_RESULTS](TEST_RESULTS.md), [schema từ catalog](Database_Schema.md) và [database.sql](../../script/database.sql). Không suy rộng kết quả này thành PASS cho Java Server, concurrency, TLS hoặc AES-GCM thực tế.

**PROVISIONAL:** Server-side AES-GCM là lựa chọn khả thi của nhóm, không phải E2EE, chưa được giảng viên xác nhận đáp ứng §12g. Nếu yêu cầu E2EE phải thiết kế lại phần khóa.

Thứ tự nguồn: PDF yêu cầu chính thức → quyết định A/B đã xác nhận → thiết kế chính thức này và SQL đã kiểm thử. Phần phân tích phương án được giữ để giải thích lựa chọn; nguồn B và ERD 64 cột nằm trong [archive](archive/README.md). Các mục 2–8 là đặc tả triển khai; contract Server vẫn cần review tích hợp.

## 1. Kết luận và nguồn quyết định

| Vấn đề | Phương án chính thức của nhóm | Thay đổi dữ liệu |
|---|---|---|
| Phiên OPEN_APP | UUID v4 do Client tạo cho mỗi phiên sử dụng mới; Server xác thực và ràng buộc chủ sở hữu; `UUID NOT NULL UNIQUE`; reconnect dùng lại ID. | Sửa kiểu/NULL của `activity_logs.session_id`, thêm UNIQUE đơn. |
| Xóa hội thoại | Mốc `last_cleared_message_id`; giữ `cleared_at` để tra cứu; mọi gửi/clear cùng cặp lấy cùng advisory transaction lock **trước khi cấp ID**. | Thêm một cột BIGINT không phải FK; đổi index lịch sử cặp sang cặp–ID. |
| Mã hóa nhóm | **PROVISIONAL**: AES-GCM tại Server; một DEK/nhóm; mode cố định từ lúc tạo; wrapped DEK dưới KEK bên ngoài DB; không E2EE. | Bỏ cột dự phòng E2EE ở membership; thêm CHECK/unique IV và trigger nhất quán; không thêm key_version/flag từng tin. |
| Admin hệ thống | `users.role = USER/ADMIN`; cùng tài khoản có thể dùng hai UI; Server kiểm tra quyền; bảo vệ Admin ACTIVE cuối bằng khóa transaction toàn cục. | Thêm một cột role và CHECK. Không có bảng admins riêng. |

Thiết kế chính thức giữ **13 bảng**, từ 64 lên **65 cột**: thêm `users.role`, thêm `conversation_clears.last_cleared_message_id`, bỏ `group_members.encrypted_group_key`. **21 FK và toàn bộ ON DELETE hiện có giữ nguyên.** Các constraint/index ngoài khả năng thể hiện của Mermaid được nêu đầy đủ bên dưới.

Đã đọc đủ 7 tài liệu được yêu cầu: [AGENTS](../../AGENTS.md), [đề chính thức](../Final%20Project.pdf) (5 trang), [Tech Stack](../Tech_Stack.md), [Project Context](../Project_Context.md), [schema B](archive/Database_Schema_Draft.md), [ERD gốc](archive/ERD_2026-10-16.mmd), [review ERD](ERD_Review.md). Không có nguồn yêu cầu nào bị thiếu/không đọc được. PDF đã được trích lại; không suy đoán nội dung DOCX mới ngoài bản Markdown hiện hành.

**Phần A/B đã chốt được kế thừa:** danh sách 13 bảng; PostgreSQL local; Java 21/JDBC/Socket/JSON; BCrypt; soft delete; xóa riêng từng phía; OPEN_APP một lần/phiên thật; biểu đồ theo số user mở app; Server giữ bí mật, phân quyền và transaction; hướng AES-GCM. **Các lựa chọn này đã được A/B chấp thuận làm cơ sở chính thức**, gồm role, watermark, UUID và schema khóa nhóm; xác nhận của giảng viên về mô hình mã hóa vẫn còn thiếu.

Đề tr. 2 yêu cầu thống kê hoạt động và hai phân hệ; tr. 3 §11 yêu cầu xóa lịch sử/tin lẻ, §12g yêu cầu mã hóa nhóm nhưng không chỉ định E2EE; tr. 4–5 yêu cầu script và thiết kế DB. Không có yêu cầu chính thức về UUID, số bảng, role hay key_version. Tiêu đề “P1 chuẩn hóa” của dự thảo không thay thế xác nhận A/B. Cách đếm bạn cấp hai được chọn tại mục 7.7, có nêu rõ giới hạn diễn giải đề.

**Quy ước SQL:** DDL dưới đây minh họa thay đổi trên baseline **rỗng**, không phải migration an toàn cho DB đã có dữ liệu. `:name` trong truy vấn là tham số mô tả; khi code pgJDBC phải thay bằng `?` và bind giá trị, không nối chuỗi. DDL thực thi tại database.sql đã PASS; các thuật toán Server/transaction minh họa bên dưới chưa được kiểm thử tích hợp.

## 2. Quyết định 1 về activity_logs.session_id

### 2.1 Vấn đề và so sánh

`VARCHAR(64) NULL` nhận được cả chuỗi tùy ý và NULL; chưa có UNIQUE nên hai request có thể tạo hai dòng. DISTINCT che trùng trong báo cáo nhưng không làm dữ liệu đúng. `COUNT(DISTINCT session_id)` không tính NULL và không phải số người mở app.

| Phương án | Ưu điểm | Nhược điểm | Kết luận |
|---|---|---|---|
| VARCHAR(64), cho NULL, Server tự kiểm tra trùng | Ít đổi kiểu; dễ gửi JSON | Check-then-insert tranh chấp; format không rõ; log NULL mất khỏi COUNT DISTINCT | Loại. |
| UUID NOT NULL, UNIQUE(user_id,session_id,action_type) | Chặn trùng một event trên cùng user/session; mở rộng nhiều action | Cho cùng UUID thuộc nhiều user; thừa phạm vi khi action hiện chỉ OPEN_APP | Chưa cần trong baseline này. |
| UUID NOT NULL, UNIQUE(session_id) | Một UUID gắn một phiên/một owner; DB xử lý đồng thời; UUID Java/PostgreSQL có sẵn | Nếu sau này log nhiều loại event/phiên phải thiết kế lại uniqueness | **Chọn** vì bảng hiện chỉ có OPEN_APP. |
| Server cấp UUID mới sau mỗi LOGIN, Client giữ để resume | Server kiểm soát nguồn cấp | Nếu commit log rồi mất LOGIN_SUCCESS, retry chưa biết UUID có thể tạo phiên mới; cần thêm idempotency token/mapping | Không chọn cho đồ án hiện tại. |

Chọn UUID v4 qua `UUID.randomUUID()` có sẵn; PostgreSQL lưu kiểu `UUID`, JSON truyền chuỗi chuẩn. ID này là **idempotency key, không phải thông tin xác thực**. Xem [UUID Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/UUID.html) và [kiểu UUID PostgreSQL](https://www.postgresql.org/docs/current/datatype-uuid.html).

### 2.2 Định nghĩa phiên và phối hợp Client–Server

Phiên được chọn là **một lần sử dụng Client đã xác thực**, bắt đầu khi login/đi vào ứng dụng thành công, kết thúc khi logout hoặc đóng Client. Mở Client nhưng chưa đăng nhập không ghi OPEN_APP. Hai tiến trình/thiết bị là hai phiên. Logout rồi đăng nhập lại là phiên mới, kể cả cùng tài khoản/cùng cửa sổ. Admin và User Client đều là phiên đã xác thực theo định nghĩa này; không tự loại tài khoản ADMIN khỏi thống kê.

1. Client tạo UUID **trước request login đầu tiên** của một phiên dự kiến, giữ trong bộ nhớ cho cả retry mất ACK. Các lần nhập sai mật khẩu chưa tạo log. Không tạo UUID mới cho từng Socket/request.
2. Server kiểm tra BCrypt, trạng thái ACTIVE, cú pháp UUID; lấy user_id từ danh tính đã xác thực, không tin user_id do Client gửi. Chỉ khi được phép vào ứng dụng mới INSERT OPEN_APP.
3. Một request retry dùng lại UUID, không ghi thêm. Nếu UUID đã thuộc user khác, từ chối; tuyệt đối không UPSERT đổi owner. UUID v4 trùng ngẫu nhiên với owner khác cũng trả yêu cầu tạo ID mới, không chiếm phiên.
4. Reconnect dùng RESUME_SESSION với cùng UUID và bằng chứng xác thực còn hợp lệ. Khi Server đã khởi động lại hoặc không còn thông tin xác thực trong bộ nhớ, yêu cầu xác thực lại rồi kiểm tra owner của dòng log; giữ UUID và thời gian OPEN_APP cũ. Xác thực lại do mất kết nối không được tự coi là một lần người dùng chủ động login.
5. Logout kết thúc session trong registry Server, đóng các Socket gắn session; Client xóa UUID và credential/token phiên. Login chủ động tiếp theo dùng UUID mới. Registry cần cập nhật nguyên tử theo UUID để hai Socket retry không tạo hai đối tượng phiên logic.
6. Server có thể cấp token ngẫu nhiên riêng để resume trong thời gian chạy; không dùng UUID log làm bearer token. Không gửi password hash để thay mật khẩu xác thực và không lưu mật khẩu gốc xuống đĩa Client.

**Giới hạn có chủ ý:** bảng log không lưu vòng đời phiên đã đóng, nên không chứng minh được “một lần mở tiến trình vật lý” trước Client đã bị sửa để tái sử dụng UUID cũ. Server ngăn reuse phiên đã logout khi registry còn sống; sau restart, resume phải tái xác thực và owner vẫn được kiểm tra. Thiết kế bảo đảm một dòng trên mỗi **phiên theo hợp đồng Client/Server**, không coi số liệu OPEN_APP là hệ thống chống gian lận. Muốn lưu logout/revocation bền vững và phân biệt mọi Client gian lận cần đặc tả session riêng; không thêm bảng chỉ vì nhu cầu chưa có trong đề.

### 2.3 DDL và ghi log đồng thời

```sql
ALTER TABLE activity_logs
    ALTER COLUMN session_id TYPE UUID USING session_id::uuid,
    ALTER COLUMN session_id SET NOT NULL,
    ADD CONSTRAINT uq_activity_session UNIQUE (session_id);
```

Không đặt DEFAULT UUID tại DB: retry cần gửi lại **cùng** ID. Server parse UUID v4 theo dạng chuẩn và pgJDBC bind đối tượng UUID bằng setObject; JSON vẫn dùng chuỗi, không đổi kiểu DB về VARCHAR vì giao thức JSON. Cách ghi bằng transaction READ COMMITTED:

```sql
BEGIN ISOLATION LEVEL READ COMMITTED;
INSERT INTO activity_logs (user_id, action_type, session_id)
VALUES (:authenticated_user_id, 'OPEN_APP', :session_id)
ON CONFLICT ON CONSTRAINT uq_activity_session DO NOTHING;

-- Câu lệnh riêng sau INSERT, có snapshot mới sau khi chờ conflict.
SELECT user_id, action_type, created_at
FROM activity_logs WHERE session_id = :session_id;
-- Server kiểm tra đúng owner/OPEN_APP; sai thì ROLLBACK và từ chối.
COMMIT;
```

UNIQUE phân xử hai INSERT đồng thời: một dòng commit, bên kia chờ rồi bỏ qua; nếu bên đầu rollback, bên sau có thể INSERT. Không dùng SELECT kiểm tra tồn tại rồi INSERT không có UNIQUE. Không dùng DO UPDATE chạm `created_at`, vì reconnect không được đổi tháng mở phiên. Thời điểm `created_at` do DB ghi lúc bắt đầu phiên, không lấy đồng hồ Client. Cơ chế conflict tham khảo [INSERT PostgreSQL](https://www.postgresql.org/docs/current/sql-insert.html#SQL-ON-CONFLICT).

Ghi `login_history` cho login chủ động mới cùng transaction khi INSERT OPEN_APP thực sự tạo dòng; retry/resume không ghi login lặp. Request resume chỉ khôi phục session sau khi xác thực thành công. Nếu đăng nhập thất bại, cả hai bảng không thêm dòng.

### 2.4 Thống kê

Lượt mở app theo user = số dòng OPEN_APP vì mỗi session đã UNIQUE; số người hoạt động = DISTINCT user_id. Khoảng thời gian dùng `[from,to)` để hai khoảng kề nhau không đếm trùng biên; dùng `TIMESTAMPTZ`, quy ước năm/tháng báo cáo `Asia/Ho_Chi_Minh`.

```sql
SELECT user_id, COUNT(*) AS open_app_count
FROM activity_logs
WHERE action_type = 'OPEN_APP'
  AND created_at >= :from_instant AND created_at < :to_instant
GROUP BY user_id;

-- :year_start là DATE ngày 01/01 của năm chọn.
WITH months AS (
  SELECT g AS month_start
  FROM generate_series(
    CAST(:year_start AS timestamp),
    CAST(:year_start AS timestamp) + interval '11 months',
    interval '1 month'
  ) AS s(g)
)
SELECT EXTRACT(MONTH FROM m.month_start)::int AS month,
       COUNT(DISTINCT a.user_id) AS active_users
FROM months m
LEFT JOIN activity_logs a
  ON a.action_type = 'OPEN_APP'
 AND a.created_at >= (m.month_start AT TIME ZONE 'Asia/Ho_Chi_Minh')
 AND a.created_at < ((m.month_start + interval '1 month')
                    AT TIME ZONE 'Asia/Ho_Chi_Minh')
GROUP BY m.month_start ORDER BY m.month_start;
```

Phiên bắt đầu tháng 1, chỉ reconnect tháng 2 thì không tạo OPEN_APP tháng 2: biểu đồ đo **người có mở ứng dụng trong tháng**, không đo mọi người đang online hoặc có gửi tin. Nếu muốn “hoạt động bất kỳ” là một chỉ số khác, cần định nghĩa khác. Đếm đối tác/nhóm chat vẫn theo DISTINCT receiver/group của tin user chủ động gửi như A/B đã chốt.

### 2.5 Kiểm thử và đáp ứng đề

| Test dự kiến | Kết quả cần đạt |
|---|---|
| Hai login retry đồng thời cùng user/UUID; mất ACK sau commit | Một OPEN_APP và một login_history; trả lại cùng session. |
| Reconnect 20 lần, nhiều Socket cùng phiên | Không thêm log, không đổi created_at. |
| Logout/login lại; hai thiết bị | Mỗi phiên mới một UUID/dòng mới. |
| UUID NULL/sai format; UUID thuộc người khác | DB hoặc Server từ chối; không sửa owner cũ. |
| User LOCKED/DELETED; resume chỉ có UUID | Không được xác thực/không tạo session hoạt động. |
| Hai phiên cùng người trong tháng; tháng không có log; biên giờ Việt Nam | Lượt mở = 2; số người = 1; tháng rỗng = 0; phân tháng đúng. |
| Server restart rồi Client resume có tái xác thực | UUID cũ không tạo dòng mới; không dùng UUID để vượt xác thực. |

Đáp ứng Admin §8–9 và quyết định OPEN_APP của A/B với định nghĩa phiên trên. Không bắt buộc hỏi thầy về kiểu UUID; Thiết kế A/B đã chọn semantics phiên và phạm vi tính cả hai Client. Nếu thầy muốn tính mọi ngày/tháng Client còn mở, phải đổi định nghĩa metric, không lặng lẽ ghi OPEN_APP khi reconnect.

## 3. Quyết định 2 về conversation_clears

### 3.1 Vấn đề và so sánh

| Phương án | Ưu điểm | Nhược điểm | Kết luận |
|---|---|---|---|
| Chỉ `created_at > cleared_at` | Ít cột, dễ viết | Timestamp bằng nhau; NOW theo đầu transaction; insert trễ/commit muộn bị ẩn sai | Loại. |
| Chỉ MAX(message_id), không khóa | So sánh nhanh, không phụ thuộc giờ | ID 100 có thể commit sau 101 và sau clear; mốc 101 sẽ ẩn nhầm 100 | Loại. |
| So sánh bộ `(timestamp,id)` | Thứ tự ổn định khi timestamp bằng nhau | Không giải quyết transaction commit ngược hoặc request đang xử lý | Không chọn làm nguồn quyết định. |
| Mốc ID + tuần tự hóa gửi/clear theo cặp; timestamp chỉ tra cứu | Ít thay đổi; có điểm phân định rõ; không thêm bảng | Mọi đường ghi phải tuân thủ cùng khóa; serialize mỗi hội thoại | **Chọn**. |
| Đánh dấu hàng loạt tất cả tin vào message_hidden | Không cần watermark; giữ chính xác tập dòng đã thấy | O(N) dòng mỗi clear, phình bảng; vẫn phải chốt tin gửi đồng thời | Không chọn cho xóa toàn bộ. |

**Semantics được chọn:** clear xóa với A mọi tin của cặp đã commit **trước lượt clear trong thứ tự khóa Server/DB**. Tin được ghi sau lượt clear vẫn hiển thị, dù Client đã bấm gửi trước đó nhưng request chưa được xử lý. Không lấy thời gian máy Client, thời gian bắt đầu transaction hoặc thứ tự nhận trên các Socket làm thứ tự toàn cục.

### 3.2 Schema và index

```sql
ALTER TABLE conversation_clears
    ADD COLUMN last_cleared_message_id BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_clear_watermark_nonnegative
      CHECK (last_cleared_message_id >= 0);

-- Khóa chặt giả định cấp ID tăng giữa các JDBC Connection.
ALTER TABLE direct_messages ALTER COLUMN id SET CACHE 1;

-- Trong bản đề xuất, thay index cặp–thời gian cũ bằng cặp–ID.
DROP INDEX idx_dm_pair_time;
CREATE INDEX idx_dm_pair_id ON direct_messages
  (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), id);
```

`0` nghĩa là chưa có tin; identity dùng START 1, INCREMENT 1, NO CYCLE và **CACHE 1**. Đây vốn là các mặc định phù hợp của baseline nhưng phải giữ thành invariant vận hành: CACHE lớn hơn 1 có thể làm các Connection phát ID không tăng theo thứ tự gọi nextval. Không cấp ID thủ công, prefetch ID, setval/restart sequence hoặc đổi CACHE khi ứng dụng chạy. Xem [cảnh báo cache sequence PostgreSQL](https://www.postgresql.org/docs/current/sql-createsequence.html#SQL-CREATESEQUENCE-NOTES). Mốc là ranh giới thứ tự, **không phải FK tới direct_messages**: có thể bằng 0 và cần còn hiệu lực nếu sau này dọn vật lý tin gốc. Hai FK user/partner, PK `(user_id,partner_id)` và ON DELETE giữ nguyên. `cleared_at` vẫn NOT NULL, do DB ghi bằng `clock_timestamp()` khi clear, chỉ dùng hiển thị/tra cứu.

### 3.3 Thuật toán khóa và gửi tin

Mọi SEND_DIRECT_MESSAGE và CLEAR_CONVERSATION chuẩn hóa cặp `lo=min(A,B), hi=max(A,B)`. Server tính một số BIGINT không âm từ 8 byte đầu SHA-256 (big endian) của UTF-8 `dm:<lo>:<hi>`, bỏ bit dấu. Dùng `MessageDigest` JDK, không cần package. Cùng cặp đảo chiều cho cùng key; va chạm hash chỉ làm hai cặp không liên quan phải chờ nhau, không làm mất khóa bảo vệ. Không ép BIGINT user ID xuống INT.

Trên **một JDBC Connection**, `autoCommit=false`, isolation READ COMMITTED:

1. Bắt đầu transaction, lấy `pg_advisory_xact_lock(:dm_lock_key)` trong **một câu lệnh riêng**. Không dùng session-level advisory lock.
2. Sau khi đã có khóa, kiểm tra lại quyền/trạng thái/block cần thiết. INSERT tin và để identity cấp ID **tại đây**. Không gọi nextval/reserve ID hoặc INSERT trước khi lấy khóa.
3. COMMIT trước khi ACK/push tin; ROLLBACK khi lỗi. Khóa được giữ tới commit/rollback, không nhả ngay sau INSERT. Không chờ thao tác UI/mạng trong transaction.

```sql
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT pg_advisory_xact_lock(:dm_lock_key);
-- Server kiểm tra quyền trong ngữ cảnh, rồi:
INSERT INTO direct_messages (sender_id, receiver_id, content)
VALUES (:authenticated_user_id, :partner_id, :content)
RETURNING id, created_at;
COMMIT;
```

PostgreSQL có [advisory lock theo transaction](https://www.postgresql.org/docs/current/explicit-locking.html#ADVISORY-LOCKS), nhưng ứng dụng phải chủ động dùng cùng quy ước. Snapshot READ COMMITTED là theo từng câu lệnh; sequence không phản ánh thứ tự commit. Đó là lý do phải khóa trước cấp ID và đọc lại sau khi đã chờ, theo [quy tắc isolation PostgreSQL](https://www.postgresql.org/docs/current/transaction-iso.html).

Không gộp “lấy khóa + MAX” trong cùng một SELECT/CTE: snapshot có thể được lấy trước lúc chờ khóa. Không dùng REPEATABLE READ cho luồng clear với snapshot đã cũ. Java `synchronized` riêng một tiến trình không thay được khóa DB khi có nhiều worker/Server instance.

### 3.4 Thuật toán clear

```sql
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT pg_advisory_xact_lock(:dm_lock_key);

-- Câu lệnh riêng, sau khi khóa đã được cấp.
SELECT COALESCE(MAX(id), 0) AS cutoff
FROM direct_messages
WHERE LEAST(sender_id, receiver_id) = :lo
  AND GREATEST(sender_id, receiver_id) = :hi;

-- :cutoff là giá trị Server vừa đọc, không nhận từ Client.
INSERT INTO conversation_clears
  (user_id, partner_id, last_cleared_message_id, cleared_at)
VALUES (:authenticated_user_id, :partner_id, :cutoff, clock_timestamp())
ON CONFLICT (user_id, partner_id) DO UPDATE SET
  last_cleared_message_id = GREATEST(
    conversation_clears.last_cleared_message_id,
    EXCLUDED.last_cleared_message_id),
  cleared_at = EXCLUDED.cleared_at;
COMMIT;
```

Không sửa dòng `(B,A)`. Clear lặp không làm mốc lùi. Không cần xóa các marker `message_hidden` cũ để đúng logic; để nguyên là đơn giản nhất. Không xóa vật lý direct_messages khi người dùng bấm xóa lịch sử.

Chứng minh giới hạn: transaction gửi S lấy khóa trước clear C thì C chỉ lấy được khóa sau S commit/rollback; SELECT MAX mới thấy S đã commit. Nếu C lấy khóa trước S, S chưa được cấp ID, nên ID S được cấp sau và lớn hơn cutoff. Các hội thoại khác vẫn có thể commit ngược thứ tự ID; điều đó không ảnh hưởng vì cutoff chỉ lấy đúng cặp. Đây là bảo đảm của **khóa + quy tắc cấp ID**, không phải thuộc tính mặc định của identity.

### 3.5 Đọc lịch sử và nhiều Client

```sql
SELECT m.id, m.sender_id, m.receiver_id, m.content, m.created_at
FROM direct_messages m
LEFT JOIN conversation_clears c
  ON c.user_id = :authenticated_user_id AND c.partner_id = :partner_id
WHERE LEAST(m.sender_id, m.receiver_id) = :lo
  AND GREATEST(m.sender_id, m.receiver_id) = :hi
  AND m.id > COALESCE(c.last_cleared_message_id, 0)
  AND NOT EXISTS (
    SELECT 1 FROM message_hidden h
    WHERE h.message_id = m.id AND h.user_id = :authenticated_user_id
  )
ORDER BY m.id;
```

Server tự tính lo/hi từ user đã xác thực và đối tác, nên truy vấn không thể đọc cặp khác. Phân trang dùng ID; tìm kiếm một/mọi hội thoại, nhảy tới tin và tải tin offline đều áp dụng hai bộ lọc trên. `message_hidden` chỉ che tin lẻ của đúng user, dù tin nằm trên watermark. Server vẫn kiểm tra user thuộc tin khi thêm marker ẩn.

Sau clear commit, gửi sự kiện `CONVERSATION_CLEARED(partner_id,cutoff)` tới **mọi Socket của A**. Client giữ max cutoff và loại tin/push/response lịch sử cũ có ID ≤ cutoff; response/push có thể tới lệch thứ tự nên không dựa vào thứ tự TCP giữa nhiều Socket. Khi mở lại/reconnect, tải watermark từ DB trước khi hòa lịch sử. B không nhận hành vi xóa phía A. Không cam kết xóa dữ liệu khỏi thiết bị đã lưu bên ngoài ứng dụng.

Advisory lock chỉ có hiệu lực khi mọi writer dùng nó; tài khoản DB ứng dụng nằm tại Server, Client không có quyền INSERT trực tiếp. Công cụ import/maintenance tương lai phải tuân thủ khóa hoặc dừng ghi. Không thêm trigger BEFORE INSERT rồi cho rằng đã đủ: default identity có thể được tính trước khi trigger lấy khóa. Lỗi kết nối sau commit của SEND có thể gây gửi lại tin trùng nếu chưa có message idempotency riêng; đó là vấn đề giao tin khác, không được watermark tự giải quyết.

### 3.6 Kiểm thử và đáp ứng đề

| Test dự kiến | Kết quả cần đạt |
|---|---|
| Nhiều tin cùng timestamp; clock Client sai | Clear đúng theo ID/khóa, không theo timestamp. |
| S giữ khóa/INSERT rồi ngủ trước commit; C bắt đầu trong lúc đó | C chờ; sau S commit, cutoff bao gồm S; nếu S rollback thì không bao gồm. |
| C giữ khóa trước; S đã nhận request nhưng chưa INSERT | S chờ rồi có ID lớn hơn cutoff và hiển thị. |
| Hai hội thoại commit ngược thứ tự ID | Không ảnh hưởng cutoff của từng cặp. |
| Test lỗi cố ý cấp ID trước khóa hoặc tăng sequence CACHE >1 | Chứng minh invariant bị phá; writer/cấu hình đó phải bị loại/sửa trước phát hành. |
| Hai clear đồng thời, hai Client của A | Một dòng PK, cutoff không giảm; mọi Client A đồng bộ, B vẫn đọc được. |
| Chưa từng chat, chưa có clear, tin đã hidden, tìm kiếm sau clear | Cutoff 0 hợp lệ; lịch sử ban đầu đầy đủ; tin hidden/cleared không xuất hiện lại. |
| Lỗi transaction, Server crash | Rollback/commit nhất quán; khóa DB tự giải phóng; Client resync DB. |

Đáp ứng User §11d–g và xóa riêng từng phía A/B đã chốt. Không bắt buộc hỏi thầy về loại khóa; Thiết kế A/B đã chọn điểm phân định “trước/sau xóa” theo thứ tự xử lý Server. Nếu giảng viên yêu cầu “chỉ xóa tập các tin nhắn đã hiển thị tại Client khi bấm”, đó là semantics khác, phải đặc tả riêng.

## 4. Quyết định 3 về AES-GCM nhóm

### 4.1 Phạm vi đề và phương án

PDF tr. 3 §12g ghi yêu cầu mã hóa nhóm và dẫn tham khảo nhóm mã hóa Facebook. Không ghi chính thức thuật ngữ E2EE, mô hình khóa hoặc khả năng bật/tắt/rotate. Tham khảo Facebook cũng không đủ để kết luận mã hóa tại Server chắc chắn đạt ý giảng viên. Do đó chọn phương án triển khai mặc định dưới đây với nhãn **PROVISIONAL**; cần hỏi thầy trước nghiệm thu yêu cầu bảo mật.

| Phương án | Lợi ích | Chi phí/hạn chế | Kết luận |
|---|---|---|---|
| Server mã hóa trước PostgreSQL, giải mã sau kiểm tra quyền | Dễ làm với JCA/JDBC; DB dump không đủ đọc nội dung nếu thiếu KEK; reset mật khẩu không mất khóa nhóm | Server có thể đọc tin; Server/KEK bị chiếm thì không bảo vệ; không tự bảo vệ đường TCP | **Chọn PROVISIONAL** cho nhóm hai người. |
| E2EE, Client giữ/phân phối khóa | Có thể bảo vệ nội dung khỏi Server khi giao thức làm đúng | Cần khóa định danh thiết bị, xác thực khóa, phân phối/rotate, multi-device, phục hồi khi reset, lịch sử theo membership; 13 bảng hiện chưa đặc tả đủ | Chỉ chuyển nếu giảng viên yêu cầu; phải thiết kế lại luồng khóa, không đổi nhãn từ Server encryption thành E2EE. |
| Tự động rotate DEK + lưu lịch sử nhiều phiên bản khóa | Quản lý khóa cũ/thu hồi theo thời kỳ rõ hơn | Cần nơi lưu nhiều khóa/metadata và mapping tin–khóa; phức tạp hơn nhu cầu hiện tại | Không chọn mặc định. |

Tính toàn vẹn GCM dựa vào tag và AAD, không chỉ che chuỗi. Xem [NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final). Bảo vệ lưu trữ không thay bảo vệ đường truyền: khi chạy qua mạng ở mốc 80%, thiết kế chọn JSSE TLS trong JDK cho Socket và xác thực chứng chỉ theo mục 7.6; phần tích hợp chưa được triển khai hoặc kiểm thử, không tự coi TCP hiện tại đã mã hóa.

### 4.2 Khóa, payload và truy cập

1. Mỗi nhóm chọn mode lúc tạo: thường hoặc mã hóa. Sau tạo, mode **bất biến**; đổi tên/thành viên vẫn được. UI không có nút bật/tắt mode nhóm cũ; muốn mode khác tạo nhóm mới. Đây là giới hạn phạm vi được chọn, không phải yêu cầu đã có của thầy.
2. Nhóm mã hóa có một DEK AES-256 ngẫu nhiên qua KeyGenerator/SecureRandom JDK. Server không gửi DEK/KEK cho Client và không gắn DEK với password người dùng.
3. KEK AES-256 được cung cấp lúc vận hành từ file riêng có quyền OS hạn chế hoặc biến môi trường Server, bên ngoài repo/JAR/client/DB. Không có secret mặc định trong SQL. Sao lưu KEK tách khỏi DB; mất toàn bộ KEK thì không thể giải mã lịch sử.
4. Bọc DEK bằng chuẩn AES Key Wrap JCA `AESWrap` (tương đương `AES/KW/NoPadding`), lưu `kw1:<kek_id>:<base64_wrapped_DEK>` vào `wrapped_encryption_key`. `kek_id` không phải secret. KW dùng để bọc khóa, còn **tin nhắn vẫn AES-GCM**; không tự viết thuật toán wrap. Chuẩn có sẵn trong [Java 21 Standard Names](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#cipher-algorithms), không thêm dependency/framework.
5. Với từng tin mã hóa, dùng `AES/GCM/NoPadding`, IV 12 byte mới từ SecureRandom, tag 128 bit. JCA trả ciphertext kèm tag từ doFinal; lưu Base64 cả hai vào content, Base64 IV vào iv. Không tách/bỏ tag; tag sai phải từ chối, không trả plaintext một phần. Xem [Cipher Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/Cipher.html).
6. AAD UTF-8 cố định `chatapp-gm-v1:<group_id>:<sender_id>`; Server dựng từ ID thật, Client không cung cấp AAD tùy ý. Đổi group/sender của ciphertext phải làm kiểm tra tag thất bại. Không đưa created_at vào AAD để tránh lệch định dạng timestamp.
7. Nhóm thường: content là văn bản UTF-8, iv NULL, wrapped key NULL. Nhóm mã hóa: content là Base64(ciphertext||tag), iv canonical Base64 16 ký tự cho 12 byte, wrapped key không NULL. Mode bất biến nên không lẫn tin plaintext cũ/mới trong một nhóm.
8. Mọi đọc/gửi tin kiểm tra user ACTIVE và membership ở Server. Admin hệ thống không tự có quyền đọc mọi tin nhóm. Thành viên mới được đọc toàn bộ lịch sử nhóm; người bị xóa không đọc/tải/push tiếp qua Server. Không thể thu hồi plaintext đã đọc/lưu trước đó. Đây là policy nhóm đã chọn, không hứa quyền mật mã theo từng giai đoạn như E2EE.

### 4.3 IV duy nhất và schema đề xuất

Giữ một DEK riêng cho từng nhóm, không tái sử dụng DEK cho nhóm khác. UNIQUE `(group_id,iv)` là rào chắn chống trùng nonce giữa **các tin được lưu** dưới DEK đó, kể cả nhiều worker. Sinh IV ngẫu nhiên 96 bit cho mỗi lần encrypt; nếu unique conflict, rollback và encrypt lại với IV mới. Không dùng timestamp làm nonce; không phát ciphertext trước khi INSERT commit. Không hard-delete rồi tái sử dụng IV hoặc khôi phục DB cũ và tiếp tục cùng khóa một cách tùy tiện. UNIQUE không phải sổ ghi mọi IV từng dùng trong lịch sử vận hành; phạm vi này dựa thêm vào SecureRandom và chính sách không reuse/không xóa vật lý lịch sử nhóm đang chạy. Giữ giới hạn vận hành dưới 2^32 lần mã hóa cho mỗi DEK, gồm retry; đồ án nhỏ thấp hơn rất nhiều. Nếu RNG lỗi/collision lặp thì dừng gửi và kiểm tra, không retry vô hạn.

```sql
ALTER TABLE chat_groups ADD CONSTRAINT chk_group_key_mode CHECK (
  (is_encrypted AND wrapped_encryption_key IS NOT NULL)
  OR (NOT is_encrypted AND wrapped_encryption_key IS NULL)
);

-- Đã bỏ theo thiết kế chính thức; phạm vi Server-side Encryption vẫn PROVISIONAL.
ALTER TABLE group_members DROP COLUMN encrypted_group_key;

ALTER TABLE group_messages ADD CONSTRAINT chk_group_message_iv CHECK (
  iv IS NULL OR iv ~ '^[A-Za-z0-9+/]{16}$'
);
CREATE UNIQUE INDEX uq_group_message_iv
  ON group_messages (group_id, iv) WHERE iv IS NOT NULL;
```

Không thêm `group_messages.is_encrypted`: sẽ lặp mode nhóm bất biến. Không thêm `key_version`: chỉ có một DEK dùng để đọc toàn bộ lịch sử hiện tại; KEK version nằm trong envelope. Không giữ `encrypted_group_key` NULL làm “E2EE dự phòng” vì dễ khiến người đọc hiểu sai trách nhiệm khóa. Nếu thầy yêu cầu E2EE thì phải review lại thay đổi này.

CHECK không được truy vấn bảng cha để bảo đảm mode–IV. DDL guard minh họa dưới đây giữ mode bất biến và khóa dòng nhóm khi kiểm tra tin; actor/membership vẫn do Server xác thực. Function/trigger không phải bảng thứ 14.

```sql
CREATE FUNCTION guard_group_encryption_mode() RETURNS trigger
LANGUAGE plpgsql SET search_path = pg_catalog, public AS $$
BEGIN
    IF NEW.is_encrypted IS DISTINCT FROM OLD.is_encrypted THEN
        RAISE EXCEPTION USING ERRCODE='23514', MESSAGE='Group encryption mode is immutable';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_group_mode_immutable BEFORE UPDATE OF is_encrypted ON chat_groups
FOR EACH ROW EXECUTE FUNCTION guard_group_encryption_mode();

CREATE FUNCTION guard_group_message_mode() RETURNS trigger
LANGUAGE plpgsql SET search_path = pg_catalog, public AS $$
DECLARE encrypted BOOLEAN;
BEGIN
    -- Let the declarative NOT NULL constraint report a missing group_id.
    IF NEW.group_id IS NULL THEN RETURN NEW; END IF;
    IF TG_OP='UPDATE' THEN
        IF NEW.group_id IS DISTINCT FROM OLD.group_id OR NEW.sender_id IS DISTINCT FROM OLD.sender_id THEN
            RAISE EXCEPTION USING ERRCODE='23514', MESSAGE='Message group and sender are immutable';
        END IF;
    END IF;
    SELECT is_encrypted INTO encrypted FROM public.chat_groups WHERE id=NEW.group_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION USING ERRCODE='23503', MESSAGE='Message group does not exist';
    END IF;
    IF encrypted <> (NEW.iv IS NOT NULL) THEN
        RAISE EXCEPTION USING ERRCODE='23514', MESSAGE='Message IV does not match group mode';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_group_message_mode BEFORE INSERT OR UPDATE ON group_messages
FOR EACH ROW EXECUTE FUNCTION guard_group_message_mode();
```

Guard không chứng minh Base64 content hợp lệ hoặc tag đúng. Server kiểm tra canonical Base64, độ dài ciphertext ≥16 byte và thực hiện AEAD khi đọc. Wrapped envelope cũng được Server parse/unwrap; SQL chỉ kiểm tra NULL tương ứng mode. Trigger không kiểm tra quyền theo session vì DB connection là của Server, không phải Client.

### 4.4 Thành viên, đổi khóa và đọc lịch sử

Mọi tạo nhóm, gửi tin, thêm/xóa thành viên, đổi quyền nhóm dùng transaction và lấy `SELECT ... FROM chat_groups WHERE id=:group_id FOR UPDATE` **trước** khi kiểm tra membership và ghi. Gửi tin khóa nhóm trước INSERT; trigger là lớp kiểm tra bổ sung. Xóa thành viên trước gửi thì gửi bị từ chối; gửi trước xóa thì tin đã được chấp nhận. Sau commit, registry/push bỏ Socket người bị xóa; hàng đợi phải kiểm tra quyền hiện hành trước đẩy nội dung. Không tự chuyển `created_by` thành bằng chứng duy nhất được gửi tin.

Không rotate DEK khi thêm/xóa thành viên trong mô hình này: Client chưa từng nhận DEK; Server kiểm soát truy cập và policy cho phép thành viên hiện tại xem toàn bộ lịch sử. Thay đổi mật khẩu/reset email không đổi DEK.

| Thao tác khóa | Xử lý được chọn | Đọc lịch sử sau đó |
|---|---|---|
| Đổi KEK định kỳ | Giữ DEK, unwrap bằng KEK cũ rồi wrap bằng KEK mới; thay envelope khi giữ khóa dòng nhóm. Giữ keyring cũ tới khi mọi nhóm/backup cần thiết đã chuyển. | Nội dung/IV không đổi; unwrap envelope hiện tại rồi giải mã bằng cùng DEK. |
| Thay DEK do sự cố thật sự | Không có hot rotation theo phiên bản trong sản phẩm mặc định. Bảo trì có kiểm soát: khóa nhóm, đọc/giải mã toàn bộ tin bằng DEK cũ, encrypt lại toàn bộ với DEK mới/IV mới, đổi envelope và COMMIT nguyên tử. Không giải mã được một tin thì rollback tất cả. | Mọi tin còn lại trong DB chỉ dùng DEK mới; không cần key_version. Lịch sử/backup cũ vẫn cần DEK cũ và KEK tương ứng theo policy backup. |
| Muốn giữ ciphertext cũ với nhiều DEK hoặc mode thay đổi theo tin | Ngoài phạm vi phương án chọn; cần thiết kế kho khóa phiên bản + mapping từng tin, có thể cần thêm bảng. | Không chỉ thêm key_version mà thiếu nơi giữ wrapped khóa cũ. Phải A/B/giảng viên duyệt trước. |

Không chỉ thay wrapped DEK bằng khóa ngẫu nhiên mới rồi để nguyên ciphertext cũ: sẽ làm mất khả năng giải mã. Full re-encryption là đường bảo trì tốn O(N), không cần làm UI/chức năng rotate ở mốc 20%; phải kiểm thử trước khi dùng ở mốc 80% nếu thật sự triển khai. Backup rollback không chữa được việc plaintext đã lộ.

Đọc lịch sử dùng **một SELECT JOIN** trả membership + mode + wrapped key + các tin trong cùng snapshot, hoặc transaction REPEATABLE READ chỉ đọc. Không tách “đọc khóa” và “đọc tin” ở hai snapshot READ COMMITTED khi bảo trì DEK có thể commit giữa chúng. Mặc định không cache DEK dài hạn để tránh cache cũ sau đổi khóa; nếu bổ sung cache, phải gắn với envelope và invalidation sau commit.

### 4.5 Kiểm thử và xác nhận với giảng viên

| Test dự kiến | Kết quả cần đạt |
|---|---|
| Nhóm thường/mã hóa, đóng Server rồi mở lại với đúng KEK | Đọc lại đúng nội dung; không phụ thuộc password/memory Client. |
| Sửa ciphertext/tag/IV/group_id/sender_id, dùng sai KEK | Từ chối/AEAD lỗi, không trả plaintext một phần; guard chặn đổi group/sender. |
| Hai worker cố dùng cùng IV trong một nhóm | Một INSERT bị UNIQUE chặn; retry encrypt lại với IV mới, chưa push bản lỗi. |
| Payload plaintext với nhóm mã hóa, IV sai; mode update sau tạo | CHECK/trigger/Server từ chối theo lớp kiểm tra. |
| Xóa thành viên và gửi đồng thời; người mới đọc lịch sử | Quyền theo thứ tự khóa; người bị xóa không truy cập tiếp; người mới thấy lịch sử theo policy. |
| Rewrap KEK, giữ DEK; re-encrypt DEK rollback giữa chừng | Lịch sử đọc đúng; re-encrypt lỗi không để nửa cũ/nửa mới. |
| Reader đang chạy trong lúc đổi DEK | Khóa và tin cùng snapshot; không lấy key cũ để giải tin mới. |
| DB dump không kèm KEK; mất KEK; reset mật khẩu | Dump thiếu KEK không đủ giải tin; mất KEK báo lỗi rõ; reset không làm mất lịch sử. |

**Bắt buộc hỏi thầy để chốt bảo mật:** yêu cầu §12g có bắt buộc Server không đọc được nội dung (E2EE) không? Nếu có, phương án này chưa đáp ứng. Có thể trình bày prototype mã hóa lưu trữ trong lúc chờ, nhưng báo cáo phải ghi đúng tên và hạn chế. Thiết kế A/B đã chọn mode bất biến, policy lịch sử khi thêm/xóa người, bỏ cột E2EE và không tự động rotate DEK. Không bắt buộc hỏi thầy về kiểu Base64 hay lựa chọn AESWrap cụ thể nếu mô hình đã được chấp thuận.

## 5. Quyết định 4 về Admin hệ thống

### 5.1 So sánh và lựa chọn

| Phương án | Ưu điểm | Nhược điểm | Kết luận |
|---|---|---|---|
| Thêm `users.role` USER/ADMIN | Dùng chung BCrypt/status/login_history; không thêm bảng; quyền tra cứu rõ | Chưa hỗ trợ nhiều role tùy biến; phải chống mass assignment và bảo vệ Admin cuối | **Chọn**. |
| Bảng admins riêng | Phân tách profile quản trị nếu nghiệp vụ khác hẳn | Thêm bảng thứ 14, trùng thông tin/mật khẩu hoặc thêm JOIN; chưa có yêu cầu profile riêng | Không cần. |
| Admin chỉ trong file cấu hình Server | Bootstrap nhanh, không đổi schema | Khóa/đổi mật khẩu/lịch sử quản trị tách khỏi users; dễ hardcode tài khoản dùng chung; khó quản lý nhiều Admin | Chỉ dùng quyền vận hành offline để bootstrap/recovery, không làm tài khoản đăng nhập sản phẩm. |
| Role/permission nhiều bảng, RBAC tổng quát | Linh hoạt nhiều cấp quyền | Vượt nhu cầu hai loại tài khoản | Không chọn. |

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'USER'
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));
```

Một tài khoản ADMIN vẫn dùng được User UI như user bình thường. `USER` chỉ dùng User UI; `ADMIN` được mở Admin UI. Quyền `group_members.is_admin` độc lập: system Admin không tự là Admin/thành viên mọi nhóm và group Admin không có quyền quản trị tài khoản toàn hệ thống.

### 5.2 Đăng nhập, protocol và chống nâng quyền

1. Hai JAR kết nối cùng Server và cùng cơ chế xác thực. Admin.jar gửi yêu cầu vào phân hệ Admin, không phải tự tuyên bố đã được cấp quyền. Server đọc users, kiểm BCrypt, kiểm ACTIVE và yêu cầu role ADMIN trước khi cho vào phân hệ này. Không hardcode password Admin hoặc tài khoản DB trong JAR.
2. Session Server gắn user_id từ kết quả xác thực. Trường role trả về chỉ để UI hiển thị; không tin role/user_id/sender_id do Client tự gửi. Với mỗi request Admin, Server kiểm lại role/status hiện hành; từ chối mặc định nếu opcode không được cho phép. Nguyên tắc này phù hợp [OWASP Authorization](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html).
3. REGISTER và UPDATE_MY_PROFILE dùng allowlist trường. REGISTER luôn ghi USER; payload chứa role/status/password_hash đặc quyền bị từ chối. UPDATE_MY_PROFILE không bind nguyên DTO vào mọi cột users. Admin thêm user cũng mặc định USER; cấp ADMIN qua thao tác quản trị riêng có kiểm tra quyền.
4. Đổi role/status chỉ qua endpoint quản trị đã xác thực. Không suy luận quyền từ tên file JAR, tab UI, client_type, username “admin” hoặc cờ is_admin nhóm. Truy vấn dùng tham số JDBC.
5. Sau khóa/soft delete/đổi password: thu hồi session theo chính sách; sau hạ role, mất quyền Admin và buộc xác thực lại là mặc định đơn giản. Mỗi request vẫn tra quyền hiện tại, nên token/Socket cũ không giữ role cũ vĩnh viễn. Request đã được xử lý trước điểm thu hồi có thể đã hoàn tất; mutation Admin dùng khóa ở §5.4 để có ranh giới rõ.

Protocol dự kiến thêm/chuẩn hóa LOGIN, RESUME_SESSION, LOGOUT; phản hồi account_role; lỗi AUTH_REQUIRED/ACCOUNT_LOCKED/FORBIDDEN; thao tác SET_USER_ROLE, SET_USER_STATUS. UI Admin cần màn login, nhãn role, hành động cấp/thu quyền và lỗi Admin cuối; User UI không có điều khiển sửa role. **Đây là định hướng hợp đồng cần review tích hợp trước khi viết Server/DTO**, chưa sửa shared/server/pom hay giao thức thực tế.

### 5.3 Tài khoản Admin đầu tiên

Chọn công cụ bootstrap **chạy riêng tại Server bởi người vận hành**. Người vận hành cung cấp username/profile/email và nhập password qua console không echo hoặc secret runtime, băm BCrypt trước INSERT. Không đưa password mẫu/plaintext vào SQL, repo, dòng lệnh ghi lịch sử hay cấu hình Client.

Bootstrap lấy khóa quyền toàn cục ở §5.4, kiểm tra chưa có tài khoản role ADMIN nào, rồi tạo ACTIVE/ADMIN trong cùng transaction. Nếu đã tồn tại Admin, từ chối bootstrap; không dùng “đăng ký đầu tiên tự thành Admin”. Những Admin tiếp theo được Admin ACTIVE hiện có cấp quyền. Nếu lỡ mất mọi quyền qua can thiệp DB ngoài ứng dụng, dùng recovery offline được ghi nhận bởi người vận hành; không mở endpoint reset quyền không xác thực cho Client.

SQL minh họa INSERT chỉ dùng giá trị bind; `:bcrypt_hash` được tạo tại Server, không phải mật khẩu gốc:

```sql
INSERT INTO users
  (username, password_hash, full_name, email, status, role)
VALUES (:username, :bcrypt_hash, :full_name, :email, 'ACTIVE', 'ADMIN');
```

Không tự seed tài khoản Admin trong ERD. Trạng thái không có Admin lúc DB vừa tạo là giai đoạn bootstrap hợp lệ; trước phục vụ Client phải có ít nhất một ACTIVE/ADMIN.

### 5.4 Khóa/xóa/hạ quyền Admin và chống mất Admin cuối

Định nghĩa Admin còn sử dụng được là `role='ADMIN' AND status='ACTIVE'`. Tài khoản LOCKED/DELETED không được tính bảo vệ. Chỉ soft delete; các FK lịch sử giữ nguyên.

**Quy tắc bắt buộc của Server:** mọi mutation Admin và mọi thay đổi role/status (kể cả với USER hiện tại) lấy một advisory transaction lock toàn cục cố định `-1::bigint`, rồi đọc lại actor/target trong READ COMMITTED. Khóa DM chỉ dùng số không âm, nên không trùng namespace này. Bootstrap/promotion/lock/delete/demotion đều dùng cùng quy tắc; không chỉ COUNT trước rồi UPDATE ở hai transaction.

```sql
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT pg_advisory_xact_lock(-1::bigint);

-- Câu lệnh riêng sau khi đã lấy khóa.
SELECT role, status FROM users WHERE id = :authenticated_actor_id;
-- Server yêu cầu ADMIN + ACTIVE, trừ bootstrap offline đặc biệt.

UPDATE users SET role = :new_role, status = :new_status
WHERE id = :target_id;

SELECT COUNT(*) AS active_admin_count
FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE';
-- Nếu count < 1: ROLLBACK; trả LAST_ACTIVE_ADMIN.
-- Nếu >= 1 và mọi điều kiện khác hợp lệ: COMMIT.
```

Các sửa đổi phải kiểm tra target tồn tại; role/status chỉ nhận enum đã CHECK. Cho phép một Admin tự khóa/hạ quyền/xóa nếu còn Admin ACTIVE khác, sau commit đóng session của họ. Hai Admin đồng thời khóa/hạ quyền nhau không thể cùng làm count về 0: transaction sau đọc actor/quyền và số lượng mới sau khóa.

Đây là invariant liên dòng, không phải CHECK trên một hàng. Bảo đảm nằm ở **Server transaction + khóa DB dùng chung**; UNIQUE/CHECK role không tự bảo vệ Admin cuối. DB runtime credential chỉ ở Server, không là superuser/owner dùng chung với Client; các đường ghi role/status khác phải bị đóng hoặc cùng đi qua service này. SQL quản trị trực tiếp bởi owner có thể phá invariant và thuộc phạm vi vận hành; không tuyên bố DDL role đơn lẻ ngăn được điều đó. Nếu cần cưỡng chế ở DB với nhiều ứng dụng ghi trực tiếp, có thể đóng gói mutation vào function được cấp quyền riêng; chưa chọn thêm hệ thống đó cho đồ án.

Không gọi SMTP/LLM hay chờ người dùng trong khi giữ khóa Admin. Mọi luồng tuân theo mục 7.1: cổng quyền shared/exclusive → nhóm → cặp user; không lấy ngược. Chat thường chỉ lấy shared lock ở cổng quyền, nên nhiều hội thoại vẫn chạy đồng thời. Rollback/retry có giới hạn khi deadlock/serialization failure; không retry âm thầm thao tác ngoài DB chưa có idempotency.

### 5.5 Kiểm thử và đáp ứng đề

| Test dự kiến | Kết quả cần đạt |
|---|---|
| USER chạy Admin.jar/gửi trực tiếp opcode Admin | Server từ chối, DB không đổi. |
| REGISTER/UPDATE_MY_PROFILE chèn role=ADMIN/status=ACTIVE | Payload đặc quyền bị từ chối; không tự nâng quyền/mở khóa. |
| Group Admin nhưng role USER; system Admin không thuộc nhóm | Không vượt ranh giới quyền tương ứng. |
| ADMIN đăng nhập hai UI | Dùng cùng profile/hash; quyền nghiệp vụ nhóm vẫn cần membership. |
| Chỉ còn một ACTIVE Admin, thử LOCKED/DELETED/USER | Rollback với LAST_ACTIVE_ADMIN; Admin LOCKED khác không được tính thay thế. |
| Hai Admin cùng khóa/hạ quyền nhau | Thực thi tuần tự, còn ít nhất một ACTIVE Admin; actor bị thu quyền không tiếp tục mutation bằng Socket cũ. |
| Hai bootstrap đồng thời; bootstrap khi đã có Admin | Một bootstrap thành công; không tạo Admin qua đăng ký công khai. |
| Khóa, soft delete, reset password Admin | Session bị thu hồi theo policy; lịch sử/FK không bị xóa dây chuyền. |

Đáp ứng phân hệ Admin trong đề và nguyên tắc Server phân quyền; không có yêu cầu bắt buộc phải hỏi thầy về users.role hay bảng admins. Thiết kế A/B đã chọn quyền cấp/thu Admin, bootstrap/recovery và việc cùng tài khoản dùng hai UI. Nếu giảng viên có mô hình vai trò riêng ngoài PDF, phải đối chiếu trước khi đổi hợp đồng.

## 6. Tổng hợp thay đổi ERD và ràng buộc

| Thành phần | Baseline | Thiết kế chính thức |
|---|---|---|
| `users` | 11 cột | 12 cột: thêm `role VARCHAR(10) NN DEFAULT 'USER'`, CHECK USER/ADMIN. |
| `activity_logs.session_id` | `VARCHAR(64) NULL` | `UUID NN UK`; UNIQUE đơn `uq_activity_session`. |
| `conversation_clears` | 3 cột, timestamp quyết định visibility | 4 cột: thêm `last_cleared_message_id BIGINT NN DEFAULT 0`, CHECK ≥0; timestamp chỉ tra cứu; cột mới không FK. |
| `group_members` | 5 cột gồm encrypted_group_key | 4 cột, bỏ encrypted_group_key theo mô hình Server. |
| `chat_groups` | flag/key chưa được ràng buộc | Cột giữ nguyên; CHECK flag–key, trigger mode bất biến. |
| `group_messages` | iv nullable chưa có unique | Cột giữ nguyên; CHECK IV Base64 12 byte; partial UNIQUE `(group_id,iv)`; trigger mode/group/sender. |
| Index/sequence direct | `idx_dm_pair_time`, identity mặc định | Thay bằng `idx_dm_pair_id` với cùng hai biểu thức cặp rồi `id`; giữ CACHE 1, increment 1, no cycle. Không âm thầm giữ cả hai index. |
| PK/FK/ON DELETE | 13 PK, 21 FK | Giữ nguyên; không có FK mới từ watermark và không đổi cascade/restrict. |

Các bảng còn lại giữ nguyên cột. Bộ **6 index tường minh của bản 16/10** được hoàn thiện thành **16 index tại mục 8.2**, gồm thay khóa sắp xếp index nhóm và bổ sung index theo truy vấn thực tế. UNIQUE session và unique ghép báo cáo spam là constraint có index hỗ trợ riêng. Các PK/UNIQUE sẵn có vẫn tạo index hỗ trợ như cũ.

Mermaid chỉ gắn UK cho UNIQUE **đơn**; không gắn UK riêng lên group_id/iv của unique ghép có điều kiện. CHECK/trigger/advisory lock/index biểu thức không phải quan hệ FK, nên không vẽ đường giả. Những cột/NULL/default không được liệt kê thay đổi đều giữ nguyên, kể cả chính sách ON DELETE.

## 7. Quyết định chính thức cho các P1 còn lại

### 7.1 Một quy ước transaction và phân quyền xuyên suốt

**Chọn READ COMMITTED, khóa transaction trong PostgreSQL và đọc lại sau khi lấy khóa**, thay vì Java synchronized (không bảo vệ nhiều Connection/Server), check-then-write không khóa (race), hoặc SERIALIZABLE cho mọi thao tác (retry khó giải thích và vẫn phải đặc tả quyền). Mỗi nghiệp vụ dùng một JDBC Connection, autoCommit=false, rollback khi lỗi và trả Connection về pool sau khi khôi phục cấu hình. Không ghép lấy advisory lock và đọc dữ liệu trong cùng câu lệnh; câu đọc sau khóa cần snapshot mới.

Quy ước thứ tự khóa bắt buộc, bổ sung cho các thuật toán mục 2–5:

1. **Cổng quyền `-1::bigint`:** request đã xác thực, login/resume và mọi transaction đọc/ghi nghiệp vụ lấy `pg_advisory_xact_lock_shared(-1::bigint)` trước đọc quyền. Các mutation Admin, đổi role/status/password (kể cả self-service/reset), bootstrap lấy exclusive `pg_advisory_xact_lock(-1::bigint)` ngay từ đầu; không nâng shared thành exclusive giữa transaction. Nhiều request thường cùng giữ shared; thao tác thu hồi quyền chờ các transaction đã vào trước, rồi chặn request mới tới commit. Không giữ khóa khi chờ BCrypt/SMTP/LLM/mạng: hash trước, trong transaction đọc lại trạng thái và hash/version đã kiểm; nếu đã đổi thì từ chối/re-auth. Actor luôn lấy từ session được Server xác thực, không lấy từ payload.
2. **Khóa dòng nhóm:** nếu có, khóa `chat_groups FOR UPDATE` theo group_id tăng dần. Tạo nhóm mới giữ dòng mới trong transaction. Các mutation nhóm và gửi tin đều dùng bước này; đọc lịch sử chỉ cần một SELECT snapshot nhất quán kèm membership.
3. **Khóa cặp user:** dùng cùng công thức SHA-256 không âm ở mục 3.3 cho DM, clear, hide, request, accept/reject, unfriend, block/unblock và report. Giữ tiền tố `dm:` cho mọi luồng để không sinh hai namespace khác nhau cho cùng cặp. Nếu một transaction cần nhiều cặp, tính và loại trùng key rồi lấy theo key tăng dần; không theo thứ tự Client gửi. Thao tác nhóm thêm người cũng lấy khóa cặp actor–người được thêm để kiểm tra block. Không có luồng lấy cặp rồi lấy nhóm.

Trạng thái ACTIVE được kiểm tra sau cổng quyền; mutation kiểm tra target theo quy tắc từng chức năng. Khóa role/status/password độc quyền khiến thao tác đang chạy có ranh giới trước/sau thu hồi rõ ràng mà không cần khóa độc quyền toàn hệ thống cho chat. Không thêm khóa dòng users FOR UPDATE trong luồng chat thường. Sửa profile thường kiểm tra ACTIVE trong shared gate; cấm sửa role/status/password_hash ngoài service chuyên trách. Mọi writer/import phải theo quy ước; owner DB trực tiếp có thể phá invariant. Runtime role không là superuser/owner, không cấp DB credential cho client; không cấp DELETE users/chat_groups/direct_messages/group_messages cho runtime.

Sau commit thu hồi session dưới registry lock của Server; mọi request tiếp theo vẫn đọc quyền hiện tại. ACK/push sau commit; ngay trước đưa nội dung vào hàng đợi kiểm tra session/trạng thái/membership hiện tại; không thể thu hồi byte đã phát hoặc plaintext đã lưu. Retry tối đa 3 lần với backoff cho deadlock/serialization failure đã rollback; timeout/cancel thì rollback và báo lỗi. Không tự retry SEND khi mất ACK sau commit vì baseline chưa có message idempotency key.

**Test cần chạy:** chat hai cặp chạy song song; khóa account chờ chat cũ và chặn chat mới; đổi password trong lúc login; worker cố đảo thứ tự khóa; rollback giải phóng gate/cặp/nhóm; retry không gửi SMTP hai lần. **Rủi ro:** thay quyền độc quyền chặn ngắn toàn hệ thống; chấp nhận cho đồ án nhỏ, giữ transaction ngắn và có lock/statement timeout. Đây là bảo đảm của service + DB lock, không phải 13 bảng tự cưỡng chế mọi quyền.

### 7.2 Friend request, friendship và block

**Chọn giữ ba bảng và enum hiện tại**, partial unique PENDING hai chiều, cùng khóa cặp cho mọi mutation. Không thêm CANCELLED hoặc bảng sự kiện: đề không yêu cầu lịch sử lý do hủy. Không tự accept lời mời ngược chiều: trả ALREADY_PENDING để người nhận chủ động đồng ý.

- SEND_REQUEST: hai user khác nhau và ACTIVE; không block ở bất kỳ chiều nào, chưa là bạn, chưa có PENDING; INSERT. Partial unique là hàng rào DB cuối, bắt unique violation thành lỗi nghiệp vụ.
- ACCEPT: actor đúng receiver, request còn PENDING; đọc lại sau khóa, hai tài khoản ACTIVE, không block hai chiều. UPDATE ACCEPTED và INSERT friendship `(min,max)` trong cùng transaction; retry request đã ACCEPTED chỉ trả thành công nếu friendship vẫn tồn tại, không tự tạo lại sau unfriend/block.
- REJECT: chỉ receiver, PENDING → REJECTED. UNFRIEND: xóa friendship chuẩn hóa, không sửa lịch sử ACCEPTED. Request mới sau unfriend được phép nếu không block.
- BLOCK: actor ACTIVE, target tồn tại và không DELETED; INSERT block có hướng idempotent, DELETE friendship, **DELETE chỉ các request PENDING hai chiều**, giữ ACCEPTED/REJECTED. Xóa request chưa giải quyết thay vì gán REJECTED sai nghĩa. UNBLOCK chỉ xóa dòng do actor tạo; không tự khôi phục bạn/request. Không bắt buộc làm UI unblock nếu phạm vi đề không yêu cầu.
- Block lưu có hướng; tìm kiếm ẩn người đã block actor theo đúng PDF. **Policy nhóm đã chọn ngoài yêu cầu tối thiểu:** khi block tồn tại ở một chiều, cấm lời mời và DM mới cả hai chiều, cấm thêm trực tiếp người đó vào nhóm bởi actor. Không tự xóa hai người khỏi nhóm chung hoặc chặn chat nhóm chung; UI vẫn cung cấp block/report riêng. Lịch sử cũ vẫn xem được theo marker xóa của từng người.
- DM không đòi friendship (PDF cho chat qua tìm kiếm); sender/receiver phải ACTIVE và không block; receiver offline vẫn được lưu. Recipient LOCKED/DELETED không nhận tin mới. `is_read` chỉ chuyển true bởi receiver của tin nhìn thấy, không dùng thay delivery ACK.

**Dữ liệu:** giữ cột, CHECK tự liên hệ, PK friendship có thứ tự và unique PENDING; thêm index inbox và chiều thứ hai friendship tại mục 8.2. **Test:** hai request ngược đồng thời chỉ một PENDING; accept/block đồng thời không còn friendship sau block; unfriend rồi retry ACCEPT không tạo lại bạn; block rồi SEND bị từ chối; unblock không hồi sinh request. **Rủi ro:** mất lịch sử PENDING bị hủy là có chủ ý; semantics block hai chiều rộng hơn câu chữ tối thiểu trong PDF và được ghi là khuyến nghị, không gán cho giảng viên.

### 7.3 Thành viên và Admin nhóm

**Chọn membership hiện tại + khóa dòng nhóm**, thay vì composite FK `(group_id,sender_id)` tới membership (xóa thành viên sẽ xung đột lịch sử), hoặc bảng lịch sử membership mới (chưa cần cho policy lịch sử đã chọn).

- CREATE: creator ACTIVE và ít nhất một user ACTIVE khác, username được resolve tại Server, loại trùng; kiểm tra block actor–người được thêm. INSERT nhóm và tất cả membership, creator is_admin=true trong một transaction. Một target lỗi thì rollback toàn bộ, không tạo nhóm một người.
- Thành viên ACTIVE được đổi tên/thêm user ACTIVE, đúng quyền tối thiểu của PDF; chỉ Admin nhóm ACTIVE được cấp/thu is_admin hoặc xóa người khác. Group creator chỉ là thông tin lịch sử, không có quyền vĩnh viễn. System Admin không tự là group Admin.
- Mọi mutation sau khóa nhóm đọc lại actor membership và status. Thêm thành viên lấy khóa cặp để không lọt qua block đồng thời. Gửi tin và remove cùng lấy khóa nhóm nên remove thắng thì send bị từ chối. User hiện tại được đọc toàn bộ lịch sử nhóm, người bị remove không tải tiếp; re-add đọc lại toàn bộ. Không cho client chọn joined_at hoặc sender_id.
- **Admin cuối:** cấm demote/remove/rời nhóm nếu còn thành viên ACTIVE mà không còn Admin ACTIVE. Cho phép chuyển quyền bằng một transaction cấp người kế nhiệm rồi rời/xóa membership. Chức năng tự rời là policy hỗ trợ, không phải yêu cầu thêm UI bắt buộc. Nếu người cuối rời, giữ nhóm và tin làm lịch sử, không hard-delete; nhóm rỗng không nhận tin và không hiển thị cho User không còn membership.
- Khi LOCKED/DELETED làm nhóm mất Admin ACTIVE: trong transaction status exclusive gate, khóa tất cả nhóm target tham gia theo ID tăng, cập nhật status, rồi nếu nhóm còn user ACTIVE mà không có Admin ACTIVE, **tự cấp is_admin cho user ACTIVE có id nhỏ nhất**. Không xóa membership/tin của target; khi unlock thì có thể có nhiều Admin, hợp lệ. Nhóm không còn user ACTIVE được coi là tạm ngưng; khi một account được mở khóa, cùng service sửa invariant bằng policy trên. Không trao thêm quyền system Admin. Cách này ưu tiên khóa tài khoản có vấn đề hơn từ chối khóa vì họ là Admin nhóm cuối.

**Dữ liệu:** giữ PK `(group_id,user_id)`, bỏ encrypted_group_key theo mục 4, không FK membership từ tin. Invariant số lượng/quyền nằm ở transaction service, không viết CHECK truy vấn bảng khác. **Test:** tạo nhóm target lỗi rollback; hai Admin xóa nhau; remove/send đồng thời; khóa Admin nhóm cuối tự chuyển quyền; khóa toàn bộ rồi mở lại một người; re-add xem lịch sử; thành viên thường gọi opcode remove bị từ chối. **Rủi ro:** policy toàn bộ lịch sử và tự chọn kế nhiệm là quyết định sản phẩm của thiết kế này; không cung cấp bảo mật lịch sử theo từng giai đoạn như E2EE.

### 7.4 Điều kiện báo cáo spam

**Chọn bằng chứng một DM đã commit từ reported → reporter**, sát User §10 PDF; không tính chỉ tin do reporter tự gửi hoặc chỉ chung nhóm. Chưa có yêu cầu report tin nhóm, không mở rộng schema evidence/event.

Actor ACTIVE, target tồn tại khác actor (cho phép LOCKED/DELETED để tiếp tục lưu khiếu nại); lấy khóa cặp và kiểm tra EXISTS direct_messages(sender_id=reported,receiver_id=actor). Tin đã hidden/clear vẫn là bằng chứng vì không xóa vật lý. INSERT spam_reports, không nhận reporter_id từ client. Một cặp chỉ có **một report trong toàn bộ thời gian** qua `UNIQUE(reporter_id,reported_id)`; retry trả report cũ, không đổi created_at/reason. Không chọn report vô hạn (spam hệ thống) hoặc cooldown dùng thời gian động trong CHECK (không phù hợp). Đề không yêu cầu đóng/mở vụ việc; nếu sau này có workflow thì thiết kế lại giới hạn này.

**Thay đổi:** thêm unique ghép, không cột/bảng/FK mới. **Test:** chưa chat, chỉ chat chiều ngược, chỉ nhóm đều từ chối; đã nhận DM nhưng clear vẫn report được; hai retry chỉ một dòng; Client giả reporter không có hiệu lực. **Rủi ro:** không report lần hai sau một vụ đã xử lý, chấp nhận cho baseline không có trạng thái xử lý; reason tùy chọn, Server giới hạn 2000 ký tự.

### 7.5 ON DELETE, soft delete và dữ liệu lịch sử

**Chọn giữ 21 FK với 9 RESTRICT, 12 CASCADE** theo ERD hiện tại; không thay tất cả thành CASCADE (mất lịch sử) hoặc tất cả thành RESTRICT (liên kết phụ thuộc không dọn được khi bảo trì hợp lệ). Không có chức năng hard-delete user/nhóm trong runtime. ID và FK không ON UPDATE CASCADE; giữ mặc định NO ACTION, FK immediate, không DEFERRABLE.

| Bảng con / FK | ON DELETE đã chọn | Lý do |
|---|---|---|
| login_history.user_id, activity_logs.user_id | RESTRICT | Giữ lịch sử/thống kê |
| friend_requests.sender_id/receiver_id | CASCADE | Liên kết tài khoản, chỉ dọn khi bảo trì xóa vật lý |
| friendships.user_id_1/user_id_2 | CASCADE | Cặp quan hệ phụ thuộc |
| user_blocks.blocker_id/blocked_id | CASCADE | Cặp chặn phụ thuộc |
| direct_messages.sender_id/receiver_id | RESTRICT | Không mất hội thoại |
| message_hidden.message_id/user_id | CASCADE | Marker phụ thuộc tin/user |
| conversation_clears.user_id/partner_id | CASCADE | Marker phụ thuộc user; watermark không FK |
| chat_groups.created_by | RESTRICT | Giữ thông tin creator |
| group_members.group_id | CASCADE | Liên kết thuộc nhóm |
| group_members.user_id | RESTRICT | Không phá membership/lịch sử qua hard delete |
| group_messages.group_id | CASCADE | Tin thuộc nhóm, chỉ bảo trì xóa nhóm toàn bộ |
| group_messages.sender_id | RESTRICT | Giữ người gửi |
| spam_reports.reporter_id/reported_id | RESTRICT | Giữ dấu vết khiếu nại |

Soft delete chỉ đổi status DELETED, không tái sử dụng username/email, không xóa tin/login/report/membership/friendship/block. DELETE user không được dùng để thực hiện UI xóa. DELETED là cuối vòng đời; cấm đổi lại ACTIVE/LOCKED, khác với khóa có thể mở. Các PENDING liên quan user DELETED được DELETE cùng transaction; không cần khóa cặp vì exclusive gate chặn mọi writer thường. Nhóm được xử lý kế nhiệm theo 7.3. UI danh sách bạn và tìm người bỏ DELETED, vẫn có thể hiện LOCKED offline; lịch sử giữ tên/hồ sơ hiện tại hoặc nhãn đã xóa, không snapshot danh tính cũ. Quyền xem lịch sử vẫn theo chính actor và membership.

**Test:** xóa user không mất FK/tin/thống kê; DELETED không login hoặc phục hồi; thử từng hard DELETE với dữ liệu con đúng RESTRICT/CASCADE trong DB test bằng owner; soft delete không chạy CASCADE; runtime DELETE bị từ chối. **Rủi ro:** chủ DB vẫn có thể xóa trực tiếp; đây không phải cơ chế lưu trữ chống can thiệp owner hoặc yêu cầu xóa dữ liệu cá nhân theo pháp lý.

### 7.6 Danh tính, validation và bảo mật cơ bản

**Chọn lưu username/email đã canonical hóa**, giữ UNIQUE đơn hiện tại, thay vì citext (extension không cần thiết) hoặc functional unique cộng cột hiển thị (phức tạp hơn baseline).

- Username ổn định, không cho đổi sau tạo; 3–50 ký tự ASCII `[a-z0-9_.-]`, trim và lowercase Locale.ROOT tại Server, DB CHECK regex `^[a-z0-9_.-]{3,50}$`. Tên tiếng Việt đặt trong full_name, trim, CHECK `btrim(full_name) <> ''`. Bảo lưu username của DELETED.
- Email 1–100 ký tự, chỉ ASCII cho đồ án; Server trim/lowercase toàn địa chỉ, kiểm định dạng cơ bản; DB CHECK `email = lower(btrim(email))`, `email <> ''`, regex `^[!-~]+$` cấm khoảng trắng/non-ASCII. UNIQUE gồm cả user DELETED. Đây là policy ứng dụng coi email không phân biệt hoa/thường, không tuyên bố mọi hệ thống mail có semantics giống nhau. Từ chối email Unicode/EAI, không cắt chuỗi âm thầm.
- Giữ 10 CHECK baseline (gender nullable với MALE/FEMALE/OTHER; status; action OPEN_APP; request enum; 6 CHECK tự liên hệ/thứ tự). Thêm role enum, watermark >=0, key–mode, IV theo mục 3–5; thêm CHECK id>0 cho tám bảng identity (giúp giả định watermark), tên nhóm `btrim(group_name) <> ''`, DM/group content không rỗng và `octet_length(content) <= 65536`, reason NULL hoặc char_length<=2000. Full_name/group_name VARCHAR giới hạn sẵn; nội dung whitespace-only từ Client bị Server từ chối, ciphertext Base64 không đem trim như plaintext.
- Server giới hạn plaintext tin 16000 byte UTF-8 trước mã hóa (Base64 + tag nằm dưới 65536), payload Socket tối đa 128 KiB/frame; JSON có framing độ dài rõ, timeout/rate limit login/report; PreparedStatement và allowlist sort/filter. Với tìm chuỗi dùng ILIKE và escape `%`, `_`, ký tự escape để tìm literal; luôn áp dụng visibility trước LIMIT/pagination. Không thêm trigram/full text cho quy mô đồ án trước khi đo.
- Password BCrypt: Server giới hạn đầu vào tối đa 72 byte UTF-8 để tránh thư viện cắt ngầm, kiểm độ mạnh tối thiểu, không log password/hash/token. Không CHECK hash chỉ bằng regex rồi coi là bảo mật; Server dùng thư viện BCrypt theo stack. Reset theo PDF: mật khẩu ngẫu nhiên đủ mạnh, chỉ hash trong DB, SMTP có TLS, không log secret; tạo/hash ngoài khóa, exclusive gate đọc lại và commit hash rồi gửi email ngoài transaction, lỗi SMTP trả thất bại gửi và cho phép reset lại. Không hứa atomic DB+SMTP; không thêm outbox/bảng reset ở baseline. Rate limit reset, phản hồi công khai không lộ email tồn tại.
- Khi chạy qua mạng, chọn JSSE TLS có sẵn JDK bọc TCP với xác minh chứng chỉ; không tự trust-all. Loopback demo có thể TCP nhưng phải ghi rõ hạn chế. Không đổi stack/dependency, đây là điều kiện triển khai an toàn của thiết kế; AES-GCM lưu DB không bảo vệ password/tin trên đường truyền. Server kiểm quyền trước gửi nội dung sang LLM, chỉ gửi đoạn người dùng chủ động chọn, không tự xuất lịch sử nhóm mã hóa.

**Test:** username khác hoa trùng sau normalize; ghi trực tiếp uppercase bị CHECK; email khoảng trắng/Unicode/trùng; chuỗi rỗng, NULL bắt buộc, ID<=0; chuỗi tìm `%_`; message UTF-8 đúng/vượt biên; mật khẩu vượt 72 byte; reset SMTP lỗi không lưu plaintext; client bỏ TLS xác minh phải thất bại. **Rủi ro:** reset làm mật khẩu cũ mất hiệu lực trước khi email tới; có thể thử reset lại, không cam kết delivery. Các giới hạn nhập liệu là lựa chọn mới, không phải yêu cầu nguyên văn PDF.

### 7.7 Thống kê Admin và khoảng thời gian

**Chọn cột thứ hai là tổng số người duy nhất cách tối đa hai cạnh**, loại chính user; cột trực tiếp riêng. Với U–A,U–B,A–C,B–C: trực tiếp 2, tổng trực tiếp+cấp hai 3. Không chọn chỉ cấp hai (=1) vì câu PDF “tính luôn” nghiêng về tổng; không chọn cộng số đường đi vì đếm trùng. Đây là diễn giải mặc định có căn cứ, chưa khẳng định thầy xác nhận. Self-join/CTE hai bước UNION DISTINCT, không cần đệ quy tùy độ sâu; loại DELETED ở cả bước trung gian và đích, LOCKED vẫn tính là quan hệ tồn tại. Block đã xóa friendship nên không tính nữa.

Lượt mở = COUNT OPEN_APP, người hoạt động/tháng = DISTINCT user_id, đối tác/nhóm = DISTINCT receiver_id/group_id trong tin do user chủ động gửi. Lịch sử hidden/clear vẫn tính hoạt động thật. Đếm đăng ký và hoạt động quá khứ bao gồm account hiện LOCKED/DELETED để không sửa số liệu lịch sử khi khóa/xóa; Admin lọc status riêng nếu muốn. JOIN profile lấy tên hiện tại. Khoảng `[from,to)` và múi giờ Asia/Ho_Chi_Minh, đủ 12 tháng kể cả 0; không dùng BETWEEN có hai biên đóng. Đã đăng nhập từ tháng trước và chỉ reconnect tháng sau không tạo OPEN_APP mới; metric không phải số người đang online.

**Dữ liệu:** không bảng/cột đếm cache; thêm users.created_at và index thời gian/activity tại 8.2. **Test:** hình kim cương, chu kỳ, user không bạn, bạn bị DELETED, 2 session/1 user, tháng 0, biên năm và UTC/VN, khóa user không làm mất số tháng cũ. **Rủi ro:** nếu giảng viên muốn chỉ cấp hai thì sửa truy vấn/nhãn, không thay schema.

## 8. Đặc tả chuyển sang database.sql

### 8.1 Nguồn cột, constraint và thứ tự tạo

Nguồn cột cuối là [ERD.mmd](ERD.mmd): **13 bảng, 65 cột, 21 FK, 8 PK identity đơn và 5 PK ghép, 58 cột NOT NULL, 7 nullable**. Giữ kiểu/độ dài/default của [schema B](archive/Database_Schema_Draft.md) trừ các thay đổi mục 6. Các CHECK/index/trigger/quyền/transaction trong tài liệu này bổ sung cho hình; không suy ra UK đơn từ unique ghép.

Tạo users trước; tiếp login_history, activity_logs, friend_requests, friendships, user_blocks, direct_messages; sau đó message_hidden, conversation_clears; chat_groups trước group_members/group_messages; spam_reports sau users. Dùng BIGINT GENERATED ALWAYS AS IDENTITY START 1 INCREMENT 1 CACHE 1 NO CYCLE cho tám id, không cấp ID trước khóa DM. TIMESTAMPTZ không đổi thành timestamp local. `conversation_clears.cleared_at` bắt buộc không default, service ghi clock_timestamp; watermark default 0. Không tự seed Admin/password/key trong script.

UNIQUE constraint cuối: users.username; users.email; activity_logs.session_id; **spam_reports(reporter_id,reported_id)**. PK ghép giữ nguyên 5 bảng. CHECK cuối: 10 baseline + 4 thuộc role/watermark/key-mode/IV + 8 id dương + username/email canonical (email tách 3 biểu thức) + tên/full_name/content/reason tại 7.6; không dùng CHECK CURRENT_DATE cho dob hoặc CHECK truy vấn bảng khác. Dob nullable, Server kiểm không ở tương lai khi nhập; không đặt invariant tuổi tùy tiện từ đề.

Trigger DDL bắt buộc của phương án mã hóa: hai guard ở 4.3 cho mode bất biến và mode–IV/group/sender. Các invariant friend/group/admin/status terminal được bảo đảm bởi Server service/khóa, không tuyên bố DDL đơn lẻ đủ. Định nghĩa quyền runtime và bootstrap vận hành riêng; mọi script DDL phải chạy trên DB test rỗng trước dùng thật. DDL minh họa ALTER ở mục 2–5 là thông tin chênh lệch, không chạy nối nguyên văn thành migration.

### 8.2 Bộ index cuối được chọn

**16 CREATE INDEX tường minh** dưới đây, cộng index tự sinh cho 13 PK và 4 UNIQUE constraint. Chưa có EXPLAIN/benchmark; chọn theo đường truy vấn cần dùng, không tạo mọi FK index một cách máy móc vì không hard-delete runtime.

| Tên | Khóa / điều kiện | Công dụng |
|---|---|---|
| idx_login_history_time | login_history(login_time DESC) | Login chung |
| idx_login_history_user_time | login_history(user_id,login_time DESC) | Login từng user |
| idx_activity_time_user | activity_logs(created_at,user_id) | Khoảng ngày, biểu đồ tháng |
| idx_activity_user_time | activity_logs(user_id,created_at) | Hoạt động từng user |
| uq_pending_friend_pair | friend_requests(LEAST(sender_id,receiver_id),GREATEST(sender_id,receiver_id)) WHERE status='PENDING', UNIQUE | Chống PENDING hai chiều |
| idx_friend_requests_inbox | friend_requests(receiver_id,created_at DESC) WHERE status='PENDING' | Inbox yêu cầu |
| idx_friendships_second | friendships(user_id_2,user_id_1) | Chiều còn lại của friendship; chiều đầu đã có PK |
| idx_dm_pair_id | direct_messages(LEAST(sender_id,receiver_id),GREATEST(sender_id,receiver_id),id) | Watermark/lịch sử; thay idx_dm_pair_time |
| idx_dm_sender_time | direct_messages(sender_id,created_at,receiver_id) | Thống kê chủ động; bằng chứng spam có thể lọc receiver |
| idx_dm_receiver_id | direct_messages(receiver_id,id) | Danh sách hội thoại/offline theo receiver |
| idx_group_members_user | group_members(user_id,group_id) | Liệt kê nhóm của user, đổi status/kế nhiệm |
| idx_group_messages_time | group_messages(group_id,created_at,id) | Lịch sử nhóm thứ tự ổn định khi cùng timestamp |
| idx_group_messages_sender_time | group_messages(sender_id,created_at,group_id) | Thống kê nhóm chủ động |
| idx_spam_reports_time | spam_reports(created_at DESC) | Danh sách spam |
| idx_users_created_at | users(created_at) | Đăng ký mới/biểu đồ đăng ký |
| uq_group_message_iv | group_messages(group_id,iv) WHERE iv IS NOT NULL, UNIQUE | Nonce không trùng trong các tin lưu cùng nhóm/DEK |

Không giữ idx_dm_pair_time cũ; không tạo thêm index username/email đã có UNIQUE, chưa cần index role cho tập Admin nhỏ; không thêm search extension. Tối ưu lại sau khi EXPLAIN trên dữ liệu test cho thấy cần thiết.

## 9. Kiểm chứng và phạm vi chưa chạy

DDL và suite tại script/tests đã thực thi trên database test mới: **202 PASS, 0 FAIL** ở lần chạy cuối. Catalog thực xác nhận 13 bảng, 65 cột, 58 NOT NULL, 13 PK, 21 FK, 4 UNIQUE, 31 CHECK, 33 index và 2 trigger. Ba lần trước dừng do lỗi trong bộ test: cách đọc DESC, fixture trùng UUID, SQLSTATE RESTRICT. Không nới lỏng constraint hoặc thay đổi logic schema để đạt PASS. Chi tiết và tên database test trong [TEST_RESULTS.md](TEST_RESULTS.md).

Chưa chạy Java/JDBC, kiểm thử nhiều Connection với barrier, quyền runtime non-owner, bootstrap/kế nhiệm Admin, SMTP/LLM, TLS, đo EXPLAIN/hiệu năng hoặc AEAD roundtrip. Những test tương ứng ở mục 2–7 vẫn là test cần triển khai. Không dùng việc PASS trigger/unique IV để khẳng định AES-GCM hoặc concurrency đã đúng.

Đủ điều kiện SQL cho phần database mốc 20%; mốc toàn dự án còn cần GUI, báo cáo và ảnh commit. Server-side AES-GCM vẫn PROVISIONAL. Không thêm bảng session/audit/key history hoặc cơ chế doanh nghiệp ngoài yêu cầu hiện tại.

## 10. Nguồn thực thi và bảo toàn tài liệu

- [database.sql](../../script/database.sql): DDL thực thi, không phải migration.
- [Database_Schema.md](Database_Schema.md): cột, ràng buộc, index từ catalog thực.
- [ERD.mmd](ERD.mmd), [SVG](ERD.svg), [PNG](ERD.png): 13 bảng/65 cột/21 FK; CHECK, index biểu thức và policy service được mô tả trong tài liệu, không vẽ FK giả.
- [Bản schema B](archive/Database_Schema_Draft.md), [DOCX](archive/Database_Schema_Draft.docx), [ERD cũ](archive/ERD_2026-10-16.mmd), [review cũ](archive/ERD_Review_2026-10-16.md): nguồn lịch sử, không phải bản chính thức song song.
- [PostgreSQL locks](https://www.postgresql.org/docs/18/explicit-locking.html), [constraints](https://www.postgresql.org/docs/18/ddl-constraints.html), [sequence/cache](https://www.postgresql.org/docs/18/sql-createsequence.html), [NIST GCM](https://csrc.nist.gov/pubs/sp/800/38/d/final).
