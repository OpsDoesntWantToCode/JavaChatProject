package user;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Man hinh Dang ky tai khoan - UserRegisterFrame
 * Phong cach: 100% DISCORD AUTHENTIC
 * Nang cap Micro-interactions: Password Strength Bar (Realtime Animated), Shake on Error, Drop Shadow, Loading Button
 */
public class UserRegisterFrame extends JFrame {

    private JFrame parentLoginFrame;

    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtDob;
    private JTextField txtAddress;
    private JComboBox<String> cmbGender;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegister;

    // Thanh do do manh mat khau
    private JProgressBar pbarStrength;
    private JLabel lblStrengthText;
    private Timer strengthAnimTimer;
    private int currentProgress = 0;

    private UITheme.ShadowCardPanel cardPanel;

    public UserRegisterFrame() {
        this(null);
    }

    public UserRegisterFrame(JFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;
        initUI();
    }

    private void initUI() {
        setTitle("Tạo tài khoản — Java Chat System");
        setSize(560, 680);
        setMinimumSize(new Dimension(520, 640));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Nen Ambient Radial Gradient dong bo
        UITheme.AmbientBackgroundPanel outerPanel = new UITheme.AmbientBackgroundPanel(new GridBagLayout());
        outerPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 2. The Card trung tam voi Drop Shadow bo goc (Elevation)
        cardPanel = new UITheme.ShadowCardPanel(new BorderLayout(0, 16), 24);
        cardPanel.setPreferredSize(new Dimension(490, 600));

        // Header
        JLabel lblTitle = new JLabel("Tạo một tài khoản", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADER_TITLE);
        lblTitle.setForeground(UITheme.DISCORD_TEXT_HEADER);

        // Form GridBagLayout: 8 truong can doi tuyet doi
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- HANG 0: Label Username | Label FullName ---
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 6, 8);
        formGrid.add(UITheme.createDiscordFieldLabel("Tên đăng nhập", true), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 6, 0);
        formGrid.add(UITheme.createDiscordFieldLabel("Họ và tên", true), gbc);

        // --- HANG 1: txtUsername | txtFullName ---
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 12, 8);
        txtUsername = new JTextField("tuanminh_2026");
        UITheme.applyDiscordInputStyle(txtUsername, "minh_tran");
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtUsername);
            }
        });
        formGrid.add(txtUsername, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 12, 0);
        txtFullName = new JTextField("Trần Nguyễn Tuấn Minh");
        UITheme.applyDiscordInputStyle(txtFullName, "Nhập họ tên");
        txtFullName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtFullName);
            }
        });
        formGrid.add(txtFullName, gbc);

        // --- HANG 2: Label Email | Label Ngay sinh ---
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 6, 8);
        formGrid.add(UITheme.createDiscordFieldLabel("Email", true), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 6, 0);
        formGrid.add(UITheme.createDiscordFieldLabel("Ngày sinh", true), gbc);

        // --- HANG 3: txtEmail | txtDob ---
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 12, 8);
        txtEmail = new JTextField("tuanminh@example.com");
        UITheme.applyDiscordInputStyle(txtEmail, "email@domain.com");
        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtEmail);
            }
        });
        formGrid.add(txtEmail, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 12, 0);
        txtDob = new JTextField("2004-10-24");
        UITheme.applyDiscordInputStyle(txtDob, "YYYY-MM-DD (vd: 2004-10-24)");
        txtDob.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtDob);
            }
        });
        formGrid.add(txtDob, gbc);

        // --- HANG 4: Label Dia chi | Label Gioi tinh ---
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 6, 8);
        formGrid.add(UITheme.createDiscordFieldLabel("Địa chỉ", true), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 6, 0);
        formGrid.add(UITheme.createDiscordFieldLabel("Giới tính", true), gbc);

        // --- HANG 5: txtAddress | cmbGender (Chung row Y=5, thang hang ngang tuyet doi) ---
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 12, 8);
        txtAddress = new JTextField("227 Nguyễn Văn Cừ, Q.5");
        UITheme.applyDiscordInputStyle(txtAddress, "Địa chỉ cư trú");
        txtAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtAddress);
            }
        });
        formGrid.add(txtAddress, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 12, 0);
        cmbGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        UITheme.applyDiscordComboStyle(cmbGender);
        formGrid.add(cmbGender, gbc);

        // --- HANG 6: Label Mat khau | Label Xac nhan mat khau ---
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 6, 8);
        formGrid.add(UITheme.createDiscordFieldLabel("Mật khẩu", true), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 6, 0);
        formGrid.add(UITheme.createDiscordFieldLabel("Xác nhận mật khẩu", true), gbc);

        // --- HANG 7: txtPassword | txtConfirmPassword ---
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 4, 8); // Chừa 4px cho thanh Password Strength ngay duoi
        txtPassword = new JPasswordField("Password@123");
        UITheme.applyDiscordPasswordStyle(txtPassword, "Tối thiểu 6 ký tự");
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtPassword);
            }
        });
        formGrid.add(txtPassword, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 4, 0);
        txtConfirmPassword = new JPasswordField("Password@123");
        UITheme.applyDiscordPasswordStyle(txtConfirmPassword, "Nhập lại mật khẩu");
        txtConfirmPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                UITheme.clearErrorOutline(txtConfirmPassword);
            }
        });
        formGrid.add(txtConfirmPassword, gbc);

        // --- HANG 8: Password Strength Bar (Chieu cao 4px, tu dong animate & doi mau) ---
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 4, 8);

        JPanel pnlStrength = new JPanel(new BorderLayout(0, 2));
        pnlStrength.setOpaque(false);

        pbarStrength = new JProgressBar(0, 100);
        pbarStrength.setPreferredSize(new Dimension(0, 4));
        pbarStrength.setValue(75);
        pbarStrength.setForeground(UITheme.DISCORD_ONLINE);
        pbarStrength.setBackground(new Color(0x23, 0x25, 0x2B));
        pbarStrength.putClientProperty(FlatClientProperties.STYLE, "arc: 4;");

        lblStrengthText = new JLabel("Độ mạnh mật khẩu: Tốt");
        lblStrengthText.setFont(UITheme.FONT_MICRO_HINT);
        lblStrengthText.setForeground(UITheme.DISCORD_ONLINE);

        pnlStrength.add(pbarStrength, BorderLayout.NORTH);
        pnlStrength.add(lblStrengthText, BorderLayout.SOUTH);
        formGrid.add(pnlStrength, gbc);

        // Lang nghe su kien nhap mat khau de animate thanh do manh
        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateStrength(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateStrength(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });

        // --- HANG 9: Nut Dang ky tai khoan (Keo gian ca 2 cot) ---
        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 12, 0);
        btnRegister = UITheme.createDiscordPrimaryButton("Đăng ký tài khoản");
        btnRegister.addActionListener(e -> handleRegisterAction());
        formGrid.add(btnRegister, gbc);

        // --- HANG 10: Footer Link Dang nhap ---
        gbc.gridy = 10;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);

        JLabel lblHasAccount = new JLabel("Đã có tài khoản? ");
        lblHasAccount.setFont(UITheme.FONT_LINK_TEXT);
        lblHasAccount.setForeground(UITheme.DISCORD_TEXT_MUTED);

        JLabel lblLoginLink = new JLabel("Đăng nhập");
        lblLoginLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLoginLink.setForeground(UITheme.DISCORD_LINK);
        lblLoginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLoginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                transitionToLogin();
            }
        });

        footer.add(lblHasAccount);
        footer.add(lblLoginLink);
        formGrid.add(footer, gbc);

        cardPanel.add(lblTitle, BorderLayout.NORTH);
        cardPanel.add(formGrid, BorderLayout.CENTER);

        outerPanel.add(cardPanel);
        setContentPane(outerPanel);
    }

    /**
     * Tinh toan va Animate thanh Password Strength muot ma 60fps
     */
    private void updateStrength() {
        String pass = new String(txtPassword.getPassword());
        int targetScore = UITheme.calculatePasswordStrength(pass);
        Color targetColor;
        String text;

        if (pass.isEmpty()) {
            targetColor = UITheme.DISCORD_DANGER;
            text = "Vui lòng nhập mật khẩu";
        } else if (targetScore < 40) {
            targetColor = UITheme.DISCORD_DANGER;
            text = "Độ mạnh: Yếu (Cần thêm số/chữ hoa)";
        } else if (targetScore < 75) {
            targetColor = UITheme.DISCORD_WARNING;
            text = "Độ mạnh: Trung bình";
        } else {
            targetColor = UITheme.DISCORD_ONLINE;
            text = "Độ mạnh: Rất an toàn ✓";
        }

        lblStrengthText.setText(text);
        lblStrengthText.setForeground(targetColor);
        pbarStrength.setForeground(targetColor);

        // Animate thanh tien trinh 60fps
        if (strengthAnimTimer != null && strengthAnimTimer.isRunning()) {
            strengthAnimTimer.stop();
        }

        strengthAnimTimer = new Timer(16, evt -> {
            if (Math.abs(currentProgress - targetScore) <= 2) {
                currentProgress = targetScore;
                pbarStrength.setValue(currentProgress);
                ((Timer) evt.getSource()).stop();
                return;
            }
            currentProgress += (targetScore > currentProgress) ? 3 : -3;
            pbarStrength.setValue(currentProgress);
        });
        strengthAnimTimer.start();
    }

    /**
     * Xu ly Dang ky voi Shake on Error va Loading Button
     */
    private void handleRegisterAction() {
        String user = txtUsername.getText().trim();
        String name = txtFullName.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (user.isEmpty()) {
            UITheme.shakeComponent(txtUsername);
            txtUsername.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            UITheme.shakeComponent(txtFullName);
            txtFullName.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            UITheme.shakeComponent(txtPassword);
            txtPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            UITheme.shakeComponent(txtConfirmPassword);
            txtConfirmPassword.requestFocus();
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi xác nhận", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kich hoat trang thai Loading
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang tạo tài khoản...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Timer delayTimer = new Timer(700, evt -> {
            ((Timer) evt.getSource()).stop();
            btnRegister.setEnabled(true);
            btnRegister.setText("Đăng ký tài khoản");
            setCursor(Cursor.getDefaultCursor());

            JOptionPane.showMessageDialog(this,
                "✓ Đăng ký tài khoản thành công cho: " + name + " (" + user + ")\n(Micro-interactions & Password Strength hoàn tất)",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            transitionToLogin();
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    /**
     * Hieu ung Smooth Transition (180ms) quay ve man hinh Dang nhap
     */
    private void transitionToLogin() {
        final long startTime = System.currentTimeMillis();
        final int duration = 180; // ms
        final Point origPos = cardPanel.getLocation();

        Timer transTimer = new Timer(16, null);
        transTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {
                transTimer.stop();
                cardPanel.setLocation(origPos);
                if (parentLoginFrame != null) {
                    parentLoginFrame.setVisible(true);
                } else {
                    new UserLoginFrame().setVisible(true);
                }
                dispose();
                return;
            }

            double progress = (double) elapsed / duration;
            int offsetY = (int) (progress * 15);
            cardPanel.setLocation(origPos.x, origPos.y + offsetY);
        });
        transTimer.start();
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            UserRegisterFrame frame = new UserRegisterFrame();
            frame.setVisible(true);
        });
    }
}
