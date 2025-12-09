package dao;

import entity.OrderProduct;
import entity.Product;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单产品关联数据访问对象
 * 对应数据库 order_product 表
 */
public class OrderProductDAO {

    private dao.ProductDAO productDAO;

    public OrderProductDAO() {
        this.productDAO = new dao.ProductDAO();
    }

    /**
     * 从结果集中提取订单产品关联信息
     */
    private OrderProduct extractOrderProductFromResultSet(ResultSet rs) throws SQLException {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(rs.getInt("order_id"));
        orderProduct.setProductId(rs.getInt("product_id"));
        orderProduct.setQuantity(rs.getInt("quantity"));
        return orderProduct;
    }

    // ========== 基础操作 ==========

    /**
     * 添加订单产品关联
     *
     * @param orderProduct 订单产品关联对象
     * @return 是否添加成功
     */
    public boolean addOrderProduct(OrderProduct orderProduct) {
        if (orderProduct == null) {
            System.err.println("添加失败：订单产品对象为空");
            return false;
        }
        if (orderProduct.getQuantity() <= 0) {
            System.err.println("添加失败：数量必须大于0");
            return false;
        }

        String sql = "INSERT INTO order_product (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderProduct.getOrderId());
            pstmt.setInt(2, orderProduct.getProductId());
            pstmt.setInt(3, orderProduct.getQuantity());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量添加订单产品关联
     *
     * @param orderId 订单ID
     * @param items   订单产品列表
     * @return 是否全部添加成功
     */
    public boolean addOrderProducts(int orderId, List<OrderProduct> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO order_product (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (OrderProduct item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getProductId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取订单的所有产品关联
     *
     * @param orderId 订单ID
     * @return 订单产品关联列表
     */
    public List<OrderProduct> getOrderProductsByOrderId(int orderId) {
        List<OrderProduct> orderProducts = new ArrayList<>();
        String sql = "SELECT * FROM order_product WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orderProducts.add(extractOrderProductFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderProducts;
    }

    /**
     * 删除订单的所有产品关联
     *
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    public boolean deleteOrderProductsByOrderId(int orderId) {
        String sql = "DELETE FROM order_product WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            pstmt.executeUpdate();
            return true;  // 即使没有记录也返回true

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除单个订单产品关联
     *
     * @param orderId   订单ID
     * @param productId 产品ID
     * @return 是否删除成功
     */
    public boolean deleteOrderProduct(int orderId, int productId) {
        String sql = "DELETE FROM order_product WHERE order_id = ? AND product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            pstmt.setInt(2, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新订单产品数量
     *
     * @param orderId   订单ID
     * @param productId 产品ID
     * @param quantity  新数量
     * @return 是否更新成功
     */
    public boolean updateQuantity(int orderId, int productId, int quantity) {
        if (quantity <= 0) {
            System.err.println("更新失败：数量必须大于0");
            return false;
        }

        String sql = "UPDATE order_product SET quantity = ? WHERE order_id = ? AND product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, orderId);
            pstmt.setInt(3, productId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== 查询功能 ==========

    /**
     * 获取订单产品详情（包含Product对象）
     *
     * @param orderId 订单ID
     * @return 产品列表（每个产品包含购买数量信息）
     */
    public List<Product> getProductsByOrderId(int orderId) {
        List<Product> products = new ArrayList<>();
        List<OrderProduct> orderProducts = getOrderProductsByOrderId(orderId);

        for (OrderProduct op : orderProducts) {
            Product product = productDAO.getProductById(op.getProductId());
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }

    /**
     * 获取订单产品详情（带数量）
     * 返回的Product对象的stock字段临时存储购买数量
     *
     * @param orderId 订单ID
     * @return 产品列表
     */
    public List<Product> getProductsWithQuantityByOrderId(int orderId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, op.quantity FROM order_product op " +
                "JOIN product p ON op.product_id = p.product_id " +
                "WHERE op.order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    // 使用stock字段临时存储购买数量
                    product.setStock(rs.getInt("quantity"));
                    products.add(product);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * 计算订单产品总金额
     *
     * @param orderId 订单ID
     * @return 总金额
     */
    public double calculateOrderTotal(int orderId) {
        String sql = "SELECT COALESCE(SUM(p.price * op.quantity), 0) AS total " +
                "FROM order_product op " +
                "JOIN product p ON op.product_id = p.product_id " +
                "WHERE op.order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
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
     * 获取产品的销售数量
     *
     * @param productId 产品ID
     * @return 销售总数量
     */
    public int getProductSalesCount(int productId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) AS count FROM order_product WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 检查订单是否包含某产品
     *
     * @param orderId   订单ID
     * @param productId 产品ID
     * @return true表示包含
     */
    public boolean hasProduct(int orderId, int productId) {
        String sql = "SELECT COUNT(*) AS count FROM order_product WHERE order_id = ? AND product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            pstmt.setInt(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取订单产品数量（种类数）
     *
     * @param orderId 订单ID
     * @return 产品种类数
     */
    public int getOrderProductCount(int orderId) {
        String sql = "SELECT COUNT(*) AS count FROM order_product WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

