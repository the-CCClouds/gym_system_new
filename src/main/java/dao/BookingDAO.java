package dao;

import entity.Booking;
import utils.DBUtil;
import utils.DateUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 预约数据访问对象
 * 对应数据库 booking 表
 *
 * 预约状态流转：
 * - pending（待确认）→ confirmed（已确认）
 * - pending（待确认）→ cancelled（已取消）
 * - confirmed（已确认）→ cancelled（已取消）
 */
public class BookingDAO {

    // ==================== 常量定义 ====================

    /** 预约状态：待确认 */
    public static final String STATUS_PENDING = "pending";
    /** 预约状态：已确认 */
    public static final String STATUS_CONFIRMED = "confirmed";
    /** 预约状态：已取消 */
    public static final String STATUS_CANCELLED = "cancelled";
    // 【新增】预约状态：已出席 (用于上课签到)
    public static final String STATUS_ATTENDED = "attended";

    // ==================== 结果集映射 ====================

    /**
     * 从结果集中提取预约信息
     *
     * @param rs 结果集
     * @return Booking对象
     * @throws SQLException SQL异常
     */
    public Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setMemberId(rs.getInt("member_id"));
        booking.setCourseId(rs.getInt("course_id"));
        // 使用 getTimestamp 而不是 getDate，以保留时间部分
        booking.setBookingTime(rs.getTimestamp("booking_time"));
        booking.setBookingStatus(rs.getString("booking_status"));
        return booking;
    }

    // ==================== 基础查询 ====================

    /**
     * 根据ID查询预约
     *
     * @param bookingId 预约ID
     * @return Booking对象，不存在返回null
     */
    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM booking WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookingFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有预约
     *
     * @return 预约列表
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking ORDER BY booking_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * 根据会员ID查询所有预约
     *
     * @param memberId 会员ID
     * @return 预约列表
     */
    public List<Booking> getBookingsByMemberId(int memberId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE member_id = ? ORDER BY booking_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * 根据课程ID查询所有预约
     *
     * @param courseId 课程ID
     * @return 预约列表
     */
    public List<Booking> getBookingsByCourseId(int courseId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE course_id = ? ORDER BY booking_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * 根据状态查询预约
     *
     * @param status 预约状态（pending/confirmed/cancelled）
     * @return 预约列表
     */
    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE booking_status = ? ORDER BY booking_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // ==================== 教练相关查询 ====================

    /**
     * 根据教练ID查询其负责课程的所有预约
     *
     * @param trainerId 教练ID（employee_id）
     * @return 预约列表
     */
    public List<Booking> getBookingsByTrainerId(int trainerId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.* FROM booking b " +
                "JOIN course c ON b.course_id = c.course_id " +
                "WHERE c.employee_id = ? " +
                "ORDER BY b.booking_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * 获取教练今日的预约
     *
     * @param trainerId 教练ID
     * @return 今日预约列表
     */
    public List<Booking> getTodayBookingsByTrainerId(int trainerId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.* FROM booking b " +
                "JOIN course c ON b.course_id = c.course_id " +
                "WHERE c.employee_id = ? " +
                "AND DATE(b.booking_time) = CURDATE() " +
                "AND b.booking_status IN ('pending', 'confirmed') " +
                "ORDER BY b.booking_time";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // ==================== 时间相关查询 ====================

    /**
     * 获取今日所有预约
     *
     * @return 今日预约列表
     */
    public List<Booking> getTodayBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM booking WHERE DATE(booking_time) = CURDATE() ORDER BY booking_time";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * 获取所有待确认的预约
     *
     * @return 待确认预约列表
     */
    public List<Booking> getPendingBookings() {
        return getBookingsByStatus(STATUS_PENDING);
    }

    /**
     * 根据日期范围查询预约历史
     *
     * @param memberId  会员ID
     * @param startDate 开始日期（可为null）
     * @param endDate   结束日期（可为null）
     * @return 预约列表
     */
    public List<Booking> getBookingHistory(int memberId, Date startDate, Date endDate) {
        List<Booking> bookings = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM booking WHERE member_id = ?");

        if (startDate != null && endDate != null) {
            sql.append(" AND booking_time BETWEEN ? AND ?");
        } else if (startDate != null) {
            sql.append(" AND booking_time >= ?");
        } else if (endDate != null) {
            sql.append(" AND booking_time <= ?");
        }
        sql.append(" ORDER BY booking_time DESC");

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
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    /**
     * 根据日期范围查询预约历史（字符串参数重载）
     *
     * @param memberId     会员ID
     * @param startDateStr 开始日期字符串（yyyy-MM-dd）
     * @param endDateStr   结束日期字符串（yyyy-MM-dd）
     * @return 预约列表
     */
    public List<Booking> getBookingHistory(int memberId, String startDateStr, String endDateStr) {
        Date startDate = DateUtils.parseDate(startDateStr);
        Date endDate = DateUtils.parseDate(endDateStr);
        return getBookingHistory(memberId, startDate, endDate);
    }

    // ==================== 添加预约 ====================

    /**
     * 添加新预约
     *
     * 业务规则：
     * 1. 检查会员卡是否有效（状态为active且未过期）
     * 2. 检查是否重复预约（同一会员同一课程只能有一个有效预约）
     * 3. 检查课程容量是否已满
     *
     * @param booking 预约对象
     * @return 是否添加成功
     */
    public boolean addBooking(Booking booking) {
        // 第1步：检查会员卡是否有效
        if (!checkMembershipCardValid(booking.getMemberId())) {
            System.err.println("预约失败：会员卡无效或已过期 (memberId=" + booking.getMemberId() + ")");
            return false;
        }

        // 第2步：检查是否重复预约
        if (checkDuplicateBooking(booking.getMemberId(), booking.getCourseId())) {
            System.err.println("预约失败：已预约过该课程 (memberId=" + booking.getMemberId() + ", courseId=" + booking.getCourseId() + ")");
            return false;
        }

        // 第3步：检查课程容量
        if (!checkCourseCapacity(booking.getCourseId())) {
            System.err.println("预约失败：课程已满 (courseId=" + booking.getCourseId() + ")");
            return false;
        }

        // 第4步：检查预约状态是否有效
        if (!isValidStatus(booking.getBookingStatus())) {
            System.err.println("预约失败：无效的预约状态 (status=" + booking.getBookingStatus() + ")");
            return false;
        }

        String sql = "INSERT INTO booking (member_id, course_id, booking_time, booking_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, booking.getMemberId());
            pstmt.setInt(2, booking.getCourseId());
            // 使用 DateUtils 获取当前时间戳
            pstmt.setTimestamp(3, DateUtils.nowTimestamp());
            pstmt.setString(4, booking.getBookingStatus() != null ? booking.getBookingStatus() : STATUS_PENDING);

            int affectedRows = pstmt.executeUpdate();

            // 获取自动生成的ID
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        booking.setBookingId(rs.getInt(1));
                    }
                }
            }
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 更新预约 ====================

    /**
     * 更新预约信息
     *
     * @param booking 预约对象
     * @return 是否更新成功
     */
    public boolean updateBooking(Booking booking) {
        if (!isValidStatus(booking.getBookingStatus())) {
            System.err.println("更新失败：无效的预约状态 (status=" + booking.getBookingStatus() + ")");
            return false;
        }

        String sql = "UPDATE booking SET member_id = ?, course_id = ?, booking_time = ?, booking_status = ? WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, booking.getMemberId());
            pstmt.setInt(2, booking.getCourseId());
            pstmt.setTimestamp(3, DateUtils.toSqlTimestamp(booking.getBookingTime()));
            pstmt.setString(4, booking.getBookingStatus());
            pstmt.setInt(5, booking.getBookingId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新预约状态
     *
     * @param bookingId 预约ID
     * @param newStatus 新状态（pending/confirmed/cancelled）
     * @return 是否更新成功
     */
    public boolean updateBookingStatus(int bookingId, String newStatus) {
        if (!isValidStatus(newStatus)) {
            System.err.println("更新失败：无效的预约状态 (status=" + newStatus + ")");
            return false;
        }

        String sql = "UPDATE booking SET booking_status = ? WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, bookingId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 确认预约
     *
     * 业务规则：
     * 1. 只有 pending 状态的预约可以确认
     * 2. 确认前需再次检查课程容量
     *
     * @param bookingId 预约ID
     * @return 是否确认成功
     */
    public boolean confirmBooking(int bookingId) {
        // 先查询预约信息
        Booking booking = getBookingById(bookingId);
        if (booking == null) {
            System.err.println("确认失败：预约不存在 (bookingId=" + bookingId + ")");
            return false;
        }

        // 检查状态是否为 pending
        if (!STATUS_PENDING.equals(booking.getBookingStatus())) {
            System.err.println("确认失败：只能确认待处理的预约 (当前状态=" + booking.getBookingStatus() + ")");
            return false;
        }

        // 再次检查课程容量
        if (!checkCourseCapacity(booking.getCourseId())) {
            System.err.println("确认失败：课程已满 (courseId=" + booking.getCourseId() + ")");
            return false;
        }

        // 更新状态为 confirmed
        return updateBookingStatus(bookingId, STATUS_CONFIRMED);
    }

    /**
     * 取消预约
     *
     * 业务规则：已取消的预约不能再次取消
     *
     * @param bookingId 预约ID
     * @return 是否取消成功
     */
    public boolean cancelBooking(int bookingId) {
        String sql = "UPDATE booking SET booking_status = ? WHERE booking_id = ? AND booking_status != ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, STATUS_CANCELLED);
            pstmt.setInt(2, bookingId);
            pstmt.setString(3, STATUS_CANCELLED);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 删除预约 ====================

    /**
     * 删除预约（物理删除，慎用）
     *
     * @param bookingId 预约ID
     * @return 是否删除成功
     */
    public boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM booking WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 业务校验方法 ====================

    /**
     * 检查会员卡是否有效
     *
     * 有效条件：
     * 1. 存在会员卡记录
     * 2. 会员卡状态为 active
     * 3. 会员卡未过期（end_date >= 今天）
     *
     * @param memberId 会员ID
     * @return true表示有效
     */
    public boolean checkMembershipCardValid(int memberId) {
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
     * 检查课程容量是否已满
     *
     * @param courseId 课程ID
     * @return true表示还有空位，false表示已满
     */
    public boolean checkCourseCapacity(int courseId) {
        // 改进：使用子查询避免 GROUP BY 的问题
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM booking WHERE course_id = ? AND booking_status IN ('confirmed', 'pending')) AS booking_count, " +
                "(SELECT max_capacity FROM course WHERE course_id = ?) AS max_capacity";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int bookingCount = rs.getInt("booking_count");
                    int maxCapacity = rs.getInt("max_capacity");
                    // 如果 max_capacity 为 0（课程不存在），返回 false
                    if (maxCapacity == 0) {
                        return false;
                    }
                    return bookingCount < maxCapacity;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查是否重复预约
     *
     * 重复条件：同一会员对同一课程已有 pending 或 confirmed 状态的预约
     *
     * @param memberId 会员ID
     * @param courseId 课程ID
     * @return true表示重复，false表示不重复
     */
    public boolean checkDuplicateBooking(int memberId, int courseId) {
        String sql = "SELECT COUNT(*) AS count FROM booking " +
                "WHERE member_id = ? AND course_id = ? " +
                "AND booking_status IN ('pending', 'confirmed')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, courseId);
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
     * 校验预约状态是否有效
     *
     * @param status 状态字符串
     * @return true表示有效
     */
    private boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status) ||
                STATUS_CONFIRMED.equals(status) ||
                STATUS_CANCELLED.equals(status)||
                STATUS_ATTENDED.equals(status);
    }

    // ==================== 统计功能 ====================

    /**
     * 获取课程已确认的预约数量
     *
     * @param courseId 课程ID
     * @return 已确认预约数
     */
    public int getConfirmedBookingCount(int courseId) {
        String sql = "SELECT COUNT(*) AS count FROM booking WHERE course_id = ? AND booking_status = 'confirmed'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
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
     * 获取课程剩余可预约名额
     *
     * @param courseId 课程ID
     * @return 剩余名额，-1表示课程不存在
     */
    public int getAvailableSlots(int courseId) {
        String sql = "SELECT " +
                "(SELECT max_capacity FROM course WHERE course_id = ?) - " +
                "(SELECT COUNT(*) FROM booking WHERE course_id = ? AND booking_status IN ('confirmed', 'pending')) AS available";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 按状态统计预约数量
     *
     * @param status 预约状态
     * @return 预约数量
     */
    public int getBookingCountByStatus(String status) {
        String sql = "SELECT COUNT(*) AS count FROM booking WHERE booking_status = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
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
     * 获取今日预约总数
     *
     * @return 今日预约数
     */
    public int getTodayBookingCount() {
        String sql = "SELECT COUNT(*) AS count FROM booking WHERE DATE(booking_time) = CURDATE()";
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
     * 获取会员的预约统计
     *
     * @param memberId 会员ID
     * @return 数组 [总预约数, 已确认数, 已取消数]
     */
    public int[] getMemberBookingStats(int memberId) {
        int[] stats = new int[3];
        String sql = "SELECT " +
                "COUNT(*) AS total, " +
                "SUM(CASE WHEN booking_status = 'confirmed' THEN 1 ELSE 0 END) AS confirmed, " +
                "SUM(CASE WHEN booking_status = 'cancelled' THEN 1 ELSE 0 END) AS cancelled " +
                "FROM booking WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats[0] = rs.getInt("total");
                    stats[1] = rs.getInt("confirmed");
                    stats[2] = rs.getInt("cancelled");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // ==================== 兼容旧方法名（已废弃，建议使用新方法名） ====================

    /**
     * @deprecated 请使用 {@link #getBookingsByMemberId(int)}
     */
    @Deprecated
    public List<Booking> getAllBookingsByMemberId(int memberId) {
        return getBookingsByMemberId(memberId);
    }

    /**
     * @deprecated 请使用 {@link #getBookingsByCourseId(int)}
     */
    @Deprecated
    public List<Booking> getAllBookingsByCourseId(int courseId) {
        return getBookingsByCourseId(courseId);
    }

    /**
     * @deprecated 请使用 {@link #getBookingsByStatus(String)}
     */
    @Deprecated
    public List<Booking> getAllBookingsByBookingStatus(String status) {
        return getBookingsByStatus(status);
    }

    /**
     * @deprecated 请使用 {@link #getConfirmedBookingCount(int)}
     */
    @Deprecated
    public int getComfirmedBookingCount(int courseId) {
        return getConfirmedBookingCount(courseId);
    }
}
