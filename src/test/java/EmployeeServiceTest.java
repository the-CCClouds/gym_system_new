import dao.EmployeeDAO;
import dao.EmployeeRoleDAO;
import entity.Course;
import entity.Employee;
import entity.EmployeeRole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.EmployeeService;
import service.EmployeeService.EmployeeDetail;
import service.EmployeeService.EmployeeStatistics;
import service.EmployeeService.ServiceResult;
import utils.DateUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * EmployeeService 测试类
 * 
 * 测试前提：
 * - 数据库中存在员工数据
 * - 员工ID=1的教练存在
 */
public class EmployeeServiceTest {

    private EmployeeService employeeService;
    private EmployeeDAO employeeDAO;
    private EmployeeRoleDAO roleDAO;
    private int testEmployeeId;  // 用于清理测试数据

    @Before
    public void setUp() {
        employeeService = new EmployeeService();
        employeeDAO = new EmployeeDAO();
        roleDAO = new EmployeeRoleDAO();
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

    // ==================== 员工入职测试 ====================

    @Test
    public void testHire() {
        ServiceResult<Employee> result = employeeService.hire(
                "测试员工Service",
                "13811112222",
                EmployeeService.ROLE_TRAINER,
                DateUtils.now()
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("测试员工Service", result.getData().getName());
        assertEquals(EmployeeService.ROLE_TRAINER, result.getData().getRoleId());

        testEmployeeId = result.getData().getId();
    }

    @Test
    public void testHireWithDefaultDate() {
        ServiceResult<Employee> result = employeeService.hire(
                "测试员工默认日期",
                "13811112223",
                EmployeeService.ROLE_RECEPTIONIST
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getHireDate());

        testEmployeeId = result.getData().getId();
    }

    @Test
    public void testHireWithEmptyName() {
        ServiceResult<Employee> result = employeeService.hire(
                "",
                "13811112224",
                EmployeeService.ROLE_TRAINER,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("姓名不能为空"));
    }

    @Test
    public void testHireWithInvalidPhone() {
        ServiceResult<Employee> result = employeeService.hire(
                "测试员工",
                "123",
                EmployeeService.ROLE_TRAINER,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("手机号"));
    }

    @Test
    public void testHireWithInvalidRoleId() {
        ServiceResult<Employee> result = employeeService.hire(
                "测试员工",
                "13811112225",
                99,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("角色ID"));
    }

    @Test
    public void testHireWithDuplicatePhone() {
        // 先创建一个员工
        ServiceResult<Employee> first = employeeService.hire(
                "第一个员工",
                "13811119999",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(first.isSuccess());
        testEmployeeId = first.getData().getId();

        // 使用相同手机号创建第二个员工
        ServiceResult<Employee> second = employeeService.hire(
                "第二个员工",
                "13811119999",
                EmployeeService.ROLE_RECEPTIONIST
        );

        assertFalse(second.isSuccess());
        assertTrue(second.getMessage().contains("手机号已被使用"));
    }

    @Test
    public void testHireTrainer() {
        ServiceResult<Employee> result = employeeService.hireTrainer(
                "测试教练",
                "13811113333"
        );

        assertTrue(result.isSuccess());
        assertEquals(EmployeeService.ROLE_TRAINER, result.getData().getRoleId());
        assertTrue(result.getMessage().contains("教练"));

        testEmployeeId = result.getData().getId();
    }

    @Test
    public void testHireReceptionist() {
        ServiceResult<Employee> result = employeeService.hireReceptionist(
                "测试前台",
                "13811114444"
        );

        assertTrue(result.isSuccess());
        assertEquals(EmployeeService.ROLE_RECEPTIONIST, result.getData().getRoleId());
        assertTrue(result.getMessage().contains("前台"));

        testEmployeeId = result.getData().getId();
    }

    @Test
    public void testHireAdmin() {
        ServiceResult<Employee> result = employeeService.hireAdmin(
                "测试管理员",
                "13811115555"
        );

        assertTrue(result.isSuccess());
        assertEquals(EmployeeService.ROLE_ADMIN, result.getData().getRoleId());
        assertTrue(result.getMessage().contains("管理员"));

        testEmployeeId = result.getData().getId();
    }

    // ==================== 员工信息管理测试 ====================

    @Test
    public void testUpdateEmployeeInfo() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "待更新员工",
                "13811116666",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        // 更新信息
        ServiceResult<Employee> updateResult = employeeService.updateEmployeeInfo(
                testEmployeeId,
                "已更新员工",
                "13899998888"
        );

        assertTrue(updateResult.isSuccess());
        assertEquals("已更新员工", updateResult.getData().getName());
        assertEquals("13899998888", updateResult.getData().getPhone());
    }

    @Test
    public void testUpdateEmployeeInfoNotFound() {
        ServiceResult<Employee> result = employeeService.updateEmployeeInfo(
                99999,
                "测试",
                "13800001111"
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("员工不存在"));
    }

    @Test
    public void testUpdateEmployeeInfoWithEmptyName() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "测试员工",
                "13811117777",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        ServiceResult<Employee> result = employeeService.updateEmployeeInfo(
                testEmployeeId,
                "",
                "13899997777"
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("姓名不能为空"));
    }

    @Test
    public void testUpdateEmployeePhone() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "手机更新员工",
                "13811118888",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        // 更新手机号
        ServiceResult<Employee> updateResult = employeeService.updateEmployeePhone(
                testEmployeeId,
                "13899996666"
        );

        assertTrue(updateResult.isSuccess());
        assertEquals("13899996666", updateResult.getData().getPhone());
    }

    @Test
    public void testUpdateEmployeePhoneInvalid() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "测试员工",
                "13811110000",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        ServiceResult<Employee> result = employeeService.updateEmployeePhone(testEmployeeId, "123");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("手机号"));
    }

    // ==================== 员工角色管理测试 ====================

    @Test
    public void testUpdateEmployeeRole() {
        // 先创建测试员工（教练）
        ServiceResult<Employee> hireResult = employeeService.hireTrainer(
                "角色变更员工",
                "13811220000"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        // 变更为前台
        ServiceResult<Employee> updateResult = employeeService.updateEmployeeRole(
                testEmployeeId,
                EmployeeService.ROLE_RECEPTIONIST
        );

        assertTrue(updateResult.isSuccess());
        assertEquals(EmployeeService.ROLE_RECEPTIONIST, updateResult.getData().getRoleId());
        assertTrue(updateResult.getMessage().contains("教练"));
        assertTrue(updateResult.getMessage().contains("前台"));
    }

    @Test
    public void testUpdateEmployeeRoleNotFound() {
        ServiceResult<Employee> result = employeeService.updateEmployeeRole(99999, EmployeeService.ROLE_ADMIN);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("员工不存在"));
    }

    @Test
    public void testUpdateEmployeeRoleSameRole() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hireTrainer(
                "同角色员工",
                "13811221111"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        // 尝试设置相同角色
        ServiceResult<Employee> result = employeeService.updateEmployeeRole(
                testEmployeeId,
                EmployeeService.ROLE_TRAINER
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("已是该角色"));
    }

    @Test
    public void testPromoteToTrainer() {
        // 先创建前台
        ServiceResult<Employee> hireResult = employeeService.hireReceptionist(
                "晋升教练员工",
                "13811222222"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        // 晋升为教练
        ServiceResult<Employee> result = employeeService.promoteToTrainer(testEmployeeId);
        assertTrue(result.isSuccess());
        assertEquals(EmployeeService.ROLE_TRAINER, result.getData().getRoleId());
    }

    @Test
    public void testPromoteToAdmin() {
        // 先创建教练
        ServiceResult<Employee> hireResult = employeeService.hireTrainer(
                "晋升管理员员工",
                "13811223333"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        // 晋升为管理员
        ServiceResult<Employee> result = employeeService.promoteToAdmin(testEmployeeId);
        assertTrue(result.isSuccess());
        assertEquals(EmployeeService.ROLE_ADMIN, result.getData().getRoleId());
    }

    // ==================== 员工离职测试 ====================

    @Test
    public void testTerminate() {
        // 创建测试员工（无关联数据）
        ServiceResult<Employee> hireResult = employeeService.hire(
                "待离职员工",
                "13811224444",
                EmployeeService.ROLE_RECEPTIONIST
        );
        assertTrue(hireResult.isSuccess());
        int employeeId = hireResult.getData().getId();

        // 离职
        ServiceResult<Void> terminateResult = employeeService.terminate(employeeId);
        assertTrue(terminateResult.isSuccess());
        assertTrue(terminateResult.getMessage().contains("离职"));

        // 验证已删除
        assertNull(employeeDAO.getEmployeeById(employeeId));

        testEmployeeId = 0;  // 已删除，不需要清理
    }

    @Test
    public void testTerminateNotFound() {
        ServiceResult<Void> result = employeeService.terminate(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("员工不存在"));
    }

    // ==================== 员工查询测试 ====================

    @Test
    public void testGetEmployeeById() {
        Employee employee = employeeService.getEmployeeById(1);
        assertNotNull(employee);
        assertEquals(1, employee.getId());
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        Employee employee = employeeService.getEmployeeById(99999);
        assertNull(employee);
    }

    @Test
    public void testGetEmployeeByPhone() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "手机查询员工",
                "13811225555",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        Employee employee = employeeService.getEmployeeByPhone("13811225555");
        assertNotNull(employee);
        assertEquals("手机查询员工", employee.getName());
    }

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        assertNotNull(employees);
        assertTrue(employees.size() > 0);
    }

    @Test
    public void testGetTrainers() {
        List<Employee> trainers = employeeService.getTrainers();
        assertNotNull(trainers);
        for (Employee trainer : trainers) {
            assertEquals(EmployeeService.ROLE_TRAINER, trainer.getRoleId());
        }
    }

    @Test
    public void testGetReceptionists() {
        List<Employee> receptionists = employeeService.getReceptionists();
        assertNotNull(receptionists);
        for (Employee receptionist : receptionists) {
            assertEquals(EmployeeService.ROLE_RECEPTIONIST, receptionist.getRoleId());
        }
    }

    @Test
    public void testGetAdmins() {
        List<Employee> admins = employeeService.getAdmins();
        assertNotNull(admins);
        for (Employee admin : admins) {
            assertEquals(EmployeeService.ROLE_ADMIN, admin.getRoleId());
        }
    }

    @Test
    public void testGetEmployeesByRole() {
        List<Employee> trainers = employeeService.getEmployeesByRole(EmployeeService.ROLE_TRAINER);
        assertNotNull(trainers);
        for (Employee trainer : trainers) {
            assertEquals(EmployeeService.ROLE_TRAINER, trainer.getRoleId());
        }
    }

    @Test
    public void testSearchByName() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "搜索测试员工",
                "13811226666",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        List<Employee> employees = employeeService.searchByName("搜索测试");
        assertNotNull(employees);
        assertTrue(employees.size() > 0);
        for (Employee employee : employees) {
            assertTrue(employee.getName().contains("搜索测试"));
        }
    }

    @Test
    public void testSearch() {
        // 先创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "综合搜索员工",
                "13811227777",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        // 按姓名搜索
        List<Employee> nameResults = employeeService.search("综合搜索");
        assertNotNull(nameResults);
        assertTrue(nameResults.size() > 0);

        // 按手机号搜索
        List<Employee> phoneResults = employeeService.search("13811227777");
        assertNotNull(phoneResults);
        assertTrue(phoneResults.size() > 0);

        // 空搜索返回所有
        List<Employee> allResults = employeeService.search("");
        assertNotNull(allResults);
        assertEquals(employeeService.getAllEmployees().size(), allResults.size());
    }

    // ==================== 员工详情测试 ====================

    @Test
    public void testGetEmployeeDetail() {
        EmployeeDetail detail = employeeService.getEmployeeDetail(1);
        assertNotNull(detail);
        assertNotNull(detail.getEmployee());
        assertEquals(1, detail.getEmployee().getId());
        assertNotNull(detail.getRoleDisplayName());

        // 打印详情
        System.out.println(detail.toString());
    }

    @Test
    public void testGetEmployeeDetailNotFound() {
        EmployeeDetail detail = employeeService.getEmployeeDetail(99999);
        assertNull(detail);
    }

    // ==================== 员工统计测试 ====================

    @Test
    public void testGetTotalEmployeeCount() {
        int count = employeeService.getTotalEmployeeCount();
        assertTrue(count > 0);
    }

    @Test
    public void testGetTrainerCount() {
        int count = employeeService.getTrainerCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetReceptionistCount() {
        int count = employeeService.getReceptionistCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetAdminCount() {
        int count = employeeService.getAdminCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMonthlyNewEmployeeCount() {
        int count = employeeService.getMonthlyNewEmployeeCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetEmployeeCountByRole() {
        Map<Integer, Integer> countMap = employeeService.getEmployeeCountByRole();
        assertNotNull(countMap);
        assertTrue(countMap.containsKey(EmployeeService.ROLE_TRAINER));
        assertTrue(countMap.containsKey(EmployeeService.ROLE_RECEPTIONIST));
        assertTrue(countMap.containsKey(EmployeeService.ROLE_ADMIN));
    }

    @Test
    public void testGetStatistics() {
        EmployeeStatistics stats = employeeService.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalCount() > 0);
        assertTrue(stats.getTrainerRate() >= 0 && stats.getTrainerRate() <= 100);
        assertTrue(stats.getAvgCoursesPerTrainer() >= 0);

        // 打印统计
        System.out.println(stats.toString());
    }

    // ==================== 权限验证测试 ====================

    @Test
    public void testValidateEmployee() {
        ServiceResult<Employee> result = employeeService.validateEmployee(1);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    public void testValidateEmployeeNotFound() {
        ServiceResult<Employee> result = employeeService.validateEmployee(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testValidateTrainer() {
        // 假设ID=1是教练
        Employee employee = employeeDAO.getEmployeeById(1);
        if (employee != null && employee.getRoleId() == EmployeeService.ROLE_TRAINER) {
            ServiceResult<Employee> result = employeeService.validateTrainer(1);
            assertTrue(result.isSuccess());
        }
    }

    @Test
    public void testValidateTrainerNotTrainer() {
        // 创建前台
        ServiceResult<Employee> hireResult = employeeService.hireReceptionist(
                "非教练员工",
                "13811228888"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        ServiceResult<Employee> result = employeeService.validateTrainer(testEmployeeId);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不是教练"));
    }

    @Test
    public void testValidateAdmin() {
        // 创建管理员
        ServiceResult<Employee> hireResult = employeeService.hireAdmin(
                "管理员验证员工",
                "13811229999"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        ServiceResult<Employee> result = employeeService.validateAdmin(testEmployeeId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testValidateAdminNotAdmin() {
        // 创建教练
        ServiceResult<Employee> hireResult = employeeService.hireTrainer(
                "非管理员员工",
                "13811230000"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        ServiceResult<Employee> result = employeeService.validateAdmin(testEmployeeId);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不是管理员"));
    }

    @Test
    public void testIsTrainer() {
        // 创建教练
        ServiceResult<Employee> hireResult = employeeService.hireTrainer(
                "教练检查员工",
                "13811231111"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        assertTrue(employeeService.isTrainer(testEmployeeId));
        assertFalse(employeeService.isReceptionist(testEmployeeId));
        assertFalse(employeeService.isAdmin(testEmployeeId));
    }

    @Test
    public void testIsReceptionist() {
        // 创建前台
        ServiceResult<Employee> hireResult = employeeService.hireReceptionist(
                "前台检查员工",
                "13811232222"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        assertFalse(employeeService.isTrainer(testEmployeeId));
        assertTrue(employeeService.isReceptionist(testEmployeeId));
        assertFalse(employeeService.isAdmin(testEmployeeId));
    }

    @Test
    public void testIsAdmin() {
        // 创建管理员
        ServiceResult<Employee> hireResult = employeeService.hireAdmin(
                "管理员检查员工",
                "13811233333"
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        assertFalse(employeeService.isTrainer(testEmployeeId));
        assertFalse(employeeService.isReceptionist(testEmployeeId));
        assertTrue(employeeService.isAdmin(testEmployeeId));
    }

    @Test
    public void testIsPhoneExists() {
        // 创建测试员工
        ServiceResult<Employee> hireResult = employeeService.hire(
                "手机存在检查员工",
                "13811234444",
                EmployeeService.ROLE_TRAINER
        );
        assertTrue(hireResult.isSuccess());
        testEmployeeId = hireResult.getData().getId();

        assertTrue(employeeService.isPhoneExists("13811234444"));
        assertFalse(employeeService.isPhoneExists("99999999999"));
    }

    // ==================== 教练相关功能测试 ====================

    @Test
    public void testGetTrainerCourses() {
        // 假设ID=1是教练
        Employee employee = employeeDAO.getEmployeeById(1);
        if (employee != null && employee.getRoleId() == EmployeeService.ROLE_TRAINER) {
            List<Course> courses = employeeService.getTrainerCourses(1);
            assertNotNull(courses);
        }
    }

    @Test
    public void testGetTrainerCourseCount() {
        // 假设ID=1是教练
        Employee employee = employeeDAO.getEmployeeById(1);
        if (employee != null && employee.getRoleId() == EmployeeService.ROLE_TRAINER) {
            int count = employeeService.getTrainerCourseCount(1);
            assertTrue(count >= 0);
        }
    }

    @Test
    public void testGetTrainerTodayBookingCount() {
        // 假设ID=1是教练
        Employee employee = employeeDAO.getEmployeeById(1);
        if (employee != null && employee.getRoleId() == EmployeeService.ROLE_TRAINER) {
            int count = employeeService.getTrainerTodayBookingCount(1);
            assertTrue(count >= 0);
        }
    }

    // ==================== 工龄相关功能测试 ====================

    @Test
    public void testGetWorkYears() {
        int years = employeeService.getWorkYears(1);
        assertTrue(years >= 0);
    }

    @Test
    public void testGetWorkDays() {
        long days = employeeService.getWorkDays(1);
        assertTrue(days >= 0);
    }

    @Test
    public void testGetWorkDurationString() {
        String duration = employeeService.getWorkDurationString(1);
        assertNotNull(duration);
        System.out.println("员工1的工龄：" + duration);
    }

    // ==================== 角色信息获取测试 ====================

    @Test
    public void testGetAllRoles() {
        List<EmployeeRole> roles = employeeService.getAllRoles();
        assertNotNull(roles);
        assertTrue(roles.size() >= 3);  // 至少有教练、前台、管理员三个角色
    }

    @Test
    public void testGetRoleDisplayName() {
        assertEquals("教练", employeeService.getRoleDisplayName(EmployeeService.ROLE_TRAINER));
        assertEquals("前台", employeeService.getRoleDisplayName(EmployeeService.ROLE_RECEPTIONIST));
        assertEquals("管理员", employeeService.getRoleDisplayName(EmployeeService.ROLE_ADMIN));
    }

    // ==================== ServiceResult 测试 ====================

    @Test
    public void testServiceResultSuccess() {
        ServiceResult<String> result = ServiceResult.success("操作成功", "数据");
        assertTrue(result.isSuccess());
        assertEquals("操作成功", result.getMessage());
        assertEquals("数据", result.getData());
    }

    @Test
    public void testServiceResultSuccessNoData() {
        ServiceResult<String> result = ServiceResult.success("操作成功");
        assertTrue(result.isSuccess());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testServiceResultFailure() {
        ServiceResult<String> result = ServiceResult.failure("操作失败");
        assertFalse(result.isSuccess());
        assertEquals("操作失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testServiceResultToString() {
        ServiceResult<String> success = ServiceResult.success("成功消息");
        assertTrue(success.toString().contains("成功"));

        ServiceResult<String> failure = ServiceResult.failure("失败消息");
        assertTrue(failure.toString().contains("失败"));
    }
}

