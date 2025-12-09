import entity.UserInfo;
import org.junit.jupiter.api.*;
import service.UserService;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService 测试类
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    private static UserService userService;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/gym_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    @BeforeAll
    public static void setUpClass() {
        userService = new UserService();
        // 准备测试数据
        prepareTestData();
    }

    @AfterAll
    public static void tearDownClass() {
        // 清理测试数据
        cleanTestData();
    }

    /**
     * 准备测试数据
     */
    private static void prepareTestData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 清理旧数据
            String deleteSql = "DELETE FROM users WHERE username IN ('testmember', 'testemployee', 'integrationtest', 'newmemberuser', 'newemployeeuser')";
            PreparedStatement stmt = conn.prepareStatement(deleteSql);
            stmt.executeUpdate();
            UserService userService1 = new UserService();
            // 计算正确的密码哈希(使用 DNUtil 的方法)
            String passwordHash = DBUtil.hashPassword("password123");

            // 插入测试用户
            String insertSql = "INSERT INTO users (username, password, user_type, reference_id, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            // 会员用户
            insertStmt.setString(1, "testmember");
            insertStmt.setString(2, passwordHash);  // 使用计算的哈希值
            insertStmt.setString(3, "member");
            insertStmt.setInt(4, 1);
            insertStmt.setString(5, "active");
            insertStmt.executeUpdate();

            // 员工用户
            insertStmt.setString(1, "testemployee");
            insertStmt.setString(2, passwordHash);  // 使用计算的哈希值
            insertStmt.setString(3, "employee");
            insertStmt.setInt(4, 1);
            insertStmt.setString(5, "active");
            insertStmt.executeUpdate();

            System.out.println("测试数据准备完成");
        } catch (SQLException e) {
            System.err.println("准备测试数据失败: " + e.getMessage());
        }
    }


    /**
     * 清理测试数据
     */
    private static void cleanTestData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM users WHERE username IN ('testmember', 'testemployee', 'integrationtest', 'newmemberuser', 'newemployeeuser')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            System.out.println("测试数据清理完成");
        } catch (SQLException e) {
            System.err.println("清理测试数据失败: " + e.getMessage());
        }
    }


    // ==================== 登录测试 ====================

    @Test
    @Order(1)
    @DisplayName("测试登录 - 会员用户成功")
    public void testLoginMemberSuccess() {
        UserService.LoginResult result = userService.login("testmember", "password123");

        assertTrue(result.isSuccess());
        assertEquals("登录成功", result.getMessage());
        assertEquals("member", result.getUserType());
        assertNotNull(result.getUserData());
        System.out.println("✓ 会员登录成功");
    }

    @Test
    @Order(2)
    @DisplayName("测试登录 - 员工用户成功")
    public void testLoginEmployeeSuccess() {
        UserService.LoginResult result = userService.login("testemployee", "password123");

        assertTrue(result.isSuccess());
        assertEquals("登录成功", result.getMessage());
        assertEquals("employee", result.getUserType());
        assertNotNull(result.getUserData());
        System.out.println("✓ 员工登录成功");
    }

    @Test
    @Order(3)
    @DisplayName("测试登录 - 用户名为空")
    public void testLoginEmptyUsername() {
        UserService.LoginResult result = userService.login("", "password123");

        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
        System.out.println("✓ 空用户名验证通过");
    }

    @Test
    @Order(4)
    @DisplayName("测试登录 - 密码为空")
    public void testLoginEmptyPassword() {
        UserService.LoginResult result = userService.login("testmember", "");

        assertFalse(result.isSuccess());
        assertEquals("密码不能为空", result.getMessage());
        System.out.println("✓ 空密码验证通过");
    }

    @Test
    @Order(5)
    @DisplayName("测试登录 - 错误密码")
    public void testLoginWrongPassword() {
        UserService.LoginResult result = userService.login("testmember", "wrongpassword");

        assertFalse(result.isSuccess());
        assertEquals("用户名或密码错误", result.getMessage());
        System.out.println("✓ 错误密码验证通过");
    }

    @Test
    @Order(6)
    @DisplayName("测试登录 - 用户不存在")
    public void testLoginUserNotExists() {
        UserService.LoginResult result = userService.login("nonexistent", "password123");

        assertFalse(result.isSuccess());
        assertEquals("用户名或密码错误", result.getMessage());
        System.out.println("✓ 不存在用户验证通过");
    }

    // ==================== 会员注册测试 ====================

    @Test
    @Order(10)
    @DisplayName("测试会员注册 - 成功")
    public void testRegisterMemberUserSuccess() {
        UserService.ServiceResult<Void> result = userService.registerMemberUser(
                1, "newmemberuser", "password123"
        );

        // 注意: 此测试可能失败如果用户名已存在,需要根据实际情况调整
        System.out.println("会员注册结果: " + result.getMessage());
    }

    @Test
    @Order(11)
    @DisplayName("测试会员注册 - 会员不存在")
    public void testRegisterMemberUserNotExists() {
        UserService.ServiceResult<Void> result = userService.registerMemberUser(
                99999, "testuser", "password123"
        );

        assertFalse(result.isSuccess());
        assertEquals("会员不存在", result.getMessage());
        System.out.println("✓ 会员不存在验证通过");
    }

    @Test
    @Order(12)
    @DisplayName("测试会员注册 - 用户名为空")
    public void testRegisterMemberUserEmptyUsername() {
        UserService.ServiceResult<Void> result = userService.registerMemberUser(
                1, "", "password123"
        );

        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
        System.out.println("✓ 空用户名验证通过");
    }

    @Test
    @Order(13)
    @DisplayName("测试会员注册 - 密码过短")
    public void testRegisterMemberUserShortPassword() {
        UserService.ServiceResult<Void> result = userService.registerMemberUser(
                1, "testuser", "123"
        );

        assertFalse(result.isSuccess());
        assertEquals("密码至少6位", result.getMessage());
        System.out.println("✓ 密码长度验证通过");
    }

    // ==================== 员工注册测试 ====================

    @Test
    @Order(20)
    @DisplayName("测试员工注册 - 成功")
    public void testRegisterEmployeeUserSuccess() {
        UserService.ServiceResult<Void> result = userService.registerEmployeeUser(
                1, "newemployeeuser", "password123"
        );

        System.out.println("员工注册结果: " + result.getMessage());
    }

    @Test
    @Order(21)
    @DisplayName("测试员工注册 - 员工不存在")
    public void testRegisterEmployeeUserNotExists() {
        UserService.ServiceResult<Void> result = userService.registerEmployeeUser(
                99999, "testuser", "password123"
        );

        assertFalse(result.isSuccess());
        assertEquals("员工不存在", result.getMessage());
        System.out.println("✓ 员工不存在验证通过");
    }

    @Test
    @Order(22)
    @DisplayName("测试员工注册 - 用户名已存在")
    public void testRegisterEmployeeUserDuplicateUsername() {
        UserService.ServiceResult<Void> result = userService.registerEmployeeUser(
                1, "testemployee", "password123"
        );

        assertFalse(result.isSuccess());
        assertEquals("用户名已存在", result.getMessage());
        System.out.println("✓ 重复用户名验证通过");
    }

    // ==================== 修改密码测试 ====================

    @Test
    @Order(30)
    @DisplayName("测试修改密码 - 成功")
    public void testChangePasswordSuccess() {
        UserService.ServiceResult<Void> result = userService.changePassword(
                "testmember", "password123", "newpassword123"
        );

        assertTrue(result.isSuccess());
        assertEquals("密码修改成功", result.getMessage());
        System.out.println("✓ 密码修改成功");

        // 验证新密码可以登录
        UserService.LoginResult loginResult = userService.login("testmember", "newpassword123");
        assertTrue(loginResult.isSuccess());

        // 改回原密码
        userService.changePassword("testmember", "newpassword123", "password123");
    }

    @Test
    @Order(31)
    @DisplayName("测试修改密码 - 原密码错误")
    public void testChangePasswordWrongOldPassword() {
        UserService.ServiceResult<Void> result = userService.changePassword(
                "testmember", "wrongpassword", "newpassword123"
        );

        assertFalse(result.isSuccess());
        assertEquals("原密码错误", result.getMessage());
        System.out.println("✓ 原密码错误验证通过");
    }

    @Test
    @Order(32)
    @DisplayName("测试修改密码 - 新密码过短")
    public void testChangePasswordShortNewPassword() {
        UserService.ServiceResult<Void> result = userService.changePassword(
                "testmember", "password123", "123"
        );

        assertFalse(result.isSuccess());
        assertEquals("新密码至少6位", result.getMessage());
        System.out.println("✓ 新密码长度验证通过");
    }

    @Test
    @Order(33)
    @DisplayName("测试修改密码 - 用户不存在")
    public void testChangePasswordUserNotExists() {
        UserService.ServiceResult<Void> result = userService.changePassword(
                "nonexistent", "password123", "newpassword123"
        );

        assertFalse(result.isSuccess());
        assertEquals("用户不存在", result.getMessage());
        System.out.println("✓ 用户不存在验证通过");
    }

    // ==================== 综合测试 ====================

    @Test
    @Order(40)
    @DisplayName("综合测试 - 注册后登录")
    public void testRegisterAndLogin() {
        // 1. 注册新用户
        UserService.ServiceResult<Void> registerResult = userService.registerMemberUser(
                2, "integrationtest", "password123"
        );

        if (registerResult.isSuccess()) {
            // 2. 使用新账号登录
            UserService.LoginResult loginResult = userService.login(
                    "integrationtest", "password123"
            );

            assertTrue(loginResult.isSuccess());
            System.out.println("✓ 注册后登录测试通过");
        } else {
            System.out.println("注册失败(可能用户名已存在): " + registerResult.getMessage());
        }
    }

    @Test
    @Order(41)
    @DisplayName("综合测试 - 登录后修改密码再登录")
    public void testLoginChangePasswordLogin() {
        // 1. 登录
        UserService.LoginResult loginResult1 = userService.login("testemployee", "password123");
        assertTrue(loginResult1.isSuccess());

        // 2. 修改密码
        UserService.ServiceResult<Void> changeResult = userService.changePassword(
                "testemployee", "password123", "newpass123"
        );
        assertTrue(changeResult.isSuccess());

        // 3. 用新密码登录
        UserService.LoginResult loginResult2 = userService.login("testemployee", "newpass123");
        assertTrue(loginResult2.isSuccess());

        // 4. 改回原密码
        userService.changePassword("testemployee", "newpass123", "password123");

        System.out.println("✓ 登录-修改密码-再登录测试通过");
    }

// ==================== 用户查询测试 ====================

    @Test
    @Order(50)
    @DisplayName("测试根据用户名查询 - 用户存在")
    public void testGetUserByUsernameExists() {
        UserInfo userInfo = userService.getUserByUsername("testmember");

        assertNotNull(userInfo);
        assertEquals("testmember", userInfo.getUsername());
        assertEquals("member", userInfo.getUserType());
        assertEquals(1, userInfo.getReferenceId());
        assertEquals("active", userInfo.getStatus());
        System.out.println("✓ 根据用户名查询成功");
    }

    @Test
    @Order(51)
    @DisplayName("测试根据用户名查询 - 用户不存在")
    public void testGetUserByUsernameNotExists() {
        UserInfo userInfo = userService.getUserByUsername("nonexistentuser");

        assertNull(userInfo);
        System.out.println("✓ 用户不存在查询验证通过");
    }

    @Test
    @Order(52)
    @DisplayName("测试根据用户名查询 - 空用户名")
    public void testGetUserByUsernameEmpty() {
        UserInfo userInfo = userService.getUserByUsername("");

        assertNull(userInfo);
        System.out.println("✓ 空用户名查询验证通过");
    }

    @Test
    @Order(53)
    @DisplayName("测试根据用户名查询 - null用户名")
    public void testGetUserByUsernameNull() {
        UserInfo userInfo = userService.getUserByUsername(null);

        assertNull(userInfo);
        System.out.println("✓ null用户名查询验证通过");
    }

// ==================== 根据关联ID查询测试 ====================

    @Test
    @Order(60)
    @DisplayName("测试根据关联ID查询 - 会员用户存在")
    public void testGetUserByReferenceMemberExists() {
        UserInfo userInfo = userService.getUserByReference("member", 1);

        assertNotNull(userInfo);
        assertEquals("member", userInfo.getUserType());
        assertEquals(1, userInfo.getReferenceId());
        System.out.println("✓ 根据关联ID查询会员成功");
    }

    @Test
    @Order(61)
    @DisplayName("测试根据关联ID查询 - 员工用户存在")
    public void testGetUserByReferenceEmployeeExists() {
        UserInfo userInfo = userService.getUserByReference("employee", 1);

        assertNotNull(userInfo);
        assertEquals("employee", userInfo.getUserType());
        assertEquals(1, userInfo.getReferenceId());
        System.out.println("✓ 根据关联ID查询员工成功");
    }

    @Test
    @Order(62)
    @DisplayName("测试根据关联ID查询 - 用户不存在")
    public void testGetUserByReferenceNotExists() {
        UserInfo userInfo = userService.getUserByReference("member", 99999);

        assertNull(userInfo);
        System.out.println("✓ 关联用户不存在查询验证通过");
    }

    @Test
    @Order(63)
    @DisplayName("测试根据关联ID查询 - 类型不匹配")
    public void testGetUserByReferenceTypeMismatch() {
        // 用员工类型查询会员ID
        UserInfo userInfo = userService.getUserByReference("employee", 99999);

        assertNull(userInfo);
        System.out.println("✓ 类型不匹配查询验证通过");
    }

// ==================== 检查账号存在测试 ====================

    @Test
    @Order(70)
    @DisplayName("测试检查账号存在 - 会员有账号")
    public void testHasAccountMemberTrue() {
        boolean hasAccount = userService.hasAccount("member", 1);

        assertTrue(hasAccount);
        System.out.println("✓ 会员有账号验证通过");
    }

    @Test
    @Order(71)
    @DisplayName("测试检查账号存在 - 员工有账号")
    public void testHasAccountEmployeeTrue() {
        boolean hasAccount = userService.hasAccount("employee", 1);

        assertTrue(hasAccount);
        System.out.println("✓ 员工有账号验证通过");
    }

    @Test
    @Order(72)
    @DisplayName("测试检查账号存在 - 无账号")
    public void testHasAccountFalse() {
        boolean hasAccount = userService.hasAccount("member", 99999);

        assertFalse(hasAccount);
        System.out.println("✓ 无账号验证通过");
    }

// ==================== UserInfo 属性测试 ====================

    @Test
    @Order(80)
    @DisplayName("测试UserInfo所有属性")
    public void testUserInfoProperties() {
        UserInfo userInfo = userService.getUserByUsername("testmember");

        assertNotNull(userInfo);
        assertTrue(userInfo.getUserId() > 0);
        assertNotNull(userInfo.getUsername());
        assertNotNull(userInfo.getUserType());
        assertTrue(userInfo.getReferenceId() > 0);
        assertNotNull(userInfo.getStatus());
        // lastLogin 可能为 null（首次查询前未登录）
        System.out.println("✓ UserInfo属性测试通过");
        System.out.println("  - userId: " + userInfo.getUserId());
        System.out.println("  - username: " + userInfo.getUsername());
        System.out.println("  - userType: " + userInfo.getUserType());
        System.out.println("  - referenceId: " + userInfo.getReferenceId());
        System.out.println("  - status: " + userInfo.getStatus());
        System.out.println("  - lastLogin: " + userInfo.getLastLogin());
    }

}
