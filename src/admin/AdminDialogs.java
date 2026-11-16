package admin;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/** Cấu hình owner và stylesheet dùng riêng cho các hộp thoại Admin. */
final class AdminDialogs {
    private AdminDialogs() { }

    static void prepare(Dialog<?> dialog, Node owner) {
        dialog.initOwner(owner.getScene().getWindow());
        dialog.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        dialog.getDialogPane().getStyleClass().add("admin-dialog");
        if (owner.getScene().getRoot().getStyleClass().contains("theme-dark")) {
            dialog.getDialogPane().getStyleClass().add("theme-dark");
        }
    }

    static boolean confirm(Node owner, String title, String message) {
        ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType apply = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, cancel, apply);
        alert.setTitle(title);
        alert.setHeaderText(title);
        prepare(alert, owner);
        ((Button) alert.getDialogPane().lookupButton(apply)).setDefaultButton(false);
        ((Button) alert.getDialogPane().lookupButton(cancel)).setDefaultButton(true);
        return alert.showAndWait().filter(apply::equals).isPresent();
    }

    static void information(Node owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        prepare(alert, owner);
        alert.showAndWait();
    }
}
