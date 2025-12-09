package Ui;

import entity.Course;
import entity.Employee;
import service.CourseService;
import service.CourseService.CourseDetail;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseManageUi extends JFrame {

    private Employee employee; // 当前操作的员工
    private CourseService courseService;

    private JTable courseTable;
    private DefaultTableModel tableModel;

    public CourseManageUi(Employee employee) {
        this.employee = employee;
        this.courseService = new CourseService();

        this.setTitle("课程管理系统 - 操作员：" + employee.getName());
        this.setSize(900, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        initView();
        loadCourseData(); // 初始加载数据

        this.setVisible(true);
    }

    private void initView() {
        // 标题
        JLabel titleLabel = new JLabel("课程管理中心");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        titleLabel.setBounds(30, 20, 200, 30);
        this.getContentPane().add(titleLabel);

        // 表格
        String[] columns = {"ID", "名称", "上课时间", "类型", "时长", "教练", "最大容量", "已约人数", "状态"}; // 【新增 "上课时间"】
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBounds(30, 70, 820, 400);
        this.getContentPane().add(scrollPane);

        // --- 按钮区 ---

        // 刷新
        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.setBounds(30, 490, 100, 40);
        refreshBtn.addActionListener(e -> loadCourseData());
        this.getContentPane().add(refreshBtn);

        // 添加课程
        JButton addBtn = new JButton("发布新课程");
        addBtn.setBackground(new Color(100, 200, 100)); // 绿色按钮
        addBtn.setBounds(580, 490, 120, 40);
        addBtn.addActionListener(e -> {
            // 打开添加窗口，并传入回调函数：当添加成功时，重新加载列表
            new CourseAddUi(() -> loadCourseData());
        });
        this.getContentPane().add(addBtn);

        // 删除课程
        JButton deleteBtn = new JButton("删除选中课程");
        deleteBtn.setBackground(new Color(220, 100, 100)); // 红色按钮
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBounds(720, 490, 130, 40);
        deleteBtn.addActionListener(e -> performDelete());
        this.getContentPane().add(deleteBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 900, 600);
        bg.setBackground(new Color(240, 240, 250));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    // 加载课程数据到表格
    private void loadCourseData() {
        tableModel.setRowCount(0);
        List<Course> courses = courseService.getAllCourses();

        for (Course c : courses) {

            // 【新增】格式化时间
            String timeStr = utils.DateUtils.formatDateTime(c.getCourseTime());


            // 获取详情以显示教练名字和预约人数
            CourseDetail detail = courseService.getCourseDetail(c.getCourseId());
            Object[] row = {
                    c.getCourseId(),
                    c.getName(),
                    timeStr, // 【新增】填入时间字符串
                    detail.getTypeDisplayName(),
                    detail.getDurationFormatted(),
                    detail.getTrainerName(),
                    c.getMaxCapacity(),
                    detail.getConfirmedBookingCount(), // 这里显示已确认预约数
                    detail.isFull() ? "已满" : "正常"
            };
            tableModel.addRow(row);
        }
    }

    // 删除逻辑
    private void performDelete() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的课程");
            return;
        }

        int courseId = (int) tableModel.getValueAt(row, 0);
        String courseName = (String) tableModel.getValueAt(row, 1);
        int bookedCount = (int) tableModel.getValueAt(row, 6);

        // 安全检查：有人预约时警告
        if (bookedCount > 0) {
            int forceConfirm = JOptionPane.showConfirmDialog(this,
                    "该课程已有 " + bookedCount + " 人预约！\n强制删除将自动取消所有关联预约。\n确定要继续吗？",
                    "高风险操作", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (forceConfirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            // 普通确认
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除课程：" + courseName + " 吗？",
                    "确认删除", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // 调用 Service 删除 (传入 true 表示强制删除关联预约)
        CourseService.ServiceResult result = courseService.deleteCourse(courseId, true);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "删除成功");
            loadCourseData(); // 刷新列表
        } else {
            JOptionPane.showMessageDialog(this, "删除失败：" + result.getMessage());
        }
    }
}