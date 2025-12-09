import dao.OrderDAO;
import entity.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.DateUtils;

import java.util.List;

import static org.junit.Assert.*;

/**
 * OrderDAO 测试类
 */
public class OrderDAOTest {

    private OrderDAO orderDAO;
    private int testOrderId;

    @Before
    public void setUp() {
        orderDAO = new OrderDAO();
        testOrderId = 0;
    }

    @After
    public void tearDown() {
        if (testOrderId > 0) {
            orderDAO.deleteOrder(testOrderId);
            testOrderId = 0;
        }
    }

    // ========== 常量测试 ==========

    @Test
    public void testTypeConstants() {
        assertEquals("membership", OrderDAO.TYPE_MEMBERSHIP);
        assertEquals("product", OrderDAO.TYPE_PRODUCT);
        assertEquals("course", OrderDAO.TYPE_COURSE);
    }

    @Test
    public void testStatusConstants() {
        assertEquals("pending", OrderDAO.STATUS_PENDING);
        assertEquals("paid", OrderDAO.STATUS_PAID);
        assertEquals("cancelled", OrderDAO.STATUS_CANCELLED);
        assertEquals("refunded", OrderDAO.STATUS_REFUNDED);
    }

    // ========== 基础查询测试 ==========

    @Test
    public void testGetAllOrders() {
        List<Order> orders = orderDAO.getAllOrders();
        assertNotNull(orders);
        assertTrue(orders.size() >= 15);  // 数据库有15个订单
    }

    @Test
    public void testGetOrderById() {
        Order order = orderDAO.getOrderById(1);
        assertNotNull(order);
        assertEquals(1, order.getMemberId());
        assertEquals(OrderDAO.TYPE_MEMBERSHIP, order.getOrderType());
        assertEquals(1200.0, order.getAmount(), 0.01);
        assertEquals(OrderDAO.STATUS_PAID, order.getPaymentStatus());
    }

    @Test
    public void testGetOrderByIdNotFound() {
        Order order = orderDAO.getOrderById(999);
        assertNull(order);
    }

    // ========== 添加订单测试 ==========

    @Test
    public void testAddOrder() {
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PENDING);

        assertTrue(orderDAO.addOrder(order));
        assertTrue(order.getOrderId() > 0);
        testOrderId = order.getOrderId();

        // 验证添加成功
        Order added = orderDAO.getOrderById(testOrderId);
        assertNotNull(added);
        assertEquals(1, added.getMemberId());
        assertEquals(OrderDAO.TYPE_PRODUCT, added.getOrderType());
        assertEquals(100.0, added.getAmount(), 0.01);
        assertEquals(OrderDAO.STATUS_PENDING, added.getPaymentStatus());
    }

    @Test
    public void testAddOrderWithDefaultStatus() {
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_MEMBERSHIP);
        order.setAmount(200.0);
        // 不设置paymentStatus，应该默认为pending

        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        Order added = orderDAO.getOrderById(testOrderId);
        assertEquals(OrderDAO.STATUS_PENDING, added.getPaymentStatus());
    }

    @Test
    public void testAddOrderInvalidType() {
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType("invalid_type");
        order.setAmount(100.0);

        assertFalse(orderDAO.addOrder(order));
    }

    @Test
    public void testAddOrderNegativeAmount() {
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(-100.0);

        assertFalse(orderDAO.addOrder(order));
    }

    // ========== 更新订单测试 ==========

    @Test
    public void testUpdateOrder() {
        // 先添加测试订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PENDING);
        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        // 更新订单
        order.setAmount(150.0);
        order.setPaymentStatus(OrderDAO.STATUS_PAID);
        assertTrue(orderDAO.updateOrder(order));

        // 验证更新成功
        Order updated = orderDAO.getOrderById(testOrderId);
        assertEquals(150.0, updated.getAmount(), 0.01);
        assertEquals(OrderDAO.STATUS_PAID, updated.getPaymentStatus());
    }

    // ========== 删除订单测试 ==========

    @Test
    public void testDeleteOrder() {
        // 先添加测试订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(50.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PENDING);
        assertTrue(orderDAO.addOrder(order));
        int orderId = order.getOrderId();

        // 删除订单
        assertTrue(orderDAO.deleteOrder(orderId));

        // 验证删除成功
        assertNull(orderDAO.getOrderById(orderId));
    }

    // ========== 查询功能测试 ==========

    @Test
    public void testGetOrdersByMemberId() {
        List<Order> orders = orderDAO.getOrdersByMemberId(1);
        assertNotNull(orders);
        assertTrue(orders.size() >= 3);  // 会员1有多个订单

        for (Order order : orders) {
            assertEquals(1, order.getMemberId());
        }
    }

    @Test
    public void testGetOrdersByType() {
        // 测试会员卡订单
        List<Order> membershipOrders = orderDAO.getOrdersByType(OrderDAO.TYPE_MEMBERSHIP);
        assertNotNull(membershipOrders);
        assertTrue(membershipOrders.size() >= 8);

        for (Order order : membershipOrders) {
            assertEquals(OrderDAO.TYPE_MEMBERSHIP, order.getOrderType());
        }

        // 测试产品订单
        List<Order> productOrders = orderDAO.getOrdersByType(OrderDAO.TYPE_PRODUCT);
        assertNotNull(productOrders);
        assertTrue(productOrders.size() >= 6);

        for (Order order : productOrders) {
            assertEquals(OrderDAO.TYPE_PRODUCT, order.getOrderType());
        }
    }

    @Test
    public void testGetOrdersByStatus() {
        List<Order> paidOrders = orderDAO.getOrdersByStatus(OrderDAO.STATUS_PAID);
        assertNotNull(paidOrders);
        assertTrue(paidOrders.size() >= 15);

        for (Order order : paidOrders) {
            assertEquals(OrderDAO.STATUS_PAID, order.getPaymentStatus());
        }
    }

    @Test
    public void testGetTodayOrders() {
        List<Order> orders = orderDAO.getTodayOrders();
        assertNotNull(orders);
        // 今日订单数可能为0
    }

    // ========== 状态管理测试 ==========

    @Test
    public void testPayOrder() {
        // 先添加待支付订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PENDING);
        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        // 支付订单
        assertTrue(orderDAO.payOrder(testOrderId));

        // 验证状态已更新
        Order paid = orderDAO.getOrderById(testOrderId);
        assertEquals(OrderDAO.STATUS_PAID, paid.getPaymentStatus());
    }

    @Test
    public void testPayOrderAlreadyPaid() {
        // 先添加已支付订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PAID);
        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        // 尝试再次支付，应该失败
        assertFalse(orderDAO.payOrder(testOrderId));
    }

    @Test
    public void testCancelOrder() {
        // 先添加待支付订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PENDING);
        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        // 取消订单
        assertTrue(orderDAO.cancelOrder(testOrderId));

        // 验证状态已更新
        Order cancelled = orderDAO.getOrderById(testOrderId);
        assertEquals(OrderDAO.STATUS_CANCELLED, cancelled.getPaymentStatus());
    }

    @Test
    public void testCancelOrderAlreadyPaid() {
        // 先添加已支付订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PAID);
        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        // 尝试取消已支付订单，应该失败
        assertFalse(orderDAO.cancelOrder(testOrderId));
    }

    @Test
    public void testRefundOrder() {
        // 先添加已支付订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PAID);
        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        // 退款
        assertTrue(orderDAO.refundOrder(testOrderId));

        // 验证状态已更新
        Order refunded = orderDAO.getOrderById(testOrderId);
        assertEquals(OrderDAO.STATUS_REFUNDED, refunded.getPaymentStatus());
    }

    @Test
    public void testRefundOrderNotPaid() {
        // 先添加待支付订单
        Order order = new Order();
        order.setMemberId(1);
        order.setOrderType(OrderDAO.TYPE_PRODUCT);
        order.setAmount(100.0);
        order.setOrderTime(DateUtils.now());
        order.setPaymentStatus(OrderDAO.STATUS_PENDING);
        assertTrue(orderDAO.addOrder(order));
        testOrderId = order.getOrderId();

        // 尝试退款未支付订单，应该失败
        assertFalse(orderDAO.refundOrder(testOrderId));
    }

    // ========== 统计功能测试 ==========

    @Test
    public void testGetTotalOrderCount() {
        int count = orderDAO.getTotalOrderCount();
        assertTrue(count >= 15);

        List<Order> orders = orderDAO.getAllOrders();
        assertEquals(orders.size(), count);
    }

    @Test
    public void testGetTodayRevenue() {
        double revenue = orderDAO.getTodayRevenue();
        assertTrue(revenue >= 0);
    }

    @Test
    public void testGetMemberTotalSpending() {
        double spending = orderDAO.getMemberTotalSpending(1);
        assertTrue(spending > 0);  // 会员1有多笔已支付订单
    }

    @Test
    public void testGetTotalRevenue() {
        double revenue = orderDAO.getTotalRevenue();
        assertTrue(revenue > 0);
    }

    // ========== 工具方法测试 ==========

    @Test
    public void testGetTypeDisplayName() {
        assertEquals("会员卡", orderDAO.getTypeDisplayName(OrderDAO.TYPE_MEMBERSHIP));
        assertEquals("产品", orderDAO.getTypeDisplayName(OrderDAO.TYPE_PRODUCT));
        assertEquals("课程", orderDAO.getTypeDisplayName(OrderDAO.TYPE_COURSE));
        assertEquals("未知", orderDAO.getTypeDisplayName("invalid"));
        assertEquals("未知", orderDAO.getTypeDisplayName(null));
    }

    @Test
    public void testGetStatusDisplayName() {
        assertEquals("待支付", orderDAO.getStatusDisplayName(OrderDAO.STATUS_PENDING));
        assertEquals("已支付", orderDAO.getStatusDisplayName(OrderDAO.STATUS_PAID));
        assertEquals("已取消", orderDAO.getStatusDisplayName(OrderDAO.STATUS_CANCELLED));
        assertEquals("已退款", orderDAO.getStatusDisplayName(OrderDAO.STATUS_REFUNDED));
        assertEquals("未知", orderDAO.getStatusDisplayName("invalid"));
        assertEquals("未知", orderDAO.getStatusDisplayName(null));
    }
}

