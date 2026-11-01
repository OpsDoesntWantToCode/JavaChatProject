package user;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Ứng dụng JavaFX chính thức cho phân hệ User (Minh phụ trách)
 * Quản lý chuyển cảnh mượt mà giữa Đăng nhập (UserLoginView) và Đăng ký (UserRegisterView)
 */
public class UserLoginApp extends Application {

    private Stage primaryStage;
    private Scene scene;
    private UserLoginView loginView;
    private UserRegisterView registerView;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Java Chat System — User Portal (JavaFX 21)");

        // Khoi tao cac views
        loginView = new UserLoginView(this::showRegisterView);
        registerView = new UserRegisterView(this::showLoginView);

        // Khoi tao Scene ban dau voi LoginView
        Parent initialRoot = loginView.getView();
        scene = new Scene(initialRoot, 520, 620);

        // Load stylesheet CSS Discord Theme
        String css = getClass().getResource("discord-theme.css") != null 
            ? getClass().getResource("discord-theme.css").toExternalForm()
            : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        stage.setScene(scene);
        stage.setMinWidth(460);
        stage.setMinHeight(560);
        stage.centerOnScreen();
        stage.show();
    }

    private void showRegisterView() {
        switchViewSmooth(registerView.getView(), 560, 680);
    }

    private void showLoginView() {
        switchViewSmooth(loginView.getView(), 520, 620);
    }

    /**
     * Hieu ung chuyen canh muot ma Fade & Slide (180ms)
     */
    private void switchViewSmooth(Parent newView, double targetWidth, double targetHeight) {
        Parent oldRoot = scene.getRoot();

        // 1. Fade out view cu
        FadeTransition fadeOut = new FadeTransition(Duration.millis(100), oldRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.1);
        fadeOut.setOnFinished(e -> {
            scene.setRoot(newView);
            primaryStage.setWidth(targetWidth);
            primaryStage.setHeight(targetHeight);

            // 2. Slide nhe va Fade in view moi
            newView.setOpacity(0.0);
            newView.setTranslateY(15);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(120), newView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(120), newView);
            slideIn.setFromY(15);
            slideIn.setToY(0);

            fadeIn.play();
            slideIn.play();
        });
        fadeOut.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

