package entity;

import java.util.Date;

public class MembershipCard {
    private int cardId;
    private int memberId;
    private int typeId;                    // 外键关联 membership_type 表
    private entity.MembershipType membershipType; // 关联的类型对象
    private Date startDate;
    private Date endDate;
    private String cardStatus; // 'active','inactive','expired'

    public MembershipCard() {
    }

    public MembershipCard(int cardId, int memberId, int typeId, Date startDate, Date endDate, String cardStatus) {
        this.cardId = cardId;
        this.memberId = memberId;
        this.typeId = typeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.cardStatus = cardStatus;
    }

    public MembershipCard(int cardId, int memberId, entity.MembershipType membershipType, Date startDate, Date endDate, String cardStatus) {
        this.cardId = cardId;
        this.memberId = memberId;
        this.membershipType = membershipType;
        this.typeId = membershipType != null ? membershipType.getTypeId() : 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.cardStatus = cardStatus;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public entity.MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(entity.MembershipType membershipType) {
        this.membershipType = membershipType;
        if (membershipType != null) {
            this.typeId = membershipType.getTypeId();
        }
    }

    /**
     * 获取卡类型名称（兼容旧代码）
     * @return 类型名称 'Monthly' 或 'Yearly'
     */
    public String getCardType() {
        if (membershipType != null) {
            return membershipType.getTypeName();
        }
        // 根据 typeId 返回默认值
        switch (typeId) {
            case 1: return "Monthly";
            case 2: return "Yearly";
            default: return "Unknown";
        }
    }

    /**
     * 设置卡类型（兼容旧代码，根据名称设置typeId）
     * @param cardType 'monthly' 或 'yearly'
     */
    public void setCardType(String cardType) {
        if ("monthly".equalsIgnoreCase(cardType)) {
            this.typeId = 1;
        } else if ("yearly".equalsIgnoreCase(cardType)) {
            this.typeId = 2;
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(String cardStatus) {
        this.cardStatus = cardStatus;
    }

    /**
     * 判断是否是月卡
     */
    public boolean isMonthly() {
        return typeId == 1 || (membershipType != null && membershipType.isMonthly());
    }

    /**
     * 判断是否是年卡
     */
    public boolean isYearly() {
        return typeId == 2 || (membershipType != null && membershipType.isYearly());
    }

    /**
     * 获取卡的价格
     */
    public double getPrice() {
        if (membershipType != null) {
            return membershipType.getPrice();
        }
        // 默认价格
        return isMonthly() ? 200.0 : 1200.0;
    }

    /**
     * 获取卡的有效期天数
     */
    public int getDurationDays() {
        if (membershipType != null) {
            return membershipType.getDurationDays();
        }
        // 默认天数
        return isMonthly() ? 30 : 365;
    }

    @Override
    public String toString() {
        return "MembershipCard{" +
                "cardId=" + cardId +
                ", memberId=" + memberId +
                ", typeId=" + typeId +
                ", cardType='" + getCardType() + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", cardStatus='" + cardStatus + '\'' +
                '}';
    }
}
