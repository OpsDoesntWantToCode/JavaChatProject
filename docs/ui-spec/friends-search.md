# ĐẶC TẢ GIAO DIỆN CỤM 3: QUẢN LÝ BẠN BÈ & TÌM KIẾM (FRIENDS & DIRECTORY SPECIFICATION)

> **Module:** Cụm 3 — Quản lý Bạn bè & Danh bạ Người dùng (Mục 6, 7, 8, 9)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)  
> **Lớp triển khai JavaFX:** `src/user/FriendsManagementView.java`

---

## 1. Cấu trúc Tổng thể Trung tâm Bạn bè (Friends Hub)

Màn hình Quản lý Bạn bè được tích hợp tại Cột 3 của Main Workspace, sử dụng thanh điều hướng Tabs phía trên cùng:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ 👥 BẠN BÈ  |  [ Đang Online (5) ]  [ Tất cả (12) ]  [ Lời mời (2) ]  [ + Thêm bạn ] │
├─────────────────────────────────────────────────────────────────────────────┤
│ 🔍 Lọc theo họ tên hoặc @username...                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ DANH SÁCH BẠN BÈ HIỆN TẠI:                                                  │
│                                                                             │
│ [Avatar] Trần Tuấn Minh  @tuanminh_2026   [● Online]     [💬 Nhắn tin] [ ⋮ ]│
│ [Avatar] Lưu Chí Hải     @chihai_2026     [● Online]     [💬 Nhắn tin] [ ⋮ ]│
│ [Avatar] Nguyễn Lan Anh  @lananh_it       [● Online]     [💬 Nhắn tin] [ ⋮ ]│
│ [Avatar] Vũ Thu Hà       @thu_ha          [○ Offline]    [💬 Nhắn tin] [ ⋮ ]│
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Chi tiết Từng Tab Nghiệp vụ

### 2.1 Tab 1 — Đang Online (Mục 8)
*   **Tiêu đề Tab:** `Đang Online (N)` hiển thị số lượng bạn bè trực tuyến thực tế.
*   **Thành phần:**
    *   Thanh lọc nhanh theo tên/username (`space.3` padding).
    *   Danh sách bạn bè có cờ `status == ONLINE`.
    *   Mỗi dòng gồm: Avatar `36px` có chấm xanh `-status-online` (`#23A55A`), Họ tên (Semi-Bold `14px`), Username (`13px`, `-text-muted`).
    *   **Thao tác nhanh:**
        *   `💬 Nhắn tin`: Nhảy sang Cột 3 mở phiên chat với người này.
        *   `➕ Tạo nhóm`: Mở modal tạo nhóm có chọn sẵn bạn bè này.

### 2.2 Tab 2 — Tất cả Bạn bè (Mục 6)
*   **Tiêu đề Tab:** `Tất cả (Total)`.
*   **Thành phần:**
    *   Hiển thị toàn bộ bạn bè, phân biệt trực quan trạng thái kết nối: Online (chấm xanh lá) / Offline (chấm xám `-status-offline: #80848E`).
    *   Menu ngữ cảnh 3 chấm dọc (`⋮`):
        *   `Hủy kết bạn` (Mục 6.c): Hộp thoại xác nhận hủy quan hệ bạn bè.
        *   `Chặn người dùng (Block)` (Mục 6.d): Chữ màu đỏ `-status-danger` (`#FA777C`). Khi xác nhận chặn, hủy khả năng tìm kiếm và nhắn tin giữa hai bên.

### 2.3 Tab 3 — Lời mời Kết bạn (Mục 7)
*   **Tiêu đề Tab:** `Lời mời (Pending)`.
*   **Thành phần:**
    *   Danh sách các yêu cầu kết bạn đến đang chờ xử lý.
    *   Mỗi dòng gồm Avatar, Họ tên, thời gian gửi và **2 nút tương tác trực tiếp**:
        *   `Đồng ý` (Icon tích xanh ✓): Nền `-accent-primary` (`#4E6BF2`), bo góc `6px`.
        *   `Từ chối` (Icon dấu nhân ✕): Nền tối viền mỏng bo góc `6px`.

### 2.4 Tab 4 — Thêm bạn / Tìm người lạ (Mục 6.b & Mục 9)
*   **Tiêu đề Tab:** `+ Thêm bạn`.
*   **Thành phần:**
    *   Khung tìm kiếm lớn: Nhập Họ tên hoặc Tên đăng nhập (Backend loại trừ chính mình và những người trong danh sách chặn).
    *   **Kết quả dạng Thẻ Card (Card List):** Thẻ phẳng bo góc `10px` (`radius.xl`), nền `-bg-card`, viền `1px solid -border-subtle`, hỗ trợ đủ **3 nút hành động độc lập**:
        1.  `[ + Kết bạn ]` (Mục 6.b): Gửi yêu cầu kết bạn.
        2.  `[ 💬 Nhắn tin ngay ]` (Mục 9): Mở phiên chat trực tiếp với người lạ mà không bắt buộc kết bạn trước.
        3.  `[ 👥 Tạo Group ]` (Mục 9): Thêm trực tiếp người lạ này vào nhóm chat mới.
