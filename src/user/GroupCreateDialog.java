package user;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hộp thoại Modal Tạo Nhóm Chat Mới (Task U12 — GroupCreateDialog)
 * Triển khai theo chuẩn docs/ui-spec/group-chat-encryption.md (Mục 12.a):
 * - Hộp thoại modal căn giữa, bo góc radius.2xl (14px), nền -bg-card (#1B1D22)
 * - Ô nhập Tên nhóm (cao 44px, bo góc 9px)
 * - Công tắc bật mã hóa nhóm E2EE phong cách Facebook Messenger
 * - Danh sách chọn bạn bè với CheckBox (lấy từ ma trận dummy-data-matrix.md)
 * - Ràng buộc: Nút "Tạo nhóm" chỉ kích hoạt khi có tên nhóm và tối thiểu 1 thành viên được chọn
 */
public class GroupCreateDialog {

    public static class FriendSelectable {
        private final String id;
        private final String name;
        private final String username;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public FriendSelectable(String id, String name, String username) {
            this.id = id;
            this.name = name;
            this.username = username;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUsername() { return username; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean val) { selected.set(val); }
        public BooleanProperty selectedProperty() { return selected; }
    }

    public static class GroupResult {
        private final String groupName;
        private final boolean encrypted;
        private final List<String> memberNames;

        public GroupResult(String groupName, boolean encrypted, List<String> memberNames) {
            this.groupName = groupName;
            this.encrypted = encrypted;
            this.memberNames = memberNames;
        }

        public String getGroupName() { return groupName; }
        public boolean isEncrypted() { return encrypted; }
        public List<String> getMemberNames() { return memberNames; }
    }

    private final Stage parentStage;
    private final Consumer<GroupResult> onCreated;

    public GroupCreateDialog(Stage parentStage, Consumer<GroupResult> onCreated) {
        this.parentStage = parentStage;
        this.onCreated = onCreated;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Tạo nhóm trò chuyện mới");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: -bg-card; -fx-border-color: -border-subtle; -fx-border-width: 1px;");

        // 1. Tiêu đề hộp thoại
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblHeader = new Label("Tạo nhóm trò chuyện mới");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("dialog-close-btn");
        btnClose.setOnAction(e -> dialog.close());

        headerRow.getChildren().addAll(lblHeader, sp, btnClose);

        // 2. Ô nhập Tên nhóm (cao 44px, bo góc 9px)
        VBox nameGroup = new VBox(6);
        Label lblName = new Label("TÊN NHÓM *");
        lblName.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        TextField txtGroupName = new TextField();
        txtGroupName.setPromptText("Nhập tên nhóm trò chuyện (ví dụ: Nhóm Đồ Án Java 2026)...");
        txtGroupName.getStyleClass().add("text-field");
        txtGroupName.setStyle("-fx-pref-height: 44px; -fx-background-radius: 9px; -fx-border-radius: 9px; -fx-padding: 0 14px;");

        nameGroup.getChildren().addAll(lblName, txtGroupName);

        // 3. Công tắc bật mã hóa nhóm E2EE Messenger style (Mục 12.g)
        HBox e2eeBox = new HBox(10);
        e2eeBox.setAlignment(Pos.CENTER_LEFT);
        e2eeBox.setStyle("-fx-background-color: #141824; -fx-border-color: #263354; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px 14px;");

        CheckBox chkE2ee = new CheckBox("🔒 Bật mã hóa nhóm (E2EE Messenger Style)");
        chkE2ee.setSelected(true);
        chkE2ee.setStyle("-fx-text-fill: #A0C0FF; -fx-font-size: 13px; -fx-font-weight: bold;");

        VBox e2eeInfo = new VBox(2);
        Label lblSubE2ee = new Label("Mọi tin nhắn sẽ được bảo vệ bằng mã hóa đối xứng AES-GCM 256-bit.");
        lblSubE2ee.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        e2eeInfo.getChildren().addAll(chkE2ee, lblSubE2ee);
        e2eeBox.getChildren().add(e2eeInfo);

        // 4. Danh sách chọn thành viên từ Danh bạ bạn bè (Mục 12.a)
        VBox friendsGroup = new VBox(6);
        Label lblSelectFriends = new Label("CHỌN THÀNH VIÊN BAN ĐẦU (Tối thiểu 1 người)");
        lblSelectFriends.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        ObservableList<FriendSelectable> friends = FXCollections.observableArrayList(
            new FriendSelectable("chihai", "Lưu Chí Hải", "@chihai_2026"),
            new FriendSelectable("lananh", "Nguyễn Lan Anh", "@lananh_it"),
            new FriendSelectable("namle", "Lê Hoàng Nam", "@nam_le"),
            new FriendSelectable("duchuy", "Phạm Đức Huy", "@duc_huy"),
            new FriendSelectable("thuha", "Vũ Thu Hà", "@thu_ha")
        );

        // Chọn mặc định Hải và Lan Anh như kịch bản dummy-data-matrix.md
        friends.get(0).setSelected(true);
        friends.get(1).setSelected(true);

        ListView<FriendSelectable> listView = new ListView<>(friends);
        listView.setPrefHeight(160);
        listView.getStyleClass().add("transparent-scroll");
        listView.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FriendSelectable item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(6, 10, 6, 10));

                    CheckBox chk = new CheckBox();
                    chk.selectedProperty().bindBidirectional(item.selectedProperty());

                    Circle avt = new Circle(14, Color.web("#4E6BF2"));
                    Label lblInitial = new Label(item.getName().substring(0, 1));
                    lblInitial.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
                    StackPane avtBox = new StackPane(avt, lblInitial);

                    VBox meta = new VBox(1);
                    Label lblN = new Label(item.getName());
                    lblN.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
                    Label lblU = new Label(item.getUsername());
                    lblU.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
                    meta.getChildren().addAll(lblN, lblU);

                    row.getChildren().addAll(chk, avtBox, meta);
                    setGraphic(row);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        friendsGroup.getChildren().addAll(lblSelectFriends, listView);

        // 5. Nút Thao tác dưới đáy
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(8, 0, 0, 0));

        Button btnCancel = new Button("Hủy");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnCreate = new Button("Tạo nhóm 🚀");
        btnCreate.getStyleClass().add("btn-primary");
        btnCreate.setStyle("-fx-pref-height: 40px; -fx-padding: 0 20px;");

        // Ràng buộc bảo đảm: Chỉ kích hoạt khi tên không rỗng và chọn >= 1 thành viên
        btnCreate.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> txtGroupName.getText().trim().isEmpty() || friends.stream().noneMatch(FriendSelectable::isSelected),
                txtGroupName.textProperty(),
                friends.get(0).selectedProperty(),
                friends.get(1).selectedProperty(),
                friends.get(2).selectedProperty(),
                friends.get(3).selectedProperty(),
                friends.get(4).selectedProperty()
            )
        );

        btnCreate.setOnAction(e -> {
            String gName = txtGroupName.getText().trim();
            boolean isEncrypted = chkE2ee.isSelected();
            List<String> chosenMembers = new ArrayList<>();
            for (FriendSelectable f : friends) {
                if (f.isSelected()) chosenMembers.add(f.getName());
            }

            if (onCreated != null) {
                onCreated.accept(new GroupResult(gName, isEncrypted, chosenMembers));
            }
            dialog.close();
        });

        buttonBar.getChildren().addAll(btnCancel, btnCreate);

        root.getChildren().addAll(headerRow, nameGroup, e2eeBox, friendsGroup, buttonBar);

        Scene scene = new Scene(root, 460, 520);
        java.net.URL cssUrl = getClass().getResource("theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }
}
