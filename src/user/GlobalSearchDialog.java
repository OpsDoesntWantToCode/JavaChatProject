package user;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Hộp thoại Tìm kiếm Lịch sử Chat Toàn cục (Mục 11.g — Global Search Dialog)
 * "Tìm kiếm chuỗi trong lịch sử chat với tất cả mọi người, có thể di chuyển đến đoạn chat đang tìm"
 *
 * Tính năng chính:
 * - Tìm kiếm thời gian thực (real-time) xuyên suốt toàn bộ các cuộc trò chuyện (1-1 và Nhóm)
 * - Bộ lọc nhanh: Tất cả / Tin nhắn / Hội thoại
 * - Trích xuất đoạn hội thoại khớp từ khóa kèm hiệu ứng bôi đậm hổ phách #F0B232
 * - Khi nhấp vào kết quả: Tự động đóng hộp thoại, chuyển đổi hội thoại và cuộn đến đúng tin nhắn (highlight 1500ms)
 * - Hỗ trợ phím tắt Esc để thoát, Enter để di chuyển nhanh
 */
public class GlobalSearchDialog {

    private final Stage dialogStage;
    private final BiConsumer<String, String> onNavigate; // (chatId, messageId) -> navigate

    private final TextField txtSearchInput = new TextField();
    private final Button btnClear = new Button("✕");
    private final VBox resultsContainer = new VBox(8);
    private final ScrollPane scrollPane = new ScrollPane(resultsContainer);
    private final Label lblResultSummary = new Label("Nhập từ khóa để bắt đầu tìm kiếm trên toàn hệ thống...");

    // Filter Chips
    private String currentFilter = "all"; // all, messages, chats
    private final Button chipAll = new Button("Tất cả");
    private final Button chipMessages = new Button("Tin nhắn (11.g)");
    private final Button chipChats = new Button("Hội thoại");

    public GlobalSearchDialog(Stage ownerStage, String initialQuery, BiConsumer<String, String> onNavigate) {
        this.onNavigate = onNavigate;
        this.dialogStage = new Stage();
        this.dialogStage.initOwner(ownerStage);
        this.dialogStage.initModality(Modality.WINDOW_MODAL);
        this.dialogStage.setTitle("Tìm kiếm lịch sử chat với tất cả mọi người (Mục 11.g)");

        buildUI(initialQuery);
    }

    private void buildUI(String initialQuery) {
        VBox root = new VBox(14);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.getStyleClass().addAll("ambient-canvas", "auth-card");
        root.setStyle(
            "-fx-background-color: #141619; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-width: 1px;"
        );
        root.setPrefWidth(680);
        root.setPrefHeight(520);

        // 1. Header Box
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("🔍 Tìm kiếm tin nhắn toàn cục (Mục 11.g)");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("btn-icon-subtle");
        btnClose.setOnAction(e -> dialogStage.close());
        JavaFXTheme.applyIconButton(btnClose);

        headerBox.getChildren().addAll(lblTitle, spacerH, btnClose);

        // 2. Search Input Row
        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.setStyle(
            "-fx-background-color: #0F1013; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 6 12;"
        );

        Label lblSearchIcon = new Label("🔎");
        lblSearchIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-muted;");

        txtSearchInput.setPromptText("Nhập từ khóa tìm kiếm (ví dụ: database, báo cáo, icon, họp nhóm)...");
        txtSearchInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-prompt-text-fill: #6E7382;");
        HBox.setHgrow(txtSearchInput, Priority.ALWAYS);

        btnClear.getStyleClass().add("btn-icon-subtle");
        btnClear.setVisible(false);
        btnClear.setOnAction(e -> {
            txtSearchInput.clear();
            txtSearchInput.requestFocus();
        });

        txtSearchInput.textProperty().addListener((obs, oldV, newV) -> {
            boolean hasText = newV != null && !newV.trim().isEmpty();
            btnClear.setVisible(hasText);
            executeSearch(newV);
        });

        inputRow.getChildren().addAll(lblSearchIcon, txtSearchInput, btnClear);

        // 3. Filter Chips Row
        HBox filterRow = new HBox(8);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        setupFilterChip(chipAll, "all");
        setupFilterChip(chipMessages, "messages");
        setupFilterChip(chipChats, "chats");
        updateFilterStyles();

        lblResultSummary.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-padding: 0 4;");
        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        filterRow.getChildren().addAll(chipAll, chipMessages, chipChats, filterSpacer, lblResultSummary);

        // 4. Results Scroll Pane
        resultsContainer.setPadding(new Insets(4, 0, 4, 0));
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("transparent-scroll");
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // 5. Footer Shortcut Hints
        HBox footerBox = new HBox(16);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setPadding(new Insets(8, 0, 0, 0));
        footerBox.setStyle("-fx-border-color: #2A2D35 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Label lblHint = new Label("💡 Nhấp vào kết quả để di chuyển trực tiếp đến đoạn chat • Nhấn Esc để đóng");
        lblHint.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted;");
        footerBox.getChildren().add(lblHint);

        root.getChildren().addAll(headerBox, inputRow, filterRow, scrollPane, footerBox);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("theme.css").toExternalForm());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dialogStage.close();
            }
        });

        dialogStage.setScene(scene);

        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            txtSearchInput.setText(initialQuery.trim());
            Platform.runLater(() -> txtSearchInput.positionCaret(initialQuery.length()));
        } else {
            // Hiển thị gợi ý ban đầu
            showDefaultSuggestions();
        }
    }

    private void setupFilterChip(Button chip, String filterKey) {
        chip.getStyleClass().add("smart-reply-chip");
        chip.setOnAction(e -> {
            this.currentFilter = filterKey;
            updateFilterStyles();
            executeSearch(txtSearchInput.getText());
        });
        JavaFXTheme.applyTactileButton(chip);
    }

    private void updateFilterStyles() {
        String activeStyle = "-fx-background-color: #4E6BF2; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-padding: 4 12;";
        String inactiveStyle = "-fx-background-color: #1E2128; -fx-text-fill: #8E929B; -fx-background-radius: 12px; -fx-padding: 4 12; -fx-border-color: #2A2D35; -fx-border-radius: 12px;";

        chipAll.setStyle("all".equals(currentFilter) ? activeStyle : inactiveStyle);
        chipMessages.setStyle("messages".equals(currentFilter) ? activeStyle : inactiveStyle);
        chipChats.setStyle("chats".equals(currentFilter) ? activeStyle : inactiveStyle);
    }

    private void showDefaultSuggestions() {
        resultsContainer.getChildren().clear();
        lblResultSummary.setText("Gợi ý từ khóa phổ biến trong dự án");

        VBox suggestBox = new VBox(10);
        suggestBox.setPadding(new Insets(16));
        suggestBox.setAlignment(Pos.CENTER);
        suggestBox.setStyle("-fx-background-color: #181A1F; -fx-background-radius: 8px; -fx-border-color: #2A2D35; -fx-border-radius: 8px;");

        Label lblEmptyTitle = new Label("Tìm kiếm nội dung tin nhắn xuyên suốt tất cả cuộc trò chuyện");
        lblEmptyTitle.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Label lblEmptyDesc = new Label("Bạn có thể thử bấm vào các từ khóa mẫu bên dưới để xem kết quả tức thì:");
        lblEmptyDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");

        HBox sampleChips = new HBox(8);
        sampleChips.setAlignment(Pos.CENTER);
        String[] samples = {"báo cáo", "database", "icon", "họp nhóm", "AES-GCM", "đá bóng"};
        for (String sample : samples) {
            Button btnSample = new Button(sample);
            btnSample.getStyleClass().add("smart-reply-chip");
            btnSample.setOnAction(e -> txtSearchInput.setText(sample));
            sampleChips.getChildren().add(btnSample);
        }

        suggestBox.getChildren().addAll(lblEmptyTitle, lblEmptyDesc, sampleChips);
        resultsContainer.getChildren().add(suggestBox);
    }

    private void executeSearch(String keyword) {
        resultsContainer.getChildren().clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            showDefaultSuggestions();
            return;
        }

        String query = keyword.trim();
        List<ChatMockRepository.GlobalMessageMatch> messageMatches = ChatMockRepository.searchAllMessages(query);
        List<ChatMockRepository.ConversationMeta> chatMatches = ChatMockRepository.searchConversations(query);

        int totalFound = 0;
        if (!"messages".equals(currentFilter)) {
            totalFound += chatMatches.size();
        }
        if (!"chats".equals(currentFilter)) {
            totalFound += messageMatches.size();
        }

        lblResultSummary.setText("Tìm thấy " + totalFound + " kết quả cho \"" + query + "\"");

        if (totalFound == 0) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(32));

            Label lblNo = new Label("Không tìm thấy kết quả nào");
            lblNo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

            Label lblDesc = new Label("Không có cuộc trò chuyện hoặc tin nhắn nào chứa từ khóa \"" + query + "\".");
            lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");

            emptyBox.getChildren().addAll(lblNo, lblDesc);
            resultsContainer.getChildren().add(emptyBox);
            return;
        }

        // 1. Hiển thị Cuộc trò chuyện (nếu bộ lọc cho phép)
        if (!"messages".equals(currentFilter) && !chatMatches.isEmpty()) {
            Label lblSectionChats = new Label("CUỘC TRÒ CHUYỆN (" + chatMatches.size() + ")");
            lblSectionChats.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-padding: 6 4 2 4;");
            resultsContainer.getChildren().add(lblSectionChats);

            for (ChatMockRepository.ConversationMeta conv : chatMatches) {
                resultsContainer.getChildren().add(createConversationResultCard(conv));
            }
        }

        // 2. Hiển thị Tin nhắn Toàn cục (Mục 11.g)
        if (!"chats".equals(currentFilter) && !messageMatches.isEmpty()) {
            Label lblSectionMsgs = new Label("TIN NHẮN TOÀN CỤC — MỤC 11.G (" + messageMatches.size() + ")");
            lblSectionMsgs.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #F0B232; -fx-padding: 10 4 2 4;");
            resultsContainer.getChildren().add(lblSectionMsgs);

            for (ChatMockRepository.GlobalMessageMatch match : messageMatches) {
                resultsContainer.getChildren().add(createMessageResultCard(match, query));
            }
        }
    }

    private Node createConversationResultCard(ChatMockRepository.ConversationMeta conv) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle(
            "-fx-background-color: #1B1D22; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        );

        Circle avt = new Circle(16, Color.web(conv.getAvatarColor()));
        Label lblI = new Label(conv.getInitials());
        lblI.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        StackPane avtBox = new StackPane(avt, lblI);

        VBox info = new VBox(2);
        Label lblName = new Label((conv.isEncrypted() ? "🔒 " : "") + conv.getTitle());
        lblName.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Label lblSub = new Label(conv.getSnippet());
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        info.getChildren().addAll(lblName, lblSub);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label lblBadge = new Label(conv.isGroup() ? "Nhóm chat" : "Trò chuyện 1-1");
        lblBadge.setStyle("-fx-font-size: 10.5px; -fx-text-fill: -text-muted; -fx-background-color: #262932; -fx-padding: 2 8; -fx-background-radius: 6px;");

        card.getChildren().addAll(avtBox, info, lblBadge);

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #262932; " +
            "-fx-border-color: #4E6BF2; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #1B1D22; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        ));

        card.setOnMouseClicked(e -> {
            dialogStage.close();
            if (onNavigate != null) {
                onNavigate.accept(conv.getId(), null);
            }
        });

        return card;
    }

    private Node createMessageResultCard(ChatMockRepository.GlobalMessageMatch match, String query) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle(
            "-fx-background-color: #1B1D22; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        );

        // Top Row: Tên cuộc hội thoại + Thời gian + Nút chuyển đến
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4, Color.web(match.getAvatarColor()));
        Label lblChatName = new Label(match.getChatTitle());
        lblChatName.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #A0C0FF;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblTime = new Label(match.getMessage().getTimestamp());
        lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");

        Label lblJumpHint = new Label("Di chuyển đến đoạn chat ➜");
        lblJumpHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #4E6BF2; -fx-font-weight: bold;");

        topRow.getChildren().addAll(dot, lblChatName, sp, lblTime, lblJumpHint);

        // Bottom Row: Người gửi + Đoạn văn bản bôi đậm từ khóa
        HBox contentRow = new HBox(6);
        contentRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSender = new Label((match.getMessage().isOutgoing() ? "Bạn" : match.getMessage().getSenderName()) + ": ");
        lblSender.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        TextFlow flow = createHighlightedTextFlow(match.getMessage().getContent(), query);
        HBox.setHgrow(flow, Priority.ALWAYS);

        contentRow.getChildren().addAll(lblSender, flow);

        card.getChildren().addAll(topRow, contentRow);

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #242730; " +
            "-fx-border-color: #F0B232; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #1B1D22; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        ));

        card.setOnMouseClicked(e -> {
            dialogStage.close();
            if (onNavigate != null) {
                onNavigate.accept(match.getChatId(), match.getMessage().getId());
            }
        });

        return card;
    }

    private TextFlow createHighlightedTextFlow(String fullText, String keyword) {
        TextFlow flow = new TextFlow();
        if (fullText == null || keyword == null || keyword.isEmpty()) {
            Text t = new Text(fullText);
            t.setStyle("-fx-fill: #EDEDED; -fx-font-size: 13px;");
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
                normal.setStyle("-fx-fill: #D0D2DC; -fx-font-size: 13px;");
                flow.getChildren().add(normal);
            }

            Text highlighted = new Text(fullText.substring(idx, idx + keyword.length()));
            highlighted.setStyle("-fx-fill: #0F1013; -fx-font-weight: bold; -fx-font-size: 13px;");
            // Wrap in background effect via parent label or inline style
            Label badge = new Label(highlighted.getText());
            badge.setStyle("-fx-background-color: #F0B232; -fx-text-fill: #0F1013; -fx-font-weight: bold; -fx-padding: 0 3; -fx-background-radius: 3px; -fx-font-size: 12.5px;");
            flow.getChildren().add(badge);

            start = idx + keyword.length();
            idx = lowerFull.indexOf(lowerKey, start);
        }

        if (start < fullText.length()) {
            Text tail = new Text(fullText.substring(start));
            tail.setStyle("-fx-fill: #D0D2DC; -fx-font-size: 13px;");
            flow.getChildren().add(tail);
        }

        return flow;
    }

    public void show() {
        dialogStage.show();
    }

    public void showAndWait() {
        dialogStage.showAndWait();
    }
}
