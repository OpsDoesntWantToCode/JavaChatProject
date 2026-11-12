package user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Hộp thoại Modal Thêm Thành Viên Vào Nhóm Chat (Mục 12.c trong final-project.pdf)
 * Thay thế hoàn toàn TextInputDialog mặc định của hệ thống bằng giao diện Obsidian Dark cao cấp:
 * - Thiết kế chuẩn mực bo góc 14px (radius.2xl), viền hairline 1px solid #2A2D35
 * - Hiệu ứng mở applyModalEntrance (180ms) và hiệu ứng đóng closeModalWithAnimation (120ms)
 * - Kiểm tra rỗng, rung lắc (shake) báo lỗi trực quan
 */
public class MemberAddDialog {

    private final Stage parentStage;
    private final Consumer<String> onMemberAdded;
    private String resultText = null;

    public MemberAddDialog(Stage parentStage, Consumer<String> onMemberAdded) {
        this.parentStage = parentStage;
        this.onMemberAdded = onMemberAdded;
    }

    public static Optional<String> show(Stage parentStage) {
        final String[] holder = new String[1];
        MemberAddDialog dialog = new MemberAddDialog(parentStage, input -> holder[0] = input);
        dialog.showAndWait();
        return Optional.ofNullable(holder[0]);
    }

    public void showAndWait() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (parentStage != null) {
            dialog.initOwner(parentStage);
        }
        dialog.setTitle("Thêm thành viên mới (Mục 12.c)");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("settings-dialog-card");
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.65), 32, 0, 0, 10);"
        );
        root.setPrefWidth(420);

        // 1. Tiêu đề hộp thoại
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblHeader = new Label("➕ Thêm bạn bè vào nhóm");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("dialog-close-btn");
        btnClose.setOnAction(e -> JavaFXTheme.closeModalWithAnimation(dialog, root));
        JavaFXTheme.applyIconButton(btnClose);

        headerRow.getChildren().addAll(lblHeader, sp, btnClose);

        // 2. Ô nhập Username hoặc họ tên
        VBox inputGroup = new VBox(8);
        Label lblPrompt = new Label("NHẬP USERNAME HOẶC HỌ TÊN BẠN BÈ *");
        lblPrompt.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        TextField txtInput = new TextField();
        txtInput.setPromptText("Ví dụ: @chihai_2026 hoặc Lưu Chí Hải...");
        txtInput.setStyle(
            "-fx-background-color: #0F1013; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-text-fill: -text-normal; " +
            "-fx-prompt-text-fill: -text-muted; " +
            "-fx-font-size: 13.5px; " +
            "-fx-padding: 10px 14px; " +
            "-fx-pref-height: 42px;"
        );

        Label lblHint = new Label("💡 Thành viên được thêm có thể đọc lịch sử tin nhắn mới và tham gia chat nhóm.");
        lblHint.setWrapText(true);
        lblHint.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted; -fx-line-spacing: 2px;");

        inputGroup.getChildren().addAll(lblPrompt, txtInput, lblHint);

        // 3. Nút hành động
        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        buttonRow.setPadding(new Insets(8, 0, 0, 0));

        Button btnCancel = new Button("Hủy bỏ");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setStyle(
            "-fx-background-color: #252832; -fx-border-color: #363A45; -fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; -fx-border-width: 1px; -fx-text-fill: -text-normal; " +
            "-fx-font-size: 13px; -fx-padding: 8px 18px; -fx-cursor: hand;"
        );
        btnCancel.setOnAction(e -> JavaFXTheme.closeModalWithAnimation(dialog, root));
        JavaFXTheme.applyTactileButton(btnCancel);

        Button btnAdd = new Button("Thêm vào nhóm ➕");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setStyle(
            "-fx-background-color: -accent-primary; -fx-border-radius: 8px; -fx-background-radius: 8px; " +
            "-fx-text-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 18px; -fx-cursor: hand;"
        );
        btnAdd.setOnAction(e -> {
            String val = txtInput.getText().trim();
            if (val.isEmpty()) {
                JavaFXTheme.shakeNode(txtInput);
                txtInput.requestFocus();
                return;
            }
            resultText = val;
            if (onMemberAdded != null) {
                onMemberAdded.accept(val);
            }
            JavaFXTheme.closeModalWithAnimation(dialog, root);
        });
        JavaFXTheme.applyTactileButton(btnAdd);

        txtInput.setOnAction(e -> btnAdd.fire());

        buttonRow.getChildren().addAll(btnCancel, btnAdd);

        root.getChildren().addAll(headerRow, inputGroup, buttonRow);

        Scene scene = new Scene(root);
        java.net.URL cssUrl = getClass().getResource("theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        dialog.setScene(scene);
        dialog.setOnShown(e -> {
            JavaFXTheme.applyModalEntrance(root);
            txtInput.requestFocus();
        });

        dialog.showAndWait();
    }
}
