package Ui;

import entity.Course;
import entity.Employee;
import service.CourseService;
import service.ServiceResult;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseManageUi extends JFrame {

    private Employee currentUser;
    private CourseService courseService;

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public CourseManageUi(Employee user) {
        this.currentUser = user;
        this.courseService = new CourseService();

        // 1. 初始化主题
        StyleUtils.initGlobalTheme();

        setTitle("📅 课程排期管理系统");
        setSize(1100, 650);
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
        toolBar.add(new JLabel("🔍 课程搜索:"));
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        toolBar.add(searchField);

        JButton searchBtn = new JButton("查询");
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> searchCourse());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("🔄 刷新");
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadData());
        toolBar.add(refreshBtn);

        // 分隔
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));

        // 操作区 (发布课程)
        JButton addBtn = new JButton("➕ 发布新课程");
        StyleUtils.styleButton(addBtn, StyleUtils.COLOR_SUCCESS);
        addBtn.addActionListener(e -> {
            new CourseAddUi(CourseManageUi.this).setVisible(true);
        });
        toolBar.add(addBtn);

        JButton delBtn = new JButton("🗑️ 删除课程");
        StyleUtils.styleButton(delBtn, StyleUtils.COLOR_DANGER);
        delBtn.addActionListener(e -> deleteCourse());
        toolBar.add(delBtn);

        // === 中间表格 ===
        String[] columns = {"ID", "课程名称", "类型", "教练", "上课时间", "时长(min)", "容量", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        courseTable = new JTable(tableModel);
        StyleUtils.styleTable(courseTable); // 美化表格

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    // 公开方法供 CourseAddUi 调用刷新
    public void loadData() {
        tableModel.setRowCount(0);
        List<Course> list = courseService.getAllCourses();
        for (Course c : list) {
            // 获取详情以填充教练名等信息
            service.CourseService.CourseDetail detail = courseService.getCourseDetail(c.getCourseId());
            String trainerName = (detail != null) ? detail.getTrainerName() : "未知";
            String status = (detail != null && detail.isFull()) ? "🔴 已满" : "🟢 可预约";

            tableModel.addRow(new Object[]{
                    c.getCourseId(),
                    c.getName(),          // 方法名核对：getName()
                    c.getType(),          // 方法名核对：getType()
                    trainerName,          // 从 detail 获取
                    c.getCourseTime(),    // 方法名核对：getCourseTime()
                    c.getDuration(),      // 方法名核对：getDuration()
                    c.getMaxCapacity(),   // 方法名核对：getMaxCapacity()
                    status
            });
        }
    }

    private void searchCourse() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        // 简单过滤显示
        tableModel.setRowCount(0);
        List<Course> list = courseService.searchByName(keyword);
        for (Course c : list) {
            service.CourseService.CourseDetail detail = courseService.getCourseDetail(c.getCourseId());
            String trainerName = (detail != null) ? detail.getTrainerName() : "未知";
            String status = (detail != null && detail.isFull()) ? "🔴 已满" : "🟢 可预约";

            tableModel.addRow(new Object[]{
                    c.getCourseId(), c.getName(), c.getType(), trainerName,
                    c.getCourseTime(), c.getDuration(), c.getMaxCapacity(), status
            });
        }
    }

    private void deleteCourse() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的课程！");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int opt = JOptionPane.showConfirmDialog(this,
                "确定要删除课程 [" + name + "] 吗？\n这将同时取消所有关联的预约！", "警告", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            CourseService.ServiceResult<Void> result = courseService.deleteCourse(id); // 默认非强制，或者你需要改成 true
            // 如果你的 Service 需要强制删除标志，这里可能需要改成 deleteCourse(id, true)
            // 根据之前的 Service 代码，deleteCourse(id) 是非强制，deleteCourse(id, true) 是强制

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ 删除成功");
                loadData();
            } else {
                // 如果是因为有预约导致失败，询问是否强制删除
                int forceOpt = JOptionPane.showConfirmDialog(this,
                        "删除失败：" + result.getMessage() + "\n是否强制删除？(将取消所有预约)", "强制删除", JOptionPane.YES_NO_OPTION);
                if (forceOpt == JOptionPane.YES_OPTION) {
                    courseService.deleteCourse(id, true);
                    loadData();
                }
            }
        }
    }
}