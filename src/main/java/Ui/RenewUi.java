package Ui;

import entity.Member;
import entity.MembershipCard;
import service.MemberService;
import dao.MembershipCardDAO;
import utils.StyleUtils; // 引入样式

import javax.swing.*;
import java.awt.*;

public class RenewUi extends JDialog {

    private MemberService memberService;
    private MembershipCardDAO cardDAO;
    private Member currentMember;
    private MembershipCard currentCard;
    private boolean isStaffOperation; // 标志位

    // 组件
    private JComboBox<String> daysComboBox;
    private JTextField daysField;
    private JTextField priceField;
    private JRadioButton balanceRadio;
    private JRadioButton cashRadio;
    private JLabel balanceTipLabel;

    public RenewUi(Frame owner, Member member, boolean isStaffOperation) {
        super(owner, isStaffOperation ? "办理续费 (员工通道)" : "自助续费", true);
        this.currentMember = member;
        this.isStaffOperation = isStaffOperation;

        this.memberService = new MemberService();
        this.cardDAO = new MembershipCardDAO();
        this.currentCard = cardDAO.getActiveMembershipCard(member.getId());

        // 1. 初始化主题
        StyleUtils.initGlobalTheme();

        setSize(500, 550); // 稍微加高一点，容纳更多信息
        setLocationRelativeTo(owner);
        setLayout(null);
        getContentPane().setBackground(StyleUtils.COLOR_BG);

        // 检查是否有卡
        if (currentCard == null) {
            String msg = isStaffOperation ? "该会员当前无有效卡，请先进行【开卡】操作。" : "您当前没有有效会员卡，请前往前台办理开卡！";
            JOptionPane.showMessageDialog(owner, msg, "提示", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        initView();
        setVisible(true);
    }

    private void initView() {
        int x = 40, w = 400, h = 40; // 统一高度
        int y = 20;

        // === 1. 顶部会员信息卡片 ===
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(20, 20, 445, 90);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(infoPanel);

        JLabel nameLbl = new JLabel("会员: " + currentMember.getName());
        nameLbl.setFont(StyleUtils.FONT_BOLD);
        nameLbl.setForeground(StyleUtils.COLOR_PRIMARY);
        nameLbl.setBounds(20, 15, 200, 25);
        infoPanel.add(nameLbl);

        JLabel dateLbl = new JLabel("有效期至: " + currentCard.getEndDate());
        dateLbl.setFont(StyleUtils.FONT_NORMAL);
        dateLbl.setForeground(StyleUtils.COLOR_DANGER); // 红色强调日期
        dateLbl.setBounds(20, 45, 300, 25);
        infoPanel.add(dateLbl);

        y += 110;

        // === 2. 续费设置 ===
        addLabel("续费时长:", x, y);

        if (isStaffOperation) {
            // 员工模式: 输入框
            daysField = new JTextField("30");
            StyleUtils.styleTextField(daysField);
            daysField.setBounds(x + 80, y, 150, h);
            add(daysField);

            JLabel unit = new JLabel("天");
            unit.setFont(StyleUtils.FONT_NORMAL);
            unit.setBounds(x + 240, y, 30, h);
            add(unit);
        } else {
            // 会员模式: 下拉框
            String[] options = {"📅 月卡续费 (30天)", "📅 年卡续费 (365天)"};
            daysComboBox = new JComboBox<>(options);
            daysComboBox.setBounds(x + 80, y, 220, h);
            daysComboBox.setFont(StyleUtils.FONT_NORMAL);
            daysComboBox.setBackground(Color.WHITE);
            daysComboBox.addActionListener(e -> updatePriceForMember());
            add(daysComboBox);
        }

        y += 60;
        addLabel("应付金额:", x, y);

        priceField = new JTextField();
        StyleUtils.styleTextField(priceField);
        priceField.setBounds(x + 80, y, 150, h);
        priceField.setFont(new Font("Arial", Font.BOLD, 16));

        if (!isStaffOperation) {
            priceField.setEditable(false);
            updatePriceForMember(); // 初始化价格
        } else {
            priceField.setText("200");
        }
        add(priceField);

        JLabel yuan = new JLabel("元");
        yuan.setFont(StyleUtils.FONT_NORMAL);
        yuan.setBounds(x + 240, y, 30, h);
        add(yuan);

        // === 3. 支付方式 ===
        y += 60;
        addLabel("支付方式:", x, y);

        balanceRadio = new JRadioButton("余额支付");
        balanceRadio.setFont(StyleUtils.FONT_NORMAL);
        balanceRadio.setBackground(StyleUtils.COLOR_BG);
        balanceRadio.setBounds(x + 80, y, 100, h);
        balanceRadio.setSelected(true);
        add(balanceRadio);

        cashRadio = new JRadioButton("现金/其它");
        cashRadio.setFont(StyleUtils.FONT_NORMAL);
        cashRadio.setBackground(StyleUtils.COLOR_BG);
        cashRadio.setBounds(x + 190, y, 100, h);

        ButtonGroup group = new ButtonGroup();
        group.add(balanceRadio);
        group.add(cashRadio);

        if (!isStaffOperation) {
            cashRadio.setVisible(false); // 会员只能看余额
            balanceRadio.setText("余额支付 (默认)");
        } else {
            add(cashRadio);
        }

        y += 35;
        balanceTipLabel = new JLabel("当前账户余额: ¥ " + String.format("%.2f", currentMember.getBalance()));
        balanceTipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        balanceTipLabel.setForeground(StyleUtils.COLOR_INFO);
        balanceTipLabel.setBounds(x + 85, y, 300, 20);
        add(balanceTipLabel);

        // === 4. 底部按钮 ===
        JButton confirmBtn = new JButton("确认续费");
        StyleUtils.styleButton(confirmBtn, StyleUtils.COLOR_SUCCESS);
        confirmBtn.setFont(new Font("微软雅黑", Font.BOLD, 18));
        confirmBtn.setBounds(40, 430, 400, 50);
        confirmBtn.addActionListener(e -> performRenew());
        add(confirmBtn);
    }

    private void addLabel(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(StyleUtils.FONT_BOLD);
        l.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        l.setBounds(x, y, 80, 40); // 高度匹配输入框
        add(l);
    }

    private void updatePriceForMember() {
        int idx = daysComboBox.getSelectedIndex();
        if (idx == 0) priceField.setText("200.0");
        else priceField.setText("1200.0");
    }

    private void performRenew() {
        try {
            int days;
            if (isStaffOperation) {
                days = Integer.parseInt(daysField.getText().trim());
            } else {
                days = (daysComboBox.getSelectedIndex() == 0) ? 30 : 365;
            }

            double price = Double.parseDouble(priceField.getText().trim());
            boolean useBalance = isStaffOperation ? balanceRadio.isSelected() : true;

            int opt = JOptionPane.showConfirmDialog(this,
                    "确认续费 " + days + " 天？\n金额：¥" + price, "确认", JOptionPane.YES_NO_OPTION);

            if (opt != JOptionPane.YES_OPTION) return;

            // 调用 Service
            MemberService.ServiceResult<Void> result = memberService.renewMembership(
                    currentMember.getId(), days, price, useBalance
            );

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ " + result.getMessage());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ " + result.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "输入格式错误！");
        }
    }
}