package Ui;

import dao.MembershipCardDAO;
import entity.Member;
import service.MemberService;
import service.MemberService.MemberDetail;

import javax.swing.*;
import java.awt.*;

public class BuyCardUi extends JFrame {

    private Member member;
    private MemberService memberService;

    public BuyCardUi(Member member) {
        this.member = member;
        this.memberService = new MemberService();

        this.setTitle("会员卡中心 - " + member.getName());
        this.setSize(500, 400);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        initView();

        this.setVisible(true);
    }

    private void initView() {
        // 1. 顶部标题
        JLabel titleLabel = new JLabel("购买/续费会员卡");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setBounds(160, 20, 200, 30);
        this.getContentPane().add(titleLabel);

        // 2. 显示当前状态
        // 获取详情以查看是否有卡
        MemberDetail detail = memberService.getMemberDetail(member.getId());
        boolean hasCard = detail.isHasValidCard();

        String statusText = hasCard ? "当前状态：已拥有有效会员卡" : "当前状态：暂无有效会员卡";
        Color statusColor = hasCard ? new Color(50, 150, 50) : Color.RED;

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statusLabel.setForeground(statusColor);
        statusLabel.setBounds(50, 70, 400, 30);
        this.getContentPane().add(statusLabel);

        if (hasCard && detail.getActiveCard() != null) {
            JLabel expireLabel = new JLabel("有效期至：" + detail.getActiveCard().getEndDate());
            expireLabel.setBounds(50, 100, 400, 20);
            this.getContentPane().add(expireLabel);
        }

        // 3. 购买区域面板
        JPanel buyPanel = new JPanel();
        buyPanel.setLayout(null);
        buyPanel.setBounds(30, 140, 420, 180);
        buyPanel.setBackground(Color.WHITE);
        buyPanel.setBorder(BorderFactory.createEtchedBorder());
        this.getContentPane().add(buyPanel);

        // --- 月卡选项 ---
        JLabel monthTitle = new JLabel("月卡 (Monthly)");
        monthTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        monthTitle.setBounds(20, 20, 120, 30);
        buyPanel.add(monthTitle);

        JLabel monthDesc = new JLabel("有效期30天，适合短期健身");
        monthDesc.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        monthDesc.setForeground(Color.GRAY);
        monthDesc.setBounds(20, 50, 200, 20);
        buyPanel.add(monthDesc);

        JButton buyMonthBtn = new JButton("购买月卡");
        buyMonthBtn.setBounds(280, 25, 100, 40);
        buyMonthBtn.addActionListener(e -> performBuy(MembershipCardDAO.TYPE_MONTHLY, "月卡"));
        buyPanel.add(buyMonthBtn);

        // 分割线
        JSeparator sep = new JSeparator();
        sep.setBounds(10, 90, 400, 10);
        buyPanel.add(sep);

        // --- 年卡选项 ---
        JLabel yearTitle = new JLabel("年卡 (Yearly)");
        yearTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        yearTitle.setForeground(new Color(200, 100, 0)); // 金色/橙色
        yearTitle.setBounds(20, 110, 120, 30);
        buyPanel.add(yearTitle);

        JLabel yearDesc = new JLabel("有效期365天，超值优惠");
        yearDesc.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        yearDesc.setForeground(Color.GRAY);
        yearDesc.setBounds(20, 140, 200, 20);
        buyPanel.add(yearDesc);

        JButton buyYearBtn = new JButton("购买年卡");
        buyYearBtn.setBounds(280, 115, 100, 40);
        buyYearBtn.setBackground(new Color(255, 240, 200)); // 淡金色背景
        buyYearBtn.addActionListener(e -> performBuy(MembershipCardDAO.TYPE_YEARLY, "年卡"));
        buyPanel.add(buyYearBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 500, 400);
        bg.setBackground(new Color(240, 248, 255));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    private void performBuy(int cardType, String cardName) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要购买 " + cardName + " 吗？\n(模拟支付：购买将立即生效)",
                "确认购买", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            MemberService.ServiceResult<Void> result = memberService.buyCard(member.getId(), cardType);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "支付成功！\n" + result.getMessage());
                this.dispose(); // 关闭购买界面
                // 重新打开自己以刷新状态，或者直接关闭让用户回主页
                new BuyCardUi(member);
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), "购买失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}