# Phân công công việc và hoàn thiện báo cáo — Mốc 20%

Bản dùng để đưa vào báo cáo và bàn giao nội dung giữa hai thành viên. Đối chiếu ngày 16/11/2026 trên `main` tại `6f57e74`; vai trò và tiến độ chính thức tiếp tục theo [kế hoạch](plan.md). Phạm vi hiện tại là giao diện có dữ liệu mẫu và thiết kế CSDL, không coi tương tác mô phỏng là chức năng backend đã hoàn thành.

## 1. Danh sách công việc từng thành viên

| Thành viên | MSSV | Công việc phụ trách |
|---|---|---|
| Lưu Chí Hải | 24127030 | Thiết kế CSDL PostgreSQL, sơ đồ ERD, ràng buộc và script tạo CSDL; kiểm chứng SQL trên database kiểm thử riêng. |
| Lưu Chí Hải | 24127030 | Xây dựng giao diện Admin JavaFX 21 + CSS: quản lý tài khoản, lịch sử đăng nhập, nhóm chat, báo cáo spam, thống kê và biểu đồ; chuẩn bị dữ liệu mẫu và tương tác mô phỏng. |
| Lưu Chí Hải | 24127030 | Kiểm tra hồi quy Admin; chuẩn bị ảnh Admin, minh chứng Git và nội dung Thiết kế CSDL/Màn hình Admin cho báo cáo. Chủ trì xử lý xung đột và kiểm tra bản tích hợp với phần User. |
| Trần Nguyễn Tuấn Minh | 24127084 | Khởi tạo repository, cấu trúc dự án và cấu hình Maven cho Java 21/JavaFX 21. |
| Trần Nguyễn Tuấn Minh | 24127084 | Xây dựng giao diện User: đăng nhập, đăng ký, hồ sơ, bạn bè, tìm kiếm, hội thoại cá nhân/nhóm, quản lý nhóm, báo cáo spam và gợi ý tin nhắn mô phỏng; hoàn thiện CSS và tương tác. |
| Trần Nguyễn Tuấn Minh | 24127084 | Chuẩn bị 18 ảnh User và minh chứng Git; đồng bộ phần User với bản tích hợp. Phụ trách khung báo cáo, nội dung User và tổng hợp báo cáo chung. |
| Cả hai | — | Thống nhất yêu cầu và công nghệ, phối hợp tích hợp, rà soát báo cáo, xác nhận bảng đóng góp và duyệt bộ nộp cuối. |

## 2. Phân công hoàn thiện báo cáo

| Phần báo cáo/sản phẩm | Người làm chính | Người phối hợp/kiểm tra | Đầu ra cần bàn giao |
|---|---|---|---|
| Thông tin nhóm, trang bìa và bố cục | Minh | Hải xác nhận thông tin | Họ tên, MSSV, lớp/nhóm và thông tin môn học chính xác. |
| Danh sách công việc | Hải soạn bảng mục 1 | Minh đối chiếu phần User; cả hai duyệt | Bảng công việc theo phạm vi thực tế, phân biệt bản mẫu với backend. |
| Bảng đóng góp | Hải và Minh | Minh đưa vào báo cáo | Dùng bảng 50%–50% tại mục 3; không suy ra từ số commit. |
| Thiết kế CSDL | Hải | Minh kiểm tra trình bày | Nội dung từ [bản Admin/CSDL](admin-spec/admin-report.md), ERD theo [schema](database/schema.md); sơ đồ đọc được trong PDF. |
| Màn hình Admin | Hải | Minh ghép và đánh số hình | 18 ảnh tại `docs/admin-spec/evidence/`, chú thích trong bản Admin/CSDL. |
| Màn hình User | Minh | Hải rà soát đủ nhóm chức năng | 18 ảnh tại `docs/ui-spec/evidence/`, chú thích chức năng và dữ liệu mẫu theo bản User cuối. |
| Minh chứng Git | Mỗi người phụ trách phần mình | Cả hai kiểm tra phạm vi ảnh | `git/commits_Hai.png`, `git/commits_Minh.png`; ảnh phản ánh đúng phiên bản được chú thích. |
| Ghép và xuất `BaoCao.pdf` | Minh | Hải rà soát CSDL/Admin; cả hai duyệt | Đủ 5 phần theo đề, không lỗi ảnh, ERD hoặc đánh số; nội dung thống nhất với bản mã nguồn nộp. |
| Kiểm tra và đóng gói ZIP | Minh | Hải kiểm tra cấu trúc; cả hai duyệt | `src/`, `script/database.sql`, `git/` và `BaoCao.pdf`; không đưa các SQL test phát triển vào thư mục script của bộ nộp. |

## 3. Bảng đóng góp từng thành viên

Tỷ lệ 50%–50% được Hải xác nhận sử dụng cho báo cáo mốc 20% trong trao đổi ngày 16/11/2026.

Phân bổ chi tiết theo từng đầu việc, A1–A11, U1–U13 và C1–C4 nằm tại [kế hoạch, mục 2.1–2.2](plan.md#21-tỷ-lệ-đóng-góp-theo-từng-đầu-việc). Khi chèn bảng chi tiết vào báo cáo, dùng bảng ở nguồn này; không cộng lại với bảng tổng theo thành viên dưới đây.

| Thành viên | MSSV | Tỷ lệ đóng góp |
|---|---|---:|
| Lưu Chí Hải | 24127030 | 50% |
| Trần Nguyễn Tuấn Minh | 24127084 | 50% |
| **Tổng** | — | **100%** |

## 4. Điểm cần kiểm tra trước khi nộp

- Kế hoạch đánh dấu khung báo cáo C1 hoàn thành, nhưng tại phiên bản kiểm tra chưa thấy bản Word hoặc `BaoCao.pdf` được lưu trong Git. Minh cung cấp bản đang giữ nếu đã soạn để tránh tạo hai bản song song.
- Bộ ảnh User tại `docs/user-spec/evidence/` trùng byte với `docs/ui-spec/evidence/`. Dùng đường dẫn `docs/ui-spec/evidence/` trong báo cáo theo kế hoạch hiện hành; không nhân đôi hình. Chưa xóa thư mục nào.
- Đã đối chiếu đủ 18 PNG User với manifest: định dạng, dung lượng và SHA-256 khớp. Đây là kiểm tra tệp, không thay thế nghiệm thu toàn bộ giao diện hoặc độ đọc của ảnh sau khi ghép PDF.
- Bản `main` mới nhất biên dịch thành công 44 tệp Java và sao chép 2 tài nguyên CSS. Các kết quả nghiệm thu Admin trước đây tiếp tục có phạm vi riêng; chưa chạy lại toàn bộ User trong lượt kiểm tra nhánh này.
