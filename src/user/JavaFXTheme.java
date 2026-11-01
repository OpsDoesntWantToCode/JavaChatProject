package user;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Lop tien ich giao dien cho JavaFX (Theme & Micro-interactions)
 */
public final class JavaFXTheme {

    private JavaFXTheme() {}

    /**
     * Tao nhan Field Label kem dau sao do bat buoc
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
     * Hieu ung Horizontal Shake 60fps khi loi validation (±8px trong 250ms)
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
     * Xoa vien do bao loi khi nguoi dung bat dau go lai
     */
    public static void clearError(Node node) {
        node.getStyleClass().remove("input-error");
    }

    /**
     * Tinh toan do manh mat khau tra ve % tu 0 den 100
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

