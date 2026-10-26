package user;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * He thong mau sac, hieu ung Micro-interactions va phong cach thiet ke DISCORD AUTHENTIC
 * Ho tro: Radial Gradient Canvas, Shadow Card Elevation, Shake on Error (60fps), va Password Strength Bar
 */
public final class UITheme {

    // 1. Bang mau chuan Discord Dark Theme
    public static final Color DISCORD_BG_CANVAS   = new Color(0x2B, 0x2D, 0x31);
    public static final Color DISCORD_BG_CARD     = new Color(0x31, 0x33, 0x38);
    public static final Color DISCORD_BG_INPUT    = new Color(0x1E, 0x1F, 0x22);
    public static final Color DISCORD_BORDER_DEF  = new Color(0x1E, 0x1F, 0x22);

    // 2. Mau nhan, Trang thai & Focus Indicator
    public static final Color DISCORD_BLURPLE     = new Color(0x58, 0x65, 0xF2);
    public static final Color DISCORD_BLURPLE_HOV = new Color(0x47, 0x52, 0xC4);
    public static final Color DISCORD_BLURPLE_ACT = new Color(0x3C, 0x45, 0xA5);
    public static final Color DISCORD_LINK        = new Color(0x00, 0xA8, 0xFC);
    public static final Color DISCORD_DANGER      = new Color(0xFA, 0x77, 0x7C); // Mau do loi #FA777C
    public static final Color DISCORD_WARNING     = new Color(0xF0, 0xB2, 0x32); // Mau vang can bao
    public static final Color DISCORD_ONLINE      = new Color(0x23, 0xA5, 0x5A); // Mau xanh Discord

    // 3. Mau chu (Typography)
    public static final Color DISCORD_TEXT_HEADER = new Color(0xF2, 0xF3, 0xF5);
    public static final Color DISCORD_TEXT_NORMAL = new Color(0xDB, 0xDE, 0xE1);
    public static final Color DISCORD_TEXT_MUTED  = new Color(0x94, 0x9B, 0xA4);
    public static final Color DISCORD_TEXT_LABEL  = new Color(0xB5, 0xBA, 0xC1);

    // 4. Font chu chuan Discord
    public static final Font FONT_HEADER_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_SUB      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_INPUT_LABEL     = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_INPUT_TEXT      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BTN_TEXT        = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_LINK_TEXT       = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MICRO_HINT      = new Font("Segoe UI", Font.BOLD, 10);

    private UITheme() {}

    /**
     * Panel Nen Canvas co chieu sau voi Radial Gradient pha anh Blurple Discord sieu nhe o goc trai tren
     */
    public static class AmbientBackgroundPanel extends JPanel {
        public AmbientBackgroundPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Radial Gradient: Tam phat quang Blurple nhe o (w * 0.2, h * 0.15) lan toa ra nen xam toi
            Point2D center = new Point2D.Float(w * 0.25f, h * 0.15f);
            float radius = Math.max(w, h) * 0.95f;
            float[] dist = {0.0f, 0.45f, 1.0f};
            Color[] colors = {
                new Color(0x38, 0x3F, 0x7E), // Sac tim xanh Discord Blurple an nhe o tam
                new Color(0x23, 0x25, 0x2B), // Xam trung gian
                new Color(0x18, 0x19, 0x1C)  // Xam toi nhat o vien ngoai
            };

            RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
            g2.setPaint(p);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Khung Card Elevation co Drop Shadow bo goc tao chieu sau noi bat tren nen Canvas
     */
    public static class ShadowCardPanel extends JPanel {
        private final int arc = 10;
        private final int shadowSize = 8;

        public ShadowCardPanel(LayoutManager layout, int internalPadding) {
            super(layout);
            setOpaque(false);
            // Padding bao gom khoang cach shadow de tranh component de len bong do
            setBorder(new EmptyBorder(internalPadding + shadowSize, internalPadding + shadowSize,
                                      internalPadding + shadowSize, internalPadding + shadowSize));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = shadowSize;
            int y = shadowSize;
            int w = getWidth() - (shadowSize * 2);
            int h = getHeight() - (shadowSize * 2);

            // 1. Ve Drop Shadow nhieu lop (Multi-layer soft shadow)
            for (int i = shadowSize; i >= 1; i--) {
                int alpha = (int) (18.0 * (1.0 - (double) i / shadowSize));
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fill(new RoundRectangle2D.Float(x - i, y - i + 2, w + (i * 2), h + (i * 2), arc + i, arc + i));
            }

            // 2. Ve be mat Card chinh (#313338)
            g2.setColor(DISCORD_BG_CARD);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, arc, arc));

            // 3. Vien mong tinh te (#3E4047)
            g2.setColor(new Color(0x3E, 0x40, 0x47));
            g2.draw(new RoundRectangle2D.Float(x, y, w, h, arc, arc));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Hieu ung Horizontal Shake khi xay ra loi validation
     * Dao dong ±8px giam dan trong 250ms o tan so 60fps (16ms/frame)
     */
    public static void shakeComponent(JComponent comp) {
        final Point originalPos = comp.getLocation();
        final long startTime = System.currentTimeMillis();
        final int duration = 250; // ms
        final int maxAmplitude = 8; // px

        // Doi vien sang mau do bao loi #FA777C
        comp.putClientProperty(FlatClientProperties.OUTLINE, "error");

        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {
                comp.setLocation(originalPos);
                timer.stop();
                return;
            }

            double progress = (double) elapsed / duration;
            double decay = 1.0 - progress; // Bien do giam dan
            // Dao dong 3 chu ky song sin
            double offset = Math.sin(progress * Math.PI * 6) * maxAmplitude * decay;
            comp.setLocation(originalPos.x + (int) Math.round(offset), originalPos.y);
        });
        timer.start();
    }

    /**
     * Xoa vien loi khi nguoi dung bat dau go lai vao o nhap
     */
    public static void clearErrorOutline(JComponent comp) {
        comp.putClientProperty(FlatClientProperties.OUTLINE, null);
    }

    /**
     * Dinh dang o nhap lieu JTextField dong bo theo chuan Discord
     */
    public static void applyDiscordInputStyle(JTextField field, String placeholder) {
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, ""
            + "arc: 6;"
            + "background: #1E1F22;"
            + "foreground: #F2F3F5;"
            + "borderColor: #1E1F22;"
            + "focusedBorderColor: #5865F2;"
            + "margin: 6,10,6,10;"
            + "placeholderForeground: #80848E;"
        );
        field.setFont(FONT_INPUT_TEXT);
        field.setCaretColor(Color.WHITE);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
        field.setMinimumSize(new Dimension(50, 38));
    }

    /**
     * Dinh dang o nhap mat khau JPasswordField dong bo theo chuan Discord
     * Co Right Padding (8px) cho icon con mat
     */
    public static void applyDiscordPasswordStyle(JPasswordField field, String placeholder) {
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, ""
            + "arc: 6;"
            + "background: #1E1F22;"
            + "foreground: #F2F3F5;"
            + "borderColor: #1E1F22;"
            + "focusedBorderColor: #5865F2;"
            + "margin: 6,10,6,6;"
            + "padding: 0,0,0,8;"
            + "showRevealButton: true;"
            + "placeholderForeground: #80848E;"
        );
        field.setFont(FONT_INPUT_TEXT);
        field.setCaretColor(Color.WHITE);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
        field.setMinimumSize(new Dimension(50, 38));
    }

    /**
     * Dinh dang JComboBox dong bo hoan toan goc bo arc: 6, chieu cao 38px
     */
    public static void applyDiscordComboStyle(JComboBox<?> combo) {
        combo.putClientProperty(FlatClientProperties.STYLE, ""
            + "arc: 6;"
            + "background: #1E1F22;"
            + "foreground: #F2F3F5;"
            + "borderColor: #1E1F22;"
            + "focusedBorderColor: #5865F2;"
            + "buttonBackground: #1E1F22;"
            + "padding: 0,6,0,6;"
        );
        combo.setFont(FONT_INPUT_TEXT);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 38));
        combo.setMinimumSize(new Dimension(50, 38));
    }

    /**
     * Tao nut bam Discord Blurple chuan voi hieu ung Hover va Pressed tu dong
     */
    public static JButton createDiscordPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.putClientProperty(FlatClientProperties.STYLE, ""
            + "arc: 6;"
            + "background: #5865F2;"
            + "hoverBackground: #4752C4;"
            + "pressedBackground: #3C45A5;"
            + "foreground: #FFFFFF;"
            + "borderWidth: 0;"
            + "font: bold 14 Segoe UI;"
        );
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 42));
        btn.setMinimumSize(new Dimension(50, 42));
        return btn;
    }

    /**
     * Tao nhan tieu de cho tung o nhap lieu co dau sao do bat buoc
     */
    public static JPanel createDiscordFieldLabel(String labelText, boolean isRequired) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setOpaque(false);

        JLabel lblText = new JLabel(labelText.toUpperCase());
        lblText.setFont(FONT_INPUT_LABEL);
        lblText.setForeground(DISCORD_TEXT_LABEL);
        pnl.add(lblText);

        if (isRequired) {
            JLabel lblStar = new JLabel(" *");
            lblStar.setFont(FONT_INPUT_LABEL);
            lblStar.setForeground(DISCORD_DANGER);
            pnl.add(lblStar);
        }
        return pnl;
    }

    /**
     * Tinh toan do manh mat khau va tra ve % tuong ung (0 - 100)
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        if (password.length() >= 6) score += 25;
        if (password.length() >= 9) score += 20;
        if (password.matches(".*[A-Z].*")) score += 20;
        if (password.matches(".*[0-9].*")) score += 20;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score += 15;
        return Math.min(score, 100);
    }
}
