import dao.MemberDAO;
import dao.MembershipCardDAO;
import entity.Member;
import entity.MembershipCard;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.MemberService;
import service.MemberService.MemberDetail;
import service.MemberService.MemberStatistics;
import service.MemberService.ServiceResult;
import utils.DateUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * MemberService 测试类
 * 
 * 测试前提：
 * - 数据库中存在会员数据
 * - 会员ID=1的张三存在
 */
public class MemberServiceTest {

    private MemberService memberService;
    private MemberDAO memberDAO;
    private MembershipCardDAO cardDAO;
    private int testMemberId;  // 用于清理测试数据

    @Before
    public void setUp() {
        memberService = new MemberService();
        memberDAO = new MemberDAO();
        cardDAO = new MembershipCardDAO();
        testMemberId = 0;
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (testMemberId > 0) {
            // 先删除会员卡
            List<MembershipCard> cards = cardDAO.getByMemberId(testMemberId);
            for (MembershipCard card : cards) {
                cardDAO.deleteMembershipCard(card.getCardId());
            }
            // 再删除会员
            memberDAO.deleteMember(testMemberId);
            testMemberId = 0;
        }
    }

    // ==================== 会员注册测试 ====================

    @Test
    public void testRegister() {
        ServiceResult<Member> result = memberService.register(
                "测试用户Service",
                "13811112222",
                "testservice@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.addYears(DateUtils.now(), -25)
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("测试用户Service", result.getData().getName());
        assertEquals(MemberDAO.STATUS_ACTIVE, result.getData().getStatus());

        testMemberId = result.getData().getId();
    }

    @Test
    public void testRegisterWithEmptyName() {
        ServiceResult<Member> result = memberService.register(
                "",
                "13811112223",
                "test2@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("姓名不能为空"));
    }

    @Test
    public void testRegisterWithInvalidPhone() {
        ServiceResult<Member> result = memberService.register(
                "测试用户",
                "123",
                "test3@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("手机号"));
    }

    @Test
    public void testRegisterWithInvalidEmail() {
        ServiceResult<Member> result = memberService.register(
                "测试用户",
                "13811112224",
                "invalid-email",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("邮箱"));
    }

    @Test
    public void testRegisterWithInvalidGender() {
        ServiceResult<Member> result = memberService.register(
                "测试用户",
                "13811112225",
                "test4@email.com",
                "invalid",
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("性别"));
    }

    @Test
    public void testRegisterWithDuplicatePhone() {
        // 使用已存在的手机号（张三的手机号）
        ServiceResult<Member> result = memberService.register(
                "测试用户",
                "13900001111",
                "newtest@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("手机号已被注册"));
    }

    @Test
    public void testRegisterWithMonthlyCard() {
        ServiceResult<Member> result = memberService.registerWithMonthlyCard(
                "测试月卡用户",
                "13811113333",
                "monthlycard@email.com",
                MemberDAO.GENDER_FEMALE,
                DateUtils.addYears(DateUtils.now(), -28)
        );

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("月卡"));

        testMemberId = result.getData().getId();

        // 验证会员卡已创建
        MembershipCard card = cardDAO.getActiveMembershipCard(testMemberId);
        assertNotNull(card);
        assertEquals(MembershipCardDAO.TYPE_MONTHLY, card.getTypeId());
    }

    @Test
    public void testRegisterWithYearlyCard() {
        ServiceResult<Member> result = memberService.registerWithYearlyCard(
                "测试年卡用户",
                "13811114444",
                "yearlycard@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.addYears(DateUtils.now(), -30)
        );

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("年卡"));

        testMemberId = result.getData().getId();

        // 验证会员卡已创建
        MembershipCard card = cardDAO.getActiveMembershipCard(testMemberId);
        assertNotNull(card);
        assertEquals(MembershipCardDAO.TYPE_YEARLY, card.getTypeId());
    }

    // ==================== 会员信息管理测试 ====================

    @Test
    public void testUpdateMemberInfo() {
        // 先创建测试会员
        ServiceResult<Member> registerResult = memberService.register(
                "待更新用户",
                "13811115555",
                "toupdate@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.addYears(DateUtils.now(), -25)
        );
        assertTrue(registerResult.isSuccess());
        testMemberId = registerResult.getData().getId();

        // 更新信息
        ServiceResult<Member> updateResult = memberService.updateMemberInfo(
                testMemberId,
                "已更新用户",
                "updated@email.com",
                MemberDAO.GENDER_FEMALE,
                DateUtils.addYears(DateUtils.now(), -26)
        );

        assertTrue(updateResult.isSuccess());
        assertEquals("已更新用户", updateResult.getData().getName());
        assertEquals("updated@email.com", updateResult.getData().getEmail());
        assertEquals(MemberDAO.GENDER_FEMALE, updateResult.getData().getGender());
    }

    @Test
    public void testUpdateMemberInfoNotFound() {
        ServiceResult<Member> result = memberService.updateMemberInfo(
                99999,
                "测试",
                "test@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("会员不存在"));
    }

    @Test
    public void testUpdateMemberPhone() {
        // 先创建测试会员
        ServiceResult<Member> registerResult = memberService.register(
                "手机更新用户",
                "13811116666",
                "phoneupdate@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );
        assertTrue(registerResult.isSuccess());
        testMemberId = registerResult.getData().getId();

        // 更新手机号
        ServiceResult<Member> updateResult = memberService.updateMemberPhone(
                testMemberId,
                "13899998888"
        );

        assertTrue(updateResult.isSuccess());
        assertEquals("13899998888", updateResult.getData().getPhone());
    }

    @Test
    public void testUpdateMemberPhoneInvalid() {
        ServiceResult<Member> result = memberService.updateMemberPhone(1, "123");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("手机号"));
    }

    // ==================== 会员状态管理测试 ====================

    @Test
    public void testActivateMember() {
        // 先冻结会员1
        memberDAO.freezeMember(1);

        ServiceResult<Void> result = memberService.activateMember(1);
        assertTrue(result.isSuccess());

        Member member = memberDAO.getMemberById(1);
        assertEquals(MemberDAO.STATUS_ACTIVE, member.getStatus());
    }

    @Test
    public void testActivateMemberAlreadyActive() {
        // 确保会员1是激活状态
        memberDAO.activateMember(1);

        ServiceResult<Void> result = memberService.activateMember(1);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("已是激活状态"));
    }

    @Test
    public void testFreezeMember() {
        // 确保会员1是激活状态
        memberDAO.activateMember(1);

        ServiceResult<Void> result = memberService.freezeMember(1, "测试冻结");
        assertTrue(result.isSuccess());

        Member member = memberDAO.getMemberById(1);
        assertEquals(MemberDAO.STATUS_FROZEN, member.getStatus());

        // 恢复
        memberDAO.activateMember(1);
    }

    @Test
    public void testFreezeMemberWithReason() {
        memberDAO.activateMember(1);

        ServiceResult<Void> result = memberService.freezeMember(1, "违规操作");
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("违规操作"));

        // 恢复
        memberDAO.activateMember(1);
    }

    @Test
    public void testFreezeMemberAlreadyFrozen() {
        memberDAO.freezeMember(1);

        ServiceResult<Void> result = memberService.freezeMember(1);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("已是冻结状态"));

        // 恢复
        memberDAO.activateMember(1);
    }

    @Test
    public void testDeactivateMember() {
        // 创建测试会员
        ServiceResult<Member> registerResult = memberService.register(
                "待注销用户",
                "13811117777",
                "deactivate@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );
        assertTrue(registerResult.isSuccess());
        testMemberId = registerResult.getData().getId();

        ServiceResult<Void> result = memberService.deactivateMember(testMemberId);
        assertTrue(result.isSuccess());

        Member member = memberDAO.getMemberById(testMemberId);
        assertEquals(MemberDAO.STATUS_INACTIVE, member.getStatus());
    }

    @Test
    public void testDeactivateMemberNotFound() {
        ServiceResult<Void> result = memberService.deactivateMember(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("会员不存在"));
    }

    // ==================== 会员查询测试 ====================

    @Test
    public void testGetMemberById() {
        Member member = memberService.getMemberById(1);
        assertNotNull(member);
        assertEquals(1, member.getId());
        assertEquals("张三", member.getName());
    }

    @Test
    public void testGetMemberByIdNotFound() {
        Member member = memberService.getMemberById(99999);
        assertNull(member);
    }

    @Test
    public void testGetMemberByPhone() {
        Member member = memberService.getMemberByPhone("13900001111");
        assertNotNull(member);
        assertEquals("张三", member.getName());
    }

    @Test
    public void testGetAllMembers() {
        List<Member> members = memberService.getAllMembers();
        assertNotNull(members);
        assertTrue(members.size() > 0);
    }

    @Test
    public void testGetActiveMembers() {
        List<Member> members = memberService.getActiveMembers();
        assertNotNull(members);
        for (Member member : members) {
            assertEquals(MemberDAO.STATUS_ACTIVE, member.getStatus());
        }
    }

    @Test
    public void testSearchByName() {
        List<Member> members = memberService.searchByName("张");
        assertNotNull(members);
        for (Member member : members) {
            assertTrue(member.getName().contains("张"));
        }
    }

    @Test
    public void testSearchByPhone() {
        List<Member> members = memberService.searchByPhone("139");
        assertNotNull(members);
        for (Member member : members) {
            assertTrue(member.getPhone().contains("139"));
        }
    }

    @Test
    public void testSearch() {
        // 按姓名搜索
        List<Member> nameResults = memberService.search("张三");
        assertNotNull(nameResults);

        // 按手机号搜索
        List<Member> phoneResults = memberService.search("139");
        assertNotNull(phoneResults);

        // 空搜索返回所有
        List<Member> allResults = memberService.search("");
        assertNotNull(allResults);
        assertEquals(memberService.getAllMembers().size(), allResults.size());
    }

    @Test
    public void testGetMembersByStatus() {
        List<Member> activeMembers = memberService.getMembersByStatus(MemberDAO.STATUS_ACTIVE);
        assertNotNull(activeMembers);
        for (Member member : activeMembers) {
            assertEquals(MemberDAO.STATUS_ACTIVE, member.getStatus());
        }
    }

    @Test
    public void testGetMembersByGender() {
        List<Member> maleMembers = memberService.getMembersByGender(MemberDAO.GENDER_MALE);
        assertNotNull(maleMembers);
        for (Member member : maleMembers) {
            assertEquals(MemberDAO.GENDER_MALE, member.getGender());
        }
    }

    @Test
    public void testGetMembersByAgeRange() {
        List<Member> members = memberService.getMembersByAgeRange(20, 40);
        assertNotNull(members);
        for (Member member : members) {
            int age = DateUtils.calculateAge(member.getBirthDate());
            assertTrue(age >= 20 && age <= 40);
        }
    }

    @Test
    public void testGetMembersWithValidCard() {
        List<Member> members = memberService.getMembersWithValidCard();
        assertNotNull(members);
    }

    // ==================== 会员详情测试 ====================

    @Test
    public void testGetMemberDetail() {
        MemberDetail detail = memberService.getMemberDetail(1);
        assertNotNull(detail);
        assertNotNull(detail.getMember());
        assertEquals(1, detail.getMember().getId());
        assertTrue(detail.getAge() > 0);
        assertTrue(detail.getMembershipDays() >= 0);

        // 打印详情
        System.out.println(detail.toString());
    }

    @Test
    public void testGetMemberDetailNotFound() {
        MemberDetail detail = memberService.getMemberDetail(99999);
        assertNull(detail);
    }

    // ==================== 会员统计测试 ====================

    @Test
    public void testGetTotalMemberCount() {
        int count = memberService.getTotalMemberCount();
        assertTrue(count > 0);
    }

    @Test
    public void testGetActiveMemberCount() {
        int count = memberService.getActiveMemberCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetTodayNewMemberCount() {
        int count = memberService.getTodayNewMemberCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMonthlyNewMemberCount() {
        int count = memberService.getMonthlyNewMemberCount();
        assertTrue(count >= 0);
    }

    @Test
    public void testGetMemberCountByStatus() {
        Map<String, Integer> countMap = memberService.getMemberCountByStatus();
        assertNotNull(countMap);
        assertTrue(countMap.containsKey(MemberDAO.STATUS_ACTIVE));
        assertTrue(countMap.containsKey(MemberDAO.STATUS_FROZEN));
        assertTrue(countMap.containsKey(MemberDAO.STATUS_INACTIVE));
    }

    @Test
    public void testGetMemberCountByGender() {
        Map<String, Integer> countMap = memberService.getMemberCountByGender();
        assertNotNull(countMap);
        assertTrue(countMap.containsKey(MemberDAO.GENDER_MALE));
        assertTrue(countMap.containsKey(MemberDAO.GENDER_FEMALE));
    }

    @Test
    public void testGetStatistics() {
        MemberStatistics stats = memberService.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalCount() > 0);
        assertTrue(stats.getActiveRate() >= 0 && stats.getActiveRate() <= 100);
        assertTrue(stats.getValidCardRate() >= 0 && stats.getValidCardRate() <= 100);

        // 打印统计
        System.out.println(stats.toString());
    }

    // ==================== 会员验证测试 ====================

    @Test
    public void testValidateMemberActive() {
        // 确保会员1是激活状态
        memberDAO.activateMember(1);

        ServiceResult<Member> result = memberService.validateMemberActive(1);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    public void testValidateMemberActiveNotFound() {
        ServiceResult<Member> result = memberService.validateMemberActive(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testValidateMemberActiveFrozen() {
        // 冻结会员1
        memberDAO.freezeMember(1);

        ServiceResult<Member> result = memberService.validateMemberActive(1);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("冻结"));

        // 恢复
        memberDAO.activateMember(1);
    }

    @Test
    public void testValidateMemberCanAccess() {
        // 确保会员1是激活状态
        memberDAO.activateMember(1);

        ServiceResult<Member> result = memberService.validateMemberCanAccess(1);
        // 结果取决于会员1是否有有效会员卡
        assertNotNull(result);
    }

    @Test
    public void testIsPhoneExists() {
        assertTrue(memberService.isPhoneExists("13900001111"));  // 张三的手机号
        assertFalse(memberService.isPhoneExists("99999999999"));
    }

    @Test
    public void testIsEmailExists() {
        // 测试已存在的邮箱
        Member member = memberDAO.getMemberById(1);
        if (member != null && member.getEmail() != null) {
            assertTrue(memberService.isEmailExists(member.getEmail()));
        }
        assertFalse(memberService.isEmailExists("notexist@email.com"));
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

    // ==================== 删除会员测试 ====================

    @Test
    public void testDeleteMember() {
        // 创建测试会员（无关联数据）
        ServiceResult<Member> registerResult = memberService.register(
                "待删除用户",
                "13811118888",
                "delete@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );
        assertTrue(registerResult.isSuccess());
        int memberId = registerResult.getData().getId();

        // 删除
        ServiceResult<Void> deleteResult = memberService.deleteMember(memberId);
        assertTrue(deleteResult.isSuccess());

        // 验证已删除
        assertNull(memberDAO.getMemberById(memberId));

        testMemberId = 0;  // 已删除，不需要清理
    }

    @Test
    public void testDeleteMemberNotFound() {
        ServiceResult<Void> result = memberService.deleteMember(99999);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    public void testDeleteMemberWithCard() {
        // 创建测试会员并开卡
        ServiceResult<Member> registerResult = memberService.registerWithMonthlyCard(
                "有卡待删除用户",
                "13811119999",
                "deletewithcard@email.com",
                MemberDAO.GENDER_MALE,
                DateUtils.now()
        );
        assertTrue(registerResult.isSuccess());
        testMemberId = registerResult.getData().getId();

        // 尝试非强制删除（应该失败）
        ServiceResult<Void> deleteResult = memberService.deleteMember(testMemberId, false);
        assertFalse(deleteResult.isSuccess());
        assertTrue(deleteResult.getMessage().contains("会员卡"));
    }
}

