package admin;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Thanh điều hướng chỉ thuộc phân hệ Admin, không chứa nghiệp vụ Server. */
public final class AdminNavigation extends VBox {
    public enum Page {
        USERS("Quản lý người dùng", "Thông tin tài khoản và các thao tác quản trị.",
                List.of("Hồ sơ người dùng", "Khóa và mở khóa tài khoản", "Lịch sử đăng nhập và bạn bè")),
        LOGIN_HISTORY("Lịch sử đăng nhập", "Theo dõi các lần đăng nhập trên toàn hệ thống.",
                List.of("Thời gian đăng nhập", "Tên đăng nhập và họ tên", "Tra cứu theo khoảng thời gian")),
        GROUPS("Nhóm chat", "Tra cứu nhóm và vai trò của từng thành viên.",
                List.of("Danh sách nhóm chat", "Thành viên trong nhóm", "Danh sách quản trị viên nhóm")),
        SPAM_REPORTS("Báo cáo spam", "Xem phản ánh và quản lý tài khoản bị báo cáo.",
                List.of("Người gửi và người bị báo cáo", "Thời gian và lý do báo cáo", "Khóa tài khoản vi phạm")),
        NEW_USERS("Người dùng đăng ký mới", "Tra cứu tài khoản được tạo trong khoảng thời gian.",
                List.of("Khoảng ngày đăng ký", "Lọc theo họ tên", "Sắp xếp theo tên hoặc ngày tạo")),
        REGISTRATION_CHART("Biểu đồ đăng ký", "Theo dõi số người đăng ký mới qua 12 tháng.",
                List.of("Lựa chọn năm", "Số đăng ký theo từng tháng", "Hiển thị cả tháng không có đăng ký")),
        FRIEND_STATISTICS("Thống kê bạn bè", "Theo dõi các mối quan hệ bạn bè của người dùng.",
                List.of("Số bạn trực tiếp", "Thống kê bạn của bạn", "Lọc theo số bạn trực tiếp")),
        ACTIVE_USERS("Người dùng hoạt động", "Tra cứu hoạt động trong khoảng thời gian.",
                List.of("Lượt mở ứng dụng", "Số người đã chủ động chat", "Số nhóm đã chủ động chat")),
        ACTIVITY_CHART("Biểu đồ hoạt động", "Theo dõi số người mở ứng dụng qua 12 tháng.",
                List.of("Lựa chọn năm", "Số người hoạt động theo tháng", "Mỗi người được tính một lần trong tháng"));

        private final String title;
        private final String description;
        private final List<String> features;

        Page(String title, String description, List<String> features) {
            this.title = title;
            this.description = description;
            this.features = features;
        }

        public String title() { return title; }
        public String description() { return description; }
        public List<String> features() { return features; }
    }

    private final ToggleGroup group = new ToggleGroup();
    private final Map<Page, ToggleButton> buttons = new EnumMap<>(Page.class);

    public AdminNavigation(Consumer<Page> onNavigate) {
        Objects.requireNonNull(onNavigate);
        getStyleClass().add("admin-sidebar");
        setPrefWidth(230);
        setMinWidth(230);
        Label brand = new Label("JAVA CHAT");
        brand.getStyleClass().add("brand");
        Label subtitle = new Label("Không gian quản trị");
        subtitle.getStyleClass().add("brand-subtitle");
        VBox identity = new VBox(6, brand, subtitle);
        identity.getStyleClass().add("brand-block");
        Label section = new Label("QUẢN TRỊ HỆ THỐNG");
        section.getStyleClass().add("nav-section");
        getChildren().addAll(identity, section);

        int index = 1;
        for (Page page : Page.values()) {
            Label number = new Label("%02d".formatted(index++));
            number.getStyleClass().add("nav-number");
            ToggleButton button = new ToggleButton(page.title(), number);
            button.getStyleClass().add("nav-button");
            button.setId("nav-" + page.name().toLowerCase(java.util.Locale.ROOT));
            button.setAccessibleText(page.title());
            button.setAlignment(Pos.CENTER_LEFT);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMinHeight(42);
            button.setToggleGroup(group);
            button.setUserData(page);
            buttons.put(page, button);
            getChildren().add(button);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label footer = new Label("Java Chat System\nBản xem trước giao diện");
        footer.getStyleClass().add("sidebar-footer");
        getChildren().addAll(spacer, footer);

        group.selectedToggleProperty().addListener((observable, previous, selected) -> {
            if (selected == null) {
                // Giữ một mục được chọn ngay cả khi người dùng bấm lại mục hiện tại.
                if (previous != null) { group.selectToggle(previous); }
                return;
            }
            onNavigate.accept((Page) selected.getUserData());
        });
        selectPage(Page.USERS);
    }

    public void selectPage(Page page) {
        group.selectToggle(buttons.get(Objects.requireNonNull(page)));
    }
}
