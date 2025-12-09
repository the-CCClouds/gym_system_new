import dao.CheckInDAO;
import dao.MemberDAO;
import dao.MembershipCardDAO;
import entity.CheckIn;
import entity.Member;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.CheckInService;
import service.CheckInService.CheckInDetail;
import service.CheckInService.CheckInStatistics;
import service.CheckInService.MemberCheckInSummary;
import service.CheckInService.ServiceResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * CheckInService 测试类
 *
 * 测试前提：
 * - 数据库中存在会员数据（有有效会员卡）
 */
public class CheckInServiceTest {

    private CheckInService checkInService;
    private CheckInDAO checkInDAO;
    private MemberDAO memberDAO;
    private MembershipCardDAO cardDAO;
    private int testCheckinId;  // 用于清理测试数据
    private int testMemberId;   // 测试用会员ID

    @Before
    public void setUp() {
        checkInService = new CheckInService();
        checkInDAO = new CheckInDAO();
        memberDAO = new MemberDAO();
        cardDAO = new MembershipCardDAO();
        testCheckinId = 0;

        // 获取一个有有效会员卡的会员
        List<Member> members = memberDAO.getMembersWithValidCard();
        if (!members.isEmpty()) {
            testMemberId = members.get(0).getId();
        } else {
            testMemberId = 1; // 默认使用ID=1
        }
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testCheckinId > 0) {
            checkInDAO.deleteCheckIn(testCheckinId);
            testCheckinId = 0;
        }
    }

    // ==================== 签到测试 ====================

    @Test
    public void testCheckIn() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        ServiceResult<CheckIn> result = checkInService.checkIn(testMemberId);

        if (result.isSuccess()) {
            assertNotNull(result.getData());
            assertEquals(testMemberId, result.getData().getMemberId());
            assertNotNull(result.getData().getCheckinTime());
            assertNull(result.getData().getCheckoutTime());
            testCheckinId = result.getData().getCheckinId();

            // 签退以便后续测试
            checkInService.checkOut(testMemberId);
        } else {
            // 如果失败，打印原因
            System.out.println("签到失败：" + result.getMessage());
        }
    }

    @Test
    public void testCheckInWithInvalidMember() {
        ServiceResult<CheckIn> result = checkInService.checkIn(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("会员"));
    }

    @Test
    public void testCheckInDuplicate() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        // 先签到
        ServiceResult<CheckIn> firstResult = checkInService.checkIn(testMemberId);
        if (!firstResult.isSuccess()) {
            System.out.println("跳过测试：无法签到 - " + firstResult.getMessage());
            return;
        }
        testCheckinId = firstResult.getData().getCheckinId();

        // 尝试重复签到
        ServiceResult<CheckIn> secondResult = checkInService.checkIn(testMemberId);
        assertFalse(secondResult.isSuccess());
        assertTrue(secondResult.getMessage().contains("已") && secondResult.getMessage().contains("签到"));

        // 签退
        checkInService.checkOut(testMemberId);
    }

    @Test
    public void testCheckInByPhone() {
        // 获取有有效会员卡的会员
        List<Member> members = memberDAO.getMembersWithValidCard();
        if (members.isEmpty()) {
            System.out.println("跳过测试：没有有效会员卡的会员");
            return;
        }

        Member member = members.get(0);

        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(member.getId())) {
            checkInDAO.checkOutByMemberId(member.getId());
        }

        ServiceResult<CheckIn> result = checkInService.checkInByPhone(member.getPhone());

        if (result.isSuccess()) {
            assertNotNull(result.getData());
            testCheckinId = result.getData().getCheckinId();
            // 签退
            checkInService.checkOut(member.getId());
        } else {
            System.out.println("签到失败：" + result.getMessage());
        }
    }

    @Test
    public void testCheckInByPhoneNotFound() {
        ServiceResult<CheckIn> result = checkInService.checkInByPhone("99999999999");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("未找到"));
    }

    // ==================== 签退测试 ====================

    @Test
    public void testCheckOut() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        // 先签到
        ServiceResult<CheckIn> checkInResult = checkInService.checkIn(testMemberId);
        if (!checkInResult.isSuccess()) {
            System.out.println("跳过测试：无法签到 - " + checkInResult.getMessage());
            return;
        }
        testCheckinId = checkInResult.getData().getCheckinId();

        // 签退
        ServiceResult<CheckIn> checkOutResult = checkInService.checkOut(testMemberId);

        assertTrue(checkOutResult.isSuccess());
        assertNotNull(checkOutResult.getData());
        assertNotNull(checkOutResult.getData().getCheckoutTime());
        assertTrue(checkOutResult.getMessage().contains("时长"));
    }

    @Test
    public void testCheckOutWithInvalidMember() {
        ServiceResult<CheckIn> result = checkInService.checkOut(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("会员"));
    }

    @Test
    public void testCheckOutWithoutCheckIn() {
        // 确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        ServiceResult<CheckIn> result = checkInService.checkOut(testMemberId);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("没有未签退"));
    }

    @Test
    public void testCheckOutById() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        // 先签到
        ServiceResult<CheckIn> checkInResult = checkInService.checkIn(testMemberId);
        if (!checkInResult.isSuccess()) {
            System.out.println("跳过测试：无法签到");
            return;
        }
        testCheckinId = checkInResult.getData().getCheckinId();

        // 根据ID签退
        ServiceResult<CheckIn> checkOutResult = checkInService.checkOutById(testCheckinId);

        assertTrue(checkOutResult.isSuccess());
        assertNotNull(checkOutResult.getData().getCheckoutTime());
    }

    @Test
    public void testCheckOutByIdNotFound() {
        ServiceResult<CheckIn> result = checkInService.checkOutById(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    // ==================== 签到查询测试 ====================

    @Test
    public void testGetCheckInById() {
        List<CheckIn> checkIns = checkInDAO.getAllCheckIns();
        if (!checkIns.isEmpty()) {
            int checkinId = checkIns.get(0).getCheckinId();
            CheckIn checkIn = checkInService.getCheckInById(checkinId);
            assertNotNull(checkIn);
            assertEquals(checkinId, checkIn.getCheckinId());
        }
    }

    @Test
    public void testGetCheckInByIdNotFound() {
        CheckIn checkIn = checkInService.getCheckInById(99999);
        assertNull(checkIn);
    }

    @Test
    public void testGetAllCheckIns() {
        List<CheckIn> checkIns = checkInService.getAllCheckIns();
        assertNotNull(checkIns);
    }

    @Test
    public void testGetCheckInsByMember() {
        List<CheckIn> checkIns = checkInService.getCheckInsByMember(testMemberId);
        assertNotNull(checkIns);
        for (CheckIn checkIn : checkIns) {
            assertEquals(testMemberId, checkIn.getMemberId());
        }
    }

    @Test
    public void testGetCurrentCheckIn() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        // 没有签到时应该返回null
        CheckIn noCheckIn = checkInService.getCurrentCheckIn(testMemberId);
        assertNull(noCheckIn);

        // 签到后应该返回记录
        ServiceResult<CheckIn> checkInResult = checkInService.checkIn(testMemberId);
        if (checkInResult.isSuccess()) {
            testCheckinId = checkInResult.getData().getCheckinId();

            CheckIn currentCheckIn = checkInService.getCurrentCheckIn(testMemberId);
            assertNotNull(currentCheckIn);
            assertEquals(testMemberId, currentCheckIn.getMemberId());
            assertNull(currentCheckIn.getCheckoutTime());

            // 签退
            checkInService.checkOut(testMemberId);
        }
    }

    @Test
    public void testGetTodayCheckIns() {
        List<CheckIn> checkIns = checkInService.getTodayCheckIns();
        assertNotNull(checkIns);
    }

    @Test
    public void testGetCurrentlyCheckedIn() {
        List<CheckIn> checkIns = checkInService.getCurrentlyCheckedIn();
        assertNotNull(checkIns);
        for (CheckIn checkIn : checkIns) {
            assertNull(checkIn.getCheckoutTime());
        }
    }

    // ==================== 签到详情测试 ====================

    @Test
    public void testGetCheckInDetail() {
        List<CheckIn> checkIns = checkInDAO.getAllCheckIns();
        if (!checkIns.isEmpty()) {
            int checkinId = checkIns.get(0).getCheckinId();
            CheckInDetail detail = checkInService.getCheckInDetail(checkinId);

            assertNotNull(detail);
            assertNotNull(detail.getCheckIn());
            assertNotNull(detail.getMemberName());
            assertNotNull(detail.getCheckinTimeFormatted());
            assertNotNull(detail.getDuration());

            // 打印详情
            System.out.println(detail.toString());
        }
    }

    @Test
    public void testGetCheckInDetailNotFound() {
        CheckInDetail detail = checkInService.getCheckInDetail(99999);
        assertNull(detail);
    }

    // ==================== 签到验证测试 ====================

    @Test
    public void testValidateMemberCanCheckIn() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        ServiceResult<Member> result = checkInService.validateMemberCanCheckIn(testMemberId);
        // 结果取决于会员状态和会员卡
        assertNotNull(result);
        System.out.println("验证结果：" + result.getMessage());
    }

    @Test
    public void testValidateMemberCanCheckInNotFound() {
        ServiceResult<Member> result = checkInService.validateMemberCanCheckIn(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testIsMemberCheckedIn() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        // 签到前
        assertFalse(checkInService.isMemberCheckedIn(testMemberId));

        // 签到
        ServiceResult<CheckIn> checkInResult = checkInService.checkIn(testMemberId);
        if (checkInResult.isSuccess()) {
            testCheckinId = checkInResult.getData().getCheckinId();

            // 签到后
            assertTrue(checkInService.isMemberCheckedIn(testMemberId));

            // 签退
            checkInService.checkOut(testMemberId);

            // 签退后
            assertFalse(checkInService.isMemberCheckedIn(testMemberId));
        }
    }

    @Test
    public void testIsCheckInExists() {
        List<CheckIn> checkIns = checkInDAO.getAllCheckIns();
        if (!checkIns.isEmpty()) {
            assertTrue(checkInService.isCheckInExists(checkIns.get(0).getCheckinId()));
        }
        assertFalse(checkInService.isCheckInExists(99999));
    }

    // ==================== 签到统计测试 ====================

    @Test
    public void testGetTodayCheckInCount() {
        int count = checkInService.getTodayCheckInCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetCurrentlyCheckedInCount() {
        int count = checkInService.getCurrentlyCheckedInCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMemberMonthlyCheckInCount() {
        int count = checkInService.getMemberMonthlyCheckInCount(testMemberId);
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMemberTotalCheckInCount() {
        int count = checkInService.getMemberTotalCheckInCount(testMemberId);
        assertTrue(count >= 0);
    }

    @Test
    public void testGetCheckInCountByHour() {
        Map<Integer, Integer> hourlyCount = checkInService.getCheckInCountByHour(new Date());
        assertNotNull(hourlyCount);
        assertEquals(24, hourlyCount.size());
        for (int hour = 0; hour < 24; hour++) {
            assertTrue(hourlyCount.containsKey(hour));
            assertTrue(hourlyCount.get(hour) >= 0);
        }
    }

    @Test
    public void testGetStatistics() {
        CheckInStatistics stats = checkInService.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTodayCount() >= 0);
        assertTrue(stats.getCurrentlyCheckedInCount() >= 0);
        assertTrue(stats.getTotalRecords() >= 0);
        assertTrue(stats.getPeakHour() >= 0 && stats.getPeakHour() < 24);

        // 打印统计
        System.out.println(stats.toString());
    }

    // ==================== 健身时长测试 ====================

    @Test
    public void testGetStayDuration() {
        List<CheckIn> checkIns = checkInDAO.getAllCheckIns();
        if (!checkIns.isEmpty()) {
            String duration = checkInService.getStayDuration(checkIns.get(0).getCheckinId());
            assertNotNull(duration);
            System.out.println("健身时长：" + duration);
        }
    }

    @Test
    public void testGetMemberTodayDuration() {
        String duration = checkInService.getMemberTodayDuration(testMemberId);
        assertNotNull(duration);
        System.out.println("今日健身时长：" + duration);
    }

    @Test
    public void testGetMemberMonthlyDuration() {
        String duration = checkInService.getMemberMonthlyDuration(testMemberId);
        assertNotNull(duration);
        System.out.println("本月健身时长：" + duration);
    }

    @Test
    public void testGetMemberMonthlyDurationMinutes() {
        long minutes = checkInService.getMemberMonthlyDurationMinutes(testMemberId);
        assertTrue(minutes >= 0);
    }

    @Test
    public void testGetMemberAverageDuration() {
        double avgMinutes = checkInService.getMemberAverageDuration(testMemberId);
        assertTrue(avgMinutes >= 0);
    }

    @Test
    public void testGetMemberAverageDurationFormatted() {
        String duration = checkInService.getMemberAverageDurationFormatted(testMemberId);
        assertNotNull(duration);
        System.out.println("平均健身时长：" + duration);
    }

    // ==================== 自动签退测试 ====================

    @Test
    public void testAutoCheckOutOvertime() {
        ServiceResult<Integer> result = checkInService.autoCheckOutOvertime(12);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData() >= 0);
        System.out.println("自动签退结果：" + result.getMessage());
    }

    @Test
    public void testAutoCheckOutOvertimeInvalidHours() {
        ServiceResult<Integer> result = checkInService.autoCheckOutOvertime(0);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("大于0"));
    }

    @Test
    public void testGetOvertimeCheckIns() {
        List<CheckIn> overtimeCheckIns = checkInService.getOvertimeCheckIns(12);
        assertNotNull(overtimeCheckIns);
        // 超时记录应该都是未签退的
        for (CheckIn checkIn : overtimeCheckIns) {
            assertNull(checkIn.getCheckoutTime());
        }
    }

    // ==================== 会员签到汇总测试 ====================

    @Test
    public void testGetMemberCheckInSummary() {
        MemberCheckInSummary summary = checkInService.getMemberCheckInSummary(testMemberId);

        if (summary != null) {
            assertEquals(testMemberId, summary.getMemberId());
            assertNotNull(summary.getMemberName());
            assertTrue(summary.getTotalCheckInCount() >= 0);
            assertTrue(summary.getMonthlyCheckInCount() >= 0);
            assertNotNull(summary.getTodayDuration());
            assertNotNull(summary.getMonthlyDuration());
            assertTrue(summary.getAverageDuration() >= 0);

            // 打印汇总
            System.out.println(summary.toString());
        } else {
            System.out.println("会员不存在");
        }
    }

    @Test
    public void testGetMemberCheckInSummaryNotFound() {
        MemberCheckInSummary summary = checkInService.getMemberCheckInSummary(99999);
        assertNull(summary);
    }

    // ==================== 删除签到记录测试 ====================

    @Test
    public void testDeleteCheckIn() {
        // 先确保会员没有未签退的记录
        if (checkInDAO.hasActiveCheckIn(testMemberId)) {
            checkInDAO.checkOutByMemberId(testMemberId);
        }

        // 先签到
        ServiceResult<CheckIn> checkInResult = checkInService.checkIn(testMemberId);
        if (!checkInResult.isSuccess()) {
            System.out.println("跳过测试：无法签到");
            return;
        }
        int checkinId = checkInResult.getData().getCheckinId();

        // 签退
        checkInService.checkOut(testMemberId);

        // 删除
        ServiceResult<Void> deleteResult = checkInService.deleteCheckIn(checkinId);
        assertTrue(deleteResult.isSuccess());

        // 验证已删除
        assertNull(checkInDAO.getCheckInById(checkinId));

        testCheckinId = 0;  // 已删除，不需要清理
    }

    @Test
    public void testDeleteCheckInNotFound() {
        ServiceResult<Void> result = checkInService.deleteCheckIn(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
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
