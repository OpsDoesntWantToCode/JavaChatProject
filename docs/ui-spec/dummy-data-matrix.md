# MA TRẬN DỮ LIỆU KIỂM THỬ THỊ GIÁC (VISUAL MOCK DATA SPECIFICATION)

> **Module:** Dữ liệu Giả lập Sống động Phục vụ Kiểm thử Giao diện & Báo cáo Mốc 20%  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)

---

## 1. Bảng Tổng Hợp Dữ Liệu Kiểm Thử

| Thực thể / Thành phần | Dữ liệu Giả lập Đổ sẵn trên Giao diện UI | Mục đích Nghiệp vụ / Minh chứng |
| :--- | :--- | :--- |
| **Tài khoản đang đăng nhập** | `tuanminh_2026` — Trần Nguyễn Tuấn Minh (Trạng thái: Online ●)<br>Email: `tuanminh@gmail.com` | Minh chứng thông tin phiên làm việc hiện tại của người dùng |
| **Bạn bè Trực tuyến (Online)** | • `chihai_2026` — Lưu Chí Hải (Online ●)<br>• `lananh_it` — Nguyễn Lan Anh (Online ●)<br>• `nam_le` — Lê Hoàng Nam (Online ●) | Minh chứng Tab Bạn bè Online (Mục 8) và chấm xanh lá trực quan |
| **Bạn bè Ngoại tuyến (Offline)** | • `duc_huy` — Phạm Đức Huy (Offline ○)<br>• `thu_ha` — Vũ Thu Hà (Offline ○) | Minh chứng Tab Tất cả Bạn bè (Mục 6) và chấm xám |
| **Lời mời Kết bạn Đang chờ** | • `quoc_bao` — Đặng Quốc Bảo (Gửi 10 phút trước)<br>• `my_duyen` — Trần Mỹ Duyên (Gửi hôm qua) | Minh chứng Tab Lời mời (Mục 7) với nút Đồng ý ✓ và Từ chối ✕ |
| **Người lạ Tìm kiếm được** | • `hoang_long` — Nguyễn Hoàng Long (`@hoang_long`)<br>• `bao_ngoc` — Trần Bảo Ngọc (`@bao_ngoc`) | Minh chứng Tab Thêm bạn (Mục 6.b & 9) với đủ 3 nút thao tác |
| **Nhóm Chat Thông thường** | • 👥 **Nhóm Thể Thao Cuối Tuần** (4 thành viên)<br>Tin cuối: *"Thứ 7 này 17h đá bóng sân cỏ nhân tạo nhé!"* | Minh chứng danh sách nhóm chat thông thường (Mục 12.a) |
| **Nhóm Chat Có Mã hóa 🔒** | • 🔒 **Nhóm Đồ Án Java 2026** (3 thành viên: Minh [Admin], Hải [Admin], Lan Anh)<br>Khóa DEK: AES-GCM 256-bit hoạt động | Minh chứng icon ổ khóa xanh lá và Banner bảo mật Messenger style (Mục 12.g) |
| **Trợ lý Trí tuệ Nhân tạo** | • 🤖 **Trợ lý AI `[AI BOT]`** (Huy hiệu tím gradient)<br>Phản hồi: Tóm tắt 5 tin nhắn gần nhất và gợi ý đoạn code Java | Minh chứng tích hợp AI tự nhiên trong luồng chat (Mục 11.h) |

---

## 2. Kịch bản Dữ liệu Hội thoại Mẫu (Sample Chat Script)

### 2.1 Đoạn Chat trong Nhóm Đồ Án có Mã hóa (`🔒 Nhóm Đồ Án Java 2026`)
```text
[Hệ thống]: 🔒 Tin nhắn trong nhóm này được bảo vệ bằng mã hóa nhóm kiểu Messenger.
[Lưu Chí Hải - 10:15]: Script database.sql đã chạy hoàn tất 13 bảng trên PostgreSQL rồi nhé!
[Nguyễn Lan Anh - 10:16]: Tuyệt vời, mình đã test thử các ràng buộc khóa ngoại và index idx_login_time chạy rất nhanh.
[Trần Nguyễn Tuấn Minh - 10:18]: Giao diện User JavaFX 21 + theme.css đã hoàn thành toàn bộ form Đăng nhập, Đăng ký 7 trường và khung chat rồi.
[Trợ lý AI - 10:19]: 💡 Các bạn nhớ chụp ảnh minh chứng TableView và các trường nhập liệu trước ngày 22/11/2026 để tổng hợp vào Báo cáo PDF mốc 20% nhé!
```

### 2.2 Đoạn Chat Trực tiếp 1-1 (`💬 Lưu Chí Hải`)
```text
[Trần Nguyễn Tuấn Minh - 14:00]: Hải ơi, cậu kiểm tra giúp mình nhánh feat/user-ui xem các icon vector trắng đã tương phản rõ chưa nhé.
[Lưu Chí Hải - 14:02]: Mình xem rồi, icon trắng Lucide trên nền badge 38x38px nhìn rất sắc nét và chuyên nghiệp!
```
