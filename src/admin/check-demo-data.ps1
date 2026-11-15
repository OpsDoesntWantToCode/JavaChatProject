# Kiem tra du lieu mau va UI Admin da trien khai; khong ket noi DB/Server.
[CmdletBinding()]
param([string]$MavenCommand = 'mvn.cmd')

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$mavenExecutable = (Get-Command $MavenCommand -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
$javaExecutable = (Get-Command java -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source

Push-Location -LiteralPath $repositoryRoot
try
{
    & $mavenExecutable '-B' 'compile' 'org.apache.maven.plugins:maven-dependency-plugin:3.11.0:build-classpath' '-DincludeGroupIds=org.openjfx' '-Dmdep.outputFile=target/admin-classpath.txt'
    if ($LASTEXITCODE -ne 0)
    {
        throw 'Maven build failed.'
    }
    $classpath = (Get-Content -Raw -LiteralPath 'target/admin-classpath.txt').Trim()
    $modules = @($classpath.Split([IO.Path]::PathSeparator) | Where-Object {
        [IO.Path]::GetFileName($_) -match '^javafx-(base|graphics|controls)-.+-win.jar$'
    })
    if ($modules.Count -ne 3)
    {
        throw 'Expected three JavaFX Windows modules from the current POM.'
    }
    $modulePath = $modules -join [IO.Path]::PathSeparator
    $checkDirectory = New-Item -ItemType Directory -Force -Path 'target/admin-demo-checks'
    $checkSource = Join-Path $checkDirectory.FullName 'AdminDemoDataCheck.java'
    $source = @'
package admin;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class AdminDemoDataCheck
{
    private static int passed;

    private static void check(boolean valid, String message)
    {
        if (!valid)
        {
            throw new AssertionError(message);
        }
        passed++;
    }

    private static void rejected(Runnable action, String message)
    {
        try
        {
            action.run();
        }
        catch (IllegalArgumentException expected)
        {
            passed++;
            return;
        }
        throw new AssertionError(message);
    }

    private static AdminUserStore store()
    {
        return new AdminUserStore(Clock.fixed(Instant.parse("2026-11-15T03:00:00Z"),
                ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    private static AdminUserStore.UserDraft draft(String username, String name, String email)
    {
        return new AdminUserStore.UserDraft(username, name, null, null, null, email);
    }

    private static AdminDemoData.Records withFriends(AdminDemoData.Records r,
            List<AdminDemoData.Friendship> pairs)
    {
        return new AdminDemoData.Records(r.logins(), pairs, r.groups(), r.memberships(),
                r.reports(), r.activities(), r.onlineUserIds());
    }

    private static Set<Long> friendIds(AdminDemoData data, long userId)
    {
        Set<Long> result = new HashSet<>();
        data.friendsOf(userId).forEach(row -> result.add(row.id()));
        return result;
    }

    private static void dataChecks()
    {
        AdminUserStore store = store();
        AdminDemoData data = new AdminDemoData(store);
        AdminDemoData.Records r = data.records();
        check(data.userStore() == store && store.users().size() == 10, "same store and original users");
        check(r.logins().size() == 11 && r.groups().size() == 3 && r.reports().size() == 3,
                "expected source fixtures, not computed totals");
        check(data.loginHistory(2).size() == 4, "four logins for Minh");
        check(data.loginHistory(2).getFirst().occurredAt().equals(OffsetDateTime.parse("2026-11-01T23:59:59+07:00")),
                "latest login first");
        check(data.loginHistory(10).isEmpty(), "Phuc has no login history");
        check(friendIds(data, 2).equals(Set.of(1L, 3L, 4L, 7L)), "friends include locked Ngoc");
        check(friendIds(data, 3).equals(Set.of(1L, 2L, 5L)), "deleted Dung excluded");
        check(data.friendsOf(8).isEmpty() && data.friendsOf(9).isEmpty()
                && data.friendsOf(10).isEmpty(), "deleted owner and empty current relations");
        check(data.isOnline(2) && !data.isOnline(7) && !data.isOnline(8), "online is status aware");
        rejected(() -> data.user(999), "unknown user rejected");
        rejected(() -> new AdminDemoData(store, withFriends(r, List.of(new AdminDemoData.Friendship(2, 2)))),
                "self friendship rejected");
        rejected(() -> new AdminDemoData(store, withFriends(r, List.of(new AdminDemoData.Friendship(3, 2)))),
                "reversed friendship rejected");
        rejected(() -> new AdminDemoData(store, withFriends(r,
                List.of(new AdminDemoData.Friendship(1, 2), new AdminDemoData.Friendship(1, 2)))),
                "duplicate friendship rejected");
        rejected(() -> new AdminDemoData(store, withFriends(r, List.of(new AdminDemoData.Friendship(2, 999)))),
                "dangling friendship rejected");
        rejected(() -> new AdminDemoData(store, new AdminDemoData.Records(
                List.of(new AdminDemoData.LoginEvent(1, 999, OffsetDateTime.parse("2026-11-01T00:00:00+07:00"))),
                r.friendships(), r.groups(), r.memberships(), r.reports(), r.activities(), r.onlineUserIds())),
                "dangling login rejected");
        rejected(() -> new AdminDemoData(store, new AdminDemoData.Records(
                List.of(new AdminDemoData.LoginEvent(1, 2, OffsetDateTime.parse("2025-01-01T00:00:00+07:00"))),
                r.friendships(), r.groups(), r.memberships(), r.reports(), r.activities(), r.onlineUserIds())),
                "history cannot predate registration");
        rejected(() -> new AdminDemoData(store, new AdminDemoData.Records(r.logins(), r.friendships(),
                r.groups(), List.of(), r.reports(), r.activities(), r.onlineUserIds())),
                "group without creator admin rejected");
        rejected(() -> new AdminDemoData(store, new AdminDemoData.Records(r.logins(), r.friendships(),
                r.groups(), r.memberships(), r.reports(), List.of(), r.onlineUserIds())),
                "spam without preceding message rejected");
        ArrayList<AdminDemoData.Friendship> external = new ArrayList<>(r.friendships());
        AdminDemoData.Records copied = withFriends(r, external);
        external.clear();
        check(copied.friendships().size() == 9, "defensive copy of fixture collections");
        try
        {
            r.logins().clear();
            throw new AssertionError("history must be immutable");
        }
        catch (UnsupportedOperationException expected)
        {
            passed++;
        }
        UserRow edited = store.update(2, draft("minh.changed", "Minh updated", "updated@example.test"));
        check(data.user(2) == edited, "resolve latest profile by ID");
        check(data.friendsOf(1).stream().anyMatch(row -> row.id() == 2 && row.username().equals("minh.changed")),
                "relationships resolve renamed user");
        check(data.loginHistory(2).size() == 4, "renaming retains history ownership");
        store.changeStatus(2, UserRow.Status.LOCKED);
        check(!data.isOnline(2) && friendIds(data, 1).contains(2L), "locking keeps relation, removes online");
        store.changeStatus(2, UserRow.Status.DELETED);
        check(!friendIds(data, 1).contains(2L) && data.friendsOf(2).isEmpty(), "soft delete hides current friends");
        check(data.records() == r && r.friendships().size() == 9 && data.loginHistory(2).size() == 4,
                "soft delete preserves raw relationships and historical records");
        UserRow added = store.add(draft("new.demo", "New demo", "new@example.test"));
        check(data.user(added.id()) == added && data.loginHistory(added.id()).isEmpty()
                && data.friendsOf(added.id()).isEmpty() && !data.isOnline(added.id()), "new user has no invented events");
        AdminDemoData reset = new AdminDemoData(store());
        check(reset.user(2).username().equals("minh.dev") && reset.isOnline(2), "sessions reset independently");
    }

    @SuppressWarnings("unchecked")
    private static void viewChecks(boolean dark)
    {
        AdminDemoData data = new AdminDemoData(store());
        AdminMainView root = new AdminMainView(() ->
        {
        }, data);
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(Objects.requireNonNull(AdminMainView.class.getResource("/admin/admin.css")).toExternalForm());
        if (dark)
        {
            root.getStyleClass().add("theme-dark");
        }
        root.applyCss();
        root.layout();
        Node originalPage = root.lookup("#user-management");
        TableView<UserRow> table = (TableView<UserRow>) root.lookup("#users-table");
        TextField filter = (TextField) root.lookup("#filter-username");
        check(table.getItems().size() == 10, "UI uses supplied store");
        filter.setText("minh.dev");
        check(table.getItems().size() == 1 && table.getItems().getFirst().id() == 2, "initial filter");
        ((ToggleButton) root.lookup("#nav-login_history")).fire();
        data.userStore().update(2, draft("minh.changed", "Updated while hidden", "changed@example.test"));
        ((ToggleButton) root.lookup("#nav-users")).fire();
        check(root.lookup("#user-management") == originalPage, "cached page reused");
        check(filter.getText().equals("minh.dev") && table.getItems().isEmpty(), "hidden page update reapplies filter");
        filter.clear();
        check(table.getItems().stream().anyMatch(row -> row.fullName().equals("Updated while hidden")), "latest profile visible");
        table.getSelectionModel().select(data.user(2));
        data.userStore().changeStatus(2, UserRow.Status.LOCKED);
        check(table.getItems().stream().anyMatch(row -> row.id() == 2 && row.status() == UserRow.Status.LOCKED),
                "external lock reaches table");
        check(((Label) root.lookup("#user-summary")).getText().contains("2 \u0111\u00e3 kh\u00f3a"), "external lock refreshes counts");
        check(((Button) root.lookup("#user-lock")).getText().equals("M\u1edf kh\u00f3a"),
                "selected actions reflect replacement: " + ((Button) root.lookup("#user-lock")).getText()
                + "; selected=" + table.getSelectionModel().getSelectedItem());
        check(table.getSelectionModel().getSelectedItem() == data.user(2), "selection uses latest immutable record");
        UserRow added = data.userStore().add(draft("live.new", "Live new", "live@example.test"));
        check(table.getItems().size() == 11 && table.getItems().contains(added), "external add reaches cached table");
        check(table.getSelectionModel().getSelectedItem().id() == 2, "adding another user preserves selection");
        ComboBox<String> statusFilter = (ComboBox<String>) root.lookup("#filter-status");
        statusFilter.getSelectionModel().select(1);
        table.getSelectionModel().select(added);
        data.userStore().changeStatus(added.id(), UserRow.Status.DELETED);
        check(table.getSelectionModel().getSelectedItem() == null
                && ((Button) root.lookup("#user-detail")).isDisabled(), "hidden selected user clears actions");
        check(statusFilter.getSelectionModel().getSelectedIndex() == 1
                && table.getItems().stream().noneMatch(row -> row.id() == added.id()), "mutation preserves active filter");
        statusFilter.getSelectionModel().select(0);
        check(table.getItems().stream().anyMatch(row -> row.id() == added.id() && row.status() == UserRow.Status.DELETED),
                "soft-deleted profile remains inspectable");
        statusFilter.getSelectionModel().select(3);
        check(table.getItems().size() == 2, "status filter sees original and newly deleted user");
        check(root.lookup("#page-title") instanceof Label, "frame remains attached");
    }

    @SuppressWarnings("unchecked")
    private static void historyChecks(boolean dark)
    {
        AdminDemoData data = new AdminDemoData(store());
        AdminMainView root = new AdminMainView(() ->
        {
        }, data);
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(Objects.requireNonNull(AdminMainView.class.getResource("/admin/admin.css")).toExternalForm());
        if (dark)
        {
            root.getStyleClass().add("theme-dark");
        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();
        try
        {
            check(((Button) root.lookup("#user-login-history")).isDisabled()
                    && ((Button) root.lookup("#user-friends")).isDisabled(), "related actions require selection");
            TableView<UserRow> users = (TableView<UserRow>) root.lookup("#users-table");
            users.getSelectionModel().select(data.user(2));
            check(!((Button) root.lookup("#user-login-history")).isDisabled()
                    && !((Button) root.lookup("#user-friends")).isDisabled(), "related actions enabled");
            ((ToggleButton) root.lookup("#nav-login_history")).fire();
            TableView<AdminDemoData.LoginEvent> table = (TableView<AdminDemoData.LoginEvent>) root.lookup("#login-history-table");
            TextField username = (TextField) root.lookup("#login-username");
            DatePicker from = (DatePicker) root.lookup("#login-from");
            DatePicker to = (DatePicker) root.lookup("#login-to");
            Label error = (Label) root.lookup("#login-error");
            Button clear = (Button) root.lookup("#login-clear");
            check(table.getItems().size() == 11, "A4 uses all shared logins");
            check(table.getItems().getFirst().id() == 11, "latest real timestamp first");
            username.setText("  MINH.DEV  ");
            check(table.getItems().size() == 4, "normalized username filter");
            from.getEditor().setText("01/11/2026");
            to.getEditor().setText("01/11/2026");
            check(table.getItems().size() == 2, "both boundaries included for Minh");
            username.clear();
            check(table.getItems().size() == 4, "day includes other users and deleted Dung");
            from.getEditor().setText("02/11/2026");
            check(error.isVisible() && table.getItems().isEmpty(), "reversed range has no misleading rows");
            from.getEditor().setText("31/02/2026");
            from.commitValue();
            check(error.isVisible() && table.getItems().isEmpty(), "invalid typed date survives commit as an error");
            clear.fire();
            check(!error.isVisible() && table.getItems().size() == 11
                    && from.getEditor().getText().isEmpty(), "clear restores all data and removes invalid date");
            from.getEditor().setText("02/11/2026");
            check(table.getItems().size() == 3, "unbounded end");
            clear.fire();
            to.getEditor().setText("31/10/2026");
            check(table.getItems().size() == 4, "unbounded start");
            clear.fire();
            username.setText("no.match");
            check(table.getItems().isEmpty() && !error.isVisible(), "valid empty result");
            username.setText("minh.dev");
            data.userStore().update(2, draft("minh.renamed", "Minh renamed", "renamed@example.test"));
            check(table.getItems().isEmpty(), "history reapplies filter after profile rename");
            username.setText("minh.renamed");
            check(table.getItems().size() == 4
                    && table.getColumns().get(2).getCellObservableValue(0).getValue().equals("Minh renamed"),
                    "history resolves latest username and name");
            ((TableColumn<AdminDemoData.LoginEvent, OffsetDateTime>) table.getColumns().getFirst())
                    .setSortType(TableColumn.SortType.ASCENDING);
            table.sort();
            check(table.getItems().getFirst().id() == 2, "time sorts by value ascending");

            for (long userId : new long[] {2, 8, 10})
            {
                UserLoginHistoryDialog dialog = new UserLoginHistoryDialog(root, data, userId);
                dialog.show();
                TableView<?> scoped = (TableView<?>) dialog.getDialogPane().lookup("#login-history-table");
                int expected = userId == 2 ? 4 : userId == 8 ? 1 : 0;
                check(scoped.getItems().size() == expected && dialog.getHeaderText().contains(data.user(userId).username()),
                        "login dialog scoped to " + userId);
                check(dialog.getOwner() == stage
                        && dialog.getDialogPane().getStyleClass().contains("theme-dark") == dark, "dialog owner and theme");
                dialog.close();
                check(dialog.getResult() == null, "closing read-only login dialog returns no wrong result type");
            }
            UserFriendsDialog friends = new UserFriendsDialog(root, data, 2);
            friends.show();
            TableView<UserRow> friendsTable = (TableView<UserRow>) friends.getDialogPane().lookup("#user-friends-table");
            check(friendsTable.getItems().size() == 4
                    && friendsTable.getItems().stream().anyMatch(row -> row.id() == 7), "locked friend kept");
            data.userStore().changeStatus(3, UserRow.Status.DELETED);
            check(friendsTable.getItems().size() == 3
                    && friendsTable.getItems().stream().noneMatch(row -> row.id() == 3), "open friends dialog follows soft delete");
            friends.close();
            for (long userId : new long[] {8, 9, 10})
            {
                UserFriendsDialog empty = new UserFriendsDialog(root, data, userId);
                empty.show();
                TableView<?> emptyTable = (TableView<?>) empty.getDialogPane().lookup("#user-friends-table");
                check(emptyTable.getItems().isEmpty() && empty.getHeaderText().contains(data.user(userId).username()),
                        "empty or deleted friends owner " + userId);
                empty.close();
            }
            check(data.loginHistory(3).size() == 2, "friend deletion keeps historical logins");
        }
        finally
        {
            stage.close();
        }
    }

    @SuppressWarnings("unchecked")
    private static void groupChecks(boolean dark)
    {
        AdminDemoData data = new AdminDemoData(store());
        AdminMainView root = new AdminMainView(() ->
        {
        }, data);
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(Objects.requireNonNull(AdminMainView.class.getResource("/admin/admin.css")).toExternalForm());
        if (dark)
        {
            root.getStyleClass().add("theme-dark");
        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();
        try
        {
            ((ToggleButton) root.lookup("#nav-groups")).fire();
            TableView<AdminDemoData.ChatGroup> table = (TableView<AdminDemoData.ChatGroup>) root.lookup("#groups-table");
            TextField filter = (TextField) root.lookup("#group-name-filter");
            Button detail = (Button) root.lookup("#group-detail");
            Button clear = (Button) root.lookup("#group-clear");
            Node cached = root.lookup("#group-management");
            check(table.getItems().size() == 3 && table.getItems().getFirst().id() == 3, "groups default latest first");
            check(detail.isDisabled(), "group detail requires selection");
            table.getSelectionModel().selectFirst();
            check(!detail.isDisabled(), "group detail available for selected row");
            filter.setText("  NH\u00d3M H\u1eccC T\u1eacP  ");
            check(table.getItems().size() == 1 && table.getItems().getFirst().id() == 2, "group filter case and whitespace");
            check(detail.isDisabled(), "changing filter clears stale selected group");
            check(table.getColumns().get(3).getCellObservableValue(0).getValue().equals(Boolean.TRUE), "encrypted flag is data only");
            ((ToggleButton) root.lookup("#nav-users")).fire();
            data.userStore().update(2, draft("minh.group", "Minh for groups", "minh.group@example.test"));
            ((ToggleButton) root.lookup("#nav-groups")).fire();
            check(root.lookup("#group-management") == cached && table.getItems().getFirst().id() == 2,
                    "group page and filter survive navigation");
            filter.setText("no.group.matches");
            check(table.getItems().isEmpty() && detail.isDisabled(), "group empty result disables details");
            clear.fire();
            check(table.getItems().size() == 3 && filter.getText().isEmpty(), "clear restores group list");
            TableColumn<AdminDemoData.ChatGroup, String> name = (TableColumn<AdminDemoData.ChatGroup, String>) table.getColumns().get(0);
            table.getSortOrder().setAll(List.of(name));
            name.setSortType(TableColumn.SortType.ASCENDING);
            table.sort();
            check(table.getItems().getFirst().id() == 1 && table.getItems().getLast().id() == 3, "group name ascending Vietnamese order");
            name.setSortType(TableColumn.SortType.DESCENDING);
            table.sort();
            check(table.getItems().getFirst().id() == 3, "group name descending");
            TableColumn<AdminDemoData.ChatGroup, OffsetDateTime> date = (TableColumn<AdminDemoData.ChatGroup, OffsetDateTime>) table.getColumns().get(2);
            date.setSortType(TableColumn.SortType.ASCENDING);
            table.getSortOrder().setAll(List.of(date));
            table.sort();
            check(table.getItems().getFirst().id() == 1, "group creation date ascending by value");
            date.setSortType(TableColumn.SortType.DESCENDING);
            table.sort();
            check(table.getItems().getFirst().id() == 3
                    && table.getColumns().get(1).getCellObservableValue(0).getValue().equals("minh.group"),
                    "group creator resolves latest profile");
            rejected(() -> new GroupDetailDialog(root, data, 999), "unknown group rejected");

            for (long groupId : new long[] {1, 2, 3})
            {
                GroupDetailDialog dialog = new GroupDetailDialog(root, data, groupId);
                dialog.show();
                TableView<UserRow> members = (TableView<UserRow>) dialog.getDialogPane().lookup("#group-members-table");
                TableView<UserRow> admins = (TableView<UserRow>) dialog.getDialogPane().lookup("#group-admins-table");
                Set<Long> expectedMembers = groupId == 1 ? Set.of(1L, 2L, 3L, 7L)
                        : groupId == 2 ? Set.of(3L, 5L, 8L, 9L) : Set.of(2L, 4L, 6L);
                Set<Long> expectedAdmins = groupId == 1 ? Set.of(1L, 2L) : groupId == 2 ? Set.of(3L, 9L) : Set.of(2L);
                Set<Long> actualMembers = new HashSet<>();
                members.getItems().forEach(row -> actualMembers.add(row.id()));
                Set<Long> actualAdmins = new HashSet<>();
                admins.getItems().forEach(row -> actualAdmins.add(row.id()));
                check(actualMembers.equals(expectedMembers), "members scoped to group " + groupId);
                check(actualAdmins.equals(expectedAdmins), "admins scoped to group " + groupId);
                check(dialog.getOwner() == stage && dialog.getDialogPane().getStyleClass().contains("theme-dark") == dark,
                        "group dialog owner and theme");
                if (groupId == 1)
                {
                    check(admins.getItems().stream().anyMatch(row -> row.id() == 2 && row.role() == UserRow.Role.USER),
                            "group admin differs from system admin");
                    data.userStore().update(3, draft("lan.group", "Lan in group", "lan.group@example.test"));
                    check(members.getItems().stream().anyMatch(row -> row.id() == 3 && row.fullName().equals("Lan in group")),
                            "open members table follows profile edits");
                }
                if (groupId == 2)
                {
                    check(members.getItems().stream().anyMatch(row -> row.id() == 8 && row.status() == UserRow.Status.DELETED),
                            "membership fixture retains deleted profile with explicit status");
                    data.userStore().changeStatus(3, UserRow.Status.LOCKED);
                    check(admins.getItems().stream().anyMatch(row -> row.id() == 3 && row.status() == UserRow.Status.LOCKED),
                            "open admins table follows account status");
                }
                dialog.close();
                check(dialog.getResult() == null, "read-only group dialog closes without result conversion error");
            }
            AdminDemoData.Records r = data.records();
            check(r.groups().size() == 3 && r.memberships().size() == 11, "group inspection does not mutate memberships");
            AdminDemoData emptyData = new AdminDemoData(store(), new AdminDemoData.Records(
                    r.logins(), r.friendships(), List.of(), List.of(), r.reports(),
                    r.activities().stream().filter(event -> event.kind() != AdminDemoData.ActivityKind.GROUP_MESSAGE).toList(),
                    r.onlineUserIds()));
            GroupManagementView empty = new GroupManagementView(emptyData);
            check(((TableView<?>) empty.lookup("#groups-table")).getItems().isEmpty()
                    && ((Button) empty.lookup("#group-detail")).isDisabled(), "empty group dataset");
            empty.close();
            ((GroupManagementView) cached).close();
        }
        finally
        {
            stage.close();
        }
    }

    private static void respondToLock(Button lock, Stage owner, boolean confirm, Runnable beforeAnswer)
    {
        check(!lock.isDisabled(), "confirmation test requires an eligible target");
        AtomicReference<Throwable> failure = new AtomicReference<>();
        boolean[] answered = {false};
        Platform.runLater(() ->
        {
            DialogPane pane = null;
            try
            {
                List<Window> dialogs = Window.getWindows().stream().filter(window -> window != owner && window.isShowing()
                        && window instanceof Stage stage && stage.getOwner() == owner).toList();
                check(dialogs.size() == 1, "exactly one confirmation owned by Admin");
                pane = (DialogPane) dialogs.getFirst().getScene().lookup(".dialog-pane");
                check(pane != null && pane.getContentText().contains("an.report"), "confirmation names reported user");
                boolean dark = owner.getScene().getRoot().getStyleClass().contains("theme-dark");
                check(pane.getStyleClass().contains("theme-dark") == dark, "confirmation theme");
                ButtonType cancel = pane.getButtonTypes().stream()
                        .filter(type -> type.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE).findFirst().orElseThrow();
                ButtonType apply = pane.getButtonTypes().stream()
                        .filter(type -> type.getButtonData() == ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();
                check(((Button) pane.lookupButton(cancel)).isDefaultButton()
                        && !((Button) pane.lookupButton(apply)).isDefaultButton(), "cancel is safe default");
                beforeAnswer.run();
                answered[0] = true;
                ((Button) pane.lookupButton(confirm ? apply : cancel)).fire();
            }
            catch (Throwable error)
            {
                failure.set(error);
                if (pane != null)
                {
                    pane.getScene().getWindow().hide();
                }
                else
                {
                    List.copyOf(Window.getWindows()).stream().filter(window -> window instanceof Stage stage
                            && stage.getOwner() == owner).forEach(Window::hide);
                }
            }
        });
        lock.fire();
        if (failure.get() != null)
        {
            throw new AssertionError("Confirmation response failed", failure.get());
        }
        check(answered[0], "confirmation was answered");
    }

    private static void selectReport(TableView<AdminDemoData.SpamReport> table, long id)
    {
        table.getSelectionModel().select(table.getItems().stream().filter(report -> report.id() == id)
                .findFirst().orElseThrow());
    }

    @SuppressWarnings("unchecked")
    private static void spamChecks(boolean dark)
    {
        AdminUserStore store = store();
        AdminDemoData.Records r = new AdminDemoData(store).records();
        List<AdminDemoData.SpamReport> reports = new ArrayList<>(r.reports());
        reports.add(new AdminDemoData.SpamReport(99, 2, 1, "Protected account test",
                OffsetDateTime.parse("2026-11-03T11:00:00+07:00")));
        reports.add(new AdminDemoData.SpamReport(100, 3, 4, "Vietnam date boundary test",
                OffsetDateTime.parse("2026-11-03T17:00:00Z")));
        List<AdminDemoData.ActivityEvent> activities = new ArrayList<>(r.activities());
        activities.add(new AdminDemoData.ActivityEvent(99, 1, AdminDemoData.ActivityKind.DIRECT_MESSAGE,
                2L, OffsetDateTime.parse("2026-11-03T10:59:00+07:00")));
        AdminDemoData data = new AdminDemoData(store, new AdminDemoData.Records(r.logins(), r.friendships(),
                r.groups(), r.memberships(), reports, activities, r.onlineUserIds()));
        AdminMainView root = new AdminMainView(() ->
        {
        }, data);
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(Objects.requireNonNull(AdminMainView.class.getResource("/admin/admin.css")).toExternalForm());
        if (dark)
        {
            root.getStyleClass().add("theme-dark");
        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        root.applyCss();
        root.layout();
        try
        {
            ((ToggleButton) root.lookup("#nav-spam_reports")).fire();
            Node cached = root.lookup("#spam-reports");
            TableView<AdminDemoData.SpamReport> table = (TableView<AdminDemoData.SpamReport>) root.lookup("#spam-reports-table");
            TextField username = (TextField) root.lookup("#spam-username");
            DatePicker from = (DatePicker) root.lookup("#spam-from");
            DatePicker to = (DatePicker) root.lookup("#spam-to");
            Button clear = (Button) root.lookup("#spam-clear");
            Button lock = (Button) root.lookup("#spam-lock");
            Label error = (Label) root.lookup("#spam-error");
            Label hint = (Label) root.lookup("#spam-selection-hint");
            check(table.getItems().size() == 5 && table.getItems().getFirst().id() == 100, "spam defaults latest first");
            check(lock.isDisabled(), "spam lock requires selected report");
            username.setText("  AN.TRAN  ");
            check(table.getItems().size() == 2, "filter matches reported username, case insensitive");
            from.getEditor().setText("03/11/2026");
            to.getEditor().setText("03/11/2026");
            check(table.getItems().size() == 1 && table.getItems().getFirst().id() == 1, "inclusive single-day reported filter");
            from.getEditor().setText("04/11/2026");
            check(error.isVisible() && table.getItems().isEmpty() && lock.isDisabled(), "reversed dates disable locking");
            to.getEditor().setText("04/11/2026");
            check(!error.isVisible() && table.getItems().size() == 1 && table.getItems().getFirst().id() == 100,
                    "report in UTC is filtered by Vietnam local date");
            from.getEditor().setText("31/02/2026");
            from.commitValue();
            check(error.isVisible() && table.getItems().isEmpty(), "invalid spam date remains an error after commit");
            clear.fire();
            check(!error.isVisible() && table.getItems().size() == 5, "clear removes invalid spam filter");
            from.getEditor().setText("04/11/2026");
            check(table.getItems().size() == 1, "spam unbounded end");
            clear.fire();
            to.getEditor().setText("03/11/2026");
            check(table.getItems().size() == 4, "spam unbounded start");
            clear.fire();
            username.setText("lan.nguyen");
            check(table.getItems().isEmpty(), "reporter username does not match reported filter");
            clear.fire();
            TableColumn<AdminDemoData.SpamReport, OffsetDateTime> time = (TableColumn<AdminDemoData.SpamReport, OffsetDateTime>) table.getColumns().getFirst();
            time.setSortType(TableColumn.SortType.ASCENDING);
            table.sort();
            check(table.getItems().getFirst().id() == 1, "spam timestamp ascending");
            TableColumn<AdminDemoData.SpamReport, String> target = (TableColumn<AdminDemoData.SpamReport, String>) table.getColumns().get(2);
            target.setSortType(TableColumn.SortType.ASCENDING);
            table.getSortOrder().setAll(List.of(target));
            table.sort();
            check(table.getItems().getFirst().reportedId() == 4, "reported username sort");
            selectReport(table, 2);
            check(lock.isDisabled() && hint.getText().contains(UserRow.Status.LOCKED.label()), "locked target explained");
            selectReport(table, 3);
            check(lock.isDisabled() && hint.getText().contains(UserRow.Status.DELETED.label()), "deleted target explained");
            selectReport(table, 99);
            check(lock.isDisabled() && hint.getText().contains("Admin"), "last active system admin protected");
            lock.fire();
            check(data.user(1).status() == UserRow.Status.ACTIVE, "disabled admin lock cannot mutate store");
            username.setText("an.tran");
            store.update(4, draft("an.report", "Reported An", "an.report@example.test"));
            check(table.getItems().isEmpty(), "rename reapplies reported username filter");
            username.setText("an.report");
            check(table.getItems().size() == 2 && target.getCellObservableValue(0).getValue().equals("an.report"),
                    "renamed target resolves by ID");
            ((ToggleButton) root.lookup("#nav-users")).fire();
            ((ToggleButton) root.lookup("#nav-spam_reports")).fire();
            check(root.lookup("#spam-reports") == cached && username.getText().equals("an.report"), "spam page preserves filter");
            selectReport(table, 1);
            check(!lock.isDisabled() && hint.getText().contains("an.report"), "active reported user eligible");
            root.applyCss();
            root.layout();
            check(hint.getHeight() + 0.1 >= hint.prefHeight(hint.getWidth()), "selected report explanation is not clipped");
            int[] mutations = {0};
            store.users().addListener((javafx.collections.ListChangeListener<UserRow>) change -> mutations[0]++);
            respondToLock(lock, stage, false, () ->
            {
            });
            check(mutations[0] == 0 && data.user(4).status() == UserRow.Status.ACTIVE, "cancel does not mutate target");
            respondToLock(lock, stage, true, () -> store.changeStatus(4, UserRow.Status.LOCKED));
            check(mutations[0] == 1 && lock.isDisabled(), "state rechecked after confirmation, no redundant update");
            store.changeStatus(4, UserRow.Status.ACTIVE);
            mutations[0] = 0;
            respondToLock(lock, stage, true, () ->
            {
            });
            check(mutations[0] == 1 && data.user(4).status() == UserRow.Status.LOCKED, "confirm locks reported user once");
            check(data.user(3).status() == UserRow.Status.ACTIVE, "reporter is unchanged");
            check(table.getItems().size() == 2 && data.records().reports().size() == 5, "locking preserves report evidence");
            check(lock.isDisabled() && table.getSelectionModel().getSelectedItem().id() == 1, "selection follows report, repeat locking disabled");
            ((ToggleButton) root.lookup("#nav-users")).fire();
            ((TextField) root.lookup("#filter-username")).setText("an.report");
            TableView<UserRow> users = (TableView<UserRow>) root.lookup("#users-table");
            check(users.getItems().size() == 1 && users.getItems().getFirst().status() == UserRow.Status.LOCKED,
                    "cached A2 shows lock from spam view");
            ((SpamReportView) cached).close();
        }
        finally
        {
            stage.close();
        }
    }

    public static void main(String[] args) throws Exception
    {
        dataChecks();
        int dataCount = passed;
        System.out.println("PASS: " + dataCount + " demo data checks.");
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() ->
        {
            try
            {
                viewChecks(false);
                viewChecks(true);
                System.out.println("PASS: " + (passed - dataCount) + " T03 JavaFX node integration checks.");
                int beforeHistory = passed;
                historyChecks(false);
                historyChecks(true);
                System.out.println("PASS: " + (passed - beforeHistory) + " A3/A4 JavaFX checks across light/dark.");
                int beforeGroups = passed;
                groupChecks(false);
                groupChecks(true);
                System.out.println("PASS: " + (passed - beforeGroups) + " A5 group JavaFX checks across light/dark.");
                int beforeSpam = passed;
                spamChecks(false);
                spamChecks(true);
                System.out.println("PASS: " + (passed - beforeSpam) + " A6 spam/confirmation JavaFX checks across light/dark.");
            }
            catch (Throwable error)
            {
                failure.set(error);
            }
            finally
            {
                finished.countDown();
            }
        });
        boolean done = finished.await(60, TimeUnit.SECONDS);
        Platform.exit();
        if (!done)
        {
            throw new AssertionError("JavaFX checks timed out");
        }
        if (failure.get() != null)
        {
            throw new AssertionError("JavaFX integration failed", failure.get());
        }
        System.out.println("PASS: " + (passed - dataCount) + " total JavaFX checks; visual review, DB and Server are separate.");
    }
}
'@
    [IO.File]::WriteAllText($checkSource, $source, [Text.UTF8Encoding]::new($false))
    $javacExecutable = Join-Path (Split-Path $javaExecutable -Parent) 'javac.exe'
    & $javacExecutable '--module-path' $modulePath '--add-modules' 'javafx.controls' '-cp' 'target/classes' '-d' $checkDirectory.FullName $checkSource
    if ($LASTEXITCODE -ne 0)
    {
        throw 'Demo checks compilation failed.'
    }
    & $javaExecutable '--module-path' $modulePath '--add-modules' 'javafx.controls' '-cp' ("target/classes" + [IO.Path]::PathSeparator + $checkDirectory.FullName) 'admin.AdminDemoDataCheck'
    if ($LASTEXITCODE -ne 0)
    {
        throw 'Demo data or JavaFX integration checks failed.'
    }
}
finally
{
    Pop-Location
}
