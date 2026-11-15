package admin;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** A6: tra cứu báo cáo và mô phỏng khóa người bị báo cáo qua store dùng chung. */
final class SpamReportView extends VBox implements AutoCloseable
{
    private final AdminDemoData data;
    private final TableView<AdminDemoData.SpamReport> table = new TableView<>();
    private final TextField username = new TextField();
    private final DatePicker from = AdminDateRange.picker("spam-from");
    private final DatePicker to = AdminDateRange.picker("spam-to");
    private final Label error = new Label();
    private final Label count = new Label();
    private final Label selectionHint = new Label();
    private final Label feedback = new Label("Dữ liệu mẫu · Khóa chỉ thay đổi trạng thái trong bộ nhớ, không xóa báo cáo.");
    private final Button lock = new Button("Khóa người bị báo cáo");
    private final FilteredList<AdminDemoData.SpamReport> filtered;
    private final ListChangeListener<UserRow> userListener = change -> refreshUsers();

    SpamReportView(AdminDemoData data)
    {
        this.data = data;
        setId("spam-reports");
        getStyleClass().add("admin-data-view");
        setSpacing(12);
        setMinSize(0, 0);
        filtered = new FilteredList<>(FXCollections.observableArrayList(data.records().reports()));
        SortedList<AdminDemoData.SpamReport> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        AdminTableSupport.prepare(table, "spam-reports-table", "Danh sách báo cáo spam",
                "Không có báo cáo phù hợp. Hãy xóa bộ lọc để xem toàn bộ báo cáo.");
        TableColumn<AdminDemoData.SpamReport, OffsetDateTime> time = AdminTableSupport.column(
                "Thời gian", AdminDemoData.SpamReport::occurredAt, 175, UserManagementView::formatDate);
        TableColumn<AdminDemoData.SpamReport, String> reporter = AdminTableSupport.column(
                "Người báo cáo", report -> data.user(report.reporterId()).username(), 135, value -> value);
        reporter.setSortable(false);
        TableColumn<AdminDemoData.SpamReport, String> reported = AdminTableSupport.column(
                "Người bị báo cáo", report -> data.user(report.reportedId()).username(), 150, value -> value);
        TableColumn<AdminDemoData.SpamReport, String> reason = AdminTableSupport.column(
                "Lý do", AdminDemoData.SpamReport::reason, 210, value -> value);
        reason.setSortable(false);
        TableColumn<AdminDemoData.SpamReport, UserRow.Status> status = AdminTableSupport.column(
                "Tài khoản", report -> data.user(report.reportedId()).status(), 115, UserRow.Status::label);
        status.setSortable(false);
        table.getColumns().setAll(List.of(time, reporter, reported, reason, status));
        time.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(time);
        username.setId("spam-username");
        username.setPromptText("Username bị báo cáo…");
        Button clear = new Button("Xóa bộ lọc");
        clear.setId("spam-clear");
        clear.getStyleClass().add("secondary-button");
        clear.setOnAction(event -> clearFilters());
        FlowPane filters = new FlowPane(10, 8, AdminTableSupport.field("Username bị báo cáo", username),
                AdminTableSupport.field("Từ ngày", from), AdminTableSupport.field("Đến ngày", to), clear);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        lock.setId("spam-lock");
        lock.getStyleClass().addAll("secondary-button", "danger-button");
        lock.setOnAction(event -> lockSelected());
        count.setId("spam-count");
        count.getStyleClass().add("muted");
        FlowPane actions = new FlowPane(12, 8, lock, count);
        actions.setAlignment(Pos.CENTER_LEFT);
        error.setId("spam-error");
        error.getStyleClass().add("validation-message");
        error.setWrapText(true);
        selectionHint.setId("spam-selection-hint");
        selectionHint.getStyleClass().add("muted");
        selectionHint.setWrapText(true);
        selectionHint.setMinHeight(Region.USE_PREF_SIZE);
        feedback.setId("spam-feedback");
        feedback.getStyleClass().add("demo-note");
        feedback.setWrapText(true);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        getChildren().addAll(filters, error, actions, table, selectionHint, feedback);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, value) -> updateActions());
        username.textProperty().addListener((observable, old, value) -> applyFilters());
        from.getEditor().textProperty().addListener((observable, old, value) -> applyFilters());
        to.getEditor().textProperty().addListener((observable, old, value) -> applyFilters());
        data.userStore().users().addListener(userListener);
        applyFilters();
    }

    private void applyFilters()
    {
        table.getSelectionModel().clearSelection();
        try
        {
            AdminDateRange range = AdminDateRange.read(from, to);
            String query = username.getText().strip().toLowerCase(Locale.ROOT);
            filtered.setPredicate(report -> range.contains(report.occurredAt())
                    && data.user(report.reportedId()).username().toLowerCase(Locale.ROOT).contains(query));
            error.setText("");
            error.setVisible(false);
            error.setManaged(false);
            count.setText(filtered.size() + " / " + data.records().reports().size() + " báo cáo");
        }
        catch (DateTimeException | IllegalArgumentException exception)
        {
            filtered.setPredicate(report -> false);
            error.setText(exception instanceof DateTimeException
                    ? "Ngày không hợp lệ. Nhập đúng dd/MM/yyyy hoặc chọn bằng lịch." : exception.getMessage());
            error.setVisible(true);
            error.setManaged(true);
            count.setText("Bộ lọc chưa hợp lệ");
        }
        updateActions();
    }

    private void refreshUsers()
    {
        AdminDemoData.SpamReport selected = table.getSelectionModel().getSelectedItem();
        applyFilters();
        table.refresh();
        table.sort();
        if (selected != null)
        {
            table.getItems().stream().filter(report -> report.id() == selected.id()).findFirst()
                    .ifPresent(report -> table.getSelectionModel().select(report));
        }
        updateActions();
    }

    private String cannotLock(UserRow user)
    {
        if (user.status() != UserRow.Status.ACTIVE)
        {
            return "Tài khoản " + user.username() + " đang ở trạng thái “" + user.status().label() + "”, không thể khóa tiếp.";
        }
        return data.userStore().isLastActiveAdmin(user)
                ? "Không thể khóa Admin hoạt động cuối cùng: " + user.username() + "." : null;
    }

    private void updateActions()
    {
        AdminDemoData.SpamReport report = table.getSelectionModel().getSelectedItem();
        if (report == null)
        {
            lock.setDisable(true);
            selectionHint.setText("Chọn báo cáo để xem lý do và khóa người bị báo cáo.");
            return;
        }
        UserRow target = data.user(report.reportedId());
        String blocked = cannotLock(target);
        lock.setDisable(blocked != null);
        selectionHint.setText("Người bị báo cáo: " + target.username() + " · "
                + (blocked == null ? "Có thể khóa tài khoản mẫu." : blocked) + "\nLý do: " + report.reason());
    }

    private void lockSelected()
    {
        AdminDemoData.SpamReport report = table.getSelectionModel().getSelectedItem();
        if (report == null)
        {
            return;
        }
        UserRow target = data.user(report.reportedId());
        String blocked = cannotLock(target);
        if (blocked != null)
        {
            feedback.setText(blocked);
            updateActions();
            return;
        }
        if (!AdminDialogs.confirm(this, "Khóa tài khoản mẫu", "Khóa “" + target.username()
                + "” — người bị báo cáo trong báo cáo #" + report.id()
                + "? Chỉ thay đổi dữ liệu trong bộ nhớ; giữ nguyên báo cáo."))
        {
            return;
        }
        try
        {
            // Đọc lại cùng ID sau xác nhận; không ghi đè trạng thái vừa được thay ở nơi khác.
            target = data.user(report.reportedId());
            blocked = cannotLock(target);
            if (blocked != null)
            {
                feedback.setText(blocked);
                updateActions();
                return;
            }
            data.userStore().changeStatus(target.id(), UserRow.Status.LOCKED);
            feedback.setText("Đã khóa tài khoản mẫu: " + target.username() + ". Báo cáo được giữ nguyên.");
        }
        catch (IllegalArgumentException exception)
        {
            feedback.setText(exception.getMessage());
            updateActions();
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
