package service;

import dao.EmployeeDAO;
import dao.EmployeeRoleDAO;
import dao.CourseDAO;
import dao.BookingDAO;
import entity.Course;
import entity.Employee;
import entity.EmployeeRole;
import utils.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工服务类
 * 提供员工相关的业务逻辑处理
 * <p>
 * 主要功能：
 * - 员工入职（创建员工）
 * - 员工信息管理
 * - 员工角色管理
 * - 员工查询（综合查询、模糊搜索）
 * - 员工统计报表
 * - 权限验证
 *
 * @author GymSystem
 * @version 1.0
 */
public class EmployeeService {

    // ==================== 依赖的DAO ====================

    private EmployeeDAO employeeDAO;
    private EmployeeRoleDAO roleDAO;
    private CourseDAO courseDAO;
    private BookingDAO bookingDAO;

    // ==================== 角色常量（引用DAO） ====================

    /**
     * 教练角色ID
     */
    public static final int ROLE_TRAINER = EmployeeRoleDAO.ROLE_ID_TRAINER;
    /**
     * 前台角色ID
     */
    public static final int ROLE_RECEPTIONIST = EmployeeRoleDAO.ROLE_ID_RECEPTIONIST;
    /**
     * 管理员角色ID
     */
    public static final int ROLE_ADMIN = EmployeeRoleDAO.ROLE_ID_ADMIN;

    // ==================== 构造方法 ====================

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAO();
        this.roleDAO = new EmployeeRoleDAO();
        this.courseDAO = new CourseDAO();
        this.bookingDAO = new BookingDAO();
    }

    // ==================== 员工入职 ====================

    /**
     * 员工入职（创建员工）
     *
     * @param name     姓名
     * @param phone    手机号
     * @param roleId   角色ID（1=教练，2=前台，3=管理员）
     * @param hireDate 入职日期
     * @return 入职结果，包含成功/失败信息和员工对象
     */
    public ServiceResult<Employee> hire(String name, String phone, int roleId, Date hireDate) {
        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.failure("入职失败：姓名不能为空");
        }
        if (!employeeDAO.isValidPhone(phone)) {
            return ServiceResult.failure("入职失败：无效的手机号格式");
        }
        if (!employeeDAO.isValidRoleId(roleId)) {
            return ServiceResult.failure("入职失败：无效的角色ID");
        }

        // 检查手机号是否已存在
        if (employeeDAO.isPhoneExists(phone)) {
            return ServiceResult.failure("入职失败：该手机号已被使用");
        }

        // 创建员工对象
        Employee employee = new Employee();
        employee.setName(name.trim());
        employee.setPhone(phone);
        employee.setRoleId(roleId);
        employee.setHireDate(hireDate != null ? hireDate : DateUtils.now());

        // 保存到数据库
        if (employeeDAO.addEmployee(employee)) {
            // 加载角色信息
            EmployeeRole role = roleDAO.getRoleById(roleId);
            employee.setEmployeeRole(role);
            String roleName = roleDAO.getRoleDisplayName(roleId);
            return ServiceResult.success("入职成功，角色：" + roleName, employee);
        } else {
            return ServiceResult.failure("入职失败：数据库操作失败");
        }
    }

    /**
     * 员工入职（使用当前日期作为入职日期）
     */
    public ServiceResult<Employee> hire(String name, String phone, int roleId) {
        return hire(name, phone, roleId, DateUtils.now());
    }

    /**
     * 招聘教练
     */
    public ServiceResult<Employee> hireTrainer(String name, String phone, Date hireDate) {
        return hire(name, phone, ROLE_TRAINER, hireDate);
    }

    /**
     * 招聘教练（使用当前日期）
     */
    public ServiceResult<Employee> hireTrainer(String name, String phone) {
        return hire(name, phone, ROLE_TRAINER);
    }

    /**
     * 招聘前台
     */
    public ServiceResult<Employee> hireReceptionist(String name, String phone, Date hireDate) {
        return hire(name, phone, ROLE_RECEPTIONIST, hireDate);
    }

    /**
     * 招聘前台（使用当前日期）
     */
    public ServiceResult<Employee> hireReceptionist(String name, String phone) {
        return hire(name, phone, ROLE_RECEPTIONIST);
    }

    /**
     * 招聘管理员
     */
    public ServiceResult<Employee> hireAdmin(String name, String phone, Date hireDate) {
        return hire(name, phone, ROLE_ADMIN, hireDate);
    }

    /**
     * 招聘管理员（使用当前日期）
     */
    public ServiceResult<Employee> hireAdmin(String name, String phone) {
        return hire(name, phone, ROLE_ADMIN);
    }

    // ==================== 员工信息管理 ====================

    /**
     * 更新员工基本信息
     *
     * @param employeeId 员工ID
     * @param name       姓名
     * @param phone      手机号
     * @return 更新结果
     */
    public ServiceResult<Employee> updateEmployeeInfo(int employeeId, String name, String phone) {
        // 查询员工是否存在
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("更新失败：员工不存在");
        }

        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.failure("更新失败：姓名不能为空");
        }
        if (!employeeDAO.isValidPhone(phone)) {
            return ServiceResult.failure("更新失败：无效的手机号格式");
        }

        // 检查手机号是否被其他员工使用
        if (employeeDAO.isPhoneExists(phone, employeeId)) {
            return ServiceResult.failure("更新失败：该手机号已被其他员工使用");
        }

        // 更新信息
        employee.setName(name.trim());
        employee.setPhone(phone);

        if (employeeDAO.updateEmployee(employee)) {
            return ServiceResult.success("更新成功", employee);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    /**
     * 更新员工手机号
     *
     * @param employeeId 员工ID
     * @param newPhone   新手机号
     * @return 更新结果
     */
    public ServiceResult<Employee> updateEmployeePhone(int employeeId, String newPhone) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("更新失败：员工不存在");
        }

        if (!employeeDAO.isValidPhone(newPhone)) {
            return ServiceResult.failure("更新失败：无效的手机号格式");
        }

        // 检查手机号是否被其他员工使用
        if (employeeDAO.isPhoneExists(newPhone, employeeId)) {
            return ServiceResult.failure("更新失败：该手机号已被其他员工使用");
        }

        employee.setPhone(newPhone);

        if (employeeDAO.updateEmployee(employee)) {
            return ServiceResult.success("手机号更新成功", employee);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    // ==================== 员工角色管理 ====================

    /**
     * 更新员工角色
     *
     * @param employeeId 员工ID
     * @param newRoleId  新角色ID
     * @return 操作结果
     */
    public ServiceResult<Employee> updateEmployeeRole(int employeeId, int newRoleId) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("操作失败：员工不存在");
        }

        if (!employeeDAO.isValidRoleId(newRoleId)) {
            return ServiceResult.failure("操作失败：无效的角色ID");
        }

        if (employee.getRoleId() == newRoleId) {
            return ServiceResult.failure("操作失败：员工已是该角色");
        }

        // 如果从教练转为其他角色，检查是否有负责的课程
        if (employee.getRoleId() == ROLE_TRAINER && newRoleId != ROLE_TRAINER) {
            List<Course> courses = employeeDAO.getCoursesByTrainerId(employeeId);
            if (!courses.isEmpty()) {
                return ServiceResult.failure("操作失败：该教练还有" + courses.size() + "门课程，请先转移课程");
            }
        }

        String oldRoleName = roleDAO.getRoleDisplayName(employee.getRoleId());
        String newRoleName = roleDAO.getRoleDisplayName(newRoleId);

        if (employeeDAO.updateEmployeeRole(employeeId, newRoleId)) {
            employee.setRoleId(newRoleId);
            employee.setEmployeeRole(roleDAO.getRoleById(newRoleId));
            return ServiceResult.success("角色变更成功：" + oldRoleName + " → " + newRoleName, employee);
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败");
        }
    }

    /**
     * 将员工设为教练
     */
    public ServiceResult<Employee> promoteToTrainer(int employeeId) {
        return updateEmployeeRole(employeeId, ROLE_TRAINER);
    }

    /**
     * 将员工设为前台
     */
    public ServiceResult<Employee> promoteToReceptionist(int employeeId) {
        return updateEmployeeRole(employeeId, ROLE_RECEPTIONIST);
    }

    /**
     * 将员工设为管理员
     */
    public ServiceResult<Employee> promoteToAdmin(int employeeId) {
        return updateEmployeeRole(employeeId, ROLE_ADMIN);
    }

    // ==================== 员工离职 ====================

    /**
     * 员工离职（删除员工）
     *
     * @param employeeId 员工ID
     * @param force      是否强制删除（忽略关联数据检查）
     * @return 操作结果
     */
    public ServiceResult<Void> terminate(int employeeId, boolean force) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("操作失败：员工不存在");
        }

        if (!force) {
            // 检查是否有关联的课程
            List<Course> courses = employeeDAO.getCoursesByTrainerId(employeeId);
            if (!courses.isEmpty()) {
                return ServiceResult.failure("操作失败：员工有" + courses.size() + "门关联课程，请先转移或删除课程");
            }
        }

        boolean deleted = force ?
                employeeDAO.forceDeleteEmployee(employeeId) :
                employeeDAO.deleteEmployee(employeeId);

        if (deleted) {
            return ServiceResult.success("员工「" + employee.getName() + "」已离职");
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败，可能存在关联数据");
        }
    }

    /**
     * 员工离职（非强制）
     */
    public ServiceResult<Void> terminate(int employeeId) {
        return terminate(employeeId, false);
    }

    // ==================== 员工查询 ====================

    /**
     * 根据ID查询员工
     *
     * @param employeeId 员工ID
     * @return 员工对象
     */
    public Employee getEmployeeById(int employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }

    /**
     * 根据手机号查询员工
     *
     * @param phone 手机号
     * @return 员工对象
     */
    public Employee getEmployeeByPhone(String phone) {
        return employeeDAO.getEmployeeByPhone(phone);
    }

    /**
     * 查询所有员工
     *
     * @return 员工列表
     */
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    /**
     * 查询所有教练
     *
     * @return 教练列表
     */
    public List<Employee> getTrainers() {
        return employeeDAO.getTrainers();
    }

    /**
     * 查询所有前台
     *
     * @return 前台列表
     */
    public List<Employee> getReceptionists() {
        return employeeDAO.getReceptionists();
    }

    /**
     * 查询所有管理员
     *
     * @return 管理员列表
     */
    public List<Employee> getAdmins() {
        return employeeDAO.getAdmins();
    }

    /**
     * 根据角色ID查询员工
     *
     * @param roleId 角色ID
     * @return 员工列表
     */
    public List<Employee> getEmployeesByRole(int roleId) {
        return employeeDAO.getEmployeesByRoleId(roleId);
    }

    /**
     * 根据姓名模糊搜索员工
     *
     * @param name 姓名关键字
     * @return 员工列表
     */
    public List<Employee> searchByName(String name) {
        return employeeDAO.searchEmployeeByName(name);
    }

    /**
     * 综合搜索员工（姓名或手机号）
     *
     * @param keyword 搜索关键字
     * @return 员工列表
     */
    public List<Employee> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return employeeDAO.getAllEmployees();
        }

        keyword = keyword.trim();

        // 如果是纯数字，按手机号搜索
        if (keyword.matches("\\d+")) {
            Employee employee = employeeDAO.getEmployeeByPhone(keyword);
            if (employee != null) {
                return List.of(employee);
            }
            return List.of();
        }

        // 否则按姓名搜索
        return employeeDAO.searchEmployeeByName(keyword);
    }

    /**
     * 根据入职日期范围查询员工
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 员工列表
     */
    public List<Employee> getEmployeesByHireDateRange(Date startDate, Date endDate) {
        return employeeDAO.getEmployeesByHireDateRange(startDate, endDate);
    }

    // ==================== 员工详情 ====================

    /**
     * 获取员工详细信息（包含角色、课程、统计等）
     *
     * @param employeeId 员工ID
     * @return 员工详情
     */
    public EmployeeDetail getEmployeeDetail(int employeeId) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return null;
        }

        EmployeeDetail detail = new EmployeeDetail();
        detail.setEmployee(employee);

        // 角色信息
        detail.setRole(employee.getEmployeeRole());
        detail.setRoleDisplayName(roleDAO.getRoleDisplayName(employee.getRoleId()));

        // 工龄信息
        detail.setWorkYears(employeeDAO.getWorkYears(employeeId));
        detail.setWorkDays(employeeDAO.getWorkDays(employeeId));
        detail.setWorkDurationString(employeeDAO.getWorkDurationString(employeeId));

        // 教练相关统计（仅对教练有效）
        if (employee.getRoleId() == ROLE_TRAINER) {
            List<Course> courses = employeeDAO.getCoursesByTrainerId(employeeId);
            detail.setCourses(courses);
            detail.setCourseCount(courses.size());
            detail.setTodayBookingCount(employeeDAO.getTodayBookingCount(employeeId));

            // 计算总预约数
            int totalBookings = 0;
            for (Course course : courses) {
                totalBookings += bookingDAO.getBookingsByCourseId(course.getCourseId()).size();
            }
            detail.setTotalBookingCount(totalBookings);
        }

        return detail;
    }

    // ==================== 员工统计 ====================

    /**
     * 获取员工总数
     *
     * @return 员工总数
     */
    public int getTotalEmployeeCount() {
        return employeeDAO.getTotalEmployeeCount();
    }

    /**
     * 获取教练数量
     *
     * @return 教练数量
     */
    public int getTrainerCount() {
        return employeeDAO.getTrainerCount();
    }

    /**
     * 获取前台数量
     *
     * @return 前台数量
     */
    public int getReceptionistCount() {
        return employeeDAO.getReceptionistCount();
    }

    /**
     * 获取管理员数量
     *
     * @return 管理员数量
     */
    public int getAdminCount() {
        return employeeDAO.getAdminCount();
    }

    /**
     * 获取本月新入职员工数量
     *
     * @return 本月新入职数
     */
    public int getMonthlyNewEmployeeCount() {
        return employeeDAO.getMonthlyNewEmployeeCount();
    }

    /**
     * 按角色统计员工数量
     *
     * @return Map<角色ID, 数量>
     */
    public Map<Integer, Integer> getEmployeeCountByRole() {
        Map<Integer, Integer> countMap = new HashMap<>();
        countMap.put(ROLE_TRAINER, employeeDAO.getTrainerCount());
        countMap.put(ROLE_RECEPTIONIST, employeeDAO.getReceptionistCount());
        countMap.put(ROLE_ADMIN, employeeDAO.getAdminCount());
        return countMap;
    }

    /**
     * 获取员工统计概览
     *
     * @return 统计概览
     */
    public EmployeeStatistics getStatistics() {
        EmployeeStatistics stats = new EmployeeStatistics();

        stats.setTotalCount(employeeDAO.getTotalEmployeeCount());
        stats.setTrainerCount(employeeDAO.getTrainerCount());
        stats.setReceptionistCount(employeeDAO.getReceptionistCount());
        stats.setAdminCount(employeeDAO.getAdminCount());
        stats.setMonthlyNewCount(employeeDAO.getMonthlyNewEmployeeCount());

        // 课程统计
        stats.setTotalCourseCount(courseDAO.getTotalCourseCount());

        return stats;
    }

    // ==================== 权限验证 ====================

    /**
     * 验证员工是否存在
     *
     * @param employeeId 员工ID
     * @return 验证结果
     */
    public ServiceResult<Employee> validateEmployee(int employeeId) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("员工不存在");
        }
        return ServiceResult.success("验证通过", employee);
    }

    /**
     * 验证员工是否是教练
     *
     * @param employeeId 员工ID
     * @return 验证结果
     */
    public ServiceResult<Employee> validateTrainer(int employeeId) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("员工不存在");
        }
        if (employee.getRoleId() != ROLE_TRAINER) {
            return ServiceResult.failure("该员工不是教练");
        }
        return ServiceResult.success("验证通过", employee);
    }

    /**
     * 验证员工是否是管理员
     *
     * @param employeeId 员工ID
     * @return 验证结果
     */
    public ServiceResult<Employee> validateAdmin(int employeeId) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("员工不存在");
        }
        if (employee.getRoleId() != ROLE_ADMIN) {
            return ServiceResult.failure("该员工不是管理员");
        }
        return ServiceResult.success("验证通过", employee);
    }

    /**
     * 检查员工是否拥有某个权限
     *
     * @param employeeId 员工ID
     * @param permission 权限名称
     * @return 是否拥有该权限
     */
    public boolean hasPermission(int employeeId, String permission) {
        return employeeDAO.hasPermission(employeeId, permission);
    }

    /**
     * 验证员工是否拥有某个权限
     *
     * @param employeeId 员工ID
     * @param permission 权限名称
     * @return 验证结果
     */
    public ServiceResult<Employee> validatePermission(int employeeId, String permission) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return ServiceResult.failure("员工不存在");
        }
        if (!employeeDAO.hasPermission(employeeId, permission)) {
            return ServiceResult.failure("权限不足：缺少「" + permission + "」权限");
        }
        return ServiceResult.success("验证通过", employee);
    }

    /**
     * 检查员工是否是教练
     *
     * @param employeeId 员工ID
     * @return true表示是教练
     */
    public boolean isTrainer(int employeeId) {
        return employeeDAO.isTrainer(employeeId);
    }

    /**
     * 检查员工是否是前台
     *
     * @param employeeId 员工ID
     * @return true表示是前台
     */
    public boolean isReceptionist(int employeeId) {
        return employeeDAO.isReceptionist(employeeId);
    }

    /**
     * 检查员工是否是管理员
     *
     * @param employeeId 员工ID
     * @return true表示是管理员
     */
    public boolean isAdmin(int employeeId) {
        return employeeDAO.isAdmin(employeeId);
    }

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @return true表示已存在
     */
    public boolean isPhoneExists(String phone) {
        return employeeDAO.isPhoneExists(phone);
    }

    // ==================== 教练相关功能 ====================

    /**
     * 获取教练负责的课程
     *
     * @param trainerId 教练ID
     * @return 课程列表
     */
    public List<Course> getTrainerCourses(int trainerId) {
        return employeeDAO.getCoursesByTrainerId(trainerId);
    }

    /**
     * 获取教练的课程数量
     *
     * @param trainerId 教练ID
     * @return 课程数量
     */
    public int getTrainerCourseCount(int trainerId) {
        return employeeDAO.getCourseCountByTrainer(trainerId);
    }

    /**
     * 获取教练今日预约数
     *
     * @param trainerId 教练ID
     * @return 今日预约数
     */
    public int getTrainerTodayBookingCount(int trainerId) {
        return employeeDAO.getTodayBookingCount(trainerId);
    }

    /**
     * 获取教练的预约列表
     *
     * @param trainerId 教练ID
     * @return 预约列表
     */
    public List<entity.Booking> getTrainerBookings(int trainerId) {
        return bookingDAO.getBookingsByTrainerId(trainerId);
    }

    /**
     * 获取教练今日的预约列表
     *
     * @param trainerId 教练ID
     * @return 今日预约列表
     */
    public List<entity.Booking> getTrainerTodayBookings(int trainerId) {
        return bookingDAO.getTodayBookingsByTrainerId(trainerId);
    }

    // ==================== 工龄相关功能 ====================

    /**
     * 获取员工工龄（年）
     *
     * @param employeeId 员工ID
     * @return 工龄（年）
     */
    public int getWorkYears(int employeeId) {
        return employeeDAO.getWorkYears(employeeId);
    }

    /**
     * 获取员工工龄（天）
     *
     * @param employeeId 员工ID
     * @return 工龄（天）
     */
    public long getWorkDays(int employeeId) {
        return employeeDAO.getWorkDays(employeeId);
    }

    /**
     * 获取员工工龄格式化字符串
     *
     * @param employeeId 员工ID
     * @return 工龄字符串（如"2年3个月"）
     */
    public String getWorkDurationString(int employeeId) {
        return employeeDAO.getWorkDurationString(employeeId);
    }

    // ==================== 角色信息获取 ====================

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    public List<EmployeeRole> getAllRoles() {
        return roleDAO.getAllRoles();
    }

    /**
     * 获取角色中文显示名称
     *
     * @param roleId 角色ID
     * @return 中文显示名称
     */
    public String getRoleDisplayName(int roleId) {
        return roleDAO.getRoleDisplayName(roleId);
    }

    // ==================== 内部类：服务结果 ====================

    /**
     * 服务操作结果
     *
     * @param <T> 数据类型
     */
    public static class ServiceResult<T> {
        private boolean success;
        private String message;
        private T data;

        private ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ServiceResult<T> success(String message, T data) {
            return new ServiceResult<>(true, message, data);
        }

        public static <T> ServiceResult<T> success(String message) {
            return new ServiceResult<>(true, message, null);
        }

        public static <T> ServiceResult<T> failure(String message) {
            return new ServiceResult<>(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }

        @Override
        public String toString() {
            return (success ? "成功" : "失败") + ": " + message;
        }
    }

    // ==================== 内部类：员工详情 ====================

    /**
     * 员工详细信息（包含关联数据和统计）
     */
    public static class EmployeeDetail {
        private Employee employee;
        private EmployeeRole role;
        private String roleDisplayName;
        private int workYears;
        private long workDays;
        private String workDurationString;
        private List<Course> courses;
        private int courseCount;
        private int todayBookingCount;
        private int totalBookingCount;

        // Getters and Setters
        public Employee getEmployee() {
            return employee;
        }

        public void setEmployee(Employee employee) {
            this.employee = employee;
        }

        public EmployeeRole getRole() {
            return role;
        }

        public void setRole(EmployeeRole role) {
            this.role = role;
        }

        public String getRoleDisplayName() {
            return roleDisplayName;
        }

        public void setRoleDisplayName(String roleDisplayName) {
            this.roleDisplayName = roleDisplayName;
        }

        public int getWorkYears() {
            return workYears;
        }

        public void setWorkYears(int workYears) {
            this.workYears = workYears;
        }

        public long getWorkDays() {
            return workDays;
        }

        public void setWorkDays(long workDays) {
            this.workDays = workDays;
        }

        public String getWorkDurationString() {
            return workDurationString;
        }

        public void setWorkDurationString(String workDurationString) {
            this.workDurationString = workDurationString;
        }

        public List<Course> getCourses() {
            return courses;
        }

        public void setCourses(List<Course> courses) {
            this.courses = courses;
        }

        public int getCourseCount() {
            return courseCount;
        }

        public void setCourseCount(int courseCount) {
            this.courseCount = courseCount;
        }

        public int getTodayBookingCount() {
            return todayBookingCount;
        }

        public void setTodayBookingCount(int todayBookingCount) {
            this.todayBookingCount = todayBookingCount;
        }

        public int getTotalBookingCount() {
            return totalBookingCount;
        }

        public void setTotalBookingCount(int totalBookingCount) {
            this.totalBookingCount = totalBookingCount;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 员工详情 ========\n");
            sb.append("基本信息：").append(employee.getBasicInfo()).append("\n");
            sb.append("角色：").append(roleDisplayName).append("\n");
            sb.append("工龄：").append(workDurationString).append("\n");
            sb.append("入职日期：").append(DateUtils.formatDate(employee.getHireDate())).append("\n");

            if (employee.getRoleId() == ROLE_TRAINER) {
                sb.append("\n");
                sb.append("======== 教练信息 ========\n");
                sb.append("负责课程数：").append(courseCount).append("\n");
                sb.append("今日预约数：").append(todayBookingCount).append("\n");
                sb.append("累计预约数：").append(totalBookingCount).append("\n");
                if (courses != null && !courses.isEmpty()) {
                    sb.append("课程列表：\n");
                    for (Course course : courses) {
                        sb.append("  - ").append(course.getName())
                                .append(" (").append(course.getType()).append(")\n");
                    }
                }
            }

            return sb.toString();
        }
    }


    // ==================== 内部类：员工统计 ====================

    /**
     * 员工统计信息
     */
    public static class EmployeeStatistics {
        private int totalCount;
        private int trainerCount;
        private int receptionistCount;
        private int adminCount;
        private int monthlyNewCount;
        private int totalCourseCount;

        // Getters and Setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getTrainerCount() {
            return trainerCount;
        }

        public void setTrainerCount(int trainerCount) {
            this.trainerCount = trainerCount;
        }

        public int getReceptionistCount() {
            return receptionistCount;
        }

        public void setReceptionistCount(int receptionistCount) {
            this.receptionistCount = receptionistCount;
        }

        public int getAdminCount() {
            return adminCount;
        }

        public void setAdminCount(int adminCount) {
            this.adminCount = adminCount;
        }

        public int getMonthlyNewCount() {
            return monthlyNewCount;
        }

        public void setMonthlyNewCount(int monthlyNewCount) {
            this.monthlyNewCount = monthlyNewCount;
        }

        public int getTotalCourseCount() {
            return totalCourseCount;
        }

        public void setTotalCourseCount(int totalCourseCount) {
            this.totalCourseCount = totalCourseCount;
        }

        /**
         * 获取教练占比
         */
        public double getTrainerRate() {
            return totalCount > 0 ? (double) trainerCount / totalCount * 100 : 0;
        }

        /**
         * 获取平均每个教练负责的课程数
         */
        public double getAvgCoursesPerTrainer() {
            return trainerCount > 0 ? (double) totalCourseCount / trainerCount : 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 员工统计 ========\n");
            sb.append("员工总数：").append(totalCount).append("\n");
            sb.append("  - 教练：").append(trainerCount)
                    .append(" (").append(String.format("%.1f", getTrainerRate())).append("%)\n");
            sb.append("  - 前台：").append(receptionistCount).append("\n");
            sb.append("  - 管理员：").append(adminCount).append("\n");
            sb.append("\n");
            sb.append("本月新入职：").append(monthlyNewCount).append("\n");
            sb.append("\n");
            sb.append("课程总数：").append(totalCourseCount).append("\n");
            sb.append("平均每教练课程数：").append(String.format("%.1f", getAvgCoursesPerTrainer())).append("\n");
            return sb.toString();
        }
    }
}

