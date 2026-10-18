# Review Database chính thức — Java Chat System

Thiết kế được cả A/B chấp thuận và chính thức hóa sau khi bộ SQL test PASS trên PostgreSQL 18.6. Bản review trước triển khai được giữ trong [archive](archive/ERD_Review_2026-10-16.md), không phải danh sách P1 đang chờ duyệt hiện tại.

Nguồn hiện hành: [Schema](Database_Schema.md), [Decisions](Database_Decisions.md), [ERD](ERD.mmd), [SQL](../../script/database.sql), [kết quả](TEST_RESULTS.md). Đề chính thức vẫn là [Final Project.pdf](../Final%20Project.pdf).

## P1 đã giải quyết trong schema và SQL test

| Vấn đề | Trạng thái |
|---|---|
| Cột/NULL/default/PK/FK | 13 bảng, 65 cột, 58 bắt buộc; đối chiếu catalog và ERD |
| Status và tự liên hệ | CHECK đã kiểm thử; friendship ID tăng, các quan hệ không tự trỏ |
| PENDING hai chiều | Partial unique expression index; cả cùng/ngược chiều bị từ chối |
| OPEN_APP | UUID NOT NULL UNIQUE đã kiểm thử; login/reconnect là hợp đồng Server |
| Clear một phía | Watermark >=0, default 0, không FK; timestamp audit; concurrency chờ Server |
| Mã hóa schema | Mode–key–IV, mode bất biến, unique IV, group/sender bất biến đã test; không chứng minh AEAD |
| Admin hệ thống | users.role CHECK USER/ADMIN; bootstrap/quyền/Admin cuối chờ Server |
| Spam | UNIQUE ghép một report/cặp suốt đời; bằng chứng incoming DM chờ Server |
| ON DELETE/soft delete | 21 FK đúng 9 RESTRICT/12 CASCADE; soft delete không mất lịch sử, test CASCADE đại diện |
| Index/identity | 16 explicit, 33 tổng; không index trùng định nghĩa, sequence CACHE 1 |
| Canonical username/email | CHECK + UNIQUE tại DB; Server vẫn normalize/validate |

Ba lỗi trong bộ test đã được sửa theo hành vi thực của PostgreSQL, không nới ràng buộc; lần cuối **202 PASS, 0 FAIL**. Chi tiết SQLSTATE/fixture/index và log nằm trong [TEST_RESULTS](TEST_RESULTS.md).

## Quyết định nghiệp vụ đã chốt nhưng chưa có Java triển khai

- READ COMMITTED, cổng quyền shared/exclusive, thứ tự khóa nhóm rồi cặp; khóa cặp trước cấp identity cho DM/clear; kiểm thử barrier nhiều Connection.
- Visibility nhất quán cho lịch sử, tìm kiếm, nhảy tới tin, offline và nhiều Client; không lộ tin hidden/cleared.
- Request/accept/reject/unfriend/block nguyên tử, không khôi phục friendship vì retry lời mời cũ; Server kiểm ACTIVE và block hai chiều cho DM/lời mời mới.
- Tạo nhóm tối thiểu creator + một user, membership/role hiện hành, bảo vệ Admin nhóm cuối và kế nhiệm khi khóa/soft delete; creator không tự có quyền vĩnh viễn.
- Xác thực Admin hệ thống, bootstrap offline, bảo vệ Admin ACTIVE cuối, thu hồi session và chống payload tự nâng role.
- Kiểm có DM từ reported → reporter; report trùng trả dòng cũ. Một report/cặp suốt đời là chính sách nhóm, PDF không yêu cầu báo cáo lặp.
- OPEN_APP đúng một phiên sử dụng thật, reconnect không tạo login/log mới; UUID log không là credential.
- SQL runtime role tối thiểu, PreparedStatement, BCrypt, input/frame limits, TLS; chưa test privilege denial bằng role non-owner.
- Truy vấn thống kê `[from,to)`, Asia/Ho_Chi_Minh, đủ 12 tháng; bạn cấp hai = tổng DISTINCT người cách tối đa hai cạnh, loại chính mình. Không đổi schema nếu giảng viên muốn chỉ đếm cấp hai.

## PROVISIONAL và giới hạn

Mô hình Server-side AES-GCM được nhóm chọn để có thể hoàn thành với hai người nhưng vẫn chờ giảng viên xác nhận §12g; **không gọi E2EE**. Một DEK/nhóm, mode bất biến, KEK ngoài DB/repo/client; Server có thể đọc plaintext. Cần kiểm thử AEAD/tag/AAD/IV, backup/restart/recovery và TLS; test schema chưa thực hiện những việc này.

ERD thể hiện cột, NULL, PK/FK/UK đơn và cardinality; CHECK, UNIQUE ghép/có điều kiện, trigger, advisory lock và quyền service được mô tả ở Schema/Decisions. Không vẽ membership FK giả từ tin vì phải giữ lịch sử sau khi xóa thành viên. SVG/PNG được nâng từ bản cùng mô hình 65 cột, không thay cấu trúc trong lần chính thức hóa; Mermaid chỉ đổi comment trạng thái.
