package user;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Optional;

/**
 * Lớp tiện ích giao diện chuẩn hóa cho JavaFX 21 (Theme & Micro-interactions)
 * Hỗ trợ các tương tác công thái học: Password Toggle Eye, Shake on Error, Strength Meter.
 * Tuân thủ chuẩn chuyển động Microsoft Fluent 2, Material 3 và Usability Heuristics (NN/g).
 */
public final class JavaFXTheme {

    // =========================================================================
    // VECTOR SVG ICONS CHUẨN LUCIDE / MATERIAL (Chống lỗi font và tofu box)
    // =========================================================================
    public static final String SVG_CHECKLIST = "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-9 14l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z";
    public static final String SVG_SEARCH = "M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";
    public static final String SVG_REPORT = "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z";
    public static final String SVG_INFO = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";
    public static final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    public static final String SVG_SIDEBAR_TOGGLE = "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2zm5 14V6H4v12h5zm11 0V6h-9v12h9z";
    public static final String SVG_CHEVRON_LEFT = "M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z";
    public static final String SVG_CHEVRON_RIGHT = "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z";
    public static final String SVG_CLOSE = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";
    public static final String SVG_TRASH = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    public static final String SVG_BAN = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8 0-1.85.63-3.55 1.69-4.9L16.9 18.31C15.55 19.37 13.85 20 12 20zm6.31-3.1L7.1 5.69C8.45 4.63 10.15 4 12 4c4.42 0 8 3.58 8 8 0 1.85-.63 3.55-1.69 4.9z";
    public static final String SVG_USER_PLUS = "M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    public static final String SVG_MESSAGE = "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z";
    public static final String SVG_USERS = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
    public static final String SVG_MORE_HORIZONTAL = "M6 10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm12 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-6 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z";
    public static final String SVG_CHECK = "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z";
    public static final String SVG_LOCK = "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z";
    public static final String SVG_SHIELD = "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z";
    public static final String SVG_PALETTE = "M12 3c-4.97 0-9 4.03-9 9 0 2.12.74 4.07 1.97 5.61L4.35 19.4c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 0l2.08-2.08C9.28 19.46 10.59 20 12 20c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-5.5 9c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z";
    public static final String SVG_SETTINGS = "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z";
    public static final String SVG_LOGOUT = "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z";
    public static final String SVG_PLUS = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z";

    /**
     * Tạo đối tượng SVGPath chuẩn sắc thái -text-muted (#8E929B) tương thích hoàn hảo với JavaFX CSS
     */
    public static SVGPath createSvgIcon(String pathData, double scale) {
        return createSvgIcon(pathData, scale, "#8E929B");
    }

    /**
     * Tạo đối tượng SVGPath với màu sắc tùy biến
     */
    public static SVGPath createSvgIcon(String pathData, double scale, String colorHex) {
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.web(colorHex));
        svg.getStyleClass().add("header-svg-icon");
        if (scale != 1.0) {
            svg.setScaleX(scale);
            svg.setScaleY(scale);
        }
        return svg;
    }

    // =========================================================================
    // ĐƯỜNG CONG GIA TỐC VẬT LÝ CHUẨN MICROSOFT FLUENT 2
    // =========================================================================
    /** Gia tốc Decelerate (Phóng nhanh rồi phanh êm): Dùng khi xuất hiện / Enter */
    public static final Interpolator FLUENT_DECELERATE = Interpolator.SPLINE(0.1, 0.9, 0.2, 1.0);
    /** Gia tốc Accelerate (Tăng tốc rồi biến mất): Dùng khi rút lui / Exit */
    public static final Interpolator FLUENT_ACCELERATE = Interpolator.SPLINE(0.7, 0.0, 1.0, 0.5);
    /** Gia tốc Standard / Point-to-Point (Bắt đầu nhanh, lướt êm): Dùng khi chuyển vị trí */
    public static final Interpolator FLUENT_STANDARD = Interpolator.SPLINE(0.8, 0.0, 0.2, 1.0);

    private JavaFXTheme() {}

    /**
     * Tạo nhãn Field Label kèm dấu sao đỏ bắt buộc theo chuẩn UX
     */
    public static HBox createFieldLabel(String text, boolean isRequired) {
        HBox box = new HBox(2);
        box.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(text.toUpperCase());
        lbl.getStyleClass().add("field-label");
        box.getChildren().add(lbl);

        if (isRequired) {
            Label star = new Label("*");
            star.getStyleClass().add("required-star");
            box.getChildren().add(star);
        }
        return box;
    }

    /**
     * Component Password Field nâng cao có nút con mắt (Eye reveal button)
     * Đồng bộ 2 chiều giữa PasswordField (dấu chấm) và TextField (chữ thường).
     */
    public static class PasswordFieldWrapper {
        private final StackPane container = new StackPane();
        private final PasswordField passwordField = new PasswordField();
        private final TextField textField = new TextField();
        private final Button btnToggle = new Button("👁");
        private boolean isRevealed = false;

        public PasswordFieldWrapper(String initialText, String promptText) {
            passwordField.setText(initialText);
            textField.setText(initialText);

            passwordField.setPromptText(promptText);
            textField.setPromptText(promptText);

            // TextField ban dau an di
            textField.setVisible(false);
            textField.setManaged(false);

            // Dong bo du lieu 2 chieu
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!textField.getText().equals(newVal)) {
                    textField.setText(newVal);
                }
                clearError(container);
            });

            textField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!passwordField.getText().equals(newVal)) {
                    passwordField.setText(newVal);
                }
                clearError(container);
            });

            // Nut toggle con mat co animation vi mo
            btnToggle.getStyleClass().add("btn-eye-toggle");
            btnToggle.setFocusTraversable(false);
            btnToggle.setOnAction(e -> toggleReveal());
            applyIconButton(btnToggle);

            StackPane.setAlignment(btnToggle, Pos.CENTER_RIGHT);
            StackPane.setMargin(btnToggle, new Insets(0, 8, 0, 0));

            // Set padding cho input de chu khong bi de len nut mat
            passwordField.setPadding(new Insets(8, 40, 8, 14));
            textField.setPadding(new Insets(8, 40, 8, 14));

            container.getChildren().addAll(passwordField, textField, btnToggle);
            HBox.setHgrow(container, Priority.ALWAYS);
        }

        private void toggleReveal() {
            isRevealed = !isRevealed;
            if (isRevealed) {
                textField.setText(passwordField.getText());
                textField.setVisible(true);
                textField.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                btnToggle.setText("🔒");
                textField.requestFocus();
                textField.positionCaret(textField.getText().length());
            } else {
                passwordField.setText(textField.getText());
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                textField.setVisible(false);
                textField.setManaged(false);
                btnToggle.setText("👁");
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            }
        }

        public StackPane getContainer() {
            return container;
        }

        public String getText() {
            return isRevealed ? textField.getText() : passwordField.getText();
        }

        public void setText(String text) {
            passwordField.setText(text);
            textField.setText(text);
        }

        public void clear() {
            passwordField.clear();
            textField.clear();
        }

        public PasswordField getPasswordField() {
            return passwordField;
        }

        public TextField getTextField() {
            return textField;
        }

        public void requestFocus() {
            if (isRevealed) {
                textField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        }
    }

    /**
     * Hiệu ứng Horizontal Spring Shake 60fps khi lỗi validation (±8px trong 200ms)
     */
    public static void shakeNode(Node node) {
        if (!node.getStyleClass().contains("input-error")) {
            node.getStyleClass().add("input-error");
        }

        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    /**
     * Xóa viền đỏ báo lỗi khi người dùng bắt đầu gõ lại
     */
    public static void clearError(Node node) {
        node.getStyleClass().remove("input-error");
    }

    /**
     * Tính toán độ mạnh mật khẩu trả về điểm % từ 0 đến 100
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        if (password.length() >= 6) score += 25;
        if (password.length() >= 9) score += 20;
        if (password.matches(".*[A-Z].*")) score += 20;
        if (password.matches(".*[0-9].*")) score += 20;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score += 15;
        return Math.min(score, 100);
    }

    /**
     * Hiệu ứng nảy xúc giác cao cấp (Tactile Micro-press & Hover Elevation):
     * - Khi di chuột (Hover): Nâng nhẹ lên 1.025x trong 110ms với Fluent 2 Decelerate.
     * - Khi nhấn (Pressed): Co lún 0.965x trong 65ms mang lại cảm giác phản hồi cơ học rõ rệt.
     * - Khi nhả (Released): Đàn hồi phục hồi về 1.025x trong 85ms.
     * - Khi rời chuột (Exited): Trở về kích thước chuẩn 1.0x trong 100ms.
     */
    public static void applyTactileButton(Button button) {
        if (button == null) return;

        ScaleTransition hoverAnim = new ScaleTransition(Duration.millis(110), button);
        hoverAnim.setInterpolator(FLUENT_DECELERATE);

        ScaleTransition pressAnim = new ScaleTransition(Duration.millis(65), button);
        pressAnim.setInterpolator(FLUENT_DECELERATE);

        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (!button.isDisabled()) {
                hoverAnim.stop();
                hoverAnim.setToX(1.025);
                hoverAnim.setToY(1.025);
                hoverAnim.playFromStart();
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            hoverAnim.stop();
            pressAnim.stop();
            hoverAnim.setToX(1.0);
            hoverAnim.setToY(1.0);
            hoverAnim.playFromStart();
        });

        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!button.isDisabled()) {
                hoverAnim.stop();
                pressAnim.stop();
                pressAnim.setToX(0.965);
                pressAnim.setToY(0.965);
                pressAnim.playFromStart();
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (!button.isDisabled()) {
                pressAnim.stop();
                hoverAnim.stop();
                hoverAnim.setToX(1.025);
                hoverAnim.setToY(1.025);
                hoverAnim.playFromStart();
            }
        });
    }

    /**
     * Hiệu ứng vi mô cho nút Icon (phóng nhẹ khi hover, co nhẹ khi bấm):
     * - Khi di chuột vào (Hover): Phóng nhẹ 1.08x trong 100ms.
     * - Khi rời chuột (Exit): Khôi phục 1.0x trong 100ms.
     * - Khi nhấn (Pressed): Co nhẹ 0.94x trong 60ms.
     */
    public static void applyIconButton(Button button) {
        if (button == null) return;

        ScaleTransition hoverAnim = new ScaleTransition(Duration.millis(100), button);
        hoverAnim.setInterpolator(FLUENT_DECELERATE);

        ScaleTransition clickAnim = new ScaleTransition(Duration.millis(60), button);
        clickAnim.setInterpolator(FLUENT_DECELERATE);

        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (!button.isDisabled()) {
                hoverAnim.stop();
                hoverAnim.setToX(1.08);
                hoverAnim.setToY(1.08);
                hoverAnim.playFromStart();
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            hoverAnim.stop();
            clickAnim.stop();
            hoverAnim.setToX(1.0);
            hoverAnim.setToY(1.0);
            hoverAnim.playFromStart();
        });

        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!button.isDisabled()) {
                clickAnim.stop();
                clickAnim.setToX(0.94);
                clickAnim.setToY(0.94);
                clickAnim.playFromStart();
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            clickAnim.stop();
            hoverAnim.stop();
            hoverAnim.setToX(1.08);
            hoverAnim.setToY(1.08);
            hoverAnim.playFromStart();
        });
    }

    /**
     * Hiệu ứng cho nút Đóng / Thoát ✕ với sắc đỏ cảnh báo (Destructive Red Hover):
     * - Khi hover: Đổi màu icon sang đỏ rực #FA777C và phóng nhẹ 1.1x.
     * - Khi nhấn (Pressed): Co nhẹ 0.92x.
     * - Khi thoát: Trở về màu muted #8E929B và tỉ lệ 1.0x.
     */
    public static void applyCloseButton(Button button) {
        if (button == null) return;
        if (!button.getStyleClass().contains("dialog-close-btn")) {
            button.getStyleClass().add("dialog-close-btn");
        }

        ScaleTransition hoverAnim = new ScaleTransition(Duration.millis(100), button);
        hoverAnim.setInterpolator(FLUENT_DECELERATE);

        ScaleTransition clickAnim = new ScaleTransition(Duration.millis(60), button);
        clickAnim.setInterpolator(FLUENT_DECELERATE);

        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (!button.isDisabled()) {
                hoverAnim.stop();
                hoverAnim.setToX(1.1);
                hoverAnim.setToY(1.1);
                hoverAnim.playFromStart();
                if (button.getGraphic() instanceof javafx.scene.shape.SVGPath) {
                    ((javafx.scene.shape.SVGPath) button.getGraphic()).setFill(Color.web("#FA777C"));
                }
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            hoverAnim.stop();
            clickAnim.stop();
            hoverAnim.setToX(1.0);
            hoverAnim.setToY(1.0);
            hoverAnim.playFromStart();
            if (button.getGraphic() instanceof javafx.scene.shape.SVGPath) {
                ((javafx.scene.shape.SVGPath) button.getGraphic()).setFill(Color.web("#8E929B"));
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!button.isDisabled()) {
                clickAnim.stop();
                clickAnim.setToX(0.92);
                clickAnim.setToY(0.92);
                clickAnim.playFromStart();
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            clickAnim.stop();
            hoverAnim.stop();
            hoverAnim.setToX(1.1);
            hoverAnim.setToY(1.1);
            hoverAnim.playFromStart();
        });
    }

    /**
     * Hiệu ứng xuất hiện cho Modal Dialog theo chuẩn Fluent 2 / macOS:
     * - Phóng nhẹ từ 0.96x lên 1.0x kết hợp Fade-in trong 180ms.
     * - Easing: Decelerate phanh êm.
     */
    public static void applyModalEntrance(Parent rootNode) {
        if (rootNode == null) return;
        rootNode.setOpacity(0.0);
        rootNode.setScaleX(0.96);
        rootNode.setScaleY(0.96);

        ScaleTransition st = new ScaleTransition(Duration.millis(180), rootNode);
        st.setFromX(0.96);
        st.setFromY(0.96);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(FLUENT_DECELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(180), rootNode);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(FLUENT_DECELERATE);

        new ParallelTransition(st, ft).play();
    }

    /**
     * Hiệu ứng đóng Modal Dialog:
     * - Co nhẹ xuống 0.97x kết hợp Fade-out trong 120ms (đóng nhanh hơn mở theo chuẩn Fluent 2).
     * - Easing: Accelerate rút nhanh.
     */
    public static void closeModalWithAnimation(Stage dialog, Parent rootNode) {
        if (dialog == null) return;
        if (rootNode == null) {
            dialog.close();
            return;
        }

        ScaleTransition st = new ScaleTransition(Duration.millis(120), rootNode);
        st.setToX(0.97);
        st.setToY(0.97);
        st.setInterpolator(FLUENT_ACCELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(120), rootNode);
        ft.setToValue(0.0);
        ft.setInterpolator(FLUENT_ACCELERATE);

        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.setOnFinished(e -> dialog.close());
        pt.play();
    }

    /**
     * Hiệu ứng chuyển đổi nội dung Tab / View (Short-distance Slide 8px + Fade):
     * - Thời lượng 150ms, gia tốc Decelerate.
     */
    public static void applyTabContentTransition(Node contentNode, boolean fromRight) {
        if (contentNode == null) return;
        contentNode.setOpacity(0.0);
        contentNode.setTranslateX(fromRight ? 8 : -8);

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), contentNode);
        tt.setToX(0);
        tt.setInterpolator(FLUENT_DECELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(150), contentNode);
        ft.setToValue(1.0);
        ft.setInterpolator(FLUENT_DECELERATE);

        new ParallelTransition(tt, ft).play();
    }

    /**
     * Hiệu ứng trượt mở Drawer cạnh bên (Fluent 2 Decelerate 220ms, TranslateX -> 0 + Opacity 0 -> 1)
     */
    public static void animateDrawerOpen(Region drawer, double width) {
        if (drawer == null) return;
        drawer.setVisible(true);
        drawer.setManaged(true);
        drawer.setOpacity(0.0);
        drawer.setTranslateX(width);

        TranslateTransition tt = new TranslateTransition(Duration.millis(220), drawer);
        tt.setToX(0);
        tt.setInterpolator(FLUENT_DECELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(220), drawer);
        ft.setToValue(1.0);
        ft.setInterpolator(FLUENT_DECELERATE);

        new ParallelTransition(tt, ft).play();
    }

    /**
     * Hiệu ứng trượt đóng Drawer cạnh bên (Fluent 2 Accelerate 160ms, TranslateX 0 -> width + Opacity 1 -> 0)
     */
    public static void animateDrawerClose(Region drawer, double width, Runnable onComplete) {
        if (drawer == null) return;

        TranslateTransition tt = new TranslateTransition(Duration.millis(160), drawer);
        tt.setToX(width);
        tt.setInterpolator(FLUENT_ACCELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(160), drawer);
        ft.setToValue(0.0);
        ft.setInterpolator(FLUENT_ACCELERATE);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> {
            drawer.setVisible(false);
            drawer.setManaged(false);
            drawer.setTranslateX(0);
            if (onComplete != null) {
                onComplete.run();
            }
        });
        pt.play();
    }

    /**
     * Hiệu ứng trượt mở Drawer cạnh trái (Fluent 2 Decelerate 220ms, TranslateX -width -> 0)
     */
    public static void animateLeftDrawerOpen(Region drawer, double width) {
        if (drawer == null) return;
        drawer.setVisible(true);
        drawer.setManaged(true);
        drawer.setOpacity(0.0);
        drawer.setTranslateX(-width);

        TranslateTransition tt = new TranslateTransition(Duration.millis(220), drawer);
        tt.setToX(0);
        tt.setInterpolator(FLUENT_DECELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(220), drawer);
        ft.setToValue(1.0);
        ft.setInterpolator(FLUENT_DECELERATE);

        new ParallelTransition(tt, ft).play();
    }

    /**
     * Hiệu ứng trượt đóng Drawer cạnh trái (Fluent 2 Accelerate 160ms, TranslateX 0 -> -width)
     */
    public static void animateLeftDrawerClose(Region drawer, double width, Runnable onComplete) {
        if (drawer == null) return;

        TranslateTransition tt = new TranslateTransition(Duration.millis(160), drawer);
        tt.setToX(-width);
        tt.setInterpolator(FLUENT_ACCELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(160), drawer);
        ft.setToValue(0.0);
        ft.setInterpolator(FLUENT_ACCELERATE);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> {
            drawer.setVisible(false);
            drawer.setManaged(false);
            drawer.setTranslateX(0);
            if (onComplete != null) {
                onComplete.run();
            }
        });
        pt.play();
    }

    /**
     * Gắn stylesheet Obsidian Dark và cấu hình hiển thị chuẩn cho ContextMenu
     */
    public static void applyContextMenuTheme(ContextMenu cm) {
        if (cm == null) return;
        java.net.URL cssUrl = JavaFXTheme.class.getResource("theme.css");
        if (cssUrl != null) {
            cm.setOnShowing(e -> {
                if (cm.getScene() != null && !cm.getScene().getStylesheets().contains(cssUrl.toExternalForm())) {
                    cm.getScene().getStylesheets().add(cssUrl.toExternalForm());
                }
            });
        }
    }

    /**
     * Chuẩn hóa DialogPane của JavaFX Dialog (Alert, TextInputDialog) theo Obsidian Dark
     * Loại bỏ icon Modena mặc định thô ráp, nạp stylesheet theme.css và hiệu ứng phóng êm.
     */
    public static void applyDialogTheme(Dialog<?> dialog) {
        if (dialog == null) return;
        try {
            dialog.initStyle(StageStyle.TRANSPARENT);
        } catch (Exception ignored) {}
        java.net.URL cssUrl = JavaFXTheme.class.getResource("theme.css");
        if (cssUrl != null) {
            DialogPane pane = dialog.getDialogPane();
            if (!pane.getStylesheets().contains(cssUrl.toExternalForm())) {
                pane.getStylesheets().add(cssUrl.toExternalForm());
            }
        }
        dialog.setGraphic(null);
        dialog.setOnShowing(e -> {
            DialogPane pane = dialog.getDialogPane();
            if (pane.getScene() != null) {
                pane.getScene().setFill(Color.TRANSPARENT);
                Stage stg = (Stage) pane.getScene().getWindow();
                if (stg != null) {
                    final double[] dragDelta = new double[2];
                    pane.setOnMousePressed(me -> {
                        dragDelta[0] = stg.getX() - me.getScreenX();
                        dragDelta[1] = stg.getY() - me.getScreenY();
                    });
                    pane.setOnMouseDragged(me -> {
                        stg.setX(me.getScreenX() + dragDelta[0]);
                        stg.setY(me.getScreenY() + dragDelta[1]);
                    });
                }
            }
            applyModalEntrance(pane);
        });
    }

    /**
     * Hộp thoại nhập văn bản chuẩn hóa Obsidian Dark (thay thế TextInputDialog gốc của JavaFX)
     */
    public static Optional<String> showInputDialog(Window owner, String title, String header, String prompt, String initialValue) {
        TextInputDialog dialog = new TextInputDialog(initialValue != null ? initialValue : "");
        if (owner != null) dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);
        applyDialogTheme(dialog);
        TextField editor = dialog.getEditor();
        if (editor != null) {
            editor.setStyle(
                "-fx-background-color: #0F1013; " +
                "-fx-border-color: #2A2D35; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-text-fill: -text-normal; " +
                "-fx-font-size: 13.5px; " +
                "-fx-padding: 8px 12px;"
            );
        }
        return dialog.showAndWait();
    }

    /**
     * Hộp thoại xác nhận thao tác chuẩn hóa Obsidian Dark (thay thế Alert.CONFIRMATION gốc)
     */
    public static boolean showConfirmDialog(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        applyDialogTheme(alert);

        DialogPane pane = alert.getDialogPane();
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().add("btn-primary");
            applyTactileButton(okBtn);
        }
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) {
            cancelBtn.getStyleClass().add("btn-secondary");
            applyTactileButton(cancelBtn);
        }

        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    /**
     * Hộp thoại cảnh báo & xác nhận thao tác nguy hiểm / hủy bỏ / xóa dữ liệu (Destructive Confirmation):
     * - Nút Xác nhận mang sắc đỏ cảnh báo (#ED4245 / .btn-danger), có tactile feedback.
     * - Nút Hủy bỏ mang sắc thứ cấp (.btn-secondary) an toàn.
     */
    public static boolean showDestructiveConfirmDialog(Window owner, String title, String header, String content, String confirmText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        applyDialogTheme(alert);

        ButtonType btnConfirm = new ButtonType(confirmText != null ? confirmText : "Xác nhận xóa", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnConfirm, btnCancel);

        DialogPane pane = alert.getDialogPane();
        pane.setStyle(pane.getStyle() + "; -fx-border-color: rgba(237, 66, 69, 0.4);");

        Button okBtn = (Button) pane.lookupButton(btnConfirm);
        if (okBtn != null) {
            okBtn.setStyle(
                "-fx-background-color: #ED4245; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 8px 16px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand;"
            );
            applyTactileButton(okBtn);
        }

        Button cancelBtn = (Button) pane.lookupButton(btnCancel);
        if (cancelBtn != null) {
            cancelBtn.getStyleClass().add("btn-secondary");
            applyTactileButton(cancelBtn);
        }

        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == btnConfirm;
    }

    /**
     * Hộp thoại thông báo thông tin chuẩn hóa Obsidian Dark (thay thế Alert.INFORMATION gốc)
     */
    public static void showInfoAlert(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        applyDialogTheme(alert);
        alert.showAndWait();
    }

    /**
     * Mini-Profile Popover hiển thị thẻ định danh người dùng (260x280px)
     * Kích hoạt khi click vào Avatar hoặc Tên người dùng trong cuộc hội thoại hoặc danh sách thành viên.
     * Hiển thị Avatar, Tên, Username, Trạng thái online, Huy hiệu thành viên, cùng 2 nút hành động nhanh:
     * - "💬 Nhắn tin trực tiếp" (Mục 11.a)
     * - "🚫 Chặn người dùng" (Mục 6.d)
     */
    public static void showMiniProfilePopover(Node anchor, String fullName, String username, boolean online, String avatarColor, String initials) {
        if (anchor == null || anchor.getScene() == null) return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox card = new VBox(12);
        card.setPrefWidth(260);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: #18191E; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.55), 18, 0, 0, 8);"
        );

        // Header: Avatar lớn 48px + chấm online
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avtStack = new StackPane();
        Circle circle = new Circle(24, Color.web(avatarColor != null ? avatarColor : "#4E6BF2"));
        Label lblInit = new Label(initials != null ? initials : "?");
        lblInit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        avtStack.getChildren().addAll(circle, lblInit);

        Circle statusDot = new Circle(5, Color.web(online ? "#23A55A" : "#8E929B"));
        statusDot.setStroke(Color.web("#18191E"));
        statusDot.setStrokeWidth(2);
        StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);
        avtStack.getChildren().add(statusDot);

        VBox nameBox = new VBox(2);
        Label lblName = new Label(fullName != null ? fullName : "Người dùng");
        lblName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #EDEDED;");
        Label lblUser = new Label(username != null && !username.isEmpty() ? username : "@user");
        lblUser.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        Label lblStatus = new Label(online ? "🟢 Đang trực tuyến" : "⚪ Ngoại tuyến");
        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (online ? "#23A55A;" : "#8E929B;"));
        nameBox.getChildren().addAll(lblName, lblUser, lblStatus);

        topRow.getChildren().addAll(avtStack, nameBox);

        // Huy hiệu bảo mật / thành viên
        HBox badgeRow = new HBox(6);
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        Label badgeE2EE = new Label("🔒 Mã hóa nhóm · Mẫu");
        badgeE2EE.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #8E929B; -fx-background-color: #22252C; -fx-padding: 2px 7px; -fx-background-radius: 4px;");
        Label badgeRole = new Label(username != null && username.contains("minh") ? "👑 Chủ nhóm" : "🛡️ Thành viên");
        badgeRole.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #A78BFA; -fx-background-color: rgba(139, 92, 246, 0.15); -fx-padding: 2px 7px; -fx-background-radius: 4px;");
        badgeRow.getChildren().addAll(badgeE2EE, badgeRole);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2A2D35; -fx-pref-height: 1px;");

        // Action Buttons
        VBox actions = new VBox(6);

        Button btnDm = new Button("Nhắn tin trực tiếp");
        btnDm.setGraphic(createSvgIcon(SVG_MESSAGE, 0.8, "#739AFE"));
        btnDm.setGraphicTextGap(6);
        btnDm.setMaxWidth(Double.MAX_VALUE);
        btnDm.setStyle(
            "-fx-background-color: #242A3E; " +
            "-fx-border-color: #3B4B75; " +
            "-fx-border-radius: 7px; " +
            "-fx-background-radius: 7px; " +
            "-fx-text-fill: #739AFE; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 6 10; " +
            "-fx-cursor: hand;"
        );
        btnDm.setOnAction(e -> {
            popup.hide();
            showInfoAlert(anchor.getScene().getWindow(), "Cuộc trò chuyện trực tiếp", "Chuyển tới tin nhắn riêng", "Đã mở cuộc trò chuyện trực tiếp với " + fullName);
        });
        applyTactileButton(btnDm);

        Button btnBlock = new Button("Chặn người dùng (Mục 6.d)");
        btnBlock.setGraphic(createSvgIcon(SVG_BAN, 0.8, "#FA777C"));
        btnBlock.setGraphicTextGap(6);
        btnBlock.setMaxWidth(Double.MAX_VALUE);
        btnBlock.setStyle(
            "-fx-background-color: rgba(250, 119, 124, 0.08); " +
            "-fx-border-color: rgba(250, 119, 124, 0.3); " +
            "-fx-border-radius: 7px; " +
            "-fx-background-radius: 7px; " +
            "-fx-text-fill: #FA777C; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 6 10; " +
            "-fx-cursor: hand;"
        );
        btnBlock.setOnAction(e -> {
            popup.hide();
            boolean ok = showDestructiveConfirmDialog(
                anchor.getScene().getWindow(),
                "Xác nhận chặn",
                "Chặn người dùng (Mục 6.d)",
                "Bạn có chắc chắn muốn chặn " + fullName + " (" + username + ") không? Người này sẽ không thể gửi tin nhắn cho bạn.",
                "Chặn người dùng"
            );
            if (ok) {
                showInfoAlert(anchor.getScene().getWindow(), "Đã chặn", "Thao tác thành công", "Đã thêm " + fullName + " vào danh sách chặn.");
            }
        });
        applyTactileButton(btnBlock);

        actions.getChildren().addAll(btnDm, btnBlock);

        card.getChildren().addAll(topRow, badgeRow, sep, actions);

        // Đăng ký CSS
        java.net.URL cssUrl = JavaFXTheme.class.getResource("theme.css");
        if (cssUrl != null) {
            card.getStylesheets().add(cssUrl.toExternalForm());
        }

        popup.getContent().add(card);

        // Vị trí hiển thị bên dưới hoặc cạnh anchor
        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds != null) {
            double posX = Math.max(10, bounds.getMinX() - 10);
            double posY = bounds.getMaxY() + 6;
            popup.show(anchor.getScene().getWindow(), posX, posY);
            applyModalEntrance(card);
        }
    }

    private static int currentThemeIndex = 0;
    private static double currentFontSize = 13.0;

    public static int getCurrentThemeIndex() { return currentThemeIndex; }
    public static double getCurrentFontSize() { return currentFontSize; }

    /**
     * Chuyển đổi và áp dụng Theme toàn diện cho ứng dụng ngay lập tức (Real-time Dynamic Theming)
     * Thay đổi các biến CSS Design Tokens trên root của Scene:
     * - 0: Obsidian Dark (Mặc định)
     * - 1: Midnight Blue (Sắc xanh đêm)
     * - 2: Deep Emerald (Sắc rêu ngọc)
     */
    public static void applyThemeToScene(Scene scene, int themeIndex, double fontSize) {
        if (scene == null || scene.getRoot() == null) return;
        currentThemeIndex = themeIndex;
        if (fontSize > 0) currentFontSize = fontSize;

        String themeVariables;
        switch (themeIndex) {
            case 1: // Midnight Blue
                themeVariables =
                    "-bg-canvas: #090E17; " +
                    "-bg-card: #121A29; " +
                    "-bg-input: #0B111D; " +
                    "-bg-nav-rail: #080D16; " +
                    "-accent-primary: #38BDF8; " +
                    "-accent-hover: #0EA5E9; " +
                    "-accent-active: #0284C7; " +
                    "-border-subtle: #1E2D44; " +
                    "-text-normal: #F1F5F9; " +
                    "-text-muted: #94A3B8; " +
                    "-text-link: #38BDF8;";
                break;
            case 2: // Deep Emerald
                themeVariables =
                    "-bg-canvas: #07120D; " +
                    "-bg-card: #0F231B; " +
                    "-bg-input: #091712; " +
                    "-bg-nav-rail: #06110D; " +
                    "-accent-primary: #10B981; " +
                    "-accent-hover: #059669; " +
                    "-accent-active: #047857; " +
                    "-border-subtle: #1B3D30; " +
                    "-text-normal: #ECFDF5; " +
                    "-text-muted: #86EFAC; " +
                    "-text-link: #10B981;";
                break;
            default: // 0: Obsidian Dark
                themeVariables =
                    "-bg-canvas: #141619; " +
                    "-bg-card: #1B1D22; " +
                    "-bg-input: #0F1013; " +
                    "-bg-nav-rail: #0F1013; " +
                    "-accent-primary: #4E6BF2; " +
                    "-accent-hover: #3D5AE0; " +
                    "-accent-active: #2E48C7; " +
                    "-border-subtle: #2A2D35; " +
                    "-text-normal: #EDEDED; " +
                    "-text-muted: #8E929B; " +
                    "-text-link: #4E6BF2;";
                break;
        }

        String fontRule = fontSize > 0 ? " -fx-font-size: " + String.format(java.util.Locale.US, "%.1f", fontSize) + "px;" : "";
        scene.getRoot().getStyleClass().removeAll("theme-midnight", "theme-emerald");
        if (themeIndex == 1) {
            scene.getRoot().getStyleClass().add("theme-midnight");
        } else if (themeIndex == 2) {
            scene.getRoot().getStyleClass().add("theme-emerald");
        }
        scene.getRoot().setStyle(themeVariables + fontRule);
        scene.getRoot().applyCss();
    }
}
