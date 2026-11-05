package user;

/**
 * Lớp khởi chạy ứng dụng (Launcher) không kế thừa javafx.application.Application.
 * Giúp khởi chạy JavaFX trực tiếp từ Classpath trong VS Code / IDE mà không bị lỗi:
 * "Error: JavaFX runtime components are missing, and are required to run this application".
 */
public class UserLauncher {
    public static void main(String[] args) {
        // Kích hoạt DirectWrite ClearType Subpixel LCD Text Rendering chống bể/răng cưa chữ trên Windows High-DPI
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "native");
        UserLoginApp.main(args);
    }
}

