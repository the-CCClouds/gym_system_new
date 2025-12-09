import dao.EmployeeRoleDAO;
import entity.EmployeeRole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * EmployeeRoleDAO 测试类
 * 
 * 测试前提：
 * - 数据库中存在3个角色：Trainer(1), Receptionist(2), Admin(3)
 */
public class EmployeeRoleDAOTest {
    
    private EmployeeRoleDAO roleDAO;
    private int testRoleId;  // 用于清理测试数据

    @Before
    public void setUp() {
        roleDAO = new EmployeeRoleDAO();
        testRoleId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testRoleId > 0) {
            roleDAO.deleteRole(testRoleId);
            testRoleId = 0;
        }
    }

    // ==================== 常量测试 ====================

    @Test
    public void testRoleIdConstants() {
        assertEquals(1, EmployeeRoleDAO.ROLE_ID_TRAINER);
        assertEquals(2, EmployeeRoleDAO.ROLE_ID_RECEPTIONIST);
        assertEquals(3, EmployeeRoleDAO.ROLE_ID_ADMIN);
    }

    @Test
    public void testRoleNameConstants() {
        assertEquals("Trainer", EmployeeRoleDAO.ROLE_NAME_TRAINER);
        assertEquals("Receptionist", EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST);
        assertEquals("Admin", EmployeeRoleDAO.ROLE_NAME_ADMIN);
    }

    @Test
    public void testValidRoleIds() {
        int[] validIds = EmployeeRoleDAO.VALID_ROLE_IDS;
        assertEquals(3, validIds.length);
        assertEquals(1, validIds[0]);
        assertEquals(2, validIds[1]);
        assertEquals(3, validIds[2]);
    }

    // ==================== 基础查询测试 ====================

    @Test
    public void testGetAllRoles() {
        List<EmployeeRole> roles = roleDAO.getAllRoles();
        assertNotNull(roles);
        assertEquals(3, roles.size());
    }

    @Test
    public void testGetRoleById() {
        // 测试获取教练角色
        EmployeeRole trainer = roleDAO.getRoleById(EmployeeRoleDAO.ROLE_ID_TRAINER);
        assertNotNull(trainer);
        assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, trainer.getRoleName());
        assertEquals("健身教练，负责课程教学", trainer.getDescription());
        assertNotNull(trainer.getPermissions());

        // 测试获取前台角色
        EmployeeRole receptionist = roleDAO.getRoleById(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST);
        assertNotNull(receptionist);
        assertEquals(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST, receptionist.getRoleName());

        // 测试获取管理员角色
        EmployeeRole admin = roleDAO.getRoleById(EmployeeRoleDAO.ROLE_ID_ADMIN);
        assertNotNull(admin);
        assertEquals(EmployeeRoleDAO.ROLE_NAME_ADMIN, admin.getRoleName());
    }

    @Test
    public void testGetRoleByIdNotFound() {
        EmployeeRole role = roleDAO.getRoleById(999);
        assertNull(role);
    }

    @Test
    public void testGetRoleByName() {
        EmployeeRole trainer = roleDAO.getRoleByName(EmployeeRoleDAO.ROLE_NAME_TRAINER);
        assertNotNull(trainer);
        assertEquals(EmployeeRoleDAO.ROLE_ID_TRAINER, trainer.getRoleId());

        EmployeeRole receptionist = roleDAO.getRoleByName(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST);
        assertNotNull(receptionist);
        assertEquals(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST, receptionist.getRoleId());

        EmployeeRole admin = roleDAO.getRoleByName(EmployeeRoleDAO.ROLE_NAME_ADMIN);
        assertNotNull(admin);
        assertEquals(EmployeeRoleDAO.ROLE_ID_ADMIN, admin.getRoleId());
    }

    @Test
    public void testGetRoleByNameNotFound() {
        EmployeeRole role = roleDAO.getRoleByName("NotExist");
        assertNull(role);
    }

    @Test
    public void testGetRoleByNameNull() {
        assertNull(roleDAO.getRoleByName(null));
        assertNull(roleDAO.getRoleByName(""));
        assertNull(roleDAO.getRoleByName("   "));
    }

    // ==================== 便捷获取方法测试 ====================

    @Test
    public void testGetTrainerRole() {
        EmployeeRole trainer = roleDAO.getTrainerRole();
        assertNotNull(trainer);
        assertEquals(EmployeeRoleDAO.ROLE_ID_TRAINER, trainer.getRoleId());
        assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, trainer.getRoleName());
    }

    @Test
    public void testGetReceptionistRole() {
        EmployeeRole receptionist = roleDAO.getReceptionistRole();
        assertNotNull(receptionist);
        assertEquals(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST, receptionist.getRoleId());
        assertEquals(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST, receptionist.getRoleName());
    }

    @Test
    public void testGetAdminRole() {
        EmployeeRole admin = roleDAO.getAdminRole();
        assertNotNull(admin);
        assertEquals(EmployeeRoleDAO.ROLE_ID_ADMIN, admin.getRoleId());
        assertEquals(EmployeeRoleDAO.ROLE_NAME_ADMIN, admin.getRoleName());
    }

    // ==================== 权限测试 ====================

    @Test
    public void testHasPermission() {
        // 教练权限测试
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_TRAINER, "course_view"));
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_TRAINER, "member_view"));
        assertFalse(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_TRAINER, "member_add"));
        assertFalse(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_TRAINER, "employee_delete"));

        // 前台权限测试
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST, "member_view"));
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST, "member_add"));
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST, "checkin_manage"));
        assertFalse(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST, "employee_delete"));

        // 管理员权限测试
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_ADMIN, "member_view"));
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_ADMIN, "member_delete"));
        assertTrue(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_ADMIN, "employee_delete"));
    }

    @Test
    public void testHasPermissionInvalidRole() {
        assertFalse(roleDAO.hasPermission(999, "member_view"));
    }

    @Test
    public void testHasPermissionNullPermission() {
        assertFalse(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_ADMIN, null));
        assertFalse(roleDAO.hasPermission(EmployeeRoleDAO.ROLE_ID_ADMIN, ""));
    }

    @Test
    public void testGetAllPermissions() {
        List<String> trainerPermissions = roleDAO.getAllPermissions(EmployeeRoleDAO.ROLE_ID_TRAINER);
        assertNotNull(trainerPermissions);
        assertTrue(trainerPermissions.size() > 0);
        assertTrue(trainerPermissions.contains("course_view"));

        List<String> adminPermissions = roleDAO.getAllPermissions(EmployeeRoleDAO.ROLE_ID_ADMIN);
        assertNotNull(adminPermissions);
        assertTrue(adminPermissions.size() > 0);
    }

    @Test
    public void testGetAllPermissionsInvalidRole() {
        List<String> permissions = roleDAO.getAllPermissions(999);
        assertNotNull(permissions);
        assertEquals(0, permissions.size());
    }

    @Test
    public void testRolePermissionList() {
        EmployeeRole trainer = roleDAO.getRoleById(EmployeeRoleDAO.ROLE_ID_TRAINER);
        assertNotNull(trainer);

        String[] permissions = trainer.getPermissionList();
        assertNotNull(permissions);
        assertTrue(permissions.length > 0);

        // 验证权限列表包含预期权限
        boolean hasCourseView = false;
        for (String p : permissions) {
            if (p.equals("course_view")) {
                hasCourseView = true;
                break;
            }
        }
        assertTrue(hasCourseView);
    }

    @Test
    public void testRoleHasPermissionMethod() {
        EmployeeRole admin = roleDAO.getRoleById(EmployeeRoleDAO.ROLE_ID_ADMIN);
        assertNotNull(admin);

        // 测试实体类的 hasPermission 方法
        assertTrue(admin.hasPermission("member_view"));
        assertTrue(admin.hasPermission("employee_delete"));
        assertFalse(admin.hasPermission("not_exist_permission"));
    }

    // ==================== 添加角色测试 ====================

    @Test
    public void testAddAndDeleteRole() {
        // 添加新角色
        EmployeeRole newRole = new EmployeeRole();
        newRole.setRoleId(99);
        newRole.setRoleName("TestRole");
        newRole.setDescription("测试角色");
        newRole.setPermissions("test_permission1,test_permission2");

        assertTrue(roleDAO.addRole(newRole));
        testRoleId = 99;

        // 验证添加成功
        EmployeeRole added = roleDAO.getRoleById(99);
        assertNotNull(added);
        assertEquals("TestRole", added.getRoleName());
        assertEquals("测试角色", added.getDescription());
        assertTrue(added.hasPermission("test_permission1"));

        // 删除测试角色
        assertTrue(roleDAO.deleteRole(99));
        testRoleId = 0;

        // 验证删除成功
        assertNull(roleDAO.getRoleById(99));
    }

    @Test
    public void testAddRoleNullObject() {
        assertFalse(roleDAO.addRole(null));
    }

    @Test
    public void testAddRoleEmptyName() {
        EmployeeRole role = new EmployeeRole();
        role.setRoleId(100);
        role.setRoleName("");
        assertFalse(roleDAO.addRole(role));
    }

    @Test
    public void testAddRoleDuplicateName() {
        EmployeeRole role = new EmployeeRole();
        role.setRoleId(100);
        role.setRoleName(EmployeeRoleDAO.ROLE_NAME_TRAINER);  // 已存在的名称
        assertFalse(roleDAO.addRole(role));
    }

    @Test
    public void testAddRoleDuplicateId() {
        EmployeeRole role = new EmployeeRole();
        role.setRoleId(EmployeeRoleDAO.ROLE_ID_TRAINER);  // 已存在的ID
        role.setRoleName("NewRole");
        assertFalse(roleDAO.addRole(role));
    }

    // ==================== 更新角色测试 ====================

    @Test
    public void testUpdateRole() {
        // 先添加一个测试角色
        EmployeeRole testRole = new EmployeeRole();
        testRole.setRoleId(98);
        testRole.setRoleName("UpdateTestRole");
        testRole.setDescription("更新测试角色");
        testRole.setPermissions("perm1");

        assertTrue(roleDAO.addRole(testRole));
        testRoleId = 98;

        // 更新角色
        testRole.setRoleName("UpdatedRole");
        testRole.setDescription("已更新的角色");
        testRole.setPermissions("perm1,perm2,perm3");

        assertTrue(roleDAO.updateRole(testRole));

        // 验证更新成功
        EmployeeRole updated = roleDAO.getRoleById(98);
        assertNotNull(updated);
        assertEquals("UpdatedRole", updated.getRoleName());
        assertEquals("已更新的角色", updated.getDescription());
        assertTrue(updated.hasPermission("perm2"));
    }

    @Test
    public void testUpdateRoleNull() {
        assertFalse(roleDAO.updateRole(null));
    }

    @Test
    public void testUpdateRoleEmptyName() {
        EmployeeRole role = roleDAO.getTrainerRole();
        assertNotNull(role);
        
        String originalName = role.getRoleName();
        role.setRoleName("");
        assertFalse(roleDAO.updateRole(role));
        
        // 恢复
        role.setRoleName(originalName);
    }

    @Test
    public void testUpdatePermissions() {
        // 先添加一个测试角色
        EmployeeRole testRole = new EmployeeRole();
        testRole.setRoleId(97);
        testRole.setRoleName("PermTestRole");
        testRole.setDescription("权限测试角色");
        testRole.setPermissions("perm1");

        assertTrue(roleDAO.addRole(testRole));
        testRoleId = 97;

        // 更新权限
        assertTrue(roleDAO.updatePermissions(97, "perm1,perm2,perm3"));

        // 验证更新成功
        EmployeeRole updated = roleDAO.getRoleById(97);
        assertNotNull(updated);
        assertTrue(updated.hasPermission("perm2"));
        assertTrue(updated.hasPermission("perm3"));
    }

    // ==================== 权限管理测试 ====================

    @Test
    public void testAddPermission() {
        // 先添加一个测试角色
        EmployeeRole testRole = new EmployeeRole();
        testRole.setRoleId(96);
        testRole.setRoleName("AddPermTestRole");
        testRole.setDescription("添加权限测试角色");
        testRole.setPermissions("perm1");

        assertTrue(roleDAO.addRole(testRole));
        testRoleId = 96;

        // 添加新权限
        assertTrue(roleDAO.addPermission(96, "new_perm"));

        // 验证添加成功
        assertTrue(roleDAO.hasPermission(96, "new_perm"));
        assertTrue(roleDAO.hasPermission(96, "perm1"));
    }

    @Test
    public void testAddPermissionDuplicate() {
        // 先添加一个测试角色
        EmployeeRole testRole = new EmployeeRole();
        testRole.setRoleId(95);
        testRole.setRoleName("DupPermTestRole");
        testRole.setDescription("重复权限测试角色");
        testRole.setPermissions("perm1");

        assertTrue(roleDAO.addRole(testRole));
        testRoleId = 95;

        // 尝试添加已存在的权限
        assertFalse(roleDAO.addPermission(95, "perm1"));
    }

    @Test
    public void testAddPermissionInvalidRole() {
        assertFalse(roleDAO.addPermission(999, "new_perm"));
    }

    @Test
    public void testRemovePermission() {
        // 先添加一个测试角色
        EmployeeRole testRole = new EmployeeRole();
        testRole.setRoleId(94);
        testRole.setRoleName("RemovePermTestRole");
        testRole.setDescription("移除权限测试角色");
        testRole.setPermissions("perm1,perm2,perm3");

        assertTrue(roleDAO.addRole(testRole));
        testRoleId = 94;

        // 移除权限
        assertTrue(roleDAO.removePermission(94, "perm2"));

        // 验证移除成功
        assertFalse(roleDAO.hasPermission(94, "perm2"));
        assertTrue(roleDAO.hasPermission(94, "perm1"));
        assertTrue(roleDAO.hasPermission(94, "perm3"));
    }

    @Test
    public void testRemovePermissionInvalidRole() {
        assertFalse(roleDAO.removePermission(999, "perm1"));
    }

    // ==================== 统计功能测试 ====================

    @Test
    public void testGetTotalRoleCount() {
        int count = roleDAO.getTotalRoleCount();
        assertEquals(3, count);
    }

    @Test
    public void testGetEmployeeCountByRole() {
        int trainerCount = roleDAO.getEmployeeCountByRole(EmployeeRoleDAO.ROLE_ID_TRAINER);
        assertTrue(trainerCount >= 0);

        int receptionistCount = roleDAO.getEmployeeCountByRole(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST);
        assertTrue(receptionistCount >= 0);

        int adminCount = roleDAO.getEmployeeCountByRole(EmployeeRoleDAO.ROLE_ID_ADMIN);
        assertTrue(adminCount >= 0);
    }

    @Test
    public void testGetTrainerCount() {
        int count = roleDAO.getTrainerCount();
        assertTrue(count >= 4);  // 数据库有4个教练
        assertEquals(roleDAO.getEmployeeCountByRole(EmployeeRoleDAO.ROLE_ID_TRAINER), count);
    }

    @Test
    public void testGetReceptionistCount() {
        int count = roleDAO.getReceptionistCount();
        assertTrue(count >= 2);  // 数据库有2个前台
        assertEquals(roleDAO.getEmployeeCountByRole(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST), count);
    }

    @Test
    public void testGetAdminCount() {
        int count = roleDAO.getAdminCount();
        assertTrue(count >= 1);  // 数据库有1个管理员
        assertEquals(roleDAO.getEmployeeCountByRole(EmployeeRoleDAO.ROLE_ID_ADMIN), count);
    }

    // ==================== 验证功能测试 ====================

    @Test
    public void testIsValidRoleId() {
        assertTrue(roleDAO.isValidRoleId(EmployeeRoleDAO.ROLE_ID_TRAINER));
        assertTrue(roleDAO.isValidRoleId(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST));
        assertTrue(roleDAO.isValidRoleId(EmployeeRoleDAO.ROLE_ID_ADMIN));
        assertFalse(roleDAO.isValidRoleId(999));
        assertFalse(roleDAO.isValidRoleId(0));
        assertFalse(roleDAO.isValidRoleId(-1));
    }

    @Test
    public void testIsValidRoleName() {
        assertTrue(roleDAO.isValidRoleName(EmployeeRoleDAO.ROLE_NAME_TRAINER));
        assertTrue(roleDAO.isValidRoleName(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST));
        assertTrue(roleDAO.isValidRoleName(EmployeeRoleDAO.ROLE_NAME_ADMIN));
        assertFalse(roleDAO.isValidRoleName("NotExist"));
        assertFalse(roleDAO.isValidRoleName(null));
    }

    @Test
    public void testIsTrainer() {
        assertTrue(roleDAO.isTrainer(EmployeeRoleDAO.ROLE_ID_TRAINER));
        assertFalse(roleDAO.isTrainer(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST));
        assertFalse(roleDAO.isTrainer(EmployeeRoleDAO.ROLE_ID_ADMIN));
    }

    @Test
    public void testIsReceptionist() {
        assertTrue(roleDAO.isReceptionist(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST));
        assertFalse(roleDAO.isReceptionist(EmployeeRoleDAO.ROLE_ID_TRAINER));
        assertFalse(roleDAO.isReceptionist(EmployeeRoleDAO.ROLE_ID_ADMIN));
    }

    @Test
    public void testIsAdmin() {
        assertTrue(roleDAO.isAdmin(EmployeeRoleDAO.ROLE_ID_ADMIN));
        assertFalse(roleDAO.isAdmin(EmployeeRoleDAO.ROLE_ID_TRAINER));
        assertFalse(roleDAO.isAdmin(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST));
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testGetRoleDisplayNameById() {
        assertEquals("教练", roleDAO.getRoleDisplayName(EmployeeRoleDAO.ROLE_ID_TRAINER));
        assertEquals("前台", roleDAO.getRoleDisplayName(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST));
        assertEquals("管理员", roleDAO.getRoleDisplayName(EmployeeRoleDAO.ROLE_ID_ADMIN));
        assertEquals("未知", roleDAO.getRoleDisplayName(999));
    }

    @Test
    public void testGetRoleDisplayNameByName() {
        assertEquals("教练", roleDAO.getRoleDisplayName(EmployeeRoleDAO.ROLE_NAME_TRAINER));
        assertEquals("前台", roleDAO.getRoleDisplayName(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST));
        assertEquals("管理员", roleDAO.getRoleDisplayName(EmployeeRoleDAO.ROLE_NAME_ADMIN));
        assertEquals("未知", roleDAO.getRoleDisplayName("Unknown"));
        assertEquals("未知", roleDAO.getRoleDisplayName((String) null));
    }

    @Test
    public void testGetRoleNameById() {
        assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, roleDAO.getRoleNameById(EmployeeRoleDAO.ROLE_ID_TRAINER));
        assertEquals(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST, roleDAO.getRoleNameById(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST));
        assertEquals(EmployeeRoleDAO.ROLE_NAME_ADMIN, roleDAO.getRoleNameById(EmployeeRoleDAO.ROLE_ID_ADMIN));
        assertEquals("Unknown", roleDAO.getRoleNameById(999));
    }

    // ==================== 删除角色测试 ====================

    @Test
    public void testDeleteRoleInUse() {
        // 尝试删除有员工使用的角色，应该失败
        assertFalse(roleDAO.deleteRole(EmployeeRoleDAO.ROLE_ID_TRAINER));
    }

    @Test
    public void testDeleteRoleNotFound() {
        assertFalse(roleDAO.deleteRole(999));
    }
}
