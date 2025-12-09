package entity;

import java.util.Date;

public class Order {
    private int orderId;
    private int memberId;
    private String orderType; // 'membership','product','course'
    private double amount;
    private Date orderTime;
    private String paymentStatus; // 'pending','paid','cancelled','refunded'


    public Order() {
    }

    public Order(int orderId, int memberId, String orderType, double amount, Date orderTime, String paymentStatus) {
        this.orderId = orderId;
        this.memberId = memberId;
        this.orderType = orderType;
        this.amount = amount;
        this.orderTime = orderTime;
        this.paymentStatus = paymentStatus;
    }

    /**
     * 获取
     *
     * @return orderId
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * 设置
     *
     * @param orderId
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取
     *
     * @return memberId
     */
    public int getMemberId() {
        return memberId;
    }

    /**
     * 设置
     *
     * @param memberId
     */
    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    /**
     * 获取
     *
     * @return orderType
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * 设置
     *
     * @param orderType
     */
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    /**
     * 获取
     *
     * @return amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * 设置
     *
     * @param amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * 获取
     *
     * @return orderTime
     */
    public Date getOrderTime() {
        return orderTime;
    }

    /**
     * 设置
     *
     * @param orderTime
     */
    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    /**
     * 获取
     *
     * @return paymentStatus
     */
    public String getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * 设置
     *
     * @param paymentStatus
     */
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String toString() {
        return "Order{orderId = " + orderId + ", memberId = " + memberId + ", orderType = " + orderType + ", amount = " + amount + ", orderTime = " + orderTime + ", paymentStatus = " + paymentStatus + "}";
    }


}
