# Lưu trữ thiết kế Database trước khi chính thức hóa

Đây là tài liệu lịch sử, **không phải ERD hoặc schema chính thức song song**. Dùng [Database_Schema](../Database_Schema.md), [Database_Decisions](../Database_Decisions.md) và [ERD hiện hành](../ERD.mmd) khi triển khai.

- [Database_Schema_Draft.docx](Database_Schema_Draft.docx) và [Markdown chuyển đổi](Database_Schema_Draft.md): nguồn của B, giữ nguyên nội dung.
- [ERD_2026-10-16.mmd](ERD_2026-10-16.mmd), [SVG](ERD_2026-10-16.svg), [PNG](ERD_2026-10-16.png): baseline 13 bảng/64 cột/21 FK, giữ nguyên nội dung.
- [ERD_Review_2026-10-16.md](ERD_Review_2026-10-16.md): review cũ, chỉ sửa đường dẫn tương đối sau khi di chuyển; các nhận xét “chưa triển khai” là trạng thái lịch sử.

Các file được chuyển bằng git mv để review lịch sử. Không xóa nguồn B; bản hiện hành được nhóm A/B chấp thuận và kiểm thử PostgreSQL trước khi nâng thành chính thức. Mô hình mã hóa ở bản hiện hành vẫn PROVISIONAL đối với xác nhận của giảng viên.
