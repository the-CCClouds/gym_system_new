package dao;

import entity.EmployeeRole;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 员工角色数据访问对象
 * 对应数据库 employee_role 表
 * 
 * <p>提供员工角色的CRUD操作、权限管理和统计功能</p>
 * 
 * @author GymSystem
 * @version 2.0
 */
public class EmployeeRoleDAO {

    // ==================== 角色ID常量 ====================
    
    /** 教练角色ID */
    public static final int ROLE_ID_TRAINER = 1;
    
    /** 前台角色ID */
    public static final int ROLE_ID_RECEPTIONIST = 2;
    
    /** 管理员角色ID */
    public static final int ROLE_ID_ADMIN = 3;

    // ==================== 角色名称常量 ====================
    
    /** 教练角色名称 */
    public static final String ROLE_NAME_TRAINER = "Trainer";
    
    /** 前台角色名称 */
    public static final String ROLE_NAME_RECEPTIONIST = "Receptionist";
    
    /** 管理员角色名称 */
    public static final String ROLE_NAME_ADMIN = "Admin";

    // ==================== 有效角色ID数组 ====================
    
    /** 有效的角色ID列表 */
    public static final int[] VALID_ROLE_IDS = {ROLE_ID_TRAINER, ROLE_ID_RECEPTIONIST, ROLE_ID_ADMIN};

    /**
     * 默认构造函数
     */
    public EmployeeRoleDAO() {
    }

    // ==================== 结果集提取 ====================

    /**
     * 从结果集中提取角色信息
     * 
     * @param rs 数据库结果集
     * @return 角色对象
     * @throws SQLException SQL异常
     */
    private EmployeeRole extractRoleFromResultSet(ResultSet rs) throws SQLException {
        EmployeeRole role = new EmployeeRole();
        role.setRoleId(rs.getInt("role_id"));
        role.setRoleName(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        role.setPermissions(rs.getString("permissions"));
        return role;
    }

    // ==================== 基础查询 ====================

    /**
     * 获取所有角色
     * 
     * @return 角色列表，查询失败返回空列表
     */
    public List<EmployeeRole> getAllRoles() {
        List<EmployeeRole> roles = new ArrayList<>();
        String sql = "SELECT * FROM employee_role ORDER BY role_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                roles.add(extractRoleFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * 根据ID获取角色
     * 
     * @param roleId 角色ID
     * @return 角色对象，不存在返回null
     */
    public EmployeeRole getRoleById(int roleId) {
        String sql = "SELECT * FROM employee_role WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据角色名称获取角色
     * 
     * @param roleName 角色名称 (Trainer, Receptionist, Admin)
     * @return 角色对象，不存在返回null
     */
    public EmployeeRole getRoleByName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT * FROM employee_role WHERE role_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== 便捷获取方法 ====================

    /**
     * 获取教练角色
     * 
     * @return 教练角色对象
     */
    public EmployeeRole getTrainerRole() {
        return getRoleById(ROLE_ID_TRAINER);
    }

    /**
     * 获取前台角色
     * 
     * @return 前台角色对象
     */
    public EmployeeRole getReceptionistRole() {
        return getRoleById(ROLE_ID_RECEPTIONIST);
    }

    /**
     * 获取管理员角色
     * 
     * @return 管理员角色对象
     */
    public EmployeeRole getAdminRole() {
        return getRoleById(ROLE_ID_ADMIN);
    }

    // ==================== 添加角色 ====================

    /**
     * 添加新角色
     * 
     * <p>注意：role_id需要手动指定，因为employee_role表没有AUTO_INCREMENT</p>
     * 
     * @param role 角色对象（必须设置roleId）
     * @return 是否添加成功
     */
    public boolean addRole(EmployeeRole role) {
        // 数据验证
        if (role == null) {
            System.err.println("添加失败：角色对象为空");
            return false;
        }
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            System.err.println("添加失败：角色名称不能为空");
            return false;
        }
        // 检查角色名是否已存在
        if (getRoleByName(role.getRoleName()) != null) {
            System.err.println("添加失败：角色名称已存在 (roleName=" + role.getRoleName() + ")");
            return false;
        }
        // 检查角色ID是否已存在
        if (getRoleById(role.getRoleId()) != null) {
            System.err.println("添加失败：角色ID已存在 (roleId=" + role.getRoleId() + ")");
            return false;
        }

        String sql = "INSERT INTO employee_role (role_id, role_name, description, permissions) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, role.getRoleId());
            pstmt.setString(2, role.getRoleName());
            pstmt.setString(3, role.getDescription());
            pstmt.setString(4, role.getPermissions());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 更新角色 ====================

    /**
     * 更新角色信息
     * 
     * @param role 角色对象
     * @return 是否更新成功
     */
    public boolean updateRole(EmployeeRole role) {
        // 数据验证
        if (role == null) {
            System.err.println("更新失败：角色对象为空");
            return false;
        }
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            System.err.println("更新失败：角色名称不能为空");
            return false;
        }

        String sql = "UPDATE employee_role SET role_name = ?, description = ?, permissions = ? WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.getRoleName());
            pstmt.setString(2, role.getDescription());
            pstmt.setString(3, role.getPermissions());
            pstmt.setInt(4, role.getRoleId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新角色权限
     * 
     * @param roleId 角色ID
     * @param permissions 新的权限字符串（逗号分隔）
     * @return 是否更新成功
     */
    public boolean updatePermissions(int roleId, String permissions) {
        String sql = "UPDATE employee_role SET permissions = ? WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, permissions);
            pstmt.setInt(2, roleId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 删除角色 ====================

    /**
     * 删除角色
     * 
     * <p>注意：删除前会检查是否有员工正在使用该角色</p>
     * 
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    public boolean deleteRole(int roleId) {
        // 检查是否有员工使用该角色
        int employeeCount = getEmployeeCountByRole(roleId);
        if (employeeCount > 0) {
            System.err.println("删除失败：有员工正在使用此角色 (roleId=" + roleId + ", employeeCount=" + employeeCount + ")");
            return false;
        }

        String sql = "DELETE FROM employee_role WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 权限管理 ====================

    /**
     * 检查角色是否拥有某个权限
     * 
     * @param roleId 角色ID
     * @param permission 权限名称
     * @return 是否拥有该权限
     */
    public boolean hasPermission(int roleId, String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        EmployeeRole role = getRoleById(roleId);
        if (role != null && role.getPermissions() != null) {
            String[] permissions = role.getPermissions().split(",");
            for (String p : permissions) {
                if (p.trim().equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取角色的所有权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表，角色不存在或无权限返回空列表
     */
    public List<String> getAllPermissions(int roleId) {
        List<String> permissionList = new ArrayList<>();
        EmployeeRole role = getRoleById(roleId);
        
        if (role != null && role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            String[] permissions = role.getPermissions().split(",");
            for (String p : permissions) {
                permissionList.add(p.trim());
            }
        }
        return permissionList;
    }

    /**
     * 为角色添加权限
     * 
     * @param roleId 角色ID
     * @param permission 要添加的权限
     * @return 是否添加成功
     */
    public boolean addPermission(int roleId, String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            System.err.println("添加权限失败：权限名称不能为空");
            return false;
        }
        
        // 检查权限是否已存在
        if (hasPermission(roleId, permission)) {
            System.err.println("添加权限失败：权限已存在 (permission=" + permission + ")");
            return false;
        }
        
        EmployeeRole role = getRoleById(roleId);
        if (role == null) {
            System.err.println("添加权限失败：角色不存在 (roleId=" + roleId + ")");
            return false;
        }
        
        String currentPermissions = role.getPermissions();
        String newPermissions;
        if (currentPermissions == null || currentPermissions.isEmpty()) {
            newPermissions = permission.trim();
        } else {
            newPermissions = currentPermissions + "," + permission.trim();
        }
        
        return updatePermissions(roleId, newPermissions);
    }

    /**
     * 移除角色的某个权限
     * 
     * @param roleId 角色ID
     * @param permission 要移除的权限
     * @return 是否移除成功
     */
    public boolean removePermission(int roleId, String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            System.err.println("移除权限失败：权限名称不能为空");
            return false;
        }
        
        EmployeeRole role = getRoleById(roleId);
        if (role == null) {
            System.err.println("移除权限失败：角色不存在 (roleId=" + roleId + ")");
            return false;
        }
        
        if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
            return false;
        }
        
        List<String> permissionList = new ArrayList<>(Arrays.asList(role.getPermissions().split(",")));
        boolean removed = permissionList.removeIf(p -> p.trim().equals(permission.trim()));
        
        if (removed) {
            String newPermissions = String.join(",", permissionList);
            return updatePermissions(roleId, newPermissions);
        }
        return false;
    }

    // ==================== 统计功能 ====================

    /**
     * 获取角色总数
     * 
     * @return 角色总数
     */
    public int getTotalRoleCount() {
        String sql = "SELECT COUNT(*) AS count FROM employee_role";

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
     * 获取某角色下的员工数量
     * 
     * @param roleId 角色ID
     * @return 员工数量
     */
    public int getEmployeeCountByRole(int roleId) {
        String sql = "SELECT COUNT(*) AS count FROM employee WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
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
     * 获取教练数量
     * 
     * @return 教练数量
     */
    public int getTrainerCount() {
        return getEmployeeCountByRole(ROLE_ID_TRAINER);
    }

    /**
     * 获取前台数量
     * 
     * @return 前台数量
     */
    public int getReceptionistCount() {
        return getEmployeeCountByRole(ROLE_ID_RECEPTIONIST);
    }

    /**
     * 获取管理员数量
     * 
     * @return 管理员数量
     */
    public int getAdminCount() {
        return getEmployeeCountByRole(ROLE_ID_ADMIN);
    }

    // ==================== 验证功能 ====================

    /**
     * 检查角色ID是否有效（存在于数据库中）
     * 
     * @param roleId 角色ID
     * @return true表示有效
     */
    public boolean isValidRoleId(int roleId) {
        return getRoleById(roleId) != null;
    }

    /**
     * 检查角色名称是否有效（存在于数据库中）
     * 
     * @param roleName 角色名称
     * @return true表示有效
     */
    public boolean isValidRoleName(String roleName) {
        return getRoleByName(roleName) != null;
    }

    /**
     * 检查是否是教练角色
     * 
     * @param roleId 角色ID
     * @return true表示是教练
     */
    public boolean isTrainer(int roleId) {
        return roleId == ROLE_ID_TRAINER;
    }

    /**
     * 检查是否是前台角色
     * 
     * @param roleId 角色ID
     * @return true表示是前台
     */
    public boolean isReceptionist(int roleId) {
        return roleId == ROLE_ID_RECEPTIONIST;
    }

    /**
     * 检查是否是管理员角色
     * 
     * @param roleId 角色ID
     * @return true表示是管理员
     */
    public boolean isAdmin(int roleId) {
        return roleId == ROLE_ID_ADMIN;
    }

    // ==================== 工具方法 ====================

    /**
     * 根据角色ID获取角色中文显示名称
     * 
     * @param roleId 角色ID
     * @return 中文显示名称
     */
    public String getRoleDisplayName(int roleId) {
        switch (roleId) {
            case ROLE_ID_TRAINER:
                return "教练";
            case ROLE_ID_RECEPTIONIST:
                return "前台";
            case ROLE_ID_ADMIN:
                return "管理员";
            default:
                return "未知";
        }
    }

    /**
     * 根据角色名称获取角色中文显示名称
     * 
     * @param roleName 角色名称（英文）
     * @return 中文显示名称
     */
    public String getRoleDisplayName(String roleName) {
        if (roleName == null) {
            return "未知";
        }
        switch (roleName) {
            case ROLE_NAME_TRAINER:
                return "教练";
            case ROLE_NAME_RECEPTIONIST:
                return "前台";
            case ROLE_NAME_ADMIN:
                return "管理员";
            default:
                return "未知";
        }
    }

    /**
     * 获取角色ID对应的英文名称
     * 
     * @param roleId 角色ID
     * @return 英文名称
     */
    public String getRoleNameById(int roleId) {
        switch (roleId) {
            case ROLE_ID_TRAINER:
                return ROLE_NAME_TRAINER;
            case ROLE_ID_RECEPTIONIST:
                return ROLE_NAME_RECEPTIONIST;
            case ROLE_ID_ADMIN:
                return ROLE_NAME_ADMIN;
            default:
                return "Unknown";
        }
    }
}
