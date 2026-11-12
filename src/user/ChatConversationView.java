package user;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Khung Trò chuyện Trực tiếp & Lịch sử Tin nhắn (Task U9, U10, U11 — ChatConversationView)
 * Triển khai theo chuẩn Vòng lặp 1 & tài liệu SSOT docs/ui-spec/chat-messaging.md, ai-assistant.md:
 * - Kiến trúc ảo hóa VirtualFlow: Dùng ListView<MessageItem> thay cho ScrollPane giữ vững 60fps
 * - Chuẩn thẩm mỹ Telegram Desktop & Zalo:
 *   + Khung chat tràn viền tự nhiên (De-boxing), không viền sáng bao quanh lịch sử
 *   + Thông báo E2EE thu gọn thành pill badge tinh tế chính giữa
 *   + Dải màu Avatar băm ngẫu nhiên theo tên người dùng phân biệt người gửi tức thì
 *   + Timestamp (10px, 65% opacity) đặt gọn góc dưới bên phải bên trong bong bóng chat
 *   + Thanh nhập tin nhắn tích hợp Capsule (gắn nút 📎, ✨, ô nhập mở rộng và nút gửi ➤ bên trong)
 *   + Dải gợi ý AI thu gọn, bật/tắt linh hoạt qua nút ✨ hoặc gõ lệnh /ai
 *   + Bong bóng Trợ lý AI tông tím than trầm (#231F32) viền trái sang trọng
 */
public class ChatConversationView extends BorderPane {

    // =========================================================================
    // HELPER: MÀU AVATAR & CHỮ CÁI VIẾT TẮT
    // =========================================================================
    public static String getUserAvatarColor(String name) {
        if (name == null || name.trim().isEmpty()) return "#4E6BF2";
        if (name.contains("Minh")) return "#3B82F6";  // Tuấn Minh: Sapphire Blue
        if (name.contains("Hải")) return "#10B981";  // Chí Hải: Emerald Green
        if (name.contains("Anh")) return "#EC4899";  // Lan Anh: Rose Pink
        if (name.contains("Nam")) return "#8B5CF6";  // Hoàng Nam: Purple
        if (name.contains("Huy")) return "#F59E0B";  // Đức Huy: Amber Orange
        if (name.contains("AI") || name.contains("Bot")) return "#8B5CF6";

        String[] palette = { "#3B82F6", "#10B981", "#EC4899", "#8B5CF6", "#F59E0B", "#06B6D4", "#6366F1" };
        int idx = Math.abs(name.hashCode()) % palette.length;
        return palette[idx];
    }

    public static String getUserInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // =========================================================================
    // DỮ LIỆU TIN NHẮN (DATA MODEL)
    // =========================================================================
    public static class MessageItem {
        private final String id;
        private final String senderName;
        private final String timestamp;
        private final String content;
        private final boolean outgoing;
        private final boolean ai;

        // Trạng thái đa chọn xóa nhiều tin (Task U10)
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        // Cường độ chớp sáng màu vàng hổ phách khi tìm kiếm (Task U9)
        private final DoubleProperty highlightIntensity = new SimpleDoubleProperty(0.0);

        public MessageItem(String id, String senderName, String timestamp, String content, boolean outgoing, boolean ai) {
            this.id = id;
            this.senderName = senderName;
            this.timestamp = timestamp;
            this.content = content;
            this.outgoing = outgoing;
            this.ai = ai;
        }

        public String getId() { return id; }
        public String getSenderName() { return senderName; }
        public String getTimestamp() { return timestamp; }
        public String getContent() { return content; }
        public boolean isOutgoing() { return outgoing; }
        public boolean isAi() { return ai; }

        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }
        public BooleanProperty selectedProperty() { return selected; }

        public double getHighlightIntensity() { return highlightIntensity.get(); }
        public void setHighlightIntensity(double value) { highlightIntensity.set(value); }
        public DoubleProperty highlightIntensityProperty() { return highlightIntensity; }
    }

    // =========================================================================
    // THÀNH PHẦN GIAO DIỆN CHÍNH
    // =========================================================================
    private Stage ownerStage;
    private Runnable onReportSpam;
    private Runnable onToggleDrawer;
    private java.util.function.Consumer<String> onTriggerGlobalSearch;

    private String currentChatId = "group_project";

    // 1. Header Khung Chat
    private final HBox chatHeader = new HBox(12);
    private final StackPane headerAvatarStack = new StackPane();
    private final Circle headerAvatarCircle = new Circle(19);
    private final Label lblHeaderAvatarText = new Label("👥");
    private final Circle headerStatusDot = new Circle(4.5);
    private final Label lblChatTitle = new Label();
    private final Label lblChatSubtitle = new Label();

    // 2. Thanh Tìm kiếm Nội bộ (Task U9)
    private final HBox searchBar = new HBox(8);
    private final TextField txtSearchBox = new TextField();
    private final Label lblSearchCount = new Label("0/0");
    private final Button btnPrevMatch = new Button("▲");
    private final Button btnNextMatch = new Button("▼");
    private final Button btnCloseSearch = new Button("✕");
    private final List<Integer> searchMatchIndices = new ArrayList<>();
    private int currentSearchPointer = -1;

    // 3. Banner Mã hóa Đầu cuối E2EE (Dạng Pill tinh tế, không bao viền gắt toàn màn hình)
    private final HBox e2eeBannerContainer = new HBox();
    private final HBox e2eePill = new HBox(6);

    // 4. Danh sách Tin nhắn Ảo hóa (VirtualFlow ListView)
    private final ObservableList<MessageItem> messagesList = FXCollections.observableArrayList();
    private final ListView<MessageItem> messageListView = new ListView<>(messagesList);

    // 5. Chế độ Chọn xóa nhiều tin nhắn (Task U10)
    private final BooleanProperty multiSelectMode = new SimpleBooleanProperty(false);
    private final HBox floatingActionBar = new HBox(12);
    private final Label lblSelectedCount = new Label("Đã chọn: 0");
    private final Button btnDeleteSelected = new Button("🗑 Xóa (0)");
    private final Button btnCancelMulti = new Button("✕ Hủy");

    // 6. Thanh Soạn thảo & Trợ lý AI (Task U11 — Integrated Capsule Bar)
    private final VBox bottomBar = new VBox(6);
    private final HBox smartRepliesBar = new HBox(8);
    private final HBox inputCapsule = new HBox(6);
    private final Button btnAttach = new Button("📎");
    private final Button btnAiToggle = new Button("✨");
    private final TextField txtMessageInput = new TextField();
    private final Button btnSend = new Button("➤");
    private boolean isAiChipsManuallyOpened = false;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    public ChatConversationView() {
        this(null, null, null);
    }

    public ChatConversationView(Stage ownerStage, Runnable onReportSpam, Runnable onToggleDrawer) {
        this.ownerStage = ownerStage;
        this.onReportSpam = onReportSpam;
        this.onToggleDrawer = onToggleDrawer;

        getStyleClass().add("main-chat-pane");

        buildHeader();
        buildSearchBar();
        buildCenterChatArea();
        buildBottomInputBar();

        // Lắp ráp bố cục BorderPane
        VBox topContainer = new VBox(chatHeader, searchBar);
        setTop(topContainer);
        setBottom(bottomBar);

        // Nạp kịch bản mẫu mặc định: Nhóm Đồ Án Java 2026 có mã hóa E2EE
        loadConversation("group_project");
    }

    // =========================================================================
    // 1. HEADER KHUNG CHAT (Top Bar — Chuẩn Single Border Source)
    // =========================================================================
    private void buildHeader() {
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setPadding(new Insets(12, 20, 12, 20));
        chatHeader.getStyleClass().add("chat-header");

        // Avatar + Status Dot
        headerAvatarCircle.setFill(Color.web("#262932"));
        lblHeaderAvatarText.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-normal;");
        headerStatusDot.setFill(Color.web("#23A55A"));
        StackPane.setAlignment(headerStatusDot, Pos.BOTTOM_RIGHT);
        headerAvatarStack.getChildren().addAll(headerAvatarCircle, lblHeaderAvatarText, headerStatusDot);

        // Title + Subtitle
        VBox titleBox = new VBox(2);
        lblChatTitle.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        lblChatSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        titleBox.getChildren().addAll(lblChatTitle, lblChatSubtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Các nút hành động trên Header (sử dụng SVG Vector chống lỗi font/tofu)
        Button btnToggleMulti = new Button();
        btnToggleMulti.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_CHECKLIST, 0.75));
        btnToggleMulti.getStyleClass().add("btn-icon-subtle");
        btnToggleMulti.setPrefSize(34, 34);
        btnToggleMulti.setMinSize(34, 34);
        btnToggleMulti.setMaxSize(34, 34);
        Tooltip.install(btnToggleMulti, new Tooltip("Chọn nhiều tin nhắn để xóa (Mục 11.e)"));
        btnToggleMulti.setOnAction(e -> setMultiSelectMode(!isMultiSelectMode()));
        JavaFXTheme.applyIconButton(btnToggleMulti);

        Button btnSearch = new Button();
        btnSearch.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_SEARCH, 0.75));
        btnSearch.getStyleClass().add("btn-icon-subtle");
        btnSearch.setPrefSize(34, 34);
        btnSearch.setMinSize(34, 34);
        btnSearch.setMaxSize(34, 34);
        Tooltip.install(btnSearch, new Tooltip("Tìm kiếm trong cuộc trò chuyện (Mục 11.f)"));
        btnSearch.setOnAction(e -> toggleSearchBar());
        JavaFXTheme.applyIconButton(btnSearch);

        Button btnReportSpam = new Button();
        btnReportSpam.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_REPORT, 0.75));
        btnReportSpam.getStyleClass().add("btn-icon-subtle");
        btnReportSpam.setPrefSize(34, 34);
        btnReportSpam.setMinSize(34, 34);
        btnReportSpam.setMaxSize(34, 34);
        Tooltip.install(btnReportSpam, new Tooltip("Báo cáo vi phạm / Spam (Mục 10)"));
        btnReportSpam.setOnAction(e -> handleReportSpam());
        JavaFXTheme.applyIconButton(btnReportSpam);

        Button btnToggleDrawer = new Button();
        btnToggleDrawer.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_INFO, 0.75));
        btnToggleDrawer.getStyleClass().add("btn-icon-subtle");
        btnToggleDrawer.setPrefSize(34, 34);
        btnToggleDrawer.setMinSize(34, 34);
        btnToggleDrawer.setMaxSize(34, 34);
        Tooltip.install(btnToggleDrawer, new Tooltip("Thông tin chi tiết hội thoại (Cột 4)"));
        btnToggleDrawer.setOnAction(e -> {
            if (onToggleDrawer != null) {
                onToggleDrawer.run();
            }
        });
        JavaFXTheme.applyIconButton(btnToggleDrawer);

        HBox headerActions = new HBox(6, btnToggleMulti, btnSearch, btnReportSpam, btnToggleDrawer);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        chatHeader.getChildren().addAll(headerAvatarStack, titleBox, headerActions);
    }

    // =========================================================================
    // 2. THANH TÌM KIẾM NỘI BỘ (Task U9 — Internal Search Bar)
    // =========================================================================
    private void buildSearchBar() {
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(8, 20, 8, 20));
        searchBar.setStyle("-fx-background-color: #181A1F; -fx-border-color: transparent transparent #2A2D35 transparent; -fx-border-width: 0 0 1px 0;");
        searchBar.setVisible(false);
        searchBar.setManaged(false);

        Label lblIcon = new Label("🔍");
        lblIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: -text-muted;");

        txtSearchBox.setPromptText("Tìm kiếm tin nhắn trong hội thoại này... (Nhấn Enter để chuyển kết quả)");
        txtSearchBox.getStyleClass().add("text-field");
        HBox.setHgrow(txtSearchBox, Priority.ALWAYS);

        txtSearchBox.textProperty().addListener((obs, oldV, newV) -> performSearch(newV));
        txtSearchBox.setOnAction(e -> navigateNextMatch());
        txtSearchBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                closeSearchBar();
            }
        });

        lblSearchCount.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-padding: 0 4px;");

        btnPrevMatch.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnPrevMatch, new Tooltip("Kết quả trước đó (Shift+Enter)"));
        btnPrevMatch.setOnAction(e -> navigatePrevMatch());

        btnNextMatch.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnNextMatch, new Tooltip("Kết quả tiếp theo (Enter)"));
        btnNextMatch.setOnAction(e -> navigateNextMatch());

        Button btnGlobalSearch = new Button("🌐 Tìm tất cả (11.g)");
        btnGlobalSearch.getStyleClass().add("btn-icon-subtle");
        btnGlobalSearch.setStyle("-fx-border-color: #2A2D35; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-padding: 3 8;");
        Tooltip.install(btnGlobalSearch, new Tooltip("Tìm kiếm chuỗi này trong tất cả cuộc trò chuyện (Mục 11.g)"));
        btnGlobalSearch.setOnAction(e -> {
            if (onTriggerGlobalSearch != null) {
                onTriggerGlobalSearch.accept(txtSearchBox.getText());
            }
        });

        btnCloseSearch.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnCloseSearch, new Tooltip("Đóng tìm kiếm (Esc)"));
        btnCloseSearch.setOnAction(e -> closeSearchBar());

        searchBar.getChildren().addAll(lblIcon, txtSearchBox, lblSearchCount, btnPrevMatch, btnNextMatch, btnGlobalSearch, btnCloseSearch);
    }

    public void toggleSearchBar() {
        boolean nextState = !searchBar.isVisible();
        if (nextState) {
            openSearchBar();
        } else {
            closeSearchBar();
        }
    }

    public void openSearchBar() {
        searchBar.setVisible(true);
        searchBar.setManaged(true);
        txtSearchBox.requestFocus();
        txtSearchBox.selectAll();
        performSearch(txtSearchBox.getText());
    }

    public void closeSearchBar() {
        searchBar.setVisible(false);
        searchBar.setManaged(false);
        searchMatchIndices.clear();
        currentSearchPointer = -1;
        lblSearchCount.setText("0/0");
    }

    private void performSearch(String keyword) {
        searchMatchIndices.clear();
        currentSearchPointer = -1;

        if (keyword == null || keyword.trim().isEmpty()) {
            lblSearchCount.setText("0/0");
            return;
        }

        String query = keyword.trim().toLowerCase();
        for (int i = 0; i < messagesList.size(); i++) {
            MessageItem item = messagesList.get(i);
            if (item.getContent().toLowerCase().contains(query) ||
                item.getSenderName().toLowerCase().contains(query)) {
                searchMatchIndices.add(i);
            }
        }

        if (!searchMatchIndices.isEmpty()) {
            currentSearchPointer = 0;
            updateMatchDisplayAndNavigate();
        } else {
            lblSearchCount.setText("0/0");
        }
    }

    private void navigateNextMatch() {
        if (searchMatchIndices.isEmpty()) return;
        currentSearchPointer = (currentSearchPointer + 1) % searchMatchIndices.size();
        updateMatchDisplayAndNavigate();
    }

    private void navigatePrevMatch() {
        if (searchMatchIndices.isEmpty()) return;
        currentSearchPointer = (currentSearchPointer - 1 + searchMatchIndices.size()) % searchMatchIndices.size();
        updateMatchDisplayAndNavigate();
    }

    private void updateMatchDisplayAndNavigate() {
        lblSearchCount.setText((currentSearchPointer + 1) + "/" + searchMatchIndices.size());
        int targetIndex = searchMatchIndices.get(currentSearchPointer);
        highlightMessage(targetIndex);
    }

    public void highlightMessage(int index) {
        if (index < 0 || index >= messagesList.size()) return;

        messageListView.scrollTo(index);
        messageListView.getSelectionModel().select(index);

        MessageItem targetItem = messagesList.get(index);
        targetItem.setHighlightIntensity(1.0);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(targetItem.highlightIntensityProperty(), 1.0)),
            new KeyFrame(Duration.millis(1500), new KeyValue(targetItem.highlightIntensityProperty(), 0.0))
        );
        timeline.play();
    }

    // =========================================================================
    // 3. KHU VỰC CHAT TRUNG TÂM (De-boxing & Pill E2EE Tinh tế)
    // =========================================================================
    private void buildCenterChatArea() {
        // Banner E2EE Messenger/Telegram style: Dạng Pill nhỏ gọn căn giữa, không viền gắt toàn màn hình
        e2eeBannerContainer.setAlignment(Pos.CENTER);
        e2eeBannerContainer.setPadding(new Insets(8, 0, 4, 0));
        e2eeBannerContainer.setStyle("-fx-background-color: transparent;");

        e2eePill.setAlignment(Pos.CENTER);
        e2eePill.setPadding(new Insets(3, 14, 3, 14));
        e2eePill.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-width: 1px;"
        );

        Label lblLockIcon = new Label("🔒");
        lblLockIcon.setStyle("-fx-font-size: 11px;");

        Label lblBanner = new Label("Tin nhắn được mã hóa đầu cuối AES-GCM 256-bit");
        lblBanner.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8E929B;");
        e2eePill.getChildren().addAll(lblLockIcon, lblBanner);
        e2eeBannerContainer.getChildren().setAll(e2eePill);

        // Date Separator Pill kiểu Telegram / Zalo (Ảnh 3, 6, 7)
        HBox datePill = new HBox(8);
        datePill.setAlignment(Pos.CENTER);
        datePill.setPadding(new Insets(2, 0, 4, 0));
        Label lblDate = new Label("Hôm nay, 12/11/2026");
        lblDate.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-width: 1px; " +
            "-fx-padding: 2px 12px; " +
            "-fx-font-size: 11px; " +
            "-fx-text-fill: #8E929B;"
        );
        datePill.getChildren().add(lblDate);

        // Cấu hình VirtualFlow ListView: Tràn viền tự nhiên, không đóng hộp
        messageListView.getStyleClass().add("transparent-scroll");
        messageListView.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-control-inner-background: transparent; " +
            "-fx-background-insets: 0; " +
            "-fx-padding: 4px 16px 8px 16px;"
        );
        messageListView.setCellFactory(lv -> new MessageListCell());
        VBox.setVgrow(messageListView, Priority.ALWAYS);

        VBox centerListContainer = new VBox(4);
        centerListContainer.setStyle("-fx-background-color: transparent;");
        centerListContainer.getChildren().addAll(e2eeBannerContainer, datePill, messageListView);
        VBox.setVgrow(messageListView, Priority.ALWAYS);

        // Thanh công cụ nổi chọn xóa nhiều tin (Task U10 — Floating Action Bar)
        buildFloatingActionBar();

        StackPane centerStack = new StackPane();
        centerStack.setStyle("-fx-background-color: transparent;");
        centerStack.getChildren().addAll(centerListContainer, floatingActionBar);
        StackPane.setAlignment(floatingActionBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(floatingActionBar, new Insets(0, 0, 16, 0));

        setCenter(centerStack);
    }

    // =========================================================================
    // 4. CHẾ ĐỘ CHỌN XÓA NHIỀU TIN NHẮN (Task U10 — Floating Action Bar)
    // =========================================================================
    private void buildFloatingActionBar() {
        floatingActionBar.setAlignment(Pos.CENTER);
        floatingActionBar.setSpacing(12);
        floatingActionBar.setPadding(new Insets(5, 16, 5, 16));
        floatingActionBar.setStyle(
            "-fx-background-color: #1E2128; " +
            "-fx-border-color: #363A45; " +
            "-fx-border-radius: 21px; " +
            "-fx-background-radius: 21px; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.55), 16, 0, 0, 6);"
        );

        // Khóa cứng chiều cao chuẩn Capsule Pill 42px (ngăn chặn StackPane kéo giãn toàn màn hình)
        floatingActionBar.setMinHeight(42);
        floatingActionBar.setPrefHeight(42);
        floatingActionBar.setMaxHeight(42);

        // Co giãn theo nội dung, không ép cứng chiều ngang
        floatingActionBar.setMinWidth(Region.USE_PREF_SIZE);
        floatingActionBar.setMaxWidth(Region.USE_PREF_SIZE);

        floatingActionBar.setVisible(false);
        floatingActionBar.setManaged(false);

        lblSelectedCount.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        lblSelectedCount.setMinWidth(Region.USE_PREF_SIZE);

        Separator divider = new Separator(javafx.geometry.Orientation.VERTICAL);
        divider.setStyle("-fx-background-color: #363A45; -fx-pref-height: 18px;");

        btnDeleteSelected.setText("🗑 Xóa (0)");
        btnDeleteSelected.setMinWidth(Region.USE_PREF_SIZE);
        btnDeleteSelected.setOnAction(e -> handleDeleteSelectedMessages());
        JavaFXTheme.applyTactileButton(btnDeleteSelected);

        btnCancelMulti.setText("✕ Hủy");
        btnCancelMulti.setStyle(
            "-fx-background-color: #262933; " +
            "-fx-border-color: #363A45; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px; " +
            "-fx-text-fill: #D0D2DC; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 4 12; " +
            "-fx-cursor: hand;"
        );
        btnCancelMulti.setMinWidth(Region.USE_PREF_SIZE);
        btnCancelMulti.setOnAction(e -> setMultiSelectMode(false));
        JavaFXTheme.applyTactileButton(btnCancelMulti);

        floatingActionBar.getChildren().addAll(lblSelectedCount, divider, btnDeleteSelected, btnCancelMulti);

        multiSelectMode.addListener((obs, oldVal, isMulti) -> {
            floatingActionBar.setVisible(isMulti);
            floatingActionBar.setManaged(isMulti);
            if (isMulti) {
                updateSelectedMessagesCount();
                floatingActionBar.setOpacity(0);
                floatingActionBar.setTranslateY(10);
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(160), floatingActionBar);
                ft.setToValue(1.0);
                javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.millis(160), floatingActionBar);
                tt.setToY(0);
                new javafx.animation.ParallelTransition(ft, tt).play();
            } else {
                for (MessageItem msg : messagesList) {
                    msg.setSelected(false);
                }
            }
        });

        messagesList.addListener((ListChangeListener<MessageItem>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (MessageItem item : change.getAddedSubList()) {
                        item.selectedProperty().addListener((o, ov, nv) -> updateSelectedMessagesCount());
                    }
                }
            }
        });
    }

    public void setMultiSelectMode(boolean active) {
        multiSelectMode.set(active);
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode.get();
    }

    public BooleanProperty multiSelectModeProperty() {
        return multiSelectMode;
    }

    private void updateSelectedMessagesCount() {
        long count = messagesList.stream().filter(MessageItem::isSelected).count();
        lblSelectedCount.setText("Đã chọn: " + count);
        btnDeleteSelected.setText("🗑 Xóa (" + count + ")");
        btnDeleteSelected.setDisable(count == 0);
        if (count == 0) {
            btnDeleteSelected.setStyle(
                "-fx-background-color: #24262E; " +
                "-fx-border-color: #363A45; " +
                "-fx-border-radius: 14px; " +
                "-fx-background-radius: 14px; " +
                "-fx-border-width: 1px; " +
                "-fx-text-fill: #6E7280; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 4 12; " +
                "-fx-cursor: default;"
            );
        } else {
            btnDeleteSelected.setStyle(
                "-fx-background-color: #2D1A1E; " +
                "-fx-border-color: #FA777C; " +
                "-fx-border-radius: 14px; " +
                "-fx-background-radius: 14px; " +
                "-fx-border-width: 1px; " +
                "-fx-text-fill: #FA777C; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 4 12; " +
                "-fx-cursor: hand;"
            );
        }
    }

    private void handleDeleteSelectedMessages() {
        long count = messagesList.stream().filter(MessageItem::isSelected).count();
        if (count == 0) return;

        boolean ok = JavaFXTheme.showConfirmDialog(
            ownerStage,
            "Xác nhận xóa tin nhắn",
            "Xóa " + count + " tin nhắn đã chọn (Mục 11.e)",
            "Bạn có chắc chắn muốn xóa vĩnh viễn các dòng tin nhắn đã chọn khỏi lịch sử trò chuyện không?"
        );
        if (ok) {
            messagesList.removeIf(MessageItem::isSelected);
            setMultiSelectMode(false);
        }
    }

    // =========================================================================
    // 5. THANH SOẠN THẢO & TRỢ LÝ AI (Integrated Capsule Bar chuẩn Telegram/Zalo)
    // =========================================================================
    private void buildBottomInputBar() {
        bottomBar.setPadding(new Insets(4, 20, 14, 20));
        bottomBar.getStyleClass().add("chat-bottom-bar");

        // Dải Smart Reply Chips: Ẩn mặc định, chỉ hiện khi nhấn icon ✨ hoặc gõ /ai
        smartRepliesBar.getChildren().clear();
        smartRepliesBar.setAlignment(Pos.CENTER_LEFT);
        smartRepliesBar.setPadding(new Insets(2, 0, 4, 4));
        smartRepliesBar.getChildren().addAll(
            createSmartChip("💡 Tóm tắt cuộc họp"),
            createSmartChip("📁 Gửi tài liệu tiến độ"),
            createSmartChip("✓ Đã hiểu & Xác nhận")
        );
        smartRepliesBar.setVisible(false);
        smartRepliesBar.setManaged(false);

        // Thanh công cụ Action Toolbar chuẩn Zalo PC (Ảnh 3, 4)
        HBox toolbarRow = new HBox(8);
        toolbarRow.setAlignment(Pos.CENTER_LEFT);
        toolbarRow.setPadding(new Insets(2, 6, 2, 6));

        Button btnEmoji = createToolbarIconButton("😀", "Biểu cảm / Emoji", () -> {
            txtMessageInput.appendText(" 😊");
            txtMessageInput.requestFocus();
        });
        Button btnPhoto = createToolbarIconButton("🖼", "Gửi hình ảnh", () -> {
            txtMessageInput.appendText(" [Ảnh: chụp_màn_hình_2026.png] ");
            txtMessageInput.requestFocus();
        });
        Button btnFile = createToolbarIconButton("📎", "Đính kèm tệp tin tài liệu (Mục 11.a)", () -> {
            txtMessageInput.appendText(" [Tài liệu: script_database.sql] ");
            txtMessageInput.requestFocus();
        });
        Button btnReminder = createToolbarIconButton("⏰", "Nhắc hẹn / Giao việc", () -> {
            txtMessageInput.appendText(" ⏰ Nhắc hẹn: Họp nhóm vào 19h tối nay! ");
            txtMessageInput.requestFocus();
        });
        Button btnAiQuick = createToolbarIconButton("✨", "Trợ lý AI (Gợi ý câu trả lời - Mục 11.h)", this::toggleAiSmartReplies);

        toolbarRow.getChildren().addAll(btnEmoji, btnPhoto, btnFile, btnReminder, btnAiQuick);

        // Khung Capsule tích hợp toàn bộ các nút bên trong
        inputCapsule.setAlignment(Pos.CENTER_LEFT);
        inputCapsule.setSpacing(6);
        inputCapsule.setPadding(new Insets(4, 6, 4, 10));
        inputCapsule.setStyle(
            "-fx-background-color: #0F1013; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 20px; " +
            "-fx-background-radius: 20px; " +
            "-fx-border-width: 1px;"
        );

        // Nút Đính kèm (📎)
        btnAttach.getStyleClass().add("btn-icon-subtle");
        btnAttach.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-text-fill: -text-muted; -fx-padding: 4 6; -fx-cursor: hand;");
        Tooltip.install(btnAttach, new Tooltip("Đính kèm tệp tin / hình ảnh"));
        JavaFXTheme.applyIconButton(btnAttach);

        // Nút AI Toggle (✨): Kích hoạt / Ẩn dải gợi ý thông minh
        btnAiToggle.getStyleClass().add("btn-icon-subtle");
        btnAiToggle.setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-text-fill: #A78BFA; -fx-padding: 4 6; -fx-cursor: hand;");
        Tooltip.install(btnAiToggle, new Tooltip("Gợi ý AI & Lệnh phản hồi nhanh (/ai)"));
        btnAiToggle.setOnAction(e -> toggleAiSmartReplies());
        JavaFXTheme.applyIconButton(btnAiToggle);

        // Ô soạn thảo mở rộng trong suốt
        txtMessageInput.setPromptText("Nhập tin nhắn vào hội thoại... (Gõ /ai để kích hoạt Trợ lý AI)");
        txtMessageInput.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-text-fill: -text-normal; " +
            "-fx-prompt-text-fill: -text-muted; " +
            "-fx-font-size: 13.5px; " +
            "-fx-padding: 6px 8px;"
        );
        HBox.setHgrow(txtMessageInput, Priority.ALWAYS);
        txtMessageInput.setOnAction(e -> handleSendMessage());

        // Nút Gửi (➤): Nằm gọn bên phải trong thanh Capsule, chỉ sáng xanh khi có nội dung
        btnSend.setPrefSize(32, 32);
        btnSend.setMinSize(32, 32);
        btnSend.setMaxSize(32, 32);
        btnSend.setOnAction(e -> handleSendMessage());
        updateSendButtonState("");

        // Lắng nghe thay đổi text: Tự động đổi màu viền, bật smart replies khi gõ /ai, cập nhật nút gửi
        txtMessageInput.textProperty().addListener((obs, oldV, newV) -> {
            updateSendButtonState(newV);

            boolean isAiCommand = newV != null && (newV.trim().startsWith("/ai") || newV.trim().startsWith("@ai"));
            if (isAiCommand) {
                smartRepliesBar.setVisible(true);
                smartRepliesBar.setManaged(true);
                inputCapsule.setStyle(
                    "-fx-background-color: #0F1013; " +
                    "-fx-border-color: #8B5CF6; " +
                    "-fx-border-radius: 20px; " +
                    "-fx-background-radius: 20px; " +
                    "-fx-border-width: 1px;"
                );
            } else {
                if (!isAiChipsManuallyOpened) {
                    smartRepliesBar.setVisible(false);
                    smartRepliesBar.setManaged(false);
                }
                boolean focused = txtMessageInput.isFocused();
                inputCapsule.setStyle(
                    "-fx-background-color: #0F1013; " +
                    "-fx-border-color: " + (focused ? "#4E6BF2" : "#2A2D35") + "; " +
                    "-fx-border-radius: 20px; " +
                    "-fx-background-radius: 20px; " +
                    "-fx-border-width: 1px;"
                );
            }
        });

        txtMessageInput.focusedProperty().addListener((obs, ov, focused) -> {
            String text = txtMessageInput.getText();
            boolean isAi = text != null && (text.trim().startsWith("/ai") || text.trim().startsWith("@ai"));
            if (!isAi) {
                inputCapsule.setStyle(
                    "-fx-background-color: #0F1013; " +
                    "-fx-border-color: " + (focused ? "#4E6BF2" : "#2A2D35") + "; " +
                    "-fx-border-radius: 20px; " +
                    "-fx-background-radius: 20px; " +
                    "-fx-border-width: 1px;"
                );
            }
        });

        inputCapsule.getChildren().addAll(btnAttach, btnAiToggle, txtMessageInput, btnSend);
        bottomBar.getChildren().addAll(smartRepliesBar, toolbarRow, inputCapsule);
    }

    private Button createToolbarIconButton(String icon, String tooltip, Runnable action) {
        Button btn = new Button(icon);
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-font-size: 13.5px; " +
            "-fx-text-fill: -text-muted; " +
            "-fx-padding: 3 6; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 6px;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #23262E; " +
            "-fx-font-size: 13.5px; " +
            "-fx-text-fill: -text-normal; " +
            "-fx-padding: 3 6; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 6px;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-font-size: 13.5px; " +
            "-fx-text-fill: -text-muted; " +
            "-fx-padding: 3 6; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 6px;"
        ));
        Tooltip.install(btn, new Tooltip(tooltip));
        btn.setOnAction(e -> {
            if (action != null) action.run();
        });
        JavaFXTheme.applyIconButton(btn);
        return btn;
    }

    private void updateSendButtonState(String text) {
        boolean hasText = text != null && !text.trim().isEmpty();
        btnSend.setDisable(!hasText);
        if (hasText) {
            btnSend.setStyle(
                "-fx-background-color: #4E6BF2; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 16px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(78, 107, 242, 0.45), 8, 0, 0, 2);"
            );
        } else {
            btnSend.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #4B4F58; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 16px; " +
                "-fx-cursor: default;"
            );
        }
    }

    private void toggleAiSmartReplies() {
        isAiChipsManuallyOpened = !isAiChipsManuallyOpened;
        smartRepliesBar.setVisible(isAiChipsManuallyOpened);
        smartRepliesBar.setManaged(isAiChipsManuallyOpened);
        if (isAiChipsManuallyOpened) {
            btnAiToggle.setStyle("-fx-background-color: #2D2248; -fx-border-color: #8B5CF6; -fx-border-radius: 15px; -fx-background-radius: 15px; -fx-border-width: 1px; -fx-font-size: 13px; -fx-text-fill: #D8B4FE; -fx-padding: 3 5; -fx-cursor: hand;");
        } else {
            btnAiToggle.setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-text-fill: #A78BFA; -fx-padding: 4 6; -fx-cursor: hand;");
        }
    }

    private Button createSmartChip(String text) {
        Button chip = new Button(text);
        chip.getStyleClass().add("smart-reply-chip");
        chip.setStyle(
            "-fx-background-color: #1F222A; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-text-fill: #A0A5B0; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 3px 10px; " +
            "-fx-cursor: hand;"
        );
        chip.setOnAction(e -> {
            txtMessageInput.setText(text);
            txtMessageInput.requestFocus();
            txtMessageInput.positionCaret(text.length());
        });
        JavaFXTheme.applyTactileButton(chip);
        return chip;
    }

    private void handleSendMessage() {
        String msg = txtMessageInput.getText().trim();
        if (msg.isEmpty()) return;

        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        MessageItem userMsg = new MessageItem(
            UUID.randomUUID().toString(),
            "Trần Nguyễn Tuấn Minh",
            now,
            msg,
            true,  // isOutgoing
            false  // isAi
        );
        userMsg.selectedProperty().addListener((o, ov, nv) -> updateSelectedMessagesCount());
        messagesList.add(userMsg);
        txtMessageInput.clear();

        Platform.runLater(() -> messageListView.scrollTo(messagesList.size() - 1));

        // Nếu người dùng kích hoạt lệnh /ai, sinh phản hồi mô phỏng từ AI Bot sau 600ms
        if (msg.startsWith("/ai") || msg.toLowerCase().contains("ai bot")) {
            simulateAiResponse(msg);
        }
    }

    private void simulateAiResponse(String prompt) {
        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(e -> {
            String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String cleanPrompt = prompt.replaceFirst("^/ai\\s*", "").trim();
            String aiAnswer = cleanPrompt.isEmpty()
                ? "💡 Tôi là Trợ lý AI. Tôi đã sẵn sàng hỗ trợ tóm tắt hội thoại, tra cứu CSDL hoặc gợi ý mã nguồn Java cho nhóm!"
                : "💡 [AI Phản hồi]: Đối với yêu cầu \"" + cleanPrompt + "\", hệ thống khuyến nghị tuân thủ quy chuẩn bảo mật AES-GCM và cấu trúc CSDL 13 bảng.";

            MessageItem botMsg = new MessageItem(
                UUID.randomUUID().toString(),
                "Trợ lý AI",
                now,
                aiAnswer,
                false, // isOutgoing
                true   // isAi
            );
            botMsg.selectedProperty().addListener((o, ov, nv) -> updateSelectedMessagesCount());
            messagesList.add(botMsg);
            Platform.runLater(() -> messageListView.scrollTo(messagesList.size() - 1));
        });
        pause.play();
    }

    private void handleReportSpam() {
        if (onReportSpam != null) {
            onReportSpam.run();
            return;
        }

        boolean confirmed = JavaFXTheme.showConfirmDialog(
            ownerStage,
            "Báo cáo vi phạm",
            "Gửi báo cáo vi phạm tới Ban quản trị (Mục 10)",
            "Bạn có chắc chắn muốn gửi báo cáo vi phạm đối với cuộc trò chuyện này không?"
        );
        if (confirmed) {
            JavaFXTheme.showInfoAlert(
                ownerStage,
                "Báo cáo đã gửi",
                "Báo cáo đã được ghi nhận",
                "Báo cáo của bạn đã được chuyển tới bảng spam_reports trong CSDL để Quản trị viên xử lý."
            );
        }
    }

    // =========================================================================
    // 6. MA TRẬN DỮ LIỆU MẪU SỐNG ĐỘNG (Nạp từ ChatMockRepository)
    // =========================================================================
    public void loadConversation(String chatId) {
        this.currentChatId = chatId;
        messagesList.clear();
        closeSearchBar();
        setMultiSelectMode(false);

        ChatMockRepository.ConversationMeta meta = ChatMockRepository.getConversation(chatId);
        if (meta != null) {
            lblChatTitle.setText((meta.isEncrypted() ? "🔒 " : (meta.isGroup() ? "👥 " : "💬 ")) + meta.getTitle());
            lblChatSubtitle.setText(meta.getSubtitle());
            lblHeaderAvatarText.setText(meta.getInitials());
            headerAvatarCircle.setFill(Color.web(meta.getAvatarColor()));

            boolean hasStatus = meta.isOnline() && !meta.isGroup();
            headerStatusDot.setVisible(hasStatus);
            headerStatusDot.setManaged(hasStatus);

            e2eeBannerContainer.setVisible(meta.isEncrypted());
            e2eeBannerContainer.setManaged(meta.isEncrypted());

            for (MessageItem msg : meta.getMessages()) {
                addMessageInternal(new MessageItem(
                    msg.getId(),
                    msg.getSenderName(),
                    msg.getTimestamp(),
                    msg.getContent(),
                    msg.isOutgoing(),
                    msg.isAi()
                ));
            }
        } else {
            lblChatTitle.setText("💬 Cuộc trò chuyện");
            lblChatSubtitle.setText("Đang hoạt động");
            lblHeaderAvatarText.setText("💬");
            headerAvatarCircle.setFill(Color.web("#4E6BF2"));
            headerStatusDot.setVisible(false);
            headerStatusDot.setManaged(false);
            e2eeBannerContainer.setVisible(false);
            e2eeBannerContainer.setManaged(false);

            addMessageInternal(new MessageItem(
                "msg_fallback",
                "Hệ thống",
                "12:00",
                "Bắt đầu cuộc trò chuyện mới. Mọi dữ liệu truyền tải đều được xác thực qua Socket Server.",
                false,
                false
            ));
        }

        Platform.runLater(() -> {
            if (!messagesList.isEmpty()) {
                messageListView.scrollTo(messagesList.size() - 1);
            }
        });
    }

    public boolean highlightAndScrollToMessage(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) return false;
        for (int i = 0; i < messagesList.size(); i++) {
            if (messageId.equals(messagesList.get(i).getId())) {
                final int targetIndex = i;
                Platform.runLater(() -> highlightMessage(targetIndex));
                return true;
            }
        }
        return false;
    }

    private void addMessageInternal(MessageItem item) {
        item.selectedProperty().addListener((o, ov, nv) -> updateSelectedMessagesCount());
        messagesList.add(item);
    }

    // =========================================================================
    // PUBLIC GETTERS & SETTERS
    // =========================================================================
    public String getCurrentChatId() { return currentChatId; }
    public ObservableList<MessageItem> getMessagesList() { return messagesList; }
    public ListView<MessageItem> getMessageListView() { return messageListView; }

    public void setOwnerStage(Stage ownerStage) { this.ownerStage = ownerStage; }
    public void setOnReportSpam(Runnable onReportSpam) { this.onReportSpam = onReportSpam; }
    public void setOnToggleDrawer(Runnable onToggleDrawer) { this.onToggleDrawer = onToggleDrawer; }
    public void setOnTriggerGlobalSearch(java.util.function.Consumer<String> onTriggerGlobalSearch) {
        this.onTriggerGlobalSearch = onTriggerGlobalSearch;
    }

    public void clearHistory() {
        messagesList.clear();
    }

    // =========================================================================
    // 7. CUSTOM LISTCELL CHO BONG BÓNG CHAT (Chuẩn Telegram Desktop / Zalo)
    // =========================================================================
    private class MessageListCell extends ListCell<MessageItem> {
        private final HBox rootBox = new HBox(8);
        private final CheckBox chkSelect = new CheckBox();

        // Avatar tròn định danh có màu sắc băm theo tên người dùng
        private final StackPane avatarPane = new StackPane();
        private final Circle avatarCircle = new Circle(16);
        private final Label lblAvatarText = new Label();

        // Bong bóng tin nhắn
        private final VBox bubble = new VBox(2);
        private final Label lblSenderName = new Label();
        private final Label lblAiTag = new Label("[AI BOT]");
        private final Label lblContent = new Label();
        private final HBox timeRow = new HBox(3);
        private final Label lblTime = new Label();
        private final Label lblStatusTicks = new Label("✓✓");

        private ChangeListener<Number> highlightListener;
        private ChangeListener<Boolean> selectedListener;

        public MessageListCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setStyle("-fx-background-color: transparent; -fx-padding: 3px 6px;");

            chkSelect.visibleProperty().bind(multiSelectMode);
            chkSelect.managedProperty().bind(multiSelectMode);
            chkSelect.setStyle("-fx-cursor: hand;");

            avatarCircle.setFill(Color.web("#252830"));
            lblAvatarText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
            avatarPane.getChildren().addAll(avatarCircle, lblAvatarText);
            avatarPane.setAlignment(Pos.TOP_CENTER);

            lblAiTag.setStyle(
                "-fx-font-size: 10px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #A78BFA; " +
                "-fx-background-color: rgba(139, 92, 246, 0.2); " +
                "-fx-background-radius: 4px; " +
                "-fx-padding: 1px 5px;"
            );

            lblContent.setWrapText(true);
            lblContent.setMaxWidth(520);

            // Context Menu thao tác nhanh trên tin nhắn
            ContextMenu contextMenu = new ContextMenu();
            JavaFXTheme.applyContextMenuTheme(contextMenu);
            MenuItem mCopy = new MenuItem("📋 Sao chép nội dung");
            mCopy.setOnAction(e -> {
                if (getItem() != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(getItem().getContent());
                    clipboard.setContent(cc);
                }
            });

            MenuItem mAskAi = new MenuItem("✨ Hỏi AI về tin này (Mục 11.h)");
            mAskAi.setOnAction(e -> {
                if (getItem() != null) {
                    txtMessageInput.setText("/ai Hãy giải thích rõ hơn về: \"" + getItem().getContent() + "\"");
                    txtMessageInput.requestFocus();
                    txtMessageInput.positionCaret(txtMessageInput.getText().length());
                }
            });

            MenuItem mSelectMode = new MenuItem("☑ Chọn nhiều tin nhắn...");
            mSelectMode.setOnAction(e -> {
                if (getItem() != null) {
                    setMultiSelectMode(true);
                    getItem().setSelected(true);
                }
            });

            MenuItem mDeleteThis = new MenuItem("🗑 Xóa tin nhắn này");
            mDeleteThis.setOnAction(e -> {
                if (getItem() != null) {
                    messagesList.remove(getItem());
                }
            });

            contextMenu.getItems().addAll(mCopy, mAskAi, new SeparatorMenuItem(), mSelectMode, mDeleteThis);
            setContextMenu(contextMenu);
        }

        @Override
        protected void updateItem(MessageItem item, boolean empty) {
            super.updateItem(item, empty);

            if (highlightListener != null && getItem() != null) {
                getItem().highlightIntensityProperty().removeListener(highlightListener);
                highlightListener = null;
            }
            if (selectedListener != null && getItem() != null) {
                getItem().selectedProperty().removeListener(selectedListener);
                selectedListener = null;
            }

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 3px 6px;");
                return;
            }

            chkSelect.setSelected(item.isSelected());
            selectedListener = (obs, ov, nv) -> chkSelect.setSelected(nv);
            item.selectedProperty().addListener(selectedListener);
            chkSelect.setOnAction(e -> item.setSelected(chkSelect.isSelected()));

            lblContent.setText(item.getContent());
            lblTime.setText(item.getTimestamp());

            bubble.getChildren().clear();
            timeRow.getChildren().clear();
            rootBox.getChildren().clear();

            bubble.setMaxWidth(520);
            bubble.setFillWidth(true);

            if (item.isOutgoing()) {
                // 1. TIN NHẮN GỬI ĐI (Outgoing) — Căn phải, màu sapphire #4E6BF2, timestamp nằm gọn góc dưới phải
                rootBox.setAlignment(Pos.CENTER_RIGHT);

                bubble.setAlignment(Pos.BOTTOM_RIGHT);
                bubble.setStyle(
                    "-fx-background-color: #4E6BF2; " +
                    "-fx-background-radius: 14px 14px 3px 14px; " +
                    "-fx-padding: 7px 12px 6px 12px;"
                );

                lblContent.setStyle(
                    "-fx-text-fill: #FFFFFF; " +
                    "-fx-font-size: 13.5px; " +
                    "-fx-line-spacing: 2px;"
                );

                timeRow.setAlignment(Pos.BOTTOM_RIGHT);
                lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255, 255, 255, 0.65);");
                lblStatusTicks.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255, 255, 255, 0.65);");
                timeRow.getChildren().addAll(lblTime, lblStatusTicks);

                bubble.getChildren().addAll(lblContent, timeRow);
                rootBox.getChildren().addAll(chkSelect, bubble);

            } else if (item.isAi()) {
                // 2. TIN NHẮN TRỢ LÝ AI (AI Bot) — Tím than trầm #231F32, viền trái 3px solid #8B5CF6
                rootBox.setAlignment(Pos.CENTER_LEFT);

                avatarCircle.setFill(Color.web("#382760"));
                avatarCircle.setStroke(Color.web("#8B5CF6"));
                avatarCircle.setStrokeWidth(1.2);
                lblAvatarText.setText("🤖");

                bubble.setAlignment(Pos.TOP_LEFT);
                bubble.setStyle(
                    "-fx-background-color: #231F32; " +
                    "-fx-border-color: transparent transparent transparent #8B5CF6; " +
                    "-fx-border-width: 0 0 0 3px; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px 12px 12px 3px; " +
                    "-fx-padding: 8px 14px 6px 14px;"
                );

                HBox aiHeader = new HBox(6);
                aiHeader.setAlignment(Pos.CENTER_LEFT);
                lblSenderName.setText("Trợ lý AI");
                lblSenderName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #D8B4FE;");
                aiHeader.getChildren().addAll(lblSenderName, lblAiTag);

                lblContent.setStyle(
                    "-fx-text-fill: #EDEDED; " +
                    "-fx-font-size: 13.5px; " +
                    "-fx-line-spacing: 2px;"
                );

                timeRow.setAlignment(Pos.BOTTOM_RIGHT);
                lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: #A78BFA;");
                timeRow.getChildren().add(lblTime);

                bubble.getChildren().addAll(aiHeader, lblContent, timeRow);
                rootBox.getChildren().addAll(chkSelect, avatarPane, bubble);

            } else {
                // 3. TIN NHẮN NHẬN ĐƯỢC (Received) — Nền tối #1E2128, viền mảnh #2A2D35, tên người gửi theo màu avatar
                rootBox.setAlignment(Pos.CENTER_LEFT);

                String color = getUserAvatarColor(item.getSenderName());
                avatarCircle.setFill(Color.web(color));
                avatarCircle.setStroke(null);
                lblAvatarText.setText(getUserInitials(item.getSenderName()));

                // Mini-Profile Popover khi click Avatar hoặc Tên người gửi
                avatarPane.setStyle("-fx-cursor: hand;");
                avatarPane.setOnMouseClicked(e -> {
                    JavaFXTheme.showMiniProfilePopover(
                        avatarPane,
                        item.getSenderName(),
                        "@" + item.getSenderName().toLowerCase().replaceAll("\\s+", "_"),
                        true,
                        color,
                        getUserInitials(item.getSenderName())
                    );
                });

                bubble.setAlignment(Pos.TOP_LEFT);
                bubble.setStyle(
                    "-fx-background-color: #1E2128; " +
                    "-fx-border-color: #2A2D35; " +
                    "-fx-border-radius: 14px; " +
                    "-fx-background-radius: 14px 14px 14px 3px; " +
                    "-fx-padding: 7px 12px 6px 12px;"
                );

                lblSenderName.setText(item.getSenderName());
                lblSenderName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-padding: 0 0 2px 0; -fx-cursor: hand;");
                lblSenderName.setOnMouseClicked(e -> {
                    JavaFXTheme.showMiniProfilePopover(
                        lblSenderName,
                        item.getSenderName(),
                        "@" + item.getSenderName().toLowerCase().replaceAll("\\s+", "_"),
                        true,
                        color,
                        getUserInitials(item.getSenderName())
                    );
                });

                lblContent.setStyle(
                    "-fx-text-fill: #EDEDED; " +
                    "-fx-font-size: 13.5px; " +
                    "-fx-line-spacing: 2px;"
                );

                timeRow.setAlignment(Pos.BOTTOM_RIGHT);
                lblTime.setStyle("-fx-font-size: 10px; -fx-text-fill: #8E929B;");
                timeRow.getChildren().add(lblTime);

                bubble.getChildren().addAll(lblSenderName, lblContent, timeRow);
                rootBox.getChildren().addAll(chkSelect, avatarPane, bubble);
            }

            // Gán hiệu ứng chớp màu nền vàng hổ phách khi tìm kiếm (Task U9)
            highlightListener = (obs, ov, nv) -> applyHighlightIntensity(nv.doubleValue());
            item.highlightIntensityProperty().addListener(highlightListener);
            applyHighlightIntensity(item.getHighlightIntensity());

            setText(null);
            setGraphic(rootBox);
        }

        private void applyHighlightIntensity(double intensity) {
            double alpha = intensity * 0.35;
            if (alpha > 0.005) {
                setStyle(
                    "-fx-background-color: rgba(240, 178, 50, " + String.format(Locale.US, "%.3f", alpha) + "); " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 3px 6px;"
                );
            } else {
                setStyle("-fx-background-color: transparent; -fx-padding: 3px 6px;");
            }
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            if (getItem() != null && getItem().getHighlightIntensity() > 0.005) {
                applyHighlightIntensity(getItem().getHighlightIntensity());
            } else {
                setStyle("-fx-background-color: transparent; -fx-padding: 3px 6px;");
            }
        }
    }
}
