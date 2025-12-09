package dao;

import entity.MembershipType;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 会员卡类型数据访问对象
 * 对应数据库 membership_type 表
 * 
 * 预定义类型：
 * - 月卡 (type_id=1, type_name=Monthly, duration_days=30)
 * - 年卡 (type_id=2, type_name=Yearly, duration_days=365)
 */
public class MembershipTypeDAO {

    // ==================== 类型ID常量 ====================

    /** 类型ID：月卡 */
    public static final int TYPE_ID_MONTHLY = 1;
    /** 类型ID：年卡 */
    public static final int TYPE_ID_YEARLY = 2;

    // ==================== 类型名称常量 ====================

    /** 类型名称：月卡 */
    public static final String TYPE_NAME_MONTHLY = "Monthly";
    /** 类型名称：年卡 */
    public static final String TYPE_NAME_YEARLY = "Yearly";

    // ==================== 默认配置常量 ====================

    /** 月卡默认天数 */
    public static final int DEFAULT_MONTHLY_DAYS = 30;
    /** 年卡默认天数 */
    public static final int DEFAULT_YEARLY_DAYS = 365;

    // ==================== 构造方法 ====================

    public MembershipTypeDAO() {
    }

    // ==================== 结果集映射 ====================

    /**
     * 从结果集中提取类型信息
     * 
     * @param rs 结果集
     * @return MembershipType对象
     * @throws SQLException SQL异常
     */
    private MembershipType extractTypeFromResultSet(ResultSet rs) throws SQLException {
        MembershipType type = new MembershipType();
        type.setTypeId(rs.getInt("type_id"));
        type.setTypeName(rs.getString("type_name"));
        type.setDurationDays(rs.getInt("duration_days"));
        type.setPrice(rs.getDouble("price"));
        type.setDescription(rs.getString("description"));
        return type;
    }

    // ==================== 基础查询 ====================

    /**
     * 根据ID查询类型
     * 
     * @param typeId 类型ID
     * @return MembershipType对象，不存在返回null
     */
    public MembershipType getTypeById(int typeId) {
        String sql = "SELECT * FROM membership_type WHERE type_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, typeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractTypeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据名称查询类型
     * 
     * @param typeName 类型名称（Monthly/Yearly）
     * @return MembershipType对象，不存在返回null
     */
    public MembershipType getTypeByName(String typeName) {
        String sql = "SELECT * FROM membership_type WHERE type_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, typeName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractTypeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有类型
     * 
     * @return 类型列表
     */
    public List<MembershipType> getAllTypes() {
        List<MembershipType> types = new ArrayList<>();
        String sql = "SELECT * FROM membership_type ORDER BY type_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                types.add(extractTypeFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }

    /**
     * 获取月卡类型
     * 
     * @return 月卡类型对象
     */
    public MembershipType getMonthlyType() {
        return getTypeById(TYPE_ID_MONTHLY);
    }

    /**
     * 获取年卡类型
     * 
     * @return 年卡类型对象
     */
    public MembershipType getYearlyType() {
        return getTypeById(TYPE_ID_YEARLY);
    }

    // ==================== 添加类型 ====================

    /**
     * 添加新类型（使用AUTO_INCREMENT）
     * 
     * @param type 类型对象
     * @return 是否添加成功
     */
    public boolean addType(MembershipType type) {
        // 数据校验
        if (type.getTypeName() == null || type.getTypeName().trim().isEmpty()) {
            System.err.println("添加失败：类型名称不能为空");
            return false;
        }
        if (type.getDurationDays() <= 0) {
            System.err.println("添加失败：有效期天数必须大于0");
            return false;
        }
        if (type.getPrice() < 0) {
            System.err.println("添加失败：价格不能为负数");
            return false;
        }

        // 检查名称是否已存在
        if (getTypeByName(type.getTypeName()) != null) {
            System.err.println("添加失败：类型名称已存在 (typeName=" + type.getTypeName() + ")");
            return false;
        }

        String sql = "INSERT INTO membership_type (type_name, duration_days, price, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, type.getTypeName());
            pstmt.setInt(2, type.getDurationDays());
            pstmt.setDouble(3, type.getPrice());
            pstmt.setString(4, type.getDescription());

            int affectedRows = pstmt.executeUpdate();

            // 获取自动生成的ID
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        type.setTypeId(rs.getInt(1));
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
     * 添加新类型（指定ID，用于初始化数据）
     * 
     * @param type 类型对象（包含typeId）
     * @return 是否添加成功
     */
    public boolean addTypeWithId(MembershipType type) {
        if (type.getTypeId() <= 0) {
            System.err.println("添加失败：类型ID必须大于0");
            return false;
        }

        String sql = "INSERT INTO membership_type (type_id, type_name, duration_days, price, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, type.getTypeId());
            pstmt.setString(2, type.getTypeName());
            pstmt.setInt(3, type.getDurationDays());
            pstmt.setDouble(4, type.getPrice());
            pstmt.setString(5, type.getDescription());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 更新类型 ====================

    /**
     * 更新类型信息
     * 
     * @param type 类型对象
     * @return 是否更新成功
     */
    public boolean updateType(MembershipType type) {
        // 数据校验
        if (type.getDurationDays() <= 0) {
            System.err.println("更新失败：有效期天数必须大于0");
            return false;
        }
        if (type.getPrice() < 0) {
            System.err.println("更新失败：价格不能为负数");
            return false;
        }

        String sql = "UPDATE membership_type SET type_name = ?, duration_days = ?, price = ?, description = ? WHERE type_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.getTypeName());
            pstmt.setInt(2, type.getDurationDays());
            pstmt.setDouble(3, type.getPrice());
            pstmt.setString(4, type.getDescription());
            pstmt.setInt(5, type.getTypeId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新类型价格
     * 
     * @param typeId 类型ID
     * @param newPrice 新价格
     * @return 是否更新成功
     */
    public boolean updatePrice(int typeId, double newPrice) {
        if (newPrice < 0) {
            System.err.println("更新失败：价格不能为负数");
            return false;
        }

        String sql = "UPDATE membership_type SET price = ? WHERE type_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, typeId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 删除类型 ====================

    /**
     * 删除类型
     * 
     * 注意：如果有会员卡使用此类型，删除可能会失败
     * 
     * @param typeId 类型ID
     * @return 是否删除成功
     */
    public boolean deleteType(int typeId) {
        // 检查是否有会员卡使用此类型
        if (getCardCountByType(typeId) > 0) {
            System.err.println("删除失败：有会员卡正在使用此类型 (typeId=" + typeId + ")");
            return false;
        }

        String sql = "DELETE FROM membership_type WHERE type_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, typeId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== 便捷查询 ====================

    /**
     * 根据类型ID获取价格
     * 
     * @param typeId 类型ID
     * @return 价格，类型不存在返回0
     */
    public double getPriceByTypeId(int typeId) {
        MembershipType type = getTypeById(typeId);
        return type != null ? type.getPrice() : 0.0;
    }

    /**
     * 根据类型ID获取有效期天数
     * 
     * @param typeId 类型ID
     * @return 有效期天数，类型不存在返回0
     */
    public int getDurationDaysByTypeId(int typeId) {
        MembershipType type = getTypeById(typeId);
        return type != null ? type.getDurationDays() : 0;
    }

    /**
     * 获取月卡价格
     * 
     * @return 月卡价格
     */
    public double getMonthlyPrice() {
        return getPriceByTypeId(TYPE_ID_MONTHLY);
    }

    /**
     * 获取年卡价格
     * 
     * @return 年卡价格
     */
    public double getYearlyPrice() {
        return getPriceByTypeId(TYPE_ID_YEARLY);
    }

    // ==================== 统计功能 ====================

    /**
     * 获取类型总数
     * 
     * @return 类型总数
     */
    public int getTotalTypeCount() {
        String sql = "SELECT COUNT(*) AS count FROM membership_type";
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
     * 获取使用指定类型的会员卡数量
     * 
     * @param typeId 类型ID
     * @return 会员卡数量
     */
    public int getCardCountByType(int typeId) {
        String sql = "SELECT COUNT(*) AS count FROM membership_card WHERE type_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, typeId);
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
     * 获取月卡数量
     * 
     * @return 月卡数量
     */
    public int getMonthlyCardCount() {
        return getCardCountByType(TYPE_ID_MONTHLY);
    }

    /**
     * 获取年卡数量
     * 
     * @return 年卡数量
     */
    public int getYearlyCardCount() {
        return getCardCountByType(TYPE_ID_YEARLY);
    }

    // ==================== 工具方法 ====================

    /**
     * 获取类型的中文名称
     * 
     * @param typeId 类型ID
     * @return 中文名称
     */
    public String getTypeDisplayName(int typeId) {
        switch (typeId) {
            case TYPE_ID_MONTHLY:
                return "月卡";
            case TYPE_ID_YEARLY:
                return "年卡";
            default:
                MembershipType type = getTypeById(typeId);
                return type != null ? type.getTypeName() : "未知";
        }
    }

    /**
     * 获取类型的中文名称
     * 
     * @param typeName 类型名称
     * @return 中文名称
     */
    public String getTypeDisplayName(String typeName) {
        if (typeName == null) {
            return "未知";
        }
        switch (typeName) {
            case TYPE_NAME_MONTHLY:
                return "月卡";
            case TYPE_NAME_YEARLY:
                return "年卡";
            default:
                return typeName;
        }
    }

    /**
     * 检查类型ID是否有效
     * 
     * @param typeId 类型ID
     * @return true表示有效
     */
    public boolean isValidTypeId(int typeId) {
        return getTypeById(typeId) != null;
    }

    /**
     * 检查类型名称是否有效
     * 
     * @param typeName 类型名称
     * @return true表示有效
     */
    public boolean isValidTypeName(String typeName) {
        return getTypeByName(typeName) != null;
    }

    /**
     * 判断是否是月卡类型
     * 
     * @param typeId 类型ID
     * @return true表示是月卡
     */
    public boolean isMonthlyType(int typeId) {
        return typeId == TYPE_ID_MONTHLY;
    }

    /**
     * 判断是否是年卡类型
     * 
     * @param typeId 类型ID
     * @return true表示是年卡
     */
    public boolean isYearlyType(int typeId) {
        return typeId == TYPE_ID_YEARLY;
    }
}
