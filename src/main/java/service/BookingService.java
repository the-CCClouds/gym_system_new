package service;

import dao.BookingDAO;
import dao.CourseDAO;
import dao.MemberDAO;
import dao.MembershipCardDAO;
import dao.EmployeeDAO;
import entity.Booking;
import entity.Course;
import entity.Member;
import entity.Employee;
import utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预约服务类
 * 提供预约相关的业务逻辑处理
 * 
 * 主要功能：
 * - 预约创建与取消
 * - 预约确认
 * - 预约查询（按会员、按课程、按状态）
 * - 预约历史记录
 * - 预约统计报表
 * 
 * 预约状态流转：
 * - pending（待确认）→ confirmed（已确认）
 * - pending（待确认）→ cancelled（已取消）
 * - confirmed（已确认）→ cancelled（已取消）
 * 
 * @author GymSystem
 * @version 1.0
 */
public class BookingService {

    // ==================== 依赖的DAO ====================

    private BookingDAO bookingDAO;
    private CourseDAO courseDAO;
    private MemberDAO memberDAO;
    private MembershipCardDAO cardDAO;
    private EmployeeDAO employeeDAO;

    // ==================== 预约状态常量（引用DAO） ====================

    /** 预约状态：待确认 */
    public static final String STATUS_PENDING = BookingDAO.STATUS_PENDING;
    /** 预约状态：已确认 */
    public static final String STATUS_CONFIRMED = BookingDAO.STATUS_CONFIRMED;
    /** 预约状态：已取消 */
    public static final String STATUS_CANCELLED = BookingDAO.STATUS_CANCELLED;

    // ==================== 构造方法 ====================

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.courseDAO = new CourseDAO();
        this.memberDAO = new MemberDAO();
        this.cardDAO = new MembershipCardDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    // ==================== 预约创建 ====================

    /**
     * 创建预约
     * 
     * 业务规则：
     * 1. 会员必须存在且状态为激活
     * 2. 会员必须有有效的会员卡
     * 3. 课程必须存在
     * 4. 课程不能已满
     * 5. 不能重复预约同一课程
     * 
     * @param memberId 会员ID
     * @param courseId 课程ID
     * @return 创建结果，包含成功/失败信息和预约对象
     */
    public ServiceResult<Booking> createBooking(int memberId, int courseId) {
        // 验证会员
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("预约失败：会员不存在");
        }
        if (!MemberDAO.STATUS_ACTIVE.equals(member.getStatus())) {
            String statusName = memberDAO.getStatusDisplayName(member.getStatus());
            return ServiceResult.failure("预约失败：会员状态为「" + statusName + "」，无法预约");
        }

        // 验证会员卡
        if (!cardDAO.hasMemberValidCard(memberId)) {
            return ServiceResult.failure("预约失败：会员没有有效的会员卡，请先开卡或续费");
        }

        // 验证课程
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("预约失败：课程不存在");
        }

        // 检查课程容量
        if (courseDAO.isFull(courseId)) {
            return ServiceResult.failure("预约失败：课程「" + course.getName() + "」已满");
        }

        // 检查重复预约
        if (bookingDAO.checkDuplicateBooking(memberId, courseId)) {
            return ServiceResult.failure("预约失败：您已预约过该课程，不能重复预约");
        }

        // 创建预约对象
        Booking booking = new Booking();
        booking.setMemberId(memberId);
        booking.setCourseId(courseId);
        booking.setBookingTime(DateUtils.now());
        booking.setBookingStatus(STATUS_PENDING);

        // 保存到数据库
        if (bookingDAO.addBooking(booking)) {
            return ServiceResult.success("预约成功，等待确认", booking);
        } else {
            return ServiceResult.failure("预约失败：数据库操作失败");
        }
    }

    /**
     * 创建预约并自动确认
     * 
     * @param memberId 会员ID
     * @param courseId 课程ID
     * @return 创建结果
     */
    public ServiceResult<Booking> createAndConfirmBooking(int memberId, int courseId) {
        ServiceResult<Booking> createResult = createBooking(memberId, courseId);
        if (!createResult.isSuccess()) {
            return createResult;
        }

        Booking booking = createResult.getData();
        ServiceResult<Booking> confirmResult = confirmBooking(booking.getBookingId());
        if (!confirmResult.isSuccess()) {
            // 确认失败，但预约已创建
            return ServiceResult.success("预约已创建，但自动确认失败：" + confirmResult.getMessage(), booking);
        }

        return ServiceResult.success("预约成功，已自动确认", confirmResult.getData());
    }

    // ==================== 预约确认 ====================

    /**
     * 确认预约
     * 
     * 业务规则：
     * 1. 只有待确认状态的预约可以确认
     * 2. 确认前需再次检查课程容量
     * 
     * @param bookingId 预约ID
     * @return 操作结果
     */
    public ServiceResult<Booking> confirmBooking(int bookingId) {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            return ServiceResult.failure("确认失败：预约不存在");
        }

        if (!STATUS_PENDING.equals(booking.getBookingStatus())) {
            String statusName = getStatusDisplayName(booking.getBookingStatus());
            return ServiceResult.failure("确认失败：当前状态为「" + statusName + "」，只能确认待确认的预约");
        }

        // 再次检查课程容量
        if (!bookingDAO.checkCourseCapacity(booking.getCourseId())) {
            return ServiceResult.failure("确认失败：课程已满");
        }

        if (bookingDAO.confirmBooking(bookingId)) {
            booking.setBookingStatus(STATUS_CONFIRMED);
            return ServiceResult.success("预约已确认", booking);
        } else {
            return ServiceResult.failure("确认失败：数据库操作失败");
        }
    }

    /**
     * 批量确认预约
     * 
     * @param bookingIds 预约ID列表
     * @return 操作结果，包含成功和失败的数量
     */
    public ServiceResult<Map<String, Integer>> batchConfirmBookings(List<Integer> bookingIds) {
        int successCount = 0;
        int failCount = 0;

        for (int bookingId : bookingIds) {
            ServiceResult<Booking> result = confirmBooking(bookingId);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failCount++;
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("success", successCount);
        resultMap.put("fail", failCount);

        String message = String.format("批量确认完成：成功%d个，失败%d个", successCount, failCount);
        return ServiceResult.success(message, resultMap);
    }

    // ==================== 预约取消 ====================

    /**
     * 取消预约
     * 
     * 业务规则：已取消的预约不能再次取消
     * 
     * @param bookingId 预约ID
     * @param reason    取消原因（可选）
     * @return 操作结果
     */
    public ServiceResult<Booking> cancelBooking(int bookingId, String reason) {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            return ServiceResult.failure("取消失败：预约不存在");
        }

        if (STATUS_CANCELLED.equals(booking.getBookingStatus())) {
            return ServiceResult.failure("取消失败：预约已经被取消");
        }

        if (bookingDAO.cancelBooking(bookingId)) {
            booking.setBookingStatus(STATUS_CANCELLED);
            String message = "预约已取消";
            if (reason != null && !reason.trim().isEmpty()) {
                message += "，原因：" + reason;
            }
            return ServiceResult.success(message, booking);
        } else {
            return ServiceResult.failure("取消失败：数据库操作失败");
        }
    }

    /**
     * 取消预约（无原因）
     */
    public ServiceResult<Booking> cancelBooking(int bookingId) {
        return cancelBooking(bookingId, null);
    }

    /**
     * 会员取消自己的预约
     * 
     * @param memberId  会员ID
     * @param bookingId 预约ID
     * @return 操作结果
     */
    public ServiceResult<Booking> memberCancelBooking(int memberId, int bookingId) {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            return ServiceResult.failure("取消失败：预约不存在");
        }

        // 验证是否是会员自己的预约
        if (booking.getMemberId() != memberId) {
            return ServiceResult.failure("取消失败：只能取消自己的预约");
        }

        return cancelBooking(bookingId, "会员主动取消");
    }

    /**
     * 批量取消预约
     * 
     * @param bookingIds 预约ID列表
     * @param reason     取消原因
     * @return 操作结果
     */
    public ServiceResult<Map<String, Integer>> batchCancelBookings(List<Integer> bookingIds, String reason) {
        int successCount = 0;
        int failCount = 0;

        for (int bookingId : bookingIds) {
            ServiceResult<Booking> result = cancelBooking(bookingId, reason);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failCount++;
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("success", successCount);
        resultMap.put("fail", failCount);

        String message = String.format("批量取消完成：成功%d个，失败%d个", successCount, failCount);
        return ServiceResult.success(message, resultMap);
    }

    /**
     * 取消课程的所有待处理预约
     * 
     * @param courseId 课程ID
     * @param reason   取消原因
     * @return 操作结果
     */
    public ServiceResult<Integer> cancelAllPendingBookingsForCourse(int courseId, String reason) {
        List<Booking> bookings = bookingDAO.getBookingsByCourseId(courseId);
        int cancelledCount = 0;

        for (Booking booking : bookings) {
            if (STATUS_PENDING.equals(booking.getBookingStatus()) || 
                STATUS_CONFIRMED.equals(booking.getBookingStatus())) {
                if (bookingDAO.cancelBooking(booking.getBookingId())) {
                    cancelledCount++;
                }
            }
        }

        return ServiceResult.success("已取消" + cancelledCount + "个预约", cancelledCount);
    }

    // ==================== 预约删除 ====================

    /**
     * 删除预约（物理删除，慎用）
     * 
     * @param bookingId 预约ID
     * @return 操作结果
     */
    public ServiceResult<Void> deleteBooking(int bookingId) {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            return ServiceResult.failure("删除失败：预约不存在");
        }

        // 只允许删除已取消的预约
        if (!STATUS_CANCELLED.equals(booking.getBookingStatus())) {
            return ServiceResult.failure("删除失败：只能删除已取消的预约，请先取消预约");
        }

        if (bookingDAO.deleteBooking(bookingId)) {
            return ServiceResult.success("预约已删除");
        } else {
            return ServiceResult.failure("删除失败：数据库操作失败");
        }
    }

    // ==================== 预约查询 ====================

    /**
     * 根据ID查询预约
     * 
     * @param bookingId 预约ID
     * @return 预约对象
     */
    public Booking getBookingById(int bookingId) {
        return bookingDAO.getBookingById(bookingId);
    }

    /**
     * 查询所有预约
     * 
     * @return 预约列表
     */
    public List<Booking> getAllBookings() {
        return bookingDAO.getAllBookings();
    }

    /**
     * 根据会员ID查询预约
     * 
     * @param memberId 会员ID
     * @return 预约列表
     */
    public List<Booking> getBookingsByMember(int memberId) {
        return bookingDAO.getBookingsByMemberId(memberId);
    }

    /**
     * 根据课程ID查询预约
     * 
     * @param courseId 课程ID
     * @return 预约列表
     */
    public List<Booking> getBookingsByCourse(int courseId) {
        return bookingDAO.getBookingsByCourseId(courseId);
    }

    /**
     * 根据状态查询预约
     * 
     * @param status 预约状态
     * @return 预约列表
     */
    public List<Booking> getBookingsByStatus(String status) {
        return bookingDAO.getBookingsByStatus(status);
    }

    /**
     * 获取待确认的预约
     * 
     * @return 预约列表
     */
    public List<Booking> getPendingBookings() {
        return bookingDAO.getPendingBookings();
    }

    /**
     * 获取已确认的预约
     * 
     * @return 预约列表
     */
    public List<Booking> getConfirmedBookings() {
        return bookingDAO.getBookingsByStatus(STATUS_CONFIRMED);
    }

    /**
     * 获取已取消的预约
     * 
     * @return 预约列表
     */
    public List<Booking> getCancelledBookings() {
        return bookingDAO.getBookingsByStatus(STATUS_CANCELLED);
    }

    /**
     * 获取今日预约
     * 
     * @return 预约列表
     */
    public List<Booking> getTodayBookings() {
        return bookingDAO.getTodayBookings();
    }

    /**
     * 根据教练ID查询预约
     * 
     * @param trainerId 教练ID
     * @return 预约列表
     */
    public List<Booking> getBookingsByTrainer(int trainerId) {
        return bookingDAO.getBookingsByTrainerId(trainerId);
    }

    /**
     * 获取教练今日的预约
     * 
     * @param trainerId 教练ID
     * @return 预约列表
     */
    public List<Booking> getTodayBookingsByTrainer(int trainerId) {
        return bookingDAO.getTodayBookingsByTrainerId(trainerId);
    }

    /**
     * 查询会员的预约历史
     * 
     * @param memberId  会员ID
     * @param startDate 开始日期（可为null）
     * @param endDate   结束日期（可为null）
     * @return 预约列表
     */
    public List<Booking> getMemberBookingHistory(int memberId, Date startDate, Date endDate) {
        return bookingDAO.getBookingHistory(memberId, startDate, endDate);
    }

    /**
     * 查询会员的有效预约（待确认或已确认）
     * 
     * @param memberId 会员ID
     * @return 预约列表
     */
    public List<Booking> getMemberActiveBookings(int memberId) {
        List<Booking> allBookings = bookingDAO.getBookingsByMemberId(memberId);
        List<Booking> activeBookings = new ArrayList<>();
        for (Booking booking : allBookings) {
            if (STATUS_PENDING.equals(booking.getBookingStatus()) ||
                STATUS_CONFIRMED.equals(booking.getBookingStatus())) {
                activeBookings.add(booking);
            }
        }
        return activeBookings;
    }

    // ==================== 预约详情 ====================

    /**
     * 获取预约详细信息（包含会员、课程、教练信息）
     * 
     * @param bookingId 预约ID
     * @return 预约详情
     */
    public BookingDetail getBookingDetail(int bookingId) {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            return null;
        }

        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);

        // 状态信息
        detail.setStatusDisplayName(getStatusDisplayName(booking.getBookingStatus()));

        // 会员信息
        Member member = memberDAO.getMemberById(booking.getMemberId());
        detail.setMember(member);
        detail.setMemberName(member != null ? member.getName() : "未知");

        // 课程信息
        Course course = courseDAO.getCourseById(booking.getCourseId());
        detail.setCourse(course);
        detail.setCourseName(course != null ? course.getName() : "未知");
        detail.setCourseTypeName(course != null ? courseDAO.getTypeDisplayName(course.getType()) : "未知");

        // 教练信息
        if (course != null) {
            Employee trainer = employeeDAO.getEmployeeById(course.getEmployeeId());
            detail.setTrainer(trainer);
            detail.setTrainerName(trainer != null ? trainer.getName() : "未知");
        }

        // 时间信息
        detail.setBookingTimeFormatted(DateUtils.formatDateTime(booking.getBookingTime()));

        return detail;
    }

    // ==================== 预约验证 ====================

    /**
     * 验证会员是否可以预约
     * 
     * @param memberId 会员ID
     * @return 验证结果
     */
    public ServiceResult<Member> validateMemberCanBook(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("会员不存在");
        }

        if (!MemberDAO.STATUS_ACTIVE.equals(member.getStatus())) {
            String statusName = memberDAO.getStatusDisplayName(member.getStatus());
            return ServiceResult.failure("会员状态为「" + statusName + "」，无法预约");
        }

        if (!cardDAO.hasMemberValidCard(memberId)) {
            return ServiceResult.failure("会员没有有效的会员卡，请先开卡或续费");
        }

        return ServiceResult.success("验证通过", member);
    }

    /**
     * 验证课程是否可以预约
     * 
     * @param courseId 课程ID
     * @return 验证结果
     */
    public ServiceResult<Course> validateCourseCanBook(int courseId) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ServiceResult.failure("课程不存在");
        }

        if (courseDAO.isFull(courseId)) {
            return ServiceResult.failure("课程「" + course.getName() + "」已满");
        }

        return ServiceResult.success("验证通过", course);
    }

    /**
     * 验证预约是否可行
     * 
     * @param memberId 会员ID
     * @param courseId 课程ID
     * @return 验证结果
     */
    public ServiceResult<Void> validateBooking(int memberId, int courseId) {
        // 验证会员
        ServiceResult<Member> memberResult = validateMemberCanBook(memberId);
        if (!memberResult.isSuccess()) {
            return ServiceResult.failure(memberResult.getMessage());
        }

        // 验证课程
        ServiceResult<Course> courseResult = validateCourseCanBook(courseId);
        if (!courseResult.isSuccess()) {
            return ServiceResult.failure(courseResult.getMessage());
        }

        // 检查重复预约
        if (bookingDAO.checkDuplicateBooking(memberId, courseId)) {
            return ServiceResult.failure("您已预约过该课程，不能重复预约");
        }

        return ServiceResult.success("可以预约");
    }

    /**
     * 检查是否已预约该课程
     * 
     * @param memberId 会员ID
     * @param courseId 课程ID
     * @return true表示已预约
     */
    public boolean hasBookedCourse(int memberId, int courseId) {
        return bookingDAO.checkDuplicateBooking(memberId, courseId);
    }

    // ==================== 预约统计 ====================

    /**
     * 获取预约总数
     * 
     * @return 预约总数
     */
    public int getTotalBookingCount() {
        return bookingDAO.getAllBookings().size();
    }

    /**
     * 获取今日预约数
     * 
     * @return 今日预约数
     */
    public int getTodayBookingCount() {
        return bookingDAO.getTodayBookingCount();
    }

    /**
     * 按状态统计预约数量
     * 
     * @param status 预约状态
     * @return 预约数量
     */
    public int getBookingCountByStatus(String status) {
        return bookingDAO.getBookingCountByStatus(status);
    }

    /**
     * 获取待确认预约数
     * 
     * @return 待确认数
     */
    public int getPendingBookingCount() {
        return bookingDAO.getBookingCountByStatus(STATUS_PENDING);
    }

    /**
     * 获取已确认预约数
     * 
     * @return 已确认数
     */
    public int getConfirmedBookingCount() {
        return bookingDAO.getBookingCountByStatus(STATUS_CONFIRMED);
    }

    /**
     * 获取已取消预约数
     * 
     * @return 已取消数
     */
    public int getCancelledBookingCount() {
        return bookingDAO.getBookingCountByStatus(STATUS_CANCELLED);
    }

    /**
     * 获取会员的预约统计
     * 
     * @param memberId 会员ID
     * @return 数组 [总预约数, 已确认数, 已取消数]
     */
    public int[] getMemberBookingStats(int memberId) {
        return bookingDAO.getMemberBookingStats(memberId);
    }

    /**
     * 获取课程的预约统计
     * 
     * @param courseId 课程ID
     * @return Map包含各状态数量
     */
    public Map<String, Integer> getCourseBookingStats(int courseId) {
        List<Booking> bookings = bookingDAO.getBookingsByCourseId(courseId);
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", bookings.size());
        stats.put("pending", 0);
        stats.put("confirmed", 0);
        stats.put("cancelled", 0);

        for (Booking booking : bookings) {
            String status = booking.getBookingStatus();
            stats.put(status, stats.get(status) + 1);
        }

        return stats;
    }

    /**
     * 获取预约统计概览
     * 
     * @return 统计概览
     */
    public BookingStatistics getStatistics() {
        BookingStatistics stats = new BookingStatistics();

        stats.setTotalCount(getTotalBookingCount());
        stats.setPendingCount(getPendingBookingCount());
        stats.setConfirmedCount(getConfirmedBookingCount());
        stats.setCancelledCount(getCancelledBookingCount());
        stats.setTodayCount(getTodayBookingCount());

        return stats;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取预约状态的中文名称
     * 
     * @param status 状态
     * @return 中文名称
     */
    public String getStatusDisplayName(String status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case STATUS_PENDING:
                return "待确认";
            case STATUS_CONFIRMED:
                return "已确认";
            case STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 检查预约状态是否有效
     * 
     * @param status 状态
     * @return true表示有效
     */
    public boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status) ||
               STATUS_CONFIRMED.equals(status) ||
               STATUS_CANCELLED.equals(status);
    }

    /**
     * 检查预约是否存在
     * 
     * @param bookingId 预约ID
     * @return true表示存在
     */
    public boolean isBookingExists(int bookingId) {
        return bookingDAO.getBookingById(bookingId) != null;
    }

    // ==================== 内部类：服务结果 ====================

    /**
     * 服务操作结果
     * 
     * @param <T> 数据类型
     */
    public static class ServiceResult<T> {
        private boolean success;
        private String message;
        private T data;

        private ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ServiceResult<T> success(String message, T data) {
            return new ServiceResult<>(true, message, data);
        }

        public static <T> ServiceResult<T> success(String message) {
            return new ServiceResult<>(true, message, null);
        }

        public static <T> ServiceResult<T> failure(String message) {
            return new ServiceResult<>(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }

        @Override
        public String toString() {
            return (success ? "成功" : "失败") + ": " + message;
        }
    }

    // ==================== 内部类：预约详情 ====================

    /**
     * 预约详细信息（包含关联数据）
     */
    public static class BookingDetail {
        private Booking booking;
        private String statusDisplayName;
        private Member member;
        private String memberName;
        private Course course;
        private String courseName;
        private String courseTypeName;
        private Employee trainer;
        private String trainerName;
        private String bookingTimeFormatted;

        // Getters and Setters
        public Booking getBooking() {
            return booking;
        }

        public void setBooking(Booking booking) {
            this.booking = booking;
        }

        public String getStatusDisplayName() {
            return statusDisplayName;
        }

        public void setStatusDisplayName(String statusDisplayName) {
            this.statusDisplayName = statusDisplayName;
        }

        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public Course getCourse() {
            return course;
        }

        public void setCourse(Course course) {
            this.course = course;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getCourseTypeName() {
            return courseTypeName;
        }

        public void setCourseTypeName(String courseTypeName) {
            this.courseTypeName = courseTypeName;
        }

        public Employee getTrainer() {
            return trainer;
        }

        public void setTrainer(Employee trainer) {
            this.trainer = trainer;
        }

        public String getTrainerName() {
            return trainerName;
        }

        public void setTrainerName(String trainerName) {
            this.trainerName = trainerName;
        }

        public String getBookingTimeFormatted() {
            return bookingTimeFormatted;
        }

        public void setBookingTimeFormatted(String bookingTimeFormatted) {
            this.bookingTimeFormatted = bookingTimeFormatted;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 预约详情 ========\n");
            sb.append("预约编号：").append(booking.getBookingId()).append("\n");
            sb.append("预约状态：").append(statusDisplayName).append("\n");
            sb.append("预约时间：").append(bookingTimeFormatted).append("\n");
            sb.append("\n");
            sb.append("======== 会员信息 ========\n");
            sb.append("会员姓名：").append(memberName).append("\n");
            if (member != null) {
                sb.append("会员手机：").append(member.getPhone()).append("\n");
            }
            sb.append("\n");
            sb.append("======== 课程信息 ========\n");
            sb.append("课程名称：").append(courseName).append("\n");
            sb.append("课程类型：").append(courseTypeName).append("\n");
            sb.append("授课教练：").append(trainerName).append("\n");
            return sb.toString();
        }
    }

    // ==================== 内部类：预约统计 ====================

    /**
     * 预约统计信息
     */
    public static class BookingStatistics {
        private int totalCount;
        private int pendingCount;
        private int confirmedCount;
        private int cancelledCount;
        private int todayCount;

        // Getters and Setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(int pendingCount) {
            this.pendingCount = pendingCount;
        }

        public int getConfirmedCount() {
            return confirmedCount;
        }

        public void setConfirmedCount(int confirmedCount) {
            this.confirmedCount = confirmedCount;
        }

        public int getCancelledCount() {
            return cancelledCount;
        }

        public void setCancelledCount(int cancelledCount) {
            this.cancelledCount = cancelledCount;
        }

        public int getTodayCount() {
            return todayCount;
        }

        public void setTodayCount(int todayCount) {
            this.todayCount = todayCount;
        }

        /**
         * 获取确认率
         */
        public double getConfirmRate() {
            int activeCount = pendingCount + confirmedCount + cancelledCount;
            return activeCount > 0 ? (double) confirmedCount / activeCount * 100 : 0;
        }

        /**
         * 获取取消率
         */
        public double getCancelRate() {
            return totalCount > 0 ? (double) cancelledCount / totalCount * 100 : 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 预约统计 ========\n");
            sb.append("预约总数：").append(totalCount).append("\n");
            sb.append("  - 待确认：").append(pendingCount).append("\n");
            sb.append("  - 已确认：").append(confirmedCount)
              .append(" (").append(String.format("%.1f", getConfirmRate())).append("%)\n");
            sb.append("  - 已取消：").append(cancelledCount)
              .append(" (").append(String.format("%.1f", getCancelRate())).append("%)\n");
            sb.append("\n");
            sb.append("今日预约：").append(todayCount).append("\n");
            return sb.toString();
        }
    }
}

