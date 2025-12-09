package entity;

/**
 * 会员卡类型实体类：体现继承关系
 * 对应数据库 membership_type 表
 */
public class MembershipType {
    private int typeId;
    private String typeName;      // 'Monthly' | 'Yearly'
    private int durationDays;     // 有效期天数
    private double price;         // 价格
    private String description;   // 描述

    public MembershipType() {
    }

    public MembershipType(int typeId, String typeName, int durationDays) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.durationDays = durationDays;
    }

    public MembershipType(int typeId, String typeName, int durationDays, double price, String description) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.durationDays = durationDays;
        this.price = price;
        this.description = description;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 判断是否是月卡
     */
    public boolean isMonthly() {
        return "Monthly".equalsIgnoreCase(typeName) || typeId == 1;
    }

    /**
     * 判断是否是年卡
     */
    public boolean isYearly() {
        return "Yearly".equalsIgnoreCase(typeName) || typeId == 2;
    }

    @Override
    public String toString() {
        return "MembershipType{" +
                "typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                ", durationDays=" + durationDays +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }
}

