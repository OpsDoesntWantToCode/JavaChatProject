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

- [x] T15: Chốt với Hải cách chọn năm ban đầu; triển khai biểu đồ đăng ký từ cùng ngày tạo với A7.
  - Ngày 16/11/2026: thêm `MonthlyStatisticsView` dùng chung A10/A11 và `AdminStatistics.registrationsByMonth`. Mặc định năm hiện tại theo giờ Việt Nam, danh sách gồm năm hiện tại và năm có dữ liệu; có thể nhập năm 1–9999 để xem cả năm rỗng. Clock được tiêm khi kiểm tra, không cố định năm mặc định trong code.
  - A10 dùng ngày tạo của cùng store A7, giữ hồ sơ khóa/xóa; đủ 12 tháng, trục Y từ 0 với bước nguyên, tooltip và 12 giá trị bằng chữ. CSS biểu đồ dùng màu Admin hiện có. Listener cập nhật khi store đổi và giữ năm đang xem.
- [x] T16: Triển khai biểu đồ số user có OPEN_APP mỗi tháng; dùng cùng sự kiện với A9.
  - A11 dùng `activeUsersByMonth` trên cùng sự kiện A9, tập ID riêng cho từng tháng. Một người mở nhiều lần vẫn tính một trong tháng; có hoạt động ở hai tháng được tính ở cả hai. Không tính tin nhắn hoặc tổng phiên, không loại lịch sử do trạng thái hiện tại.
- [x] T17: Kiểm tra đủ 12 tháng, năm không có dữ liệu, đổi năm, tháng giá trị 0 và một user mở app nhiều lần; kiểm tra hai theme.
  - `check-statistics.ps1` với Maven như T10: **BUILD SUCCESS, 88 PASS** logic (gồm 54 kiểm tra trước đó). Có kỳ vọng độc lập từng tháng, năm rỗng, thời điểm UTC chuyển sang đầu năm Việt Nam, trùng lượt mở/tháng và một người ở hai tháng, snapshot bất biến và khóa/xóa giữ lịch sử.
  - `check-demo-data.ps1` với Maven như T02: **BUILD SUCCESS, 66 PASS A10/A11** (33 mỗi theme), cùng **28 PASS dữ liệu + 484 PASS JavaFX hồi quy**; tổng **550 PASS JavaFX**. Kiểm tra năm mặc định thực tế và Clock 2050, nguồn hai năm 2026/2027, đúng thứ tự 12 tháng, số liệu bằng chữ, năm sai/rỗng, khôi phục năm ban đầu, cache trang và giữ lịch sử khi khóa/xóa.
  - Bộ kiểm tra hai năm dùng store với Clock 2027 để thêm hồ sơ mới rồi thêm sự kiện hợp lệ sau đăng ký, không đổi 10 hồ sơ gốc. Phiên demo chuẩn vẫn chỉ có dữ liệu gốc năm 2026; không đưa sự kiện tương lai giả vào phiên chuẩn. Nếu cần ảnh báo cáo với hai năm có số liệu, phải chuẩn bị bộ mẫu nhất quán và đối chiếu lại các trang tại T20.
  - Kiểm tra trực quan bằng Computer Use: A10 sáng/tối đủ 12 tháng, tổng 10 đăng ký; nhập 2025 hiện đủ 12 số 0 rồi phục hồi 2026. A11 sáng/tối thấy tháng 10 = 1, tháng 11 = 4, các tháng khác = 0; năm sai báo lỗi rõ, chọn năm từ popup phục hồi đúng. Nhãn trục, số theo tháng và ghi chú không bị cắt ở cửa sổ chuẩn. Bản chạy cuối đóng bình thường, mã thoát 0, không có exception trong output.
  - Phát hiện và sửa: không ghi đè `accessibleText` đã được BarChart bind; khởi tạo ô năm rõ ràng; đồng bộ văn bản nhập và giá trị ComboBox để chọn lại năm ban đầu sau nhập sai có hiệu lực. Bộ test giữ Toolkit sống giữa các stage tạm và ghi nhận exception JavaFX qua handler, tránh bỏ sót lỗi. Kết quả PASS trên là lần chạy cuối sau sửa, không tính các lần lỗi trước đó.
  - Hai truy vấn chart của Pro Max trả kết quả radar không phù hợp; không tiếp nhận kết quả này làm quy tắc. Dùng BarChart đã chốt trong spec và nguyên tắc một hệ thống/nhãn cụ thể của taste. Chưa kiểm tra mọi DPI, trình đọc màn hình, toàn bộ đường bàn phím, DB/Server hoặc xuất ảnh báo cáo; phần này vẫn thuộc nghiệm thu/bàn giao.

## Đợt 5 — Nghiệm thu và phần nộp của Hải

- [x] T18: Chạy kiểm tra liên thông toàn Admin, hồi quy A2, bàn phím, popup và cửa sổ chuẩn/phóng to; ghi phần chưa kiểm chứng.
  - Ngày 16/11/2026: bổ sung `acceptanceChecks` trong `check-demo-data.ps1`. Cùng một phiên đi đủ 9 mục ở bố cục 1000×700 và 1920×1032, hai theme; kiểm tra đúng tiêu đề, trang thật thay bản xem trước, cache trang/theme và chiều cao ghi chú.
  - Thêm hồ sơ khi các trang đã mở: A2/A7/A10 cập nhật cùng lúc, A8 có người mới 0 bạn, A9/A11 không phát sinh hoạt động giả. Đổi tên cùng ID cập nhật hồ sơ thống kê, tên trong lịch sử và người tạo nhóm; xóa mềm giữ lịch sử đăng ký/hoạt động và số liệu hai biểu đồ, nhưng loại chủ thể khỏi đồ thị bạn bè. Sự kiện và quan hệ gốc giữ nguyên.
  - Lệnh `check-users.ps1` với Maven như T00: **BUILD SUCCESS, 45 PASS A2**. Lệnh `check-demo-data.ps1` với Maven như T02: **BUILD SUCCESS, 156 PASS T18** (78 mỗi theme), cùng **28 PASS dữ liệu + 550 PASS JavaFX hồi quy**; tổng **706 PASS JavaFX**. Không phát hiện lỗi chức năng/bố cục trong các trường hợp trên.
  - Kiểm tra trực quan qua Computer Use trên bản `run-admin.ps1`: A2 sáng/tối, lọc `hai.admin` còn 1/10, chọn Admin cuối cùng thấy Khóa/Xóa mềm disabled và lý do đầy đủ. Enter từ bảng mở đúng hồ sơ chỉ xem; Đóng quay lại giữ bộ lọc/lựa chọn. Dialog lịch sử sáng có đúng 1 lượt của Hải, dialog bạn bè tối có đúng 2 người.
  - Form thêm tối để trống báo lỗi username, dialog tự giãn và thông báo không bị cắt; mở lịch ngày sinh tối đọc rõ; Hủy giữ tổng 10 hồ sơ. Form mật khẩu mô phỏng để trống báo lỗi đầy đủ, Hủy không đổi dữ liệu. Không nhập hoặc thay mật khẩu thật.
  - Phóng to cửa sổ: A2 giãn cột, nút thao tác về một hàng; A4 hiển thị đủ lịch sử. Chọn Từ ngày 01/11/2026 qua popup tối trả 7/11 lượt; Shift+Tab chuyển từ ngày về username với focus rõ. A5 mở chi tiết bằng Enter, nhóm Trao đổi bài tập có 3 thành viên/1 Admin nhóm, nội dung rõ ở theme tối. Sau khi đóng dialog, bản dùng để xem A6 ở kích thước chuẩn.
  - A6 sáng/tối: chọn báo cáo #1, hộp khóa ghi đúng `an.tran`, Hủy là lựa chọn mặc định. Hủy giữ tài khoản Hoạt động và 3 báo cáo. Hộp đăng xuất giải thích chưa có phiên thật; Ở lại giữ nguyên trang/dòng. A7–A11 đã có kiểm tra trực quan trong cùng đợt triển khai tại T14/T17; lượt T18 chạy lại hồi quy chức năng cho toàn bộ các trang này.
  - Công cụ đôi lúc báo chỉ mục control trong dialog không còn trong cache; quan sát lại và dùng tọa độ từ ảnh mới để thao tác thành công. Thử Escape với dialog hồ sơ chưa xác nhận đóng được nên không tính là PASS bàn phím dialog; đóng bằng nút Đóng. Không coi lỗi cache là lỗi ứng dụng.
  - Đóng Admin bình thường, mã thoát 0, không có exception trong output. Lượt T18 chỉ bổ sung kiểm tra/tài liệu, không đổi mã giao diện hoặc nghiệp vụ. Chưa kiểm tra mọi tổ hợp CRUD bằng tay, toàn bộ đường Tab/Shift+Tab trong mọi dialog, thao tác ngày sinh sai khi mất focus, mọi DPI, trình đọc màn hình, DB/Server, SQL hoặc bộ ảnh báo cáo. Không coi nghiệm thu bản mẫu này là nghiệm thu backend hay toàn bộ bài nộp. Chưa commit/push T15–T18.
- [x] T19: Kiểm chứng SQL hiện hành và test email trên database trống dùng riêng; không lấy kết quả baseline cũ thay cho lần chạy mới.
  - Ngày 16/11/2026: đã đọc schema/DDL và bộ test email. Ban đầu Docker chưa sẵn sàng và Windows báo thiếu paging file; sau khi Hải mở Docker Desktop thành công, đã tiếp tục kiểm chứng thực tế. Không thay đổi paging file/cấu hình hệ thống.
  - Dùng image có sẵn `postgres:16.12`, tạo riêng container `java-chat-t19-3413b255069a` với database `java_chat_email_test_3413b255069a`. Giới hạn RAM 256 MiB, `--network none`, không publish cổng, PostgreSQL chỉ nhận Unix socket; dữ liệu nằm trong tmpfs 128 MiB, script repo mount chỉ đọc. Xác thực trust chỉ dùng trong môi trường cô lập không có mạng này, không phải cấu hình triển khai.
  - Kiểm tra ban đầu: PostgreSQL 16.12, đúng database mới và public có 0 bảng. Chạy `psql -X -w -U postgres -d <database kiểm thử> -v ON_ERROR_STOP=1 -f /checks/database.sql`: tạo đầy đủ schema và COMMIT thành công. Chạy `/checks/tests/email-uniqueness.sql`: **25 PASS mới**, gồm 13 bảng/65 cột/21 FK/31 CHECK/33 index/2 trigger và hành vi email, hồ sơ khóa/xóa, ID/lịch sử; kết thúc ROLLBACK.
  - Chạy lại DDL trên cùng schema kiểm thử: từ chối đúng với `Expected an empty public schema; use a new database`, psql mã 3 như kỳ vọng. Schema giữ nguyên sau lỗi; không áp dụng DDL lên database ứng dụng.
  - Thêm `script/tests/schema-smoke.sql`, chạy thành công cả bản thử tạm và bản lưu trong repo: **8 PASS bổ sung** gồm xác nhận email đã rollback, chế độ mã hóa nhóm bất biến, IV khớp chế độ nhóm, nhận bản ghi đúng chế độ, chặn IV trùng, sender tin nhóm bất biến và FK giữ chủ thể lịch sử. Nội dung/envelope chỉ là marker kiểm thử, không phải mật khẩu/khóa hay dữ liệu mã hóa thật.
  - Sau ROLLBACK: users = 0, group_messages = 0, public vẫn có 13 bảng. Xác minh lại tên/nhãn/image/network trước khi xóa đúng container vừa tạo; đã kiểm tra container không còn, tmpfs bị hủy và không có volume test tồn tại lâu dài. Docker Desktop của Hải vẫn chạy; không dừng lại ứng dụng đó sau kiểm thử thành công.
  - Không sửa `script/database.sql` hoặc test email cũ. Cách chạy lại và bằng chứng mới được ghi ở schema mục 4.8. Chưa kiểm thử quyền role Server thực tế, race condition/concurrency, hiệu năng, JDBC, khôi phục mật khẩu qua email/SMTP, mã hóa thực tế hoặc E2EE. Quy tắc FOF loại nút DELETED trong ví dụ truy vấn schema vẫn là chênh lệch đã ghi ở Plan, cần phối hợp khi làm backend; không lấy PASS ràng buộc này để khẳng định nghiệp vụ backend đã hoàn tất. Chưa commit/push T15–T19.
- [x] T20: Chụp ảnh 9 nhóm chức năng và dialog quan trọng; viết phần Admin/CSDL cho báo cáo, dùng ERD hiện hành.
  - Ngày 16/11/2026: đọc đề gốc bằng pypdf (trang 2: 9 nhóm chức năng Admin; trang 4: 5 mục báo cáo mốc 20%). Tạo `admin-report.md` là phần nội dung bàn giao để ghép, không ghi đè báo cáo chung hoặc tạo tỷ lệ đóng góp chưa được hai thành viên chốt.
  - Chụp và xem trực tiếp **18 ảnh** từ bản JavaFX chạy bằng `run-admin.ps1`: 9 màn hình chính, 8 dialog (thêm/sửa/chỉ xem hồ sơ, mật khẩu mẫu, lịch sử cá nhân, bạn bè, chi tiết nhóm, xác nhận khóa) và một ảnh biểu đồ theme tối. Lưu tại `docs/admin-spec/evidence/`, chụp dialog bằng ảnh cửa sổ riêng do Computer Use trả về; không vẽ lại hoặc sửa dữ liệu/ảnh để minh họa.
  - Ảnh giữ nguyên byte JPEG của công cụ, tên `.jpg` đúng định dạng gốc. `evidence/manifest.json` ghi nguồn, kích thước và SHA-256 từng ảnh; các ảnh chính 1002×732, dialog theo kích thước riêng. Mốc Git nền là `03c5162`; ảnh bao gồm thay đổi T15–T19 chưa commit tại thời điểm chụp, không gán chúng cho commit cũ.
  - Báo cáo có tổng quan 13 bảng/65 cột/21 FK, quyết định xóa mềm/email, vai trò, phạm vi kiểm chứng SQL mới và kết quả kiểm tra UI. Trích nguyên khối Mermaid ERD từ schema hiện hành, đã đối chiếu nội dung/hash; bản trích chỉ phục vụ báo cáo, nguồn duy nhất tiếp tục là schema. Có chú thích cho đủ 18 hình và hướng dẫn ghép với phần User.
  - Kiểm tra tệp thực tế: đủ 18 JPEG có header/kích thước hợp lệ, đường dẫn ảnh/tài liệu đều tồn tại, ERD khớp nguồn. Đã xem từng ảnh trong quá trình chụp, bỏ tooltip trước khi lưu chi tiết nhóm; có ghi rõ ô bảng dài có thể rút gọn và xem qua thanh cuộn. Phiên chụp chỉ mở/xem/hủy, không thêm/sửa/khóa dữ liệu; đóng Admin bình thường, mã thoát 0, không có exception trong output.
  - Giới hạn bàn giao: bản hiện tại là Markdown chỉnh sửa được và ảnh gốc, chưa phải PDF tổng hợp. ERD cần render và kiểm tra độ đọc khi Minh ghép vào Word/PDF; chưa xuất `BaoCao.pdf`, chưa chụp ảnh commit T21, chưa gửi tài liệu cho Minh, chưa đóng gói ZIP hoặc chốt tỷ lệ đóng góp. Không tuyên bố SMTP/JDBC/Server/mã hóa thực tế đã hoàn thành; các giới hạn kiểm chứng vẫn theo T18/T19. Chưa commit/push T15–T20.
- [x] T21: Chuẩn bị ảnh commit thật của Hải; không tạo commit chỉ để đủ số lượng, không commit/push tự động.
  - Ngày 16/11/2026: chụp cửa sổ Gitk thực tế bằng Computer Use, lưu `git/commits_Hai.png` (1920×1032). Ảnh chọn commit `03c5162`, hiển thị tác giả, thời gian, hash và nhãn nhánh local/remote. Đã xem ảnh, bỏ phần xem trước cửa sổ che nội dung và đóng Gitk sau khi lưu.
  - Đối chiếu bằng `git rev-list --count HEAD --author='lchai2429@clc.fitus.edu.vn'`: **15 commit** gồm commit merge. Gitk có thêm commit của Minh làm ngữ cảnh và dòng thay đổi chưa commit; không coi mọi dòng là commit của Hải hoặc suy ra tỷ lệ đóng góp.
  - Ảnh JPEG gốc được xuất PNG, không cắt ghép hoặc sửa nội dung; kiểm tra kích thước và 6.893 điểm ảnh khớp nguồn. Hash và hướng dẫn đọc tại [minh chứng Git](git-evidence.md).
  - Ảnh ban đầu đến T14 tại commit trên. Sau khi Hải đồng ý commit/push, ngày 16/11/2026 đã push T15–T21 tại `8725388` và chụp lại `git/commits_Hai.png` tới phiên bản này: **16 commit** theo email tác giả. Kiểm tra PNG 1920×1032, 6.893 điểm ảnh khớp bản gốc và hash đã cập nhật trong tài liệu minh chứng; đóng Gitk và xác nhận cửa sổ không còn.
  - Giới hạn: commit lưu ảnh cập nhật và tài liệu bàn giao được tạo sau lần chụp nên không xuất hiện trong ảnh. Chưa có ảnh riêng của Minh, chưa tích hợp chính thức hoặc đóng gói.
- [ ] T22: Bàn giao Minh phần báo cáo/minh chứng; phối hợp review PR, build tích hợp và kiểm tra cấu trúc bộ nộp theo đề. Không tự merge hoặc đóng gói khi chưa phối hợp.
  - Đã chuẩn bị [bảng bàn giao và việc cần phối hợp](handoff-checklist.md). Fetch nhánh Minh tới `5ce5b07`; kiểm tra ghép tạm với Hải tại `8725388` xác định xung đột `docs/plan.md`. Không sửa tài liệu chung hoặc merge nhánh.
  - Trích riêng `pom.xml` và `src/` từ cây ghép tạm vào `build/`, biên dịch offline bằng Maven 3.9.11: **BUILD SUCCESS, 40 tệp Java, 2 tài nguyên CSS**. Chưa chạy nghiệm thu User hoặc hai UI từ bản ghép; chưa coi T22 hoàn tất. Cần phối hợp giải quyết xung đột, review, báo cáo PDF và minh chứng riêng của Minh.
  - Cập nhật 16/11/2026: Hải xác nhận Minh đồng ý tự xử lý và merge. Tích hợp Admin `99a86cf` với User mới nhất `eb141ac`, giải quyết 6 khối xung đột trong `docs/plan.md`, phân định chuẩn UI và sửa nhãn E2EE thành mã hóa nhóm mô phỏng. Build **42 tệp Java/2 CSS thành công**; chạy lại **45 A2 + 88 thống kê + 28 dữ liệu + 706 JavaFX PASS**. Mở thử cả hai UI, kiểm tra biểu đồ Admin, workspace và dialog tạo nhóm User; đóng các phiên bình thường. Chi tiết và giới hạn ở mục cập nhật trong bảng bàn giao; PR tích hợp theo yêu cầu người dùng. T22 tiếp tục mở cho báo cáo, ảnh Minh và bộ nộp.

## Trạng thái SDD

| Bước | Trạng thái |
|---|---|
| Specify | Đã ghi yêu cầu và quyết định chính; các đề xuất còn mở được ghi riêng. |
| Plan | Đã có phương án kỹ thuật để review. |
| Tasks | T00–T21 đã hoàn tất theo phạm vi và giới hạn kiểm chứng ghi trên; T22 đã xử lý tích hợp, còn báo cáo chung và bộ nộp. |
| Implement | Đã có A3–A11, logic thống kê, nghiệm thu UI/SQL, bản nội dung báo cáo Admin/CSDL kèm ảnh và minh chứng Git hiện tại; còn phối hợp tích hợp/báo cáo chung và cập nhật minh chứng khi chốt bản nộp. |
