package Ui;

import entity.Member;

import javax.swing.*;
import java.awt.*;

public class InfoUi extends JFrame {

    public InfoUi(Member member) {
        // 1. 窗口基本设置
        this.setTitle("个人信息 - " + member.getName());
        this.setSize(400, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // 注意：这里用 DISPOSE，关闭时不退出整个程序
        this.getContentPane().setLayout(null);

        // 2. 初始化组件
        initView(member);

        this.setVisible(true);
    }

    private void initView(Member member) {
        int startY = 30;
        int gap = 40;
        int labelX = 50;
        int valueX = 150;

        // 标题
        JLabel titleLabel = new JLabel("会员档案");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        titleLabel.setBounds(140, 20, 150, 30);
        titleLabel.setForeground(new Color(50, 100, 200));
        this.getContentPane().add(titleLabel);

        startY += 50;

        // 姓名
        addInfoItem("姓名：", member.getName(), startY);

        // 手机
        addInfoItem("手机号：", member.getPhone(), startY + gap);

        // 邮箱
        addInfoItem("邮箱：", member.getEmail(), startY + gap * 2);

        // 性别
        // 注意：数据库存的是 male/female，显示时最好转中文，也可以直接显示
        String genderShow = "male".equals(member.getGender()) ? "男" : ("female".equals(member.getGender()) ? "女" : member.getGender());
        addInfoItem("性别：", genderShow, startY + gap * 3);

        // 生日 (需要处理日期格式，假设 member.getBirthDate() 返回 Date)
        String birthStr = member.getBirthDate() != null ? member.getBirthDate().toString() : "未填写";
        // 如果你有 DateUtils 工具类，可以用 DateUtils.format(member.getBirthDate())
        addInfoItem("生日：", birthStr, startY + gap * 4);

        // 状态
        addInfoItem("账号状态：", member.getStatus(), startY + gap * 5);

        // 注册时间
        String regStr = member.getRegisterDate() != null ? member.getRegisterDate().toString() : "";
        addInfoItem("注册时间：", regStr, startY + gap * 6);

        // 关闭按钮
        JButton closeBtn = new JButton("关闭");
        closeBtn.setBounds(140, 400, 100, 35);
        closeBtn.addActionListener(e -> this.dispose()); // Lambda写法，点击关闭当前窗口
        this.getContentPane().add(closeBtn);

        // 背景
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 400, 500);
        bg.setBackground(new Color(240, 248, 255));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    // 辅助方法：快速添加一行 "标签：值"
    private void addInfoItem(String labelText, String valueText, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        label.setBounds(50, y, 90, 30);
        this.getContentPane().add(label);

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        value.setBounds(140, y, 200, 30);
        this.getContentPane().add(value);
    }
}