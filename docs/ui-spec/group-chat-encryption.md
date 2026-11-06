# ĐẶC TẢ GIAO DIỆN CỤM 6: NHÓM CHAT & MÃ HÓA ĐẦU CUỐI (GROUP CHAT & ENCRYPTION SPECIFICATION)

> **Module:** Cụm 6 — Quản trị Nhóm Chat & Cơ chế Mã hóa E2EE (Mục 12.a ➔ 12.g)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)  
> **Lớp triển khai JavaFX:** `src/user/GroupManagementDrawer.java`, `src/user/ChatConversationView.java`  
> **Mô hình trải nghiệm chuẩn:** Facebook Messenger E2EE

---

## 1. Cấu trúc Quản trị Nhóm & Nhận diện Mã hóa

Giao diện tích hợp quản trị thành viên tại Cột 4 (Right Drawer) và hiển thị chỉ báo an toàn bảo mật trực tiếp trên thanh tiêu đề và luồng chat:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ 💬 HEADER: 🔒 Nhóm Đồ Án Java (3 thành viên) - [Được bảo vệ mã hóa]         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│         ┌─────────────────────────────────────────────────────────┐         │
│         │ 🔒 Tin nhắn trong nhóm này được bảo vệ bằng mã hóa nhóm.│         │
│         │    Chỉ thành viên trong nhóm mới có khóa giải mã.       │         │
│         └─────────────────────────────────────────────────────────┘         │
│                                                                             │
│ [Avatar Hải] Lưu Chí Hải  10:15                                             │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ Script database.sql đã chạy hoàn tất 13 bảng trên PostgreSQL rồi nhé!   │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Đặc tả Chi tiết Từng Yêu cầu Nghiệp vụ

### 2.1 Hộp thoại Tạo Nhóm Chat Mới (Mục 12.a)
*   **Định dạng:** Modal Dialog bo góc `radius.2xl` (`14px`), nền `-bg-card` (`#1B1D22`).
*   **Các thành phần:**
    1.  Ô nhập Tên nhóm: Chiều cao `44px`, bo góc `9px`.
    2.  Công tắc bật mã hóa (Toggle Switch): Nhãn *"Bật mã hóa nhóm (E2EE Messenger Style)"*.
    3.  Danh sách chọn bạn bè: Hiển thị danh bạ kèm `CheckBox`.
    4.  Nút *"Tạo nhóm"*: Chỉ kích hoạt khi đã chọn tối thiểu 1 người bạn và tên nhóm không rỗng.

---

### 2.2 Quản trị Phân quyền Admin ở Ngăn kéo Cột 4 (Mục 12.b, 12.c, 12.d, 12.e)
Toàn bộ thao tác quản trị được gom gọn trong **Right Drawer** (`280px`):

1.  **Đổi tên nhóm (Mục 12.b):** Nhấp vào icon bút `✏️` cạnh tên nhóm để mở trường chỉnh sửa trực tiếp.
2.  **Thêm thành viên mới (Mục 12.c):** Nút *"+ Thêm thành viên"* mở danh sách bạn bè chưa tham gia.
3.  **Phân quyền Quản trị viên (Mục 12.d):** Thành viên Admin hiển thị huy hiệu `[Admin]` màu vàng `-status-warning` (`#F0B232`).
4.  **Xóa thành viên khỏi nhóm (Mục 12.e):**
    *   **Ràng buộc bảo mật giao diện:**
        ```java
        btnRemoveMember.visibleProperty().bind(isCurrentUserAdmin);
        btnRemoveMember.managedProperty().bind(isCurrentUserAdmin);
        ```
    *   Chỉ Quản trị viên mới nhìn thấy và kích hoạt được nút loại bỏ thành viên.

---

### 2.3 Rời Nhóm & Giải tán Nhóm (Mục 12.f)
*   Thành viên có thể chủ động chọn *"Rời khỏi nhóm"*.
*   Trường hợp Quản trị viên cuối cùng rời nhóm: Hệ thống hiển thị cảnh báo yêu cầu gán quyền Admin cho thành viên khác hoặc xác nhận **Giải tán nhóm hoàn toàn**.

---

### 2.4 Trải nghiệm Mã hóa Nhóm Kiểu Facebook Messenger (Mục 12.g)
1.  **Biểu tượng Ổ khóa An toàn (`🔒`):**
    *   Hiển thị cạnh tên nhóm ở Cột 2 (Danh sách hội thoại) và trên Header của Cột 3 với sắc xanh lá `-status-online` (`#23A55A`).
2.  **Banner Thông báo Bảo mật Hệ thống (System Security Banner):**
    *   Căn giữa ở đầu cuộc trò chuyện:
        ```text
        ┌─────────────────────────────────────────────────────────────┐
        │ 🔒 Tin nhắn trong nhóm này được bảo vệ bằng mã hóa nhóm.    │
        │    Chỉ thành viên trong nhóm mới có khóa giải mã tin nhắn.  │
        └─────────────────────────────────────────────────────────────┘
        ```
3.  **Xác minh Khóa Bảo mật / Fingerprint:**
    *   Mục *"Quyền riêng tư & Mã hóa"* trong Cột 4 cho phép mở popup hiển thị chuỗi mã định danh thiết bị của các thành viên phục vụ xác thực bảo mật kênh truyền.
