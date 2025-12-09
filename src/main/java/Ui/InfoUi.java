package Ui;

import entity.Member;
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;

public class InfoUi extends JFrame {

    private Member member;

    public InfoUi(Member member) {
        this.member = member;
        StyleUtils.initGlobalTheme();

        setTitle("👤 个人档案");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);

        initView();
    }

    private void initView() {
        // 白色卡片背景
        JPanel cardPanel = new JPanel(null);
        cardPanel.setBounds(20, 20, 345, 420);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(cardPanel);

        // 头像区 (模拟)
        JLabel avatarLbl = new JLabel("🤠", SwingConstants.CENTER);
        avatarLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        avatarLbl.setBounds(0, 30, 345, 80);
        cardPanel.add(avatarLbl);

        JLabel nameLbl = new JLabel(member.getName(), SwingConstants.CENTER);
        nameLbl.setFont(StyleUtils.FONT_TITLE);
        nameLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        nameLbl.setBounds(0, 110, 345, 30);
        cardPanel.add(nameLbl);

        JLabel idLbl = new JLabel("ID: " + member.getId(), SwingConstants.CENTER);
        idLbl.setFont(StyleUtils.FONT_NORMAL);
        idLbl.setForeground(StyleUtils.COLOR_INFO);
        idLbl.setBounds(0, 140, 345, 20);
        cardPanel.add(idLbl);

        // 分割线
        JSeparator sep = new JSeparator();
        sep.setBounds(40, 170, 265, 1);
        cardPanel.add(sep);

        // 信息列表
        int startY = 190;
        int gap = 35;

        addInfoRow(cardPanel, "📱 手机号:", member.getPhone(), startY);
        addInfoRow(cardPanel, "📧 邮  箱:", member.getEmail(), startY + gap);
        addInfoRow(cardPanel, "🚻 性  别:", "male".equals(member.getGender()) ? "男" : "女", startY + gap * 2);

        // 余额高亮显示
        JLabel balanceKey = new JLabel("💰 账户余额:");
        balanceKey.setFont(StyleUtils.FONT_BOLD);
        balanceKey.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        balanceKey.setBounds(50, startY + gap * 3, 100, 20);
        cardPanel.add(balanceKey);

        JLabel balanceVal = new JLabel("¥ " + member.getBalance());
        balanceVal.setFont(new Font("Arial", Font.BOLD, 16));
        balanceVal.setForeground(StyleUtils.COLOR_DANGER); // 红色金额
        balanceVal.setHorizontalAlignment(SwingConstants.RIGHT);
        balanceVal.setBounds(150, startY + gap * 3, 140, 20);
        cardPanel.add(balanceVal);

        // 关闭按钮
        JButton closeBtn = new JButton("关闭");
        StyleUtils.styleButton(closeBtn, StyleUtils.COLOR_INFO);
        closeBtn.setBounds(50, 360, 245, 40);
        closeBtn.addActionListener(e -> dispose());
        cardPanel.add(closeBtn);
    }

    private void addInfoRow(JPanel panel, String label, String value, int y) {
        JLabel k = new JLabel(label);
        k.setFont(StyleUtils.FONT_NORMAL);
        k.setForeground(StyleUtils.COLOR_INFO);
        k.setBounds(50, y, 100, 20);
        panel.add(k);

        JLabel v = new JLabel(value);
        v.setFont(StyleUtils.FONT_BOLD);
        v.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setBounds(150, y, 140, 20);
        panel.add(v);
    }
}