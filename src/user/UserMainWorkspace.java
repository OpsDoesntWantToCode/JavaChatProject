package user;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Khung Không gian Làm việc Chính của Phân hệ Người dùng (Task U3 — UserMainWorkspace)
 * Thiết kế công thái học Obsidian Dark Surface theo mô hình 4 Cột tiêu chuẩn:
 * - Cột 1: Navigation Rail (60px cố định, nền #0F1013)
 * - Cột 2: Sub-Sidebar (300px cố định, nền #1B1D22, viền hairline #2A2D35)
 * - Cột 3: Main View (Tự co giãn, nền #141619)
 * - Cột 4: Right Drawer (280px trượt đóng/mở mượt mà)
 */
public class UserMainWorkspace extends BorderPane {

    private final Stage primaryStage;
    private final Runnable onLogout;

    // Cột 1: Navigation Rail
    private final VBox navRail = new VBox(16);
    private final Button btnNavChat = new Button("💬");
    private final Button btnNavFriends = new Button("👥");
    private final Button btnNavSettings = new Button("⚙️");
    private final Button btnNavLogout = new Button("🚪");
    private boolean isChatMode = true;

    // Cột 2: Sub-Sidebar
    private final VBox subSidebar = new VBox(12);
    private final Label lblSidebarTitle = new Label("Đoạn chat");
    private final TextField txtSearch = new TextField();
    private final VBox conversationListBox = new VBox(4);
    private final ScrollPane conversationScrollPane = new ScrollPane();

    // Cột 3: Main View
    private final BorderPane mainViewPane = new BorderPane();
    private final HBox chatHeader = new HBox(12);
    private final Label lblChatTitle = new Label();
    private final Label lblChatSubtitle = new Label();
    private final VBox messagesContainer = new VBox(12);
    private final ScrollPane messagesScrollPane = new ScrollPane();
    private final TextField txtMessageInput = new TextField();
    private final HBox smartRepliesBar = new HBox(8);

    // Cột 4: Right Drawer
    private final VBox rightDrawer = new VBox(14);
    private final ScrollPane drawerScrollPane = new ScrollPane();
    private boolean isDrawerOpen = true;

    // Danh sách bạn bè phục vụ Chế độ Danh bạ (Mode Friends)
    private final VBox friendsContainer = new VBox(12);
    private final ScrollPane friendsScrollPane = new ScrollPane();

    // Dữ liệu cuộc hội thoại đang chọn
    private String currentChatId = "group_project";

    public UserMainWorkspace(Stage primaryStage, Runnable onLogout) {
        this.primaryStage = primaryStage;
        this.onLogout = onLogout;

        getStyleClass().add("ambient-canvas");

        // 1. Dựng Cột 1: Navigation Rail (60px)
        buildNavigationRail();

        // 2. Dựng Cột 2: Sub-Sidebar (300px)
        buildSubSidebar();

        // 3. Dựng Cột 3: Main View (Flexible)
        buildMainView();

        // 4. Dựng Cột 4: Right Drawer (280px)
        buildRightDrawer();

        // Lắp ráp bố cục tổng thể
        setLeft(new HBox(navRail, subSidebar));
        setCenter(mainViewPane);
        setRight(rightDrawer);

        // Nạp dữ liệu hội thoại mặc định (Nhóm Đồ Án Java 2026 có mã hóa)
        loadConversation("group_project");
    }

    /* ==========================================================================
       CỘT 1: NAVIGATION RAIL (60px)
       ========================================================================== */
    private void buildNavigationRail() {
        navRail.setPrefWidth(60);
        navRail.setMinWidth(60);
        navRail.setMaxWidth(60);
        navRail.setAlignment(Pos.TOP_CENTER);
        navRail.setPadding(new Insets(16, 0, 16, 0));
        navRail.getStyleClass().add("nav-rail");

        // Avatar người dùng hiện tại (Trần Nguyễn Tuấn Minh - tuanminh_2026)
        StackPane avatarBox = new StackPane();
        avatarBox.setPrefSize(42, 42);
        avatarBox.getStyleClass().add("nav-avatar-btn");

        Circle avatarCircle = new Circle(20, Color.web("#4E6BF2"));
        Label lblInitials = new Label("TM");
        lblInitials.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Circle onlineDot = new Circle(4.5, Color.web("#23A55A"));
        onlineDot.setStroke(Color.web("#0F1013"));
        onlineDot.setStrokeWidth(2);
        StackPane.setAlignment(onlineDot, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(onlineDot, new Insets(0, 2, 2, 0));

        avatarBox.getChildren().addAll(avatarCircle, lblInitials, onlineDot);
        avatarBox.setOnMouseClicked(e -> openSettingsDialog());
        Tooltip.install(avatarBox, new Tooltip("Hồ sơ & Cài đặt (tuanminh_2026)"));

        // Nút chuyển chế độ
        btnNavChat.getStyleClass().addAll("nav-item-btn", "nav-item-btn-active");
        btnNavFriends.getStyleClass().add("nav-item-btn");
        btnNavSettings.getStyleClass().add("nav-item-btn");
        btnNavLogout.getStyleClass().add("nav-item-btn");

        Tooltip.install(btnNavChat, new Tooltip("Đoạn chat & Nhóm"));
        Tooltip.install(btnNavFriends, new Tooltip("Danh bạ & Bạn bè"));
        Tooltip.install(btnNavSettings, new Tooltip("Cài đặt hệ thống"));
        Tooltip.install(btnNavLogout, new Tooltip("Đăng xuất tài khoản"));

        btnNavChat.setOnAction(e -> switchMode(true));
        btnNavFriends.setOnAction(e -> switchMode(false));
        btnNavSettings.setOnAction(e -> openSettingsDialog());
        btnNavLogout.setOnAction(e -> {
            if (onLogout != null) {
                onLogout.run();
            }
        });

        // Spacer đẩy Settings & Logout xuống đáy
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        navRail.getChildren().addAll(
            avatarBox,
            new Separator(),
            btnNavChat,
            btnNavFriends,
            spacer,
            btnNavSettings,
            btnNavLogout
        );
    }

    private void switchMode(boolean chatMode) {
        this.isChatMode = chatMode;
        if (chatMode) {
            btnNavChat.getStyleClass().add("nav-item-btn-active");
            btnNavFriends.getStyleClass().remove("nav-item-btn-active");
            lblSidebarTitle.setText("Đoạn chat");
            txtSearch.setPromptText("🔍 Tìm kiếm cuộc trò chuyện...");
            conversationScrollPane.setContent(conversationListBox);
            mainViewPane.setCenter(messagesScrollPane);
            mainViewPane.setBottom(createChatInputBar());
            rightDrawer.setVisible(isDrawerOpen);
            rightDrawer.setManaged(isDrawerOpen);
            loadConversation(currentChatId);
        } else {
            btnNavFriends.getStyleClass().add("nav-item-btn-active");
            btnNavChat.getStyleClass().remove("nav-item-btn-active");
            lblSidebarTitle.setText("Danh bạ");
            txtSearch.setPromptText("🔍 Tìm bạn bè theo tên...");
            buildFriendsSidebarList();
            buildFriendsCenterView("all");
            rightDrawer.setVisible(false);
            rightDrawer.setManaged(false);
        }
    }

    private void openSettingsDialog() {
        UserSettingsDialog dialog = new UserSettingsDialog(primaryStage);
        dialog.showAndWait();
    }

    /* ==========================================================================
       CỘT 2: SUB-SIDEBAR (300px)
       ========================================================================== */
    private void buildSubSidebar() {
        subSidebar.setPrefWidth(300);
        subSidebar.setMinWidth(300);
        subSidebar.setMaxWidth(300);
        subSidebar.setPadding(new Insets(16, 12, 16, 12));
        subSidebar.getStyleClass().add("sub-sidebar");

        HBox topBox = new HBox(8);
        topBox.setAlignment(Pos.CENTER_LEFT);
        lblSidebarTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnNewGroup = new Button("➕");
        btnNewGroup.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnNewGroup, new Tooltip("Tạo nhóm trò chuyện mới"));
        btnNewGroup.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(primaryStage);
            alert.setTitle("Tạo nhóm mới");
            alert.setHeaderText("Tạo nhóm chat kèm mã hóa E2EE");
            alert.setContentText("Hộp thoại tạo nhóm chat với công tắc mã hóa AES-GCM (Mục 12.a) sẽ kết nối Socket ở mốc 80%.");
            alert.showAndWait();
        });

        topBox.getChildren().addAll(lblSidebarTitle, sp, btnNewGroup);

        txtSearch.setPromptText("🔍 Tìm kiếm cuộc trò chuyện...");
        txtSearch.getStyleClass().add("sidebar-search-field");

        conversationScrollPane.setFitToWidth(true);
        conversationScrollPane.getStyleClass().add("transparent-scroll");
        VBox.setVgrow(conversationScrollPane, Priority.ALWAYS);

        buildConversationList();
        conversationScrollPane.setContent(conversationListBox);

        subSidebar.getChildren().addAll(topBox, txtSearch, conversationScrollPane);
    }

    private void buildConversationList() {
        conversationListBox.getChildren().clear();

        // 1. Nhóm Đồ Án Java 2026 (Active, Có mã hóa)
        conversationListBox.getChildren().add(createConversationItem(
            "group_project", "Nhóm Đồ Án Java 2026", "💡 Các bạn nhớ chụp ảnh minh chứng...",
            "10:19", true, 2, true, "#4E6BF2", "PJ"
        ));

        // 2. Chat 1-1: Lưu Chí Hải (Online)
        conversationListBox.getChildren().add(createConversationItem(
            "user_chihai", "Lưu Chí Hải", "Mình xem rồi, icon trắng Lucide rất nét!",
            "14:02", false, 0, true, "#23A55A", "CH"
        ));

        // 3. Chat 1-1: Nguyễn Lan Anh (Online)
        conversationListBox.getChildren().add(createConversationItem(
            "user_lananh", "Nguyễn Lan Anh", "Tuyệt vời, mình đã test ràng buộc CSDL...",
            "10:16", false, 0, true, "#E91E63", "LA"
        ));

        // 4. Nhóm Thể Thao Cuối Tuần (Nhóm thường)
        conversationListBox.getChildren().add(createConversationItem(
            "group_sport", "Nhóm Thể Thao Cuối Tuần", "Thứ 7 này 17h đá bóng sân cỏ nhé!",
            "Hôm qua", false, 0, false, "#FF9800", "TT"
        ));

        // 5. Chat 1-1: Lê Hoàng Nam (Online)
        conversationListBox.getChildren().add(createConversationItem(
            "user_namle", "Lê Hoàng Nam", "Chiều nay có họp nhóm không Tuấn Minh?",
            "09:30", false, 0, true, "#9C27B0", "HN"
        ));

        // 6. Chat 1-1: Phạm Đức Huy (Offline)
        conversationListBox.getChildren().add(createConversationItem(
            "user_duchuy", "Phạm Đức Huy", "Ok ông nhé, tài liệu gửi qua Drive rồi.",
            "T3", false, 0, false, "#80848E", "DH"
        ));
    }

    private HBox createConversationItem(String id, String title, String snippet, String time,
                                       boolean isEncrypted, int unreadCount, boolean isOnline,
                                       String avatarColor, String initials) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("conversation-item");
        if (id.equals(currentChatId)) {
            item.getStyleClass().add("conversation-item-selected");
        }

        // Avatar Box
        StackPane avtBox = new StackPane();
        Circle c = new Circle(18, Color.web(avatarColor));
        Label lblI = new Label(initials);
        lblI.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        avtBox.getChildren().addAll(c, lblI);

        if (isOnline) {
            Circle dot = new Circle(4, Color.web("#23A55A"));
            dot.setStroke(Color.web("#1B1D22"));
            dot.setStrokeWidth(1.5);
            StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
            avtBox.getChildren().add(dot);
        }

        // Text Meta
        VBox meta = new VBox(2);
        HBox titleRow = new HBox(6);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label lblT = new Label(title);
        lblT.getStyleClass().add("conversation-item-title");

        if (isEncrypted) {
            Label lblLock = new Label("🔒");
            lblLock.setStyle("-fx-font-size: 11px;");
            titleRow.getChildren().addAll(lblT, lblLock);
        } else {
            titleRow.getChildren().add(lblT);
        }

        Label lblS = new Label(snippet);
        lblS.getStyleClass().add("conversation-item-snippet");

        meta.getChildren().addAll(titleRow, lblS);
        HBox.setHgrow(meta, Priority.ALWAYS);

        // Right side (Time + Unread Badge)
        VBox rightBox = new VBox(4);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblTime = new Label(time);
        lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        rightBox.getChildren().add(lblTime);

        if (unreadCount > 0) {
            Label lblBadge = new Label(String.valueOf(unreadCount));
            lblBadge.getStyleClass().add("unread-badge");
            rightBox.getChildren().add(lblBadge);
        }

        item.getChildren().addAll(avtBox, meta, rightBox);

        item.setOnMouseClicked(e -> {
            this.currentChatId = id;
            buildConversationList();
            loadConversation(id);
        });

        return item;
    }

    private void buildFriendsSidebarList() {
        conversationListBox.getChildren().clear();

        conversationListBox.getChildren().addAll(
            createFriendsCategoryItem("all", "👥 Tất cả bạn bè", 5),
            createFriendsCategoryItem("online", "🟢 Đang trực tuyến", 3),
            createFriendsCategoryItem("pending", "📩 Lời mời kết bạn", 2),
            createFriendsCategoryItem("blocked", "⛔ Đã chặn", 1),
            createFriendsCategoryItem("add", "➕ Thêm bạn / Tìm kiếm", 0)
        );
    }

    private HBox createFriendsCategoryItem(String catId, String title, int count) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("conversation-item");

        Label lbl = new Label(title);
        lbl.getStyleClass().add("conversation-item-title");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        item.getChildren().add(lbl);

        if (count > 0) {
            Label cnt = new Label(String.valueOf(count));
            cnt.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted; -fx-background-color: #262932; -fx-padding: 2 6; -fx-background-radius: 10px;");
            item.getChildren().add(cnt);
        }

        item.setOnMouseClicked(e -> buildFriendsCenterView(catId));
        return item;
    }

    /* ==========================================================================
       CỘT 3: MAIN VIEW (Flexible)
       ========================================================================== */
    private void buildMainView() {
        mainViewPane.getStyleClass().add("main-chat-pane");

        // 1. Header cuộc trò chuyện
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setPadding(new Insets(12, 20, 12, 20));
        chatHeader.getStyleClass().add("chat-header");

        VBox titleBox = new VBox(2);
        lblChatTitle.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        lblChatSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        titleBox.getChildren().addAll(lblChatTitle, lblChatSubtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnSearchMsg = new Button("🔍");
        btnSearchMsg.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnSearchMsg, new Tooltip("Tìm kiếm trong cuộc trò chuyện (Mục 11)"));
        btnSearchMsg.setOnAction(e -> {
            TextInputDialog td = new TextInputDialog();
            td.initOwner(primaryStage);
            td.setTitle("Tìm kiếm tin nhắn");
            td.setHeaderText("Tìm kiếm tin nhắn trong hội thoại này");
            td.setContentText("Nhập từ khóa cần tìm:");
            td.showAndWait().ifPresent(kw -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.initOwner(primaryStage);
                a.setHeaderText("Kết quả tìm kiếm");
                a.setContentText("Tính năng cuộn tự động và Highlight vàng 1.5s cho từ khóa '" + kw + "' sẽ kết nối Socket ở mốc 80%.");
                a.showAndWait();
            });
        });

        Button btnReportSpam = new Button("⚠️");
        btnReportSpam.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnReportSpam, new Tooltip("Báo cáo vi phạm / Spam"));
        btnReportSpam.setOnAction(e -> handleReportSpam());

        Button btnToggleDrawer = new Button("ℹ️");
        btnToggleDrawer.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnToggleDrawer, new Tooltip("Thông tin chi tiết (Cột 4)"));
        btnToggleDrawer.setOnAction(e -> toggleRightDrawer());

        chatHeader.getChildren().addAll(titleBox, btnSearchMsg, btnReportSpam, btnToggleDrawer);
        mainViewPane.setTop(chatHeader);

        // 2. Vùng hiển thị tin nhắn (ScrollPane)
        messagesContainer.setPadding(new Insets(16, 24, 16, 24));
        messagesScrollPane.setContent(messagesContainer);
        messagesScrollPane.setFitToWidth(true);
        messagesScrollPane.getStyleClass().add("transparent-scroll");
        messagesScrollPane.vvalueProperty().bind(messagesContainer.heightProperty());
        mainViewPane.setCenter(messagesScrollPane);

        // 3. Thanh nhập tin nhắn ở dưới đáy
        mainViewPane.setBottom(createChatInputBar());
    }

    private VBox createChatInputBar() {
        VBox bottomContainer = new VBox(8);
        bottomContainer.setPadding(new Insets(8, 20, 16, 20));
        bottomContainer.getStyleClass().add("chat-bottom-bar");

        // Gợi ý phản hồi nhanh (Smart Reply Chips)
        smartRepliesBar.getChildren().clear();
        smartRepliesBar.getChildren().addAll(
            createSmartChip("💡 Tóm tắt cuộc họp"),
            createSmartChip("📁 Gửi tài liệu tiến độ"),
            createSmartChip("✓ Đã hiểu & Xác nhận")
        );

        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        Button btnAttach = new Button("📎");
        btnAttach.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnAttach, new Tooltip("Đính kèm tệp tin"));

        txtMessageInput.setPromptText("Nhập tin nhắn vào hội thoại... (Nhấn Enter để gửi)");
        txtMessageInput.getStyleClass().add("chat-input-field");
        HBox.setHgrow(txtMessageInput, Priority.ALWAYS);
        txtMessageInput.setOnAction(e -> handleSendMessage());

        Button btnAiSuggest = new Button("✨ Gợi ý AI");
        btnAiSuggest.getStyleClass().add("btn-ai-suggest");
        btnAiSuggest.setOnAction(e -> handleAiSuggestion());

        Button btnSend = new Button("Gửi 🚀");
        btnSend.getStyleClass().add("btn-primary");
        btnSend.setOnAction(e -> handleSendMessage());

        inputRow.getChildren().addAll(btnAttach, txtMessageInput, btnAiSuggest, btnSend);
        bottomContainer.getChildren().addAll(smartRepliesBar, inputRow);
        return bottomContainer;
    }

    private Button createSmartChip(String text) {
        Button chip = new Button(text);
        chip.getStyleClass().add("smart-reply-chip");
        chip.setOnAction(e -> {
            txtMessageInput.setText(text);
            txtMessageInput.requestFocus();
            txtMessageInput.positionCaret(text.length());
        });
        return chip;
    }

    private void handleSendMessage() {
        String msg = txtMessageInput.getText().trim();
        if (msg.isEmpty()) {
            return;
        }

        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        messagesContainer.getChildren().add(createMessageBubble("Trần Nguyễn Tuấn Minh", now, msg, true, false));
        txtMessageInput.clear();
    }

    private void handleAiSuggestion() {
        txtMessageInput.setText("Tôi đã xem qua tài liệu, tiến độ hiện tại rất khả quan và sẵn sàng cho buổi review mốc 20%.");
        txtMessageInput.requestFocus();
    }

    private void handleReportSpam() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("Báo cáo vi phạm");
        alert.setHeaderText("Gửi báo cáo vi phạm tới Ban quản trị (Mục 11.g)");
        alert.setContentText("Bạn có chắc chắn muốn gửi báo cáo spam đối với cuộc trò chuyện này không?");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.initOwner(primaryStage);
                ok.setHeaderText("Báo cáo đã được ghi nhận");
                ok.setContentText("Báo cáo của bạn đã được lưu vào bảng spam_reports trong CSDL để Quản trị viên xử lý.");
                ok.showAndWait();
            }
        });
    }

    private void loadConversation(String chatId) {
        messagesContainer.getChildren().clear();

        if ("group_project".equals(chatId)) {
            lblChatTitle.setText("🔒 Nhóm Đồ Án Java 2026");
            lblChatSubtitle.setText("3 thành viên • Mã hóa đầu cuối AES-GCM 256-bit");

            // Banner E2EE kiểu Messenger
            HBox e2eeBanner = new HBox(10);
            e2eeBanner.setAlignment(Pos.CENTER);
            e2eeBanner.setPadding(new Insets(10, 16, 10, 16));
            e2eeBanner.getStyleClass().add("e2ee-banner");

            Label lblBanner = new Label("🔒 Tin nhắn trong nhóm này được bảo vệ bằng mã hóa đối xứng AES-GCM. Chỉ các thành viên trong nhóm mới có khóa giải mã.");
            lblBanner.setWrapText(true);
            lblBanner.setStyle("-fx-font-size: 12px; -fx-text-fill: #A0C0FF;");
            e2eeBanner.getChildren().add(lblBanner);
            messagesContainer.getChildren().add(e2eeBanner);

            // Kịch bản chat mẫu từ dummy-data-matrix.md
            messagesContainer.getChildren().addAll(
                createMessageBubble("Lưu Chí Hải", "10:15", "Script database.sql đã chạy hoàn tất 13 bảng trên PostgreSQL rồi nhé!", false, false),
                createMessageBubble("Nguyễn Lan Anh", "10:16", "Tuyệt vời, mình đã test thử các ràng buộc khóa ngoại và index idx_login_time chạy rất nhanh.", false, false),
                createMessageBubble("Trần Nguyễn Tuấn Minh", "10:18", "Giao diện User JavaFX 21 + theme.css đã hoàn thành toàn bộ form Đăng nhập, Đăng ký 7 trường và khung chat rồi.", true, false),
                createMessageBubble("Trợ lý AI [BOT]", "10:19", "💡 Các bạn nhớ chụp ảnh minh chứng TableView và các trường nhập liệu trước ngày 22/11/2026 để tổng hợp vào Báo cáo PDF mốc 20% nhé!", false, true)
            );
        } else if ("user_chihai".equals(chatId)) {
            lblChatTitle.setText("💬 Lưu Chí Hải");
            lblChatSubtitle.setText("Đang trực tuyến • @chihai_2026");

            messagesContainer.getChildren().addAll(
                createMessageBubble("Trần Nguyễn Tuấn Minh", "14:00", "Hải ơi, cậu kiểm tra giúp mình nhánh feat/user-ui xem các icon vector trắng đã tương phản rõ chưa nhé.", true, false),
                createMessageBubble("Lưu Chí Hải", "14:02", "Mình xem rồi, icon trắng Lucide trên nền badge 38x38px nhìn rất sắc nét và chuyên nghiệp!", false, false)
            );
        } else {
            lblChatTitle.setText("💬 Cuộc trò chuyện");
            lblChatSubtitle.setText("Đang hoạt động");
            messagesContainer.getChildren().add(createMessageBubble("Hệ thống", "12:00", "Bắt đầu cuộc trò chuyện mới...", false, false));
        }

        updateRightDrawerContent();
    }

    private Node createMessageBubble(String sender, String time, String text, boolean isOutgoing, boolean isAi) {
        VBox bubbleBox = new VBox(4);
        bubbleBox.setMaxWidth(520);

        HBox metaRow = new HBox(6);
        metaRow.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label lblSender = new Label(sender);
        lblSender.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");
        if (isAi) {
            lblSender.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #9D7BFF;");
        }

        Label lblTime = new Label(time);
        lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");

        metaRow.getChildren().addAll(lblSender, lblTime);

        Label lblContent = new Label(text);
        lblContent.setWrapText(true);
        lblContent.getStyleClass().add(isOutgoing ? "chat-bubble-sent" : "chat-bubble-received");

        if (isAi) {
            lblContent.setStyle("-fx-background-color: #211A33; -fx-border-color: #5D4399; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 10px 14px; -fx-text-fill: #EDEDED;");
        }

        bubbleBox.getChildren().addAll(metaRow, lblContent);

        HBox wrapper = new HBox(bubbleBox);
        wrapper.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return wrapper;
    }

    /* ==========================================================================
       CỘT 4: RIGHT DRAWER (280px)
       ========================================================================== */
    private void buildRightDrawer() {
        rightDrawer.setPrefWidth(280);
        rightDrawer.setMinWidth(280);
        rightDrawer.setMaxWidth(280);
        rightDrawer.setPadding(new Insets(16));
        rightDrawer.getStyleClass().add("right-drawer");

        drawerScrollPane.setFitToWidth(true);
        drawerScrollPane.getStyleClass().add("transparent-scroll");
        VBox.setVgrow(drawerScrollPane, Priority.ALWAYS);

        updateRightDrawerContent();
    }

    private void updateRightDrawerContent() {
        VBox drawerBody = new VBox(14);

        // Header Drawer
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label lblHeader = new Label("Chi tiết hội thoại");
        lblHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("dialog-close-btn");
        btnClose.setOnAction(e -> toggleRightDrawer());
        topRow.getChildren().addAll(lblHeader, sp, btnClose);

        // Avatar lớn & Tên
        VBox profileCard = new VBox(6);
        profileCard.setAlignment(Pos.CENTER);
        Circle bigAvt = new Circle(32, Color.web("#4E6BF2"));
        Label lblBigName = new Label("Nhóm Đồ Án Java 2026");
        lblBigName.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblBigSub = new Label("Khởi tạo ngày 23/10/2026");
        lblBigSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted;");
        profileCard.getChildren().addAll(bigAvt, lblBigName, lblBigSub);

        // Phân đoạn: Quản lý Thành viên
        VBox membersSection = new VBox(8);
        Label lblSecMembers = new Label("THÀNH VIÊN NHÓM (3)");
        lblSecMembers.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        membersSection.getChildren().addAll(
            lblSecMembers,
            createMemberRow("Trần Nguyễn Tuấn Minh", "Admin (Bạn)", true),
            createMemberRow("Lưu Chí Hải", "Admin", false),
            createMemberRow("Nguyễn Lan Anh", "Thành viên", false)
        );

        // Phân đoạn: Bảo mật E2EE
        VBox secSection = new VBox(6);
        Label lblSecSec = new Label("MÃ HÓA & BẢO MẬT");
        lblSecSec.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        Label lblKeyInfo = new Label("Khóa DEK: AES-GCM 256-bit");
        lblKeyInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: -status-online;");

        Label lblFingerprint = new Label("Mã định danh: 7f3b...e92a");
        lblFingerprint.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        secSection.getChildren().addAll(lblSecSec, lblKeyInfo, lblFingerprint);

        // Phân đoạn: Thao tác nguy hiểm
        VBox dangerSection = new VBox(8);
        Button btnClearHistory = new Button("🗑 Xóa lịch sử trò chuyện");
        btnClearHistory.getStyleClass().add("btn-danger-outline");
        btnClearHistory.setMaxWidth(Double.MAX_VALUE);
        btnClearHistory.setOnAction(e -> {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.initOwner(primaryStage);
            c.setHeaderText("Xác nhận xóa lịch sử");
            c.setContentText("Bạn có chắc chắn muốn xóa lịch sử cuộc trò chuyện này ở phía bạn?");
            c.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    messagesContainer.getChildren().clear();
                }
            });
        });
        dangerSection.getChildren().add(btnClearHistory);

        drawerBody.getChildren().addAll(topRow, new Separator(), profileCard, new Separator(), membersSection, new Separator(), secSection, new Separator(), dangerSection);
        drawerScrollPane.setContent(drawerBody);
        rightDrawer.getChildren().setAll(drawerScrollPane);
    }

    private HBox createMemberRow(String name, String role, boolean isSelf) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(3, Color.web("#4E6BF2"));
        VBox meta = new VBox(1);
        Label lblN = new Label(name);
        lblN.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblR = new Label(role);
        lblR.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        meta.getChildren().addAll(lblN, lblR);
        HBox.setHgrow(meta, Priority.ALWAYS);

        row.getChildren().addAll(dot, meta);

        if (!isSelf) {
            Button btnMore = new Button("•••");
            btnMore.getStyleClass().add("btn-icon-subtle");
            Tooltip.install(btnMore, new Tooltip("Phân quyền hoặc xóa thành viên"));
            btnMore.setOnAction(e -> {
                ContextMenu cm = new ContextMenu();
                MenuItem miAdmin = new MenuItem("Gán quyền Admin nhóm");
                MenuItem miKick = new MenuItem("Xóa khỏi nhóm (Kick)");
                cm.getItems().addAll(miAdmin, miKick);
                cm.show(btnMore, javafx.geometry.Side.BOTTOM, 0, 0);
            });
            row.getChildren().add(btnMore);
        }

        return row;
    }

    private void toggleRightDrawer() {
        isDrawerOpen = !isDrawerOpen;
        rightDrawer.setVisible(isDrawerOpen);
        rightDrawer.setManaged(isDrawerOpen);
    }

    /* ==========================================================================
       CHẾ ĐỘ DANH BẠ & QUẢN LÝ BẠN BÈ (FRIENDS CENTER)
       ========================================================================== */
    private void buildFriendsCenterView(String category) {
        friendsContainer.getChildren().clear();
        friendsContainer.setPadding(new Insets(20));

        Label lblCatTitle = new Label("Trung tâm Quản lý Bạn bè & Danh bạ");
        lblCatTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        Button btnAll = new Button("Tất cả bạn bè (5)");
        Button btnOnline = new Button("Đang Online (3)");
        Button btnPending = new Button("Lời mời kết bạn (2)");
        Button btnAddFriend = new Button("➕ Thêm bạn mới");

        btnAll.getStyleClass().add("settings-tab-btn");
        btnOnline.getStyleClass().add("settings-tab-btn");
        btnPending.getStyleClass().add("settings-tab-btn");
        btnAddFriend.getStyleClass().add("settings-tab-btn");

        filterBar.getChildren().addAll(btnAll, btnOnline, btnPending, btnAddFriend);

        VBox listCard = new VBox(8);
        listCard.getStyleClass().add("friends-list-card");
        listCard.setPadding(new Insets(12));

        // Nạp dữ liệu bạn bè theo ma trận dummy-data-matrix.md
        if ("pending".equals(category)) {
            listCard.getChildren().addAll(
                createFriendRow("Đặng Quốc Bảo", "@quoc_bao", "Gửi lời mời 10 phút trước", false, true),
                createFriendRow("Trần Mỹ Duyên", "@my_duyen", "Gửi lời mời hôm qua", false, true)
            );
        } else {
            listCard.getChildren().addAll(
                createFriendRow("Lưu Chí Hải", "@chihai_2026", "Đang trực tuyến • Lưu Chí Hải (Admin)", true, false),
                createFriendRow("Nguyễn Lan Anh", "@lananh_it", "Đang trực tuyến • Thành viên nhóm đồ án", true, false),
                createFriendRow("Lê Hoàng Nam", "@nam_le", "Đang trực tuyến", true, false),
                createFriendRow("Phạm Đức Huy", "@duc_huy", "Ngoại tuyến (Offline)", false, false),
                createFriendRow("Vũ Thu Hà", "@thu_ha", "Ngoại tuyến (Offline)", false, false)
            );
        }

        friendsContainer.getChildren().addAll(lblCatTitle, filterBar, new Separator(), listCard);
        friendsScrollPane.setContent(friendsContainer);
        friendsScrollPane.setFitToWidth(true);
        friendsScrollPane.getStyleClass().add("transparent-scroll");

        mainViewPane.setCenter(friendsScrollPane);
        mainViewPane.setBottom(null);
    }

    private HBox createFriendRow(String name, String tag, String status, boolean isOnline, boolean isPending) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("friend-row-item");
        row.setPadding(new Insets(8, 12, 8, 12));

        Circle avt = new Circle(18, isOnline ? Color.web("#23A55A") : Color.web("#4E6BF2"));
        VBox meta = new VBox(2);
        Label lblN = new Label(name + " (" + tag + ")");
        lblN.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblS = new Label(status);
        lblS.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted;");
        meta.getChildren().addAll(lblN, lblS);
        HBox.setHgrow(meta, Priority.ALWAYS);

        row.getChildren().addAll(avt, meta);

        if (isPending) {
            Button btnAccept = new Button("Đồng ý ✓");
            btnAccept.getStyleClass().add("btn-primary");
            Button btnReject = new Button("Từ chối ✕");
            btnReject.getStyleClass().add("btn-secondary");
            row.getChildren().addAll(btnAccept, btnReject);
        } else {
            Button btnChat = new Button("💬 Nhắn tin");
            btnChat.getStyleClass().add("btn-secondary");
            btnChat.setOnAction(e -> {
                switchMode(true);
                loadConversation("user_chihai");
            });

            Button btnUnfriend = new Button("Hủy kết bạn");
            btnUnfriend.getStyleClass().add("btn-icon-subtle");

            Button btnBlock = new Button("⛔ Chặn");
            btnBlock.getStyleClass().add("btn-danger-subtle");
            btnBlock.setOnAction(e -> {
                Alert b = new Alert(Alert.AlertType.CONFIRMATION);
                b.initOwner(primaryStage);
                b.setTitle("Xác nhận chặn người dùng");
                b.setHeaderText("Chặn " + name + " (Mục 6.c)");
                b.setContentText("Sau khi chặn, người này sẽ không thể tìm thấy bạn hoặc gửi tin nhắn cho bạn.");
                b.showAndWait();
            });

            row.getChildren().addAll(btnChat, btnUnfriend, btnBlock);
        }

        return row;
    }
}
