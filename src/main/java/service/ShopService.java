package service;

import dao.OrderDAO;
import dao.OrderProductDAO;
import dao.ProductDAO;
import entity.Order;
import entity.OrderProduct;
import entity.Product;
import utils.DateUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ShopService {

    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private OrderProductDAO orderProductDAO;

    public ShopService() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.orderProductDAO = new OrderProductDAO();
    }

    /**
     * 获取所有在售商品
     */
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    /**
     * 结算/创建订单
     * @param memberId 会员ID (可以是 0 或 -1 表示散客/匿名购买)
     * @param cart 购物车数据：Map<商品ID, 购买数量>
     * @return 操作结果
     */
    public ServiceResult<Void> checkout(int memberId, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            return ServiceResult.failure("购物车是空的");
        }

        // 1. 计算总金额并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = productDAO.getProductById(productId);
            if (product == null) {
                return ServiceResult.failure("商品ID " + productId + " 不存在");
            }
            if (product.getStock() < quantity) {
                return ServiceResult.failure("商品 [" + product.getName() + "] 库存不足 (仅剩 " + product.getStock() + ")");
            }

            // 累加金额: price * quantity
            BigDecimal priceBD = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = priceBD.multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 2. 创建主订单
        Order order = new Order();
        if (memberId > 0) {
            order.setMemberId(memberId); // 是会员，设置ID
        } else {
            // 散客：我们约定使用 0 或 -1 来表示
            // 实际上 Order 初始化时 memberId 默认就是 0，所以这里甚至可以什么都不写
            order.setMemberId(0);
        }
// ...
        order.setOrderType("product"); // 类型：商品消费
        order.setAmount(totalAmount.doubleValue());
        order.setOrderTime(DateUtils.now()); // 使用当前时间
        order.setPaymentStatus("paid"); // 默认现结已支付

        // 正确写法：
        boolean success = orderDAO.addOrder(order); // 1. 先执行插入，返回布尔值

        if (!success) {
            return ServiceResult.failure("订单创建失败 (数据库错误)");
        }

        // 2. 从 order 对象中获取自动生成的 ID
        int orderId = order.getOrderId();

        // 3. 创建订单明细 & 扣减库存
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            // 3.1 保存明细
            OrderProduct item = new OrderProduct();
            item.setOrderId(orderId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            orderProductDAO.addOrderProduct(item);

            // 3.2 扣库存
            // 注意：ProductDAO 需要有一个 decreaseStock 方法，或者 updateProduct 方法
            Product p = productDAO.getProductById(productId);
            p.setStock(p.getStock() - quantity);
            productDAO.updateProduct(p);
        }

        return ServiceResult.success("交易成功！收款金额: ¥" + totalAmount);
    }

    // 复用通用的结果类
    public static class ServiceResult<T> {
        private boolean success;
        private String message;

        public static <T> ServiceResult<T> success(String msg) {
            ServiceResult<T> r = new ServiceResult<>();
            r.success = true;
            r.message = msg;
            return r;
        }

        public static <T> ServiceResult<T> failure(String msg) {
            ServiceResult<T> r = new ServiceResult<>();
            r.success = false;
            r.message = msg;
            return r;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}