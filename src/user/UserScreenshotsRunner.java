package user;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Tiện ích chụp ảnh tự động toàn bộ 19 màn hình phân hệ User phục vụ Báo cáo Đồ án và Minh chứng mốc 20%.
 * Quy chuẩn đặt tên: u1 .. u12 bám sát 100% thứ tự 12 chức năng trong final-project.pdf:
 * - Mục 1:  u1-register.png (Đăng ký tài khoản)
 * - Mục 2:  u2-profile-view.png & u2-profile-edit.png (Cập nhật thông tin tài khoản)
 * - Mục 3:  u3-forgot-password.png (Khởi tạo lại mật khẩu: random gửi tới email)
 * - Mục 4:  u4-change-password.png (Cập nhật mật khẩu)
 * - Mục 5:  u5-login.png (Đăng nhập)
 * - Mục 6:  u6-friends-all.png (Danh sách bạn bè, hủy kết bạn, block tài khoản)
 * - Mục 7:  u7-friends-pending.png (Danh sách yêu cầu kết bạn)
 * - Mục 8:  u8-friends-online.png (Danh sách bạn bè đang online)
 * - Mục 9:  u9-friends-search.png (Tìm người dựa vào username/tên, loại trừ block)
 * - Mục 10: u10-spam-report.png (Báo cáo spam khi chat)
 * - Mục 11: u11-workspace-direct.png, u11-direct-chat-details.png, u11-message-multiselect.png, u11-global-search.png, u11-smart-replies.png (Chat & Lịch sử)
 * - Mục 12: u12-workspace-group.png, u12-group-create.png, u12-member-add.png (Nhóm chat & Quản lý nhóm)
 */
public class UserScreenshotsRunner extends Application {

    private static final File DIR_USER_SPEC = new File("docs/user-spec/evidence");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DIR_USER_SPEC.mkdirs();

        new Thread(() -> {
            try {
                // Cho JavaFX khởi tạo hoàn chỉnh
                Thread.sleep(600);

                // =========================================================================
                // MỤC 1: ĐĂNG KÝ TÀI KHOẢN
                // =========================================================================
                captureSync(primaryStage, "u1-register.png", 1040, 720, () -> {
                    StackPane root = new StackPane();
                    root.setStyle("-fx-background-color: -bg-canvas;");
                    UserRegisterView regView = new UserRegisterView(() -> {});
                    root.getChildren().add(regView.getView());
                    return root;
                });

                // =========================================================================
                // MỤC 2: CẬP NHẬT THÔNG TIN TÀI KHOẢN
                // =========================================================================
                // 2a. Màn hình Hiển thị hồ sơ người dùng
                captureSync(primaryStage, "u2-profile-view.png", 640, 600, () -> {
                    return buildProfileViewCard();
                });

                // 2b. Màn hình Khi đang thay đổi/điều chỉnh hồ sơ người dùng
                captureSync(primaryStage, "u2-profile-edit.png", 640, 620, () -> {
                    return buildProfileEditCard();
                });

                // =========================================================================
                // MỤC 3: KHỞI TẠO LẠI MẬT KHẨU (Random gửi tới email)
                // =========================================================================
                captureSync(primaryStage, "u3-forgot-password.png", 640, 500, () -> {
                    StackPane root = new StackPane();
                    root.setStyle("-fx-background-color: -bg-canvas;");
                    UserForgotPasswordView forgotView = new UserForgotPasswordView(() -> {});
                    root.getChildren().add(forgotView.getView());
                    return root;
                });

                // =========================================================================
                // MỤC 4: CẬP NHẬT MẬT KHẨU
                // =========================================================================
                captureSync(primaryStage, "u4-change-password.png", 460, 560, () -> {
                    return buildChangePasswordCard();
                });

                // =========================================================================
                // MỤC 5: ĐĂNG NHẬP
                // =========================================================================
                captureSync(primaryStage, "u5-login.png", 720, 640, () -> {
                    StackPane root = new StackPane();
                    root.getStyleClass().add("ambient-canvas");
                    UserLoginView loginView = new UserLoginView(() -> {}, () -> {});
                    root.getChildren().add(loginView.getView());
                    return root;
                });

                // =========================================================================
                // MỤC 6: DANH SÁCH BẠN BÈ (Online/Offline, Hủy kết bạn, Block tài khoản)
                // =========================================================================
                captureSync(primaryStage, "u6-friends-all.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("all");
                    return buildFriendsAllWithActionMenu(fv);
                });

                // =========================================================================
                // MỤC 7: DANH SÁCH YÊU CẦU KẾT BẠN (Lọc tên, Đồng ý/Từ chối)
                // =========================================================================
                captureSync(primaryStage, "u7-friends-pending.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("pending");
                    return wrapFriendsView(fv);
                });

                // =========================================================================
                // MỤC 8: DANH SÁCH BẠN BÈ ĐANG ONLINE (Lọc tên, Chat, Tạo group)
                // =========================================================================
                captureSync(primaryStage, "u8-friends-online.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("online");
                    return wrapFriendsView(fv);
                });

                // =========================================================================
                // MỤC 9: TÌM NGƯỜI DỰA VÀO USERNAME/TÊN (Không thấy người block mình)
                // =========================================================================
                captureSync(primaryStage, "u9-friends-search.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("add");
                    return wrapFriendsView(fv);
                });

                // =========================================================================
                // MỤC 10: BÁO CÁO SPAM KHI CHAT
                // =========================================================================
                captureSync(primaryStage, "u10-spam-report.png", 540, 540, () -> {
                    return buildSpamReportCard();
                });

                // =========================================================================
                // MỤC 11: CHAT & LỊCH SỬ TIN NHẮN
                // =========================================================================
                // 11.a-b-c. Không gian làm việc Chat 1-1 tổng thể
                captureSync(primaryStage, "u11-workspace-direct.png", 1120, 740, () -> {
                    UserMainWorkspace ws = new UserMainWorkspace(primaryStage, () -> {});
                    try {
                        Method m = UserMainWorkspace.class.getDeclaredMethod("loadConversation", String.class);
                        m.setAccessible(true);
                        m.invoke(ws, "user_chihai");
                    } catch (Exception ignored) {}
                    return ws;
                });

                // 11.c. Khung hội thoại & bong bóng chat 1-1 chi tiết
                captureSync(primaryStage, "u11-direct-chat-details.png", 880, 680, () -> {
                    ChatConversationView cv = new ChatConversationView(primaryStage, () -> {}, () -> {}, () -> {});
                    cv.loadConversation("user_chihai");
                    return cv;
                });

                // 11.e. Xóa 1 hoặc vài dòng trong lịch sử chat (Multi-Select)
                captureSync(primaryStage, "u11-message-multiselect.png", 880, 680, () -> {
                    ChatConversationView cv = new ChatConversationView(primaryStage, () -> {}, () -> {}, () -> {});
                    cv.loadConversation("user_chihai");
                    cv.setMultiSelectMode(true);
                    if (cv.getMessagesList().size() >= 2) {
                        cv.getMessagesList().get(0).setSelected(true);
                        cv.getMessagesList().get(1).setSelected(true);
                    }
                    return cv;
                });

                // 11.g. Tìm kiếm chuỗi trong lịch sử chat với tất cả mọi người
                captureSync(primaryStage, "u11-global-search.png", 680, 600, () -> {
                    return buildGlobalSearchCard();
                });

                // 11.h. Trợ lý AI (LLM) gợi ý tin nhắn & Smart Reply Chips
                captureSync(primaryStage, "u11-smart-replies.png", 880, 680, () -> {
                    ChatConversationView cv = new ChatConversationView(primaryStage, () -> {}, () -> {}, () -> {});
                    cv.loadConversation("user_chihai");
                    cv.setInputMessage("/ai Tóm tắt các nội dung thống nhất về báo cáo 20% giúp mình");
                    cv.toggleAiSmartReplies();
                    return cv;
                });

                // =========================================================================
                // MỤC 12: NHÓM CHAT (Tạo nhóm, Quản lý thành viên/Admin, Mã hóa nhóm)
                // =========================================================================
                // 12.f-g. Không gian làm việc Nhóm chat & Mã hóa nhóm AES-GCM
                captureSync(primaryStage, "u12-workspace-group.png", 1120, 740, () -> {
                    UserMainWorkspace ws = new UserMainWorkspace(primaryStage, () -> {});
                    return ws;
                });

                // 12.a. Hộp thoại Tạo nhóm chat mới (tối thiểu 1 thành viên)
                captureSync(primaryStage, "u12-group-create.png", 560, 600, () -> {
                    return buildGroupCreateCard();
                });

                // 12.c. Hộp thoại Thêm thành viên vào nhóm chat
                captureSync(primaryStage, "u12-member-add.png", 460, 260, () -> {
                    return buildMemberAddCard();
                });

                System.out.println("ALL_USER_SCREENSHOTS_SUCCESS");
                Platform.exit();

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.exit();
            }
        }).start();
    }

    private static Node wrapFriendsView(FriendsManagementView fv) {
        BorderPane container = new BorderPane();
        container.setStyle("-fx-background-color: -bg-canvas;");
        container.setCenter(fv);
        return container;
    }

    /**
     * Dựng màn hình Danh sách bạn bè kèm Context Menu nổi bật 2 thao tác:
     * - Hủy kết bạn (Mục 6.c)
     * - Chặn người dùng (Block - Mục 6.d)
     */
    private static Node buildFriendsAllWithActionMenu(FriendsManagementView fv) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: -bg-canvas;");

        BorderPane container = new BorderPane();
        container.setCenter(fv);
        root.getChildren().add(container);

        // Hộp thoại tùy chọn ngữ cảnh (Action Menu Popup)
        VBox menuCard = new VBox(6);
        menuCard.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        menuCard.setPrefWidth(260);
        menuCard.setPadding(new Insets(10));
        menuCard.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.75), 20, 0, 0, 8);"
        );

        javafx.scene.control.Label lblMenuTitle = new javafx.scene.control.Label("TÙY CHỌN BẠN BÈ (@chihai_2026)");
        lblMenuTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: -text-muted; -fx-padding: 2px 6px;");

        HBox itemUnfriend = createMenuItemNode("✕ Hủy kết bạn (Mục 6.c)", "#FA777C");
        HBox itemBlock = createMenuItemNode("🚫 Chặn người dùng (Block - Mục 6.d)", "#FA777C");

        javafx.scene.control.Label lblNote = new javafx.scene.control.Label("Sau khi block: Người này không thể tìm thấy hoặc gửi lại lời mời kết bạn.");
        lblNote.setWrapText(true);
        lblNote.setStyle("-fx-font-size: 10.5px; -fx-text-fill: -text-muted; -fx-padding: 4px 6px;");

        menuCard.getChildren().addAll(lblMenuTitle, itemUnfriend, itemBlock, lblNote);

        StackPane.setAlignment(menuCard, Pos.TOP_RIGHT);
        StackPane.setMargin(menuCard, new Insets(185, 45, 0, 0));

        root.getChildren().add(menuCard);
        return root;
    }

    private static HBox createMenuItemNode(String text, String textColor) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 10, 8, 10));
        item.setStyle("-fx-background-radius: 6px; -fx-cursor: hand; -fx-background-color: #242731;");
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        item.getChildren().add(lbl);
        return item;
    }

    /**
     * Dựng màn hình Xem thông tin hồ sơ cá nhân (Mục 2 - View Profile)
     */
    private static Node buildProfileViewCard() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Hồ sơ tài khoản cá nhân (Mục 2)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_USER_PLUS, 0.85, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        javafx.scene.control.Label lblSub = new javafx.scene.control.Label("Xem thông tin chi tiết hồ sơ người dùng trong hệ thống");
        lblSub.setStyle("-fx-font-size: 12.5px; -fx-text-fill: -text-muted;");

        // Avatar Row
        HBox avatarBox = new HBox(14);
        avatarBox.setAlignment(Pos.CENTER_LEFT);
        avatarBox.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 12px 16px;");

        StackPane avtCircle = new StackPane();
        Circle c = new Circle(26, Color.web("#4E6BF2"));
        javafx.scene.control.Label lblInit = new javafx.scene.control.Label("TM");
        lblInit.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        avtCircle.getChildren().addAll(c, lblInit);

        VBox userMeta = new VBox(3);
        javafx.scene.control.Label lblFullName = new javafx.scene.control.Label("Trần Nguyễn Tuấn Minh");
        lblFullName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        javafx.scene.control.Label lblTag = new javafx.scene.control.Label("@tuanminh_2026 • Đang trực tuyến (Online)");
        lblTag.setStyle("-fx-font-size: 12px; -fx-text-fill: -status-online; -fx-font-weight: bold;");
        userMeta.getChildren().addAll(lblFullName, lblTag);
        HBox.setHgrow(userMeta, Priority.ALWAYS);

        avatarBox.getChildren().addAll(avtCircle, userMeta);

        // Grid 2 cột thông tin chi tiết
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createReadonlyBox("Tên đăng nhập (Username)", "tuanminh_2026"), 0, 0);
        grid.add(createReadonlyBox("Hộp thư Email", "tuanminh@example.com"), 1, 0);
        grid.add(createReadonlyBox("Họ và tên", "Trần Nguyễn Tuấn Minh"), 0, 1);
        grid.add(createReadonlyBox("Ngày sinh", "29/10/2004"), 1, 1);
        grid.add(createReadonlyBox("Giới tính", "Nam"), 0, 2);
        grid.add(createReadonlyBox("Địa chỉ cư trú", "227 Nguyễn Văn Cừ, Quận 5, TP.HCM"), 1, 2);

        // Cụm nút hành động: Nổi bật nút "Chỉnh sửa thông tin hồ sơ"
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        javafx.scene.control.Button btnClose = new javafx.scene.control.Button("Đóng");
        btnClose.getStyleClass().add("btn-secondary");

        javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("Chỉnh sửa thông tin hồ sơ");
        btnEdit.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_EDIT, 0.85, "#FFFFFF"));
        btnEdit.setGraphicTextGap(8);
        btnEdit.getStyleClass().add("btn-primary");

        actionRow.getChildren().addAll(btnClose, btnEdit);

        root.getChildren().addAll(lblHeader, lblSub, avatarBox, grid, actionRow);
        return root;
    }

    private static VBox createReadonlyBox(String labelText, String valText) {
        VBox box = new VBox(4);
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(labelText);
        lbl.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        javafx.scene.control.TextField tf = new javafx.scene.control.TextField(valText);
        tf.setEditable(false);
        tf.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-text-fill: -text-normal; -fx-opacity: 0.9;");
        box.getChildren().addAll(lbl, tf);
        return box;
    }

    /**
     * Dựng màn hình Cập nhật thông tin tài khoản khi đang chỉnh sửa (Mục 2 - Edit Profile)
     */
    private static Node buildProfileEditCard() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Cập nhật thông tin tài khoản (Mục 2)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_USER_PLUS, 0.85, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        // Banner thông báo đang chỉnh sửa
        HBox banner = new HBox(8);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle("-fx-background-color: rgba(78, 107, 242, 0.15); -fx-border-color: #4E6BF2; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 12px;");
        javafx.scene.control.Label lblBanner = new javafx.scene.control.Label("ℹ️ Đang ở chế độ chỉnh sửa thông tin cá nhân. Nhập các thay đổi và bấm 'Lưu thay đổi hồ sơ'.");
        lblBanner.setStyle("-fx-font-size: 12px; -fx-text-fill: #8BB2FE; -fx-font-weight: bold;");
        banner.getChildren().add(lblBanner);

        // Grid 2 cột trường chỉnh sửa
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Hàng 0: Username và Email cố định
        javafx.scene.control.TextField tfUser = new javafx.scene.control.TextField("tuanminh_2026");
        tfUser.setDisable(true);
        tfUser.setStyle("-fx-opacity: 0.65;");
        grid.add(createFieldBlock("Tên đăng nhập (Cố định)", false, tfUser), 0, 0);

        javafx.scene.control.TextField tfEmail = new javafx.scene.control.TextField("tuanminh@example.com");
        tfEmail.setDisable(true);
        tfEmail.setStyle("-fx-opacity: 0.65;");
        grid.add(createFieldBlock("Hộp thư Email (Cố định)", false, tfEmail), 1, 0);

        // Hàng 1: Họ tên và Ngày sinh
        javafx.scene.control.TextField tfName = new javafx.scene.control.TextField("Trần Nguyễn Tuấn Minh");
        tfName.setStyle("-fx-border-color: #4E6BF2; -fx-border-width: 1.5px; -fx-border-radius: 8px;");
        grid.add(createFieldBlock("Họ và tên", true, tfName), 0, 1);

        javafx.scene.control.DatePicker dp = new javafx.scene.control.DatePicker(java.time.LocalDate.of(2004, 10, 29));
        dp.setMaxWidth(Double.MAX_VALUE);
        grid.add(createFieldBlock("Ngày sinh", true, dp), 1, 1);

        // Hàng 2: Giới tính và Địa chỉ
        javafx.scene.control.ComboBox<String> cbGender = new javafx.scene.control.ComboBox<>();
        cbGender.getItems().addAll("Nam", "Nữ", "Khác");
        cbGender.setValue("Nam");
        cbGender.setMaxWidth(Double.MAX_VALUE);
        grid.add(createFieldBlock("Giới tính", true, cbGender), 0, 2);

        javafx.scene.control.TextField tfAddress = new javafx.scene.control.TextField("227 Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM");
        tfAddress.setStyle("-fx-border-color: #4E6BF2; -fx-border-width: 1.5px; -fx-border-radius: 8px;");
        grid.add(createFieldBlock("Địa chỉ cư trú", true, tfAddress), 1, 2);

        // Action Row
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Hủy bỏ");
        btnCancel.getStyleClass().add("btn-secondary");

        javafx.scene.control.Button btnSave = new javafx.scene.control.Button("💾 Lưu thay đổi hồ sơ");
        btnSave.getStyleClass().add("btn-primary");

        actionRow.getChildren().addAll(btnCancel, btnSave);

        root.getChildren().addAll(lblHeader, banner, grid, actionRow);
        return root;
    }

    /**
     * Dựng màn hình Cập nhật mật khẩu tài khoản (Mục 4)
     */
    private static Node buildChangePasswordCard() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Cập nhật mật khẩu tài khoản (Mục 4)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_LOCK, 0.85, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        javafx.scene.control.Label lblSub = new javafx.scene.control.Label("Để bảo vệ tài khoản, vui lòng thiết lập mật khẩu mới có độ mạnh cao.");
        lblSub.setStyle("-fx-font-size: 12.5px; -fx-text-fill: -text-muted;");

        VBox form = new VBox(12);

        javafx.scene.control.Label l1 = new javafx.scene.control.Label("Mật khẩu hiện tại");
        l1.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        javafx.scene.control.PasswordField pf1 = new javafx.scene.control.PasswordField();
        pf1.setText("OldSecretPassword2026");

        javafx.scene.control.Label l2 = new javafx.scene.control.Label("Mật khẩu mới (Tối thiểu 6 ký tự)");
        l2.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        javafx.scene.control.PasswordField pf2 = new javafx.scene.control.PasswordField();
        pf2.setText("VeryStrongP@ss2026!");

        // Password strength meter
        HBox meter = new HBox(4);
        meter.setAlignment(Pos.CENTER_LEFT);
        meter.setFillHeight(false);
        for (int i = 0; i < 4; i++) {
            Region seg = new Region();
            seg.setMinSize(42, 4);
            seg.setPrefSize(42, 4);
            seg.setMaxSize(42, 4);
            seg.setStyle("-fx-background-color: #23A55A; -fx-background-radius: 2px;");
            meter.getChildren().add(seg);
        }
        javafx.scene.control.Label lblMeter = new javafx.scene.control.Label("Độ mạnh: Rất an toàn ✓");
        lblMeter.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #23A55A; -fx-font-weight: bold;");
        HBox meterRow = new HBox(8, meter, lblMeter);
        meterRow.setAlignment(Pos.CENTER_LEFT);
        meterRow.setFillHeight(false);

        javafx.scene.control.Label l3 = new javafx.scene.control.Label("Xác nhận mật khẩu mới");
        l3.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        javafx.scene.control.PasswordField pf3 = new javafx.scene.control.PasswordField();
        pf3.setText("VeryStrongP@ss2026!");

        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnClose = new javafx.scene.control.Button("Đóng");
        btnClose.getStyleClass().add("btn-secondary");
        javafx.scene.control.Button btnSave = new javafx.scene.control.Button("Cập nhật mật khẩu");
        btnSave.getStyleClass().add("btn-primary");
        actionRow.getChildren().addAll(btnClose, btnSave);

        form.getChildren().addAll(l1, pf1, l2, pf2, meterRow, l3, pf3, actionRow);

        root.getChildren().addAll(lblHeader, lblSub, form);
        return root;
    }

    private static Node buildSpamReportCard() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Báo cáo vi phạm người dùng (Mục 10)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_REPORT, 0.85, "#F59E0B"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        HBox targetBox = new HBox(12);
        targetBox.setAlignment(Pos.CENTER_LEFT);
        targetBox.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 14px;");

        javafx.scene.shape.Circle avatar = new javafx.scene.shape.Circle(18, Color.web("#EF4444"));
        javafx.scene.control.Label lblAvatar = new javafx.scene.control.Label("SP");
        lblAvatar.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatarStack = new StackPane(avatar, lblAvatar);

        VBox userMeta = new VBox(2);
        javafx.scene.control.Label lblName = new javafx.scene.control.Label("Tài khoản Nghi vấn Spam");
        lblName.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        javafx.scene.control.Label lblUsername = new javafx.scene.control.Label("@spammer_bot");
        lblUsername.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted;");
        userMeta.getChildren().addAll(lblName, lblUsername);
        targetBox.getChildren().addAll(avatarStack, userMeta);

        javafx.scene.control.ToggleGroup reasonGroup = new javafx.scene.control.ToggleGroup();
        javafx.scene.control.RadioButton rb1 = new javafx.scene.control.RadioButton("Gửi tin nhắn rác, quảng cáo, lừa đảo (Spam)");
        rb1.setToggleGroup(reasonGroup);
        rb1.setSelected(true);
        javafx.scene.control.RadioButton rb2 = new javafx.scene.control.RadioButton("Quấy rối, đe dọa hoặc dùng từ ngữ xúc phạm");
        rb2.setToggleGroup(reasonGroup);
        javafx.scene.control.RadioButton rb3 = new javafx.scene.control.RadioButton("Giả mạo danh tính người khác");
        rb3.setToggleGroup(reasonGroup);

        VBox radioBox = new VBox(10, rb1, rb2, rb3);

        javafx.scene.control.TextArea txtDetails = new javafx.scene.control.TextArea("Liên tục gửi link quảng cáo không rõ nguồn gốc vào hội thoại lúc 14:30.");
        txtDetails.setPrefRowCount(3);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Hủy bỏ");
        btnCancel.getStyleClass().add("btn-secondary");
        javafx.scene.control.Button btnSubmit = new javafx.scene.control.Button("Gửi báo cáo vi phạm");
        btnSubmit.getStyleClass().add("btn-danger");
        actionBox.getChildren().addAll(btnCancel, btnSubmit);

        root.getChildren().addAll(lblHeader, targetBox, radioBox, txtDetails, actionBox);
        return root;
    }

    private static Node buildGroupCreateCard() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Tạo nhóm trò chuyện mới (Mục 12.a)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_USERS, 0.85, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        VBox form = new VBox(12);

        // 1. Tên nhóm
        javafx.scene.control.Label l1 = new javafx.scene.control.Label("Tên nhóm chat");
        l1.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        javafx.scene.control.TextField tf1 = new javafx.scene.control.TextField("Nhóm Nghiên Cứu Hệ Thống Phân Tán");

        // 2. Tìm kiếm username thành viên
        javafx.scene.control.Label l2 = new javafx.scene.control.Label("Tìm kiếm thành viên theo @username (Tối thiểu 1 người)");
        l2.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.control.TextField tfSearch = new javafx.scene.control.TextField("chihai");
        tfSearch.setPromptText("Nhập @username để thêm thành viên...");
        tfSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: -text-normal; -fx-font-size: 12px;");
        HBox.setHgrow(tfSearch, Priority.ALWAYS);
        searchBox.getChildren().addAll(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_SEARCH, 0.8, "#8E929B"), tfSearch);
        searchBox.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 3px 10px;");

        // 3. Chip tags các thành viên đã chọn
        HBox selectedChips = new HBox(8);
        selectedChips.setAlignment(Pos.CENTER_LEFT);
        selectedChips.getChildren().addAll(
            createMemberChip("Lưu Chí Hải (@chihai_2026)"),
            createMemberChip("Nguyễn Lan Anh (@lananh_it)")
        );

        // 4. Danh sách gợi ý bạn bè
        VBox resultList = new VBox(6);
        resultList.getChildren().addAll(
            createSearchUserRow("CH", "Lưu Chí Hải", "@chihai_2026", true),
            createSearchUserRow("NL", "Lê Hoàng Nam", "@nam_le", false)
        );

        // 5. Checkbox mã hóa nhóm
        javafx.scene.control.CheckBox cbEncrypt = new javafx.scene.control.CheckBox("Bật mã hóa nhóm AES-GCM (mô phỏng)");
        cbEncrypt.setSelected(true);
        cbEncrypt.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");

        // 6. Action Row
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Hủy");
        btnCancel.getStyleClass().add("btn-secondary");
        javafx.scene.control.Button btnCreate = new javafx.scene.control.Button("Tạo nhóm (2 thành viên)");
        btnCreate.getStyleClass().add("btn-primary");
        actionRow.getChildren().addAll(btnCancel, btnCreate);

        form.getChildren().addAll(l1, tf1, l2, searchBox, selectedChips, resultList, cbEncrypt, actionRow);
        root.getChildren().addAll(lblHeader, form);
        return root;
    }

    private static Node buildMemberAddCard() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Thêm thành viên vào nhóm (Mục 12.c)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_USERS, 0.8, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        // Thanh tìm kiếm username
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.control.TextField tfUsername = new javafx.scene.control.TextField("duc_huy");
        tfUsername.setPromptText("Nhập @username để tìm kiếm...");
        tfUsername.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: -text-normal; -fx-font-size: 12px;");
        HBox.setHgrow(tfUsername, Priority.ALWAYS);
        searchBox.getChildren().addAll(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_SEARCH, 0.8, "#8E929B"), tfUsername);
        searchBox.setStyle("-fx-background-color: -bg-input; -fx-border-color: #4E6BF2; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 4px 10px;");

        // Thẻ kết quả người dùng tìm thấy (Không có nhãn quan hệ thừa theo phản hồi người dùng)
        HBox userCard = new HBox(12);
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 12px;");

        StackPane avt = new StackPane();
        Circle c = new Circle(16, Color.web("#4E6BF2"));
        javafx.scene.control.Label lblInit = new javafx.scene.control.Label("DH");
        lblInit.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");
        avt.getChildren().addAll(c, lblInit);

        VBox meta = new VBox(2);
        javafx.scene.control.Label lblName = new javafx.scene.control.Label("Phạm Đức Huy");
        lblName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        javafx.scene.control.Label lblTag = new javafx.scene.control.Label("@duc_huy");
        lblTag.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted;");
        meta.getChildren().addAll(lblName, lblTag);
        HBox.setHgrow(meta, Priority.ALWAYS);

        javafx.scene.control.Button btnAddNow = new javafx.scene.control.Button("+ Thêm vào nhóm");
        btnAddNow.getStyleClass().addAll("btn-primary", "btn-compact");

        userCard.getChildren().addAll(avt, meta, btnAddNow);

        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Đóng");
        btnCancel.getStyleClass().add("btn-secondary");
        actionRow.getChildren().add(btnCancel);

        root.getChildren().addAll(lblHeader, searchBox, userCard, actionRow);
        return root;
    }

    private static HBox createMemberChip(String text) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setStyle("-fx-background-color: rgba(78, 107, 242, 0.15); -fx-border-color: #4E6BF2; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 3 8;");
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
        lbl.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #A5C2FF; -fx-font-weight: bold;");
        javafx.scene.control.Label lblClose = new javafx.scene.control.Label("✕");
        lblClose.setStyle("-fx-font-size: 10px; -fx-text-fill: #FA777C; -fx-cursor: hand;");
        chip.getChildren().addAll(lbl, lblClose);
        return chip;
    }

    private static HBox createSearchUserRow(String initials, String name, String tag, boolean isAdded) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 6 12;");

        StackPane avt = new StackPane();
        Circle c = new Circle(14, Color.web("#4E6BF2"));
        javafx.scene.control.Label lblI = new javafx.scene.control.Label(initials);
        lblI.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white;");
        avt.getChildren().addAll(c, lblI);

        VBox meta = new VBox(1);
        javafx.scene.control.Label lblN = new javafx.scene.control.Label(name);
        lblN.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        javafx.scene.control.Label lblT = new javafx.scene.control.Label(tag);
        lblT.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        meta.getChildren().addAll(lblN, lblT);
        HBox.setHgrow(meta, Priority.ALWAYS);

        javafx.scene.control.Button btn = new javafx.scene.control.Button(isAdded ? "✓ Đã thêm" : "+ Thêm");
        btn.getStyleClass().addAll(isAdded ? "btn-secondary" : "btn-primary", "btn-compact");
        btn.setDisable(isAdded);

        row.getChildren().addAll(avt, meta, btn);
        return row;
    }

    private static Node buildGlobalSearchCard() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Tìm kiếm tin nhắn toàn cục (Mục 11.g)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_SEARCH, 0.85, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        javafx.scene.control.TextField txtSearch = new javafx.scene.control.TextField("báo cáo");

        HBox chips = new HBox(8);
        javafx.scene.control.Button c1 = new javafx.scene.control.Button("Tất cả (4)");
        c1.getStyleClass().add("filter-chip");
        javafx.scene.control.Button c2 = new javafx.scene.control.Button("Tin nhắn (3)");
        c2.getStyleClass().addAll("filter-chip", "filter-chip-active");
        javafx.scene.control.Button c3 = new javafx.scene.control.Button("Hội thoại (1)");
        c3.getStyleClass().add("filter-chip");
        chips.getChildren().addAll(c1, c2, c3);

        VBox results = new VBox(8);
        results.getChildren().addAll(
            createSearchResult("Nhóm Đồ Án Java 2026", "Lưu Chí Hải: Đã cập nhật xong phần Admin vào báo cáo mốc 20% nhé!"),
            createSearchResult("Lưu Chí Hải", "Hải: Minh xem lại báo cáo phần User giúp mình với!"),
            createSearchResult("Nguyễn Lan Anh", "Lan Anh: File báo cáo mẫu gửi qua mail rồi nha.")
        );

        root.getChildren().addAll(lblHeader, txtSearch, chips, results);
        return root;
    }

    private static HBox createSearchResult(String chatTitle, String snippet) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px 14px;");
        VBox meta = new VBox(3);
        javafx.scene.control.Label lblT = new javafx.scene.control.Label(chatTitle);
        lblT.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -accent-primary;");
        javafx.scene.control.Label lblS = new javafx.scene.control.Label(snippet);
        lblS.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-normal;");
        meta.getChildren().addAll(lblT, lblS);
        item.getChildren().add(meta);
        return item;
    }

    private static VBox createFieldBlock(String labelText, boolean required, Node field) {
        VBox block = new VBox(4);
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(labelText + (required ? " *" : ""));
        lbl.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        block.getChildren().addAll(lbl, field);
        return block;
    }

    private static void captureSync(Stage stage, String filename, int w, int h, java.util.concurrent.Callable<Node> nodeSupplier) throws Exception {
        java.util.concurrent.CompletableFuture<Void> future = new java.util.concurrent.CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                Node node = nodeSupplier.call();
                Scene scene = new Scene((Parent) node, w, h);
                scene.setFill(Color.web("#141619"));
                scene.getStylesheets().add(UserScreenshotsRunner.class.getResource("/user/theme.css").toExternalForm());

                stage.setScene(scene);
                stage.setWidth(w);
                stage.setHeight(h);
                stage.show();

                // Chờ render xong 1 pulse
                Platform.runLater(() -> {
                    try {
                        SnapshotParameters params = new SnapshotParameters();
                        params.setFill(Color.web("#141619"));
                        WritableImage snapshot = node.snapshot(params, null);

                        int width = (int) snapshot.getWidth();
                        int height = (int) snapshot.getHeight();
                        PixelReader pr = snapshot.getPixelReader();
                        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                bi.setRGB(x, y, pr.getArgb(x, y));
                            }
                        }

                        // Lưu vào thư mục evidence của user-spec
                        File out1 = new File(DIR_USER_SPEC, filename);
                        ImageIO.write(bi, "png", out1);
                        System.out.println("Captured: " + filename + " [" + width + "x" + height + "]");
                        future.complete(null);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        future.get(15, java.util.concurrent.TimeUnit.SECONDS);
    }
}
