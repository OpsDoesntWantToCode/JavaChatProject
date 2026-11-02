package admin;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Bảng quản trị A2 với dữ liệu mẫu; thao tác không gọi Server hoặc Database. */
public final class UserManagementView extends VBox {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final AdminUserStore store;
    private final TableView<UserRow> table = new TableView<>();
    private final TextField nameFilter = new TextField();
    private final TextField usernameFilter = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final Label summary = new Label();
    private final Label resultCount = new Label();
    private final Label selectionHint = new Label("Chọn một người dùng để xem hoặc thao tác.");
    private final Label feedback = new Label("Dữ liệu mẫu · Chỉ lưu trong bộ nhớ, khởi động lại sẽ trở về dữ liệu ban đầu.");
    private final Button detail = button("Chi tiết", "user-detail");
    private final Button edit = button("Cập nhật", "user-edit");
    private final Button remove = button("Xóa mềm", "user-delete");
    private final Button lock = button("Khóa", "user-lock");
    private final Button password = button("Cập nhật mật khẩu", "user-password");
    private final FilteredList<UserRow> filtered;

    public UserManagementView(AdminUserStore store) {
        this.store = store;
        setId("user-management");
        getStyleClass().add("user-management");
        setSpacing(12);
        setMinSize(0, 0);
        filtered = new FilteredList<>(store.users());
        SortedList<UserRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        buildTable();

        Label badge = new Label("DỮ LIỆU MẪU");
        badge.getStyleClass().add("preview-badge");
        summary.getStyleClass().add("muted");
        summary.setId("user-summary");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox top = new HBox(12, badge, gap, summary);
        top.setAlignment(Pos.CENTER_LEFT);
        nameFilter.setPromptText("Nhập họ tên…");
        nameFilter.setId("filter-full-name");
        usernameFilter.setPromptText("Nhập username…");
        usernameFilter.setId("filter-username");
        statusFilter.getItems().add("Tất cả trạng thái");
        for (UserRow.Status value : UserRow.Status.values()) { statusFilter.getItems().add(value.label()); }
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setId("filter-status");
        statusFilter.setPrefWidth(150);
        Button clear = button("Xóa bộ lọc", "clear-user-filters");
        clear.setOnAction(event -> clearFilters());
        HBox filters = new HBox(10, filterField("Họ tên", nameFilter),
                filterField("Tên đăng nhập", usernameFilter), filterField("Trạng thái", statusFilter), clear);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        nameFilter.textProperty().addListener((observable, old, value) -> applyFilters());
        usernameFilter.textProperty().addListener((observable, old, value) -> applyFilters());
        statusFilter.valueProperty().addListener((observable, old, value) -> applyFilters());

        Button add = button("+ Thêm người dùng", "user-add");
        add.getStyleClass().add("primary-button");
        add.setOnAction(event -> new UserEditDialog(this, store, null, false).showAndWait()
                .ifPresent(row -> afterMutation(row, "Đã thêm hồ sơ mẫu: " + row.username())));
        detail.setOnAction(event -> openProfile(true));
        edit.setOnAction(event -> openProfile(false));
        lock.setOnAction(event -> changeStatus(false));
        remove.setOnAction(event -> changeStatus(true));
        password.setOnAction(event -> previewPassword());
        FlowPane actions = new FlowPane(8, 8, add, detail, edit, lock, remove, password);
        remove.getStyleClass().add("danger-button");
        resultCount.setId("user-result-count");
        resultCount.getStyleClass().add("muted");
        Label sortHint = new Label("Sắp xếp bằng tiêu đề Họ tên / Ngày tạo");
        sortHint.getStyleClass().add("muted");
        Region sortGap = new Region();
        HBox.setHgrow(sortGap, Priority.ALWAYS);
        HBox counts = new HBox(12, resultCount, sortGap, sortHint);
        selectionHint.setId("user-selection-hint");
        selectionHint.setWrapText(true);
        selectionHint.getStyleClass().add("muted");
        feedback.setId("user-feedback");
        feedback.setWrapText(true);
        feedback.getStyleClass().add("demo-note");
        getChildren().addAll(top, filters, actions, counts, table, selectionHint, feedback);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, row) -> updateActions());
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openProfile(true);
            }
        });
        table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isAltDown()
                    && !event.isControlDown() && !event.isShiftDown()) {
                openProfile(true);
                event.consume();
            }
        });
        store.users().addListener((javafx.collections.ListChangeListener<UserRow>) change -> {
            updateCounts();
            updateActions();
        });
        updateCounts();
        updateActions();
    }

    private void buildTable() {
        table.setId("users-table");
        table.setAccessibleText("Danh sách người dùng");
        table.setAccessibleHelp("Dùng phím mũi tên để chọn, Enter hoặc nút Chi tiết để xem đầy đủ hồ sơ.");
        Label emptyTitle = new Label("Không có người dùng phù hợp với bộ lọc.");
        emptyTitle.setWrapText(true);
        emptyTitle.getStyleClass().add("empty-state-title");
        Button reset = button("Xóa bộ lọc", "empty-clear-user-filters");
        reset.setOnAction(event -> clearFilters());
        VBox empty = new VBox(12, emptyTitle, reset);
        empty.setAlignment(Pos.CENTER);
        empty.getStyleClass().add("empty-state");
        table.setPlaceholder(empty);
        table.setMinHeight(160);
        table.setPrefHeight(300);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<UserRow, Long> id = column("ID", UserRow::id, 40);
        id.setMaxWidth(56);
        TableColumn<UserRow, String> username = column("Tên đăng nhập", UserRow::username, 115);
        TableColumn<UserRow, String> name = column("Họ tên", UserRow::fullName, 145);
        TableColumn<UserRow, String> email = column("Email", UserRow::email, 165);
        TableColumn<UserRow, UserRow.Status> status = column("Trạng thái", UserRow::status, 96);
        status.setMaxWidth(112);
        status.setCellFactory(ignored -> new TableCell<>() {
            private final Label badge = new Label();
            @Override protected void updateItem(UserRow.Status value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                setGraphic(null);
                setAccessibleText(null);
                if (!empty && value != null) {
                    badge.setText(value.label());
                    badge.getStyleClass().setAll("status-badge", "status-" + value.name().toLowerCase(Locale.ROOT));
                    setGraphic(badge);
                    setAccessibleText(value.label());
                }
            }
        });
        TableColumn<UserRow, OffsetDateTime> created = column("Ngày tạo", UserRow::createdAt, 132);
        created.setMaxWidth(160);
        created.setCellFactory(ignored -> new TableCell<>() {
            @Override protected void updateItem(OffsetDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : formatDate(value));
            }
        });
        // So sánh ngày bằng kiểu thời gian, không sắp xếp chuỗi dd/MM/yyyy.
        created.setSortType(TableColumn.SortType.DESCENDING);
        name.setComparator(java.text.Collator.getInstance(Locale.forLanguageTag("vi-VN"))::compare);
        table.getColumns().addAll(java.util.List.of(id, username, name, email, status, created));
        table.getSortOrder().add(created);
    }

    private static <T> TableColumn<UserRow, T> column(String title,
            java.util.function.Function<UserRow, T> value, double width) {
        TableColumn<UserRow, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        column.setMinWidth(width);
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Tooltip fullValue = new Tooltip();
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                String text = empty || item == null ? null : item.toString();
                setText(text);
                fullValue.setText(text);
                setTooltip(text == null ? null : fullValue);
                setAccessibleText(text);
            }
        });
        return column;
    }

    private void clearFilters() {
        nameFilter.clear();
        usernameFilter.clear();
        statusFilter.getSelectionModel().selectFirst();
    }

    private void applyFilters() {
        String name = nameFilter.getText().strip().toLowerCase(Locale.ROOT);
        String username = usernameFilter.getText().strip().toLowerCase(Locale.ROOT);
        int statusIndex = statusFilter.getSelectionModel().getSelectedIndex() - 1;
        UserRow.Status status = statusIndex < 0 ? null : UserRow.Status.values()[statusIndex];
        table.getSelectionModel().clearSelection();
        filtered.setPredicate(row -> row.fullName().toLowerCase(Locale.ROOT).contains(name)
                && row.username().toLowerCase(Locale.ROOT).contains(username)
                && (status == null || row.status() == status));
        updateCounts();
    }

    private void updateCounts() {
        long active = store.users().stream().filter(row -> row.status() == UserRow.Status.ACTIVE).count();
        long locked = store.users().stream().filter(row -> row.status() == UserRow.Status.LOCKED).count();
        summary.setText("%d hồ sơ · %d hoạt động · %d đã khóa · %d đã xóa".formatted(
                store.users().size(), active, locked, store.users().size() - active - locked));
        resultCount.setText("%d / %d người dùng".formatted(filtered.size(), store.users().size()));
    }

    private void updateActions() {
        UserRow row = table.getSelectionModel().getSelectedItem();
        boolean missing = row == null;
        boolean deleted = !missing && row.status() == UserRow.Status.DELETED;
        boolean protectedAdmin = !missing && store.isLastActiveAdmin(row);
        detail.setDisable(missing);
        edit.setDisable(missing || deleted);
        password.setDisable(missing || deleted);
        lock.setDisable(missing || deleted || protectedAdmin);
        remove.setDisable(missing || deleted || protectedAdmin);
        lock.setText(!missing && row.status() == UserRow.Status.LOCKED ? "Mở khóa" : "Khóa");
        String reason = missing ? "Chọn một người dùng để xem hoặc thao tác."
                : deleted ? row.username() + " · Tài khoản đã xóa chỉ được xem, không thể khôi phục."
                : protectedAdmin ? row.username() + " · Admin hoạt động cuối cùng: không thể khóa hoặc xóa."
                : row.username() + " · " + row.fullName() + " · Thay đổi chỉ có hiệu lực trong bản demo.";
        selectionHint.setText(reason);
        lock.setTooltip(new Tooltip(reason));
        remove.setTooltip(new Tooltip(reason));
    }

    private void openProfile(boolean readOnly) {
        UserRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) { return; }
        new UserEditDialog(this, store, row, readOnly).showAndWait()
                .ifPresent(saved -> afterMutation(saved, "Đã cập nhật hồ sơ mẫu: " + saved.username()));
    }

    private void changeStatus(boolean deleting) {
        UserRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) { return; }
        UserRow.Status next = deleting ? UserRow.Status.DELETED
                : row.status() == UserRow.Status.LOCKED ? UserRow.Status.ACTIVE : UserRow.Status.LOCKED;
        String action = deleting ? "Xóa mềm" : next == UserRow.Status.LOCKED ? "Khóa tài khoản" : "Mở khóa tài khoản";
        if (!AdminDialogs.confirm(this, action + " mẫu", action + " “" + row.username()
                + "”? Chỉ thay đổi dữ liệu trong bộ nhớ, không lưu lên hệ thống.")) { return; }
        try {
            afterMutation(store.changeStatus(row.id(), next), action + " mẫu: " + row.username());
        } catch (IllegalArgumentException exception) {
            AdminDialogs.information(this, "Không thể thực hiện", exception.getMessage());
        }
    }

    private void afterMutation(UserRow row, String message) {
        if (table.getItems().contains(row)) {
            table.getSelectionModel().select(row);
            table.scrollTo(row);
        } else {
            table.getSelectionModel().clearSelection();
        }
        updateCounts();
        updateActions();
        feedback.setText(message + ". Chưa có thay đổi trên Server/Database.");
    }

    private void previewPassword() {
        UserRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) { return; }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật mật khẩu · Bản mẫu");
        dialog.setHeaderText(row.username());
        AdminDialogs.prepare(dialog, this);
        PasswordField first = new PasswordField();
        PasswordField second = new PasswordField();
        first.setId("password-new");
        second.setId("password-confirm");
        Label error = new Label();
        error.getStyleClass().add("validation-message");
        error.setWrapText(true);
        error.setMaxWidth(380);
        error.setFocusTraversable(true);
        error.setVisible(false);
        error.setManaged(false);
        Label note = new Label("Chưa kết nối Server. Form này không lưu hoặc cập nhật mật khẩu.");
        note.setWrapText(true);
        note.getStyleClass().add("demo-note");
        dialog.getDialogPane().setContent(new VBox(10, filterField("Mật khẩu mới", first),
                filterField("Xác nhận mật khẩu", second), error, note));
        ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType check = new ButtonType("Kiểm tra bản mẫu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cancel, check);
        ((Button) dialog.getDialogPane().lookupButton(check)).addEventFilter(ActionEvent.ACTION, event -> {
            if (first.getText().isBlank() || !first.getText().equals(second.getText())) {
                error.setText("Mật khẩu không được trống và hai ô phải giống nhau.");
                error.setVisible(true);
                error.setManaged(true);
                dialog.getDialogPane().getScene().getWindow().sizeToScene();
                error.requestFocus();
                event.consume();
            }
        });
        try {
            if (dialog.showAndWait().filter(check::equals).isPresent()) {
                feedback.setText("Form mật khẩu hợp lệ. Chưa cập nhật mật khẩu cho " + row.username() + ".");
            }
        } finally {
            first.clear();
            second.clear();
        }
    }

    public static String formatDate(OffsetDateTime value) { return DATE_FORMAT.format(value); }

    private static VBox filterField(String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.setLabelFor(control);
        label.getStyleClass().add("filter-label");
        VBox box = new VBox(5, label, control);
        if (control instanceof TextField) {
            HBox.setHgrow(box, Priority.ALWAYS);
            box.setMinWidth(110);
        }
        return box;
    }

    private static Button button(String label, String id) {
        Button button = new Button(label);
        button.setId(id);
        button.getStyleClass().add("secondary-button");
        return button;
    }
}
