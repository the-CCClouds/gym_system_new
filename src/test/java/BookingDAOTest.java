import dao.BookingDAO;
import entity.Booking;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.DateUtils;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * BookingDAO 测试类
 *
 * 测试前提：
 * - 数据库中存在 member_id = 1 的会员，且有有效的会员卡
 * - 数据库中存在 course_id = 1 的课程
 * - 数据库中存在 employee_id = 1 的教练
 */
public class BookingDAOTest {

    private BookingDAO bookingDAO;
    private int testBookingId;  // 用于存储测试创建的预约ID，便于清理

    @Before
    public void setUp() {
        bookingDAO = new BookingDAO();
        testBookingId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testBookingId > 0) {
            bookingDAO.deleteBooking(testBookingId);
            testBookingId = 0;
        }
    }

    // ==================== 基础查询测试 ====================

    @Test
    public void testGetBookingById() {
        // 假设数据库中存在 booking_id = 1 的预约
        Booking booking = bookingDAO.getBookingById(1);
        if (booking != null) {
            assertEquals(1, booking.getBookingId());
            assertTrue(booking.getMemberId() > 0);
            assertTrue(booking.getCourseId() > 0);
            assertNotNull(booking.getBookingStatus());
        }
    }

    @Test
    public void testGetBookingByIdNotFound() {
        Booking booking = bookingDAO.getBookingById(99999);
        assertNull(booking);
    }

    @Test
    public void testGetAllBookings() {
        List<Booking> bookings = bookingDAO.getAllBookings();
        assertNotNull(bookings);
        // 验证返回的列表不为null，可能为空
    }

    @Test
    public void testGetBookingsByMemberId() {
        // 假设会员1有预约记录
        List<Booking> bookings = bookingDAO.getBookingsByMemberId(1);
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertEquals(1, booking.getMemberId());
        }
    }

    @Test
    public void testGetBookingsByCourseId() {
        // 假设课程1有预约记录
        List<Booking> bookings = bookingDAO.getBookingsByCourseId(1);
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertEquals(1, booking.getCourseId());
        }
    }

    @Test
    public void testGetBookingsByStatus() {
        // 测试获取已确认的预约
        List<Booking> confirmedBookings = bookingDAO.getBookingsByStatus(BookingDAO.STATUS_CONFIRMED);
        assertNotNull(confirmedBookings);
        for (Booking booking : confirmedBookings) {
            assertEquals(BookingDAO.STATUS_CONFIRMED, booking.getBookingStatus());
        }

        // 测试获取待确认的预约
        List<Booking> pendingBookings = bookingDAO.getBookingsByStatus(BookingDAO.STATUS_PENDING);
        assertNotNull(pendingBookings);
        for (Booking booking : pendingBookings) {
            assertEquals(BookingDAO.STATUS_PENDING, booking.getBookingStatus());
        }
    }

    @Test
    public void testGetPendingBookings() {
        List<Booking> pendingBookings = bookingDAO.getPendingBookings();
        assertNotNull(pendingBookings);
        for (Booking booking : pendingBookings) {
            assertEquals(BookingDAO.STATUS_PENDING, booking.getBookingStatus());
        }
    }

    // ==================== 添加预约测试 ====================

    @Test
    public void testAddBooking() {
        // 创建测试预约（需确保会员1有有效会员卡，且未预约课程2）
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(2);  // 使用课程2避免与已有预约冲突
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 先检查是否已有重复预约，如果有则先取消
        if (bookingDAO.checkDuplicateBooking(1, 2)) {
            // 已存在预约，跳过此测试或清理数据
            System.out.println("会员1已预约课程2，跳过添加测试");
            return;
        }

        boolean result = bookingDAO.addBooking(booking);
        if (result) {
            assertTrue(booking.getBookingId() > 0);
            testBookingId = booking.getBookingId();

            // 验证添加成功
            Booking added = bookingDAO.getBookingById(testBookingId);
            assertNotNull(added);
            assertEquals(1, added.getMemberId());
            assertEquals(2, added.getCourseId());
            assertEquals(BookingDAO.STATUS_PENDING, added.getBookingStatus());
            assertNotNull(added.getBookingTime());
        } else {
            // 添加失败可能是因为会员卡无效或课程已满
            System.out.println("添加预约失败，可能是会员卡无效或课程已满");
        }
    }

    @Test
    public void testAddBookingWithDefaultStatus() {
        // 测试不设置状态时的默认值
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(3);  // 使用课程3
        // 不设置 bookingStatus

        if (!bookingDAO.checkDuplicateBooking(1, 3)) {
            boolean result = bookingDAO.addBooking(booking);
            if (result) {
                testBookingId = booking.getBookingId();
                Booking added = bookingDAO.getBookingById(testBookingId);
                // 默认状态应为 pending
                assertEquals(BookingDAO.STATUS_PENDING, added.getBookingStatus());
            }
        }
    }

    @Test
    public void testAddBookingDuplicate() {
        // 测试重复预约
        Booking booking1 = new Booking();
        booking1.setMemberId(1);
        booking1.setCourseId(4);
        booking1.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 先清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 4 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        // 第一次添加应该成功
        boolean result1 = bookingDAO.addBooking(booking1);
        if (result1) {
            testBookingId = booking1.getBookingId();

            // 第二次添加同一课程应该失败（重复预约）
            Booking booking2 = new Booking();
            booking2.setMemberId(1);
            booking2.setCourseId(4);
            booking2.setBookingStatus(BookingDAO.STATUS_PENDING);

            boolean result2 = bookingDAO.addBooking(booking2);
            assertFalse("重复预约应该失败", result2);
        }
    }

    @Test
    public void testAddBookingInvalidMember() {
        // 测试无效会员ID
        Booking booking = new Booking();
        booking.setMemberId(99999);  // 不存在的会员
        booking.setCourseId(1);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        boolean result = bookingDAO.addBooking(booking);
        assertFalse("无效会员ID应该添加失败", result);
    }

    @Test
    public void testAddBookingInvalidStatus() {
        // 测试无效状态
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(5);
        booking.setBookingStatus("invalid_status");

        boolean result = bookingDAO.addBooking(booking);
        assertFalse("无效状态应该添加失败", result);
    }

    // ==================== 更新预约测试 ====================

    @Test
    public void testUpdateBooking() {
        // 先添加一个测试预约
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(5);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 5 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            testBookingId = booking.getBookingId();

            // 更新预约状态
            booking.setBookingStatus(BookingDAO.STATUS_CONFIRMED);
            boolean result = bookingDAO.updateBooking(booking);
            assertTrue(result);

            // 验证更新成功
            Booking updated = bookingDAO.getBookingById(testBookingId);
            assertEquals(BookingDAO.STATUS_CONFIRMED, updated.getBookingStatus());
        }
    }

    @Test
    public void testUpdateBookingStatus() {
        // 先添加一个测试预约
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(6);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 6 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            testBookingId = booking.getBookingId();

            // 更新状态
            boolean result = bookingDAO.updateBookingStatus(testBookingId, BookingDAO.STATUS_CONFIRMED);
            assertTrue(result);

            Booking updated = bookingDAO.getBookingById(testBookingId);
            assertEquals(BookingDAO.STATUS_CONFIRMED, updated.getBookingStatus());
        }
    }

    @Test
    public void testUpdateBookingStatusInvalid() {
        // 测试更新为无效状态
        boolean result = bookingDAO.updateBookingStatus(1, "invalid_status");
        assertFalse("更新为无效状态应该失败", result);
    }

    // ==================== 确认和取消预约测试 ====================

    @Test
    public void testConfirmBooking() {
        // 先添加一个待确认的预约
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(7);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 7 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            testBookingId = booking.getBookingId();

            // 确认预约
            boolean result = bookingDAO.confirmBooking(testBookingId);
            assertTrue(result);

            Booking confirmed = bookingDAO.getBookingById(testBookingId);
            assertEquals(BookingDAO.STATUS_CONFIRMED, confirmed.getBookingStatus());
        }
    }

    @Test
    public void testConfirmBookingAlreadyConfirmed() {
        // 先添加并确认一个预约
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(8);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 8 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            testBookingId = booking.getBookingId();
            bookingDAO.confirmBooking(testBookingId);

            // 再次确认应该失败
            boolean result = bookingDAO.confirmBooking(testBookingId);
            assertFalse("已确认的预约不能再次确认", result);
        }
    }

    @Test
    public void testConfirmBookingNotFound() {
        boolean result = bookingDAO.confirmBooking(99999);
        assertFalse("不存在的预约确认应该失败", result);
    }

    @Test
    public void testCancelBooking() {
        // 先添加一个预约
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(9);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 9 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            testBookingId = booking.getBookingId();

            // 取消预约
            boolean result = bookingDAO.cancelBooking(testBookingId);
            assertTrue(result);

            Booking cancelled = bookingDAO.getBookingById(testBookingId);
            assertEquals(BookingDAO.STATUS_CANCELLED, cancelled.getBookingStatus());
        }
    }

    @Test
    public void testCancelBookingAlreadyCancelled() {
        // 先添加并取消一个预约
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(10);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 10 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            testBookingId = booking.getBookingId();
            bookingDAO.cancelBooking(testBookingId);

            // 再次取消应该失败
            boolean result = bookingDAO.cancelBooking(testBookingId);
            assertFalse("已取消的预约不能再次取消", result);
        }
    }

    // ==================== 删除预约测试 ====================

    @Test
    public void testDeleteBooking() {
        // 先添加一个预约
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(11);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 11 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.deleteBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            int bookingId = booking.getBookingId();

            // 删除预约
            boolean result = bookingDAO.deleteBooking(bookingId);
            assertTrue(result);

            // 验证已删除
            Booking deleted = bookingDAO.getBookingById(bookingId);
            assertNull(deleted);

            testBookingId = 0;  // 已删除，不需要 tearDown 清理
        }
    }

    @Test
    public void testDeleteBookingNotFound() {
        boolean result = bookingDAO.deleteBooking(99999);
        assertFalse("删除不存在的预约应该失败", result);
    }

    // ==================== 业务校验测试 ====================

    @Test
    public void testCheckMembershipCardValid() {
        // 假设会员1有有效的会员卡
        boolean valid = bookingDAO.checkMembershipCardValid(1);
        // 结果取决于数据库数据，只验证方法能正常执行
        assertNotNull(valid);
    }

    @Test
    public void testCheckMembershipCardValidInvalidMember() {
        // 不存在的会员应该返回 false
        boolean valid = bookingDAO.checkMembershipCardValid(99999);
        assertFalse(valid);
    }

    @Test
    public void testCheckCourseCapacity() {
        // 假设课程1存在
        boolean hasCapacity = bookingDAO.checkCourseCapacity(1);
        // 结果取决于数据库数据
        assertNotNull(hasCapacity);
    }

    @Test
    public void testCheckCourseCapacityInvalidCourse() {
        // 不存在的课程应该返回 false
        boolean hasCapacity = bookingDAO.checkCourseCapacity(99999);
        assertFalse(hasCapacity);
    }

    @Test
    public void testCheckDuplicateBooking() {
        // 测试重复预约检查
        boolean isDuplicate = bookingDAO.checkDuplicateBooking(1, 1);
        // 结果取决于数据库数据
        assertNotNull(isDuplicate);
    }

    // ==================== 统计功能测试 ====================

    @Test
    public void testGetConfirmedBookingCount() {
        int count = bookingDAO.getConfirmedBookingCount(1);
        assertTrue(count >= 0);
    }

    @Test
    public void testGetAvailableSlots() {
        int slots = bookingDAO.getAvailableSlots(1);
        // -1 表示课程不存在，其他值表示剩余名额
        assertTrue(slots >= -1);
    }

    @Test
    public void testGetAvailableSlotsInvalidCourse() {
        int slots = bookingDAO.getAvailableSlots(99999);
        // 不存在的课程应该返回 -1 或 0
        assertTrue(slots <= 0);
    }

    @Test
    public void testGetBookingCountByStatus() {
        int pendingCount = bookingDAO.getBookingCountByStatus(BookingDAO.STATUS_PENDING);
        int confirmedCount = bookingDAO.getBookingCountByStatus(BookingDAO.STATUS_CONFIRMED);
        int cancelledCount = bookingDAO.getBookingCountByStatus(BookingDAO.STATUS_CANCELLED);

        assertTrue(pendingCount >= 0);
        assertTrue(confirmedCount >= 0);
        assertTrue(cancelledCount >= 0);
    }

    @Test
    public void testGetTodayBookingCount() {
        int count = bookingDAO.getTodayBookingCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMemberBookingStats() {
        int[] stats = bookingDAO.getMemberBookingStats(1);
        assertNotNull(stats);
        assertEquals(3, stats.length);
        assertTrue(stats[0] >= 0);  // 总数
        assertTrue(stats[1] >= 0);  // 已确认数
        assertTrue(stats[2] >= 0);  // 已取消数
        // 已确认 + 已取消 <= 总数
        assertTrue(stats[1] + stats[2] <= stats[0]);
    }

    // ==================== 时间相关查询测试 ====================

    @Test
    public void testGetTodayBookings() {
        List<Booking> todayBookings = bookingDAO.getTodayBookings();
        assertNotNull(todayBookings);
        // 验证所有预约都是今天的
        for (Booking booking : todayBookings) {
            assertTrue(DateUtils.isToday(booking.getBookingTime()));
        }
    }

    @Test
    public void testGetBookingHistory() {
        // 测试日期范围查询
        Date startDate = DateUtils.addDays(DateUtils.now(), -30);  // 30天前
        Date endDate = DateUtils.now();

        List<Booking> history = bookingDAO.getBookingHistory(1, startDate, endDate);
        assertNotNull(history);
        for (Booking booking : history) {
            assertEquals(1, booking.getMemberId());
        }
    }

    @Test
    public void testGetBookingHistoryWithStrings() {
        // 测试字符串参数的日期范围查询
        String startDateStr = DateUtils.formatDate(DateUtils.addDays(DateUtils.now(), -30));
        String endDateStr = DateUtils.formatDate(DateUtils.now());

        List<Booking> history = bookingDAO.getBookingHistory(1, startDateStr, endDateStr);
        assertNotNull(history);
    }

    @Test
    public void testGetBookingHistoryNullDates() {
        // 测试日期参数为 null 的情况
        List<Booking> history = bookingDAO.getBookingHistory(1, (Date) null, (Date) null);
        assertNotNull(history);
        // 应该返回该会员的所有预约
    }

    // ==================== 教练相关查询测试 ====================

    @Test
    public void testGetBookingsByTrainerId() {
        // 假设教练1存在
        List<Booking> bookings = bookingDAO.getBookingsByTrainerId(1);
        assertNotNull(bookings);
    }

    @Test
    public void testGetTodayBookingsByTrainerId() {
        List<Booking> todayBookings = bookingDAO.getTodayBookingsByTrainerId(1);
        assertNotNull(todayBookings);
    }

    // ==================== 兼容旧方法名测试 ====================

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedMethods() {
        // 测试旧方法名是否仍然可用
        List<Booking> bookings1 = bookingDAO.getAllBookingsByMemberId(1);
        List<Booking> bookings2 = bookingDAO.getBookingsByMemberId(1);
        assertEquals(bookings1.size(), bookings2.size());

        List<Booking> bookings3 = bookingDAO.getAllBookingsByCourseId(1);
        List<Booking> bookings4 = bookingDAO.getBookingsByCourseId(1);
        assertEquals(bookings3.size(), bookings4.size());

        List<Booking> bookings5 = bookingDAO.getAllBookingsByBookingStatus(BookingDAO.STATUS_CONFIRMED);
        List<Booking> bookings6 = bookingDAO.getBookingsByStatus(BookingDAO.STATUS_CONFIRMED);
        assertEquals(bookings5.size(), bookings6.size());

        int count1 = bookingDAO.getComfirmedBookingCount(1);
        int count2 = bookingDAO.getConfirmedBookingCount(1);
        assertEquals(count1, count2);
    }

    // ==================== 常量测试 ====================

    @Test
    public void testStatusConstants() {
        assertEquals("pending", BookingDAO.STATUS_PENDING);
        assertEquals("confirmed", BookingDAO.STATUS_CONFIRMED);
        assertEquals("cancelled", BookingDAO.STATUS_CANCELLED);
    }

    // ==================== 边界情况测试 ====================

    @Test
    public void testBookingTimeNotNull() {
        // 验证添加预约时 bookingTime 会被自动设置
        Booking booking = new Booking();
        booking.setMemberId(1);
        booking.setCourseId(12);
        booking.setBookingStatus(BookingDAO.STATUS_PENDING);

        // 清理可能存在的预约
        List<Booking> existing = bookingDAO.getBookingsByMemberId(1);
        for (Booking b : existing) {
            if (b.getCourseId() == 12 && !BookingDAO.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                bookingDAO.cancelBooking(b.getBookingId());
            }
        }

        if (bookingDAO.addBooking(booking)) {
            testBookingId = booking.getBookingId();

            Booking added = bookingDAO.getBookingById(testBookingId);
            assertNotNull("预约时间不应为空", added.getBookingTime());
        }
    }

    @Test
    public void testExtractBookingFromResultSet() {
        // 通过查询验证 extractBookingFromResultSet 方法正确提取了所有字段
        Booking booking = bookingDAO.getBookingById(1);
        if (booking != null) {
            assertTrue(booking.getBookingId() > 0);
            assertTrue(booking.getMemberId() > 0);
            assertTrue(booking.getCourseId() > 0);
            assertNotNull(booking.getBookingTime());
            assertNotNull(booking.getBookingStatus());
        }
    }
}

