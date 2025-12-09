package dao;

import entity.Member;
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
 * 会员数据访问对象
 * 对应数据库 member 表
 * 
 * 会员状态：active（活跃）、frozen（冻结）、inactive（停用）
 * 性别：male（男）、female（女）
 */
public class MemberDAO {

    // ==================== 状态常量 ====================

    /** 会员状态：活跃 */
    public static final String STATUS_ACTIVE = "active";
    /** 会员状态：冻结 */
    public static final String STATUS_FROZEN = "frozen";
    /** 会员状态：停用 */
    public static final String STATUS_INACTIVE = "inactive";

    /** 所有有效状态 */
    public static final String[] VALID_STATUSES = {STATUS_ACTIVE, STATUS_FROZEN, STATUS_INACTIVE};

    // ==================== 性别常量 ====================

    /** 性别：男 */
    public static final String GENDER_MALE = "male";
    /** 性别：女 */
    public static final String GENDER_FEMALE = "female";

    /** 所有有效性别 */
    public static final String[] VALID_GENDERS = {GENDER_MALE, GENDER_FEMALE};

    // ==================== 构造方法 ====================

    public MemberDAO() {
    }

    // ==================== 结果集映射 ====================

    /**
     * 从结果集中提取会员信息
     * 
     * @param rs 结果集
     * @return Member对象
     * @throws SQLException SQL异常
     */
    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("member_id"));
        member.setName(rs.getString("name"));
        member.setPhone(rs.getString("phone"));
        member.setEmail(rs.getString("email"));
        member.setGender(rs.getString("gender"));
        member.setBirthDate(rs.getDate("birth_date"));
        member.setRegisterDate(rs.getTimestamp("register_date"));
        member.setStatus(rs.getString("status"));
        return member;
    }

    // ==================== 基础查询 ====================

    /**
     * 根据ID查询会员
     * 
     * @param memberId 会员ID
     * @return Member对象，不存在返回null
     */
    public Member getMemberById(int memberId) {
        String sql = "SELECT * FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有会员
     * 
     * @return 会员列表
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 根据名字查询会员（精确匹配）
     * 
     * @param name 会员名字
     * @return 会员列表
     */
    public List<Member> getMembersByName(String name) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE name = ? ORDER BY member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 根据名字模糊搜索会员
     * 
     * @param name 名字关键字
     * @return 会员列表
     */
    public List<Member> searchMembersByName(String name) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE name LIKE ? ORDER BY member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 根据手机号查询会员（精确匹配，用于登录验证）
     * 
     * @param phone 手机号
     * @return Member对象，不存在返回null
     */
    public Member getMemberByPhone(String phone) {
        String sql = "SELECT * FROM member WHERE phone = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据手机号模糊搜索会员
     * 
     * @param phone 手机号关键字
     * @return 会员列表
     */
    public List<Member> searchByPhone(String phone) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE phone LIKE ? ORDER BY member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + phone + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 根据性别查询会员
     * 
     * @param gender 性别（male/female）
     * @return 会员列表
     */
    public List<Member> getMembersByGender(String gender) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE gender = ? ORDER BY member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gender);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 根据状态查询会员
     * 
     * @param status 状态（active/frozen/inactive）
     * @return 会员列表
     */
    public List<Member> getMembersByStatus(String status) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE status = ? ORDER BY member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 根据出生日期查询会员
     * 
     * @param birthDate 出生日期
     * @return 会员列表
     */
    public List<Member> getMembersByBirthDate(Date birthDate) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE birth_date = ? ORDER BY member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, DateUtils.toSqlDate(birthDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 获取所有活跃会员
     * 
     * @return 活跃会员列表
     */
    public List<Member> getActiveMembers() {
        return getMembersByStatus(STATUS_ACTIVE);
    }

    /**
     * 根据年龄范围查询会员
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 会员列表
     */
    public List<Member> getMembersByAgeRange(int minAge, int maxAge) {
        List<Member> members = new ArrayList<>();
        // 计算出生日期范围
        Date maxBirthDate = DateUtils.addYears(DateUtils.now(), -minAge);
        Date minBirthDate = DateUtils.addYears(DateUtils.now(), -maxAge - 1);

        String sql = "SELECT * FROM member WHERE birth_date BETWEEN ? AND ? ORDER BY birth_date";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, DateUtils.toSqlDate(minBirthDate));
            pstmt.setDate(2, DateUtils.toSqlDate(maxBirthDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * 获取拥有有效会员卡的会员
     * 
     * @return 会员列表
     */
    public List<Member> getMembersWithValidCard() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT DISTINCT m.* FROM member m " +
                "JOIN membership_card mc ON m.member_id = mc.member_id " +
                "WHERE mc.card_status = 'active' AND mc.end_date >= CURDATE() " +
                "ORDER BY m.member_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // ==================== 添加会员 ====================

    /**
     * 添加新会员
     * 
     * 业务规则：
     * 1. 邮箱格式必须有效
     * 2. 手机号格式必须有效（中国大陆手机号）
     * 3. 状态必须是有效状态
     * 
     * @param member 会员对象
     * @return 是否添加成功
     */
    public boolean addMember(Member member) {
        // 数据校验
        if (!isValidEmail(member.getEmail())) {
            System.err.println("添加失败：无效的邮箱格式 (email=" + member.getEmail() + ")");
            return false;
        }
        if (!isValidPhone(member.getPhone())) {
            System.err.println("添加失败：无效的手机号 (phone=" + member.getPhone() + ")");
            return false;
        }
        if (!isValidStatus(member.getStatus())) {
            System.err.println("添加失败：无效的状态 (status=" + member.getStatus() + ")");
            return false;
        }
        if (!isValidGender(member.getGender())) {
            System.err.println("添加失败：无效的性别 (gender=" + member.getGender() + ")");
            return false;
        }

        String sql = "INSERT INTO member (name, phone, email, gender, birth_date, register_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getGender());
            pstmt.setDate(5, DateUtils.toSqlDate(member.getBirthDate()));
            // 如果没有设置注册时间，使用当前时间
            pstmt.setTimestamp(6, member.getRegisterDate() != null
                    ? DateUtils.toSqlTimestamp(member.getRegisterDate())
                    : DateUtils.nowTimestamp());
            pstmt.setString(7, member.getStatus());

            int affectedRows = pstmt.executeUpdate();

            // 获取自动生成的ID
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        member.setId(rs.getInt(1));
                    }
                }
            }
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 更新会员 ====================

    /**
     * 更新会员信息
     * 
     * @param member 会员对象
     * @return 是否更新成功
     */
    public boolean updateMember(Member member) {
        // 数据校验
        if (!isValidEmail(member.getEmail())) {
            System.err.println("更新失败：无效的邮箱格式 (email=" + member.getEmail() + ")");
            return false;
        }
        if (!isValidPhone(member.getPhone())) {
            System.err.println("更新失败：无效的手机号 (phone=" + member.getPhone() + ")");
            return false;
        }
        if (!isValidStatus(member.getStatus())) {
            System.err.println("更新失败：无效的状态 (status=" + member.getStatus() + ")");
            return false;
        }

        String sql = "UPDATE member SET name = ?, phone = ?, email = ?, gender = ?, birth_date = ?, register_date = ?, status = ? WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getGender());
            pstmt.setDate(5, DateUtils.toSqlDate(member.getBirthDate()));
            pstmt.setTimestamp(6, DateUtils.toSqlTimestamp(member.getRegisterDate()));
            pstmt.setString(7, member.getStatus());
            pstmt.setInt(8, member.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新会员状态
     * 
     * @param memberId 会员ID
     * @param newStatus 新状态
     * @return 是否更新成功
     */
    public boolean updateMemberStatus(int memberId, String newStatus) {
        if (!isValidStatus(newStatus)) {
            System.err.println("更新失败：无效的状态 (status=" + newStatus + ")");
            return false;
        }

        String sql = "UPDATE member SET status = ? WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, memberId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 冻结会员
     * 
     * @param memberId 会员ID
     * @return 是否成功
     */
    public boolean freezeMember(int memberId) {
        return updateMemberStatus(memberId, STATUS_FROZEN);
    }

    /**
     * 激活会员
     * 
     * @param memberId 会员ID
     * @return 是否成功
     */
    public boolean activateMember(int memberId) {
        return updateMemberStatus(memberId, STATUS_ACTIVE);
    }

    // ==================== 删除会员 ====================

    /**
     * 根据ID删除会员
     * 
     * 注意：如果会员有关联数据（会员卡、预约等），删除可能会失败
     * 
     * @param memberId 会员ID
     * @return 是否删除成功
     */
    public boolean deleteMember(int memberId) {
        String sql = "DELETE FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除会员（使用对象）
     * 
     * @param member 会员对象
     * @return 是否删除成功
     */
    public boolean deleteMember(Member member) {
        return deleteMember(member.getId());
    }

    // ==================== 统计功能 ====================

    /**
     * 获取会员总数
     * 
     * @return 会员总数
     */
    public int getTotalMemberCount() {
        String sql = "SELECT COUNT(*) AS count FROM member";
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
     * 按状态统计会员数量
     * 
     * @return Map<状态, 数量>
     */
    public Map<String, Integer> getMemberCountByStatus() {
        Map<String, Integer> countMap = new HashMap<>();
        // 初始化所有状态为0
        for (String status : VALID_STATUSES) {
            countMap.put(status, 0);
        }

        String sql = "SELECT status, COUNT(*) AS count FROM member GROUP BY status";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                countMap.put(status, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countMap;
    }

    /**
     * 按性别统计会员数量
     * 
     * @return Map<性别, 数量>
     */
    public Map<String, Integer> getMemberCountByGender() {
        Map<String, Integer> countMap = new HashMap<>();
        // 初始化所有性别为0
        for (String gender : VALID_GENDERS) {
            countMap.put(gender, 0);
        }

        String sql = "SELECT gender, COUNT(*) AS count FROM member GROUP BY gender";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String gender = rs.getString("gender");
                int count = rs.getInt("count");
                countMap.put(gender, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countMap;
    }

    /**
     * 获取活跃会员数量
     * 
     * @return 活跃会员数
     */
    public int getActiveMemberCount() {
        String sql = "SELECT COUNT(*) AS count FROM member WHERE status = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, STATUS_ACTIVE);
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
     * 获取今日新注册会员数
     * 
     * @return 今日新注册数
     */
    public int getTodayNewMemberCount() {
        String sql = "SELECT COUNT(*) AS count FROM member WHERE DATE(register_date) = CURDATE()";
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
     * 获取本月新注册会员数
     * 
     * @return 本月新注册数
     */
    public int getMonthlyNewMemberCount() {
        String sql = "SELECT COUNT(*) AS count FROM member WHERE YEAR(register_date) = YEAR(CURDATE()) AND MONTH(register_date) = MONTH(CURDATE())";
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

    // ==================== 年龄相关 ====================

    /**
     * 获取会员年龄
     * 
     * @param memberId 会员ID
     * @return 年龄，会员不存在返回-1
     */
    public int getMemberAge(int memberId) {
        Member member = getMemberById(memberId);
        if (member == null || member.getBirthDate() == null) {
            return -1;
        }
        return DateUtils.calculateAge(member.getBirthDate());
    }

    /**
     * 获取会员的会籍时长（从注册到现在的天数）
     * 
     * @param memberId 会员ID
     * @return 会籍天数，会员不存在返回-1
     */
    public long getMembershipDays(int memberId) {
        Member member = getMemberById(memberId);
        if (member == null || member.getRegisterDate() == null) {
            return -1;
        }
        return DateUtils.daysBetween(member.getRegisterDate(), DateUtils.now());
    }

    // ==================== 校验方法 ====================

    /**
     * 校验邮箱格式
     * 
     * @param email 邮箱
     * @return true表示有效
     */
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * 校验手机号格式（中国大陆手机号）
     * 
     * @param phone 手机号
     * @return true表示有效
     */
    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 校验状态是否有效
     * 
     * @param status 状态
     * @return true表示有效
     */
    public boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equals(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验性别是否有效
     * 
     * @param gender 性别
     * @return true表示有效
     */
    public boolean isValidGender(String gender) {
        if (gender == null) {
            return false;
        }
        for (String validGender : VALID_GENDERS) {
            if (validGender.equals(gender)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查手机号是否已存在
     * 
     * @param phone 手机号
     * @return true表示已存在
     */
    public boolean isPhoneExists(String phone) {
        return getMemberByPhone(phone) != null;
    }

    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @return true表示已存在
     */
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) AS count FROM member WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
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

    // ==================== 工具方法 ====================

    /**
     * 获取状态的中文名称
     * 
     * @param status 状态
     * @return 中文名称
     */
    public String getStatusDisplayName(String status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case STATUS_ACTIVE:
                return "活跃";
            case STATUS_FROZEN:
                return "冻结";
            case STATUS_INACTIVE:
                return "停用";
            default:
                return "未知";
        }
    }

    /**
     * 获取性别的中文名称
     * 
     * @param gender 性别
     * @return 中文名称
     */
    public String getGenderDisplayName(String gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case GENDER_MALE:
                return "男";
            case GENDER_FEMALE:
                return "女";
            default:
                return "未知";
        }
    }

    // ==================== 兼容旧方法名（已废弃） ====================

    /**
     * @deprecated 请使用 {@link #getMembersByName(String)}
     */
    @Deprecated
    public List<Member> getMemberByName(String name) {
        return getMembersByName(name);
    }

    /**
     * @deprecated 请使用 {@link #getMembersByGender(String)}
     */
    @Deprecated
    public List<Member> getMemberByGender(String gender) {
        return getMembersByGender(gender);
    }

    /**
     * @deprecated 请使用 {@link #getMembersByBirthDate(Date)}
     */
    @Deprecated
    public List<Member> getMemberByBirthDate(Date birthDate) {
        return getMembersByBirthDate(birthDate);
    }

    /**
     * @deprecated 请使用 {@link #getMembersByStatus(String)}
     */
    @Deprecated
    public List<Member> getMemberByStatus(String status) {
        return getMembersByStatus(status);
    }
}
