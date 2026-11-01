package user;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Giao dien Dang ky tai khoan JavaFX - UserRegisterView
 * Phong cach: Discord Authentic 100% (2 Cot can doi, Password Strength Realtime, Shake on Error)
 */
public class UserRegisterView {

    private final StackPane root = new StackPane();
    private final TextField txtUsername = new TextField("tuanminh_2026");
    private final TextField txtFullName = new TextField("Trần Nguyễn Tuấn Minh");
    private final TextField txtEmail = new TextField("tuanminh@example.com");
    private final TextField txtDob = new TextField("2004-10-24");
    private final TextField txtAddress = new TextField("227 Nguyễn Văn Cừ, Q.5");
    private final ComboBox<String> cmbGender = new ComboBox<>();
    private final PasswordField txtPassword = new PasswordField();
    private final PasswordField txtConfirmPassword = new PasswordField();
    private final Button btnRegister = new Button("Đăng ký tài khoản");

    private final ProgressBar pbarStrength = new ProgressBar(0.75);
    private final Label lblStrength = new Label("Độ mạnh: Rất an toàn ✓");

    private final Runnable onNavigateToLogin;

    public UserRegisterView(Runnable onNavigateToLogin) {
        this.onNavigateToLogin = onNavigateToLogin;
        initUI();
    }

    public Parent getView() {
        return root;
    }

    private void initUI() {
        root.getStyleClass().add("ambient-canvas");
        root.setPadding(new Insets(24));

        VBox card = new VBox(16);
        card.getStyleClass().add("discord-card");
        card.setMaxWidth(520);
        card.setAlignment(Pos.CENTER);

        // Header
        Label lblTitle = new Label("Tạo một tài khoản");
        lblTitle.getStyleClass().add("header-title");

        // GridPane 2 Cot
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Hang 0: Username & Full Name
        VBox pnlUser = createFieldBlock("Tên đăng nhập", true, txtUsername);
        txtUsername.setPromptText("minh_tran");
        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtUsername));

        VBox pnlName = createFieldBlock("Họ và tên", true, txtFullName);
        txtFullName.setPromptText("Nhập họ tên");
        txtFullName.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtFullName));

        grid.add(pnlUser, 0, 0);
        grid.add(pnlName, 1, 0);

        // Hang 1: Email & Ngay sinh
        VBox pnlEmail = createFieldBlock("Email", true, txtEmail);
        txtEmail.setPromptText("email@domain.com");
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtEmail));

        VBox pnlDob = createFieldBlock("Ngày sinh", true, txtDob);
        txtDob.setPromptText("YYYY-MM-DD (vd: 2004-10-24)");
        txtDob.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtDob));

        grid.add(pnlEmail, 0, 1);
        grid.add(pnlDob, 1, 1);

        // Hang 2: Dia chi & Gioi tinh (Thang hang ngang tuyet doi)
        VBox pnlAddress = createFieldBlock("Địa chỉ", true, txtAddress);
        txtAddress.setPromptText("Địa chỉ cư trú");
        txtAddress.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtAddress));

        cmbGender.getItems().addAll("Nam", "Nữ", "Khác");
        cmbGender.setValue("Nam");
        cmbGender.setMaxWidth(Double.MAX_VALUE);
        VBox pnlGender = createFieldBlock("Giới tính", true, cmbGender);

        grid.add(pnlAddress, 0, 2);
        grid.add(pnlGender, 1, 2);

        // Hang 3: Mat khau & Xac nhan
        txtPassword.setText("Password@123");
        txtPassword.setPromptText("Tối thiểu 6 ký tự");
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            JavaFXTheme.clearError(txtPassword);
            updatePasswordStrength(newVal);
        });

        VBox pnlPass = createFieldBlock("Mật khẩu", true, txtPassword);

        // Them thanh do do manh ngay duoi o password
        pbarStrength.setMaxWidth(Double.MAX_VALUE);
        pbarStrength.getStyleClass().add("strength-strong");
        lblStrength.getStyleClass().add("strength-label-strong");

        VBox strengthBox = new VBox(2, pbarStrength, lblStrength);
        pnlPass.getChildren().add(strengthBox);

        txtConfirmPassword.setText("Password@123");
        txtConfirmPassword.setPromptText("Nhập lại mật khẩu");
        txtConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtConfirmPassword));

        VBox pnlConfirm = createFieldBlock("Xác nhận mật khẩu", true, txtConfirmPassword);

        grid.add(pnlPass, 0, 3);
        grid.add(pnlConfirm, 1, 3);

        // CTA Button
        btnRegister.getStyleClass().add("btn-primary");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> handleRegister());

        // Footer Login Link
        HBox footerBox = new HBox(4);
        footerBox.setAlignment(Pos.CENTER_LEFT);

        Label lblHasAccount = new Label("Đã có tài khoản? ");
        lblHasAccount.getStyleClass().add("text-muted");

        Label lblLogin = new Label("Đăng nhập");
        lblLogin.getStyleClass().add("discord-link");
        lblLogin.setOnMouseClicked(e -> {
            if (onNavigateToLogin != null) {
                onNavigateToLogin.run();
            }
        });

        footerBox.getChildren().addAll(lblHasAccount, lblLogin);

        card.getChildren().addAll(lblTitle, grid, btnRegister, footerBox);
        root.getChildren().add(card);
    }

    private VBox createFieldBlock(String labelText, boolean isRequired, Control field) {
        VBox block = new VBox(6);
        block.setAlignment(Pos.CENTER_LEFT);
        block.getChildren().addAll(
            JavaFXTheme.createFieldLabel(labelText, isRequired),
            field
        );
        return block;
    }

    private void updatePasswordStrength(String pass) {
        int score = JavaFXTheme.calculatePasswordStrength(pass);
        double progress = score / 100.0;
        pbarStrength.setProgress(progress);

        pbarStrength.getStyleClass().removeAll("strength-weak", "strength-medium", "strength-strong");
        lblStrength.getStyleClass().removeAll("strength-label-weak", "strength-label-medium", "strength-label-strong");

        if (pass.isEmpty()) {
            lblStrength.setText("Vui lòng nhập mật khẩu");
            lblStrength.getStyleClass().add("strength-label-weak");
            pbarStrength.getStyleClass().add("strength-weak");
        } else if (score < 40) {
            lblStrength.setText("Độ mạnh: Yếu (Cần thêm số/ký tự hoa)");
            lblStrength.getStyleClass().add("strength-label-weak");
            pbarStrength.getStyleClass().add("strength-weak");
        } else if (score < 75) {
            lblStrength.setText("Độ mạnh: Trung bình");
            lblStrength.getStyleClass().add("strength-label-medium");
            pbarStrength.getStyleClass().add("strength-medium");
        } else {
            lblStrength.setText("Độ mạnh: Rất an toàn ✓");
            lblStrength.getStyleClass().add("strength-label-strong");
            pbarStrength.getStyleClass().add("strength-strong");
        }
    }

    private void handleRegister() {
        String user = txtUsername.getText().trim();
        String name = txtFullName.getText().trim();
        String pass = txtPassword.getText();
        String confirm = txtConfirmPassword.getText();

        if (user.isEmpty()) {
            JavaFXTheme.shakeNode(txtUsername);
            txtUsername.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            JavaFXTheme.shakeNode(txtFullName);
            txtFullName.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            JavaFXTheme.shakeNode(txtPassword);
            txtPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            JavaFXTheme.shakeNode(txtConfirmPassword);
            txtConfirmPassword.requestFocus();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Lỗi xác nhận");
            alert.setHeaderText(null);
            alert.setContentText("Mật khẩu xác nhận không khớp!");
            alert.showAndWait();
            return;
        }

        btnRegister.setDisable(true);
        btnRegister.setText("Đang tạo tài khoản...");

        PauseTransition delay = new PauseTransition(Duration.millis(700));
        delay.setOnFinished(evt -> {
            btnRegister.setDisable(false);
            btnRegister.setText("Đăng ký tài khoản");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("✓ Tạo tài khoản thành công cho: " + name + " (" + user + ")\n(JavaFX 21 + CSS Discord Theme)");
            alert.showAndWait();

            if (onNavigateToLogin != null) {
                onNavigateToLogin.run();
            }
        });
        delay.play();
    }
}

