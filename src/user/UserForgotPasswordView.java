package user;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Giao diện Khởi tạo lại mật khẩu JavaFX — UserForgotPasswordView (Mục 3)
 * Form tối giản: Nhập Email/Tên đăng nhập, hiển thị loading ProgressBar indeterminate.
 */
public class UserForgotPasswordView {

    private final StackPane root = new StackPane();
    private final TextField txtIdentity = new TextField("tuanminh@example.com");
    private final Button btnSend = new Button("Gửi mật khẩu mới qua Email");
    private final ProgressBar progressBar = new ProgressBar();
    private final Label lblStatus = new Label();
    private final Runnable onNavigateToLogin;

    public UserForgotPasswordView(Runnable onNavigateToLogin) {
        this.onNavigateToLogin = onNavigateToLogin;
        initUI();
    }

    public Parent getView() {
        return root;
    }

    private void initUI() {
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.CENTER);

        VBox card = new VBox(22);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);

        // Header
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("Khởi tạo lại mật khẩu");
        lblTitle.getStyleClass().add("header-title");

        Label lblSub = new Label("Nhập email hoặc tên đăng nhập của bạn để nhận mật khẩu mới.");
        lblSub.getStyleClass().add("header-subtitle");
        lblSub.setWrapText(true);
        lblSub.setAlignment(Pos.CENTER);

        headerBox.getChildren().addAll(lblTitle, lblSub);

        // Form Fields
        VBox formBox = new VBox(16);
        formBox.setAlignment(Pos.CENTER_LEFT);

        VBox inputBlock = new VBox(8);
        inputBlock.getChildren().addAll(
            JavaFXTheme.createFieldLabel("Email hoặc Tên đăng nhập", true),
            txtIdentity
        );
        txtIdentity.setPromptText("Nhập email đã đăng ký...");
        txtIdentity.textProperty().addListener((obs, oldVal, newVal) -> {
            JavaFXTheme.clearError(txtIdentity);
            lblStatus.setText("");
        });

        // Loading bar (an ban dau)
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.getStyleClass().add("indeterminate-bar");
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        lblStatus.getStyleClass().add("status-message");
        lblStatus.setWrapText(true);

        // CTA Button
        btnSend.getStyleClass().add("btn-primary");
        btnSend.setMaxWidth(Double.MAX_VALUE);
        btnSend.setOnAction(e -> handleResetPassword());

        // Footer Back to Login Link
        HBox footerBox = new HBox(4);
        footerBox.setAlignment(Pos.CENTER);

        Label lblBack = new Label("← Quay lại Đăng nhập");
        lblBack.getStyleClass().add("link-action");
        lblBack.setOnMouseClicked(e -> {
            if (onNavigateToLogin != null) {
                onNavigateToLogin.run();
            }
        });

        footerBox.getChildren().add(lblBack);

        formBox.getChildren().addAll(inputBlock, progressBar, lblStatus, btnSend, footerBox);
        card.getChildren().addAll(headerBox, formBox);

        // Vầng sáng Ambient Aura phát xuất trực tiếp từ phía sau thẻ Card
        Region cardAura = JavaFXTheme.createCardAmbientAura(card);
        root.getChildren().addAll(cardAura, card);
    }

    private void handleResetPassword() {
        String identity = txtIdentity.getText().trim();
        if (identity.isEmpty()) {
            JavaFXTheme.shakeNode(txtIdentity);
            txtIdentity.requestFocus();
            return;
        }

        // Bat dau trang thai loading indeterminate (gia lap ket noi Jakarta Mail SMTP)
        btnSend.setDisable(true);
        btnSend.setText("Đang kết nối Mail Server...");
        progressBar.setVisible(true);
        progressBar.setManaged(true);
        lblStatus.setText("Đang khởi tạo mật khẩu ngẫu nhiên và gửi tới hộp thư...");
        lblStatus.getStyleClass().removeAll("status-success", "status-error");

        PauseTransition delay = new PauseTransition(Duration.millis(1200));
        delay.setOnFinished(evt -> {
            btnSend.setDisable(false);
            btnSend.setText("Gửi mật khẩu mới qua Email");
            progressBar.setVisible(false);
            progressBar.setManaged(false);

            lblStatus.setText("✓ Mật khẩu mới đã được gửi thành công đến: " + identity + "\nVui lòng kiểm tra hộp thư đến (Inbox / Spam).");
            lblStatus.getStyleClass().add("status-success");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Gửi Email Thành công");
            alert.setHeaderText("Đã cấp lại mật khẩu qua Email");
            alert.setContentText("Hệ thống đã gửi mật khẩu ngẫu nhiên đến " + identity + ".\nBạn có thể quay lại màn hình Đăng nhập để truy cập tài khoản.");
            alert.showAndWait();
        });
        delay.play();
    }
}

