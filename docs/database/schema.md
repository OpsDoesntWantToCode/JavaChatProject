# DATABASE SCHEMA CHÍNH THỨC — JAVA CHAT SYSTEM

> **Phiên bản:** 3.0 (Finalized).  
> **Hệ quản trị CSDL:** PostgreSQL 16+ (Local).  
> **Quy mô:** 13 bảng, 65 cột, 21 khóa ngoại (FK), 6 chỉ mục (Index) tường minh, 11 ràng buộc CHECK, 2 triggers bảo vệ tính toàn vẹn.  
> **Trạng thái:** Đã giải quyết toàn bộ các vấn đề P1 (UUID phiên, watermark xóa tin, AES-GCM Server-side, phân quyền role, chống trùng lời mời kết bạn).

---

## 1. Tóm tắt các vấn đề cốt lõi đã được giải quyết

1. **Ghi nhận phiên mở ứng dụng (`activity_logs.session_id`)**:
   - Sửa từ `VARCHAR(64) NULL` thành `UUID NOT NULL UNIQUE`.
   - Client sinh UUID v4 một lần cho mỗi phiên đăng nhập thành công. Khi Socket reconnect, dùng lại UUID cũ. DB dùng `ON CONFLICT DO NOTHING` chặn triệt để trùng lặp log, bảo đảm số liệu thống kê Admin chính xác 100%.
2. **Xóa toàn bộ lịch sử chat không bị lệch thời gian (`conversation_clears`)**:
   - Khắc phục nhược điểm so sánh `created_at > cleared_at` (dễ lỗi do đồng hồ lệch hoặc transaction commit ngược thứ tự).
   - Bổ sung cột mốc ID: `last_cleared_message_id BIGINT NOT NULL DEFAULT 0 CHECK (last_cleared_message_id >= 0)`.
   - Kết hợp tuần tự hóa gửi/xóa qua khóa giao dịch PostgreSQL `pg_advisory_xact_lock` theo cặp `(min(user_id, partner_id), max(user_id, partner_id))`.
3. **Mã hóa nhóm chat an toàn (`chat_groups`, `group_messages`)**:
   - Triển khai **Server-side AES-GCM (Provisional)**: mỗi nhóm có 1 Khóa mã hóa dữ liệu (DEK). Khóa DEK được bọc (wrap) bằng Khóa chủ (KEK) lưu bên ngoài DB (biến môi trường / file bảo mật).
   - Bỏ cột thừa `group_members.encrypted_group_key`.
   - Trạng thái `is_encrypted` bất biến sau khi tạo nhóm (bảo vệ bởi trigger).
   - IV (Nonce) 96-bit (12 byte) canonical Base64 16 ký tự, kiểm tra bằng regex và `UNIQUE (group_id, iv) WHERE iv IS NOT NULL` để ngăn tuyệt đối tấn công tái sử dụng Nonce trong AES-GCM.
4. **Phân quyền Admin hệ thống (`users.role`)**:
   - Bổ sung cột `role VARCHAR(10) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN'))`.
   - Không cần tạo thêm bảng `admins` thứ 14 gây dư thừa dữ liệu.
   - Bảo vệ Admin hoạt động (`ACTIVE`) cuối cùng bằng advisory lock toàn cục `-1::bigint` khi khóa/xóa/hạ quyền.
5. **Chống trùng lời mời kết bạn 2 chiều (`friend_requests`)**:
   - Ràng buộc partial unique index trên biểu thức: `(LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) WHERE status = 'PENDING'`.
6. **Bảo toàn dữ liệu lịch sử (Soft Delete)**:
   - Áp dụng xóa mềm `users.status = 'DELETED'`. Toàn bộ 9 khóa ngoại quan trọng trỏ về `users` được thiết lập `ON DELETE RESTRICT`, ngăn ngừa xóa nhầm làm mất toàn bộ lịch sử tin nhắn, đăng nhập và báo cáo vi phạm.

---

## 2. Chi tiết cấu trúc 13 Bảng

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
| `email` | `VARCHAR(100)` | `NOT NULL` | `UNIQUE` | Email duy nhất (dùng nhận pass ngẫu nhiên) |
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
- **Chỉ mục chống gửi lặp 2 chiều:** `uq_pending_friend_pair`.

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

### Nhóm 3: Tin nhắn Cá nhân (1-1) & Xóa lịch sử (3 bảng)

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
| `content` | `TEXT` | `NOT NULL` | | Plaintext UTF-8 hoặc Base64(Ciphertext||Tag) |
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

## 3. Danh sách Chỉ mục (Indexes) Tường minh

| Tên Index | Bảng | Biểu thức Index | Mục đích sử dụng |
| :--- | :--- | :--- | :--- |
| `idx_login_history_time` | `login_history` | `(login_time DESC)` | Admin xem lịch sử đăng nhập theo thời gian |
| `uq_pending_friend_pair` | `friend_requests` | `(LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) WHERE status = 'PENDING'` | Ngăn gửi 2 lời mời kết bạn trùng chiều hoặc đảo chiều |
| `idx_dm_pair_id` | `direct_messages` | `(LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), id)` | Tải lịch sử chat 1-1 cực nhanh theo mốc watermark |
| `idx_group_messages_time` | `group_messages` | `(group_id, created_at)` | Tải lịch sử chat nhóm theo thời gian |
| `uq_group_message_iv` | `group_messages` | `(group_id, iv) WHERE iv IS NOT NULL` | Chống tái sử dụng IV/Nonce trong cùng một khóa DEK nhóm |
| `idx_spam_reports_time` | `spam_reports` | `(created_at DESC)` | Admin xem và xử lý báo cáo spam mới nhất |

---

## 4. Trigger & Hàm Bảo vệ Toàn vẹn Dữ liệu

```sql
-- Trigger 1: Ngăn chặn sửa đổi chế độ mã hóa của nhóm chat sau khi tạo
CREATE OR REPLACE FUNCTION guard_group_encryption_mode() RETURNS trigger
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

-- Trigger 2: Bảo đảm tin nhắn khớp chính xác với chế độ mã hóa của nhóm
CREATE OR REPLACE FUNCTION guard_group_message_mode() RETURNS trigger
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

---

## 5. File DDL hoàn chỉnh sẵn sàng thực thi

File thực thi độc lập chuẩn được lưu tại: [script/database.sql](file:///d:/JavaChatProject/script/database.sql).
Mọi thành viên có thể chạy lại trực tiếp trên database PostgreSQL rỗng để khởi tạo đầy đủ hệ thống.
