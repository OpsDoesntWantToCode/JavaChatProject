package user;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Chào mừng & Giới thiệu Tính năng (Empty State Carousel — Chuẩn Zalo PC Ảnh 1, 2)
 * Hiển thị ở Cột 3 khi người dùng chưa chọn cuộc trò chuyện hoặc khi khởi động ứng dụng:
 * - Minh họa đồ họa vector phẳng trực quan (Mã hóa AES-GCM, Chat nhóm, Trợ lý AI)
 * - Tiêu đề chào mừng lớn và mô tả súc tích
 * - Phân trang dạng chấm (Dots Pagination Indicator)
 * - Nút mũi tên lướt slide (< và >) kèm hiệu ứng chuyển động mượt mà
 * - Nút kêu gọi hành động CTA "Bắt đầu trò chuyện ngay 💬"
 */
public class EmptyChatWelcomeView extends StackPane {

    public static class CarouselSlide {
        private final String title;
        private final String subtitle;
        private final String highlightText;
        private final Node illustration;

        public CarouselSlide(String title, String subtitle, String highlightText, Node illustration) {
            this.title = title;
            this.subtitle = subtitle;
            this.highlightText = highlightText;
            this.illustration = illustration;
        }

        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getHighlightText() { return highlightText; }
        public Node getIllustration() { return illustration; }
    }

    private final List<CarouselSlide> slides = new ArrayList<>();
    private int currentSlideIndex = 0;

    private final VBox slideContentBox = new VBox(14);
    private final Label lblTitle = new Label();
    private final Label lblSubtitle = new Label();
    private final Label lblHighlight = new Label();
    private final StackPane illustrationContainer = new StackPane();

    private final HBox dotsBox = new HBox(8);
    private final List<Rectangle> dotIndicators = new ArrayList<>();

    private final Button btnPrev = new Button("‹");
    private final Button btnNext = new Button("›");
    private final Button btnStartAction = new Button("Bắt đầu trò chuyện ngay 💬");

    private final Runnable onStartChat;

    public EmptyChatWelcomeView(Runnable onStartChat) {
        this.onStartChat = onStartChat;

        getStyleClass().add("empty-welcome-pane");
        setStyle("-fx-background-color: -bg-canvas;");

        initSlides();
        buildUI();
        displaySlide(0, true);
    }

    private void initSlides() {
        // Slide 1: Bảo mật & Mã hóa E2EE AES-GCM
        slides.add(new CarouselSlide(
            "Chào mừng đến với Java Chat System!",
            "Khám phá ứng dụng trò chuyện và làm việc nhóm hiện đại, hỗ trợ mã hóa đầu cuối AES-GCM 256-bit tối ưu cho máy tính của bạn.",
            "Bảo mật thông tin & Truyền tải Socket thời gian thực",
            createShieldIllustration()
        ));

        // Slide 2: Làm việc & Trò chuyện Nhóm hiệu quả
        slides.add(new CarouselSlide(
            "Kết nối & Trò chuyện Nhóm Hiệu quả",
            "Tạo nhóm chat đồ án, phân quyền Quản trị viên, trao đổi thông tin tức thời và chia sẻ tệp tin tài liệu an toàn tuyệt đối.",
            "Giải quyết công việc nhóm nhanh hơn, tiện lợi hơn",
            createGroupIllustration()
        ));

        // Slide 3: Trợ lý AI Thông minh
        slides.add(new CarouselSlide(
            "Tăng tốc Công việc cùng Trợ lý AI",
            "Trợ lý AI sẵn sàng hỗ trợ tóm tắt nội dung cuộc họp, tra cứu cơ sở dữ liệu và đề xuất phản hồi thông minh với lệnh /ai.",
            "Tích hợp mô hình ngôn ngữ lớn (LLM API) trong từng hội thoại",
            createAiIllustration()
        ));
    }

    private void buildUI() {
        // Cấu hình khối nội dung slide
        slideContentBox.setAlignment(Pos.CENTER);
        slideContentBox.setMaxWidth(560);
        slideContentBox.setPadding(new Insets(24));

        lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: -text-normal; -fx-text-alignment: center;");
        lblTitle.setWrapText(true);

        lblSubtitle.setStyle("-fx-font-size: 13.5px; -fx-text-fill: -text-muted; -fx-text-alignment: center; -fx-line-spacing: 3px;");
        lblSubtitle.setWrapText(true);
        lblSubtitle.setMaxWidth(500);

        lblHighlight.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -accent-primary; -fx-text-alignment: center;");
        lblHighlight.setWrapText(true);

        illustrationContainer.setMinHeight(160);
        illustrationContainer.setPrefHeight(180);
        illustrationContainer.setMaxHeight(180);
        illustrationContainer.setAlignment(Pos.CENTER);

        // Dots Pagination
        dotsBox.setAlignment(Pos.CENTER);
        dotsBox.setPadding(new Insets(10, 0, 14, 0));
        for (int i = 0; i < slides.size(); i++) {
            final int targetIndex = i;
            Rectangle dot = new Rectangle(8, 8);
            dot.setArcWidth(8);
            dot.setArcHeight(8);
            dot.setFill(Color.web("#3A3D46"));
            dot.setStyle("-fx-cursor: hand;");
            dot.setOnMouseClicked(e -> displaySlide(targetIndex, targetIndex > currentSlideIndex));
            dotIndicators.add(dot);
            dotsBox.getChildren().add(dot);
        }

        // Nút CTA Bắt đầu trò chuyện
        btnStartAction.getStyleClass().add("btn-primary");
        btnStartAction.setStyle("-fx-font-size: 13.5px; -fx-padding: 10px 24px; -fx-background-radius: 20px;");
        btnStartAction.setOnAction(e -> {
            if (onStartChat != null) {
                onStartChat.run();
            }
        });
        JavaFXTheme.applyTactileButton(btnStartAction);

        slideContentBox.getChildren().addAll(
            illustrationContainer,
            lblTitle,
            lblSubtitle,
            dotsBox,
            lblHighlight,
            btnStartAction
        );

        // Các nút điều hướng < và >
        btnPrev.getStyleClass().add("btn-icon-subtle");
        btnPrev.setStyle(
            "-fx-font-size: 24px; -fx-text-fill: -text-muted; " +
            "-fx-background-color: rgba(30, 33, 40, 0.6); -fx-border-color: #2A2D35; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-border-width: 1px; " +
            "-fx-pref-width: 40px; -fx-pref-height: 40px; -fx-cursor: hand;"
        );
        btnPrev.setOnAction(e -> navigatePrev());
        JavaFXTheme.applyIconButton(btnPrev);

        btnNext.getStyleClass().add("btn-icon-subtle");
        btnNext.setStyle(
            "-fx-font-size: 24px; -fx-text-fill: -text-muted; " +
            "-fx-background-color: rgba(30, 33, 40, 0.6); -fx-border-color: #2A2D35; " +
            "-fx-border-radius: 20px; -fx-background-radius: 20px; -fx-border-width: 1px; " +
            "-fx-pref-width: 40px; -fx-pref-height: 40px; -fx-cursor: hand;"
        );
        btnNext.setOnAction(e -> navigateNext());
        JavaFXTheme.applyIconButton(btnNext);

        getChildren().addAll(slideContentBox, btnPrev, btnNext);
        StackPane.setAlignment(slideContentBox, Pos.CENTER);
        StackPane.setAlignment(btnPrev, Pos.CENTER_LEFT);
        StackPane.setMargin(btnPrev, new Insets(0, 0, 0, 32));
        StackPane.setAlignment(btnNext, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnNext, new Insets(0, 32, 0, 0));
    }

    public void navigateNext() {
        int nextIndex = (currentSlideIndex + 1) % slides.size();
        displaySlide(nextIndex, true);
    }

    public void navigatePrev() {
        int prevIndex = (currentSlideIndex - 1 + slides.size()) % slides.size();
        displaySlide(prevIndex, false);
    }

    private void displaySlide(int index, boolean slideFromRight) {
        if (index < 0 || index >= slides.size()) return;
        this.currentSlideIndex = index;

        CarouselSlide slide = slides.get(index);

        // Cập nhật text & hình ảnh
        lblTitle.setText(slide.getTitle());
        lblSubtitle.setText(slide.getSubtitle());
        lblHighlight.setText(slide.getHighlightText());
        illustrationContainer.getChildren().setAll(slide.getIllustration());

        // Cập nhật trạng thái thanh Dots
        for (int i = 0; i < dotIndicators.size(); i++) {
            Rectangle dot = dotIndicators.get(i);
            if (i == index) {
                dot.setWidth(22);
                dot.setFill(Color.web("#4E6BF2"));
            } else {
                dot.setWidth(8);
                dot.setFill(Color.web("#3A3D46"));
            }
        }

        // Hoạt ảnh chuyển đổi mượt mà
        slideContentBox.setOpacity(0.0);
        slideContentBox.setTranslateX(slideFromRight ? 18 : -18);

        TranslateTransition tt = new TranslateTransition(Duration.millis(200), slideContentBox);
        tt.setToX(0);
        tt.setInterpolator(JavaFXTheme.FLUENT_DECELERATE);

        FadeTransition ft = new FadeTransition(Duration.millis(200), slideContentBox);
        ft.setToValue(1.0);
        ft.setInterpolator(JavaFXTheme.FLUENT_DECELERATE);

        new ParallelTransition(tt, ft).play();
    }

    // =========================================================================
    // HỌA TIẾT MINH HỌA VECTOR PHẲNG NGHỆ THUẬT (FLAT VECTOR ART)
    // =========================================================================
    private Node createShieldIllustration() {
        StackPane box = new StackPane();
        Circle outer = new Circle(58, Color.web("#141E34"));
        outer.setStroke(Color.web("#26427D"));
        outer.setStrokeWidth(1.5);

        Circle inner = new Circle(44, Color.web("#1A284A"));

        SVGPath shieldSvg = new SVGPath();
        shieldSvg.setContent("M 12 1 L 3 5 L 3 11 C 3 16.55 6.84 21.74 12 23 C 17.16 21.74 21 16.55 21 11 L 21 5 L 12 1 Z");
        shieldSvg.setFill(Color.web("#4E6BF2"));
        shieldSvg.setScaleX(2.0);
        shieldSvg.setScaleY(2.0);

        Label lblKey = new Label("🔒");
        lblKey.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF;");

        box.getChildren().addAll(outer, inner, shieldSvg, lblKey);
        return box;
    }

    private Node createGroupIllustration() {
        HBox box = new HBox(-12);
        box.setAlignment(Pos.CENTER);

        box.getChildren().addAll(
            createAvatarCircle("TM", "#3B82F6", 26),
            createAvatarCircle("CH", "#10B981", 32),
            createAvatarCircle("LA", "#EC4899", 26)
        );
        return box;
    }

    private StackPane createAvatarCircle(String initials, String color, double radius) {
        StackPane sp = new StackPane();
        Circle c = new Circle(radius, Color.web(color));
        c.setStroke(Color.web("#0C0D11"));
        c.setStrokeWidth(2.5);

        Label lbl = new Label(initials);
        lbl.setStyle("-fx-font-size: " + (radius * 0.75) + "px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        sp.getChildren().addAll(c, lbl);
        return sp;
    }

    private Node createAiIllustration() {
        StackPane box = new StackPane();
        Circle outer = new Circle(58, Color.web("#231838"));
        outer.setStroke(Color.web("#5C3699"));
        outer.setStrokeWidth(1.5);

        Circle inner = new Circle(44, Color.web("#2E1F4A"));

        Label lblBot = new Label("🤖");
        lblBot.setStyle("-fx-font-size: 32px;");

        Label lblSparkle = new Label("✨");
        lblSparkle.setStyle("-fx-font-size: 16px;");
        StackPane.setAlignment(lblSparkle, Pos.TOP_RIGHT);
        StackPane.setMargin(lblSparkle, new Insets(10, 20, 0, 0));

        box.getChildren().addAll(outer, inner, lblBot, lblSparkle);
        return box;
    }
}
