package Ui;

import entity.Member;
import service.MemberService;
import service.MemberService.MemberDetail;
import service.CheckInService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CheckInUi extends JFrame {

    private MemberService memberService;
    private CheckInService checkInService;

    // 当前操作的会员
    private Member currentMember;
    private boolean isCheckedIn = false;

    // 组件
    private JTextField searchField;
    private JLabel infoLabel;
    private JLabel statusLabel;
    private JButton actionBtn; // 签到/签退按钮动态变化

    public CheckInUi() {
        this.memberService = new MemberService();
        this.checkInService = new CheckInService();

        this.setTitle("前台门禁签到系统");
        this.setSize(650, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        initView();
        this.setVisible(true);
    }

    private void initView() {
        // 1. 顶部查询区
        JLabel searchLabel = new JLabel("会员搜索:");
        searchLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        searchLabel.setBounds(40, 30, 80, 30);
        this.getContentPane().add(searchLabel);

        searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchField.setToolTipText("输入姓名或手机号");
        searchField.setBounds(130, 30, 250, 35);
        // 回车触发查询
        searchField.addActionListener(e -> performQuery());
        this.getContentPane().add(searchField);

        JButton queryBtn = new JButton("查询");
        queryBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        queryBtn.setBounds(400, 30, 100, 35);
        queryBtn.setBackground(new Color(100, 150, 250)); // 蓝色按钮
        queryBtn.setForeground(Color.WHITE);
        queryBtn.addActionListener(e -> performQuery());
        this.getContentPane().add(queryBtn);

        // 分割线
        JSeparator sep = new JSeparator();
        sep.setBounds(30, 85, 580, 10);
        this.getContentPane().add(sep);

        // 2. 会员信息展示卡片
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(null);
        infoPanel.setBounds(40, 100, 550, 220);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEtchedBorder());
        this.getContentPane().add(infoPanel);

        // 信息文本
        infoLabel = new JLabel("<html><div style='text-align: center; margin-top: 60px; color: gray;'>请先输入关键词查询会员</div></html>");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        infoLabel.setBounds(20, 20, 510, 120);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoPanel.add(infoLabel);

        // 状态栏 (在卡片底部)
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBounds(20, 160, 510, 40);
        infoPanel.add(statusLabel);

        // 3. 底部操作按钮 (默认不可用)
        actionBtn = new JButton("等待查询...");
        actionBtn.setFont(new Font("微软雅黑", Font.BOLD, 22));
        actionBtn.setBounds(175, 360, 300, 60);
        actionBtn.setEnabled(false);
        actionBtn.addActionListener(e -> performAction());
        this.getContentPane().add(actionBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 650, 500);
        bg.setBackground(new Color(240, 245, 248));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    // 查询会员信息 (支持模糊搜索)
    private void performQuery() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入查询关键字 (姓名或手机号)");
            return;
        }

        // 调用 Service 的综合搜索
        java.util.List<Member> results = memberService.search(keyword);

        if (results.isEmpty()) {
            resetInfoPanel("<html><div style='text-align: center; color: red; margin-top: 50px;'>未找到匹配的会员<br/>请检查输入是否正确</div></html>");
            return;
        }

        // 处理结果
        if (results.size() == 1) {
            selectMember(results.get(0));
        } else {
            // 多个结果，弹窗让选
            showSelectionDialog(results);
        }
    }

    // 多选一弹窗
    private void showSelectionDialog(List<Member> members) {
        JDialog dialog = new JDialog(this, "查询到多位会员，请选择", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"ID", "姓名", "手机号", "性别"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        for (Member m : members) {
            model.addRow(new Object[]{m.getId(), m.getName(), m.getPhone(), "male".equals(m.getGender())?"男":"女"});
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 双击事件
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection(table, members, dialog);
                }
            }
        });

        JButton confirmBtn = new JButton("确定");
        confirmBtn.addActionListener(e -> confirmSelection(table, members, dialog));

        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel p = new JPanel(); p.add(confirmBtn);
        dialog.add(p, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void confirmSelection(JTable table, List<Member> members, JDialog dialog) {
        int row = table.getSelectedRow();
        if (row != -1) {
            selectMember(members.get(row));
            dialog.dispose();
        }
    }

    // 选中会员，更新界面
    private void selectMember(Member member) {
        this.currentMember = member;

        // 获取详细信息
        MemberDetail detail = memberService.getMemberDetail(member.getId());

        // 构建 HTML 显示
        StringBuilder sb = new StringBuilder("<html><div style='padding: 10px;'>");
        sb.append("<span style='font-size: 14px;'>会员ID: ").append(member.getId()).append("</span><br/>");
        sb.append("<span style='font-size: 18px; font-weight: bold;'>姓名: ").append(member.getName()).append("</span>&nbsp;&nbsp;");
        sb.append("<span style='font-size: 14px;'>手机: ").append(member.getPhone()).append("</span><br/><br/>");

        // 账号状态
        String statusColor = "active".equals(member.getStatus()) ? "green" : "red";
        String statusText = "active".equals(member.getStatus()) ? "正常" : ("frozen".equals(member.getStatus()) ? "已冻结" : "已停用");
        sb.append("账号状态: <font color='").append(statusColor).append("'>").append(statusText).append("</font><br/>");

        // 会员卡状态
        if (detail.isHasValidCard()) {
            sb.append("会员卡: <font color='green'>有效 (").append(detail.getActiveCard().getCardType()).append(")</font>");
            sb.append(" 剩余 ").append(detail.getCardRemainingDays()).append(" 天");
        } else {
            sb.append("会员卡: <font color='red'>无效 / 已过期</font>");
        }
        sb.append("</div></html>");

        infoLabel.setText(sb.toString());

        // 判断当前是在馆还是离馆
        isCheckedIn = detail.isCurrentlyCheckedIn();
        updateActionButton(detail, member.getStatus());
    }

    private void updateActionButton(MemberDetail detail, String status) {
        if (isCheckedIn) {
            // 已在馆 -> 显示签退
            statusLabel.setText("当前状态：【 在馆中 (In Gym) 】");
            statusLabel.setForeground(new Color(34, 139, 34)); // 深绿

            actionBtn.setText("签退 (Check Out)");
            actionBtn.setBackground(new Color(255, 100, 100)); // 红色
            actionBtn.setEnabled(true);
        } else {
            // 不在馆 -> 显示签到
            statusLabel.setText("当前状态：【 不在馆 (Out) 】");
            statusLabel.setForeground(Color.GRAY);

            // 只有卡有效 且 账号正常 才能签到
            boolean canCheckIn = detail.isHasValidCard() && "active".equals(status);

            if (canCheckIn) {
                actionBtn.setText("签到 (Check In)");
                actionBtn.setBackground(new Color(60, 179, 113)); // 绿色
                actionBtn.setEnabled(true);
            } else {
                actionBtn.setText("禁止入场 (卡无效或冻结)");
                actionBtn.setBackground(Color.GRAY);
                actionBtn.setEnabled(false);
            }
        }
    }

    private void resetInfoPanel(String msg) {
        currentMember = null;
        infoLabel.setText(msg);
        statusLabel.setText("");
        actionBtn.setEnabled(false);
        actionBtn.setText("等待查询...");
        actionBtn.setBackground(null);
    }

    // 执行操作
    private void performAction() {
        if (currentMember == null) return;

        if (isCheckedIn) {
            // 签退
            CheckInService.ServiceResult result = checkInService.checkOut(currentMember.getId());
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "签退成功！\n再见，" + currentMember.getName());
                selectMember(currentMember); // 刷新
            } else {
                JOptionPane.showMessageDialog(this, "签退失败：" + result.getMessage());
            }
        } else {
            // 签到
            CheckInService.ServiceResult result = checkInService.checkIn(currentMember.getId());
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "签到成功！\n欢迎光临，" + currentMember.getName());
                selectMember(currentMember); // 刷新
            } else {
                JOptionPane.showMessageDialog(this, "签到失败：" + result.getMessage());
            }
        }
    }
}