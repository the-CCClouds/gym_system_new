package Ui;

import entity.CheckIn;
import entity.Member;
import service.CheckInService;
import service.MemberService; // 引入 MemberService
import service.ServiceResult;
import utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class CheckInUi extends JFrame {

    private CheckInService checkInService;
    private MemberService memberService; // 新增：用于搜索

    private JTextField inputField;
    private JTextArea resultArea;

    public CheckInUi() {
        this.checkInService = new CheckInService();
        this.memberService = new MemberService(); // 初始化

        StyleUtils.initGlobalTheme();

        setTitle("✅ 会员进场签到");
        setSize(600, 480); // 稍微高一点，防止遮挡
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(StyleUtils.COLOR_BG);
        setLayout(null);

        initView();
        setVisible(true);
    }

    private void initView() {
        // 标题区
        JLabel iconLbl = new JLabel("👋", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLbl.setBounds(0, 30, 600, 70);
        add(iconLbl);

        JLabel titleLbl = new JLabel("会员进场签到", SwingConstants.CENTER);
        titleLbl.setFont(StyleUtils.FONT_TITLE_BIG);
        titleLbl.setForeground(StyleUtils.COLOR_TEXT_MAIN);
        titleLbl.setBounds(0, 100, 600, 40);
        add(titleLbl);

        // 输入区 (居中大框)
        JLabel tipLbl = new JLabel("支持输入：会员ID / 姓名 / 手机号", SwingConstants.CENTER);
        tipLbl.setFont(StyleUtils.FONT_NORMAL);
        tipLbl.setForeground(StyleUtils.COLOR_INFO);
        tipLbl.setBounds(0, 160, 600, 20);
        add(tipLbl);

        inputField = new JTextField();
        inputField.setBounds(150, 190, 300, 50); // 大输入框
        inputField.setFont(new Font("Arial", Font.BOLD, 20));
        inputField.setHorizontalAlignment(SwingConstants.CENTER);
        StyleUtils.styleTextField(inputField);

        // 回车直接签到
        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performCheckIn();
            }
        });
        add(inputField);

        JButton checkBtn = new JButton("搜索并签到");
        StyleUtils.styleButton(checkBtn, StyleUtils.COLOR_PRIMARY);
        checkBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        checkBtn.setBounds(150, 255, 300, 45);
        checkBtn.addActionListener(e -> performCheckIn());
        add(checkBtn);

        // 结果反馈区
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(StyleUtils.FONT_NORMAL);
        resultArea.setBackground(new Color(245, 247, 250));
        resultArea.setForeground(StyleUtils.COLOR_INFO);
        // 自动换行
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBounds(50, 330, 500, 80); // 加高一点
        scroll.setBorder(BorderFactory.createTitledBorder("操作日志"));
        add(scroll);
    }

    // 核心逻辑：智能搜索 + 签到
    private void performCheckIn() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            showMsg("⚠️ 请输入会员信息", false);
            return;
        }

        // 1. 调用 MemberService 进行综合搜索 (ID/名字/手机)
        // 你的 MemberService.search 方法已经实现了这个逻辑
        List<Member> list = memberService.search(text);

        if (list.isEmpty()) {
            showMsg("❌ 未找到会员：[" + text + "]", false);
            inputField.selectAll();
            return;
        }

        // 2. 判断搜索结果
        Member targetMember = null;

        if (list.size() == 1) {
            // 只有一个匹配，直接锁定
            targetMember = list.get(0);
        } else {
            // 找到多个 (比如重名)，弹窗让前台选
            MemberItem[] options = new MemberItem[list.size()];
            for (int i = 0; i < list.size(); i++) {
                options[i] = new MemberItem(list.get(i));
            }

            MemberItem selected = (MemberItem) JOptionPane.showInputDialog(
                    this,
                    "找到 " + list.size() + " 位匹配会员，请选择：",
                    "多重匹配确认",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (selected != null) {
                targetMember = selected.member;
            } else {
                showMsg("⚠️ 操作已取消", false); // 用户关掉了弹窗
                return;
            }
        }

        // 3. 执行签到 (使用锁定的 ID)
        if (targetMember != null) {
            CheckInService.ServiceResult<CheckIn> result = checkInService.checkIn(targetMember.getId());

            if (result.isSuccess()) {
                showMsg("✅ [" + targetMember.getName() + "] " + result.getMessage(), true);
                inputField.setText(""); // 成功后清空，方便下一个
                inputField.requestFocus();
            } else {
                showMsg("❌ [" + targetMember.getName() + "] 签到失败：" + result.getMessage(), false);
                inputField.selectAll();
            }
        }
    }

    private void showMsg(String msg, boolean success) {
        // 在底部追加日志，而不是覆盖，方便看历史
        String time = utils.DateUtils.formatDateTime(new java.util.Date()); // 假设你有这个工具方法，或者用 new Date().toString()
        // 简单起见，这里手动拼个时间
        String log = String.format("[%tT] %s\n", System.currentTimeMillis(), msg);

        resultArea.append(log);
        // 滚动到底部
        resultArea.setCaretPosition(resultArea.getDocument().getLength());

        // 也可以同时改变字体颜色提示当前状态(虽然TextArea只能单色，这里作为整体提示)
        if (!success) {
            // 如果失败，可以弹个声音或者把输入框变红一下
            inputField.setBackground(new Color(255, 235, 235));
        } else {
            inputField.setBackground(Color.WHITE);
        }
    }

    // 内部类：用于下拉框显示 (让名字更好看)
    private static class MemberItem {
        Member member;
        public MemberItem(Member m) { this.member = m; }
        @Override
        public String toString() {
            // 显示格式：张三 (ID:1001 | 13800000000)
            return member.getName() + " (ID:" + member.getId() + " | " + member.getPhone() + ")";
        }
    }
}