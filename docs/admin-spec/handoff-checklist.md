# Bàn giao phần Admin/CSDL — Mốc 20%

## Phiên bản đã chuẩn bị

Ngày kiểm tra: 16/11/2026. Phần mã nguồn và báo cáo của Hải đã push tại `feat/admin-db`, commit `8725388`. Nhánh Minh được fetch để đối chiếu tại `origin/feat/user-ui`, commit `5ce5b07`. Tài liệu này là bản chuẩn bị bàn giao; chưa gửi thông báo cho Minh hoặc xác nhận review chéo.

| Thành phần | Tài liệu/tệp bàn giao |
|---|---|
| Nội dung báo cáo Admin/CSDL | [admin-report.md](admin-report.md), có chú thích 18 hình và hướng dẫn ghép |
| Ảnh giao diện gốc | [evidence/manifest.json](evidence/manifest.json), liệt kê ảnh và SHA-256 |
| Minh chứng Git Hải | [git-evidence.md](git-evidence.md), liên kết ảnh thật và phạm vi phiên bản |
| CSDL và ERD chính thức | [schema.md](../database/schema.md), [database.sql](../../script/database.sql) |
| Nghiệm thu và giới hạn | [implementation-tasks.md](implementation-tasks.md), T18–T21 |

Các kết quả đã ghi: 45 kiểm tra A2, 88 kiểm tra logic thống kê, 28 kiểm tra dữ liệu mẫu và 706 kiểm tra JavaFX; SQL có 25 kiểm tra email/schema và 8 kiểm tra bổ sung. Giao diện tương tác với dữ liệu trong bộ nhớ; không tuyên bố có Server/JDBC/SMTP/mã hóa thực tế.

## Kiểm tra ghép trước khi phối hợp

- `git merge-tree --write-tree --name-only 8725388 5ce5b07` trả mã 1 do **xung đột nội dung tại `docs/plan.md`**. `AGENTS.md` ghép tự động được về mặt văn bản; vẫn cần review ý nghĩa khi làm PR.
- Không thực hiện merge vào nhánh làm việc. Cây kết quả tạm: `c4e78b2a6005806716636f2a0b630f0c33e26033`. Chỉ trích `pom.xml` và `src/` vào `build/t22-integration-8725388-5ce5b07/` để biên dịch; không dùng tài liệu còn xung đột làm bản bàn giao chính thức.
- Maven 3.9.11, Java 21, lệnh offline: `mvn -o -B -f build/t22-integration-8725388-5ce5b07/pom.xml -DskipTests compile`. Kết quả: **BUILD SUCCESS**, biên dịch 40 tệp Java, sao chép 2 tài nguyên CSS. Chưa chạy hai ứng dụng từ bản ghép hoặc kiểm thử hành vi User; compile thành công không đồng nghĩa nghiệm thu tích hợp.
- Tại thời điểm fetch: Hải ahead 8/behind 0 và Minh ahead 20/behind 1 so với `origin/main` (`88d9b0b`). Số này gắn với các commit trên, sẽ đổi khi có commit mới.
- Nhánh Minh có `git/commits_Minh.txt`; chưa thấy ảnh Git riêng hoặc báo cáo `.pdf`/`.docx` và ảnh UI được track trên nhánh đó. Điều này không kết luận các tệp chưa có trên máy Minh.

## Việc hai thành viên cần chốt

- [ ] Review chéo PR và giải quyết `docs/plan.md`: giữ mô tả Admin JavaFX hiện hành, bỏ mô tả Swing và ghi chú Admin “sẽ làm” đã lỗi thời; giữ tiến độ User do Minh xác nhận, đối chiếu tên class thực tế. Không chọn toàn bộ một phía của tệp.
- [ ] Minh ghép phần Admin/CSDL vào báo cáo, bổ sung phần User, thông tin nhóm, phân công và tỷ lệ đóng góp tổng 100% theo thống nhất.
- [ ] Minh bổ sung ảnh Git thật và ảnh User; render ERD, kiểm tra chữ và đánh số hình khi xuất PDF.
- [ ] Sau xử lý xung đột, build lại phiên bản đã review và mở cả Admin/User để kiểm tra tài nguyên, launcher và giao diện.
- [ ] Kiểm tra bản nộp theo đề: `src/`, `script/database.sql`, `git/` chứa ảnh hai thành viên và `BaoCao.pdf`. Các SQL test phục vụ phát triển không đưa vào thư mục script của bộ nộp.

## Cập nhật tích hợp được hai thành viên đồng ý

- Ngày 16/11/2026, Hải xác nhận Minh đồng ý các phương án xử lý và yêu cầu tự sửa rồi merge. Nhánh tích hợp `feat/integrate-ui-milestone-20` ghép Admin `99a86cf` với **User mới nhất `eb141ac`**, thay cho bản User `5ce5b07` đã kiểm tra trước đó.
- Đã xử lý 6 khối xung đột trong `docs/plan.md`: giữ mô tả Admin JavaFX, cập nhật tên class thật và giữ trạng thái User từ nhánh Minh. Trạng thái UI không xác nhận chức năng backend đã hoàn thành. Phạm vi chuẩn User/Admin được phân biệt trong `AGENTS.md` và `base-ui-spec.md`.
- Biên dịch bản ghép: **BUILD SUCCESS, 42 tệp Java, 2 tài nguyên CSS**. Chạy lại trên bản ghép: **45 PASS A2, 88 PASS thống kê, 28 PASS dữ liệu mẫu, 706 PASS JavaFX**. Không thay đổi `pom.xml`, DDL hoặc giao thức/Server trong lần tích hợp này.
- Mở trực tiếp cả hai launcher từ `target/classes` với JavaFX 21. Admin hiện dữ liệu mẫu, chuyển từ danh sách người dùng sang biểu đồ hoạt động đúng. User mở trang đăng nhập, chuyển vào workspace bằng đăng nhập mô phỏng. Đã đóng các phiên kiểm tra bình thường, mã thoát 0.
- Phát hiện nhãn User đang khẳng định E2EE, trái với quyết định mã hóa còn chờ xác nhận. Sửa nhãn hội thoại, tạo nhóm, thẻ giới thiệu và tài liệu thành mã hóa nhóm mô phỏng; không thay cờ dữ liệu hoặc triển khai mã hóa. Biên dịch lại thành công; mở lại User xác nhận nhãn ở workspace và dialog tạo nhóm đọc được, Hủy không tạo nhóm, đóng bình thường. Các tên biến `e2ee...` cũ được giữ để tránh đổi API không cần thiết; chúng không chứng minh có E2EE.
- Áp dụng nguyên tắc chữ rõ và nhất quán của hai skill UI/UX đã cài; kiểm tra bằng JavaFX thực tế. Không áp dụng bố cục web hoặc quy trình trình duyệt của skill cho ứng dụng desktop này. Chưa nghiệm thu toàn bộ User, mọi DPI, bàn phím hoặc trình đọc màn hình; kết quả trên là kiểm tra khởi chạy và chuyển màn hình.
- Việc tích hợp được lưu qua PR của nhánh trên vào `main`, giữ lịch sử hai thành viên. Không gửi thông báo đại diện cho Hải hoặc Minh, không tự chốt tỷ lệ đóng góp, không xuất PDF hoặc đóng gói ZIP trong đợt sửa/merge.

T22 vẫn còn phần báo cáo chung, ảnh riêng của Minh và kiểm tra bộ nộp. Các mục chưa đánh dấu trong bảng phía trên là danh sách bàn giao ban đầu; phần giải quyết xung đột, build và kiểm tra khởi chạy được cập nhật bằng bằng chứng ở mục này. Trạng thái merge chính thức tra cứu trong PR và lịch sử `main`.
