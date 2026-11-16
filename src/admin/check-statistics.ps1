# Kiem tra logic thong ke Admin; khong khoi tao Toolkit, khong mo UI va khong ket noi DB/Server.
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
    $checkDirectory = New-Item -ItemType Directory -Force -Path 'target/admin-statistics-checks'
    $checkSource = Join-Path $checkDirectory.FullName 'AdminStatisticsCheck.java'
    $source = @'
package admin;

import java.time.*;
import java.util.*;

public final class AdminStatisticsCheck
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

    private static AdminDateRange range(String start, String end)
    {
        return new AdminDateRange(start == null ? null : LocalDate.parse(start),
                end == null ? null : LocalDate.parse(end));
    }

    private static List<Long> ids(List<UserRow> users)
    {
        return users.stream().map(UserRow::id).toList();
    }

    private static AdminStatistics.FriendStats friends(AdminStatistics stats, long id)
    {
        return stats.friendStatistics().stream().filter(row -> row.user().id() == id).findFirst().orElseThrow();
    }

    private static AdminStatistics.ActivityStats activity(AdminStatistics stats, AdminDateRange range, long id)
    {
        return stats.activeUsers(range).stream().filter(row -> row.user().id() == id).findFirst().orElseThrow();
    }

    private static void activityEquals(AdminStatistics stats, AdminDateRange range, long id,
            long opens, int partners, int groups)
    {
        AdminStatistics.ActivityStats row = activity(stats, range, id);
        check(row.openAppCount() == opens && row.chatPartners() == partners && row.chatGroups() == groups,
                "activity for user " + id + ": " + row);
    }

    private static void immutable(Runnable mutation)
    {
        try
        {
            mutation.run();
            throw new AssertionError("Statistics results must be immutable");
        }
        catch (UnsupportedOperationException expected)
        {
            passed++;
        }
    }

    public static void main(String[] args)
    {
        Clock clock = Clock.fixed(Instant.parse("2026-11-16T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        AdminUserStore store = new AdminUserStore(clock);
        AdminDemoData data = new AdminDemoData(store);
        AdminDemoData.Records original = data.records();
        AdminStatistics stats = new AdminStatistics(data);
        AdminDateRange all = range(null, null);
        long[] expectedRegistrations = {2, 1, 0, 0, 1, 0, 0, 0, 1, 3, 2, 0};
        long[] expectedActivity = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 0};
        check(stats.registrationsByMonth(2026).size() == 12 && stats.activeUsersByMonth(2026).size() == 12,
                "both charts return all twelve months");
        for (int month = 1; month <= 12; month++)
        {
            check(stats.registrationsByMonth(2026).get(month - 1).equals(new AdminStatistics.MonthCount(month, expectedRegistrations[month - 1])),
                    "manual registration total for month " + month);
            check(stats.activeUsersByMonth(2026).get(month - 1).equals(new AdminStatistics.MonthCount(month, expectedActivity[month - 1])),
                    "manual distinct OPEN_APP users for month " + month);
        }
        check(stats.registrationsByMonth(2025).stream().allMatch(month -> month.count() == 0)
                && stats.activeUsersByMonth(2025).stream().allMatch(month -> month.count() == 0), "empty year keeps twelve zeros");
        check(stats.chartYears(true, 2050).equals(List.of(2050, 2026))
                && stats.chartYears(false, 2050).equals(List.of(2050, 2026)), "current year included without invented history");
        immutable(() -> stats.registrationsByMonth(2026).clear());
        immutable(() -> stats.activeUsersByMonth(2026).clear());
        AdminUserStore secondYearStore = new AdminUserStore(Clock.fixed(Instant.parse("2026-12-31T17:00:00Z"), ZoneOffset.UTC));
        UserRow nextYear = secondYearStore.add(new AdminUserStore.UserDraft("year.new", "Year test", null, null, null, "year@example.test"));
        List<AdminDemoData.ActivityEvent> yearEvents = new ArrayList<>(original.activities());
        yearEvents.add(new AdminDemoData.ActivityEvent(200, nextYear.id(), AdminDemoData.ActivityKind.OPEN_APP, null,
                OffsetDateTime.parse("2026-12-31T17:00:00Z")));
        yearEvents.add(new AdminDemoData.ActivityEvent(201, nextYear.id(), AdminDemoData.ActivityKind.OPEN_APP, null,
                OffsetDateTime.parse("2027-01-01T00:00:01+07:00")));
        yearEvents.add(new AdminDemoData.ActivityEvent(202, nextYear.id(), AdminDemoData.ActivityKind.OPEN_APP, null,
                OffsetDateTime.parse("2027-02-01T00:00:00+07:00")));
        AdminStatistics twoYearStats = new AdminStatistics(new AdminDemoData(secondYearStore,
                new AdminDemoData.Records(original.logins(), original.friendships(), original.groups(), original.memberships(),
                        original.reports(), yearEvents, original.onlineUserIds())));
        check(twoYearStats.registrationsByMonth(2026).stream().mapToLong(AdminStatistics.MonthCount::count).sum() == 10
                && twoYearStats.registrationsByMonth(2027).getFirst().count() == 1, "registration UTC year boundary converted to Vietnam");
        check(twoYearStats.activeUsersByMonth(2027).getFirst().count() == 1
                && twoYearStats.activeUsersByMonth(2027).get(1).count() == 1, "repeat opens deduplicated per month, same user counts in each month");
        check(twoYearStats.chartYears(true, 2050).equals(List.of(2050, 2027, 2026))
                && twoYearStats.chartYears(false, 2050).equals(List.of(2050, 2027, 2026)), "two-year sources produce real year choices");
        secondYearStore.changeStatus(nextYear.id(), UserRow.Status.DELETED);
        check(twoYearStats.registrationsByMonth(2027).getFirst().count() == 1
                && twoYearStats.activeUsersByMonth(2027).getFirst().count() == 1, "deleted user retained in both monthly histories");

        check(stats.registrations(all).size() == 10, "all registrations including deleted profiles");
        check(ids(stats.registrations(range("2026-10-31", "2026-11-01"))).equals(List.of(10L, 9L, 8L)),
                "registration dates at midnight and end of day, newest first");
        check(ids(stats.registrations(range("2026-10-01", "2026-10-01"))).equals(List.of(6L)), "registration at start boundary");
        check(stats.registrations(range(null, "2026-10-31")).size() == 8, "unbounded registration start");
        check(ids(stats.registrations(range("2026-11-01", null))).equals(List.of(10L, 9L)), "unbounded registration end");
        check(stats.registrations(range("2025-01-01", "2025-12-31")).isEmpty(), "year without registrations");
        check(stats.registrations(all).stream().anyMatch(user -> user.id() == 8 && user.status() == UserRow.Status.DELETED),
                "deleted user retained in registration history");

        // Explicit expectations for the seeded graph; not computed by the function under test.
        long[][] expectedFriends = {{1,2,3}, {2,4,1}, {3,3,2}, {4,2,3}, {5,2,2}, {6,0,0}, {7,1,3}, {9,0,0}, {10,0,0}};
        check(stats.friendStatistics().size() == 9, "deleted owner excluded from friend statistics");
        for (long[] expected : expectedFriends)
        {
            AdminStatistics.FriendStats row = friends(stats, expected[0]);
            check(row.directFriends() == expected[1] && row.friendsOfFriends() == expected[2],
                    "friend graph for user " + expected[0] + ": " + row);
        }
        check(friends(stats, 7).user().status() == UserRow.Status.LOCKED, "locked node participates in graph");
        check(friends(stats, 4).friendsOfFriends() == 3, "two paths to user 3 count once");
        check(friends(stats, 9).friendsOfFriends() == 0, "deleted intermediate 8 cannot connect user 9 to user 3");
        check(stats.friendStatistics().stream().allMatch(row -> row.directFriends() == data.friendsOf(row.user().id()).size()),
                "direct counts agree with A3 friends source");

        check(stats.activeUsers(all).stream().map(row -> row.user().id()).toList().equals(List.of(2L, 3L, 4L, 7L, 8L)),
                "only event senders/openers are active, in deterministic order");
        activityEquals(stats, all, 2, 3, 2, 2);
        activityEquals(stats, all, 3, 1, 0, 0);
        activityEquals(stats, all, 4, 0, 1, 0);
        activityEquals(stats, all, 7, 1, 1, 0);
        activityEquals(stats, all, 8, 1, 1, 0);
        activityEquals(stats, range("2026-11-01", "2026-11-01"), 2, 2, 0, 0);
        activityEquals(stats, range("2026-10-31", "2026-10-31"), 2, 1, 0, 0);
        activityEquals(stats, range("2026-11-02", "2026-11-02"), 2, 0, 2, 2);
        check(stats.activeUsers(range("2026-11-02", "2026-11-02")).size() == 3, "activity window changes active population");
        check(stats.activeUsers(range("2026-11-04", "2026-11-04")).isEmpty(), "empty activity day");
        check(stats.activeUsers(range(null, "2026-10-31")).size() == 1, "unbounded activity start");
        check(stats.activeUsers(range("2026-11-03", null)).size() == 3, "unbounded activity end");

        List<AdminDemoData.ActivityEvent> events = new ArrayList<>(original.activities());
        events.add(new AdminDemoData.ActivityEvent(100, 2, AdminDemoData.ActivityKind.OPEN_APP, null,
                OffsetDateTime.parse("2026-10-31T17:00:00Z")));
        events.add(new AdminDemoData.ActivityEvent(101, 2, AdminDemoData.ActivityKind.OPEN_APP, null,
                OffsetDateTime.parse("2026-11-01T16:59:59.999999999Z")));
        events.add(new AdminDemoData.ActivityEvent(102, 2, AdminDemoData.ActivityKind.OPEN_APP, null,
                OffsetDateTime.parse("2026-11-01T17:00:00Z")));
        AdminDemoData offsetData = new AdminDemoData(store, new AdminDemoData.Records(original.logins(), original.friendships(),
                original.groups(), original.memberships(), original.reports(), events, original.onlineUserIds()));
        AdminStatistics offsetStats = new AdminStatistics(offsetData);
        activityEquals(offsetStats, range("2026-11-01", "2026-11-01"), 2, 4, 0, 0);
        activityEquals(offsetStats, range("2026-11-02", "2026-11-02"), 2, 1, 2, 2);

        List<AdminStatistics.FriendStats> snapshot = stats.friendStatistics();
        UserRow renamed = store.update(2, new AdminUserStore.UserDraft("minh.stats", "Statistics Minh", null, null, null,
                "minh.stats@example.test"));
        check(friends(stats, 2).user() == renamed && activity(stats, all, 2).user() == renamed
                && stats.registrations(all).contains(renamed), "all calculations resolve latest profile");
        check(snapshot.stream().anyMatch(row -> row.user().id() == 2 && row.user().username().equals("minh.dev")),
                "previous result remains a snapshot, caller must recompute after mutation");
        store.changeStatus(2, UserRow.Status.LOCKED);
        check(friends(stats, 1).directFriends() == 2 && friends(stats, 1).friendsOfFriends() == 3,
                "locking leaves graph unchanged");
        activityEquals(stats, all, 2, 3, 2, 2);
        store.changeStatus(2, UserRow.Status.DELETED);
        check(stats.friendStatistics().size() == 8, "new deleted user excluded from graph");
        check(stats.registrationsByMonth(2026).getFirst().count() == 2
                && stats.activeUsersByMonth(2026).get(10).count() == 4, "soft delete does not change past monthly totals");
        check(friends(stats, 1).directFriends() == 1 && friends(stats, 1).friendsOfFriends() == 1,
                "deleting intermediate changes both degrees without deleting raw edges");
        check(friends(stats, 7).directFriends() == 0, "locked user's deleted friend no longer counted");
        check(stats.registrations(all).size() == 10, "soft delete keeps registration history");
        activityEquals(stats, all, 2, 3, 2, 2);
        activityEquals(stats, all, 7, 1, 1, 0);
        check(data.records() == original && original.friendships().size() == 9 && original.activities().size() == 15,
                "statistics and soft delete do not mutate raw historical data");

        UserRow added = store.add(new AdminUserStore.UserDraft("new.stats", "New stats user", null, null, null,
                "new.stats@example.test"));
        check(ids(stats.registrations(range("2026-11-16", "2026-11-16"))).equals(List.of(added.id())),
                "new registration uses injected clock and appears immediately");
        check(friends(stats, added.id()).directFriends() == 0 && friends(stats, added.id()).friendsOfFriends() == 0,
                "new user has zero friends");
        check(stats.activeUsers(all).stream().noneMatch(row -> row.user().id() == added.id()), "new user has no invented activity");
        immutable(() -> stats.registrations(all).clear());
        immutable(() -> stats.friendStatistics().clear());
        immutable(() -> stats.activeUsers(all).clear());
        check(store.users().size() == 11, "calculations cannot mutate user collection");
        System.out.println("PASS: " + passed + " AdminStatistics checks; no Toolkit, UI, DB or Server tests.");
    }
}
'@
    [IO.File]::WriteAllText($checkSource, $source, [Text.UTF8Encoding]::new($false))
    $javacExecutable = Join-Path (Split-Path $javaExecutable -Parent) 'javac.exe'
    & $javacExecutable '--module-path' $modulePath '--add-modules' 'javafx.controls' '-cp' 'target/classes' '-d' $checkDirectory.FullName $checkSource
    if ($LASTEXITCODE -ne 0)
    {
        throw 'Statistics checks compilation failed.'
    }
    & $javaExecutable '--module-path' $modulePath '--add-modules' 'javafx.controls' '-cp' ("target/classes" + [IO.Path]::PathSeparator + $checkDirectory.FullName) 'admin.AdminStatisticsCheck'
    if ($LASTEXITCODE -ne 0)
    {
        throw 'Statistics checks failed.'
    }
}
finally
{
    Pop-Location
}
