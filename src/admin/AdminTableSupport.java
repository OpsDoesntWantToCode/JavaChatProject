package admin;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/** Thành phần bảng dùng chung cho các trang dữ liệu Admin. */
final class AdminTableSupport
{
    private AdminTableSupport()
    {
    }

    static <R, V> TableColumn<R, V> column(String title, Function<R, V> value,
            double width, Function<V, String> display)
    {
        TableColumn<R, V> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        column.setMinWidth(110);
        column.setCellFactory(ignored -> new TableCell<>()
        {
            @Override
            protected void updateItem(V item, boolean empty)
            {
                super.updateItem(item, empty);
                String text = empty || item == null ? null : display.apply(item);
                setText(text);
                setTooltip(text == null ? null : new Tooltip(text));
                setAccessibleText(text);
            }
        });
        return column;
    }

    static void prepare(TableView<?> table, String id, String accessibleText, String emptyText)
    {
        table.setId(id);
        table.setAccessibleText(accessibleText);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setMinHeight(160);
        table.setPrefHeight(300);
        Label empty = new Label(emptyText);
        empty.setWrapText(true);
        empty.getStyleClass().addAll("empty-state", "empty-state-title");
        table.setPlaceholder(empty);
    }

    static VBox field(String text, Node input)
    {
        Label label = new Label(text);
        label.setLabelFor(input);
        label.getStyleClass().add("filter-label");
        VBox box = new VBox(5, label, input);
        box.setMinWidth(130);
        return box;
    }
}
