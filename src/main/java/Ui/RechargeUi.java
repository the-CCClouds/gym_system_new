package Ui;

import entity.Member;
import service.ShopService;
import service.MemberService;
import service.ServiceResult;
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class RechargeUi extends JFrame {

    private MemberService memberService;
    private ShopService shopService;
    private Member currentMember;

    // 组件
    private JTextField searchField;
    private JLabel infoLabel;
    private JLabel balanceLabel;
    private JTextField amountField;
    private JButton confirmBtn;

    public RechargeUi() {
        this.memberService = new MemberService();
        this.shopService = new ShopService();

        // 1. 应用全局主题
        StyleUtils.initGlobalTheme();

        setTitle("💰 会员余额充值");
        setSize(600, 500); // 稍微加宽加高
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null); // 保持 absolute layout 以匹配原逻辑结构

        initView();
        setVisible(true);
    }

    private void initView() {
        // === 1. 顶部搜索区 ===
        JPanel searchPanel = new JPanel(null);
        searchPanel.setBounds(20, 20, 545, 100);
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(searchPanel);

        JLabel lbl1 = new JLabel("🔍 第一步：搜索会员");
        lbl1.setFont(StyleUtils.FONT_BOLD);
        lbl1.setForeground(StyleUtils.COLOR_PRIMARY);
        lbl1.setBounds(20, 15, 300, 20);
        searchPanel.add(lbl1);

        searchField = new JTextField();
        searchField.setBounds(20, 45, 380, 40);
        StyleUtils.styleTextField(searchField);
        // 回车搜索
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) searchMember();
            }
        });
        searchPanel.add(searchField);

        JButton searchBtn = new JButton("查找");
        searchBtn.setBounds(410, 45, 115, 40);
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchMember());
        searchPanel.add(searchBtn);

        // === 2. 会员信息展示区 (卡片风格) ===
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(20, 140, 545, 100);
        infoPanel.setBackground(new Color(240, 248, 255)); // 淡蓝背景
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(176, 224, 230), 1));
        add(infoPanel);

        infoLabel = new JLabel("姓名：-   |   手机：-");
        infoLabel.setFont(StyleUtils.FONT_NORMAL);
        infoLabel.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        infoLabel.setBounds(20, 25, 500, 25);
        infoPanel.add(infoLabel);

        balanceLabel = new JLabel("当前余额：¥ 0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 22));
        balanceLabel.setForeground(StyleUtils.COLOR_SUCCESS); // 绿色金额
        balanceLabel.setBounds(20, 55, 500, 30);
        infoPanel.add(balanceLabel);

        // === 3. 充值操作区 ===
        JPanel rechargePanel = new JPanel(null);
        rechargePanel.setBounds(20, 260, 545, 180);
        rechargePanel.setBackground(Color.WHITE);
        rechargePanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(rechargePanel);

        JLabel lbl2 = new JLabel("💰 第二步：充值金额");
        lbl2.setFont(StyleUtils.FONT_BOLD);
        lbl2.setForeground(StyleUtils.COLOR_WARNING);
        lbl2.setBounds(20, 15, 200, 20);
        rechargePanel.add(lbl2);

        amountField = new JTextField();
        amountField.setBounds(20, 45, 380, 45);
        amountField.setFont(new Font("Arial", Font.BOLD, 20)); // 大字号
        StyleUtils.styleTextField(amountField);
        rechargePanel.add(amountField);

        confirmBtn = new JButton("确认充值");
        confirmBtn.setBounds(410, 45, 115, 45);
        StyleUtils.styleButton(confirmBtn, StyleUtils.COLOR_SUCCESS);
        confirmBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        confirmBtn.setEnabled(false); // 默认禁用
        confirmBtn.addActionListener(e -> performRecharge());
        rechargePanel.add(confirmBtn);

        // 快捷金额按钮
        addQuickBtn(rechargePanel, "¥ 100", 20, 110);
        addQuickBtn(rechargePanel, "¥ 500", 110, 110);
        addQuickBtn(rechargePanel, "¥ 1000", 200, 110);

        JLabel tipLbl = new JLabel("<html><font color='gray'>* 支持现金/扫码收款后录入</font></html>");
        tipLbl.setBounds(350, 110, 200, 30);
        rechargePanel.add(tipLbl);
    }

    private void addQuickBtn(JPanel panel, String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 80, 35);
        btn.setFont(StyleUtils.FONT_NORMAL);
        btn.setBackground(new Color(245, 245, 245));
        btn.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 点击填充金额
        btn.addActionListener(e -> amountField.setText(text.replace("¥ ", "")));
        panel.add(btn);
    }

    private void searchMember() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键字！");
            return;
        }

        List<Member> list = memberService.search(keyword);
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到会员！");
            resetInfo();
        } else {
            // 默认选第一个
            currentMember = list.get(0);
            infoLabel.setText("姓名：" + currentMember.getName() + "   |   手机：" + currentMember.getPhone());
            balanceLabel.setText("当前余额：¥ " + String.format("%,.2f", currentMember.getBalance()));
            confirmBtn.setEnabled(true);

            // 自动聚焦到金额框，方便直接输入
            amountField.requestFocus();

            if (list.size() > 1) {
                // 如果有多个结果，这里简单提示，也可以像 CheckInUi 那样弹窗选
                JOptionPane.showMessageDialog(this, "找到多个结果，已自动加载第一个。\n(" + currentMember.getName() + ")");
            }
        }
    }

    private void resetInfo() {
        currentMember = null;
        infoLabel.setText("姓名：-   |   手机：-");
        balanceLabel.setText("当前余额：¥ 0.00");
        confirmBtn.setEnabled(false);
        amountField.setText("");
    }

    private void performRecharge() {
        if (currentMember == null) return;

        String amountStr = amountField.getText().trim();
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "充值金额必须大于0！");
                return;
            }
            if (amount > 100000) {
                JOptionPane.showMessageDialog(this, "单次充值金额过大，请确认输入是否正确！");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "会员：" + currentMember.getName() + "\n" +
                            "充值金额：¥ " + String.format("%,.2f", amount) + "\n\n" +
                            "确认立即充值吗？",
                    "充值确认", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // 调用 ShopService 的充值方法 (保持原逻辑)
                ServiceResult<Void> result = shopService.recharge(currentMember.getId(), amount);

                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "✅ " + result.getMessage());
                    // 充值成功后，重新搜索刷新余额显示
                    searchMember();
                    amountField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ " + result.getMessage());
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的金额数字！");
        }
    }
}