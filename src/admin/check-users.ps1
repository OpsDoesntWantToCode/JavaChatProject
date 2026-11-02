# Kiem tra store A2 trong bo nho; khong ket noi DB, khong mo giao dien.
[CmdletBinding()]
param([string]$MavenCommand = 'mvn.cmd')
$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$mavenExecutable = (Get-Command $MavenCommand -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
$javaExecutable = (Get-Command java -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
Push-Location -LiteralPath $repositoryRoot
try {
    & $mavenExecutable '-B' 'compile' 'org.apache.maven.plugins:maven-dependency-plugin:3.11.0:build-classpath' '-DincludeGroupIds=org.openjfx' '-Dmdep.outputFile=target/admin-classpath.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }
    $baseJar = ((Get-Content -Raw -LiteralPath 'target/admin-classpath.txt').Trim().Split([IO.Path]::PathSeparator) |
        Where-Object { [IO.Path]::GetFileName($_) -match '^javafx-base-.+-win.jar$' })
    if (@($baseJar).Count -ne 1) { throw 'Expected one JavaFX base Windows jar.' }
    $checkDirectory = New-Item -ItemType Directory -Force -Path 'target/admin-checks'
    $checkSource = Join-Path $checkDirectory.FullName 'AdminUserStoreCheck.java'
    $source = @'
package admin;
import java.time.*;
public class AdminUserStoreCheck {
    static int passed;
    static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        passed++;
    }
    static void rejected(Runnable action, String label) {
        try { action.run(); } catch (IllegalArgumentException expected) { passed++; return; }
        throw new AssertionError(label);
    }
    static AdminUserStore.UserDraft draft(String username, String name, String email) {
        return new AdminUserStore.UserDraft(username, name, null, null, null, email);
    }
    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        AdminUserStore store = new AdminUserStore(clock);
        check(store.users().size() == 10, "fixture count");
        check(store.users().stream().filter(u -> u.status() == UserRow.Status.ACTIVE).count() == 8, "active count");
        check(store.users().stream().filter(u -> u.status() == UserRow.Status.LOCKED).count() == 1, "locked count");
        check(store.users().stream().filter(u -> u.status() == UserRow.Status.DELETED).count() == 1, "deleted count");
        rejected(() -> store.changeStatus(1, UserRow.Status.LOCKED), "last admin lock");
        rejected(() -> store.changeStatus(1, UserRow.Status.DELETED), "last admin delete");
        check(store.users().getFirst().status() == UserRow.Status.ACTIVE, "admin unchanged");
        check(store.changeStatus(7, UserRow.Status.ACTIVE).status() == UserRow.Status.ACTIVE, "unlock");
        check(store.changeStatus(7, UserRow.Status.LOCKED).status() == UserRow.Status.LOCKED, "lock");
        UserRow row = store.add(draft("  TEST.USER  ", "  Demo Name  ", " TEST.USER@EXAMPLE.TEST "));
        check(row.id() == 11 && row.role() == UserRow.Role.USER && row.status() == UserRow.Status.ACTIVE, "safe add defaults");
        check(row.username().equals("test.user") && row.email().equals("test.user@example.test"), "canonicalization");
        check(row.fullName().equals("Demo Name") && row.address() == null && row.dob() == null, "optional fields");
        check(row.createdAt().equals(OffsetDateTime.now(clock)), "created at");
        UserRow edited = store.update(row.id(), draft("test.changed", "Updated Name", "updated@example.test"));
        check(edited.createdAt().equals(row.createdAt()) && edited.id() == row.id() && edited.role() == row.role(), "edit keeps identity role date");
        check(store.users().size() == 11 && edited.username().equals("test.changed"), "edit replaces profile");
        store.changeStatus(row.id(), UserRow.Status.DELETED);
        check(store.users().size() == 11, "soft delete keeps row");
        rejected(() -> store.changeStatus(row.id(), UserRow.Status.ACTIVE), "no undelete");
        rejected(() -> store.update(row.id(), draft("restored", "Restored", "restored@example.test")), "no deleted edit");
        rejected(() -> store.add(draft("test.changed", "Another", "another@example.test")), "deleted username reserved");
        AdminUserStore emailStore = new AdminUserStore(clock);
        UserRow firstOwner = emailStore.add(draft("email.first", "First owner", " DUNG.DO@EXAMPLE.TEST "));
        check(firstOwner.id() == 11 && firstOwner.email().equals("dung.do@example.test"), "deleted email reusable and normalized");
        rejected(() -> emailStore.add(draft("email.duplicate", "Duplicate", "dung.do@example.test")), "active email reserved");
        rejected(() -> emailStore.add(draft("email.locked", "Locked duplicate", "ngoc.vo@example.test")), "locked email reserved");
        rejected(() -> emailStore.update(2, draft("minh.dev", "Another owner", "dung.do@example.test")), "edit cannot claim active email");
        rejected(() -> emailStore.update(2, draft("minh.dev", "Another owner", "ngoc.vo@example.test")), "edit cannot claim locked email");
        emailStore.changeStatus(firstOwner.id(), UserRow.Status.LOCKED);
        rejected(() -> emailStore.add(draft("email.duplicate", "Duplicate", "dung.do@example.test")), "locking keeps email reserved");
        emailStore.changeStatus(firstOwner.id(), UserRow.Status.DELETED);
        UserRow nextOwner = emailStore.add(draft("email.next", "Next owner", "dung.do@example.test"));
        check(nextOwner.id() != firstOwner.id(), "reused email gets a new identity");
        check(emailStore.users().stream().filter(u -> u.email().equals("dung.do@example.test")
            && u.status() == UserRow.Status.DELETED).count() == 2, "multiple deleted email owners retained");
        check(emailStore.users().stream().filter(u -> u.email().equals("dung.do@example.test")
            && u.status() != UserRow.Status.DELETED).count() == 1, "one non-deleted email owner");
        rejected(() -> emailStore.changeStatus(firstOwner.id(), UserRow.Status.ACTIVE), "email release does not enable restore");
        UserRow changedEmail = emailStore.update(2, draft("minh.dev", "Updated owner", "unused.owner@example.test"));
        check(changedEmail.id() == 2 && changedEmail.email().equals("unused.owner@example.test"), "profile email can change without changing identity");
        UserRow oldEmailOwner = emailStore.add(draft("email.previous", "Previous email", "minh.dev@example.test"));
        check(oldEmailOwner.email().equals("minh.dev@example.test"), "changing profile email releases previous value");
        UserRow sameEmail = emailStore.update(changedEmail.id(), draft("minh.dev", "Updated again", changedEmail.email()));
        check(sameEmail.email().equals(changedEmail.email()), "profile can retain its own email");
        emailStore.changeStatus(nextOwner.id(), UserRow.Status.DELETED);
        UserRow editedToDeletedEmail = emailStore.update(2, draft("minh.dev", "Updated owner", "dung.do@example.test"));
        check(editedToDeletedEmail.email().equals("dung.do@example.test"), "profile can use email held only by deleted rows");
        rejected(() -> store.add(draft("x", "Name", "a@example.test")), "short username");
        rejected(() -> store.add(draft("invalid name", "Name", "a@example.test")), "invalid username");
        rejected(() -> store.add(draft("valid.user", "  ", "a@example.test")), "blank full name");
        rejected(() -> store.add(draft("valid.user", "x".repeat(101), "a@example.test")), "long full name");
        rejected(() -> store.add(draft("valid.user", "Name", "not-an-email")), "invalid email");
        rejected(() -> store.add(new AdminUserStore.UserDraft("valid.user", "Name", null,
            LocalDate.of(2999, 1, 1), null, "a@example.test")), "future birthdate");
        rejected(() -> store.update(2, draft("hai.admin", "Name", "valid@example.test")), "duplicate update");
        check(store.users().get(1).username().equals("minh.dev"), "failed update atomic");
        rejected(() -> store.changeStatus(999, UserRow.Status.LOCKED), "unknown user");
        UserRow admin = store.update(1, draft("hai.admin", "Admin updated", "hai.admin@example.test"));
        check(admin.role() == UserRow.Role.ADMIN && store.isLastActiveAdmin(admin), "admin profile preserves role");
        try { store.users().clear(); throw new AssertionError("external mutation"); }
        catch (UnsupportedOperationException expected) { passed++; }
        check(new AdminUserStore(clock).users().size() == 10, "new store resets demo");
        System.out.println("PASS: " + passed + " A2 in-memory store checks; no DB or Server tests.");
    }
}
'@
    [IO.File]::WriteAllText($checkSource, $source, [Text.UTF8Encoding]::new($false))
    $javacExecutable = Join-Path (Split-Path $javaExecutable -Parent) 'javac.exe'
    & $javacExecutable '--module-path' $baseJar '--add-modules' 'javafx.base' '-cp' 'target/classes' '-d' $checkDirectory.FullName $checkSource
    if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed.' }
    & $javaExecutable '--module-path' $baseJar '--add-modules' 'javafx.base' '-cp' ("target/classes" + [IO.Path]::PathSeparator + $checkDirectory.FullName) 'admin.AdminUserStoreCheck'
    if ($LASTEXITCODE -ne 0) { throw 'A2 checks failed.' }
} finally {
    Pop-Location
}
