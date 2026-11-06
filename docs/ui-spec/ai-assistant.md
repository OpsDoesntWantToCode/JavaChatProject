# ĐẶC TẢ GIAO DIỆN CỤM 5: TRỢ LÝ AI TRONG HỘI THOẠI (AI ASSISTANT SPECIFICATION)

> **Module:** Cụm 5 — Trí tuệ Nhân tạo / LLM Tích hợp Hội thoại (Mục 11.h)  
> **Kế thừa quy chuẩn:** [base-ui-spec.md](base-ui-spec.md)  
> **Lớp triển khai JavaFX:** `src/user/ChatConversationView.java` (AI Sub-module)

---

## 1. Triết lý Thiết kế: Tích hợp Tự nhiên trong Luồng Chat

Trợ lý AI được thiết kế xuất hiện tự nhiên như một thành viên trong nhóm chat, sử dụng định dạng bong bóng chuyên biệt để người dùng dễ dàng phân biệt:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ 💬 KHUNG HỘI THOẠI CÓ SỰ THAM GIA CỦA AI                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ [Avatar Minh] Trần Tuấn Minh  14:30                                         │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ Thầy yêu cầu phần mã hóa nhóm phải tham khảo Messenger kìa các bạn!     │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ [Avatar AI] 🤖 Trợ lý AI  [AI BOT]  14:31                                   │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ ┌─ Đang phản hồi tin nhắn của @Trần Tuấn Minh                           │ │
│ │ └───────────────────────────────────────────────────────────────────────┘ │
│ │ 💡 Theo yêu cầu của thầy, nhóm nên vẽ thêm icon ổ khóa 🔒 trên Header,  │ │
│ │    banner hệ thống thông báo mã hóa E2EE và trang xem Khóa bảo mật nhé!│ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ THANH NHẬP LIỆU META AI:                                                    │
│ [ Dải Smart Reply Chips ]:                                                  │
│   ( ✨ Tóm tắt đoạn chat )  ( 💬 Gợi ý trả lời nhanh )  ( ❓ Hỏi đáp tự do ) │
│ ┌───────────────────────────────────────────────────────────┬─────────────┐ │
│ │ [💬 Context: Cả đoạn chat ▼] Gõ /ai hoặc nhập tin nhắn... │ ✨ AI   Gửi │ │
│ └───────────────────────────────────────────────────────────┴─────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Đặc tả Thành phần Thị giác của Trợ lý AI

### 2.1 Bong bóng Tin nhắn Bot (AI Chat Bubble)
*   **Avatar:** Biểu tượng Robot 🤖 hoặc Vòng tròn gradient 3D Meta AI.
*   **Tên hiển thị:** `Trợ lý AI` kèm huy hiệu nổi bật `[AI BOT]` mang màu tím chuyển sắc (`#8B5CF6`).
*   **Phong cách Bong bóng:**
    *   Bề mặt tối chuyên biệt: `#2B2738`.
    *   Đường viền điểm nhấn ở mép trái: `border-left: 4px solid #8B5CF6`.
    *   Bo góc: `12px` (Góc dưới trái: `3px`).
    *   Chữ hiển thị: Màu `-text-normal: #EDEDED`.
*   **Hiệu ứng Gõ chữ Từng từ (Streaming Text):** Văn bản phản hồi từ LLM API được cập nhật dần từng từ thông qua JavaFX Service / Platform.runLater, tạo cảm giác phản hồi theo thời gian thực.

---

## 3. Ba Chế độ Ngữ cảnh (Context-Aware Modes)

1.  **Toàn cảnh Hội thoại (Full-Context Mode):** Đọc ngầm 5 đến 10 tin nhắn gần nhất trong phòng chat để tạo bản tóm tắt nhanh hoặc gợi ý nội dung thống nhất.
2.  **Trích dẫn Tin nhắn Cụ thể (Single-Message Context):** Chuột phải vào một tin nhắn ➔ Chọn *"✨ Hỏi AI về tin này"*, bot trích dẫn nội dung câu chat và đưa ra phản hồi đối chiếu.
3.  **Hỏi đáp Độc lập (Zero-Context Mode):** Hỏi đáp kiến thức kỹ thuật hoặc dịch thuật tổng quát.

---

## 4. Khung Soạn thảo Kích hoạt AI & Smart Reply Chips

### 4.1 Dải Nút Gợi ý Nhanh (Smart Reply Chips)
Bố trí ngay trên ô soạn thảo, gồm các nút bấm bo tròn (`radius: 16px`), nền `-bg-chip: #1E2026`:
*   `✨ Tóm tắt đoạn chat`: Tự động gọi AI tóm tắt tin nhắn gần nhất.
*   `💬 Gợi ý trả lời`: Đề xuất 3 phương án phản hồi nhanh.
*   `❓ Hỏi đáp tự do`: Chuyển ô soạn thảo sang tương tác trực tiếp với bot.

### 4.2 Khung Nhập Kích hoạt AI (`.ai-active`)
Khi gõ `/ai` hoặc bấm nút biểu tượng ✨ AI, ô soạn thảo áp dụng stylesheet:
```css
.chat-input-box.ai-active {
    -fx-border-color: linear-gradient(to right, #00C6FF, #7F00FF, #E100FF);
    -fx-border-width: 1.5px;
    -fx-border-radius: 9px;
    -fx-effect: dropshadow(gaussian, rgba(127, 0, 255, 0.35), 10, 0.2, 0, 0);
}
```

### 4.3 Menu Lệnh Lập trình (Slash Commands)
Menu nổi xuất hiện khi gõ `/` (`/goi-y`, `/sua-loi`, `/dich`), trượt nhẹ `10px` bằng hoạt ảnh kết hợp `FadeTransition` và `TranslateTransition` trong `150ms`.
