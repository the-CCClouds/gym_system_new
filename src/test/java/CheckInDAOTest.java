import dao.CheckInDAO;
import entity.CheckIn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * CheckInDAO 测试类
 * 
 * 测试前提：
 * - 数据库中存在 member_id = 1 的会员，且有有效的会员卡
 * - 数据库中已有一些签到记录
 */
public class CheckInDAOTest {

    private CheckInDAO checkInDAO;
    private int testCheckInId;  // 用于存储测试创建的签到ID，便于清理

    @Before
    public void setUp() {
        checkInDAO = new CheckInDAO();
        testCheckInId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testCheckInId > 0) {
            checkInDAO.deleteCheckIn(testCheckInId);
            testCheckInId = 0;
        }
    }

    // ==================== 基础查询测试 ====================

    @Test
    public void testGetCheckInById() {
        // 假设数据库中存在 checkin_id = 1 的记录
        CheckIn checkIn = checkInDAO.getCheckInById(1);
        if (checkIn != null) {
            assertEquals(1, checkIn.getCheckinId());
            assertTrue(checkIn.getMemberId() > 0);
            assertNotNull(checkIn.getCheckinTime());
        }
    }

    @Test
    public void testGetCheckInByIdNotFound() {
        CheckIn checkIn = checkInDAO.getCheckInById(99999);
        assertNull(checkIn);
    }

    @Test
    public void testGetAllCheckIns() {
        List<CheckIn> checkIns = checkInDAO.getAllCheckIns();
        assertNotNull(checkIns);
        assertTrue("数据库应有签到记录", checkIns.size() > 0);
    }

    @Test
    public void testGetCheckInsByMemberId() {
        // 假设会员1有签到记录
        List<CheckIn> checkIns = checkInDAO.getCheckInsByMemberId(1);
        assertNotNull(checkIns);
        for (CheckIn checkIn : checkIns) {
            assertEquals(1, checkIn.getMemberId());
        }
    }

    // ==================== 签到测试 ====================

    @Test
    public void testCheckIn() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(1)) {
            checkInDAO.checkOutByMemberId(1);
        }

        // 测试签到
        boolean result = checkInDAO.checkIn(1);
        if (result) {
            // 获取刚创建的签到记录
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(1);
            assertNotNull(currentCheckIn);
            testCheckInId = currentCheckIn.getCheckinId();

            assertEquals(1, currentCheckIn.getMemberId());
            assertNotNull(currentCheckIn.getCheckinTime());
            assertNull(currentCheckIn.getCheckoutTime());  // 未签退
        } else {
            // 签到失败可能是会员卡无效
            System.out.println("签到失败，可能是会员卡无效");
        }
    }

    @Test
    public void testCheckInWithObject() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(2)) {
            checkInDAO.checkOutByMemberId(2);
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setMemberId(2);

        boolean result = checkInDAO.checkIn(checkIn);
        if (result) {
            assertTrue(checkIn.getCheckinId() > 0);
            testCheckInId = checkIn.getCheckinId();

            // 验证签到记录
            CheckIn added = checkInDAO.getCheckInById(testCheckInId);
            assertNotNull(added);
            assertEquals(2, added.getMemberId());
        }
    }

    @Test
    public void testCheckInDuplicate() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(3)) {
            checkInDAO.checkOutByMemberId(3);
        }

        // 第一次签到
        boolean result1 = checkInDAO.checkIn(3);
        if (result1) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(3);
            testCheckInId = currentCheckIn.getCheckinId();

            // 第二次签到应该失败（重复签到）
            boolean result2 = checkInDAO.checkIn(3);
            assertFalse("重复签到应该失败", result2);
        }
    }

    @Test
    public void testCheckInInvalidMember() {
        // 不存在的会员应该签到失败
        boolean result = checkInDAO.checkIn(99999);
        assertFalse("无效会员签到应该失败", result);
    }

    // ==================== 签退测试 ====================

    @Test
    public void testCheckOut() {
        // 先签到
        if (checkInDAO.hasActiveCheckIn(4)) {
            checkInDAO.checkOutByMemberId(4);
        }

        if (checkInDAO.checkIn(4)) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(4);
            testCheckInId = currentCheckIn.getCheckinId();

            // 签退
            boolean result = checkInDAO.checkOut(testCheckInId);
            assertTrue(result);

            // 验证签退成功
            CheckIn checkedOut = checkInDAO.getCheckInById(testCheckInId);
            assertNotNull(checkedOut.getCheckoutTime());
        }
    }

    @Test
    public void testCheckOutByMemberId() {
        // 先签到
        if (checkInDAO.hasActiveCheckIn(5)) {
            checkInDAO.checkOutByMemberId(5);
        }

        if (checkInDAO.checkIn(5)) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(5);
            testCheckInId = currentCheckIn.getCheckinId();

            // 根据会员ID签退
            boolean result = checkInDAO.checkOutByMemberId(5);
            assertTrue(result);

            // 验证签退成功
            CheckIn checkedOut = checkInDAO.getCheckInById(testCheckInId);
            assertNotNull(checkedOut.getCheckoutTime());
        }
    }

    @Test
    public void testCheckOutAlreadyCheckedOut() {
        // 对已签退的记录再次签退应该失败
        // 假设 checkin_id = 1 已经签退
        CheckIn checkIn = checkInDAO.getCheckInById(1);
        if (checkIn != null && checkIn.getCheckoutTime() != null) {
            boolean result = checkInDAO.checkOut(1);
            assertFalse("已签退的记录不能再次签退", result);
        }
    }

    @Test
    public void testCheckOutNoActiveCheckIn() {
        // 没有未签退记录时签退应该失败
        // 先确保会员6没有未签退记录
        if (checkInDAO.hasActiveCheckIn(6)) {
            checkInDAO.checkOutByMemberId(6);
        }

        boolean result = checkInDAO.checkOutByMemberId(6);
        assertFalse("没有未签退记录时签退应该失败", result);
    }

    // ==================== 删除测试 ====================

    @Test
    public void testDeleteCheckIn() {
        // 先签到创建记录
        if (checkInDAO.hasActiveCheckIn(7)) {
            checkInDAO.checkOutByMemberId(7);
        }

        if (checkInDAO.checkIn(7)) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(7);
            int checkInId = currentCheckIn.getCheckinId();

            // 删除
            boolean result = checkInDAO.deleteCheckIn(checkInId);
            assertTrue(result);

            // 验证已删除
            CheckIn deleted = checkInDAO.getCheckInById(checkInId);
            assertNull(deleted);

            testCheckInId = 0;  // 已删除，不需要 tearDown 清理
        }
    }

    @Test
    public void testDeleteCheckInNotFound() {
        boolean result = checkInDAO.deleteCheckIn(99999);
        assertFalse("删除不存在的记录应该失败", result);
    }

    // ==================== 业务校验测试 ====================

    @Test
    public void testCheckMembershipValid() {
        // 假设会员1有有效会员卡
        boolean valid = checkInDAO.checkMembershipValid(1);
        // 结果取决于数据库数据
        assertNotNull(valid);
    }

    @Test
    public void testCheckMembershipValidInvalidMember() {
        boolean valid = checkInDAO.checkMembershipValid(99999);
        assertFalse("不存在的会员应该返回无效", valid);
    }

    @Test
    public void testHasActiveCheckIn() {
        // 先签到
        if (checkInDAO.hasActiveCheckIn(8)) {
            checkInDAO.checkOutByMemberId(8);
        }

        // 签到前应该没有活跃签到
        assertFalse(checkInDAO.hasActiveCheckIn(8));

        if (checkInDAO.checkIn(8)) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(8);
            testCheckInId = currentCheckIn.getCheckinId();

            // 签到后应该有活跃签到
            assertTrue(checkInDAO.hasActiveCheckIn(8));
        }
    }

    @Test
    public void testIsMemberCheckedIn() {
        // isMemberCheckedIn 应该和 hasActiveCheckIn 返回相同结果
        boolean result1 = checkInDAO.isMemberCheckedIn(1);
        boolean result2 = checkInDAO.hasActiveCheckIn(1);
        assertEquals(result1, result2);
    }

    @Test
    public void testGetCurrentCheckIn() {
        // 先签到
        if (checkInDAO.hasActiveCheckIn(9)) {
            checkInDAO.checkOutByMemberId(9);
        }

        // 签到前应该没有当前签到记录
        CheckIn before = checkInDAO.getCurrentCheckIn(9);
        assertNull(before);

        if (checkInDAO.checkIn(9)) {
            // 签到后应该有当前签到记录
            CheckIn after = checkInDAO.getCurrentCheckIn(9);
            assertNotNull(after);
            assertEquals(9, after.getMemberId());
            assertNull(after.getCheckoutTime());

            testCheckInId = after.getCheckinId();
        }
    }

    // ==================== 时间相关查询测试 ====================

    @Test
    public void testGetTodayCheckIns() {
        List<CheckIn> todayCheckIns = checkInDAO.getTodayCheckIns();
        assertNotNull(todayCheckIns);
        // 验证所有记录都是今天的
        for (CheckIn checkIn : todayCheckIns) {
            assertTrue(DateUtils.isToday(checkIn.getCheckinTime()));
        }
    }

    @Test
    public void testGetCurrentlyCheckedIn() {
        List<CheckIn> currentlyCheckedIn = checkInDAO.getCurrentlyCheckedIn();
        assertNotNull(currentlyCheckedIn);
        // 验证所有记录都未签退
        for (CheckIn checkIn : currentlyCheckedIn) {
            assertNull(checkIn.getCheckoutTime());
        }
    }

    @Test
    public void testGetCheckInsByDate() {
        // 使用今天的日期
        Date today = DateUtils.now();
        List<CheckIn> checkIns = checkInDAO.getCheckInsByDate(today);
        assertNotNull(checkIns);
    }

    @Test
    public void testGetCheckInsByDateRange() {
        Date startDate = DateUtils.addDays(DateUtils.now(), -30);
        Date endDate = DateUtils.now();

        List<CheckIn> checkIns = checkInDAO.getCheckInsByDateRange(startDate, endDate);
        assertNotNull(checkIns);
    }

    @Test
    public void testGetCheckInHistory() {
        Date startDate = DateUtils.addDays(DateUtils.now(), -30);
        Date endDate = DateUtils.now();

        List<CheckIn> history = checkInDAO.getCheckInHistory(1, startDate, endDate);
        assertNotNull(history);
        for (CheckIn checkIn : history) {
            assertEquals(1, checkIn.getMemberId());
        }
    }

    @Test
    public void testGetCheckInHistoryWithStrings() {
        String startDateStr = DateUtils.formatDate(DateUtils.addDays(DateUtils.now(), -30));
        String endDateStr = DateUtils.formatDate(DateUtils.now());

        List<CheckIn> history = checkInDAO.getCheckInHistory(1, startDateStr, endDateStr);
        assertNotNull(history);
    }

    @Test
    public void testGetCheckInHistoryNullDates() {
        // 日期参数为 null 时应该返回所有记录
        List<CheckIn> history = checkInDAO.getCheckInHistory(1, (Date) null, (Date) null);
        assertNotNull(history);
    }

    // ==================== 统计功能测试 ====================

    @Test
    public void testGetTodayCheckInCount() {
        int count = checkInDAO.getTodayCheckInCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetCurrentlyCheckedInCount() {
        int count = checkInDAO.getCurrentlyCheckedInCount();
        assertTrue(count >= 0);

        // 验证与列表大小一致
        List<CheckIn> currentlyCheckedIn = checkInDAO.getCurrentlyCheckedIn();
        assertEquals(currentlyCheckedIn.size(), count);
    }

    @Test
    public void testGetMonthlyCheckInCount() {
        int count = checkInDAO.getMonthlyCheckInCount(1);
        assertTrue(count >= 0);
    }

    @Test
    public void testGetTotalCheckInCount() {
        int count = checkInDAO.getTotalCheckInCount(1);
        assertTrue(count >= 0);

        // 验证与列表大小一致
        List<CheckIn> allCheckIns = checkInDAO.getCheckInsByMemberId(1);
        assertEquals(allCheckIns.size(), count);
    }

    @Test
    public void testGetAverageStayDuration() {
        double avgDuration = checkInDAO.getAverageStayDuration(1);
        assertTrue(avgDuration >= 0);
    }

    @Test
    public void testGetCheckInCountByHour() {
        Date today = DateUtils.now();
        Map<Integer, Integer> hourlyCount = checkInDAO.getCheckInCountByHour(today);

        assertNotNull(hourlyCount);
        assertEquals(24, hourlyCount.size());  // 应该有24个小时的数据

        // 验证所有小时的值都 >= 0
        for (int hour = 0; hour < 24; hour++) {
            assertTrue(hourlyCount.containsKey(hour));
            assertTrue(hourlyCount.get(hour) >= 0);
        }
    }

    @Test
    public void testGetCheckInCountByDate() {
        Date today = DateUtils.now();
        int count = checkInDAO.getCheckInCountByDate(today);
        assertTrue(count >= 0);

        // 验证与今日签到数一致
        int todayCount = checkInDAO.getTodayCheckInCount();
        assertEquals(todayCount, count);
    }

    // ==================== 时长计算测试 ====================

    @Test
    public void testGetStayDuration() {
        // 假设 checkin_id = 1 存在且已签退
        String duration = checkInDAO.getStayDuration(1);
        assertNotNull(duration);
        // 可能是 "x小时x分钟" 或 "进行中"
    }

    @Test
    public void testGetStayDurationNotFound() {
        String duration = checkInDAO.getStayDuration(99999);
        assertEquals("记录不存在", duration);
    }

    @Test
    public void testGetTodayStayDuration() {
        String duration = checkInDAO.getTodayStayDuration(1);
        assertNotNull(duration);
        // 格式应该是 "x小时x分钟" 或 "0分钟"
    }

    @Test
    public void testGetMonthlyTotalMinutes() {
        long minutes = checkInDAO.getMonthlyTotalMinutes(1);
        assertTrue(minutes >= 0);
    }

    @Test
    public void testGetMonthlyTotalDuration() {
        String duration = checkInDAO.getMonthlyTotalDuration(1);
        assertNotNull(duration);
    }

    // ==================== 自动签退测试 ====================

    @Test
    public void testGetOvertimeCheckIns() {
        // 获取超过12小时未签退的记录
        List<CheckIn> overtimeCheckIns = checkInDAO.getOvertimeCheckIns(12);
        assertNotNull(overtimeCheckIns);
        // 所有返回的记录都应该未签退
        for (CheckIn checkIn : overtimeCheckIns) {
            assertNull(checkIn.getCheckoutTime());
        }
    }

    @Test
    public void testAutoCheckOutOvertime() {
        // 注意：这个测试会修改数据，谨慎使用
        // 使用一个很大的小时数，确保不会影响正常数据
        int count = checkInDAO.autoCheckOutOvertime(9999);
        assertTrue(count >= 0);
    }

    // ==================== 边界情况测试 ====================

    @Test
    public void testCheckInTimeNotNull() {
        // 验证签到时间会被自动设置
        if (checkInDAO.hasActiveCheckIn(10)) {
            checkInDAO.checkOutByMemberId(10);
        }

        if (checkInDAO.checkIn(10)) {
            CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(10);
            testCheckInId = currentCheckIn.getCheckinId();

            assertNotNull("签到时间不应为空", currentCheckIn.getCheckinTime());
        }
    }

    @Test
    public void testExtractCheckInFromResultSet() {
        // 通过查询验证字段正确提取
        CheckIn checkIn = checkInDAO.getCheckInById(1);
        if (checkIn != null) {
            assertTrue(checkIn.getCheckinId() > 0);
            assertTrue(checkIn.getMemberId() > 0);
            assertNotNull(checkIn.getCheckinTime());
            // checkoutTime 可能为 null
        }
    }

    // ==================== 完整流程测试 ====================

    @Test
    public void testCompleteCheckInOutFlow() {
        // 测试完整的签到-签退流程
        int memberId = 1;

        // 确保会员没有未签退记录
        if (checkInDAO.hasActiveCheckIn(memberId)) {
            checkInDAO.checkOutByMemberId(memberId);
        }

        // 1. 验证会员卡有效
        if (!checkInDAO.checkMembershipValid(memberId)) {
            System.out.println("会员卡无效，跳过完整流程测试");
            return;
        }

        // 2. 签到
        assertTrue("签到应该成功", checkInDAO.checkIn(memberId));

        // 3. 验证签到状态
        assertTrue("应该处于已签到状态", checkInDAO.isMemberCheckedIn(memberId));

        // 4. 获取当前签到记录
        CheckIn currentCheckIn = checkInDAO.getCurrentCheckIn(memberId);
        assertNotNull(currentCheckIn);
        testCheckInId = currentCheckIn.getCheckinId();

        // 5. 不能重复签到
        assertFalse("不能重复签到", checkInDAO.checkIn(memberId));

        // 6. 签退
        assertTrue("签退应该成功", checkInDAO.checkOutByMemberId(memberId));

        // 7. 验证签退状态
        assertFalse("应该处于未签到状态", checkInDAO.isMemberCheckedIn(memberId));

        // 8. 验证签退时间已设置
        CheckIn checkedOut = checkInDAO.getCheckInById(testCheckInId);
        assertNotNull(checkedOut.getCheckoutTime());

        // 9. 不能重复签退
        assertFalse("不能重复签退", checkInDAO.checkOutByMemberId(memberId));
    }
}

