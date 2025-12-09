package service;

import dao.CheckInDAO;
import dao.MemberDAO;
import dao.MembershipCardDAO;
import entity.CheckIn;
import entity.Member;
import utils.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 签到服务类
 * 提供签到相关的业务逻辑处理
 * 
 * 主要功能：
 * - 会员签到/签退
 * - 签到记录查询
 * - 签到统计报表
 * - 健身时长计算
 * - 自动签退管理
 * 
 * 业务流程：
 * 1. 会员签到 → 创建记录（checkin_time = 当前时间，checkout_time = NULL）
 * 2. 会员签退 → 更新记录（checkout_time = 当前时间）
 * 
 * @author GymSystem
 * @version 1.0
 */
public class CheckInService {

    // ==================== 依赖的DAO ====================

    private CheckInDAO checkInDAO;
    private MemberDAO memberDAO;
    private MembershipCardDAO cardDAO;

    // ==================== 默认配置 ====================

    /** 默认最大签到时长（小时），超过此时长自动签退 */
    public static final int DEFAULT_MAX_CHECKIN_HOURS = 12;

    // ==================== 构造方法 ====================

    public CheckInService() {
        this.checkInDAO = new CheckInDAO();
        this.memberDAO = new MemberDAO();
        this.cardDAO = new MembershipCardDAO();
    }

    // ==================== 签到操作 ====================

    /**
     * 会员签到
     * 
     * 业务规则：
     * 1. 会员必须存在且状态为激活
     * 2. 会员必须有有效的会员卡
     * 3. 不能重复签到（已有未签退记录时）
     * 
     * @param memberId 会员ID
     * @return 签到结果，包含成功/失败信息和签到记录
     */
    public ServiceResult<CheckIn> checkIn(int memberId) {
        // 验证会员
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("签到失败：会员不存在");
        }
        if (!MemberDAO.STATUS_ACTIVE.equals(member.getStatus())) {
            String statusName = memberDAO.getStatusDisplayName(member.getStatus());
            return ServiceResult.failure("签到失败：会员状态为「" + statusName + "」，无法签到");
        }

        // 验证会员卡
        if (!cardDAO.hasMemberValidCard(memberId)) {
            return ServiceResult.failure("签到失败：会员没有有效的会员卡，请先开卡或续费");
        }

        // 检查是否已签到未签退
        if (checkInDAO.hasActiveCheckIn(memberId)) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(memberId);
            String checkinTime = DateUtils.formatDateTime(currentCheckIn.getCheckinTime());
            return ServiceResult.failure("签到失败：您已于 " + checkinTime + " 签到，请先签退");
        }

        // 创建签到记录
        CheckIn checkIn = new CheckIn();
        checkIn.setMemberId(memberId);
        checkIn.setCheckinTime(DateUtils.now());

        if (checkInDAO.checkIn(checkIn)) {
            return ServiceResult.success("签到成功，欢迎光临！", checkIn);
        } else {
            return ServiceResult.failure("签到失败：数据库操作失败");
        }
    }

    /**
     * 快速签到（通过手机号）
     * 
     * @param phone 会员手机号
     * @return 签到结果
     */
    public ServiceResult<CheckIn> checkInByPhone(String phone) {
        Member member = memberDAO.getMemberByPhone(phone);
        if (member == null) {
            return ServiceResult.failure("签到失败：未找到该手机号对应的会员");
        }
        return checkIn(member.getId());
    }

    // ==================== 签退操作 ====================

    /**
     * 会员签退
     * 
     * @param memberId 会员ID
     * @return 签退结果
     */
    public ServiceResult<CheckIn> checkOut(int memberId) {
        // 验证会员
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("签退失败：会员不存在");
        }

        // 获取当前签到记录
        CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(memberId);
        if (currentCheckIn == null) {
            return ServiceResult.failure("签退失败：您没有未签退的签到记录");
        }

        // 执行签退
        if (checkInDAO.checkOut(currentCheckIn.getCheckinId())) {
            // 重新获取签退后的记录
            CheckIn updatedCheckIn = checkInDAO.getCheckInById(currentCheckIn.getCheckinId());
            String duration = DateUtils.calculateCheckinDuration(
                    updatedCheckIn.getCheckinTime(), 
                    updatedCheckIn.getCheckoutTime()
            );
            return ServiceResult.success("签退成功，本次健身时长：" + duration, updatedCheckIn);
        } else {
            return ServiceResult.failure("签退失败：数据库操作失败");
        }
    }

    /**
     * 快速签退（通过手机号）
     * 
     * @param phone 会员手机号
     * @return 签退结果
     */
    public ServiceResult<CheckIn> checkOutByPhone(String phone) {
        Member member = memberDAO.getMemberByPhone(phone);
        if (member == null) {
            return ServiceResult.failure("签退失败：未找到该手机号对应的会员");
        }
        return checkOut(member.getId());
    }

    /**
     * 根据签到记录ID签退
     * 
     * @param checkinId 签到记录ID
     * @return 签退结果
     */
    public ServiceResult<CheckIn> checkOutById(int checkinId) {
        CheckIn checkIn = checkInDAO.getCheckInById(checkinId);
        if (checkIn == null) {
            return ServiceResult.failure("签退失败：签到记录不存在");
        }

        if (checkIn.getCheckoutTime() != null) {
            return ServiceResult.failure("签退失败：该记录已签退");
        }

        if (checkInDAO.checkOut(checkinId)) {
            CheckIn updatedCheckIn = checkInDAO.getCheckInById(checkinId);
            String duration = DateUtils.calculateCheckinDuration(
                    updatedCheckIn.getCheckinTime(), 
                    updatedCheckIn.getCheckoutTime()
            );
            return ServiceResult.success("签退成功，本次健身时长：" + duration, updatedCheckIn);
        } else {
            return ServiceResult.failure("签退失败：数据库操作失败");
        }
    }

    // ==================== 签到记录删除 ====================

    /**
     * 删除签到记录（管理员功能）
     * 
     * @param checkinId 签到记录ID
     * @return 操作结果
     */
    public ServiceResult<Void> deleteCheckIn(int checkinId) {
        CheckIn checkIn = checkInDAO.getCheckInById(checkinId);
        if (checkIn == null) {
            return ServiceResult.failure("删除失败：签到记录不存在");
        }

        if (checkInDAO.deleteCheckIn(checkinId)) {
            return ServiceResult.success("签到记录已删除");
        } else {
            return ServiceResult.failure("删除失败：数据库操作失败");
        }
    }

    // ==================== 签到查询 ====================

    /**
     * 根据ID查询签到记录
     * 
     * @param checkinId 签到记录ID
     * @return 签到记录
     */
    public CheckIn getCheckInById(int checkinId) {
        return checkInDAO.getCheckInById(checkinId);
    }

    /**
     * 查询所有签到记录
     * 
     * @return 签到记录列表
     */
    public List<CheckIn> getAllCheckIns() {
        return checkInDAO.getAllCheckIns();
    }

    /**
     * 查询会员的所有签到记录
     * 
     * @param memberId 会员ID
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInsByMember(int memberId) {
        return checkInDAO.getCheckInsByMemberId(memberId);
    }

    /**
     * 获取会员当前的签到记录（未签退的）
     * 
     * @param memberId 会员ID
     * @return 签到记录，没有则返回null
     */
    public CheckIn getCurrentCheckIn(int memberId) {
        return checkInDAO.getCurrentCheckIn(memberId);
    }

    /**
     * 查询会员的签到历史
     * 
     * @param memberId  会员ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 签到记录列表
     */
    public List<CheckIn> getMemberCheckInHistory(int memberId, Date startDate, Date endDate) {
        return checkInDAO.getCheckInHistory(memberId, startDate, endDate);
    }

    /**
     * 获取今日所有签到记录
     * 
     * @return 签到记录列表
     */
    public List<CheckIn> getTodayCheckIns() {
        return checkInDAO.getTodayCheckIns();
    }

    /**
     * 获取当前在馆会员的签到记录
     * 
     * @return 签到记录列表
     */
    public List<CheckIn> getCurrentlyCheckedIn() {
        return checkInDAO.getCurrentlyCheckedIn();
    }

    /**
     * 按日期查询签到记录
     * 
     * @param date 日期
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInsByDate(Date date) {
        return checkInDAO.getCheckInsByDate(date);
    }

    /**
     * 按日期范围查询签到记录
     * 
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInsByDateRange(Date startDate, Date endDate) {
        return checkInDAO.getCheckInsByDateRange(startDate, endDate);
    }

    // ==================== 签到详情 ====================

    /**
     * 获取签到详细信息（包含会员信息）
     * 
     * @param checkinId 签到记录ID
     * @return 签到详情
     */
    public CheckInDetail getCheckInDetail(int checkinId) {
        CheckIn checkIn = checkInDAO.getCheckInById(checkinId);
        if (checkIn == null) {
            return null;
        }

        CheckInDetail detail = new CheckInDetail();
        detail.setCheckIn(checkIn);

        // 会员信息
        Member member = memberDAO.getMemberById(checkIn.getMemberId());
        detail.setMember(member);
        detail.setMemberName(member != null ? member.getName() : "未知");
        detail.setMemberPhone(member != null ? member.getPhone() : "未知");

        // 时间信息
        detail.setCheckinTimeFormatted(DateUtils.formatDateTime(checkIn.getCheckinTime()));
        if (checkIn.getCheckoutTime() != null) {
            detail.setCheckoutTimeFormatted(DateUtils.formatDateTime(checkIn.getCheckoutTime()));
            detail.setDuration(DateUtils.calculateCheckinDuration(
                    checkIn.getCheckinTime(), checkIn.getCheckoutTime()));
            detail.setDurationMinutes(DateUtils.minutesBetween(
                    checkIn.getCheckinTime(), checkIn.getCheckoutTime()));
        } else {
            detail.setCheckoutTimeFormatted("未签退");
            detail.setDuration("进行中");
            detail.setDurationMinutes(DateUtils.minutesBetween(
                    checkIn.getCheckinTime(), DateUtils.now()));
        }

        detail.setCheckedOut(checkIn.getCheckoutTime() != null);

        return detail;
    }

    // ==================== 签到验证 ====================

    /**
     * 验证会员是否可以签到
     * 
     * @param memberId 会员ID
     * @return 验证结果
     */
    public ServiceResult<Member> validateMemberCanCheckIn(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("会员不存在");
        }

        if (!MemberDAO.STATUS_ACTIVE.equals(member.getStatus())) {
            String statusName = memberDAO.getStatusDisplayName(member.getStatus());
            return ServiceResult.failure("会员状态为「" + statusName + "」，无法签到");
        }

        if (!cardDAO.hasMemberValidCard(memberId)) {
            return ServiceResult.failure("会员没有有效的会员卡，请先开卡或续费");
        }

        if (checkInDAO.hasActiveCheckIn(memberId)) {
            return ServiceResult.failure("会员已签到未签退");
        }

        return ServiceResult.success("可以签到", member);
    }

    /**
     * 检查会员是否已签到（在馆中）
     * 
     * @param memberId 会员ID
     * @return true表示已签到未签退
     */
    public boolean isMemberCheckedIn(int memberId) {
        return checkInDAO.hasActiveCheckIn(memberId);
    }

    /**
     * 检查签到记录是否存在
     * 
     * @param checkinId 签到记录ID
     * @return true表示存在
     */
    public boolean isCheckInExists(int checkinId) {
        return checkInDAO.getCheckInById(checkinId) != null;
    }

    // ==================== 签到统计 ====================

    /**
     * 获取今日签到人数
     * 
     * @return 签到人数
     */
    public int getTodayCheckInCount() {
        return checkInDAO.getTodayCheckInCount();
    }

    /**
     * 获取当前在馆人数
     * 
     * @return 在馆人数
     */
    public int getCurrentlyCheckedInCount() {
        return checkInDAO.getCurrentlyCheckedInCount();
    }

    /**
     * 获取会员本月签到次数
     * 
     * @param memberId 会员ID
     * @return 签到次数
     */
    public int getMemberMonthlyCheckInCount(int memberId) {
        return checkInDAO.getMonthlyCheckInCount(memberId);
    }

    /**
     * 获取会员总签到次数
     * 
     * @param memberId 会员ID
     * @return 签到次数
     */
    public int getMemberTotalCheckInCount(int memberId) {
        return checkInDAO.getTotalCheckInCount(memberId);
    }

    /**
     * 获取指定日期的签到人数
     * 
     * @param date 日期
     * @return 签到人数
     */
    public int getCheckInCountByDate(Date date) {
        return checkInDAO.getCheckInCountByDate(date);
    }

    /**
     * 按小时统计签到人数（高峰期分析）
     * 
     * @param date 日期
     * @return Map<小时(0-23), 签到人数>
     */
    public Map<Integer, Integer> getCheckInCountByHour(Date date) {
        return checkInDAO.getCheckInCountByHour(date);
    }

    /**
     * 获取签到统计概览
     * 
     * @return 统计概览
     */
    public CheckInStatistics getStatistics() {
        CheckInStatistics stats = new CheckInStatistics();

        stats.setTodayCount(checkInDAO.getTodayCheckInCount());
        stats.setCurrentlyCheckedInCount(checkInDAO.getCurrentlyCheckedInCount());
        stats.setTotalRecords(checkInDAO.getAllCheckIns().size());

        // 今日高峰时段分析
        Map<Integer, Integer> hourlyCount = checkInDAO.getCheckInCountByHour(DateUtils.now());
        int peakHour = 0;
        int peakCount = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyCount.entrySet()) {
            if (entry.getValue() > peakCount) {
                peakCount = entry.getValue();
                peakHour = entry.getKey();
            }
        }
        stats.setPeakHour(peakHour);
        stats.setPeakHourCount(peakCount);

        return stats;
    }

    // ==================== 健身时长 ====================

    /**
     * 计算单次健身时长
     * 
     * @param checkinId 签到记录ID
     * @return 格式化后的时长字符串
     */
    public String getStayDuration(int checkinId) {
        return checkInDAO.getStayDuration(checkinId);
    }

    /**
     * 获取会员今日健身时长
     * 
     * @param memberId 会员ID
     * @return 格式化后的时长字符串
     */
    public String getMemberTodayDuration(int memberId) {
        return checkInDAO.getTodayStayDuration(memberId);
    }

    /**
     * 获取会员本月总健身时长
     * 
     * @param memberId 会员ID
     * @return 格式化后的时长字符串
     */
    public String getMemberMonthlyDuration(int memberId) {
        return checkInDAO.getMonthlyTotalDuration(memberId);
    }

    /**
     * 获取会员本月总健身时长（分钟）
     * 
     * @param memberId 会员ID
     * @return 总时长（分钟）
     */
    public long getMemberMonthlyDurationMinutes(int memberId) {
        return checkInDAO.getMonthlyTotalMinutes(memberId);
    }

    /**
     * 获取会员平均健身时长
     * 
     * @param memberId 会员ID
     * @return 平均时长（分钟）
     */
    public double getMemberAverageDuration(int memberId) {
        return checkInDAO.getAverageStayDuration(memberId);
    }

    /**
     * 获取会员平均健身时长（格式化）
     * 
     * @param memberId 会员ID
     * @return 格式化后的时长字符串
     */
    public String getMemberAverageDurationFormatted(int memberId) {
        double avgMinutes = checkInDAO.getAverageStayDuration(memberId);
        return DateUtils.formatDuration((long) avgMinutes);
    }

    // ==================== 自动签退 ====================

    /**
     * 自动签退超时的签到记录
     * 
     * @param maxHours 最大允许的签到时长（小时）
     * @return 操作结果，包含自动签退的记录数
     */
    public ServiceResult<Integer> autoCheckOutOvertime(int maxHours) {
        if (maxHours <= 0) {
            return ServiceResult.failure("最大时长必须大于0");
        }

        int count = checkInDAO.autoCheckOutOvertime(maxHours);
        if (count > 0) {
            return ServiceResult.success("已自动签退" + count + "条超时记录", count);
        } else {
            return ServiceResult.success("没有需要自动签退的记录", 0);
        }
    }

    /**
     * 自动签退超时的签到记录（使用默认时长）
     */
    public ServiceResult<Integer> autoCheckOutOvertime() {
        return autoCheckOutOvertime(DEFAULT_MAX_CHECKIN_HOURS);
    }

    /**
     * 获取超时未签退的签到记录
     * 
     * @param maxHours 最大允许的签到时长（小时）
     * @return 超时的签到记录列表
     */
    public List<CheckIn> getOvertimeCheckIns(int maxHours) {
        return checkInDAO.getOvertimeCheckIns(maxHours);
    }

    /**
     * 获取超时未签退的签到记录（使用默认时长）
     */
    public List<CheckIn> getOvertimeCheckIns() {
        return getOvertimeCheckIns(DEFAULT_MAX_CHECKIN_HOURS);
    }

    // ==================== 会员签到信息汇总 ====================

    /**
     * 获取会员签到信息汇总
     * 
     * @param memberId 会员ID
     * @return 签到汇总信息
     */
    public MemberCheckInSummary getMemberCheckInSummary(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return null;
        }

        MemberCheckInSummary summary = new MemberCheckInSummary();
        summary.setMemberId(memberId);
        summary.setMemberName(member.getName());

        // 签到统计
        summary.setTotalCheckInCount(checkInDAO.getTotalCheckInCount(memberId));
        summary.setMonthlyCheckInCount(checkInDAO.getMonthlyCheckInCount(memberId));

        // 当前状态
        summary.setCurrentlyCheckedIn(checkInDAO.hasActiveCheckIn(memberId));
        if (summary.isCurrentlyCheckedIn()) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(memberId);
            summary.setCurrentCheckinTime(currentCheckIn.getCheckinTime());
        }

        // 时长统计
        summary.setTodayDuration(checkInDAO.getTodayStayDuration(memberId));
        summary.setMonthlyDuration(checkInDAO.getMonthlyTotalDuration(memberId));
        summary.setAverageDuration(checkInDAO.getAverageStayDuration(memberId));

        return summary;
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

    // ==================== 内部类：签到详情 ====================

    /**
     * 签到详细信息（包含会员信息）
     */
    public static class CheckInDetail {
        private CheckIn checkIn;
        private Member member;
        private String memberName;
        private String memberPhone;
        private String checkinTimeFormatted;
        private String checkoutTimeFormatted;
        private String duration;
        private long durationMinutes;
        private boolean isCheckedOut;

        // Getters and Setters
        public CheckIn getCheckIn() {
            return checkIn;
        }

        public void setCheckIn(CheckIn checkIn) {
            this.checkIn = checkIn;
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

        public String getMemberPhone() {
            return memberPhone;
        }

        public void setMemberPhone(String memberPhone) {
            this.memberPhone = memberPhone;
        }

        public String getCheckinTimeFormatted() {
            return checkinTimeFormatted;
        }

        public void setCheckinTimeFormatted(String checkinTimeFormatted) {
            this.checkinTimeFormatted = checkinTimeFormatted;
        }

        public String getCheckoutTimeFormatted() {
            return checkoutTimeFormatted;
        }

        public void setCheckoutTimeFormatted(String checkoutTimeFormatted) {
            this.checkoutTimeFormatted = checkoutTimeFormatted;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public long getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(long durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public boolean isCheckedOut() {
            return isCheckedOut;
        }

        public void setCheckedOut(boolean checkedOut) {
            isCheckedOut = checkedOut;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 签到详情 ========\n");
            sb.append("签到编号：").append(checkIn.getCheckinId()).append("\n");
            sb.append("会员姓名：").append(memberName).append("\n");
            sb.append("会员手机：").append(memberPhone).append("\n");
            sb.append("签到时间：").append(checkinTimeFormatted).append("\n");
            sb.append("签退时间：").append(checkoutTimeFormatted).append("\n");
            sb.append("健身时长：").append(duration).append("\n");
            return sb.toString();
        }
    }

    // ==================== 内部类：签到统计 ====================

    /**
     * 签到统计信息
     */
    public static class CheckInStatistics {
        private int todayCount;
        private int currentlyCheckedInCount;
        private int totalRecords;
        private int peakHour;
        private int peakHourCount;

        // Getters and Setters
        public int getTodayCount() {
            return todayCount;
        }

        public void setTodayCount(int todayCount) {
            this.todayCount = todayCount;
        }

        public int getCurrentlyCheckedInCount() {
            return currentlyCheckedInCount;
        }

        public void setCurrentlyCheckedInCount(int currentlyCheckedInCount) {
            this.currentlyCheckedInCount = currentlyCheckedInCount;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getPeakHour() {
            return peakHour;
        }

        public void setPeakHour(int peakHour) {
            this.peakHour = peakHour;
        }

        public int getPeakHourCount() {
            return peakHourCount;
        }

        public void setPeakHourCount(int peakHourCount) {
            this.peakHourCount = peakHourCount;
        }

        /**
         * 获取高峰时段描述
         */
        public String getPeakHourDescription() {
            return String.format("%02d:00 - %02d:00", peakHour, peakHour + 1);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 签到统计 ========\n");
            sb.append("今日签到：").append(todayCount).append("人次\n");
            sb.append("当前在馆：").append(currentlyCheckedInCount).append("人\n");
            sb.append("历史总记录：").append(totalRecords).append("条\n");
            sb.append("\n");
            sb.append("今日高峰时段：").append(getPeakHourDescription()).append("\n");
            sb.append("高峰时段签到：").append(peakHourCount).append("人次\n");
            return sb.toString();
        }
    }

    // ==================== 内部类：会员签到汇总 ====================

    /**
     * 会员签到信息汇总
     */
    public static class MemberCheckInSummary {
        private int memberId;
        private String memberName;
        private int totalCheckInCount;
        private int monthlyCheckInCount;
        private boolean currentlyCheckedIn;
        private Date currentCheckinTime;
        private String todayDuration;
        private String monthlyDuration;
        private double averageDuration;

        // Getters and Setters
        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public int getTotalCheckInCount() {
            return totalCheckInCount;
        }

        public void setTotalCheckInCount(int totalCheckInCount) {
            this.totalCheckInCount = totalCheckInCount;
        }

        public int getMonthlyCheckInCount() {
            return monthlyCheckInCount;
        }

        public void setMonthlyCheckInCount(int monthlyCheckInCount) {
            this.monthlyCheckInCount = monthlyCheckInCount;
        }

        public boolean isCurrentlyCheckedIn() {
            return currentlyCheckedIn;
        }

        public void setCurrentlyCheckedIn(boolean currentlyCheckedIn) {
            this.currentlyCheckedIn = currentlyCheckedIn;
        }

        public Date getCurrentCheckinTime() {
            return currentCheckinTime;
        }

        public void setCurrentCheckinTime(Date currentCheckinTime) {
            this.currentCheckinTime = currentCheckinTime;
        }

        public String getTodayDuration() {
            return todayDuration;
        }

        public void setTodayDuration(String todayDuration) {
            this.todayDuration = todayDuration;
        }

        public String getMonthlyDuration() {
            return monthlyDuration;
        }

        public void setMonthlyDuration(String monthlyDuration) {
            this.monthlyDuration = monthlyDuration;
        }

        public double getAverageDuration() {
            return averageDuration;
        }

        public void setAverageDuration(double averageDuration) {
            this.averageDuration = averageDuration;
        }

        /**
         * 获取平均时长格式化字符串
         */
        public String getAverageDurationFormatted() {
            return DateUtils.formatDuration((long) averageDuration);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 会员签到汇总 ========\n");
            sb.append("会员姓名：").append(memberName).append("\n");
            sb.append("当前状态：").append(currentlyCheckedIn ? "在馆中" : "不在馆").append("\n");
            if (currentlyCheckedIn && currentCheckinTime != null) {
                sb.append("签到时间：").append(DateUtils.formatDateTime(currentCheckinTime)).append("\n");
            }
            sb.append("\n");
            sb.append("======== 签到统计 ========\n");
            sb.append("本月签到：").append(monthlyCheckInCount).append("次\n");
            sb.append("累计签到：").append(totalCheckInCount).append("次\n");
            sb.append("\n");
            sb.append("======== 健身时长 ========\n");
            sb.append("今日时长：").append(todayDuration).append("\n");
            sb.append("本月时长：").append(monthlyDuration).append("\n");
            sb.append("平均时长：").append(getAverageDurationFormatted()).append("\n");
            return sb.toString();
        }
    }
}

