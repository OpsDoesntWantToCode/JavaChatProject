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

- [x] T07: Dựng bảng nhóm và dialog thành viên/Admin nhóm; lọc tên, sort tên/ngày.
  - Ngày 15/11/2026: thêm `GroupManagementView`, `GroupDetailDialog`, nối mục Nhóm chat trong `AdminMainView`. Bảng gồm tên nhóm, người tạo, ngày tạo và cờ mã hóa mẫu; lọc tên bỏ khoảng trắng/không phân biệt hoa thường, sort tên theo tiếng Việt và thời gian theo giá trị thực.
  - Có xóa bộ lọc, số kết quả, trạng thái rỗng và nút Chi tiết bị vô hiệu khi chưa chọn. Mở chi tiết bằng nút/Enter/nhấp đúp dòng có dữ liệu; thay bộ lọc bỏ chọn dòng cũ để tránh mở nhầm nhóm.
  - Dialog tách bảng thành viên và bảng Admin nhóm, lấy vai trò từ membership thay vì quyền hệ thống. Hồ sơ và trạng thái cập nhật từ store chung; giữ dữ liệu membership mẫu, hiển thị rõ tài khoản LOCKED/DELETED. Gỡ listener khi đóng; chỉ xem, không sửa nhóm hoặc thực hiện mã hóa.
  - Chạy `check-demo-data.ps1` theo lệnh T02: **BUILD SUCCESS, 64 PASS A5** (32 mỗi theme), cùng **28 PASS dữ liệu + 94 PASS JavaFX hồi quy T03/A3/A4**. Kiểm tra đủ 3 nhóm, nhóm nhiều Admin, lọc rỗng, sort tăng/giảm, giữ trang/bộ lọc, cập nhật hồ sơ/trạng thái, owner/theme và đóng dialog; dữ liệu nhóm không đổi.
  - Kiểm tra trực quan qua Computer Use ở cửa sổ chuẩn: trang nhóm và dialog Nhóm học tập ở cả hai theme, đúng 4 thành viên/2 Admin; cờ mã hóa ghi rõ là mẫu. Tăng chiều cao bảng Admin từ 100 lên 124 để hai dòng hiển thị trọn vẹn, chạy lại suite và kiểm tra bố cục cuối ở sáng/tối. Phiên chạy đóng bình thường, không có exception trong output.
  - Giới hạn: chưa kiểm thử mọi DPI/kích thước hoặc trình đọc màn hình. T08 và kiểm tra khóa từ spam T09 chưa làm; không kiểm thử DB/Server hoặc mã hóa thật.
- [x] T08: Dựng báo cáo spam; lọc/sort và xác nhận khóa đúng người bị báo cáo qua store chung.
  - Ngày 15/11/2026: thêm `SpamReportView`, nối mục Báo cáo spam trong khung. Bảng có thời gian, người báo cáo, người bị báo cáo, lý do và trạng thái tài khoản bị báo cáo; lọc username bị báo cáo/khoảng ngày, sort thời gian/username, có xóa bộ lọc và trạng thái rỗng/lỗi.
  - Tách `AdminDateRange` dùng chung A4/A6, giữ parse nghiêm ngặt và quy tắc ngày theo giờ Việt Nam; các kiểm tra A4 cũ vẫn PASS. Chọn báo cáo hiển thị đầy đủ lý do; tài khoản LOCKED/DELETED hoặc Admin cuối cùng bị vô hiệu hóa nút khóa và có giải thích.
  - Xác nhận nêu đúng username bị báo cáo, Hủy là mặc định an toàn. Sau xác nhận đọc lại cùng ID và kiểm tra lại trạng thái trước khi gọi store; không khóa nhầm người gửi báo cáo, không mở khóa hoặc ghi đè trạng thái vừa đổi ở nơi khác. Giữ nguyên bản ghi báo cáo.
- [x] T09: Kiểm tra Hủy không đổi dữ liệu, không khóa Admin cuối cùng/DELETED/LOCKED, A2 phản ánh trạng thái; kiểm tra hai theme.
  - Chạy `check-demo-data.ps1` theo lệnh T02: **BUILD SUCCESS, 94 PASS A6/xác nhận** (47 mỗi theme), cùng **28 PASS dữ liệu + 158 PASS JavaFX hồi quy T03/A3/A4/A5**. Tổng JavaFX cuối là 252 kiểm tra.
  - Kiểm tra bộ lọc AND, ngày sai/khoảng đảo, một đầu trống, mốc UTC sang ngày Việt Nam, dữ liệu rỗng, sort và rename theo ID. Giữ trang/bộ lọc, giải thích đầy đủ, Hủy không mutation, xác nhận chỉ đổi đúng target một lần; báo cáo vẫn còn và A2 đã mở nhận trạng thái mới.
  - Dùng fixture bổ sung chỉ trong test để kiểm tra báo cáo Admin cuối cùng. Hộp xác nhận thật được kiểm tra owner/theme, Hủy mặc định; mô phỏng target đã bị khóa trong lúc hộp mở và xác nhận không ghi thêm thay đổi. Đây không phải kiểm thử concurrency của Server/DB.
  - Kiểm tra trực quan bảng và hộp xác nhận ở cả hai theme: Hủy giữ an.tran ACTIVE; xác nhận khóa ở theme tối làm A2 có 2 tài khoản đã khóa và an.tran hiển thị LOCKED. Báo cáo vẫn giữ 3/3, nút khóa tiếp bị vô hiệu.
  - Phát hiện nhãn giải thích bị cắt dòng; đặt chiều cao tối thiểu theo nội dung cho nhãn lý do/feedback, thêm kiểm tra bố cục và chạy lại suite. Kiểm tra lại bằng mắt ở hai theme: lý do và giải thích tài khoản đã xóa hiển thị đầy đủ. Phiên UI đóng bình thường, không có exception trong output.
  - Giới hạn: chưa kiểm tra mọi DPI/kích thước, trình đọc màn hình, DB/Server hoặc thu hồi phiên đăng nhập thật. Tất cả thao tác chỉ trên dữ liệu mẫu trong bộ nhớ.

## Đợt 3 — A7/A8/A9

- [x] T10: Thêm logic thống kê có trường hợp kỳ vọng độc lập cho ngày biên, FOF, số đối tượng chat và OPEN_APP.
  - Ngày 16/11/2026: thêm `AdminStatistics` với `registrations`, `friendStatistics`, `activeUsers`. Mỗi lần gọi đọc nguồn mẫu/store hiện tại, trả danh sách snapshot bất biến; không cache hoặc tự thay đổi dữ liệu.
  - Đăng ký giữ hồ sơ LOCKED/DELETED và lọc theo `AdminDateRange`. Bạn bè xây đồ thị không có nút DELETED, giữ LOCKED, đếm bậc hai bằng tập hợp rồi loại chính mình/bạn trực tiếp. Hoạt động đếm OPEN_APP và hai tập ID riêng cho người/nhóm đã chủ động chat; chỉ trả user có sự kiện trong khoảng chọn, giữ cả lịch sử của user hiện đã khóa/xóa.
  - Lệnh: `powershell -NoProfile -ExecutionPolicy Bypass -File src/admin/check-statistics.ps1 -MavenCommand ./build/tools/apache-maven-3.9.11/bin/mvn.cmd`.
  - Kết quả: **BUILD SUCCESS, 54 PASS** kiểm tra logic. Kỳ vọng số bạn cho cả 9 user chưa xóa được ghi sẵn bằng tay; kiểm tra quan hệ vòng/đường trùng, nút trung gian đã xóa, đối chiếu số bạn trực tiếp với A3, user 0 bạn và user chỉ nhận tin.
  - Kiểm tra thêm ngày biên và một đầu trống, timestamp UTC ở đầu/cuối ngày Việt Nam (cả phần giây lẻ), nhiều lượt mở app/nhiều tin cùng đối tượng, rename/khóa/xóa mềm, user mới theo Clock cố định, snapshot không sửa được và dữ liệu gốc không đổi.
  - Giới hạn: chưa dựng/nối màn hình A7–A9, chưa thống kê theo tháng cho A10/A11. Lượt này không khởi tạo Toolkit hoặc kiểm tra UI/DB/Server; T11–T13 cần lắng nghe store và gọi lại logic khi dữ liệu thay đổi.
- [x] T11: Dựng đăng ký mới với trạng thái, lọc ngày/tên, sort tên/ngày; giữ tài khoản đã xóa trong lịch sử.
  - Ngày 16/11/2026: thêm `NewUsersView`, nối mục Người dùng đăng ký mới; cột username, họ tên, email, trạng thái và ngày tạo. Dùng `AdminStatistics.registrations` và `AdminDateRange`, lọc AND tên/ngày, mặc định mới nhất trước, sort tên tiếng Việt/ngày tạo qua tiêu đề cột.
  - Có xóa bộ lọc, số kết quả, trạng thái rỗng và lỗi ngày; user đã khóa/xóa vẫn nằm trong lịch sử. Listener gọi lại thống kê khi store đổi, giữ bộ lọc và trang khi chuyển mục.
  - `check-demo-data.ps1`: **BUILD SUCCESS, 34 PASS A7** (17 mỗi theme), cùng **28 PASS dữ liệu + 252 PASS JavaFX hồi quy**. Kiểm tra ngày biên/một đầu trống, tên kết hợp ngày, ngày sai/khoảng đảo, sort tăng/giảm, rename lúc trang ẩn, khóa/xóa mềm và user mới xuất hiện ngay.
  - Đã chạy `run-admin.ps1` và build thành công. Chưa kiểm tra trực quan: Computer Use lỗi `native pipe is unavailable` (os error 2), thử lại và reset runtime vẫn không kết nối. Dừng phiên lệnh chạy UI bằng Ctrl+C; đây không phải lỗi build. Cần kiểm tra trực quan bố cục, lịch chọn ngày và hai theme ở T14 khi công cụ hoạt động lại.
  - Chưa kiểm tra DB/Server, mọi DPI hoặc trình đọc màn hình. Kết quả trên là kiểm tra node/chức năng, không thay thế kiểm tra trực quan.
- [x] T12: Dựng thống kê bạn bè; lọc số trực tiếp, FOF không trùng và loại nút DELETED ở mọi bậc.
  - Ngày 16/11/2026: thêm `FriendStatisticsView`, nối mục Thống kê bạn bè. Dùng `AdminStatistics`, lọc AND họ tên/số bạn trực tiếp, toán tử =/< />; ô số trống bỏ điều kiện, số âm/thập phân/chữ hoặc quá lớn báo lỗi và không hiện kết quả cũ.
  - Có xóa bộ lọc, số kết quả, sort tên/ngày/số liệu. Listener tính lại khi store đổi: giữ LOCKED, loại DELETED khỏi chủ thể và đường đi bậc hai, user mới có 0 bạn.
  - `check-demo-data.ps1`: **BUILD SUCCESS, 40 PASS A8** (20 mỗi theme), cùng **28 PASS dữ liệu + 286 PASS JavaFX hồi quy**. Kiểm tra toán tử/0 bạn, nhập sai, lọc kết hợp, sort số/ngày, rename lúc trang ẩn, khóa/xóa nút trung gian, giữ dữ liệu quan hệ gốc và user mới.
  - Chưa kiểm tra trực quan A8; ghi vào phần cần kiểm tra tại T14 cùng A7. Sau lượt kiểm tra, Hải yêu cầu cài và lập hướng dẫn hai skill UI/UX; chưa áp dụng một lượt chỉnh giao diện theo skill hoặc thực hiện T13.
- [x] T13: Dựng hoạt động với ba chỉ số, chọn cột lọc, toán tử/số và khoảng ngày; chỉ hiện user có hoạt động trong khoảng.
  - Ngày 16/11/2026: thêm `ActiveUsersView`, nối mục Người dùng hoạt động. Sáu cột theo spec; dùng `AdminStatistics.activeUsers` và `AdminDateRange`, lọc AND tên/ngày/chỉ số, toán tử =/< />. Số trống bỏ điều kiện số; số âm/thập phân/chữ/quá lớn và ngày sai/khoảng đảo báo lỗi, không giữ kết quả cũ.
  - Hai hàng bộ lọc có nhãn liên kết control, nút xóa bộ lọc, số kết quả trong khoảng ngày, trạng thái rỗng và giải thích cách đếm. Dùng lại bảng/CSS Admin, sort tên tiếng Việt/ngày/số theo giá trị thực; giữ trang/bộ lọc và tính lại khi store đổi, kể cả lúc trang ẩn.
  - Đã đối chiếu taste về căn lề, một hệ thống thành phần và nội dung cụ thể; Pro Max truy vấn `error validation feedback --domain ux` cho lỗi rõ và cách khắc phục. Chỉ tiếp nhận khuyến nghị phù hợp desktop, không dùng mã web hoặc thay thiết kế đã chốt; nhãn lỗi/ghi chú giữ chiều cao theo nội dung.
  - Lệnh `check-demo-data.ps1` với Maven như T02: **BUILD SUCCESS, 64 PASS A9** (32 mỗi theme), cùng **28 PASS dữ liệu + 326 PASS JavaFX hồi quy**; tổng **390 PASS JavaFX**. Kiểm tra đổi cả ba chỉ số, =/< />, số 0/trống/sai, ngày biên/một đầu trống/rỗng/sai, sort tăng/giảm, rename khi ẩn, chủ thể/đối tượng đã khóa/xóa và user mới không có sự kiện. Dữ liệu gốc giữ nguyên 15 sự kiện.
  - Giới hạn: đây là kiểm tra node/chức năng, chưa xem trực quan A9, lịch popup, DPI hoặc trình đọc màn hình; phần trực quan A7–A9 chờ T14. Không kiểm tra DB/Server, không commit/push.
- [x] T14: Kiểm tra cùng user trước/sau khóa và xóa mềm: quan hệ hiện tại đổi đúng, lịch sử giữ nguyên; kiểm tra dữ liệu rỗng, nhập sai và hai theme.
  - **Đã đạt phần chức năng và trực quan trong phạm vi ghi dưới đây.** Ngày 16/11/2026: bổ sung kịch bản `statisticsIntegrationChecks` trong `check-demo-data.ps1`, mở A7/A8/A9 từ cùng một khung/store và giữ cả ba trang trong bộ nhớ. Lọc chính xác Tuấn Minh, đối chiếu trước/sau Hủy khóa, xác nhận khóa và xác nhận xóa mềm từ nút A2.
  - Kiểm tra dialog đúng chủ thể/owner/theme, Hủy là mặc định; khóa giữ nguyên số liệu, xóa giữ lịch sử A7/A9 nhưng loại chủ thể/nút trung gian khỏi A8. Các trang ẩn cập nhật ngay, giữ trang/bộ lọc; xóa bộ lọc trả đúng tập 10 hồ sơ đăng ký / 8 chủ thể quan hệ / 5 chủ thể hoạt động. Giữ nguyên 9 quan hệ và 15 sự kiện gốc.
  - Kiểm tra thêm bộ lọc rỗng, nhập sai, phục hồi và chiều cao cấp cho nhãn lỗi/ghi chú so với nội dung tại Scene 1000×700. Đây là kiểm tra node/bố cục, không phải bằng chứng nhìn thấy giao diện.
  - Lệnh `check-demo-data.ps1` với Maven như T02: **BUILD SUCCESS, 94 PASS T14** (47 mỗi theme), cùng **28 PASS dữ liệu + 390 PASS JavaFX hồi quy**; tổng **484 PASS JavaFX**. Lần đầu kịch bản dùng tên `minh` khớp cả Minh Châu nên kỳ vọng một dòng sai; sửa truy vấn test thành `tuấn minh`, không sửa hành vi tìm kiếm của app. Chỉ kết quả chạy cuối được tính PASS.
  - Các lần trước bị lỗi kết nối Computer Use; ngày 16/11/2026 lúc 12:03, công cụ đã hoạt động và kiểm tra trực tiếp bản chạy từ `run-admin.ps1` (BUILD SUCCESS). Đã xem bảng, bộ lọc và ghi chú A7–A9 ở cả theme sáng/tối tại cửa sổ chuẩn, đối chiếu chuẩn Admin và nguyên tắc phù hợp desktop của hai skill. Không dùng browser gate để kiểm tra JavaFX.
  - A7: chọn 01/11/2026 bằng lịch Từ ngày trả 2/10 hồ sơ; nhập 31/02/2026 báo lỗi và giữ lỗi sau Tab, focus chuyển sang Đến ngày. Chuyển tối thấy lỗi rõ; xóa bộ lọc trả 10/10. Đã mở lịch sáng và lịch Đến ngày tối, đóng popup bằng Escape.
  - A8: theme tối nhập -1 báo lỗi đầy đủ; = 0 trả 3/9, dùng Down/Enter trong ComboBox chuyển sang < 0 trả 0/9 và trạng thái rỗng rõ. Chuyển sáng vẫn giữ điều kiện; xóa bộ lọc trả 9/9. Bảng và ghi chú nhìn rõ ở cả hai theme.
  - A9: theme sáng đổi ba chỉ số với = 0 lần lượt trả 1/5, 1/5, 4/5; đổi chỉ số bằng chuột và phím Down. Nhập 1.5 báo lỗi và bỏ kết quả cũ; xóa bộ lọc trả 5/5. Chọn Từ ngày 03/11/2026 trả 3/3. Theme tối mở lịch Đến ngày, chọn 01/11/2026 báo khoảng đảo rõ; xóa bộ lọc phục hồi 5/5. Tab từ nút xóa chuyển focus sang bảng, viền rõ. Phóng to 1920×1032 thấy cột giãn và tên dài đầy đủ; ở cửa sổ chuẩn một số ô dài rút gọn bằng dấu ba chấm theo thành phần bảng hiện có.
  - Không phát hiện lỗi cần sửa trong lượt trực quan này; đóng Admin bình thường, tiến trình thoát mã 0, không có exception trong output. Chưa kiểm tra mọi tổ hợp bộ lọc bằng tay, toàn bộ đường Tab/Shift+Tab, mọi DPI, trình đọc màn hình, DB/Server hoặc chụp bộ ảnh báo cáo; nghiệm thu rộng hơn vẫn thuộc T18/T20.

## Đợt 4 — A10/A11

- [ ] T15: Chốt với Hải cách chọn năm ban đầu; triển khai biểu đồ đăng ký từ cùng ngày tạo với A7.
  - Hải đã đồng ý ngày 16/11/2026: mặc định năm hiện tại, cho chọn thêm các năm có dữ liệu. Phần triển khai biểu đồ chưa thực hiện.
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
| Tasks | T00–T14 đã hoàn tất theo phạm vi và giới hạn kiểm chứng ghi trên. T15 đã chốt cách chọn năm, chưa triển khai; T16–T22 chưa thực hiện. |
| Implement | Đã có A3–A9 và logic thống kê T10; chưa dựng A10/A11. |
