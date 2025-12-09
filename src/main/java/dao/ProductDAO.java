package dao;

import entity.Product;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 产品数据访问对象
 * 对应数据库 product 表
 */
public class ProductDAO {

    public ProductDAO() {
    }

    /**
     * 从结果集中提取产品信息
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
        return product;
    }

    // ========== 基础 CRUD ==========

    /**
     * 添加产品
     *
     * @param product 产品对象
     * @return 是否添加成功
     */
    public boolean addProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().trim().isEmpty()) {
            System.err.println("添加失败：产品名称不能为空");
            return false;
        }
        if (product.getPrice() < 0) {
            System.err.println("添加失败：价格不能为负数");
            return false;
        }
        if (product.getStock() < 0) {
            System.err.println("添加失败：库存不能为负数");
            return false;
        }

        String sql = "INSERT INTO product (name, price, stock) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setProductId(rs.getInt(1));
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
     * 根据ID查询产品
     *
     * @param productId 产品ID
     * @return 产品对象，不存在返回null
     */
    public Product getProductById(int productId) {
        String sql = "SELECT * FROM product WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有产品
     *
     * @return 产品列表
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product ORDER BY product_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * 更新产品信息
     *
     * @param product 产品对象
     * @return 是否更新成功
     */
    public boolean updateProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().trim().isEmpty()) {
            System.err.println("更新失败：产品名称不能为空");
            return false;
        }
        if (product.getPrice() < 0) {
            System.err.println("更新失败：价格不能为负数");
            return false;
        }
        if (product.getStock() < 0) {
            System.err.println("更新失败：库存不能为负数");
            return false;
        }

        String sql = "UPDATE product SET name = ?, price = ?, stock = ? WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setInt(4, product.getProductId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除产品
     *
     * @param productId 产品ID
     * @return 是否删除成功
     */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== 库存管理 ==========

    /**
     * 更新库存
     *
     * @param productId 产品ID
     * @param stock     新库存数量
     * @return 是否更新成功
     */
    public boolean updateStock(int productId, int stock) {
        if (stock < 0) {
            System.err.println("更新失败：库存不能为负数");
            return false;
        }

        String sql = "UPDATE product SET stock = ? WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stock);
            pstmt.setInt(2, productId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 减少库存（售出时调用）
     *
     * @param productId 产品ID
     * @param quantity  减少数量
     * @return 是否操作成功
     */
    public boolean decreaseStock(int productId, int quantity) {
        if (quantity <= 0) {
            System.err.println("操作失败：数量必须大于0");
            return false;
        }

        // 先检查库存是否充足
        if (!isStockAvailable(productId, quantity)) {
            System.err.println("操作失败：库存不足");
            return false;
        }

        String sql = "UPDATE product SET stock = stock - ? WHERE product_id = ? AND stock >= ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 增加库存
     *
     * @param productId 产品ID
     * @param quantity  增加数量
     * @return 是否操作成功
     */
    public boolean increaseStock(int productId, int quantity) {
        if (quantity <= 0) {
            System.err.println("操作失败：数量必须大于0");
            return false;
        }

        String sql = "UPDATE product SET stock = stock + ? WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查库存是否充足
     *
     * @param productId 产品ID
     * @param quantity  需要的数量
     * @return true表示库存充足
     */
    public boolean isStockAvailable(int productId, int quantity) {
        Product product = getProductById(productId);
        return product != null && product.getStock() >= quantity;
    }

    // ========== 查询功能 ==========

    /**
     * 按名称模糊查询产品
     *
     * @param name 产品名称关键字
     * @return 产品列表
     */
    public List<Product> searchProductsByName(String name) {
        List<Product> products = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return products;
        }

        String sql = "SELECT * FROM product WHERE name LIKE ? ORDER BY product_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(extractProductFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * 获取有库存的产品
     *
     * @return 有库存的产品列表
     */
    public List<Product> getAvailableProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE stock > 0 ORDER BY product_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // ========== 统计功能 ==========

    /**
     * 获取产品总数
     *
     * @return 产品总数
     */
    public int getTotalProductCount() {
        String sql = "SELECT COUNT(*) AS count FROM product";

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
     * 获取库存总价值
     *
     * @return 库存总价值
     */
    public double getTotalStockValue() {
        String sql = "SELECT SUM(price * stock) AS total_value FROM product";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total_value");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}

