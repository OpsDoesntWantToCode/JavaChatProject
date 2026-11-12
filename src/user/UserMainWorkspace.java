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
    private final Button btnNavSettings = new Button("⚙");
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
    private ChatConversationView chatConversationView;

    // Cột 4: Right Drawer
    private final VBox rightDrawer = new VBox();
    private GroupManagementDrawer groupManagementDrawer;
    private boolean isDrawerOpen = true;

    // View Quản lý Bạn bè chuyên dụng (Task U4, U5, U6, U7)
    private FriendsManagementView friendsManagementView;

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

        JavaFXTheme.applyIconButton(btnNavChat);
        JavaFXTheme.applyIconButton(btnNavFriends);
        JavaFXTheme.applyIconButton(btnNavSettings);
        JavaFXTheme.applyIconButton(btnNavLogout);

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
            mainViewPane.setCenter(chatConversationView);
            mainViewPane.setBottom(null);
            rightDrawer.setVisible(isDrawerOpen);
            rightDrawer.setManaged(isDrawerOpen);
            loadConversation(currentChatId);
            JavaFXTheme.applyTabContentTransition(chatConversationView, false);
        } else {
            btnNavFriends.getStyleClass().add("nav-item-btn-active");
            btnNavChat.getStyleClass().remove("nav-item-btn-active");
            lblSidebarTitle.setText("Danh bạ");
            txtSearch.setPromptText("🔍 Tìm bạn bè theo tên...");
            buildFriendsSidebarList();
            if (friendsManagementView == null) {
                friendsManagementView = new FriendsManagementView(primaryStage, userId -> {
                    switchMode(true);
                    loadConversation(userId);
                }, userId -> {
                    JavaFXTheme.showInfoAlert(
                        primaryStage,
                        "Tạo nhóm mới",
                        "Tạo nhóm trò chuyện",
                        "Tính năng tạo nhóm chat với thành viên này sẽ kết nối Socket ở mốc 80%."
                    );
                });
            }
            mainViewPane.setCenter(friendsManagementView);
            mainViewPane.setBottom(null);
            rightDrawer.setVisible(false);
            rightDrawer.setManaged(false);
            JavaFXTheme.applyTabContentTransition(friendsManagementView, true);
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
        JavaFXTheme.applyIconButton(btnNewGroup);
        btnNewGroup.setOnAction(e -> {
            new GroupCreateDialog(primaryStage, result -> {
                conversationListBox.getChildren().add(0, createConversationItem(
                    "group_" + System.currentTimeMillis(), result.getGroupName(),
                    result.isEncrypted() ? "🔒 Nhóm mã hóa E2EE vừa tạo..." : "Nhóm mới tạo...",
                    "Vừa xong", result.isEncrypted(), 0, true, "#3F51B5", "GP"
                ));
                loadConversation("group_project");
            }).show();
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

        item.setOnMouseClicked(e -> {
            if (friendsManagementView != null) {
                friendsManagementView.switchTab(catId);
            }
        });
        return item;
    }

    /* ==========================================================================
       CỘT 3: MAIN VIEW (Flexible)
       ========================================================================== */
    private void buildMainView() {
        chatConversationView = new ChatConversationView(primaryStage, this::handleReportSpam, this::toggleRightDrawer);
        mainViewPane.setCenter(chatConversationView);
    }

    private void handleReportSpam() {
        String targetName = "Lưu Chí Hải";
        String targetUsername = "@chihai_2026";
        if ("group_project".equals(currentChatId)) {
            targetName = "Nhóm Đồ Án Java 2026";
            targetUsername = "@group_project";
        }
        new SpamReportDialog(primaryStage, targetName, targetUsername, reason -> {
            // Callback đã hiển thị thông báo ghi nhận
        }).show();
    }

    private void loadConversation(String chatId) {
        this.currentChatId = chatId;
        if (chatConversationView != null) {
            chatConversationView.loadConversation(chatId);
        }
        updateRightDrawerContent();
    }

    /* ==========================================================================
       CỘT 4: RIGHT DRAWER (280px)
       ========================================================================== */
    private void buildRightDrawer() {
        rightDrawer.setPrefWidth(280);
        rightDrawer.setMinWidth(280);
        rightDrawer.setMaxWidth(280);
        rightDrawer.getStyleClass().add("right-drawer");

        updateRightDrawerContent();
    }

    private void updateRightDrawerContent() {
        if (groupManagementDrawer == null) {
            groupManagementDrawer = new GroupManagementDrawer(
                primaryStage,
                this::toggleRightDrawer,
                newName -> {
                    if (chatConversationView != null) {
                        chatConversationView.loadConversation(currentChatId);
                    }
                },
                () -> {
                    JavaFXTheme.showInputDialog(
                        primaryStage,
                        "Thêm thành viên mới",
                        "Thêm bạn bè vào nhóm (Mục 12.c)",
                        "Nhập username hoặc họ tên bạn bè:",
                        ""
                    ).ifPresent(u -> {
                        String trimmed = u.trim();
                        if (!trimmed.isEmpty()) {
                            groupManagementDrawer.addMember("u_" + System.currentTimeMillis(), trimmed, "@" + trimmed.toLowerCase().replace(" ", "_"));
                        }
                    });
                },
                () -> {
                    if (chatConversationView != null) {
                        chatConversationView.clearHistory();
                    }
                }
            );
        }

        if (currentChatId != null && currentChatId.startsWith("group")) {
            groupManagementDrawer.setGroupName("group_project".equals(currentChatId) ? "Nhóm Đồ Án Java 2026" : "Nhóm Thể Thao Cuối Tuần");
        }

        rightDrawer.getChildren().setAll(groupManagementDrawer);
    }

    private void toggleRightDrawer() {
        isDrawerOpen = !isDrawerOpen;
        if (isDrawerOpen) {
            JavaFXTheme.animateDrawerOpen(rightDrawer, 280);
        } else {
            JavaFXTheme.animateDrawerClose(rightDrawer, 280, null);
        }
    }
}
