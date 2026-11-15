package admin;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
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
import javafx.util.StringConverter;

/** A4 và bảng lịch sử theo user ở A3; cùng nguồn sự kiện, chỉ khác phạm vi. */
final class GlobalLoginHistoryView extends VBox implements AutoCloseable
{
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private final AdminDemoData data;
    private final Long userId;
    private final ObservableList<AdminDemoData.LoginEvent> rows = FXCollections.observableArrayList();
    private final FilteredList<AdminDemoData.LoginEvent> filtered = new FilteredList<>(rows);
    private final TextField username = new TextField();
    private final DatePicker from = datePicker("login-from");
    private final DatePicker to = datePicker("login-to");
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

    private static DatePicker datePicker(String id)
    {
        DatePicker picker = new DatePicker();
        picker.setId(id);
        picker.setPromptText("dd/MM/yyyy");
        picker.setConverter(new StringConverter<>()
        {
            @Override
            public String toString(LocalDate value)
            {
                return value == null ? "" : DATE.format(value);
            }

            @Override
            public LocalDate fromString(String value)
            {
                try
                {
                    return parseDate(value);
                }
                catch (DateTimeException exception)
                {
                    // Giữ nội dung sai để báo lỗi; không ném exception từ commit khi rời ô nhập.
                    return picker.getValue();
                }
            }
        });
        return picker;
    }

    private static LocalDate parseDate(String value)
    {
        return value == null || value.isBlank() ? null : LocalDate.parse(value.strip(), DATE);
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
            LocalDate start = parseDate(from.getEditor().getText());
            LocalDate end = parseDate(to.getEditor().getText());
            if (start != null && end != null && start.isAfter(end))
            {
                throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng Đến ngày.");
            }
            String query = username.getText().strip().toLowerCase(Locale.ROOT);
            filtered.setPredicate(event ->
            {
                LocalDate date = event.occurredAt().atZoneSameInstant(ZONE).toLocalDate();
                return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end))
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
