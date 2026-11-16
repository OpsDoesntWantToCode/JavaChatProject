# Minh chứng lịch sử Git của Hải — Mốc 20%

## Ảnh và nguồn đối chiếu

- [Ảnh Gitk thực tế](../../git/commits_Hai.png), chụp ngày 16/11/2026 bằng Computer Use; kích thước 1920×1032.
- Nhánh: `feat/admin-db`. Commit được chọn: `8725388620292f36305f415580eb61bb44fc0c1d` — `feat(admin-ui): complete monthly charts and milestone evidence`.
- Nhãn nhánh cục bộ và `remotes/origin/feat/admin-db` cùng trỏ tới commit trên tại thời điểm chụp.
- Ảnh gốc do công cụ trả về là JPEG; xuất sang PNG để khớp tên tệp trong kế hoạch, không cắt ghép hoặc sửa nội dung. Đã kiểm tra kích thước và đối chiếu 6.893 điểm ảnh giữa hai bản.
- SHA-256 PNG: `131d0d8c98c77cdbf7954af985b9d017ee4c4c907625edd176c23f01cf769908`.

## Cách đọc lịch sử

Lệnh đối chiếu số commit theo email tác giả trong lịch sử đến commit trên:

```sh
git rev-list --count 8725388620292f36305f415580eb61bb44fc0c1d --author='lchai2429@clc.fitus.edu.vn'
git log 8725388620292f36305f415580eb61bb44fc0c1d --author='lchai2429@clc.fitus.edu.vn' --format='%h %ad %an %s' --date=short
```

Kết quả đến commit được chụp: **16 commit**, gồm cả commit merge. Cùng email xuất hiện với các tên `Luu Chi Hai`, `ChuHaiLi` và `Chu Hai Li`. Đây là thống kê theo metadata Git, không dùng để tự suy ra tỷ lệ đóng góp trong báo cáo.

Gitk hiển thị thêm commit của Minh để giữ ngữ cảnh lịch sử. Vì vậy, không tính mọi dòng trong ảnh là commit của Hải.

## Phạm vi bàn giao

- Ảnh cập nhật sau khi commit/push `8725388`, bao gồm mã nguồn và bằng chứng T15–T21. Bản ảnh trước đó tại `03c5162` đã được thay thế; có thể xem lại qua lịch sử Git.
- Commit lưu ảnh cập nhật và biên bản bàn giao được tạo sau thời điểm chụp, nên không xuất hiện trong chính ảnh. Ảnh xác nhận phiên bản mã nguồn `8725388`; không tạo vòng lặp chụp/commit chỉ để ảnh chứa commit lưu nó.
- Minh chứng riêng của Minh, báo cáo PDF tổng hợp và việc tích hợp thuộc bước phối hợp T22; ảnh này không thay thế các phần đó.
