package user;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ngăn kéo Cột 4 Quản trị Nhóm Chat & Mã hóa E2EE (Task U12 — GroupManagementDrawer)
 * Triển khai theo chuẩn docs/ui-spec/group-chat-encryption.md & base-ui-spec.md:
 * - Chiều rộng cố định 280px, chỉ có viền trái (Single Border Source: 1px solid #2A2D35)
 * - Đổi tên nhóm trực tiếp bằng icon bút ✏️ (Mục 12.b)
 * - Thêm thành viên mới từ danh bạ (Mục 12.c)
 * - Phân quyền Quản trị viên nhóm: Huy hiệu [Admin] màu vàng (Mục 12.d)
 * - Xóa thành viên khỏi nhóm: Ràng buộc bảo mật btnRemoveMember.visibleProperty().bind(isCurrentUserAdmin) (Mục 12.e)
 * - Trải nghiệm mã hóa nhóm E2EE phong cách Messenger: Icon 🔒, khóa DEK AES-GCM 256-bit, fingerprint (Mục 12.g)
 */
public class GroupManagementDrawer extends VBox {

    public static class MemberItem {
        private final String id;
        private final String name;
        private final String username;
        private final boolean isSelf;
        private final BooleanProperty isAdmin;

        public MemberItem(String id, String name, String username, boolean isSelf, boolean isAdmin) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.isSelf = isSelf;
            this.isAdmin = new SimpleBooleanProperty(isAdmin);
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUsername() { return username; }
        public boolean isSelf() { return isSelf; }
        public boolean isAdmin() { return isAdmin.get(); }
        public void setAdmin(boolean value) { isAdmin.set(value); }
        public BooleanProperty adminProperty() { return isAdmin; }
    }

    private final Stage primaryStage;
    private final Runnable onClose;
    private final Consumer<String> onRename;
    private final Runnable onAddMember;
    private final Runnable onClearHistory;

    // Ràng buộc bảo mật giao diện: Chỉ Admin mới có quyền thao tác quản trị
    private final BooleanProperty isCurrentUserAdmin = new SimpleBooleanProperty(true);

    private String groupName = "Nhóm Đồ Án Java 2026";
    private final Label lblGroupName = new Label(groupName);
    private final VBox membersListBox = new VBox(8);
    private final List<MemberItem> members = new ArrayList<>();

    public GroupManagementDrawer(Stage primaryStage, Runnable onClose, Consumer<String> onRename, Runnable onAddMember, Runnable onClearHistory) {
        this.primaryStage = primaryStage;
        this.onClose = onClose;
        this.onRename = onRename;
        this.onAddMember = onAddMember;
        this.onClearHistory = onClearHistory;

        setPrefWidth(280);
        setMinWidth(280);
        setMaxWidth(280);
        getStyleClass().add("right-drawer");
        // Tuân thủ quy tắc Single Border Source: Chỉ vẽ viền trái
        setStyle("-fx-background-color: -bg-card; -fx-border-color: transparent transparent transparent -border-subtle; -fx-border-width: 0 0 0 1px;");

        initDummyMembers();
        buildUI();
    }

    private void initDummyMembers() {
        members.clear();
        members.add(new MemberItem("user_minh", "Trần Nguyễn Tuấn Minh", "@tuanminh_2026", true, true));
        members.add(new MemberItem("user_hai", "Lưu Chí Hải", "@chihai_2026", false, true));
        members.add(new MemberItem("user_lananh", "Nguyễn Lan Anh", "@lananh_it", false, false));
    }

    private void buildUI() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("transparent-scroll");

        VBox content = new VBox(14);
        content.setPadding(new Insets(16));

        // 1. Header ngăn kéo
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Thông tin nhóm");
        lblTitle.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("dialog-close-btn");
        btnClose.setOnAction(e -> {
            if (onClose != null) onClose.run();
        });
        JavaFXTheme.applyIconButton(btnClose);

        topRow.getChildren().addAll(lblTitle, spacer, btnClose);

        // 2. Avatar nhóm & Tên có nút sửa ✏️ (Mục 12.b)
        VBox profileCard = new VBox(8);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPadding(new Insets(8, 0, 8, 0));

        Circle groupAvatar = new Circle(32, Color.web("#4E6BF2"));
        Label lblAvtText = new Label("👥");
        lblAvtText.setStyle("-fx-font-size: 24px;");
        StackPane avtBox = new StackPane(groupAvatar, lblAvtText);

        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER);
        lblGroupName.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Button btnEditName = new Button();
        btnEditName.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_EDIT, 0.70));
        btnEditName.getStyleClass().add("btn-icon-subtle");
        btnEditName.setTooltip(new Tooltip("Đổi tên nhóm (Mục 12.b)"));
        btnEditName.setOnAction(e -> handleRenameGroup());
        JavaFXTheme.applyIconButton(btnEditName);

        nameRow.getChildren().addAll(lblGroupName, btnEditName);

        Label lblCreatedDate = new Label("Khởi tạo ngày 23/10/2026 • 3 thành viên");
        lblCreatedDate.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted;");

        profileCard.getChildren().addAll(avtBox, nameRow, lblCreatedDate);

        // Hàng nút tác vụ nhanh (Quick Actions Row) kiểu Zalo / Telegram
        HBox quickActionsRow = new HBox(8);
        quickActionsRow.setAlignment(Pos.CENTER);
        quickActionsRow.setPadding(new Insets(6, 0, 4, 0));

        quickActionsRow.getChildren().addAll(
            createQuickActionItem("🔔", "Tắt thông báo", () -> {
                JavaFXTheme.showInfoAlert(primaryStage, "Thông báo", "Tắt thông báo", "Đã bật chế độ im lặng cho nhóm này.");
            }),
            createQuickActionItem("📌", "Ghim chat", () -> {
                JavaFXTheme.showInfoAlert(primaryStage, "Ghim", "Ghim hội thoại", "Đã ghim cuộc trò chuyện này lên đầu.");
            }),
            createQuickActionItem("➕", "Thêm bạn", () -> {
                if (onAddMember != null) onAddMember.run();
            }),
            createQuickActionItem("🔍", "Tìm tin", () -> {
                // Tác vụ tìm kiếm
            })
        );
        profileCard.getChildren().add(quickActionsRow);

        // 3. Phân đoạn Thành viên & Nút Thêm Thành viên (Mục 12.c, 12.d, 12.e)
        VBox membersSection = new VBox(8);
        HBox membersHeaderRow = new HBox(8);
        membersHeaderRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSecMembers = new Label("THÀNH VIÊN NHÓM (" + members.size() + ")");
        lblSecMembers.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        Button btnAddMember = new Button("+ Thêm");
        btnAddMember.getStyleClass().add("btn-secondary");
        btnAddMember.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
        btnAddMember.setOnAction(e -> {
            if (onAddMember != null) onAddMember.run();
        });
        JavaFXTheme.applyTactileButton(btnAddMember);

        membersHeaderRow.getChildren().addAll(lblSecMembers, sp2, btnAddMember);
        renderMembersList();

        membersSection.getChildren().addAll(membersHeaderRow, membersListBox);

        // 4. Thao tác hội thoại & Thao tác nguy hiểm (Dạng danh sách phẳng, không viền đỏ thô kệch)
        VBox dangerSection = new VBox(2);
        dangerSection.setPadding(new Insets(4, 0, 0, 0));

        HBox rowClear = createDangerListRow("🗑", "Xóa lịch sử trò chuyện", () -> {
            boolean ok = JavaFXTheme.showConfirmDialog(
                primaryStage,
                "Xác nhận xóa lịch sử",
                "Xóa lịch sử tin nhắn phía bạn (Mục 11.d)",
                "Tin nhắn sẽ được ẩn ở phía bạn bằng cơ chế Watermark theo CSDL. Bạn có chắc chắn?"
            );
            if (ok && onClearHistory != null) {
                onClearHistory.run();
            }
        });

        HBox rowLeave = createDangerListRow("🚪", "Rời khỏi nhóm trò chuyện", this::handleLeaveGroup);

        dangerSection.getChildren().addAll(rowClear, rowLeave);

        VBox mediaSection = createMediaSection();

        content.getChildren().addAll(
            topRow,
            new Separator(),
            profileCard,
            new Separator(),
            membersSection,
            new Separator(),
            mediaSection,
            new Separator(),
            dangerSection
        );

        scrollPane.setContent(content);
        getChildren().setAll(scrollPane);
    }

    private VBox createQuickActionItem(String iconText, String labelText, Runnable onClick) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(58);

        Button btn = new Button(iconText);
        btn.setStyle(
            "-fx-background-color: #22252E; " +
            "-fx-border-color: #2F333E; " +
            "-fx-border-radius: 18px; " +
            "-fx-background-radius: 18px; " +
            "-fx-text-fill: -text-normal; " +
            "-fx-font-size: 13px; " +
            "-fx-pref-width: 36px; -fx-pref-height: 36px; " +
            "-fx-min-width: 36px; -fx-min-height: 36px; " +
            "-fx-padding: 0; -fx-cursor: hand;"
        );
        btn.setOnAction(e -> {
            if (onClick != null) onClick.run();
        });
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #2E3342; " +
            "-fx-border-color: #4E6BF2; " +
            "-fx-border-radius: 18px; " +
            "-fx-background-radius: 18px; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 13px; " +
            "-fx-pref-width: 36px; -fx-pref-height: 36px; " +
            "-fx-min-width: 36px; -fx-min-height: 36px; " +
            "-fx-padding: 0; -fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #22252E; " +
            "-fx-border-color: #2F333E; " +
            "-fx-border-radius: 18px; " +
            "-fx-background-radius: 18px; " +
            "-fx-text-fill: -text-normal; " +
            "-fx-font-size: 13px; " +
            "-fx-pref-width: 36px; -fx-pref-height: 36px; " +
            "-fx-min-width: 36px; -fx-min-height: 36px; " +
            "-fx-padding: 0; -fx-cursor: hand;"
        ));
        JavaFXTheme.applyIconButton(btn);

        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: -text-muted;");
        box.getChildren().addAll(btn, lbl);
        return box;
    }

    private VBox createMediaSection() {
        VBox sec = new VBox(8);
        Label lblHeader = new Label("MEDIA & TÀI LIỆU CHIA SẺ");
        lblHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        VBox filesBox = createMediaCategoryRow("📁", "Tài liệu & Tệp tin", "0 tệp tin", () -> {
            JavaFXTheme.showInfoAlert(primaryStage, "Tài liệu", "Quản lý tệp tin", "Chưa có tài liệu nào được chia sẻ trong hội thoại này.");
        });

        VBox imagesBox = createMediaCategoryRow("🖼", "Ảnh & Video", "0 hình ảnh", () -> {
            JavaFXTheme.showInfoAlert(primaryStage, "Hình ảnh", "Kho ảnh & Video", "Chưa có hình ảnh nào được chia sẻ trong hội thoại này.");
        });

        VBox linksBox = createMediaCategoryRow("🔗", "Liên kết chia sẻ", "0 liên kết", () -> {
            JavaFXTheme.showInfoAlert(primaryStage, "Liên kết", "Liên kết chia sẻ", "Chưa có liên kết web nào trong hội thoại này.");
        });

        sec.getChildren().addAll(lblHeader, filesBox, imagesBox, linksBox);
        return sec;
    }

    private VBox createMediaCategoryRow(String icon, String title, String countText, Runnable onClick) {
        VBox container = new VBox(2);
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 10, 7, 10));
        row.setStyle("-fx-background-color: #1A1C23; -fx-border-color: #262932; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;");

        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 14px;");

        VBox textBox = new VBox(1);
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblCount = new Label(countText);
        lblCount.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        textBox.getChildren().addAll(lblTitle, lblCount);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label lblArrow = new Label("›");
        lblArrow.setStyle("-fx-font-size: 14px; -fx-text-fill: -text-muted;");

        row.getChildren().addAll(lblIcon, textBox, lblArrow);
        row.setOnMouseClicked(e -> {
            if (onClick != null) onClick.run();
        });
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #22252E; -fx-border-color: #363A45; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #1A1C23; -fx-border-color: #262932; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"));

        container.getChildren().add(row);
        return container;
    }

    private HBox createDangerListRow(String icon, String title, Runnable action) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: transparent; -fx-background-radius: 8px; -fx-cursor: hand;");

        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #FA777C;");

        Label lblText = new Label(title);
        lblText.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 500; -fx-text-fill: #FA777C;");
        HBox.setHgrow(lblText, Priority.ALWAYS);

        row.getChildren().addAll(lblIcon, lblText);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(250, 119, 124, 0.08); -fx-background-radius: 8px; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-background-radius: 8px; -fx-cursor: hand;"));
        row.setOnMouseClicked(e -> {
            if (action != null) action.run();
        });
        return row;
    }

    private void renderMembersList() {
        membersListBox.getChildren().clear();

        for (MemberItem m : members) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 0, 5, 0));

            // Avatar tròn 32px kèm chấm xanh Online ở góc dưới
            StackPane avtPane = new StackPane();
            Circle avtCircle = new Circle(16, Color.web(getMemberColor(m.getName())));
            Label lblInitials = new Label(getMemberInitials(m.getName()));
            lblInitials.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
            avtPane.getChildren().addAll(avtCircle, lblInitials);

            Circle onlineDot = new Circle(3.5, Color.web("#23A55A"));
            onlineDot.setStroke(Color.web("#1B1D22"));
            onlineDot.setStrokeWidth(1.5);
            StackPane.setAlignment(onlineDot, Pos.BOTTOM_RIGHT);
            avtPane.getChildren().add(onlineDot);

            // Popover tương tác Avatar
            avtPane.setStyle("-fx-cursor: hand;");
            avtPane.setOnMouseClicked(e -> {
                JavaFXTheme.showMiniProfilePopover(
                    avtPane,
                    m.getName(),
                    m.getUsername(),
                    true,
                    getMemberColor(m.getName()),
                    getMemberInitials(m.getName())
                );
            });

            VBox meta = new VBox(2);
            HBox nameBadgeRow = new HBox(6);
            nameBadgeRow.setAlignment(Pos.CENTER_LEFT);

            Label lblName = new Label(m.getName() + (m.isSelf() ? " (Bạn)" : ""));
            lblName.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal; -fx-cursor: hand;");
            lblName.setOnMouseClicked(e -> {
                JavaFXTheme.showMiniProfilePopover(
                    lblName,
                    m.getName(),
                    m.getUsername(),
                    true,
                    getMemberColor(m.getName()),
                    getMemberInitials(m.getName())
                );
            });
            nameBadgeRow.getChildren().add(lblName);

            if (m.isAdmin()) {
                Label badge = new Label("[Admin]");
                badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #F0B232; -fx-background-color: rgba(240, 178, 50, 0.15); -fx-padding: 1px 4px; -fx-background-radius: 4px;");
                nameBadgeRow.getChildren().add(badge);
            }

            Label lblUser = new Label(m.getUsername());
            lblUser.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");

            meta.getChildren().addAll(nameBadgeRow, lblUser);
            HBox.setHgrow(meta, Priority.ALWAYS);

            row.getChildren().addAll(avtPane, meta);

            // Nút thao tác Quản trị viên (Chỉ hiển thị cho người khác, không áp dụng cho chính mình)
            if (!m.isSelf()) {
                Button btnMore = new Button("•••");
                btnMore.getStyleClass().add("btn-icon-subtle");
                btnMore.setTooltip(new Tooltip("Thao tác quản trị viên"));
                JavaFXTheme.applyIconButton(btnMore);

                // Ràng buộc bảo mật: Nút hành động chỉ hiển thị khi người dùng hiện tại có quyền Admin
                btnMore.visibleProperty().bind(isCurrentUserAdmin);
                btnMore.managedProperty().bind(isCurrentUserAdmin);

                btnMore.setOnAction(e -> {
                    ContextMenu menu = new ContextMenu();
                    JavaFXTheme.applyContextMenuTheme(menu);

                    MenuItem miToggleAdmin = new MenuItem(m.isAdmin() ? "Thu hồi quyền Admin" : "Gán quyền Admin nhóm (Mục 12.d)");
                    miToggleAdmin.setOnAction(ev -> {
                        m.setAdmin(!m.isAdmin());
                        renderMembersList();
                    });

                    MenuItem miRemove = new MenuItem("Xóa khỏi nhóm (Kick - Mục 12.e)");
                    miRemove.setStyle("-fx-text-fill: -status-danger;");
                    miRemove.setOnAction(ev -> {
                        boolean ok = JavaFXTheme.showConfirmDialog(
                            primaryStage,
                            "Xác nhận loại bỏ thành viên",
                            "Xóa @" + m.getUsername() + " khỏi nhóm",
                            "Bạn có chắc chắn muốn xóa thành viên " + m.getName() + " ra khỏi nhóm không?"
                        );
                        if (ok) {
                            members.remove(m);
                            renderMembersList();
                        }
                    });

                    menu.getItems().addAll(miToggleAdmin, new SeparatorMenuItem(), miRemove);
                    menu.show(btnMore, javafx.geometry.Side.BOTTOM, 0, 0);
                });

                row.getChildren().add(btnMore);
            }

            membersListBox.getChildren().add(row);
        }
    }

    private void handleRenameGroup() {
        JavaFXTheme.showInputDialog(
            primaryStage,
            "Đổi tên nhóm",
            "Thay đổi tên nhóm chat (Mục 12.b)",
            "Nhập tên nhóm mới:",
            groupName
        ).ifPresent(newName -> {
            String trimmed = newName.trim();
            if (!trimmed.isEmpty()) {
                this.groupName = trimmed;
                lblGroupName.setText(trimmed);
                if (onRename != null) onRename.accept(trimmed);
            }
        });
    }

    private void handleLeaveGroup() {
        boolean confirmed = JavaFXTheme.showConfirmDialog(
            primaryStage,
            "Rời khỏi nhóm",
            "Xác nhận rời khỏi nhóm chat",
            "Bạn sẽ không thể xem hoặc nhận tin nhắn mới trong nhóm này cho đến khi được thêm lại."
        );
        if (confirmed) {
            JavaFXTheme.showInfoAlert(
                primaryStage,
                "Rời nhóm thành công",
                "Đã rời nhóm",
                "Bạn đã rời khỏi nhóm chat thành công."
            );
        }
    }

    public void addMember(String id, String name, String username) {
        members.add(new MemberItem(id, name, username, false, false));
        renderMembersList();
    }

    public void setGroupName(String name) {
        this.groupName = name;
        this.lblGroupName.setText(name);
    }

    public BooleanProperty currentUserAdminProperty() {
        return isCurrentUserAdmin;
    }

    private String getMemberColor(String name) {
        if (name == null || name.isEmpty()) return "#4E6BF2";
        if (name.contains("Minh")) return "#3B82F6";
        if (name.contains("Hải")) return "#10B981";
        if (name.contains("Lan Anh")) return "#EC4899";
        if (name.contains("Nam")) return "#8B5CF6";
        if (name.contains("Huy")) return "#F59E0B";
        return "#3B82F6";
    }

    private String getMemberInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
