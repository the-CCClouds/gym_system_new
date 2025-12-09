package Ui;

import entity.Booking;
import entity.Course;
import entity.Employee;
import service.BookingService;
import service.CourseService;
import service.ServiceResult;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseAttendanceUi extends JFrame {

    private Employee trainer;
    private CourseService courseService;
    private BookingService bookingService;

    // 组件
    private JComboBox<CourseItem> courseBox;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel infoLabel;

    public CourseAttendanceUi(Employee trainer) {
        this.trainer = trainer;
        this.courseService = new CourseService();
        this.bookingService = new BookingService();

        StyleUtils.initGlobalTheme();
        setTitle("📋 上课点名系统 - 教练: " + trainer.getName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));

        initView();
        loadMyCourses();
        setVisible(true);
    }

    private void initView() {
        // === 顶部选择栏 ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        add(topPanel, BorderLayout.NORTH);

        JLabel lbl = new JLabel("选择当前课程:");
        lbl.setFont(StyleUtils.FONT_TITLE);
        topPanel.add(lbl);

        courseBox = new JComboBox<>();
        courseBox.setPreferredSize(new Dimension(300, 35));
        courseBox.addActionListener(e -> loadStudents());
        topPanel.add(courseBox);

        JButton loadBtn = new JButton("📂 加载名单");
        StyleUtils.styleButton(loadBtn, StyleUtils.COLOR_PRIMARY);
        loadBtn.addActionListener(e -> loadStudents());
        topPanel.add(loadBtn);

        // === 中间学生列表 ===
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        centerPanel.setOpaque(false);

        infoLabel = new JLabel("请选择课程以查看预约学生...", SwingConstants.CENTER);
        infoLabel.setForeground(StyleUtils.COLOR_INFO);
        centerPanel.add(infoLabel, BorderLayout.NORTH);

        String[] cols = {"预约ID", "会员姓名", "手机号", "当前状态", "操作"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        studentTable = new JTable(tableModel);
        StyleUtils.styleTable(studentTable);

        // 双击点名
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) checkInStudent();
            }
        });

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // === 底部提示 ===
        JLabel tipLbl = new JLabel("💡 提示：双击学生行可进行 [签到/核销] 操作", SwingConstants.CENTER);
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        tipLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(tipLbl, BorderLayout.SOUTH);
    }

    // 内部类：用于 ComboBox 存储课程
    private static class CourseItem {
        Course c;
        public CourseItem(Course c) { this.c = c; }
        @Override public String toString() { return c.getName() + " (" + c.getCourseTime() + ")"; }
    }

    private void loadMyCourses() {
        courseBox.removeAllItems();
        // 假设 Service 有 getCoursesByTrainer
        List<Course> list = courseService.getCoursesByTrainer(trainer.getId());
        for (Course c : list) {
            courseBox.addItem(new CourseItem(c));
        }
        if (list.isEmpty()) {
            infoLabel.setText("您当前没有排课记录。");
        }
    }

    private void loadStudents() {
        CourseItem item = (CourseItem) courseBox.getSelectedItem();
        if (item == null) return;

        tableModel.setRowCount(0);
        // 获取该课程的所有预约
        List<Booking> bookings = bookingService.getBookingsByCourse(item.c.getCourseId());

        int count = 0;
        for (Booking b : bookings) {
            // 只显示 有效的预约 (待确认 或 已确认)
            // 已取消的就不显示了，或者显示为灰色
            if (!BookingService.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                // 获取会员详情
                service.BookingService.BookingDetail detail = bookingService.getBookingDetail(b.getBookingId());
                String memberName = (detail != null) ? detail.getMemberName() : "未知";
                String phone = (detail != null && detail.getMember() != null) ? detail.getMember().getPhone() : "-";

                String statusDisplay = "❓ " + b.getBookingStatus();
                if (BookingService.STATUS_CONFIRMED.equals(b.getBookingStatus())) statusDisplay = "✅ 已确认";
                if (BookingService.STATUS_PENDING.equals(b.getBookingStatus())) statusDisplay = "⏳ 待确认";

                tableModel.addRow(new Object[]{
                        b.getBookingId(), memberName, phone, statusDisplay, "双击操作"
                });
                count++;
            }
        }
        infoLabel.setText("当前课程: [" + item.c.getName() + "] - 共 " + count + " 人预约");
    }

    private void checkInStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) return;

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, 3);

        if (status.contains("已确认")) {
            JOptionPane.showMessageDialog(this, "该学生已经确认过了！");
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "确认学生 [" + name + "] 到场并参加课程？", "上课签到", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            BookingService.ServiceResult<Booking> res = bookingService.confirmBooking(bookingId);
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ 签到/确认成功！");
                loadStudents(); // 刷新状态
            } else {
                JOptionPane.showMessageDialog(this, "❌ 操作失败：" + res.getMessage());
            }
        }
    }
}