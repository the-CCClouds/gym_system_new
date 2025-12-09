package service;


import dao.MemberDAO;
import dao.EmployeeDAO;
import entity.Member;
import entity.Employee;
import entity.UserInfo;
import utils.DBUtil;
import java.sql.*;

public class UserService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/gym_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    private MemberDAO memberDAO;
    private EmployeeDAO employeeDAO;

    public UserService() {
        this.memberDAO = new MemberDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * 用户登录
     */
    public LoginResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return LoginResult.failure("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return LoginResult.failure("密码不能为空");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE username = ? AND status = 'active'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username.trim());

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return LoginResult.failure("用户名或密码错误");
            }

            String storedPasswordHash = rs.getString("password");
            String inputPasswordHash = DBUtil.hashPassword(password);

            // 比较哈希值
            if (!storedPasswordHash.equals(inputPasswordHash)) {
                return LoginResult.failure("用户名或密码错误");
            }

            updateLastLogin(conn, rs.getInt("user_id"));

            String userType = rs.getString("user_type");
            int referenceId = rs.getInt("reference_id");

            if ("member".equals(userType)) {
                Member member = memberDAO.getMemberById(referenceId);
                if (member == null) {
                    return LoginResult.failure("关联会员不存在");
                }
                return LoginResult.success("登录成功", userType, member);
            } else if ("employee".equals(userType)) {
                Employee employee = employeeDAO.getEmployeeById(referenceId);
                if (employee == null) {
                    return LoginResult.failure("关联员工不存在");
                }
                return LoginResult.success("登录成功", userType, employee);
            }

            return LoginResult.failure("未知用户类型");

        } catch (SQLException e) {
            return LoginResult.failure("登录失败: " + e.getMessage());
        }
    }

    /**
     * 为会员注册用户账号
     */
    public ServiceResult<Void> registerMemberUser(int memberId, String username, String password) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("会员不存在");
        }

        return createUser(username, password, "member", memberId);
    }

    /**
     * 为员工注册用户账号
     */
    public ServiceResult<Void> registerEmployeeUser(int employeeId, String username, String password) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("员工不存在");
        }

        return createUser(username, password, "employee", employeeId);
    }

    /**
     * 验证密码 - 比较明文密码与存储的哈希
     */
    public boolean verifyPassword(String plainPassword, String storedHash) {
        String inputHash = DBUtil.hashPassword(plainPassword);
        return inputHash.equals(storedHash);
    }

    /**
     * 创建用户账号
     */
    private ServiceResult<Void> createUser(String username, String password, String userType, int referenceId) {
        if (username == null || username.trim().isEmpty()) {
            return ServiceResult.failure("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            return ServiceResult.failure("密码至少6位");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 检查用户名是否存在
            if (isUsernameExists(conn, username)) {
                return ServiceResult.failure("用户名已存在");
            }

            String sql = "INSERT INTO users (username, password, user_type, reference_id) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, DBUtil.hashPassword(password));
            stmt.setString(3, userType);
            stmt.setInt(4, referenceId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return ServiceResult.success("账号创建成功");
            } else {
                return ServiceResult.failure("账号创建失败");
            }

        } catch (SQLException e) {
            return ServiceResult.failure("创建失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    public ServiceResult<Void> changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return ServiceResult.failure("新密码至少6位");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String checkSql = "SELECT password FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                return ServiceResult.failure("用户不存在");
            }

            // 验证旧密码
            if (!verifyPassword(oldPassword, rs.getString("password"))) {
                return ServiceResult.failure("原密码错误");
            }

            String updateSql = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, DBUtil.hashPassword(newPassword));
            updateStmt.setString(2, username);

            int rows = updateStmt.executeUpdate();
            return rows > 0 ? ServiceResult.success("密码修改成功") : ServiceResult.failure("密码修改失败");

        } catch (SQLException e) {
            return ServiceResult.failure("修改失败: " + e.getMessage());
        }
    }

    // 检查用户名是否存在
    private boolean isUsernameExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    // 更新最后登录时间
    private void updateLastLogin(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.executeUpdate();
    }

    /**
     * 根据用户名查询用户信息
     */
    public UserInfo getUserByUsername(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserInfo(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("user_type"),
                        rs.getInt("reference_id"),
                        rs.getString("status"),
                        rs.getTimestamp("last_login")
                );
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 根据关联ID和用户类型查询用户
     */
    public UserInfo getUserByReference(String userType, int referenceId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE user_type = ? AND reference_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userType);
            stmt.setInt(2, referenceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserInfo(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("user_type"),
                        rs.getInt("reference_id"),
                        rs.getString("status"),
                        rs.getTimestamp("last_login")
                );
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 检查会员/员工是否已有账号
     */
    public boolean hasAccount(String userType, int referenceId) {
        return getUserByReference(userType, referenceId) != null;
    }


    // 登录结果类
    public static class LoginResult {
        private boolean success;
        private String message;
        private String userType;
        private Object userData;

        private LoginResult(boolean success, String message, String userType, Object userData) {
            this.success = success;
            this.message = message;
            this.userType = userType;
            this.userData = userData;
        }

        public static LoginResult success(String message, String userType, Object userData) {
            return new LoginResult(true, message, userType, userData);
        }

        public static LoginResult failure(String message) {
            return new LoginResult(false, message, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getUserType() { return userType; }
        public Object getUserData() { return userData; }
    }

    // >>> 新增：管理员强制重置密码 (无需旧密码) <<<
    public ServiceResult<Void> resetPassword(String username, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            return ServiceResult.failure("用户名不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ServiceResult.failure("新密码至少6位");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. 检查用户是否存在
            if (!isUsernameExists(conn, username)) {
                return ServiceResult.failure("该账号不存在 (可能是老员工未注册账号)");
            }

            // 2. 直接更新密码
            String sql = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, DBUtil.hashPassword(newPassword));
            stmt.setString(2, username);

            int rows = stmt.executeUpdate();
            return rows > 0 ? ServiceResult.success("密码重置成功") : ServiceResult.failure("密码重置失败");

        } catch (SQLException e) {
            return ServiceResult.failure("系统错误: " + e.getMessage());
        }
    }
    /**
     * 【新增】设置员工账号（创建 或 更新）
     * 功能：管理员手动管理员工账号。
     * 逻辑：如果员工已有账号，则更新用户名和密码；如果没有，则创建新账号。
     */
    public ServiceResult<Void> setEmployeeAccount(int employeeId, String newUsername, String newPassword) {
        if (newUsername == null || newUsername.trim().isEmpty()) return ServiceResult.failure("用户名不能为空");
        if (newPassword == null || newPassword.length() < 6) return ServiceResult.failure("密码至少6位");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 1. 检查用户名是否被【其他人】占用
            // 查询拥有该用户名的用户ID和关联ID
            String sqlCheck = "SELECT user_id, reference_id, user_type FROM users WHERE username = ?";
            PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck);
            stmtCheck.setString(1, newUsername);
            ResultSet rsCheck = stmtCheck.executeQuery();

            if (rsCheck.next()) {
                // 如果查到了用户，且该用户不是当前正在操作的这个员工
                boolean isSelf = "employee".equals(rsCheck.getString("user_type")) && rsCheck.getInt("reference_id") == employeeId;
                if (!isSelf) {
                    return ServiceResult.failure("该用户名 [" + newUsername + "] 已被其他人使用！");
                }
            }

            // 2. 检查该员工当前是否有账号
            String sqlExist = "SELECT user_id FROM users WHERE user_type = 'employee' AND reference_id = ?";
            PreparedStatement stmtExist = conn.prepareStatement(sqlExist);
            stmtExist.setInt(1, employeeId);
            ResultSet rsExist = stmtExist.executeQuery();

            if (rsExist.next()) {
                // --- 情况 A: 已有账号 -> 执行 UPDATE (修改用户名/重置密码) ---
                String sqlUpdate = "UPDATE users SET username = ?, password = ? WHERE user_type = 'employee' AND reference_id = ?";
                PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
                stmtUpdate.setString(1, newUsername);
                stmtUpdate.setString(2, DBUtil.hashPassword(newPassword));
                stmtUpdate.setInt(3, employeeId);

                int rows = stmtUpdate.executeUpdate();
                return rows > 0 ? ServiceResult.success("✅ 账号信息已更新 (密码重置)") : ServiceResult.failure("更新失败");
            } else {
                // --- 情况 B: 无账号 -> 执行 INSERT (开通账号) ---
                String sqlInsert = "INSERT INTO users (username, password, user_type, reference_id) VALUES (?, ?, 'employee', ?)";
                PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert);
                stmtInsert.setString(1, newUsername);
                stmtInsert.setString(2, DBUtil.hashPassword(newPassword));
                stmtInsert.setInt(3, employeeId);

                int rows = stmtInsert.executeUpdate();
                return rows > 0 ? ServiceResult.success("✅ 账号已成功开通") : ServiceResult.failure("开通失败");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return ServiceResult.failure("系统错误: " + e.getMessage());
        }
    }

    // 服务结果类
    public static class ServiceResult<T> {
        private boolean success;
        private String message;

        private ServiceResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static <T> ServiceResult<T> success(String message) {
            return new ServiceResult<>(true, message);
        }

        public static <T> ServiceResult<T> failure(String message) {
            return new ServiceResult<>(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}

