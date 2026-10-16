# TECH STACK — JAVA CHAT SYSTEM

> **Phiên bản:** 1.0 — tổng hợp các quyết định A/B đến ngày 16/10/2026  
> **Phạm vi:** Đồ án cuối kỳ môn Lập trình ứng dụng Java, nhóm 2 người  
> **Trạng thái:** **Tech stack đã thống nhất.** Các lựa chọn kiến trúc/bảo mật còn mở được đánh dấu riêng bên dưới.  
> **Nguồn quyết định:** Thảo luận A–B trong ChatGPT; tài liệu `Final Project.pdf` và Google Docs `Feedback 2 ben`.

## 1. Công nghệ chính thức

| Lớp | Công nghệ ĐÃ CHỐT | Mục đích và ghi chú |
|---|---|---|
| Ngôn ngữ | **Java 21 LTS** | Viết ứng dụng desktop, Server và nghiệp vụ. |
| Build/dependencies | **Apache Maven** | Quản lý dependency, biên dịch, kiểm thử và đóng gói JAR. |
| Desktop GUI | **Java Swing** | Dựng giao diện Admin và User. |
| Look & Feel | **FlatLaf** | Giao diện Swing hiện đại, đồng nhất giữa hai phân hệ. |
| Database | **PostgreSQL local** | DB quan hệ chính, tái tạo bằng SQL script trên máy khác. |
| Kết nối DB | **JDBC với pgJDBC** | Server truy vấn PostgreSQL; tránh phát credential DB đặc quyền trong client. |
| Client–Server / realtime | **Java Socket (TCP)** | Kết nối Client ↔ Server, truyền tin và trạng thái online. |
| Dữ liệu qua Socket | **Gson (JSON)** | Serialize/deserialize các message/DTO theo giao thức chung. |
| Biểu đồ | **JFreeChart** | Hai biểu đồ Admin: người đăng ký mới và người hoạt động theo tháng. |
| Email | **Jakarta Mail** | Chức năng gửi email khi khởi tạo lại mật khẩu. Có thể dùng runtime implementation như Angus Mail. |
| Mật khẩu | **BCrypt** | Chỉ lưu `password_hash`, không lưu mật khẩu thô. |
| Mã hóa nhóm | **Java Cryptography API (`javax.crypto`) / AES-GCM** | Cơ chế mã hóa dự kiến; **E2EE hay mã hóa phía Server chưa được thầy xác nhận**. |
| Gọi LLM | **`java.net.http.HttpClient` (JDK 21)** | Gọi REST API để chỉnh/gợi ý tin nhắn; chưa chọn nhà cung cấp/model/API cụ thể. |
| Version control | **Git + GitHub** | Một repository chung, làm việc theo branch và có commit của cả hai thành viên. |

### Công cụ hỗ trợ (không phải quyết định bắt buộc)

- **VS Code + Codex CLI**: môi trường phù hợp cho thành viên A khi code và chạy SQL/Java trên máy local.
- **DBeaver hoặc pgAdmin**: quản lý PostgreSQL, kiểm tra schema và xuất ERD.
- **JUnit**: có thể bổ sung khi viết unit tests ở phiên bản 80%; chưa được hai bên chốt làm thư viện bắt buộc.

### Những lựa chọn KHÔNG nằm trong stack chính

- **Không dùng Supabase**: PostgreSQL chạy local là phương án của nhóm. Việc nộp SQL script vốn cũng có thể thực hiện trên Supabase, nhưng nhóm chủ động loại dịch vụ Cloud để giảm phụ thuộc.
- **Không chọn JavaFX**, **không chọn Jackson** (chọn Swing + FlatLaf và Gson).
- **Không chọn Spring Boot** hay ORM làm stack chính. Nếu muốn thêm framework về sau, hai thành viên phải thống nhất trước; không để Codex tự áp dụng.

## 2. Kiến trúc triển khai dự kiến

```text
    Admin.jar (Swing + FlatLaf)       User.jar (Swing + FlatLaf)
                 \                          /
                  \--- TCP Socket + JSON ---/
                              |
                         Java Server
                    [xác thực + phân quyền]
                    [chat + business logic]
                    [quản lý online sessions]
                       /       |        \
                      /        |         \
           pgJDBC / JDBC   Jakarta Mail   HttpClient
                 |              |             |
        PostgreSQL local    Email SMTP      LLM API
```

- **Server là nơi giữ các bí mật**: mật khẩu kết nối database có đặc quyền, khóa API LLM, thông tin email và khóa mã hóa bảo vệ ở phía Server/cấu hình môi trường, **không hardcode trong `User.jar` hay `Admin.jar`**.
- **Hai ứng dụng dùng cùng một hệ thống**. Khi dev, mỗi người có thể tái tạo PostgreSQL local từ cùng script; khi chạy kiểm thử tích hợp, các client phải kết nối **cùng một Server và cùng một nguồn dữ liệu**, không phải hai DB độc lập.
- **TCP không tự cung cấp mã hóa truyền tải**: nếu cần bảo mật đường truyền, phải bổ sung cơ chế phù hợp; không nhầm với AES-GCM mã hóa nội dung nhóm.
- **Trạng thái online/offline** do Java Server quản lý theo session/kết nối, không dùng cột `users.is_online` cố định. Có thể lưu `last_seen_at`.

## 3. Cấu trúc mã nguồn và người phụ trách

**Phương án một Maven project / `pom.xml` ở gốc đã thống nhất; cách đóng gói nhiều JAR vẫn cần cấu hình/kiểm thử thực tế.**

```text
JavaChatProject/
├── pom.xml                         # B khởi tạo; sửa chung phải phối hợp
├── src/
│   └── main/
│       └── java/
│           └── com/chatapp/
│               ├── admin/          # A sở hữu
│               ├── user/           # B sở hữu
│               ├── server/         # chung, cần review
│               └── shared/         # chung: model/DTO/protocol, cần review
├── script/
│   └── database.sql                # A phụ trách
├── git/                             # ảnh minh chứng commit từng người
└── docs/                            # tài liệu kỹ thuật, thiết kế và báo cáo
```

**Branch đang thống nhất gần nhất:** `main`, `feat/admin` (A), `feat/user` (B). Tên branch cũ `feat/admin-db` / `feat/user-ui` xuất hiện ở bản thảo đầu, **không phải lựa chọn mới nhất**.

### Sản phẩm cuối kỳ (phiên bản 80%)

- `release/` chứa **ít nhất** `Admin.jar`, `User.jar` và **file cấu hình kết nối** theo yêu cầu đề bài.
- Với Server độc lập, cần cung cấp cách chạy Server (ví dụ đóng gói thêm `Server.jar`), nhưng đề chỉ nêu tối thiểu hai JAR Admin/User; phương án build/deployment cuối cùng phải được xác nhận và kiểm thử.
- File cấu hình cho phép thay đổi thông số môi trường phù hợp mà không cần sửa source/build lại JAR.
- Phải nộp SQL script, mã nguồn, minh chứng commit, báo cáo và các mục demo/hướng dẫn theo PDF đề bài.

## 4. Công nghệ theo mốc thực hiện

| Phiên bản 1 — 20% | Phiên bản 2 — 80% |
|---|---|
| Swing + FlatLaf: toàn bộ giao diện Admin/User, chưa bắt buộc gắn sự kiện | Gắn sự kiện, triển khai nghiệp vụ thực tế |
| PostgreSQL: ERD + `script/database.sql` chạy từ DB rỗng | JDBC/pgJDBC, transaction, dữ liệu thật |
| Maven + cấu trúc package, repo và commit minh chứng | Java Socket TCP + JSON/Gson, chat realtime/offline |
| Dữ liệu giao diện mẫu phục vụ chụp hình | Email, LLM, mã hóa, thống kê, kiểm thử, phát hành JAR |
| Báo cáo thiết kế DB + ảnh các màn hình | Báo cáo thiết kế đầy đủ, hướng dẫn, demo video, tự đánh giá |

## 5. Quy tắc kỹ thuật và bảo mật đã thống nhất

1. **Database:** PostgreSQL thuần; dùng `BIGINT GENERATED ALWAYS AS IDENTITY` cho ID tự tăng khi phù hợp, `TIMESTAMPTZ` cho thời gian, `BOOLEAN` cho cờ; SQL script có khóa ngoại và ràng buộc.
2. **User:** mật khẩu băm; `status` thuộc `ACTIVE` / `LOCKED` / `DELETED`; xóa người dùng theo cơ chế soft delete nhằm bảo toàn lịch sử.
3. **Friendships:** lưu một cặp theo thứ tự `user_id_1 < user_id_2`, khóa chính `(user_id_1, user_id_2)`.
4. **Xóa tin nhắn cá nhân:** `message_hidden` ẩn riêng từng tin; `conversation_clears` đánh dấu mốc xóa toàn bộ lịch sử theo từng người, không xóa bên đối thoại.
5. **OPEN_APP:** một bản ghi mỗi phiên sử dụng thực sự, **không** ghi lại vì Socket reconnect.
6. **Thống kê Admin:** đếm số người mà user **chủ động gửi** tin (`DISTINCT receiver_id`), số nhóm mà user **chủ động gửi** tin (`DISTINCT group_id`); biểu đồ hoạt động theo tháng đếm `DISTINCT user_id` mở app.
7. **ON DELETE:** RESTRICT/CASCADE theo từng FK, không cascade đại trà; thao tác đa bảng cần transaction.
8. **Mã hóa nhóm:** AES-GCM cần IV/nonce và metadata giải mã; không lưu khóa AES plaintext; wrapped key chỉ an toàn nếu khóa bảo vệ được giữ tách biệt.
9. **Cần kiểm tra quyền ở Server:** chỉ người thuộc hội thoại được ẩn tin; người ngoài nhóm không được gửi tin; chỉ Admin nhóm được xóa thành viên; quy tắc báo cáo spam gắn với lịch sử chat.

## 6. Các quyết định CÒN MỞ — không được tự suy diễn thành đã chốt

- **Mã hóa nhóm:** cần hỏi giảng viên E2EE hay mã hóa phía Server. `wrapped_encryption_key`, `encrypted_group_key`, `iv`, `key_version`/định dạng tin mã hóa mới là hướng thiết kế; quản lý vòng đời khóa chưa hoàn chỉnh.
- **Schema 13 bảng:** là baseline, chưa phải SQL được nghiệm thu. Đợt review gần nhất yêu cầu bổ sung `NOT NULL`, `CHECK`, các index thực tế, chống lời mời kết bạn `PENDING` trùng hai chiều, quy tắc xóa lịch sử ở thời điểm đồng thời, quyền và transaction.
- **Giao diện:** kích thước cửa sổ, bảng màu, font chữ chi tiết chưa chốt.
- **LLM:** nhà cung cấp và endpoint cụ thể chưa chọn; gọi API qua HttpClient là hướng đã thống nhất.
- **Build nhiều JAR / chạy Server:** cần cấu hình Maven và kiểm chứng trên máy khác.

## 7. Hướng dẫn cho Codex khi đọc tài liệu này

- Xem **bảng công nghệ ở Mục 1 là baseline bất biến** trừ khi người dùng và thành viên B đồng ý thay đổi.
- **Không tự thay Swing bằng JavaFX, Gson bằng Jackson, PostgreSQL bằng Supabase, hay thêm Spring Boot/ORM**.
- Với những mục ở **Mục 6**, hãy nêu giả định/đề xuất và yêu cầu duyệt khi việc triển khai đòi hỏi một quyết định mới; không ghi nhầm là quyết định hai bên đã chốt.
- Khi sửa database, đối chiếu `PROJECT_CONTEXT.md` và PDF đề bài trước; không xem tên 13 bảng là bằng chứng đã có đầy đủ ràng buộc.
