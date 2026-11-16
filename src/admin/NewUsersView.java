package admin;

import java.text.Collator;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** A7: lịch sử đăng ký mẫu, giữ cả hồ sơ hiện đã khóa/xóa. */
final class NewUsersView extends VBox implements AutoCloseable
{
    private final AdminDemoData data;
    private final AdminStatistics statistics;
    private final ObservableList<UserRow> rows = FXCollections.observableArrayList();
    private final TextField name = new TextField();
    private final DatePicker from = AdminDateRange.picker("new-users-from");
    private final DatePicker to = AdminDateRange.picker("new-users-to");
    private final Label error = new Label();
    private final Label count = new Label();
    private final ListChangeListener<UserRow> listener = change -> reload();

    NewUsersView(AdminDemoData data)
    {
        this.data = data;
        statistics = new AdminStatistics(data);
        setId("new-users");
        getStyleClass().add("admin-data-view");
        setSpacing(12);
        setMinSize(0, 0);
        TableView<UserRow> table = new TableView<>();
        AdminTableSupport.prepare(table, "new-users-table", "Người dùng đăng ký mới",
                "Không có người đăng ký phù hợp. Hãy xóa bộ lọc để xem toàn bộ lịch sử.");
        TableColumn<UserRow, String> username = AdminTableSupport.column("Tên đăng nhập", UserRow::username, 140, value -> value);
        TableColumn<UserRow, String> fullName = AdminTableSupport.column("Họ tên", UserRow::fullName, 170, value -> value);
        fullName.setComparator(Collator.getInstance(Locale.forLanguageTag("vi-VN"))::compare);
        TableColumn<UserRow, String> email = AdminTableSupport.column("Email", UserRow::email, 180, value -> value);
        TableColumn<UserRow, UserRow.Status> status = AdminTableSupport.column("Trạng thái", UserRow::status, 110, UserRow.Status::label);
        TableColumn<UserRow, OffsetDateTime> created = AdminTableSupport.column("Ngày tạo", UserRow::createdAt, 170, UserManagementView::formatDate);
        table.getColumns().setAll(List.of(username, fullName, email, status, created));
        SortedList<UserRow> sorted = new SortedList<>(rows);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        created.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(created);
        name.setId("new-users-name");
        name.setPromptText("Nhập họ tên…");
        Button clear = new Button("Xóa bộ lọc");
        clear.setId("new-users-clear");
        clear.getStyleClass().add("secondary-button");
        clear.setOnAction(event ->
        {
            name.clear();
            from.setValue(null);
            to.setValue(null);
            from.getEditor().clear();
            to.getEditor().clear();
            reload();
        });
        FlowPane filters = new FlowPane(10, 8, AdminTableSupport.field("Họ tên", name),
                AdminTableSupport.field("Từ ngày", from), AdminTableSupport.field("Đến ngày", to), clear);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        error.setId("new-users-error");
        error.setWrapText(true);
        error.getStyleClass().add("validation-message");
        count.setId("new-users-count");
        count.getStyleClass().add("muted");
        Label note = new Label("Lịch sử đăng ký mẫu · Giữ cả tài khoản đã khóa/xóa. Sắp xếp bằng tiêu đề Họ tên / Ngày tạo.");
        note.setWrapText(true);
        note.getStyleClass().add("demo-note");
        getChildren().addAll(filters, error, count, table, note);
        VBox.setVgrow(table, Priority.ALWAYS);
        name.textProperty().addListener((observable, old, value) -> reload());
        from.getEditor().textProperty().addListener((observable, old, value) -> reload());
        to.getEditor().textProperty().addListener((observable, old, value) -> reload());
        data.userStore().users().addListener(listener);
        reload();
    }

    private void reload()
    {
        try
        {
            AdminDateRange range = AdminDateRange.read(from, to);
            String query = name.getText().strip().toLowerCase(Locale.ROOT);
            rows.setAll(statistics.registrations(range).stream()
                    .filter(user -> user.fullName().toLowerCase(Locale.ROOT).contains(query)).toList());
            error.setText("");
            error.setVisible(false);
            error.setManaged(false);
            count.setText(rows.size() + " / " + data.userStore().users().size() + " người đăng ký");
        }
        catch (DateTimeException | IllegalArgumentException exception)
        {
            rows.clear();
            error.setText(exception instanceof DateTimeException
                    ? "Ngày không hợp lệ. Nhập đúng dd/MM/yyyy hoặc chọn bằng lịch." : exception.getMessage());
            error.setVisible(true);
            error.setManaged(true);
            count.setText("Bộ lọc chưa hợp lệ");
        }
    }

    @Override
    public void close()
    {
        data.userStore().users().removeListener(listener);
    }
}
