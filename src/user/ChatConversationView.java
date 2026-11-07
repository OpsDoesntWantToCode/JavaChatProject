package user;

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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Khung Trò chuyện Trực tiếp & Lịch sử Tin nhắn (Task U9, U10, U11 — ChatConversationView)
 * Triển khai theo chuẩn Vòng lặp 1 & tài liệu SSOT docs/ui-spec/chat-messaging.md, ai-assistant.md:
 * - Kiến trúc ảo hóa VirtualFlow: Dùng ListView<MessageItem> thay cho ScrollPane giữ vững 60fps
 * - Custom ListCell hiển thị 3 loại bong bóng: Gửi đi (sent), Nhận được (received), và Trợ lý AI (ai)
 * - Task U9: Thanh tìm kiếm nội bộ, cuộn tự động scrollTo(index) và chớp màu nền vàng hổ phách 1500ms
 * - Task U10: Chế độ chọn xóa nhiều tin nhắn (Multi-Select) với CheckBox và Floating Action Bar nổi
 * - Task U11: Thanh nhập tin nhắn thông minh, dải Smart Reply Chips, kích hoạt AI (/ai) và nút ✨ Gợi ý AI
 * - Header chuẩn Single Border Source (chỉ có border-bottom 1px solid #2A2D35)
 */
public class ChatConversationView extends BorderPane {

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

    // 3. Banner Mã hóa Đầu cuối E2EE
    private final HBox e2eeBanner = new HBox(10);

    // 4. Danh sách Tin nhắn Ảo hóa (VirtualFlow ListView)
    private final ObservableList<MessageItem> messagesList = FXCollections.observableArrayList();
    private final ListView<MessageItem> messageListView = new ListView<>(messagesList);

    // 5. Chế độ Chọn xóa nhiều tin nhắn (Task U10)
    private final BooleanProperty multiSelectMode = new SimpleBooleanProperty(false);
    private final HBox floatingActionBar = new HBox(14);
    private final Label lblSelectedCount = new Label("Đã chọn 0 tin nhắn");
    private final Button btnDeleteSelected = new Button("🗑 Xóa tin đã chọn");
    private final Button btnCancelMulti = new Button("Hủy chọn");

    // 6. Thanh Soạn thảo & Trợ lý AI (Task U11)
    private final VBox bottomBar = new VBox(8);
    private final HBox smartRepliesBar = new HBox(8);
    private final TextField txtMessageInput = new TextField();
    private final Button btnAiSuggest = new Button("✨ Gợi ý AI");
    private final Button btnSend = new Button("Gửi 🚀");

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
        headerStatusDot.setFill(Color.web("#23A55A")); // -status-online: #23A55A
        StackPane.setAlignment(headerStatusDot, Pos.BOTTOM_RIGHT);
        headerAvatarStack.getChildren().addAll(headerAvatarCircle, lblHeaderAvatarText, headerStatusDot);

        // Title + Subtitle
        VBox titleBox = new VBox(2);
        lblChatTitle.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        lblChatSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        titleBox.getChildren().addAll(lblChatTitle, lblChatSubtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Các nút hành động trên Header
        Button btnToggleMulti = new Button("☑️");
        btnToggleMulti.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnToggleMulti, new Tooltip("Chọn nhiều tin nhắn để xóa (Mục 11.e)"));
        btnToggleMulti.setOnAction(e -> setMultiSelectMode(!isMultiSelectMode()));

        Button btnSearch = new Button("🔍");
        btnSearch.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnSearch, new Tooltip("Tìm kiếm trong cuộc trò chuyện (Mục 11.f)"));
        btnSearch.setOnAction(e -> toggleSearchBar());

        Button btnReportSpam = new Button("⚠️");
        btnReportSpam.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnReportSpam, new Tooltip("Báo cáo vi phạm / Spam (Mục 10)"));
        btnReportSpam.setOnAction(e -> handleReportSpam());

        Button btnToggleDrawer = new Button("ℹ️");
        btnToggleDrawer.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnToggleDrawer, new Tooltip("Thông tin chi tiết hội thoại (Cột 4)"));
        btnToggleDrawer.setOnAction(e -> {
            if (onToggleDrawer != null) {
                onToggleDrawer.run();
            }
        });

        chatHeader.getChildren().addAll(headerAvatarStack, titleBox, btnToggleMulti, btnSearch, btnReportSpam, btnToggleDrawer);
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

        btnCloseSearch.getStyleClass().add("btn-icon-subtle");
        Tooltip.install(btnCloseSearch, new Tooltip("Đóng tìm kiếm (Esc)"));
        btnCloseSearch.setOnAction(e -> closeSearchBar());

        searchBar.getChildren().addAll(lblIcon, txtSearchBox, lblSearchCount, btnPrevMatch, btnNextMatch, btnCloseSearch);
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

    /**
     * Cuộn tới vị trí tin nhắn và kích hoạt hiệu ứng chớp màu nền vàng hổ phách (Task U9):
     * rgba(240, 178, 50, 0.35) mờ dần về trong suốt trong 1500ms (motion.highlight)
     */
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
    // 3. KHU VỰC CHAT TRUNG TÂM (VirtualFlow ListView & Floating Action Bar)
    // =========================================================================
    private void buildCenterChatArea() {
        // Banner E2EE Messenger style
        e2eeBanner.setAlignment(Pos.CENTER);
        e2eeBanner.setPadding(new Insets(10, 16, 10, 16));
        e2eeBanner.getStyleClass().add("e2ee-banner");

        Label lblBanner = new Label("🔒 Tin nhắn trong nhóm này được bảo vệ bằng mã hóa đối xứng AES-GCM. Chỉ các thành viên trong nhóm mới có khóa giải mã.");
        lblBanner.setWrapText(true);
        lblBanner.setStyle("-fx-font-size: 12px; -fx-text-fill: #A0C0FF;");
        e2eeBanner.getChildren().add(lblBanner);

        // Cấu hình VirtualFlow ListView
        messageListView.getStyleClass().add("transparent-scroll");
        messageListView.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-control-inner-background: transparent; " +
            "-fx-background-insets: 0; " +
            "-fx-padding: 8px 16px;"
        );
        messageListView.setCellFactory(lv -> new MessageListCell());
        VBox.setVgrow(messageListView, Priority.ALWAYS);

        VBox centerListContainer = new VBox(8);
        centerListContainer.setPadding(new Insets(12, 16, 12, 16));
        centerListContainer.getChildren().addAll(e2eeBanner, messageListView);
        VBox.setVgrow(messageListView, Priority.ALWAYS);

        // Thanh công cụ nổi chọn xóa nhiều tin (Task U10 — Floating Action Bar)
        buildFloatingActionBar();

        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(centerListContainer, floatingActionBar);
        StackPane.setAlignment(floatingActionBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(floatingActionBar, new Insets(0, 24, 16, 24));

        setCenter(centerStack);
    }

    // =========================================================================
    // 4. CHẾ ĐỘ CHỌN XÓA NHIỀU TIN NHẮN (Task U10 — Floating Action Bar)
    // =========================================================================
    private void buildFloatingActionBar() {
        floatingActionBar.setAlignment(Pos.CENTER);
        floatingActionBar.setPadding(new Insets(10, 20, 10, 20));
        floatingActionBar.setStyle(
            "-fx-background-color: #1B1D22; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 14, 0, 0, 4);"
        );
        floatingActionBar.setMaxWidth(480);
        floatingActionBar.setVisible(false);
        floatingActionBar.setManaged(false);

        lblSelectedCount.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnDeleteSelected.getStyleClass().add("btn-danger-outline");
        btnDeleteSelected.setOnAction(e -> handleDeleteSelectedMessages());

        btnCancelMulti.getStyleClass().add("btn-icon-subtle");
        btnCancelMulti.setStyle("-fx-border-color: #2A2D35; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 6 12;");
        btnCancelMulti.setOnAction(e -> setMultiSelectMode(false));

        floatingActionBar.getChildren().addAll(lblSelectedCount, spacer, btnDeleteSelected, btnCancelMulti);

        // Theo dõi thay đổi trạng thái multiSelectMode
        multiSelectMode.addListener((obs, oldVal, isMulti) -> {
            floatingActionBar.setVisible(isMulti);
            floatingActionBar.setManaged(isMulti);
            if (isMulti) {
                updateSelectedMessagesCount();
            } else {
                for (MessageItem msg : messagesList) {
                    msg.setSelected(false);
                }
            }
        });

        // Cập nhật số lượng đếm khi bất kỳ tin nhắn nào được tick chọn
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
        lblSelectedCount.setText("Đã chọn " + count + " tin nhắn");
        btnDeleteSelected.setDisable(count == 0);
    }

    private void handleDeleteSelectedMessages() {
        long count = messagesList.stream().filter(MessageItem::isSelected).count();
        if (count == 0) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        if (ownerStage != null) confirm.initOwner(ownerStage);
        confirm.setTitle("Xác nhận xóa tin nhắn");
        confirm.setHeaderText("Xóa " + count + " tin nhắn đã chọn (Mục 11.e)");
        confirm.setContentText("Bạn có chắc chắn muốn xóa vĩnh viễn các dòng tin nhắn đã chọn khỏi lịch sử trò chuyện không?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                messagesList.removeIf(MessageItem::isSelected);
                setMultiSelectMode(false);
            }
        });
    }

    // =========================================================================
    // 5. THANH SOẠN THẢO & TRỢ LÝ AI (Task U11 — Smart Reply Chips & Input Bar)
    // =========================================================================
    private void buildBottomInputBar() {
        bottomBar.setPadding(new Insets(8, 20, 16, 20));
        bottomBar.getStyleClass().add("chat-bottom-bar");

        // Dải Smart Reply Chips (.smart-reply-chip)
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
        Tooltip.install(btnAttach, new Tooltip("Đính kèm tệp tin tài liệu"));

        txtMessageInput.setPromptText("Nhập tin nhắn vào hội thoại... (Gõ /ai để kích hoạt Trợ lý AI)");
        txtMessageInput.getStyleClass().add("chat-input-field");
        HBox.setHgrow(txtMessageInput, Priority.ALWAYS);
        txtMessageInput.setOnAction(e -> handleSendMessage());

        // Hiệu ứng phát sáng viền tím (.ai-active) khi gõ /ai hoặc @ai
        txtMessageInput.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && (newV.trim().startsWith("/ai") || newV.trim().startsWith("@ai"))) {
                if (!txtMessageInput.getStyleClass().contains("ai-active")) {
                    txtMessageInput.getStyleClass().add("ai-active");
                }
            } else {
                txtMessageInput.getStyleClass().remove("ai-active");
            }
        });

        btnAiSuggest.getStyleClass().add("btn-ai-suggest");
        Tooltip.install(btnAiSuggest, new Tooltip("Nhờ Trợ lý AI phân tích và đưa ra phản hồi thông minh (Mục 11.h)"));
        btnAiSuggest.setOnAction(e -> handleAiSuggestion());

        btnSend.getStyleClass().add("btn-primary");
        btnSend.setOnAction(e -> handleSendMessage());

        inputRow.getChildren().addAll(btnAttach, txtMessageInput, btnAiSuggest, btnSend);
        bottomBar.getChildren().addAll(smartRepliesBar, inputRow);
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

    private void handleAiSuggestion() {
        txtMessageInput.setText("Tôi đã kiểm tra kỹ lưỡng các module, toàn bộ chỉ tiêu kỹ thuật đã sẵn sàng cho buổi review!");
        txtMessageInput.requestFocus();
        txtMessageInput.positionCaret(txtMessageInput.getText().length());
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (ownerStage != null) alert.initOwner(ownerStage);
        alert.setTitle("Báo cáo vi phạm");
        alert.setHeaderText("Gửi báo cáo vi phạm tới Ban quản trị (Mục 10)");
        alert.setContentText("Bạn có chắc chắn muốn gửi báo cáo vi phạm đối với cuộc trò chuyện này không?");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                if (ownerStage != null) ok.initOwner(ownerStage);
                ok.setHeaderText("Báo cáo đã được ghi nhận");
                ok.setContentText("Báo cáo của bạn đã được chuyển tới bảng spam_reports trong CSDL để Quản trị viên xử lý.");
                ok.showAndWait();
            }
        });
    }

    // =========================================================================
    // 6. MA TRẬN DỮ LIỆU MẪU SỐNG ĐỘNG (Kịch bản group_project & user_chihai)
    // =========================================================================
    public void loadConversation(String chatId) {
        this.currentChatId = chatId;
        messagesList.clear();
        closeSearchBar();
        setMultiSelectMode(false);

        if ("group_project".equals(chatId)) {
            lblChatTitle.setText("🔒 Nhóm Đồ Án Java 2026");
            lblChatSubtitle.setText("3 thành viên • Mã hóa đầu cuối AES-GCM 256-bit");
            lblHeaderAvatarText.setText("👥");
            e2eeBanner.setVisible(true);
            e2eeBanner.setManaged(true);

            // 4 tin nhắn mẫu kèm Bot AI theo ma trận dummy-data-matrix.md
            addMessageInternal(new MessageItem(
                "msg_1",
                "Lưu Chí Hải",
                "10:15",
                "Script database.sql đã chạy hoàn tất 13 bảng trên PostgreSQL rồi nhé!",
                false, // received
                false
            ));
            addMessageInternal(new MessageItem(
                "msg_2",
                "Nguyễn Lan Anh",
                "10:16",
                "Tuyệt vời, mình đã test thử các ràng buộc khóa ngoại và index idx_login_time chạy rất nhanh.",
                false, // received
                false
            ));
            addMessageInternal(new MessageItem(
                "msg_3",
                "Trần Nguyễn Tuấn Minh",
                "10:18",
                "Giao diện User JavaFX 21 + theme.css đã hoàn thành toàn bộ form Đăng nhập, Đăng ký 7 trường và khung chat rồi.",
                true,  // sent
                false
            ));
            addMessageInternal(new MessageItem(
                "msg_4",
                "Trợ lý AI",
                "10:19",
                "💡 Các bạn nhớ chụp ảnh minh chứng TableView và các trường nhập liệu trước ngày 22/11/2026 để tổng hợp vào Báo cáo PDF mốc 20% nhé!",
                false, // received
                true   // AI bot
            ));
        } else if ("user_chihai".equals(chatId)) {
            lblChatTitle.setText("💬 Lưu Chí Hải");
            lblChatSubtitle.setText("Đang trực tuyến • @chihai_2026");
            lblHeaderAvatarText.setText("H");
            e2eeBanner.setVisible(false);
            e2eeBanner.setManaged(false);

            addMessageInternal(new MessageItem(
                "msg_5",
                "Trần Nguyễn Tuấn Minh",
                "14:00",
                "Hải ơi, cậu kiểm tra giúp mình nhánh feat/user-ui xem các icon vector trắng đã tương phản rõ chưa nhé.",
                true,  // sent
                false
            ));
            addMessageInternal(new MessageItem(
                "msg_6",
                "Lưu Chí Hải",
                "14:02",
                "Mình xem rồi, icon trắng Lucide trên nền badge 38x38px nhìn rất sắc nét và chuyên nghiệp!",
                false, // received
                false
            ));
        } else {
            lblChatTitle.setText("💬 Cuộc trò chuyện");
            lblChatSubtitle.setText("Đang hoạt động");
            lblHeaderAvatarText.setText("💬");
            e2eeBanner.setVisible(false);
            e2eeBanner.setManaged(false);

            addMessageInternal(new MessageItem(
                "msg_7",
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

    public void clearHistory() {
        messagesList.clear();
    }

    // =========================================================================
    // 7. CUSTOM LISTCELL CHO BONG BÓNG CHAT (3 Loại Bong Bóng)
    // =========================================================================
    private class MessageListCell extends ListCell<MessageItem> {
        private final HBox rootBox = new HBox(10);
        private final CheckBox chkSelect = new CheckBox();

        // Avatar
        private final StackPane avatarPane = new StackPane();
        private final Circle avatarCircle = new Circle(17);
        private final Label lblAvatarText = new Label();

        // Khung nội dung
        private final VBox bubbleContainer = new VBox(4);
        private final HBox metaRow = new HBox(6);
        private final Label lblSender = new Label();
        private final Label lblAiTag = new Label("[AI BOT]");
        private final Label lblTime = new Label();
        private final Label lblContent = new Label();

        private ChangeListener<Number> highlightListener;
        private ChangeListener<Boolean> selectedListener;

        public MessageListCell() {
            // Không áp dụng nền mặc định màu xanh của ListView khi select
            setStyle("-fx-background-color: transparent; -fx-padding: 4px 12px;");

            chkSelect.visibleProperty().bind(multiSelectMode);
            chkSelect.managedProperty().bind(multiSelectMode);
            chkSelect.setStyle("-fx-cursor: hand;");

            avatarCircle.setFill(Color.web("#252830"));
            lblAvatarText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
            avatarPane.getChildren().addAll(avatarCircle, lblAvatarText);

            lblAiTag.setStyle(
                "-fx-font-size: 10.5px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #A78BFA; " +
                "-fx-background-color: rgba(139, 92, 246, 0.2); " +
                "-fx-background-radius: 4px; " +
                "-fx-padding: 1px 6px;"
            );

            lblContent.setWrapText(true);
            lblContent.setMaxWidth(520);

            // Context Menu thao tác nhanh trên tin nhắn
            ContextMenu contextMenu = new ContextMenu();
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

            MenuItem mSelectMode = new MenuItem("☑️ Chọn nhiều tin nhắn...");
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

            // Dọn dẹp listener cũ để tránh rò rỉ bộ nhớ
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
                setStyle("-fx-background-color: transparent; -fx-padding: 4px 12px;");
                return;
            }

            // Gán dữ liệu hiển thị
            lblSender.setText(item.isOutgoing() ? "Bạn" : item.getSenderName());
            lblTime.setText(item.getTimestamp());
            lblContent.setText(item.getContent());

            // Đồng bộ CheckBox
            chkSelect.setSelected(item.isSelected());
            selectedListener = (obs, ov, nv) -> chkSelect.setSelected(nv);
            item.selectedProperty().addListener(selectedListener);

            chkSelect.setOnAction(e -> item.setSelected(chkSelect.isSelected()));

            // Định dạng theo 3 loại bong bóng
            metaRow.getChildren().clear();
            bubbleContainer.getChildren().clear();
            rootBox.getChildren().clear();

            if (item.isOutgoing()) {
                // 1. Tin nhắn Gửi đi (Outgoing): Căn phải, màu xanh chủ đạo
                rootBox.setAlignment(Pos.CENTER_RIGHT);
                bubbleContainer.setAlignment(Pos.TOP_RIGHT);
                metaRow.setAlignment(Pos.CENTER_RIGHT);

                lblSender.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted;");
                lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
                metaRow.getChildren().addAll(lblTime, lblSender);

                lblContent.getStyleClass().setAll("chat-bubble-sent");
                // Bo góc 12px, góc dưới phải 3px
                lblContent.setStyle("-fx-background-radius: 12px 12px 3px 12px; -fx-line-spacing: 2px;");

                bubbleContainer.getChildren().addAll(metaRow, lblContent);

                // CheckBox nằm ở ngoài cùng bên trái khi bật multi-select
                rootBox.getChildren().addAll(chkSelect, bubbleContainer);
            } else if (item.isAi()) {
                // 2. Tin nhắn Trợ lý AI (Bot): Căn trái, nền tối viền tím 3px, huy hiệu [AI BOT]
                rootBox.setAlignment(Pos.CENTER_LEFT);
                bubbleContainer.setAlignment(Pos.TOP_LEFT);
                metaRow.setAlignment(Pos.CENTER_LEFT);

                avatarCircle.setFill(Color.web("#382760"));
                avatarCircle.setStroke(Color.web("#8B5CF6"));
                avatarCircle.setStrokeWidth(1.2);
                lblAvatarText.setText("🤖");

                lblSender.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #D8B4FE;");
                lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
                metaRow.getChildren().addAll(lblSender, lblAiTag, lblTime);

                lblContent.getStyleClass().setAll("chat-bubble-ai");
                // Bo góc 12px, góc dưới trái 3px, viền mép trái 3px tím
                lblContent.setStyle(
                    "-fx-background-color: #2B2738; " +
                    "-fx-border-color: transparent transparent transparent #8B5CF6; " +
                    "-fx-border-width: 0 0 0 3px; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px 12px 12px 3px; " +
                    "-fx-padding: 10px 14px; " +
                    "-fx-text-fill: -text-normal; " +
                    "-fx-font-size: 13.5px; " +
                    "-fx-line-spacing: 2px;"
                );

                bubbleContainer.getChildren().addAll(metaRow, lblContent);
                rootBox.getChildren().addAll(chkSelect, avatarPane, bubbleContainer);
            } else {
                // 3. Tin nhắn Nhận được (Received): Căn trái, màu thẻ tối #1E2128
                rootBox.setAlignment(Pos.CENTER_LEFT);
                bubbleContainer.setAlignment(Pos.TOP_LEFT);
                metaRow.setAlignment(Pos.CENTER_LEFT);

                avatarCircle.setFill(Color.web("#262932"));
                avatarCircle.setStroke(null);
                String initial = item.getSenderName().isEmpty() ? "?" : item.getSenderName().substring(0, 1).toUpperCase();
                lblAvatarText.setText(initial);

                lblSender.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
                lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
                metaRow.getChildren().addAll(lblSender, lblTime);

                lblContent.getStyleClass().setAll("chat-bubble-received");
                // Bo góc 12px, góc dưới trái 3px
                lblContent.setStyle(
                    "-fx-background-color: #1E2128; " +
                    "-fx-border-color: -border-subtle; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-background-radius: 12px 12px 12px 3px; " +
                    "-fx-padding: 10px 14px; " +
                    "-fx-text-fill: -text-normal; " +
                    "-fx-font-size: 13.5px; " +
                    "-fx-line-spacing: 2px;"
                );

                bubbleContainer.getChildren().addAll(metaRow, lblContent);
                rootBox.getChildren().addAll(chkSelect, avatarPane, bubbleContainer);
            }

            // Gán hiệu ứng chớp màu nền vàng hổ phách 1500ms (Task U9)
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
                    "-fx-padding: 4px 12px;"
                );
            } else {
                setStyle("-fx-background-color: transparent; -fx-padding: 4px 12px;");
            }
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            // Ngăn chặn thanh màu xanh mặc định của JavaFX ListView che phủ giao diện
            if (getItem() != null && getItem().getHighlightIntensity() > 0.005) {
                applyHighlightIntensity(getItem().getHighlightIntensity());
            } else {
                setStyle("-fx-background-color: transparent; -fx-padding: 4px 12px;");
            }
        }
    }
}
