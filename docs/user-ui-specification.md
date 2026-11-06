# ĐẶC TẢ GIAO DIỆN & TRẢI NGHIỆM NGƯỜI DÙNG — PHÂN HỆ USER (USER UI MASTER SPECIFICATION)

> **Dự án:** Java Chat System (Đồ án Lập trình Ứng dụng Java — ĐH KHTN ĐHQG-HCM)  
> **Thành viên phụ trách:** Trần Nguyễn Tuấn Minh (MSSV: 24127084 — Nhánh `feat/user-ui`)  
> **Nền tảng công nghệ:** Java 21 LTS, JavaFX 21 + CSS (`src/user/theme.css`)  
> **Tài liệu tham chiếu chuẩn:** [docs/final-project.pdf](final-project.pdf), [AGENTS.md](../AGENTS.md), [docs/tech-stack.md](tech-stack.md), [docs/plan.md](plan.md)  
> **Phong cách chủ đạo:** Modern Obsidian Dark Surface (Bản sắc độc lập, phong cách công cụ chuyên nghiệp Developer Tooling với bo góc có chọn lọc, kết hợp Telegram Desktop, Notion AI và Messenger E2EE — Tuyệt đối không phụ thuộc hay sao chép Discord).  
quang
---

## 1. Cấu trúc Module hóa Tài liệu Đặc tả theo Nhóm Chức năng

Để đảm bảo tài liệu rõ ràng, dễ bảo trì và không bị quá tải ngữ cảnh, toàn bộ đặc tả chi tiết của phân hệ User đã được chia tách thành các tài liệu module chuyên biệt nằm trong thư mục `docs/ui-spec/`:

```text
docs/ui-spec/
├── README.md                   # Tổng quan kiến trúc UI, mục lục định tuyến & quy chuẩn dùng relative path
├── base-ui-spec.md             # TÀI LIỆU CHA: Quy chuẩn nền tảng toàn cục (Tokens, Grid, Radius, Typography, Motion)
├── auth.md                     # Cụm 1: Màn hình Xác thực & Tài khoản (Đăng nhập, Đăng ký 7 trường, Quên MK, Hero Fullscreen)
├── workspace-shell.md          # Cụm 2: Khung giao diện chính (Navigation Rail 60px, Sub-Sidebar 280-320px, Main View, Right Drawer 280px)
├── friends-search.md           # Cụm 3: Quản lý Bạn bè & Tìm kiếm Người dùng (Tabs Online, Tất cả, Lời mời, Thêm bạn, Chặn)
├── chat-messaging.md           # Cụm 4: Khung Chat Trực tiếp, Tối ưu Danh sách & Lịch sử Tin nhắn (VirtualFlow ListView, Highlight cuộn, Xóa tin)
├── ai-assistant.md             # Cụm 5: Trợ lý AI như một Thành viên trong Nhóm (Giao diện Chatbot, Streaming, Khối code, Smart Reply)
├── group-chat-encryption.md    # Cụm 6: Nhóm Chat & Mã hóa Đầu cuối Messenger Style (Tạo nhóm, Phân quyền Admin, Khóa AES-GCM)
├── dummy-data-matrix.md        # Ma trận Dữ liệu Giả lập Sống động cho Báo cáo Mốc 20% & Chụp ảnh minh chứng
└── components-and-files.md     # Danh mục toàn bộ File mã nguồn JavaFX (src/user/) và Stylesheet (src/user/theme.css)
```

---

## 2. Bản đồ Tham chiếu Nhanh đến Các Module Chức năng

| Cấp bậc | Module Đặc tả | Mô tả Nội dung Chi tiết | Đường dẫn Kế thừa / Tham chiếu |
| :---: | :--- | :--- | :--- |
| **CHA** | **[TÀI LIỆU CHA] Quy chuẩn Nền tảng Toàn cục** | Định nghĩa toàn bộ Lưới 8-point, Bảng màu Obsidian cố định, Ma trận bo góc chọn lọc, Typography DirectWrite, Icon Lucide trắng, Hệ thống Độ nổi Phân tầng (Layered Surface Elevation) | [docs/ui-spec/base-ui-spec.md](ui-spec/base-ui-spec.md) |
| CON | **Cụm 1: Xác thực & Tài khoản** | Đăng nhập (Mục 5), Đăng ký 7 trường 2 cột (Mục 1), Quên mật khẩu (Mục 3), Hero Section 45%-55%, Password Eye Toggle, 4-Segment Meter, Matte Dark ComboBox | [docs/ui-spec/auth.md](ui-spec/auth.md) |
| CON | **Cụm 2: Khung Workspace Chính** | Bố cục 4 cột: Rail 60px, Sub-Sidebar 280-320px, Main View co giãn, Right Drawer 280px trượt đóng/mở mượt mà | [docs/ui-spec/workspace-shell.md](ui-spec/workspace-shell.md) |
| CON | **Cụm 3: Quản lý Bạn bè & Tìm kiếm** | Trung tâm Bạn bè 4 tabs (Online, Tất cả, Lời mời, Thêm bạn), Chặn người dùng, Hủy kết bạn, 3 nút thao tác độc lập với người lạ (Mục 6, 7, 8, 9) | [docs/ui-spec/friends-search.md](ui-spec/friends-search.md) |
| CON | **Cụm 4: Khung Chat & Lịch sử Tin nhắn** | Chat 1-1 & nhóm tối ưu VirtualFlow `ListView<Message>`, Tìm kiếm tin nhắn cuộn highlight vàng `1.5s`, Chọn xóa nhiều tin nhắn với CheckBox trượt (Mục 10, 11) | [docs/ui-spec/chat-messaging.md](ui-spec/chat-messaging.md) |
| CON | **Cụm 5: Trợ lý AI trong Hội thoại** | Bot AI xuất hiện tự nhiên trong luồng chat thông thường, 3 chế độ Context, Khung nhập kích hoạt Meta AI gradient, Gợi ý Smart Reply Chips (Mục 11.h) | [docs/ui-spec/ai-assistant.md](ui-spec/ai-assistant.md) |
| CON | **Cụm 6: Nhóm Chat & Mã hóa E2EE** | Tạo nhóm kèm công tắc mã hóa, Ràng buộc quyền hạn Admin ở Cột 4, Đổi tên, Xóa thành viên, Khóa bảo mật đối xứng AES-GCM kiểu Messenger (Mục 12) | [docs/ui-spec/group-chat-encryption.md](ui-spec/group-chat-encryption.md) |
| CON | **Ma trận Dữ liệu Giả lập (20%)** | Bảng dữ liệu sống động về người dùng, danh bạ bạn bè, nhóm chat mã hóa và kịch bản chat mẫu phục vụ chụp ảnh nộp Báo cáo PDF Mốc 20% | [docs/ui-spec/dummy-data-matrix.md](ui-spec/dummy-data-matrix.md) |
| CON | **Danh mục File Mã nguồn (`src/user/`)** | Bảng phân công trách nhiệm từng tệp JavaFX và stylesheet CSS, xác nhận trạng thái hoàn thành và tiến độ thực thi | [docs/ui-spec/components-and-files.md](ui-spec/components-and-files.md) |

---

## 3. Các Nguyên Tắc Kỹ Thuật Nền Tảng (Core Engineering Principles)

1. **Kiến trúc Design Tokens Ba Tầng (W3C DTCG Standard):**
   * Quản trị tập trung tại [docs/ui-spec/base-ui-spec.md](ui-spec/base-ui-spec.md): Primitive Tokens ➔ Semantic Tokens ➔ Component Tokens.
   * Áp dụng nhất quán trên `src/user/theme.css` và toàn bộ các tệp đặc tả chức năng con.
2. **Hiển thị Văn bản Sắc nét (DirectWrite Subpixel LCD ClearType):**
   * Kích hoạt khử răng cưa trực tiếp trên các Text Node (`-fx-font-smoothing-type: lcd;`).
   * Không gắn các bộ lọc mờ đục hoặc hiệu ứng đồ họa làm giảm độ sắc nét của chữ.
3. **Độ Nổi Phân Tầng Bề Mặt (Layered Surface Elevation & Hairline Borders):**
   * Phân cấp chiều sâu bằng các tầng sắc thái tối giản (Canvas `#141619` ➔ Input `#0F1013` ➔ Card `#1B1D22` ➔ Menu/Hover `#22252C`) kết hợp viền sắc nét hairline `1px solid #2A2D35`, thay thế hoàn toàn cho các hiệu ứng phát sáng mờ ảo (glow/aura) thiếu tính công học.
4. **Hệ Thống Icon Vector Đơn Sắc Trắng (Monochromatic Vector Icons):**
   * Sử dụng chuỗi vector `SVGPath` chuẩn Lucide / Feather với sắc trắng thuần túy (`#FFFFFF`), đặt trên khung badge trung tính `38x38px` bo góc `8px`.
5. **Ma Trận Bo Góc Chọn Lọc (Selective Corner Radius):**
   * Thẻ Card chính (`14px`), Thẻ công cụ Hero (`10px`), Trường nhập liệu (`9px`), Nút bấm CTA (`8px`), Menu popup (`8px`), Phân đoạn mật khẩu (`2.5px`).
6. **Tương tác Phản hồi Trực quan & Mượt mà:**
   * ComboBox Giới tính Matte Dark chống xung đột màu hover/selection, tích hợp popup animation (Fade + TranslateY 8px).
   * Trường mật khẩu tích hợp nút con mắt `👁` đồng bộ dữ liệu hai chiều và thanh đo độ mạnh mật khẩu 4 phân đoạn trực quan.
