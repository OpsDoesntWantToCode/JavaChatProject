package admin;

import java.text.Collator;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** A8: thống kê quan hệ hiện tại từ cùng đồ thị với danh sách bạn bè A3. */
final class FriendStatisticsView extends VBox implements AutoCloseable
{
    private final AdminStatistics statistics;
    private final AdminDemoData data;
    private final ObservableList<AdminStatistics.FriendStats> rows = FXCollections.observableArrayList();
    private final TextField name = new TextField();
    private final ComboBox<String> operator = new ComboBox<>();
    private final TextField number = new TextField();
    private final Label error = new Label();
    private final Label count = new Label();
    private final ListChangeListener<UserRow> listener = change -> reload();

    FriendStatisticsView(AdminDemoData data)
    {
        this.data = data;
        statistics = new AdminStatistics(data);
        setId("friend-statistics");
        getStyleClass().add("admin-data-view");
        setSpacing(12);
        setMinSize(0, 0);
        TableView<AdminStatistics.FriendStats> table = new TableView<>();
        AdminTableSupport.prepare(table, "friend-statistics-table", "Thống kê bạn bè hiện tại",
                "Không có người dùng phù hợp. Hãy xóa bộ lọc để xem toàn bộ thống kê.");
        TableColumn<AdminStatistics.FriendStats, String> username = AdminTableSupport.column(
                "Tên đăng nhập", row -> row.user().username(), 145, value -> value);
        TableColumn<AdminStatistics.FriendStats, String> fullName = AdminTableSupport.column(
                "Họ tên", row -> row.user().fullName(), 180, value -> value);
        fullName.setComparator(Collator.getInstance(Locale.forLanguageTag("vi-VN"))::compare);
        TableColumn<AdminStatistics.FriendStats, OffsetDateTime> created = AdminTableSupport.column(
                "Ngày tạo", row -> row.user().createdAt(), 170, UserManagementView::formatDate);
        TableColumn<AdminStatistics.FriendStats, Integer> direct = AdminTableSupport.column(
                "Bạn trực tiếp", AdminStatistics.FriendStats::directFriends, 125, Object::toString);
        TableColumn<AdminStatistics.FriendStats, Integer> second = AdminTableSupport.column(
                "Bạn của bạn", AdminStatistics.FriendStats::friendsOfFriends, 125, Object::toString);
        table.getColumns().setAll(List.of(username, fullName, created, direct, second));
        SortedList<AdminStatistics.FriendStats> sorted = new SortedList<>(rows);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.getSortOrder().add(fullName);
        name.setId("friend-stats-name");
        name.setPromptText("Nhập họ tên…");
        operator.setId("friend-stats-operator");
        operator.getItems().setAll("=", "<", ">");
        operator.setValue("=");
        operator.setPrefWidth(100);
        number.setId("friend-stats-number");
        number.setPromptText("Số nguyên không âm");
        number.setPrefColumnCount(12);
        Button clear = new Button("Xóa bộ lọc");
        clear.setId("friend-stats-clear");
        clear.getStyleClass().add("secondary-button");
        clear.setOnAction(event ->
        {
            name.clear();
            number.clear();
            operator.setValue("=");
            reload();
        });
        FlowPane filters = new FlowPane(10, 8, AdminTableSupport.field("Họ tên", name),
                AdminTableSupport.field("So sánh", operator), AdminTableSupport.field("Số bạn trực tiếp", number), clear);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        error.setId("friend-stats-error");
        error.setWrapText(true);
        error.getStyleClass().add("validation-message");
        count.setId("friend-stats-count");
        count.getStyleClass().add("muted");
        Label note = new Label("Dữ liệu mẫu · Bạn của bạn không trùng, không gồm chính mình hoặc bạn trực tiếp. Không tính tài khoản đã xóa.");
        note.setWrapText(true);
        note.getStyleClass().add("demo-note");
        getChildren().addAll(filters, error, count, table, note);
        VBox.setVgrow(table, Priority.ALWAYS);
        name.textProperty().addListener((observable, old, value) -> reload());
        number.textProperty().addListener((observable, old, value) -> reload());
        operator.valueProperty().addListener((observable, old, value) -> reload());
        data.userStore().users().addListener(listener);
        reload();
    }

    private void reload()
    {
        try
        {
            String text = number.getText().strip();
            if (!text.isEmpty() && !text.matches("[0-9]+"))
            {
                throw new IllegalArgumentException("Số bạn trực tiếp phải là số nguyên không âm.");
            }
            Long threshold = text.isEmpty() ? null : Long.parseLong(text);
            String operation = operator.getValue();
            if (operation == null || !operator.getItems().contains(operation))
            {
                throw new IllegalArgumentException("Chọn toán tử =, < hoặc >.");
            }
            String query = name.getText().strip().toLowerCase(Locale.ROOT);
            List<AdminStatistics.FriendStats> all = statistics.friendStatistics();
            rows.setAll(all.stream().filter(row -> row.user().fullName().toLowerCase(Locale.ROOT).contains(query))
                    .filter(row -> threshold == null || switch (operation)
                    {
                        case "=" -> row.directFriends() == threshold;
                        case "<" -> row.directFriends() < threshold;
                        default -> row.directFriends() > threshold;
                    }).toList());
            error.setText("");
            error.setVisible(false);
            error.setManaged(false);
            count.setText(rows.size() + " / " + all.size() + " người dùng chưa xóa");
        }
        catch (IllegalArgumentException exception)
        {
            rows.clear();
            error.setText(exception instanceof NumberFormatException
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
