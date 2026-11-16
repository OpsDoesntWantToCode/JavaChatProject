package user;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * Thanh Tiêu đề Tùy biến (Custom Title Bar) chuẩn Zalo PC & Fluent UI
 * - Thiết kế tối giản, đồng bộ hoàn hảo với Theme Obsidian Dark (-bg-nav-rail / -bg-card)
 * - Tích hợp đầy đủ các chức năng:
 *   + Kéo thả chuột để di chuyển cửa sổ (Draggable Window)
 *   + Nhấp đúp (Double-click) để phóng to / khôi phục (Maximize / Restore)
 *   + Cụm nút điều khiển cửa sổ kiểu Zalo PC sử dụng Vector Shapes / SVG sắc nét 100%,
 *     loại bỏ hoàn toàn hiện tượng nhòe viền ClearType (subpixel chromatic fringing) của ký tự Unicode.
 */
public class CustomTitleBar extends HBox {

    private final Stage stage;
    private final Label lblTitle = new Label();
    private final Button btnLock = new Button();
    private final Button btnMin = new Button();
    private final Button btnMax = new Button();
    private final Button btnClose = new Button();
    private final Runnable onLockAction;

    public CustomTitleBar(Stage stage, String titleText, Runnable onLockAction) {
        this.stage = stage;
        this.onLockAction = onLockAction;

        setPrefHeight(32);
        setMinHeight(32);
        setMaxHeight(32);
        setAlignment(Pos.CENTER_LEFT);
        setStyle(
            "-fx-background-color: -bg-nav-rail; " +
            "-fx-border-color: transparent transparent -border-subtle transparent; " +
            "-fx-border-width: 0 0 1px 0; " +
            "-fx-padding: 0 0 0 12px;"
        );

        // 1. Icon & Tiêu đề ứng dụng bên trái
        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBadge = new StackPane();
        Circle c = new Circle(9, Color.web("#4E6BF2"));
        Label lblI = new Label("💬");
        lblI.setStyle("-fx-font-size: 10px; -fx-text-fill: #FFFFFF;");
        iconBadge.getChildren().addAll(c, lblI);

        lblTitle.setText(titleText);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: -text-muted;");

        titleBox.getChildren().addAll(iconBadge, lblTitle);

        // 2. Vùng co giãn kéo thả cửa sổ (Draggable region)
        Region dragSpacer = new Region();
        HBox.setHgrow(dragSpacer, Priority.ALWAYS);

        // 3. Cụm nút điều khiển kiểu Zalo PC với Vector Graphics chuẩn Fluent UI
        HBox controlBox = new HBox(0);
        controlBox.setAlignment(Pos.CENTER_RIGHT);

        if (onLockAction != null) {
            btnLock.setGraphic(createLockGraphic());
            configureControlBtn(btnLock, "Khóa ứng dụng / Đăng xuất tạm thời", () -> {
                if (this.onLockAction != null) {
                    this.onLockAction.run();
                }
            });
            btnLock.setStyle("-fx-background-color: transparent; -fx-pref-width: 38px; -fx-pref-height: 32px; -fx-cursor: hand; -fx-padding: 0;");
            applyControlHover(btnLock);
            controlBox.getChildren().add(btnLock);
        }

        btnMin.setGraphic(createMinimizeGraphic());
        configureControlBtn(btnMin, "Thu nhỏ (Minimize)", () -> stage.setIconified(true));
        btnMin.setStyle("-fx-background-color: transparent; -fx-pref-width: 44px; -fx-pref-height: 32px; -fx-cursor: hand; -fx-padding: 0;");

        updateMaxGraphic();
        configureControlBtn(btnMax, stage.isMaximized() ? "Khôi phục" : "Phóng to", () -> {
            stage.setMaximized(!stage.isMaximized());
            updateMaxGraphic();
        });
        btnMax.setStyle("-fx-background-color: transparent; -fx-pref-width: 44px; -fx-pref-height: 32px; -fx-cursor: hand; -fx-padding: 0;");

        stage.maximizedProperty().addListener((obs, oldV, isMax) -> updateMaxGraphic());

        btnClose.setGraphic(createCloseGraphic(Color.web("#C4C7D0")));
        configureControlBtn(btnClose, "Đóng (Alt+F4)", () -> stage.close());
        btnClose.setStyle("-fx-background-color: transparent; -fx-pref-width: 44px; -fx-pref-height: 32px; -fx-cursor: hand; -fx-padding: 0;");

        // Hover effect cho nút Đóng (Đỏ rực chuẩn Windows / Discord)
        btnClose.setOnMouseEntered(e -> {
            btnClose.setStyle("-fx-background-color: #E81123; -fx-pref-width: 44px; -fx-pref-height: 32px; -fx-cursor: hand; -fx-padding: 0;");
            btnClose.setGraphic(createCloseGraphic(Color.WHITE));
        });
        btnClose.setOnMouseExited(e -> {
            btnClose.setStyle("-fx-background-color: transparent; -fx-pref-width: 44px; -fx-pref-height: 32px; -fx-cursor: hand; -fx-padding: 0;");
            btnClose.setGraphic(createCloseGraphic(Color.web("#C4C7D0")));
        });

        // Hover effect cho các nút thông thường
        applyControlHover(btnMin);
        applyControlHover(btnMax);

        controlBox.getChildren().addAll(btnMin, btnMax, btnClose);

        getChildren().addAll(titleBox, dragSpacer, controlBox);

        // Kéo thả di chuyển cửa sổ (Window dragging)
        final double[] dragDelta = new double[2];
        setOnMousePressed(mouseEvent -> {
            dragDelta[0] = stage.getX() - mouseEvent.getScreenX();
            dragDelta[1] = stage.getY() - mouseEvent.getScreenY();
        });
        setOnMouseDragged(mouseEvent -> {
            if (!stage.isMaximized()) {
                stage.setX(mouseEvent.getScreenX() + dragDelta[0]);
                stage.setY(mouseEvent.getScreenY() + dragDelta[1]);
            }
        });

        // Nhấp đúp để phóng to / khôi phục kích thước
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                stage.setMaximized(!stage.isMaximized());
                updateMaxGraphic();
            }
        });
    }

    private void updateMaxGraphic() {
        boolean isMax = stage.isMaximized();
        btnMax.setGraphic(isMax ? createRestoreGraphic() : createMaximizeGraphic());
        Tooltip.install(btnMax, new Tooltip(isMax ? "Khôi phục kích thước" : "Phóng to"));
    }

    private Node createMinimizeGraphic() {
        Rectangle line = new Rectangle(10, 1.2);
        line.setFill(Color.web("#C4C7D0"));
        return line;
    }

    private Node createMaximizeGraphic() {
        Rectangle rect = new Rectangle(10, 10);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.web("#C4C7D0"));
        rect.setStrokeWidth(1.0);
        return rect;
    }

    private Node createRestoreGraphic() {
        SVGPath path = new SVGPath();
        path.setContent("M 2.5 2.5 V 0.5 H 9.5 V 7.5 H 7.5 M 0.5 2.5 H 7.5 V 9.5 H 0.5 Z");
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web("#C4C7D0"));
        path.setStrokeWidth(1.0);
        return path;
    }

    private Node createCloseGraphic(Color strokeColor) {
        SVGPath path = new SVGPath();
        path.setContent("M 0.5 0.5 L 9.5 9.5 M 9.5 0.5 L 0.5 9.5");
        path.setFill(Color.TRANSPARENT);
        path.setStroke(strokeColor);
        path.setStrokeWidth(1.15);
        return path;
    }

    private Node createLockGraphic() {
        SVGPath path = new SVGPath();
        path.setContent("M 2 4.5 V 2.5 C 2 1.1 3.1 0 4.5 0 C 5.9 0 7 1.1 7 2.5 V 4.5 H 8 C 8.6 4.5 9 4.9 9 5.5 V 10 C 9 10.6 8.6 11 8 11 H 1 C 0.4 11 0 10.6 0 10 V 5.5 C 0 4.9 0.4 4.5 1 4.5 H 2 Z M 3 4.5 H 6 V 2.5 C 6 1.7 5.3 1 4.5 1 C 3.7 1 3 1.7 3 2.5 V 4.5 Z");
        path.setFill(Color.web("#8E929B"));
        return path;
    }

    private void configureControlBtn(Button btn, String tooltipText, Runnable action) {
        Tooltip.install(btn, new Tooltip(tooltipText));
        btn.setOnAction(e -> action.run());
    }

    private void applyControlHover(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.08); " +
            "-fx-pref-width: " + (btn == btnLock ? "38px" : "44px") + "; " +
            "-fx-pref-height: 32px; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 0;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-pref-width: " + (btn == btnLock ? "38px" : "44px") + "; " +
            "-fx-pref-height: 32px; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 0;"
        ));
    }

    public void setTitle(String title) {
        lblTitle.setText(title);
    }
}
