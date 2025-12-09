import Ui.LoginUi;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import utils.DBUtil;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // ==========================================
        //  大招 1：开启“一体化标题栏” (Window Decorations)
        //  让标题栏变成扁平风格，不再是 Windows 默认的那个白条
        // ==========================================
        System.setProperty("flatlaf.useWindowDecorations", "true");
        System.setProperty("flatlaf.menuBarEmbedded", "true");

        try {
            // ==========================================
            //  大招 2：选择更高级的皮肤 (四选一)
            // ==========================================

            // 选项 A: Mac 风格-亮色 (推荐！按钮圆润，看着很舒服)
            FlatMacLightLaf.setup();

            // 选项 B: Mac 风格-暗色 (如果你喜欢黑客风/夜间模式)
            // FlatMacDarkLaf.setup();

            // 选项 C: 经典的 FlatLaf 亮色 (你刚才用的那个)
            // FlatLightLaf.setup();

            // 选项 D: 经典的 FlatLaf 暗色
            // FlatDarkLaf.setup();

            System.out.println("✅ 皮肤加载成功");
        } catch (Exception e) {
            System.err.println("❌ 皮肤加载失败");
            e.printStackTrace();
        }

        // ==========================================
        //  大招 3：全局字体优化 (解决中文字体细、小的问题)
        // ==========================================
        optimizeFont();

        // 启动系统
        SwingUtilities.invokeLater(() -> {
            new LoginUi().LoginJFrame();
        });

        // 后台连接数据库
        new Thread(() -> {
            try {
                DBUtil.getConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 设置全局字体：使用微软雅黑，字号加大到 14 (默认12太小)
     */
    private static void optimizeFont() {
        // 这里的字体名字可以改，比如 "Microsoft YaHei", "SimHei" 等
        // 14 是字体大小，12 太费眼，14 刚刚好
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 14);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
            }
        }
    }
}