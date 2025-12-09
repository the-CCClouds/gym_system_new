package service;

import dao.MemberDAO;
import dao.MembershipCardDAO;
import dao.BookingDAO;
import dao.CheckInDAO;
import dao.OrderDAO;
import entity.Member;
import entity.MembershipCard;
import utils.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 会员服务类
 * 提供会员相关的业务逻辑处理
 * 
 * 主要功能：
 * - 会员注册（包含创建会员 + 自动创建会员卡）
 * - 会员信息管理
 * - 会员状态管理（激活、冻结、注销）
 * - 会员查询（综合查询、模糊搜索）
 * - 会员统计报表
 * 
 * @author GymSystem
 * @version 1.0
 */
public class MemberService {

    // ==================== 依赖的DAO ====================

    private MemberDAO memberDAO;
    private MembershipCardDAO cardDAO;
    private BookingDAO bookingDAO;
    private CheckInDAO checkInDAO;
    private OrderDAO orderDAO;

    // ==================== 构造方法 ====================

    public MemberService() {
        this.memberDAO = new MemberDAO();
        this.cardDAO = new MembershipCardDAO();
        this.bookingDAO = new BookingDAO();
        this.checkInDAO = new CheckInDAO();
        this.orderDAO = new OrderDAO();
    }

    // ==================== 会员注册 ====================

    /**
     * 会员注册（仅创建会员，不开卡）
     * 
     * @param name      姓名
     * @param phone     手机号
     * @param email     邮箱
     * @param gender    性别（male/female）
     * @param birthDate 出生日期
     * @return 注册结果，包含成功/失败信息和会员对象
     */
    public ServiceResult<Member> register(String name, String phone, String email, 
                                          String gender, Date birthDate) {
        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.failure("注册失败：姓名不能为空");
        }
        if (!memberDAO.isValidPhone(phone)) {
            return ServiceResult.failure("注册失败：无效的手机号格式");
        }
        if (!memberDAO.isValidEmail(email)) {
            return ServiceResult.failure("注册失败：无效的邮箱格式");
        }
        if (!memberDAO.isValidGender(gender)) {
            return ServiceResult.failure("注册失败：无效的性别，请使用 male 或 female");
        }

        // 检查手机号是否已存在
        if (memberDAO.isPhoneExists(phone)) {
            return ServiceResult.failure("注册失败：该手机号已被注册");
        }

        // 检查邮箱是否已存在
        if (memberDAO.isEmailExists(email)) {
            return ServiceResult.failure("注册失败：该邮箱已被注册");
        }

        // 创建会员对象
        Member member = new Member();
        member.setName(name.trim());
        member.setPhone(phone);
        member.setEmail(email);
        member.setGender(gender);
        member.setBirthDate(birthDate);
        member.setRegisterDate(DateUtils.now());
        member.setStatus(MemberDAO.STATUS_ACTIVE);

        // 保存到数据库
        if (memberDAO.addMember(member)) {
            return ServiceResult.success("注册成功", member);
        } else {
            return ServiceResult.failure("注册失败：数据库操作失败");
        }
    }

    /**
     * 会员注册并开卡（一站式服务）
     * 
     * @param name      姓名
     * @param phone     手机号
     * @param email     邮箱
     * @param gender    性别（male/female）
     * @param birthDate 出生日期
     * @param cardType  卡类型（1=月卡，2=年卡）
     * @return 注册结果
     */
    public ServiceResult<Member> registerWithCard(String name, String phone, String email,
                                                   String gender, Date birthDate, int cardType) {
        // 先注册会员
        ServiceResult<Member> registerResult = register(name, phone, email, gender, birthDate);
        if (!registerResult.isSuccess()) {
            return registerResult;
        }

        Member member = registerResult.getData();

        // 开卡
        boolean cardCreated;
        String cardTypeName;
        if (cardType == MembershipCardDAO.TYPE_MONTHLY) {
            cardCreated = cardDAO.createMonthlyCard(member.getId());
            cardTypeName = "月卡";
        } else if (cardType == MembershipCardDAO.TYPE_YEARLY) {
            cardCreated = cardDAO.createYearlyCard(member.getId());
            cardTypeName = "年卡";
        } else {
            // 卡类型无效，但会员已创建，返回成功但提示卡创建失败
            return ServiceResult.success("会员注册成功，但开卡失败：无效的卡类型", member);
        }

        if (cardCreated) {
            return ServiceResult.success("注册成功，已开通" + cardTypeName, member);
        } else {
            return ServiceResult.success("会员注册成功，但开卡失败", member);
        }
    }

    /**
     * 会员注册并开月卡
     */
    public ServiceResult<Member> registerWithMonthlyCard(String name, String phone, String email,
                                                          String gender, Date birthDate) {
        return registerWithCard(name, phone, email, gender, birthDate, MembershipCardDAO.TYPE_MONTHLY);
    }

    /**
     * 会员注册并开年卡
     */
    public ServiceResult<Member> registerWithYearlyCard(String name, String phone, String email,
                                                         String gender, Date birthDate) {
        return registerWithCard(name, phone, email, gender, birthDate, MembershipCardDAO.TYPE_YEARLY);
    }

    // ==================== 会员信息管理 ====================

    /**
     * 更新会员基本信息
     * 
     * @param memberId  会员ID
     * @param name      姓名
     * @param email     邮箱
     * @param gender    性别
     * @param birthDate 出生日期
     * @return 更新结果
     */
    public ServiceResult<Member> updateMemberInfo(int memberId, String name, String email,
                                                   String gender, Date birthDate) {
        // 查询会员是否存在
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("更新失败：会员不存在");
        }

        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.failure("更新失败：姓名不能为空");
        }
        if (!memberDAO.isValidEmail(email)) {
            return ServiceResult.failure("更新失败：无效的邮箱格式");
        }
        if (!memberDAO.isValidGender(gender)) {
            return ServiceResult.failure("更新失败：无效的性别");
        }

        // 检查邮箱是否被其他会员使用
        Member existingMember = getMemberByEmail(email);
        if (existingMember != null && existingMember.getId() != memberId) {
            return ServiceResult.failure("更新失败：该邮箱已被其他会员使用");
        }

        // 更新信息
        member.setName(name.trim());
        member.setEmail(email);
        member.setGender(gender);
        member.setBirthDate(birthDate);

        if (memberDAO.updateMember(member)) {
            return ServiceResult.success("更新成功", member);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    /**
     * 更新会员手机号
     * 
     * @param memberId 会员ID
     * @param newPhone 新手机号
     * @return 更新结果
     */
    public ServiceResult<Member> updateMemberPhone(int memberId, String newPhone) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("更新失败：会员不存在");
        }

        if (!memberDAO.isValidPhone(newPhone)) {
            return ServiceResult.failure("更新失败：无效的手机号格式");
        }

        // 检查手机号是否被其他会员使用
        Member existingMember = memberDAO.getMemberByPhone(newPhone);
        if (existingMember != null && existingMember.getId() != memberId) {
            return ServiceResult.failure("更新失败：该手机号已被其他会员使用");
        }

        member.setPhone(newPhone);

        if (memberDAO.updateMember(member)) {
            return ServiceResult.success("手机号更新成功", member);
        } else {
            return ServiceResult.failure("更新失败：数据库操作失败");
        }
    }

    // ==================== 会员状态管理 ====================

    /**
     * 激活会员
     * 
     * @param memberId 会员ID
     * @return 操作结果
     */
    public ServiceResult<Void> activateMember(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("操作失败：会员不存在");
        }

        if (MemberDAO.STATUS_ACTIVE.equals(member.getStatus())) {
            return ServiceResult.failure("操作失败：会员已是激活状态");
        }

        if (memberDAO.activateMember(memberId)) {
            return ServiceResult.success("会员已激活");
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败");
        }
    }

    /**
     * 冻结会员
     * 
     * @param memberId 会员ID
     * @param reason   冻结原因（可选）
     * @return 操作结果
     */
    public ServiceResult<Void> freezeMember(int memberId, String reason) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("操作失败：会员不存在");
        }

        if (MemberDAO.STATUS_FROZEN.equals(member.getStatus())) {
            return ServiceResult.failure("操作失败：会员已是冻结状态");
        }

        // 检查是否有未完成的签到
        if (checkInDAO.hasActiveCheckIn(memberId)) {
            return ServiceResult.failure("操作失败：会员当前在馆中，请先签退");
        }

        if (memberDAO.freezeMember(memberId)) {
            String message = "会员已冻结";
            if (reason != null && !reason.trim().isEmpty()) {
                message += "，原因：" + reason;
            }
            return ServiceResult.success(message);
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败");
        }
    }

    /**
     * 冻结会员（无原因）
     */
    public ServiceResult<Void> freezeMember(int memberId) {
        return freezeMember(memberId, null);
    }

    /**
     * 注销会员（设为停用状态）
     * 
     * @param memberId 会员ID
     * @return 操作结果
     */
    public ServiceResult<Void> deactivateMember(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("操作失败：会员不存在");
        }

        if (MemberDAO.STATUS_INACTIVE.equals(member.getStatus())) {
            return ServiceResult.failure("操作失败：会员已是停用状态");
        }

        // 检查是否有未完成的签到
        if (checkInDAO.hasActiveCheckIn(memberId)) {
            return ServiceResult.failure("操作失败：会员当前在馆中，请先签退");
        }

        // 检查是否有待处理的预约
        int[] bookingStats = bookingDAO.getMemberBookingStats(memberId);
        // bookingStats[0] = 总预约数, 需要检查是否有pending状态的预约
        List<entity.Booking> pendingBookings = bookingDAO.getBookingsByMemberId(memberId);
        long pendingCount = pendingBookings.stream()
                .filter(b -> BookingDAO.STATUS_PENDING.equals(b.getBookingStatus()) ||
                            BookingDAO.STATUS_CONFIRMED.equals(b.getBookingStatus()))
                .count();
        if (pendingCount > 0) {
            return ServiceResult.failure("操作失败：会员有" + pendingCount + "个未完成的预约，请先取消");
        }

        if (memberDAO.updateMemberStatus(memberId, MemberDAO.STATUS_INACTIVE)) {
            return ServiceResult.success("会员已注销");
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败");
        }
    }

    /**
     * 删除会员（物理删除，慎用）
     * 
     * @param memberId 会员ID
     * @param force    是否强制删除（忽略关联数据检查）
     * @return 操作结果
     */
    public ServiceResult<Void> deleteMember(int memberId, boolean force) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("操作失败：会员不存在");
        }

        if (!force) {
            // 检查关联数据
            List<MembershipCard> cards = cardDAO.getByMemberId(memberId);
            if (!cards.isEmpty()) {
                return ServiceResult.failure("操作失败：会员有" + cards.size() + "张会员卡记录，无法删除");
            }

            List<entity.Booking> bookings = bookingDAO.getBookingsByMemberId(memberId);
            if (!bookings.isEmpty()) {
                return ServiceResult.failure("操作失败：会员有" + bookings.size() + "条预约记录，无法删除");
            }

            List<entity.CheckIn> checkIns = checkInDAO.getCheckInsByMemberId(memberId);
            if (!checkIns.isEmpty()) {
                return ServiceResult.failure("操作失败：会员有" + checkIns.size() + "条签到记录，无法删除");
            }

            List<entity.Order> orders = orderDAO.getOrdersByMemberId(memberId);
            if (!orders.isEmpty()) {
                return ServiceResult.failure("操作失败：会员有" + orders.size() + "条订单记录，无法删除");
            }
        }

        if (memberDAO.deleteMember(memberId)) {
            return ServiceResult.success("会员已删除");
        } else {
            return ServiceResult.failure("操作失败：数据库操作失败，可能存在关联数据");
        }
    }

    /**
     * 删除会员（非强制）
     */
    public ServiceResult<Void> deleteMember(int memberId) {
        return deleteMember(memberId, false);
    }

    // ... 在 MemberService.java 中添加 ...

    /**
     * 会员购买会员卡（用于已经注册的会员）
     * * @param memberId 会员ID
     * @param cardType 卡类型 (1=月卡, 2=年卡)
     * @return 操作结果
     */
    public ServiceResult<Void> buyCard(int memberId, int cardType) {
        // 1. 验证会员是否存在
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("购买失败：会员不存在");
        }

        if (cardDAO.hasMemberValidCard(memberId)) {
            return ServiceResult.failure("您当前已有有效的会员卡，无需重复购买！");
        }

        // 3. 执行开卡
        boolean success = false;
        String typeName = "";

        if (cardType == MembershipCardDAO.TYPE_MONTHLY) {
            success = cardDAO.createMonthlyCard(memberId);
            typeName = "月卡";
        } else if (cardType == MembershipCardDAO.TYPE_YEARLY) {
            success = cardDAO.createYearlyCard(memberId);
            typeName = "年卡";
        } else {
            return ServiceResult.failure("无效的卡类型");
        }

        if (success) {
            return ServiceResult.success("购买成功！您已开通 " + typeName);
        } else {
            return ServiceResult.failure("购买失败：数据库操作错误");
        }
    }

    // ==================== 会员查询 ====================

    /**
     * 根据ID查询会员
     * 
     * @param memberId 会员ID
     * @return 会员对象
     */
    public Member getMemberById(int memberId) {
        return memberDAO.getMemberById(memberId);
    }

    /**
     * 根据手机号查询会员
     * 
     * @param phone 手机号
     * @return 会员对象
     */
    public Member getMemberByPhone(String phone) {
        return memberDAO.getMemberByPhone(phone);
    }

    /**
     * 根据邮箱查询会员
     * 
     * @param email 邮箱
     * @return 会员对象
     */
    public Member getMemberByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        List<Member> allMembers = memberDAO.getAllMembers();
        for (Member member : allMembers) {
            if (email.equals(member.getEmail())) {
                return member;
            }
        }
        return null;
    }

    /**
     * 查询所有会员
     * 
     * @return 会员列表
     */
    public List<Member> getAllMembers() {
        return memberDAO.getAllMembers();
    }

    /**
     * 查询所有活跃会员
     * 
     * @return 活跃会员列表
     */
    public List<Member> getActiveMembers() {
        return memberDAO.getActiveMembers();
    }

    /**
     * 根据姓名模糊搜索会员
     * 
     * @param name 姓名关键字
     * @return 会员列表
     */
    public List<Member> searchByName(String name) {
        return memberDAO.searchMembersByName(name);
    }

    /**
     * 根据手机号模糊搜索会员
     * 
     * @param phone 手机号关键字
     * @return 会员列表
     */
    public List<Member> searchByPhone(String phone) {
        return memberDAO.searchByPhone(phone);
    }

    /**
     * 综合搜索会员（姓名或手机号）
     * 
     * @param keyword 搜索关键字
     * @return 会员列表
     */
    public List<Member> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return memberDAO.getAllMembers();
        }

        keyword = keyword.trim();

        // 如果是纯数字，按手机号搜索
        if (keyword.matches("\\d+")) {
            return memberDAO.searchByPhone(keyword);
        }

        // 否则按姓名搜索
        return memberDAO.searchMembersByName(keyword);
    }

    /**
     * 根据状态查询会员
     * 
     * @param status 状态（active/frozen/inactive）
     * @return 会员列表
     */
    public List<Member> getMembersByStatus(String status) {
        return memberDAO.getMembersByStatus(status);
    }

    /**
     * 根据性别查询会员
     * 
     * @param gender 性别（male/female）
     * @return 会员列表
     */
    public List<Member> getMembersByGender(String gender) {
        return memberDAO.getMembersByGender(gender);
    }

    /**
     * 根据年龄范围查询会员
     * 
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 会员列表
     */
    public List<Member> getMembersByAgeRange(int minAge, int maxAge) {
        return memberDAO.getMembersByAgeRange(minAge, maxAge);
    }

    /**
     * 获取拥有有效会员卡的会员
     * 
     * @return 会员列表
     */
    public List<Member> getMembersWithValidCard() {
        return memberDAO.getMembersWithValidCard();
    }

    // ==================== 会员详情 ====================

    /**
     * 获取会员详细信息（包含会员卡、统计等）
     * 
     * @param memberId 会员ID
     * @return 会员详情
     */
    public MemberDetail getMemberDetail(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return null;
        }

        MemberDetail detail = new MemberDetail();
        detail.setMember(member);

        // 会员卡信息
        detail.setMembershipCards(cardDAO.getByMemberId(memberId));
        detail.setActiveCard(cardDAO.getActiveMembershipCard(memberId));

        // 统计信息
        detail.setAge(memberDAO.getMemberAge(memberId));
        detail.setMembershipDays(memberDAO.getMembershipDays(memberId));
        detail.setTotalCheckInCount(checkInDAO.getTotalCheckInCount(memberId));
        detail.setMonthlyCheckInCount(checkInDAO.getMonthlyCheckInCount(memberId));
        detail.setTotalSpending(orderDAO.getMemberTotalSpending(memberId));

        // 预约统计
        int[] bookingStats = bookingDAO.getMemberBookingStats(memberId);
        detail.setTotalBookingCount(bookingStats[0]);
        detail.setConfirmedBookingCount(bookingStats[1]);
        detail.setCancelledBookingCount(bookingStats[2]);

        // 当前状态
        detail.setCurrentlyCheckedIn(checkInDAO.hasActiveCheckIn(memberId));
        detail.setHasValidCard(cardDAO.hasMemberValidCard(memberId));

        return detail;
    }

    // ==================== 会员统计 ====================

    /**
     * 获取会员总数
     * 
     * @return 会员总数
     */
    public int getTotalMemberCount() {
        return memberDAO.getTotalMemberCount();
    }

    /**
     * 获取活跃会员数
     * 
     * @return 活跃会员数
     */
    public int getActiveMemberCount() {
        return memberDAO.getActiveMemberCount();
    }

    /**
     * 获取今日新注册会员数
     * 
     * @return 今日新注册数
     */
    public int getTodayNewMemberCount() {
        return memberDAO.getTodayNewMemberCount();
    }

    /**
     * 获取本月新注册会员数
     * 
     * @return 本月新注册数
     */
    public int getMonthlyNewMemberCount() {
        return memberDAO.getMonthlyNewMemberCount();
    }

    /**
     * 按状态统计会员数量
     * 
     * @return Map<状态, 数量>
     */
    public Map<String, Integer> getMemberCountByStatus() {
        return memberDAO.getMemberCountByStatus();
    }

    /**
     * 按性别统计会员数量
     * 
     * @return Map<性别, 数量>
     */
    public Map<String, Integer> getMemberCountByGender() {
        return memberDAO.getMemberCountByGender();
    }

    /**
     * 获取会员统计概览
     * 
     * @return 统计概览
     */
    public MemberStatistics getStatistics() {
        MemberStatistics stats = new MemberStatistics();

        stats.setTotalCount(memberDAO.getTotalMemberCount());
        stats.setActiveCount(memberDAO.getActiveMemberCount());
        stats.setFrozenCount(memberDAO.getMembersByStatus(MemberDAO.STATUS_FROZEN).size());
        stats.setInactiveCount(memberDAO.getMembersByStatus(MemberDAO.STATUS_INACTIVE).size());

        stats.setTodayNewCount(memberDAO.getTodayNewMemberCount());
        stats.setMonthlyNewCount(memberDAO.getMonthlyNewMemberCount());

        stats.setMaleCount(memberDAO.getMembersByGender(MemberDAO.GENDER_MALE).size());
        stats.setFemaleCount(memberDAO.getMembersByGender(MemberDAO.GENDER_FEMALE).size());

        stats.setMembersWithValidCardCount(memberDAO.getMembersWithValidCard().size());

        return stats;
    }



    // ==================== 会员验证 ====================

    /**
     * 验证会员是否可以进行操作（状态为激活）
     * 
     * @param memberId 会员ID
     * @return 验证结果
     */
    public ServiceResult<Member> validateMemberActive(int memberId) {
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            return ServiceResult.failure("会员不存在");
        }

        if (!MemberDAO.STATUS_ACTIVE.equals(member.getStatus())) {
            String statusName = memberDAO.getStatusDisplayName(member.getStatus());
            return ServiceResult.failure("会员状态为「" + statusName + "」，无法进行操作");
        }

        return ServiceResult.success("验证通过", member);
    }

    /**
     * 验证会员是否可以预约/签到（状态激活且有有效会员卡）
     * 
     * @param memberId 会员ID
     * @return 验证结果
     */
    public ServiceResult<Member> validateMemberCanAccess(int memberId) {
        // 先验证会员状态
        ServiceResult<Member> activeResult = validateMemberActive(memberId);
        if (!activeResult.isSuccess()) {
            return activeResult;
        }

        // 再验证会员卡
        if (!cardDAO.hasMemberValidCard(memberId)) {
            return ServiceResult.failure("会员没有有效的会员卡，请先开卡或续费");
        }

        return ServiceResult.success("验证通过", activeResult.getData());
    }

    /**
     * 检查手机号是否已存在
     * 
     * @param phone 手机号
     * @return true表示已存在
     */
    public boolean isPhoneExists(String phone) {
        return memberDAO.isPhoneExists(phone);
    }

    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @return true表示已存在
     */
    public boolean isEmailExists(String email) {
        return memberDAO.isEmailExists(email);
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

    // ==================== 内部类：会员详情 ====================

    /**
     * 会员详细信息（包含关联数据和统计）
     */
    public static class MemberDetail {
        private Member member;
        private List<MembershipCard> membershipCards;
        private MembershipCard activeCard;
        private int age;
        private long membershipDays;
        private int totalCheckInCount;
        private int monthlyCheckInCount;
        private double totalSpending;
        private int totalBookingCount;
        private int confirmedBookingCount;
        private int cancelledBookingCount;
        private boolean currentlyCheckedIn;
        private boolean hasValidCard;

        // Getters and Setters
        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public List<MembershipCard> getMembershipCards() {
            return membershipCards;
        }

        public void setMembershipCards(List<MembershipCard> membershipCards) {
            this.membershipCards = membershipCards;
        }

        public MembershipCard getActiveCard() {
            return activeCard;
        }

        public void setActiveCard(MembershipCard activeCard) {
            this.activeCard = activeCard;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public long getMembershipDays() {
            return membershipDays;
        }

        public void setMembershipDays(long membershipDays) {
            this.membershipDays = membershipDays;
        }

        public int getTotalCheckInCount() {
            return totalCheckInCount;
        }

        public void setTotalCheckInCount(int totalCheckInCount) {
            this.totalCheckInCount = totalCheckInCount;
        }

        public int getMonthlyCheckInCount() {
            return monthlyCheckInCount;
        }

        public void setMonthlyCheckInCount(int monthlyCheckInCount) {
            this.monthlyCheckInCount = monthlyCheckInCount;
        }

        public double getTotalSpending() {
            return totalSpending;
        }

        public void setTotalSpending(double totalSpending) {
            this.totalSpending = totalSpending;
        }

        public int getTotalBookingCount() {
            return totalBookingCount;
        }

        public void setTotalBookingCount(int totalBookingCount) {
            this.totalBookingCount = totalBookingCount;
        }

        public int getConfirmedBookingCount() {
            return confirmedBookingCount;
        }

        public void setConfirmedBookingCount(int confirmedBookingCount) {
            this.confirmedBookingCount = confirmedBookingCount;
        }

        public int getCancelledBookingCount() {
            return cancelledBookingCount;
        }

        public void setCancelledBookingCount(int cancelledBookingCount) {
            this.cancelledBookingCount = cancelledBookingCount;
        }

        public boolean isCurrentlyCheckedIn() {
            return currentlyCheckedIn;
        }

        public void setCurrentlyCheckedIn(boolean currentlyCheckedIn) {
            this.currentlyCheckedIn = currentlyCheckedIn;
        }

        public boolean isHasValidCard() {
            return hasValidCard;
        }

        public void setHasValidCard(boolean hasValidCard) {
            this.hasValidCard = hasValidCard;
        }

        /**
         * 获取会员卡剩余天数
         */
        public int getCardRemainingDays() {
            if (activeCard == null) {
                return 0;
            }
            return (int) DateUtils.daysRemaining(activeCard.getEndDate());
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 会员详情 ========\n");
            sb.append("基本信息：").append(member.getBasicInfo()).append("\n");
            sb.append("年龄：").append(age).append("岁\n");
            sb.append("会籍时长：").append(membershipDays).append("天\n");
            sb.append("当前状态：").append(currentlyCheckedIn ? "在馆中" : "未签到").append("\n");
            sb.append("\n");
            sb.append("会员卡状态：").append(hasValidCard ? "有效" : "无效/未开卡").append("\n");
            if (activeCard != null) {
                sb.append("当前会员卡：").append(activeCard.getCardType())
                  .append("，剩余").append(getCardRemainingDays()).append("天\n");
            }
            sb.append("历史会员卡数：").append(membershipCards != null ? membershipCards.size() : 0).append("\n");
            sb.append("\n");
            sb.append("签到统计：本月").append(monthlyCheckInCount).append("次，累计").append(totalCheckInCount).append("次\n");
            sb.append("预约统计：总").append(totalBookingCount).append("次，完成").append(confirmedBookingCount)
              .append("次，取消").append(cancelledBookingCount).append("次\n");
            sb.append("累计消费：¥").append(String.format("%.2f", totalSpending)).append("\n");
            return sb.toString();
        }
    }

    // ==================== 内部类：会员统计 ====================

    /**
     * 会员统计信息
     */
    public static class MemberStatistics {
        private int totalCount;
        private int activeCount;
        private int frozenCount;
        private int inactiveCount;
        private int todayNewCount;
        private int monthlyNewCount;
        private int maleCount;
        private int femaleCount;
        private int membersWithValidCardCount;

        // Getters and Setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public void setActiveCount(int activeCount) {
            this.activeCount = activeCount;
        }

        public int getFrozenCount() {
            return frozenCount;
        }

        public void setFrozenCount(int frozenCount) {
            this.frozenCount = frozenCount;
        }

        public int getInactiveCount() {
            return inactiveCount;
        }

        public void setInactiveCount(int inactiveCount) {
            this.inactiveCount = inactiveCount;
        }

        public int getTodayNewCount() {
            return todayNewCount;
        }

        public void setTodayNewCount(int todayNewCount) {
            this.todayNewCount = todayNewCount;
        }

        public int getMonthlyNewCount() {
            return monthlyNewCount;
        }

        public void setMonthlyNewCount(int monthlyNewCount) {
            this.monthlyNewCount = monthlyNewCount;
        }

        public int getMaleCount() {
            return maleCount;
        }

        public void setMaleCount(int maleCount) {
            this.maleCount = maleCount;
        }

        public int getFemaleCount() {
            return femaleCount;
        }

        public void setFemaleCount(int femaleCount) {
            this.femaleCount = femaleCount;
        }

        public int getMembersWithValidCardCount() {
            return membersWithValidCardCount;
        }

        public void setMembersWithValidCardCount(int membersWithValidCardCount) {
            this.membersWithValidCardCount = membersWithValidCardCount;
        }

        /**
         * 获取活跃率（活跃会员/总会员）
         */
        public double getActiveRate() {
            return totalCount > 0 ? (double) activeCount / totalCount * 100 : 0;
        }

        /**
         * 获取有效卡率（有有效卡的会员/总会员）
         */
        public double getValidCardRate() {
            return totalCount > 0 ? (double) membersWithValidCardCount / totalCount * 100 : 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("======== 会员统计 ========\n");
            sb.append("会员总数：").append(totalCount).append("\n");
            sb.append("  - 活跃：").append(activeCount).append(" (").append(String.format("%.1f", getActiveRate())).append("%)\n");
            sb.append("  - 冻结：").append(frozenCount).append("\n");
            sb.append("  - 停用：").append(inactiveCount).append("\n");
            sb.append("\n");
            sb.append("新增会员：\n");
            sb.append("  - 今日：").append(todayNewCount).append("\n");
            sb.append("  - 本月：").append(monthlyNewCount).append("\n");
            sb.append("\n");
            sb.append("性别分布：\n");
            sb.append("  - 男：").append(maleCount).append("\n");
            sb.append("  - 女：").append(femaleCount).append("\n");
            sb.append("\n");
            sb.append("有效会员卡：").append(membersWithValidCardCount)
              .append(" (").append(String.format("%.1f", getValidCardRate())).append("%)\n");
            return sb.toString();
        }
    }
}

