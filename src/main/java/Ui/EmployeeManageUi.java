package Ui;

import dao.EmployeeRoleDAO;
import entity.Employee;
import service.EmployeeService;
import service.EmployeeService.EmployeeDetail;
import service.UserService; // 【新增】引入 UserService

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeManageUi extends JFrame {

    private EmployeeService employeeService;
    private UserService userService; // 【新增】

    private JTable empTable;
    private DefaultTableModel tableModel;

    public EmployeeManageUi() {
        this.employeeService = new EmployeeService();
        this.userService = new UserService(); // 【新增】初始化

        this.setTitle("员工人事管理 (管理员)");
        this.setSize(900, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        initView();
        loadData();

        this.setVisible(true);
    }

    private void initView() {
        // ... (标题和表格部分保持不变) ...
        JLabel titleLabel = new JLabel("员工列表 & 入职管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setBounds(30, 20, 250, 30);
        this.getContentPane().add(titleLabel);

        String[] columns = {"ID", "姓名", "手机号", "角色", "入职日期", "工龄"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        empTable = new JTable(tableModel);
        empTable.setRowHeight(25);
        empTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选
        JScrollPane scroll = new JScrollPane(empTable);
        scroll.setBounds(30, 70, 820, 400);
        this.getContentPane().add(scroll);

        // --- 按钮区 ---

        // 1. 招聘教练
        JButton hireTrainerBtn = new JButton("招聘教练");
        hireTrainerBtn.setBounds(30, 490, 100, 40);
        hireTrainerBtn.addActionListener(e -> showHireDialog("教练", EmployeeRoleDAO.ROLE_ID_TRAINER));
        this.getContentPane().add(hireTrainerBtn);

        // 2. 招聘前台
        JButton hireRecepBtn = new JButton("招聘前台");
        hireRecepBtn.setBounds(140, 490, 100, 40);
        hireRecepBtn.addActionListener(e -> showHireDialog("前台", EmployeeRoleDAO.ROLE_ID_RECEPTIONIST));
        this.getContentPane().add(hireRecepBtn);

        // 3. 【新增】注册/重置账号按钮
        JButton regAccountBtn = new JButton("注册/重置账号");
        regAccountBtn.setBounds(260, 490, 140, 40);
        regAccountBtn.setBackground(new Color(100, 200, 250)); // 蓝色高亮
        regAccountBtn.addActionListener(e -> performRegisterAccount());
        this.getContentPane().add(regAccountBtn);

        // 4. 刷新
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setBounds(600, 490, 80, 40);
        refreshBtn.addActionListener(e -> loadData());
        this.getContentPane().add(refreshBtn);

        // 5. 离职
        JButton fireBtn = new JButton("办理离职");
        fireBtn.setBackground(new Color(220, 100, 100));
        fireBtn.setForeground(Color.WHITE);
        fireBtn.setBounds(700, 490, 100, 40);
        fireBtn.addActionListener(e -> performFire());
        this.getContentPane().add(fireBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 900, 600);
        bg.setBackground(new Color(230, 230, 240));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    // ... (loadData 和 showHireDialog 方法保持不变) ...
    // 注意：showHireDialog 里原本的“自动注册账号”逻辑可以删掉了，或者保留作为默认值

    private void loadData() {
        tableModel.setRowCount(0);
        List<Employee> list = employeeService.getAllEmployees();
        for (Employee e : list) {
            EmployeeDetail detail = employeeService.getEmployeeDetail(e.getId());
            Object[] row = {
                    e.getId(),
                    e.getName(),
                    e.getPhone(),
                    detail.getRoleDisplayName(),
                    utils.DateUtils.formatDate(e.getHireDate()),
                    detail.getWorkDurationString()
            };
            tableModel.addRow(row);
        }
    }

    // 入职弹窗
    private void showHireDialog(String roleName, int roleId) {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        Object[] message = {"姓名:", nameField, "手机号:", phoneField};

        int option = JOptionPane.showConfirmDialog(this, message, "入职 - " + roleName, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            // 这里只负责创建员工档案，不再自动创建账号
            EmployeeService.ServiceResult<Employee> hireResult = employeeService.hire(name, phone, roleId);
            if (hireResult.isSuccess()) {
                JOptionPane.showMessageDialog(this, "员工档案创建成功！\n请选中该员工并点击“注册账号”来分配登录权限。");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "入职失败：" + hireResult.getMessage());
            }
        }
    }

    // ... (performFire 保持不变) ...
    private void performFire() {
        int row = empTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要操作的员工");
            return;
        }
        int empId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String role = (String) tableModel.getValueAt(row, 3);
        if ("管理员".equals(role)) {
            JOptionPane.showMessageDialog(this, "不能删除管理员！");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "确定要办理 [" + name + "] 的离职手续吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            employeeService.terminate(empId);
            loadData();
        }
    }

    // ================== 【新增】手动注册/设置账号逻辑 ==================
    private void performRegisterAccount() {
        // 1. 检查是否选中了员工
        int row = empTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先在列表中选中一位员工！");
            return;
        }

        int empId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        // 2. 弹出输入框：用户名和密码
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
                "正在为 [" + name + "] 设置账号",
                "登录用户名:", usernameField,
                "登录密码:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "注册/重置账号", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名和密码不能为空！");
                return;
            }

            // 3. 调用 UserService 进行注册
            // 注意：registerEmployeeUser 如果发现 reference_id 已存在，可能会报错或需要改为 update
            // 为了简单，我们这里假设是新注册，或者是覆盖
            UserService.ServiceResult<Void> result = userService.registerEmployeeUser(empId, username, password);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "账号设置成功！\n用户名：" + username + "\n密码：" + password);
            } else {
                JOptionPane.showMessageDialog(this, "操作失败：" + result.getMessage() + "\n(可能该用户名已被占用)");
            }
        }
    }
}