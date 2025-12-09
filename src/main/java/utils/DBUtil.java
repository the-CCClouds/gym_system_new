package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;

public class DBUtil {
    // 数据库配置信息
    private static final String URL = "jdbc:mysql://localhost:3306/gym_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    // 静态代码块：类加载时自动执行，只加载一次驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ 数据库驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ 数据库驱动加载失败！");
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     * @return Connection 对象
     * @throws SQLException 连接失败时抛出异常
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 测试数据库连接
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ 数据库连接测试成功！");
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败！");
            e.printStackTrace();
        }
    }

    // 可选：添加关闭连接的工具方法
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 密码哈希 - 使用 SHA-256 加密
     * 输入: 明文密码
     * 输出: Base64 编码的哈希值
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

}
