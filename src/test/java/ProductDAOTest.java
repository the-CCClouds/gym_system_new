import dao.ProductDAO;
import entity.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * ProductDAO 测试类
 */
public class ProductDAOTest {

    private ProductDAO productDAO;
    private int testProductId;

    @Before
    public void setUp() {
        productDAO = new ProductDAO();
        testProductId = 0;
    }

    @After
    public void tearDown() {
        if (testProductId > 0) {
            productDAO.deleteProduct(testProductId);
            testProductId = 0;
        }
    }

    // ========== 基础查询测试 ==========

    @Test
    public void testGetAllProducts() {
        List<Product> products = productDAO.getAllProducts();
        assertNotNull(products);
        assertTrue(products.size() >= 10);  // 数据库有10个产品
    }

    @Test
    public void testGetProductById() {
        Product product = productDAO.getProductById(1);
        assertNotNull(product);
        assertEquals("运动毛巾", product.getName());
        assertEquals(25.0, product.getPrice(), 0.01);
        assertEquals(100, product.getStock());
    }

    @Test
    public void testGetProductByIdNotFound() {
        Product product = productDAO.getProductById(999);
        assertNull(product);
    }

    // ========== 添加产品测试 ==========

    @Test
    public void testAddProduct() {
        Product product = new Product();
        product.setName("测试产品");
        product.setPrice(99.0);
        product.setStock(50);

        assertTrue(productDAO.addProduct(product));
        assertTrue(product.getProductId() > 0);
        testProductId = product.getProductId();

        // 验证添加成功
        Product added = productDAO.getProductById(testProductId);
        assertNotNull(added);
        assertEquals("测试产品", added.getName());
        assertEquals(99.0, added.getPrice(), 0.01);
        assertEquals(50, added.getStock());
    }

    @Test
    public void testAddProductEmptyName() {
        Product product = new Product();
        product.setName("");
        product.setPrice(10.0);
        product.setStock(10);

        assertFalse(productDAO.addProduct(product));
    }

    @Test
    public void testAddProductNegativePrice() {
        Product product = new Product();
        product.setName("负价格产品");
        product.setPrice(-10.0);
        product.setStock(10);

        assertFalse(productDAO.addProduct(product));
    }

    @Test
    public void testAddProductNegativeStock() {
        Product product = new Product();
        product.setName("负库存产品");
        product.setPrice(10.0);
        product.setStock(-10);

        assertFalse(productDAO.addProduct(product));
    }

    // ========== 更新产品测试 ==========

    @Test
    public void testUpdateProduct() {
        // 先添加测试产品
        Product product = new Product();
        product.setName("待更新产品");
        product.setPrice(50.0);
        product.setStock(20);
        assertTrue(productDAO.addProduct(product));
        testProductId = product.getProductId();

        // 更新产品
        product.setName("已更新产品");
        product.setPrice(60.0);
        product.setStock(30);
        assertTrue(productDAO.updateProduct(product));

        // 验证更新成功
        Product updated = productDAO.getProductById(testProductId);
        assertNotNull(updated);
        assertEquals("已更新产品", updated.getName());
        assertEquals(60.0, updated.getPrice(), 0.01);
        assertEquals(30, updated.getStock());
    }

    // ========== 删除产品测试 ==========

    @Test
    public void testDeleteProduct() {
        // 先添加测试产品
        Product product = new Product();
        product.setName("待删除产品");
        product.setPrice(10.0);
        product.setStock(5);
        assertTrue(productDAO.addProduct(product));
        int productId = product.getProductId();

        // 删除产品
        assertTrue(productDAO.deleteProduct(productId));

        // 验证删除成功
        assertNull(productDAO.getProductById(productId));
    }

    // ========== 库存管理测试 ==========

    @Test
    public void testUpdateStock() {
        // 先添加测试产品
        Product product = new Product();
        product.setName("库存测试产品");
        product.setPrice(10.0);
        product.setStock(100);
        assertTrue(productDAO.addProduct(product));
        testProductId = product.getProductId();

        // 更新库存
        assertTrue(productDAO.updateStock(testProductId, 50));

        // 验证更新成功
        Product updated = productDAO.getProductById(testProductId);
        assertEquals(50, updated.getStock());
    }

    @Test
    public void testUpdateStockNegative() {
        assertFalse(productDAO.updateStock(1, -10));
    }

    @Test
    public void testDecreaseStock() {
        // 先添加测试产品
        Product product = new Product();
        product.setName("减库存测试产品");
        product.setPrice(10.0);
        product.setStock(100);
        assertTrue(productDAO.addProduct(product));
        testProductId = product.getProductId();

        // 减少库存
        assertTrue(productDAO.decreaseStock(testProductId, 30));

        // 验证减少成功
        Product updated = productDAO.getProductById(testProductId);
        assertEquals(70, updated.getStock());
    }

    @Test
    public void testDecreaseStockInsufficient() {
        // 先添加测试产品
        Product product = new Product();
        product.setName("库存不足测试产品");
        product.setPrice(10.0);
        product.setStock(10);
        assertTrue(productDAO.addProduct(product));
        testProductId = product.getProductId();

        // 尝试减少超过库存的数量
        assertFalse(productDAO.decreaseStock(testProductId, 20));

        // 验证库存未变
        Product updated = productDAO.getProductById(testProductId);
        assertEquals(10, updated.getStock());
    }

    @Test
    public void testIncreaseStock() {
        // 先添加测试产品
        Product product = new Product();
        product.setName("增库存测试产品");
        product.setPrice(10.0);
        product.setStock(50);
        assertTrue(productDAO.addProduct(product));
        testProductId = product.getProductId();

        // 增加库存
        assertTrue(productDAO.increaseStock(testProductId, 30));

        // 验证增加成功
        Product updated = productDAO.getProductById(testProductId);
        assertEquals(80, updated.getStock());
    }

    @Test
    public void testIsStockAvailable() {
        // 先添加测试产品
        Product product = new Product();
        product.setName("库存检查测试产品");
        product.setPrice(10.0);
        product.setStock(50);
        assertTrue(productDAO.addProduct(product));
        testProductId = product.getProductId();

        assertTrue(productDAO.isStockAvailable(testProductId, 50));
        assertTrue(productDAO.isStockAvailable(testProductId, 30));
        assertFalse(productDAO.isStockAvailable(testProductId, 51));
        assertFalse(productDAO.isStockAvailable(testProductId, 100));
    }

    // ========== 查询功能测试 ==========

    @Test
    public void testSearchProductsByName() {
        List<Product> products = productDAO.searchProductsByName("运动");
        assertNotNull(products);
        assertTrue(products.size() >= 3);  // 运动毛巾、运动水杯、运动背包

        for (Product product : products) {
            assertTrue(product.getName().contains("运动"));
        }
    }

    @Test
    public void testSearchProductsByNameNotFound() {
        List<Product> products = productDAO.searchProductsByName("不存在的产品XYZ");
        assertNotNull(products);
        assertEquals(0, products.size());
    }

    @Test
    public void testGetAvailableProducts() {
        List<Product> products = productDAO.getAvailableProducts();
        assertNotNull(products);
        assertTrue(products.size() > 0);

        for (Product product : products) {
            assertTrue(product.getStock() > 0);
        }
    }

    // ========== 统计功能测试 ==========

    @Test
    public void testGetTotalProductCount() {
        int count = productDAO.getTotalProductCount();
        assertTrue(count >= 10);

        // 应该与getAllProducts返回的列表大小一致
        List<Product> products = productDAO.getAllProducts();
        assertEquals(products.size(), count);
    }

    @Test
    public void testGetTotalStockValue() {
        double value = productDAO.getTotalStockValue();
        assertTrue(value > 0);
    }
}

