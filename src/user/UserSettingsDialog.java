package user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại Cài đặt & Hồ sơ cá nhân (Mục 2 & Mục 4 trong đề bài)
 * - Tab 1: Cập nhật thông tin cá nhân (Họ tên, Địa chỉ, Ngày sinh, Giới tính)
 * - Tab 2: Bảo mật & Đổi mật khẩu (Mật khẩu cũ, Mật khẩu mới kèm thanh đo độ mạnh, Xác nhận)
 * Thiết kế công thái học Obsidian Dark Surface, bo góc chọn lọc 14px, viền hairline 1px #2A2D35.
 */
public class UserSettingsDialog extends Stage {

    private final VBox rootNode = new VBox(16);
    private final VBox tabProfileContent = new VBox(14);
    private final VBox tabSecurityContent = new VBox(14);
    private final Button btnTabProfile = new Button("👤 Hồ sơ cá nhân");
    private final Button btnTabSecurity = new Button("🔒 Bảo mật & Đổi mật khẩu");

    // Các trường Tab 1: Hồ sơ
    private final TextField txtUsername = new TextField("tuanminh_2026");
    private final TextField txtFullName = new TextField("Trần Nguyễn Tuấn Minh");
    private final TextField txtEmail = new TextField("tuanminh@gmail.com");
    private final DatePicker dpBirthDate = new DatePicker(LocalDate.of(2004, 10, 29));
    private final ComboBox<String> cbGender = new ComboBox<>();
    private final TextField txtAddress = new TextField("TP. Hồ Chí Minh");
    private final Label lblProfileStatus = new Label();

    // Các trường Tab 2: Đổi mật khẩu
    private final JavaFXTheme.PasswordFieldWrapper passCurrent = new JavaFXTheme.PasswordFieldWrapper("", "Nhập mật khẩu hiện tại");
    private final JavaFXTheme.PasswordFieldWrapper passNew = new JavaFXTheme.PasswordFieldWrapper("", "Nhập mật khẩu mới (ít nhất 6 ký tự)");
    private final JavaFXTheme.PasswordFieldWrapper passConfirm = new JavaFXTheme.PasswordFieldWrapper("", "Nhập lại mật khẩu mới");
    private final HBox strengthMeterBox = new HBox(4);
    private final Region[] strengthSegments = new Region[4];
    private final Label lblStrength = new Label("Độ mạnh: Chưa nhập");
    private final Label lblSecurityStatus = new Label();

    public UserSettingsDialog(Window owner) {
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle("Cài đặt tài khoản & Hồ sơ cá nhân");
        setResizable(false);

        rootNode.getStyleClass().add("settings-dialog-card");
        rootNode.setPadding(new Insets(24));
        rootNode.setPrefWidth(540);

        // 1. Header hộp thoại
        HBox headerBox = createHeader();

        // 2. Bộ chuyển đổi Tab dạng Segmented Control
        HBox tabSelector = createTabSelector();

        // 3. Khởi tạo nội dung 2 Tabs
        initProfileTab();
        initSecurityTab();

        StackPane contentStack = new StackPane();
        contentStack.getChildren().addAll(tabProfileContent, tabSecurityContent);

        // Mặc định hiển thị Tab 1
        selectTab(true);

        rootNode.getChildren().addAll(headerBox, tabSelector, contentStack);

        Scene scene = new Scene(rootNode);
        java.net.URL cssUrl = getClass().getResource("theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        setScene(scene);
        centerOnScreen();

        // Hiệu ứng phóng nhẹ 0.96x -> 1.0x + Fade-in theo chuẩn Microsoft Fluent 2
        setOnShown(e -> JavaFXTheme.applyModalEntrance(rootNode));
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Label lblTitle = new Label("Cài đặt & Hồ sơ cá nhân");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Label lblSub = new Label("Quản lý thông tin tài khoản và thiết lập an toàn bảo mật");
        lblSub.setStyle("-fx-font-size: 12.5px; -fx-text-fill: -text-muted;");
        titleBox.getChildren().addAll(lblTitle, lblSub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("dialog-close-btn");
        btnClose.setOnAction(e -> JavaFXTheme.closeModalWithAnimation(this, rootNode));
        JavaFXTheme.applyIconButton(btnClose);

        header.getChildren().addAll(titleBox, btnClose);
        return header;
    }

    private HBox createTabSelector() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("settings-tab-bar");

        btnTabProfile.getStyleClass().add("settings-tab-btn");
        btnTabSecurity.getStyleClass().add("settings-tab-btn");

        btnTabProfile.setOnAction(e -> selectTab(true));
        btnTabSecurity.setOnAction(e -> selectTab(false));

        bar.getChildren().addAll(btnTabProfile, btnTabSecurity);
        return bar;
    }

    private void selectTab(boolean isProfile) {
        tabProfileContent.setVisible(isProfile);
        tabProfileContent.setManaged(isProfile);
        tabSecurityContent.setVisible(!isProfile);
        tabSecurityContent.setManaged(!isProfile);

        if (isProfile) {
            btnTabProfile.getStyleClass().add("settings-tab-btn-active");
            btnTabSecurity.getStyleClass().remove("settings-tab-btn-active");
            JavaFXTheme.applyTabContentTransition(tabProfileContent, false);
        } else {
            btnTabSecurity.getStyleClass().add("settings-tab-btn-active");
            btnTabProfile.getStyleClass().remove("settings-tab-btn-active");
            JavaFXTheme.applyTabContentTransition(tabSecurityContent, true);
        }
    }

    private void initProfileTab() {
        // Avatar Mini Preview
        HBox avatarBox = new HBox(14);
        avatarBox.setAlignment(Pos.CENTER_LEFT);
        avatarBox.getStyleClass().add("settings-avatar-row");

        StackPane avatarCircle = new StackPane();
        Circle bgCircle = new Circle(26, Color.web("#4E6BF2"));
        Label lblInitials = new Label("TM");
        lblInitials.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        avatarCircle.getChildren().addAll(bgCircle, lblInitials);

        VBox avatarMeta = new VBox(4);
        Label lblName = new Label("Trần Nguyễn Tuấn Minh");
        lblName.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblTag = new Label("@tuanminh_2026 • Đang trực tuyến");
        lblTag.setStyle("-fx-font-size: 12px; -fx-text-fill: -status-online;");
        avatarMeta.getChildren().addAll(lblName, lblTag);

        Button btnChangeAvatar = new Button("Đổi ảnh đại diện");
        btnChangeAvatar.getStyleClass().add("btn-secondary");
        JavaFXTheme.applyTactileButton(btnChangeAvatar);
        btnChangeAvatar.setOnAction(e -> {
            lblProfileStatus.setText("✓ Tính năng tải ảnh avatar sẽ kết nối Socket ở mốc 80%");
            lblProfileStatus.getStyleClass().setAll("status-message", "status-success");
        });

        HBox.setHgrow(avatarMeta, Priority.ALWAYS);
        avatarBox.getChildren().addAll(avatarCircle, avatarMeta, btnChangeAvatar);

        // GridPane 2 Cột trường nhập
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Hàng 0: Username (Chỉ đọc) & Email (Chỉ đọc)
        txtUsername.setDisable(true);
        txtUsername.setStyle("-fx-opacity: 0.75;");
        VBox pnlUser = createFieldBlock("Tên đăng nhập (Cố định)", false, txtUsername);

        txtEmail.setDisable(true);
        txtEmail.setStyle("-fx-opacity: 0.75;");
        VBox pnlEmail = createFieldBlock("Hộp thư Email (Cố định)", false, txtEmail);
        grid.add(pnlUser, 0, 0);
        grid.add(pnlEmail, 1, 0);

        // Hàng 1: Họ tên & Ngày sinh
        VBox pnlName = createFieldBlock("Họ và tên", true, txtFullName);
        dpBirthDate.setMaxWidth(Double.MAX_VALUE);
        VBox pnlDob = createFieldBlock("Ngày sinh", true, dpBirthDate);
        grid.add(pnlName, 0, 1);
        grid.add(pnlDob, 1, 1);

        // Hàng 2: Giới tính & Địa chỉ
        cbGender.getItems().addAll("Nam", "Nữ", "Khác");
        cbGender.setValue("Nam");
        cbGender.setMaxWidth(Double.MAX_VALUE);
        VBox pnlGender = createFieldBlock("Giới tính", true, cbGender);

        VBox pnlAddress = createFieldBlock("Địa chỉ cư trú", true, txtAddress);
        grid.add(pnlGender, 0, 2);
        grid.add(pnlAddress, 1, 2);

        // Nút lưu & Status
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        lblProfileStatus.getStyleClass().add("status-message");
        HBox.setHgrow(lblProfileStatus, Priority.ALWAYS);

        Button btnCancel = new Button("Đóng");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setOnAction(e -> JavaFXTheme.closeModalWithAnimation(this, rootNode));
        JavaFXTheme.applyTactileButton(btnCancel);

        Button btnSave = new Button("Lưu thay đổi hồ sơ");
        btnSave.getStyleClass().add("btn-primary");
        btnSave.setOnAction(e -> handleSaveProfile());
        JavaFXTheme.applyTactileButton(btnSave);

        actionRow.getChildren().addAll(lblProfileStatus, btnCancel, btnSave);

        tabProfileContent.getChildren().addAll(avatarBox, grid, actionRow);
    }

    private void handleSaveProfile() {
        String name = txtFullName.getText().trim();
        String addr = txtAddress.getText().trim();

        if (name.isEmpty()) {
            JavaFXTheme.shakeNode(txtFullName);
            lblProfileStatus.setText("Vui lòng không để trống Họ và tên!");
            lblProfileStatus.getStyleClass().setAll("status-message", "status-error");
            txtFullName.requestFocus();
            return;
        }

        if (addr.isEmpty()) {
            JavaFXTheme.shakeNode(txtAddress);
            lblProfileStatus.setText("Vui lòng không để trống Địa chỉ!");
            lblProfileStatus.getStyleClass().setAll("status-message", "status-error");
            txtAddress.requestFocus();
            return;
        }

        lblProfileStatus.setText("✓ Đã cập nhật thông tin hồ sơ thành công!");
        lblProfileStatus.getStyleClass().setAll("status-message", "status-success");
    }

    private void initSecurityTab() {
        VBox pnlCurrent = createFieldBlock("Mật khẩu hiện tại", true, passCurrent.getContainer());
        VBox pnlNew = createFieldBlock("Mật khẩu mới", true, passNew.getContainer());

        // Thanh đo độ mạnh mật khẩu 4 phân đoạn
        initStrengthMeter();
        passNew.getPasswordField().textProperty().addListener((obs, oldV, newV) -> updatePasswordStrength(newV));
        passNew.getTextField().textProperty().addListener((obs, oldV, newV) -> updatePasswordStrength(newV));

        VBox pnlConfirm = createFieldBlock("Xác nhận mật khẩu mới", true, passConfirm.getContainer());

        // Hàng nút bấm & Status
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        lblSecurityStatus.getStyleClass().add("status-message");
        HBox.setHgrow(lblSecurityStatus, Priority.ALWAYS);

        Button btnCancel = new Button("Đóng");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setOnAction(e -> JavaFXTheme.closeModalWithAnimation(this, rootNode));
        JavaFXTheme.applyTactileButton(btnCancel);

        Button btnChangePass = new Button("Cập nhật mật khẩu");
        btnChangePass.getStyleClass().add("btn-primary");
        btnChangePass.setOnAction(e -> handleChangePassword());
        JavaFXTheme.applyTactileButton(btnChangePass);

        actionRow.getChildren().addAll(lblSecurityStatus, btnCancel, btnChangePass);

        tabSecurityContent.getChildren().addAll(
            pnlCurrent,
            pnlNew,
            strengthMeterBox,
            lblStrength,
            pnlConfirm,
            actionRow
        );
    }

    private void initStrengthMeter() {
        strengthMeterBox.getStyleClass().add("strength-meter");
        for (int i = 0; i < 4; i++) {
            strengthSegments[i] = new Region();
            strengthSegments[i].getStyleClass().add("strength-segment");
            HBox.setHgrow(strengthSegments[i], Priority.ALWAYS);
            strengthMeterBox.getChildren().add(strengthSegments[i]);
        }
        lblStrength.getStyleClass().add("strength-label");
        lblStrength.setStyle("-fx-text-fill: -text-muted;");
    }

    private void updatePasswordStrength(String password) {
        int score = JavaFXTheme.calculatePasswordStrength(password);
        for (int i = 0; i < 4; i++) {
            strengthSegments[i].getStyleClass().removeAll(
                "strength-segment-weak",
                "strength-segment-medium",
                "strength-segment-good",
                "strength-segment-strong"
            );
        }

        if (password == null || password.isEmpty()) {
            lblStrength.setText("Độ mạnh: Chưa nhập");
            lblStrength.setStyle("-fx-text-fill: -text-muted;");
            return;
        }

        if (score <= 25) {
            strengthSegments[0].getStyleClass().add("strength-segment-weak");
            lblStrength.setText("Độ mạnh: Yếu (Cần thêm số/chữ hoa/ký tự)");
            lblStrength.setStyle("-fx-text-fill: -status-danger;");
        } else if (score <= 50) {
            strengthSegments[0].getStyleClass().add("strength-segment-medium");
            strengthSegments[1].getStyleClass().add("strength-segment-medium");
            lblStrength.setText("Độ mạnh: Trung bình (Có thể an toàn hơn)");
            lblStrength.setStyle("-fx-text-fill: -status-warning;");
        } else if (score <= 75) {
            strengthSegments[0].getStyleClass().add("strength-segment-good");
            strengthSegments[1].getStyleClass().add("strength-segment-good");
            strengthSegments[2].getStyleClass().add("strength-segment-good");
            lblStrength.setText("Độ mạnh: Khá (Bảo mật tốt)");
            lblStrength.setStyle("-fx-text-fill: -accent-primary;");
        } else {
            for (int i = 0; i < 4; i++) {
                strengthSegments[i].getStyleClass().add("strength-segment-strong");
            }
            lblStrength.setText("Độ mạnh: Rất an toàn ✓");
            lblStrength.setStyle("-fx-text-fill: -status-online;");
        }
    }

    private void handleChangePassword() {
        String cur = passCurrent.getText().trim();
        String nw = passNew.getText().trim();
        String cf = passConfirm.getText().trim();

        if (cur.isEmpty()) {
            JavaFXTheme.shakeNode(passCurrent.getContainer());
            lblSecurityStatus.setText("Vui lòng nhập mật khẩu hiện tại!");
            lblSecurityStatus.getStyleClass().setAll("status-message", "status-error");
            passCurrent.requestFocus();
            return;
        }

        if (nw.length() < 6) {
            JavaFXTheme.shakeNode(passNew.getContainer());
            lblSecurityStatus.setText("Mật khẩu mới phải có ít nhất 6 ký tự!");
            lblSecurityStatus.getStyleClass().setAll("status-message", "status-error");
            passNew.requestFocus();
            return;
        }

        if (!nw.equals(cf)) {
            JavaFXTheme.shakeNode(passConfirm.getContainer());
            lblSecurityStatus.setText("Xác nhận mật khẩu mới không khớp!");
            lblSecurityStatus.getStyleClass().setAll("status-message", "status-error");
            passConfirm.requestFocus();
            return;
        }

        lblSecurityStatus.setText("✓ Đổi mật khẩu thành công! Hãy dùng mật khẩu mới ở lần đăng nhập tiếp theo.");
        lblSecurityStatus.getStyleClass().setAll("status-message", "status-success");
        passCurrent.clear();
        passNew.clear();
        passConfirm.clear();
    }

    private VBox createFieldBlock(String label, boolean isRequired, javafx.scene.Node inputNode) {
        VBox box = new VBox(6);
        box.getChildren().addAll(JavaFXTheme.createFieldLabel(label, isRequired), inputNode);
        return box;
    }
}
