package user;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

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
    private final Button btnNavCloud = new Button("📁");
    private final Button btnNavSettings = new Button("⚙");
    private final Button btnNavLogout = new Button("🚪");
    private boolean isChatMode = true;

    // Cột 2: Sub-Sidebar
    private final VBox subSidebar = new VBox(12);
    private final Label lblSidebarTitle = new Label("Đoạn chat");
    private final TextField txtSearch = new TextField();
    private final Button btnClearSearch = new Button("✕");
    private final Button btnOpenGlobalDialog = new Button("🌐");
    private final Button btnNewGroup = new Button("➕");
    private final VBox conversationListBox = new VBox(4);
    private final VBox friendsCategoryBox = new VBox(4);
    private final ScrollPane conversationScrollPane = new ScrollPane();
    private String currentFriendsCat = "online";
    private String currentChatFilter = "all";
    private final HBox pillFilterBar = new HBox(6);
    private final Button pillAll = new Button("Tất cả");
    private final Button pillUnread = new Button("Chưa đọc");
    private final Button pillGroups = new Button("Nhóm");

    // Cột 3: Main View
    private final BorderPane mainViewPane = new BorderPane();
    private ChatConversationView chatConversationView;
    private EmptyChatWelcomeView emptyWelcomeView;

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

        // Lắng nghe phím tắt toàn cục Ctrl+Shift+F hoặc Ctrl+K (Mục 11.g)
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.F) {
                        openGlobalSearchDialog(txtSearch.getText());
                    } else if (e.isControlDown() && e.getCode() == KeyCode.K) {
                        openGlobalSearchDialog(txtSearch.getText());
                    } else if (e.isControlDown() && e.getCode() == KeyCode.F) {
                        txtSearch.requestFocus();
                        txtSearch.selectAll();
                    }
                });
            }
        });
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
        btnNavCloud.getStyleClass().add("nav-item-btn");
        btnNavSettings.getStyleClass().add("nav-item-btn");
        btnNavLogout.getStyleClass().add("nav-item-btn");

        JavaFXTheme.applyIconButton(btnNavChat);
        JavaFXTheme.applyIconButton(btnNavFriends);
        JavaFXTheme.applyIconButton(btnNavCloud);
        JavaFXTheme.applyIconButton(btnNavSettings);
        JavaFXTheme.applyIconButton(btnNavLogout);

        Tooltip.install(btnNavChat, new Tooltip("Đoạn chat & Nhóm"));
        Tooltip.install(btnNavFriends, new Tooltip("Danh bạ & Bạn bè"));
        Tooltip.install(btnNavCloud, new Tooltip("Truyền File & Tài liệu (Zalo Cloud Style)"));
        Tooltip.install(btnNavSettings, new Tooltip("Cài đặt hệ thống"));
        Tooltip.install(btnNavLogout, new Tooltip("Đăng xuất tài khoản"));

        btnNavChat.setOnAction(e -> switchMode(true));
        btnNavFriends.setOnAction(e -> switchMode(false));
        btnNavCloud.setOnAction(e -> {
            switchMode(true);
            loadConversation("group_project");
            JavaFXTheme.showInfoAlert(primaryStage, "Truyền File", "Cloud cá nhân & Truyền file", "Tính năng lưu trữ và đồng bộ tệp tin đám mây giữa các thiết bị.");
        });
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
            btnNavCloud,
            spacer,
            btnNavSettings,
            btnNavLogout
        );
    }

    private void switchMode(boolean chatMode) {
        this.isChatMode = chatMode;
        txtSearch.clear();

        if (chatMode) {
            if (!btnNavChat.getStyleClass().contains("nav-item-btn-active")) {
                btnNavChat.getStyleClass().add("nav-item-btn-active");
            }
            btnNavFriends.getStyleClass().remove("nav-item-btn-active");

            lblSidebarTitle.setText("Đoạn chat");
            btnNewGroup.setVisible(true);
            btnNewGroup.setManaged(true);
            btnOpenGlobalDialog.setVisible(true);
            btnOpenGlobalDialog.setManaged(true);
            pillFilterBar.setVisible(true);
            pillFilterBar.setManaged(true);
            txtSearch.setPromptText("🔍 Tìm kiếm cuộc trò chuyện / tin nhắn...");

            buildConversationList();
            conversationScrollPane.setContent(conversationListBox);

            mainViewPane.setCenter(chatConversationView);
            mainViewPane.setBottom(null);
            rightDrawer.setVisible(isDrawerOpen);
            rightDrawer.setManaged(isDrawerOpen);
            loadConversation(currentChatId);
            JavaFXTheme.applyTabContentTransition(chatConversationView, false);
        } else {
            if (!btnNavFriends.getStyleClass().contains("nav-item-btn-active")) {
                btnNavFriends.getStyleClass().add("nav-item-btn-active");
            }
            btnNavChat.getStyleClass().remove("nav-item-btn-active");

            lblSidebarTitle.setText("Danh bạ");
            btnNewGroup.setVisible(false);
            btnNewGroup.setManaged(false);
            btnOpenGlobalDialog.setVisible(false);
            btnOpenGlobalDialog.setManaged(false);
            pillFilterBar.setVisible(false);
            pillFilterBar.setManaged(false);
            txtSearch.setPromptText("🔍 Tìm bạn bè theo tên...");

            buildFriendsSidebarList();
            conversationScrollPane.setContent(friendsCategoryBox);

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

        btnNewGroup.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnNewGroup, new Tooltip("Tạo nhóm hoặc thêm bạn mới (Quick Action FAB)"));
        JavaFXTheme.applyIconButton(btnNewGroup);

        ContextMenu popover = new ContextMenu();
        JavaFXTheme.applyContextMenuTheme(popover);

        MenuItem mNewGroup = new MenuItem("👥 Tạo nhóm trò chuyện mới (Mục 12.a)");
        mNewGroup.setOnAction(e -> {
            new GroupCreateDialog(primaryStage, result -> {
                conversationListBox.getChildren().add(0, createConversationItem(
                    "group_" + System.currentTimeMillis(), result.getGroupName(),
                    result.isEncrypted() ? "🔒 Nhóm mã hóa E2EE vừa tạo..." : "Nhóm mới tạo...",
                    "Vừa xong", result.isEncrypted(), 0, true, "#3F51B5", "GP"
                ));
                loadConversation("group_project");
            }).show();
        });

        MenuItem mAddFriend = new MenuItem("👤 Thêm bạn bè vào danh bạ (Mục 6.b)");
        mAddFriend.setOnAction(e -> {
            MemberAddDialog.show(primaryStage).ifPresent(u -> {
                JavaFXTheme.showInfoAlert(
                    primaryStage,
                    "Lời mời kết bạn",
                    "Đã gửi yêu cầu kết bạn (Mục 6.b)",
                    "Đã gửi lời mời kết bạn tới người dùng @" + u.toLowerCase().replace(" ", "_")
                );
            });
        });

        MenuItem mSecretChat = new MenuItem("🔒 Trò chuyện mã hóa E2EE (Mục 12.g)");
        mSecretChat.setOnAction(e -> loadConversation("group_project"));

        popover.getItems().addAll(mNewGroup, mAddFriend, new javafx.scene.control.SeparatorMenuItem(), mSecretChat);

        btnNewGroup.setOnAction(e -> {
            popover.show(btnNewGroup, javafx.geometry.Side.BOTTOM, 0, 4);
        });

        topBox.getChildren().addAll(lblSidebarTitle, sp, btnNewGroup);

        // Thanh Tìm kiếm Hội thoại & Tin nhắn Toàn cục (Mục 11.g)
        HBox searchContainer = new HBox(6);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.getStyleClass().add("sidebar-search-field");

        Label lblSearchIcon = new Label("🔍");
        lblSearchIcon.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");

        txtSearch.setPromptText("Tìm kiếm cuộc trò chuyện / tin nhắn...");
        txtSearch.setStyle("-fx-background-color: transparent; -fx-text-fill: -text-normal; -fx-padding: 0;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        btnClearSearch.getStyleClass().add("btn-icon-subtle");
        btnClearSearch.setStyle("-fx-font-size: 11px; -fx-padding: 2 4;");
        btnClearSearch.setVisible(false);
        btnClearSearch.setOnAction(e -> {
            txtSearch.clear();
            txtSearch.requestFocus();
        });

        btnOpenGlobalDialog.getStyleClass().add("btn-icon-subtle");
        btnOpenGlobalDialog.setStyle("-fx-font-size: 11px; -fx-padding: 2 4;");
        Tooltip.install(btnOpenGlobalDialog, new Tooltip("Mở cửa sổ tìm kiếm toàn cục nâng cao (Mục 11.g - Ctrl+Shift+F)"));
        btnOpenGlobalDialog.setOnAction(e -> openGlobalSearchDialog(txtSearch.getText()));

        searchContainer.getChildren().addAll(lblSearchIcon, txtSearch, btnClearSearch, btnOpenGlobalDialog);

        txtSearch.textProperty().addListener((obs, oldV, newV) -> {
            boolean hasText = newV != null && !newV.trim().isEmpty();
            btnClearSearch.setVisible(hasText);
            if (isChatMode) {
                if (hasText) {
                    renderSidebarSearchResults(newV.trim());
                } else {
                    buildConversationList();
                }
            }
        });

        // Hàng bộ lọc Pill kiểu Telegram (Tất cả / Chưa đọc / Nhóm)
        setupChatPills();

        conversationScrollPane.setFitToWidth(true);
        conversationScrollPane.getStyleClass().add("transparent-scroll");
        VBox.setVgrow(conversationScrollPane, Priority.ALWAYS);

        buildConversationList();
        conversationScrollPane.setContent(conversationListBox);

        subSidebar.getChildren().addAll(topBox, searchContainer, pillFilterBar, conversationScrollPane);
    }

    private void setupChatPills() {
        pillFilterBar.setAlignment(Pos.CENTER_LEFT);
        pillFilterBar.setPadding(new Insets(2, 0, 4, 0));

        configurePill(pillAll, "all");
        configurePill(pillUnread, "unread");
        configurePill(pillGroups, "groups");

        updateChatPillStyles();
        pillFilterBar.getChildren().addAll(pillAll, pillUnread, pillGroups);
    }

    private void configurePill(Button pill, String filterType) {
        pill.setOnAction(e -> {
            this.currentChatFilter = filterType;
            updateChatPillStyles();
            buildConversationList();
        });
        JavaFXTheme.applyTactileButton(pill);
    }

    private void updateChatPillStyles() {
        String activeStyle = "-fx-background-color: #2D313D; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-background-radius: 12px; -fx-padding: 3 10; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: -text-muted; -fx-font-size: 11.5px; -fx-background-radius: 12px; -fx-padding: 3 10; -fx-cursor: hand;";

        pillAll.setStyle("all".equals(currentChatFilter) ? activeStyle : inactiveStyle);
        pillUnread.setStyle("unread".equals(currentChatFilter) ? activeStyle : inactiveStyle);
        pillGroups.setStyle("groups".equals(currentChatFilter) ? activeStyle : inactiveStyle);
    }

    private void buildConversationList() {
        conversationListBox.getChildren().clear();

        int displayedCount = 0;
        for (ChatMockRepository.ConversationMeta conv : ChatMockRepository.getAllConversations()) {
            if ("unread".equals(currentChatFilter) && conv.getUnreadCount() <= 0) {
                continue;
            }
            if ("groups".equals(currentChatFilter) && !conv.isGroup()) {
                continue;
            }
            displayedCount++;
            conversationListBox.getChildren().add(createConversationItem(
                conv.getId(),
                conv.getTitle(),
                conv.getSnippet(),
                conv.getLastTime(),
                conv.isEncrypted(),
                conv.getUnreadCount(),
                conv.isOnline(),
                conv.getAvatarColor(),
                conv.getInitials()
            ));
        }

        if (displayedCount == 0) {
            VBox emptyBox = new VBox(6);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(24, 8, 24, 8));
            Label lblEmpty = new Label("Không có cuộc trò chuyện nào");
            lblEmpty.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
            emptyBox.getChildren().add(lblEmpty);
            conversationListBox.getChildren().add(emptyBox);
        }
    }

    private void renderSidebarSearchResults(String query) {
        conversationListBox.getChildren().clear();

        List<ChatMockRepository.ConversationMeta> chatMatches = ChatMockRepository.searchConversations(query);
        List<ChatMockRepository.GlobalMessageMatch> messageMatches = ChatMockRepository.searchAllMessages(query);

        if (!chatMatches.isEmpty()) {
            Label lblSection = new Label("CUỘC TRÒ CHUYỆN (" + chatMatches.size() + ")");
            lblSection.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-padding: 4 6 2 6;");
            conversationListBox.getChildren().add(lblSection);

            for (ChatMockRepository.ConversationMeta conv : chatMatches) {
                conversationListBox.getChildren().add(createConversationItem(
                    conv.getId(), conv.getTitle(), conv.getSnippet(), conv.getLastTime(),
                    conv.isEncrypted(), conv.getUnreadCount(), conv.isOnline(),
                    conv.getAvatarColor(), conv.getInitials()
                ));
            }
        }

        Label lblMsgSection = new Label("TIN NHẮN TOÀN CỤC — MỤC 11.G (" + messageMatches.size() + ")");
        lblMsgSection.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #F0B232; -fx-padding: 8 6 2 6;");
        conversationListBox.getChildren().add(lblMsgSection);

        if (messageMatches.isEmpty()) {
            VBox emptyBox = new VBox(4);
            emptyBox.setPadding(new Insets(12, 8, 12, 8));
            emptyBox.setAlignment(Pos.CENTER);
            Label lblNone = new Label("Không có tin nhắn nào khớp từ khóa");
            lblNone.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
            emptyBox.getChildren().add(lblNone);
            conversationListBox.getChildren().add(emptyBox);
        } else {
            for (ChatMockRepository.GlobalMessageMatch match : messageMatches) {
                conversationListBox.getChildren().add(createSidebarMessageResultItem(match, query));
            }
        }
    }

    private VBox createSidebarMessageResultItem(ChatMockRepository.GlobalMessageMatch match, String query) {
        VBox item = new VBox(4);
        item.getStyleClass().add("conversation-item");
        item.setPadding(new Insets(8, 10, 8, 10));

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(3.5, Color.web(match.getAvatarColor()));
        Label lblChat = new Label(match.getChatTitle());
        lblChat.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #A0C0FF;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblTime = new Label(match.getMessage().getTimestamp());
        lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: -text-muted;");

        topRow.getChildren().addAll(dot, lblChat, sp, lblTime);

        HBox snippetRow = new HBox(4);
        snippetRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSender = new Label((match.getMessage().isOutgoing() ? "Bạn: " : (match.getMessage().getSenderName() + ": ")));
        lblSender.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        TextFlow textFlow = createSnippetFlow(match.getMessage().getContent(), query);
        HBox.setHgrow(textFlow, Priority.ALWAYS);

        snippetRow.getChildren().addAll(lblSender, textFlow);
        item.getChildren().addAll(topRow, snippetRow);

        item.setOnMouseClicked(e -> {
            this.currentChatId = match.getChatId();
            loadConversation(match.getChatId());
            chatConversationView.highlightAndScrollToMessage(match.getMessage().getId());
        });

        return item;
    }

    private TextFlow createSnippetFlow(String fullText, String keyword) {
        TextFlow flow = new TextFlow();
        if (fullText == null || keyword == null || keyword.isEmpty()) {
            Text t = new Text(fullText != null ? fullText : "");
            t.setStyle("-fx-fill: #8E929B; -fx-font-size: 11px;");
            flow.getChildren().add(t);
            return flow;
        }

        String lowerFull = fullText.toLowerCase();
        String lowerKey = keyword.toLowerCase();

        int start = 0;
        int idx = lowerFull.indexOf(lowerKey, start);

        while (idx != -1) {
            if (idx > start) {
                Text normal = new Text(fullText.substring(start, idx));
                normal.setStyle("-fx-fill: #8E929B; -fx-font-size: 11px;");
                flow.getChildren().add(normal);
            }

            Text highlighted = new Text(fullText.substring(idx, idx + keyword.length()));
            Label badge = new Label(highlighted.getText());
            badge.setStyle("-fx-background-color: #F0B232; -fx-text-fill: #0F1013; -fx-font-weight: bold; -fx-padding: 0 2; -fx-background-radius: 2px; -fx-font-size: 11px;");
            flow.getChildren().add(badge);

            start = idx + keyword.length();
            idx = lowerFull.indexOf(lowerKey, start);
        }

        if (start < fullText.length()) {
            Text tail = new Text(fullText.substring(start));
            tail.setStyle("-fx-fill: #8E929B; -fx-font-size: 11px;");
            flow.getChildren().add(tail);
        }

        return flow;
    }

    public void openGlobalSearchDialog(String initialQuery) {
        GlobalSearchDialog dialog = new GlobalSearchDialog(primaryStage, initialQuery, (chatId, messageId) -> {
            switchMode(true);
            loadConversation(chatId);
            if (messageId != null) {
                chatConversationView.highlightAndScrollToMessage(messageId);
            }
        });
        dialog.showAndWait();
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

        // Avatar Box (42px tiêu chuẩn)
        StackPane avtBox = new StackPane();
        Circle c = new Circle(21, Color.web(avatarColor));
        Label lblI = new Label(initials);
        lblI.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        avtBox.getChildren().addAll(c, lblI);

        if (isOnline) {
            Circle dot = new Circle(4.5, Color.web("#23A55A"));
            dot.setStroke(Color.web("#1B1D22"));
            dot.setStrokeWidth(2);
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
        friendsCategoryBox.getChildren().clear();

        friendsCategoryBox.getChildren().addAll(
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
        if (catId.equals(currentFriendsCat)) {
            item.getStyleClass().add("conversation-item-selected");
        }

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
            this.currentFriendsCat = catId;
            buildFriendsSidebarList();
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
        emptyWelcomeView = new EmptyChatWelcomeView(() -> loadConversation("group_project"));
        chatConversationView = new ChatConversationView(primaryStage, this::handleReportSpam, this::toggleRightDrawer);
        chatConversationView.setOnTriggerGlobalSearch(this::openGlobalSearchDialog);
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
        if (chatId == null || chatId.isEmpty()) {
            mainViewPane.setCenter(emptyWelcomeView);
            rightDrawer.setVisible(false);
            rightDrawer.setManaged(false);
            return;
        }

        if (mainViewPane.getCenter() != chatConversationView) {
            mainViewPane.setCenter(chatConversationView);
        }
        if (chatConversationView != null) {
            chatConversationView.loadConversation(chatId);
        }
        rightDrawer.setVisible(isDrawerOpen);
        rightDrawer.setManaged(isDrawerOpen);
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
                    MemberAddDialog.show(primaryStage).ifPresent(u -> {
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
