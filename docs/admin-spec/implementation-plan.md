# Kế hoạch kỹ thuật Admin — Mốc 20%

## 1. Căn cứ và giới hạn

- Yêu cầu và chính sách dữ liệu: [đặc tả màn hình](admin-screens.md).
- Chuẩn giao diện: [chuẩn thiết kế Admin](admin-design-system.md).
- Đây là bước Plan; checklist thực hiện ở [Tasks](implementation-tasks.md). Chưa bắt đầu implement.
- Chỉ triển khai trong `src/admin/` và tài liệu Admin. Giữ Java 21, JavaFX 21.0.5 và POM hiện tại; không thêm framework.
- Không sửa User UI, Socket/DTO, Server hoặc SQL/schema dùng chung trong lượt triển khai UI này. Những thay đổi chung cần review riêng.

## 2. Tổ chức thành phần

| Thành phần | Cách thực hiện dự kiến |
|---|---|
| Hồ sơ user | Giữ `AdminUserStore`, `UserRow`, validation và bảo vệ Admin cuối cùng. |
| Dữ liệu bổ sung | Thêm `AdminDemoData` và các record cho đăng nhập, cặp bạn bè, nhóm/thành viên, spam và sự kiện hoạt động; tham chiếu user bằng ID. |
| Thống kê | Tách `AdminStatistics` tính từ hồ sơ và dữ liệu mẫu; không để mỗi màn hình giữ một bộ tổng số độc lập. |
| Khung và điều hướng | Giữ `AdminMainView`, `AdminNavigation` và cơ chế giữ trang trong bộ nhớ; truyền cùng store/nguồn mẫu vào các trang. |
| Bảng và dialog | Tái sử dụng `AdminDialogs`, CSS và form hiện có; bổ sung lớp riêng cho màn hình/dialog còn thiếu. |
| Bộ lọc | Tách tiện ích khoảng ngày và điều kiện số dùng chung khi có từ hai màn hình cần; chỉ chia sẻ logic thực sự trùng. |

Các tên lớp mới là tên dự kiến, không phải API dùng chung với User/Server. Mã mới theo Allman; không định dạng lại toàn bộ code cũ.

## 3. Dòng dữ liệu và cập nhật màn hình

1. Khởi tạo một `AdminUserStore` và một `AdminDemoData` cho mỗi phiên Admin.
2. Sự kiện, quan hệ và báo cáo giữ ID; lấy tên/trạng thái mới nhất từ store khi hiển thị. User mới chưa có quan hệ hoặc sự kiện nên mặc định không có lịch sử, bạn bè và hoạt động.
3. Thao tác khóa từ spam gọi lại bảo vệ trạng thái của store; không sửa bản sao user trong màn hình spam.
4. Khi store đổi, các trang đã khởi tạo tính/lọc lại từ cùng dữ liệu. Cập nhật cả khi thêm, sửa, khóa và xóa mềm; giữ lựa chọn/bộ lọc hợp lệ nếu có thể.
5. Bản ghi mẫu bổ sung không bị xóa theo thao tác xóa mềm. A3/A8 lọc quan hệ hiện tại; A7/A9/A10/A11 giữ lịch sử theo spec.
6. Danh sách bạn hiện tại lấy từ đồ thị không có nút DELETED. FOF dùng tập hợp để loại trùng; tài khoản LOCKED không bị loại vì trạng thái khóa.

## 4. Thời gian và dữ liệu kiểm chứng

- Chuyển khoảng ngày UI sang đầu ngày bắt đầu và đầu ngày sau ngày kết thúc theo `Asia/Ho_Chi_Minh`; dùng khoảng đóng/mở để bao trọn ngày cuối.
- Logic thống kê nhận thời gian/nguồn dữ liệu làm đầu vào để kiểm tra độc lập với giao diện; dùng `Clock` cố định trong kiểm tra khi cần.
- Chuẩn bị dữ liệu qua hai năm, thời điểm biên, tháng rỗng, nhiều lượt OPEN_APP cùng user/tháng, nhiều tin tới cùng đối tượng và đồ thị bạn bè có vòng/đường trùng.
- Ghi trước kết quả mong đợi của một số trường hợp, không tính kỳ vọng bằng chính hàm đang kiểm tra.
- Cách chọn năm ban đầu vẫn cần Hải chốt: đề xuất năm hiện tại; danh sách gồm năm hiện tại và các năm có dữ liệu. Chưa khóa lựa chọn này vào code.

## 5. Thứ tự triển khai và kiểm tra

| Đợt | Phạm vi | Kiểm tra trọng tâm |
|---|---|---|
| 0 | Kiểm tra A1/A2, bổ sung nền dữ liệu tối thiểu | Build, store hiện có, dữ liệu tham chiếu ID hợp lệ. |
| 1 | A3–A4 | Đúng user, lịch sử khớp, lọc ngày biên, bạn LOCKED/DELETED, dialog rỗng. |
| 2 | A5–A6 | Đúng nhóm/thành viên/Admin; Hủy và xác nhận khóa; trạng thái khớp A2. |
| 3 | A7–A9 | Lịch sử đăng ký, FOF không trùng, ba chỉ số hoạt động, validation bộ lọc. |
| 4 | A10–A11 | Đủ 12 tháng, năm rỗng, đếm khác nhau giữa số phiên và số người. |
| 5 | Nghiệm thu và bàn giao | Liên thông giữa trang, hai theme, ảnh và nội dung báo cáo của Hải. |

Sau mỗi đợt: compile, kiểm tra logic mới phù hợp và xem giao diện sáng/tối. Kiểm tra A2 lại khi thay đổi store hoặc cách tích hợp; không đánh dấu PASS nếu chỉ đọc code.

## 6. Rủi ro và bàn giao

- Trang giữ trong bộ nhớ có thể hiển thị dữ liệu cũ: kiểm tra thay đổi user khi quay lại từng trang.
- Schema có truy vấn FOF chưa lọc DELETED ở mọi nút: ghi nhận để review backend sau; không tuyên bố bản SQL hiện đã đáp ứng quy tắc mới.
- SQL tạo database và test email là luồng kiểm chứng riêng; cần database trống dùng riêng, không áp dụng DDL lên database đang sử dụng.
- Không đổi lịch trình hoặc đánh dấu task trong `docs/plan.md` dùng chung khi chưa phối hợp Minh. Checklist Admin ghi trạng thái kiểm tra thực tế.
- Sau UI: Hải chuẩn bị ảnh, phần CSDL/Admin và minh chứng Git để Minh tổng hợp. Chưa tự merge, commit, push hoặc xuất bản bộ nộp chung.
