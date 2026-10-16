# Đề xuất bốn quyết định Database Java Chat System

Ngày: 16/10/2026. Người đề xuất: Codex, hỗ trợ thành viên A. Trạng thái: **PROPOSED, chưa được A/B phê duyệt**; riêng phạm vi mã hóa là **PROVISIONAL, cần giảng viên xác nhận**. Tài liệu chọn phương án cụ thể để hai thành viên duyệt, không ghi đè thiết kế gốc và không phải script triển khai.

## 1. Kết luận và nguồn quyết định

| Vấn đề | Phương án khuyến nghị cuối cùng | Thay đổi dữ liệu |
|---|---|---|
| Phiên OPEN_APP | UUID v4 do Client tạo cho mỗi phiên sử dụng mới; Server xác thực và ràng buộc chủ sở hữu; `UUID NOT NULL UNIQUE`; reconnect dùng lại ID. | Sửa kiểu/NULL của `activity_logs.session_id`, thêm UNIQUE đơn. |
| Xóa hội thoại | Mốc `last_cleared_message_id`; giữ `cleared_at` để tra cứu; mọi gửi/clear cùng cặp lấy cùng advisory transaction lock **trước khi cấp ID**. | Thêm một cột BIGINT không phải FK; đổi index lịch sử cặp sang cặp–ID. |
| Mã hóa nhóm | **PROVISIONAL**: AES-GCM tại Server; một DEK/nhóm; mode cố định từ lúc tạo; wrapped DEK dưới KEK bên ngoài DB; không E2EE. | Bỏ cột dự phòng E2EE ở membership; thêm CHECK/unique IV và trigger nhất quán; không thêm key_version/flag từng tin. |
| Admin hệ thống | `users.role = USER/ADMIN`; cùng tài khoản có thể dùng hai UI; Server kiểm tra quyền; bảo vệ Admin ACTIVE cuối bằng khóa transaction toàn cục. | Thêm một cột role và CHECK. Không có bảng admins riêng. |

Kết quả đề xuất vẫn **13 bảng**, từ 64 lên **65 cột**: thêm `users.role`, thêm `conversation_clears.last_cleared_message_id`, bỏ `group_members.encrypted_group_key`. **21 FK và toàn bộ ON DELETE hiện có giữ nguyên.** Các constraint/index ngoài khả năng thể hiện của Mermaid được nêu đầy đủ bên dưới.

Đã đọc đủ 7 tài liệu được yêu cầu: [AGENTS](../../AGENTS.md), [đề chính thức](../Final%20Project.pdf) (5 trang), [Tech Stack](../Tech_Stack.md), [Project Context](../Project_Context.md), [schema B](Database_Schema_Draft.md), [ERD gốc](ERD.mmd), [review ERD](ERD_Review.md). Không có nguồn yêu cầu nào bị thiếu/không đọc được. PDF đã được trích lại; không suy đoán nội dung DOCX mới ngoài bản Markdown hiện hành.

**Phần A/B đã chốt được kế thừa:** danh sách 13 bảng; PostgreSQL local; Java 21/JDBC/Socket/JSON; BCrypt; soft delete; xóa riêng từng phía; OPEN_APP một lần/phiên thật; biểu đồ theo số user mở app; Server giữ bí mật, phân quyền và transaction; hướng AES-GCM. **Mọi chi tiết chọn mới trong tài liệu này vẫn là đề xuất Codex**, kể cả việc bỏ cột, khóa hội thoại và bổ sung role.

Đề tr. 2 yêu cầu thống kê hoạt động và hai phân hệ; tr. 3 §11 yêu cầu xóa lịch sử/tin lẻ, §12g yêu cầu mã hóa nhóm nhưng không chỉ định E2EE; tr. 4–5 yêu cầu script và thiết kế DB. Không có yêu cầu chính thức về UUID, số bảng, role hay key_version. Tiêu đề “P1 chuẩn hóa” của dự thảo không thay thế xác nhận A/B. Khác biệt cách đếm bạn cấp hai trong review cũ vẫn còn, không được bốn quyết định này tự giải quyết.

**Quy ước SQL:** DDL dưới đây minh họa thay đổi trên baseline **rỗng**, không phải migration an toàn cho DB đã có dữ liệu. `:name` trong truy vấn là tham số mô tả; khi code pgJDBC phải thay bằng `?` và bind giá trị, không nối chuỗi. Chưa thực thi bất kỳ DDL hay truy vấn nào.

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

Đáp ứng Admin §8–9 và quyết định OPEN_APP của A/B với định nghĩa phiên trên. Không bắt buộc hỏi thầy về kiểu UUID; A/B cần duyệt semantics phiên và phạm vi tính cả hai Client. Nếu thầy muốn tính mọi ngày/tháng Client còn mở, phải đổi định nghĩa metric, không lặng lẽ ghi OPEN_APP khi reconnect.

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

Đáp ứng User §11d–g và xóa riêng từng phía A/B đã chốt. Không bắt buộc hỏi thầy về loại khóa; A/B phải duyệt điểm phân định “trước/sau xóa” theo thứ tự xử lý Server. Nếu giảng viên yêu cầu “chỉ xóa tập các tin nhắn đã hiển thị tại Client khi bấm”, đó là semantics khác, phải đặc tả riêng.

## 4. Quyết định 3 về AES-GCM nhóm

### 4.1 Phạm vi đề và phương án

PDF tr. 3 §12g ghi yêu cầu mã hóa nhóm và dẫn tham khảo nhóm mã hóa Facebook. Không ghi chính thức thuật ngữ E2EE, mô hình khóa hoặc khả năng bật/tắt/rotate. Tham khảo Facebook cũng không đủ để kết luận mã hóa tại Server chắc chắn đạt ý giảng viên. Do đó chọn phương án triển khai mặc định dưới đây với nhãn **PROVISIONAL**; cần hỏi thầy trước nghiệm thu yêu cầu bảo mật.

| Phương án | Lợi ích | Chi phí/hạn chế | Kết luận |
|---|---|---|---|
| Server mã hóa trước PostgreSQL, giải mã sau kiểm tra quyền | Dễ làm với JCA/JDBC; DB dump không đủ đọc nội dung nếu thiếu KEK; reset mật khẩu không mất khóa nhóm | Server có thể đọc tin; Server/KEK bị chiếm thì không bảo vệ; không tự bảo vệ đường TCP | **Chọn PROVISIONAL** cho nhóm hai người. |
| E2EE, Client giữ/phân phối khóa | Có thể bảo vệ nội dung khỏi Server khi giao thức làm đúng | Cần khóa định danh thiết bị, xác thực khóa, phân phối/rotate, multi-device, phục hồi khi reset, lịch sử theo membership; 13 bảng hiện chưa đặc tả đủ | Chỉ chuyển nếu giảng viên yêu cầu; phải thiết kế lại luồng khóa, không đổi nhãn từ Server encryption thành E2EE. |
| Tự động rotate DEK + lưu lịch sử nhiều phiên bản khóa | Quản lý khóa cũ/thu hồi theo thời kỳ rõ hơn | Cần nơi lưu nhiều khóa/metadata và mapping tin–khóa; phức tạp hơn nhu cầu hiện tại | Không chọn mặc định. |

Tính toàn vẹn GCM dựa vào tag và AAD, không chỉ che chuỗi. Xem [NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final). Bảo vệ lưu trữ không thay bảo vệ đường truyền: với chạy qua mạng ở mốc 80%, khuyến nghị JSSE TLS trong JDK cho Socket và xác thực chứng chỉ; đây là đề xuất cần A/B review, không tự coi TCP hiện tại đã mã hóa.

### 4.2 Khóa, payload và truy cập

1. Mỗi nhóm chọn mode lúc tạo: thường hoặc mã hóa. Sau tạo, mode **bất biến**; đổi tên/thành viên vẫn được. UI không có nút bật/tắt mode nhóm cũ; muốn mode khác tạo nhóm mới. Đây là giới hạn phạm vi được chọn, không phải yêu cầu đã có của thầy.
2. Nhóm mã hóa có một DEK AES-256 ngẫu nhiên qua KeyGenerator/SecureRandom JDK. Server không gửi DEK/KEK cho Client và không gắn DEK với password người dùng.
3. KEK AES-256 được cung cấp lúc vận hành từ file riêng có quyền OS hạn chế hoặc biến môi trường Server, bên ngoài repo/JAR/client/DB. Không có secret mặc định trong SQL. Sao lưu KEK tách khỏi DB; mất toàn bộ KEK thì không thể giải mã lịch sử.
4. Bọc DEK bằng chuẩn AES Key Wrap JCA `AESWrap` (tương đương `AES/KW/NoPadding`), lưu `kw1:<kek_id>:<base64_wrapped_DEK>` vào `wrapped_encryption_key`. `kek_id` không phải secret. KW dùng để bọc khóa, còn **tin nhắn vẫn AES-GCM**; không tự viết thuật toán wrap. Chuẩn có sẵn trong [Java 21 Standard Names](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#cipher-algorithms), không thêm dependency/framework.
5. Với từng tin mã hóa, dùng `AES/GCM/NoPadding`, IV 12 byte mới từ SecureRandom, tag 128 bit. JCA trả ciphertext kèm tag từ doFinal; lưu Base64 cả hai vào content, Base64 IV vào iv. Không tách/bỏ tag; tag sai phải từ chối, không trả plaintext một phần. Xem [Cipher Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/Cipher.html).
6. AAD UTF-8 cố định `chatapp-gm-v1:<group_id>:<sender_id>`; Server dựng từ ID thật, Client không cung cấp AAD tùy ý. Đổi group/sender của ciphertext phải làm kiểm tra tag thất bại. Không đưa created_at vào AAD để tránh lệch định dạng timestamp.
7. Nhóm thường: content là văn bản UTF-8, iv NULL, wrapped key NULL. Nhóm mã hóa: content là Base64(ciphertext||tag), iv canonical Base64 16 ký tự cho 12 byte, wrapped key không NULL. Mode bất biến nên không lẫn tin plaintext cũ/mới trong một nhóm.
8. Mọi đọc/gửi tin kiểm tra user ACTIVE và membership ở Server. Admin hệ thống không tự có quyền đọc mọi tin nhóm. Thành viên mới được đọc toàn bộ lịch sử nhóm; người bị xóa không đọc/tải/push tiếp qua Server. Không thể thu hồi plaintext đã đọc/lưu trước đó. Đây là policy mới cần A/B duyệt, không hứa quyền mật mã theo từng giai đoạn như E2EE.

### 4.3 IV duy nhất và schema đề xuất

Giữ một DEK riêng cho từng nhóm, không tái sử dụng DEK cho nhóm khác. UNIQUE `(group_id,iv)` là rào chắn chống trùng nonce giữa **các tin được lưu** dưới DEK đó, kể cả nhiều worker. Sinh IV ngẫu nhiên 96 bit cho mỗi lần encrypt; nếu unique conflict, rollback và encrypt lại với IV mới. Không dùng timestamp làm nonce; không phát ciphertext trước khi INSERT commit. Không hard-delete rồi tái sử dụng IV hoặc khôi phục DB cũ và tiếp tục cùng khóa một cách tùy tiện. UNIQUE không phải sổ ghi mọi IV từng dùng trong lịch sử vận hành; phạm vi này dựa thêm vào SecureRandom và chính sách không reuse/không xóa vật lý lịch sử nhóm đang chạy. Giữ giới hạn vận hành dưới 2^32 lần mã hóa cho mỗi DEK, gồm retry; đồ án nhỏ thấp hơn rất nhiều. Nếu RNG lỗi/collision lặp thì dừng gửi và kiểm tra, không retry vô hạn.

```sql
ALTER TABLE chat_groups ADD CONSTRAINT chk_group_key_mode CHECK (
  (is_encrypted AND wrapped_encryption_key IS NOT NULL)
  OR (NOT is_encrypted AND wrapped_encryption_key IS NULL)
);

-- Chỉ bỏ trong bản đề xuất sau khi A/B duyệt Server-side Encryption.
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
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.is_encrypted IS DISTINCT FROM OLD.is_encrypted THEN
    RAISE EXCEPTION 'Group encryption mode is immutable';
  END IF;
  RETURN NEW;
END;
$$;
CREATE TRIGGER trg_group_mode_immutable
BEFORE UPDATE OF is_encrypted ON chat_groups
FOR EACH ROW EXECUTE FUNCTION guard_group_encryption_mode();

CREATE FUNCTION guard_group_message_mode() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE encrypted boolean;
BEGIN
  IF TG_OP = 'UPDATE' THEN
    IF NEW.group_id IS DISTINCT FROM OLD.group_id OR
       NEW.sender_id IS DISTINCT FROM OLD.sender_id THEN
      RAISE EXCEPTION 'Message group and sender are immutable';
    END IF;
  END IF;
  SELECT is_encrypted INTO STRICT encrypted
    FROM chat_groups WHERE id = NEW.group_id FOR UPDATE;
  IF encrypted <> (NEW.iv IS NOT NULL) THEN
    RAISE EXCEPTION 'Message IV does not match group mode';
  END IF;
  RETURN NEW;
END;
$$;
CREATE TRIGGER trg_group_message_mode
BEFORE INSERT OR UPDATE ON group_messages
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

**Bắt buộc hỏi thầy để chốt bảo mật:** yêu cầu §12g có bắt buộc Server không đọc được nội dung (E2EE) không? Nếu có, phương án này chưa đáp ứng. Có thể trình bày prototype mã hóa lưu trữ trong lúc chờ, nhưng báo cáo phải ghi đúng tên và hạn chế. A/B cần duyệt mode bất biến, policy lịch sử khi thêm/xóa người, bỏ cột E2EE và không tự động rotate DEK. Không bắt buộc hỏi thầy về kiểu Base64 hay lựa chọn AESWrap cụ thể nếu mô hình đã được chấp thuận.

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

Protocol dự kiến thêm/chuẩn hóa LOGIN, RESUME_SESSION, LOGOUT; phản hồi account_role; lỗi AUTH_REQUIRED/ACCOUNT_LOCKED/FORBIDDEN; thao tác SET_USER_ROLE, SET_USER_STATUS. UI Admin cần màn login, nhãn role, hành động cấp/thu quyền và lỗi Admin cuối; User UI không có điều khiển sửa role. **Đây là hợp đồng đề xuất cho A/B review**, chưa sửa shared/server/pom hay giao thức thực tế.

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

Không gọi SMTP/LLM hay chờ người dùng trong khi giữ khóa Admin. Luồng nào thật sự cần nhiều loại khóa phải lấy theo thứ tự: khóa quyền Admin → khóa hội thoại/nhóm → khóa dòng user tăng dần; không lấy ngược. Luồng chat thường không lấy khóa Admin toàn cục. Rollback/retry có giới hạn khi deadlock/serialization failure; không retry âm thầm thao tác ngoài DB chưa có idempotency.

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

Đáp ứng phân hệ Admin trong đề và nguyên tắc Server phân quyền; không có yêu cầu bắt buộc phải hỏi thầy về users.role hay bảng admins. A/B cần duyệt quyền cấp/thu Admin, bootstrap/recovery và việc cùng tài khoản dùng hai UI. Nếu giảng viên có mô hình vai trò riêng ngoài PDF, phải đối chiếu trước khi đổi hợp đồng.

## 6. Tổng hợp thay đổi ERD và ràng buộc

| Thành phần | Baseline | Bản đề xuất |
|---|---|---|
| `users` | 11 cột | 12 cột: thêm `role VARCHAR(10) NN DEFAULT 'USER'`, CHECK USER/ADMIN. |
| `activity_logs.session_id` | `VARCHAR(64) NULL` | `UUID NN UK`; UNIQUE đơn `uq_activity_session`. |
| `conversation_clears` | 3 cột, timestamp quyết định visibility | 4 cột: thêm `last_cleared_message_id BIGINT NN DEFAULT 0`, CHECK ≥0; timestamp chỉ tra cứu; cột mới không FK. |
| `group_members` | 5 cột gồm encrypted_group_key | 4 cột, bỏ encrypted_group_key theo mô hình Server. |
| `chat_groups` | flag/key chưa được ràng buộc | Cột giữ nguyên; CHECK flag–key, trigger mode bất biến. |
| `group_messages` | iv nullable chưa có unique | Cột giữ nguyên; CHECK IV Base64 12 byte; partial UNIQUE `(group_id,iv)`; trigger mode/group/sender. |
| Index/sequence direct | `idx_dm_pair_time`, identity mặc định | Thay bằng `idx_dm_pair_id` với cùng hai biểu thức cặp rồi `id`; giữ CACHE 1, increment 1, no cycle. Không âm thầm giữ cả hai index. |
| PK/FK/ON DELETE | 13 PK, 21 FK | Giữ nguyên; không có FK mới từ watermark và không đổi cascade/restrict. |

Các bảng còn lại giữ nguyên: login_history, friend_requests, friendships, user_blocks, direct_messages (trừ index và việc ghi rõ CACHE 1), message_hidden, spam_reports. Giữ bốn index tường minh còn lại từ dự thảo: idx_login_history_time, uq_pending_friend_pair, idx_group_messages_time, idx_spam_reports_time. Cộng index thay thế và unique IV là **6 CREATE INDEX tường minh** trong phương án; UNIQUE session là constraint có index hỗ trợ riêng. Các PK/UNIQUE sẵn có vẫn tạo index hỗ trợ như cũ.

Mermaid chỉ gắn UK cho UNIQUE **đơn**; không gắn UK riêng lên group_id/iv của unique ghép có điều kiện. CHECK/trigger/advisory lock/index biểu thức không phải quan hệ FK, nên không vẽ đường giả. Những cột/NULL/default không được liệt kê thay đổi đều giữ nguyên, kể cả chính sách ON DELETE.

## 7. Các quyết định còn chờ và mức sẵn sàng viết SQL

| Cần xác nhận | Người xác nhận | Mặc định đã chọn để làm tiếp sau phê duyệt |
|---|---|---|
| Server encryption có đủ §12g hay phải E2EE | Giảng viên, sau đó A/B | PROVISIONAL Server-side AES-GCM; không quảng bá E2EE. |
| Phiên thật, logout/resume, thống kê gồm hai Client | A/B | Theo §2; UUID Client tạo là idempotency, không là auth. |
| Clear theo lượt transaction, mọi writer phải lấy khóa trước ID | A/B | Theo §3; timestamp không quyết định visibility. |
| Mode mã hóa bất biến; một DEK; lịch sử thành viên mới; bỏ cột dự phòng | A/B | Theo §4; rotate KEK bằng rewrap, DEK chỉ bảo trì full re-encryption. |
| Role, bootstrap, bảo vệ Admin cuối, quyền hai UI và opcode | A/B | Theo §5; mọi API dùng chung phải review trước code. |
| P1 khác trong ERD_Review | A/B | Vẫn phải chốt transaction friend/block/spam, chính sách từng ON DELETE, cách đếm bạn cấp hai, username/email hoa thường và bộ index cuối. Không coi bốn đề xuất này là đã nghiệm thu toàn bộ schema. |

**Kết luận sẵn sàng:** đã có bốn phương án kỹ thuật cụ thể để chuyển sang DDL sau khi A/B duyệt. **Chưa sẵn sàng coi database.sql là thiết kế chốt cuối** vì phạm vi mã hóa còn PROVISIONAL, thay đổi hợp đồng chưa được B duyệt và các P1 ngoài bốn vấn đề vẫn còn. Có thể bắt đầu bản SQL nháp có đánh dấu giả định khi người dùng yêu cầu ở bước sau; trong tác vụ này không tạo file SQL.

## 8. Kiểm tra tài liệu và giới hạn xác minh

Bộ đầu ra: [Database_Decisions_Proposed.md](Database_Decisions_Proposed.md), [ERD_Proposed.mmd](ERD_Proposed.mmd), [ERD_Proposed.svg](ERD_Proposed.svg), [ERD_Proposed.png](ERD_Proposed.png).

Đã kiểm tra thành công:

| Kiểm tra thực tế | Kết quả |
|---|---|
| Mermaid chính thức có sẵn | `mermaid.parse`, `getDiagramFromText`, `render` chạy thành công bằng bundle Mermaid của VS Code; không tải/cài renderer. |
| Mô hình sau parse | 13 bảng, 65 cột, 21 FK; 8 PK đơn và 5 PK ghép; 3 UNIQUE đơn (username, email, session_id). Có 58 cột bắt buộc và 7 nullable. |
| Đối chiếu baseline | Chỉ thêm role/watermark, sửa session_id, bỏ encrypted_group_key; các kiểu và PK/FK/UK còn lại khớp. 21 đường FK, cardinality và ON DELETE không đổi. Metadata bổ sung đúng các giả định ở §6. |
| Hình ảnh | SVG 2857 × 2673, hợp lệ XML, không dùng foreignObject; PNG 5714 × 5346 pixel, mở/giải mã được. Đã xem toàn cảnh và chi tiết các cột mới/nhóm: font Arial, nền sáng, chữ không bị cắt. |
| Tài liệu | 12 khối SQL minh họa có code fence cân bằng; link file nội bộ tồn tại; không ký tự lỗi Unicode hoặc khoảng trắng cuối dòng. |
| Phạm vi | SHA-256 của 10 file nguồn, schema và ERD/hình gốc không đổi. Chỉ giữ 4 đầu ra đề xuất; file kiểm tra/render tạm được dọn sau xác minh. |

Renderer dùng Node/Playwright/Chromium và Mermaid có sẵn trong môi trường, chỉ nạp module cài đặt qua localhost. Không đưa schema lên dịch vụ render bên ngoài. Sơ đồ tổng quan vẫn cần xem phóng to khi chèn báo cáo; chưa kiểm tra nhập SVG trong Word. Constraint ghép/có điều kiện, index biểu thức và trigger không được Mermaid thực thi; tài liệu này là nguồn mô tả phần đó.

Chưa có PostgreSQL/pgJDBC đang được kiểm thử trong phạm vi này. **12 khối SQL chưa được chạy hoặc kiểm chứng bằng parser PostgreSQL**, chưa chạy test trên DB rỗng/concurrency/AEAD/Server; trigger/khóa cần kiểm thử tích hợp trước phát hành. Danh sách test ở từng quyết định là test cần triển khai, chưa phải test đã chạy. Không cài dependencies mới, không tạo/sửa database.sql hoặc Java, không commit/push, không sửa tài liệu chung hay schema/ERD gốc.
