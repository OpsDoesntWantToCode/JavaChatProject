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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** A9: lịch sử hoạt động mẫu, đếm đối tượng chat khác nhau trong khoảng ngày. */
final class ActiveUsersView extends VBox implements AutoCloseable
{
    private enum Metric
    {
        OPEN_APP("Lượt mở app"), CHAT_PARTNERS("Số người đã chat"), CHAT_GROUPS("Số nhóm đã chat");

        private final String label;

        Metric(String label)
        {
            this.label = label;
        }

        long value(AdminStatistics.ActivityStats row)
        {
            return switch (this)
            {
                case OPEN_APP -> row.openAppCount();
                case CHAT_PARTNERS -> row.chatPartners();
                case CHAT_GROUPS -> row.chatGroups();
            };
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    private final AdminDemoData data;
    private final AdminStatistics statistics;
    private final ObservableList<AdminStatistics.ActivityStats> rows = FXCollections.observableArrayList();
    private final TextField name = new TextField();
    private final DatePicker from = AdminDateRange.picker("active-users-from");
    private final DatePicker to = AdminDateRange.picker("active-users-to");
    private final ComboBox<Metric> metric = new ComboBox<>();
    private final ComboBox<String> operator = new ComboBox<>();
    private final TextField number = new TextField();
    private final Label error = new Label();
    private final Label count = new Label();
    private final ListChangeListener<UserRow> listener = change -> reload();

    ActiveUsersView(AdminDemoData data)
    {
        this.data = data;
        statistics = new AdminStatistics(data);
        setId("active-users");
        getStyleClass().add("admin-data-view");
        setSpacing(12);
        setMinSize(0, 0);
        TableView<AdminStatistics.ActivityStats> table = new TableView<>();
        AdminTableSupport.prepare(table, "active-users-table", "Người dùng hoạt động",
                "Không có người dùng hoạt động phù hợp. Hãy xóa bộ lọc để xem toàn bộ lịch sử.");
        TableColumn<AdminStatistics.ActivityStats, String> username = AdminTableSupport.column(
                "Tên đăng nhập", row -> row.user().username(), 120, value -> value);
        TableColumn<AdminStatistics.ActivityStats, String> fullName = AdminTableSupport.column(
                "Họ tên", row -> row.user().fullName(), 150, value -> value);
        fullName.setComparator(Collator.getInstance(Locale.forLanguageTag("vi-VN"))::compare);
        TableColumn<AdminStatistics.ActivityStats, OffsetDateTime> created = AdminTableSupport.column(
                "Ngày tạo", row -> row.user().createdAt(), 160, UserManagementView::formatDate);
        created.setMinWidth(150);
        TableColumn<AdminStatistics.ActivityStats, Long> opens = AdminTableSupport.column(
                "Lượt mở app", AdminStatistics.ActivityStats::openAppCount, 110, Object::toString);
        TableColumn<AdminStatistics.ActivityStats, Integer> partners = AdminTableSupport.column(
                "Người đã chat", AdminStatistics.ActivityStats::chatPartners, 115, Object::toString);
        TableColumn<AdminStatistics.ActivityStats, Integer> groups = AdminTableSupport.column(
                "Nhóm đã chat", AdminStatistics.ActivityStats::chatGroups, 115, Object::toString);
        table.getColumns().setAll(List.of(username, fullName, created, opens, partners, groups));
        SortedList<AdminStatistics.ActivityStats> sorted = new SortedList<>(rows);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.getSortOrder().add(fullName);
        name.setId("active-users-name");
        name.setPromptText("Nhập họ tên…");
        metric.setId("active-users-metric");
        metric.getItems().setAll(Metric.values());
        metric.setValue(Metric.OPEN_APP);
        metric.setPrefWidth(180);
        operator.setId("active-users-operator");
        operator.getItems().setAll("=", "<", ">");
        operator.setValue("=");
        operator.setPrefWidth(130);
        number.setId("active-users-number");
        number.setPromptText("Số nguyên không âm");
        number.setPrefColumnCount(12);
        Button clear = new Button("Xóa bộ lọc");
        clear.setId("active-users-clear");
        clear.getStyleClass().add("secondary-button");
        clear.setOnAction(event ->
        {
            name.clear();
            number.clear();
            metric.setValue(Metric.OPEN_APP);
            operator.setValue("=");
            from.setValue(null);
            to.setValue(null);
            from.getEditor().clear();
            to.getEditor().clear();
            reload();
        });
        FlowPane dates = new FlowPane(10, 8, AdminTableSupport.field("Họ tên", name),
                AdminTableSupport.field("Từ ngày", from), AdminTableSupport.field("Đến ngày", to));
        FlowPane numbers = new FlowPane(10, 8, AdminTableSupport.field("Chỉ số cần lọc", metric),
                AdminTableSupport.field("So sánh", operator), AdminTableSupport.field("Số lượng", number), clear);
        dates.setAlignment(Pos.BOTTOM_LEFT);
        numbers.setAlignment(Pos.BOTTOM_LEFT);
        error.setId("active-users-error");
        error.setWrapText(true);
        error.setMinHeight(Region.USE_PREF_SIZE);
        error.getStyleClass().add("validation-message");
        count.setId("active-users-count");
        count.getStyleClass().add("muted");
        Label note = new Label("Dữ liệu mẫu · Đếm người/nhóm khác nhau đã chủ động gửi tin, không đếm số tin. Giữ lịch sử tài khoản đã khóa/xóa.");
        note.setWrapText(true);
        note.setMinHeight(Region.USE_PREF_SIZE);
        note.getStyleClass().add("demo-note");
        getChildren().addAll(dates, numbers, error, count, table, note);
        VBox.setVgrow(table, Priority.ALWAYS);
        name.textProperty().addListener((observable, old, value) -> reload());
        from.getEditor().textProperty().addListener((observable, old, value) -> reload());
        to.getEditor().textProperty().addListener((observable, old, value) -> reload());
        metric.valueProperty().addListener((observable, old, value) -> reload());
        operator.valueProperty().addListener((observable, old, value) -> reload());
        number.textProperty().addListener((observable, old, value) -> reload());
        data.userStore().users().addListener(listener);
        reload();
    }

    private void reload()
    {
        try
        {
            AdminDateRange range = AdminDateRange.read(from, to);
            String text = number.getText().strip();
            if (!text.isEmpty() && !text.matches("[0-9]+"))
            {
                throw new IllegalArgumentException("Số lượng phải là số nguyên không âm; để trống nếu không lọc số.");
            }
            Long threshold = text.isEmpty() ? null : Long.parseLong(text);
            Metric selected = metric.getValue();
            String operation = operator.getValue();
            if (selected == null || operation == null || !operator.getItems().contains(operation))
            {
                throw new IllegalArgumentException("Chọn chỉ số và toán tử =, < hoặc >.");
            }
            String query = name.getText().strip().toLowerCase(Locale.ROOT);
            List<AdminStatistics.ActivityStats> active = statistics.activeUsers(range);
            rows.setAll(active.stream().filter(row -> row.user().fullName().toLowerCase(Locale.ROOT).contains(query))
                    .filter(row -> threshold == null || switch (operation)
                    {
                        case "=" -> selected.value(row) == threshold;
                        case "<" -> selected.value(row) < threshold;
                        default -> selected.value(row) > threshold;
                    }).toList());
            error.setText("");
            error.setVisible(false);
            error.setManaged(false);
            count.setText(rows.size() + " / " + active.size() + " người hoạt động trong khoảng ngày");
        }
        catch (DateTimeException | IllegalArgumentException exception)
        {
            rows.clear();
            error.setText(exception instanceof DateTimeException
                    ? "Ngày không hợp lệ. Nhập đúng dd/MM/yyyy hoặc chọn bằng lịch."
                    : exception instanceof NumberFormatException
                    ? "Số lượng quá lớn. Hãy nhập số nguyên không âm nhỏ hơn." : exception.getMessage());
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
