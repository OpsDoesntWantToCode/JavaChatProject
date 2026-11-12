package user;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
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

    /**
     * Tạo đối tượng SVGPath chuẩn sắc thái -text-muted (#8E929B) tương thích hoàn hảo với JavaFX CSS
     */
    public static SVGPath createSvgIcon(String pathData, double scale) {
        SVGPath svg = new SVGPath();
        svg.setContent(pathData);
        svg.setFill(Color.web("#8E929B"));
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
     * Hiệu ứng nảy xúc giác (Tactile Micro-press) cho các nút CTA chính:
     * - Khi nhấn (Pressed): Co nhẹ xuống 0.975x trong 70ms với Fluent 2 Decelerate.
     * - Khi nhả (Released): Phục hồi 1.0x trong 80ms.
     * - Khi chuột rời khỏi vùng bấm khi đang nhấn: Tự động khôi phục 1.0x.
     */
    public static void applyTactileButton(Button button) {
        if (button == null) return;

        ScaleTransition pressAnim = new ScaleTransition(Duration.millis(70), button);
        pressAnim.setToX(0.975);
        pressAnim.setToY(0.975);
        pressAnim.setInterpolator(FLUENT_DECELERATE);

        ScaleTransition releaseAnim = new ScaleTransition(Duration.millis(80), button);
        releaseAnim.setToX(1.0);
        releaseAnim.setToY(1.0);
        releaseAnim.setInterpolator(FLUENT_DECELERATE);

        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!button.isDisabled()) {
                releaseAnim.stop();
                pressAnim.playFromStart();
            }
        });

        button.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            pressAnim.stop();
            releaseAnim.playFromStart();
        });

        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            pressAnim.stop();
            if (button.getScaleX() != 1.0 || button.getScaleY() != 1.0) {
                releaseAnim.playFromStart();
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
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
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
}
