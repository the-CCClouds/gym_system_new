import dao.EmployeeDAO;
import dao.EmployeeRoleDAO;
import entity.Course;
import entity.Employee;
import entity.EmployeeRole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.DateUtils;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * EmployeeDAO 测试类
 * 
 * 测试前提：
 * - 数据库中存在7个员工（4教练、2前台、1管理员）
 * - 数据库中存在3个角色
 */
public class EmployeeDAOTest {
    
    private EmployeeDAO employeeDAO;
    private int testEmployeeId;  // 用于清理测试数据

    @Before
    public void setUp() {
        employeeDAO = new EmployeeDAO();
        testEmployeeId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testEmployeeId > 0) {
            employeeDAO.forceDeleteEmployee(testEmployeeId);
            testEmployeeId = 0;
        }
    }

    // ==================== 常量测试 ====================

    @Test
    public void testRoleIdConstants() {
        assertEquals(EmployeeRoleDAO.ROLE_ID_TRAINER, EmployeeDAO.ROLE_ID_TRAINER);
        assertEquals(EmployeeRoleDAO.ROLE_ID_RECEPTIONIST, EmployeeDAO.ROLE_ID_RECEPTIONIST);
        assertEquals(EmployeeRoleDAO.ROLE_ID_ADMIN, EmployeeDAO.ROLE_ID_ADMIN);
    }

    // ==================== 基础查询测试 ====================

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = employeeDAO.getAllEmployees();
        assertNotNull(employees);
        assertTrue(employees.size() >= 7);
    }

    @Test
    public void testGetEmployeeById() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);
        assertEquals("张教练", employee.getName());
        assertEquals(EmployeeDAO.ROLE_ID_TRAINER, employee.getRoleId());
        assertEquals("13800001111", employee.getPhone());

        // 验证关联的角色对象
        EmployeeRole role = employee.getEmployeeRole();
        assertNotNull(role);
        assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, role.getRoleName());
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        Employee employee = employeeDAO.getEmployeeById(999);
        assertNull(employee);
    }

    // ==================== 按角色查询测试 ====================

    @Test
    public void testGetTrainers() {
        List<Employee> trainers = employeeDAO.getTrainers();
        assertNotNull(trainers);
        assertTrue(trainers.size() >= 4);

        for (Employee trainer : trainers) {
            assertEquals(EmployeeDAO.ROLE_ID_TRAINER, trainer.getRoleId());
            assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, trainer.getRole());
        }
    }

    @Test
    public void testGetReceptionists() {
        List<Employee> receptionists = employeeDAO.getReceptionists();
        assertNotNull(receptionists);
        assertTrue(receptionists.size() >= 2);

        for (Employee receptionist : receptionists) {
            assertEquals(EmployeeDAO.ROLE_ID_RECEPTIONIST, receptionist.getRoleId());
            assertEquals(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST, receptionist.getRole());
        }
    }

    @Test
    public void testGetAdmins() {
        List<Employee> admins = employeeDAO.getAdmins();
        assertNotNull(admins);
        assertTrue(admins.size() >= 1);

        for (Employee admin : admins) {
            assertEquals(EmployeeDAO.ROLE_ID_ADMIN, admin.getRoleId());
            assertEquals(EmployeeRoleDAO.ROLE_NAME_ADMIN, admin.getRole());
        }
    }

    @Test
    public void testGetEmployeesByRoleId() {
        // 测试获取教练
        List<Employee> trainers = employeeDAO.getEmployeesByRoleId(EmployeeDAO.ROLE_ID_TRAINER);
        assertNotNull(trainers);
        assertTrue(trainers.size() > 0);

        // 测试获取前台
        List<Employee> receptionists = employeeDAO.getEmployeesByRoleId(EmployeeDAO.ROLE_ID_RECEPTIONIST);
        assertNotNull(receptionists);
        assertTrue(receptionists.size() > 0);

        // 测试获取管理员
        List<Employee> admins = employeeDAO.getEmployeesByRoleId(EmployeeDAO.ROLE_ID_ADMIN);
        assertNotNull(admins);
        assertTrue(admins.size() > 0);
    }

    // ==================== 业务查询测试 ====================

    @Test
    public void testGetEmployeeByPhone() {
        Employee employee = employeeDAO.getEmployeeByPhone("13800001111");
        assertNotNull(employee);
        assertEquals("张教练", employee.getName());
        assertEquals(EmployeeDAO.ROLE_ID_TRAINER, employee.getRoleId());
    }

    @Test
    public void testGetEmployeeByPhoneNotFound() {
        Employee employee = employeeDAO.getEmployeeByPhone("00000000000");
        assertNull(employee);
    }

    @Test
    public void testGetEmployeeByPhoneNull() {
        assertNull(employeeDAO.getEmployeeByPhone(null));
        assertNull(employeeDAO.getEmployeeByPhone(""));
    }

    @Test
    public void testSearchEmployeeByName() {
        List<Employee> employees = employeeDAO.searchEmployeeByName("教练");
        assertNotNull(employees);
        assertTrue(employees.size() >= 4);

        for (Employee employee : employees) {
            assertTrue(employee.getName().contains("教练"));
        }
    }

    @Test
    public void testSearchEmployeeByNameNotFound() {
        List<Employee> employees = employeeDAO.searchEmployeeByName("不存在的名字999");
        assertNotNull(employees);
        assertEquals(0, employees.size());
    }

    @Test
    public void testSearchEmployeeByNameEmpty() {
        List<Employee> employees = employeeDAO.searchEmployeeByName("");
        assertNotNull(employees);
        assertEquals(0, employees.size());
    }

    @Test
    public void testGetEmployeesByHireDateRange() {
        // 查询2023年入职的员工
        Date startDate = DateUtils.parseDate("2023-01-01");
        Date endDate = DateUtils.parseDate("2023-12-31");
        
        List<Employee> employees = employeeDAO.getEmployeesByHireDateRange(startDate, endDate);
        assertNotNull(employees);
        // 根据测试数据，可能有员工在这个范围内
    }

    @Test
    public void testGetEmployeesByHireDateRangeInvalid() {
        // 结束日期早于开始日期
        Date startDate = DateUtils.parseDate("2023-12-31");
        Date endDate = DateUtils.parseDate("2023-01-01");
        
        List<Employee> employees = employeeDAO.getEmployeesByHireDateRange(startDate, endDate);
        assertNotNull(employees);
        assertEquals(0, employees.size());
    }

    @Test
    public void testGetCoursesByTrainerId() {
        // 张教练(ID=1)负责多个瑜伽课程
        List<Course> courses = employeeDAO.getCoursesByTrainerId(1);
        assertNotNull(courses);
        assertTrue(courses.size() > 0);

        for (Course course : courses) {
            assertEquals(1, course.getEmployeeId());
        }
    }

    @Test
    public void testGetCoursesByTrainerIdNotFound() {
        List<Course> courses = employeeDAO.getCoursesByTrainerId(999);
        assertNotNull(courses);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetTodayBookingCount() {
        // 测试今日预约数（可能为0，取决于测试数据）
        int count = employeeDAO.getTodayBookingCount(1);
        assertTrue(count >= 0);
    }

    @Test
    public void testGetCourseCountByTrainer() {
        int count = employeeDAO.getCourseCountByTrainer(1);
        assertTrue(count >= 0);
        
        // 应该与getCoursesByTrainerId返回的列表大小一致
        List<Course> courses = employeeDAO.getCoursesByTrainerId(1);
        assertEquals(courses.size(), count);
    }

    // ==================== 添加员工测试 ====================

    @Test
    public void testAddEmployee() {
        Employee employee = new Employee();
        employee.setName("测试员工");
        employee.setRoleId(EmployeeDAO.ROLE_ID_TRAINER);
        employee.setPhone("13899998888");
        employee.setHireDate(DateUtils.now());

        assertTrue(employeeDAO.addEmployee(employee));
        assertTrue(employee.getId() > 0);
        testEmployeeId = employee.getId();

        // 验证添加成功
        Employee added = employeeDAO.getEmployeeById(employee.getId());
        assertNotNull(added);
        assertEquals("测试员工", added.getName());
    }

    @Test
    public void testAddEmployeeNull() {
        assertFalse(employeeDAO.addEmployee(null));
    }

    @Test
    public void testAddEmployeeEmptyName() {
        Employee employee = new Employee();
        employee.setName("");
        employee.setRoleId(EmployeeDAO.ROLE_ID_TRAINER);
        employee.setPhone("13899997777");
        employee.setHireDate(DateUtils.now());

        assertFalse(employeeDAO.addEmployee(employee));
    }

    @Test
    public void testAddEmployeeInvalidPhone() {
        Employee employee = new Employee();
        employee.setName("无效手机号员工");
        employee.setRoleId(EmployeeDAO.ROLE_ID_TRAINER);
        employee.setPhone("123");  // 无效手机号
        employee.setHireDate(DateUtils.now());

        assertFalse(employeeDAO.addEmployee(employee));
    }

    @Test
    public void testAddEmployeeInvalidRoleId() {
        Employee employee = new Employee();
        employee.setName("无效角色员工");
        employee.setRoleId(99);  // 无效角色ID
        employee.setPhone("13899997777");
        employee.setHireDate(DateUtils.now());

        assertFalse(employeeDAO.addEmployee(employee));
    }

    @Test
    public void testAddEmployeeDuplicatePhone() {
        // 尝试添加已存在手机号的员工
        Employee employee = new Employee();
        employee.setName("重复手机号员工");
        employee.setRoleId(EmployeeDAO.ROLE_ID_TRAINER);
        employee.setPhone("13800001111");  // 已存在的手机号
        employee.setHireDate(DateUtils.now());

        assertFalse(employeeDAO.addEmployee(employee));
    }

    // ==================== 更新员工测试 ====================

    @Test
    public void testUpdateEmployee() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        String originalName = employee.getName();
        String originalPhone = employee.getPhone();

        employee.setName("张教练updated");
        employee.setPhone("13811112222");

        assertTrue(employeeDAO.updateEmployee(employee));

        Employee updated = employeeDAO.getEmployeeById(1);
        assertEquals("张教练updated", updated.getName());
        assertEquals("13811112222", updated.getPhone());

        // 恢复原始数据
        employee.setName(originalName);
        employee.setPhone(originalPhone);
        employeeDAO.updateEmployee(employee);
    }

    @Test
    public void testUpdateEmployeeNull() {
        assertFalse(employeeDAO.updateEmployee(null));
    }

    @Test
    public void testUpdateEmployeeEmptyName() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        String originalName = employee.getName();
        employee.setName("");

        assertFalse(employeeDAO.updateEmployee(employee));

        // 恢复
        employee.setName(originalName);
    }

    @Test
    public void testUpdateEmployeeInvalidPhone() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        String originalPhone = employee.getPhone();
        employee.setPhone("123");  // 无效手机号

        assertFalse(employeeDAO.updateEmployee(employee));

        // 恢复
        employee.setPhone(originalPhone);
    }

    @Test
    public void testUpdateEmployeeRole() {
        // 先添加一个测试员工
        Employee employee = new Employee();
        employee.setName("角色更新测试员工");
        employee.setRoleId(EmployeeDAO.ROLE_ID_TRAINER);
        employee.setPhone("13866664444");
        employee.setHireDate(DateUtils.now());

        assertTrue(employeeDAO.addEmployee(employee));
        testEmployeeId = employee.getId();

        // 更新角色
        assertTrue(employeeDAO.updateEmployeeRole(testEmployeeId, EmployeeDAO.ROLE_ID_RECEPTIONIST));

        // 验证更新成功
        Employee updated = employeeDAO.getEmployeeById(testEmployeeId);
        assertEquals(EmployeeDAO.ROLE_ID_RECEPTIONIST, updated.getRoleId());
    }

    @Test
    public void testUpdateEmployeeRoleInvalid() {
        assertFalse(employeeDAO.updateEmployeeRole(1, 99));  // 无效角色ID
    }

    // ==================== 删除员工测试 ====================

    @Test
    public void testDeleteEmployee() {
        // 先添加一个测试员工
        Employee employee = new Employee();
        employee.setName("待删除员工");
        employee.setRoleId(EmployeeDAO.ROLE_ID_RECEPTIONIST);  // 前台，没有课程关联
        employee.setPhone("13866665555");
        employee.setHireDate(DateUtils.now());

        assertTrue(employeeDAO.addEmployee(employee));
        int employeeId = employee.getId();

        // 删除员工
        assertTrue(employeeDAO.forceDeleteEmployee(employeeId));

        // 验证已删除
        assertNull(employeeDAO.getEmployeeById(employeeId));
    }

    @Test
    public void testDeleteEmployeeWithCourses() {
        // 尝试删除有课程的教练，应该失败
        // 注意：这取决于数据库中的数据，如果教练有课程则会失败
        Employee trainer = employeeDAO.getEmployeeById(1);
        assertNotNull(trainer);
        
        List<Course> courses = employeeDAO.getCoursesByTrainerId(1);
        if (!courses.isEmpty()) {
            assertFalse(employeeDAO.deleteEmployee(1));
        }
    }

    @Test
    public void testForceDeleteEmployee() {
        // 先添加一个测试员工
        Employee employee = new Employee();
        employee.setName("强制删除测试员工");
        employee.setRoleId(EmployeeDAO.ROLE_ID_TRAINER);
        employee.setPhone("13866663333");
        employee.setHireDate(DateUtils.now());

        assertTrue(employeeDAO.addEmployee(employee));
        int employeeId = employee.getId();

        // 强制删除
        assertTrue(employeeDAO.forceDeleteEmployee(employeeId));
        assertNull(employeeDAO.getEmployeeById(employeeId));
    }

    // ==================== 权限检查测试 ====================

    @Test
    public void testHasPermission() {
        // 测试教练权限
        assertTrue(employeeDAO.hasPermission(1, "course_view"));
        assertTrue(employeeDAO.hasPermission(1, "member_view"));
        assertFalse(employeeDAO.hasPermission(1, "member_add"));

        // 测试前台权限（假设ID=5是前台）
        Employee receptionist = employeeDAO.getReceptionists().get(0);
        assertTrue(employeeDAO.hasPermission(receptionist.getId(), "member_add"));
        assertTrue(employeeDAO.hasPermission(receptionist.getId(), "checkin_manage"));

        // 测试管理员权限（假设ID=7是管理员）
        Employee admin = employeeDAO.getAdmins().get(0);
        assertTrue(employeeDAO.hasPermission(admin.getId(), "employee_delete"));
        assertTrue(employeeDAO.hasPermission(admin.getId(), "member_delete"));
    }

    @Test
    public void testHasPermissionInvalidEmployee() {
        assertFalse(employeeDAO.hasPermission(999, "member_view"));
    }

    @Test
    public void testIsTrainer() {
        assertTrue(employeeDAO.isTrainer(1));  // 张教练
        assertFalse(employeeDAO.isTrainer(999));
    }

    @Test
    public void testIsReceptionist() {
        Employee receptionist = employeeDAO.getReceptionists().get(0);
        assertTrue(employeeDAO.isReceptionist(receptionist.getId()));
        assertFalse(employeeDAO.isReceptionist(1));  // 教练不是前台
    }

    @Test
    public void testIsAdmin() {
        Employee admin = employeeDAO.getAdmins().get(0);
        assertTrue(employeeDAO.isAdmin(admin.getId()));
        assertFalse(employeeDAO.isAdmin(1));  // 教练不是管理员
    }

    // ==================== 统计功能测试 ====================

    @Test
    public void testGetTotalEmployeeCount() {
        int count = employeeDAO.getTotalEmployeeCount();
        assertTrue(count >= 7);
        
        // 应该与getAllEmployees返回的列表大小一致
        List<Employee> employees = employeeDAO.getAllEmployees();
        assertEquals(employees.size(), count);
    }

    @Test
    public void testGetEmployeeCountByRole() {
        int trainerCount = employeeDAO.getEmployeeCountByRole(EmployeeDAO.ROLE_ID_TRAINER);
        assertTrue(trainerCount >= 4);

        int receptionistCount = employeeDAO.getEmployeeCountByRole(EmployeeDAO.ROLE_ID_RECEPTIONIST);
        assertTrue(receptionistCount >= 2);

        int adminCount = employeeDAO.getEmployeeCountByRole(EmployeeDAO.ROLE_ID_ADMIN);
        assertTrue(adminCount >= 1);
    }

    @Test
    public void testGetTrainerCount() {
        int count = employeeDAO.getTrainerCount();
        assertTrue(count >= 4);
        assertEquals(employeeDAO.getEmployeeCountByRole(EmployeeDAO.ROLE_ID_TRAINER), count);
    }

    @Test
    public void testGetReceptionistCount() {
        int count = employeeDAO.getReceptionistCount();
        assertTrue(count >= 2);
        assertEquals(employeeDAO.getEmployeeCountByRole(EmployeeDAO.ROLE_ID_RECEPTIONIST), count);
    }

    @Test
    public void testGetAdminCount() {
        int count = employeeDAO.getAdminCount();
        assertTrue(count >= 1);
        assertEquals(employeeDAO.getEmployeeCountByRole(EmployeeDAO.ROLE_ID_ADMIN), count);
    }

    @Test
    public void testGetMonthlyNewEmployeeCount() {
        int count = employeeDAO.getMonthlyNewEmployeeCount();
        assertTrue(count >= 0);
    }

    // ==================== 工龄计算测试 ====================

    @Test
    public void testGetWorkYears() {
        int years = employeeDAO.getWorkYears(1);
        assertTrue(years >= 0);
    }

    @Test
    public void testGetWorkYearsInvalidEmployee() {
        int years = employeeDAO.getWorkYears(999);
        assertEquals(-1, years);
    }

    @Test
    public void testGetWorkDays() {
        long days = employeeDAO.getWorkDays(1);
        assertTrue(days >= 0);
    }

    @Test
    public void testGetWorkDaysInvalidEmployee() {
        long days = employeeDAO.getWorkDays(999);
        assertEquals(-1, days);
    }

    @Test
    public void testGetWorkDurationString() {
        String duration = employeeDAO.getWorkDurationString(1);
        assertNotNull(duration);
        // 应该包含年、月或天
        assertTrue(duration.contains("年") || duration.contains("月") || duration.contains("天"));
    }

    @Test
    public void testGetWorkDurationStringInvalidEmployee() {
        String duration = employeeDAO.getWorkDurationString(999);
        assertNull(duration);
    }

    // ==================== 验证功能测试 ====================

    @Test
    public void testIsValidPhone() {
        assertTrue(employeeDAO.isValidPhone("13800001111"));
        assertTrue(employeeDAO.isValidPhone("15912345678"));
        assertTrue(employeeDAO.isValidPhone("18888888888"));
        
        assertFalse(employeeDAO.isValidPhone("123"));
        assertFalse(employeeDAO.isValidPhone("12345678901"));  // 不以1开头
        assertFalse(employeeDAO.isValidPhone("1380000111"));   // 少一位
        assertFalse(employeeDAO.isValidPhone("138000011111")); // 多一位
        assertFalse(employeeDAO.isValidPhone(null));
        assertFalse(employeeDAO.isValidPhone(""));
    }

    @Test
    public void testIsValidRoleId() {
        assertTrue(employeeDAO.isValidRoleId(EmployeeDAO.ROLE_ID_TRAINER));
        assertTrue(employeeDAO.isValidRoleId(EmployeeDAO.ROLE_ID_RECEPTIONIST));
        assertTrue(employeeDAO.isValidRoleId(EmployeeDAO.ROLE_ID_ADMIN));
        assertFalse(employeeDAO.isValidRoleId(99));
        assertFalse(employeeDAO.isValidRoleId(0));
    }

    @Test
    public void testIsPhoneExists() {
        assertTrue(employeeDAO.isPhoneExists("13800001111"));  // 已存在
        assertFalse(employeeDAO.isPhoneExists("13999999999")); // 不存在
    }

    @Test
    public void testIsPhoneExistsExcludeEmployee() {
        // 员工1的手机号，排除员工1后不应该存在
        assertFalse(employeeDAO.isPhoneExists("13800001111", 1));
        // 员工1的手机号，排除其他员工后应该存在
        assertTrue(employeeDAO.isPhoneExists("13800001111", 2));
    }

    @Test
    public void testIsEmployeeExists() {
        assertTrue(employeeDAO.isEmployeeExists(1));
        assertFalse(employeeDAO.isEmployeeExists(999));
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testGetRoleDisplayName() {
        assertEquals("教练", employeeDAO.getRoleDisplayName(EmployeeDAO.ROLE_ID_TRAINER));
        assertEquals("前台", employeeDAO.getRoleDisplayName(EmployeeDAO.ROLE_ID_RECEPTIONIST));
        assertEquals("管理员", employeeDAO.getRoleDisplayName(EmployeeDAO.ROLE_ID_ADMIN));
        assertEquals("未知", employeeDAO.getRoleDisplayName(99));
    }

    @Test
    public void testGetTrainerRole() {
        EmployeeRole role = employeeDAO.getTrainerRole();
        assertNotNull(role);
        assertEquals(EmployeeDAO.ROLE_ID_TRAINER, role.getRoleId());
        assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, role.getRoleName());
    }

    @Test
    public void testGetReceptionistRole() {
        EmployeeRole role = employeeDAO.getReceptionistRole();
        assertNotNull(role);
        assertEquals(EmployeeDAO.ROLE_ID_RECEPTIONIST, role.getRoleId());
        assertEquals(EmployeeRoleDAO.ROLE_NAME_RECEPTIONIST, role.getRoleName());
    }

    @Test
    public void testGetAdminRole() {
        EmployeeRole role = employeeDAO.getAdminRole();
        assertNotNull(role);
        assertEquals(EmployeeDAO.ROLE_ID_ADMIN, role.getRoleId());
        assertEquals(EmployeeRoleDAO.ROLE_NAME_ADMIN, role.getRoleName());
    }

    // ==================== 实体类关联测试 ====================

    @Test
    public void testEmployeeRoleRelation() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        // 测试 getRole() 方法返回角色名称
        assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, employee.getRole());

        // 测试关联的 EmployeeRole 对象
        EmployeeRole role = employee.getEmployeeRole();
        assertNotNull(role);
        assertEquals(EmployeeDAO.ROLE_ID_TRAINER, role.getRoleId());
        assertEquals(EmployeeRoleDAO.ROLE_NAME_TRAINER, role.getRoleName());
        assertNotNull(role.getPermissions());
    }

    @Test
    public void testEmployeeBasicInfo() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        String basicInfo = employee.getBasicInfo();
        assertNotNull(basicInfo);
        assertTrue(basicInfo.contains("张教练"));
        assertTrue(basicInfo.contains(EmployeeRoleDAO.ROLE_NAME_TRAINER));
        assertTrue(basicInfo.contains("13800001111"));
    }

    @Test
    public void testEmployeeToString() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        String str = employee.toString();
        assertNotNull(str);
        assertTrue(str.contains("employeeId"));
        assertTrue(str.contains("name"));
        assertTrue(str.contains("roleId"));
    }
}
