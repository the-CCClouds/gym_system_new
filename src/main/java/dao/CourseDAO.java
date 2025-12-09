package dao;

import entity.Course;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程数据访问对象
 * 对应数据库 course 表
 *
 * 课程类型：yoga, spinning, pilates, aerobics, strength, other
 */
public class CourseDAO {

    // ==================== 课程类型常量 ====================

    /** 课程类型：瑜伽 */
    public static final String TYPE_YOGA = "yoga";
    /** 课程类型：动感单车 */
    public static final String TYPE_SPINNING = "spinning";
    /** 课程类型：普拉提 */
    public static final String TYPE_PILATES = "pilates";
    /** 课程类型：有氧操 */
    public static final String TYPE_AEROBICS = "aerobics";
    /** 课程类型：力量训练 */
    public static final String TYPE_STRENGTH = "strength";
    /** 课程类型：其他 */
    public static final String TYPE_OTHER = "other";

    /** 所有有效的课程类型 */
    public static final String[] VALID_TYPES = {
            TYPE_YOGA, TYPE_SPINNING, TYPE_PILATES, TYPE_AEROBICS, TYPE_STRENGTH, TYPE_OTHER
    };

    // ==================== 结果集映射 ====================

    /**
     * 从结果集中提取课程信息
     *
     * @param rs 结果集
     * @return Course对象
     * @throws SQLException SQL异常
     */
    private Course extractCourseFromResultSet(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setName(rs.getString("name"));
        course.setType(rs.getString("type"));
        course.setDuration(rs.getInt("duration"));
        course.setMaxCapacity(rs.getInt("max_capacity"));
        course.setEmployeeId(rs.getInt("employee_id"));
        course.setCourseTime(rs.getTimestamp("course_time"));
        return course;
    }

    // ==================== 基础查询 ====================

    /**
     * 根据ID查询课程
     *
     * @param courseId 课程ID
     * @return Course对象，不存在返回null
     */
    public Course getCourseById(int courseId) {
        String sql = "SELECT * FROM course WHERE course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCourseFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有课程
     *
     * @return 课程列表
     */
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course ORDER BY course_time DESC, course_id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                courses.add(extractCourseFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * 根据名称模糊搜索课程
     *
     * @param name 课程名称（支持模糊匹配）
     * @return 课程列表
     */
    public List<Course> searchCourseByName(String name) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE name LIKE ? ORDER BY course_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(extractCourseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * 根据类型查询课程
     *
     * @param type 课程类型（yoga/spinning/pilates/aerobics/strength/other）
     * @return 课程列表
     */
    public List<Course> getCoursesByType(String type) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE type = ? ORDER BY course_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(extractCourseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * 根据教练ID查询课程
     *
     * @param employeeId 教练ID（employee_id）
     * @return 课程列表
     */
    public List<Course> getCoursesByEmployeeId(int employeeId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE employee_id = ? ORDER BY course_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(extractCourseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * 根据时长范围查询课程
     *
     * @param minDuration 最小时长（分钟）
     * @param maxDuration 最大时长（分钟）
     * @return 课程列表
     */
    public List<Course> getCoursesByDurationRange(int minDuration, int maxDuration) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE duration BETWEEN ? AND ? ORDER BY duration";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, minDuration);
            pstmt.setInt(2, maxDuration);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(extractCourseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    // ==================== 添加课程 ====================

    /**
     * 添加新课程
     *
     * 业务规则：
     * 1. 课程类型必须是有效类型
     * 2. 时长必须大于0
     * 3. 最大容量必须大于0
     *
     * @param course 课程对象
     * @return 是否添加成功
     */
    public boolean addCourse(Course course) {
        // 第1步：验证课程类型
        if (!isValidType(course.getType())) {
            System.err.println("添加失败：无效的课程类型 (type=" + course.getType() + ")");
            return false;
        }

        // 第2步：验证参数
        if (course.getDuration() <= 0) {
            System.err.println("添加失败：课程时长必须大于0");
            return false;
        }
        if (course.getMaxCapacity() <= 0) {
            System.err.println("添加失败：最大容量必须大于0");
            return false;
        }

        String sql = "INSERT INTO course (name, type, duration, max_capacity, employee_id, course_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getType());
            pstmt.setInt(3, course.getDuration());
            pstmt.setInt(4, course.getMaxCapacity());
            pstmt.setInt(5, course.getEmployeeId());
            // 【新增这一行】设置第 6 个参数：将 Date 转为 SQL Timestamp
            pstmt.setTimestamp(6, utils.DateUtils.toSqlTimestamp(course.getCourseTime()));
            int affectedRows = pstmt.executeUpdate();

            // 获取自动生成的ID
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        course.setCourseId(rs.getInt(1));
                    }
                }
            }
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 更新课程 ====================

    /**
     * 更新课程信息
     *
     * @param course 课程对象
     * @return 是否更新成功
     */
    public boolean updateCourse(Course course) {
        // 验证课程类型
        if (!isValidType(course.getType())) {
            System.err.println("更新失败：无效的课程类型 (type=" + course.getType() + ")");
            return false;
        }

        // 验证参数
        if (course.getDuration() <= 0 || course.getMaxCapacity() <= 0) {
            System.err.println("更新失败：无效的课程参数");
            return false;
        }

        String sql = "UPDATE course SET name = ?, type = ?, duration = ?, max_capacity = ?, employee_id = ?, course_time = ? WHERE course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getType());
            pstmt.setInt(3, course.getDuration());
            pstmt.setInt(4, course.getMaxCapacity());
            pstmt.setInt(5, course.getEmployeeId());
            // 【新增这一行】设置第 6 个参数：时间
            pstmt.setTimestamp(6, utils.DateUtils.toSqlTimestamp(course.getCourseTime()));

            // 【注意】ID 变成了第 7 个参数
            pstmt.setInt(7, course.getCourseId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 删除课程 ====================

    /**
     * 删除课程
     *
     * 注意：如果课程有关联的预约记录，删除可能会失败
     *
     * @param courseId 课程ID
     * @return 是否删除成功
     */
    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM course WHERE course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 预约相关功能 ====================

    /**
     * 获取课程已确认的预约数
     *
     * @param courseId 课程ID
     * @return 已确认预约数
     */
    public int getConfirmedBookingCount(int courseId) {
        String sql = "SELECT COUNT(*) AS count FROM booking WHERE course_id = ? AND booking_status IN ('confirmed', 'pending')";
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
        Course course = getCourseById(courseId);
        if (course == null) {
            return -1;
        }
        int confirmedCount = getConfirmedBookingCount(courseId);
        return course.getMaxCapacity() - confirmedCount;
    }

    /**
     * 检查课程是否已满
     *
     * @param courseId 课程ID
     * @return true表示已满
     */
    public boolean isFull(int courseId) {
        int availableSlots = getAvailableSlots(courseId);
        return availableSlots <= 0;
    }

    /**
     * 获取有空位的课程
     *
     * @return 有空位的课程列表
     */
    public List<Course> getAvailableCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM course c " +
                "WHERE c.max_capacity > (SELECT COUNT(*) FROM booking b WHERE b.course_id = c.course_id AND b.booking_status IN ('confirmed', 'pending')) " +
                "ORDER BY c.course_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                courses.add(extractCourseFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    // ==================== 统计功能 ====================

    /**
     * 获取总课程数
     *
     * @return 课程总数
     */
    public int getTotalCourseCount() {
        String sql = "SELECT COUNT(*) AS count FROM course";
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
     * 按类型统计课程数量
     *
     * @return Map<课程类型, 数量>
     */
    public Map<String, Integer> getCourseCountByType() {
        Map<String, Integer> countMap = new HashMap<>();
        // 初始化所有类型为0
        for (String type : VALID_TYPES) {
            countMap.put(type, 0);
        }

        String sql = "SELECT type, COUNT(*) AS count FROM course GROUP BY type";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString("type");
                int count = rs.getInt("count");
                countMap.put(type, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countMap;
    }

    /**
     * 按教练统计课程数量
     *
     * @return Map<教练ID, 课程数量>
     */
    public Map<Integer, Integer> getCourseCountByEmployee() {
        Map<Integer, Integer> countMap = new HashMap<>();
        String sql = "SELECT employee_id, COUNT(*) AS count FROM course GROUP BY employee_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int employeeId = rs.getInt("employee_id");
                int count = rs.getInt("count");
                countMap.put(employeeId, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countMap;
    }

    /**
     * 获取指定类型的课程数量
     *
     * @param type 课程类型
     * @return 课程数量
     */
    public int getCourseCountByType(String type) {
        String sql = "SELECT COUNT(*) AS count FROM course WHERE type = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
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

    // ==================== 工具方法 ====================

    /**
     * 获取所有有效的课程类型
     *
     * @return 课程类型列表
     */
    public List<String> getAllCourseTypes() {
        List<String> types = new ArrayList<>();
        for (String type : VALID_TYPES) {
            types.add(type);
        }
        return types;
    }

    /**
     * 检查课程类型是否有效
     *
     * @param type 课程类型
     * @return true表示有效
     */
    public boolean isValidType(String type) {
        if (type == null) {
            return false;
        }
        for (String validType : VALID_TYPES) {
            if (validType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取课程类型的中文名称
     *
     * @param type 课程类型
     * @return 中文名称
     */
    public String getTypeDisplayName(String type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case TYPE_YOGA:
                return "瑜伽";
            case TYPE_SPINNING:
                return "动感单车";
            case TYPE_PILATES:
                return "普拉提";
            case TYPE_AEROBICS:
                return "有氧操";
            case TYPE_STRENGTH:
                return "力量训练";
            case TYPE_OTHER:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 格式化课程时长显示
     *
     * @param duration 时长（分钟）
     * @return 格式化字符串（如"1小时30分钟"）
     */
    public String formatDuration(int duration) {
        if (duration <= 0) {
            return "0分钟";
        }
        int hours = duration / 60;
        int minutes = duration % 60;
        if (hours > 0 && minutes > 0) {
            return hours + "小时" + minutes + "分钟";
        } else if (hours > 0) {
            return hours + "小时";
        } else {
            return minutes + "分钟";
        }
    }

    // ==================== 兼容旧方法名（已废弃） ====================

    /**
     * @deprecated 请使用 {@link #getAllCourses()}
     */
    @Deprecated
    public List<Course> getAllCourse() {
        return getAllCourses();
    }

    /**
     * @deprecated 请使用 {@link #getCoursesByType(String)}
     */
    @Deprecated
    public List<Course> getCourseByType(String type) {
        return getCoursesByType(type);
    }

    /**
     * @deprecated 请使用 {@link #getCoursesByEmployeeId(int)}
     */
    @Deprecated
    public List<Course> getCourseByEmployeeId(String employeeId) {
        try {
            return getCoursesByEmployeeId(Integer.parseInt(employeeId));
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }
}
