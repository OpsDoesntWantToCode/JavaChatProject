package admin;

import java.net.URL;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Điểm khởi chạy riêng của bản xem trước Admin. */
public final class AdminApplication extends Application {
    @Override
    public void start(Stage stage) {
        AdminMainView root = new AdminMainView(stage::close);
        Scene scene = new Scene(root, 1000, 700);
        URL stylesheet = Objects.requireNonNull(
                AdminApplication.class.getResource("/admin/admin.css"),
                "Không tìm thấy tài nguyên /admin/admin.css");
        scene.getStylesheets().add(stylesheet.toExternalForm());
        stage.setTitle("Java Chat System — Admin");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
