package Ui;

import dao.EmployeeRoleDAO;
import entity.Employee;
import entity.Member;
import utils.StyleUtils; // 确保引入了样式工具类

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainUi extends JFrame {

    private String userType;
    private Object userData;

    public MainUi(String userType, Object userData) {
        this.userType = userType;
        this.userData = userData;

        // 1. 初始化全局皮肤
        StyleUtils.initGlobalTheme();

        initView();
    }

    private void initView() {
        // 窗口设置 (稍微加高一点以适应分区)
        this.setSize(1000, 750);
        this.setTitle("💪 健身房智能管理系统 Pro");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.getContentPane().setBackground(StyleUtils.COLOR_BG);

        // ================= 顶部导航栏 =================
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 1000, 70);
        header.setBackground(Color.WHITE);
        this.getContentPane().add(header);

        // Logo
        JLabel logo = new JLabel("🏋️ Gym System");
        logo.setFont(StyleUtils.FONT_TITLE_BIG);
        logo.setForeground(StyleUtils.COLOR_PRIMARY);
        logo.setBounds(30, 15, 250, 40);
        header.add(logo);

        // 用户信息
        String welcomeText = "欢迎，访客";
        if ("member".equals(userType) && userData instanceof Member) {
            welcomeText = "👋 欢迎回来，" + ((Member) userData).getName() + " (会员)";
        } else if ("employee".equals(userType) && userData instanceof Employee) {
            welcomeText = "👋 工作愉快，" + ((Employee) userData).getName() + " (" + ((Employee) userData).getRole() + ")";
        }
        JLabel userLbl = new JLabel(welcomeText);
        userLbl.setFont(StyleUtils.FONT_NORMAL);
        userLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        userLbl.setBounds(500, 20, 300, 30);
        header.add(userLbl);

        // 退出按钮
        JButton logoutBtn = new JButton("退出 ❌");
        StyleUtils.styleButton(logoutBtn, StyleUtils.COLOR_DANGER);
        logoutBtn.setBounds(820, 18, 100, 35);
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "确定退出吗?", "退出", JOptionPane.YES_NO_OPTION) == 0) {
                dispose();
                new LoginUi().LoginJFrame();
            }
        });
        header.add(logoutBtn);

        // 分割线
        JSeparator sep = new JSeparator();
        sep.setBounds(0, 70, 1000, 1);
        sep.setForeground(Color.LIGHT_GRAY);
        this.getContentPane().add(sep);

        // ================= 菜单加载 =================
        if ("member".equals(userType)) {
            loadMemberMenu();
        } else if ("employee".equals(userType)) {
            loadEmployeeMenu();
        }

        this.setVisible(true);
    }

    // ==================== 1. 会员菜单 ====================
    private void loadMemberMenu() {
        int startX = 100;
        int startY = 120;
        int gap = 200;

        addSectionTitle("我的服务", startX, startY - 30);

        createMenuBtn("个人信息", "👤", StyleUtils.COLOR_PRIMARY, startX, startY,
                () -> new Ui.InfoUi((Member) userData).setVisible(true));

        createMenuBtn("预约课程", "📅", StyleUtils.COLOR_SUCCESS, startX + gap, startY,
                () -> new BookCourseUi((Member) userData));

        createMenuBtn("我的预约", "📋", StyleUtils.COLOR_WARNING, startX + gap * 2, startY,
                () -> new Ui.MyBookingUi((Member) userData));

        // 第二排
        createMenuBtn("会员卡/续费", "💳", StyleUtils.COLOR_DANGER, startX, startY + 120, () -> {
            Member mem = (Member) userData;
            dao.MembershipCardDAO cardDAO = new dao.MembershipCardDAO();
            if (cardDAO.hasMemberValidCard(mem.getId())) {
                new RenewUi(this, mem, false);
            } else {
                JOptionPane.showMessageDialog(this, "您当前没有有效的会员卡。\n请前往前台办理。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // ==================== 2. 员工菜单 (布局优化版) ====================
    private void loadEmployeeMenu() {
        if (!(userData instanceof Employee)) return;
        Employee emp = (Employee) userData;
        int roleId = emp.getRoleId();

        int x = 80;
        int y = 140;
        int w = 200; // 按钮宽度
        int h = 100; // 按钮高度 (加大)
        int gapX = 50;
        int gapY = 60; // 行间距

        // --- 教练权限 ---
        if (roleId == EmployeeRoleDAO.ROLE_ID_TRAINER) {
            addSectionTitle("🏋️ 教练工作台", x, y - 30);
            createMenuBtn("上课点名", "📝", StyleUtils.COLOR_PRIMARY, x, y,
                    () -> new Ui.CourseAttendanceUi((Employee) userData));
        }

        // --- 前台权限 (重点优化) ---
        else if (roleId == EmployeeRoleDAO.ROLE_ID_RECEPTIONIST) {

            // 第一排：高频业务 (签到、收银、充值)
            addSectionTitle("🔥 前台高频业务", x, y - 30);
            createMenuBtn("进场签到", "✅", StyleUtils.COLOR_PRIMARY, x, y, () -> new CheckInUi());
            createMenuBtn("商品售卖", "🛒", StyleUtils.COLOR_WARNING, x + w + gapX, y, () -> new ShopUi());
            createMenuBtn("余额充值", "💰", StyleUtils.COLOR_SUCCESS, x + (w + gapX) * 2, y, () -> new RechargeUi());

            // 第二排：会籍服务 (会员、开卡、续费)
            int y2 = y + h + gapY;
            addSectionTitle("👥 会籍与会员服务", x, y2 - 30);
            createMenuBtn("会员管理", "📂", StyleUtils.COLOR_INFO, x, y2, () -> new Ui.MemberManageUi());
            createMenuBtn("新会员开卡", "🆕", StyleUtils.COLOR_DANGER, x + w + gapX, y2, () -> handleStaffCardAction("buy"));
            createMenuBtn("会员续费", "🔄", StyleUtils.COLOR_DANGER, x + (w + gapX) * 2, y2, () -> handleStaffCardAction("renew"));

            // 第三排：后台管理 (排课、库存)
            int y3 = y2 + h + gapY;
            addSectionTitle("📦 后台管理", x, y3 - 30);
            createMenuBtn("排课管理", "📅", StyleUtils.COLOR_INFO, x, y3, () -> new Ui.CourseManageUi((Employee) userData));
            createMenuBtn("库存管理", "📊", StyleUtils.COLOR_INFO, x + w + gapX, y3, () -> new ProductManageUi());
        }

        // --- 管理员权限 ---
        else if (roleId == EmployeeRoleDAO.ROLE_ID_ADMIN) {
            addSectionTitle("🛡️ 综合管理控制台", x, y - 30);

            // 第一排：基础运营
            createMenuBtn("进场签到", "✅", StyleUtils.COLOR_PRIMARY, x, y, () -> new CheckInUi());
            createMenuBtn("排课管理", "📅", StyleUtils.COLOR_PRIMARY, x + w + gapX, y, () -> new Ui.CourseManageUi((Employee) userData));
            createMenuBtn("会员管理", "👥", StyleUtils.COLOR_PRIMARY, x + (w + gapX) * 2, y, () -> new Ui.MemberManageUi());
            createMenuBtn("员工/人事", "👔", StyleUtils.COLOR_DANGER, x + (w + gapX) * 3, y, () -> new Ui.EmployeeManageUi());

            // 第二排：业务与收银
            int y2 = y + h + gapY;
            createMenuBtn("上课点名", "📝", StyleUtils.COLOR_INFO, x, y2, () -> new Ui.CourseAttendanceUi((Employee) userData));
            createMenuBtn("库存管理", "📦", StyleUtils.COLOR_WARNING, x + w + gapX, y2, () -> new ProductManageUi());
            createMenuBtn("商品售卖", "🛒", StyleUtils.COLOR_SUCCESS, x + (w + gapX) * 2, y2, () -> new ShopUi());
            createMenuBtn("余额充值", "💰", StyleUtils.COLOR_SUCCESS, x + (w + gapX) * 3, y2, () -> new RechargeUi());

            // 第三排：决策与高级
            int y3 = y2 + h + gapY;
            createMenuBtn("开卡/续费", "💳", StyleUtils.COLOR_DANGER, x, y3, () -> {
                String[] options = {"新会员开卡", "老会员续费"};
                int choice = JOptionPane.showOptionDialog(this, "请选择业务类型:", "会籍业务",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if (choice == 0) handleStaffCardAction("buy");
                if (choice == 1) handleStaffCardAction("renew");
            });

            createMenuBtn("经营报表", "📊", new Color(100, 100, 255), x + w + gapX, y3, () -> new ReportUi());
        }
    }

    // ==================== 辅助方法 ====================

    private void createMenuBtn(String text, String icon, Color color, int x, int y, Runnable action) {
        // 使用 HTML 实现图标在上，文字在下
        String html = "<html><center><font size='6'>" + icon + "</font><br><font size='4'>" + text + "</font></center></html>";
        JButton btn = new JButton(html);

        btn.setBounds(x, y, 180, 90); // 统一大按钮
        StyleUtils.styleButton(btn, color);

        // 悬停变色
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });

        btn.addActionListener(e -> action.run());
        this.getContentPane().add(btn);
    }

    private void addSectionTitle(String title, int x, int y) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(StyleUtils.FONT_BOLD);
        lbl.setForeground(Color.GRAY);
        lbl.setBounds(x, y, 300, 30);
        this.getContentPane().add(lbl);
    }

    // 员工开卡/续费辅助逻辑
    private void handleStaffCardAction(String actionType) {
        String input = JOptionPane.showInputDialog(this, "请输入会员手机号或ID:");
        if (input == null || input.trim().isEmpty()) return;

        service.MemberService ms = new service.MemberService();
        java.util.List<Member> list = ms.search(input);

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到该会员！请先在「会员管理」中注册。");
            return;
        }
        Member targetMember = list.get(0);
        if ("buy".equals(actionType)) {
            dao.MembershipCardDAO cardDAO = new dao.MembershipCardDAO();
            if (cardDAO.hasMemberValidCard(targetMember.getId())) {
                JOptionPane.showMessageDialog(this, "该会员已有有效卡！请使用续费功能。");
            } else {
                new Ui.BuyCardUi(targetMember);
            }
        } else if ("renew".equals(actionType)) {
            new RenewUi(this, targetMember, true);
        }
    }
}