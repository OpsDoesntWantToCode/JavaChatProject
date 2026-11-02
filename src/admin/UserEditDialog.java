package admin;

import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Form hồ sơ tối thiểu phục vụ thêm/sửa trong A2, không tạo thông tin đăng nhập. */
final class UserEditDialog extends Dialog<UserRow> {
    private UserRow saved;

    UserEditDialog(Node owner, AdminUserStore store, UserRow current, boolean readOnly) {
        boolean creating = current == null;
        setTitle(readOnly ? "Chi tiết người dùng" : creating ? "Thêm người dùng mẫu" : "Cập nhật hồ sơ mẫu");
        setHeaderText(creating ? "Hồ sơ mới · Trạng thái Hoạt động · Vai trò USER" : current.username());
        AdminDialogs.prepare(this, owner);
        getDialogPane().setPrefWidth(520);
        getDialogPane().setId("user-edit-dialog");
        TextField username = field("profile-username", creating ? "" : current.username());
        TextField name = field("profile-name", creating ? "" : current.fullName());
        TextField email = field("profile-email", creating ? "" : current.email());
        TextArea address = new TextArea(creating || current.address() == null ? "" : current.address());
        address.setId("profile-address");
        address.setPrefRowCount(2);
        address.setWrapText(true);
        DatePicker dob = new DatePicker(creating ? null : current.dob());
        DateTimeFormatter birthDateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        dob.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate value) {
                return value == null ? "" : birthDateFormat.format(value);
            }
            @Override public LocalDate fromString(String value) {
                return value == null || value.isBlank() ? null : LocalDate.parse(value.strip(), birthDateFormat);
            }
        });
        dob.setId("profile-dob");
        dob.setPromptText("dd/MM/yyyy");
        ComboBox<UserRow.Gender> gender = new ComboBox<>();
        gender.getItems().addAll(UserRow.Gender.values());
        gender.setValue(creating ? null : current.gender());
        gender.setPromptText("Chưa cung cấp");
        gender.setId("profile-gender");
        Button clearGender = new Button("Bỏ chọn");
        clearGender.getStyleClass().add("secondary-button");
        clearGender.setOnAction(event -> gender.setValue(null));
        javafx.scene.layout.HBox genders = new javafx.scene.layout.HBox(8, gender, clearGender);
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 8, 0));
        ColumnConstraints labelColumn = new ColumnConstraints(130);
        ColumnConstraints fieldColumn = new ColumnConstraints(0, 300, Double.MAX_VALUE);
        fieldColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().setAll(labelColumn, fieldColumn);
        Node[] controls = {username, name, address, dob, genders, email};
        String[] labels = {"Username *", "Họ tên *", "Địa chỉ", "Ngày sinh", "Giới tính", "Email *"};
        for (int index = 0; index < controls.length; index++) {
            Label label = new Label(labels[index]);
            label.setLabelFor(controls[index] == genders ? gender : controls[index]);
            form.add(label, 0, index);
            form.add(controls[index], 1, index);
            GridPane.setHgrow(controls[index], Priority.ALWAYS);
        }
        if (!creating) {
            form.addRow(6, new Label("Trạng thái / Quyền"),
                    new Label(current.status().label() + " / " + current.role().name()));
            form.addRow(7, new Label("Ngày tạo"), new Label(UserManagementView.formatDate(current.createdAt())));
        }
        username.setEditable(!readOnly);
        name.setEditable(!readOnly);
        email.setEditable(!readOnly);
        address.setEditable(!readOnly);
        dob.setDisable(readOnly);
        gender.setDisable(readOnly);
        clearGender.setDisable(readOnly);
        Label error = new Label();
        error.setId("profile-error");
        error.getStyleClass().add("validation-message");
        error.setWrapText(true);
        error.setFocusTraversable(true);
        error.setVisible(false);
        error.setManaged(false);
        Label note = new Label(readOnly
                ? "Chế độ chỉ xem. Hồ sơ mẫu chưa kết nối Server/Database."
                : "* Bắt buộc. Chỉ thay đổi hồ sơ mẫu trong bộ nhớ. Không tạo tài khoản đăng nhập thật.");
        note.setWrapText(true);
        note.getStyleClass().add("muted");
        getDialogPane().setContent(new VBox(8, form, error, note));

        ButtonType close = new ButtonType(readOnly ? "Đóng" : "Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(close);
        // Dialog chỉ xem không trả ButtonType thành UserRow khi đóng.
        setResultConverter(button -> null);
        if (!readOnly) {
            ButtonType save = new ButtonType("Lưu hồ sơ mẫu", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().add(save);
            ((Button) getDialogPane().lookupButton(save)).addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    if (dob.getEditor().getText().isBlank()) { dob.setValue(null); }
                    else { dob.setValue(dob.getConverter().fromString(dob.getEditor().getText())); }
                    AdminUserStore.UserDraft draft = new AdminUserStore.UserDraft(username.getText(), name.getText(),
                            address.getText(), dob.getValue(), gender.getValue(), email.getText());
                    saved = creating ? store.add(draft) : store.update(current.id(), draft);
                } catch (IllegalArgumentException | DateTimeParseException exception) {
                    error.setText(exception instanceof DateTimeParseException
                            ? "Ngày sinh không hợp lệ. Hãy chọn bằng lịch hoặc nhập đúng định dạng hiển thị."
                            : exception.getMessage());
                    error.setVisible(true);
                    error.setManaged(true);
                    getDialogPane().getScene().getWindow().sizeToScene();
                    error.requestFocus();
                    event.consume();
                }
            });
            setResultConverter(button -> button == save ? saved : null);
        }
    }

    private static TextField field(String id, String value) {
        TextField input = new TextField(value);
        input.setId(id);
        return input;
    }
}
