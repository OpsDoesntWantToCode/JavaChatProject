package admin;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

/** A4 và bảng lịch sử theo user ở A3; cùng nguồn sự kiện, chỉ khác phạm vi. */
final class GlobalLoginHistoryView extends VBox implements AutoCloseable
{
    private final AdminDemoData data;
    private final Long userId;
    private final ObservableList<AdminDemoData.LoginEvent> rows = FXCollections.observableArrayList();
    private final FilteredList<AdminDemoData.LoginEvent> filtered = new FilteredList<>(rows);
    private final TextField username = new TextField();
    private final DatePicker from = AdminDateRange.picker("login-from");
    private final DatePicker to = AdminDateRange.picker("login-to");
    private final Label error = new Label();
    private final Label count = new Label();
    private final ListChangeListener<UserRow> userListener = change -> reload();

    GlobalLoginHistoryView(AdminDemoData data)
    {
        this(data, null);
    }

    GlobalLoginHistoryView(AdminDemoData data, Long userId)
    {
        this.data = data;
        this.userId = userId;
        if (userId != null)
        {
            data.user(userId);
        }
        setId("login-history");
        getStyleClass().add("admin-data-view");
        setSpacing(12);
        setMinSize(0, 0);
        TableView<AdminDemoData.LoginEvent> table = new TableView<>();
        AdminTableSupport.prepare(table, "login-history-table", "Lịch sử đăng nhập",
                "Không có lịch sử đăng nhập phù hợp.");
        TableColumn<AdminDemoData.LoginEvent, OffsetDateTime> time = AdminTableSupport.column(
                "Thời gian đăng nhập", AdminDemoData.LoginEvent::occurredAt, 210,
                UserManagementView::formatDate);
        TableColumn<AdminDemoData.LoginEvent, String> account = AdminTableSupport.column(
                "Tên đăng nhập", event -> data.user(event.userId()).username(), 180, value -> value);
        TableColumn<AdminDemoData.LoginEvent, String> name = AdminTableSupport.column(
                "Họ tên", event -> data.user(event.userId()).fullName(), 210, value -> value);
        table.getColumns().setAll(List.of(time, account, name));
        time.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(time);
        SortedList<AdminDemoData.LoginEvent> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        username.setId("login-username");
        username.setPromptText("Nhập username…");
        Button clear = new Button("Xóa bộ lọc");
        clear.setId("login-clear");
        clear.getStyleClass().add("secondary-button");
        clear.setOnAction(event -> clearFilters());
        FlowPane filters = new FlowPane(10, 8, AdminTableSupport.field("Tên đăng nhập", username),
                AdminTableSupport.field("Từ ngày", from), AdminTableSupport.field("Đến ngày", to), clear);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        if (userId == null)
        {
            getChildren().add(filters);
        }
        error.setId("login-error");
        error.getStyleClass().add("validation-message");
        error.setWrapText(true);
        error.setAccessibleText("Lỗi bộ lọc lịch sử đăng nhập");
        count.setId("login-count");
        count.getStyleClass().add("muted");
        Label note = new Label("Lịch sử mẫu · Giữ cả dữ liệu của tài khoản đã khóa/xóa.");
        note.setWrapText(true);
        note.getStyleClass().add("demo-note");
        getChildren().addAll(error, count, table, note);
        VBox.setVgrow(table, Priority.ALWAYS);
        username.textProperty().addListener((observable, old, value) -> applyFilters());
        from.getEditor().textProperty().addListener((observable, old, value) -> applyFilters());
        to.getEditor().textProperty().addListener((observable, old, value) -> applyFilters());
        data.userStore().users().addListener(userListener);
        reload();
    }

    private void reload()
    {
        rows.setAll(data.records().logins().stream()
                .filter(event -> userId == null || event.userId() == userId).toList());
        applyFilters();
    }

    private void applyFilters()
    {
        try
        {
            AdminDateRange range = AdminDateRange.read(from, to);
            String query = username.getText().strip().toLowerCase(Locale.ROOT);
            filtered.setPredicate(event ->
            {
                return range.contains(event.occurredAt())
                        && data.user(event.userId()).username().toLowerCase(Locale.ROOT).contains(query);
            });
            error.setText("");
            error.setVisible(false);
            error.setManaged(false);
            count.setText(filtered.size() + " / " + rows.size() + " lượt đăng nhập");
        }
        catch (DateTimeException | IllegalArgumentException exception)
        {
            filtered.setPredicate(event -> false);
            error.setText(exception instanceof DateTimeException
                    ? "Ngày không hợp lệ. Nhập đúng dd/MM/yyyy hoặc chọn bằng lịch." : exception.getMessage());
            error.setVisible(true);
            error.setManaged(true);
            count.setText("Bộ lọc chưa hợp lệ");
        }
    }

    private void clearFilters()
    {
        username.clear();
        from.setValue(null);
        to.setValue(null);
        from.getEditor().clear();
        to.getEditor().clear();
        applyFilters();
    }

    @Override
    public void close()
    {
        data.userStore().users().removeListener(userListener);
    }
}
