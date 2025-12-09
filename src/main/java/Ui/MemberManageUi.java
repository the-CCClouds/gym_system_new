package Ui;

import dao.MemberDAO;
import entity.Member;
import utils.StyleUtils; // 引入样式

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MemberManageUi extends JFrame {

    private MemberDAO memberDAO;
    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public MemberManageUi() {
        this.memberDAO = new MemberDAO();

        // 1. 基础设置
        StyleUtils.initGlobalTheme(); // 确保主题一致
        setTitle("👥 会员档案管理");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));

        initView();
        loadData();
        setVisible(true);
    }

    private void initView() {
        // === 顶部工具栏 (白色背景，带阴影感) ===
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        add(toolBar, BorderLayout.NORTH);

        // 搜索框
        JLabel searchLbl = new JLabel("🔍 搜索:");
        searchLbl.setFont(StyleUtils.FONT_NORMAL);
        toolBar.add(searchLbl);

        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        toolBar.add(searchField);

        JButton searchBtn = new JButton("查询");
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchMember());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 刷新");
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadData());
        toolBar.add(refreshBtn);

        // 分隔线
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));

        // 操作按钮
        JButton addBtn = new JButton("➕ 新增");
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        // 这里只是演示，实际需要你链接到 AddMemberUi 或 RegisterUi
        addBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "请使用前台主界面的[新会员开卡]功能"));
        toolBar.add(addBtn);

        JButton editBtn = new JButton("✏️ 编辑");
        StyleUtils.styleButton(editBtn, StyleUtils.COLOR_WARNING);
        editBtn.addActionListener(e -> editMember());
        toolBar.add(editBtn);

        JButton delBtn = new JButton("🗑️ 删除");
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteMember());
        toolBar.add(delBtn);

        // === 中间表格区域 ===
        // 表头
        String[] columns = {"ID", "姓名", "手机号", "性别", "注册时间", "状态", "余额(¥)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        memberTable = new JTable(tableModel);
        StyleUtils.styleTable(memberTable); // 应用美化样式

        // 滚动条包裹（去掉默认边框，更现代）
        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 表格四周留白
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Member> members = memberDAO.getAllMembers();
        for (Member m : members) {
            addMemberToTable(m);
        }
    }

    private void searchMember() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Member> members = memberDAO.searchMembersByName(keyword);
        for (Member m : members) {
            addMemberToTable(m);
        }
    }

    private void addMemberToTable(Member m) {
        tableModel.addRow(new Object[]{
                m.getId(), m.getName(), m.getPhone(), m.getGender(),
                m.getRegisterDate(), m.getStatus(), m.getBalance()
        });
    }

    private void deleteMember() {
        int row = memberTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的会员！");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int opt = JOptionPane.showConfirmDialog(this,
                "确定要删除会员 [" + name + "] 吗？\n此操作不可恢复！", "确认删除", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            if (memberDAO.deleteMember(id)) {
                JOptionPane.showMessageDialog(this, "删除成功");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，可能存在关联数据");
            }
        }
    }

    private void editMember() {
        int row = memberTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的会员");
            return;
        }
        // 这里可以弹出一个简单的编辑对话框，或者复用 InfoUi 修改版
        JOptionPane.showMessageDialog(this, "编辑功能需单独实现 EditMemberUi");
    }
}