package dao;

import entity.CheckIn;
import utils.DBUtil;
import utils.DateUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签到数据访问对象
 * 对应数据库 check_in 表
 * 
 * 业务流程：
 * 1. 会员签到 → 创建记录（checkin_time = 当前时间，checkout_time = NULL）
 * 2. 会员签退 → 更新记录（checkout_time = 当前时间）
 * 
 * 业务规则：
 * - 签到前需验证会员卡有效性
 * - 不允许重复签到（已有未签退记录时）
 */
public class CheckInDAO {

    // ==================== 结果集映射 ====================

    /**
     * 从结果集中提取签到信息
     * 
     * @param rs 结果集
     * @return CheckIn对象
     * @throws SQLException SQL异常
     */
    private CheckIn extractCheckInFromResultSet(ResultSet rs) throws SQLException {
        CheckIn checkIn = new CheckIn();
        checkIn.setCheckinId(rs.getInt("checkin_id"));
        checkIn.setMemberId(rs.getInt("member_id"));
        checkIn.setCheckinTime(rs.getTimestamp("checkin_time"));
        checkIn.setCheckoutTime(rs.getTimestamp("checkout_time"));
        return checkIn;
    }

    // ==================== 签到操作 ====================

    /**
     * 会员签到
     * 
     * 业务规则：
     * 1. 会员卡必须有效（active状态且未过期）
     * 2. 不能重复签到（当前已有未签退记录）
     * 
     * @param memberId 会员ID
     * @return 是否签到成功
     */
    public boolean checkIn(int memberId) {
        // 第1步：验证会员卡有效性
        if (!checkMembershipValid(memberId)) {
            System.err.println("签到失败：会员卡无效或已过期 (memberId=" + memberId + ")");
            return false;
        }

        // 第2步：检查是否已签到未签退
        if (hasActiveCheckIn(memberId)) {
            System.err.println("签到失败：该会员已签到未签退 (memberId=" + memberId + ")");
            return false;
        }

        // 第3步：创建签到记录
        String sql = "INSERT INTO check_in (member_id, checkin_time) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, memberId);
            pstmt.setTimestamp(2, DateUtils.nowTimestamp());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 会员签到（使用CheckIn对象）
     * 
     * @param checkIn 签到对象
     * @return 是否签到成功
     */
    public boolean checkIn(CheckIn checkIn) {
        // 验证会员卡有效性
        if (!checkMembershipValid(checkIn.getMemberId())) {
            System.err.println("签到失败：会员卡无效或已过期 (memberId=" + checkIn.getMemberId() + ")");
            return false;
        }

        // 检查是否已签到未签退
        if (hasActiveCheckIn(checkIn.getMemberId())) {
            System.err.println("签到失败：该会员已签到未签退 (memberId=" + checkIn.getMemberId() + ")");
            return false;
        }

        String sql = "INSERT INTO check_in (member_id, checkin_time) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, checkIn.getMemberId());
            // 如果没有设置签到时间，使用当前时间
            pstmt.setTimestamp(2, checkIn.getCheckinTime() != null 
                    ? DateUtils.toSqlTimestamp(checkIn.getCheckinTime()) 
                    : DateUtils.nowTimestamp());

            int affectedRows = pstmt.executeUpdate();

            // 获取自动生成的ID
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        checkIn.setCheckinId(rs.getInt(1));
                    }
                }
            }
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 签退操作 ====================

    /**
     * 根据签到ID签退
     * 
     * @param checkinId 签到记录ID
     * @return 是否签退成功
     */
    public boolean checkOut(int checkinId) {
        String sql = "UPDATE check_in SET checkout_time = ? WHERE checkin_id = ? AND checkout_time IS NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, DateUtils.nowTimestamp());
            pstmt.setInt(2, checkinId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据会员ID签退（自动找到未签退的记录）
     * 
     * @param memberId 会员ID
     * @return 是否签退成功
     */
    public boolean checkOutByMemberId(int memberId) {
        // 找到该会员当前未签退的记录
        CheckIn currentCheckIn = getCurrentCheckIn(memberId);
        if (currentCheckIn == null) {
            System.err.println("签退失败：该会员没有未签退的签到记录 (memberId=" + memberId + ")");
            return false;
        }
        return checkOut(currentCheckIn.getCheckinId());
    }

    // ==================== 基础查询 ====================

    /**
     * 根据ID查询签到记录
     * 
     * @param checkinId 签到记录ID
     * @return CheckIn对象，不存在返回null
     */
    public CheckIn getCheckInById(int checkinId) {
        String sql = "SELECT * FROM check_in WHERE checkin_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, checkinId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCheckInFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有签到记录
     * 
     * @return 签到记录列表
     */
    public List<CheckIn> getAllCheckIns() {
        List<CheckIn> checkIns = new ArrayList<>();
        String sql = "SELECT * FROM check_in ORDER BY checkin_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                checkIns.add(extractCheckInFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }

    /**
     * 删除签到记录（管理员功能）
     * 
     * @param checkinId 签到记录ID
     * @return 是否删除成功
     */
    public boolean deleteCheckIn(int checkinId) {
        String sql = "DELETE FROM check_in WHERE checkin_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, checkinId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 会员相关查询 ====================

    /**
     * 查询会员的所有签到记录
     * 
     * @param memberId 会员ID
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInsByMemberId(int memberId) {
        List<CheckIn> checkIns = new ArrayList<>();
        String sql = "SELECT * FROM check_in WHERE member_id = ? ORDER BY checkin_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    checkIns.add(extractCheckInFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }

    /**
     * 查询会员当前是否已签到（未签退）
     * 
     * @param memberId 会员ID
     * @return true表示已签到未签退
     */
    public boolean isMemberCheckedIn(int memberId) {
        return hasActiveCheckIn(memberId);
    }

    /**
     * 获取会员当前的签到记录（未签退的）
     * 
     * @param memberId 会员ID
     * @return CheckIn对象，没有则返回null
     */
    public CheckIn getCurrentCheckIn(int memberId) {
        String sql = "SELECT * FROM check_in WHERE member_id = ? AND checkout_time IS NULL ORDER BY checkin_time DESC LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCheckInFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询会员的签到历史（日期范围）
     * 
     * @param memberId  会员ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInHistory(int memberId, Date startDate, Date endDate) {
        List<CheckIn> checkIns = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM check_in WHERE member_id = ?");

        if (startDate != null && endDate != null) {
            sql.append(" AND checkin_time BETWEEN ? AND ?");
        } else if (startDate != null) {
            sql.append(" AND checkin_time >= ?");
        } else if (endDate != null) {
            sql.append(" AND checkin_time <= ?");
        }
        sql.append(" ORDER BY checkin_time DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            pstmt.setInt(idx++, memberId);

            if (startDate != null && endDate != null) {
                pstmt.setTimestamp(idx++, DateUtils.toSqlTimestamp(startDate));
                pstmt.setTimestamp(idx++, DateUtils.toSqlTimestamp(endDate));
            } else if (startDate != null) {
                pstmt.setTimestamp(idx++, DateUtils.toSqlTimestamp(startDate));
            } else if (endDate != null) {
                pstmt.setTimestamp(idx++, DateUtils.toSqlTimestamp(endDate));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    checkIns.add(extractCheckInFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }

    /**
     * 查询会员的签到历史（字符串日期参数）
     * 
     * @param memberId     会员ID
     * @param startDateStr 开始日期字符串（yyyy-MM-dd）
     * @param endDateStr   结束日期字符串（yyyy-MM-dd）
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInHistory(int memberId, String startDateStr, String endDateStr) {
        Date startDate = DateUtils.parseDate(startDateStr);
        Date endDate = DateUtils.parseDate(endDateStr);
        return getCheckInHistory(memberId, startDate, endDate);
    }

    // ==================== 时间相关查询 ====================

    /**
     * 获取今日所有签到记录
     * 
     * @return 签到记录列表
     */
    public List<CheckIn> getTodayCheckIns() {
        List<CheckIn> checkIns = new ArrayList<>();
        String sql = "SELECT * FROM check_in WHERE DATE(checkin_time) = CURDATE() ORDER BY checkin_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                checkIns.add(extractCheckInFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }

    /**
     * 获取当前在馆会员的签到记录（已签到未签退）
     * 
     * @return 签到记录列表
     */
    public List<CheckIn> getCurrentlyCheckedIn() {
        List<CheckIn> checkIns = new ArrayList<>();
        String sql = "SELECT * FROM check_in WHERE checkout_time IS NULL ORDER BY checkin_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                checkIns.add(extractCheckInFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }

    /**
     * 按日期查询签到记录
     * 
     * @param date 日期
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInsByDate(Date date) {
        List<CheckIn> checkIns = new ArrayList<>();
        String sql = "SELECT * FROM check_in WHERE DATE(checkin_time) = ? ORDER BY checkin_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, DateUtils.toSqlDate(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    checkIns.add(extractCheckInFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }

    /**
     * 按日期范围查询签到记录
     * 
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 签到记录列表
     */
    public List<CheckIn> getCheckInsByDateRange(Date startDate, Date endDate) {
        List<CheckIn> checkIns = new ArrayList<>();
        String sql = "SELECT * FROM check_in WHERE checkin_time BETWEEN ? AND ? ORDER BY checkin_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, DateUtils.toSqlTimestamp(startDate));
            pstmt.setTimestamp(2, DateUtils.toSqlTimestamp(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    checkIns.add(extractCheckInFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }

    // ==================== 业务校验 ====================

    /**
     * 检查会员卡是否有效
     * 
     * @param memberId 会员ID
     * @return true表示有效
     */
    public boolean checkMembershipValid(int memberId) {
        String sql = "SELECT COUNT(*) AS count FROM membership_card " +
                "WHERE member_id = ? AND card_status = 'active' AND end_date >= CURDATE()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查会员是否有未签退的签到记录
     * 
     * @param memberId 会员ID
     * @return true表示有未签退记录
     */
    public boolean hasActiveCheckIn(int memberId) {
        String sql = "SELECT COUNT(*) AS count FROM check_in WHERE member_id = ? AND checkout_time IS NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 统计功能 ====================

    /**
     * 获取今日签到人数
     * 
     * @return 签到人数
     */
    public int getTodayCheckInCount() {
        String sql = "SELECT COUNT(*) AS count FROM check_in WHERE DATE(checkin_time) = CURDATE()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前在馆人数
     * 
     * @return 在馆人数
     */
    public int getCurrentlyCheckedInCount() {
        String sql = "SELECT COUNT(*) AS count FROM check_in WHERE checkout_time IS NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取会员本月签到次数
     * 
     * @param memberId 会员ID
     * @return 签到次数
     */
    public int getMonthlyCheckInCount(int memberId) {
        String sql = "SELECT COUNT(*) AS count FROM check_in " +
                "WHERE member_id = ? AND YEAR(checkin_time) = YEAR(CURDATE()) AND MONTH(checkin_time) = MONTH(CURDATE())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取会员总签到次数
     * 
     * @param memberId 会员ID
     * @return 签到次数
     */
    public int getTotalCheckInCount(int memberId) {
        String sql = "SELECT COUNT(*) AS count FROM check_in WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取会员平均健身时长（分钟）
     * 
     * @param memberId 会员ID
     * @return 平均时长（分钟），没有记录返回0
     */
    public double getAverageStayDuration(int memberId) {
        String sql = "SELECT AVG(TIMESTAMPDIFF(MINUTE, checkin_time, checkout_time)) AS avg_duration " +
                "FROM check_in WHERE member_id = ? AND checkout_time IS NOT NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_duration");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 按小时统计签到人数（高峰期分析）
     * 
     * @param date 日期
     * @return Map<小时(0-23), 签到人数>
     */
    public Map<Integer, Integer> getCheckInCountByHour(Date date) {
        Map<Integer, Integer> hourlyCount = new HashMap<>();
        // 初始化所有小时为0
        for (int i = 0; i < 24; i++) {
            hourlyCount.put(i, 0);
        }

        String sql = "SELECT HOUR(checkin_time) AS hour, COUNT(*) AS count " +
                "FROM check_in WHERE DATE(checkin_time) = ? " +
                "GROUP BY HOUR(checkin_time)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, DateUtils.toSqlDate(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("hour");
                    int count = rs.getInt("count");
                    hourlyCount.put(hour, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hourlyCount;
    }

    /**
     * 获取指定日期的签到人数
     * 
     * @param date 日期
     * @return 签到人数
     */
    public int getCheckInCountByDate(Date date) {
        String sql = "SELECT COUNT(*) AS count FROM check_in WHERE DATE(checkin_time) = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, DateUtils.toSqlDate(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ==================== 时长计算 ====================

    /**
     * 计算单次健身时长
     * 
     * @param checkinId 签到记录ID
     * @return 格式化后的时长字符串（如"2小时30分钟"），未签退返回"进行中"
     */
    public String getStayDuration(int checkinId) {
        CheckIn checkIn = getCheckInById(checkinId);
        if (checkIn == null) {
            return "记录不存在";
        }
        return DateUtils.calculateCheckinDuration(checkIn.getCheckinTime(), checkIn.getCheckoutTime());
    }

    /**
     * 计算会员今日健身时长
     * 
     * @param memberId 会员ID
     * @return 格式化后的时长字符串
     */
    public String getTodayStayDuration(int memberId) {
        long totalMinutes = 0;
        String sql = "SELECT checkin_time, checkout_time FROM check_in " +
                "WHERE member_id = ? AND DATE(checkin_time) = CURDATE()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Date checkinTime = rs.getTimestamp("checkin_time");
                    Date checkoutTime = rs.getTimestamp("checkout_time");
                    // 如果未签退，用当前时间计算
                    if (checkoutTime == null) {
                        checkoutTime = DateUtils.now();
                    }
                    totalMinutes += DateUtils.minutesBetween(checkinTime, checkoutTime);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DateUtils.formatDuration(totalMinutes);
    }

    /**
     * 计算会员本月总健身时长（分钟）
     * 
     * @param memberId 会员ID
     * @return 总时长（分钟）
     */
    public long getMonthlyTotalMinutes(int memberId) {
        String sql = "SELECT SUM(TIMESTAMPDIFF(MINUTE, checkin_time, IFNULL(checkout_time, NOW()))) AS total_minutes " +
                "FROM check_in WHERE member_id = ? " +
                "AND YEAR(checkin_time) = YEAR(CURDATE()) AND MONTH(checkin_time) = MONTH(CURDATE())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total_minutes");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 计算会员本月总健身时长（格式化字符串）
     * 
     * @param memberId 会员ID
     * @return 格式化后的时长字符串
     */
    public String getMonthlyTotalDuration(int memberId) {
        return DateUtils.formatDuration(getMonthlyTotalMinutes(memberId));
    }

    // ==================== 自动签退功能 ====================

    /**
     * 自动签退超时的签到记录
     * 对于超过指定小时数未签退的记录，自动设置签退时间
     * 
     * @param maxHours 最大允许的签到时长（小时）
     * @return 自动签退的记录数
     */
    public int autoCheckOutOvertime(int maxHours) {
        String sql = "UPDATE check_in SET checkout_time = DATE_ADD(checkin_time, INTERVAL ? HOUR) " +
                "WHERE checkout_time IS NULL AND checkin_time < DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, maxHours);
            pstmt.setInt(2, maxHours);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取超时未签退的签到记录
     * 
     * @param maxHours 最大允许的签到时长（小时）
     * @return 超时的签到记录列表
     */
    public List<CheckIn> getOvertimeCheckIns(int maxHours) {
        List<CheckIn> checkIns = new ArrayList<>();
        String sql = "SELECT * FROM check_in WHERE checkout_time IS NULL " +
                "AND checkin_time < DATE_SUB(NOW(), INTERVAL ? HOUR) ORDER BY checkin_time";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, maxHours);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    checkIns.add(extractCheckInFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkIns;
    }
}

