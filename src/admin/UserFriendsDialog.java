package admin;

import java.util.List;
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

final class UserFriendsDialog extends Dialog<Void>
{
    UserFriendsDialog(Node owner, AdminDemoData data, long userId)
    {
        setTitle("Danh sách bạn bè");
        AdminDialogs.prepare(this, owner);
        TableView<UserRow> table = new TableView<>();
        AdminTableSupport.prepare(table, "user-friends-table", "Bạn bè hiện tại của người dùng",
                "Người dùng chưa có bạn bè hiện tại.");
        table.getColumns().setAll(List.of(
                AdminTableSupport.column("Tên đăng nhập", UserRow::username, 170, value -> value),
                AdminTableSupport.column("Họ tên", UserRow::fullName, 190, value -> value),
                AdminTableSupport.column("Trực tuyến", row -> data.isOnline(row.id()) ? "Online" : "Offline",
                        110, value -> value),
                AdminTableSupport.column("Tài khoản", UserRow::status, 110, UserRow.Status::label)));
        Label count = new Label();
        count.setId("user-friends-count");
        count.getStyleClass().add("muted");
        Label note = new Label();
        note.setWrapText(true);
        note.getStyleClass().add("demo-note");
        VBox body = new VBox(12, count, table, note);
        body.getStyleClass().add("admin-data-view");
        VBox.setVgrow(table, Priority.ALWAYS);
        Runnable reload = () ->
        {
            setHeaderText(data.user(userId).username() + " · Bạn bè hiện tại");
            List<UserRow> friends = data.friendsOf(userId);
            table.setItems(FXCollections.observableArrayList(friends));
            table.sort();
            count.setText(friends.size() + " bạn bè");
            boolean deleted = data.user(userId).status() == UserRow.Status.DELETED;
            String emptyText = deleted ? "Tài khoản đã xóa không còn danh sách bạn bè hiện tại."
                    : "Người dùng chưa có bạn bè hiện tại.";
            ((Label) table.getPlaceholder()).setText(emptyText);
            note.setText(deleted ? emptyText : "Dữ liệu mẫu · Không tính bạn đã xóa; bạn bị khóa vẫn được giữ.");
        };
        ListChangeListener<UserRow> listener = change -> reload.run();
        data.userStore().users().addListener(listener);
        reload.run();
        getDialogPane().setId("user-friends-dialog");
        getDialogPane().setPrefWidth(720);
        getDialogPane().setContent(body);
        getDialogPane().getButtonTypes().add(new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE));
        setResultConverter(button -> null);
        setOnHidden(event -> data.userStore().users().removeListener(listener));
    }
}
