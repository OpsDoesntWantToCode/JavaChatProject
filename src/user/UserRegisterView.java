package user;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Giao diện Đăng ký tài khoản JavaFX — UserRegisterView (Mục 1 trong spec)
 * Bố cục 2 Cột đối xứng (GridPane 50% - 50%), DatePicker chọn ngày sinh,
 * 2 ô mật khẩu có nút con mắt 👁 ẩn/hiện, thanh đo độ mạnh mật khẩu theo thời gian thực.
 */
public class UserRegisterView {

    private final StackPane root = new StackPane();
    private final TextField txtUsername = new TextField("tuanminh_2026");
    private final TextField txtFullName = new TextField("Trần Nguyễn Tuấn Minh");
    private final TextField txtEmail = new TextField("tuanminh@example.com");
    private final DatePicker dpDob = new DatePicker(LocalDate.of(2004, 10, 24));
    private final TextField txtAddress = new TextField("227 Nguyễn Văn Cừ, Q.5");
    private final ComboBox<String> cmbGender = new ComboBox<>();

    private final JavaFXTheme.PasswordFieldWrapper passWrapper = 
        new JavaFXTheme.PasswordFieldWrapper("Password@123", "Tối thiểu 6 ký tự");
    private final JavaFXTheme.PasswordFieldWrapper confirmPassWrapper = 
        new JavaFXTheme.PasswordFieldWrapper("Password@123", "Nhập lại mật khẩu");

    private final Button btnRegister = new Button("Đăng ký tài khoản");

    private final HBox strengthMeter = new HBox(4);
    private final Region[] segments = new Region[4];
    private final Label lblStrength = new Label();

    private final Runnable onNavigateToLogin;

    public UserRegisterView(Runnable onNavigateToLogin) {
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
        card.setMaxWidth(680);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setAlignment(Pos.CENTER);

        // Header
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("Tạo một tài khoản");
        lblTitle.getStyleClass().add("header-title");

        Label lblSub = new Label("Điền đầy đủ thông tin bên dưới để bắt đầu kết nối!");
        lblSub.getStyleClass().add("header-subtitle");

        headerBox.getChildren().addAll(lblTitle, lblSub);

        // GridPane 2 Cột cân đối thoáng đãng
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Hàng 0: Username & Full Name
        VBox pnlUser = createFieldBlock("Tên đăng nhập", true, txtUsername);
        txtUsername.setPromptText("minh_tran");
        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtUsername));

        VBox pnlName = createFieldBlock("Họ và tên", true, txtFullName);
        txtFullName.setPromptText("Nhập họ tên");
        txtFullName.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtFullName));

        grid.add(pnlUser, 0, 0);
        grid.add(pnlName, 1, 0);

        // Hàng 1: Email & Ngày sinh (DatePicker)
        VBox pnlEmail = createFieldBlock("Email", true, txtEmail);
        txtEmail.setPromptText("email@domain.com");
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtEmail));

        setupDatePicker(dpDob);
        VBox pnlDob = createFieldBlock("Ngày sinh", true, dpDob);

        grid.add(pnlEmail, 0, 1);
        grid.add(pnlDob, 1, 1);

        // Hàng 2: Địa chỉ & Giới tính
        VBox pnlAddress = createFieldBlock("Địa chỉ", true, txtAddress);
        txtAddress.setPromptText("Địa chỉ cư trú");
        txtAddress.textProperty().addListener((obs, oldVal, newVal) -> JavaFXTheme.clearError(txtAddress));

        cmbGender.getItems().addAll("Nam", "Nữ", "Khác");
        cmbGender.setValue("Nam");
        cmbGender.setMaxWidth(Double.MAX_VALUE);
        setupComboBox(cmbGender);
        VBox pnlGender = createFieldBlock("Giới tính", true, cmbGender);

        grid.add(pnlAddress, 0, 2);
        grid.add(pnlGender, 1, 2);

        // Hàng 3: Mật khẩu & Xác nhận mật khẩu (kèm Eye Toggle)
        VBox pnlPass = new VBox(6);
        pnlPass.setAlignment(Pos.CENTER_LEFT);
        pnlPass.getChildren().addAll(
            JavaFXTheme.createFieldLabel("Mật khẩu", true),
            passWrapper.getContainer()
        );

        // Lắng nghe thay đổi mật khẩu để cập nhật độ mạnh theo thời gian thực
        passWrapper.getPasswordField().textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        passWrapper.getTextField().textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));

        // Thanh đo độ mạnh mật khẩu 4 phân đoạn (5px) đặt ngay dưới ô mật khẩu
        strengthMeter.getStyleClass().add("strength-meter");
        strengthMeter.setAlignment(Pos.CENTER_LEFT);
        strengthMeter.setMaxWidth(Double.MAX_VALUE);
        for (int i = 0; i < 4; i++) {
            segments[i] = new Region();
            segments[i].getStyleClass().add("strength-segment");
            segments[i].setPrefHeight(5);
            segments[i].setMinHeight(5);
            segments[i].setMaxHeight(5);
            HBox.setHgrow(segments[i], Priority.ALWAYS);
            strengthMeter.getChildren().add(segments[i]);
        }
        lblStrength.getStyleClass().add("strength-label");

        VBox strengthBox = new VBox(6, strengthMeter, lblStrength);
        pnlPass.getChildren().add(strengthBox);

        // Khởi tạo trạng thái độ mạnh ban đầu theo mật khẩu mặc định
        updatePasswordStrength(passWrapper.getText());

        VBox pnlConfirm = new VBox(6);
        pnlConfirm.setAlignment(Pos.CENTER_LEFT);
        pnlConfirm.getChildren().addAll(
            JavaFXTheme.createFieldLabel("Xác nhận mật khẩu", true),
            confirmPassWrapper.getContainer()
        );

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
        lblLogin.getStyleClass().add("link-action");
        lblLogin.setOnMouseClicked(e -> {
            if (onNavigateToLogin != null) {
                onNavigateToLogin.run();
            }
        });

        footerBox.getChildren().addAll(lblHasAccount, lblLogin);

        card.getChildren().addAll(headerBox, grid, btnRegister, footerBox);
        root.getChildren().add(card);
    }

    private void setupDatePicker(DatePicker picker) {
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setPromptText("YYYY-MM-DD");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        picker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });
    }

    private void setupComboBox(ComboBox<String> combo) {
        // Custom cell factory bảo đảm tuyệt đối 100% theme tối, phân tách rõ ràng từng ô, không bị trùng màu khi hover
        combo.setCellFactory(lv -> new ListCell<String>() {
            {
                setPadding(new Insets(8, 12, 8, 12));
                selectedProperty().addListener((obs, oldV, isSel) -> updateCellVisuals());
                hoverProperty().addListener((obs, oldV, isHov) -> updateCellVisuals());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    updateCellVisuals();
                }
            }

            private void updateCellVisuals() {
                if (getItem() == null || isEmpty()) return;
                if (isSelected()) {
                    setStyle("-fx-background-color: #4E6BF2; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-font-size: 14.5px;");
                } else if (isHover()) {
                    setStyle("-fx-background-color: #262932; -fx-text-fill: #FFFFFF; -fx-font-weight: normal; -fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-font-size: 14.5px;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #EDEDED; -fx-font-weight: normal; -fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-font-size: 14.5px;");
                }
            }
        });

        combo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #EDEDED; -fx-font-size: 15px; -fx-background-color: transparent;");
                }
            }
        });

        // Hoạt ảnh Dropdown Animation: Truy xuất trực tiếp getPopupContent từ skin, mở trượt xuống 8px và Fade In trong 150ms
        combo.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                Platform.runLater(() -> {
                    Node popup = null;
                    if (combo.getSkin() instanceof ComboBoxListViewSkin<?> skin) {
                        popup = skin.getPopupContent();
                    }
                    if (popup == null) {
                        popup = combo.lookup(".combo-box-popup");
                    }
                    if (popup != null) {
                        popup.setOpacity(0.0);
                        popup.setTranslateY(-8);

                        FadeTransition ft = new FadeTransition(Duration.millis(150), popup);
                        ft.setFromValue(0.0);
                        ft.setToValue(1.0);

                        TranslateTransition tt = new TranslateTransition(Duration.millis(150), popup);
                        tt.setFromY(-8);
                        tt.setToY(0);

                        ParallelTransition pt = new ParallelTransition(ft, tt);
                        pt.play();
                    }
                });
            }
        });
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
        if (pass == null) pass = "";
        int score = JavaFXTheme.calculatePasswordStrength(pass);

        // Reset màu tất cả 4 phân đoạn
        for (Region seg : segments) {
            if (seg != null) {
                seg.getStyleClass().removeAll("strength-segment-weak", "strength-segment-medium", 
                                              "strength-segment-good", "strength-segment-strong");
            }
        }

        if (pass.isEmpty()) {
            lblStrength.setText("Vui lòng nhập mật khẩu");
            lblStrength.setStyle("-fx-text-fill: #949BA4;");
        } else if (score < 40) {
            segments[0].getStyleClass().add("strength-segment-weak");
            lblStrength.setText("Độ mạnh: Yếu (Cần thêm số/ký tự hoa)");
            lblStrength.setStyle("-fx-text-fill: #FA777C;");
        } else if (score < 70) {
            segments[0].getStyleClass().add("strength-segment-medium");
            segments[1].getStyleClass().add("strength-segment-medium");
            lblStrength.setText("Độ mạnh: Trung bình (Có thể an toàn hơn)");
            lblStrength.setStyle("-fx-text-fill: #F0B232;");
        } else if (score < 90) {
            segments[0].getStyleClass().add("strength-segment-good");
            segments[1].getStyleClass().add("strength-segment-good");
            segments[2].getStyleClass().add("strength-segment-good");
            lblStrength.setText("Độ mạnh: Khá (Bảo mật tốt)");
            lblStrength.setStyle("-fx-text-fill: #4E6BF2;");
        } else {
            for (Region seg : segments) {
                seg.getStyleClass().add("strength-segment-strong");
            }
            lblStrength.setText("Độ mạnh: Rất an toàn ✓");
            lblStrength.setStyle("-fx-text-fill: #23A55A;");
        }
    }

    private void handleRegister() {
        String user = txtUsername.getText().trim();
        String name = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String pass = passWrapper.getText();
        String confirm = confirmPassWrapper.getText();

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
        if (email.isEmpty()) {
            JavaFXTheme.shakeNode(txtEmail);
            txtEmail.requestFocus();
            return;
        }
        if (dpDob.getValue() == null) {
            JavaFXTheme.shakeNode(dpDob);
            dpDob.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            JavaFXTheme.shakeNode(passWrapper.getContainer());
            passWrapper.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            JavaFXTheme.shakeNode(confirmPassWrapper.getContainer());
            confirmPassWrapper.requestFocus();
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
            alert.setContentText("✓ Tạo tài khoản thành công cho: " + name + " (" + user + ")\n(JavaFX 21 + CSS Obsidian Theme)");
            alert.showAndWait();

            if (onNavigateToLogin != null) {
                onNavigateToLogin.run();
            }
        });
        delay.play();
    }
}
