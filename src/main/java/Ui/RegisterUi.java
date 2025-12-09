package Ui;

import com.toedter.calendar.JDateChooser;
import entity.Member;
import service.MemberService;
import service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;


public class RegisterUi extends JFrame implements MouseListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton adminRadio;
    private JRadioButton memberRadio;
    private UserService userService;

    //创建注册的按钮
    JButton register = new JButton("注册");

    //创建用户名输入
    JTextField userJTextField = new JTextField();

    //创建密码输入
    JPasswordField passwordJTextField = new JPasswordField();

    //创建手机号输入
    JTextField phoneJTextField = new JTextField();

    //创建邮箱输入
    JTextField emailJTextField = new JTextField();

    //创建性别输入
    JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"男", "女"});

    //创建验证码
    String generate = utils.LoginUtils.generateVerificationCode();
    JLabel generateJlabel = new JLabel(generate);
    JLabel codeTextLabel = new JLabel("验证码"); // 稍微加个提示字或者图标

    JDateChooser birthdayChooser = new JDateChooser();




    // 【新增】创建验证码输入框 (用户填这里)
    JTextField codeJTextField = new JTextField();

    public void RegisterJFrame() {
        //在创建登录界面的时候,创建

        this.setSize(500, 500);


        //设置界面标题
        this.setTitle("健身系统注册页面");

        //设置界面居中


        this.setLocationRelativeTo(null);

        //设置空布局
        this.getContentPane().setLayout(null);


        components();
        //设置游戏的关闭模式
        //关闭一个页面就关闭所有页面

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setVisible(true);

    }

    public void components() {
            int leftMargin = 100;
            int labelWidth = 80;
            int fieldWidth = 220;
            int fieldHeight = 30;
            int verticalGap = 45;
            int startY = 50;

            // 用户名
            JLabel userJlabel = new JLabel("用户名");
            userJlabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            userJlabel.setBounds(leftMargin, startY, labelWidth, fieldHeight);
            this.getContentPane().add(userJlabel);

            userJTextField.setBounds(leftMargin + labelWidth, startY, fieldWidth, fieldHeight);
            userJTextField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            this.getContentPane().add(userJTextField);

            // 密码
            JLabel passwordJlabel = new JLabel("密码");
            passwordJlabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            passwordJlabel.setBounds(leftMargin, startY + verticalGap, labelWidth, fieldHeight);
            this.getContentPane().add(passwordJlabel);

            passwordJTextField.setBounds(leftMargin + labelWidth, startY + verticalGap, fieldWidth, fieldHeight);
            passwordJTextField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            this.getContentPane().add(passwordJTextField);

            // 手机号
            JLabel phoneJlabel = new JLabel("手机号");
            phoneJlabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            phoneJlabel.setBounds(leftMargin, startY + verticalGap * 2, labelWidth, fieldHeight);
            this.getContentPane().add(phoneJlabel);

            phoneJTextField.setBounds(leftMargin + labelWidth, startY + verticalGap * 2, fieldWidth, fieldHeight);
            phoneJTextField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            this.getContentPane().add(phoneJTextField);

            // 邮箱
            JLabel emailJlabel = new JLabel("邮箱");
            emailJlabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            emailJlabel.setBounds(leftMargin, startY + verticalGap * 3, labelWidth, fieldHeight);
            this.getContentPane().add(emailJlabel);

            emailJTextField.setBounds(leftMargin + labelWidth, startY + verticalGap * 3, fieldWidth, fieldHeight);
            emailJTextField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            this.getContentPane().add(emailJTextField);

            // 性别
            JLabel genderJlabel = new JLabel("性别");
            genderJlabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            genderJlabel.setBounds(leftMargin, startY + verticalGap * 4, labelWidth, fieldHeight);
            this.getContentPane().add(genderJlabel);

            genderComboBox.setBounds(leftMargin + labelWidth, startY + verticalGap * 4, fieldWidth, fieldHeight);
            genderComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            genderComboBox.setSelectedIndex(0);
            this.getContentPane().add(genderComboBox);

            // 生日
            JLabel birthdayJlabel = new JLabel("生日");
            birthdayJlabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            birthdayJlabel.setBounds(leftMargin, startY + verticalGap * 5, labelWidth, fieldHeight);
            this.getContentPane().add(birthdayJlabel);

            birthdayChooser.setBounds(leftMargin + labelWidth, startY + verticalGap * 5, fieldWidth, fieldHeight);
            birthdayChooser.setDateFormatString("yyyy-MM-dd");
            birthdayChooser.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            this.getContentPane().add(birthdayChooser);

            // 验证码
            codeTextLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            codeTextLabel.setBounds(leftMargin, startY + verticalGap * 6, labelWidth, fieldHeight);
            this.getContentPane().add(codeTextLabel);

            codeJTextField.setBounds(leftMargin + labelWidth, startY + verticalGap * 6, 120, fieldHeight);
            codeJTextField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            this.getContentPane().add(codeJTextField);

            generateJlabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            generateJlabel.setForeground(Color.RED);
            generateJlabel.setBounds(leftMargin + labelWidth + 130, startY + verticalGap * 6, 90, fieldHeight);
            generateJlabel.addMouseListener(this);
            this.getContentPane().add(generateJlabel);

            // 注册按钮
            register.addMouseListener(this);
            register.setFont(new Font("微软雅黑", Font.BOLD, 16));
            register.setBounds(leftMargin + labelWidth + 50, startY + verticalGap * 7, 120, 40);
            this.getContentPane().add(register);

            // 背景
            JLabel backgroundJlabel = new JLabel();
            backgroundJlabel.setBounds(0, 0, 488, 430);
            backgroundJlabel.setBackground(new Color(220, 235, 250));
            backgroundJlabel.setOpaque(true);
            this.getContentPane().add(backgroundJlabel);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getSource() == register) {
            // 获取输入值
            String name = userJTextField.getText().trim();
            String password = new String(passwordJTextField.getPassword()).trim();
            String phone = phoneJTextField.getText().trim();  // 改为 getText()
            String email = emailJTextField.getText().trim();  // 改为 getText()
            String code = codeJTextField.getText().trim();
            String genderDisplay = (String) genderComboBox.getSelectedItem();
            Date birthday = birthdayChooser.getDate();

            // 基本验证
            if (name.isEmpty() || password.isEmpty() || phone.isEmpty() ||
                    email.isEmpty() || birthday == null) {
                JOptionPane.showMessageDialog(this, "请填写所有必填信息！");
                return;
            }

            // 验证码检查
            if (!code.equalsIgnoreCase(generate)) {
                JOptionPane.showMessageDialog(this, "验证码错误，请重新输入。");
                codeJTextField.setText("");
                generate = utils.LoginUtils.generateVerificationCode();
                generateJlabel.setText(generate);
                return;
            }

            // 性别转换：UI显示"男"/"女" -> 数据库需要 "male"/"female"
            String gender = "男".equals(genderDisplay) ? "male" : "female";

            // 注册会员
            MemberService memberService = new MemberService();
            MemberService.ServiceResult<Member> result =
                    memberService.register(name, phone, email, gender, birthday);

            if (result.isSuccess()) {
                // 获取会员ID并创建用户账户
                int memberId = result.getData().getId();
                UserService userService = new UserService();
                UserService.ServiceResult userResult =
                        userService.registerMemberUser(memberId, name, password);

                if (userResult.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "注册成功！请登录。");
                    this.dispose();
                    LoginUi loginUi = new LoginUi();
                    loginUi.LoginJFrame();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "用户账户创建失败：" + userResult.getMessage());
                    clearForm();
                }
            } else {
                // 显示具体的失败原因
                JOptionPane.showMessageDialog(this,
                        "注册失败：" + result.getMessage());
                clearForm();
            }
        } else if (e.getSource() == generateJlabel) {
            // 刷新验证码
            generate = utils.LoginUtils.generateVerificationCode();
            generateJlabel.setText(generate);
        }

    }

    private void clearForm() {
        userJTextField.setText("");
        passwordJTextField.setText("");
        phoneJTextField.setText("");
        emailJTextField.setText("");
        codeJTextField.setText("");
        birthdayChooser.setDate(null);
        genderComboBox.setSelectedIndex(0);
        generate = utils.LoginUtils.generateVerificationCode();
        generateJlabel.setText(generate);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


}
