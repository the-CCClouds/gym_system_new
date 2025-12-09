package dao;

import entity.MembershipCard;
import entity.MembershipType;
import utils.DBUtil;
import utils.DateUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 会员卡数据访问对象
 * 对应数据库 membership_card 表
 * 
 * 会员卡状态：active（有效）、inactive（停用）、expired（过期）
 */
public class MembershipCardDAO {

    // ==================== 状态常量 ====================

    /** 会员卡状态：有效 */
    public static final String STATUS_ACTIVE = "active";
    /** 会员卡状态：停用 */
    public static final String STATUS_INACTIVE = "inactive";
    /** 会员卡状态：过期 */
    public static final String STATUS_EXPIRED = "expired";

    /** 所有有效状态 */
    public static final String[] VALID_STATUSES = {STATUS_ACTIVE, STATUS_INACTIVE, STATUS_EXPIRED};

    // ==================== 类型常量 ====================

    /** 类型ID：月卡 */
    public static final int TYPE_MONTHLY = 1;
    /** 类型ID：年卡 */
    public static final int TYPE_YEARLY = 2;

    // ==================== 依赖 ====================

    private MembershipTypeDAO typeDAO;
    private MemberDAO memberDAO; // 添加 MemberDAO 依赖

    // ==================== 构造方法 ====================

    public MembershipCardDAO() {
        this.typeDAO = new MembershipTypeDAO();
        this.memberDAO = new MemberDAO(); // 初始化 MemberDAO
    }

    // ==================== 结果集映射 ====================

    /**
     * 从结果集映射为实体类（不含类型对象）
     * 
     * @param rs 结果集
     * @return MembershipCard对象
     * @throws SQLException SQL异常
     */
    private MembershipCard mapResultSetToEntity(ResultSet rs) throws SQLException {
        MembershipCard mc = new MembershipCard();
        mc.setCardId(rs.getInt("card_id"));
        mc.setMemberId(rs.getInt("member_id"));
        mc.setTypeId(rs.getInt("type_id"));
        mc.setStartDate(rs.getDate("start_date"));
        mc.setEndDate(rs.getDate("end_date"));
        mc.setCardStatus(rs.getString("card_status"));
        return mc;
    }

    /**
     * 从结果集映射为实体类（包含类型对象）
     * 
     * @param rs 结果集
     * @return MembershipCard对象（含MembershipType）
     * @throws SQLException SQL异常
     */
    private MembershipCard mapResultSetToEntityWithType(ResultSet rs) throws SQLException {
        MembershipCard mc = mapResultSetToEntity(rs);
        // 加载关联的类型对象
        MembershipType type = typeDAO.getTypeById(mc.getTypeId());
        mc.setMembershipType(type);
        return mc;
    }

    // ==================== 基础查询 ====================

    /**
     * 根据ID查询会员卡
     * 
     * @param cardId 会员卡ID
     * @return MembershipCard对象，不存在返回null
     */
    public MembershipCard getById(int cardId) {
        String sql = "SELECT * FROM membership_card WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntityWithType(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有会员卡
     * 
     * @return 会员卡列表
     */
    public List<MembershipCard> getAll() {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card ORDER BY card_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                cards.add(mapResultSetToEntityWithType(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * 根据会员ID查询会员卡
     * 
     * @param memberId 会员ID
     * @return 会员卡列表
     */
    public List<MembershipCard> getByMemberId(int memberId) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE member_id = ? ORDER BY card_id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToEntityWithType(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * 根据状态查询会员卡
     * 
     * @param status 状态（active/inactive/expired）
     * @return 会员卡列表
     */
    public List<MembershipCard> getByStatus(String status) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE card_status = ? ORDER BY card_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToEntityWithType(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * 根据类型ID查询会员卡
     * 
     * @param typeId 类型ID（1=月卡，2=年卡）
     * @return 会员卡列表
     */
    public List<MembershipCard> getByTypeId(int typeId) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE type_id = ? ORDER BY card_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, typeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToEntityWithType(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * 获取所有月卡
     * 
     * @return 月卡列表
     */
    public List<MembershipCard> getMonthlyCards() {
        return getByTypeId(TYPE_MONTHLY);
    }

    /**
     * 获取所有年卡
     * 
     * @return 年卡列表
     */
    public List<MembershipCard> getYearlyCards() {
        return getByTypeId(TYPE_YEARLY);
    }

    /**
     * 获取所有有效的会员卡
     * 
     * @return 有效会员卡列表
     */
    public List<MembershipCard> getActiveCards() {
        return getByStatus(STATUS_ACTIVE);
    }

    /**
     * 获取会员当前有效的会员卡
     * 
     * @param memberId 会员ID
     * @return 有效的会员卡，没有返回null
     */
    public MembershipCard getActiveMembershipCard(int memberId) {
        String sql = "SELECT * FROM membership_card WHERE member_id = ? AND card_status = ? AND end_date >= CURDATE() ORDER BY end_date DESC LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setString(2, STATUS_ACTIVE);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntityWithType(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取即将过期的会员卡（指定天数内）
     * 
     * @param days 天数
     * @return 即将过期的会员卡列表
     */
    public List<MembershipCard> getExpiringCards(int days) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE card_status = ? AND end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY end_date";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, STATUS_ACTIVE);
            pstmt.setInt(2, days);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToEntityWithType(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    // ==================== 添加会员卡 ====================

    /**
     * 添加会员卡
     * 
     * 业务规则：
     * 1. 会员必须存在
     * 2. 类型ID必须有效
     * 3. 状态必须有效
     * 4. 开始日期必须早于结束日期
     * 
     * @param card 会员卡对象
     * @return 是否添加成功
     */
    public boolean addMembershipCard(MembershipCard card) {
        // 数据校验
        if (memberDAO.getMemberById(card.getMemberId())==null) { // 使用 MemberDAO 验证会员是否存在
            System.err.println("添加失败：会员不存在 (memberId=" + card.getMemberId() + ")");
            return false;
        }
        if (!isValidTypeId(card.getTypeId())) {
            System.err.println("添加失败：无效的卡类型 (typeId=" + card.getTypeId() + ")");
            return false;
        }
        if (!isValidCardStatus(card.getCardStatus())) {
            System.err.println("添加失败：无效的卡状态 (status=" + card.getCardStatus() + ")");
            return false;
        }
        if (!DateUtils.isValidDateRange(card.getStartDate(), card.getEndDate())) {
            System.err.println("添加失败：日期范围无效 (startDate必须早于endDate)");
            return false;
        }

        String sql = "INSERT INTO membership_card (member_id, type_id, start_date, end_date, card_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, card.getMemberId());
            pstmt.setInt(2, card.getTypeId());
            pstmt.setDate(3, DateUtils.toSqlDate(card.getStartDate()));
            pstmt.setDate(4, DateUtils.toSqlDate(card.getEndDate()));
            pstmt.setString(5, card.getCardStatus());

            int affectedRows = pstmt.executeUpdate();

            // 获取自动生成的ID
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        card.setCardId(rs.getInt(1));
                    }
                }
            }
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 为会员创建月卡
     * 
     * @param memberId 会员ID
     * @return 是否创建成功
     */
    public boolean createMonthlyCard(int memberId) {
        MembershipCard card = new MembershipCard();
        card.setMemberId(memberId);
        card.setTypeId(TYPE_MONTHLY);
        card.setStartDate(DateUtils.now());
        card.setEndDate(DateUtils.getMonthlyCardEndDate());
        card.setCardStatus(STATUS_ACTIVE);
        return addMembershipCard(card);
    }

    /**
     * 为会员创建年卡
     * 
     * @param memberId 会员ID
     * @return 是否创建成功
     */
    public boolean createYearlyCard(int memberId) {
        MembershipCard card = new MembershipCard();
        card.setMemberId(memberId);
        card.setTypeId(TYPE_YEARLY);
        card.setStartDate(DateUtils.now());
        card.setEndDate(DateUtils.getYearlyCardEndDate());
        card.setCardStatus(STATUS_ACTIVE);
        return addMembershipCard(card);
    }

    // ==================== 更新会员卡 ====================

    /**
     * 更新会员卡信息
     * 
     * @param card 会员卡对象
     * @return 是否更新成功
     */
    public boolean updateMembershipCard(MembershipCard card) {
        String sql = "UPDATE membership_card SET member_id = ?, type_id = ?, start_date = ?, end_date = ?, card_status = ? WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, card.getMemberId());
            pstmt.setInt(2, card.getTypeId());
            pstmt.setDate(3, DateUtils.toSqlDate(card.getStartDate()));
            pstmt.setDate(4, DateUtils.toSqlDate(card.getEndDate()));
            pstmt.setString(5, card.getCardStatus());
            pstmt.setInt(6, card.getCardId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新会员卡状态
     * 
     * @param cardId 会员卡ID
     * @param newStatus 新状态
     * @return 是否更新成功
     */
    public boolean updateCardStatus(int cardId, String newStatus) {
        if (!isValidCardStatus(newStatus)) {
            System.err.println("更新失败：无效的卡状态 (status=" + newStatus + ")");
            return false;
        }

        String sql = "UPDATE membership_card SET card_status = ? WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, cardId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量更新过期会员卡状态
     * 将已过期但状态仍为active的卡更新为expired
     * 
     * @return 更新的记录数
     */
    public int updateExpiredCards() {
        String sql = "UPDATE membership_card SET card_status = ? WHERE end_date < CURDATE() AND card_status = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, STATUS_EXPIRED);
            pstmt.setString(2, STATUS_ACTIVE);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 续费会员卡
     * 
     * @param cardId 会员卡ID
     * @param extraDays 续费天数
     * @return 是否续费成功
     */
    public boolean renewCard(int cardId, int extraDays) {
        if (extraDays <= 0) {
            System.err.println("续费失败：续费天数必须大于0");
            return false;
        }

        MembershipCard card = getById(cardId);
        if (card == null) {
            System.err.println("续费失败：会员卡不存在 (cardId=" + cardId + ")");
            return false;
        }

        // 计算新的结束日期
        Date baseDate = DateUtils.isExpired(card.getEndDate()) ? DateUtils.now() : card.getEndDate();
        Date newEndDate = DateUtils.addDays(baseDate, extraDays);

        String sql = "UPDATE membership_card SET end_date = ?, card_status = ? WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, DateUtils.toSqlDate(newEndDate));
            pstmt.setString(2, STATUS_ACTIVE);
            pstmt.setInt(3, cardId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 续费月卡（延长30天）
     * 
     * @param cardId 会员卡ID
     * @return 是否续费成功
     */
    public boolean renewMonthlyCard(int cardId) {
        return renewCard(cardId, 30);
    }

    /**
     * 续费年卡（延长365天）
     * 
     * @param cardId 会员卡ID
     * @return 是否续费成功
     */
    public boolean renewYearlyCard(int cardId) {
        return renewCard(cardId, 365);
    }

    // ==================== 删除会员卡 ====================

    /**
     * 删除会员卡
     * 
     * @param cardId 会员卡ID
     * @return 是否删除成功
     */
    public boolean deleteMembershipCard(int cardId) {
        String sql = "DELETE FROM membership_card WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cardId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 时间相关查询 ====================

    /**
     * 计算会员卡剩余天数
     * 
     * @param cardId 会员卡ID
     * @return 剩余天数（负数表示已过期），-999表示卡不存在
     */
    public int getRemainingDays(int cardId) {
        MembershipCard card = getById(cardId);
        if (card == null || card.getEndDate() == null) {
            return -999;
        }
        return (int) DateUtils.daysRemaining(card.getEndDate());
    }

    /**
     * 检查会员卡是否有效
     * 
     * @param cardId 会员卡ID
     * @return true表示有效
     */
    public boolean isCardValid(int cardId) {
        MembershipCard card = getById(cardId);
        if (card == null) {
            return false;
        }
        return STATUS_ACTIVE.equals(card.getCardStatus()) && DateUtils.isNotExpired(card.getEndDate());
    }

    /**
     * 检查会员是否有有效的会员卡
     * 
     * @param memberId 会员ID
     * @return true表示有有效卡
     */
    public boolean hasMemberValidCard(int memberId) {
        return getActiveMembershipCard(memberId) != null;
    }

    // ==================== 统计功能 ====================

    /**
     * 获取会员卡总数
     * 
     * @return 总数
     */
    public int getTotalCardCount() {
        String sql = "SELECT COUNT(*) AS count FROM membership_card";
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
     * 按状态统计会员卡数量
     * 
     * @return Map<状态, 数量>
     */
    public Map<String, Integer> getCardCountByStatus() {
        Map<String, Integer> countMap = new HashMap<>();
        for (String status : VALID_STATUSES) {
            countMap.put(status, 0);
        }

        String sql = "SELECT card_status, COUNT(*) AS count FROM membership_card GROUP BY card_status";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("card_status");
                int count = rs.getInt("count");
                countMap.put(status, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countMap;
    }

    /**
     * 按类型统计会员卡数量
     * 
     * @return Map<类型ID, 数量>
     */
    public Map<Integer, Integer> getCardCountByType() {
        Map<Integer, Integer> countMap = new HashMap<>();
        countMap.put(TYPE_MONTHLY, 0);
        countMap.put(TYPE_YEARLY, 0);

        String sql = "SELECT type_id, COUNT(*) AS count FROM membership_card GROUP BY type_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int typeId = rs.getInt("type_id");
                int count = rs.getInt("count");
                countMap.put(typeId, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countMap;
    }

    /**
     * 获取有效会员卡数量
     * 
     * @return 有效卡数量
     */
    public int getActiveCardCount() {
        String sql = "SELECT COUNT(*) AS count FROM membership_card WHERE card_status = ? AND end_date >= CURDATE()";
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
     * 获取已过期会员卡数量
     * 
     * @return 过期卡数量
     */
    public int getExpiredCardCount() {
        String sql = "SELECT COUNT(*) AS count FROM membership_card WHERE card_status = ? OR end_date < CURDATE()";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, STATUS_EXPIRED);
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

    // ==================== 校验方法 ====================

    /**
     * 校验类型ID是否有效
     * 
     * @param typeId 类型ID
     * @return true表示有效
     */
    public boolean isValidTypeId(int typeId) {
        // 从数据库验证
        return typeDAO.getTypeById(typeId) != null;
    }

    /**
     * 校验卡状态是否有效
     * 
     * @param status 状态
     * @return true表示有效
     */
    public boolean isValidCardStatus(String status) {
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
                return "有效";
            case STATUS_INACTIVE:
                return "停用";
            case STATUS_EXPIRED:
                return "过期";
            default:
                return "未知";
        }
    }

    /**
     * 获取类型的中文名称
     * 
     * @param typeId 类型ID
     * @return 中文名称
     */
    public String getTypeDisplayName(int typeId) {
        switch (typeId) {
            case TYPE_MONTHLY:
                return "月卡";
            case TYPE_YEARLY:
                return "年卡";
            default:
                return "未知";
        }
    }
}
