package entity;

import java.util.Date;

public class Booking {


        private int bookingId;
        private int memberId;
        private int courseId;
        private Date bookingTime;
        private String bookingStatus; // 'pending','confirmed','cancelled

    public Booking() {
    }

    public Booking(int bookingId, int memberId, int courseId, Date bookingTime, String bookingStatus) {
        this.bookingId = bookingId;
        this.memberId = memberId;
        this.courseId = courseId;
        this.bookingTime = bookingTime;
        this.bookingStatus = bookingStatus;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Date getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Date bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }


    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", memberId=" + memberId +
                ", courseId=" + courseId +
                ", bookingTime=" + bookingTime +
                ", bookingStatus='" + bookingStatus + '\'' +
                '}';
    }


}
