package admin;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

/** Khoảng ngày Admin tính cả hai đầu theo giờ Việt Nam; ô trống không giới hạn. */
record AdminDateRange(LocalDate start, LocalDate end)
{
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    AdminDateRange
    {
        if (start != null && end != null && start.isAfter(end))
        {
            throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng Đến ngày.");
        }
    }

    static AdminDateRange read(DatePicker from, DatePicker to)
    {
        return new AdminDateRange(parseDate(from.getEditor().getText()), parseDate(to.getEditor().getText()));
    }

    boolean contains(OffsetDateTime time)
    {
        LocalDate date = time.atZoneSameInstant(ZONE).toLocalDate();
        return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end));
    }

    static DatePicker picker(String id)
    {
        DatePicker picker = new DatePicker();
        picker.setId(id);
        picker.setPromptText("dd/MM/yyyy");
        picker.setConverter(new StringConverter<>()
        {
            @Override
            public String toString(LocalDate value)
            {
                return value == null ? "" : DATE.format(value);
            }

            @Override
            public LocalDate fromString(String value)
            {
                try
                {
                    return parseDate(value);
                }
                catch (DateTimeException exception)
                {
                    // Giữ nội dung sai để màn hình báo lỗi, kể cả khi commit do mất focus.
                    return picker.getValue();
                }
            }
        });
        return picker;
    }

    private static LocalDate parseDate(String value)
    {
        return value == null || value.isBlank() ? null : LocalDate.parse(value.strip(), DATE);
    }
}
