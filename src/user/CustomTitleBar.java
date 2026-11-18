package user;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Thanh Tiêu đề Tùy biến (Custom Title Bar) chuẩn Zalo PC & Fluent UI
 * - Thiết kế tối giản, đồng bộ hoàn hảo với Theme Obsidian Dark (-bg-nav-rail / -bg-card)
 * - Tích hợp đầy đủ các chức năng:
 *   + Thu nhỏ cửa sổ xuống Taskbar (Minimize qua stage.setIconified)
 *   + Phóng to thông minh chừa Taskbar của Windows (Taskbar-Aware Maximize qua Screen.getVisualBounds)
 *   + Khôi phục chuẩn xác 100% kích thước và vị trí ban đầu (Restore bounds)
 *   + Kéo thả thanh tiêu đề để di chuyển cửa sổ (Draggable Window)
 *   + Tự động khôi phục và bám chuột khi kéo thanh tiêu đề lúc đang phóng to (Snap & Drag Restore)
 *   + Nhấp đúp (Double-click) hoặc phím F11 để phóng to / khôi phục
 *   + Tách biệt vùng biên 6px mép trên để nhường quyền co giãn cho WindowResizeHelper
 *   + Cụm nút điều khiển kiểu Zalo PC sử dụng Vector Shapes / SVG sắc nét 100%.
 */
public class CustomTitleBar extends HBox {

    private static final double TOP_RESIZE_MARGIN = 6.0;

    private final Stage stage;
    private final Label lblTitle = new Label();
    private final Button btnLock = new Button();
    private final Button btnMin = new Button();
    private final Button btnMax = new Button();
    private final Button btnClose = new Button();
    private final Runnable onLockAction;

    // Trạng thái lưu trữ để khôi phục kích thước cửa sổ
    private boolean isCustomMaximized = false;
    private double prevX = 0;
    private double prevY = 0;
    private double prevWidth = 0;
    private double prevHeight = 0;

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
        SVGPath svgIcon = JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_MESSAGE, 0.55, "#FFFFFF");
        iconBadge.getChildren().addAll(c, svgIcon);

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
        configureControlBtn(btnMax, isCustomMaximized ? "Khôi phục" : "Phóng to", this::toggleMaximize);
        btnMax.setStyle("-fx-background-color: transparent; -fx-pref-width: 44px; -fx-pref-height: 32px; -fx-cursor: hand; -fx-padding: 0;");

        btnClose.setGraphic(createCloseGraphic(Color.web("#C4C7D0")));
        configureControlBtn(btnClose, "Đóng (Alt+F4)", stage::close);
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

        // 4. Kéo thả di chuyển cửa sổ với tính năng Snap & Drag Restore (Lựa chọn A)
        final double[] dragDelta = new double[2];
        final boolean[] isDraggingActive = new boolean[1];

        setOnMousePressed(mouseEvent -> {
            // Nhường vùng mép trên cho WindowResizeHelper co giãn viền
            if (mouseEvent.getY() < TOP_RESIZE_MARGIN) {
                isDraggingActive[0] = false;
                return;
            }
            // Không kích hoạt kéo khi nhấn chuột trúng các nút bấm
            if (isNodeInsideControl(mouseEvent.getTarget())) {
                isDraggingActive[0] = false;
                return;
            }

            isDraggingActive[0] = true;
            dragDelta[0] = stage.getX() - mouseEvent.getScreenX();
            dragDelta[1] = stage.getY() - mouseEvent.getScreenY();
        });

        setOnMouseDragged(mouseEvent -> {
            if (!isDraggingActive[0]) return;

            if (isCustomMaximized) {
                // Đang phóng to mà kéo thanh tiêu đề: Tự động khôi phục và bám chuột chuẩn Zalo PC / Windows
                double currentBarWidth = getWidth();
                double mouseRatio = currentBarWidth > 0 ? (mouseEvent.getX() / currentBarWidth) : 0.5;
                mouseRatio = Math.max(0.05, Math.min(0.95, mouseRatio));

                isCustomMaximized = false;
                stage.getProperties().put("custom_maximized", Boolean.FALSE);
                updateMaxGraphic();

                double targetW = (prevWidth > 0) ? prevWidth : Math.max(stage.getMinWidth(), 960);
                double targetH = (prevHeight > 0) ? prevHeight : Math.max(stage.getMinHeight(), 640);

                stage.setWidth(targetW);
                stage.setHeight(targetH);

                double restoredX = mouseEvent.getScreenX() - (targetW * mouseRatio);
                double restoredY = mouseEvent.getScreenY() - mouseEvent.getY();
                stage.setX(restoredX);
                stage.setY(restoredY);

                dragDelta[0] = stage.getX() - mouseEvent.getScreenX();
                dragDelta[1] = stage.getY() - mouseEvent.getScreenY();
            } else {
                stage.setX(mouseEvent.getScreenX() + dragDelta[0]);
                stage.setY(mouseEvent.getScreenY() + dragDelta[1]);
            }
        });

        setOnMouseReleased(mouseEvent -> isDraggingActive[0] = false);

        // 5. Nhấp đúp chuột để phóng to / khôi phục kích thước
        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                if (!isNodeInsideControl(mouseEvent.getTarget())) {
                    toggleMaximize();
                }
            }
        });

        // 6. Hỗ trợ phím tắt F11 toàn cục để Phóng to / Khôi phục
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F11) {
                        event.consume();
                        toggleMaximize();
                    }
                });
            }
        });
    }

    /**
     * Bật/tắt trạng thái Phóng to (Maximize) thông minh:
     * - Khi phóng to: Tự động canh theo VisualBounds để không che mất Windows Taskbar.
     * - Khi khôi phục: Phục hồi chính xác 100% tọa độ và kích thước trước đó.
     */
    public void toggleMaximize() {
        if (!isCustomMaximized) {
            // Lưu lại vị trí và kích thước hiện tại trước khi phóng to
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            // Tìm màn hình chứa cửa sổ hiện thời
            ObservableList<Screen> screens = Screen.getScreensForRectangle(prevX, prevY, prevWidth, prevHeight);
            Screen screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
            Rectangle2D visualBounds = screen.getVisualBounds();

            stage.setX(visualBounds.getMinX());
            stage.setY(visualBounds.getMinY());
            stage.setWidth(visualBounds.getWidth());
            stage.setHeight(visualBounds.getHeight());

            isCustomMaximized = true;
            stage.getProperties().put("custom_maximized", Boolean.TRUE);
        } else {
            // Khôi phục kích thước và vị trí ban đầu
            if (prevWidth > 0 && prevHeight > 0) {
                stage.setX(prevX);
                stage.setY(prevY);
                stage.setWidth(prevWidth);
                stage.setHeight(prevHeight);
            } else {
                stage.setWidth(Math.max(stage.getMinWidth(), 960));
                stage.setHeight(Math.max(stage.getMinHeight(), 640));
                stage.centerOnScreen();
            }

            isCustomMaximized = false;
            stage.getProperties().put("custom_maximized", Boolean.FALSE);
        }
        updateMaxGraphic();
    }

    public boolean isCustomMaximized() {
        return isCustomMaximized;
    }

    private boolean isNodeInsideControl(Object target) {
        if (!(target instanceof Node)) return false;
        Node curr = (Node) target;
        while (curr != null && curr != this) {
            if (curr instanceof Button) return true;
            curr = curr.getParent();
        }
        return false;
    }

    private void updateMaxGraphic() {
        btnMax.setGraphic(isCustomMaximized ? createRestoreGraphic() : createMaximizeGraphic());
        Tooltip.install(btnMax, new Tooltip(isCustomMaximized ? "Khôi phục kích thước (F11)" : "Phóng to (F11)"));
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
