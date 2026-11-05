package user;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Cột Hero Branding (Hiển thị khi ở chế độ Fullscreen hoặc màn hình rộng >= 1000px)
 * Thiết kế Modern Obsidian Surface & Developer Tooling UI:
 * - Tuyệt đối không dùng box oval/pill.
 * - Cỡ chữ tương đương với màn Đăng ký và Đăng nhập (26px, 14px, 14.5px, 13.5px).
 * - Bo góc 10px cho các thẻ tính năng, tương phản cao sắc nét (WCAG AAA).
 */
public final class UserHeroSection {

    private UserHeroSection() {}

    public static VBox createHeroSection() {
        VBox hero = new VBox(22);
        hero.getStyleClass().add("hero-brand-section");
        hero.setMaxWidth(460);
        hero.setAlignment(Pos.CENTER_LEFT);

        // 1. Logo Box phẳng và nhãn chữ phân cấp (Không dùng badge hình oval/pill)
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        StackPane logoBox = new StackPane();
        logoBox.getStyleClass().add("hero-logo-box");
        Label lblLogo = new Label("💬");
        lblLogo.setStyle("-fx-font-size: 24px;");
        logoBox.getChildren().add(lblLogo);

        Label lblTag = new Label("DESKTOP CLIENT • JAVA 21 & POSTGRESQL");
        lblTag.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #949BA4;");

        topBar.getChildren().addAll(logoBox, lblTag);

        // 2. Tiêu đề và Phụ đề rút gọn (Cỡ chữ chuẩn 26px & 14px đồng bộ với Auth Card)
        VBox titleBox = new VBox(8);
        Label lblTitle = new Label("Java Chat System");
        lblTitle.getStyleClass().add("hero-title");

        Label lblSubtitle = new Label("Giao tiếp thời gian thực bảo mật cao cho máy tính để bàn.");
        lblSubtitle.getStyleClass().add("hero-subtitle");
        lblSubtitle.setWrapText(true);

        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        // 3. Danh sách 3 Thẻ tính năng phẳng hình chữ nhật (Không hình oval)
        // Độ lớn chữ: Tiêu đề 14.5px Bold (#F2F3F5), Mô tả 13.5px (#B5BAC1)
        VBox featuresBox = new VBox(10);
        featuresBox.getChildren().addAll(
            createFeatureChip("🔒", "Mã hóa AES-GCM", "Khóa riêng từng phiên chat, an toàn dữ liệu."),
            createFeatureChip("⚡", "TCP Socket thuần", "Độ trễ thấp, kết nối liên tục không gián đoạn."),
            createFeatureChip("👥", "Quản lý hội thoại", "Nhắn tin trực tiếp, nhóm chat và danh bạ trực tuyến.")
        );

        // 4. Footer Credit
        Label lblCredit = new Label("Đồ án Lập trình Ứng dụng Java — Phân hệ User (Tuấn Minh)");
        lblCredit.setStyle("-fx-font-size: 12px; -fx-text-fill: #949BA4;");

        hero.getChildren().addAll(topBar, titleBox, featuresBox, lblCredit);
        return hero;
    }

    private static HBox createFeatureChip(String icon, String title, String description) {
        HBox chip = new HBox(12);
        chip.getStyleClass().add("feature-chip");
        chip.setAlignment(Pos.CENTER_LEFT);

        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 20px;");

        VBox textBox = new VBox(3);
        Label lblT = new Label(title);
        lblT.getStyleClass().add("feature-chip-title");

        Label lblD = new Label(description);
        lblD.getStyleClass().add("feature-chip-desc");
        lblD.setWrapText(true);

        textBox.getChildren().addAll(lblT, lblD);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        chip.getChildren().addAll(lblIcon, textBox);
        return chip;
    }
}
