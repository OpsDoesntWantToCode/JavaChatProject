# JavaChatProject

Dự án Java Chat Application phục vụ đồ án môn Lập trình ứng dụng Java.

Giao diện ứng dụng dùng **JavaFX 21 + CSS** trên JDK 21 (quyết định Hải–Minh ngày 29/10/2026); đóng gói bằng Maven.

**Trạng thái ngày 02/11/2026:** đã có `pom.xml` (JavaFX 21.0.5, nguồn Java/tài nguyên tại `src/`); Admin và User mới có thư mục giữ chỗ, chưa có class Java hoặc UI chạy. POM chọn `user.UserLoginApp`, class chưa tồn tại; cách chạy Admin và đóng gói hai JAR chưa được kiểm thử. Không tạo thêm cấu trúc `src/main/` song song với POM hiện tại.

## 1. Thành viên Nhóm

| STT | Họ và Tên | MSSV | Vai trò & Phân công | Nhánh làm việc |
| :---: | :--- | :---: | :--- | :--- |
| 1 | **Lưu Chí Hải** | **24127030** | Thành viên A: Phụ trách Admin UI, CSDL PostgreSQL (`script/database.sql`), sơ đồ ERD | `feat/admin-db` |
| 2 | **Trần Nguyễn Tuấn Minh** | **24127084** | Thành viên B: Phụ trách User UI, Maven/Repository (`pom.xml`), tổng hợp Báo cáo | `feat/user-ui` |

- **Nhánh chính (Tích hợp):** `main`
- **Sở hữu chung (Review chéo):** `server/`, `shared/` (DTO, Socket protocol), `pom.xml`, `docs/plan.md`.

## 2. Cấu trúc Tài liệu & Database

- **Chỉ dẫn AI & Quy tắc dự án:** [AGENTS.md](AGENTS.md)
- **Kế hoạch & Checklist tiến độ:** [docs/plan.md](docs/plan.md)
- **Công nghệ & Kiến trúc:** [docs/tech-stack.md](docs/tech-stack.md)
- **Cơ sở dữ liệu & Sơ đồ ERD:** [docs/database/schema.md](docs/database/schema.md) (đặc tả 13 bảng, 65 cột, sơ đồ Mermaid ERD và cẩm nang vận hành backend).
- **Script tạo CSDL PostgreSQL:** [script/database.sql](script/database.sql).
- **Chuẩn bị Admin:** tài liệu đặc tả và dữ liệu mẫu đang được chuẩn bị trên nhánh `feat/admin-db`, chưa tích hợp vào `main`.

## 3. Cấu trúc Thư mục

```text
JavaChatProject/
├── pom.xml          # Cấu hình Maven + JavaFX 21
├── docs/            # Tài liệu thiết kế, kế hoạch và CSDL
├── script/          # Chứa database.sql tạo CSDL PostgreSQL
├── src/
│   ├── admin/       # Lưu Chí Hải phụ trách Admin UI (JavaFX)
│   └── user/        # Trần Nguyễn Tuấn Minh phụ trách User UI (JavaFX)
├── git/             # Nơi lưu ảnh chụp commit / git log lúc nộp bài
├── AGENTS.md        # Bản chỉ dẫn toàn diện cho AI coding assistants
└── README.md
```
