# TÀI LIỆU CƠ SỞ DỮ LIỆU TOÀN DIỆN — JAVA CHAT SYSTEM

> **Phiên bản:** 3.1 (Unified Architecture & Implementation Guide).  
> **Hệ quản trị CSDL:** PostgreSQL 16+ (Local port 5432).  
> **Quy mô:** 13 bảng, 65 cột, 21 khóa ngoại (FK), 6 chỉ mục (Index) tường minh, 11 ràng buộc CHECK, 2 triggers bảo vệ tính toàn vẹn.  
> **Tệp DDL thực thi:** `script/database.sql` (Chạy tạo CSDL từ database rỗng).

---

## 1. Sơ đồ Quan hệ Thực thể (ERD — Entity Relationship Diagram)

```mermaid
%%{init: {"theme":"base","fontFamily":"Arial","htmlLabels":false,"themeVariables":{"background":"#ffffff","primaryColor":"#edf4fc","primaryTextColor":"#172b45","primaryBorderColor":"#50749a","lineColor":"#62758b","fontSize":"16px"},"er":{"diagramPadding":30,"entityPadding":14,"minEntityWidth":180,"minEntityHeight":70,"nodeSpacing":70,"rankSpacing":240,"useMaxWidth":false}}}%%
erDiagram
    direction LR

    users {
        BIGINT id PK "NN; IDENTITY"
        VARCHAR(50) username UK "NN"
        VARCHAR(255) password_hash "NN"
        VARCHAR(100) full_name "NN"
        TEXT address "NULL"
        DATE dob "NULL"
        VARCHAR(10) gender "NULL"
        VARCHAR(100) email UK "NN"
        VARCHAR(20) status "NN; default 'ACTIVE'"
        VARCHAR(10) role "NN; default 'USER'; USER or ADMIN"
        TIMESTAMPTZ last_seen_at "NULL"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    login_history {
        BIGINT id PK "NN; IDENTITY"
        BIGINT user_id FK "NN"
        TIMESTAMPTZ login_time "NN; default NOW()"
    }

    activity_logs {
        BIGINT id PK "NN; IDENTITY"
        BIGINT user_id FK "NN"
        VARCHAR(30) action_type "NN; default 'OPEN_APP'"
        UUID session_id UK "NN; one OPEN_APP per session"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    friend_requests {
        BIGINT id PK "NN; IDENTITY"
        BIGINT sender_id FK "NN"
        BIGINT receiver_id FK "NN"
        VARCHAR(20) status "NN; default 'PENDING'"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    friendships {
        BIGINT user_id_1 PK, FK "NN"
        BIGINT user_id_2 PK, FK "NN"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    user_blocks {
        BIGINT blocker_id PK, FK "NN"
        BIGINT blocked_id PK, FK "NN"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    direct_messages {
        BIGINT id PK "NN; IDENTITY; CACHE 1"
        BIGINT sender_id FK "NN"
        BIGINT receiver_id FK "NN"
        TEXT content "NN"
        BOOLEAN is_read "NN; default FALSE"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    message_hidden {
        BIGINT message_id PK, FK "NN"
        BIGINT user_id PK, FK "NN"
        TIMESTAMPTZ hidden_at "NN; default NOW()"
    }

    conversation_clears {
        BIGINT user_id PK, FK "NN"
        BIGINT partner_id PK, FK "NN"
        BIGINT last_cleared_message_id "NN; default 0; watermark, not FK"
        TIMESTAMPTZ cleared_at "NN; audit only"
    }

    chat_groups {
        BIGINT id PK "NN; IDENTITY"
        VARCHAR(100) group_name "NN"
        BIGINT created_by FK "NN"
        BOOLEAN is_encrypted "NN; default FALSE; immutable"
        TEXT wrapped_encryption_key "NULL; AES-KW envelope if encrypted"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    group_members {
        BIGINT group_id PK, FK "NN"
        BIGINT user_id PK, FK "NN"
        BOOLEAN is_admin "NN; default FALSE"
        TIMESTAMPTZ joined_at "NN; default NOW()"
    }

    group_messages {
        BIGINT id PK "NN; IDENTITY"
        BIGINT group_id FK "NN"
        BIGINT sender_id FK "NN"
        TEXT content "NN"
        VARCHAR(64) iv "NULL; 12-byte nonce if encrypted"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    spam_reports {
        BIGINT id PK "NN; IDENTITY"
        BIGINT reporter_id FK "NN"
        BIGINT reported_id FK "NN"
        TEXT reason "NULL"
        TIMESTAMPTZ created_at "NN; default NOW()"
    }

    users ||..o{ login_history : "user_id / RESTRICT"
    users ||..o{ activity_logs : "user_id / RESTRICT"
    users ||..o{ friend_requests : "sender_id / CASCADE"
    users ||..o{ friend_requests : "receiver_id / CASCADE"
    users ||--o{ friendships : "user_id_1 / CASCADE"
    users ||--o{ friendships : "user_id_2 / CASCADE"
    users ||--o{ user_blocks : "blocker_id / CASCADE"
    users ||--o{ user_blocks : "blocked_id / CASCADE"
    users ||..o{ direct_messages : "sender_id / RESTRICT"
    users ||..o{ direct_messages : "receiver_id / RESTRICT"
    direct_messages ||--o{ message_hidden : "message_id / CASCADE"
    users ||--o{ message_hidden : "user_id / CASCADE"
    users ||--o{ conversation_clears : "user_id / CASCADE"
    users ||--o{ conversation_clears : "partner_id / CASCADE"
    users ||..o{ chat_groups : "created_by / RESTRICT"
    chat_groups ||--o{ group_members : "group_id / CASCADE"
    users ||--o{ group_members : "user_id / RESTRICT"
    chat_groups ||..o{ group_messages : "group_id / CASCADE"
    users ||..o{ group_messages : "sender_id / RESTRICT"
    users ||..o{ spam_reports : "reporter_id / RESTRICT"
    users ||..o{ spam_reports : "reported_id / RESTRICT"
```

---

## 2. Đặc tả Cấu trúc 13 Bảng & Ràng buộc

### Nhóm 1: Quản lý Người dùng & Hoạt động (3 bảng)

#### 1. Bảng `users` (Hồ sơ người dùng & Phân quyền)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Khóa chính tự tăng |
| `username` | `VARCHAR(50)` | `NOT NULL` | `UNIQUE` | Tên đăng nhập duy nhất |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | | Chuỗi băm mật khẩu BCrypt |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | | Họ và tên hiển thị |
| `address` | `TEXT` | `NULL` | | Địa chỉ |
| `dob` | `DATE` | `NULL` | | Ngày sinh |
| `gender` | `VARCHAR(10)` | `NULL` | `CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))` | Giới tính |
| `email` | `VARCHAR(100)` | `NOT NULL` | `UNIQUE` | Email duy nhất (nhận mật khẩu ngẫu nhiên) |
| `status` | `VARCHAR(20)` | `NOT NULL` | `DEFAULT 'ACTIVE'`, `CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED'))` | Trạng thái tài khoản |
| `role` | `VARCHAR(10)` | `NOT NULL` | `DEFAULT 'USER'`, `CHECK (role IN ('USER', 'ADMIN'))` | Phân quyền truy cập UI |
| `last_seen_at` | `TIMESTAMPTZ` | `NULL` | | Thời điểm hoạt động cuối |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm tạo tài khoản |

#### 2. Bảng `login_history` (Lịch sử đăng nhập)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Khóa chính tự tăng |
| `user_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Người dùng đăng nhập |
| `login_time` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm đăng nhập |

#### 3. Bảng `activity_logs` (Nhật ký mở ứng dụng theo phiên)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Khóa chính tự tăng |
| `user_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Người dùng thực hiện |
| `action_type` | `VARCHAR(30)` | `NOT NULL` | `DEFAULT 'OPEN_APP'`, `CHECK (action_type IN ('OPEN_APP'))` | Loại hành động |
| `session_id` | `UUID` | `NOT NULL` | `UNIQUE` | Khóa chống trùng lặp phiên (Idempotency key) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm mở ứng dụng |

---

### Nhóm 2: Bạn bè & Chặn người dùng (3 bảng)

#### 4. Bảng `friend_requests` (Lời mời kết bạn)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Khóa chính tự tăng |
| `sender_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | Người gửi lời mời |
| `receiver_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | Người nhận lời mời |
| `status` | `VARCHAR(20)` | `NOT NULL` | `DEFAULT 'PENDING'`, `CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))` | Trạng thái lời mời |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm gửi |
- **Ràng buộc:** `CHECK (sender_id <> receiver_id)`.
- **Chỉ mục chống trùng gửi 2 chiều:** `UNIQUE INDEX uq_pending_friend_pair ON friend_requests (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) WHERE status = 'PENDING'`.

#### 5. Bảng `friendships` (Quan hệ bạn bè chính thức)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `user_id_1` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | ID người dùng nhỏ hơn |
| `user_id_2` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | ID người dùng lớn hơn |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm kết bạn |
- **Khóa chính:** `PRIMARY KEY (user_id_1, user_id_2)`.
- **Ràng buộc chuẩn hóa:** `CHECK (user_id_1 < user_id_2)`.

#### 6. Bảng `user_blocks` (Danh sách chặn tài khoản)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `blocker_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | Người thực hiện chặn |
| `blocked_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | Người bị chặn |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm chặn |
- **Khóa chính:** `PRIMARY KEY (blocker_id, blocked_id)`.
- **Ràng buộc:** `CHECK (blocker_id <> blocked_id)`.

---

### Nhóm 3: Tin nhắn Cá nhân (1-1) & Xóa Lịch sử (3 bảng)

#### 7. Bảng `direct_messages` (Tin nhắn trực tiếp 1-1)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY (CACHE 1) PRIMARY KEY` | Khóa chính tự tăng tuần tự |
| `sender_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Người gửi tin |
| `receiver_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Người nhận tin |
| `content` | `TEXT` | `NOT NULL` | | Nội dung tin nhắn văn bản |
| `is_read` | `BOOLEAN` | `NOT NULL` | `DEFAULT FALSE` | Cờ đã xem tin nhắn |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm gửi |
- **Ràng buộc:** `CHECK (sender_id <> receiver_id)`.

#### 8. Bảng `message_hidden` (Xóa lẻ 1 hoặc vài dòng tin nhắn)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `message_id` | `BIGINT` | `NOT NULL` | `REFERENCES direct_messages(id) ON DELETE CASCADE` | Tin nhắn bị ẩn |
| `user_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | Người thực hiện ẩn phía mình |
| `hidden_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm ẩn |
- **Khóa chính:** `PRIMARY KEY (message_id, user_id)`.

#### 9. Bảng `conversation_clears` (Mốc xóa toàn bộ cuộc trò chuyện 1 phía)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `user_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | Người bấm xóa lịch sử |
| `partner_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE CASCADE` | Người đối thoại trong cuộc chat |
| `last_cleared_message_id` | `BIGINT` | `NOT NULL` | `DEFAULT 0`, `CHECK (last_cleared_message_id >= 0)` | Mốc ID tin nhắn cao nhất đã xóa |
| `cleared_at` | `TIMESTAMPTZ` | `NOT NULL` | | Thời điểm xóa (dùng audit/hiển thị) |
- **Khóa chính:** `PRIMARY KEY (user_id, partner_id)`.
- **Ràng buộc:** `CHECK (user_id <> partner_id)`.

---

### Nhóm 4: Nhóm chat & Mã hóa (3 bảng)

#### 10. Bảng `chat_groups` (Nhóm chat)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Khóa chính tự tăng |
| `group_name` | `VARCHAR(100)` | `NOT NULL` | | Tên nhóm chat |
| `created_by` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Người tạo nhóm |
| `is_encrypted` | `BOOLEAN` | `NOT NULL` | `DEFAULT FALSE` | Cờ kích hoạt mã hóa AES-GCM (Bất biến) |
| `wrapped_encryption_key` | `TEXT` | `NULL` | | Envelope khóa DEK bọc bởi KEK |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm tạo nhóm |
- **Ràng buộc:** `CHECK ((is_encrypted AND wrapped_encryption_key IS NOT NULL) OR (NOT is_encrypted AND wrapped_encryption_key IS NULL))`.

#### 11. Bảng `group_members` (Thành viên nhóm chat)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `group_id` | `BIGINT` | `NOT NULL` | `REFERENCES chat_groups(id) ON DELETE CASCADE` | Nhóm chat tham gia |
| `user_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Thành viên tham gia |
| `is_admin` | `BOOLEAN` | `NOT NULL` | `DEFAULT FALSE` | Quyền Admin trong nhóm chat |
| `joined_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm gia nhập nhóm |
- **Khóa chính:** `PRIMARY KEY (group_id, user_id)`.

#### 12. Bảng `group_messages` (Tin nhắn trong nhóm chat)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Khóa chính tự tăng |
| `group_id` | `BIGINT` | `NOT NULL` | `REFERENCES chat_groups(id) ON DELETE CASCADE` | Nhóm nhận tin nhắn |
| `sender_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Thành viên gửi tin |
| `content` | `TEXT` | `NOT NULL` | | Plaintext UTF-8 hoặc Base64(Ciphertext\|\|Tag) |
| `iv` | `VARCHAR(64)` | `NULL` | | Base64 của IV 12 byte (Null nếu nhóm thường) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm gửi tin |
- **Ràng buộc:** `CHECK (iv IS NULL OR iv ~ '^[A-Za-z0-9+/]{16}$')`.
- **Chỉ mục duy nhất:** `UNIQUE (group_id, iv) WHERE iv IS NOT NULL`.

---

### Nhóm 5: Báo cáo Spam (1 bảng)

#### 13. Bảng `spam_reports` (Báo cáo vi phạm / làm phiền)
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc / Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `NOT NULL` | `GENERATED ALWAYS AS IDENTITY PRIMARY KEY` | Khóa chính tự tăng |
| `reporter_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Người gửi báo cáo |
| `reported_id` | `BIGINT` | `NOT NULL` | `REFERENCES users(id) ON DELETE RESTRICT` | Người bị báo cáo vi phạm |
| `reason` | `TEXT` | `NULL` | | Lý do báo cáo |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | `DEFAULT NOW()` | Thời điểm tạo báo cáo |
- **Ràng buộc:** `CHECK (reporter_id <> reported_id)`.

---

## 3. Danh sách Chỉ mục (Indexes) & Triggers

### 3.1 Chỉ mục Tường minh hỗ trợ Tối ưu Truy vấn
- `idx_login_history_time`: `login_history(login_time DESC)` — Xem lịch sử đăng nhập theo thời gian.
- `uq_pending_friend_pair`: `friend_requests(LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) WHERE status = 'PENDING'` — Chống lặp lời mời kết bạn 2 chiều.
- `idx_dm_pair_id`: `direct_messages(LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), id)` — Tải lịch sử chat 1-1 cực nhanh theo mốc watermark.
- `idx_group_messages_time`: `group_messages(group_id, created_at)` — Tải lịch sử chat nhóm theo thời gian.
- `uq_group_message_iv`: `group_messages(group_id, iv) WHERE iv IS NOT NULL` — Chống tái sử dụng Nonce AES-GCM.
- `idx_spam_reports_time`: `spam_reports(created_at DESC)` — Admin xem báo cáo spam mới nhất.

### 3.2 Triggers Bảo vệ Toàn vẹn Dữ liệu
1. `trg_group_mode_immutable`: Ngăn chặn tuyệt đối việc thay đổi cờ `is_encrypted` của nhóm sau khi tạo.
2. `trg_group_message_mode`: Đảm bảo tin nhắn nhóm gửi vào phải khớp đúng chế độ mã hóa (nhóm mã hóa bắt buộc có IV hợp lệ, nhóm thường IV phải NULL).

---

## 4. Cẩm nang Vận hành & Lập trình Tầng Server (Backend Implementation Guide)

### 4.1 Xác thực & Quản lý Phiên Mở Ứng Dụng
- **Đăng ký:** Mật khẩu băm BCrypt (`BCrypt.hashpw`), gán cố định `role = 'USER'`.
- **Đăng nhập:** Kiểm tra BCrypt, kiểm tra `status = 'ACTIVE'`, phân quyền theo `role` (Admin UI chỉ cho phép `role = 'ADMIN'`).
- **Ghi nhận `OPEN_APP` không trùng lặp:**
  - Client sinh UUID v4 (`session_id`) lưu trong bộ nhớ tiến trình của phiên làm việc.
  - Server thực thi:
    ```sql
    INSERT INTO activity_logs (user_id, action_type, session_id, created_at)
    VALUES (?, 'OPEN_APP', ?, NOW())
    ON CONFLICT (session_id) DO NOTHING;
    ```
  - Khi Socket reconnect, Client gửi lại UUID cũ. Nhờ `ON CONFLICT DO NOTHING`, DB tự động bỏ qua, đảm bảo thống kê mở app chính xác 100%.

### 4.2 Nghiệp vụ Bạn bè & Chặn (Block)
- **Đồng ý kết bạn (`ACCEPT`):** Thực hiện trong **một Transaction**:
  ```sql
  UPDATE friend_requests SET status = 'ACCEPTED' WHERE id = ? AND receiver_id = ? AND status = 'PENDING';
  INSERT INTO friendships (user_id_1, user_id_2, created_at)
  VALUES (LEAST(?, ?), GREATEST(?, ?), NOW())
  ON CONFLICT (user_id_1, user_id_2) DO NOTHING;
  ```
- **Chặn tài khoản (`BLOCK`):** Thực hiện trong **một Transaction**:
  ```sql
  INSERT INTO user_blocks (blocker_id, blocked_id, created_at) VALUES (?, ?, NOW()) ON CONFLICT DO NOTHING;
  DELETE FROM friendships WHERE user_id_1 = LEAST(?, ?) AND user_id_2 = GREATEST(?, ?);
  UPDATE friend_requests SET status = 'REJECTED'
  WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) AND status = 'PENDING';
  ```
- **Tìm kiếm người dùng:** Loại trừ những người đã chặn mình:
  ```sql
  SELECT id, username, full_name, last_seen_at FROM users
  WHERE status = 'ACTIVE' AND id != :my_id
    AND (LOWER(username) LIKE LOWER(:keyword) OR LOWER(full_name) LIKE LOWER(:keyword))
    AND id NOT IN (SELECT blocker_id FROM user_blocks WHERE blocked_id = :my_id);
  ```

### 4.3 Tin nhắn Cá nhân 1-1 & Cơ chế Xóa Lịch sử
- **Đồng bộ bằng Advisory Lock:** Server băm cặp `(min_id, max_id)` thành số `BIGINT` dương và gọi `SELECT pg_advisory_xact_lock(:dm_lock_key);` trước khi gửi hoặc xóa tin.
- **Xóa toàn bộ lịch sử 1 phía:** Ghi mốc watermark `MAX(id)` tại thời điểm xóa:
  ```sql
  SELECT COALESCE(MAX(id), 0) AS cutoff FROM direct_messages
  WHERE (sender_id = :user_id AND receiver_id = :partner_id) OR (sender_id = :partner_id AND receiver_id = :user_id);

  INSERT INTO conversation_clears (user_id, partner_id, last_cleared_message_id, cleared_at)
  VALUES (:user_id, :partner_id, :cutoff, clock_timestamp())
  ON CONFLICT (user_id, partner_id) DO UPDATE SET
    last_cleared_message_id = GREATEST(conversation_clears.last_cleared_message_id, EXCLUDED.last_cleared_message_id),
    cleared_at = EXCLUDED.cleared_at;
  ```
- **Xóa lẻ 1/vài dòng:** Ghi nhận vào `message_hidden`:
  ```sql
  INSERT INTO message_hidden (message_id, user_id, hidden_at) VALUES (?, :my_id, NOW()) ON CONFLICT DO NOTHING;
  ```
- **Tải lịch sử chat hiển thị cho người dùng:** Chỉ lấy tin nhắn sau mốc watermark và chưa bị ẩn:
  ```sql
  SELECT m.id, m.sender_id, m.receiver_id, m.content, m.is_read, m.created_at
  FROM direct_messages m
  LEFT JOIN conversation_clears c ON c.user_id = :my_id AND c.partner_id = :partner_id
  WHERE ((m.sender_id = :my_id AND m.receiver_id = :partner_id) OR (m.sender_id = :partner_id AND m.receiver_id = :my_id))
    AND m.id > COALESCE(c.last_cleared_message_id, 0)
    AND NOT EXISTS (SELECT 1 FROM message_hidden h WHERE h.message_id = m.id AND h.user_id = :my_id)
  ORDER BY m.id ASC;
  ```

### 4.4 Nhóm Chat & Mã hóa AES-GCM Server-side
- **Tạo nhóm:** Nếu nhóm mã hóa, Server sinh khóa DEK (AES-256), bọc (wrap) bằng Master Key KEK bên ngoài DB (`AESWrap`), lưu chuỗi envelope vào `chat_groups.wrapped_encryption_key`. Thêm người tạo làm Admin đầu tiên vào `group_members`.
- **Gửi tin:** Server sinh IV 12 byte ngẫu nhiên (Base64 16 ký tự), mã hóa nội dung bằng `AES/GCM/NoPadding` với DEK của nhóm, chèn vào `group_messages`.

### 4.5 Các câu Truy vấn Thống kê Phức tạp cho Admin
- **Bạn của bạn (Friend-of-Friends — Yêu cầu 7):** Đếm số bạn bậc 2, loại trừ chính mình và bạn trực tiếp:
  ```sql
  WITH direct_friends AS (
      SELECT user_id_1 AS u_id, user_id_2 AS friend_id FROM friendships
      UNION
      SELECT user_id_2 AS u_id, user_id_1 AS friend_id FROM friendships
  ),
  fof_raw AS (
      SELECT df1.u_id AS user_id, df2.friend_id AS fof_id
      FROM direct_friends df1
      JOIN direct_friends df2 ON df1.friend_id = df2.u_id
      WHERE df2.friend_id != df1.u_id
        AND df2.friend_id NOT IN (SELECT friend_id FROM direct_friends WHERE u_id = df1.u_id)
  )
  SELECT u.id, u.username, u.full_name, u.created_at,
         COUNT(DISTINCT df.friend_id) AS direct_friends_count,
         COUNT(DISTINCT fof.fof_id) AS friends_of_friends_count
  FROM users u
  LEFT JOIN direct_friends df ON u.id = df.u_id
  LEFT JOIN fof_raw fof ON u.id = fof.user_id
  WHERE u.status != 'DELETED'
  GROUP BY u.id, u.username, u.full_name, u.created_at
  ORDER BY u.created_at DESC;
  ```
- **Người dùng hoạt động trong khoảng thời gian (Yêu cầu 8):** Đếm lượt mở app, số người đã chat, số nhóm đã chat:
  ```sql
  SELECT u.id, u.username, u.full_name,
         COALESCE(act.open_app_count, 0) AS open_app_count,
         COALESCE(dm.chat_partners_count, 0) AS chat_partners_count,
         COALESCE(gm.chat_groups_count, 0) AS chat_groups_count
  FROM users u
  LEFT JOIN (
      SELECT user_id, COUNT(*) AS open_app_count FROM activity_logs
      WHERE action_type = 'OPEN_APP' AND created_at >= :from_time AND created_at < :to_time GROUP BY user_id
  ) act ON u.id = act.user_id
  LEFT JOIN (
      SELECT sender_id, COUNT(DISTINCT receiver_id) AS chat_partners_count FROM direct_messages
      WHERE created_at >= :from_time AND created_at < :to_time GROUP BY sender_id
  ) dm ON u.id = dm.sender_id
  LEFT JOIN (
      SELECT sender_id, COUNT(DISTINCT group_id) AS chat_groups_count FROM group_messages
      WHERE created_at >= :from_time AND created_at < :to_time GROUP BY sender_id
  ) gm ON u.id = gm.sender_id
  WHERE (COALESCE(act.open_app_count, 0) > 0 OR COALESCE(dm.chat_partners_count, 0) > 0 OR COALESCE(gm.chat_groups_count, 0) > 0)
  ORDER BY u.id ASC;
  ```
- **Biểu đồ 12 tháng theo năm (Yêu cầu 9):**
  ```sql
  WITH months AS (
      SELECT generate_series(
          DATE_TRUNC('year', :chosen_year_date::timestamptz),
          DATE_TRUNC('year', :chosen_year_date::timestamptz) + interval '11 months',
          interval '1 month'
      ) AS m_start
  )
  SELECT EXTRACT(MONTH FROM m.m_start)::int AS month, COUNT(DISTINCT a.user_id) AS active_users_count
  FROM months m
  LEFT JOIN activity_logs a ON a.action_type = 'OPEN_APP' AND a.created_at >= m.m_start AND a.created_at < m.m_start + interval '1 month'
  GROUP BY m.m_start ORDER BY m.m_start ASC;
  ```

### 4.6 Cơ chế Bảo vệ Tài khoản Admin Cuối cùng
```sql
BEGIN;
SELECT pg_advisory_xact_lock(-1::bigint);
UPDATE users SET role = :new_role, status = :new_status WHERE id = :target_id;
SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE';
-- NẾU count < 1: Thực thi ROLLBACK và trả lỗi "Không thể khóa/hạ quyền Admin hoạt động cuối cùng của hệ thống!"
-- NẾU count >= 1: Thực thi COMMIT;
```
