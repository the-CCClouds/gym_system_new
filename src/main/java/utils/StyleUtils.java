package utils;

import com.formdev.flatlaf.FlatLightLaf; // 关键：导入 FlatLaf

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class StyleUtils {

    // ==================== 1. 配色方案 (保持不变) ====================
    public static final Color COLOR_PRIMARY   = new Color(64, 158, 255);
    public static final Color COLOR_SUCCESS   = new Color(103, 194, 58);
    public static final Color COLOR_WARNING   = new Color(230, 162, 60);
    public static final Color COLOR_DANGER    = new Color(245, 108, 108);
    public static final Color COLOR_INFO      = new Color(144, 147, 153);
    public static final Color COLOR_TEXT_MAIN = new Color(48, 49, 51);
    public static final Color COLOR_BG        = new Color(245, 247, 250);
    public static final Color COLOR_WHITE     = Color.WHITE;

    // ==================== 2. 全局字体 (保持不变) ====================
    public static final Font FONT_TITLE_BIG  = new Font("微软雅黑", Font.BOLD, 24);
    public static final Font FONT_TITLE      = new Font("微软雅黑", Font.BOLD, 18);
    public static final Font FONT_NORMAL     = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font FONT_BOLD       = new Font("微软雅黑", Font.BOLD, 14);

    /**
     * 初始化全局皮肤 (适配 FlatLaf)
     */
    public static void initGlobalTheme() {
        try {
            // 1. 尝试启动 FlatLaf (这是最关键的一步)
            // 如果你没有导包，请确保 pom.xml 或库里有 flatlaf.jar
            FlatLightLaf.setup();

            // 2. 覆盖默认字体 (FlatLaf 允许这样微调)
            UIManager.put("defaultFont", FONT_NORMAL);
            UIManager.put("Button.font", FONT_BOLD);
            UIManager.put("Label.font", FONT_NORMAL);
            UIManager.put("TableHeader.font", FONT_BOLD);

            // 3. 微调一些 FlatLaf 的颜色变量 (可选)
            UIManager.put("Button.arc", 10); // 按钮圆角
            UIManager.put("Component.arc", 10); // 组件圆角
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);

        } catch (Exception e) {
            e.printStackTrace();
            // 如果 FlatLaf 启动失败，回退到系统默认
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 美化按钮 (适配 FlatLaf)
     */
    public static void styleButton(JButton btn, Color bgColor) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FlatLaf 特有属性：去掉边框但保留圆角效果
        btn.putClientProperty("JButton.buttonType", "roundRect");

        // 如果想让按钮完全扁平没有阴影/边框：
        // btn.setBorderPainted(false);
        // btn.setFocusPainted(false);
    }

    /**
     * 美化输入框
     */
    public static void styleTextField(JTextField field) {
        field.setFont(FONT_NORMAL);
        field.setForeground(COLOR_TEXT_MAIN);
        // FlatLaf 自带好康的边框，不需要我们手动 new LineBorder 了，否则会很丑
        // 我们只加一点内边距即可
        field.setMargin(new Insets(5, 5, 5, 5));
    }

    /**
     * 美化表格
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(35); //稍微高一点
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        // FlatLaf 会自动处理选中颜色，我们只需要设置字体
        table.setFont(FONT_NORMAL);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(242, 242, 242));

        // 内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }
}