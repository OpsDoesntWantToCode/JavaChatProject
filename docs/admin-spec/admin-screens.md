# Đặc tả Admin — Mốc 20%

## 1. Phạm vi và trạng thái

- Người phụ trách: Hải, nhánh `feat/admin-db`. Mã nhiệm vụ A1–A11 theo [kế hoạch chung](../plan.md).
- Tài liệu này là bước **Specify**: yêu cầu, hành vi và tiêu chí nghiệm thu. Chưa phải kế hoạch kỹ thuật, danh sách task triển khai hoặc bằng chứng hoàn thành.
- Căn cứ: [đề bài gốc](../final-project.pdf), kế hoạch chung, [schema hiện hành](../database/schema.md), [chuẩn thiết kế Admin](admin-design-system.md) và các quyết định đã trao đổi với Hải.
- Đề bài gốc được ưu tiên nếu có khác biệt. Những lựa chọn ghi là đề xuất không được coi là yêu cầu đã duyệt.
- Đã đọc nội dung đề gốc: trang 2 liệt kê 9 nhóm Admin; trang 4 quy định mốc 20% chỉ cần giao diện, chưa cần lập trình sự kiện. Tương tác mô phỏng là lựa chọn bổ sung Hải đã đồng ý, không phải điều kiện bắt buộc của giảng viên.
- Phạm vi Hải: UI Admin có dữ liệu mẫu, SQL/thiết kế CSDL, phần báo cáo Admin/CSDL và minh chứng cá nhân. Minh tổng hợp báo cáo; tích hợp và đóng gói là việc chung.
- Chưa triển khai JDBC, Socket, xác thực thật, email, LLM, mã hóa thật, Docker hoặc đóng gói JAR trong đặc tả UI này.

## 2. Quyết định đã chốt với Hải

1. Các màn hình còn lại có tương tác mô phỏng vừa đủ, tương tự A2: điều hướng, lọc, sắp xếp, mở/đóng dialog và xác nhận thao tác.
2. Dữ liệu chỉ tồn tại trong bộ nhớ; khởi động lại trả về dữ liệu mẫu. Thông báo phải nói rõ thao tác mẫu, không tuyên bố cập nhật hệ thống thật.
3. Dùng cùng định danh người dùng giữa các màn hình. Khóa tài khoản từ báo cáo spam phải phản ánh trong quản lý người dùng; không tạo hai trạng thái độc lập cho cùng user.
4. Khoảng ngày tính cả ngày bắt đầu và kết thúc theo giờ Việt Nam. Một đầu để trống nghĩa là không giới hạn đầu đó; cả hai trống nghĩa là toàn bộ dữ liệu.
5. Ngày bắt đầu lớn hơn ngày kết thúc phải báo lỗi. Ngày nhập tay không hợp lệ phải báo lỗi, không âm thầm bỏ điều kiện lọc.
6. A9 cho chọn một trong ba chỉ số: lượt mở app, số người đã chat, số nhóm đã chat; sau đó chọn `=`, `<`, `>` và nhập số nguyên không âm.
7. Giữ chuẩn Admin sáng/tối hiện có. Không thay bằng hệ màu User chỉ vì tham khảo spec của Minh.
8. Xóa mềm chỉ đổi trạng thái thành DELETED, giữ hồ sơ, lịch sử và bản ghi quan hệ. A2 vẫn cho tra cứu hồ sơ đã xóa nhưng không sửa/khôi phục.
9. A7/A10 vẫn tính tài khoản đã khóa/xóa theo ngày đăng ký; A7 có cột trạng thái để giải thích. Trạng thái hiện tại không làm thay đổi số đăng ký quá khứ.
10. A9/A11 vẫn tính sự kiện hoạt động đã phát sinh của tài khoản hiện đã khóa/xóa. A9 chỉ hiển thị user có ít nhất một trong ba chỉ số lớn hơn 0 trong khoảng chọn, trước khi áp dụng các bộ lọc khác.
11. A3/A8 không hiển thị hoặc tính tài khoản DELETED trong quan hệ bạn bè hiện tại; tài khoản LOCKED vẫn giữ quan hệ và được tính. Với bạn bậc hai, loại tài khoản DELETED ở cả nút trung gian và đích. Đây là lọc hiển thị/tính toán, không xóa bản ghi quan hệ.

## 3. Quy tắc dùng chung

- Bảng có tiêu đề cột, số kết quả và nút xóa bộ lọc. Kết hợp các điều kiện lọc theo AND.
- Lọc văn bản bỏ khoảng trắng đầu/cuối, không phân biệt chữ hoa/thường; chưa yêu cầu tìm không dấu.
- Sắp xếp số và thời gian theo giá trị thực, không theo chuỗi hiển thị. Cho phép tăng/giảm qua tiêu đề cột.
- Trống ô số nghĩa là không áp dụng điều kiện số. Số âm, số thập phân hoặc chữ phải báo lỗi; không thay kết quả bằng danh sách có vẻ hợp lệ khi bộ lọc sai.
- Không có kết quả: hiện giải thích và cách xóa bộ lọc. Không có dòng được chọn: vô hiệu hóa thao tác cần đối tượng.
- Dialog có owner, tiêu đề xác định đúng đối tượng, nút Đóng/Hủy; hủy không làm thay đổi dữ liệu.
- Khóa/xóa cần xác nhận rõ username. Giữ bảo vệ Admin ACTIVE cuối cùng; hồ sơ DELETED chỉ được xem, không sửa/khóa/khôi phục.
- Mật khẩu chỉ dùng để kiểm tra form mẫu, không ghi vào model, file hoặc log; không hiển thị hash.
- Nút, bảng, popup, trạng thái rỗng và lỗi phải đọc được ở cả theme sáng/tối; có focus bàn phím và nhãn trạng thái bằng chữ.

## 4. Đặc tả màn hình và tiêu chí nghiệm thu

### A1 — Khung Admin

- Cửa sổ khoảng 1000×700, sidebar 9 mục, header danh tính mẫu và nút đăng xuất; đổi theme sáng/tối.
- Chuyển trang giữ dữ liệu mô phỏng trong phiên. Đăng xuất chỉ xác nhận đóng bản mẫu, không giả lập đã thu hồi session thật.
- **Nghiệm thu:** cả 9 mục mở đúng trang; tiêu đề khớp mục chọn; dialog theo theme hiện tại; không cắt mất thao tác khi phóng to cửa sổ.

### A2 — Quản lý người dùng

- Cột: ID, username, họ tên, email, trạng thái, ngày tạo. Lọc tên/username/trạng thái; sắp xếp tên/ngày tạo.
- Thêm/sửa hồ sơ mẫu; xem chi tiết; xóa mềm; khóa/mở khóa; kiểm tra form cập nhật mật khẩu. Không tạo tài khoản đăng nhập thật.
- Hồ sơ đủ username, họ tên, địa chỉ, ngày sinh, giới tính, email và trạng thái. Giữ validation và bảo vệ thao tác của store hiện có.
- Username duy nhất kể cả hồ sơ đã xóa; email duy nhất giữa ACTIVE/LOCKED theo schema hiện hành.
- **Nghiệm thu:** lọc kết hợp đúng; lưu hợp lệ cập nhật bảng; dữ liệu sai giữ dialog mở và báo lỗi; Hủy không sửa dữ liệu; không khóa/xóa Admin hoạt động cuối cùng.

### A3 — Các hộp thoại theo người dùng

- Tái sử dụng form hồ sơ đã có khi phù hợp; không bắt buộc đổi tên lớp chỉ để giống tên dự kiến trong kế hoạch.
- Cho mở lịch sử đăng nhập và danh sách bạn bè từ đúng user đang chọn; tiêu đề có username.
- Lịch sử gồm thời gian, username và họ tên; mới nhất trước. Bạn bè gồm username, họ tên và trạng thái online/offline mẫu.
- Lịch sử vẫn tra cứu được cho user đã khóa/xóa. Danh sách bạn bè hiện tại chỉ áp dụng cho chủ hồ sơ chưa xóa, loại bạn DELETED và giữ bạn LOCKED; hồ sơ chủ đã xóa hiển thị giải thích không còn quan hệ bạn bè hiện tại.
- Form mật khẩu có mật khẩu mới và xác nhận; sai/rỗng không báo thành công; hợp lệ chỉ thông báo kiểm tra mẫu.
- **Nghiệm thu:** chọn hai user khác nhau nhận dữ liệu tương ứng; user không có lịch sử/bạn bè có trạng thái rỗng; không lẫn dữ liệu sau khi đóng/mở dialog.

### A4 — Lịch sử đăng nhập chung

- Cột: thời gian đăng nhập, username, họ tên. Mặc định mới nhất trước.
- Lọc username và khoảng ngày; xóa bộ lọc trở về toàn bộ lịch sử mẫu.
- **Nghiệm thu:** dữ liệu đúng các ngày biên đều được tính; lọc user khớp dialog A3; có trường hợp không có kết quả.

### A5 — Quản lý nhóm

- Cột: tên nhóm, người tạo, ngày tạo, cờ mã hóa. Lọc tên, sắp xếp tên/ngày tạo.
- Mở chi tiết nhóm để xem thành viên và danh sách Admin nhóm; không bổ sung quyền sửa nhóm cho Admin hệ thống trong mốc này.
- Cờ mã hóa chỉ mô tả dữ liệu mẫu; không tuyên bố đã mã hóa thật hoặc E2EE.
- **Nghiệm thu:** chi tiết đúng nhóm; có nhóm thường/mã hóa và nhóm nhiều Admin; đổi dòng chọn không để lại nội dung nhóm trước.

### A6 — Báo cáo spam

- Cột: thời gian, người báo cáo, người bị báo cáo, lý do. Lọc khoảng ngày/username bị báo cáo; sắp xếp thời gian/username.
- Khóa người bị báo cáo sau xác nhận; không khóa nhầm người gửi báo cáo. Tài khoản LOCKED/DELETED hoặc Admin cuối cùng phải được giải thích vì sao không thể khóa tiếp.
- **Nghiệm thu:** Hủy không đổi trạng thái; xác nhận hợp lệ cập nhật cùng user tại A2; giữ nguyên báo cáo làm minh chứng mẫu.

### A7 — Người đăng ký mới

- Bảng hồ sơ đăng ký có username, họ tên, email, trạng thái, ngày tạo. Lọc tên/khoảng ngày; sắp xếp tên/ngày tạo. Bao gồm cả tài khoản hiện đã khóa/xóa.
- **Nghiệm thu:** dùng ngày tạo tài khoản; đổi khoảng ngày thay đổi kết quả; các thời điểm đầu/cuối ngày được xử lý đúng quy tắc chung.

### A8 — Thống kê bạn bè

- Cột: username, họ tên, ngày tạo, số bạn trực tiếp, số bạn của bạn. Lọc tên và số bạn trực tiếp bằng `=`, `<`, `>`; sắp xếp tên/ngày tạo/số liệu.
- Theo schema hiện hành: bạn của bạn là tập bậc hai không trùng, loại chính mình và bạn trực tiếp; không cộng lặp cùng một người qua nhiều đường.
- Đây là cách diễn giải của thiết kế nhóm cho cụm “tính luôn số lượng bạn của bạn” trong đề; nếu giảng viên yêu cầu tổng cả bậc một và bậc hai thì cần thống nhất lại trước khi đổi cách đếm.
- Loại user DELETED khỏi các dòng thống kê và toàn bộ đồ thị quan hệ dùng đếm bạn; không loại user LOCKED. Quan hệ gốc vẫn được giữ trong dữ liệu.
- **Nghiệm thu:** có user 0 bạn, có quan hệ vòng và nhiều đường tới cùng bạn bậc hai; kết quả đối chiếu được với danh sách A3.

### A9 — Người dùng hoạt động

- Cột: username, họ tên, ngày tạo, lượt mở app, số người đã chat, số nhóm đã chat.
- Lọc tên/khoảng ngày và một chỉ số số lượng theo mục 2; sắp xếp tên/ngày tạo/các chỉ số.
- Lượt mở app đếm sự kiện OPEN_APP; người/nhóm đã chat đếm đối tượng khác nhau mà user chủ động gửi tin trong khoảng chọn, không đếm tổng tin nhắn.
- Không loại sự kiện do trạng thái hiện tại của chủ thể hoặc đối tượng chat đã khóa/xóa; chỉ hiển thị chủ thể có ít nhất một chỉ số lớn hơn 0 trong khoảng chọn.
- **Nghiệm thu:** gửi nhiều tin cùng một người/nhóm không tăng số đối tượng; thay chỉ số lọc dùng đúng cột; khoảng ngày làm thay đổi số liệu và tập user hoạt động.

### A10 — Biểu đồ đăng ký

- Chọn năm; biểu đồ cột đủ 12 tháng. Trục X là tháng, Y là số tài khoản đăng ký; có cách xem giá trị số.
- Tính cả tài khoản hiện đã khóa/xóa, cùng chính sách với A7.
- **Nghiệm thu:** tháng không có đăng ký hiện giá trị 0; chọn năm khác cập nhật đủ 12 tháng; số liệu đối chiếu được với ngày tạo tại A7 theo cùng chính sách trạng thái.

### A11 — Biểu đồ hoạt động

- Chọn năm; biểu đồ cột đủ 12 tháng. Mỗi tháng đếm số user khác nhau có OPEN_APP, không đếm số phiên.
- Giữ sự kiện lịch sử của user hiện đã khóa/xóa, cùng chính sách với A9.
- **Nghiệm thu:** một user mở app nhiều lần trong tháng chỉ tính một; user xuất hiện ở hai tháng được tính ở từng tháng; năm rỗng vẫn đủ 12 giá trị 0.

## 5. Dữ liệu mẫu và bằng chứng cần chuẩn bị

- Giữ 10 hồ sơ gốc của A2; bổ sung lịch sử, quan hệ bạn bè, nhóm, báo cáo và sự kiện hoạt động tham chiếu cùng ID. Không dùng tài khoản hoặc thông tin đăng nhập thật.
- Có dữ liệu ở ít nhất hai năm, ngày biên, tháng không có sự kiện, user không có lịch sử và trường hợp lọc rỗng.
- Dữ liệu thống kê phải có thể đối chiếu với sự kiện/quan hệ mẫu; không hardcode số liệu mâu thuẫn giữa các trang.
- Hải cung cấp phần thiết kế CSDL/ERD và ảnh màn hình Admin cho Minh tổng hợp báo cáo; ảnh bao quát 9 nhóm chức năng và các dialog quan trọng.
- Chuẩn bị ảnh lịch sử commit của Hải trong `git/`; file văn bản lịch sử không thay thế ảnh nếu đề yêu cầu ảnh.
- SQL hiện hành cần được kiểm tra trên database trống. Kết quả kiểm thử baseline cũ không tự chứng minh thay đổi email mới đã PASS.
- Cấu trúc ZIP và báo cáo chung theo kế hoạch/đề gốc; không tự thay file dùng chung hoặc tạo báo cáo cuối khi chưa phối hợp Minh.

## 6. Hiện trạng quan sát và điểm cần thảo luận

Đối chiếu code tại `362f230`, không phải kết quả chạy lại:

| Phần | Quan sát |
|---|---|
| A1 | Đã có khung, điều hướng và theme; cần kiểm tra lại khi gắn các trang mới. |
| A2 | Đã có bảng, form hồ sơ, thao tác mẫu và validation; chưa nghiệm thu lại trong lượt viết spec. |
| A3 | Có một phần hồ sơ/mật khẩu trong A2; còn thiếu lịch sử và bạn bè theo user. |
| A4–A11 | Đang là trang xem trước trong khung Admin. |
| SQL/báo cáo/minh chứng | Có SQL/schema và test email trong repo; chưa xác nhận hoàn tất bộ nộp của Hải. |

### 6.1 Ảnh hưởng tới triển khai

- Các quy tắc trạng thái tại mục 2 đã được Hải đồng ý, thay thế các câu hỏi trước đây về chính sách thống kê.
- Truy vấn FOF minh họa trong schema hiện chỉ loại chủ thể DELETED, chưa loại nút trung gian/đích DELETED. Bước Plan phải ghi nhận chênh lệch này; khi triển khai backend cần review và đồng bộ tài liệu/truy vấn dùng chung. Lượt này chưa sửa schema hoặc SQL.
- Nghiệm thu bổ sung: xóa mềm một user mẫu làm giảm số bạn hiện tại phù hợp ở A3/A8 nhưng không làm mất đăng ký/lượt hoạt động cũ tại A7/A9/A10/A11; khóa user không làm mất quan hệ bạn bè.

### 6.2 Đề xuất còn mở cho bước Plan

- Thời điểm thêm user mẫu ở A2 dùng đồng hồ hiện tại; đề xuất chọn năm hiện tại ban đầu và cho chọn thêm các năm có dữ liệu, thay vì cố định một năm trong code. Chưa coi đây là quyết định đã duyệt.

Không đánh dấu nhiệm vụ UI hoàn thành chỉ vì đã viết tài liệu này. Sau khi chốt các điểm mở mới lập kế hoạch kỹ thuật, chia task và xin Hải bắt đầu implement.
