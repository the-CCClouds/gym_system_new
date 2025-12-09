package Ui;

import service.UserService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LoginUi extends JFrame implements MouseListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserService userService;
    //创建登录按钮
    JButton loginJbutton = new JButton("登录");

    //创建注册的按钮
    JButton register = new JButton("注册");

    //创建用户名输入
    JTextField userJTextField = new JTextField();

    //创建密码输入
    JPasswordField passwordJTextField = new JPasswordField();

    //创建验证码
    String gennerate= utils.LoginUtils.generateVerificationCode();
    JLabel gennerateJlabel = new JLabel(gennerate);
    JLabel codeTextLabel = new JLabel("验证码"); // 稍微加个提示字或者图标

    // 【新增】创建验证码输入框 (用户填这里)
    JTextField codeJTextField = new JTextField();

    public void LoginJFrame() {
        //在创建登录界面的时候,创建

        this.setSize(488, 430);


        //设置界面标题
        this.setTitle("健身系统登录页面");

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
        //用户名添加
        JLabel userJlabel = new JLabel("用户名");
        userJlabel.setBounds(116, 135, 51, 19);
        this.getContentPane().add(userJlabel);


        //创建用户名的输入框

        userJTextField.setBounds(195, 134, 200, 30);
        this.getContentPane().add(userJTextField);

        //创建密码文字
        JLabel passwordJlabel = new JLabel("密码");
        passwordJlabel.setBounds(130, 195, 35, 18);
        this.getContentPane().add(passwordJlabel);

        //创建密码的文本输入框

        passwordJTextField.setBounds(195, 195, 200, 30);
        this.getContentPane().add(passwordJTextField);


        //创建登录按钮
        loginJbutton.addMouseListener(this);
        loginJbutton.setBounds(133, 310, 90, 40);
        this.getContentPane().add(loginJbutton);


        //创建注册的按钮
        register.addMouseListener(this);
        register.setBounds(256, 310, 90, 40);
        this.getContentPane().add(register);

        //验证码输入框


        codeTextLabel.setBounds(130, 256, 50, 30);
        this.getContentPane().add(codeTextLabel);

        codeJTextField.setBounds(195, 256, 100, 30); // 输入框宽度100
        this.getContentPane().add(codeJTextField);

        //生成验证码并呈现

        gennerateJlabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        gennerateJlabel.setForeground(Color.RED); // 红色醒目
        gennerateJlabel.setBounds(305, 256, 90, 30);//设置坐标
        gennerateJlabel.addMouseListener(this);//添加监听
        this.getContentPane().add(gennerateJlabel);

        //背景色创建
        JLabel backgroundJlabel = new JLabel();
        backgroundJlabel.setBounds(0, 0, 470, 390);
        backgroundJlabel.setBackground(new Color(220, 235, 250));
        backgroundJlabel.setOpaque(true);
//        // 将背景添加到索引 0 的位置，这在 Swing 中意味着最底层。
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

        if (e.getSource() == gennerateJlabel) {
            gennerate = utils.LoginUtils.generateVerificationCode();
            gennerateJlabel.setText(gennerate);
        }

        if (e.getSource() == loginJbutton) {
            UserService userService = new UserService();
            String username = userJTextField.getText();
            String password = new String(passwordJTextField.getPassword());
            String code = codeJTextField.getText();
            if (!code.equalsIgnoreCase(gennerate.trim())) {
                JOptionPane.showMessageDialog(this, "验证码错误！");
                userJTextField.setText("");
                passwordJTextField.setText("");
                gennerate= utils.LoginUtils.generateVerificationCode();
                gennerateJlabel.setText(gennerate);
                codeJTextField.setText("");
                return;
            }

            UserService.LoginResult result = userService.login(username, password);

            // 然后判断 result 是否成功
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "登录成功！");
                this.dispose(); // 关闭登录界面

                new MainUi(result.getUserType(), result.getUserData());

            }else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！", "登录失败",JOptionPane.ERROR_MESSAGE);
                userJTextField.setText("");
                passwordJTextField.setText("");
                gennerate= utils.LoginUtils.generateVerificationCode();
                gennerateJlabel.setText(gennerate);
                codeJTextField.setText("");
            }
        }

        if (e.getSource() == register) {
            this.dispose();
            RegisterUi registerUi = new RegisterUi();
            registerUi.RegisterJFrame();
        }



    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


}
