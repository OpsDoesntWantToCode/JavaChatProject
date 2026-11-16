# Minh chứng lịch sử Git của Hải — Mốc 20%

## Ảnh và nguồn đối chiếu

- [Ảnh Gitk thực tế](../../git/commits_Hai.png), chụp ngày 16/11/2026 bằng Computer Use; kích thước 1920×1032.
- Nhánh: `feat/admin-db`. Commit được chọn: `03c516202cae92607bae23c3637b070e9e628216` — `feat(admin-ui): add registration friendship and activity statistics`.
- Nhãn nhánh cục bộ và `remotes/origin/feat/admin-db` cùng trỏ tới commit trên tại thời điểm chụp.
- Ảnh gốc do công cụ trả về là JPEG; xuất sang PNG để khớp tên tệp trong kế hoạch, không cắt ghép hoặc sửa nội dung. Đã kiểm tra kích thước và đối chiếu 6.893 điểm ảnh giữa hai bản.
- SHA-256 PNG: `113058000fe266745fcd20e4b274b01b62af0db9c7f44282920fa5ddc87008af`.

## Cách đọc lịch sử

Lệnh đối chiếu số commit theo email tác giả trong lịch sử đến commit trên:

```sh
git rev-list --count 03c516202cae92607bae23c3637b070e9e628216 --author='lchai2429@clc.fitus.edu.vn'
git log 03c516202cae92607bae23c3637b070e9e628216 --author='lchai2429@clc.fitus.edu.vn' --format='%h %ad %an %s' --date=short
```

Kết quả: **15 commit**, gồm cả commit merge. Cùng email xuất hiện với các tên `Luu Chi Hai`, `ChuHaiLi` và `Chu Hai Li`. Đây là thống kê theo metadata Git, không dùng để tự suy ra tỷ lệ đóng góp trong báo cáo.

Gitk hiển thị thêm commit của Minh để giữ ngữ cảnh lịch sử và một dòng thay đổi cục bộ chưa commit. Vì vậy, không tính mọi dòng trong ảnh là commit của Hải.

## Phạm vi bàn giao

- Commit mới nhất trong ảnh bao gồm công việc đã commit đến T14. T15–T20 và tài liệu/ảnh T21 chưa được commit hoặc push tại thời điểm chụp.
- Khi cần minh chứng bản nộp cuối, chụp lại sau khi Hải cho phép commit/push phần còn lại; giữ chú thích đúng với phiên bản thực tế.
- Minh chứng riêng của Minh, báo cáo PDF tổng hợp và việc tích hợp thuộc bước phối hợp T22; ảnh này không thay thế các phần đó.
