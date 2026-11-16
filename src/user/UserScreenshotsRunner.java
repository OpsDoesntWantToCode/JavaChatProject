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
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Tiện ích chụp ảnh tự động toàn bộ 16 màn hình phân hệ User phục vụ Báo cáo Đồ án và Minh chứng mốc 20%.
 */
public class UserScreenshotsRunner extends Application {

    private static final File DIR_USER_SPEC = new File("docs/user-spec/evidence");
    private static final File DIR_UI_SPEC = new File("docs/ui-spec/evidence");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DIR_USER_SPEC.mkdirs();
        DIR_UI_SPEC.mkdirs();

        new Thread(() -> {
            try {
                // Cho JavaFX khởi tạo hoàn chỉnh
                Thread.sleep(600);

                // 1. Màn hình Đăng nhập (Compact Card & Hero Section Split)
                captureSync(primaryStage, "u1-login.png", 1000, 680, () -> {
                    BorderPane root = new BorderPane();
                    root.setStyle("-fx-background-color: -bg-canvas;");
                    UserLoginView loginView = new UserLoginView(() -> {}, () -> {});
                    VBox heroSection = UserHeroSection.createHeroSection();
                    
                    HBox split = new HBox(0);
                    split.setAlignment(Pos.CENTER);
                    split.getChildren().addAll(heroSection, loginView.getView());
                    HBox.setHgrow(heroSection, Priority.ALWAYS);
                    HBox.setHgrow(loginView.getView(), Priority.ALWAYS);
                    root.setCenter(split);
                    return root;
                });

                // 2. Màn hình Đăng ký tài khoản (Full 2-column form & password strength meter)
                captureSync(primaryStage, "u1-register.png", 1040, 720, () -> {
                    StackPane root = new StackPane();
                    root.setStyle("-fx-background-color: -bg-canvas;");
                    UserRegisterView regView = new UserRegisterView(() -> {});
                    root.getChildren().add(regView.getView());
                    return root;
                });

                // 3. Màn hình Quên mật khẩu
                captureSync(primaryStage, "u2-forgot-password.png", 640, 500, () -> {
                    StackPane root = new StackPane();
                    root.setStyle("-fx-background-color: -bg-canvas;");
                    UserForgotPasswordView forgotView = new UserForgotPasswordView(() -> {});
                    root.getChildren().add(forgotView.getView());
                    return root;
                });

                // 4. Màn hình Không gian làm việc chính — Hội thoại nhóm (UserMainWorkspace Group Chat)
                captureSync(primaryStage, "u3-workspace-group.png", 1120, 740, () -> {
                    UserMainWorkspace ws = new UserMainWorkspace(primaryStage, () -> {});
                    return ws;
                });

                // 5. Màn hình Không gian làm việc chính — Chat 1-1 & Drawer hồ sơ (UserMainWorkspace Direct Chat)
                captureSync(primaryStage, "u3-workspace-direct.png", 1120, 740, () -> {
                    UserMainWorkspace ws = new UserMainWorkspace(primaryStage, () -> {});
                    try {
                        Method m = UserMainWorkspace.class.getDeclaredMethod("loadConversation", String.class);
                        m.setAccessible(true);
                        m.invoke(ws, "user_chihai");
                    } catch (Exception ignored) {}
                    return ws;
                });

                // 6. Quản lý bạn bè — Tab 1: Đang Online (FriendsManagementView Online)
                captureSync(primaryStage, "u6-friends-online.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("online");
                    return wrapFriendsView(fv);
                });

                // 7. Quản lý bạn bè — Tab 2: Tất cả bạn bè (FriendsManagementView All Friends)
                captureSync(primaryStage, "u4-friends-all.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("all");
                    return wrapFriendsView(fv);
                });

                // 8. Quản lý bạn bè — Tab 3: Lời mời kết bạn đang chờ (FriendsManagementView Pending)
                captureSync(primaryStage, "u5-friends-pending.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("pending");
                    return wrapFriendsView(fv);
                });

                // 9. Quản lý bạn bè — Tab 4: Thêm bạn mới & Tìm kiếm người lạ (FriendsManagementView Search/Add)
                captureSync(primaryStage, "u7-friends-search.png", 1040, 700, () -> {
                    FriendsManagementView fv = new FriendsManagementView(primaryStage, id -> {}, id -> {});
                    fv.switchTab("add");
                    return wrapFriendsView(fv);
                });

                // 10. Khung hội thoại & bong bóng chat chi tiết (ChatConversationView)
                captureSync(primaryStage, "u8-direct-chat-details.png", 880, 680, () -> {
                    ChatConversationView cv = new ChatConversationView(primaryStage, () -> {}, () -> {}, () -> {});
                    cv.loadConversation("user_chihai");
                    return cv;
                });

                // 11. Xóa tin nhắn linh hoạt & Multi-Select Floating Bar (Task U10)
                captureSync(primaryStage, "u10-message-multiselect.png", 880, 680, () -> {
                    ChatConversationView cv = new ChatConversationView(primaryStage, () -> {}, () -> {}, () -> {});
                    cv.loadConversation("user_chihai");
                    cv.setMultiSelectMode(true);
                    if (cv.getMessagesList().size() >= 2) {
                        cv.getMessagesList().get(0).setSelected(true);
                        cv.getMessagesList().get(1).setSelected(true);
                    }
                    return cv;
                });

                // 12. Trợ lý AI gợi ý tin nhắn & Smart Reply Chips (Task U11)
                captureSync(primaryStage, "u11-smart-replies.png", 880, 680, () -> {
                    ChatConversationView cv = new ChatConversationView(primaryStage, () -> {}, () -> {}, () -> {});
                    cv.loadConversation("user_chihai");
                    cv.setInputMessage("/ai Tóm tắt các nội dung thống nhất về báo cáo 20% giúp mình");
                    cv.toggleAiSmartReplies();
                    return cv;
                });

                // 11. Hộp thoại Báo cáo Spam (SpamReportDialog)
                captureSync(primaryStage, "u13-spam-report.png", 540, 540, () -> {
                    return buildSpamReportCard();
                });

                // 12. Hộp thoại Tạo nhóm chat mới (GroupCreateDialog)
                captureSync(primaryStage, "u12-group-create.png", 560, 600, () -> {
                    return buildGroupCreateCard();
                });

                // 13. Hộp thoại Thêm thành viên vào nhóm (MemberAddDialog)
                captureSync(primaryStage, "u12-member-add.png", 460, 260, () -> {
                    return buildMemberAddCard();
                });

                // 14. Hộp thoại Tìm kiếm tin nhắn toàn cục (GlobalSearchDialog)
                captureSync(primaryStage, "u9-global-search.png", 680, 600, () -> {
                    return buildGlobalSearchCard();
                });

                // 15. Hộp thoại Cài đặt tài khoản & Đổi mật khẩu (UserSettingsDialog Tab 1)
                captureSync(primaryStage, "u2-profile-settings.png", 620, 620, () -> {
                    return buildUserSettingsCard(true);
                });

                // 16. Hộp thoại Cài đặt tài khoản & Đổi mật khẩu (UserSettingsDialog Tab 2)
                captureSync(primaryStage, "u2-security-password.png", 620, 620, () -> {
                    return buildUserSettingsCard(false);
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
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Tạo nhóm trò chuyện mới (Mục 12)");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_USERS, 0.85, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        VBox fieldGroup = new VBox(6);
        javafx.scene.control.Label lblFieldName = new javafx.scene.control.Label("Tên nhóm chat");
        lblFieldName.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        javafx.scene.control.TextField txtGroupName = new javafx.scene.control.TextField("Nhóm Đồ Án Chuyên Đề 2026");
        fieldGroup.getChildren().addAll(lblFieldName, txtGroupName);

        javafx.scene.control.CheckBox chkEncrypt = new javafx.scene.control.CheckBox("Bật mã hóa đối xứng AES-GCM (Bảo mật cao)");
        chkEncrypt.setSelected(true);

        javafx.scene.control.Label lblMembers = new javafx.scene.control.Label("CHỌN THÀNH VIÊN VÀO NHÓM (2/4 ĐÃ CHỌN)");
        lblMembers.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -text-muted;");

        VBox memberList = new VBox(8);
        memberList.getChildren().addAll(
            createMemberRow("Lưu Chí Hải", "@chihai_2026", true),
            createMemberRow("Nguyễn Lan Anh", "@lananh_it", true),
            createMemberRow("Lê Hoàng Nam", "@hoangnam_coder", false)
        );

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Hủy");
        btnCancel.getStyleClass().add("btn-secondary");
        javafx.scene.control.Button btnCreate = new javafx.scene.control.Button("Tạo nhóm ngay (2 thành viên)");
        btnCreate.getStyleClass().add("btn-primary");
        actionBox.getChildren().addAll(btnCancel, btnCreate);

        root.getChildren().addAll(lblHeader, fieldGroup, chkEncrypt, lblMembers, memberList, actionBox);
        return root;
    }

    private static HBox createMemberRow(String name, String username, boolean selected) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: -bg-input; -fx-border-color: -border-subtle; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px 12px;");
        javafx.scene.control.CheckBox cb = new javafx.scene.control.CheckBox();
        cb.setSelected(selected);
        VBox meta = new VBox(2);
        javafx.scene.control.Label lblN = new javafx.scene.control.Label(name);
        lblN.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");
        javafx.scene.control.Label lblU = new javafx.scene.control.Label(username);
        lblU.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-muted;");
        meta.getChildren().addAll(lblN, lblU);
        row.getChildren().addAll(cb, meta);
        return row;
    }

    private static Node buildMemberAddCard() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Thêm thành viên vào nhóm chat");
        lblHeader.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_USER_PLUS, 0.85, "#4E6BF2"));
        lblHeader.setGraphicTextGap(8);
        lblHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        VBox fieldBox = new VBox(6);
        javafx.scene.control.Label lblInput = new javafx.scene.control.Label("Nhập @username thành viên");
        lblInput.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
        javafx.scene.control.TextField txtUsername = new javafx.scene.control.TextField("@hoangnam_coder");
        fieldBox.getChildren().addAll(lblInput, txtUsername);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnCancel = new javafx.scene.control.Button("Hủy");
        btnCancel.getStyleClass().add("btn-secondary");
        javafx.scene.control.Button btnAdd = new javafx.scene.control.Button("Thêm vào nhóm");
        btnAdd.getStyleClass().add("btn-primary");
        actionBox.getChildren().addAll(btnCancel, btnAdd);

        root.getChildren().addAll(lblHeader, fieldBox, actionBox);
        return root;
    }

    private static Node buildGlobalSearchCard() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(22));
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

    private static Node buildUserSettingsCard(boolean isProfileTab) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #1A1C23; " +
            "-fx-border-color: #2A2D35; " +
            "-fx-border-radius: 14px; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-width: 1px;"
        );

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("Cài đặt tài khoản & Hồ sơ cá nhân (Mục 2 & 4)");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-normal;");

        HBox tabs = new HBox(8);
        tabs.getStyleClass().add("settings-tab-bar");
        javafx.scene.control.Button btnTab1 = new javafx.scene.control.Button("Hồ sơ cá nhân");
        btnTab1.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_USER_PLUS, 0.8, "#8E929B"));
        btnTab1.setGraphicTextGap(6);
        btnTab1.getStyleClass().add("settings-tab-btn");

        javafx.scene.control.Button btnTab2 = new javafx.scene.control.Button("Đổi mật khẩu & Bảo mật");
        btnTab2.setGraphic(JavaFXTheme.createSvgIcon(JavaFXTheme.SVG_LOCK, 0.8, "#8E929B"));
        btnTab2.setGraphicTextGap(6);
        btnTab2.getStyleClass().add("settings-tab-btn");

        if (isProfileTab) {
            btnTab1.getStyleClass().add("settings-tab-btn-active");
        } else {
            btnTab2.getStyleClass().add("settings-tab-btn-active");
        }
        tabs.getChildren().addAll(btnTab1, btnTab2);

        VBox form = new VBox(10);
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnClose = new javafx.scene.control.Button("Đóng");
        btnClose.getStyleClass().add("btn-secondary");

        if (isProfileTab) {
            javafx.scene.control.Label l1 = new javafx.scene.control.Label("Họ và tên");
            l1.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
            javafx.scene.control.TextField tf1 = new javafx.scene.control.TextField("Trần Nguyễn Tuấn Minh");

            javafx.scene.control.Label l2 = new javafx.scene.control.Label("Email tài khoản (Cố định)");
            l2.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
            javafx.scene.control.TextField tf2 = new javafx.scene.control.TextField("tuanminh@example.com");
            tf2.setDisable(true);
            tf2.setStyle("-fx-opacity: 0.75;");

            javafx.scene.control.Label l3 = new javafx.scene.control.Label("Địa chỉ cư trú");
            l3.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
            javafx.scene.control.TextField tf3 = new javafx.scene.control.TextField("227 Nguyễn Văn Cừ, Quận 5, TP.HCM");

            javafx.scene.control.Button btnSave = new javafx.scene.control.Button("Lưu thay đổi hồ sơ");
            btnSave.getStyleClass().add("btn-primary");
            actionRow.getChildren().addAll(btnClose, btnSave);

            form.getChildren().addAll(l1, tf1, l2, tf2, l3, tf3, actionRow);
        } else {
            javafx.scene.control.Label l1 = new javafx.scene.control.Label("Mật khẩu hiện tại");
            l1.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
            javafx.scene.control.PasswordField pf1 = new javafx.scene.control.PasswordField();
            pf1.setText("SecretPass123");

            javafx.scene.control.Label l2 = new javafx.scene.control.Label("Mật khẩu mới (Tối thiểu 6 ký tự)");
            l2.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
            javafx.scene.control.PasswordField pf2 = new javafx.scene.control.PasswordField();
            pf2.setText("NewSecurePass@2026");

            javafx.scene.control.Label l3 = new javafx.scene.control.Label("Xác nhận mật khẩu mới");
            l3.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-muted; -fx-font-weight: bold;");
            javafx.scene.control.PasswordField pf3 = new javafx.scene.control.PasswordField();
            pf3.setText("NewSecurePass@2026");

            javafx.scene.control.Button btnSave = new javafx.scene.control.Button("Cập nhật mật khẩu");
            btnSave.getStyleClass().add("btn-primary");
            actionRow.getChildren().addAll(btnClose, btnSave);

            form.getChildren().addAll(l1, pf1, l2, pf2, l3, pf3, actionRow);
        }

        root.getChildren().addAll(lblHeader, tabs, form);
        return root;
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

                        // Lưu vào cả hai thư mục
                        File out1 = new File(DIR_USER_SPEC, filename);
                        File out2 = new File(DIR_UI_SPEC, filename);
                        ImageIO.write(bi, "png", out1);
                        ImageIO.write(bi, "png", out2);
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
