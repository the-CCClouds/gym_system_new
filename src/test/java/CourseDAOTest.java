import dao.CourseDAO;
import entity.Course;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * CourseDAO 测试类
 * 
 * 测试前提：
 * - 数据库中存在课程数据
 * - 数据库中存在 employee_id = 1 的教练
 */
public class CourseDAOTest {

    private CourseDAO courseDAO;
    private int testCourseId;  // 用于存储测试创建的课程ID，便于清理

    @Before
    public void setUp() {
        courseDAO = new CourseDAO();
        testCourseId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testCourseId > 0) {
            courseDAO.deleteCourse(testCourseId);
            testCourseId = 0;
        }
    }

    // ==================== 基础查询测试 ====================

    @Test
    public void testGetCourseById() {
        Course course = courseDAO.getCourseById(1);
        assertNotNull(course);
        assertEquals(1, course.getCourseId());
        assertNotNull(course.getName());
        assertNotNull(course.getType());
        assertTrue(course.getDuration() > 0);
        assertTrue(course.getMaxCapacity() > 0);
    }

    @Test
    public void testGetCourseByIdNotFound() {
        Course course = courseDAO.getCourseById(99999);
        assertNull(course);
    }

    @Test
    public void testGetAllCourses() {
        List<Course> courses = courseDAO.getAllCourses();
        assertNotNull(courses);
        assertTrue("数据库应有课程数据", courses.size() > 0);
    }

    @Test
    public void testSearchCourseByName() {
        List<Course> courses = courseDAO.searchCourseByName("瑜伽");
        assertNotNull(courses);
        for (Course course : courses) {
            assertTrue(course.getName().contains("瑜伽"));
        }
    }

    @Test
    public void testSearchCourseByNameNotFound() {
        List<Course> courses = courseDAO.searchCourseByName("不存在的课程名999");
        assertNotNull(courses);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCoursesByType() {
        List<Course> yogaCourses = courseDAO.getCoursesByType(CourseDAO.TYPE_YOGA);
        assertNotNull(yogaCourses);
        for (Course course : yogaCourses) {
            assertEquals(CourseDAO.TYPE_YOGA, course.getType());
        }
    }

    @Test
    public void testGetCoursesByTypeInvalid() {
        List<Course> courses = courseDAO.getCoursesByType("invalid_type");
        assertNotNull(courses);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetCoursesByEmployeeId() {
        List<Course> courses = courseDAO.getCoursesByEmployeeId(1);
        assertNotNull(courses);
        for (Course course : courses) {
            assertEquals(1, course.getEmployeeId());
        }
    }

    @Test
    public void testGetCoursesByDurationRange() {
        List<Course> courses = courseDAO.getCoursesByDurationRange(30, 60);
        assertNotNull(courses);
        for (Course course : courses) {
            assertTrue(course.getDuration() >= 30 && course.getDuration() <= 60);
        }
    }

    // ==================== 添加课程测试 ====================

    @Test
    public void testAddCourse() {
        Course course = new Course();
        course.setName("测试课程");
        course.setType(CourseDAO.TYPE_YOGA);
        course.setDuration(60);
        course.setMaxCapacity(20);
        course.setEmployeeId(1);

        assertTrue(courseDAO.addCourse(course));
        assertTrue(course.getCourseId() > 0);
        testCourseId = course.getCourseId();

        // 验证添加成功
        Course added = courseDAO.getCourseById(testCourseId);
        assertNotNull(added);
        assertEquals("测试课程", added.getName());
        assertEquals(CourseDAO.TYPE_YOGA, added.getType());
        assertEquals(60, added.getDuration());
        assertEquals(20, added.getMaxCapacity());
    }

    @Test
    public void testAddCourseInvalidType() {
        Course course = new Course();
        course.setName("无效类型课程");
        course.setType("invalid_type");
        course.setDuration(60);
        course.setMaxCapacity(20);
        course.setEmployeeId(1);

        assertFalse("无效类型应该添加失败", courseDAO.addCourse(course));
    }

    @Test
    public void testAddCourseInvalidDuration() {
        Course course = new Course();
        course.setName("无效时长课程");
        course.setType(CourseDAO.TYPE_YOGA);
        course.setDuration(0);  // 无效时长
        course.setMaxCapacity(20);
        course.setEmployeeId(1);

        assertFalse("无效时长应该添加失败", courseDAO.addCourse(course));
    }

    @Test
    public void testAddCourseInvalidCapacity() {
        Course course = new Course();
        course.setName("无效容量课程");
        course.setType(CourseDAO.TYPE_YOGA);
        course.setDuration(60);
        course.setMaxCapacity(0);  // 无效容量
        course.setEmployeeId(1);

        assertFalse("无效容量应该添加失败", courseDAO.addCourse(course));
    }

    // ==================== 更新课程测试 ====================

    @Test
    public void testUpdateCourse() {
        // 先获取课程
        Course course = courseDAO.getCourseById(1);
        assertNotNull(course);

        String originalName = course.getName();
        int originalDuration = course.getDuration();

        // 更新
        course.setName("瑜伽基础班Updated");
        course.setDuration(75);

        assertTrue(courseDAO.updateCourse(course));

        // 验证更新成功
        Course updated = courseDAO.getCourseById(1);
        assertEquals("瑜伽基础班Updated", updated.getName());
        assertEquals(75, updated.getDuration());

        // 恢复原始数据
        course.setName(originalName);
        course.setDuration(originalDuration);
        courseDAO.updateCourse(course);
    }

    @Test
    public void testUpdateCourseInvalidType() {
        Course course = courseDAO.getCourseById(1);
        assertNotNull(course);

        String originalType = course.getType();
        course.setType("invalid_type");

        assertFalse("无效类型应该更新失败", courseDAO.updateCourse(course));

        // 恢复
        course.setType(originalType);
    }

    // ==================== 删除课程测试 ====================

    @Test
    public void testDeleteCourse() {
        // 先添加测试数据
        Course course = new Course();
        course.setName("待删除课程");
        course.setType(CourseDAO.TYPE_OTHER);
        course.setDuration(45);
        course.setMaxCapacity(15);
        course.setEmployeeId(1);

        assertTrue(courseDAO.addCourse(course));
        int courseId = course.getCourseId();

        // 删除
        assertTrue(courseDAO.deleteCourse(courseId));

        // 验证已删除
        assertNull(courseDAO.getCourseById(courseId));

        testCourseId = 0;  // 已删除，不需要 tearDown 清理
    }

    @Test
    public void testDeleteCourseNotFound() {
        assertFalse(courseDAO.deleteCourse(99999));
    }

    // ==================== 预约相关功能测试 ====================

    @Test
    public void testGetConfirmedBookingCount() {
        int count = courseDAO.getConfirmedBookingCount(1);
        assertTrue(count >= 0);
    }

    @Test
    public void testGetAvailableSlots() {
        int slots = courseDAO.getAvailableSlots(1);
        assertTrue(slots >= -1);  // -1 表示课程不存在

        // 验证计算正确
        Course course = courseDAO.getCourseById(1);
        if (course != null) {
            int confirmedCount = courseDAO.getConfirmedBookingCount(1);
            assertEquals(course.getMaxCapacity() - confirmedCount, slots);
        }
    }

    @Test
    public void testGetAvailableSlotsNotFound() {
        int slots = courseDAO.getAvailableSlots(99999);
        assertEquals(-1, slots);
    }

    @Test
    public void testIsFull() {
        boolean isFull = courseDAO.isFull(1);
        // 结果取决于数据库数据
        assertNotNull(isFull);

        // 验证与 getAvailableSlots 一致
        int slots = courseDAO.getAvailableSlots(1);
        assertEquals(slots <= 0, isFull);
    }

    @Test
    public void testGetAvailableCourses() {
        List<Course> courses = courseDAO.getAvailableCourses();
        assertNotNull(courses);
        // 所有返回的课程都应该有空位
        for (Course course : courses) {
            assertFalse(courseDAO.isFull(course.getCourseId()));
        }
    }

    // ==================== 统计功能测试 ====================

    @Test
    public void testGetTotalCourseCount() {
        int count = courseDAO.getTotalCourseCount();
        assertTrue(count > 0);

        // 验证与列表大小一致
        List<Course> courses = courseDAO.getAllCourses();
        assertEquals(courses.size(), count);
    }

    @Test
    public void testGetCourseCountByTypeMap() {
        Map<String, Integer> countMap = courseDAO.getCourseCountByType();
        assertNotNull(countMap);

        // 应该包含所有有效类型
        for (String type : CourseDAO.VALID_TYPES) {
            assertTrue(countMap.containsKey(type));
            assertTrue(countMap.get(type) >= 0);
        }

        // 验证总数正确
        int total = 0;
        for (int count : countMap.values()) {
            total += count;
        }
        assertEquals(courseDAO.getTotalCourseCount(), total);
    }

    @Test
    public void testGetCourseCountByTypeSingle() {
        int yogaCount = courseDAO.getCourseCountByType(CourseDAO.TYPE_YOGA);
        assertTrue(yogaCount >= 0);

        // 验证与列表大小一致
        List<Course> yogaCourses = courseDAO.getCoursesByType(CourseDAO.TYPE_YOGA);
        assertEquals(yogaCourses.size(), yogaCount);
    }

    @Test
    public void testGetCourseCountByEmployee() {
        Map<Integer, Integer> countMap = courseDAO.getCourseCountByEmployee();
        assertNotNull(countMap);

        // 验证每个教练的课程数
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            int employeeId = entry.getKey();
            int count = entry.getValue();
            List<Course> courses = courseDAO.getCoursesByEmployeeId(employeeId);
            assertEquals(courses.size(), count);
        }
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testGetAllCourseTypes() {
        List<String> types = courseDAO.getAllCourseTypes();
        assertNotNull(types);
        assertEquals(CourseDAO.VALID_TYPES.length, types.size());

        for (String type : CourseDAO.VALID_TYPES) {
            assertTrue(types.contains(type));
        }
    }

    @Test
    public void testIsValidType() {
        // 有效类型
        assertTrue(courseDAO.isValidType(CourseDAO.TYPE_YOGA));
        assertTrue(courseDAO.isValidType(CourseDAO.TYPE_SPINNING));
        assertTrue(courseDAO.isValidType(CourseDAO.TYPE_PILATES));
        assertTrue(courseDAO.isValidType(CourseDAO.TYPE_AEROBICS));
        assertTrue(courseDAO.isValidType(CourseDAO.TYPE_STRENGTH));
        assertTrue(courseDAO.isValidType(CourseDAO.TYPE_OTHER));

        // 无效类型
        assertFalse(courseDAO.isValidType("invalid"));
        assertFalse(courseDAO.isValidType(null));
        assertFalse(courseDAO.isValidType(""));
    }

    @Test
    public void testGetTypeDisplayName() {
        assertEquals("瑜伽", courseDAO.getTypeDisplayName(CourseDAO.TYPE_YOGA));
        assertEquals("动感单车", courseDAO.getTypeDisplayName(CourseDAO.TYPE_SPINNING));
        assertEquals("普拉提", courseDAO.getTypeDisplayName(CourseDAO.TYPE_PILATES));
        assertEquals("有氧操", courseDAO.getTypeDisplayName(CourseDAO.TYPE_AEROBICS));
        assertEquals("力量训练", courseDAO.getTypeDisplayName(CourseDAO.TYPE_STRENGTH));
        assertEquals("其他", courseDAO.getTypeDisplayName(CourseDAO.TYPE_OTHER));
        assertEquals("未知", courseDAO.getTypeDisplayName("invalid"));
        assertEquals("未知", courseDAO.getTypeDisplayName(null));
    }

    @Test
    public void testFormatDuration() {
        assertEquals("0分钟", courseDAO.formatDuration(0));
        assertEquals("30分钟", courseDAO.formatDuration(30));
        assertEquals("1小时", courseDAO.formatDuration(60));
        assertEquals("1小时30分钟", courseDAO.formatDuration(90));
        assertEquals("2小时", courseDAO.formatDuration(120));
        assertEquals("2小时15分钟", courseDAO.formatDuration(135));
    }

    // ==================== 常量测试 ====================

    @Test
    public void testTypeConstants() {
        assertEquals("yoga", CourseDAO.TYPE_YOGA);
        assertEquals("spinning", CourseDAO.TYPE_SPINNING);
        assertEquals("pilates", CourseDAO.TYPE_PILATES);
        assertEquals("aerobics", CourseDAO.TYPE_AEROBICS);
        assertEquals("strength", CourseDAO.TYPE_STRENGTH);
        assertEquals("other", CourseDAO.TYPE_OTHER);
    }

    @Test
    public void testValidTypesArray() {
        assertEquals(6, CourseDAO.VALID_TYPES.length);
    }

    // ==================== 兼容旧方法名测试 ====================

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedMethods() {
        // 测试旧方法名是否仍然可用
        List<Course> courses1 = courseDAO.getAllCourse();
        List<Course> courses2 = courseDAO.getAllCourses();
        assertEquals(courses1.size(), courses2.size());

        List<Course> courses3 = courseDAO.getCourseByType(CourseDAO.TYPE_YOGA);
        List<Course> courses4 = courseDAO.getCoursesByType(CourseDAO.TYPE_YOGA);
        assertEquals(courses3.size(), courses4.size());

        List<Course> courses5 = courseDAO.getCourseByEmployeeId("1");
        List<Course> courses6 = courseDAO.getCoursesByEmployeeId(1);
        assertEquals(courses5.size(), courses6.size());
    }

    // ==================== 边界情况测试 ====================

    @Test
    public void testExtractCourseFromResultSet() {
        // 通过查询验证字段正确提取
        Course course = courseDAO.getCourseById(1);
        if (course != null) {
            assertTrue(course.getCourseId() > 0);
            assertNotNull(course.getName());
            assertNotNull(course.getType());
            assertTrue(course.getDuration() > 0);
            assertTrue(course.getMaxCapacity() > 0);
            assertTrue(course.getEmployeeId() > 0);
        }
    }
}
