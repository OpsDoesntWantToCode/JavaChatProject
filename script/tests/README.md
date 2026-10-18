# PostgreSQL schema và bộ kiểm thử

## Trạng thái kiểm chứng

PostgreSQL 18.6: **202 PASS, 0 FAIL** trong lần chạy cuối trên database test mới. Xem [TEST_RESULTS](../../docs/database/TEST_RESULTS.md), [log](latest-run.txt), [catalog](../../docs/database/validation_catalog.json). Ba lỗi trong bộ test đã sửa theo catalog/SQLSTATE thật; không nới constraint hoặc đổi logic DDL. Java/concurrency/AEAD chưa chạy.

## Chạy trên database mới, riêng biệt

Cần PostgreSQL đã cài và chạy, `psql`/`createdb` trong PATH hoặc chỉ định `-PgBin`. Tài khoản cần quyền CREATEDB; cấu hình xác thực trước (ví dụ pgpass của PostgreSQL), không đưa password vào repo hoặc câu lệnh. Đã kiểm thử PostgreSQL 18.6 với database UTF-8; chưa kiểm thử các phiên bản khác. Script kỳ vọng SQLSTATE 23001 cho DELETE RESTRICT theo hành vi thực tế bản này.

Từ gốc repository, chạy:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File script/tests/run.ps1 -DbHost localhost -Port 5432 -DbUser postgres
```

Nếu tools chưa nằm trong PATH, thêm `-PgBin` trỏ tới thư mục `bin` của bản PostgreSQL thực tế trên máy. `ExecutionPolicy Bypass` chỉ áp dụng cho tiến trình chạy này, không thay chính sách máy.

Runner luôn tạo database mới từ `template0`, tên `java_chat_test_<32 ký tự hex ngẫu nhiên>`. Không nhận tên database đích từ người chạy; CREATE thất bại thì dừng, không dùng lại DB đã có. Không có DROP DATABASE hoặc lệnh dọn dữ liệu database hiện hữu. Kết nối `postgres` chỉ để yêu cầu tạo DB mới, mọi DDL/fixture chạy trên DB vừa tạo. Database test được giữ lại và in tên để kiểm tra sau đó; không tự xóa.

`run.sql` chặn tên ngoài mẫu và database không rỗng trước khi gọi `database.sql`. `ON_ERROR_STOP` và exit code làm runner báo lỗi ngay khi có assertion thất bại. Chỉ coi test đạt khi runner in `PASS: schema/constraint suite ...` và kết thúc exit code 0. Các NOTICE PASS riêng lẻ trước một lỗi không có nghĩa cả bộ đạt.

Schema DDL chạy trong transaction riêng và commit khi hoàn chỉnh; fixture và helper kiểm thử nằm trong transaction ROLLBACK. Sequence vẫn có thể tăng dù rollback, nên tuyệt đối không dùng bộ test trên database làm việc. Không chạy riêng `constraints.sql` trên DB ứng dụng; guard tên là một hàng rào, runner tạo mới mới là quy trình an toàn.

## Số lượng đã xác nhận bằng catalog PostgreSQL 18.6

| Thành phần | Số lượng |
|---|---:|
| Bảng / cột | 13 / 65 |
| PK | 13 (8 đơn identity, 5 ghép) |
| FK | 21 (9 RESTRICT, 12 CASCADE) |
| UNIQUE constraint | 4 |
| CHECK | 31 |
| NOT NULL, bao gồm 8 identity | 58 |
| Constraint PK + FK + UNIQUE + CHECK | 69 |
| CREATE INDEX tường minh | 16 (14 thường, 2 partial unique) |
| Index backing PK/UNIQUE | 17 |
| Tổng index | 33 |
| Function trigger / trigger nghiệp vụ | 2 / 2 |

NOT NULL được thống kê riêng; cách PostgreSQL biểu diễn nó trong `pg_constraint` phụ thuộc phiên bản. Không dùng tổng `count(*)` của pg_constraint để giả định tất cả phiên bản giống nhau. Các constraint/index ở bảng này đều có assertion catalog trong SQL test.

Đã rà soát 16 index so với thiết kế: không giữ `idx_dm_pair_time`, không thêm bản sao PK/UNIQUE. Các cặp login/activity/group-message có leading key khác nhau để phục vụ truy vấn toàn hệ thống và từng user; index cặp DM không thay sender/receiver index vì là expression index. Giữ 16 index theo thiết kế; chưa có EXPLAIN hoặc số đo để khẳng định tối ưu hiệu năng.

## Phạm vi test khi có PostgreSQL

- Catalog: đúng bảng/cột, NOT NULL, PK/FK/UNIQUE/CHECK, từng cột FK/bảng cha/ON DELETE, identity CACHE 1, 16 định nghĩa index, tổng 33 index và hai trigger.
- DML: duplicate từng PK, NULL từng cột bắt buộc, missing-parent từng FK; username/email trùng; UUID trùng kể cả khác owner và sai format.
- PENDING cùng chiều/ngược chiều bị chặn, REJECTED lịch sử vẫn được phép; friendship đảo thứ tự/tự kết bạn, tự nhắn/tự block/tự report bị CHECK chặn.
- Validation: enum, canonicalization, ID dương, chuỗi rỗng/kích thước, watermark âm.
- Mã hóa **chỉ schema**: IV format/unique, tương ứng mode–IV/key, mode bất biến, sender/group của tin bất biến. Dummy ciphertext/key không chứng minh AES-GCM hoạt động.
- Soft delete giữ lịch sử/quan hệ và giữ UNIQUE username; hard-delete lịch sử bị RESTRICT; thực thi CASCADE đại diện cho marker tin và thành viên/tin nhóm. Toàn bộ 21 chính sách FK còn được kiểm tra catalog.

## Chính sách và công việc Server còn lại

PDF không quy định tần suất báo cáo spam. Nhóm chọn `UNIQUE(reporter_id,reported_id)`: **một reporter chỉ báo cáo một reported user một lần trong toàn bộ thời gian**. Retry trả báo cáo cũ; không tạo thêm vụ việc sau đó. Đây là chính sách nhóm đã chọn, không phải yêu cầu của giảng viên; chưa thấy xung đột trực tiếp với PDF. Không UNIQUE riêng từng cột. Server phải kiểm đã nhận DM từ reported → reporter; FK không chứng minh điều kiện đó.

`database.sql` không tự tạo role tài khoản PostgreSQL hoặc cấp quyền runtime. Người vận hành cần tạo một role Server không phải owner/superuser, cấp CONNECT/USAGE, SELECT/INSERT/UPDATE và DELETE chỉ trên bảng liên kết mà service cần (friend_requests/friendships/user_blocks/group_members/message_hidden); cấp quyền sequence phù hợp identity. Không cấp DELETE/TRUNCATE trên users/chat_groups/direct_messages/group_messages và không cấp DDL. Client không có credential DB. Kiểm thử privilege denial cần thực hiện sau khi cấu hình role thực tế; chưa có kết quả trong bộ test này.

Java Server cần thực hiện đầy đủ quy ước ở tài liệu quyết định: cổng quyền shared/exclusive và thứ tự khóa; pair lock **trước** identity cho gửi/clear; visibility cho lịch sử/tìm kiếm; friend/block transaction; role/status hiện hành, bootstrap và bảo vệ Admin ACTIVE cuối; quyền nhóm/kế nhiệm; idempotency OPEN_APP và thu hồi session. DDL không kiểm tra quyền theo Socket hoặc bảo đảm terminal DELETED. Không tự hiểu các fixture SQL bypass service là hành vi ứng dụng hợp lệ.

Server-side AES-GCM vẫn **PROVISIONAL**: chưa được giảng viên xác nhận thay cho E2EE. Server còn phải giữ KEK ngoài DB/repo/client, wrap/unwrap DEK, kiểm Base64/tag/AAD và nonce, TLS, BCrypt, SMTP/LLM. Chưa chạy Java, concurrency integration test, AEAD hoặc đo hiệu năng.

Phần SQL/thiết kế database đã PASS và đủ điều kiện cho mốc 20%. Mốc 20% toàn dự án còn cần GUI, báo cáo và ảnh commit theo PDF.

Tham khảo cú pháp đã đối chiếu: [PostgreSQL CREATE TABLE](https://www.postgresql.org/docs/18/sql-createtable.html), [PL/pgSQL errors](https://www.postgresql.org/docs/18/plpgsql-errors-and-messages.html), [createdb](https://www.postgresql.org/docs/18/app-createdb.html). Đối chiếu tài liệu không thay cho chạy test thực tế.
