package Ui;

import entity.Booking;
import entity.Course;
import entity.Member;
import service.BookingService;
import service.CourseService;
import service.ServiceResult;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookCourseUi extends JFrame {

    private Member member;
    private CourseService courseService;
    private BookingService bookingService;

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BookCourseUi(Member member) {
        this.member = member;
        this.courseService = new CourseService();
        this.bookingService = new BookingService();

        // 1. 初始化主题
        StyleUtils.initGlobalTheme();

        setTitle("📅 预约课程 - " + member.getName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));

        initView();
        loadCourses();
        setVisible(true);
    }

    private void initView() {
        // === 顶部标题栏 ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLbl = new JLabel("🔥 热门课程预约");
        titleLbl.setFont(StyleUtils.FONT_TITLE);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);

        // 搜索区
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchField = new JTextField(15);
        StyleUtils.styleTextField(searchField);
        JButton searchBtn = new JButton("🔍 搜索");
        StyleUtils.styleButton(searchBtn, StyleUtils.COLOR_PRIMARY);
        searchBtn.addActionListener(e -> loadCourses());

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // === 中间表格区域 ===
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20)); // 左右留白

        String[] cols = {"ID", "课程名称", "教练", "时间", "时长(分)", "剩余名额", "状态"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        courseTable = new JTable(tableModel);
        StyleUtils.styleTable(courseTable);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // === 底部操作栏 ===
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setBackground(Color.WHITE);

        JButton refreshBtn = new JButton("🔄 刷新列表");
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_INFO);
        refreshBtn.addActionListener(e -> loadCourses());

        JButton bookBtn = new JButton("✅ 立即预约");
        StyleUtils.styleButton(bookBtn, StyleUtils.COLOR_SUCCESS);
        bookBtn.setPreferredSize(new Dimension(120, 40));
        bookBtn.addActionListener(e -> performBooking());

        bottomPanel.add(refreshBtn);
        bottomPanel.add(bookBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        String keyword = searchField.getText().trim();
        List<Course> courses = courseService.getAvailableCourses(); // 获取可用课程

        // 简单的内存搜索过滤（如果Service没有search方法的话）
        for (Course c : courses) {
            // 如果有关键字且课程名不包含关键字，则跳过
            if (!keyword.isEmpty() && !c.getName().contains(keyword)) {
                continue;
            }

            // 获取详情以拿到教练名和剩余名额（利用 CourseService 中已有的 getCourseDetail 方法）
            // CourseService.java 中的 getCourseDetail 方法
            service.CourseService.CourseDetail detail = courseService.getCourseDetail(c.getCourseId());

            String trainerName = "未知";
            int availableSlots = 0;

            if (detail != null) {
                trainerName = detail.getTrainerName();
                availableSlots = detail.getAvailableSlots();
            }

            tableModel.addRow(new Object[]{
                    c.getCourseId(),
                    c.getName(),          // 修正为 getName()
                    trainerName,          // 从 detail 获取教练名
                    c.getCourseTime(),    // 修正为 getCourseTime()
                    c.getDuration(),      // 修正为 getDuration()
                    availableSlots,       // 从 detail 获取剩余名额
                    "🟢 可预约"
            });
        }
    }

    private void performBooking() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一门课程！");
            return;
        }

        int courseId = (int) tableModel.getValueAt(row, 0);
        String courseName = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要预约课程 [" + courseName + "] 吗？", "预约确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 修正 1: 方法名改为 createAndConfirmBooking (这是 BookingService 里有的方法)
            // 修正 2: 泛型改为 <?> 或 <entity.Booking>，因为返回值不是 Void
            BookingService.ServiceResult<Booking> result = bookingService.createAndConfirmBooking(member.getId(), courseId);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "🎉 预约成功！请准时参加。");

                loadCourses(); // 刷新列表
            } else {
                JOptionPane.showMessageDialog(this, "预约失败：" + result.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }


    }
}