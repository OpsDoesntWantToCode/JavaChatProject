package user;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Giao diện Đăng nhập JavaFX — UserLoginView
 * Phong cách: Modern Obsidian Dark Surface (Thẻ Card bo góc 14px, Nút con mắt ẩn/hiện mật khẩu)
 */
public class UserLoginView {

    private final StackPane root = new StackPane();
    private final TextField txtUsername = new TextField("tuanminh_2026");
    private final JavaFXTheme.PasswordFieldWrapper passWrapper = 
        new JavaFXTheme.PasswordFieldWrapper("Password@123", "Nhập mật khẩu của bạn...");
    private final Button btnLogin = new Button("Đăng nhập");

    private final Runnable onNavigateToRegister;
    private final Runnable onNavigateToForgotPassword;

    public UserLoginView(Runnable onNavigateToRegister, Runnable onNavigateToForgotPassword) {
        this.onNavigateToRegister = onNavigateToRegister;
        this.onNavigateToForgotPassword = onNavigateToForgotPassword;
        initUI();
    }

    public Parent getView() {
        return root;
    }

    private void initUI() {
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.CENTER);

        // Thẻ Card trung tâm — Bo góc thanh lịch 14px, nền Obsidian Dark
        VBox card = new VBox(22);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);

        // 1. Header căn giữa chuẩn mực
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("Chào mừng trở lại!");
        lblTitle.getStyleClass().add("header-title");

        Label lblSub = new Label("Rất vui được gặp lại bạn một lần nữa!");
        lblSub.getStyleClass().add("header-subtitle");

        headerBox.getChildren().addAll(lblTitle, lblSub);

        // 2. Form Fields
        VBox formBox = new VBox(16);
        formBox.setAlignment(Pos.CENTER_LEFT);

        // Username / Email
        VBox userBox = new VBox(8);
        userBox.getChildren().addAll(
            JavaFXTheme.createFieldLabel("Tên đăng nhập hoặc Email", true),
            txtUsername
        );
        txtUsername.setPromptText("Nhập username hoặc email...");
        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtUsername));

        // Password với nút con mắt ẩn/hiện mật khẩu
        VBox passBox = new VBox(8);
        Label lblForgot = new Label("Quên mật khẩu?");
        lblForgot.getStyleClass().add("link-action");
        lblForgot.setOnMouseClicked(e -> {
            if (onNavigateToForgotPassword != null) {
                onNavigateToForgotPassword.run();
            }
        });

        passBox.getChildren().addAll(
            JavaFXTheme.createFieldLabel("Mật khẩu", true),
            passWrapper.getContainer(),
            lblForgot
        );

        // CTA Button
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> handleLogin());

        // Footer Register Link
        HBox footerBox = new HBox(4);
        footerBox.setAlignment(Pos.CENTER_LEFT);

        Label lblNeed = new Label("Cần một tài khoản? ");
        lblNeed.getStyleClass().add("text-muted");

        Label lblRegister = new Label("Đăng ký");
        lblRegister.getStyleClass().add("link-action");
        lblRegister.setOnMouseClicked(e -> {
            if (onNavigateToRegister != null) {
                onNavigateToRegister.run();
            }
        });

        footerBox.getChildren().addAll(lblNeed, lblRegister);

        formBox.getChildren().addAll(userBox, passBox, btnLogin, footerBox);
        card.getChildren().addAll(headerBox, formBox);

        // Vầng sáng Ambient Aura phát xuất trực tiếp từ phía sau thẻ Card
        Region cardAura = JavaFXTheme.createCardAmbientAura(card);
        root.getChildren().addAll(cardAura, card);
    }

    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = passWrapper.getText().trim();

        if (user.isEmpty()) {
            JavaFXTheme.shakeNode(txtUsername);
            txtUsername.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            JavaFXTheme.shakeNode(passWrapper.getContainer());
            passWrapper.requestFocus();
            return;
        }

        // Loading state
        btnLogin.setDisable(true);
        btnLogin.setText("Đang đăng nhập...");

        PauseTransition delay = new PauseTransition(Duration.millis(600));
        delay.setOnFinished(evt -> {
            btnLogin.setDisable(false);
            btnLogin.setText("Đăng nhập");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("✓ Đăng nhập thành công với tài khoản: " + user + "\n(Giao diện JavaFX 21 + CSS đạt chuẩn Discord & Linear)");
            alert.showAndWait();
        });
        delay.play();
    }
}
