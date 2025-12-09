package Ui;

import service.UserService;
import utils.StyleUtils; // 导入样式

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LoginUi extends JFrame implements MouseListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel registerLabel;

    public void LoginJFrame() {
        // 1. 初始化主题
        StyleUtils.initGlobalTheme();

        this.setSize(900, 600); // 窗口做大一点，大气
        this.setTitle("💪 健身房管理系统 - 登录");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);

        // 设置整体背景色
        this.getContentPane().setBackground(StyleUtils.COLOR_BG);

        initView();

        this.setVisible(true);
    }

    private void initView() {
        // === 1. 左侧装饰图/标题区 ===
        JPanel leftPanel = new JPanel();
        leftPanel.setBounds(0, 0, 400, 600);
        leftPanel.setBackground(StyleUtils.COLOR_PRIMARY);
        leftPanel.setLayout(null);

        JLabel logoText = new JLabel("Gym System");
        logoText.setFont(new Font("Arial", Font.BOLD, 40));
        logoText.setForeground(Color.WHITE);
        logoText.setBounds(50, 200, 300, 50);
        leftPanel.add(logoText);

        JLabel subText = new JLabel("专业的健身房管理专家");
        subText.setFont(StyleUtils.FONT_NORMAL);
        subText.setForeground(new Color(255, 255, 255, 200));
        subText.setBounds(55, 260, 300, 30);
        leftPanel.add(subText);

        this.add(leftPanel);

        // === 2. 右侧登录表单区 ===
        int startX = 500;
        int startY = 120;
        int fieldW = 300;
        int fieldH = 45; // 增高输入框

        JLabel titleLbl = new JLabel("欢迎登录");
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        titleLbl.setBounds(startX, startY, 200, 40);
        this.add(titleLbl);

        // 用户名
        JLabel uLabel = new JLabel("账号 / Username");
        uLabel.setFont(StyleUtils.FONT_NORMAL);
        uLabel.setForeground(StyleUtils.COLOR_INFO);
        uLabel.setBounds(startX, startY + 60, 200, 30);
        this.add(uLabel);

        usernameField = new JTextField();
        usernameField.setBounds(startX, startY + 90, fieldW, fieldH);
        StyleUtils.styleTextField(usernameField);
        this.add(usernameField);

        // 密码
        JLabel pLabel = new JLabel("密码 / Password");
        pLabel.setFont(StyleUtils.FONT_NORMAL);
        pLabel.setForeground(StyleUtils.COLOR_INFO);
        pLabel.setBounds(startX, startY + 150, 200, 30);
        this.add(pLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(startX, startY + 180, fieldW, fieldH);
        StyleUtils.styleTextField(passwordField);
        this.add(passwordField);

        // 登录按钮
        loginButton = new JButton("立即登录");
        loginButton.setBounds(startX, startY + 260, fieldW, 50);
        StyleUtils.styleButton(loginButton, StyleUtils.COLOR_PRIMARY);
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        loginButton.addMouseListener(this);
        this.add(loginButton);

        // 注册链接
        registerLabel = new JLabel("<html><u>没有账号？点此注册会员</u></html>");
        registerLabel.setFont(StyleUtils.FONT_NORMAL);
        registerLabel.setForeground(StyleUtils.COLOR_PRIMARY);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.setBounds(startX, startY + 320, 200, 30);
        registerLabel.addMouseListener(this);
        this.add(registerLabel);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        } else if (e.getSource() == registerLabel) {
            this.dispose();
            new RegisterUi().RegisterJFrame();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名和密码");
            return;
        }

        UserService userService = new UserService();
        UserService.LoginResult result = userService.login(username, password);

        if (result.isSuccess()) {
            this.dispose();
            new MainUi(result.getUserType(), result.getUserData());
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "登录失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 空实现
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}