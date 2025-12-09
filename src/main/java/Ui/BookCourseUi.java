package Ui;

import com.toedter.calendar.JDateChooser; // 引入日历组件
import entity.Course;
import entity.Member;
import service.BookingService;
import service.CourseService;
import service.CourseService.CourseDetail;
import utils.DateUtils; // 引入工具类

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

public class BookCourseUi extends JFrame {

    private Member member;
    private CourseService courseService;
    private BookingService bookingService;

    // 表格相关组件
    private JTable courseTable;
    private DefaultTableModel tableModel;

    // 【新增】日期筛选组件
    private JDateChooser dateChooser;

    public BookCourseUi(Member member) {
        this.member = member;
        this.courseService = new CourseService();
        this.bookingService = new BookingService();

        // 1. 窗口基本设置
        this.setTitle("预约课程 - " + member.getName());
        this.setSize(900, 600); // 稍微加宽加高
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        // 2. 初始化界面组件
        initView();

        // 3. 加载数据 (默认加载全部，或者你可以设为默认加载今天)
        loadCourseData();

        this.setVisible(true);
    }

    private void initView() {
        // --- 标题 ---
        JLabel titleLabel = new JLabel("课程列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setBounds(30, 20, 150, 30);
        this.getContentPane().add(titleLabel);

        // --- 【新增】筛选区域 (Y=60) ---
        JLabel filterLabel = new JLabel("按日期筛选:");
        filterLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterLabel.setBounds(30, 60, 80, 30);
        this.getContentPane().add(filterLabel);

        // 日历组件
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date()); // 默认选中今天
        dateChooser.setBounds(110, 60, 150, 30);
        this.getContentPane().add(dateChooser);

        // 查询按钮
        JButton searchBtn = new JButton("查询");
        searchBtn.setBounds(270, 60, 80, 30);
        searchBtn.addActionListener(e -> loadCourseData());
        this.getContentPane().add(searchBtn);

        // 显示全部按钮
        JButton showAllBtn = new JButton("显示全部");
        showAllBtn.setBounds(360, 60, 100, 30);
        showAllBtn.addActionListener(e -> {
            dateChooser.setDate(null); // 清空日期
            loadCourseData();
        });
        this.getContentPane().add(showAllBtn);

        // --- 表格区域 (Y下移到 110) ---
        String[] columnNames = {"ID", "课程名称", "上课时间", "类型", "时长", "教练", "剩余名额", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setRowHeight(25);
        courseTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 调整列宽 (可选)
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(140); // 时间

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBounds(30, 110, 820, 350); // 高度调整
        this.getContentPane().add(scrollPane);

        // --- 底部按钮 (Y下移到 480) ---
        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.setBounds(30, 480, 120, 40);
        refreshBtn.addActionListener(e -> loadCourseData());
        this.getContentPane().add(refreshBtn);

        JButton bookBtn = new JButton("立即预约");
        bookBtn.setFont(new Font("微软雅黑", Font.BOLD, 15));
        bookBtn.setBounds(730, 480, 120, 40);
        bookBtn.setBackground(new Color(100, 200, 100)); // 绿色按钮
        bookBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performBooking();
            }
        });
        this.getContentPane().add(bookBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 900, 600);
        bg.setBackground(new Color(240, 248, 255));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    // 加载/刷新表格数据 (带筛选逻辑)
    private void loadCourseData() {
        tableModel.setRowCount(0); // 清空

        // 获取筛选日期
        Date filterDate = dateChooser.getDate();
        String filterDateStr = (filterDate != null) ? DateUtils.formatDate(filterDate) : null;

        // 获取所有课程
        List<Course> courses = courseService.getAllCourses();

        for (Course course : courses) {
            // --- 【新增】筛选逻辑 ---
            if (filterDateStr != null) {
                // 如果选了日期，检查课程日期是否匹配
                // 使用 DateUtils.formatDate 只比较 "yyyy-MM-dd" 部分，忽略时分秒
                String courseDateStr = DateUtils.formatDate(course.getCourseTime());
                if (!filterDateStr.equals(courseDateStr)) {
                    continue; // 日期不匹配，跳过
                }
            }
            // -----------------------

            CourseDetail detail = courseService.getCourseDetail(course.getCourseId());
            String timeStr = DateUtils.formatDateTime(course.getCourseTime());

            Object[] rowData = {
                    course.getCourseId(),
                    course.getName(),
                    timeStr, // 显示完整时间
                    detail.getTypeDisplayName(),
                    detail.getDurationFormatted(),
                    detail.getTrainerName(),
                    detail.getAvailableSlots(),
                    detail.isFull() ? "已满" : "可预约"
            };
            tableModel.addRow(rowData);
        }

        // 如果筛选后没有数据，给个提示（可选）
        if (tableModel.getRowCount() == 0 && filterDateStr != null) {
            // 这里就不弹窗了，否则体验不好，列表空着就行
        }
    }

    // 执行预约逻辑
    private void performBooking() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一门课程！");
            return;
        }

        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        String courseName = (String) tableModel.getValueAt(selectedRow, 1);
        String timeStr = (String) tableModel.getValueAt(selectedRow, 2); // 获取时间显示

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要预约课程吗？\n\n课程：" + courseName + "\n时间：" + timeStr,
                "确认预约", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 调用 BookingService 进行预约 (这里用的是自动确认版，如果你改回了手动确认版请自行调整)
            BookingService.ServiceResult result = bookingService.createAndConfirmBooking(member.getId(), courseId);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "预约成功！\n请在'我的预约'中查看详情。");
                loadCourseData(); // 刷新列表（更新剩余名额）
            } else {
                JOptionPane.showMessageDialog(this, "预约失败：\n" + result.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}