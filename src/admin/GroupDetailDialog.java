package admin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Chi tiết A5: vai trò trong nhóm độc lập với quyền quản trị hệ thống. */
final class GroupDetailDialog extends Dialog<Void>
{
    GroupDetailDialog(Node owner, AdminDemoData data, long groupId)
    {
        AdminDemoData.ChatGroup group = data.records().groups().stream()
                .filter(value -> value.id() == groupId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm đã chọn."));
        List<AdminDemoData.Membership> memberships = data.records().memberships().stream()
                .filter(member -> member.groupId() == groupId).toList();
        Set<Long> adminIds = memberships.stream().filter(AdminDemoData.Membership::admin)
                .map(AdminDemoData.Membership::userId).collect(Collectors.toSet());
        setTitle("Chi tiết nhóm chat");
        setHeaderText(group.name());
        AdminDialogs.prepare(this, owner);
        Label metadata = new Label();
        metadata.setId("group-metadata");
        metadata.setWrapText(true);
        metadata.getStyleClass().add("muted");
        TableView<UserRow> members = new TableView<>();
        AdminTableSupport.prepare(members, "group-members-table", "Thành viên nhóm " + group.name(),
                "Nhóm chưa có thành viên mẫu.");
        members.setMinHeight(120);
        members.setPrefHeight(200);
        members.getColumns().setAll(List.of(
                AdminTableSupport.column("Tên đăng nhập", UserRow::username, 180, value -> value),
                AdminTableSupport.column("Họ tên", UserRow::fullName, 230, value -> value),
                AdminTableSupport.column("Vai trò nhóm", row -> adminIds.contains(row.id()) ? "Admin nhóm" : "Thành viên",
                        120, value -> value),
                AdminTableSupport.column("Tài khoản", UserRow::status, 120, UserRow.Status::label)));
        TableView<UserRow> admins = new TableView<>();
        AdminTableSupport.prepare(admins, "group-admins-table", "Admin nhóm " + group.name(),
                "Nhóm chưa có Admin mẫu.");
        admins.setMinHeight(100);
        admins.setPrefHeight(124);
        admins.getColumns().setAll(List.of(
                AdminTableSupport.column("Tên đăng nhập", UserRow::username, 200, value -> value),
                AdminTableSupport.column("Họ tên", UserRow::fullName, 300, value -> value),
                AdminTableSupport.column("Tài khoản", UserRow::status, 130, UserRow.Status::label)));
        Label memberCount = new Label();
        memberCount.setId("group-member-count");
        memberCount.getStyleClass().add("filter-label");
        Label adminCount = new Label();
        adminCount.setId("group-admin-count");
        adminCount.getStyleClass().add("filter-label");
        Label note = new Label("Dữ liệu thành viên mẫu · Vai trò Admin chỉ áp dụng trong nhóm này.");
        note.setWrapText(true);
        note.getStyleClass().add("demo-note");
        VBox body = new VBox(10, metadata, memberCount, members, adminCount, admins, note);
        body.getStyleClass().add("admin-data-view");
        VBox.setVgrow(members, Priority.ALWAYS);
        Runnable reload = () ->
        {
            metadata.setText("Người tạo: " + data.user(group.creatorId()).username()
                    + " · Ngày tạo: " + UserManagementView.formatDate(group.createdAt())
                    + "\nMã hóa nhóm: " + (group.encrypted() ? "Bật (cờ mẫu, chưa mã hóa thật)" : "Tắt"));
            List<UserRow> rows = memberships.stream().map(member -> data.user(member.userId())).toList();
            members.setItems(FXCollections.observableArrayList(rows));
            admins.setItems(FXCollections.observableArrayList(rows.stream().filter(row -> adminIds.contains(row.id())).toList()));
            members.sort();
            admins.sort();
            memberCount.setText("THÀNH VIÊN (" + rows.size() + ")");
            adminCount.setText("ADMIN NHÓM (" + admins.getItems().size() + ")");
        };
        ListChangeListener<UserRow> listener = change -> reload.run();
        reload.run();
        data.userStore().users().addListener(listener);
        getDialogPane().setId("group-detail-dialog");
        getDialogPane().setPrefWidth(760);
        getDialogPane().setContent(body);
        getDialogPane().getButtonTypes().add(new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE));
        setResultConverter(button -> null);
        setOnHidden(event -> data.userStore().users().removeListener(listener));
    }
}
