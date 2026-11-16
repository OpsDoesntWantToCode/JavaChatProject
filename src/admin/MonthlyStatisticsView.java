package admin;

import java.time.Clock;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** A10/A11: biểu đồ tháng và giá trị bằng chữ từ cùng nguồn với A7/A9. */
final class MonthlyStatisticsView extends VBox implements AutoCloseable
{
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final AdminDemoData data;
    private final AdminStatistics statistics;
    private final boolean registrations;
    private final Clock clock;
    private final ComboBox<String> year = new ComboBox<>();
    private final Label error = new Label();
    private final Label summary = new Label();
    private final FlowPane values = new FlowPane(16, 6);
    private final BarChart<String, Number> chart;
    private boolean updatingYears;
    private final ListChangeListener<UserRow> listener = change -> reload();

    MonthlyStatisticsView(AdminDemoData data, boolean registrations)
    {
        this(data, registrations, Clock.system(ZONE));
    }

    MonthlyStatisticsView(AdminDemoData data, boolean registrations, Clock clock)
    {
        this.data = data;
        this.registrations = registrations;
        this.clock = clock.withZone(ZONE);
        statistics = new AdminStatistics(data);
        String id = registrations ? "registration-chart" : "activity-chart";
        setId(id);
        getStyleClass().addAll("admin-data-view", "monthly-statistics");
        setSpacing(12);
        setMinSize(0, 0);
        CategoryAxis months = new CategoryAxis();
        months.setLabel("Tháng");
        NumberAxis counts = new NumberAxis();
        counts.setLabel(registrations ? "Số tài khoản đăng ký" : "Số người mở ứng dụng");
        counts.setForceZeroInRange(true);
        counts.setMinorTickVisible(false);
        chart = new BarChart<>(months, counts);
        chart.setId(id + "-plot");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setMinHeight(180);
        chart.setPrefHeight(280);
        chart.setCategoryGap(12);
        chart.setAccessibleText(registrations ? "Biểu đồ đăng ký theo tháng" : "Biểu đồ người mở ứng dụng theo tháng");
        year.setId(id + "-year");
        year.setEditable(true);
        year.setPrefWidth(160);
        year.getEditor().setPromptText("Nhập năm, ví dụ 2026");
        year.setAccessibleHelp("Chọn năm có dữ liệu hoặc nhập năm khác để tra cứu.");
        String currentYear = Integer.toString(Year.now(this.clock).getValue());
        year.getItems().setAll(statistics.chartYears(registrations, Year.now(this.clock).getValue()).stream()
                .map(Object::toString).toList());
        year.setValue(currentYear);
        year.getEditor().setText(currentYear);
        Label hint = new Label("Chọn hoặc nhập năm khác để tra cứu.");
        hint.getStyleClass().add("muted");
        FlowPane filters = new FlowPane(12, 8, AdminTableSupport.field("Năm", year), hint);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        error.setId(id + "-error");
        error.setWrapText(true);
        error.setMinHeight(Region.USE_PREF_SIZE);
        error.getStyleClass().add("validation-message");
        summary.setId(id + "-summary");
        summary.setWrapText(true);
        summary.setMinHeight(Region.USE_PREF_SIZE);
        summary.getStyleClass().add("muted");
        values.setId(id + "-values");
        values.setMinHeight(Region.USE_PREF_SIZE);
        Label note = new Label(registrations
                ? "Dữ liệu mẫu · Tính theo ngày tạo, gồm tài khoản hiện đã khóa/xóa."
                : "Dữ liệu mẫu · Mỗi người mở ứng dụng chỉ tính một lần trong từng tháng; giữ lịch sử tài khoản đã khóa/xóa.");
        note.setWrapText(true);
        note.setMinHeight(Region.USE_PREF_SIZE);
        note.getStyleClass().add("demo-note");
        getChildren().addAll(filters, error, summary, chart, values, note);
        VBox.setVgrow(chart, Priority.ALWAYS);
        year.valueProperty().addListener((observable, old, value) ->
        {
            if (!updatingYears)
            {
                year.getEditor().setText(value == null ? "" : value);
                reload();
            }
        });
        year.getEditor().textProperty().addListener((observable, old, value) ->
        {
            if (!updatingYears)
            {
                updatingYears = true;
                try
                {
                    year.setValue(value);
                }
                finally
                {
                    updatingYears = false;
                }
                reload();
            }
        });
        data.userStore().users().addListener(listener);
        reload();
    }

    private void reload()
    {
        String input = year.getEditor().getText();
        List<String> options = statistics.chartYears(registrations, Year.now(clock).getValue()).stream()
                .map(Object::toString).toList();
        if (!year.getItems().equals(options))
        {
            updatingYears = true;
            try
            {
                year.getItems().setAll(options);
                year.setValue(input);
                year.getEditor().setText(input);
            }
            finally
            {
                updatingYears = false;
            }
        }
        if (input == null || !input.strip().matches("[0-9]{1,4}") || Integer.parseInt(input.strip()) == 0)
        {
            chart.getData().clear();
            values.getChildren().clear();
            error.setText("Năm phải là số nguyên từ 1 đến 9999. Chọn hoặc nhập lại năm.");
            error.setVisible(true);
            error.setManaged(true);
            summary.setText("Năm chưa hợp lệ");
            return;
        }
        int selectedYear = Integer.parseInt(input.strip());
        List<AdminStatistics.MonthCount> months = registrations ? statistics.registrationsByMonth(selectedYear)
                : statistics.activeUsersByMonth(selectedYear);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        values.getChildren().clear();
        for (AdminStatistics.MonthCount month : months)
        {
            String text = "Tháng " + month.month() + ": " + month.count();
            XYChart.Data<String, Number> bar = new XYChart.Data<>(Integer.toString(month.month()), month.count());
            bar.nodeProperty().addListener((observable, old, node) ->
            {
                if (node != null)
                {
                    Tooltip.install(node, new Tooltip(text));
                }
            });
            series.getData().add(bar);
            Label label = new Label(text);
            label.setMinWidth(95);
            values.getChildren().add(label);
        }
        chart.getData().setAll(List.of(series));
        NumberAxis axis = (NumberAxis) chart.getYAxis();
        long peak = months.stream().mapToLong(AdminStatistics.MonthCount::count).max().orElse(0);
        double tick = Math.max(1, Math.ceil(peak / 5.0));
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setTickUnit(tick);
        axis.setUpperBound(Math.max(tick, Math.ceil(peak / tick) * tick));
        error.setText("");
        error.setVisible(false);
        error.setManaged(false);
        long total = months.stream().mapToLong(AdminStatistics.MonthCount::count).sum();
        summary.setText(total == 0 ? "Không có dữ liệu năm " + selectedYear + ". Vẫn hiển thị đủ 12 tháng."
                : registrations ? "Năm " + selectedYear + " · " + total + " tài khoản đăng ký"
                : "Năm " + selectedYear + " · " + months.stream().filter(month -> month.count() > 0).count()
                        + " tháng có người mở ứng dụng");
    }

    @Override
    public void close()
    {
        data.userStore().users().removeListener(listener);
    }
}
