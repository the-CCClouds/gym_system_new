package Ui;

import dao.EmployeeRoleDAO;
import entity.Employee;
import entity.Member;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MainUi extends JFrame implements MouseListener {

    // 保存当前登录的用户信息
    private String userType; // "member" 或 "employee"
    private Object userData; // Member 对象 或 Employee 对象

    // --- 通用组件 ---
    private JLabel welcomeLabel;
    private JButton logoutBtn = new JButton("退出登录");

    // --- 会员专属按钮 ---
    private JButton myProfileBtn = new JButton("个人信息");
    private JButton bookCourseBtn = new JButton("预约课程");
    private JButton myBookingsBtn = new JButton("我的预约");
    private JButton buyCardBtn = new JButton("会员卡/续费");

    // --- 员工专属按钮 ---
    // 1. 教练专用
    private JButton courseCheckInBtn = new JButton("上课点名");

    // 2. 前台/管理员通用 (运营功能)
    private JButton checkInBtn = new JButton("进场签到");
    private JButton courseManageBtn = new JButton("排课管理");
    private JButton memberManageBtn = new JButton("会员管理");

    // 3. 管理员专用 (人事功能)
    private JButton employeeManageBtn = new JButton("员工/人事管理");

    /**
     * 构造方法
     * @param userType 用户类型 "member" 或 "employee"
     * @param userData 具体的用户对象 (Member 或 Employee)
     */
    public MainUi(String userType, Object userData) {
        this.userType = userType;
        this.userData = userData;
        initView();
    }

    private void initView() {
        // 1. 基础窗口设置
        this.setSize(900, 600);
        this.setTitle("健身房管理系统 - 主页");
        this.setLocationRelativeTo(null); // 居中
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null); // 空布局

        // 2. 显示欢迎信息
        String name = "用户";
        String roleName = "访客";

        if ("member".equals(userType) && userData instanceof Member) {
            name = ((Member) userData).getName();
            roleName = "会员";
        } else if ("employee".equals(userType) && userData instanceof Employee) {
            name = ((Employee) userData).getName();
            roleName = ((Employee) userData).getRole(); // 获取具体角色(教练/前台/管理员)
        }

        welcomeLabel = new JLabel("欢迎回来，" + name + " [" + roleName + "]");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        welcomeLabel.setBounds(30, 20, 500, 30);
        this.getContentPane().add(welcomeLabel);

        // 3. 添加通用按钮 (退出登录)
        logoutBtn.setBounds(750, 20, 100, 30);
        logoutBtn.setBackground(new Color(255, 100, 100)); // 淡红色
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addMouseListener(this);
        this.getContentPane().add(logoutBtn);

        // 4. 根据用户类型加载不同的菜单按钮
        if ("member".equals(userType)) {
            loadMemberMenu();
        } else if ("employee".equals(userType)) {
            loadEmployeeMenu();
        }

        // 5. 设置背景
        JLabel background = new JLabel();
        background.setBounds(0, 0, 900, 600);
        background.setBackground(new Color(225, 240, 255)); // 淡蓝色背景
        background.setOpaque(true);
        this.getContentPane().add(background);

        this.setVisible(true);
    }

    // ==================== 会员菜单加载 ====================
    private void loadMemberMenu() {
        int startX = 100;
        int startY = 100;
        int btnWidth = 160;
        int btnHeight = 60;
        int gap = 40;

        // 第一排按钮
        // 1. 个人信息
        myProfileBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        myProfileBtn.setBounds(startX, startY, btnWidth, btnHeight);
        myProfileBtn.addMouseListener(this);
        this.getContentPane().add(myProfileBtn);

        // 2. 预约课程
        bookCourseBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        bookCourseBtn.setBounds(startX + btnWidth + gap, startY, btnWidth, btnHeight);
        bookCourseBtn.addMouseListener(this);
        this.getContentPane().add(bookCourseBtn);

        // 3. 我的预约
        myBookingsBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        myBookingsBtn.setBounds(startX + (btnWidth + gap) * 2, startY, btnWidth, btnHeight);
        myBookingsBtn.addMouseListener(this);
        this.getContentPane().add(myBookingsBtn);

        // 第二排按钮
        // 4. 购买会员卡
        buyCardBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        buyCardBtn.setBackground(new Color(255, 215, 0)); // 金色
        buyCardBtn.setBounds(startX, startY + btnHeight + gap, btnWidth, btnHeight);
        buyCardBtn.addMouseListener(this);
        this.getContentPane().add(buyCardBtn);

        //商品售卖


    }

    // ==================== 员工菜单加载 (权限分离) ====================
    private void loadEmployeeMenu() {
        if (!(userData instanceof Employee)) return;
        Employee emp = (Employee) userData;
        int roleId = emp.getRoleId();

        int x = 100;
        int y = 120;
        int w = 180;
        int h = 60;
        int gap = 40;

        // --- 角色权限判断 ---

        // 1. 教练权限 (Trainer)
        // 功能：上课点名
        if (roleId == EmployeeRoleDAO.ROLE_ID_TRAINER) {
            addSectionLabel("教练功能", x, y - 40);

            courseCheckInBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
            courseCheckInBtn.setBounds(x, y, w, h);
            courseCheckInBtn.addMouseListener(this);
            this.getContentPane().add(courseCheckInBtn);
        }

        // 2. 前台权限 (Receptionist)
        // 功能：进场签到、排课管理、会员管理
        else if (roleId == EmployeeRoleDAO.ROLE_ID_RECEPTIONIST) {
            addSectionLabel("前台运营", x, y - 40);

            // 第一排
            checkInBtn.setBounds(x, y, w, h);
            checkInBtn.addMouseListener(this);
            this.getContentPane().add(checkInBtn);

            courseManageBtn.setBounds(x + w + gap, y, w, h);
            courseManageBtn.addMouseListener(this);
            this.getContentPane().add(courseManageBtn);

            memberManageBtn.setBounds(x + (w + gap) * 2, y, w, h);
            memberManageBtn.addMouseListener(this);
            this.getContentPane().add(memberManageBtn);
            // 第二排：增值业务 (商品售卖)
            int y2 = y + h + gap; // 下移一行

            JButton shopBtn = new JButton("商品售卖 (POS)");
            shopBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
            shopBtn.setBackground(new Color(255, 230, 200)); // 淡橙色高亮
            shopBtn.setBounds(x, y2, w, h); // 放在第二排第一个
            shopBtn.addActionListener(e -> new ShopUi());
            this.getContentPane().add(shopBtn);
        }

        // 3. 管理员权限 (Admin)
        // 功能：所有功能 + 员工管理
        else if (roleId == EmployeeRoleDAO.ROLE_ID_ADMIN) {
            addSectionLabel("综合管理 (管理员)", x, y - 40);

            // 第一排：业务运营
            checkInBtn.setBounds(x, y, w, h);
            checkInBtn.addMouseListener(this);
            this.getContentPane().add(checkInBtn);

            courseManageBtn.setBounds(x + w + gap, y, w, h);
            courseManageBtn.addMouseListener(this);
            this.getContentPane().add(courseManageBtn);

            memberManageBtn.setBounds(x + (w + gap) * 2, y, w, h);
            memberManageBtn.addMouseListener(this);
            this.getContentPane().add(memberManageBtn);

            // 第二排：高级管理 & 教学
            int y2 = y + h + gap;

            courseCheckInBtn.setText("上课点名 (代教)");
            courseCheckInBtn.setBounds(x, y2, w, h);
            courseCheckInBtn.addMouseListener(this);
            this.getContentPane().add(courseCheckInBtn);

            employeeManageBtn.setText("员工/人事管理");
            employeeManageBtn.setBackground(new Color(255, 150, 150)); // 特殊颜色
            employeeManageBtn.setBounds(x + w + gap, y2, w, h);
            employeeManageBtn.addMouseListener(this);
            this.getContentPane().add(employeeManageBtn);
        }
    }

    // 辅助方法：添加小标题
    private void addSectionLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        label.setForeground(Color.GRAY);
        label.setBounds(x, y, 200, 30);
        this.getContentPane().add(label);
    }

    // ==================== 事件处理 ====================
    @Override
    public void mouseClicked(MouseEvent e) {
        // --- 退出登录 ---
        if (e.getSource() == logoutBtn) {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要退出吗？", "退出", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose(); // 关闭主界面
                new LoginUi().LoginJFrame(); // 返回登录界面
            }
        }

        // --- 会员功能 ---
        else if (e.getSource() == myProfileBtn) {
            if (userData instanceof Member) {
                new Ui.InfoUi((Member) userData).setVisible(true);
            }
        } else if (e.getSource() == bookCourseBtn) {
            if (userData instanceof Member) {
                new BookCourseUi((Member) userData);
            }
        } else if (e.getSource() == myBookingsBtn) {
            if (userData instanceof Member) {
                new Ui.MyBookingUi((Member) userData);
            }
        } else if (e.getSource() == buyCardBtn) {
            if (userData instanceof Member) {
                new Ui.BuyCardUi((Member) userData);
            }
        }

        // --- 员工功能 ---
        // 1. 进场签到 (前台/管理员)
        else if (e.getSource() == checkInBtn) {
            new CheckInUi();
        }
        // 2. 排课管理 (前台/管理员)
        else if (e.getSource() == courseManageBtn) {
            if (userData instanceof Employee) {
                new Ui.CourseManageUi((Employee) userData);
            }
        }
        // 3. 会员管理 (前台/管理员)
        else if (e.getSource() == memberManageBtn) {
            new Ui.MemberManageUi();
        }
        // 4. 上课点名 (教练/管理员)
        else if (e.getSource() == courseCheckInBtn) {
            if (userData instanceof Employee) {
                new Ui.CourseAttendanceUi((Employee) userData);
            }
        }
        // 5. 员工管理 (管理员独有)
        else if (e.getSource() == employeeManageBtn) {
            new Ui.EmployeeManageUi();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}