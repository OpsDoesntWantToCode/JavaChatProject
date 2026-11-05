package user;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Lớp tiện ích giao diện chuẩn hóa cho JavaFX 21 (Theme & Micro-interactions)
 * Hỗ trợ các tương tác mượt mà: Password Toggle Eye, Shake on Error, Strength Meter, Card Ambient Aura.
 */
public final class JavaFXTheme {

    private JavaFXTheme() {}

    /**
     * Tạo lớp đệm vầng sáng Ambient Aura phát xuất trực tiếp từ phía sau thẻ Card (Login / Register / Forgot Password)
     * Vầng sáng tự động đồng bộ kích thước và vị trí theo viền thẻ (+80px bề rộng và chiều cao).
     */
    public static Region createCardAmbientAura(Region card) {
        Region aura = new Region();
        aura.getStyleClass().add("card-ambient-aura");
        aura.setMouseTransparent(true);

        // Mở rộng viền hào quang phát xuất từ 4 cạnh của thẻ Card (40px mỗi cạnh)
        aura.prefWidthProperty().bind(card.widthProperty().add(80));
        aura.prefHeightProperty().bind(card.heightProperty().add(80));
        aura.minWidthProperty().bind(aura.prefWidthProperty());
        aura.minHeightProperty().bind(aura.prefHeightProperty());
        aura.maxWidthProperty().bind(aura.prefWidthProperty());
        aura.maxHeightProperty().bind(aura.prefHeightProperty());

        return aura;
    }

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

            // Nut toggle con mat
            btnToggle.getStyleClass().add("btn-eye-toggle");
            btnToggle.setFocusTraversable(false);
            btnToggle.setOnAction(e -> toggleReveal());

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
}
