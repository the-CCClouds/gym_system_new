package entity;
import java.util.Date;
public class CheckIn {

    private int checkinId;
    private int memberId;
    private Date checkinTime;
    private Date checkoutTime;

    public CheckIn() {
    }

    public CheckIn(int checkinId, int memberId, Date checkinTime, Date checkoutTime) {
        this.checkinId = checkinId;
        this.memberId = memberId;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
    }


    /**
     * 获取
     * @return checkinId
     */
    public int getCheckinId() {
        return checkinId;
    }

    /**
     * 设置
     * @param checkinId
     */
    public void setCheckinId(int checkinId) {
        this.checkinId = checkinId;
    }

    /**
     * 获取
     * @return memberId
     */
    public int getMemberId() {
        return memberId;
    }

    /**
     * 设置
     * @param memberId
     */
    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    /**
     * 获取
     * @return checkinTime
     */
    public Date getCheckinTime() {
        return checkinTime;
    }

    /**
     * 设置
     * @param checkinTime
     */
    public void setCheckinTime(Date checkinTime) {
        this.checkinTime = checkinTime;
    }

    /**
     * 获取
     * @return checkoutTime
     */
    public Date getCheckoutTime() {
        return checkoutTime;
    }

    /**
     * 设置
     * @param checkoutTime
     */
    public void setCheckoutTime(Date checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public String toString() {
        return "CheckIn{checkinId = " + checkinId + ", memberId = " + memberId + ", checkinTime = " + checkinTime + ", checkoutTime = " + checkoutTime + "}";
    }
}
