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

import java.util.function.Consumer;

/**
 * Hộp thoại Modal Báo Cáo Vi Phạm / Spam (Task U13 — SpamReportDialog)
 * Triển khai theo chuẩn docs/ui-spec/chat-messaging.md (Mục 10):
 * - Hộp thoại modal căn giữa, bo góc radius.2xl (14px), nền -bg-card (#1B1D22)
 * - Hiển thị danh tính người bị báo cáo
 * - Phân loại lý do vi phạm (RadioButtons chuẩn)
 * - Vùng nhập chi tiết mô tả vi phạm
 * - Nút "Gửi báo cáo vi phạm" lưu vết vào bảng spam_reports
 */
public class SpamReportDialog {

    private final Stage parentStage;
    private final String targetName;
    private final String targetUsername;
    private final Consumer<String> onSubmitted;

    public SpamReportDialog(Stage parentStage, String targetName, String targetUsername, Consumer<String> onSubmitted) {
        this.parentStage = parentStage;
        this.targetName = targetName;
        this.targetUsername = targetUsername;
        this.onSubmitted = onSubmitted;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Báo cáo vi phạm / Spam");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("settings-dialog-card");

        // 1. Tiêu đề
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblHeader = new Label("⚠ Báo cáo vi phạm người dùng");
        lblHeader.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("dialog-close-btn");
        btnClose.setOnAction(e -> JavaFXTheme.closeModalWithAnimation(dialog, root));
        JavaFXTheme.applyIconButton(btnClose);

        headerRow.getChildren().addAll(lblHeader, sp, btnClose);

        // 2. Thẻ hiển thị đối tượng bị báo cáo
        HBox targetBox = new HBox(12);
        targetBox.setAlignment(Pos.CENTER_LEFT);
        targetBox.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 14px;");

        Circle avt = new Circle(16, Color.web("#FA777C"));
        Label lblAvtIcon = new Label("👤");
        lblAvtIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFFFFF;");
        StackPane avtBox = new StackPane(avt, lblAvtIcon);

        VBox targetMeta = new VBox(2);
        Label lblTName = new Label("Đối tượng: " + targetName);
        lblTName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        Label lblTUser = new Label(targetUsername);
        lblTUser.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        targetMeta.getChildren().addAll(lblTName, lblTUser);

        targetBox.getChildren().addAll(avtBox, targetMeta);

        // 3. Phân loại lý do vi phạm
        VBox reasonsBox = new VBox(10);
        Label lblReasonTitle = new Label("LÝ DO BÁO CÁO VI PHẠM *");
        lblReasonTitle.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        ToggleGroup reasonGroup = new ToggleGroup();
        RadioButton rb1 = new RadioButton("Gửi tin nhắn Spam / Quảng cáo rác phiền toái");
        RadioButton rb2 = new RadioButton("Hành vi quấy rối, xúc phạm hoặc đe dọa");
        RadioButton rb3 = new RadioButton("Nghi vấn lừa đảo hoặc giả mạo danh tính");
        RadioButton rb4 = new RadioButton("Lý do khác");

        rb1.setToggleGroup(reasonGroup);
        rb2.setToggleGroup(reasonGroup);
        rb3.setToggleGroup(reasonGroup);
        rb4.setToggleGroup(reasonGroup);
        rb1.setSelected(true);

        reasonsBox.getChildren().addAll(lblReasonTitle, rb1, rb2, rb3, rb4);

        // 4. Vùng nhập chi tiết
        VBox detailBox = new VBox(6);
        Label lblDetail = new Label("MÔ TẢ CHI TIẾT NỘI DUNG VI PHẠM");
        lblDetail.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        TextArea txtDetail = new TextArea();
        txtDetail.setPromptText("Cung cấp thêm thông tin hoặc bối cảnh cuộc trò chuyện để Ban quản trị đối soát lịch sử...");
        txtDetail.setPrefRowCount(3);
        txtDetail.setWrapText(true);
        txtDetail.getStyleClass().add("text-area");

        detailBox.getChildren().addAll(lblDetail, txtDetail);

        // 5. Nút bấm thao tác
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(6, 0, 0, 0));

        Button btnCancel = new Button("Hủy bỏ");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setOnAction(e -> JavaFXTheme.closeModalWithAnimation(dialog, root));
        JavaFXTheme.applyTactileButton(btnCancel);

        Button btnSubmit = new Button("Gửi báo cáo vi phạm ⚠");
        btnSubmit.getStyleClass().add("btn-danger-solid");
        JavaFXTheme.applyTactileButton(btnSubmit);
        btnSubmit.setOnAction(e -> {
            RadioButton selected = (RadioButton) reasonGroup.getSelectedToggle();
            String reason = selected != null ? selected.getText() : "Spam";

            JavaFXTheme.showInfoAlert(
                parentStage,
                "Báo cáo đã gửi",
                "Báo cáo vi phạm đã được ghi nhận",
                "Thông tin vi phạm đối với " + targetName + " đã được chuyển đến Ban quản trị (ghi nhận vào bảng spam_reports). Cảm ơn sự đóng góp của bạn!"
            );

            if (onSubmitted != null) {
                onSubmitted.accept(reason);
            }
            JavaFXTheme.closeModalWithAnimation(dialog, root);
        });

        buttonBar.getChildren().addAll(btnCancel, btnSubmit);

        root.getChildren().addAll(headerRow, targetBox, reasonsBox, detailBox, buttonBar);

        Scene scene = new Scene(root, 440, 480);
        java.net.URL cssUrl = getClass().getResource("theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.setOnShown(e -> JavaFXTheme.applyModalEntrance(root));
        dialog.showAndWait();
    }
}
