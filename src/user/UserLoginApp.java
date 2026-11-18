package user;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Ứng dụng JavaFX chính thức cho phân hệ User (Minh phụ trách)
 * Chuẩn thiết kế Centered Card sang trọng (như Telegram Desktop, Discord, Zalo):
 * - Thẻ Đăng nhập / Đăng ký luôn căn giữa màn hình ở mọi kích thước.
 * - Nền Ambient Canvas Obsidian tối giản với quầng sáng mềm (Subtle Glow).
 * - 100% Vector & CSS: Không bể hạt, không răng cưa, sắc nét tuyệt đối trên mọi độ phân giải.
 */
public class UserLoginApp extends Application {

    private Stage primaryStage;
    private Scene scene;
    private BorderPane authShell;
    private CustomTitleBar authTitleBar;
    private StackPane masterRoot;
    private StackPane formContainer;

    private UserLoginView loginView;
    private UserRegisterView registerView;
    private UserForgotPasswordView forgotPasswordView;
    private UserMainWorkspace mainWorkspace;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Java Chat System — User Portal (JavaFX 21)");

        // Khởi tạo các Views với Navigation Callbacks
        loginView = new UserLoginView(this::showRegisterView, this::showForgotPasswordView, this::showMainWorkspace);
        registerView = new UserRegisterView(this::showLoginView);
        forgotPasswordView = new UserForgotPasswordView(this::showLoginView);

        // Khởi tạo khung chứa chính (Master Shell)
        masterRoot = new StackPane();
        masterRoot.getStyleClass().add("ambient-canvas");
        masterRoot.setAlignment(Pos.CENTER);

        // Form Container luôn căn giữa tuyệt đối
        formContainer = new StackPane();
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(24));

        // Đặt LoginView vào formContainer ban đầu
        formContainer.getChildren().add(loginView.getView());
        masterRoot.getChildren().add(formContainer);

        // Shell chứa Custom Title Bar cho phân hệ Auth
        authTitleBar = new CustomTitleBar(stage, "Java Chat System — Cổng đăng nhập", null);
        authShell = new BorderPane();
        authShell.getStyleClass().add("ambient-canvas");
        authShell.setTop(authTitleBar);
        authShell.setCenter(masterRoot);

        // Khởi tạo Scene ban đầu chuẩn Telegram/Discord Desktop (540 x 600)
        scene = new Scene(authShell, 540, 600);

        // Nạp stylesheet CSS chính thức của hệ thống (theme.css)
        java.net.URL cssUrl = getClass().getResource("theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: theme.css not found in classpath!");
        }

        stage.setScene(scene);
        stage.setMinWidth(520);
        stage.setMinHeight(560);
        stage.centerOnScreen();

        // Gắn tính năng kéo giãn mép cửa sổ Undecorated chuẩn ngành
        WindowResizeHelper.attach(stage, scene);

        stage.show();
    }

    private void showRegisterView() {
        switchViewSmooth(registerView.getView(), 760, 720);
    }

    private void showLoginView() {
        if (scene.getRoot() != authShell) {
            scene.setRoot(authShell);
            primaryStage.setTitle("Java Chat System — User Portal (JavaFX 21)");
            primaryStage.setMinWidth(520);
            primaryStage.setMinHeight(560);
            primaryStage.setWidth(540);
            primaryStage.setHeight(600);
            primaryStage.centerOnScreen();
        }
        switchViewSmooth(loginView.getView(), 540, 600);
    }

    private void showForgotPasswordView() {
        switchViewSmooth(forgotPasswordView.getView(), 540, 580);
    }

    /**
     * Chuyển sang Khung Không gian Làm việc Chính (UserMainWorkspace — Task U3)
     * Kích thước chuẩn 1000x700 responsive theo quy chuẩn đồ án
     */
    private void showMainWorkspace() {
        mainWorkspace = new UserMainWorkspace(primaryStage, this::showLoginView);
        mainWorkspace.setOpacity(0.0);

        scene.setRoot(mainWorkspace);
        primaryStage.setTitle("Java Chat System — Không gian làm việc (@tuanminh_2026)");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.setWidth(1080);
        primaryStage.setHeight(720);
        primaryStage.centerOnScreen();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), mainWorkspace);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Chuyển cảnh mượt mà Fade & Slide (120ms - 60fps) bên trong formContainer
     */
    private void switchViewSmooth(Parent newView, double targetWidth, double targetHeight) {
        Node oldView = formContainer.getChildren().isEmpty() ? null : formContainer.getChildren().get(0);

        if (oldView == null) {
            formContainer.getChildren().setAll(newView);
            return;
        }

        // 1. Fade out view cũ
        FadeTransition fadeOut = new FadeTransition(Duration.millis(90), oldView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.1);
        fadeOut.setOnFinished(e -> {
            formContainer.getChildren().setAll(newView);

            // Chỉ thay đổi kích thước stage nếu không ở chế độ Maximize hoặc Fullscreen
            if (!primaryStage.isMaximized() && !primaryStage.isFullScreen()) {
                primaryStage.setWidth(targetWidth);
                primaryStage.setHeight(targetHeight);
            }

            // 2. Slide nhẹ và Fade in view mới
            newView.setOpacity(0.0);
            newView.setTranslateY(10);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(110), newView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(110), newView);
            slideIn.setFromY(10);
            slideIn.setToY(0);

            fadeIn.play();
            slideIn.play();
        });
        fadeOut.play();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "native");
        launch(args);
    }
}
