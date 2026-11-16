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

T22 còn mở đến khi có phối hợp và kiểm chứng các mục trên. Chưa merge, chưa tạo bộ ZIP nộp bài, chưa xuất hoặc ghi đè báo cáo chung.
