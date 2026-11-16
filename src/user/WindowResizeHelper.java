package user;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Trợ thủ thay đổi kích thước cửa sổ không viền (Undecorated Stage Resizing Helper)
 * Cho phép người dùng kéo giãn kích thước cửa sổ tại các mép viền và góc (8 hướng)
 * giống hệt như các ứng dụng desktop native hiện đại (VS Code, Discord, Spotify).
 */
public class WindowResizeHelper {

    private static final double BORDER_THRESHOLD = 6.0;

    public static void attach(Stage stage, Scene scene) {
        if (stage == null || scene == null) return;
        ResizeListener listener = new ResizeListener(stage);
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, listener);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, listener);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, listener);
    }

    private static class ResizeListener implements EventHandler<MouseEvent> {
        private final Stage stage;
        private Cursor cursorEvent = Cursor.DEFAULT;
        private double startX = 0;
        private double startY = 0;
        private double startStageX = 0;
        private double startStageY = 0;
        private double startStageWidth = 0;
        private double startStageHeight = 0;

        public ResizeListener(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            if (stage.isMaximized() || stage.isFullScreen()) {
                if (cursorEvent != Cursor.DEFAULT) {
                    cursorEvent = Cursor.DEFAULT;
                    stage.getScene().setCursor(Cursor.DEFAULT);
                }
                return;
            }

            if (MouseEvent.MOUSE_MOVED.equals(mouseEvent.getEventType())) {
                double mouseX = mouseEvent.getSceneX();
                double mouseY = mouseEvent.getSceneY();
                double sceneWidth = stage.getScene().getWidth();
                double sceneHeight = stage.getScene().getHeight();

                boolean isLeft = mouseX < BORDER_THRESHOLD;
                boolean isRight = mouseX > sceneWidth - BORDER_THRESHOLD;
                boolean isTop = mouseY < BORDER_THRESHOLD;
                boolean isBottom = mouseY > sceneHeight - BORDER_THRESHOLD;

                if (isTop && isLeft) {
                    cursorEvent = Cursor.NW_RESIZE;
                } else if (isTop && isRight) {
                    cursorEvent = Cursor.NE_RESIZE;
                } else if (isBottom && isLeft) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (isBottom && isRight) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (isLeft) {
                    cursorEvent = Cursor.W_RESIZE;
                } else if (isRight) {
                    cursorEvent = Cursor.E_RESIZE;
                } else if (isTop) {
                    cursorEvent = Cursor.N_RESIZE;
                } else if (isBottom) {
                    cursorEvent = Cursor.S_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }

                stage.getScene().setCursor(cursorEvent);

            } else if (MouseEvent.MOUSE_EXITED.equals(mouseEvent.getEventType())) {
                cursorEvent = Cursor.DEFAULT;
                stage.getScene().setCursor(Cursor.DEFAULT);

            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEvent.getEventType())) {
                startX = mouseEvent.getScreenX();
                startY = mouseEvent.getScreenY();
                startStageX = stage.getX();
                startStageY = stage.getY();
                startStageWidth = stage.getWidth();
                startStageHeight = stage.getHeight();

            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEvent.getEventType())) {
                if (cursorEvent == Cursor.DEFAULT) return;

                double deltaX = mouseEvent.getScreenX() - startX;
                double deltaY = mouseEvent.getScreenY() - startY;

                double minWidth = Math.max(stage.getMinWidth(), 360);
                double minHeight = Math.max(stage.getMinHeight(), 300);

                // Resize theo hướng ngang
                if (cursorEvent == Cursor.E_RESIZE || cursorEvent == Cursor.NE_RESIZE || cursorEvent == Cursor.SE_RESIZE) {
                    double newWidth = startStageWidth + deltaX;
                    if (newWidth >= minWidth) {
                        stage.setWidth(newWidth);
                    }
                } else if (cursorEvent == Cursor.W_RESIZE || cursorEvent == Cursor.NW_RESIZE || cursorEvent == Cursor.SW_RESIZE) {
                    double newWidth = startStageWidth - deltaX;
                    if (newWidth >= minWidth) {
                        stage.setX(startStageX + deltaX);
                        stage.setWidth(newWidth);
                    }
                }

                // Resize theo hướng dọc
                if (cursorEvent == Cursor.S_RESIZE || cursorEvent == Cursor.SW_RESIZE || cursorEvent == Cursor.SE_RESIZE) {
                    double newHeight = startStageHeight + deltaY;
                    if (newHeight >= minHeight) {
                        stage.setHeight(newHeight);
                    }
                } else if (cursorEvent == Cursor.N_RESIZE || cursorEvent == Cursor.NW_RESIZE || cursorEvent == Cursor.NE_RESIZE) {
                    double newHeight = startStageHeight - deltaY;
                    if (newHeight >= minHeight) {
                        stage.setY(startStageY + deltaY);
                        stage.setHeight(newHeight);
                    }
                }
            }
        }
    }
}
