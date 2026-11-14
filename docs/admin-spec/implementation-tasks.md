# Checklist triển khai Admin — Mốc 20%

## Cách dùng

- Thực hiện theo [Plan](implementation-plan.md), đối chiếu tiêu chí trong [spec](admin-screens.md).
- Chưa task nào dưới đây được xác nhận hoàn thành. Code A1/A2 đã có không thay thế việc kiểm tra lại.
- Chỉ đánh dấu khi có kết quả thực tế; ghi lệnh/kịch bản, kết quả và giới hạn ngay dưới task liên quan.
- Chưa bắt đầu implement cho đến khi Hải đồng ý chuyển sang bước này.

## Đợt 0 — Nền hiện có và dữ liệu mẫu

- [ ] T00: Kiểm tra JDK/Maven, compile POM hiện tại và chạy `src/admin/check-users.ps1`; ghi rõ nếu môi trường không chạy được.
- [ ] T01: Chạy A1/A2 qua `src/admin/run-admin.ps1`, kiểm tra điều hướng, theme, form và bảo vệ Admin cuối cùng.
- [ ] T02: Thêm nguồn dữ liệu bổ sung theo ID; kiểm tra không có tham chiếu tới user không tồn tại, cặp bạn tự nối hoặc cặp trùng.
- [ ] T03: Gắn cùng nguồn mẫu/store vào khung; xác minh thay đổi user cập nhật được trang đã mở.

## Đợt 1 — A3/A4

- [ ] T04: Bổ sung dialog lịch sử và bạn bè theo user, nối vào thao tác A2; giữ form hồ sơ/mật khẩu đã có.
- [ ] T05: Dựng lịch sử đăng nhập chung, lọc username/ngày và sort thời gian; tái sử dụng lịch sử với T04.
- [ ] T06: Kiểm tra user khác nhau, user rỗng, ngày biên/sai, bạn khóa/xóa, đóng dialog và hai theme.

## Đợt 2 — A5/A6

- [ ] T07: Dựng bảng nhóm và dialog thành viên/Admin nhóm; lọc tên, sort tên/ngày.
- [ ] T08: Dựng báo cáo spam; lọc/sort và xác nhận khóa đúng người bị báo cáo qua store chung.
- [ ] T09: Kiểm tra Hủy không đổi dữ liệu, không khóa Admin cuối cùng/DELETED/LOCKED, A2 phản ánh trạng thái; kiểm tra hai theme.

## Đợt 3 — A7/A8/A9

- [ ] T10: Thêm logic thống kê có trường hợp kỳ vọng độc lập cho ngày biên, FOF, số đối tượng chat và OPEN_APP.
- [ ] T11: Dựng đăng ký mới với trạng thái, lọc ngày/tên, sort tên/ngày; giữ tài khoản đã xóa trong lịch sử.
- [ ] T12: Dựng thống kê bạn bè; lọc số trực tiếp, FOF không trùng và loại nút DELETED ở mọi bậc.
- [ ] T13: Dựng hoạt động với ba chỉ số, chọn cột lọc, toán tử/số và khoảng ngày; chỉ hiện user có hoạt động trong khoảng.
- [ ] T14: Kiểm tra cùng user trước/sau khóa và xóa mềm: quan hệ hiện tại đổi đúng, lịch sử giữ nguyên; kiểm tra dữ liệu rỗng, nhập sai và hai theme.

## Đợt 4 — A10/A11

- [ ] T15: Chốt với Hải cách chọn năm ban đầu; triển khai biểu đồ đăng ký từ cùng ngày tạo với A7.
- [ ] T16: Triển khai biểu đồ số user có OPEN_APP mỗi tháng; dùng cùng sự kiện với A9.
- [ ] T17: Kiểm tra đủ 12 tháng, năm không có dữ liệu, đổi năm, tháng giá trị 0 và một user mở app nhiều lần; kiểm tra hai theme.

## Đợt 5 — Nghiệm thu và phần nộp của Hải

- [ ] T18: Chạy kiểm tra liên thông toàn Admin, hồi quy A2, bàn phím, popup và cửa sổ chuẩn/phóng to; ghi phần chưa kiểm chứng.
- [ ] T19: Kiểm chứng SQL hiện hành và test email trên database trống dùng riêng; không lấy kết quả baseline cũ thay cho lần chạy mới.
- [ ] T20: Chụp ảnh 9 nhóm chức năng và dialog quan trọng; viết phần Admin/CSDL cho báo cáo, dùng ERD hiện hành.
- [ ] T21: Chuẩn bị ảnh commit thật của Hải; không tạo commit chỉ để đủ số lượng, không commit/push tự động.
- [ ] T22: Bàn giao Minh phần báo cáo/minh chứng; phối hợp review PR, build tích hợp và kiểm tra cấu trúc bộ nộp theo đề. Không tự merge hoặc đóng gói khi chưa phối hợp.

## Trạng thái SDD

| Bước | Trạng thái |
|---|---|
| Specify | Đã ghi yêu cầu và quyết định chính; các đề xuất còn mở được ghi riêng. |
| Plan | Đã có phương án kỹ thuật để review. |
| Tasks | Đã chia việc; chưa thực hiện các task trên. |
| Implement | Chưa bắt đầu. |
