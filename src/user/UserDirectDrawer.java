package user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Ngăn kéo Cột 4: Thông tin Người chat & Quản lý Hội thoại 1-1 (Task U8, U10 — UserDirectDrawer)
 * Triển khai theo chuẩn docs/ui-spec/base-ui-spec.md:
 * - Chiều rộng cố định 280px, chỉ có viền trái (Single Border Source: 1px solid #2A2D35)
 * - Nút ✕ đóng ngăn kéo
 * - Hiển thị Avatar kích thước lớn, Trạng thái Online/Offline, Tên, Username
 * - Các hành động: Báo cáo vi phạm (Mục 10), Xóa toàn bộ lịch sử (Mục 11.c), Chặn người dùng (Mục 8.d)
 */
public class UserDirectDrawer extends VBox {

    private final Stage primaryStage;
    private final Runnable onClose;
    private final Runnable onReportSpam;
    private final Runnable onClearHistory;
    private final Runnable onBlockUser;

    private final Circle avatarCircle = new Circle(32, Color.web("#4E6BF2"));
    private final Label lblInitials = new Label("CH");
    private final Circle statusDot = new Circle(6, Color.web("#23A55A"));
    private final Label lblFullName = new Label("Lưu Chí Hải");
    private final Label lblUsername = new Label("@chihai_2026");
    private final Label lblStatusText = new Label("● Đang hoạt động");

    private final Label lblEmailVal = new Label("chihai@student.fit.hcmus.edu.vn");
    private final Label lblRoleVal = new Label("Thành viên A (Admin & DB)");

    public UserDirectDrawer(Stage primaryStage, Runnable onClose, Runnable onReportSpam, Runnable onClearHistory, Runnable onBlockUser) {
        this.primaryStage = primaryStage;
        this.onClose = onClose;
        this.onReportSpam = onReportSpam;
        this.onClearHistory = onClearHistory;
        this.onBlockUser = onBlockUser;

        setPrefWidth(280);
        setMinWidth(280);
        setMaxWidth(280);
        getStyleClass().add("right-drawer");
        setStyle("-fx-background-color: -bg-card; -fx-border-color: transparent transparent transparent -border-subtle; -fx-border-width: 0 0 0 1px;");

        buildUI();
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

        Label lblTitle = new Label("Thông tin người chat");
        lblTitle.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button();
        btnClose.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_CLOSE, 0.75, "#8E929B"));
        btnClose.setOnAction(e -> {
            if (onClose != null) onClose.run();
        });
        JavaFXTheme.applyCloseButton(btnClose);

        topRow.getChildren().addAll(lblTitle, spacer, btnClose);

        // 2. Avatar & Thẻ hồ sơ cá nhân
        VBox profileCard = new VBox(8);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPadding(new Insets(10, 0, 10, 0));

        lblInitials.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        statusDot.setStroke(Color.web("#1E2128"));
        statusDot.setStrokeWidth(2);

        StackPane avtBox = new StackPane(avatarCircle, lblInitials, statusDot);
        StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(statusDot, new Insets(0, 4, 4, 0));

        lblFullName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        lblUsername.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        lblStatusText.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #23A55A; -fx-font-weight: bold;");

        profileCard.getChildren().addAll(avtBox, lblFullName, lblUsername, lblStatusText);

        // 3. Khối thông tin chi tiết
        VBox infoSection = new VBox(8);
        infoSection.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px;");

        Label lblInfoTitle = new Label("THÔNG TIN CHI TIẾT");
        lblInfoTitle.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-letter-spacing: 0.5px;");

        Label lblEmailTitle = new Label("Email liên hệ:");
        lblEmailTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        lblEmailVal.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-normal;");
        lblEmailVal.setWrapText(true);

        Label lblRoleTitle = new Label("Vai trò:");
        lblRoleTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        lblRoleVal.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-normal;");

        HBox secBox = new HBox(6);
        secBox.setAlignment(Pos.CENTER_LEFT);
        secBox.setPadding(new Insets(4, 0, 0, 0));
        Label lblSecText = new Label("Hội thoại bảo mật AES-GCM 256-bit");
        lblSecText.setStyle("-fx-font-size: 11px; -fx-text-fill: #3B82F6;");
        secBox.getChildren().addAll(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_LOCK, 0.75, "#3B82F6"), lblSecText);

        infoSection.getChildren().addAll(lblInfoTitle, new Separator(), lblEmailTitle, lblEmailVal, lblRoleTitle, lblRoleVal, secBox);

        // 4. Khối hành động tương tác
        VBox actionSection = new VBox(6);
        actionSection.setPadding(new Insets(8, 0, 0, 0));

        Label lblActionTitle = new Label("TÙY CHỌN HỘI THOẠI");
        lblActionTitle.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-letter-spacing: 0.5px;");

        Button btnReport = createActionButton(
            "Báo cáo vi phạm / Spam (Mục 10)",
            JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_REPORT, 0.8, "#F59E0B"),
            "drawer-action-btn",
            () -> {
                if (onReportSpam != null) onReportSpam.run();
            }
        );

        Button btnClear = createActionButton(
            "Xóa toàn bộ lịch sử chat (Mục 11.c)",
            JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_TRASH, 0.8, "#FA777C"),
            "drawer-danger-btn",
            () -> {
                boolean ok = JavaFXTheme.showDestructiveConfirmDialog(
                    primaryStage,
                    "Xác nhận xóa lịch sử",
                    "Xóa toàn bộ tin nhắn phía bạn? (Mục 11.c)",
                    "Hành động này sẽ cập nhật Watermark và ẩn toàn bộ tin nhắn cũ khỏi khung nhìn của bạn. Thao tác không thể hoàn tác.",
                    "Xóa lịch sử"
                );
                if (ok && onClearHistory != null) {
                    onClearHistory.run();
                }
            }
        );

        Button btnBlock = createActionButton(
            "Chặn người dùng (Mục 8.d)",
            JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_BAN, 0.8, "#FA777C"),
            "drawer-danger-btn",
            () -> {
                boolean ok = JavaFXTheme.showDestructiveConfirmDialog(
                    primaryStage,
                    "Xác nhận chặn",
                    "Chặn người dùng " + lblFullName.getText() + "? (Mục 8.d)",
                    "Sau khi chặn, người này sẽ không thể tìm thấy bạn hoặc gửi tin nhắn cho bạn. Bạn có thể mở khóa bất cứ lúc nào.",
                    "Chặn người dùng"
                );
                if (ok && onBlockUser != null) {
                    onBlockUser.run();
                }
            }
        );

        actionSection.getChildren().addAll(lblActionTitle, btnReport, btnClear, btnBlock);

        content.getChildren().addAll(topRow, profileCard, infoSection, actionSection);
        scrollPane.setContent(content);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().setAll(scrollPane);
    }

    private Button createActionButton(String text, javafx.scene.Node graphic, String styleClass, Runnable action) {
        Button btn = new Button(text);
        if (graphic != null) {
            btn.setGraphic(graphic);
            btn.setGraphicTextGap(8);
        }
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add(styleClass);
        btn.setOnAction(e -> {
            if (action != null) action.run();
        });
        JavaFXTheme.applyTactileButton(btn);
        return btn;
    }

    public void setUser(String name, String username, String email, String bio, boolean isOnline, String color, String initials) {
        lblFullName.setText(name);
        lblUsername.setText(username);
        lblEmailVal.setText(email != null && !email.isEmpty() ? email : "chưa cập nhật");
        lblRoleVal.setText(bio != null && !bio.isEmpty() ? bio : "Thành viên đồ án");

        lblInitials.setText(initials != null ? initials : (name.length() >= 2 ? name.substring(0, 2).toUpperCase() : "U"));
        avatarCircle.setFill(Color.web(color != null ? color : "#4E6BF2"));

        if (isOnline) {
            statusDot.setFill(Color.web("#23A55A"));
            lblStatusText.setText("● Đang hoạt động");
            lblStatusText.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #23A55A; -fx-font-weight: bold;");
        } else {
            statusDot.setFill(Color.web("#8E929B"));
            lblStatusText.setText("○ Ngoại tuyến");
            lblStatusText.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8E929B; -fx-font-weight: normal;");
        }
    }
}
