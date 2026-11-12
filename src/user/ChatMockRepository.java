package user;

import java.util.*;

/**
 * Kho Dữ liệu Mẫu Tập trung cho Hội thoại & Tin nhắn Người dùng
 * Phục vụ hiển thị giao diện mốc 20% và Tìm kiếm Lịch sử Chat Toàn cục (Mục 11.g):
 * "Tìm kiếm chuỗi trong lịch sử chat với tất cả mọi người, có thể di chuyển đến đoạn chat đang tìm"
 */
public class ChatMockRepository {

    public static class ConversationMeta {
        private final String id;
        private final String title;
        private final String subtitle;
        private final String snippet;
        private final String lastTime;
        private final boolean encrypted;
        private final int unreadCount;
        private final boolean online;
        private final String avatarColor;
        private final String initials;
        private final boolean group;
        private final List<ChatConversationView.MessageItem> messages = new ArrayList<>();

        public ConversationMeta(String id, String title, String subtitle, String snippet,
                                String lastTime, boolean encrypted, int unreadCount,
                                boolean online, String avatarColor, String initials,
                                boolean group) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.snippet = snippet;
            this.lastTime = lastTime;
            this.encrypted = encrypted;
            this.unreadCount = unreadCount;
            this.online = online;
            this.avatarColor = avatarColor;
            this.initials = initials;
            this.group = group;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getSnippet() { return snippet; }
        public String getLastTime() { return lastTime; }
        public boolean isEncrypted() { return encrypted; }
        public int getUnreadCount() { return unreadCount; }
        public boolean isOnline() { return online; }
        public String getAvatarColor() { return avatarColor; }
        public String getInitials() { return initials; }
        public boolean isGroup() { return group; }
        public List<ChatConversationView.MessageItem> getMessages() { return messages; }

        public void addMessage(ChatConversationView.MessageItem msg) {
            messages.add(msg);
        }
    }

    public static class GlobalMessageMatch {
        private final String chatId;
        private final String chatTitle;
        private final String avatarColor;
        private final String initials;
        private final boolean group;
        private final ChatConversationView.MessageItem message;

        public GlobalMessageMatch(String chatId, String chatTitle, String avatarColor,
                                  String initials, boolean group,
                                  ChatConversationView.MessageItem message) {
            this.chatId = chatId;
            this.chatTitle = chatTitle;
            this.avatarColor = avatarColor;
            this.initials = initials;
            this.group = group;
            this.message = message;
        }

        public String getChatId() { return chatId; }
        public String getChatTitle() { return chatTitle; }
        public String getAvatarColor() { return avatarColor; }
        public String getInitials() { return initials; }
        public boolean isGroup() { return group; }
        public ChatConversationView.MessageItem getMessage() { return message; }
    }

    private static final Map<String, ConversationMeta> CONVERSATIONS = new LinkedHashMap<>();

    static {
        initMockData();
    }

    private static void initMockData() {
        CONVERSATIONS.clear();

        // 1. Nhóm Đồ Án Java 2026 (Group, E2EE)
        ConversationMeta c1 = new ConversationMeta(
            "group_project",
            "Nhóm Đồ Án Java 2026",
            "3 thành viên • Mã hóa đầu cuối AES-GCM 256-bit",
            "💡 Các bạn nhớ chụp ảnh minh chứng...",
            "10:19",
            true,
            2,
            true,
            "#4E6BF2",
            "PJ",
            true
        );
        c1.addMessage(new ChatConversationView.MessageItem(
            "msg_1",
            "Lưu Chí Hải",
            "10:15",
            "Script database.sql đã chạy hoàn tất 13 bảng trên PostgreSQL rồi nhé!",
            false,
            false
        ));
        c1.addMessage(new ChatConversationView.MessageItem(
            "msg_2",
            "Nguyễn Lan Anh",
            "10:16",
            "Tuyệt vời, mình đã test thử các ràng buộc khóa ngoại và index idx_login_time chạy rất nhanh.",
            false,
            false
        ));
        c1.addMessage(new ChatConversationView.MessageItem(
            "msg_3",
            "Trần Nguyễn Tuấn Minh",
            "10:18",
            "Giao diện User JavaFX 21 + theme.css đã hoàn thành toàn bộ form Đăng nhập, Đăng ký 7 trường và khung chat rồi.",
            true,
            false
        ));
        c1.addMessage(new ChatConversationView.MessageItem(
            "msg_4",
            "Trợ lý AI",
            "10:19",
            "💡 Các bạn nhớ chụp ảnh minh chứng TableView và các trường nhập liệu trước ngày 22/11/2026 để tổng hợp vào Báo cáo PDF mốc 20% nhé!",
            false,
            true
        ));
        CONVERSATIONS.put(c1.getId(), c1);

        // 2. Chat 1-1: Lưu Chí Hải (Online)
        ConversationMeta c2 = new ConversationMeta(
            "user_chihai",
            "Lưu Chí Hải",
            "Đang trực tuyến • @chihai_2026",
            "Mình xem rồi, icon trắng Lucide rất nét!",
            "14:02",
            false,
            0,
            true,
            "#23A55A",
            "CH",
            false
        );
        c2.addMessage(new ChatConversationView.MessageItem(
            "msg_5",
            "Trần Nguyễn Tuấn Minh",
            "14:00",
            "Hải ơi, cậu kiểm tra giúp mình nhánh feat/user-ui xem các icon vector trắng đã tương phản rõ chưa nhé.",
            true,
            false
        ));
        c2.addMessage(new ChatConversationView.MessageItem(
            "msg_6",
            "Lưu Chí Hải",
            "14:02",
            "Mình xem rồi, icon trắng Lucide trên nền badge 38x38px nhìn rất sắc nét và chuyên nghiệp!",
            false,
            false
        ));
        CONVERSATIONS.put(c2.getId(), c2);

        // 3. Chat 1-1: Nguyễn Lan Anh (Online)
        ConversationMeta c3 = new ConversationMeta(
            "user_lananh",
            "Nguyễn Lan Anh",
            "Đang trực tuyến • @lananh_it",
            "Tuyệt vời, mình đã test ràng buộc CSDL...",
            "10:16",
            false,
            0,
            true,
            "#E91E63",
            "LA",
            false
        );
        c3.addMessage(new ChatConversationView.MessageItem(
            "msg_7",
            "Nguyễn Lan Anh",
            "10:10",
            "Minh ơi, phần báo cáo mốc 20% cần tổng hợp thêm biểu đồ gì về cấu trúc mã nguồn không?",
            false,
            false
        ));
        c3.addMessage(new ChatConversationView.MessageItem(
            "msg_8",
            "Trần Nguyễn Tuấn Minh",
            "10:12",
            "Có nhé Lan Anh, cần sơ đồ ERD 13 bảng của Hải và cấu trúc package JavaFX của mình.",
            true,
            false
        ));
        c3.addMessage(new ChatConversationView.MessageItem(
            "msg_9",
            "Nguyễn Lan Anh",
            "10:14",
            "Ok, mình đang chuẩn bị bảng đối chiếu chỉ tiêu mốc 20% để chèn vào báo cáo.",
            false,
            false
        ));
        CONVERSATIONS.put(c3.getId(), c3);

        // 4. Nhóm Thể Thao Cuối Tuần (Group)
        ConversationMeta c4 = new ConversationMeta(
            "group_sport",
            "Nhóm Thể Thao Cuối Tuần",
            "12 thành viên • Kế hoạch giao lưu thể thao",
            "Thứ 7 này 17h đá bóng sân cỏ nhé!",
            "Hôm qua",
            false,
            0,
            false,
            "#FF9800",
            "TT",
            true
        );
        c4.addMessage(new ChatConversationView.MessageItem(
            "msg_10",
            "Lê Hoàng Nam",
            "08:30",
            "Thứ 7 này 17h đá bóng sân cỏ nhé cả nhóm!",
            false,
            false
        ));
        c4.addMessage(new ChatConversationView.MessageItem(
            "msg_11",
            "Phạm Đức Huy",
            "08:45",
            "Ok Nam ơi, mình sẽ mang áo đồng phục xanh.",
            false,
            false
        ));
        c4.addMessage(new ChatConversationView.MessageItem(
            "msg_12",
            "Trần Nguyễn Tuấn Minh",
            "09:00",
            "Mình tham gia nhé, đá xong anh em đi ăn tối trao đổi đồ án luôn.",
            true,
            false
        ));
        CONVERSATIONS.put(c4.getId(), c4);

        // 5. Chat 1-1: Lê Hoàng Nam (Online)
        ConversationMeta c5 = new ConversationMeta(
            "user_namle",
            "Lê Hoàng Nam",
            "Đang trực tuyến • @nam_le",
            "Chiều nay có họp nhóm không Tuấn Minh?",
            "09:30",
            false,
            0,
            true,
            "#9C27B0",
            "HN",
            false
        );
        c5.addMessage(new ChatConversationView.MessageItem(
            "msg_13",
            "Lê Hoàng Nam",
            "09:30",
            "Chiều nay có họp nhóm không Tuấn Minh?",
            false,
            false
        ));
        c5.addMessage(new ChatConversationView.MessageItem(
            "msg_14",
            "Trần Nguyễn Tuấn Minh",
            "09:35",
            "Có nhé, 15h chúng ta họp qua Google Meet để thống nhất checklist nộp bài.",
            true,
            false
        ));
        CONVERSATIONS.put(c5.getId(), c5);

        // 6. Chat 1-1: Phạm Đức Huy (Offline)
        ConversationMeta c6 = new ConversationMeta(
            "user_duchuy",
            "Phạm Đức Huy",
            "Ngoại tuyến • @duchuy_dev",
            "Ok ông nhé, tài liệu gửi qua Drive rồi.",
            "T3",
            false,
            0,
            false,
            "#80848E",
            "DH",
            false
        );
        c6.addMessage(new ChatConversationView.MessageItem(
            "msg_15",
            "Phạm Đức Huy",
            "11:20",
            "Ok ông nhé, tài liệu gửi qua Drive rồi.",
            false,
            false
        ));
        c6.addMessage(new ChatConversationView.MessageItem(
            "msg_16",
            "Trần Nguyễn Tuấn Minh",
            "11:25",
            "Cảm ơn Huy, mình đã tải tài liệu về máy và kiểm tra đầy đủ rồi.",
            true,
            false
        ));
        CONVERSATIONS.put(c6.getId(), c6);
    }

    public static Collection<ConversationMeta> getAllConversations() {
        return CONVERSATIONS.values();
    }

    public static ConversationMeta getConversation(String id) {
        return CONVERSATIONS.get(id);
    }

    /**
     * Tìm kiếm tin nhắn trên TẤT CẢ các cuộc trò chuyện (Mục 11.g)
     * Trả về danh sách kết quả chứa thông tin hội thoại và tin nhắn tương ứng
     */
    public static List<GlobalMessageMatch> searchAllMessages(String query) {
        List<GlobalMessageMatch> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String q = query.trim().toLowerCase();
        for (ConversationMeta conv : CONVERSATIONS.values()) {
            for (ChatConversationView.MessageItem msg : conv.getMessages()) {
                if (msg.getContent().toLowerCase().contains(q) ||
                    msg.getSenderName().toLowerCase().contains(q)) {
                    results.add(new GlobalMessageMatch(
                        conv.getId(),
                        conv.getTitle(),
                        conv.getAvatarColor(),
                        conv.getInitials(),
                        conv.isGroup(),
                        msg
                    ));
                }
            }
        }
        return results;
    }

    /**
     * Tìm kiếm cuộc trò chuyện theo tên hoặc tóm tắt
     */
    public static List<ConversationMeta> searchConversations(String query) {
        List<ConversationMeta> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(CONVERSATIONS.values());
        }

        String q = query.trim().toLowerCase();
        for (ConversationMeta conv : CONVERSATIONS.values()) {
            if (conv.getTitle().toLowerCase().contains(q) ||
                conv.getSnippet().toLowerCase().contains(q)) {
                results.add(conv);
            }
        }
        return results;
    }
}
