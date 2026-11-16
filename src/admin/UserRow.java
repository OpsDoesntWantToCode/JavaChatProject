package admin;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Hồ sơ hiển thị trong demo Admin; không phải DTO và không chứa mật khẩu. */
public record UserRow(long id, String username, String fullName, String address,
        LocalDate dob, Gender gender, String email, Status status, Role role,
        OffsetDateTime createdAt) {
    public enum Status {
        ACTIVE("Hoạt động"), LOCKED("Đã khóa"), DELETED("Đã xóa");
        private final String label;
        Status(String label) { this.label = label; }
        public String label() { return label; }
        @Override public String toString() { return label; }
    }

    public enum Role { USER, ADMIN }

    public enum Gender {
        MALE("Nam"), FEMALE("Nữ"), OTHER("Khác");
        private final String label;
        Gender(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public UserRow withStatus(Status value) {
        return new UserRow(id, username, fullName, address, dob, gender, email, value, role, createdAt);
    }
}
