package user;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Giao dien Dang nhap JavaFX - UserLoginView
 * Phong cach: Discord Authentic 100%
 */
public class UserLoginView {

    private final StackPane root = new StackPane();
    private final TextField txtUsername = new TextField("tuanminh_2026");
    private final PasswordField txtPassword = new PasswordField();
    private final Button btnLogin = new Button("Đăng nhập");
    private final Runnable onNavigateToRegister;

    public UserLoginView(Runnable onNavigateToRegister) {
        this.onNavigateToRegister = onNavigateToRegister;
        initUI();
    }

    public Parent getView() {
        return root;
    }

    private void initUI() {
        root.getStyleClass().add("ambient-canvas");
        root.setPadding(new Insets(24));

        // The Card trung tam
        VBox card = new VBox(16);
        card.getStyleClass().add("discord-card");
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER_LEFT);

        // 1. Header: Can giua
        VBox headerBox = new VBox(6);
        headerBox.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("Chào mừng trở lại!");
        lblTitle.getStyleClass().add("header-title");

        Label lblSub = new Label("Rất vui được gặp lại bạn một lần nữa!");
        lblSub.getStyleClass().add("header-subtitle");

        headerBox.getChildren().addAll(lblTitle, lblSub);

        // 2. Form Fields
        VBox formBox = new VBox(14);
        formBox.setAlignment(Pos.CENTER_LEFT);

        // Username
        VBox userBox = new VBox(6);
        userBox.getChildren().addAll(
            JavaFXTheme.createFieldLabel("Tên đăng nhập hoặc Email", true),
            txtUsername
        );
        txtUsername.setPromptText("Nhập username hoặc email của bạn...");
        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtUsername));

        // Password
        VBox passBox = new VBox(6);
        txtPassword.setText("Password@123");
        txtPassword.setPromptText("Nhập mật khẩu...");
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtPassword));

        Label lblForgot = new Label("Quên mật khẩu?");
        lblForgot.getStyleClass().add("discord-link");
        lblForgot.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quên mật khẩu");
            alert.setHeaderText(null);
            alert.setContentText("Chức năng Quên mật khẩu sẽ kích hoạt OTP gửi về Email (Task U2).");
            alert.showAndWait();
        });

        passBox.getChildren().addAll(
            JavaFXTheme.createFieldLabel("Mật khẩu", true),
            txtPassword,
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
        lblRegister.getStyleClass().add("discord-link");
        lblRegister.setOnMouseClicked(e -> {
            if (onNavigateToRegister != null) {
                onNavigateToRegister.run();
            }
        });

        footerBox.getChildren().addAll(lblNeed, lblRegister);

        formBox.getChildren().addAll(userBox, passBox, btnLogin, footerBox);
        card.getChildren().addAll(headerBox, formBox);

        root.getChildren().add(card);
    }

    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty()) {
            JavaFXTheme.shakeNode(txtUsername);
            txtUsername.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            JavaFXTheme.shakeNode(txtPassword);
            txtPassword.requestFocus();
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
            alert.setContentText("✓ Đăng nhập thành công với tài khoản: " + user + "\n(Giao diện JavaFX 21 + CSS đạt chuẩn Discord)");
            alert.showAndWait();
        });
        delay.play();
    }
}

