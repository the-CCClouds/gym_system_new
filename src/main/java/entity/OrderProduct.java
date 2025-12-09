package entity;

public class OrderProduct {
    private int orderId;
    private int productId;
    private int quantity;

    public OrderProduct() {
    }

    public OrderProduct(int orderId, int productId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    /**
     * 获取
     * @return orderId
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * 设置
     * @param orderId
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取
     * @return productId
     */
    public int getProductId() {
        return productId;
    }

    /**
     * 设置
     * @param productId
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * 获取
     * @return quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * 设置
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toString() {
        return "OrderProduct{orderId = " + orderId + ", productId = " + productId + ", quantity = " + quantity + "}";
    }

}
