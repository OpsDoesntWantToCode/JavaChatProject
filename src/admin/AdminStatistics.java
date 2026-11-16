package admin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Tính số liệu từ dữ liệu mẫu hiện tại; không cache kết quả hoặc truy cập DB/Server. */
final class AdminStatistics
{
    record FriendStats(UserRow user, int directFriends, int friendsOfFriends)
    {
    }

    record ActivityStats(UserRow user, long openAppCount, int chatPartners, int chatGroups)
    {
    }

    private final AdminDemoData data;

    AdminStatistics(AdminDemoData data)
    {
        this.data = Objects.requireNonNull(data);
    }

    /** Lịch sử đăng ký gồm cả hồ sơ hiện đã khóa/xóa, mới nhất trước. */
    List<UserRow> registrations(AdminDateRange range)
    {
        Objects.requireNonNull(range);
        return data.userStore().users().stream().filter(user -> range.contains(user.createdAt()))
                .sorted(Comparator.comparing(UserRow::createdAt).reversed().thenComparingLong(UserRow::id))
                .toList();
    }

    /** Đồ thị quan hệ hiện tại: không có nút DELETED, vẫn có nút LOCKED và user không có bạn. */
    List<FriendStats> friendStatistics()
    {
        List<UserRow> users = data.userStore().users().stream()
                .filter(user -> user.status() != UserRow.Status.DELETED)
                .sorted(Comparator.comparingLong(UserRow::id)).toList();
        Map<Long, Set<Long>> graph = new HashMap<>();
        for (UserRow user : users)
        {
            graph.put(user.id(), new HashSet<>());
        }
        for (AdminDemoData.Friendship pair : data.records().friendships())
        {
            if (graph.containsKey(pair.firstUserId()) && graph.containsKey(pair.secondUserId()))
            {
                graph.get(pair.firstUserId()).add(pair.secondUserId());
                graph.get(pair.secondUserId()).add(pair.firstUserId());
            }
        }
        return users.stream().map(user ->
        {
            Set<Long> direct = graph.get(user.id());
            Set<Long> secondDegree = new HashSet<>();
            for (long friendId : direct)
            {
                secondDegree.addAll(graph.get(friendId));
            }
            secondDegree.remove(user.id());
            secondDegree.removeAll(direct);
            return new FriendStats(user, direct.size(), secondDegree.size());
        }).toList();
    }

    /** Chỉ user có sự kiện trong khoảng chọn; người nhận không tự được tính là người chủ động chat. */
    List<ActivityStats> activeUsers(AdminDateRange range)
    {
        Objects.requireNonNull(range);
        Map<Long, ActivityCount> counts = new HashMap<>();
        for (AdminDemoData.ActivityEvent event : data.records().activities())
        {
            if (!range.contains(event.occurredAt()))
            {
                continue;
            }
            ActivityCount count = counts.computeIfAbsent(event.userId(), ignored -> new ActivityCount());
            switch (event.kind())
            {
                case OPEN_APP -> count.opens++;
                case DIRECT_MESSAGE -> count.partners.add(event.targetId());
                case GROUP_MESSAGE -> count.groups.add(event.targetId());
            }
        }
        return data.userStore().users().stream().filter(user -> counts.containsKey(user.id()))
                .sorted(Comparator.comparingLong(UserRow::id)).map(user ->
                {
                    ActivityCount count = counts.get(user.id());
                    return new ActivityStats(user, count.opens, count.partners.size(), count.groups.size());
                }).toList();
    }

    private static final class ActivityCount
    {
        private long opens;
        private final Set<Long> partners = new HashSet<>();
        private final Set<Long> groups = new HashSet<>();
    }
}
