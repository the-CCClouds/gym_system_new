package entity;

public class Product {

    private int productId;
    private String name;
    private double price;
    private int stock;


    public Product() {
    }

    public Product(int productId, String name, double price, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    /**
     * 获取
     *
     * @return productId
     */
    public int getProductId() {
        return productId;
    }

    /**
     * 设置
     *
     * @param productId
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * 获取
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     *
     * @return price
     */
    public double getPrice() {
        return price;
    }

    /**
     * 设置
     *
     * @param price
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * 获取
     *
     * @return stock
     */
    public int getStock() {
        return stock;
    }

    /**
     * 设置
     *
     * @param stock
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    public String toString() {
        return "Product{productId = " + productId + ", name = " + name + ", price = " + price + ", stock = " + stock + "}";
    }

}
