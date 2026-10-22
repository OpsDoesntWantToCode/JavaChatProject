# PostgreSQL Database — kết quả kiểm thử

## Kết quả cuối

**PASS: 202 assertion, 0 FAIL trong lần chạy cuối**, trên PostgreSQL **18.6**, Windows x64. Xác thực pgpass không cần nhập mật khẩu; không đọc hoặc ghi mật khẩu vào repository/log.

Database test cuối: `java_chat_test_3cfeccfbb97046058ce25fe43e595a98`. Runner tạo database mới bằng `createdb --template=template0`, sau đó chạy DDL và test với `ON_ERROR_STOP`. Schema commit, dữ liệu fixture và helper rollback. Database test được giữ lại, không tự DROP. Không chạy DDL/DML trên database làm việc khác.

Minh chứng: [log lần PASS](../../script/tests/latest-run.txt), [catalog từ DB đã PASS](validation_catalog.json), [suite](../../script/tests/constraints.sql), [runner](../../script/tests/run.ps1). Số 202 là số NOTICE PASS do các helper assertion phát ra, bao gồm các trường hợp lặp từng PK/NOT NULL/FK/index, không phải số file test hoặc số chức năng ứng dụng.

## Cấu trúc đã xác nhận bằng catalog thực

| Thành phần | Kết quả |
|---|---:|
| Bảng / cột | 13 / 65 |
| NOT NULL (gồm identity) | 58 |
| PK | 13 (8 đơn identity, 5 ghép) |
| FK | 21: 9 RESTRICT, 12 CASCADE |
| UNIQUE constraint | 4 |
| CHECK | 31 |
| PK + FK + UNIQUE + CHECK | 69 |
| Index | 33: 16 tường minh + 17 backing PK/UNIQUE |
| Trigger nghiệp vụ | 2 |
| Sequence identity | 8: START 1, INCREMENT 1, CACHE 1, NO CYCLE |

PostgreSQL 18 còn biểu diễn NOT NULL bằng constraint loại `n`; số 69 không cộng loại này. Không dùng tổng pg_constraint thiếu phân loại để so giữa phiên bản PostgreSQL.

## Những test đã chạy

- Tạo schema từ database trống, đủ bảng/cột, loại UUID, watermark không FK, GENERATED ALWAYS và thông số sequence.
- Từng PK trùng, từng cột bắt buộc nhận NULL và từng FK thiếu cha đều bị từ chối; từng FK trỏ đúng cột/bảng với chính sách xóa đúng thiết kế.
- Từng định nghĩa trong 16 index: khóa/biểu thức, ASC/DESC, NULLS order, uniqueness và điều kiện partial. Không còn index cặp DM theo timestamp cũ.
- Username/email trùng, giữ username khi DELETED; UUID trùng kể cả khác owner và format sai.
- PENDING trùng cùng/ngược chiều; REJECTED lịch sử vẫn hợp lệ; friendship đảo ID, tự kết bạn/nhắn/block/report bị chặn.
- Enum, canonicalization, ID dương, chuỗi rỗng/kích thước và watermark âm; các default cơ bản.
- Mode nhóm bất biến; mode–key–IV nhất quán; IV format/unique; group/sender của tin bất biến; nhiều tin plaintext IV NULL hợp lệ.
- Soft delete không làm mất lịch sử/quan hệ; hard-delete user có lịch sử bị RESTRICT; CASCADE thực tế cho marker tin và member/message thuộc nhóm.

## Lỗi phát hiện và sửa

Có **3 lần suite dừng trước lần PASS**, mỗi lần trên database mới. Không sửa logic schema hoặc nới constraint để đạt kết quả.

| Database test | Vấn đề | Sửa nguyên nhân |
|---|---|---|
| `java_chat_test_1c9b993f979b4f409682e15f2bf8e787` | Test dùng pg_get_indexdef(index,column,true), không trả DESC | Đọc thêm pg_index.indoption, kiểm cả chiều và NULLS ordering |
| `java_chat_test_06e4917ccd7949408b64b95f748ee95a` | Fixture kiểm FK activity_logs tái dùng UUID đã UNIQUE, gặp 23505 trước FK | Cấp UUID fixture khác, vẫn yêu cầu lỗi missing-parent 23503 |
| `java_chat_test_61d3b9180b394601b78d9f8ba46711e7` | Test DELETE RESTRICT kỳ vọng 23503 nhưng PostgreSQL 18.6 trả restrict_violation 23001 | Kỳ vọng đúng 23001 cho test RESTRICT; missing-parent vẫn 23503 |

Không có lỗi logic DDL cần sửa trong các lần thực thi này. Điều đó chỉ áp dụng cho phạm vi SQL test đã chạy; không chứng minh mọi luồng nghiệp vụ đều đúng. Các lần thất bại vẫn giữ schema đã tạo; fixture rollback khi kết nối kết thúc. Không dọn hoặc tái sử dụng chúng để giả lập test DB trống.

## Chưa kiểm chứng

- Java/JDBC và concurrency nhiều Connection, đặc biệt send/clear, block/accept, remove/send và khóa/hạ quyền Admin đồng thời.
- Bootstrap, Admin ACTIVE cuối/kế nhiệm nhóm, quyền theo Socket, trạng thái DELETED terminal, session revocation và spam evidence.
- Privilege của role runtime non-owner; các test hiện chạy bằng postgres trên DB test.
- AES-GCM roundtrip, tamper/tag/AAD, khóa DEK/KEK và restart/recovery; TLS, SMTP, LLM.
- EXPLAIN/hiệu năng, tải lớn; giảng viên xác nhận mô hình mã hóa hoặc diễn giải bạn cấp hai.

**Server-side AES-GCM vẫn PROVISIONAL, không phải E2EE.** Hai trigger chỉ kiểm cấu trúc/mode, dummy payload test không phải mã hóa thật.

## Kết luận mốc 20%

Phần **SQL và thiết kế Database đủ điều kiện cho mốc 20%**: có script tái tạo, ERD/tài liệu chính thức, test trên PostgreSQL thật và minh chứng. Chưa đánh giá đủ điều kiện nộp toàn dự án vì còn GUI, báo cáo nhóm và ảnh commit. Tích hợp Server và bảo mật nghiệp vụ thuộc phần triển khai tiếp theo.
