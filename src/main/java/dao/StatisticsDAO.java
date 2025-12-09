package dao;

import utils.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsDAO {

    /**
     * 获取总收入
     */
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM `order` WHERE payment_status LIKE 'paid%'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * 获取总会员数
     */
    public int getTotalMembers() {
        String sql = "SELECT COUNT(*) FROM member";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取今日订单数
     */
    public int getTodayOrderCount() {
        String sql = "SELECT COUNT(*) FROM `order` WHERE DATE(order_time) = CURDATE()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取库存紧张商品数
     */
    public int getLowStockProductCount() {
        String sql = "SELECT COUNT(*) FROM product WHERE stock < 10";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 【新增】获取各类型业务的营收占比 (用于画图)
     * 返回: Map<业务类型, 总金额>
     */
    public Map<String, Double> getRevenueByType() {
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT order_type, SUM(amount) FROM `order` WHERE payment_status LIKE 'paid%' GROUP BY order_type";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString(1);
                double amount = rs.getDouble(2);

                // 简单的类型名转换
                if ("membership".equalsIgnoreCase(type)) type = "会员卡/续费";
                else if ("product".equalsIgnoreCase(type)) type = "商品售卖";
                else if ("course".equalsIgnoreCase(type)) type = "课程预约";
                else if ("recharge".equalsIgnoreCase(type)) type = "余额充值";
                else if ("renewal".equalsIgnoreCase(type)) type = "续费业务";

                map.put(type, amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取最近订单 (用于表格)
     */
    public List<Map<String, Object>> getRecentOrders() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT o.order_id, m.name AS member_name, o.order_type, o.amount, o.order_time, o.payment_status " +
                "FROM `order` o " +
                "LEFT JOIN member m ON o.member_id = m.member_id " +
                "ORDER BY o.order_time DESC LIMIT 100"; // 查最近100条

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("order_id"));
                String name = rs.getString("member_name");
                map.put("name", name == null ? "散客" : name);
                map.put("type", rs.getString("order_type"));
                map.put("amount", rs.getDouble("amount"));
                map.put("time", rs.getTimestamp("order_time"));
                map.put("status", rs.getString("payment_status"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}