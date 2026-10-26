package user;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Man hinh Dang nhap - UserLoginFrame
 * Phong cach: 100% DISCORD AUTHENTIC
 * Nang cap Micro-interactions & Visual Depth (Ambient Radial Gradient, Shadow Elevation, Shake on Error, Loading Button)
 */
public class UserLoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private UITheme.ShadowCardPanel cardPanel;

    public UserLoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Đăng nhập — Java Chat System");
        setSize(480, 520);
        setMinimumSize(new Dimension(450, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Nen Ambient Radial Gradient pha anh Blurple Discord sieu nhe o goc trai tren
        UITheme.AmbientBackgroundPanel outerPanel = new UITheme.AmbientBackgroundPanel(new GridBagLayout());
        outerPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 2. The Card chinh giua voi Drop Shadow bo goc tao chieu sau (Elevation)
        cardPanel = new UITheme.ShadowCardPanel(new BorderLayout(0, 20), 24);
        cardPanel.setPreferredSize(new Dimension(420, 430));

        // 3. Header: Can giua 100% be ngang cua Card
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Chào mừng trở lại!", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADER_TITLE);
        lblTitle.setForeground(UITheme.DISCORD_TEXT_HEADER);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Rất vui được gặp lại bạn một lần nữa!", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_HEADER_SUB);
        lblSub.setForeground(UITheme.DISCORD_TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(lblSub);

        // 4. Form Nhap lieu: GridBagLayout Fill 100% chieu ngang
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Hang 0: Label Username
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        JPanel pnlUserLabel = UITheme.createDiscordFieldLabel("Tên đăng nhập hoặc Email", true);
        formPanel.add(pnlUserLabel, gbc);

        // Hang 1: O nhap Username
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        txtUsername = new JTextField("tuanminh_2026");
        UITheme.applyDiscordInputStyle(txtUsername, "Nhập username hoặc email của bạn...");
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtUsername);
            }
        });
        formPanel.add(txtUsername, gbc);

        // Hang 2: Label Password
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        JPanel pnlPassLabel = UITheme.createDiscordFieldLabel("Mật khẩu", true);
        formPanel.add(pnlPassLabel, gbc);

        // Hang 3: O nhap Password
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        txtPassword = new JPasswordField("Password@123");
        UITheme.applyDiscordPasswordStyle(txtPassword, "Nhập mật khẩu...");
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtPassword);
            }
        });
        formPanel.add(txtPassword, gbc);

        // Hang 4: Link Quen mat khau
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        JLabel lblForgot = new JLabel("Quên mật khẩu?");
        lblForgot.setFont(UITheme.FONT_LINK_TEXT);
        lblForgot.setForeground(UITheme.DISCORD_LINK);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(UserLoginFrame.this,
                    "Chức năng Quên mật khẩu sẽ gửi mã OTP về Email đăng ký (Task U2).",
                    "Quên mật khẩu", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        formPanel.add(lblForgot, gbc);

        // Hang 5: Nut Dang nhap Blurple voi trang thai Loading
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 14, 0);
        btnLogin = UITheme.createDiscordPrimaryButton("Đăng nhập");
        btnLogin.addActionListener(e -> handleLoginAction());
        formPanel.add(btnLogin, gbc);

        // Hang 6: Footer Link Dang ky
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);

        JLabel lblNeedAccount = new JLabel("Cần một tài khoản? ");
        lblNeedAccount.setFont(UITheme.FONT_LINK_TEXT);
        lblNeedAccount.setForeground(UITheme.DISCORD_TEXT_MUTED);

        JLabel lblRegister = new JLabel("Đăng ký");
        lblRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRegister.setForeground(UITheme.DISCORD_LINK);
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                transitionToRegister();
            }
        });

        footer.add(lblNeedAccount);
        footer.add(lblRegister);
        formPanel.add(footer, gbc);

        cardPanel.add(headerPanel, BorderLayout.NORTH);
        cardPanel.add(formPanel, BorderLayout.CENTER);

        outerPanel.add(cardPanel);
        setContentPane(outerPanel);
    }

    /**
     * Xu ly Dang nhap voi Horizontal Shake on Error va Loading Button
     */
    private void handleLoginAction() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        // 1. Kiem tra Validate loi -> Rung lac ngang (Horizontal Shake 60fps) + Doi vien sang mau do #FA777C
        if (user.isEmpty()) {
            UITheme.shakeComponent(txtUsername);
            txtUsername.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            UITheme.shakeComponent(txtPassword);
            txtPassword.requestFocus();
            return;
        }

        // 2. Kich hoat trang thai Loading tren Nut CTA (Vo hieu hoa nut va hien hieu ung cho)
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Gia lap do tre mang 600ms bang Timer muot ma
        Timer delayTimer = new Timer(600, evt -> {
            ((Timer) evt.getSource()).stop();
            btnLogin.setEnabled(true);
            btnLogin.setText("Đăng nhập");
            setCursor(Cursor.getDefaultCursor());

            JOptionPane.showMessageDialog(this,
                "✓ Đăng nhập thành công với tài khoản: " + user + "\n(Micro-interactions & Visual Depth hoàn thành)",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    /**
     * Hieu ung Smooth View Transition (Slide/Fade nhe 180ms) sang man hinh Dang ky
     */
    private void transitionToRegister() {
        final long startTime = System.currentTimeMillis();
        final int duration = 180; // ms
        final Point origPos = cardPanel.getLocation();

        Timer transTimer = new Timer(16, null);
        transTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {
                transTimer.stop();
                cardPanel.setLocation(origPos);
                // Mo man hinh Dang ky
                UserRegisterFrame regFrame = new UserRegisterFrame(UserLoginFrame.this);
                regFrame.setVisible(true);
                UserLoginFrame.this.setVisible(false);
                return;
            }

            double progress = (double) elapsed / duration;
            // Truot nhe xuong duoi 15px truoc khi bien mat
            int offsetY = (int) (progress * 15);
            cardPanel.setLocation(origPos.x, origPos.y + offsetY);
        });
        transTimer.start();
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            UserLoginFrame frame = new UserLoginFrame();
            frame.setVisible(true);
        });
    }
}
