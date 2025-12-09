package Ui;

import dao.MembershipCardDAO;
import entity.Member;
import service.MemberService;
import service.ServiceResult;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BuyCardUi extends JFrame {

    private Member member;
    private MemberService memberService;

    // 选中的卡类型
    private int selectedType = -1;
    private JPanel monthlyPanel;
    private JPanel yearlyPanel;

    public BuyCardUi(Member member) {
        this.member = member;
        this.memberService = new MemberService();

        StyleUtils.initGlobalTheme();
        setTitle("💳 办理会员卡");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);

        initView();
        setVisible(true);
    }

    private void initView() {
        // 标题
        JLabel titleLbl = new JLabel("选择您的会员方案", SwingConstants.CENTER);
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        titleLbl.setBounds(0, 30, 700, 40);
        add(titleLbl);

        JLabel subLbl = new JLabel("为 [" + member.getName() + "] 办理开卡业务", SwingConstants.CENTER);
        subLbl.setFont(StyleUtils.FONT_NORMAL);
        subLbl.setForeground(StyleUtils.COLOR_INFO);
        subLbl.setBounds(0, 70, 700, 20);
        add(subLbl);

        // === 卡片区域 ===
        int cardY = 120;
        int cardW = 240;
        int cardH = 260;
        int gap = 60;
        int startX = (700 - (cardW * 2 + gap)) / 2;

        // 月卡卡片
        monthlyPanel = createCardPanel("月卡 (Monthly)", "¥ 200", "🗓️ 有效期 30 天", "⭐ 适合短期体验", startX, cardY, cardW, cardH);
        monthlyPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { selectCard(MembershipCardDAO.TYPE_MONTHLY); }
        });
        add(monthlyPanel);

        // 年卡卡片
        yearlyPanel = createCardPanel("年卡 (Yearly)", "¥ 1200", "🗓️ 有效期 365 天", "🔥 超值！每天仅需 3 元", startX + cardW + gap, cardY, cardW, cardH);
        yearlyPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { selectCard(MembershipCardDAO.TYPE_YEARLY); }
        });
        add(yearlyPanel);

        // 底部按钮
        JButton confirmBtn = new JButton("立即开通");
        StyleUtils.styleButton(confirmBtn, StyleUtils.COLOR_PRIMARY);
        confirmBtn.setBounds(250, 410, 200, 45);
        confirmBtn.addActionListener(e -> performBuy());
        add(confirmBtn);
    }

    // 辅助：创建卡片面板
    private JPanel createCardPanel(String title, String price, String desc1, String desc2, int x, int y, int w, int h) {
        JPanel p = new JPanel(null);
        p.setBounds(x, y, w, h);
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel tLbl = new JLabel(title, SwingConstants.CENTER);
        tLbl.setFont(StyleUtils.FONT_TITLE);
        tLbl.setBounds(0, 20, w, 30);
        p.add(tLbl);

        JLabel pLbl = new JLabel(price, SwingConstants.CENTER);
        pLbl.setFont(new Font("Arial", Font.BOLD, 36));
        pLbl.setForeground(StyleUtils.COLOR_DANGER);
        pLbl.setBounds(0, 60, w, 50);
        p.add(pLbl);

        JLabel d1 = new JLabel(desc1, SwingConstants.CENTER);
        d1.setFont(StyleUtils.FONT_NORMAL);
        d1.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        d1.setBounds(0, 130, w, 20);
        p.add(d1);

        JLabel d2 = new JLabel(desc2, SwingConstants.CENTER);
        d2.setFont(StyleUtils.FONT_NORMAL);
        d2.setForeground(StyleUtils.COLOR_WARNING);
        d2.setBounds(0, 160, w, 20);
        p.add(d2);

        return p;
    }

    // 选中逻辑：改变边框颜色
    private void selectCard(int type) {
        selectedType = type;
        // 重置边框
        monthlyPanel.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        yearlyPanel.setBorder(new LineBorder(new Color(220, 220, 220), 1));

        // 高亮选中
        if (type == MembershipCardDAO.TYPE_MONTHLY) {
            monthlyPanel.setBorder(new LineBorder(StyleUtils.COLOR_PRIMARY, 3));
        } else {
            yearlyPanel.setBorder(new LineBorder(StyleUtils.COLOR_PRIMARY, 3));
        }
    }

    private void performBuy() {
        if (selectedType == -1) {
            JOptionPane.showMessageDialog(this, "请先点击选择一种会员卡！");
            return;
        }

        double price = (selectedType == MembershipCardDAO.TYPE_MONTHLY) ? 200.0 : 1200.0;
        String name = (selectedType == MembershipCardDAO.TYPE_MONTHLY) ? "月卡" : "年卡";

        int opt = JOptionPane.showConfirmDialog(this,
                "确认开通 [" + name + "] ?\n需支付现金：¥ " + price, "支付确认", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {

            MemberService.ServiceResult<Void> res = memberService.buyCard(member.getId(), selectedType);
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ 开卡成功！");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ 失败：" + res.getMessage());
            }
        }
    }
}