package user;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Cột Hero Branding (Hiển thị khi ở chế độ Fullscreen hoặc màn hình rộng >= 1000px)
 * Thiết kế Modern Obsidian Surface & Developer Tooling UI:
 * - Tuyệt đối không dùng box oval/pill.
 * - Icon Vector SVG sắc nét, độ tương phản cao với hộp chứa tinted độc lập, không bị chìm vào nền.
 * - Cỡ chữ tương đương với màn Đăng ký và Đăng nhập (26px, 14px, 14.5px, 13.5px).
 * - Bo góc 10px cho các thẻ tính năng, tương phản cao sắc nét (WCAG AAA).
 */
public final class UserHeroSection {

    // Vector SVG Paths chuẩn nét
    private static final String SVG_LOCK = "M12 2C9.24 2 7 4.24 7 7v3H6c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-8c0-1.1-.9-2-2-2h-1V7c0-2.76-2.24-5-5-5zm-3 8V7c0-1.66 1.34-3 3-3s3 1.34 3 3v3H9zm3 6c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z";
    private static final String SVG_ZAP = "M13 2L3 14h8l-1 8 11-12h-9l2-8z";
    private static final String SVG_USERS = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
    private static final String SVG_CHAT = "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z";

    private UserHeroSection() {}

    public static VBox createHeroSection() {
        VBox hero = new VBox(22);
        hero.getStyleClass().add("hero-brand-section");
        hero.setMaxWidth(460);
        hero.setAlignment(Pos.CENTER_LEFT);

        // 1. Logo Box phẳng và nhãn chữ phân cấp
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        StackPane logoBox = new StackPane();
        logoBox.getStyleClass().add("hero-logo-box");
        SVGPath logoSvg = new SVGPath();
        logoSvg.setContent(SVG_CHAT);
        logoSvg.setFill(Color.WHITE);
        logoBox.getChildren().add(logoSvg);

        Label lblTag = new Label("DESKTOP CLIENT • JAVA 21 & POSTGRESQL");
        lblTag.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #8E929B;");

        topBar.getChildren().addAll(logoBox, lblTag);

        // 2. Tiêu đề và Phụ đề rút gọn (Cỡ chữ chuẩn 26px & 14px đồng bộ với Auth Card)
        VBox titleBox = new VBox(8);
        Label lblTitle = new Label("Java Chat System");
        lblTitle.getStyleClass().add("hero-title");

        Label lblSubtitle = new Label("Giao tiếp thời gian thực bảo mật cao cho máy tính để bàn.");
        lblSubtitle.getStyleClass().add("hero-subtitle");
        lblSubtitle.setWrapText(true);

        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        // 3. Danh sách 3 Thẻ tính năng với Icon Vector đơn sắc trắng thuần túy (Minimalist, không màu mè)
        VBox featuresBox = new VBox(10);
        featuresBox.getChildren().addAll(
            createFeatureChip(SVG_LOCK, "Mã hóa AES-GCM", "Khóa riêng từng phiên chat, an toàn dữ liệu."),
            createFeatureChip(SVG_ZAP, "TCP Socket thuần", "Độ trễ thấp, kết nối liên tục không gián đoạn."),
            createFeatureChip(SVG_USERS, "Quản lý hội thoại", "Nhắn tin trực tiếp, nhóm chat và danh bạ trực tuyến.")
        );

        // 4. Footer Credit
        Label lblCredit = new Label("Đồ án Lập trình Ứng dụng Java — Phân hệ User (Tuấn Minh)");
        lblCredit.setStyle("-fx-font-size: 12px; -fx-text-fill: #8E929B;");

        hero.getChildren().addAll(topBar, titleBox, featuresBox, lblCredit);
        return hero;
    }

    private static HBox createFeatureChip(String svgPathData, String title, String description) {
        HBox chip = new HBox(14);
        chip.getStyleClass().add("feature-chip");
        chip.setAlignment(Pos.CENTER_LEFT);

        // Hộp chứa Icon Badge bo góc 8px với màu nền tối giản trung tính, không màu mè
        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(38, 38);
        iconBadge.setMinSize(38, 38);
        iconBadge.setMaxSize(38, 38);
        iconBadge.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-border-width: 1px;");

        // Icon Vector thuần sắc trắng (Pure White), lấy từ thư viện Lucide/Feather
        SVGPath svg = new SVGPath();
        svg.setContent(svgPathData);
        svg.setFill(Color.WHITE);
        iconBadge.getChildren().add(svg);

        VBox textBox = new VBox(3);
        Label lblT = new Label(title);
        lblT.getStyleClass().add("feature-chip-title");

        Label lblD = new Label(description);
        lblD.getStyleClass().add("feature-chip-desc");
        lblD.setWrapText(true);

        textBox.getChildren().addAll(lblT, lblD);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        chip.getChildren().addAll(iconBadge, textBox);
        return chip;
    }
}
