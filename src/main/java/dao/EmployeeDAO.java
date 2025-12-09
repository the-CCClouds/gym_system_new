package dao;

import entity.Course;
import entity.Employee;
import entity.EmployeeRole;
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
 * 员工数据访问对象
 * 对应数据库 employee 表
 * 
 * <p>提供员工的CRUD操作、角色查询、业务查询和统计功能</p>
 * 
 * @author GymSystem
 * @version 2.0
 */
public class EmployeeDAO {

    // ==================== 角色ID常量（引用EmployeeRoleDAO） ====================
    
    /** 教练角色ID */
    public static final int ROLE_ID_TRAINER = EmployeeRoleDAO.ROLE_ID_TRAINER;
    
    /** 前台角色ID */
    public static final int ROLE_ID_RECEPTIONIST = EmployeeRoleDAO.ROLE_ID_RECEPTIONIST;
    
    /** 管理员角色ID */
    public static final int ROLE_ID_ADMIN = EmployeeRoleDAO.ROLE_ID_ADMIN;

    private EmployeeRoleDAO roleDAO;

    /**
     * 默认构造函数
     */
    public EmployeeDAO() {
        this.roleDAO = new EmployeeRoleDAO();
    }

    // ==================== 结果集提取 ====================

    /**
     * 从结果集中提取员工信息（不包含角色对象）
     * 
     * @param rs 数据库结果集
     * @return 员工对象
     * @throws SQLException SQL异常
     */
    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setName(rs.getString("name"));
        employee.setRoleId(rs.getInt("role_id"));
        employee.setPhone(rs.getString("phone"));
        employee.setHireDate(rs.getDate("hire_date"));
        return employee;
    }

    /**
     * 从结果集中提取员工信息（包含角色对象）
     * 
     * @param rs 数据库结果集
     * @return 员工对象（包含关联的角色信息）
     * @throws SQLException SQL异常
     */
    private Employee extractEmployeeWithRoleFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = extractEmployeeFromResultSet(rs);
        // 加载关联的角色对象
        EmployeeRole role = roleDAO.getRoleById(employee.getRoleId());
        employee.setEmployeeRole(role);
        return employee;
    }

    // ==================== 基础 CRUD ====================

    /**
     * 新增员工
     *
     * <p>会自动验证手机号格式和角色ID有效性</p>
     *
     * @param employee 员工对象
     * @return 是否添加成功
     */
    public boolean addEmployee(Employee employee) {
        // 数据校验
        if (employee == null) {
            System.err.println("添加失败：员工对象为空");
            return false;
        }
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            System.err.println("添加失败：员工姓名不能为空");
            return false;
        }
        if (!isValidPhone(employee.getPhone())) {
            System.err.println("无效的手机号: " + employee.getPhone());
            return false;
        }
        if (!isValidRoleId(employee.getRoleId())) {
            System.err.println("无效的角色ID: " + employee.getRoleId());
            return false;
        }
        // 检查手机号是否已存在
        if (isPhoneExists(employee.getPhone())) {
            System.err.println("添加失败：手机号已存在 (phone=" + employee.getPhone() + ")");
            return false;
        }

        String sql = "INSERT INTO employee (name, role_id, phone, hire_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getRoleId());
            pstmt.setString(3, employee.getPhone());
            pstmt.setDate(4, DateUtils.toSqlDate(employee.getHireDate()));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        employee.setEmployeeId(rs.getInt(1));
                    }
                }
            }
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据ID查询员工
     * 
     * @param employeeId 员工ID
     * @return 员工对象（包含角色信息），不存在返回null
     */
    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT * FROM employee WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEmployeeWithRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有员工
     * 
     * @return 员工列表，查询失败返回空列表
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employee ORDER BY employee_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                employees.add(extractEmployeeWithRoleFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * 更新员工信息
     * 
     * @param employee 员工对象
     * @return 是否更新成功
     */
    public boolean updateEmployee(Employee employee) {
        // 数据验证
        if (employee == null) {
            System.err.println("更新失败：员工对象为空");
            return false;
        }
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            System.err.println("更新失败：员工姓名不能为空");
            return false;
        }
        if (!isValidPhone(employee.getPhone())) {
            System.err.println("更新失败：无效的手机号 (phone=" + employee.getPhone() + ")");
            return false;
        }
        if (!isValidRoleId(employee.getRoleId())) {
            System.err.println("更新失败：无效的角色ID (roleId=" + employee.getRoleId() + ")");
            return false;
        }

        String sql = "UPDATE employee SET name = ?, role_id = ?, phone = ?, hire_date = ? WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getRoleId());
            pstmt.setString(3, employee.getPhone());
            pstmt.setDate(4, DateUtils.toSqlDate(employee.getHireDate()));
            pstmt.setInt(5, employee.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新员工角色
     * 
     * @param employeeId 员工ID
     * @param roleId 新角色ID
     * @return 是否更新成功
     */
    public boolean updateEmployeeRole(int employeeId, int roleId) {
        if (!isValidRoleId(roleId)) {
            System.err.println("更新失败：无效的角色ID (roleId=" + roleId + ")");
            return false;
        }

        String sql = "UPDATE employee SET role_id = ? WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            pstmt.setInt(2, employeeId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除员工
     * 
     * <p>注意：删除前会检查员工是否有关联的课程</p>
     * 
     * @param employeeId 员工ID
     * @return 是否删除成功
     */
    public boolean deleteEmployee(int employeeId) {
        // 检查是否有关联的课程
        List<Course> courses = getCoursesByTrainerId(employeeId);
        if (!courses.isEmpty()) {
            System.err.println("删除失败：员工有关联的课程，请先删除或转移课程 (courseCount=" + courses.size() + ")");
            return false;
        }

        String sql = "DELETE FROM employee WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 强制删除员工（不检查关联数据）
     * 
     * <p>警告：此方法不检查关联的课程，可能导致数据完整性问题</p>
     * 
     * @param employeeId 员工ID
     * @return 是否删除成功
     */
    public boolean forceDeleteEmployee(int employeeId) {
        String sql = "DELETE FROM employee WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 按角色查询 ====================

    /**
     * 查询所有教练
     * 
     * @return 教练列表
     */
    public List<Employee> getTrainers() {
        return getEmployeesByRoleId(ROLE_ID_TRAINER);
    }

    /**
     * 查询所有前台
     * 
     * @return 前台列表
     */
    public List<Employee> getReceptionists() {
        return getEmployeesByRoleId(ROLE_ID_RECEPTIONIST);
    }

    /**
     * 查询所有管理员
     * 
     * @return 管理员列表
     */
    public List<Employee> getAdmins() {
        return getEmployeesByRoleId(ROLE_ID_ADMIN);
    }

    /**
     * 根据角色ID查询员工
     * 
     * @param roleId 角色ID
     * @return 员工列表
     */
    public List<Employee> getEmployeesByRoleId(int roleId) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employee WHERE role_id = ? ORDER BY employee_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(extractEmployeeWithRoleFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    // ==================== 业务查询 ====================

    /**
     * 根据手机号查询员工（用于登录验证）
     * 
     * @param phone 手机号
     * @return 员工对象，不存在返回null
     */
    public Employee getEmployeeByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT * FROM employee WHERE phone = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEmployeeWithRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据姓名模糊查询员工
     * 
     * @param name 姓名关键字
     * @return 员工列表
     */
    public List<Employee> searchEmployeeByName(String name) {
        List<Employee> employees = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            return employees;
        }
        
        String sql = "SELECT * FROM employee WHERE name LIKE ? ORDER BY employee_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(extractEmployeeWithRoleFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * 根据入职日期范围查询员工
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 员工列表
     */
    public List<Employee> getEmployeesByHireDateRange(Date startDate, Date endDate) {
        List<Employee> employees = new ArrayList<>();
        if (startDate == null || endDate == null) {
            return employees;
        }
        if (!DateUtils.isValidDateRange(startDate, endDate)) {
            System.err.println("查询失败：无效的日期范围");
            return employees;
        }
        
        String sql = "SELECT * FROM employee WHERE hire_date BETWEEN ? AND ? ORDER BY hire_date";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, DateUtils.toSqlDate(startDate));
            pstmt.setDate(2, DateUtils.toSqlDate(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(extractEmployeeWithRoleFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * 查询某教练负责的所有课程
     * 
     * @param employeeId 教练ID
     * @return 课程列表
     */
    public List<Course> getCoursesByTrainerId(int employeeId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setCourseId(rs.getInt("course_id"));
                    course.setName(rs.getString("name"));
                    course.setType(rs.getString("type"));
                    course.setDuration(rs.getInt("duration"));
                    course.setMaxCapacity(rs.getInt("max_capacity"));
                    course.setEmployeeId(rs.getInt("employee_id"));
                    courses.add(course);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * 查询某教练今日的课程预约数
     * 
     * @param trainerId 教练ID
     * @return 今日预约数
     */
    public int getTodayBookingCount(int trainerId) {
        String sql = "SELECT COUNT(*) AS count FROM booking b " +
                "JOIN course c ON b.course_id = c.course_id " +
                "WHERE c.employee_id = ? " +
                "AND DATE(b.booking_time) = CURDATE() " +
                "AND b.booking_status IN ('pending', 'confirmed')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainerId);
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
     * 获取某教练的课程数量
     * 
     * @param trainerId 教练ID
     * @return 课程数量
     */
    public int getCourseCountByTrainer(int trainerId) {
        String sql = "SELECT COUNT(*) AS count FROM course WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainerId);
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

    // ==================== 权限检查 ====================

    /**
     * 检查员工是否拥有某个权限
     * 
     * @param employeeId 员工ID
     * @param permission 权限名称
     * @return 是否拥有该权限
     */
    public boolean hasPermission(int employeeId, String permission) {
        Employee employee = getEmployeeById(employeeId);
        if (employee != null) {
            return roleDAO.hasPermission(employee.getRoleId(), permission);
        }
        return false;
    }

    /**
     * 检查员工是否是教练
     * 
     * @param employeeId 员工ID
     * @return true表示是教练
     */
    public boolean isTrainer(int employeeId) {
        Employee employee = getEmployeeById(employeeId);
        return employee != null && employee.getRoleId() == ROLE_ID_TRAINER;
    }

    /**
     * 检查员工是否是前台
     * 
     * @param employeeId 员工ID
     * @return true表示是前台
     */
    public boolean isReceptionist(int employeeId) {
        Employee employee = getEmployeeById(employeeId);
        return employee != null && employee.getRoleId() == ROLE_ID_RECEPTIONIST;
    }

    /**
     * 检查员工是否是管理员
     * 
     * @param employeeId 员工ID
     * @return true表示是管理员
     */
    public boolean isAdmin(int employeeId) {
        Employee employee = getEmployeeById(employeeId);
        return employee != null && employee.getRoleId() == ROLE_ID_ADMIN;
    }

    // ==================== 统计功能 ====================

    /**
     * 获取员工总数
     * 
     * @return 员工总数
     */
    public int getTotalEmployeeCount() {
        String sql = "SELECT COUNT(*) AS count FROM employee";

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
     * 按角色统计员工数量
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

    /**
     * 获取本月新入职员工数量
     * 
     * @return 本月新入职员工数量
     */
    public int getMonthlyNewEmployeeCount() {
        String sql = "SELECT COUNT(*) AS count FROM employee " +
                "WHERE YEAR(hire_date) = YEAR(CURDATE()) AND MONTH(hire_date) = MONTH(CURDATE())";

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

    // ==================== 工龄计算 ====================

    /**
     * 计算员工工龄（年）
     * 
     * @param employeeId 员工ID
     * @return 工龄（年），员工不存在返回-1
     */
    public int getWorkYears(int employeeId) {
        Employee employee = getEmployeeById(employeeId);
        if (employee == null || employee.getHireDate() == null) {
            return -1;
        }
        return DateUtils.calculateAge(employee.getHireDate());
    }

    /**
     * 计算员工工龄（天）
     * 
     * @param employeeId 员工ID
     * @return 工龄（天），员工不存在返回-1
     */
    public long getWorkDays(int employeeId) {
        Employee employee = getEmployeeById(employeeId);
        if (employee == null || employee.getHireDate() == null) {
            return -1;
        }
        return DateUtils.daysBetween(employee.getHireDate(), DateUtils.now());
    }

    /**
     * 获取员工工龄格式化字符串
     * 
     * @param employeeId 员工ID
     * @return 工龄字符串（如"2年3个月"），员工不存在返回null
     */
    public String getWorkDurationString(int employeeId) {
        Employee employee = getEmployeeById(employeeId);
        if (employee == null || employee.getHireDate() == null) {
            return null;
        }
        
        long totalDays = DateUtils.daysBetween(employee.getHireDate(), DateUtils.now());
        long years = totalDays / 365;
        long months = (totalDays % 365) / 30;
        
        if (years > 0 && months > 0) {
            return years + "年" + months + "个月";
        } else if (years > 0) {
            return years + "年";
        } else if (months > 0) {
            return months + "个月";
        } else {
            return totalDays + "天";
        }
    }

    // ==================== 验证功能 ====================

    /**
     * 验证手机号格式（中国大陆手机号）
     * 
     * @param phone 手机号
     * @return true表示格式有效
     */
    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 验证角色ID是否有效
     * 
     * @param roleId 角色ID
     * @return true表示有效
     */
    public boolean isValidRoleId(int roleId) {
        return roleDAO.isValidRoleId(roleId);
    }

    /**
     * 检查手机号是否已存在
     * 
     * @param phone 手机号
     * @return true表示已存在
     */
    public boolean isPhoneExists(String phone) {
        return getEmployeeByPhone(phone) != null;
    }

    /**
     * 检查手机号是否已存在（排除指定员工）
     * 
     * @param phone 手机号
     * @param excludeEmployeeId 要排除的员工ID
     * @return true表示已存在
     */
    public boolean isPhoneExists(String phone, int excludeEmployeeId) {
        Employee employee = getEmployeeByPhone(phone);
        return employee != null && employee.getId() != excludeEmployeeId;
    }

    /**
     * 检查员工ID是否存在
     * 
     * @param employeeId 员工ID
     * @return true表示存在
     */
    public boolean isEmployeeExists(int employeeId) {
        return getEmployeeById(employeeId) != null;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取角色中文显示名称
     * 
     * @param roleId 角色ID
     * @return 中文显示名称
     */
    public String getRoleDisplayName(int roleId) {
        return roleDAO.getRoleDisplayName(roleId);
    }

    /**
     * 获取教练角色对象
     * 
     * @return 教练角色
     */
    public EmployeeRole getTrainerRole() {
        return roleDAO.getTrainerRole();
    }

    /**
     * 获取前台角色对象
     * 
     * @return 前台角色
     */
    public EmployeeRole getReceptionistRole() {
        return roleDAO.getReceptionistRole();
    }

    /**
     * 获取管理员角色对象
     * 
     * @return 管理员角色
     */
    public EmployeeRole getAdminRole() {
        return roleDAO.getAdminRole();
    }
}
