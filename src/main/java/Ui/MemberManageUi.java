package Ui;

import entity.Member;
import service.MemberService;
import service.MemberService.MemberDetail;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MemberManageUi extends JFrame {

    private MemberService memberService;

    // 组件
    private JTextField searchField;
    private JTable memberTable;
    private DefaultTableModel tableModel;

    public MemberManageUi() {
        this.memberService = new MemberService();

        this.setTitle("会员管理中心");
        this.setSize(900, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        initView();
        loadData(null); // 初始加载所有数据

        this.setVisible(true);
    }

    private void initView() {
        // 1. 顶部搜索区
        JLabel searchLabel = new JLabel("搜索会员(姓名/手机):");
        searchLabel.setBounds(30, 20, 150, 30);
        this.getContentPane().add(searchLabel);

        searchField = new JTextField();
        searchField.setBounds(160, 20, 200, 30);
        this.getContentPane().add(searchField);

        JButton searchBtn = new JButton("查询");
        searchBtn.setBounds(370, 20, 80, 30);
        searchBtn.addActionListener(e -> loadData(searchField.getText().trim()));
        this.getContentPane().add(searchBtn);

        // 2. 数据表格
        String[] columns = {"ID", "姓名", "手机号", "性别", "注册日期", "当前状态", "会员卡情况"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止编辑
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setRowHeight(25);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBounds(30, 70, 820, 400);
        this.getContentPane().add(scrollPane);

        // 3. 底部操作按钮
        JButton freezeBtn = new JButton("冻结/解冻会员");
        freezeBtn.setBounds(30, 490, 150, 40);
        freezeBtn.setBackground(new Color(255, 200, 100)); // 橙色
        freezeBtn.addActionListener(e -> toggleMemberStatus());
        this.getContentPane().add(freezeBtn);

        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.setBounds(750, 490, 100, 40);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadData(null);
        });
        this.getContentPane().add(refreshBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 900, 600);
        bg.setBackground(new Color(240, 245, 240)); // 浅绿色调
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    // 加载数据
    private void loadData(String keyword) {
        tableModel.setRowCount(0); // 清空
        List<Member> list;

        if (keyword == null || keyword.isEmpty()) {
            list = memberService.getAllMembers();
        } else {
            list = memberService.search(keyword);
        }

        for (Member m : list) {
            // 获取详情以查看会员卡状态
            MemberDetail detail = memberService.getMemberDetail(m.getId());
            String cardStatus = "无有效卡";
            if (detail != null && detail.isHasValidCard()) {
                cardStatus = detail.getActiveCard().getCardType() + " (剩余" + detail.getCardRemainingDays() + "天)";
            }

            // 状态转中文
            String status = m.getStatus();
            if ("active".equals(status)) status = "正常";
            else if ("frozen".equals(status)) status = "已冻结";
            else if ("inactive".equals(status)) status = "已注销";

            Object[] row = {
                    m.getId(),
                    m.getName(),
                    m.getPhone(),
                    "male".equals(m.getGender()) ? "男" : "女",
                    m.getRegisterDate(),
                    status,
                    cardStatus
            };
            tableModel.addRow(row);
        }
    }

    // 切换状态操作
    private void toggleMemberStatus() {
        int row = memberTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一位会员");
            return;
        }

        int memberId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String currentStatusStr = (String) tableModel.getValueAt(row, 5); // "正常" 或 "已冻结"

        // 如果是正常 -> 冻结
        if ("正常".equals(currentStatusStr)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要冻结会员 [" + name + "] 吗？\n冻结后该会员将无法登录、预约或进场。",
                    "确认冻结", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                MemberService.ServiceResult<Void> result = memberService.freezeMember(memberId, "管理员操作");
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "会员已冻结");
                    loadData(null);
                } else {
                    JOptionPane.showMessageDialog(this, "操作失败：" + result.getMessage());
                }
            }
        }
        // 如果是已冻结 -> 解冻
        else if ("已冻结".equals(currentStatusStr)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要激活/解冻会员 [" + name + "] 吗？",
                    "确认解冻", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                MemberService.ServiceResult<Void> result = memberService.activateMember(memberId);
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "会员已恢复正常");
                    loadData(null);
                } else {
                    JOptionPane.showMessageDialog(this, "操作失败：" + result.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "该会员状态不支持此操作（可能是已注销）");
        }
    }
}