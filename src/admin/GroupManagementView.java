package admin;

import java.text.Collator;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** A5: tra cứu nhóm mẫu; không thay đổi nhóm hoặc quyền thành viên. */
final class GroupManagementView extends VBox implements AutoCloseable
{
    private final AdminDemoData data;
    private final TableView<AdminDemoData.ChatGroup> table = new TableView<>();
    private final TextField nameFilter = new TextField();
    private final Label count = new Label();
    private final Button detail = new Button("Chi tiết nhóm");
    private final FilteredList<AdminDemoData.ChatGroup> filtered;
    private final ListChangeListener<UserRow> userListener = change -> table.refresh();

    GroupManagementView(AdminDemoData data)
    {
        this.data = data;
        setId("group-management");
        getStyleClass().add("admin-data-view");
        setSpacing(12);
        setMinSize(0, 0);
        filtered = new FilteredList<>(FXCollections.observableArrayList(data.records().groups()));
        SortedList<AdminDemoData.ChatGroup> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        AdminTableSupport.prepare(table, "groups-table", "Danh sách nhóm chat",
                "Không có nhóm phù hợp. Hãy xóa bộ lọc để xem toàn bộ nhóm.");
        table.setAccessibleHelp("Dùng phím mũi tên để chọn nhóm; Enter hoặc nút Chi tiết nhóm để xem thành viên.");
        TableColumn<AdminDemoData.ChatGroup, String> name = AdminTableSupport.column(
                "Tên nhóm", AdminDemoData.ChatGroup::name, 230, value -> value);
        name.setComparator(Collator.getInstance(Locale.forLanguageTag("vi-VN"))::compare);
        TableColumn<AdminDemoData.ChatGroup, String> creator = AdminTableSupport.column(
                "Người tạo", group -> data.user(group.creatorId()).username(), 150, value -> value);
        creator.setSortable(false);
        TableColumn<AdminDemoData.ChatGroup, OffsetDateTime> created = AdminTableSupport.column(
                "Ngày tạo", AdminDemoData.ChatGroup::createdAt, 175, UserManagementView::formatDate);
        TableColumn<AdminDemoData.ChatGroup, Boolean> encrypted = AdminTableSupport.column(
                "Mã hóa nhóm", AdminDemoData.ChatGroup::encrypted, 120, value -> value ? "Bật (mẫu)" : "Tắt");
        table.getColumns().setAll(List.of(name, creator, created, encrypted));
        created.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(created);
        table.setRowFactory(ignored ->
        {
            TableRow<AdminDemoData.ChatGroup> row = new TableRow<>();
            row.setOnMouseClicked(event ->
            {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    showDetails(row.getItem());
                }
            });
            return row;
        });
        table.setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.ENTER && !event.isAltDown()
                    && !event.isControlDown() && !event.isShiftDown())
            {
                showDetails(table.getSelectionModel().getSelectedItem());
                event.consume();
            }
        });
        nameFilter.setId("group-name-filter");
        nameFilter.setPromptText("Nhập tên nhóm…");
        nameFilter.setPrefColumnCount(23);
        nameFilter.textProperty().addListener((observable, old, value) -> applyFilter());
        Button clear = new Button("Xóa bộ lọc");
        clear.setId("group-clear");
        clear.getStyleClass().add("secondary-button");
        clear.setOnAction(event -> nameFilter.clear());
        detail.setId("group-detail");
        detail.getStyleClass().addAll("secondary-button", "primary-button");
        detail.setDisable(true);
        detail.setOnAction(event -> showDetails(table.getSelectionModel().getSelectedItem()));
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, value) -> detail.setDisable(value == null));
        FlowPane filters = new FlowPane(10, 8, AdminTableSupport.field("Tên nhóm", nameFilter), clear, detail);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        count.setId("group-count");
        count.getStyleClass().add("muted");
        Label hint = new Label("Sắp xếp bằng tiêu đề Tên nhóm / Ngày tạo. Chọn một nhóm để xem chi tiết.");
        hint.setWrapText(true);
        hint.getStyleClass().add("muted");
        Label note = new Label("Dữ liệu mẫu · Cờ mã hóa chỉ minh họa cấu hình nhóm, chưa thực hiện mã hóa.");
        note.setWrapText(true);
        note.getStyleClass().add("demo-note");
        getChildren().addAll(filters, count, table, hint, note);
        VBox.setVgrow(table, Priority.ALWAYS);
        data.userStore().users().addListener(userListener);
        applyFilter();
    }

    private void applyFilter()
    {
        String query = nameFilter.getText().strip().toLowerCase(Locale.ROOT);
        table.getSelectionModel().clearSelection();
        filtered.setPredicate(group -> group.name().toLowerCase(Locale.ROOT).contains(query));
        count.setText(filtered.size() + " / " + data.records().groups().size() + " nhóm");
    }

    private void showDetails(AdminDemoData.ChatGroup group)
    {
        if (group != null)
        {
            new GroupDetailDialog(this, data, group.id()).showAndWait();
        }
    }

    @Override
    public void close()
    {
        data.userStore().users().removeListener(userListener);
    }
}
