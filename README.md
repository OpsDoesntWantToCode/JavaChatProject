# JavaChatProject

Dự án Java Chat Application phục vụ bài tập nhóm.

Giao diện dùng **JavaFX 21 + CSS** trên JDK 21 theo quyết định Hải–Minh ngày 29/10/2026; build bằng Maven.

## Database chính thức của nhóm

- [Schema](docs/database/Database_Schema.md), [quyết định thiết kế](docs/database/Database_Decisions.md), [ERD](docs/database/ERD.mmd) ([SVG](docs/database/ERD.svg), [PNG](docs/database/ERD.png)).
- [SQL tạo database](script/database.sql), [hướng dẫn test](script/tests/README.md), [kết quả PostgreSQL 18.6](docs/database/TEST_RESULTS.md): 202 PASS, 0 FAIL ở lần chạy cuối.
- [Nguồn thiết kế cũ](docs/database/archive/README.md) được lưu riêng; schema hiện hành có 13 bảng, 65 cột và 21 FK. Server-side AES-GCM vẫn PROVISIONAL, không phải E2EE. Test SQL không thay cho test nghiệp vụ Java/concurrency/AEAD.

## Cấu trúc thư mục

```
JavaChatProject/
├── pom.xml          # Maven + JavaFX
├── src/
│   └── main/
│       ├── java/com/chatapp/
│       │   ├── admin/   # A phụ trách Admin UI
│       │   └── user/    # B phụ trách User UI (dự kiến)
│       └── resources/  # CSS và tài nguyên giao diện
├── script/          # Bạn A lưu trữ các file cơ sở dữ liệu (.sql) vào đây
└── git/             # Nơi lưu ảnh chụp commit / git log của 2 người lúc nộp bài
```

## Phân công & Nhánh làm việc (Branches)

- `main`: Branch tích hợp chung.
- `feat/admin-db`: Branch dành cho **thành viên A**, phụ trách Admin UI, ERD, PostgreSQL và `script/database.sql`.
- `feat/user-ui`: Branch dành cho **thành viên B**, phụ trách User UI, Maven/repository và báo cáo.

## Hướng dẫn nộp bài

1. Mỗi bạn làm việc trên nhánh tính năng tương ứng (`feat/admin-db` hoặc `feat/user-ui`).
2. Sau khi hoàn thành và merge, chụp ảnh màn hình lịch sử commit (`git log --graph` hoặc trên GitHub).
3. Lưu các hình ảnh vào thư mục `git/` trước khi đóng gói nộp bài.
