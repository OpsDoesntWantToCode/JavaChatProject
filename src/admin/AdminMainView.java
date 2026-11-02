package admin;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Khung Admin: quản lý user mẫu ở A2, các trang còn lại là bản xem trước. */
public final class AdminMainView extends BorderPane {
    private final Label pageTitle = new Label();
    private final Label pageDescription = new Label();
    private final StackPane content = new StackPane();
    private final AdminUserStore userStore = new AdminUserStore();
    private final Map<AdminNavigation.Page, Node> pages =
            new EnumMap<>(AdminNavigation.Page.class);

    public AdminMainView(Runnable onExit) {
        Objects.requireNonNull(onExit);
        getStyleClass().add("admin-root");
        setId("admin-root");
        setCenter(createWorkspace(onExit));
        setLeft(new AdminNavigation(this::showPage));
    }

    private BorderPane createWorkspace(Runnable onExit) {
        Label breadcrumb = new Label("QUẢN TRỊ  /  JAVA CHAT SYSTEM");
        breadcrumb.getStyleClass().add("breadcrumb");
        Label avatar = new Label("H");
        avatar.getStyleClass().add("avatar");
        Label name = new Label("Lưu Chí Hải");
        name.getStyleClass().add("identity-name");
        Label role = new Label("Quản trị viên · Tài khoản mẫu");
        role.getStyleClass().add("muted");
        HBox identity = new HBox(10, avatar, new VBox(2, name, role));
        identity.setAlignment(Pos.CENTER_LEFT);
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        Button logout = new Button("Đăng xuất");
        logout.setId("logout-button");
        logout.getStyleClass().add("secondary-button");
        logout.setOnAction(event -> confirmExit(onExit));
        HBox header = new HBox(16, breadcrumb, gap, identity, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("admin-header");

        pageTitle.setId("page-title");
        pageTitle.getStyleClass().add("page-title");
        pageTitle.setWrapText(true);
        pageDescription.getStyleClass().add("page-description");
        pageDescription.setWrapText(true);
        VBox titles = new VBox(6, pageTitle, pageDescription);
        HBox.setHgrow(titles, Priority.ALWAYS);
        ToggleButton theme = new ToggleButton("Giao diện tối");
        theme.setAccessibleHelp("Chuyển giữa giao diện sáng và tối cho không gian Admin.");
        theme.setId("theme-toggle");
        theme.getStyleClass().add("secondary-button");
        theme.setMinWidth(116);
        theme.setOnAction(event -> {
            getStyleClass().remove("theme-dark");
            if (theme.isSelected()) { getStyleClass().add("theme-dark"); }
            theme.setText(theme.isSelected() ? "Giao diện sáng" : "Giao diện tối");
        });
        HBox heading = new HBox(16, titles, theme);
        heading.setAlignment(Pos.TOP_LEFT);
        heading.getStyleClass().add("page-heading");
        content.getStyleClass().add("page-content");
        BorderPane body = new BorderPane(content);
        body.setTop(heading);
        BorderPane workspace = new BorderPane(body);
        workspace.setTop(header);
        return workspace;
    }

    private void showPage(AdminNavigation.Page page) {
        pageTitle.setText(page.title());
        pageDescription.setText(page.description());
        content.getChildren().setAll(pages.computeIfAbsent(page, selected ->
                selected == AdminNavigation.Page.USERS ? new UserManagementView(userStore) : createPreview(selected)));
    }

    private Node createPreview(AdminNavigation.Page page) {
        Label badge = new Label("BẢN XEM TRƯỚC");
        badge.getStyleClass().add("preview-badge");
        Label title = new Label("Không gian " + page.title().toLowerCase(java.util.Locale.ROOT));
        title.getStyleClass().add("preview-title");
        title.setWrapText(true);
        Label description = new Label(
                "Khu vực chức năng đang được chuẩn bị. Bạn có thể khám phá các mục bên trái.");
        description.setWrapText(true);
        description.getStyleClass().add("page-description");
        Label section = new Label("CHỨC NĂNG DỰ KIẾN");
        section.getStyleClass().add("feature-heading");
        VBox card = new VBox(16, badge, title, description, section);
        card.getStyleClass().add("preview-card");
        int index = 1;
        for (String feature : page.features()) {
            Label number = new Label("%02d".formatted(index++));
            number.getStyleClass().add("feature-number");
            Label text = new Label(feature);
            text.setWrapText(true);
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox row = new HBox(14, number, text);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("feature-row");
            card.getChildren().add(row);
        }
        Label note = new Label("Dữ liệu và thao tác quản trị sẽ được bổ sung ở các màn hình tiếp theo.");
        note.setWrapText(true);
        note.getStyleClass().add("preview-note");
        card.getChildren().add(note);
        ScrollPane scroll = new ScrollPane(card);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("content-scroll");
        return scroll;
    }

    private void confirmExit(Runnable onExit) {
        ButtonType cancel = new ButtonType("Ở lại", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType exit = new ButtonType("Đóng bản xem trước", ButtonBar.ButtonData.OK_DONE);
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "", cancel, exit);
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Đăng xuất bản xem trước");
        dialog.setHeaderText("Bạn muốn đóng cửa sổ Admin?");
        dialog.setContentText("Đây là tài khoản mẫu. Không có phiên đăng nhập thực tế cần đăng xuất.");
        dialog.getDialogPane().getStylesheets().addAll(getScene().getStylesheets());
        dialog.getDialogPane().getStyleClass().add("admin-dialog");
        if (getStyleClass().contains("theme-dark")) {
            dialog.getDialogPane().getStyleClass().add("theme-dark");
        }
        dialog.showAndWait().filter(exit::equals).ifPresent(result -> onExit.run());
    }
}
