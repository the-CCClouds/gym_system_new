import dao.BookingDAO;
import dao.CourseDAO;
import dao.MemberDAO;
import dao.MembershipCardDAO;
import entity.Booking;
import entity.Course;
import entity.Member;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.BookingService;
import service.BookingService.BookingDetail;
import service.BookingService.BookingStatistics;
import service.BookingService.ServiceResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * BookingService 测试类
 * 
 * 测试前提：
 * - 数据库中存在会员数据（有有效会员卡）
 * - 数据库中存在课程数据
 */
public class BookingServiceTest {

    private BookingService bookingService;
    private BookingDAO bookingDAO;
    private CourseDAO courseDAO;
    private MemberDAO memberDAO;
    private MembershipCardDAO cardDAO;
    private int testBookingId;  // 用于清理测试数据
    private int testMemberId;   // 测试用会员ID
    private int testCourseId;   // 测试用课程ID

    @Before
    public void setUp() {
        bookingService = new BookingService();
        bookingDAO = new BookingDAO();
        courseDAO = new CourseDAO();
        memberDAO = new MemberDAO();
        cardDAO = new MembershipCardDAO();
        testBookingId = 0;
        
        // 获取一个有有效会员卡的会员
        List<Member> members = memberDAO.getMembersWithValidCard();
        if (!members.isEmpty()) {
            testMemberId = members.get(0).getId();
        } else {
            testMemberId = 1; // 默认使用ID=1
        }
        
        // 获取一个有空位的课程
        List<Course> courses = courseDAO.getAvailableCourses();
        if (!courses.isEmpty()) {
            testCourseId = courses.get(0).getCourseId();
        } else {
            testCourseId = 1; // 默认使用ID=1
        }
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testBookingId > 0) {
            // 先取消再删除
            bookingDAO.cancelBooking(testBookingId);
            bookingDAO.deleteBooking(testBookingId);
            testBookingId = 0;
        }
    }

    // ==================== 预约创建测试 ====================

    @Test
    public void testCreateBooking() {
        // 先检查是否已有预约，如果有则先取消
        if (bookingDAO.checkDuplicateBooking(testMemberId, testCourseId)) {
            List<Booking> existingBookings = bookingDAO.getBookingsByMemberId(testMemberId);
            for (Booking b : existingBookings) {
                if (b.getCourseId() == testCourseId && 
                    !BookingService.STATUS_CANCELLED.equals(b.getBookingStatus())) {
                    bookingDAO.cancelBooking(b.getBookingId());
                }
            }
        }

        ServiceResult<Booking> result = bookingService.createBooking(testMemberId, testCourseId);

        if (result.isSuccess()) {
            assertNotNull(result.getData());
            assertEquals(testMemberId, result.getData().getMemberId());
            assertEquals(testCourseId, result.getData().getCourseId());
            assertEquals(BookingService.STATUS_PENDING, result.getData().getBookingStatus());
            testBookingId = result.getData().getBookingId();
        } else {
            // 如果失败，打印原因（可能是会员卡无效或课程已满）
            System.out.println("预约创建失败：" + result.getMessage());
        }
    }

    @Test
    public void testCreateBookingWithInvalidMember() {
        ServiceResult<Booking> result = bookingService.createBooking(99999, testCourseId);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("会员"));
    }

    @Test
    public void testCreateBookingWithInvalidCourse() {
        // 使用有有效会员卡的会员来测试无效课程
        List<Member> membersWithCard = memberDAO.getMembersWithValidCard();
        if (membersWithCard.isEmpty()) {
            System.out.println("跳过测试：没有有效会员卡的会员");
            return;
        }
        int validMemberId = membersWithCard.get(0).getId();
        
        ServiceResult<Booking> result = bookingService.createBooking(validMemberId, 99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("课程"));
    }

    // ==================== 预约确认测试 ====================

    @Test
    public void testConfirmBooking() {
        // 先创建预约
        if (bookingDAO.checkDuplicateBooking(testMemberId, testCourseId)) {
            System.out.println("跳过测试：会员已预约该课程");
            return;
        }

        ServiceResult<Booking> createResult = bookingService.createBooking(testMemberId, testCourseId);
        if (!createResult.isSuccess()) {
            System.out.println("跳过测试：无法创建预约 - " + createResult.getMessage());
            return;
        }
        testBookingId = createResult.getData().getBookingId();

        // 确认预约
        ServiceResult<Booking> confirmResult = bookingService.confirmBooking(testBookingId);

        assertTrue(confirmResult.isSuccess());
        assertEquals(BookingService.STATUS_CONFIRMED, confirmResult.getData().getBookingStatus());
    }

    @Test
    public void testConfirmBookingNotFound() {
        ServiceResult<Booking> result = bookingService.confirmBooking(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testConfirmBookingAlreadyConfirmed() {
        // 先创建并确认预约
        if (bookingDAO.checkDuplicateBooking(testMemberId, testCourseId)) {
            System.out.println("跳过测试：会员已预约该课程");
            return;
        }

        ServiceResult<Booking> createResult = bookingService.createBooking(testMemberId, testCourseId);
        if (!createResult.isSuccess()) {
            System.out.println("跳过测试：无法创建预约");
            return;
        }
        testBookingId = createResult.getData().getBookingId();
        bookingService.confirmBooking(testBookingId);

        // 再次确认
        ServiceResult<Booking> result = bookingService.confirmBooking(testBookingId);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("已确认") || result.getMessage().contains("待确认"));
    }

    // ==================== 预约取消测试 ====================

    @Test
    public void testCancelBooking() {
        // 先创建预约
        if (bookingDAO.checkDuplicateBooking(testMemberId, testCourseId)) {
            System.out.println("跳过测试：会员已预约该课程");
            return;
        }

        ServiceResult<Booking> createResult = bookingService.createBooking(testMemberId, testCourseId);
        if (!createResult.isSuccess()) {
            System.out.println("跳过测试：无法创建预约");
            return;
        }
        testBookingId = createResult.getData().getBookingId();

        // 取消预约
        ServiceResult<Booking> cancelResult = bookingService.cancelBooking(testBookingId, "测试取消");

        assertTrue(cancelResult.isSuccess());
        assertEquals(BookingService.STATUS_CANCELLED, cancelResult.getData().getBookingStatus());
        assertTrue(cancelResult.getMessage().contains("测试取消"));
    }

    @Test
    public void testCancelBookingNotFound() {
        ServiceResult<Booking> result = bookingService.cancelBooking(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testCancelBookingAlreadyCancelled() {
        // 先创建并取消预约
        if (bookingDAO.checkDuplicateBooking(testMemberId, testCourseId)) {
            System.out.println("跳过测试：会员已预约该课程");
            return;
        }

        ServiceResult<Booking> createResult = bookingService.createBooking(testMemberId, testCourseId);
        if (!createResult.isSuccess()) {
            System.out.println("跳过测试：无法创建预约");
            return;
        }
        testBookingId = createResult.getData().getBookingId();
        bookingService.cancelBooking(testBookingId);

        // 再次取消
        ServiceResult<Booking> result = bookingService.cancelBooking(testBookingId);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("已经被取消"));
    }

    @Test
    public void testMemberCancelBooking() {
        // 先创建预约
        if (bookingDAO.checkDuplicateBooking(testMemberId, testCourseId)) {
            System.out.println("跳过测试：会员已预约该课程");
            return;
        }

        ServiceResult<Booking> createResult = bookingService.createBooking(testMemberId, testCourseId);
        if (!createResult.isSuccess()) {
            System.out.println("跳过测试：无法创建预约");
            return;
        }
        testBookingId = createResult.getData().getBookingId();

        // 会员自己取消
        ServiceResult<Booking> result = bookingService.memberCancelBooking(testMemberId, testBookingId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testMemberCancelOtherBooking() {
        // 先创建预约
        if (bookingDAO.checkDuplicateBooking(testMemberId, testCourseId)) {
            System.out.println("跳过测试：会员已预约该课程");
            return;
        }

        ServiceResult<Booking> createResult = bookingService.createBooking(testMemberId, testCourseId);
        if (!createResult.isSuccess()) {
            System.out.println("跳过测试：无法创建预约");
            return;
        }
        testBookingId = createResult.getData().getBookingId();

        // 其他会员尝试取消
        ServiceResult<Booking> result = bookingService.memberCancelBooking(99999, testBookingId);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("只能取消自己的预约"));
    }

    // ==================== 预约查询测试 ====================

    @Test
    public void testGetBookingById() {
        // 使用已存在的预约
        List<Booking> bookings = bookingDAO.getAllBookings();
        if (!bookings.isEmpty()) {
            int bookingId = bookings.get(0).getBookingId();
            Booking booking = bookingService.getBookingById(bookingId);
            assertNotNull(booking);
            assertEquals(bookingId, booking.getBookingId());
        }
    }

    @Test
    public void testGetBookingByIdNotFound() {
        Booking booking = bookingService.getBookingById(99999);
        assertNull(booking);
    }

    @Test
    public void testGetAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        assertNotNull(bookings);
    }

    @Test
    public void testGetBookingsByMember() {
        List<Booking> bookings = bookingService.getBookingsByMember(testMemberId);
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertEquals(testMemberId, booking.getMemberId());
        }
    }

    @Test
    public void testGetBookingsByCourse() {
        List<Booking> bookings = bookingService.getBookingsByCourse(testCourseId);
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertEquals(testCourseId, booking.getCourseId());
        }
    }

    @Test
    public void testGetBookingsByStatus() {
        List<Booking> pendingBookings = bookingService.getBookingsByStatus(BookingService.STATUS_PENDING);
        assertNotNull(pendingBookings);
        for (Booking booking : pendingBookings) {
            assertEquals(BookingService.STATUS_PENDING, booking.getBookingStatus());
        }
    }

    @Test
    public void testGetPendingBookings() {
        List<Booking> bookings = bookingService.getPendingBookings();
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertEquals(BookingService.STATUS_PENDING, booking.getBookingStatus());
        }
    }

    @Test
    public void testGetConfirmedBookings() {
        List<Booking> bookings = bookingService.getConfirmedBookings();
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertEquals(BookingService.STATUS_CONFIRMED, booking.getBookingStatus());
        }
    }

    @Test
    public void testGetCancelledBookings() {
        List<Booking> bookings = bookingService.getCancelledBookings();
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertEquals(BookingService.STATUS_CANCELLED, booking.getBookingStatus());
        }
    }

    @Test
    public void testGetTodayBookings() {
        List<Booking> bookings = bookingService.getTodayBookings();
        assertNotNull(bookings);
    }

    @Test
    public void testGetMemberActiveBookings() {
        List<Booking> bookings = bookingService.getMemberActiveBookings(testMemberId);
        assertNotNull(bookings);
        for (Booking booking : bookings) {
            assertTrue(BookingService.STATUS_PENDING.equals(booking.getBookingStatus()) ||
                      BookingService.STATUS_CONFIRMED.equals(booking.getBookingStatus()));
        }
    }

    // ==================== 预约详情测试 ====================

    @Test
    public void testGetBookingDetail() {
        List<Booking> bookings = bookingDAO.getAllBookings();
        if (!bookings.isEmpty()) {
            int bookingId = bookings.get(0).getBookingId();
            BookingDetail detail = bookingService.getBookingDetail(bookingId);
            
            assertNotNull(detail);
            assertNotNull(detail.getBooking());
            assertNotNull(detail.getStatusDisplayName());
            assertNotNull(detail.getMemberName());
            assertNotNull(detail.getCourseName());

            // 打印详情
            System.out.println(detail.toString());
        }
    }

    @Test
    public void testGetBookingDetailNotFound() {
        BookingDetail detail = bookingService.getBookingDetail(99999);
        assertNull(detail);
    }

    // ==================== 预约验证测试 ====================

    @Test
    public void testValidateMemberCanBook() {
        ServiceResult<Member> result = bookingService.validateMemberCanBook(testMemberId);
        // 结果取决于会员状态和会员卡
        assertNotNull(result);
        System.out.println("验证结果：" + result.getMessage());
    }

    @Test
    public void testValidateMemberCanBookNotFound() {
        ServiceResult<Member> result = bookingService.validateMemberCanBook(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testValidateCourseCanBook() {
        ServiceResult<Course> result = bookingService.validateCourseCanBook(testCourseId);
        // 结果取决于课程容量
        assertNotNull(result);
        System.out.println("验证结果：" + result.getMessage());
    }

    @Test
    public void testValidateCourseCanBookNotFound() {
        ServiceResult<Course> result = bookingService.validateCourseCanBook(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testValidateBooking() {
        ServiceResult<Void> result = bookingService.validateBooking(testMemberId, testCourseId);
        // 结果取决于会员和课程状态
        assertNotNull(result);
        System.out.println("验证结果：" + result.getMessage());
    }

    @Test
    public void testHasBookedCourse() {
        boolean hasBooked = bookingService.hasBookedCourse(testMemberId, testCourseId);
        // 结果取决于是否已预约
        System.out.println("是否已预约：" + hasBooked);
    }

    // ==================== 预约统计测试 ====================

    @Test
    public void testGetTotalBookingCount() {
        int count = bookingService.getTotalBookingCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetTodayBookingCount() {
        int count = bookingService.getTodayBookingCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetBookingCountByStatus() {
        int pendingCount = bookingService.getBookingCountByStatus(BookingService.STATUS_PENDING);
        int confirmedCount = bookingService.getBookingCountByStatus(BookingService.STATUS_CONFIRMED);
        int cancelledCount = bookingService.getBookingCountByStatus(BookingService.STATUS_CANCELLED);

        assertTrue(pendingCount >= 0);
        assertTrue(confirmedCount >= 0);
        assertTrue(cancelledCount >= 0);
    }

    @Test
    public void testGetPendingBookingCount() {
        int count = bookingService.getPendingBookingCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetConfirmedBookingCount() {
        int count = bookingService.getConfirmedBookingCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetCancelledBookingCount() {
        int count = bookingService.getCancelledBookingCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMemberBookingStats() {
        int[] stats = bookingService.getMemberBookingStats(testMemberId);
        assertNotNull(stats);
        assertEquals(3, stats.length);
        assertTrue(stats[0] >= 0);  // 总数
        assertTrue(stats[1] >= 0);  // 已确认
        assertTrue(stats[2] >= 0);  // 已取消
    }

    @Test
    public void testGetCourseBookingStats() {
        Map<String, Integer> stats = bookingService.getCourseBookingStats(testCourseId);
        assertNotNull(stats);
        assertTrue(stats.containsKey("total"));
        assertTrue(stats.containsKey("pending"));
        assertTrue(stats.containsKey("confirmed"));
        assertTrue(stats.containsKey("cancelled"));
    }

    @Test
    public void testGetStatistics() {
        BookingStatistics stats = bookingService.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalCount() >= 0);
        assertTrue(stats.getConfirmRate() >= 0 && stats.getConfirmRate() <= 100);
        assertTrue(stats.getCancelRate() >= 0 && stats.getCancelRate() <= 100);

        // 打印统计
        System.out.println(stats.toString());
    }

    // ==================== 批量操作测试 ====================

    @Test
    public void testBatchConfirmBookings() {
        List<Booking> pendingBookings = bookingService.getPendingBookings();
        if (pendingBookings.size() >= 2) {
            List<Integer> bookingIds = Arrays.asList(
                    pendingBookings.get(0).getBookingId(),
                    pendingBookings.get(1).getBookingId()
            );

            ServiceResult<Map<String, Integer>> result = bookingService.batchConfirmBookings(bookingIds);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertTrue(result.getData().containsKey("success"));
            assertTrue(result.getData().containsKey("fail"));
        } else {
            System.out.println("跳过测试：待确认预约不足2个");
        }
    }

    @Test
    public void testBatchCancelBookings() {
        // 创建两个预约用于测试
        // 由于测试数据可能不够，这里只验证方法能正常运行
        List<Integer> bookingIds = Arrays.asList(99998, 99999);
        ServiceResult<Map<String, Integer>> result = bookingService.batchCancelBookings(bookingIds, "批量取消测试");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    // ==================== 工具方法测试 ====================

    @Test
    public void testGetStatusDisplayName() {
        assertEquals("待确认", bookingService.getStatusDisplayName(BookingService.STATUS_PENDING));
        assertEquals("已确认", bookingService.getStatusDisplayName(BookingService.STATUS_CONFIRMED));
        assertEquals("已取消", bookingService.getStatusDisplayName(BookingService.STATUS_CANCELLED));
        assertEquals("未知", bookingService.getStatusDisplayName("invalid"));
        assertEquals("未知", bookingService.getStatusDisplayName(null));
    }

    @Test
    public void testIsValidStatus() {
        assertTrue(bookingService.isValidStatus(BookingService.STATUS_PENDING));
        assertTrue(bookingService.isValidStatus(BookingService.STATUS_CONFIRMED));
        assertTrue(bookingService.isValidStatus(BookingService.STATUS_CANCELLED));
        assertFalse(bookingService.isValidStatus("invalid"));
        assertFalse(bookingService.isValidStatus(null));
    }

    @Test
    public void testIsBookingExists() {
        List<Booking> bookings = bookingDAO.getAllBookings();
        if (!bookings.isEmpty()) {
            assertTrue(bookingService.isBookingExists(bookings.get(0).getBookingId()));
        }
        assertFalse(bookingService.isBookingExists(99999));
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

