package Ui;

import com.toedter.calendar.JDateChooser; // 确保引入了 jcalendar 库
import entity.Employee;
import service.CourseService;
import service.EmployeeService;
import utils.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CourseAddUi extends JFrame {

    private CourseService courseService;
    private EmployeeService employeeService;
    private Runnable onSuccessCallback;

    // 表单组件
    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JTextField durationField;
    private JTextField capacityField;
    private JComboBox<TrainerItem> trainerCombo;

    // 日期和时间选择组件
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;

    public CourseAddUi(Runnable onSuccessCallback) {
        this.onSuccessCallback = onSuccessCallback;
        this.courseService = new CourseService();
        this.employeeService = new EmployeeService();

        this.setTitle("发布新课程");
        this.setSize(450, 580); // 调整高度以容纳所有组件
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(null);

        initView();
        this.setVisible(true);
    }

    private void initView() {
        int labelX = 50;
        int fieldX = 130;
        int startY = 30;
        int gap = 50;

        // 1. 课程名称
        addLabel("课程名称:", labelX, startY);
        nameField = new JTextField();
        nameField.setBounds(fieldX, startY, 220, 30);
        this.getContentPane().add(nameField);

        // 2. 课程类型
        addLabel("课程类型:", labelX, startY + gap);
        String[] types = {"yoga", "spinning", "pilates", "aerobics", "strength", "other"};
        typeCombo = new JComboBox<>(types);
        typeCombo.setBounds(fieldX, startY + gap, 220, 30);
        this.getContentPane().add(typeCombo);

        // 3. 时长
        addLabel("时长(分钟):", labelX, startY + gap * 2);
        durationField = new JTextField("60");
        durationField.setBounds(fieldX, startY + gap * 2, 220, 30);
        this.getContentPane().add(durationField);

        // 4. 容量
        addLabel("最大人数:", labelX, startY + gap * 3);
        capacityField = new JTextField("10");
        capacityField.setBounds(fieldX, startY + gap * 3, 220, 30);
        this.getContentPane().add(capacityField);

        // 5. 上课时间 (日期 + 时间)
        addLabel("上课时间:", labelX, startY + gap * 4);

        // (1) 左侧：日期选择器
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date()); // 默认今天
        dateChooser.setBounds(fieldX, startY + gap * 4, 130, 30);
        this.getContentPane().add(dateChooser);

        // (2) 右侧：时间微调器
        SpinnerDateModel model = new SpinnerDateModel();
        timeSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(editor);
        // 设置默认时间为 10:00
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(cal.getTime());

        timeSpinner.setBounds(fieldX + 140, startY + gap * 4, 80, 30);
        this.getContentPane().add(timeSpinner);

        // 提示文字
        JLabel hintLabel = new JLabel("请分别选择 日期 和 时间");
        hintLabel.setFont(new Font("宋体", Font.PLAIN, 12));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setBounds(fieldX, startY + gap * 4 + 30, 200, 20);
        this.getContentPane().add(hintLabel);

        // 6. 选择教练 (位置下移)
        int trainerY = startY + gap * 5 + 15;
        addLabel("授课教练:", labelX, trainerY);
        trainerCombo = new JComboBox<>();
        loadTrainers();
        trainerCombo.setBounds(fieldX, trainerY, 220, 30);
        this.getContentPane().add(trainerCombo);

        // 7. 按钮区域
        int btnY = trainerY + 60;
        JButton confirmBtn = new JButton("确认发布");
        confirmBtn.setBackground(new Color(100, 200, 100));
        confirmBtn.setBounds(100, btnY, 100, 40);
        confirmBtn.addActionListener(e -> performAdd());
        this.getContentPane().add(confirmBtn);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setBounds(240, btnY, 100, 40);
        cancelBtn.addActionListener(e -> this.dispose());
        this.getContentPane().add(cancelBtn);

        // 背景色
        JLabel bg = new JLabel();
        bg.setBounds(0, 0, 450, 580);
        bg.setBackground(new Color(245, 245, 245));
        bg.setOpaque(true);
        this.getContentPane().add(bg);
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        label.setBounds(x, y, 80, 30);
        this.getContentPane().add(label);
    }

    private void loadTrainers() {
        // 使用 EmployeeService 获取所有教练
        List<Employee> employees = employeeService.getEmployeesByRole(EmployeeService.ROLE_TRAINER);
        if (employees != null) {
            for (Employee emp : employees) {
                trainerCombo.addItem(new TrainerItem(emp.getId(), emp.getName()));
            }
        }
    }

    private void performAdd() {
        // 1. 获取基础输入
        String name = nameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String durationStr = durationField.getText().trim();
        String capacityStr = capacityField.getText().trim();
        TrainerItem selectedTrainer = (TrainerItem) trainerCombo.getSelectedItem();

        // 2. 基础校验
        if (name.isEmpty() || durationStr.isEmpty() || capacityStr.isEmpty() || selectedTrainer == null) {
            JOptionPane.showMessageDialog(this, "请填写所有必填信息！");
            return;
        }

        // 3. 获取并合并日期时间
        Date selectedDate = dateChooser.getDate();
        Date selectedTime = (Date) timeSpinner.getValue();

        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "请选择上课日期！");
            return;
        }

        // 合并逻辑：取日期的年月日 + 时间的时分
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(selectedDate);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(selectedTime);

        Calendar finalCal = Calendar.getInstance();
        finalCal.set(dateCal.get(Calendar.YEAR),
                dateCal.get(Calendar.MONTH),
                dateCal.get(Calendar.DAY_OF_MONTH),
                timeCal.get(Calendar.HOUR_OF_DAY),
                timeCal.get(Calendar.MINUTE),
                0); // 秒置0

        Date courseTime = finalCal.getTime();

        // 4. 提交数据
        try {
            int duration = Integer.parseInt(durationStr);
            int capacity = Integer.parseInt(capacityStr);
            int trainerId = selectedTrainer.id;

            // 调用 Service 创建课程
            CourseService.ServiceResult result = courseService.createCourse(
                    name, type, duration, capacity, trainerId, courseTime);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "课程发布成功！\n时间：" + DateUtils.formatDateTime(courseTime));
                if (onSuccessCallback != null) {
                    onSuccessCallback.run(); // 刷新父界面
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "发布失败：" + result.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "时长和容量必须是有效的数字！");
        }
    }

    // 内部类：用于下拉框显示
    private static class TrainerItem {
        int id;
        String name;

        public TrainerItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name; // JComboBox 显示这个名字
        }
    }
}