package Ui;

import entity.Booking;
import entity.Member;
import service.BookingService;
import service.ServiceResult;
import utils.StyleUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyBookingUi extends JFrame {

    private Member member;
    private BookingService bookingService;

    private JTable bookingTable;
    private DefaultTableModel tableModel;

    public MyBookingUi(Member member) {
        this.member = member;
        this.bookingService = new BookingService();

        // 1. 初始化全局主题
        StyleUtils.initGlobalTheme();

        setTitle("📋 我的课程预约记录");
        setSize(900, 600); // 稍微宽一点，显示更多信息
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(new BorderLayout(15, 15));

        initView();
        loadMyBookings();
        setVisible(true);
    }

    private void initView() {
        // === 顶部标题栏 ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLbl = new JLabel("📅 我的预约历史");
        titleLbl.setFont(StyleUtils.FONT_TITLE);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);

        // 刷新按钮
        JButton refreshBtn = new JButton("🔄 刷新列表");
        StyleUtils.styleButton(refreshBtn, StyleUtils.COLOR_PRIMARY);
        refreshBtn.addActionListener(e -> loadMyBookings());

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // === 中间表格区域 ===
        // 使用白色背景容器包裹表格
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // 左右留白

        String[] cols = {"预约ID", "课程名称", "上课时间", "教练", "当前状态", "操作提示"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingTable = new JTable(tableModel);
        StyleUtils.styleTable(bookingTable); // 应用美化样式

        // 添加双击事件：取消预约
        bookingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) cancelBooking();
            }
        });

        JScrollPane scroll = new JScrollPane(bookingTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);

        centerPanel.add(scroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // === 底部提示栏 ===
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(StyleUtils.COLOR_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel tipLbl = new JLabel("💡 提示：双击表格中的记录可进行 [取消预约] 操作");
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        bottomPanel.add(tipLbl);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadMyBookings() {
        tableModel.setRowCount(0);
        // 获取该会员的所有预约
        List<Booking> list = bookingService.getBookingsByMember(member.getId());

        for (Booking b : list) {
            // 获取详细信息（利用 Service 中已有的 getBookingDetail 方法）
            service.BookingService.BookingDetail detail = bookingService.getBookingDetail(b.getBookingId());

            String courseName = "未知课程";
            String trainer = "-";
            String time = "-";

            if (detail != null) {
                courseName = detail.getCourseName();
                trainer = detail.getTrainerName();
                // 如果 Course 实体有 getCourseTime()，且不为空
                if (detail.getCourse() != null && detail.getCourse().getCourseTime() != null) {
                    time = utils.DateUtils.formatDateTime(detail.getCourse().getCourseTime());
                }
            }

            // 状态美化：将英文状态转换为中文+图标
            String statusRaw = b.getBookingStatus();
            String statusDisplay;

            if (BookingService.STATUS_CONFIRMED.equals(statusRaw)) {
                statusDisplay = "✅ 已确认";
            } else if (BookingService.STATUS_PENDING.equals(statusRaw)) {
                statusDisplay = "⏳ 待确认";
            } else if (BookingService.STATUS_CANCELLED.equals(statusRaw)) {
                statusDisplay = "⚪ 已取消";
            } else {
                statusDisplay = statusRaw;
            }

            // 操作提示列
            String actionTip = statusRaw.equals(BookingService.STATUS_CANCELLED) ? "-" : "双击取消";

            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    courseName,
                    time,
                    trainer,
                    statusDisplay,
                    actionTip
            });
        }
    }

    private void cancelBooking() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) return;

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String courseName = (String) tableModel.getValueAt(row, 1);
        String statusDisplay = (String) tableModel.getValueAt(row, 4);

        // 如果已经是取消状态，就别弹窗了
        if (statusDisplay.contains("已取消")) {
            JOptionPane.showMessageDialog(this, "该预约已经是取消状态了，无需重复操作。");
            return;
        }

        // 确认弹窗
        int opt = JOptionPane.showConfirmDialog(this,
                "确定要取消课程 [" + courseName + "] 的预约吗？\n取消后名额将释放给其他会员。",
                "取消确认", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            // 调用 Service 的成员取消方法

            BookingService.ServiceResult<Booking> res = bookingService.memberCancelBooking(member.getId(), bookingId);
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, "✅ 预约已成功取消！");
                loadMyBookings(); // 刷新表格
            } else {
                JOptionPane.showMessageDialog(this, "❌ 取消失败：" + res.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}