package admin;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

final class UserLoginHistoryDialog extends Dialog<Void>
{
    UserLoginHistoryDialog(Node owner, AdminDemoData data, long userId)
    {
        setTitle("Lịch sử đăng nhập người dùng");
        setHeaderText(data.user(userId).username() + " · Lịch sử đăng nhập");
        AdminDialogs.prepare(this, owner);
        GlobalLoginHistoryView view = new GlobalLoginHistoryView(data, userId);
        getDialogPane().setId("user-login-history-dialog");
        getDialogPane().setPrefWidth(720);
        getDialogPane().setContent(view);
        getDialogPane().getButtonTypes().add(new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE));
        setResultConverter(button -> null);
        setOnHidden(event -> view.close());
    }
}
