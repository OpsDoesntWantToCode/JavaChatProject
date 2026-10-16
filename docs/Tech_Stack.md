# TECH STACK — JAVA CHAT SYSTEM (TÀI LIỆU DÙNG CHUNG)

> **Phiên bản tài liệu:** 2.0 — chỉnh cách diễn đạt để dùng chung cho hai thành viên và mọi trợ lý AI, 16/10/2026. **Không thay đổi stack đã thống nhất.**  
> **Phạm vi:** Đồ án cuối kỳ môn Lập trình ứng dụng Java, nhóm 2 người  
> **Trạng thái:** **Tech stack đã thống nhất.** Các lựa chọn kiến trúc/bảo mật còn mở được đánh dấu riêng bên dưới.  
> **Nguồn quyết định:** Thảo luận và xác nhận giữa thành viên A và B; yêu cầu chính thức `Final Project.pdf`; bản thiết kế/feedback `Feedback 2 ben`. Việc nhóm dùng các trợ lý AI khác nhau **không ảnh hưởng các quyết định kỹ thuật**.

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

- **VS Code hoặc IDE phù hợp**: mỗi thành viên tự chọn môi trường lập trình. A sử dụng Codex CLI trong VS Code; B có thể sử dụng một trợ lý AI khác. **Công cụ AI không phải một thành phần của tech stack sản phẩm.**
- **DBeaver hoặc pgAdmin**: quản lý PostgreSQL, kiểm tra schema và xuất ERD.
- **JUnit**: có thể bổ sung khi viết unit tests ở phiên bản 80%; chưa được hai bên chốt làm thư viện bắt buộc.

### Những lựa chọn KHÔNG nằm trong stack chính

- **Không dùng Supabase**: PostgreSQL chạy local là phương án của nhóm. Việc nộp SQL script vốn cũng có thể thực hiện trên Supabase, nhưng nhóm chủ động loại dịch vụ Cloud để giảm phụ thuộc.
- **Không chọn JavaFX**, **không chọn Jackson** (chọn Swing + FlatLaf và Gson).
- **Không chọn Spring Boot** hay ORM làm stack chính. Nếu muốn thêm framework về sau, hai thành viên phải thống nhất trước; không để bất kỳ trợ lý AI nào tự áp dụng.

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

## 7. Quy tắc sử dụng tài liệu đối với CẢ HAI thành viên và mọi AI assistant

- Đây là **tài liệu quyết định kỹ thuật dùng chung**, không phải prompt dành riêng cho Codex, ChatGPT, Gemini hay công cụ nào khác.
- Thành viên A và B đều dùng cùng một baseline ở Mục 1. Chỉ thay đổi stack sau khi **cả hai thành viên đồng ý** và cập nhật tài liệu này; các gợi ý của AI không tự có hiệu lực.
- **Không tự thay Swing bằng JavaFX, Gson bằng Jackson, PostgreSQL bằng Supabase, hay tự thêm Spring Boot/ORM**.
- Các mục trong **Mục 6 là quyết định còn mở**. AI chỉ được đưa ra đề xuất và nêu tác động, không trình bày chúng thành yêu cầu đã chốt.
- Đối chiếu `docs/Project_Context.md` và `Final Project.pdf` khi thiết kế database/chức năng. Không xem baseline 13 bảng là schema SQL hoàn chỉnh.
- Mọi thay đổi ảnh hưởng `server/`, `shared/`, giao thức JSON/Socket hoặc `pom.xml` cần được hai thành viên review, dù AI nào đề xuất thay đổi.

## 8. Phân biệt công nghệ sản phẩm với công cụ hỗ trợ lập trình

**Công nghệ sản phẩm (bắt buộc theo quyết định nhóm)**: Java/Maven/Swing/PostgreSQL/Socket/Gson/... ở Mục 1. **Công cụ cá nhân (không bắt buộc đồng nhất)**: IDE, AI coding assistant, cách viết prompt, extension. A có thể dùng Codex CLI; B dùng AI khác mà vẫn tuân thủ cùng source of truth. Không đưa quy định "chỉ người A được sửa code" vào tài liệu tech stack; việc chia trách nhiệm nằm trong `Project_Context.md` và phân quyền Git của nhóm.
