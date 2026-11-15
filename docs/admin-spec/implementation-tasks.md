# Checklist triển khai Admin — Mốc 20%

## Cách dùng

- Thực hiện theo [Plan](implementation-plan.md), đối chiếu tiêu chí trong [spec](admin-screens.md).
- Trạng thái dưới đây phản ánh các kiểm tra thực tế; code A1/A2 đã có không thay thế việc kiểm tra lại.
- Chỉ đánh dấu khi có kết quả thực tế; ghi lệnh/kịch bản, kết quả và giới hạn ngay dưới task liên quan.
- Hải đã đồng ý bắt đầu Implement; không tự commit/push các thay đổi tiếp theo.

## Đợt 0 — Nền hiện có và dữ liệu mẫu

- [x] T00: Kiểm tra JDK/Maven, compile POM hiện tại và chạy `src/admin/check-users.ps1`; ghi rõ nếu môi trường không chạy được.
  - Ngày 14/11/2026, tại commit `fa6c513`: Temurin JDK 21.0.12.1, Maven 3.9.11 trong `build/tools/`, JavaFX 21.0.5 theo POM. Maven chưa có trên PATH nên truyền `-MavenCommand` cho script.
  - Lệnh: `powershell -NoProfile -ExecutionPolicy Bypass -File src/admin/check-users.ps1 -MavenCommand ./build/tools/apache-maven-3.9.11/bin/mvn.cmd`.
  - Kết quả: **BUILD SUCCESS, 45 PASS** kiểm tra store trong bộ nhớ, bao gồm Admin cuối cùng, xóa mềm và tái sử dụng email. Không kiểm tra DB/Server.
  - Lần chạy sandbox đầu không truy cập được cache Maven; chạy lại ngoài sandbox thành công. Không đổi POM, dependencies hoặc execution policy hệ thống; tùy chọn Bypass chỉ áp dụng tiến trình chạy script.
- [x] T01: Chạy A1/A2 qua `src/admin/run-admin.ps1`, kiểm tra điều hướng, theme, form và bảo vệ Admin cuối cùng.
  - Lệnh cùng cách truyền Maven như T00, thay tên script bằng `run-admin.ps1`: **BUILD SUCCESS**; cửa sổ Admin mở và đóng bình thường, không có exception trong đầu ra phiên chạy.
  - Kiểm tra trực quan qua Computer Use ở cửa sổ chuẩn: bảng 10 user hiển thị ở theme sáng/tối; lọc `hai.admin` được 1/10; chọn Admin cuối cùng thấy Khóa/Xóa mềm disabled và lý do bằng chữ.
  - Theme tối: mở/đóng hồ sơ chỉ xem đúng Hải; form thêm trống báo lỗi username, giữ dialog mở; Hủy không đổi tổng 10 hồ sơ. Chuyển sang trang xem trước lịch sử, biểu đồ hoạt động rồi quay lại A2 giữ bộ lọc; Xóa bộ lọc trả 10/10.
  - Giới hạn: chưa chạy đủ CRUD UI, form mật khẩu, toàn bộ 9 mục, mọi popup ở cả hai theme, DPI/phóng to hoặc trình đọc màn hình. Bảo vệ và mutation store được kiểm tra bằng T00; T01 là kiểm tra UI cơ bản, không phải nghiệm thu cuối T18. Hai lần thử mở trực tiếp trước script không tạo cửa sổ để kiểm tra; không tính là PASS.
- [x] T02: Thêm nguồn dữ liệu bổ sung theo ID; kiểm tra không có tham chiếu tới user không tồn tại, cặp bạn tự nối hoặc cặp trùng.
  - Ngày 15/11/2026: thêm `AdminDemoData`, giữ nguyên 10 hồ sơ A2. Nguồn mẫu gồm 11 lần đăng nhập, 9 cặp bạn, 3 nhóm, 11 thành viên nhóm, 3 báo cáo và 15 sự kiện mở app/chat; trạng thái online là dữ liệu mẫu.
  - Các bản ghi chỉ giữ ID, không sao chép hồ sơ user. Tra cứu tên/trạng thái từ store hiện tại; danh sách lịch sử/quan hệ bất biến, user mới chưa có sự kiện được trả danh sách rỗng.
  - Lệnh: `powershell -NoProfile -ExecutionPolicy Bypass -File src/admin/check-demo-data.ps1 -MavenCommand ./build/tools/apache-maven-3.9.11/bin/mvn.cmd`.
  - Kết quả cuối: **BUILD SUCCESS, 28 PASS** kiểm tra dữ liệu, gồm tham chiếu sai, cặp trùng/tự nối/đảo thứ tự, thời gian trước đăng ký, thiếu Admin nhóm, báo cáo thiếu tin nhắn trước đó, đổi tên/khóa/xóa mềm và reset phiên.
  - Giới hạn dữ liệu: lịch sử hiện chỉ trong năm 2026 vì 10 hồ sơ gốc đều được tạo năm đó; chưa thêm dữ liệu năm thứ hai hoặc quyết định năm mặc định. Bổ sung khi chuẩn bị A10/A11, không tạo sự kiện năm 2025 trước ngày đăng ký.
- [x] T03: Gắn cùng nguồn mẫu/store vào khung; xác minh thay đổi user cập nhật được trang đã mở.
  - `AdminMainView` giữ một `AdminDemoData` mỗi phiên, A2 dùng đúng store bên trong; có constructor nội bộ nhận nguồn mẫu để kiểm tra tích hợp. Giữ entrypoint công khai, POM, CSS và các trang xem trước hiện có.
  - Script T02 chạy thêm **32 PASS kiểm tra node JavaFX (16 mỗi theme)**: dùng cùng store, giữ trang/bộ lọc khi chuyển mục, sửa user lúc trang ẩn, khóa từ store, thêm/xóa mềm và lọc trạng thái.
  - Phát hiện và sửa lỗi A2 mất selectedItem khi thay record user: ghi ID trước khi FilteredList/SortedList xử lý rồi chọn lại bản ghi mới nếu vẫn nằm trong bộ lọc. Kiểm tra nút Mở khóa và số tổng cập nhật; nếu user bị lọc ẩn thì bỏ chọn/vô hiệu hóa thao tác, không chọn nhầm dòng khác.
  - Chạy lại `check-users.ps1` trên code cuối: **BUILD SUCCESS, 45 PASS**. `git diff --check`: PASS.
  - Lần kiểm tra đầu phát hiện chuỗi kỳ vọng tiếng Việt bị Windows PowerShell 5 đọc sai encoding; script dùng escape Unicode trong Java để khắc phục. Các lần phát hiện lỗi selection chưa tính là PASS; số trên là lần chạy cuối sau sửa.
  - Giới hạn: kiểm tra tích hợp tạo Scene/applyCss/layout, không chụp hoặc kiểm tra trực quan UI trong lượt này; không kiểm tra DB/Server. Các trang A3–A11 chưa triển khai nên chưa nghiệm thu liên thông toàn Admin.

## Đợt 1 — A3/A4

- [x] T04: Bổ sung dialog lịch sử và bạn bè theo user, nối vào thao tác A2; giữ form hồ sơ/mật khẩu đã có.
  - Ngày 15/11/2026: thêm `UserLoginHistoryDialog`, `UserFriendsDialog`, các nút Lịch sử đăng nhập/Bạn bè trong A2; cần chọn user trước khi mở. Hồ sơ DELETED vẫn tra cứu lịch sử, bạn bè hiện tại rỗng và có giải thích.
  - Hai dialog có owner/theme, kết quả đóng trả null; gỡ listener store khi đóng. Dùng ID để lấy dữ liệu, không đổi form hồ sơ/mật khẩu hiện có.
- [x] T05: Dựng lịch sử đăng nhập chung, lọc username/ngày và sort thời gian; tái sử dụng lịch sử với T04.
  - Thêm `GlobalLoginHistoryView`: cùng thành phần bảng dùng trong dialog A3, trang chung có lọc username/khoảng ngày, xóa bộ lọc, đếm kết quả và trạng thái rỗng/lỗi.
  - Ngày theo giờ Việt Nam, tính cả hai ngày biên; cho để trống một/hai đầu. Parse nghiêm ngặt dd/MM/yyyy từ ô nhập; sai ngày/khoảng đảo thì báo lỗi và không hiện kết quả cũ. Commit ngày sai không ném exception ra UI.
  - Thời gian sort theo OffsetDateTime; mặc định mới nhất trước. Store đổi tên/họ tên thì bảng cập nhật và áp dụng lại bộ lọc. CSS bảng được dùng chung qua `.admin-data-view`, không thêm bảng màu riêng.
- [x] T06: Kiểm tra user khác nhau, user rỗng, ngày biên/sai, bạn khóa/xóa, đóng dialog và hai theme.
  - Chạy `check-demo-data.ps1` với cách truyền Maven như T02: **BUILD SUCCESS, 28 PASS dữ liệu + 32 PASS T03 + 62 PASS A3/A4** (31 kiểm tra mới mỗi theme).
  - Chạy hồi quy `check-users.ps1` sau tích hợp: **BUILD SUCCESS, 45 PASS A2**. `git diff --check` và liên kết tài liệu Admin: PASS.
  - A3/A4 kiểm tra đúng Minh/Dũng/Phúc, lịch sử tài khoản đã xóa, bạn bè rỗng/khóa/xóa, dialog owner/theme/kết quả đóng; thay store trong lúc dialog bạn bè mở cập nhật ngay. Kiểm tra ngày biên, một đầu trống, ngày sai và commit, khoảng đảo, sort tăng/giảm, xóa bộ lọc và tên mới theo ID.
  - Kiểm tra trực quan ở cửa sổ chuẩn: A2 hiển thị đủ hai nút mới (thanh thao tác xuống hai hàng); nút mở dialog đúng user. Dialog lịch sử Minh 4 dòng ở theme sáng; dialog bạn bè Minh 4 dòng ở cả hai theme, giữ bạn Ngọc LOCKED/Offline.
  - Trang A4 kiểm tra trực quan ở cả hai theme, lịch DatePicker ở theme tối, nhập ngày 31/02/2026 hiện lỗi và bảng rỗng; Xóa bộ lọc khôi phục 11/11. Đóng phiên chạy bình thường, không có exception trong output.
  - Giới hạn: chưa kiểm thử trình đọc màn hình, mọi mức DPI/kích thước hoặc mọi tổ hợp popup bằng mắt. Theme của dialog lịch sử tối được kiểm tra tự động; chưa kiểm tra trực quan trường hợp đó. Không kiểm tra DB/Server hoặc coi đây là nghiệm thu toàn hệ thống.

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
| Tasks | T00–T06 hoàn thành theo phạm vi ghi ở trên; T07–T22 chưa thực hiện. |
| Implement | Đã có nguồn dữ liệu chung, A3 và A4; chưa triển khai A5–A11. |
