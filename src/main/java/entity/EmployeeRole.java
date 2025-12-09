package entity;

/**
 * 员工角色实体类：体现继承关系
 * 对应数据库 employee_role 表
 */
public class EmployeeRole {
    private int roleId;
    private String roleName;  // 'Trainer', 'Receptionist', 'Admin'
    private String description;
    private String permissions;  // 权限列表，逗号分隔

    public EmployeeRole() {
    }

    public EmployeeRole(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public EmployeeRole(int roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    public EmployeeRole(int roleId, String roleName, String description, String permissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    /**
     * 检查是否拥有某个权限
     *
     * @param permission 权限名称
     * @return 是否拥有该权限
     */
    public boolean hasPermission(String permission) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        String[] permList = permissions.split(",");
        for (String p : permList) {
            if (p.trim().equals(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取权限列表数组
     *
     * @return 权限数组
     */
    public String[] getPermissionList() {
        if (permissions == null || permissions.isEmpty()) {
            return new String[0];
        }
        return permissions.split(",");
    }

    @Override
    public String toString() {
        return "EmployeeRole{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                ", permissions='" + permissions + '\'' +
                '}';
    }
}

