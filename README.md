# JavaChatProject

Dự án Java Chat Application phục vụ đồ án môn Lập trình ứng dụng Java.

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

## 3. Cấu trúc Thư mục

```text
JavaChatProject/
├── docs/            # Tài liệu thiết kế, kế hoạch và CSDL
├── script/          # Chứa duy nhất file database.sql tạo CSDL
├── src/
│   ├── admin/       # Lưu Chí Hải phụ trách Admin UI
│   └── user/        # Trần Nguyễn Tuấn Minh phụ trách User UI
├── git/             # Nơi lưu ảnh chụp commit / git log của 2 người lúc nộp bài
├── AGENTS.md        # Bản chỉ dẫn toàn diện cho AI coding assistants
└── README.md
```
