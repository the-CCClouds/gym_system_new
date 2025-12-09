package dao;

import entity.Order;
import utils.DBUtil;
import utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据访问对象
 * 对应数据库 order 表
 */
public class OrderDAO {

    // ========== 订单类型常量 ==========
    public static final String TYPE_MEMBERSHIP = "membership";
    public static final String TYPE_PRODUCT = "product";
    public static final String TYPE_COURSE = "course";

    // ========== 支付状态常量 ==========
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PAID = "paid";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_REFUNDED = "refunded";

    // 有效的订单类型
    public static final String[] VALID_TYPES = {TYPE_MEMBERSHIP, TYPE_PRODUCT, TYPE_COURSE};

    // 有效的支付状态
    public static final String[] VALID_STATUSES = {STATUS_PENDING, STATUS_PAID, STATUS_CANCELLED, STATUS_REFUNDED};

    public OrderDAO() {
    }

    /**
     * 从结果集中提取订单信息
     */
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setMemberId(rs.getInt("member_id"));
        order.setOrderType(rs.getString("order_type"));
        order.setAmount(rs.getDouble("amount"));
        order.setOrderTime(rs.getTimestamp("order_time"));
        order.setPaymentStatus(rs.getString("payment_status"));
        return order;
    }

    /**
     * 验证订单类型是否有效
     */
    private boolean isValidType(String type) {
        if (type == null) return false;
        for (String validType : VALID_TYPES) {
            if (validType.equals(type)) return true;
        }
        return false;
    }

    /**
     * 验证支付状态是否有效
     */
    private boolean isValidStatus(String status) {
        if (status == null) return false;
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equals(status)) return true;
        }
        return false;
    }

    // ========== 基础 CRUD ==========

    /**
     * 创建订单
     *
     * @param order 订单对象
     * @return 是否创建成功
     */
    public boolean addOrder(Order order) {
        if (order == null) {
            System.err.println("添加失败：订单对象为空");
            return false;
        }
        if (!isValidType(order.getOrderType())) {
            System.err.println("添加失败：无效的订单类型 (type=" + order.getOrderType() + ")");
            return false;
        }
        if (order.getAmount() < 0) {
            System.err.println("添加失败：金额不能为负数");
            return false;
        }


        String sql = "INSERT INTO `order` (member_id, order_type, amount, order_time, payment_status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // 原来的写法（会导致散客购买失败）：
            // pstmt.setInt(1, order.getMemberId());

            // ✅ 修改后的写法（支持散客）：
            if (order.getMemberId() <= 0) {
                // 如果 ID 是 0 或负数，就在数据库里存 NULL (表示没有会员)
                pstmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                // 如果有具体的 ID，就存进去
                pstmt.setInt(1, order.getMemberId());
            }
            pstmt.setString(2, order.getOrderType());
            pstmt.setDouble(3, order.getAmount());
            pstmt.setTimestamp(4, order.getOrderTime() != null ? 
                    DateUtils.toSqlTimestamp(order.getOrderTime()) : DateUtils.nowTimestamp());
            pstmt.setString(5, order.getPaymentStatus() != null ? 
                    order.getPaymentStatus() : STATUS_PENDING);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    }
                }
            }
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单对象，不存在返回null
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM `order` WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractOrderFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有订单
     *
     * @return 订单列表
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `order` ORDER BY order_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * 更新订单
     *
     * @param order 订单对象
     * @return 是否更新成功
     */
    public boolean updateOrder(Order order) {
        if (order == null) {
            System.err.println("更新失败：订单对象为空");
            return false;
        }
        if (!isValidType(order.getOrderType())) {
            System.err.println("更新失败：无效的订单类型");
            return false;
        }
        if (!isValidStatus(order.getPaymentStatus())) {
            System.err.println("更新失败：无效的支付状态");
            return false;
        }

        String sql = "UPDATE `order` SET member_id = ?, order_type = ?, amount = ?, order_time = ?, payment_status = ? WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, order.getMemberId());
            pstmt.setString(2, order.getOrderType());
            pstmt.setDouble(3, order.getAmount());
            pstmt.setTimestamp(4, DateUtils.toSqlTimestamp(order.getOrderTime()));
            pstmt.setString(5, order.getPaymentStatus());
            pstmt.setInt(6, order.getOrderId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM `order` WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== 查询功能 ==========

    /**
     * 查询会员的所有订单
     *
     * @param memberId 会员ID
     * @return 订单列表
     */
    public List<Order> getOrdersByMemberId(int memberId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `order` WHERE member_id = ? ORDER BY order_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(extractOrderFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * 按订单类型查询
     *
     * @param orderType 订单类型（membership/product/course）
     * @return 订单列表
     */
    public List<Order> getOrdersByType(String orderType) {
        List<Order> orders = new ArrayList<>();
        if (!isValidType(orderType)) {
            return orders;
        }

        String sql = "SELECT * FROM `order` WHERE order_type = ? ORDER BY order_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(extractOrderFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * 按支付状态查询
     *
     * @param status 支付状态（pending/paid/cancelled/refunded）
     * @return 订单列表
     */
    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        if (!isValidStatus(status)) {
            return orders;
        }

        String sql = "SELECT * FROM `order` WHERE payment_status = ? ORDER BY order_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(extractOrderFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * 查询今日订单
     *
     * @return 今日订单列表
     */
    public List<Order> getTodayOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `order` WHERE DATE(order_time) = CURDATE() ORDER BY order_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // ========== 状态管理 ==========

    /**
     * 更新支付状态
     *
     * @param orderId 订单ID
     * @param status  新状态
     * @return 是否更新成功
     */
    public boolean updatePaymentStatus(int orderId, String status) {
        if (!isValidStatus(status)) {
            System.err.println("更新失败：无效的支付状态 (status=" + status + ")");
            return false;
        }

        String sql = "UPDATE `order` SET payment_status = ? WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @return 是否操作成功
     */
    public boolean payOrder(int orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            System.err.println("支付失败：订单不存在");
            return false;
        }
        if (!STATUS_PENDING.equals(order.getPaymentStatus())) {
            System.err.println("支付失败：订单状态不是待支付 (status=" + order.getPaymentStatus() + ")");
            return false;
        }
        return updatePaymentStatus(orderId, STATUS_PAID);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否操作成功
     */
    public boolean cancelOrder(int orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            System.err.println("取消失败：订单不存在");
            return false;
        }
        if (!STATUS_PENDING.equals(order.getPaymentStatus())) {
            System.err.println("取消失败：只能取消待支付的订单 (status=" + order.getPaymentStatus() + ")");
            return false;
        }
        return updatePaymentStatus(orderId, STATUS_CANCELLED);
    }

    /**
     * 退款
     *
     * @param orderId 订单ID
     * @return 是否操作成功
     */
    public boolean refundOrder(int orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            System.err.println("退款失败：订单不存在");
            return false;
        }
        if (!STATUS_PAID.equals(order.getPaymentStatus())) {
            System.err.println("退款失败：只能退款已支付的订单 (status=" + order.getPaymentStatus() + ")");
            return false;
        }
        return updatePaymentStatus(orderId, STATUS_REFUNDED);
    }

    // ========== 统计功能 ==========

    /**
     * 获取订单总数
     *
     * @return 订单总数
     */
    public int getTotalOrderCount() {
        String sql = "SELECT COUNT(*) AS count FROM `order`";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取今日营收（已支付订单）
     *
     * @return 今日营收
     */
    public double getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS revenue FROM `order` " +
                "WHERE DATE(order_time) = CURDATE() AND payment_status = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, STATUS_PAID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("revenue");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * 获取会员总消费
     *
     * @param memberId 会员ID
     * @return 总消费金额
     */
    public double getMemberTotalSpending(int memberId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM `order` " +
                "WHERE member_id = ? AND payment_status = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setString(2, STATUS_PAID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * 获取总营收
     *
     * @return 总营收
     */
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS revenue FROM `order` WHERE payment_status = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, STATUS_PAID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("revenue");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // ========== 工具方法 ==========

    /**
     * 获取订单类型中文名
     *
     * @param type 订单类型
     * @return 中文名称
     */
    public String getTypeDisplayName(String type) {
        if (type == null) return "未知";
        switch (type) {
            case TYPE_MEMBERSHIP:
                return "会员卡";
            case TYPE_PRODUCT:
                return "产品";
            case TYPE_COURSE:
                return "课程";
            default:
                return "未知";
        }
    }

    /**
     * 获取支付状态中文名
     *
     * @param status 支付状态
     * @return 中文名称
     */
    public String getStatusDisplayName(String status) {
        if (status == null) return "未知";
        switch (status) {
            case STATUS_PENDING:
                return "待支付";
            case STATUS_PAID:
                return "已支付";
            case STATUS_CANCELLED:
                return "已取消";
            case STATUS_REFUNDED:
                return "已退款";
            default:
                return "未知";
        }
    }
}

