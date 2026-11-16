package admin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.time.Year;
import java.time.ZoneId;
import java.util.TreeSet;
import java.util.stream.IntStream;

/** Tính số liệu từ dữ liệu mẫu hiện tại; không cache kết quả hoặc truy cập DB/Server. */
final class AdminStatistics
{
    record FriendStats(UserRow user, int directFriends, int friendsOfFriends)
    {
    }

    record ActivityStats(UserRow user, long openAppCount, int chatPartners, int chatGroups)
    {
    }

    record MonthCount(int month, long count)
    {
    }

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

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

    /** Đủ 12 tháng, giữ lịch sử đăng ký của mọi trạng thái. */
    List<MonthCount> registrationsByMonth(int year)
    {
        Year.of(year);
        long[] counts = new long[12];
        for (UserRow user : data.userStore().users())
        {
            var date = user.createdAt().atZoneSameInstant(ZONE);
            if (date.getYear() == year)
            {
                counts[date.getMonthValue() - 1]++;
            }
        }
        return IntStream.range(0, 12).mapToObj(month -> new MonthCount(month + 1, counts[month])).toList();
    }

    /** Mỗi user có OPEN_APP chỉ tính một lần trong từng tháng, không đếm tin nhắn. */
    List<MonthCount> activeUsersByMonth(int year)
    {
        Year.of(year);
        Map<Integer, Set<Long>> users = new HashMap<>();
        for (AdminDemoData.ActivityEvent event : data.records().activities())
        {
            var date = event.occurredAt().atZoneSameInstant(ZONE);
            if (event.kind() == AdminDemoData.ActivityKind.OPEN_APP && date.getYear() == year)
            {
                users.computeIfAbsent(date.getMonthValue(), ignored -> new HashSet<>()).add(event.userId());
            }
        }
        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> new MonthCount(month, users.getOrDefault(month, Set.of()).size())).toList();
    }

    List<Integer> chartYears(boolean registrations, int currentYear)
    {
        TreeSet<Integer> years = new TreeSet<>(Comparator.reverseOrder());
        years.add(currentYear);
        if (registrations)
        {
            data.userStore().users().forEach(user -> years.add(user.createdAt().atZoneSameInstant(ZONE).getYear()));
        }
        else
        {
            data.records().activities().stream().filter(event -> event.kind() == AdminDemoData.ActivityKind.OPEN_APP)
                    .forEach(event -> years.add(event.occurredAt().atZoneSameInstant(ZONE).getYear()));
        }
        return List.copyOf(years);
    }

    private static final class ActivityCount
    {
        private long opens;
        private final Set<Long> partners = new HashSet<>();
        private final Set<Long> groups = new HashSet<>();
    }
}
