# SCHEMA CONTEXT & HƯỚNG DẪN SỬ DỤNG SCHEMA CHUẨN MỰC

> **Mục tiêu:** Hướng dẫn các lập trình viên (Thành viên A, Thành viên B và các AI Assistants) cách tương tác đúng nhất với cơ sở dữ liệu PostgreSQL qua tầng Java Server (pgJDBC).  
> **Nguyên tắc cốt lõi:**  
> 1. Java Server là nơi duy nhất giữ kết nối CSDL và các thông tin bí mật (Master Key KEK, Email Password, LLM API Key).  
> 2. Không bao giờ tin tưởng dữ liệu Client gửi lên; luôn lấy danh tính (`user_id`, `role`) từ phiên đã xác thực.  
> 3. Mọi thao tác ảnh hưởng nhiều bảng phải được bọc trong một **Database Transaction** (`conn.setAutoCommit(false)`).

---

## 1. Xác thực & Quản lý Phiên Hoạt động

### 1.1 Đăng ký & Đăng nhập
- **Đăng ký (`REGISTER`)**:
  - Luôn mã hóa mật khẩu bằng BCrypt: `String hash = BCrypt.hashpw(plainPass, BCrypt.gensalt());`
  - Trường `role` luôn gán cố định là `'USER'`. Nghiêm cấm Client tự gửi role hoặc status để chiếm quyền.
- **Đăng nhập (`LOGIN`)**:
  - Truy vấn người dùng theo `username`:
    ```sql
    SELECT id, password_hash, status, role FROM users WHERE LOWER(username) = LOWER(?);
    ```
  - Kiểm tra mật khẩu: `BCrypt.checkpw(inputPass, hashFromDB)`.
  - Kiểm tra trạng thái: Nếu `status = 'LOCKED'` trả lỗi `ACCOUNT_LOCKED`; nếu `status = 'DELETED'` trả lỗi `ACCOUNT_NOT_FOUND`.
  - Phân quyền theo `role`: Ứng dụng `Admin.jar` chỉ cho phép truy cập nếu `role = 'ADMIN'`.

### 1.2 Ghi nhận phiên mở ứng dụng (`OPEN_APP`) không trùng lặp
- **Quy tắc sinh Session ID**:
  - Khi Client đăng nhập thành công, Client sinh một UUID v4 ngẫu nhiên (`UUID.randomUUID()`) và lưu trong bộ nhớ tiến trình của phiên làm việc đó.
- **Ghi nhận vào CSDL**:
  - Khi bắt đầu phiên, Server thực thi câu lệnh SQL với cơ chế `ON CONFLICT DO NOTHING`:
    ```sql
    INSERT INTO activity_logs (user_id, action_type, session_id, created_at)
    VALUES (?, 'OPEN_APP', ?, NOW())
    ON CONFLICT (session_id) DO NOTHING;
    ```
  - Đồng thời ghi 1 dòng vào `login_history`:
    ```sql
    INSERT INTO login_history (user_id, login_time) VALUES (?, NOW());
    ```
- **Xử lý Reconnect**:
  - Khi mất mạng và Socket tự động kết nối lại (`RESUME_SESSION`), Client gửi kèm UUID cũ của phiên hiện tại.
  - Nhờ ràng buộc `UNIQUE (session_id)`, DB sẽ tự động bỏ qua (không tạo thêm bản ghi mới), giữ nguyên số liệu thống kê mở ứng dụng chính xác 100%.

---

## 2. Quản lý Bạn bè & Chặn (Block)

### 2.1 Gửi lời mời kết bạn
- Kiểm tra điều kiện ở Server:
  1. `sender_id != receiver_id` (không tự kết bạn).
  2. Hai bên không nằm trong bảng `user_blocks` (không bị chặn theo cả 2 chiều).
  3. Chưa là bạn bè trong bảng `friendships`.
- Thực thi:
  ```sql
  INSERT INTO friend_requests (sender_id, receiver_id, status)
  VALUES (?, ?, 'PENDING');
  ```
- *Lưu ý:* Nếu bên kia đã gửi lời mời đang chờ (`PENDING`), partial unique index `uq_pending_friend_pair` sẽ tự động chặn việc gửi lặp.

### 2.2 Đồng ý kết bạn (`ACCEPT`)
Thực hiện trong **một Transaction**:
```sql
-- 1. Cập nhật trạng thái lời mời
UPDATE friend_requests SET status = 'ACCEPTED'
WHERE id = ? AND receiver_id = ? AND status = 'PENDING';

-- 2. Thêm vào bảng friendships với quy tắc user_id_1 < user_id_2
INSERT INTO friendships (user_id_1, user_id_2, created_at)
VALUES (LEAST(?, ?), GREATEST(?, ?), NOW())
ON CONFLICT (user_id_1, user_id_2) DO NOTHING;
```

### 2.3 Chặn tài khoản (`BLOCK`)
Khi User A chặn User B, thực hiện trong **một Transaction**:
```sql
-- 1. Ghi nhận vào bảng chặn
INSERT INTO user_blocks (blocker_id, blocked_id, created_at)
VALUES (?, ?, NOW())
ON CONFLICT (blocker_id, blocked_id) DO NOTHING;

-- 2. Hủy quan hệ bạn bè nếu đang là bạn
DELETE FROM friendships
WHERE user_id_1 = LEAST(?, ?) AND user_id_2 = GREATEST(?, ?);

-- 3. Hủy mọi lời mời kết bạn đang chờ giữa 2 người
UPDATE friend_requests SET status = 'REJECTED'
WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))
  AND status = 'PENDING';
```

### 2.4 Tìm kiếm người dùng (Không thấy người đã block mình)
```sql
SELECT id, username, full_name, last_seen_at
FROM users
WHERE status = 'ACTIVE'
  AND id != :my_id
  AND (LOWER(username) LIKE LOWER(:keyword) OR LOWER(full_name) LIKE LOWER(:keyword))
  AND id NOT IN (
    -- Loại trừ những người đã chặn tôi
    SELECT blocker_id FROM user_blocks WHERE blocked_id = :my_id
  );
```

---

## 3. Tin nhắn Cá nhân (1-1) & Xóa Lịch sử Chuẩn xác

### 3.1 Khóa giao dịch Advisory Lock theo cặp
Để tránh xung đột giữa luồng đang gửi tin nhắn và luồng đang bấm xóa toàn bộ lịch sử, Server sử dụng **PostgreSQL Transaction Advisory Lock**:
- Key khóa: Tính từ chuỗi `dm:<min_id>:<max_id>` băm SHA-256 lấy số nguyên `BIGINT` dương.
- Câu lệnh khóa:
  ```sql
  SELECT pg_advisory_xact_lock(:dm_lock_key);
  ```

### 3.2 Gửi tin nhắn 1-1
```sql
BEGIN;
SELECT pg_advisory_xact_lock(:dm_lock_key);

-- Kiểm tra xem người nhận có block người gửi không trước khi insert
INSERT INTO direct_messages (sender_id, receiver_id, content, created_at)
VALUES (?, ?, ?, NOW())
RETURNING id, created_at;

COMMIT;
```

### 3.3 Xóa toàn bộ lịch sử chat một phía (`CLEAR_CONVERSATION`)
```sql
BEGIN;
SELECT pg_advisory_xact_lock(:dm_lock_key);

-- 1. Lấy ID tin nhắn lớn nhất đã commit tại thời điểm này
SELECT COALESCE(MAX(id), 0) AS cutoff
FROM direct_messages
WHERE (sender_id = :user_id AND receiver_id = :partner_id)
   OR (sender_id = :partner_id AND receiver_id = :user_id);

-- 2. Ghi mốc watermark (last_cleared_message_id) cho phía người bấm xóa
INSERT INTO conversation_clears (user_id, partner_id, last_cleared_message_id, cleared_at)
VALUES (:user_id, :partner_id, :cutoff, clock_timestamp())
ON CONFLICT (user_id, partner_id) DO UPDATE SET
  last_cleared_message_id = GREATEST(conversation_clears.last_cleared_message_id, EXCLUDED.last_cleared_message_id),
  cleared_at = EXCLUDED.cleared_at;

COMMIT;
```

### 3.4 Xóa lẻ 1 hoặc vài dòng tin nhắn
Chỉ người trong cuộc trò chuyện mới được ẩn tin nhắn phía mình:
```sql
INSERT INTO message_hidden (message_id, user_id, hidden_at)
VALUES (?, :my_id, NOW())
ON CONFLICT (message_id, user_id) DO NOTHING;
```

### 3.5 Tải lịch sử chat 1-1 hiển thị cho người dùng
Chỉ lấy các tin nhắn **lớn hơn mốc xóa toàn bộ** VÀ **chưa bị xóa lẻ**:
```sql
SELECT m.id, m.sender_id, m.receiver_id, m.content, m.is_read, m.created_at
FROM direct_messages m
LEFT JOIN conversation_clears c
  ON c.user_id = :my_id AND c.partner_id = :partner_id
WHERE ((m.sender_id = :my_id AND m.receiver_id = :partner_id)
    OR (m.sender_id = :partner_id AND m.receiver_id = :my_id))
  AND m.id > COALESCE(c.last_cleared_message_id, 0)
  AND NOT EXISTS (
    SELECT 1 FROM message_hidden h
    WHERE h.message_id = m.id AND h.user_id = :my_id
  )
ORDER BY m.id ASC;
```

---

## 4. Chat Nhóm & Cơ chế Mã hóa AES-GCM

### 4.1 Tạo nhóm chat mới
Thực hiện trong **một Transaction**:
1. Nếu nhóm thường: `is_encrypted = FALSE`, `wrapped_encryption_key = NULL`.
2. Nếu nhóm mã hóa:
   - Server sinh ngẫu nhiên 1 khóa DEK AES-256 (`KeyGenerator`).
   - Bọc (Wrap) DEK bằng Master Key KEK của Server dùng thuật toán `AESWrap` (`AES/KW/NoPadding`).
   - Chuỗi envelope lưu dạng: `kw1:kek_v1:<base64_wrapped_dek>`.
3. Chèn nhóm vào `chat_groups`.
4. Thêm người tạo làm Admin đầu tiên vào `group_members` (`is_admin = TRUE`).
5. Thêm các thành viên ban đầu vào `group_members` (`is_admin = FALSE`).

### 4.2 Gửi tin nhắn trong nhóm mã hóa
- Server kiểm tra người gửi hiện tại phải là thành viên trong `group_members`.
- Sinh IV ngẫu nhiên 12 byte (96-bit). Mã hóa Base64 thành chuỗi 16 ký tự.
- Gán AAD (Additional Authenticated Data) để bảo vệ ngữ cảnh: `chatapp-gm-v1:<group_id>:<sender_id>`.
- Mã hóa nội dung bằng `AES/GCM/NoPadding` với khóa DEK của nhóm.
- Ghép Ciphertext và Authentication Tag (128-bit) rồi chuyển sang chuỗi Base64.
- Chèn vào CSDL:
  ```sql
  INSERT INTO group_messages (group_id, sender_id, content, iv, created_at)
  VALUES (?, ?, :base64_ciphertext_with_tag, :base64_iv_16_chars, NOW());
  ```

---

## 5. Các câu Truy vấn Nghiệp vụ Phức tạp cho Admin

### 5.1 Thống kê "Bạn của bạn" (Friends of Friends - Admin Yêu cầu 7)
Đếm số bạn trực tiếp và số "bạn của bạn" (bậc 2), **không đếm trùng, loại trừ bạn trực tiếp và loại trừ chính bản thân**:

```sql
WITH direct_friends AS (
    -- Tập hợp tất cả các cặp bạn bè trực tiếp của mọi người
    SELECT user_id_1 AS u_id, user_id_2 AS friend_id FROM friendships
    UNION
    SELECT user_id_2 AS u_id, user_id_1 AS friend_id FROM friendships
),
fof_raw AS (
    -- Bạn của bạn: Từ bạn trực tiếp tìm tiếp bạn của họ
    SELECT df1.u_id AS user_id, df2.friend_id AS fof_id
    FROM direct_friends df1
    JOIN direct_friends df2 ON df1.friend_id = df2.u_id
    WHERE df2.friend_id != df1.u_id -- Không tính chính mình
      AND df2.friend_id NOT IN (    -- Không tính những ai đã là bạn trực tiếp
          SELECT friend_id FROM direct_friends WHERE u_id = df1.u_id
      )
)
SELECT 
    u.id, 
    u.username, 
    u.full_name, 
    u.created_at,
    COUNT(DISTINCT df.friend_id) AS direct_friends_count,
    COUNT(DISTINCT fof.fof_id) AS friends_of_friends_count
FROM users u
LEFT JOIN direct_friends df ON u.id = df.u_id
LEFT JOIN fof_raw fof ON u.id = fof.user_id
WHERE u.status != 'DELETED'
GROUP BY u.id, u.username, u.full_name, u.created_at
ORDER BY u.created_at DESC;
```

### 5.2 Thống kê người dùng hoạt động trong khoảng thời gian (Admin Yêu cầu 8)
Hiển thị danh sách người dùng kèm 3 số liệu: Lượt mở app, Số người đã chat, Số nhóm đã chat trong khoảng thời gian `[:from_time, :to_time)`:

```sql
SELECT 
    u.id,
    u.username,
    u.full_name,
    -- 1. Số lượt mở ứng dụng (Số phiên thực tế)
    COALESCE(act.open_app_count, 0) AS open_app_count,
    -- 2. Đã chủ động chat với bao nhiêu người
    COALESCE(dm.chat_partners_count, 0) AS chat_partners_count,
    -- 3. Đã chủ động chat trong bao nhiêu nhóm
    COALESCE(gm.chat_groups_count, 0) AS chat_groups_count
FROM users u
LEFT JOIN (
    SELECT user_id, COUNT(*) AS open_app_count
    FROM activity_logs
    WHERE action_type = 'OPEN_APP' 
      AND created_at >= :from_time AND created_at < :to_time
    GROUP BY user_id
) act ON u.id = act.user_id
LEFT JOIN (
    SELECT sender_id, COUNT(DISTINCT receiver_id) AS chat_partners_count
    FROM direct_messages
    WHERE created_at >= :from_time AND created_at < :to_time
    GROUP BY sender_id
) dm ON u.id = dm.sender_id
LEFT JOIN (
    SELECT sender_id, COUNT(DISTINCT group_id) AS chat_groups_count
    FROM group_messages
    WHERE created_at >= :from_time AND created_at < :to_time
    GROUP BY sender_id
) gm ON u.id = gm.sender_id
WHERE (COALESCE(act.open_app_count, 0) > 0 
    OR COALESCE(dm.chat_partners_count, 0) > 0 
    OR COALESCE(gm.chat_groups_count, 0) > 0)
ORDER BY u.id ASC;
```

### 5.3 Biểu đồ người hoạt động theo 12 tháng trong năm (Admin Yêu cầu 9)
Vẽ biểu đồ với trục hoành là 12 tháng, trục tung là **số người có mở ứng dụng** (`COUNT(DISTINCT user_id)`):

```sql
WITH months AS (
    SELECT generate_series(
        DATE_TRUNC('year', :chosen_year_date::timestamptz),
        DATE_TRUNC('year', :chosen_year_date::timestamptz) + interval '11 months',
        interval '1 month'
    ) AS m_start
)
SELECT 
    EXTRACT(MONTH FROM m.m_start)::int AS month,
    COUNT(DISTINCT a.user_id) AS active_users_count
FROM months m
LEFT JOIN activity_logs a 
  ON a.action_type = 'OPEN_APP'
 AND a.created_at >= m.m_start 
 AND a.created_at < m.m_start + interval '1 month'
GROUP BY m.m_start
ORDER BY m.m_start ASC;
```

---

## 6. Cơ chế Bảo vệ Tài khoản Admin Cuối cùng

Để ngăn chặn lỗi quản trị làm mất toàn bộ quyền điều khiển hệ thống (ví dụ: tự khóa hoặc xóa tài khoản Admin duy nhất còn lại), Server áp dụng **Khóa Advisory Lock toàn cục `-1::bigint`** trước khi đổi role hoặc status:

```sql
BEGIN;
-- Khóa toàn cục chống race condition giữa 2 Admin thao tác cùng lúc
SELECT pg_advisory_xact_lock(-1::bigint);

-- Cập nhật trạng thái / role
UPDATE users SET role = :new_role, status = :new_status WHERE id = :target_id;

-- Kiểm tra số lượng Admin ACTIVE còn lại
SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE';
-- NẾU count < 1:
--   Server thực thi ROLLBACK;
--   Trả lỗi về UI: "Không thể khóa/hạ quyền Admin hoạt động cuối cùng của hệ thống!"
-- NẾU count >= 1:
--   COMMIT;
```
