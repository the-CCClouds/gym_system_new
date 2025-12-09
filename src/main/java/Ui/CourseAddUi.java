package Ui;

import com.toedter.calendar.JDateChooser;
import entity.Course;
import entity.Employee;
import service.CourseService;
import service.ServiceResult;
import service.EmployeeService;
import utils.StyleUtils; // 引入样式

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CourseAddUi extends JFrame {

    private CourseManageUi parentUi; // 用于刷新父窗口
    private CourseService courseService;
    private EmployeeService employeeService;

    // 组件
    private JTextField nameField;
    private JComboBox<String> typeBox;
    private JComboBox<TrainerItem> trainerBox; // 存放教练对象
    private JDateChooser dateChooser;
    private JSpinner timeSpinner; // 时间选择
    private JTextField durationField;
    private JTextField capacityField;

    public CourseAddUi(CourseManageUi parent) {
        this.parentUi = parent;
        this.courseService = new CourseService();
        this.employeeService = new EmployeeService(); // 需确保有此服务

        StyleUtils.initGlobalTheme();
        setTitle("📝 发布新课程");
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);

        initView();
        loadTrainers(); // 加载教练列表
        setVisible(true);
    }

    private void initView() {
        JPanel formPanel = new JPanel(null);
        formPanel.setBounds(30, 30, 425, 540);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(formPanel);

        // 标题
        JLabel titleLbl = new JLabel("排课信息录入", SwingConstants.CENTER);
        titleLbl.setFont(StyleUtils.FONT_TITLE);
        titleLbl.setForeground(StyleUtils.COLOR_PRIMARY);
        titleLbl.setBounds(0, 20, 425, 30);
        formPanel.add(titleLbl);

        int x = 40, y = 70, w = 345, h = 40, gap = 70;

        // 1. 课程名称
        addLabel(formPanel, "课程名称", x, y - 25);
        nameField = new JTextField();
        StyleUtils.styleTextField(nameField);
        nameField.setBounds(x, y, w, h);
        formPanel.add(nameField);

        // 2. 课程类型
        y += gap;
        addLabel(formPanel, "课程类型", x, y - 25);
        String[] types = {
                CourseService.TYPE_YOGA,
                CourseService.TYPE_SPINNING,
                CourseService.TYPE_PILATES,
                CourseService.TYPE_AEROBICS,
                CourseService.TYPE_STRENGTH,
                CourseService.TYPE_OTHER
        };
        typeBox = new JComboBox<>(types);
        typeBox.setBackground(Color.WHITE);
        typeBox.setBounds(x, y, w, h);
        formPanel.add(typeBox);

        // 3. 授课教练
        y += gap;
        addLabel(formPanel, "授课教练", x, y - 25);
        trainerBox = new JComboBox<>();
        trainerBox.setBackground(Color.WHITE);
        trainerBox.setBounds(x, y, w, h);
        formPanel.add(trainerBox);

        // 4. 上课日期 & 时间 (一行两个)
        y += gap;
        addLabel(formPanel, "上课日期", x, y - 25);
        addLabel(formPanel, "时间", x + 200, y - 25);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setBounds(x, y, 190, h);
        // 简单美化 dateChooser (去边框)
        dateChooser.getDateEditor().getUiComponent().setBorder(BorderFactory.createEmptyBorder());
        dateChooser.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formPanel.add(dateChooser);

        // 时间选择器 (Spinner)
        SpinnerDateModel model = new SpinnerDateModel();
        timeSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(editor);
        timeSpinner.setValue(new Date()); // 默认当前时间
        timeSpinner.setBounds(x + 200, y, 145, h);
        formPanel.add(timeSpinner);

        // 5. 时长 & 容量
        y += gap;
        addLabel(formPanel, "时长 (分钟)", x, y - 25);
        addLabel(formPanel, "最大人数", x + 180, y - 25);

        durationField = new JTextField("60"); // 默认60
        StyleUtils.styleTextField(durationField);
        durationField.setBounds(x, y, 160, h);
        formPanel.add(durationField);

        capacityField = new JTextField("20"); // 默认20
        StyleUtils.styleTextField(capacityField);
        capacityField.setBounds(x + 180, y, 165, h);
        formPanel.add(capacityField);

        // 提交按钮
        y += gap + 10;
        JButton submitBtn = new JButton("确认发布");
        StyleUtils.styleButton(submitBtn, StyleUtils.COLOR_PRIMARY);
        submitBtn.setBounds(x, y, w, 45);
        submitBtn.addActionListener(e -> performAdd());
        formPanel.add(submitBtn);
    }

    private void addLabel(JPanel p, String txt, int x, int y) {
        JLabel l = new JLabel(txt);
        l.setFont(StyleUtils.FONT_NORMAL);
        l.setForeground(StyleUtils.COLOR_INFO);
        l.setBounds(x, y, 200, 20);
        p.add(l);
    }

    // 内部类：用于 ComboBox 显示教练名但存储对象
    private static class TrainerItem {
        Employee emp;
        public TrainerItem(Employee emp) { this.emp = emp; }
        @Override public String toString() { return emp.getName() + " (ID:" + emp.getId() + ")"; }
    }

    private void loadTrainers() {
        // 假设 EmployeeService 有 getAllEmployees 或 getTrainers
        // 这里为了稳妥，我们用 getEmployeesByRole (假设你有) 或者遍历所有员工
        // 如果没有现成方法，你需要自己确保 Service 能查到教练
        List<Employee> list = employeeService.getAllEmployees();
        for (Employee e : list) {
            // 简单筛选角色 (假设 Role ID 2 是教练)
            if (e.getRoleId() == dao.EmployeeRoleDAO.ROLE_ID_TRAINER) {
                trainerBox.addItem(new TrainerItem(e));
            }
        }
    }

    private void performAdd() {
        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        TrainerItem trainerItem = (TrainerItem) trainerBox.getSelectedItem();
        Date date = dateChooser.getDate();
        Date time = (Date) timeSpinner.getValue();

        if (name.isEmpty() || trainerItem == null || date == null) {
            JOptionPane.showMessageDialog(this, "请补全课程基本信息！");
            return;
        }

        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            int capacity = Integer.parseInt(capacityField.getText().trim());

            // 合并日期和时间
            Calendar calDate = Calendar.getInstance();
            calDate.setTime(date);
            Calendar calTime = Calendar.getInstance();
            calTime.setTime(time);

            calDate.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
            calDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
            Date finalDate = calDate.getTime();

            // 调用 Service
            CourseService.ServiceResult<Course> result = courseService.createCourse(
                    name, type, duration, capacity, trainerItem.emp.getId(), finalDate
            );

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "🎉 发布成功！");
                if (parentUi != null) parentUi.loadData(); // 刷新父窗口
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ 失败：" + result.getMessage());
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "时长和容量必须是数字！");
        }
    }
}