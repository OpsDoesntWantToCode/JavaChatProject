package admin;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Dữ liệu mẫu theo ID cho một phiên Admin; không phải DTO hoặc dữ liệu từ Server. */
final class AdminDemoData
{
    record LoginEvent(long id, long userId, OffsetDateTime occurredAt)
    {
    }

    /** Mỗi cặp chỉ lưu một lần, với firstUserId < secondUserId. */
    record Friendship(long firstUserId, long secondUserId)
    {
    }

    record ChatGroup(long id, String name, long creatorId, OffsetDateTime createdAt,
            boolean encrypted)
    {
    }

    record Membership(long groupId, long userId, boolean admin)
    {
    }

    record SpamReport(long id, long reporterId, long reportedId, String reason,
            OffsetDateTime occurredAt)
    {
    }

    enum ActivityKind
    {
        OPEN_APP, DIRECT_MESSAGE, GROUP_MESSAGE
    }

    /** targetId: null khi mở app, ID user khi chat cá nhân, ID nhóm khi chat nhóm. */
    record ActivityEvent(long id, long userId, ActivityKind kind, Long targetId,
            OffsetDateTime occurredAt)
    {
    }

    /** Bản ghi lịch sử bất biến; hồ sơ và trạng thái luôn tra cứu từ store hiện tại. */
    record Records(List<LoginEvent> logins, List<Friendship> friendships,
            List<ChatGroup> groups, List<Membership> memberships,
            List<SpamReport> reports, List<ActivityEvent> activities, Set<Long> onlineUserIds)
    {
        Records
        {
            logins = List.copyOf(logins);
            friendships = List.copyOf(friendships);
            groups = List.copyOf(groups);
            memberships = List.copyOf(memberships);
            reports = List.copyOf(reports);
            activities = List.copyOf(activities);
            onlineUserIds = Set.copyOf(onlineUserIds);
        }
    }

    private final AdminUserStore userStore;
    private final Records records;

    AdminDemoData(AdminUserStore userStore)
    {
        this(userStore, sampleRecords());
    }

    AdminDemoData(AdminUserStore userStore, Records records)
    {
        this.userStore = Objects.requireNonNull(userStore);
        this.records = Objects.requireNonNull(records);
        validate();
    }

    AdminUserStore userStore()
    {
        return userStore;
    }

    Records records()
    {
        return records;
    }

    UserRow user(long id)
    {
        return userStore.users().stream().filter(row -> row.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ID user mẫu không tồn tại: " + id));
    }

    List<LoginEvent> loginHistory(long userId)
    {
        user(userId);
        return records.logins().stream().filter(event -> event.userId() == userId)
                .sorted(Comparator.comparing(LoginEvent::occurredAt).reversed()).toList();
    }

    List<UserRow> friendsOf(long userId)
    {
        if (user(userId).status() == UserRow.Status.DELETED)
        {
            return List.of();
        }
        return records.friendships().stream()
                .filter(pair -> pair.firstUserId() == userId || pair.secondUserId() == userId)
                .map(pair -> user(pair.firstUserId() == userId ? pair.secondUserId() : pair.firstUserId()))
                .filter(row -> row.status() != UserRow.Status.DELETED)
                .sorted(Comparator.comparingLong(UserRow::id)).toList();
    }

    boolean isOnline(long userId)
    {
        return user(userId).status() == UserRow.Status.ACTIVE && records.onlineUserIds().contains(userId);
    }

    private void validate()
    {
        Set<Long> loginIds = new HashSet<>();
        for (LoginEvent event : records.logins())
        {
            uniqueId(loginIds, event.id());
            requireUserAt(event.userId(), event.occurredAt());
        }
        Set<Friendship> pairs = new HashSet<>();
        for (Friendship pair : records.friendships())
        {
            require(pair.firstUserId() < pair.secondUserId(), "Cặp bạn phải có ID tăng dần, không tự nối.");
            user(pair.firstUserId());
            user(pair.secondUserId());
            require(pairs.add(pair), "Trùng cặp bạn mẫu.");
        }
        Set<Long> groupIds = new HashSet<>();
        for (ChatGroup group : records.groups())
        {
            uniqueId(groupIds, group.id());
            require(group.name() != null && !group.name().isBlank(), "Tên nhóm mẫu không được rỗng.");
            requireUserAt(group.creatorId(), group.createdAt());
        }
        Set<String> memberPairs = new HashSet<>();
        for (Membership member : records.memberships())
        {
            group(member.groupId());
            user(member.userId());
            require(memberPairs.add(member.groupId() + ":" + member.userId()), "Trùng thành viên nhóm.");
        }
        for (ChatGroup group : records.groups())
        {
            require(records.memberships().stream().anyMatch(member -> member.groupId() == group.id()
                    && member.userId() == group.creatorId() && member.admin()), "Người tạo phải là Admin nhóm mẫu.");
        }
        Set<Long> activityIds = new HashSet<>();
        for (ActivityEvent event : records.activities())
        {
            uniqueId(activityIds, event.id());
            requireUserAt(event.userId(), event.occurredAt());
            require(event.kind() != null, "Thiếu loại hoạt động mẫu.");
            if (event.kind() == ActivityKind.OPEN_APP)
            {
                require(event.targetId() == null, "OPEN_APP không có đối tượng chat.");
            }
            else if (event.kind() == ActivityKind.DIRECT_MESSAGE)
            {
                require(event.targetId() != null && event.targetId() != event.userId(), "Đối tượng chat không hợp lệ.");
                requireUserAt(event.targetId(), event.occurredAt());
            }
            else
            {
                require(event.targetId() != null, "Thiếu nhóm chat.");
                ChatGroup group = group(event.targetId());
                require(!event.occurredAt().isBefore(group.createdAt()), "Tin nhắn có trước nhóm.");
                require(records.memberships().stream().anyMatch(member -> member.groupId() == group.id()
                        && member.userId() == event.userId()), "Người gửi không thuộc nhóm mẫu.");
            }
        }
        Set<Long> reportIds = new HashSet<>();
        for (SpamReport report : records.reports())
        {
            uniqueId(reportIds, report.id());
            requireUserAt(report.reporterId(), report.occurredAt());
            requireUserAt(report.reportedId(), report.occurredAt());
            require(report.reporterId() != report.reportedId(), "Không tự báo cáo spam.");
            require(report.reason() != null && !report.reason().isBlank(), "Thiếu lý do báo cáo.");
            require(records.activities().stream().anyMatch(event -> event.kind() == ActivityKind.DIRECT_MESSAGE
                    && event.userId() == report.reportedId() && Long.valueOf(report.reporterId()).equals(event.targetId())
                    && !event.occurredAt().isAfter(report.occurredAt())), "Thiếu tin nhắn trước báo cáo mẫu.");
        }
        records.onlineUserIds().forEach(this::user);
    }

    private ChatGroup group(long id)
    {
        return records.groups().stream().filter(value -> value.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ID nhóm mẫu không tồn tại: " + id));
    }

    private void requireUserAt(long id, OffsetDateTime time)
    {
        require(time != null && !time.isBefore(user(id).createdAt()), "Sự kiện có trước hồ sơ hoặc thiếu thời gian.");
    }

    private static void uniqueId(Set<Long> ids, long id)
    {
        require(id > 0 && ids.add(id), "ID bản ghi mẫu phải dương và không trùng trong cùng loại dữ liệu.");
    }

    private static void require(boolean valid, String message)
    {
        if (!valid)
        {
            throw new IllegalArgumentException(message);
        }
    }

    private static OffsetDateTime at(String value)
    {
        return OffsetDateTime.parse(value + "+07:00");
    }

    private static Records sampleRecords()
    {
        // Giữ nguyên 10 hồ sơ A2 (đều tạo trong 2026); không tạo lịch sử trước ngày đăng ký.
        return new Records(
                List.of(
                        new LoginEvent(1, 1, at("2026-01-05T08:00:00")),
                        new LoginEvent(2, 2, at("2026-01-20T09:00:00")),
                        new LoginEvent(3, 3, at("2026-02-10T10:00:00")),
                        new LoginEvent(4, 2, at("2026-10-31T23:59:59")),
                        new LoginEvent(5, 2, at("2026-11-01T00:00:00")),
                        new LoginEvent(6, 8, at("2026-11-01T00:00:00")),
                        new LoginEvent(7, 9, at("2026-11-01T00:00:00")),
                        new LoginEvent(8, 2, at("2026-11-01T23:59:59")),
                        new LoginEvent(9, 3, at("2026-11-02T08:30:00")),
                        new LoginEvent(10, 7, at("2026-11-02T09:00:00")),
                        new LoginEvent(11, 6, at("2026-11-03T10:00:00"))),
                List.of(new Friendship(1, 2), new Friendship(1, 3), new Friendship(2, 3),
                        new Friendship(2, 4), new Friendship(2, 7), new Friendship(3, 5),
                        new Friendship(4, 5), new Friendship(3, 8), new Friendship(8, 9)),
                List.of(
                        new ChatGroup(1, "Đồ án Java", 1, at("2026-10-20T08:00:00"), false),
                        new ChatGroup(2, "Nhóm học tập", 3, at("2026-11-01T08:00:00"), true),
                        new ChatGroup(3, "Trao đổi bài tập", 2, at("2026-11-02T08:00:00"), false)),
                List.of(new Membership(1, 1, true), new Membership(1, 2, true), new Membership(1, 3, false),
                        new Membership(1, 7, false), new Membership(2, 3, true), new Membership(2, 5, false),
                        new Membership(2, 8, false), new Membership(2, 9, true),
                        new Membership(3, 2, true), new Membership(3, 4, false), new Membership(3, 6, false)),
                List.of(
                        new SpamReport(1, 3, 4, "Gửi quảng cáo lặp lại", at("2026-11-03T09:10:00")),
                        new SpamReport(2, 2, 7, "Tin nhắn không mong muốn", at("2026-11-03T09:20:00")),
                        new SpamReport(3, 9, 8, "Đường dẫn đáng ngờ", at("2026-11-03T09:30:00"))),
                List.of(
                        new ActivityEvent(1, 2, ActivityKind.OPEN_APP, null, at("2026-10-31T23:59:59")),
                        new ActivityEvent(2, 2, ActivityKind.OPEN_APP, null, at("2026-11-01T00:00:00")),
                        new ActivityEvent(3, 2, ActivityKind.OPEN_APP, null, at("2026-11-01T23:59:59")),
                        new ActivityEvent(4, 8, ActivityKind.OPEN_APP, null, at("2026-11-01T00:00:00")),
                        new ActivityEvent(5, 7, ActivityKind.OPEN_APP, null, at("2026-11-02T09:00:00")),
                        new ActivityEvent(6, 3, ActivityKind.OPEN_APP, null, at("2026-11-02T08:30:00")),
                        new ActivityEvent(7, 2, ActivityKind.DIRECT_MESSAGE, 3L, at("2026-11-02T10:00:00")),
                        new ActivityEvent(8, 2, ActivityKind.DIRECT_MESSAGE, 3L, at("2026-11-02T10:01:00")),
                        new ActivityEvent(9, 2, ActivityKind.DIRECT_MESSAGE, 4L, at("2026-11-02T10:02:00")),
                        new ActivityEvent(10, 2, ActivityKind.GROUP_MESSAGE, 1L, at("2026-11-02T10:03:00")),
                        new ActivityEvent(11, 2, ActivityKind.GROUP_MESSAGE, 1L, at("2026-11-02T10:04:00")),
                        new ActivityEvent(12, 2, ActivityKind.GROUP_MESSAGE, 3L, at("2026-11-02T10:05:00")),
                        new ActivityEvent(13, 4, ActivityKind.DIRECT_MESSAGE, 3L, at("2026-11-03T09:00:00")),
                        new ActivityEvent(14, 7, ActivityKind.DIRECT_MESSAGE, 2L, at("2026-11-03T09:01:00")),
                        new ActivityEvent(15, 8, ActivityKind.DIRECT_MESSAGE, 9L, at("2026-11-03T09:02:00"))),
                Set.of(1L, 2L, 3L, 6L, 9L));
    }
}
