package Ui;

import dao.EmployeeDAO;
import dao.EmployeeRoleDAO;
import entity.Employee;
import utils.StyleUtils;
import service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeManageUi extends JFrame {

    private EmployeeDAO employeeDAO;
    private EmployeeRoleDAO roleDAO;
    private UserService userService;

    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public EmployeeManageUi() {
        this.employeeDAO = new EmployeeDAO();
        this.roleDAO = new EmployeeRoleDAO();
        this.userService = new UserService();

        StyleUtils.initGlobalTheme();
        setTitle("👔 员工/人事档案管理");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(10, 10));

        initView();
        loadData();
        setVisible(true);
    }

    private void initView() {
        // === 顶部工具栏 ===
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        add(toolBar, BorderLayout.NORTH);

        // 搜索区
        toolBar.add(new JLabel("🔍 姓名/ID:"));
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        toolBar.add(searchField);

        JButton searchBtn = new JButton("查询");
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchEmployee());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 刷新");
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadData());
        toolBar.add(refreshBtn);

        toolBar.add(new JSeparator(SwingConstants.VERTICAL));

        // 操作区
        JButton addBtn = new JButton("➕ 入职登记");
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        addBtn.addActionListener(e -> addEmployee());
        toolBar.add(addBtn);

        // >>> 核心修改：合并为一个强大的账号管理按钮 <<<
        JButton accountBtn = new JButton("👤 账号管理");
        StyleUtils.styleButton(accountBtn, new Color(155, 89, 182)); // 紫色
        accountBtn.setToolTipText("开通账号 / 重置密码 / 修改登录名");
        accountBtn.addActionListener(e -> manageAccount());
        toolBar.add(accountBtn);

        JButton editBtn = new JButton("✏️ 修改信息");
        StyleUtils.styleButton(editBtn, StyleUtils.COLOR_WARNING);
        editBtn.addActionListener(e -> editEmployee());
        toolBar.add(editBtn);

        JButton delBtn = new JButton("🗑️ 离职/删除");
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteEmployee());
        toolBar.add(delBtn);

        // === 表格区域 ===
        String[] columns = {"ID", "姓名", "角色", "手机号", "入职日期"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        employeeTable = new JTable(tableModel);
        StyleUtils.styleTable(employeeTable);

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Employee> list = employeeDAO.getAllEmployees();
        for (Employee e : list) {
            addEmployeeToTable(e);
        }
    }

    private void searchEmployee() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        List<Employee> list = employeeDAO.searchEmployeeByName(kw);
        for (Employee e : list) {
            addEmployeeToTable(e);
        }
    }

    private void addEmployeeToTable(Employee e) {
        String roleName = roleDAO.getRoleDisplayName(e.getRoleId());
        tableModel.addRow(new Object[]{
                e.getId(), e.getName(), roleName, e.getPhone(), e.getHireDate()
        });
    }

    // 1. 入职登记
    private void addEmployee() {
        JTextField nameF = new JTextField();
        JTextField phoneF = new JTextField();
        String[] roles = {"管理员", "前台", "教练"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        Object[] message = { "姓名:", nameF, "手机号:", phoneF, "职位:", roleBox };

        int option = JOptionPane.showConfirmDialog(this, message, "新员工入职", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim();
            String phone = phoneF.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "姓名和手机号不能为空！");
                return;
            }

            int roleIdx = roleBox.getSelectedIndex();
            // Role ID mapping: 0->3, 1->2, 2->1
            int roleId = (roleIdx == 0) ? dao.EmployeeRoleDAO.ROLE_ID_ADMIN :
                    (roleIdx == 1) ? dao.EmployeeRoleDAO.ROLE_ID_RECEPTIONIST : dao.EmployeeRoleDAO.ROLE_ID_TRAINER;

            Employee emp = new Employee();
            emp.setName(name);
            emp.setPhone(phone);
            emp.setRoleId(roleId);
            emp.setHireDate(new java.util.Date());

            if (employeeDAO.addEmployee(emp)) {
                // 自动注册账号 (默认账号=手机号, 密码=123456)
                userService.setEmployeeAccount(emp.getId(), emp.getPhone(), "123456");
                JOptionPane.showMessageDialog(this, "✅ 入职办理成功！\n默认账号：" + emp.getPhone() + "\n默认密码：123456");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "添加失败（可能是手机号重复）");
            }
        }
    }

    // 2. 【新增】账号管理 (开通/重置/改名)
    private void manageAccount() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一名员工！");
            return;
        }

        int empId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String currentPhone = (String) tableModel.getValueAt(row, 3);

        // 弹出设置框
        JTextField userF = new JTextField(currentPhone); // 默认填入手机号
        JTextField passF = new JTextField(); // 留空让用户填新密码

        Object[] message = {
                "正在管理 [" + name + "] 的登录账号",
                "登录用户名 (可修改):", userF,
                "新密码 (重置/设置):", passF,
                "<html><font color='gray' size='2'>* 若账号不存在将自动创建<br>* 若账号存在将更新密码</font></html>"
        };

        int option = JOptionPane.showConfirmDialog(this, message, "账号管理", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newUser = userF.getText().trim();
            String newPass = passF.getText().trim();

            if (newUser.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名和密码不能为空！");
                return;
            }

            // 调用 Service 的智能方法
            service.UserService.ServiceResult<Void> result = userService.setEmployeeAccount(empId, newUser, newPass);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage() + "\n用户名: " + newUser + "\n密码: " + newPass);
            } else {
                JOptionPane.showMessageDialog(this, "❌ 操作失败: " + result.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editEmployee() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的员工");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Employee emp = employeeDAO.getEmployeeById(id);

        String newName = JOptionPane.showInputDialog(this, "修改姓名:", emp.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            emp.setName(newName);
            employeeDAO.updateEmployee(emp);
            loadData();
            JOptionPane.showMessageDialog(this, "修改成功");
        }
    }

    private void deleteEmployee() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的员工");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        if (JOptionPane.showConfirmDialog(this, "确定删除员工 [" + name + "] 吗?\n(这将同时删除该员工的登录账号)", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (employeeDAO.deleteEmployee(id)) {
                JOptionPane.showMessageDialog(this, "删除成功");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，可能有关联数据");
            }
        }
    }
}