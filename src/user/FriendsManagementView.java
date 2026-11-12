package user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Trung tâm Quản lý Bạn bè & Danh bạ (Task U4, U5, U6, U7 — FriendsManagementView)
 * Triển khai đầy đủ 4 Tabs theo đặc tả docs/ui-spec/friends-search.md:
 * - Tab 1: Bạn bè đang Online (Mục 8)
 * - Tab 2: Tất cả bạn bè & Hủy kết bạn, Chặn (Mục 6, 6.c, 6.d)
 * - Tab 3: Lời mời kết bạn đang chờ & Đồng ý/Từ chối (Mục 7)
 * - Tab 4: Thêm bạn & 3 nút thao tác độc lập với người lạ (Mục 6.b, 9)
 */
public class FriendsManagementView extends BorderPane {

    private final Stage ownerStage;
    private final Consumer<String> onOpenChatWithUser;
    private final Consumer<String> onCreateGroupWithUser;

    // Tabs
    private final Button btnTabOnline = new Button("🟢 Đang Online (3)");
    private final Button btnTabAll = new Button("👥 Tất cả (5)");
    private final Button btnTabPending = new Button("📩 Lời mời (2)");
    private final Button btnTabAdd = new Button("➕ Thêm bạn mới");

    private final TextField txtFilter = new TextField();
    private final VBox contentList = new VBox(8);
    private final ScrollPane scrollPane = new ScrollPane();
    private String currentTab = "online";

    // Danh sách dữ liệu mẫu theo ma trận dummy-data-matrix.md
    private final List<FriendModel> friendsList = new ArrayList<>();
    private final List<PendingRequestModel> pendingList = new ArrayList<>();
    private final List<StrangerModel> strangersList = new ArrayList<>();

    public FriendsManagementView(Stage ownerStage, Consumer<String> onOpenChatWithUser, Consumer<String> onCreateGroupWithUser) {
        this.ownerStage = ownerStage;
        this.onOpenChatWithUser = onOpenChatWithUser;
        this.onCreateGroupWithUser = onCreateGroupWithUser;

        getStyleClass().add("ambient-canvas");
        setPadding(new Insets(20, 24, 20, 24));

        initDummyData();
        buildTopBar();
        buildCenterArea();

        // Mặc định hiển thị Tab Online
        switchTab("online");
    }

    private void initDummyData() {
        // 1. Danh sách bạn bè hiện tại (Mục 6 & 8)
        friendsList.clear();
        friendsList.add(new FriendModel("user_chihai", "Lưu Chí Hải", "@chihai_2026", true, "Lập trình viên Backend • Admin"));
        friendsList.add(new FriendModel("user_lananh", "Nguyễn Lan Anh", "@lananh_it", true, "Đồ án Java 2026 • Tester"));
        friendsList.add(new FriendModel("user_namle", "Lê Hoàng Nam", "@nam_le", true, "Bạn cùng lớp CNTT"));
        friendsList.add(new FriendModel("user_duchuy", "Phạm Đức Huy", "@duc_huy", false, "Ngoại tuyến (Offline)"));
        friendsList.add(new FriendModel("user_thuha", "Vũ Thu Hà", "@thu_ha", false, "Ngoại tuyến (Offline)"));

        // 2. Lời mời kết bạn đang chờ (Mục 7)
        pendingList.clear();
        pendingList.add(new PendingRequestModel("user_quocbao", "Đặng Quốc Bảo", "@quoc_bao", "Gửi lời mời 10 phút trước"));
        pendingList.add(new PendingRequestModel("user_myduyen", "Trần Mỹ Duyên", "@my_duyen", "Gửi lời mời hôm qua"));

        // 3. Người lạ tìm kiếm được (Mục 6.b & 9)
        strangersList.clear();
        strangersList.add(new StrangerModel("user_long", "Nguyễn Hoàng Long", "@hoang_long", "Sinh viên Kỹ thuật Phần mềm • Thích chơi cầu lông"));
        strangersList.add(new StrangerModel("user_ngoc", "Trần Bảo Ngọc", "@bao_ngoc", "Thành viên CLB Lập trình Java • Designer"));
    }

    private void buildTopBar() {
        VBox topBox = new VBox(14);

        // Header Title
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Trung tâm Quản lý Bạn bè & Danh bạ");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        headerRow.getChildren().add(lblTitle);

        // Segmented Tabs Bar
        HBox tabsBar = new HBox(8);
        tabsBar.setAlignment(Pos.CENTER_LEFT);
        tabsBar.getStyleClass().add("settings-tab-bar");

        btnTabOnline.getStyleClass().add("settings-tab-btn");
        btnTabAll.getStyleClass().add("settings-tab-btn");
        btnTabPending.getStyleClass().add("settings-tab-btn");
        btnTabAdd.getStyleClass().add("settings-tab-btn");

        btnTabOnline.setOnAction(e -> switchTab("online"));
        btnTabAll.setOnAction(e -> switchTab("all"));
        btnTabPending.setOnAction(e -> switchTab("pending"));
        btnTabAdd.setOnAction(e -> switchTab("add"));

        tabsBar.getChildren().addAll(btnTabOnline, btnTabAll, btnTabPending, btnTabAdd);

        // Filter / Search Input
        txtFilter.getStyleClass().add("sidebar-search-field");
        txtFilter.setPrefHeight(38);
        txtFilter.textProperty().addListener((obs, oldV, newV) -> renderCurrentTab());

        topBox.getChildren().addAll(headerRow, tabsBar, txtFilter, new Separator());
        setTop(topBox);
    }

    private void buildCenterArea() {
        contentList.setPadding(new Insets(12, 0, 12, 0));
        scrollPane.setContent(contentList);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("transparent-scroll");
        setCenter(scrollPane);
    }

    public void switchTab(String tabId) {
        this.currentTab = tabId;

        btnTabOnline.getStyleClass().remove("settings-tab-btn-active");
        btnTabAll.getStyleClass().remove("settings-tab-btn-active");
        btnTabPending.getStyleClass().remove("settings-tab-btn-active");
        btnTabAdd.getStyleClass().remove("settings-tab-btn-active");

        if ("online".equals(tabId)) {
            btnTabOnline.getStyleClass().add("settings-tab-btn-active");
            txtFilter.setPromptText("🔍 Lọc bạn bè đang Online theo họ tên hoặc @username...");
        } else if ("all".equals(tabId)) {
            btnTabAll.getStyleClass().add("settings-tab-btn-active");
            txtFilter.setPromptText("🔍 Tìm kiếm trong toàn bộ bạn bè...");
        } else if ("pending".equals(tabId)) {
            btnTabPending.getStyleClass().add("settings-tab-btn-active");
            txtFilter.setPromptText("🔍 Lọc danh sách lời mời kết bạn...");
        } else if ("add".equals(tabId)) {
            btnTabAdd.getStyleClass().add("settings-tab-btn-active");
            txtFilter.setPromptText("🔍 Nhập họ tên hoặc @username người lạ để tìm kiếm (Mục 6.b & 9)...");
        }

        renderCurrentTab();
        JavaFXTheme.applyTabContentTransition(contentList, true);
    }

    private void renderCurrentTab() {
        contentList.getChildren().clear();
        String query = txtFilter.getText().trim().toLowerCase();

        if ("online".equals(currentTab)) {
            renderOnlineTab(query);
        } else if ("all".equals(currentTab)) {
            renderAllTab(query);
        } else if ("pending".equals(currentTab)) {
            renderPendingTab(query);
        } else if ("add".equals(currentTab)) {
            renderAddTab(query);
        }
    }

    /* --------------------------------------------------------------------------
       TAB 1: BẠN BÈ ĐANG ONLINE (MỤC 8)
       -------------------------------------------------------------------------- */
    private void renderOnlineTab(String query) {
        List<FriendModel> filtered = friendsList.stream()
            .filter(FriendModel::isOnline)
            .filter(f -> matchesQuery(f.name, f.tag, query))
            .collect(Collectors.toList());

        btnTabOnline.setText("🟢 Đang Online (" + filtered.size() + ")");

        if (filtered.isEmpty()) {
            contentList.getChildren().add(createEmptyState("Không tìm thấy bạn bè nào đang trực tuyến."));
            return;
        }

        for (FriendModel friend : filtered) {
            contentList.getChildren().add(createFriendRowNode(friend, true));
        }
    }

    /* --------------------------------------------------------------------------
       TAB 2: TẤT CẢ BẠN BÈ & HỦY KẾT BẠN / CHẶN (MỤC 6, 6.c, 6.d)
       -------------------------------------------------------------------------- */
    private void renderAllTab(String query) {
        List<FriendModel> filtered = friendsList.stream()
            .filter(f -> matchesQuery(f.name, f.tag, query))
            .collect(Collectors.toList());

        btnTabAll.setText("👥 Tất cả (" + friendsList.size() + ")");

        if (filtered.isEmpty()) {
            contentList.getChildren().add(createEmptyState("Danh sách bạn bè trống hoặc không có kết quả phù hợp."));
            return;
        }

        for (FriendModel friend : filtered) {
            contentList.getChildren().add(createFriendRowNode(friend, false));
        }
    }

    private Node createFriendRowNode(FriendModel friend, boolean isOnlineTabOnly) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("friend-row-item");
        row.setPadding(new Insets(10, 16, 10, 16));

        // Avatar + Status Dot
        StackPane avtBox = new StackPane();
        Circle bg = new Circle(20, friend.isOnline ? Color.web("#23A55A") : Color.web("#4E6BF2"));
        Label lblI = new Label(friend.name.substring(0, Math.min(2, friend.name.length())).toUpperCase());
        lblI.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        avtBox.getChildren().addAll(bg, lblI);

        Circle dot = new Circle(4.5, friend.isOnline ? Color.web("#23A55A") : Color.web("#80848E"));
        dot.setStroke(Color.web("#141619"));
        dot.setStrokeWidth(2);
        StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
        avtBox.getChildren().add(dot);

        // Name & Tag & Subtitle
        VBox meta = new VBox(2);
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label lblName = new Label(friend.name);
        lblName.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Label lblTag = new Label(friend.tag);
        lblTag.setStyle("-fx-font-size: 12.5px; -fx-text-fill: -text-muted;");
        nameRow.getChildren().addAll(lblName, lblTag);

        Label lblSub = new Label(friend.subInfo + " • " + (friend.isOnline ? "Đang trực tuyến" : "Ngoại tuyến"));
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (friend.isOnline ? "-status-online;" : "-text-muted;"));

        meta.getChildren().addAll(nameRow, lblSub);
        HBox.setHgrow(meta, Priority.ALWAYS);

        // Action Buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnChat = new Button("💬 Nhắn tin");
        btnChat.getStyleClass().add("btn-secondary");
        btnChat.setOnAction(e -> {
            if (onOpenChatWithUser != null) {
                onOpenChatWithUser.accept(friend.id);
            }
        });
        JavaFXTheme.applyTactileButton(btnChat);

        Button btnGroup = new Button("➕ Tạo nhóm");
        btnGroup.getStyleClass().add("btn-secondary");
        btnGroup.setOnAction(e -> {
            if (onCreateGroupWithUser != null) {
                onCreateGroupWithUser.accept(friend.id);
            }
        });
        JavaFXTheme.applyTactileButton(btnGroup);

        Button btnMore = new Button("•••");
        btnMore.getStyleClass().add("btn-icon-subtle");
        JavaFXTheme.applyIconButton(btnMore);
        btnMore.setOnAction(e -> {
            ContextMenu cm = new ContextMenu();
            JavaFXTheme.applyContextMenuTheme(cm);

            MenuItem miUnfriend = new MenuItem("❌ Hủy kết bạn (Mục 6.c)");
            miUnfriend.setOnAction(ev -> handleUnfriend(friend));

            MenuItem miBlock = new MenuItem("⛔ Chặn người dùng (Mục 6.d)");
            miBlock.setStyle("-fx-text-fill: #FA777C;");
            miBlock.setOnAction(ev -> handleBlock(friend));

            cm.getItems().addAll(miUnfriend, miBlock);
            cm.show(btnMore, javafx.geometry.Side.BOTTOM, 0, 0);
        });

        actions.getChildren().addAll(btnChat, btnGroup, btnMore);
        row.getChildren().addAll(avtBox, meta, actions);
        return row;
    }

    private void handleUnfriend(FriendModel friend) {
        boolean ok = JavaFXTheme.showConfirmDialog(
            ownerStage,
            "Hủy kết bạn",
            "Hủy kết bạn với " + friend.name + " (" + friend.tag + ")?",
            "Hai bạn sẽ trở thành người lạ và không còn nằm trong danh sách bạn bè của nhau."
        );
        if (ok) {
            friendsList.remove(friend);
            renderCurrentTab();
        }
    }

    private void handleBlock(FriendModel friend) {
        boolean ok = JavaFXTheme.showConfirmDialog(
            ownerStage,
            "Chặn người dùng",
            "Chặn " + friend.name + " (" + friend.tag + ")?",
            "Sau khi chặn:\n- Người này không thể tìm thấy hoặc xem thông tin của bạn.\n- Không thể gửi tin nhắn hoặc lời mời kết bạn.\n- Bạn có thể mở khóa chặn bất cứ lúc nào trong mục Đã chặn."
        );
        if (ok) {
            friendsList.remove(friend);
            renderCurrentTab();
        }
    }

    /* --------------------------------------------------------------------------
       TAB 3: LỜI MỜI KẾT BẠN ĐANG CHỜ (MỤC 7)
       -------------------------------------------------------------------------- */
    private void renderPendingTab(String query) {
        List<PendingRequestModel> filtered = pendingList.stream()
            .filter(p -> matchesQuery(p.name, p.tag, query))
            .collect(Collectors.toList());

        btnTabPending.setText("📩 Lời mời (" + pendingList.size() + ")");

        if (filtered.isEmpty()) {
            contentList.getChildren().add(createEmptyState("Bạn không có lời mời kết bạn nào đang chờ xử lý."));
            return;
        }

        for (PendingRequestModel req : filtered) {
            contentList.getChildren().add(createPendingRowNode(req));
        }
    }

    private Node createPendingRowNode(PendingRequestModel req) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("friend-row-item");
        row.setPadding(new Insets(10, 16, 10, 16));

        Circle avt = new Circle(20, Color.web("#4E6BF2"));
        VBox meta = new VBox(2);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label lblName = new Label(req.name);
        lblName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblTag = new Label(req.tag);
        lblTag.setStyle("-fx-font-size: 12.5px; -fx-text-fill: -text-muted;");
        titleRow.getChildren().addAll(lblName, lblTag);

        Label lblTime = new Label(req.timeInfo);
        lblTime.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted;");
        meta.getChildren().addAll(titleRow, lblTime);
        HBox.setHgrow(meta, Priority.ALWAYS);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnAccept = new Button("Đồng ý ✓");
        btnAccept.getStyleClass().add("btn-primary");
        btnAccept.setOnAction(e -> {
            pendingList.remove(req);
            friendsList.add(new FriendModel(req.id, req.name, req.tag, true, "Vừa trở thành bạn bè"));
            renderCurrentTab();
            JavaFXTheme.showInfoAlert(
                ownerStage,
                "Kết bạn thành công",
                null,
                "✓ Bạn và " + req.name + " đã trở thành bạn bè!"
            );
        });
        JavaFXTheme.applyTactileButton(btnAccept);

        Button btnReject = new Button("Từ chối ✕");
        btnReject.getStyleClass().add("btn-secondary");
        btnReject.setOnAction(e -> {
            pendingList.remove(req);
            renderCurrentTab();
        });
        JavaFXTheme.applyTactileButton(btnReject);

        actions.getChildren().addAll(btnAccept, btnReject);
        row.getChildren().addAll(avt, meta, actions);
        return row;
    }

    /* --------------------------------------------------------------------------
       TAB 4: THÊM BẠN / TÌM KIẾM NGƯỜI LẠ (MỤC 6.b & MỤC 9)
       -------------------------------------------------------------------------- */
    private void renderAddTab(String query) {
        VBox addContainer = new VBox(14);

        Label lblGuide = new Label("Tìm kiếm người dùng trong hệ thống để kết bạn, nhắn tin trực tiếp hoặc tạo nhóm mới:");
        lblGuide.setStyle("-fx-font-size: 13.5px; -fx-text-fill: -text-muted;");
        addContainer.getChildren().add(lblGuide);

        List<StrangerModel> filtered = strangersList.stream()
            .filter(s -> matchesQuery(s.name, s.tag, query))
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            addContainer.getChildren().add(createEmptyState("Không tìm thấy người dùng nào phù hợp với từ khóa '" + query + "'."));
        } else {
            for (StrangerModel stranger : filtered) {
                addContainer.getChildren().add(createStrangerCardNode(stranger));
            }
        }

        contentList.getChildren().add(addContainer);
    }

    private Node createStrangerCardNode(StrangerModel stranger) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("friends-list-card");
        card.setPadding(new Insets(14, 18, 14, 18));

        Circle avt = new Circle(22, Color.web("#6C5CE7"));
        VBox info = new VBox(3);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label lblN = new Label(stranger.name);
        lblN.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblT = new Label(stranger.tag);
        lblT.setStyle("-fx-font-size: 13px; -fx-text-fill: -text-muted;");
        titleRow.getChildren().addAll(lblN, lblT);

        Label lblBio = new Label(stranger.bio);
        lblBio.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        info.getChildren().addAll(titleRow, lblBio);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Đủ 3 nút thao tác độc lập theo đúng Mục 6.b & Mục 9 đề bài:
        HBox actionRow = new HBox(8);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnAdd = new Button(stranger.isRequested ? "✓ Đã gửi lời mời" : "+ Kết bạn (Mục 6.b)");
        btnAdd.getStyleClass().add(stranger.isRequested ? "btn-secondary" : "btn-primary");
        btnAdd.setDisable(stranger.isRequested);
        btnAdd.setOnAction(e -> {
            stranger.isRequested = true;
            btnAdd.setText("✓ Đã gửi lời mời");
            btnAdd.setDisable(true);
        });
        JavaFXTheme.applyTactileButton(btnAdd);

        Button btnChatNow = new Button("💬 Nhắn tin ngay (Mục 9)");
        btnChatNow.getStyleClass().add("btn-secondary");
        btnChatNow.setOnAction(e -> {
            if (onOpenChatWithUser != null) {
                onOpenChatWithUser.accept(stranger.id);
            }
        });
        JavaFXTheme.applyTactileButton(btnChatNow);

        Button btnCreateGroup = new Button("👥 Tạo Group (Mục 9)");
        btnCreateGroup.getStyleClass().add("btn-secondary");
        btnCreateGroup.setOnAction(e -> {
            if (onCreateGroupWithUser != null) {
                onCreateGroupWithUser.accept(stranger.id);
            }
        });
        JavaFXTheme.applyTactileButton(btnCreateGroup);

        actionRow.getChildren().addAll(btnAdd, btnChatNow, btnCreateGroup);
        card.getChildren().addAll(avt, info, actionRow);
        return card;
    }

    private Node createEmptyState(String text) {
        VBox empty = new VBox(8);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(40, 20, 40, 20));

        Label icon = new Label("📭");
        icon.setStyle("-fx-font-size: 32px;");

        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13.5px; -fx-text-fill: -text-muted;");

        empty.getChildren().addAll(icon, lbl);
        return empty;
    }

    private boolean matchesQuery(String name, String tag, String query) {
        if (query.isEmpty()) return true;
        return name.toLowerCase().contains(query) || tag.toLowerCase().contains(query);
    }

    // Static Data Models
    public static class FriendModel {
        public final String id;
        public final String name;
        public final String tag;
        public final boolean isOnline;
        public final String subInfo;

        public FriendModel(String id, String name, String tag, boolean isOnline, String subInfo) {
            this.id = id;
            this.name = name;
            this.tag = tag;
            this.isOnline = isOnline;
            this.subInfo = subInfo;
        }

        public boolean isOnline() {
            return isOnline;
        }
    }

    public static class PendingRequestModel {
        public final String id;
        public final String name;
        public final String tag;
        public final String timeInfo;

        public PendingRequestModel(String id, String name, String tag, String timeInfo) {
            this.id = id;
            this.name = name;
            this.tag = tag;
            this.timeInfo = timeInfo;
        }
    }

    public static class StrangerModel {
        public final String id;
        public final String name;
        public final String tag;
        public final String bio;
        public boolean isRequested = false;

        public StrangerModel(String id, String name, String tag, String bio) {
            this.id = id;
            this.name = name;
            this.tag = tag;
            this.bio = bio;
        }
    }
}
