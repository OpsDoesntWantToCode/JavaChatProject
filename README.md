# JavaChatProject

Dự án Java Chat Application phục vụ bài tập nhóm.

## Cấu trúc thư mục

```
JavaChatProject/
├── src/
│   ├── admin/       # Bạn A chỉ code trong thư mục này
│   └── user/        # Bạn B chỉ code trong thư mục này
├── script/          # Bạn A lưu trữ các file cơ sở dữ liệu (.sql) vào đây
└── git/             # Nơi lưu ảnh chụp commit / git log của 2 người lúc nộp bài
```

## Phân công & Nhánh làm việc (Branches)

- `main`: Nhánh chính tích hợp sản phẩm hoàn chỉnh.
- `feat/admin-db`: Nhánh dành cho **Bạn A** (phát triển phần Admin và cơ sở dữ liệu).
- `feat/user-ui`: Nhánh dành cho **Bạn B** (phát triển giao diện và phần User).

## Hướng dẫn nộp bài

1. Mỗi bạn làm việc trên nhánh tính năng tương ứng (`feat/admin-db` hoặc `feat/user-ui`).
2. Sau khi hoàn thành và merge, chụp ảnh màn hình lịch sử commit (`git log --graph` hoặc trên GitHub).
3. Lưu các hình ảnh vào thư mục `git/` trước khi đóng gói nộp bài.
