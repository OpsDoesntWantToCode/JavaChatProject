# Database Schema Draft

> Bản chuyển đổi từ `Database_Schema_Draft.docx`; giữ nguyên nội dung kỹ thuật và thứ tự nguồn. Đây là đề xuất của B, không phải xác nhận nghiệm thu P1. Nhận xét và các điểm cần duyệt được tách vào `ERD_Review.md`.

## 2. Chi tiết 13 Bảng & Ràng buộc chuẩn hóa (P1)

### Nhóm 1: Tài khoản & Hoạt động (3 bảng)

#### users

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `username: VARCHAR(50) NOT NULL UNIQUE (nên dùng LOWER(username) khi tìm kiếm)`

- `password_hash: VARCHAR(255) NOT NULL (lưu hash BCrypt)`

- `full_name: VARCHAR(100) NOT NULL`

- `address: TEXT NULL`

- `dob: DATE NULL`

- `gender: VARCHAR(10) NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))`

- `email: VARCHAR(100) NOT NULL UNIQUE`

- `status: VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED'))`

- `last_seen_at: TIMESTAMPTZ NULL`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Quy tắc: Xóa tài khoản áp dụng Soft Delete (status = 'DELETED'), không dùng DELETE FROM users để tránh mất dữ liệu liên đới.

#### login_history

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `login_time: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Chỉ mục: CREATE INDEX idx_login_history_time ON login_history (login_time DESC); (phục vụ Admin xem theo thời gian).

#### activity_logs

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `action_type: VARCHAR(30) NOT NULL DEFAULT 'OPEN_APP' CHECK (action_type IN ('OPEN_APP'))`

- `session_id: VARCHAR(64) NULL`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Quy tắc chống ghi trùng: Khi Socket client reconnect, giữ nguyên session_id. Admin thống kê số lượng mở app bằng COUNT(DISTINCT session_id) hoặc COUNT(DISTINCT user_id) theo tháng.

### Nhóm 2: Bạn bè & Chặn (3 bảng)

#### friend_requests

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `sender_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `receiver_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `status: VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

- `CONSTRAINT chk_fr_no_self CHECK (sender_id <> receiver_id)`

Chống gửi trùng 2 chiều:

```sql
CREATE UNIQUE INDEX uq_pending_friend_pair
ON friend_requests (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id))
WHERE status = 'PENDING';
```

#### friendships

- `user_id_1: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `user_id_2: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

- `PRIMARY KEY (user_id_1, user_id_2)`

- `CONSTRAINT chk_friendship_order CHECK (user_id_1 < user_id_2)`

Quy tắc: Java Server luôn sắp xếp Math.min(idA, idB) vào user_id_1 và Math.max(idA, idB) vào user_id_2. Mỗi quan hệ bạn bè chỉ chiếm đúng 1 dòng duy nhất!

#### user_blocks

- `blocker_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `blocked_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

- `PRIMARY KEY (blocker_id, blocked_id)`

- `CONSTRAINT chk_block_no_self CHECK (blocker_id <> blocked_id)`

Quy tắc nghiệp vụ: Khi A block B, thực hiện trong 1 Transaction: (1) Thêm vào user_blocks, (2) Xóa khỏi friendships, (3) Hủy mọi lời mời trong friend_requests.

### Nhóm 3: Chat 1-1 & Xử lý Xóa lịch sử (3 bảng)

#### direct_messages

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `sender_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `receiver_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `content: TEXT NOT NULL`

- `is_read: BOOLEAN NOT NULL DEFAULT FALSE`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

- `CONSTRAINT chk_dm_no_self CHECK (sender_id <> receiver_id)`

Chỉ mục: CREATE INDEX idx_dm_pair_time ON direct_messages (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), created_at);

#### message_hidden (Xóa 1 hoặc vài dòng tin nhắn lẻ phía mình)

- `message_id: BIGINT NOT NULL REFERENCES direct_messages(id) ON DELETE CASCADE`

- `user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `hidden_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

- `PRIMARY KEY (message_id, user_id)`

#### conversation_clears (Xóa toàn bộ lịch sử chat với 1 người phía mình)

- `user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `partner_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE`

- `cleared_at: TIMESTAMPTZ NOT NULL`

- `PRIMARY KEY (user_id, partner_id)`

- `CONSTRAINT chk_clear_no_self CHECK (user_id <> partner_id)`

Quy tắc hiển thị lịch sử chat của User A với User B: Lấy các tin nhắn có created_at > (cleared_at của A với B) VÀ id NOT IN (message_hidden của A). Người kia vẫn đọc được bình thường!

### Nhóm 4: Chat nhóm & Mã hóa (3 bảng)

#### chat_groups

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `group_name: VARCHAR(100) NOT NULL`

- `created_by: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `is_encrypted: BOOLEAN NOT NULL DEFAULT FALSE`

- `wrapped_encryption_key: TEXT NULL`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

#### group_members

- `group_id: BIGINT NOT NULL REFERENCES chat_groups(id) ON DELETE CASCADE`

- `user_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `is_admin: BOOLEAN NOT NULL DEFAULT FALSE`

- `encrypted_group_key: TEXT NULL`

- `joined_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

- `PRIMARY KEY (group_id, user_id)`

#### group_messages

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `group_id: BIGINT NOT NULL REFERENCES chat_groups(id) ON DELETE CASCADE`

- `sender_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `content: TEXT NOT NULL (nếu nhóm mã hóa thì lưu ciphertext)`

- `iv: VARCHAR(64) NULL (chuỗi Base64 của Initialization Vector cho AES-GCM)`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Chỉ mục: CREATE INDEX idx_group_messages_time ON group_messages (group_id, created_at);

### Nhóm 5: Báo cáo Spam (1 bảng)

#### spam_reports

- `id: BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`

- `reporter_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `reported_id: BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT`

- `reason: TEXT NULL`

- `created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()`

- `CONSTRAINT chk_spam_no_self CHECK (reporter_id <> reported_id)`

Chỉ mục: CREATE INDEX idx_spam_reports_time ON spam_reports (created_at DESC);

## 3. Đáp ứng các truy vấn nghiệp vụ khó của đề bài

Tính số bạn trực tiếp & Bạn của bạn (Admin mục 7):

Bạn trực tiếp của U: Những ai có trong friendships với U.

Bạn của bạn: Lấy tập bạn bè của những người bạn trực tiếp, trừ đi tập bạn trực tiếp và chính U. (Câu lệnh UNION / WITH RECURSIVE hoặc Self-Join viết rất gọn).

Thống kê người dùng hoạt động (Admin mục 8):

Số người đã chat: SELECT COUNT(DISTINCT receiver_id) FROM direct_messages WHERE sender_id = ? AND created_at BETWEEN ? AND ?

Số nhóm đã chat: SELECT COUNT(DISTINCT group_id) FROM group_messages WHERE sender_id = ? AND created_at BETWEEN ? AND ?

Mở ứng dụng: SELECT COUNT(DISTINCT session_id) FROM activity_logs WHERE user_id = ? AND created_at BETWEEN ? AND ?
