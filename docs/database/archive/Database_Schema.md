# Database Schema — Java Chat System

Thiết kế chính thức dùng chung cho A/B, được nhóm chấp thuận và kiểm thử trên PostgreSQL 18.6.

Nguồn thực thi: [database.sql](../../script/database.sql). Tài liệu này được tạo từ [catalog PostgreSQL đã xuất](validation_catalog.json), không suy diễn từ hình ERD. [Quyết định](Database_Decisions.md), [ERD](ERD.mmd), [kết quả kiểm thử](TEST_RESULTS.md).

**13 bảng, 65 cột, 58 NOT NULL, 13 PK, 21 FK, 4 UNIQUE, 31 CHECK; 16 index tường minh và 17 backing index = 33 index; 2 trigger nghiệp vụ.**

Server-side AES-GCM vẫn **PROVISIONAL**, không phải E2EE. Test schema không chứng minh mã hóa, concurrency hoặc phân quyền Server.

Chạy DDL bằng owner trên database mới/rỗng, không dùng như migration. Không có password/key/seed Admin. Runtime Server dùng role riêng, không phải owner/superuser; cấu hình privilege và kiểm thử quyền thực tế là việc vận hành chưa hoàn thành.

Tám cột identity dùng GENERATED ALWAYS, START 1, INCREMENT 1, CACHE 1, NO CYCLE. Mọi PK/FK immediate; FK ON UPDATE mặc định NO ACTION. NOT NULL được trình bày trong bảng cột, không liệt kê lại constraint loại n của PostgreSQL 18.

## Danh mục bảng

| Bảng | Số cột |
|---|---:|
| `activity_logs` | 5 |
| `chat_groups` | 6 |
| `conversation_clears` | 4 |
| `direct_messages` | 6 |
| `friend_requests` | 5 |
| `friendships` | 3 |
| `group_members` | 4 |
| `group_messages` | 6 |
| `login_history` | 3 |
| `message_hidden` | 3 |
| `spam_reports` | 5 |
| `user_blocks` | 3 |
| `users` | 12 |

## activity_logs

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `user_id` | `bigint` | Có | — |
| `action_type` | `character varying(30)` | Có | `'OPEN_APP'::character varying` |
| `session_id` | `uuid` | Có | — |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_activity_action`: `CHECK (action_type::text = 'OPEN_APP'::text)`
- `chk_activity_logs_id_positive`: `CHECK (id > 0)`
- `fk_activity_logs_user`: `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT`
- `pk_activity_logs`: `PRIMARY KEY (id)`
- `uq_activity_session`: `UNIQUE (session_id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_activity_time_user ON public.activity_logs USING btree (created_at, user_id)`
- `CREATE INDEX idx_activity_user_time ON public.activity_logs USING btree (user_id, created_at)`
- `CREATE UNIQUE INDEX pk_activity_logs ON public.activity_logs USING btree (id)`
- `CREATE UNIQUE INDEX uq_activity_session ON public.activity_logs USING btree (session_id)`

## chat_groups

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `group_name` | `character varying(100)` | Có | — |
| `created_by` | `bigint` | Có | — |
| `is_encrypted` | `boolean` | Có | `false` |
| `wrapped_encryption_key` | `text` | Không | — |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_chat_groups_id_positive`: `CHECK (id > 0)`
- `chk_group_key_mode`: `CHECK (is_encrypted AND wrapped_encryption_key IS NOT NULL OR NOT is_encrypted AND wrapped_encryption_key IS NULL)`
- `chk_group_name`: `CHECK (btrim(group_name::text) <> ''::text)`
- `fk_chat_groups_creator`: `FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT`
- `pk_chat_groups`: `PRIMARY KEY (id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE UNIQUE INDEX pk_chat_groups ON public.chat_groups USING btree (id)`

- Trigger `trg_group_mode_immutable`: `CREATE TRIGGER trg_group_mode_immutable BEFORE UPDATE OF is_encrypted ON chat_groups FOR EACH ROW EXECUTE FUNCTION guard_group_encryption_mode()`

## conversation_clears

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `user_id` | `bigint` | Có | — |
| `partner_id` | `bigint` | Có | — |
| `last_cleared_message_id` | `bigint` | Có | `0` |
| `cleared_at` | `timestamp with time zone` | Có | — |

Ràng buộc:

- `chk_clear_no_self`: `CHECK (user_id <> partner_id)`
- `chk_clear_watermark_nonnegative`: `CHECK (last_cleared_message_id >= 0)`
- `fk_conversation_clears_partner`: `FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE CASCADE`
- `fk_conversation_clears_user`: `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE`
- `pk_conversation_clears`: `PRIMARY KEY (user_id, partner_id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE UNIQUE INDEX pk_conversation_clears ON public.conversation_clears USING btree (user_id, partner_id)`

## direct_messages

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `sender_id` | `bigint` | Có | — |
| `receiver_id` | `bigint` | Có | — |
| `content` | `text` | Có | — |
| `is_read` | `boolean` | Có | `false` |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_direct_messages_id_positive`: `CHECK (id > 0)`
- `chk_dm_content`: `CHECK (content <> ''::text AND octet_length(content) <= 65536)`
- `chk_dm_no_self`: `CHECK (sender_id <> receiver_id)`
- `fk_direct_messages_receiver`: `FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE RESTRICT`
- `fk_direct_messages_sender`: `FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT`
- `pk_direct_messages`: `PRIMARY KEY (id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_dm_pair_id ON public.direct_messages USING btree (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), id)`
- `CREATE INDEX idx_dm_receiver_id ON public.direct_messages USING btree (receiver_id, id)`
- `CREATE INDEX idx_dm_sender_time ON public.direct_messages USING btree (sender_id, created_at, receiver_id)`
- `CREATE UNIQUE INDEX pk_direct_messages ON public.direct_messages USING btree (id)`

## friend_requests

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `sender_id` | `bigint` | Có | — |
| `receiver_id` | `bigint` | Có | — |
| `status` | `character varying(20)` | Có | `'PENDING'::character varying` |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_fr_no_self`: `CHECK (sender_id <> receiver_id)`
- `chk_fr_status`: `CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying]::text[]))`
- `chk_friend_requests_id_positive`: `CHECK (id > 0)`
- `fk_friend_requests_receiver`: `FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE`
- `fk_friend_requests_sender`: `FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE`
- `pk_friend_requests`: `PRIMARY KEY (id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_friend_requests_inbox ON public.friend_requests USING btree (receiver_id, created_at DESC) WHERE ((status)::text = 'PENDING'::text)`
- `CREATE UNIQUE INDEX pk_friend_requests ON public.friend_requests USING btree (id)`
- `CREATE UNIQUE INDEX uq_pending_friend_pair ON public.friend_requests USING btree (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) WHERE ((status)::text = 'PENDING'::text)`

## friendships

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `user_id_1` | `bigint` | Có | — |
| `user_id_2` | `bigint` | Có | — |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_friendship_order`: `CHECK (user_id_1 < user_id_2)`
- `fk_friendships_first`: `FOREIGN KEY (user_id_1) REFERENCES users(id) ON DELETE CASCADE`
- `fk_friendships_second`: `FOREIGN KEY (user_id_2) REFERENCES users(id) ON DELETE CASCADE`
- `pk_friendships`: `PRIMARY KEY (user_id_1, user_id_2)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_friendships_second ON public.friendships USING btree (user_id_2, user_id_1)`
- `CREATE UNIQUE INDEX pk_friendships ON public.friendships USING btree (user_id_1, user_id_2)`

## group_members

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `group_id` | `bigint` | Có | — |
| `user_id` | `bigint` | Có | — |
| `is_admin` | `boolean` | Có | `false` |
| `joined_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `fk_group_members_group`: `FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE`
- `fk_group_members_user`: `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT`
- `pk_group_members`: `PRIMARY KEY (group_id, user_id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_group_members_user ON public.group_members USING btree (user_id, group_id)`
- `CREATE UNIQUE INDEX pk_group_members ON public.group_members USING btree (group_id, user_id)`

## group_messages

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `group_id` | `bigint` | Có | — |
| `sender_id` | `bigint` | Có | — |
| `content` | `text` | Có | — |
| `iv` | `character varying(64)` | Không | — |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_group_message_content`: `CHECK (content <> ''::text AND octet_length(content) <= 65536)`
- `chk_group_message_iv`: `CHECK (iv IS NULL OR iv::text ~ '^[A-Za-z0-9+/]{16}$'::text)`
- `chk_group_messages_id_positive`: `CHECK (id > 0)`
- `fk_group_messages_group`: `FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE`
- `fk_group_messages_sender`: `FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT`
- `pk_group_messages`: `PRIMARY KEY (id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_group_messages_sender_time ON public.group_messages USING btree (sender_id, created_at, group_id)`
- `CREATE INDEX idx_group_messages_time ON public.group_messages USING btree (group_id, created_at, id)`
- `CREATE UNIQUE INDEX pk_group_messages ON public.group_messages USING btree (id)`
- `CREATE UNIQUE INDEX uq_group_message_iv ON public.group_messages USING btree (group_id, iv) WHERE (iv IS NOT NULL)`

- Trigger `trg_group_message_mode`: `CREATE TRIGGER trg_group_message_mode BEFORE INSERT OR UPDATE ON group_messages FOR EACH ROW EXECUTE FUNCTION guard_group_message_mode()`

## login_history

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `user_id` | `bigint` | Có | — |
| `login_time` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_login_history_id_positive`: `CHECK (id > 0)`
- `fk_login_history_user`: `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT`
- `pk_login_history`: `PRIMARY KEY (id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_login_history_time ON public.login_history USING btree (login_time DESC)`
- `CREATE INDEX idx_login_history_user_time ON public.login_history USING btree (user_id, login_time DESC)`
- `CREATE UNIQUE INDEX pk_login_history ON public.login_history USING btree (id)`

## message_hidden

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `message_id` | `bigint` | Có | — |
| `user_id` | `bigint` | Có | — |
| `hidden_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `fk_message_hidden_message`: `FOREIGN KEY (message_id) REFERENCES direct_messages(id) ON DELETE CASCADE`
- `fk_message_hidden_user`: `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE`
- `pk_message_hidden`: `PRIMARY KEY (message_id, user_id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE UNIQUE INDEX pk_message_hidden ON public.message_hidden USING btree (message_id, user_id)`

## spam_reports

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `reporter_id` | `bigint` | Có | — |
| `reported_id` | `bigint` | Có | — |
| `reason` | `text` | Không | — |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_spam_no_self`: `CHECK (reporter_id <> reported_id)`
- `chk_spam_reason`: `CHECK (reason IS NULL OR char_length(reason) <= 2000)`
- `chk_spam_reports_id_positive`: `CHECK (id > 0)`
- `fk_spam_reports_reported`: `FOREIGN KEY (reported_id) REFERENCES users(id) ON DELETE RESTRICT`
- `fk_spam_reports_reporter`: `FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE RESTRICT`
- `pk_spam_reports`: `PRIMARY KEY (id)`
- `uq_spam_report_pair`: `UNIQUE (reporter_id, reported_id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_spam_reports_time ON public.spam_reports USING btree (created_at DESC)`
- `CREATE UNIQUE INDEX pk_spam_reports ON public.spam_reports USING btree (id)`
- `CREATE UNIQUE INDEX uq_spam_report_pair ON public.spam_reports USING btree (reporter_id, reported_id)`

## user_blocks

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `blocker_id` | `bigint` | Có | — |
| `blocked_id` | `bigint` | Có | — |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_block_no_self`: `CHECK (blocker_id <> blocked_id)`
- `fk_user_blocks_blocked`: `FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE`
- `fk_user_blocks_blocker`: `FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE`
- `pk_user_blocks`: `PRIMARY KEY (blocker_id, blocked_id)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE UNIQUE INDEX pk_user_blocks ON public.user_blocks USING btree (blocker_id, blocked_id)`

## users

| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |
|---|---|---|---|
| `id` | `bigint` | Có | `GENERATED ALWAYS AS IDENTITY` |
| `username` | `character varying(50)` | Có | — |
| `password_hash` | `character varying(255)` | Có | — |
| `full_name` | `character varying(100)` | Có | — |
| `address` | `text` | Không | — |
| `dob` | `date` | Không | — |
| `gender` | `character varying(10)` | Không | — |
| `email` | `character varying(100)` | Có | — |
| `status` | `character varying(20)` | Có | `'ACTIVE'::character varying` |
| `role` | `character varying(10)` | Có | `'USER'::character varying` |
| `last_seen_at` | `timestamp with time zone` | Không | — |
| `created_at` | `timestamp with time zone` | Có | `now()` |

Ràng buộc:

- `chk_users_email_ascii`: `CHECK (email::text ~ '^[!-~]+$'::text)`
- `chk_users_email_canonical`: `CHECK (email::text = lower(btrim(email::text)))`
- `chk_users_email_nonempty`: `CHECK (email::text <> ''::text)`
- `chk_users_full_name`: `CHECK (btrim(full_name::text) <> ''::text)`
- `chk_users_gender`: `CHECK (gender::text = ANY (ARRAY['MALE'::character varying, 'FEMALE'::character varying, 'OTHER'::character varying]::text[]))`
- `chk_users_id_positive`: `CHECK (id > 0)`
- `chk_users_role`: `CHECK (role::text = ANY (ARRAY['USER'::character varying, 'ADMIN'::character varying]::text[]))`
- `chk_users_status`: `CHECK (status::text = ANY (ARRAY['ACTIVE'::character varying, 'LOCKED'::character varying, 'DELETED'::character varying]::text[]))`
- `chk_users_username`: `CHECK (username::text ~ '^[a-z0-9_.-]{3,50}$'::text)`
- `pk_users`: `PRIMARY KEY (id)`
- `uq_users_email`: `UNIQUE (email)`
- `uq_users_username`: `UNIQUE (username)`

Index (gồm cả backing PK/UNIQUE):

- `CREATE INDEX idx_users_created_at ON public.users USING btree (created_at)`
- `CREATE UNIQUE INDEX pk_users ON public.users USING btree (id)`
- `CREATE UNIQUE INDEX uq_users_email ON public.users USING btree (email)`
- `CREATE UNIQUE INDEX uq_users_username ON public.users USING btree (username)`

## Ngữ nghĩa bắt buộc ở Server

- UUID session là idempotency của OPEN_APP, không phải bearer token; reconnect không tạo lượt mở mới.
- Watermark không là FK, cho phép 0. Clear chỉ một phía; đọc/tìm kiếm lọc cả watermark và message_hidden. Khóa cặp trước cấp ID là điều kiện correctness chưa kiểm thử concurrency.
- Mỗi reporter chỉ báo cáo một reported user một lần trong toàn bộ thời gian. UNIQUE ghép không làm riêng hai cột thành UNIQUE; Server kiểm bằng chứng DM chiều reported → reporter.
- Soft delete cập nhật status, giữ tin và FK; Server áp dụng DELETED terminal, thu hồi session, bảo vệ Admin cuối và kế nhiệm nhóm.
- Các FK sender/group không chứng minh membership hoặc quyền. Quyền Admin hệ thống và Admin nhóm độc lập.
- CHECK IV/mode và unique nonce chỉ kiểm schema; payload/tag/AAD, quản lý DEK/KEK và TLS cần Server triển khai. Không có khóa thô trong DB.
- Canonical username/email, BCrypt và validation đầu vào nằm tại Server bên cạnh CHECK của DB.

## Tái tạo tài liệu

Xuất lại catalog bằng `script/tests/catalog.sql` trên database test vừa PASS, sau đó chạy `python script/tests/build_schema_doc.py`. Không xuất schema từ database công việc khác. Khi DDL thay đổi, phải test lại và đồng bộ catalog, tài liệu, ERD trong cùng thay đổi.
