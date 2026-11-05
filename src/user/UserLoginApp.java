package user;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Ứng dụng JavaFX chính thức cho phân hệ User (Minh phụ trách)
 * Hỗ trợ chuyển cảnh mượt mà, tỷ lệ vàng và bố cục Split-Screen thông minh khi Fullscreen.
 */
public class UserLoginApp extends Application {

    private Stage primaryStage;
    private Scene scene;
    private StackPane masterRoot;
    private HBox splitBox;
    private VBox heroSection;
    private StackPane formContainer;

    private UserLoginView loginView;
    private UserRegisterView registerView;
    private UserForgotPasswordView forgotPasswordView;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Java Chat System — User Portal (JavaFX 21)");

        // Khởi tạo các Views với Navigation Callbacks
        loginView = new UserLoginView(this::showRegisterView, this::showForgotPasswordView);
        registerView = new UserRegisterView(this::showLoginView);
        forgotPasswordView = new UserForgotPasswordView(this::showLoginView);

        // Khởi tạo khung chứa chính (Master Shell)
        masterRoot = new StackPane();
        masterRoot.getStyleClass().add("ambient-canvas");

        // 1. Các đốm sáng Ambient Glow Orbs mờ ảo phía sau nền
        setupAmbientBackgroundOrbs();

        // 2. Khung Split-Screen HBox
        splitBox = new HBox(40);
        splitBox.setAlignment(Pos.CENTER);
        splitBox.setPadding(new Insets(24, 48, 24, 48));

        // Cột trái: Hero Branding Section
        heroSection = UserHeroSection.createHeroSection();
        heroSection.setVisible(false);
        heroSection.setManaged(false);

        // Cột phải: Form Container
        formContainer = new StackPane();
        formContainer.setAlignment(Pos.CENTER);
        HBox.setHgrow(formContainer, Priority.ALWAYS);

        // Đặt LoginView vào formContainer ban đầu
        formContainer.getChildren().add(loginView.getView());

        splitBox.getChildren().addAll(heroSection, formContainer);
        masterRoot.getChildren().add(splitBox);

        // Khởi tạo Scene ban đầu chuẩn Discord Desktop (540 x 600)
        scene = new Scene(masterRoot, 540, 600);

        // Lắng nghe kích thước để tự động kích hoạt Split-Screen khi rộng >= 1080px (chế độ Fullscreen thực thụ)
        scene.widthProperty().addListener((obs, oldW, newW) -> updateResponsiveLayout(newW.doubleValue()));

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
        stage.show();
    }

    private void setupAmbientBackgroundOrbs() {
        // Vầng sáng Aura khuếch tán cực nhẹ (~8% opacity) màu Sapphire Tech, êm dịu, không neon
        Circle ambientAura = new Circle(380);
        ambientAura.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.rgb(78, 107, 242, 0.08)),
            new Stop(0.6, Color.rgb(78, 107, 242, 0.02)),
            new Stop(1.0, Color.TRANSPARENT)));
        StackPane.setAlignment(ambientAura, Pos.CENTER);
        ambientAura.setMouseTransparent(true);

        masterRoot.getChildren().add(ambientAura);
    }

    private void updateResponsiveLayout(double windowWidth) {
        boolean isWide = windowWidth >= 1080;
        if (heroSection.isVisible() != isWide) {
            heroSection.setVisible(isWide);
            heroSection.setManaged(isWide);
            if (isWide) {
                HBox.setHgrow(heroSection, Priority.SOMETIMES);
            }
        }
    }

    private void showRegisterView() {
        switchViewSmooth(registerView.getView(), 820, 750);
    }

    private void showLoginView() {
        switchViewSmooth(loginView.getView(), 540, 600);
    }

    private void showForgotPasswordView() {
        switchViewSmooth(forgotPasswordView.getView(), 540, 580);
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
            if (!primaryStage.isMaximized() && !primaryStage.isFullScreen() && !heroSection.isVisible()) {
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
