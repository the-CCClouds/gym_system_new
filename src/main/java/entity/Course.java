package entity;

import java.util.Date;

public class Course {
    private int courseId;
    private String name;
    private String type; // 'yoga','spinning'等
    private int duration; // 分钟数
    private int maxCapacity;
    private int employeeId; // 教练ID

    // 【新增】上课时间 (使用 java.util.Date)
    private Date courseTime;

    public Course() {
    }

    // 修改构造方法，加上 courseTime
    public Course(int courseId, String name, String type, int duration,
                  int maxCapacity, int employeeId, Date courseTime) {
        this.courseId = courseId;
        this.name = name;
        this.type = type;
        this.duration = duration;
        this.maxCapacity = maxCapacity;
        this.employeeId = employeeId;
        this.courseTime = courseTime;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    // 【新增】Getter/Setter
    public Date getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(Date courseTime) {
        this.courseTime = courseTime;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", duration=" + duration +
                ", maxCapacity=" + maxCapacity +
                ", employeeId=" + employeeId +
                ", courseTime=" + courseTime +
                '}';
    }
}