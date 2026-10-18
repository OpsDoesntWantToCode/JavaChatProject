# AGENTS.md — Java Chat System (quy tắc chung cho AI assistants khi làm việc trong repo)

> File này có thể được Codex tự nhận diện. AI khác có thể **không** tự đọc `AGENTS.md`, vì vậy mỗi thành viên cần nhắc AI đọc file này cùng hai tài liệu `docs/`.

## Tài liệu gốc
- Khi thay đổi stack hoặc dependencies, đối chiếu `docs/Tech_Stack.md`.
- Khi thay đổi kiến trúc, schema, trách nhiệm hoặc luồng nghiệp vụ, đối chiếu `docs/Project_Context.md`.
- Nếu có `Final Project.pdf`, ưu tiên yêu cầu chính thức của giảng viên. Phân biệt rõ yêu cầu bắt buộc, quyết định A/B đã chốt và đề xuất còn chờ duyệt.

## Phạm vi công việc
- Branch `main` là branch tích hợp chung.
- Thành viên A làm việc trên `feat/admin-db`, phụ trách Admin UI (`src/main/java/com/chatapp/admin/`), ERD, PostgreSQL và `script/database.sql`.
- Thành viên B làm việc trên `feat/user-ui`, phụ trách User UI (`src/main/java/com/chatapp/user/`), Maven/repository và báo cáo.
- `src/main/java/com/chatapp/server/`, `src/main/java/com/chatapp/shared/`, `pom.xml`, giao thức Socket/JSON và các API dùng chung cần được cả hai review trước khi đổi giao diện công khai hoặc hợp đồng dữ liệu.
- Đọc vai trò của người yêu cầu trong prompt; không áp dụng giới hạn chỉ dành cho A lên các tác vụ của B hoặc ngược lại.

## Stack đã chọn
Java 21 LTS, Maven, Java Swing + FlatLaf, PostgreSQL local + pgJDBC, Java Socket TCP, Gson, JFreeChart, Jakarta Mail, BCrypt, Java Cryptography API (AES-GCM), Java HttpClient cho LLM và Git/GitHub. Không tự ý thay stack hoặc thêm framework lớn nếu chưa có sự đồng ý.

## Nguyên tắc thực hiện
- Trước khi sửa, xem `git status`, xem mã nguồn liên quan và tóm tắt phạm vi file dự kiến thay đổi.
- Nếu nhiệm vụ chỉ yêu cầu phân tích/review, không tự viết lại mã nguồn.
- Khi được yêu cầu code, ưu tiên thay đổi nhỏ, đúng phạm vi; chạy kiểm thử phù hợp nếu môi trường cho phép. Phân biệt kết quả đã chạy với điều chưa kiểm chứng.
- Không tự ý `git commit`, `git push`, `git merge`, `git reset --hard` hoặc ghi đè thay đổi chưa commit nếu chưa được người sử dụng AI yêu cầu; thay đổi dùng chung phải tuân thủ quy ước review A–B.
- Đối với PostgreSQL, kiểm tra PK, FK, NOT NULL, UNIQUE, CHECK, index và chính sách ON DELETE theo từng bảng; không dùng CASCADE đại trà. Thử chạy script trên database trống.
- Khi hoàn tất, liệt kê các file đã sửa, test đã chạy, rủi ro còn lại và bước tiếp theo.

## An toàn và dữ liệu
- Không lưu mật khẩu gốc; sử dụng password hash phù hợp. Không để credential DB đặc quyền, key LLM, email secret hoặc master encryption key trong client/JAR hay repository.
- Server kiểm tra quyền truy cập cho nhắn tin, xóa lịch sử, quản lý nhóm, block và report spam; các thao tác liên quan nhiều bảng phải được thực hiện nhất quán.
- Database chính thức: `docs/database/Database_Schema.md`, `Database_Decisions.md`, `ERD.mmd` và `script/database.sql`; SQL đã PASS PostgreSQL 18.6. Nghiệp vụ Server/concurrency chưa được kiểm chứng. Server-side AES-GCM vẫn PROVISIONAL, không tương đương E2EE; mở rộng ngoài thiết kế vẫn cần review.
