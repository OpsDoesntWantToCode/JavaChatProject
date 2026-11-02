package admin;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** Nguồn dữ liệu trong bộ nhớ của Admin; không kết nối DB hoặc lưu ra đĩa. */
public final class AdminUserStore {
    public record UserDraft(String username, String fullName, String address,
            LocalDate dob, UserRow.Gender gender, String email) { }

    private final ObservableList<UserRow> users = FXCollections.observableArrayList();
    private final ObservableList<UserRow> readOnlyUsers = FXCollections.unmodifiableObservableList(users);
    private final Clock clock;
    private long nextId = 11;

    public AdminUserStore() {
        this(Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    AdminUserStore(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        users.addAll(
                sample(1, "hai.admin", "Lưu Chí Hải", "Quận 5, TP.HCM", "2005-02-14", UserRow.Gender.MALE, UserRow.Status.ACTIVE, "2026-01-05T08:00:00+07:00"),
                sample(2, "minh.dev", "Trần Nguyễn Tuấn Minh", "TP. Thủ Đức, TP.HCM", "2005-08-20", UserRow.Gender.MALE, UserRow.Status.ACTIVE, "2026-01-20T09:00:00+07:00"),
                sample(3, "lan.nguyen", "Nguyễn Thị Lan", "Quận 10, TP.HCM", "2005-03-08", UserRow.Gender.FEMALE, UserRow.Status.ACTIVE, "2026-02-10T10:00:00+07:00"),
                sample(4, "an.tran", "Trần Hoàng An", "Biên Hòa, Đồng Nai", "2004-12-01", UserRow.Gender.MALE, UserRow.Status.ACTIVE, "2026-05-02T08:00:00+07:00"),
                sample(5, "binh.le", "Lê Thanh Bình", "Dĩ An, Bình Dương", "2005-07-19", UserRow.Gender.OTHER, UserRow.Status.ACTIVE, "2026-09-01T09:00:00+07:00"),
                sample(6, "chau.pham", "Phạm Minh Châu", "Quận 7, TP.HCM", "2005-05-25", UserRow.Gender.FEMALE, UserRow.Status.ACTIVE, "2026-10-01T00:00:00+07:00"),
                sample(7, "ngoc.vo", "Võ Bảo Ngọc", "Quận 3, TP.HCM", "2005-11-02", UserRow.Gender.FEMALE, UserRow.Status.LOCKED, "2026-10-15T14:00:00+07:00"),
                sample(8, "dung.do", "Đỗ Anh Dũng", "Bến Lức, Long An", "2004-09-09", UserRow.Gender.MALE, UserRow.Status.DELETED, "2026-10-31T23:59:00+07:00"),
                sample(9, "linh.ho", "Hồ Khánh Linh", "Nha Trang, Khánh Hòa", "2005-04-16", UserRow.Gender.FEMALE, UserRow.Status.ACTIVE, "2026-11-01T00:00:00+07:00"),
                sample(10, "phuc.bui", "Bùi Hữu Phúc", null, null, null, UserRow.Status.ACTIVE, "2026-11-01T09:00:00+07:00"));
    }

    private static UserRow sample(long id, String username, String name, String address,
            String dob, UserRow.Gender gender, UserRow.Status status, String createdAt) {
        return new UserRow(id, username, name, address, dob == null ? null : LocalDate.parse(dob),
                gender, username + "@example.test", status,
                id == 1 ? UserRow.Role.ADMIN : UserRow.Role.USER, OffsetDateTime.parse(createdAt));
    }

    public ObservableList<UserRow> users() { return readOnlyUsers; }

    public UserRow add(UserDraft draft) {
        UserDraft valid = validate(draft, -1);
        UserRow row = new UserRow(nextId++, valid.username(), valid.fullName(), valid.address(),
                valid.dob(), valid.gender(), valid.email(), UserRow.Status.ACTIVE, UserRow.Role.USER,
                OffsetDateTime.now(clock));
        users.add(row);
        return row;
    }

    public UserRow update(long id, UserDraft draft) {
        UserRow current = find(id);
        requireEditable(current);
        UserDraft valid = validate(draft, id);
        UserRow updated = new UserRow(id, valid.username(), valid.fullName(), valid.address(),
                valid.dob(), valid.gender(), valid.email(), current.status(), current.role(), current.createdAt());
        return replace(current, updated);
    }

    public UserRow changeStatus(long id, UserRow.Status status) {
        Objects.requireNonNull(status);
        UserRow current = find(id);
        requireEditable(current);
        if (current.role() == UserRow.Role.ADMIN && current.status() == UserRow.Status.ACTIVE
                && status != UserRow.Status.ACTIVE && isLastActiveAdmin(current)) {
            throw new IllegalArgumentException("Không thể khóa hoặc xóa Admin hoạt động cuối cùng.");
        }
        return replace(current, current.withStatus(status));
    }

    public boolean isLastActiveAdmin(UserRow row) {
        UserRow current = find(row.id());
        return current.role() == UserRow.Role.ADMIN && current.status() == UserRow.Status.ACTIVE
                && users.stream().filter(user -> user.role() == UserRow.Role.ADMIN
                        && user.status() == UserRow.Status.ACTIVE).count() == 1;
    }

    private UserRow replace(UserRow current, UserRow updated) {
        users.set(users.indexOf(current), updated);
        return updated;
    }

    private UserRow find(long id) {
        return users.stream().filter(row -> row.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng đã chọn."));
    }

    private static void requireEditable(UserRow user) {
        if (user.status() == UserRow.Status.DELETED) {
            throw new IllegalArgumentException("Tài khoản đã xóa chỉ được xem, không thể sửa hoặc khôi phục.");
        }
    }

    private UserDraft validate(UserDraft draft, long excludedId) {
        Objects.requireNonNull(draft);
        String username = clean(draft.username()).toLowerCase(Locale.ROOT);
        String name = clean(draft.fullName());
        String email = clean(draft.email()).toLowerCase(Locale.ROOT);
        if (!username.matches("[a-z0-9_.-]{3,50}")) {
            throw new IllegalArgumentException("Username cần 3–50 ký tự: a–z, 0–9, dấu chấm, gạch dưới hoặc gạch ngang.");
        }
        if (name.isBlank() || name.codePointCount(0, name.length()) > 100) {
            throw new IllegalArgumentException("Họ tên bắt buộc nhập, tối đa 100 ký tự.");
        }
        if (email.length() > 100 || !email.matches("[!-~]+")
                || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Email cần đúng dạng tên@miền, dùng ký tự ASCII và tối đa 100 ký tự.");
        }
        if (draft.dob() != null && draft.dob().isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Ngày sinh không được nằm trong tương lai.");
        }
        for (UserRow row : users) {
            if (row.id() == excludedId) { continue; }
            if (row.username().equals(username)) {
                throw new IllegalArgumentException("Username đã được sử dụng, kể cả ở tài khoản đã xóa.");
            }
            if (row.email().equals(email)) {
                throw new IllegalArgumentException("Email đã được sử dụng, kể cả ở tài khoản đã xóa.");
            }
        }
        String address = clean(draft.address());
        return new UserDraft(username, name, address.isEmpty() ? null : address,
                draft.dob(), draft.gender(), email);
    }

    private static String clean(String text) { return text == null ? "" : text.strip(); }
}
