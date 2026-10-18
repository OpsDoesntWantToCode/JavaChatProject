# JavaChatProject

Dự án Java Chat Application phục vụ bài tập nhóm.

## Cấu trúc thư mục

```
JavaChatProject/
├── src/
│   ├── admin/       # A phụ trách Admin UI
│   └── user/        # B phụ trách User UI
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
