package Ui;

import com.toedter.calendar.JDateChooser; // 引入日历组件
import dao.BookingDAO;
import entity.Booking;
import entity.Course;
import entity.Employee;
import service.BookingService;
import service.BookingService.BookingDetail;
import service.CourseService;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseAttendanceUi extends JFrame {

    private Employee employee; // 当前操作员
    private CourseService courseService;
    private BookingService bookingService;

    // 数据缓存 (用于搜索过滤)
    private List<Booking> currentCourseBookings = new ArrayList<>();

    // 组件
    private JDateChooser dateChooser;
    private JComboBox<CourseItem> courseCombo;
    private JTextField searchField;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public CourseAttendanceUi(Employee employee) {
        this.employee = employee;
        this.courseService = new CourseService();
        this.bookingService = new BookingService();

        this.setTitle("课程点名/签到 - 操作员: " + (employee != null ? employee.getName() : "未知"));
        this.setSize(900, 650); // 稍微加大尺寸
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        initView();

        // 默认加载当天的课程
        loadCoursesByDate();

        this.setVisible(true);
    }

    private void initView() {
        int startY = 20;
        int rowHeight = 35;
        int gap = 15;

        // --- 1. 日期选择区 ---
        JLabel dateLabel = new JLabel("上课日期:");
        dateLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        dateLabel.setBounds(30, startY, 80, rowHeight);
        this.getContentPane().add(dateLabel);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date()); // 默认今天
        dateChooser.setBounds(110, startY, 150, rowHeight);
        // 日期改变时，自动重新加载课程列表
        dateChooser.addPropertyChangeListener("date", e -> loadCoursesByDate());
        this.getContentPane().add(dateChooser);

        // --- 2. 课程选择区 ---
        JLabel courseLabel = new JLabel("选择课程:");
        courseLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        courseLabel.setBounds(280, startY, 80, rowHeight);
        this.getContentPane().add(courseLabel);

        courseCombo = new JComboBox<>();
        courseCombo.setBounds(360, startY, 300, rowHeight);
        courseCombo.addActionListener(e -> loadStudents(null)); // 选课变动时加载全部名单
        this.getContentPane().add(courseCombo);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setBounds(680, startY, 80, rowHeight);
        refreshBtn.addActionListener(e -> {
            loadCoursesByDate(); // 重新走一遍流程
        });
        this.getContentPane().add(refreshBtn);

        // --- 3. 学生搜索区 (新增) ---
        int searchY = startY + rowHeight + gap;

        JLabel searchLabel = new JLabel("搜索学生:");
        searchLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        searchLabel.setBounds(30, searchY, 80, rowHeight);
        this.getContentPane().add(searchLabel);

        searchField = new JTextField();
        searchField.setToolTipText("输入姓名或手机号");
        searchField.setBounds(110, searchY, 200, rowHeight);
        // 回车触发搜索
        searchField.addActionListener(e -> performSearch());
        this.getContentPane().add(searchField);

        JButton searchBtn = new JButton("查找");
        searchBtn.setBounds(320, searchY, 80, rowHeight);
        searchBtn.addActionListener(e -> performSearch());
        this.getContentPane().add(searchBtn);

        JButton showAllBtn = new JButton("显示全部");
        showAllBtn.setBounds(410, searchY, 100, rowHeight);
        showAllBtn.addActionListener(e -> {
            searchField.setText("");
            loadStudents(null); // 传入null表示不过滤
        });
        this.getContentPane().add(showAllBtn);

        // --- 4. 学生名单表格 ---
        int tableY = searchY + rowHeight + gap;

        String[] columns = {"预约ID", "会员姓名", "手机号", "当前状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(30);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBounds(30, tableY, 820, 400);
        this.getContentPane().add(scroll);

        // --- 5. 底部操作按钮 ---
        int btnY = tableY + 400 + gap;
        JButton checkInBtn = new JButton("确认出席 (Check In)");
        checkInBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        checkInBtn.setBackground(new Color(100, 200, 100)); // 绿色
        checkInBtn.setForeground(Color.WHITE);
        checkInBtn.setBounds(300, btnY, 300, 50);
        checkInBtn.addActionListener(e -> performCheckIn());
        this.getContentPane().add(checkInBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 900, 650);
        bg.setBackground(new Color(240, 240, 250));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    /**
     * 根据选中的日期加载课程列表
     */
    private void loadCoursesByDate() {
        courseCombo.removeAllItems();
        tableModel.setRowCount(0); // 清空学生表
        currentCourseBookings.clear();

        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) return;

        String selectedDateStr = DateUtils.formatDate(selectedDate); // 转为 yyyy-MM-dd

        List<Course> allCourses = courseService.getAllCourses();
        boolean hasCourse = false;

        for (Course c : allCourses) {
            if (c.getCourseTime() == null) continue;

            // 比较日期 (忽略时间)
            String courseDateStr = DateUtils.formatDate(c.getCourseTime());
            if (selectedDateStr.equals(courseDateStr)) {
                // 如果是教练登录，只显示自己的课 (可选逻辑，根据需求决定是否开启)
                // if (employee.getRoleId() == 1 && c.getEmployeeId() != employee.getId()) continue;

                String timeStr = DateUtils.formatDateTime(c.getCourseTime());
                String label = c.getName() + " [" + timeStr.substring(11, 16) + "]"; // 只显示 HH:mm
                courseCombo.addItem(new CourseItem(c.getCourseId(), label));
                hasCourse = true;
            }
        }

        if (!hasCourse) {
            // 如果当天没课，可以加个提示项或者保持空白
            // courseCombo.addItem(new CourseItem(-1, "（该日期无课程）"));
        } else {
            // 默认选中第一个并加载名单
            if (courseCombo.getItemCount() > 0) {
                courseCombo.setSelectedIndex(0);
            }
        }
    }

    /**
     * 加载学生名单
     * @param keyword 搜索关键词 (null 或 空字符串表示显示全部)
     */
    private void loadStudents(String keyword) {
        tableModel.setRowCount(0);

        // 1. 获取当前选中的课程
        CourseItem selectedCourse = (CourseItem) courseCombo.getSelectedItem();
        if (selectedCourse == null || selectedCourse.id == -1) return;

        // 2. 如果是第一次加载该课程（或刷新），从数据库获取数据缓存起来
        //    只有当 keyword 为 null 时才重新查库，搜索时只在内存里过滤
        if (keyword == null) {
            currentCourseBookings = bookingService.getBookingsByCourse(selectedCourse.id);
        }

        // 3. 遍历缓存并过滤
        for (Booking b : currentCourseBookings) {
            // 过滤无效预约 (已取消/待确认)
            String status = b.getBookingStatus();
            if (BookingDAO.STATUS_CANCELLED.equals(status) || BookingDAO.STATUS_PENDING.equals(status)) {
                continue;
            }

            BookingDetail detail = bookingService.getBookingDetail(b.getBookingId());
            String memberName = detail.getMemberName();
            String memberPhone = detail.getMember().getPhone();

            // --- 核心搜索逻辑 ---
            // 如果有关键词，且 姓名/手机 都不包含关键词，则跳过
            if (keyword != null && !keyword.isEmpty()) {
                boolean nameMatch = memberName.contains(keyword);
                boolean phoneMatch = memberPhone.contains(keyword);
                if (!nameMatch && !phoneMatch) {
                    continue;
                }
            }
            // ------------------

            // 状态显示转换
            String statusShow = "待签到";
            if ("attended".equals(status)) {
                statusShow = "已出席";
            }

            Object[] row = {
                    b.getBookingId(),
                    memberName,
                    memberPhone,
                    statusShow
            };
            tableModel.addRow(row);
        }
    }

    // 触发搜索
    private void performSearch() {
        String keyword = searchField.getText().trim();
        loadStudents(keyword);
    }

    // 执行签到操作
    private void performCheckIn() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请选择一位会员进行签到");
            return;
        }

        // 1. 获取选中行的数据
        int bookingId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, 3);

        // 2. 检查当前状态
        if ("已出席".equals(status)) {
            JOptionPane.showMessageDialog(this, "该会员已经签到过了！");
            return;
        }

        // 3. 【新增】时间检查逻辑
        CourseItem selectedItem = (CourseItem) courseCombo.getSelectedItem();
        if (selectedItem != null) {
            Course course = courseService.getCourseById(selectedItem.id);
            if (course != null && course.getCourseTime() != null) {
                Date now = DateUtils.now();
                Date start = course.getCourseTime();
                // 计算结束时间 = 开始 + duration (分钟)
                long endTimeMillis = start.getTime() + (course.getDuration() * 60 * 1000L);
                Date end = new Date(endTimeMillis);

                // 规则A: 还没到上课时间 (比如提前超过30分钟)
                long diffToStart = start.getTime() - now.getTime();
                if (diffToStart > 30 * 60 * 1000) {
                    int confirmEarly = JOptionPane.showConfirmDialog(this,
                            "课程 [" + course.getName() + "] 还没开始呢！\n" +
                                    "上课时间：" + DateUtils.formatDateTime(start) + "\n" +
                                    "确定要现在签到吗？",
                            "提前签到警告", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirmEarly != JOptionPane.YES_OPTION) {
                        return; // 用户点了否，取消操作
                    }
                }

                // 规则B: 课程已经结束很久了 (比如超过12小时，防止误点历史课程)
                // 这里设置宽松一点，因为有时候教练会课后补录
                long diffFromEnd = now.getTime() - end.getTime();
                if (diffFromEnd > 12 * 60 * 60 * 1000) {
                    int confirmLate = JOptionPane.showConfirmDialog(this,
                            "课程 [" + course.getName() + "] 已经结束超过12小时了。\n" +
                                    "这是补录签到吗？",
                            "过期签到警告", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (confirmLate != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            }
        }

        // 4. 正常的确认弹窗
        int confirm = JOptionPane.showConfirmDialog(this,
                "确认会员 [" + name + "] 已到场参加课程吗？", "课程签到", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            BookingDAO dao = new BookingDAO();
            boolean success = dao.updateBookingStatus(bookingId, BookingDAO.STATUS_ATTENDED);

            if (success) {
                JOptionPane.showMessageDialog(this, "签到成功！");
                loadStudents(null); // 刷新表格 (保持搜索词为空)
            } else {
                JOptionPane.showMessageDialog(this, "签到失败");
            }
        }
    }

    // 内部类：下拉框对象
    private static class CourseItem {
        int id;
        String label;
        public CourseItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}