package Ui;

import entity.Booking;
import entity.Member;
import service.BookingService;
import service.BookingService.BookingDetail;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyBookingUi extends JFrame {

    private Member member;
    private BookingService bookingService;

    // 表格组件
    private JTable bookingTable;
    private DefaultTableModel tableModel;

    public MyBookingUi(Member member) {
        this.member = member;
        this.bookingService = new BookingService();

        // 1. 窗口基本设置
        this.setTitle("我的预约记录 - " + member.getName());
        this.setSize(800, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        // 2. 初始化组件
        initView();

        // 3. 加载数据
        loadBookingData();

        this.setVisible(true);
    }

    private void initView() {
        // 标题
        JLabel titleLabel = new JLabel("我的预约");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setBounds(30, 20, 150, 30);
        this.getContentPane().add(titleLabel);

        // 表格列名
        String[] columnNames = {"预约ID", "课程名称", "上课时间", "教练", "下单时间", "状态"}; // 【调整列】

        // 创建表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止编辑
            }
        };

        // 创建表格
        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setRowHeight(25);
        bookingTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 滚动面板
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBounds(30, 60, 720, 300);
        this.getContentPane().add(scrollPane);

        // 刷新按钮
        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.setBounds(30, 380, 120, 40);
        refreshBtn.addActionListener(e -> loadBookingData());
        this.getContentPane().add(refreshBtn);

        // 取消预约按钮
        JButton cancelBtn = new JButton("取消预约");
        cancelBtn.setFont(new Font("微软雅黑", Font.BOLD, 15));
        cancelBtn.setBounds(630, 380, 120, 40);
        cancelBtn.setBackground(new Color(220, 100, 100)); // 红色按钮
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> performCancel());
        this.getContentPane().add(cancelBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 800, 500);
        bg.setBackground(new Color(240, 248, 255));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    // 加载数据
    private void loadBookingData() {
        tableModel.setRowCount(0); // 清空

        // 获取该会员的所有预约
        List<Booking> bookings = bookingService.getBookingsByMember(member.getId());

        // 倒序遍历（最新的在最前面）
        for (int i = bookings.size() - 1; i >= 0; i--) {
            Booking booking = bookings.get(i);

            // 获取预约详情（为了拿到课程名和教练名）
            BookingDetail detail = bookingService.getBookingDetail(booking.getBookingId());
            String classTime = "未知";
            if (detail.getCourse() != null) {
                classTime =utils.DateUtils.formatDateTime(detail.getCourse().getCourseTime());
            }
            Object[] rowData = {
                    booking.getBookingId(),
                    detail.getCourseName(),
                    classTime,
                    detail.getTrainerName(),
                    // 格式化时间，如果 detail 里已经格式化好了就用 detail 的，否则用工具类
                    detail.getBookingTimeFormatted(),
                    detail.getStatusDisplayName() // 显示中文状态
            };
            tableModel.addRow(rowData);
        }
    }

    // 执行取消操作
    private void performCancel() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要取消的预约记录");
            return;
        }

        // 获取选中行的 ID 和 状态
        int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 4);

        // 检查状态
        if ("已取消".equals(status)) {
            JOptionPane.showMessageDialog(this, "该预约已经是取消状态了！");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要取消这条预约吗？", "确认取消", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 调用 Service 取消预约
            // 这里的 member.getId() 传入是为了验证身份，Service 中 memberCancelBooking 方法会检查
            BookingService.ServiceResult result = bookingService.memberCancelBooking(member.getId(), bookingId);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "取消成功！");
                loadBookingData(); // 刷新表格
            } else {
                JOptionPane.showMessageDialog(this, "取消失败：" + result.getMessage());
            }
        }
    }
}