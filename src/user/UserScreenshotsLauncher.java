package user;

/**
 * Lớp khởi chạy UserScreenshotsRunner không kế thừa Application.
 * Giúp khởi chạy JavaFX trực tiếp từ Classpath mà không bị lỗi JavaFX runtime components are missing.
 */
public class UserScreenshotsLauncher {
    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "native");
        UserScreenshotsRunner.main(args);
    }
}
