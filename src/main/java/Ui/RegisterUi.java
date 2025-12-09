package Ui;

import com.toedter.calendar.JDateChooser;
import entity.Member;
import service.MemberService;
import service.UserService;
import service.ServiceResult; // 确保导入
import utils.LoginUtils;
import utils.StyleUtils; // 引入样式工具类

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

public class RegisterUi extends JFrame {

    // 输入组件
    private JTextField userField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> genderBox;
    private JDateChooser birthdayChooser;
    private JTextField codeField;
    private JLabel codeImageLbl; // 显示验证码文本

    // 验证码数据
    private String currentCode;

    public void RegisterJFrame() {
        // 1. 初始化主题
        StyleUtils.initGlobalTheme();

        this.setSize(550, 750); // 注册项多，窗口高一点
        this.setTitle("💪 健身房管理系统 - 新用户注册");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.getContentPane().setBackground(StyleUtils.COLOR_BG); // 淡灰背景

        initView();
        refreshCode(); // 初始化验证码

        this.setVisible(true);
    }

    private void initView() {
        // === 1. 顶部标题区 ===
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 550, 80);
        headerPanel.setBackground(Color.WHITE);
        this.add(headerPanel);

        JLabel titleLbl = new JLabel("📝 注册新账号");
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_PRIMARY);
        titleLbl.setBounds(40, 20, 300, 40);
        headerPanel.add(titleLbl);

        JLabel subLbl = new JLabel("加入我们，开启健康生活");
        subLbl.setFont(StyleUtils.FONT_NORMAL);
        subLbl.setForeground(StyleUtils.COLOR_INFO);
        subLbl.setBounds(45, 55, 300, 20);
        headerPanel.add(subLbl);

        // === 2. 表单区域 (白色卡片风格) ===
        JPanel formPanel = new JPanel(null);
        formPanel.setBounds(40, 100, 455, 580);
        formPanel.setBackground(Color.WHITE);
        // 简单的阴影效果可以通过边框模拟，或者直接纯白背景
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        this.add(formPanel);

        int x = 40, y = 30;
        int w = 375, h = 40; // 输入框高度
        int gap = 65; // 垂直间距

        // 用户名
        addLabel(formPanel, "用户名", x, y);
        userField = new JTextField();
        userField.setBounds(x, y + 25, w, h);
        StyleUtils.styleTextField(userField);
        formPanel.add(userField);

        // 密码
        y += gap;
        addLabel(formPanel, "登录密码", x, y);
        passField = new JPasswordField();
        passField.setBounds(x, y + 25, w, h);
        StyleUtils.styleTextField(passField);
        formPanel.add(passField);

        // 确认密码
        y += gap;
        addLabel(formPanel, "确认密码", x, y);
        confirmPassField = new JPasswordField();
        confirmPassField.setBounds(x, y + 25, w, h);
        StyleUtils.styleTextField(confirmPassField);
        formPanel.add(confirmPassField);

        // 手机号 (左) & 邮箱 (右) - 一行放两个显得紧凑
        y += gap;
        addLabel(formPanel, "手机号码", x, y);
        phoneField = new JTextField();
        phoneField.setBounds(x, y + 25, 175, h);
        StyleUtils.styleTextField(phoneField);
        formPanel.add(phoneField);

        addLabel(formPanel, "电子邮箱", x + 200, y);
        emailField = new JTextField();
        emailField.setBounds(x + 200, y + 25, 175, h);
        StyleUtils.styleTextField(emailField);
        formPanel.add(emailField);

        // 性别 & 生日
        y += gap;
        addLabel(formPanel, "性别", x, y);
        genderBox = new JComboBox<>(new String[]{"男", "女"});
        genderBox.setBounds(x, y + 25, 100, h);
        genderBox.setFont(StyleUtils.FONT_NORMAL);
        genderBox.setBackground(Color.WHITE);
        formPanel.add(genderBox);

        addLabel(formPanel, "出生日期", x + 120, y);
        birthdayChooser = new JDateChooser();
        birthdayChooser.setBounds(x + 120, y + 25, 255, h);
        birthdayChooser.setDateFormatString("yyyy-MM-dd");
        birthdayChooser.setFont(StyleUtils.FONT_NORMAL);

        // >>> 新增：调用美化方法 <<<
        styleDateChooser(birthdayChooser);

        formPanel.add(birthdayChooser);
        // 验证码
        y += gap;
        addLabel(formPanel, "验证码", x, y);
        codeField = new JTextField();
        codeField.setBounds(x, y + 25, 150, h);
        StyleUtils.styleTextField(codeField);
        formPanel.add(codeField);

        // 验证码显示 (模拟图片)
        codeImageLbl = new JLabel("ABCD");
        codeImageLbl.setBounds(x + 170, y + 25, 100, h);
        codeImageLbl.setOpaque(true);
        codeImageLbl.setBackground(new Color(240, 248, 255));
        codeImageLbl.setFont(new Font("Monospaced", Font.BOLD | Font.ITALIC, 24));
        codeImageLbl.setForeground(Color.BLUE);
        codeImageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        codeImageLbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        codeImageLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        codeImageLbl.setToolTipText("点击刷新验证码");
        codeImageLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshCode();
            }
        });
        formPanel.add(codeImageLbl);

        // 注册按钮
        y += gap + 10;
        JButton registerBtn = new JButton("立即注册");
        registerBtn.setBounds(x, y, w, 50);
        StyleUtils.styleButton(registerBtn, StyleUtils.COLOR_PRIMARY);
        registerBtn.setFont(new Font("微软雅黑", Font.BOLD, 18));
        registerBtn.addActionListener(e -> performRegister());
        formPanel.add(registerBtn);

        // 返回登录链接
        JLabel backLabel = new JLabel("<html>已有账号？<u style='color:#409EFF'>返回登录</u></html>");
        backLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backLabel.setFont(StyleUtils.FONT_NORMAL);
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.setBounds(x, y + 60, w, 30);
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginUi().LoginJFrame();
            }
        });
        formPanel.add(backLabel);
    }

    /**
     * 专门用于美化 JDateChooser 的“整容手术”方法
     */
    private void styleDateChooser(JDateChooser dateChooser) {
        // 1. 获取并美化内部的文本输入框
        JTextField dateEditor = (JTextField) dateChooser.getDateEditor().getUiComponent();
        StyleUtils.styleTextField(dateEditor); // 应用统一的输入框样式
        dateEditor.setBorder(null); // 去掉多余边框，让它融入背景

        // 2. 遍历组件找到那个丑丑的按钮，把它变漂亮
        for (Component comp : dateChooser.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;

                // 移除自带的像素图标
                btn.setIcon(null);

                // 换成高清 Emoji 图标
                btn.setText("📅");
                // 稍微调大字体让 Emoji 居中
                btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

                // 去掉老式按钮的凸起边框
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // 稍微调整一下背景色，让它看起来像一个可点击的图标
                btn.setBackground(Color.WHITE);
            }
        }

        // 3. 给整个控件加一个统一的边框，让它看起来像一个整体的输入框
        dateChooser.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(220, 223, 230), 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        dateChooser.setBackground(Color.WHITE);
    }

    // 辅助方法：添加小标签
    private void addLabel(JPanel panel, String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("微软雅黑", Font.BOLD, 12));
        lbl.setForeground(StyleUtils.COLOR_INFO);
        lbl.setBounds(x, y, 200, 20);
        panel.add(lbl);
    }

    private void refreshCode() {
        this.currentCode = LoginUtils.generateVerificationCode();
        codeImageLbl.setText(currentCode);
    }

    private void performRegister() {
        // 1. 获取输入
        String name = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String confirmPass = new String(confirmPassField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String genderStr = (String) genderBox.getSelectedItem();
        Date birth = birthdayChooser.getDate();
        String inputCode = codeField.getText().trim();

        // 2. 验证非空
        if (name.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() ||
                phone.isEmpty() || email.isEmpty() || birth == null || inputCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写完整的注册信息！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. 验证密码一致
        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "两次密码输入不一致！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. 验证验证码
        if (!inputCode.equalsIgnoreCase(currentCode)) {
            JOptionPane.showMessageDialog(this, "验证码错误！", "错误", JOptionPane.ERROR_MESSAGE);
            refreshCode();
            codeField.setText("");
            return;
        }

        // 5. 调用 Service
        String gender = "男".equals(genderStr) ? "male" : "female";
        MemberService memberService = new MemberService();
        MemberService.ServiceResult<Member> memResult = memberService.register(name, phone, email, gender, birth);

        if (memResult.isSuccess()) {
            // 注册用户账号
            UserService userService = new UserService();
            int memberId = memResult.getData().getId();

            UserService.ServiceResult<Void> userResult = userService.registerMemberUser(memberId, name, pass);

            if (userResult.isSuccess()) {
                JOptionPane.showMessageDialog(this, "🎉 注册成功！即将跳转登录界面。");
                this.dispose();
                new LoginUi().LoginJFrame();
            } else {
                JOptionPane.showMessageDialog(this, "会员资料创建成功，但账户创建失败：" + userResult.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "注册失败：" + memResult.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}