import dao.OrderDAO;
import dao.OrderProductDAO;
import entity.Order;
import entity.OrderProduct;
import entity.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * OrderProductDAO 测试类
 */
public class OrderProductDAOTest {

    private OrderProductDAO orderProductDAO;
    private OrderDAO orderDAO;
    private int testOrderId;

    @Before
    public void setUp() {
        orderProductDAO = new OrderProductDAO();
        orderDAO = new OrderDAO();
        testOrderId = 0;
    }

    @After
    public void tearDown() {
        if (testOrderId > 0) {
            // 先删除订单产品关联，再删除订单
            orderProductDAO.deleteOrderProductsByOrderId(testOrderId);
            orderDAO.deleteOrder(testOrderId);
            testOrderId = 0;
        }
    }

    /**
     * 创建测试订单
     */
    private int createTestOrder() {
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PENDING);
        orderDAO.addOrder(order);
        return order.getOrderId();
    }

    // ========== 基础查询测试 ==========

    @Test
    public void testGetOrderProductsByOrderId() {
        // 订单4有2个产品（product_id=1和2）
        List<OrderProduct> orderProducts = orderProductDAO.getOrderProductsByOrderId(4);
        assertNotNull(orderProducts);
        assertEquals(2, orderProducts.size());

        for (OrderProduct op : orderProducts) {
            assertEquals(4, op.getOrderId());
            assertTrue(op.getQuantity() > 0);
        }
    }

    @Test
    public void testGetOrderProductsByOrderIdNotFound() {
        List<OrderProduct> orderProducts = orderProductDAO.getOrderProductsByOrderId(999);
        assertNotNull(orderProducts);
        assertEquals(0, orderProducts.size());
    }

    // ========== 添加订单产品测试 ==========

    @Test
    public void testAddOrderProduct() {
        testOrderId = createTestOrder();

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(testOrderId);
        orderProduct.setProductId(1);  // 运动毛巾
        orderProduct.setQuantity(2);

        assertTrue(orderProductDAO.addOrderProduct(orderProduct));

        // 验证添加成功
        List<OrderProduct> products = orderProductDAO.getOrderProductsByOrderId(testOrderId);
        assertEquals(1, products.size());
        assertEquals(1, products.get(0).getProductId());
        assertEquals(2, products.get(0).getQuantity());
    }

    @Test
    public void testAddOrderProductInvalidQuantity() {
        testOrderId = createTestOrder();

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(testOrderId);
        orderProduct.setProductId(1);
        orderProduct.setQuantity(0);  // 无效数量

        assertFalse(orderProductDAO.addOrderProduct(orderProduct));
    }

    @Test
    public void testAddOrderProducts() {
        testOrderId = createTestOrder();

        List<OrderProduct> items = new ArrayList<>();

        OrderProduct item1 = new OrderProduct();
        item1.setProductId(1);
        item1.setQuantity(2);
        items.add(item1);

        OrderProduct item2 = new OrderProduct();
        item2.setProductId(2);
        item2.setQuantity(1);
        items.add(item2);

        assertTrue(orderProductDAO.addOrderProducts(testOrderId, items));

        // 验证添加成功
        List<OrderProduct> products = orderProductDAO.getOrderProductsByOrderId(testOrderId);
        assertEquals(2, products.size());
    }

    // ========== 删除订单产品测试 ==========

    @Test
    public void testDeleteOrderProductsByOrderId() {
        testOrderId = createTestOrder();

        // 添加测试数据
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(testOrderId);
        orderProduct.setProductId(1);
        orderProduct.setQuantity(1);
        assertTrue(orderProductDAO.addOrderProduct(orderProduct));

        // 删除
        assertTrue(orderProductDAO.deleteOrderProductsByOrderId(testOrderId));

        // 验证删除成功
        List<OrderProduct> products = orderProductDAO.getOrderProductsByOrderId(testOrderId);
        assertEquals(0, products.size());
    }

    @Test
    public void testDeleteOrderProduct() {
        testOrderId = createTestOrder();

        // 添加测试数据
        OrderProduct op1 = new OrderProduct();
        op1.setOrderId(testOrderId);
        op1.setProductId(1);
        op1.setQuantity(1);
        assertTrue(orderProductDAO.addOrderProduct(op1));

        OrderProduct op2 = new OrderProduct();
        op2.setOrderId(testOrderId);
        op2.setProductId(2);
        op2.setQuantity(1);
        assertTrue(orderProductDAO.addOrderProduct(op2));

        // 删除其中一个
        assertTrue(orderProductDAO.deleteOrderProduct(testOrderId, 1));

        // 验证只删除了一个
        List<OrderProduct> products = orderProductDAO.getOrderProductsByOrderId(testOrderId);
        assertEquals(1, products.size());
        assertEquals(2, products.get(0).getProductId());
    }

    // ========== 更新数量测试 ==========

    @Test
    public void testUpdateQuantity() {
        testOrderId = createTestOrder();

        // 添加测试数据
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(testOrderId);
        orderProduct.setProductId(1);
        orderProduct.setQuantity(1);
        assertTrue(orderProductDAO.addOrderProduct(orderProduct));

        // 更新数量
        assertTrue(orderProductDAO.updateQuantity(testOrderId, 1, 5));

        // 验证更新成功
        List<OrderProduct> products = orderProductDAO.getOrderProductsByOrderId(testOrderId);
        assertEquals(5, products.get(0).getQuantity());
    }

    @Test
    public void testUpdateQuantityInvalid() {
        testOrderId = createTestOrder();

        // 添加测试数据
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(testOrderId);
        orderProduct.setProductId(1);
        orderProduct.setQuantity(1);
        assertTrue(orderProductDAO.addOrderProduct(orderProduct));

        // 尝试更新为无效数量
        assertFalse(orderProductDAO.updateQuantity(testOrderId, 1, 0));
        assertFalse(orderProductDAO.updateQuantity(testOrderId, 1, -1));
    }

    // ========== 查询功能测试 ==========

    @Test
    public void testGetProductsByOrderId() {
        // 订单4有产品
        List<Product> products = orderProductDAO.getProductsByOrderId(4);
        assertNotNull(products);
        assertTrue(products.size() > 0);
    }

    @Test
    public void testGetProductsWithQuantityByOrderId() {
        // 订单4有产品
        List<Product> products = orderProductDAO.getProductsWithQuantityByOrderId(4);
        assertNotNull(products);
        assertTrue(products.size() > 0);

        // stock字段存储的是购买数量
        for (Product product : products) {
            assertTrue(product.getStock() > 0);  // 数量应该大于0
        }
    }

    @Test
    public void testCalculateOrderTotal() {
        // 订单4：运动毛巾(25元)*1 + 瑜伽垫(89元)*1 = 114元
        double total = orderProductDAO.calculateOrderTotal(4);
        assertEquals(114.0, total, 0.01);
    }

    @Test
    public void testCalculateOrderTotalEmptyOrder() {
        testOrderId = createTestOrder();
        double total = orderProductDAO.calculateOrderTotal(testOrderId);
        assertEquals(0.0, total, 0.01);
    }

    @Test
    public void testGetProductSalesCount() {
        // 产品1（运动毛巾）在订单4中有销售
        int count = orderProductDAO.getProductSalesCount(1);
        assertTrue(count >= 1);
    }

    @Test
    public void testHasProduct() {
        // 订单4包含产品1
        assertTrue(orderProductDAO.hasProduct(4, 1));
        assertTrue(orderProductDAO.hasProduct(4, 2));
        assertFalse(orderProductDAO.hasProduct(4, 99));
    }

    @Test
    public void testGetOrderProductCount() {
        // 订单4有2种产品
        int count = orderProductDAO.getOrderProductCount(4);
        assertEquals(2, count);
    }

    @Test
    public void testGetOrderProductCountEmpty() {
        testOrderId = createTestOrder();
        int count = orderProductDAO.getOrderProductCount(testOrderId);
        assertEquals(0, count);
    }
}

